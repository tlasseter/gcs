package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsLogCurvesLoader extends StsWellLoader
{
	transient protected ArrayList<StsLogCurve> logCurves;

	transient byte computeOperation = COMPUTE_NONE;
	/** these are the fixed columns required for the vector set;  they are not all required,
	 *  but if they are found, their index in the vector set is the third argument.
	 *  Argument in constructFixedColumnName: StsEnum columnEnumerator, String[] aliases, int columnIndex.
	 *  The column enumerator contains a string and integer for this column type.
	 */
	static final byte COMPUTE_NONE = 01;
	static final byte COMPUTE_MDEPTH = 1;
	static final byte COMPUTE_DEPTH = 2;

	public StsLogCurvesLoader(StsModel model, StsWell well, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, well, deleteStsData, progressPanel);
	}

	public void initializeNameSet()
	{
		super.initializeNameSet();
        acceptableNameSet.addAliases(StsLoader.depthColumnName);
        acceptableNameSet.addAliases(StsLoader.mdepthColumnName);
        acceptableNameSet.addAliases(StsLoader.seismicTimeColumnName);
	}

	public void setGroup() { group = StsLoader.GROUP_WELL_LOGS; }

	public Class getVectorSetClass() { return StsLogVectorSet.class; }

	private void checkLoadLogCurves(StsAbstractFile wellFile)
	{
		StsAbstractFile[] logFiles = wellFile.getGroupSubFiles(StsLoader.GROUP_WELL_LOGS);
		if(logFiles.length == 0) return;
		StsLogCurvesLoader logCurveLoader = new StsLogCurvesLoader(StsLoader.model, well, false, progressPanel);
		for(StsAbstractFile logFile : logFiles)
		{
			if(!well.hasLogCurveFileObject(logFile))
				logCurveLoader.processVectorFile(logFile, well, false, StsLoader.isSourceData);
		}
	}

	public boolean loadFile(StsAbstractFile file, boolean loadValues, boolean addToProject, boolean isSourceData)
	{
		if(!processVectorFile(file, well, loadValues, isSourceData)) return false;
		getTimeVectorSet().checkSetCurrentTime();
		return true;
	}

	public boolean processVectorFiles(StsAbstractFile[] files, boolean loadValues)
	{
		boolean processedOk = true;
		for(StsAbstractFile logFile : files)
		{
			if(well.hasLogCurveFileObject(logFile)) continue;
			if(!processVectorFile(logFile, well, loadValues, StsLoader.isSourceData))
				processedOk = false;
		}
		return processedOk;
	}

	public boolean addVectorSetToObject()
	{
		if(clockTimeVector != null) clockTimeVector.initializeTimeIndex();
		StsLogVectorSet logVectorSet = (StsLogVectorSet)vectorSet;
		well.addVectorSetToObject(logVectorSet);
		logVectorSet.createLogCurves(well);
		return true;
	}
}