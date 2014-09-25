package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Utilities.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 5/18/12
 */
public class StsLoaderDataTypeDirectories
{
	/** source categories; generally the various format types for input or output */
	public String type;
	/** pathname to this source directory or filesystem */
	public String[] pathnames = new String[0];

	public StsLoaderDataTypeDirectories() { }

	public StsLoaderDataTypeDirectories(String type)
	{
		this.type = type;
	}

	public void addDirectory(String directory)
	{
		pathnames = (String[]) StsMath.arrayAddElement(pathnames, directory);
	}

	public void checkAddDirectory(String directory)
	{
		for(String pathname : pathnames)
			if(pathname.equals(directory)) return;
		addDirectory(directory);
	}

}
