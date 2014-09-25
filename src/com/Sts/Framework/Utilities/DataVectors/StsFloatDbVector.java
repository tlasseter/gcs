package com.Sts.Framework.Utilities.DataVectors;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/19/11
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsFloatDbVector extends StsAbstractFloatVector implements Cloneable
{
    protected float[] values;

    static private final int initialCapacity = 4;

    public StsFloatDbVector()
    {
    }

    public StsFloatDbVector(String name)
    {
		super(false);
		this.name = name;
		this.capacity = initialCapacity;
		this.growInc = initialCapacity;
    }

    public StsFloatDbVector(String name, int capacity)
    {
		super(false);
		this.name = name;
		this.capacity = capacity;
		this.growInc =  10;
    }

	public StsFloatDbVector(int capacity)
	{
		super(false);
		this.capacity = capacity;
		this.growInc =  10;
	}

    public StsFloatDbVector(String name, float[] values)
    {
		super(false);
		this.name = name;
        checkSetValues(values);
    }

	public void setValues(float[] values)
	{
		this.values = values;
		this.size = values.length;
		this.capacity = values.length;
	}

	public void setValues(float[] values, int size, int capacity)
	{
		this.values = values;
		this.size = size;
		this.capacity = capacity;
	}
	public float[] getValues() { return values; }
}
