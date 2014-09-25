package com.Sts.Framework.Utilities.DataVectors;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 12/7/11
 *
 * This floatVector is not persisted and is used for transient floatVectors.
 * It differs from its parent class in that isPersistable returns "false".
 */
public class StsFloatTransientVector extends StsFloatTransientValuesVector
{
	public StsFloatTransientVector(float[] values)
	{
		super(values);
	}

	public StsFloatTransientVector(int capacity, int inc, float nullValue)
	{
		super(capacity, inc, nullValue);
	}

	public StsFloatTransientVector(String name, float[] values)
	{
		super(name, values);
	}

	/** These vector(s) are not to be persisted, so override the default stsObject.addToModel(). */
	public void addToModel() { }

	public boolean isPersistable() { return false; }
}
