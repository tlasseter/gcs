package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsObjectLoader extends StsLoader
{
	private transient StsMainObject stsMainObject;

	abstract protected boolean readProcessData(StsAbstractFile file, StsProgressPanel progressPanel);

	public StsObjectLoader(StsModel model, String name, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		super(model, name, deleteStsData, isSourceData, progressPanel);
	}

    public StsObjectLoader(StsModel model, String name)
    {
        super(model);
    }

	public boolean loadFile(StsAbstractFile sourceFile, boolean loadValues, boolean addToProject, boolean isSourceData)
	{
		//this.sourceFile = sourceFile;
		return processFile(sourceFile, null);
	}

	public boolean processFile(StsAbstractFile file, StsProgressPanel progressPanel)
	{
		if(!file.openReader()) return false;
		if(!readFileHeader(file)) return false;
		return readProcessData(file, progressPanel);
	}

	public boolean processStsFile(StsAbstractFile file, boolean loadValues)
	{
		return true;
	}

	public boolean processFiles(StsAbstractFile[] files)
	{
		boolean processedOk = true;
		for(StsAbstractFile file : files)
		{
			if(processFile(file, progressPanel)) continue;
			appendErrorLine("Failed to process file " + file.name);
			processedOk = false;
		}
		return processedOk;
	}

	public boolean processColumnNames()
	{
		return true;
	}

	public StsMainObject getStsMainObject()
	{
		return stsMainObject;
	}

	public void setStsMainObject(StsMainObject stsMainObject)
	{
		this.stsMainObject = stsMainObject;
	}
}