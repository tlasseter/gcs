package com.Sts.Framework.IO;

import java.io.*;
import java.util.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

/** This is a concrete set of StsFile(s) in this directory which satisfy a filenameFilter or filters */
public class StsFileSet extends StsAbstractFileSet
{
	FilenameFilter filenameFilter = null;

	public StsFileSet() { }

    protected StsFileSet(String directory)
    {
        this.directory = directory;
    }

    protected StsFileSet(String directory, FilenameFilter filter)
    {
        this.directory = directory;
		this.filenameFilter = filter;
		addFiles();
    }

	static public StsFileSet constructor(String directory, FilenameFilter filter)
	{
		return new StsFileSet(directory, filter);
	}

    static public StsFileSet constructor(String directory)
    {
        return new StsFileSet(directory);
    }

	public boolean addFiles(String directory)
    {
		this.directory = directory;
		return addFiles();
	}

    public boolean addFiles()
    {
        File directoryFile = new File(directory);
        String[] filenames = directoryFile.list(filenameFilter);
        if(filenames == null) return false;
		for(String filename : filenames)
		{
			StsFile file = StsFile.constructor(directory, filename);
			if(file.isAFileAndNotDirectory())
				abstractFiles.add(file);
			else
				addDirectoryFiles(file);
		}
		return true;
    }

	public boolean addDirectoryFiles(StsFile directoryFile)
	{
		String[] filenames = directoryFile.getFile().list(filenameFilter);
		if(filenames == null) return false;
		for(String filename : filenames)
		{
			StsFile file = StsFile.constructor(directoryFile.getDirectory(), filename);
			if(file.isAFileAndNotDirectory())
				abstractFiles.add(file);
			else
				addDirectoryFiles(file);
		}
		return true;
	}

	public String getDescription()
    {
        return new String("files in directory: " + directory);
    }

	public StsAbstractFile getCompareObject(String filename)
	{
		StsFile file = new StsFile();
		file.filename = filename;
		return file;
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
