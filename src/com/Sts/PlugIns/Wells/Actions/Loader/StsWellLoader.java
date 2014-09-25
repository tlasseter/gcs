package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsWellLoader extends StsTimeVectorSetLoader
{
	transient public StsWell well;

	static protected float datumShift = 0.0f;
	static public float appliedShift;

	public StsWellLoader(StsModel model)
	{
		super(model);
	}

	public StsWellLoader(StsModel model, StsProgressPanel progressPanel)
	{
		super(model, false, progressPanel);
	}

	public StsWellLoader(StsModel model, StsWell well, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, deleteStsData, progressPanel);
		setTimeVectorSetObject(well);
		this.well = well;
	}

	public boolean loadFile(StsAbstractFile file, boolean loadValues, boolean addToProject, boolean isSourceData)
	{
		if(well == null) return false;
		//this.sourceFile = file;
		return processVectorFile(file, well, loadValues, isSourceData);
	}

	public boolean readFileHeader(StsAbstractFile file)
	{
		String line = "";
		nameSet.clear();
		try
		{
			while (true)
			{
				line = file.readLine();
				if(line == null) return false;
				line.trim();
				// line = StsStringUtils.deTabString(line);
				if(line.endsWith(WELLNAME))
				{
					String name = new String(file.readLine().trim());
				}
				else if(line.endsWith(NULL_VALUE))
				{
					line = file.readLine().trim();  // get the next line
					StringTokenizer stok = new StringTokenizer(line);
					nullValue = Float.valueOf(stok.nextToken()).floatValue();
				}

				else if(line.endsWith(CURVE))
					return readMultiLineColumnNames(file, VALUE);
				else if(line.endsWith(VALUE)) // didn't find any curve names
				{
					StsMessageFiles.errorMessage(this, "readFileHeader", " Didn't find  any curve names in file: " + file.filename);
					return false;
				}
				else
					progressPanel.appendErrorLine("File " + file.filename + " failed to process line: " + line);
			}
		}
		catch(Exception e)
		{
			StsMessageFiles.errorMessage(this, "readFileHeader", "failed reading line: " + line + "in file: " + file.filename);
			StsException.outputWarningException(this, "read", e);
			return false;
		}
	}

    public void setNullValue()
    {
        nullValue = project.getLogNull();
    }
}