package com.Sts.Framework.IO;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/7/11
 */
public class StsSubFileSet implements Comparable<StsSubFileSet>
{
	String name = null;
	private StsAbstractFile masterFile;
	ArrayList<StsAbstractFile> subFiles = new ArrayList<StsAbstractFile>();
	public boolean isDirectory = false;

	public StsSubFileSet() {}

	public void addSubFile(StsAbstractFile subFile)
	{
		if(name == null) name = subFile.name;
		subFiles.add(subFile);
	}

	public StsAbstractFile setSubFilesInMaster()
	{
		if(masterFile == null) return null;
		if(subFiles != null)
			masterFile.subFiles = subFiles.toArray(new StsAbstractFile[0]);
		subFiles = null;
		return masterFile;
	}

	public StsAbstractFile getMasterFile()
	{
		return masterFile;
	}

	public void setMasterFile(StsAbstractFile masterFile)
	{
		this.masterFile = masterFile;
		name = masterFile.name;
	}

	public String toString()
	{
		if(masterFile != null)
			return masterFile.name;
		else
			return name + " BAD: no dev";
	}

	public int compareTo(StsSubFileSet other)
	{
		return name.compareTo(other.name);
	}
}
