package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.MVC.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 11/9/11
 */
public class StsFloatMdWellVector extends StsFloatTransientValuesVector
{
	StsLineVectorSet lineVectorSet;
	{
		name = StsLoader.MDEPTH;
	}

	public StsFloatMdWellVector()
	{
	}

	public StsFloatMdWellVector(StsLineVectorSet lineVectorSet)
	{
		this.lineVectorSet = lineVectorSet;
	}

	public boolean initialize(StsModel model)
	{
		return lineVectorSet.checkComputeWellMDepthVector();
	}

	public float[] getValues()
	{
		if(values != null) return values;
		values = lineVectorSet.computeMDepths();
		checkSetValues(values);
		return values;
	}
}
