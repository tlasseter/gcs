package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/18/11
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsAbstractFloatVector extends StsAbstractVector implements Cloneable
{
	protected double origin;
	private float maxValue = -Float.MAX_VALUE;
	private float minValue = Float.MAX_VALUE;

	private float minCutoff;
	private float maxCutoff;
	/** if non-zero, origin has been shifted this amount */
	protected double originOffset;
	private boolean offsetFromOrigin = false;
	public float nullValue = StsParameters.nullValue;
	// protected boolean isNull = true;

	// transient double originAdjust = 0.0;
	/** Histogram of the data distribution */
	transient public float[] dataHist = null;

	/** Total samples in each of 255 steps */
	transient private int dataCnt[] = new int[255];
	transient private int ttlHistogramSamples = 0;

	static final public float largeFloat = StsParameters.largeFloat;

	abstract public void setValues(float[] values, int size, int capacity);
	abstract public void setValues(float[] values);
	abstract public float[] getValues();

	public StsAbstractFloatVector()
	{
	}

	public StsAbstractFloatVector(boolean persistent)
	{
		super(persistent);
	}

	public void initialize(StsVectorSetLoader vectorSetLoader, boolean initializeValues)
	{
		this.capacity = vectorSetLoader.capacity;
		this.growInc = vectorSetLoader.growInc;
		this.nullValue = vectorSetLoader.nullValue;
		if(initializeValues) setValues(new float[capacity], 0, capacity);
		// reinitializeName();
	}

	public boolean setOrigin(double origin)
	{
		if(getValues() == null) return false;
		if(this.origin == origin) return false;
		this.origin = origin;
		return true;
	}

	public double getOrigin() { return origin; }

	public boolean checkSetOrigin(double origin)
	{
		double currentOrigin = getOrigin();
		if(currentOrigin == origin) return false;
		setOrigin(origin);
		return true;
	}

	public boolean checkLoadVector()
	{
		return getValues() != null;
	}

	public void checkAdjustOrigin(double modelOrigin)
	{
		float adjustOrigin = (float) (origin - modelOrigin);
		if(!StsMath.sameAs(adjustOrigin, 0.0f)) offsetValues(adjustOrigin);
		origin = modelOrigin;
		adjustRange(adjustOrigin);
	}

	public void checkSetValues(float[] values)
	{
		int size = values.length;
		setValues(values, size, size);
		checkSetMinMax();
	}

	public boolean checkSetMinMax()
	{
		isNull = true;
		float[] values = getValues();
		setMinValue(Float.MAX_VALUE);
		setMaxValue(-Float.MAX_VALUE);
		if(values == null || size == 0) return false;
		int n;
		for(n = 0; n < size; n++)
		{
			float value = values[n];
			if(value != StsParameters.nullValue)
			{
				setMinValue(value);
				setMaxValue(value);
				isNull = false;
				n++;
				break;
			}
		}
		for(; n < size; n++)
		{
			float value = values[n];
			if(value == StsParameters.nullValue) continue;
			if(value < getMinValue())
				setMinValue(value);
			else if(value > getMaxValue())
				setMaxValue(value);
		}
		return true;
	}

	public void adjustMinMax(float[] values)
	{
		for(float value : values)
		{
			if(value != StsParameters.nullValue)
			{
				if(value < getMinValue()) setMinValue(value);
				if(value > getMaxValue()) setMaxValue(value);
			}
		}
	}

	public void adjustMinMax(float[] values, int n)
	{
		for(; n < size; n++)
		{
			float value = values[n];
			if(value != StsParameters.nullValue)
			{
				if(value < getMinValue()) setMinValue(value);
				if(value > getMaxValue()) setMaxValue(value);
			}
		}
	}

	public void resetVector()
	{
		size = 0;
		setMaxValue(-Float.MAX_VALUE);
		setMinValue(Float.MAX_VALUE);
		checkSetValues(new float[0]);
		capacity = 1;
	}

	public float getFloat(int index)
	{
		return getValues()[index];
	}

	public void deleteFloat(int index)
	{
		float[] values = getValues();
		float[] newValues = new float[size - 1];
		if(index > 0) System.arraycopy(values, 0, newValues, 0, index);
		if(index < size - 1) System.arraycopy(values, index + 1, newValues, index, size - index - 1);
		values = newValues;
		size = size - 1;
	}

	public int insertBefore(float value, int index)
	{
		float[] values = getValues();
		float[] newValues = new float[size + 1];
		if(index > 0) System.arraycopy(values, 0, newValues, 0, index);
		newValues[index] = value;
		System.arraycopy(values, index, newValues, index + 1, size - index);
		values = newValues;
		size = size + 1;
		checkSetMinMax(value);
		return index;
	}

	public float getElement(int index)
	{
		try
		{ return getValues()[index]; }
		catch(Exception e) { return StsParameters.nullValue; }
	}

	// set value by index with optional min/max & monotonic calculations
	// assume value is not null
	public boolean setValue(int index, float value)
	{
		try
		{
			float[] values = getValues();
			values[index] = value;
			checkSetMinMax(value);
			checkMonotonic(index);
			return true;
		}
		catch(Exception e) { return false; }
	}

	public void checkSetMinMax(float value)
	{
		if(value == StsParameters.nullValue) return;
		if(size == 1)
		{
			setMinValue(value);
			setMaxValue(value);
		}
		else if(value < getMinValue())
			setMinValue(value);
		else if(value > getMaxValue())
			setMaxValue(value);
	}

	/**
	 * Check if value is null because 1) it is out of reasonable range, or 2) it is the defined nullValue.
	 * On the first nonNull, reset the origin (if origin is being used: offsetFromOrigin = true).
	 * All vector are offsets from origin and first value is 0, i.e., absolute coordinate is origin value.
	 */
	public void appendOffset(double value)
	{
		if(isNullValue((float)value))
			append(StsParameters.nullValue);
		else if(isNull) // origin not set yet
		{
			origin = value;
			append(0.0f);
			isNull = false;
		}
		else
			append((float)(value - origin));
	}

	private float checkGetNull(float value)
	{
		if(isNullValue(value))
			return StsParameters.nullValue;
		else
			return value;
	}

	public boolean isNullValue(double value)
	{
		float floatValue = (float)value;
		return floatValue == nullValue || floatValue <= -Float.MAX_VALUE || floatValue >= Float.MAX_VALUE;
	}

	public boolean append(float value)
	{
		value = checkGetNull(value);
		float[] values;
		if(size >= capacity)
		{
			if(growInc == 0) return false;
			capacity = size + growInc;
			float[] oldValues = getValues();
			values = new float[capacity];
			if(oldValues != null)
				System.arraycopy(oldValues, 0, values, 0, size);
			setValues(values, size, capacity);
		}
		else
			values = getValues();
		if(value != StsParameters.nullValue) isNull = false;
		values[size++] = value;
		checkSetMinMax(value);
		return true;
	}

	public boolean appendToken(String token)
	{
		try
		{
			append(Float.parseFloat(token));
			return true;
		}
		catch(Exception e)
		{
			StsMessageFiles.errorMessage("Failed to parse token to float: " + token);
			return false;
		}
	}

	public float getMinValue() { return minValue; }

	public float getMaxValue() { return maxValue; }

	public void setMinValue(float minValue)
	{
		this.minValue = minValue;
		this.minCutoff = minValue;
	}

	public void setMaxValue(float maxValue)
	{
		this.maxValue = maxValue;
		this.maxCutoff = maxValue;
	}

	public float getFirst() { return getValues()[0]; }

	public float getLast() {return getValues()[size - 1]; }

	public void adjustRange(float adjustment)
	{
		if(getMinValue() != largeFloat) setMinValue(getMinValue() + adjustment);
		if(getMaxValue() != -largeFloat) setMaxValue(getMaxValue() + adjustment);
	}

	public float getIncrement()
	{
		float[] values = getValues();
		if(values == null) return largeFloat;
		return values[1] - values[0];
	}

	/* calculate min and max for all the vector */

	/**
	 * Sets the min-max range for the vector and
	 * converts vector which equal the given nullValue to the standard nullValue
	 * and checks whether the complete vector is null.
	 * @param nullValue compare vector to this nullValue
	 * @return true if all vector are null
	 */
	public boolean setMinMaxAndNulls(float nullValue)
	{
		float[] values = getValues();
		if(values == null || size == 0)
			isNull = false;

		isNull = true;
		setMinValue(largeFloat);
		setMaxValue(-largeFloat);
		for(int i = 0; i < size; i++)
		{
			float value = values[i];
			if(isNullValue(value))
				values[i] = StsParameters.nullValue;
			else
			{
				isNull = false;
				if(value < getMinValue()) setMinValue(value);
				if(value > getMaxValue()) setMaxValue(value);
			}
		}
		return isNull;
	}

	public boolean setMinMax()
	{
		float[] values = getValues();
		if(values == null || size == 0) return false;
		setMinValue(values[0]);
		setMaxValue(values[0]);
		for(int i = 1; i < size; i++)
		{
			float value = values[i];
			if(value < getMinValue()) setMinValue(value);
			else if(value > getMaxValue()) setMaxValue(value);
		}
		return false;
	}

	public void resetMinMaxCutoffs()
	{
		minCutoff = StsParameters.largeFloat;
		maxCutoff = -StsParameters.largeFloat;
	}

	/** calculate monotonic value for vector */
	public void checkMonotonic()
	{
		if(size <= 1)
		{
			monotonic = MONOTONIC_UNKNOWN;
			return;
		}
		float[] values = getValues();
		if(values[0] == values[1]) // can't be monotonic
		{
			monotonic = MONOTONIC_NOT;
			return;
		}
		monotonic = (values[1] > values[0]) ? MONOTONIC_INCR : MONOTONIC_DECR;
		// see if increments stay in same direction
		float lastVal = values[1];
		for(int i = 2; i < size; i++)
		{
			float val = values[i];
			checkMonotonic(lastVal, val);
			lastVal = val;
		}
		// if we get to here, the vector are monotonic
	}

	private void checkMonotonic(float lastVal, float val)
	{
		if(val < lastVal)
		{
			if(monotonic == MONOTONIC_INCR) monotonic = MONOTONIC_NOT;
			return;
		}
		else if(val > lastVal)
		{
			if(monotonic == MONOTONIC_DECR) monotonic = MONOTONIC_NOT;
			return;
		}
		else
			monotonic = MONOTONIC_NOT;
	}

	private void checkMonotonic(int index)
	{
		float[] values = getValues();
		if(index > 0) checkMonotonic(values[index - 1], values[index]);
		if(index < size - 1) checkMonotonic(values[index], values[index + 1]);
	}

	/** get monotonic value */
	public int getMonotonic()
	{ return monotonic; }

	/** get monotonic status */
	public boolean isMonotonic()
	{
		if(monotonic == MONOTONIC_UNKNOWN) checkMonotonic();
		return monotonic != MONOTONIC_NOT;
	}

	public boolean isMonotonicIncreasing() { return monotonic == MONOTONIC_INCR; }

	/** get min/max indices enclosed by a range of vector */
	public int[] getIndicesInValueRange(float minValue, float maxValue)
	{
		if(!isMonotonic()) return null; // range checks not valid
		if(size == 0) return null;
		float sign = (monotonic == MONOTONIC_DECR) ? -1.0f : 1.0f;
		if(sign < 0)
		{
			minValue *= sign;
			maxValue *= sign;
		}
		if(minValue > maxValue)
		{
			float temp = minValue;
			minValue = maxValue;
			maxValue = temp;
		}
		int[] indexRange = new int[2];
		// get start index
		float[] values = getValues();
		if(minValue < values[0] * sign) indexRange[0] = 0;
		else if(minValue > values[size - 1] * sign) indexRange[0] = size;
		else
		{
			for(int i = 0; i < size; i++)
			{
				if(minValue <= values[i] * sign)
				{
					indexRange[0] = i;
					break;
				}
			}
		}
		// get end index
		if(maxValue < values[0] * sign) indexRange[1] = -1;
		else if(maxValue > values[size - 1] * sign) indexRange[1] = size - 1;
		else
		{
			for(int i = 0; i < size; i++)
			{
				if(maxValue == values[i] * sign)
				{
					indexRange[1] = i;
					break;
				}
				else if(maxValue < values[i] * sign)
				{
					indexRange[1] = i - 1;
					break;
				}
			}
		}
		if(indexRange[1] < indexRange[0]) return null;
		return indexRange;
	}

	public void trimToSize()
	{
		float[] values = getValues();
		if(isNull()) values = null;
		if(values == null)
		{
			size = 0;
			capacity = 0;
			return;
		}
		float[] newValues = new float[size];
		System.arraycopy(values, 0, newValues, 0, size);
		setValues(newValues, size, size);
		capacity = size;
	}

	public boolean hasValues()
	{
		return getValues() != null;
	}

	public boolean isConstant() { return getMinValue() == getMaxValue(); }

	public void resortWithIndex(int[] sortIndex)
	{
		float[] newValues = new float[capacity];
		float[] values = getValues();
		for(int n = 0; n < size; n++)
			newValues[n] = values[sortIndex[n]];
		values = newValues;
	}

	public float getIndexFactor(float value)
	{
		float[] values = getValues();
		return StsMath.binarySearchF(values, size, value);
	}

	public int getIndex(float value)
	{
		float[] values = getValues();
		return StsMath.binarySearch(values, size, value);
	}

	public IndexF getIndexF(float value)
	{
		return getIndexF(value, false, false);
	}

	public IndexF getIndexF(float value, boolean extrapolate)
	{
		if(extrapolate)
			return getIndexF(value, true, false);
		else
			return getIndexF(value, false, false);
	}

	/** Assumes independent vector is monotonic increasing. */
	public StsAbstractVector.IndexF getIndexF(float value, boolean extrapolate, boolean nearest)
	{
		float prevValue, nextValue;

		float[] values = getValues();
		if(values == null) return null;
		int nValues = getSize();
		if(nValues < 2) return null;
		if(values[1] <= values[0])
		{
			StsException.systemError(this, "getIndexF", "Does not handle decreasing logVector");
			return null;
		}
		int index;

		if(value <= values[0])
		{
			if(!extrapolate)
			{
				if(nearest)
					return new StsAbstractVector.IndexF(0, 0.0f);
				else
					return null;
			}
			else
				index = 0;
		}
		else if(value >= values[nValues - 1])
		{
			if(!extrapolate)
			{
				if(nearest)
					return new StsAbstractVector.IndexF(nValues - 1, 0.0f);
				else
					return null;
			}
			else
				index = nValues - 2;
		}
		else
		{
			float indexF = StsMath.binarySearchF(values, nValues, value);
			return new IndexF(indexF);
		}

		prevValue = values[index];
		nextValue = values[index + 1];
		float f = (value - prevValue) / (nextValue - prevValue);
		return new IndexF(index, f);
	}

	/** vector are checked against this null; if null or out of range, value is skipped */
	public void setNullValue(float nullValue)
	{ this.nullValue = nullValue; }

	/** isNull indicates vector is all nulls */
	public boolean isNull() { return isNull; }

	public void setVersion(int version) { this.version = version; }
	public int getVersion() { return version; }

	public void resetMinMaxValue(float val)
	{
		if(val > getMaxValue())
			setMaxValue(val);
		if(val < getMinValue())
			setMinValue(val);
	}

	public void resetMinMaxValue(double[] vals)
	{
		for(int i = 0; i < vals.length; i++)
		{
			if(vals[i] > getMaxValue())
				setMaxValue((float) vals[i]);
			if(vals[i] < getMinValue())
				setMinValue((float) vals[i]);
		}
	}

	public void setName(String name) { this.name = name; }

	public void offsetValues(float offset)
	{
		float[] values = getValues();
		if(values == null) return;
		for(int n = 0; n < size; n++)
			values[n] += offset;
	}

	/** validate and set min/max cutoffs */
	public void setCutoffs(float minCutoff, float maxCutoff)
	{
		if(minCutoff > maxCutoff)
		{
			StsException.systemError("StsDataVector.setCutoffs failed." +
					" min cutoff must be less than max cutoff");
			return;
		}
		this.minCutoff = minCutoff;
		this.maxCutoff = maxCutoff;
	}

	public void setOriginOffset(double offset)
	{
		originOffset = offset;
	}

	public void clearArray() { checkSetValues(null); }
/*
	public boolean checkLoadVector()
	{
		float[] values = getValues();
		return values != null;
	}

	public float[] checkGetValues()
	{
		float[] floatValues = checkGetValues();
		return floatValues;
	}
*/

	private InputStream getInputStream(URL url)
	{
		try
		{
			URLConnection conn = url.openConnection();
			return conn.getInputStream();
		}
		catch(Exception e)
		{
			StsException.outputException("StsSelectCurveTableModel.getInputStream() failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	public void append(double value)
	{
		append((float) value);
	}

	public void writeAppend(double value)
	{
		setGrowIncrement(1);
		append(value);
		checkSetMinMax();
	}

	public void writeReplaceAt(double value, int index)
	{
		setValue(index, (float) value);
		checkSetMinMax();
	}

	public void writeAppend(double[] vals)
	{
		setGrowIncrement(1);
		append(vals);
		checkSetMinMax();
	}

	public boolean append(float[] newValues)
	{
		float[] values = getValues();
		if(values == null) return false;
		int nNewValues = newValues.length;
		int nOldValues = size;
		int nTotalValues = nOldValues + nNewValues;
		float[] totalValues = new float[nTotalValues];
		System.arraycopy(values, 0, totalValues, 0, size);
		System.arraycopy(newValues, 0, totalValues, size, nNewValues);
		checkSetValues(totalValues);
		size = nTotalValues;
		adjustMinMax(totalValues, nOldValues);
		return true;
	}

	public boolean append(double[] newDoubleValues)
	{
		float[] values = getValues();
		if(values == null) return false;
		int nNewValues = newDoubleValues.length;
		float[] newValues = new float[nNewValues];
		if(!StsMath.copyDoublesToFloats(newDoubleValues, newValues)) return false;
		int nOldValues = size;
		int nTotalValues = nOldValues + nNewValues;
		float[] totalValues = new float[nTotalValues];
		System.arraycopy(values, 0, totalValues, 0, size);
		System.arraycopy(newValues, 0, totalValues, size, nNewValues);
		size = nTotalValues;
		setValues(totalValues, size, size);
		checkSetValues(totalValues);
		adjustMinMax(totalValues, nOldValues);
		return true;
	}

	public float getMinCutoff()
	{
		return minCutoff;
	}

	public void setMinCutoff(float minCutoff)
	{
		this.minCutoff = minCutoff;
	}

	public float getMaxCutoff()
	{
		return maxCutoff;
	}

	public void setMaxCutoff(float maxCutoff)
	{
		this.maxCutoff = maxCutoff;
	}

	public void applyScalar(float scalar)
	{
		if(scalar == 1.0f) return;
		double origin = getOrigin();
		origin *= scalar;
		setOrigin(origin);
		float[] values = getValues();
		for(int n = 0; n < values.length; n++)
			values[n] *= scalar;
		setMinValue(getMinValue() * scalar);
		setMaxValue(getMaxValue() * scalar);
	}

	public float getValue(int index)
	{
		float[] values = getValues();
		if(values == null)
		{
			StsException.systemError(this, "getValue" + " values are null for vector " + name);
			return 0.0f;
		}
		if(values.length < index + 1)
		{
			StsException.systemError(this, "getValue" + " index " + index + " out of range for vector " + name);
			return 0.0f;
		}
		return getValues()[index];
	}

	public void setValue(float value, int index)
	{
		float[] values = getValues();
		values[index] = value;
	}

	public float getValue(float indexF)
	{
		float[] values = getValues();
		if(values == null) return StsParameters.nullValue;
		int nValues = values.length;
		if(nValues < 2) return StsParameters.nullValue;
		int index = (int) indexF;
		index = StsMath.minMax(index, 0, nValues - 2);
		float f = indexF - index;
		float prevValue = values[index];
		float nextValue = values[index + 1];
		return prevValue + f * (nextValue - prevValue);
	}

	public float getValue(IndexF indexF)
	{
		float[] values = getValues();
		if(values == null) return StsParameters.nullValue;
		int nValues = values.length;
		if(nValues < 2) return StsParameters.nullValue;
		int index = indexF.index;
		index = StsMath.minMax(index, 0, nValues - 2);
		float f = indexF.f;
		float prevValue = values[index];
		float nextValue = values[index + 1];
		return prevValue + f * (nextValue - prevValue);
	}

	static public StsAbstractFloatVector getVectorWithName(StsObjectRefList abstractVectors, String name)
	{
		return (StsAbstractFloatVector) StsAbstractVector.getVectorWithName(abstractVectors, name);
	}

	static public StsAbstractFloatVector getVectorWithNameInList(StsObjectRefList abstractVectors, String[] names)
	{
		return (StsAbstractFloatVector) StsAbstractVector.getVectorWithNameInList(abstractVectors, names);
	}

	static public StsAbstractFloatVector getVectorWithNameInList(StsAbstractFloatVector[] vectors, String[] names)
	{
		return (StsAbstractFloatVector) StsAbstractVector.getVectorWithNameInList(vectors, names);
	}

	static public StsAbstractFloatVector getVectorWithName(StsAbstractFloatVector[] abstractVectors, String name)
	{
		if(abstractVectors == null) return null;
		int nVectors = abstractVectors.length;
		for(int n = 0; n < nVectors; n++)
		{
			if(abstractVectors[n] != null && abstractVectors[n].name.equals(name))
				return abstractVectors[n];

		}
		return null;
	}

	static public StsAbstractFloatVector getElement(StsObjectRefList abstractVectors, int i)
	{
		return (StsAbstractFloatVector) StsAbstractVector.getElement(abstractVectors, i);
	}

	public float[] copyScaleClipNormalizeValues(boolean clipRange, boolean scaleLinear)
	{
		float minScale, maxScale;
		float[] floats = getValues();
		int nValues = getSize();

		minScale = minCutoff;
		maxScale = maxCutoff;

		float scale;
		float[] scaledFloats = new float[nValues];
		if(!scaleLinear) // log scale the values
		{
			minScale = (float) Math.log10(minScale);
			maxScale = (float) Math.log10(maxScale);
			for(int n = 0; n < floats.length; n++)
				floats[n] = (float) Math.log10(floats[n]);
		}
		scale = 1.0f / (maxScale - minScale);
		for(int n = 0; n < floats.length; n++)
		{
			float value = floats[n];
			if(clipRange)
			{
				if(value > maxCutoff)
					scaledFloats[n] = 1.0f;
				else if(value <= minCutoff)
					scaledFloats[n] = 0.0f;
			}
			else
				scaledFloats[n] = (value - minScale) * scale;
		}
		return scaledFloats;
	}

	public float copyScaleClipNormalizeValue(int index, boolean clipRange, boolean scaleLinear)
	{
		float minScale, maxScale;
		float[] floats = getValues();
		int nValues = getSize();

		minScale = minCutoff;
		maxScale = maxCutoff;

		float scale;
		float scaledFloat = floats[index];
		if(!scaleLinear) // log scale the values
		{
			minScale = (float) Math.log10(minScale);
			maxScale = (float) Math.log10(maxScale);
			scaledFloat = (float) Math.log10(scaledFloat);
		}
		scale = 1.0f / (maxScale - minScale);
		if(clipRange)
		{
			if(scaledFloat > maxCutoff)
				scaledFloat = 1.0f;
			else if(scaledFloat <= minCutoff)
				scaledFloat = 0.0f;
		}
		else
			scaledFloat = (scaledFloat - minScale) * scale;
		return scaledFloat;
	}

	public float[] copyScaleClipSizeValues(boolean clipRange, boolean scaleLinear, float minSize, float maxSize)
	{
		float[] floats = getValues();
		float scale = maxSize - minSize;
		float[] scaledFloats = copyScaleClipNormalizeValues(clipRange, scaleLinear);

		for(int n = 0; n < floats.length; n++)
			scaledFloats[n] = scaledFloats[n] * scale + minSize;
		return scaledFloats;
	}

	public float copyScaleClipSizeValue(int index, boolean clipRange, boolean scaleLinear, float minSize, float maxSize)
	{
		float[] floats = getValues();
		float scale = maxSize - minSize;
		float normalizedValue = copyScaleClipNormalizeValue(index, clipRange, scaleLinear);
		return scale * normalizedValue + minSize;
	}

	static public float interpolateValue(float inputValue, StsAbstractFloatVector inputVector, StsAbstractFloatVector outputVector)
	{
		float[] inputValues = inputVector.getValues();
		if(inputValues == null) return StsParameters.nullValue;
		float[] outputValues = outputVector.getValues();
		if(outputValues == null) return StsParameters.nullValue;
		return StsMath.interpolateValue(inputValue, inputValues, outputValues);
	}

	public float[] getHistogram()
	{
		if(dataHist != null)
			return dataHist;
		dataHist = new float[255];
		clearHistogram();
		int size = getSize();
		for(int i = 0; i < size; i++)
		{
			accumulateHistogram(getValues()[i]);
		}
		calculateHistogram();
		return dataHist;
	}

	public void accumulateHistogram(int bindex)
	{
		if(bindex > 254)
		{
			bindex = 254;
		}
		if(bindex < 0)
		{
			bindex = 0;
		}
		dataCnt[bindex] = dataCnt[bindex] + 1;
		ttlHistogramSamples++;
	}

	private void accumulateHistogram(float value)
	{
		float scaledFloat = 254.0f * (float) (((double) value - getMinValue()) / (getMaxValue() - getMinValue()));
		int scaledInt = StsMath.minMax(Math.round(scaledFloat), 0, 254);
		accumulateHistogram(scaledInt);
	}

	public void calculateHistogram()
	{
		for(int i = 0; i < 255; i++)
		{
			dataHist[i] = (float) ((float) dataCnt[i] / (float) ttlHistogramSamples) * 100.0f;
		}
	}

	public void clearHistogram()
	{
		for(int i = 0; i < 255; i++)
		{
			dataCnt[i] = 0;
			dataHist[i] = 0.0f;
		}
		ttlHistogramSamples = 0;
	}

	public void deletePoint(int index)
	{
		deleteFloat(index);
	}

	public int insertAfter(float value, int index)
	{
		return insertBefore(value, index + 1);
	}

	public float[] getRelativeRange()
	{
		return new float[]{getMinValue(), getMaxValue()};
	}

	public double[] getAbsoluteRange()
	{
		double origin = getOrigin();
		return new double[]{origin + getMinValue(), origin + getMaxValue()};
	}

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "clone", e);
			return null;
		}
	}

	public void setValuesFromPoints(String name, ArrayList<StsPoint> points, int index)
	{
		int nValues = points.size();
		float[] values = new float[nValues];
		for(int n = 0; n < nValues; n++)
			values[n] = points.get(n).v[index];
		checkSetValues(values);
	}

	public boolean isOffsetFromOrigin()
	{
		return offsetFromOrigin;
	}

	public void setOffsetFromOrigin(boolean offsetFromOrigin)
	{
		this.offsetFromOrigin = offsetFromOrigin;
	}

	static public float getInterpolatedVectorFloat(StsAbstractFloatVector inputVector, float inputValue, StsAbstractFloatVector outputVector)
	{
		if(inputVector == null || outputVector == null) return StsParameters.nullValue;
        StsAbstractVector.IndexF indexF  = inputVector.getIndexF(inputValue);
		if(indexF == null) return StsParameters.nullValue;
		return outputVector.getValue(indexF);
	}
}
