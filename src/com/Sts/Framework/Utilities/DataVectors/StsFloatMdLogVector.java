package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.MVC.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 11/9/11
 */
public class StsFloatMdLogVector extends StsFloatTransientValuesVector
{
	StsLogVectorSet logVectorSet;
	{
		name = StsLoader.MDEPTH;
	}

	public StsFloatMdLogVector()
	{
	}

	public StsFloatMdLogVector(StsLogVectorSet logVectorSet)
	{
		this.logVectorSet = logVectorSet;
	}

	public boolean initialize(StsModel model)
	{
		return true;
	}

	public float[] getValues()
	{
		if(values != null) return values;
		StsWell well = (StsWell)logVectorSet.getLineVectorSetObject();
		values = well.computeMDepthsFromLogDepths(logVectorSet);
		size = values.length;
		setMinValue(values[0]);
		setMaxValue(values[size-1]);
		return values;
	}
}
