//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

/* The long values in this longVector are not persisted but generally the vector itself is persisted.
 * This file must have a backing file on disk from which values are loaded by the vectorSet which owns this.
 */

package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;

abstract public class StsLongTransientValuesVector extends StsAbstractLongVector implements Cloneable
{
	transient long[] values = null;
	transient long origin;

	static public final long largeLong = Long.MAX_VALUE;

	public StsLongTransientValuesVector()
	{
	}

	public StsLongTransientValuesVector(boolean persistent)
	{
		super(persistent);
	}

	public StsLongTransientValuesVector(long[] values)
	{
		setValues(values);
	}

	public long[] getValues() { return values; }

	public void initializeValues(long[] values) { this.values = values; }

	public void setValues(long[] values)
	{
		this.values = values;
		if(values == null)
			size = 0;
		else
			size = values.length;
		capacity = size;
	}

	public void setValues(long[] values, int size, int capacity)
	{
		this.values = values;
		this.size = size;
		this.capacity = capacity;
	}

	public void applyScalar(long scalar)
	{
		if(scalar == 1.0f) return;
		origin *= scalar;
		for(int n = 0; n < values.length; n++)
			values[n] *= scalar;
		minValue *= scalar;
		maxValue *= scalar;
	}

	static public boolean deviationVectorsOK(StsAbstractVector[] vectors)
	{
		StsAbstractVector xVector, yVector, zVector, mVector;
		xVector = getVectorWithName(vectors, StsLoader.X);
		yVector = getVectorWithName(vectors, StsLoader.Y);
		zVector = getVectorWithName(vectors, StsLoader.DEPTH);
		mVector = getVectorWithName(vectors, StsLoader.MDEPTH);
		return xVector != null && yVector != null && (zVector != null || mVector != null);
	}
}

































