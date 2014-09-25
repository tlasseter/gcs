package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.MVC.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 11/9/11
 */
public class StsFloatDepthLogVector extends StsFloatTransientValuesVector
{
	StsLogVectorSet logVectorSet;
	{
		name = StsLoader.DEPTH;
	}
	public StsFloatDepthLogVector()
	{
	}

	public StsFloatDepthLogVector(StsLogVectorSet logVectorSet)
	{
		this.logVectorSet = logVectorSet;
	}

	public boolean initialize(StsModel model)
	{
		return true;
	}

	public float[] getDepthValues()
	{
		if(values != null) return values;
		StsWell well = (StsWell) logVectorSet.getLineVectorSetObject();
		values = well.computeDepthsFromLogMDepths(logVectorSet);
		return values;
	}
}
