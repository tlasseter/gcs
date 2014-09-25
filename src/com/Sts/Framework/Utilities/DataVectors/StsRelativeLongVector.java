package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Utilities.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 5/5/11
 * Time: 10:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsRelativeLongVector extends StsFloatTransientValuesVector
{
	protected StsLongTransientValuesVector longVector;
	protected long longOrigin = StsParameters.nullLongValue;

	public StsRelativeLongVector() { }

	public StsRelativeLongVector(String name, StsLongTransientValuesVector longVector)
	{
		this.name = name;
		this.longVector = longVector;
	}

	public void setLongOrigin(long longOrigin)
	{
		this.longOrigin = longOrigin;
		setOrigin((double)longOrigin);
	}

	public long getLongOrigin() { return longOrigin; }

   	public boolean checkLoadVector()
    {
		if(!longVector.checkLoadVector()) return false;
		long[] longValues = longVector.getValues();
		size = longValues.length;
		if(longOrigin == StsParameters.nullLongValue)
		{
			long longOrigin = longValues[0];
			setLongOrigin(longOrigin);
		}
		float[] values = new float[size];
		values[0] = 0.0f;
		for(int n = 1; n < size; n++)
			values[n] = (float)(longValues[n] - longOrigin);
        checkSetMinMax();
		return true;
    }

	public StsLongTransientValuesVector getLongVector()
	{
		return longVector;
	}

	public void setLongVector(StsLongTransientValuesVector longVector)
	{
		this.longVector = longVector;
	}
}
