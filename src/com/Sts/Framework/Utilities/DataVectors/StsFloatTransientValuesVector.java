/**Title:        S2S: Seismic-to-simulation
 *Version:
 *Copyright:    Copyright (c) 2012
 *Author:       Tom Lasseter
 *Company:      S2S Systems
 *Description:  Seismic-to-simulation interpretation system
 *
 * The float values in this floatVector are not persisted but generally the vector itself is persisted.
 * This file must have a backing file on disk from which values are loaded by the vectorSet which owns this.
 */

package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

abstract public class StsFloatTransientValuesVector extends StsAbstractFloatVector implements Cloneable
{
	transient float[] values = null;

	static public final float largeFloat = Float.MAX_VALUE;

	public StsFloatTransientValuesVector()
	{
	}

	public StsFloatTransientValuesVector(boolean persistent)
	{
		super(persistent);
	}

	public StsFloatTransientValuesVector(String name)
	{
		super(false);
		this.name = name;
	}

	public StsFloatTransientValuesVector(String name, float[] values)
	{
		super(false);
		this.name = name;
		checkSetValues(values);
	}

	public StsFloatTransientValuesVector(String name, ArrayList<StsPoint> points, int index)
	{
		super(false);
		this.name = name;
		int nValues = points.size();
		float[] values = new float[nValues];
		for(int n = 0; n < nValues; n++)
			values[n] = points.get(n).v[index];
		checkSetValues(values);
	}

	public StsFloatTransientValuesVector(float[] values)
	{
		super(false);
		checkSetValues(values);
	}

	public StsFloatTransientValuesVector(int capacity, int inc, float nullValue)
	{
		super(false);
		this.capacity = capacity;
		this.growInc = inc;
		this.nullValue = nullValue;
	}

	public void setValues(float[] values, int size, int capacity)
	{
		this.values = values;
		this.size = size;
		this.capacity = capacity;
	}

	public void deleteValues()
	{
		this.values = null;
		this.size = 0;
		this.capacity = 0;
	}

	public void clearValues()
	{
		this.values = null;
	}

	public void initializeValues(int capacity)
	{
		this.capacity = capacity;
		size = 0;
		this.values = new float[capacity];
	}

	public void setValues(float[] values)
	{
		this.values = values;
		if(values == null)
			size = 0;
		else
			size = values.length;
		capacity = size;
	}

	public void checkSetValues(float[] values)
	{
		setValues(values);
		checkSetMinMax();
	}

	public void checkSetValues(double[] values)
	{

		size = values.length;
		this.values = new float[size];
		isNull = true;
		setMinValue(Float.MAX_VALUE);
		setMaxValue(-Float.MAX_VALUE);
		for(int n = 0; n < size; n++)
		{
			if(isNullValue(values[n]))
				values[n] = StsParameters.nullValue;
			else if(isNull)
			{
				origin = values[n];
				this.values[n] = 0.0f;
				setMinValue(0.0f);
				setMaxValue(0.0f);
				isNull = false;
			}
			else
			{

				float value = (float) (values[n] - origin);
				this.values[n] = value;
				if(value < getMinValue()) setMinValue(value);
				else if(value > getMaxValue()) setMaxValue(value);
			}
		}
	}

	public void setValues(double[] values)
	{

		size = values.length;
		capacity = size;
		this.values = new float[size];
		isNull = true;
		for(int n = 0; n < size; n++)
		{
			if(isNullValue((float)values[n]))
				values[n] = StsParameters.nullValue;
			else if(isNull)
			{
				origin = values[0];
				this.values[0] = 0.0f;
				isNull = false;
			}
			else
				this.values[n] = (float) (values[n] - origin);
		}
	}

	public float[] getValues() { return values; }

	/** Set the origin for this vector.  For dataVectors, subsequent points should substract this origin (effectively treated as offsets). */
	public boolean setOrigin(double origin)
	{
		if(origin == 0.0) return false;
		setOffsetFromOrigin(true);
		this.origin = origin;
		return true;
	}
}

































