package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.net.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/18/11
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsAbstractLongVector extends StsAbstractVector implements Cloneable
{
	protected long origin;
	protected long maxValue = -Long.MAX_VALUE;
	protected long minValue = Long.MAX_VALUE;

    protected long minCutoff;
    protected long maxCutoff;
	private long originOffset;
	protected boolean offsetFromOrigin = true;

	// protected boolean isNull = true;

	// transient double originAdjust = 0.0;
/** Histogram of the data distribution */
    transient public float[] dataHist = null;

    /** Total samples in each of 255 steps */
    transient private int dataCnt[] = new int[255];
    transient private int ttlHistogramSamples = 0;

	static final public long largeLong = StsParameters.largeLong;
	static final public long nullValue = StsParameters.nullLongValue;

	public abstract void initializeValues(long[] values);
    abstract public void setValues(long[] values, int size, int capacity);
	abstract public void setValues(long[] values);
	abstract public long[] getValues();

	public StsAbstractLongVector()
    {
    }

	public StsAbstractLongVector(boolean persistent)
	{
		super(persistent);
	}

	public void initialize(StsVectorSetLoader vectorSetLoader, boolean initializeValues)
	{
		capacity = vectorSetLoader.capacity;
		growInc = vectorSetLoader.growInc;
		if(initializeValues) initializeValues(new long[capacity]);
	}

    /** set an array */
    public void setValues(long[] values, int length)
    {
        setValues(values);
        size = (values == null) ? 0 : Math.min(values.length, length);
        checkSetMinMax();
    }

	public boolean setOrigin(long origin)
	{
		if(getValues() == null) return false;
		if(this.origin == origin) return false;
		this.origin = origin;
		return true;
	}

	public long getOrigin() { return origin; }

    public void checkAdjustOrigin(long modelOrigin)
    {
        long adjustOrigin = origin - modelOrigin;
        if(!StsMath.sameAs(adjustOrigin, 0l)) offsetValues(adjustOrigin);
        origin = modelOrigin;
        adjustRange(adjustOrigin);
    }

    public boolean checkSetMinMax()
    {
		isNull = true;
		long[] values = getValues();
        if(values == null) return false;
		if(size == 0) return false;
        minValue = Long.MAX_VALUE;
        maxValue = -Long.MAX_VALUE;
        int n;
        for(n = 0; n < size; n++)
        {
            long value = values[n];
            if(value != StsParameters.nullValue)
            {
                minValue = value;
                maxValue = value;
				isNull = false;
                break;
            }
        }
        for(; n < size; n++)
        {
            long value = values[n];
            if(value < minValue)
                minValue = value;
            else if(value > maxValue)
                maxValue = value;
        }
        return true;
    }

    public void resetVector()
    {
        size = 0;
        maxValue = -Long.MAX_VALUE;
        minValue = Long.MAX_VALUE;
        setValues(new long[0]);
        capacity = 1;
    }

    public long getLong(int index)
    {
        return getValues()[index];
    }

    public void deleteValues()
	{
		setValues(null);
		size = 0;
		capacity = 0;
	}

    public void clearValues()
	{
		setValues(null);
	}

    public void deleteLong(int index)
    {
		long[] values = getValues();
        long[] newValues = new long[size - 1];
        if (index > 0) System.arraycopy(values, 0, newValues, 0, index);
        if (index < size - 1) System.arraycopy(values, index + 1, newValues, index, size - index - 1);
        values =  newValues;
        size = size - 1;
    }

    public int insertBefore(long value, int index)
    {
		long[] values = getValues();
        long[] newValues = new long[size+1];
        if(index > 0) System.arraycopy(values, 0, newValues, 0, index);
        newValues[index] = value;
        System.arraycopy(values, index, newValues, index+1, size - index);
        values = newValues;
        size = size+1;
        checkSetMinMax(value);
        return index;
    }

    public long getElement(int index)
    {
        try { return getValues()[index]; }
        catch (Exception e) { return nullValue; }
    }

    // set value by index with optional min/max & monotonic calculations
    // assume value is not null
    public boolean setValue(int index, long value)
    {
        try
        {
			long[] values = getValues();
            values[index] = value;
            checkSetMinMax(value);
            checkMonotonic(index);
            return true;
        }
        catch (Exception e) { return false; }
    }

    public void checkSetMinMax(long value)
    {
		if(value == nullValue) return;
        if (value < minValue) minValue = value;
        if (value > maxValue) maxValue = value;
		isNull = false;
    }

    /** how much to grow array on append? */
    public void setGrowIncrement(int inc) { growInc = inc; }

	public boolean append(long value)
	{
		long[] values;
        if (size >= capacity)
        {
            if (growInc == 0) return false;
            capacity = size + growInc;
			long[] oldValues = getValues();
			values = new long[capacity];
			if(oldValues != null)
				System.arraycopy(oldValues, 0, values, 0, size);
			setValues(values, size, capacity);
        }
		else
			values = getValues();
		if(value == StsParameters.nullLongValue)
		{
			if(size == 0)
				value = 0L;
			else
				value = values[size] + 1;
		}
		values[size++] = value;
        checkSetMinMax(value);
        return true;
    }

	public boolean appendToken(String token)
	{
		try
		{
			append(Long.parseLong(token));
			return true;
		}
		catch(Exception e)
		{
			StsMessageFiles.errorMessage("Failed to parse token to long: " + token);
			return false;
		}
	}

    public long getMinValue() { return minValue; }
    public long getMaxValue() { return maxValue; }
    public void setMinValue(long minValue) { this.minValue = minValue; }
    public void setMaxValue(long maxValue) { this.maxValue = maxValue; }

	public long getFirst() { return getValue(0);
	}
	public long getLast() {return getValue(size-1); }

    public void adjustRange(long adjustment)
    {
        if(minValue != largeLong) minValue += adjustment;
        if(maxValue != -largeLong) maxValue += adjustment;
    }

    public long getIncrement()
    {
		long[] values = getValues();
        if (values == null) return largeLong;
        return values[1] - values[0];
    }

    /* calculate min and max for all the vector */

	/** Sets the min-max range for the vector and
     *  converts vector which equal the given nullValue to the standard nullValue
     *  and checks whether the complete vector is null.
     *  @param nullValue compare vector to this nullValue
	 *  @return true if all vector are null
	 */
	public boolean setMinMaxAndNulls(long nullValue)
	{
		long[] values = getValues();
		if (values == null || size == 0)
			isNull = false;

		isNull = true;
		minValue = largeLong;
		maxValue = -largeLong;
		for (int i = 0; i < size; i++)
		{
			long value = values[i];
			if(value <= -largeLong || value >= largeLong)
				values[i] = nullValue;
			else if(value == nullValue)
                values[i] = nullValue;
            else
			{
				isNull = false;
				if (value < minValue) minValue = value;
				if (value > maxValue) maxValue = value;
			}
		}
		return isNull;
	}

	public boolean setMinMax()
	{
		long[] values = getValues();
		if(values == null || size == 0) return false;
		minValue = values[0];
		maxValue = values[0];
		for (int i = 1; i < size; i++)
		{
			long value = values[i];
			if (value < minValue) minValue = value;
			else if (value > maxValue) maxValue = value;
		}
		return false;
	}

    /** calculate monotonic value for vector */
    public byte checkMonotonic()
    {
        if (size <= 1)
        {
            monotonic = MONOTONIC_UNKNOWN;
            return monotonic;
        }
		long[] values = getValues();
        if (values[0] == values[1]) // can't be monotonic
        {
            monotonic = MONOTONIC_NOT;
            return monotonic;
        }
        monotonic = (values[1] > values[0]) ? MONOTONIC_INCR : MONOTONIC_DECR;
        // see if increments stay in same direction
        long lastVal = values[1];
        for (int i = 2; i < size; i++)
        {
            long val = values[i];
            checkMonotonic(lastVal, val);
            lastVal = val;
        }
        return  monotonic;
    }

    private void checkMonotonic(long lastVal, long val)
    {
        if (val < lastVal)
        {
            if (monotonic == MONOTONIC_INCR) monotonic = MONOTONIC_NOT;
            return;
        }
        else if (val > lastVal)
        {
            if (monotonic == MONOTONIC_DECR) monotonic = MONOTONIC_NOT;
            return;
        }
        else
            monotonic = MONOTONIC_NOT;
    }

    private void checkMonotonic(int index)
    {
		long[] values = getValues();
        if (index > 0) checkMonotonic(values[index - 1], values[index]);
        if (index < size - 1) checkMonotonic(values[index], values[index + 1]);
    }

    /** get monotonic value */
    public int getMonotonic() { return monotonic; }

    /** get monotonic status */
    public boolean isMonotonic()
    {
        if (monotonic == MONOTONIC_UNKNOWN) checkMonotonic();
        return monotonic != MONOTONIC_NOT;
    }

    public boolean isMonotonicIncreasing() { return monotonic == MONOTONIC_INCR; }

    /** get min/max indices enclosed by a range of vector */
    public int[] getIndicesInValueRange(long minValue, long maxValue)
    {
        if (!isMonotonic()) return null; // range checks not valid
        if (size == 0) return null;
        int sign = (monotonic == MONOTONIC_DECR) ? -1 : 1;
        if (sign < 0) { minValue *= sign; maxValue *= sign; }
        if (minValue > maxValue)
        {
            long temp = minValue;
            minValue = maxValue;
            maxValue = temp;
        }
        int[] indexRange = new int[2];
        // get start index
		long[] values = getValues();
        if (minValue < values[0] * sign) indexRange[0] = 0;
        else if (minValue > values[size - 1] * sign) indexRange[0] = size;
        else
        {
            for (int i = 0; i < size; i++)
            {
                if (minValue <= values[i] * sign)
                {
                    indexRange[0] = i;
                    break;
                }
            }
        }
        // get end index
        if (maxValue < values[0] * sign) indexRange[1] = -1;
        else if (maxValue > values[size - 1] * sign) indexRange[1] = size - 1;
        else
        {
            for (int i = 0; i < size; i++)
            {
                if (maxValue == values[i] * sign)
                {
                    indexRange[1] = i;
                    break;
                }
                else if (maxValue < values[i] * sign)
                {
                    indexRange[1] = i - 1;
                    break;
                }
            }
        }
        if (indexRange[1] < indexRange[0]) return null;
        return indexRange;
    }

    public void trimToSize()
    {
		long[] values = getValues();
		if(values == null) return;
        long[] newVector = new long[size];
        System.arraycopy(values, 0, newVector, 0, size);
        setValues(newVector);
        capacity = size;
    }

	public boolean hasValues()
	{
		return getValues() != null;
	}

    public boolean isConstant() { return minValue == maxValue; }

    public void resortWithIndex(int[] sortIndex)
    {
        long[] newValues = new long[capacity];
		long[] values = getValues();
        for(int n = 0; n < size; n++)
            newValues[n] = values[sortIndex[n]];
        values = newValues;
    }

	/** isNull indicates vector is all nulls */
	public boolean isNull() { return isNull; }


    public void setVersion(int version) { this.version = version; }
    public int getVersion() { return version; }

	public void resetMinMaxValue(long val)
	{
    	if(val > getMaxValue())
    		setMaxValue(val);
    	if(val < getMinValue())
    		setMinValue(val);
	}

	public void resetMinMaxValue(double[] vals)
	{
        for(int i=0; i<vals.length; i++)
        {
    	    if(vals[i] > getMaxValue())
    		    setMaxValue((long)vals[i]);
    	    if(vals[i] < getMinValue())
    		    setMinValue((long)vals[i]);
        }
	}

    public void setName(String name) { this.name = name; }

    public void checkAdjustOrigin(double modelOrigin)
    {
		long[] values = getValues();
        if(values == null) return;
		checkAdjustOrigin(modelOrigin);
    }

    public void offsetValues(long offset)
    {
        long[] values = getValues();
        if(values == null) return;
		for(int n = 0; n < size; n++)
			values[n] += offset;
    }

    /** validate and set min/max cutoffs */
    public void setCutoffs(long minCutoff, long maxCutoff)
    {
	    if (minCutoff > maxCutoff)
    	{
            StsException.systemError("StsDataVector.setCutoffs failed." +
                    " min cutoff must be less than max cutoff");
            return;
 	    }
    	this.minCutoff = minCutoff;
    	this.maxCutoff = maxCutoff;
    }

	public void setOriginOffset(long offset)
	{
		originOffset = offset;
	}

    public void clearArray() { setValues(null); }

   public boolean checkVector()
   {
       long[] longValues = getValues();
	   return longValues != null;
   }

   public boolean checkLoadVector()
   {
	   long[] values = getValues();
	   return values != null;
   }

   public long[] checkGetValues()
   {
	   long[] longValues = getValues();
       return longValues;
   }

   public boolean checkLoadVector(String directory)
   {
       return checkLoadVector();
   }

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
        append((long) value);
	}

	public void writeAppend(double value)
	{
		setGrowIncrement(1);
		append(value);
        checkSetMinMax();
	}

	public void writeReplaceAt(double value, int index)
	{
        setValue(index, (long) value);
        checkSetMinMax();
	}

	public void writeAppend(long[] vals)
	{
        setGrowIncrement(1);
		append(vals);
        checkSetMinMax();
	}

    public boolean append(long[] newLongValues)
    {
		long[] values = getValues();
        if(values == null) return false;
        int nNewValues = newLongValues.length;
		int nOldValues = size;
        int nTotalValues = nOldValues + nNewValues;
        long[] totalValues = new long[nTotalValues];
        System.arraycopy(values, 0, totalValues, 0, size);
        System.arraycopy(newLongValues, 0, totalValues, size, nNewValues);
		setValues(totalValues);
        size = nTotalValues;
        adjustMinMax(totalValues, nOldValues);
        return true;
    }

	public void adjustMinMax(long[] values)
	{
		for(long value : values)
		{
			if(value != StsParameters.nullValue)
			{
			   if (value < minValue) minValue = value;
			   if (value > maxValue) maxValue = value;
			}
		}
	}

	public void adjustMinMax(long[] values, int n)
	{
		for(; n < size; n++)
		{
			long value = values[n];
			if(value != StsParameters.nullValue)
			{
			   if (value < minValue) minValue = value;
			   if (value > maxValue) maxValue = value;
			}
		}
	}

    public IndexF getIndexF(long value)
    {
        return getIndexF(value, true);
    }

	public float getIndexFactor(long value)
	{
		long[] values = getValues();
		return StsMath.binarySearchF(values, size, value);
	}

	public int getIndex(long value)
	{
		long[] values = getValues();
		return StsMath.binarySearch(values, size, value);
	}

    /** Assumes independent vector is monotonic increasing. */
    public IndexF getIndexF(long value, boolean extrapolate)
    {
        long prevValue, nextValue;

        if(!checkLoadVector()) return null;
        long[] values = getValues();
        int nValues = getSize();
        if(nValues < 2) return null;
        if(values[1] <= values[0])
        {
            StsException.systemError(this, "getIndexF", "Does not handle decreasing logVector");
            return new IndexF(0, 0l);
        }
        int index;

        if(value <= values[0])
        {
            if(!extrapolate)
                return new IndexF(0, 0l);
            else
                index = 0;
        }
        else if(value >= values[nValues-1])
        {
            if(!extrapolate)
                return new IndexF(nValues-1, 0l);
            else
                index = nValues-2;
        }
        else
            index =  StsMath.binarySearch(values, nValues, value);

        prevValue = values[index];
        nextValue = values[index+1];
        long f = (value - prevValue) / (nextValue - prevValue);
        return new IndexF(index, f);
    }

	public long getValue(int index)
	{
		if(!checkLoadVector()) return 0;

		long[] values = getValues();
		if(values == null)
		{
			StsException.systemError(this, "getValue" + " values are null for vector " + name);
			return 0;
		}
		if(values.length < index + 1)
		{
			StsException.systemError(this, "getValue" + " index " + index + " out of range for vector " + name);
			return 0;
		}
		return getValues()[index];
	}

    public void setValue(long value, int index)
    {
		long[] values = getValues();
		values[index] = value;
    }

    public long getValue(long indexF)
    {
        if(!checkLoadVector()) return nullValue;
        long[] values = getValues();
        int nValues = values.length;
        if(nValues < 2) return nullValue;
        int index = (int)indexF;
        index = StsMath.minMax(index, 0, nValues-2);
        long f = indexF - index;
        long prevValue = values[index];
        long nextValue = values[index+1];
        return prevValue + f*(nextValue- prevValue);
    }

    public long getValue(IndexF indexF)
    {
        if(!checkLoadVector()) return nullValue;
		return indexF.getInterpolatedValue(getValues());
    }

    public float[] getHistogram()
    {
        if(dataHist != null) return dataHist;
        dataHist = new float[255];
        clearHistogram();
		int size = getSize();
        for(int i=0; i< size; i++)
        {
            accumulateHistogram(getValues()[i]);
        }
        calculateHistogram();
        return dataHist;
    }

    public void accumulateHistogram(int bindex)
    {
        if (bindex > 254)
        {
            bindex = 254;
        }
        if (bindex < 0)
        {
            bindex = 0;
        }
        dataCnt[bindex] = dataCnt[bindex] + 1;
        ttlHistogramSamples++;
    }

    private void accumulateHistogram(long value)
    {
        float scaledLong = 254.0f * (float)((value - getMinValue()) / (maxValue - minValue));
        int scaledInt = StsMath.minMax(Math.round(scaledLong), 0, 254);
        accumulateHistogram(scaledInt);
    }

    public void calculateHistogram()
    {
        for (int i = 0; i < 255; i++)
        {
            dataHist[i] = (float) ( (long)dataCnt[i] / (long)ttlHistogramSamples) * 100.0f;
        }
    }

    public void clearHistogram()
    {
        for (int i = 0; i < 255; i++)
        {
            dataCnt[i] = 0;
            dataHist[i] = 0.0f;
        }
        ttlHistogramSamples = 0;
    }

    static public long interpolateValue(long inputValue, StsAbstractLongVector inputVector, StsAbstractLongVector outputVector)
    {
        if(!inputVector.checkLoadVector()) return nullValue;
        if(!outputVector.checkLoadVector()) return nullValue;
        long[] inputValues = inputVector.getValues();
        long[] outputValues = outputVector.getValues();
        return StsMath.interpolateValue(inputValue, inputValues, outputValues);
    }

    public void deletePoint(int index)
    {
        deleteLong(index);
    }

    public int insertAfter(long value, int index)
    {
        return insertBefore(value, index+1);
    }

    public long[] getRelativeRange()
    {
        return new long[] { minValue, maxValue };
    }

    public double[] getAbsoluteRange()
    {
		double origin = getOrigin();
        return new double[] { origin + minValue, origin + maxValue };
    }

	public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "clone", e);
            return null;
        }
    }

    static public StsAbstractLongVector getVectorWithName(StsAbstractLongVector[] vectors, String name)
    {
        if(vectors == null) return null;
        int nLogVectors = vectors.length;
        for(int n = 0; n < nLogVectors; n++)
        {
        	if(vectors[n] != null && vectors[n].name.equals(name))
        		return vectors[n];

        }
        return null;
    }

	/** if non-zero, origin has been shifted this amount */
	public long getOriginOffset()
	{
		return originOffset;
	}
}
