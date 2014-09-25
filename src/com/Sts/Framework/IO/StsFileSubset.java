package com.Sts.Framework.IO;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/19/11
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsFileSubset
{
	public String name;
	public StsAbstractFile[] subFiles;

	StsFileSubset()
	{
		name = "none";
		subFiles = new StsAbstractFile[0];
	}

	StsFileSubset(String name, File[] files)
	{
		File file = null;
		try
		{
			this.name = name;
			int nSubFiles = files.length;
			subFiles = new StsAbstractFile[nSubFiles];
			for(int n = 0; n < nSubFiles; n++)
				subFiles[n] = StsFile.constructor(files[n].getPath());
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "constructor(" + name + ", " + file.getPath(), e);
		}
	}

	StsFileSubset(String name, int firstIndex, int lastIndex, ArrayList<StsAbstractFile> files)
	{
		this.name = name;
		int nSubFiles = lastIndex - firstIndex + 1;
		subFiles = new StsAbstractFile[nSubFiles];
		// System.arrayCastCopy(files, firstIndex, subFiles, 0, nSubFiles);
		files.subList(firstIndex, lastIndex+1).toArray(subFiles);
		Arrays.sort(subFiles, StsAbstractFileTypeComparator.comparator);
	}

	public StsAbstractFile[] getTypeFiles(String group, String format)
	{
		int nSubFiles = subFiles.length;
		StsAbstractFile[] typeFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile file : subFiles)
		{
			if(file.compareTypeTo(group, format) == 0)
				typeFiles[n++] = file;
		}
		return (StsAbstractFile[])StsMath.trimArray(typeFiles, n);
	}

	public StsAbstractFile[] getGroupFiles(String group)
	{
		int nSubFiles = subFiles.length;
		StsAbstractFile[] groupFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile file : subFiles)
		{
			if(file.compareGroupTo(group) == 0)
				groupFiles[n++] = file;
		}
		return (StsAbstractFile[])StsMath.trimArray(groupFiles, n);
	}

	public StsAbstractFile[] getGroupTextFiles(String group)
	{
		int nSubFiles = subFiles.length;
		StsAbstractFile[] groupFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile file : subFiles)
		{
			if(file.group.equals(group) && !file.format.equals(StsLoader.FORMAT_BIN))
				groupFiles[n++] = file;
		}
		return (StsAbstractFile[])StsMath.trimArray(groupFiles, n);
	}
}