package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

import java.io.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

/** This is a concrete set of StsFile(s) in this directory which satisfy a filenameFilter or filters */
public class StsGraphicFileSet extends StsFileSet
{

    private StsGraphicFileSet(String directory)
    {
		super(directory);
    }

	private StsGraphicFileSet(String directory, FilenameFilter filenameFilter)
	{
		super(directory, filenameFilter);
	}

	static public StsGraphicFileSet constructor(String directory, FilenameFilter filter)
	{
		StsGraphicFileSet fileSet = new StsGraphicFileSet(directory, filter);
		fileSet.getFiles();
		return fileSet;
	}

    static public StsGraphicFileSet constructor(String directory)
    {
        try
        {
            StsGraphicFileSet fileset = new StsGraphicFileSet(directory);
            return fileset;
        }
        catch(Exception e)
        {
            StsException.outputException("StsFileSet.constructor() failed.",
                e, StsException.WARNING);
            return null;
        }
    }
}
