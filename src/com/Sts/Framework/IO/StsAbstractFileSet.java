package com.Sts.Framework.IO;

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

/** Defines a set of files of a certain type and origin.  Subclassed by StsFileSet, StsJar, and StsWebStartFileSet. */
abstract public class StsAbstractFileSet
{
	/** top level directory where this fileSet is stored. */
	protected String directory;
	protected ArrayList<StsAbstractFile> abstractFiles = new ArrayList<StsAbstractFile>();

	static public final int ASCIIFILES = 0;
	static public final int JARFILE = 1;
	static public final int WEBJARFILE = 2;

	abstract public StsAbstractFile getCompareObject(String filename);
	abstract public boolean addFiles(String directory);

	public StsAbstractFileSet()
    {
    }

	public StsAbstractFileSet(String directory)
	{
		this.directory = directory;
	}

	static public StsAbstractFileSet constructor(Class objectClass, String dirName)
	{
		int fileSetType = getFileSetType();

		switch(fileSetType)
		{
			//case WEBJARFILE:
			//	return StsWebStartJar.constructor(dirName, getJarFilename(objectClass));
			case JARFILE:
				return StsJar.constructor(dirName, getJarFilename(objectClass));
			case ASCIIFILES:
				return StsFileSet.constructor(dirName);
			default:
				return null;
		}
	}

	static public int getFileSetType()
	{
		if(!Main.isJarFile)
			return ASCIIFILES;
		else if(Main.isWebStart)
			return WEBJARFILE;
		else
			return JARFILE;
	}

	static private String getJarFilename(Class objectClass)
	{
	 	try
		{
			return (String)StsToolkit.getStaticFieldObject(objectClass, "stsJarFilename");
		}
		catch(Exception e)
		{
			StsException.systemError(StsAbstractFileSet.class, "getJarFilename", "Failed to return String field: " + objectClass.getSimpleName() + ".stsJarFilename");
			return null;
		}
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getDirectory() { return directory; }

    public int size()
    {
        return abstractFiles.size();
    }

	public StsAbstractFile getFileEndingWith(String suffix)
    {
        String[] filenames = getFilenames();
        for(int i=0; i<filenames.length; i++)
        {
            if(filenames[i] == null) continue;
            if(filenames[i].endsWith(suffix))
                return getFile(i);
        }
        return null;
    }

	public String[] getFilenames()
	{
		int nFilenames = abstractFiles.size();
		String[] filenames = new String[nFilenames];
		for(int n = 0; n < nFilenames; n++)
			filenames[n] = abstractFiles.get(n).getFilename();
		return filenames;
	}

    public int getIndex(String filename)
    {
        if(abstractFiles == null)
        {
            StsException.systemError("StsAbstractFileSet.index() failed. No files are available.");
            return -1;
        }

		StsAbstractFile key = getCompareObject(filename);
		return Collections.binarySearch(abstractFiles, key);
    }

	public StsAbstractFile getFile(int index)
	{
		if(abstractFiles == null || index < 0 || index >= abstractFiles.size()) return null;
		return abstractFiles.get(index);
	}

    public StsAbstractFile getFile(String filename)
    {
        int index = getIndex(filename);
        if(index < 0) return null;
        return getFile(index);
    }

	public StsAbstractFile[] getFiles()
	{
		return abstractFiles.toArray(new StsAbstractFile[0]);
	}

	public ArrayList<StsAbstractFile> getFileList()
	{
		return abstractFiles;
	}
}
