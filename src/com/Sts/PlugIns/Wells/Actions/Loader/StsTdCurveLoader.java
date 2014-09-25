package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 8/23/11
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsTdCurveLoader extends StsLogCurvesLoader
{
	public StsTdCurveLoader(StsModel model, StsWell well, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, well, deleteStsData, progressPanel);
	}

	public void setGroup() { group = GROUP_WELL_TD; }

	public Class getVectorSetClass() { return StsTdVectorSet.class; }

	public boolean addVectorSetToObject()
	{
		if(clockTimeVector != null) clockTimeVector.initializeTimeIndex();
		//vectorSet.addToModel();
		StsWell well = (StsWell) getVectorSetObject();
		StsTdVectorSet tdVectorSet = (StsTdVectorSet)vectorSet;
		tdVectorSet.createLogCurves(well);
		well.addVectorSetToObject(tdVectorSet);
		return true;
	}
}
