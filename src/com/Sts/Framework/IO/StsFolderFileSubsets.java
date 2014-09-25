package com.Sts.Framework.IO;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/19/11
 * Time: 1:01 PM
 * This file manages a set of folders each of which has a name used to identify the object associated with the files in the folder.
 */
public class StsFolderFileSubsets extends StsFileSubsets
{
	ArrayList<File> folders;
	Comparator<StsAbstractFile> comparator;
	int searchIndex = 0;

	StsFolderFileSubsets(ArrayList<File> folders, Comparator<StsAbstractFile> comparator)
	{
		this.folders = folders;
		this.comparator = comparator;
	}

	/** files are sorted by name and then grouped into subsets of file which have the same name but may be of different types. */
	public void constructSubSets()
	{
		for(File folder : folders)
		{
			File[] files = folder.listFiles();
			add(new StsFileSubset(folder.getName(), files));
		}
	}

	public StsAbstractFile[] getSetFiles(String name)
	{
		StsFileSubset subset = getNameFileSet(name);
		return subset.subFiles;
	}
}
