package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.PlugIns.Wells.Actions.Loader.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 10/8/11
 */
public class StsTdVectorSet extends StsLogVectorSet
{
	public StsTdVectorSet() { }

	public StsTdVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(vectorSetLoader);
	}

	static public StsTdVectorSet constructor(StsTdCurveLoader vectorSetLoader)
	{
		StsTdVectorSet vectorSet = new StsTdVectorSet(vectorSetLoader);
		//StsNameSet fileNameSet = vectorSetLoader.nameSet;
		//String name = vectorSetLoader.name;
		if(!vectorSet.constructDataVectors(vectorSetLoader)) return null;
		return vectorSet;
	}
	public boolean constructDataVectors(String name, StsNameSet fileNameSet)
	{
		if(!super.constructDataVectors(name, fileNameSet)) return false;
		return getVectorOfType(COL_T) != null;
	}

	public boolean addVectorSetToObject()
	{
		if(clockTimeVector != null) clockTimeVector.initializeTimeIndex();
		StsWell well = (StsWell) getLineVectorSetObject();
		return well.addVectorSetToObject(this);
	}

	public boolean checkVectors()
	{
		//setProjectObject(well);
		return getTVector() != null && (getZVector() != null || getMVector() != null);
	}

	public boolean createLogCurves(StsWell well)
	{
		StsLogCurve logCurve = StsLogCurve.constructLogCurve(well, this, getTVector(), getVersion());
		if(logCurve != null) ((StsWell) getLineVectorSetObject()).addLogCurve(logCurve);
		return true;
	}
}
