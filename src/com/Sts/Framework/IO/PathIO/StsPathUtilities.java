package com.Sts.Framework.IO.PathIO;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/17/11
 */
public class StsPathUtilities
{
	static public boolean copyFiles(Path inputPath, Path outputPath)
	{
		try
		{
			Files.copy(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			return true;
		}
		catch(IOException e)
		{
			return false;
		}
	}

	static public BasicFileAttributes getFileAttributes(Path path)
	{
		try
		{
			return Files.readAttributes(path, BasicFileAttributes.class);
		}
		catch(IOException e)
		{
			return null;
		}
	}

	static public String getFileAttributesString(Path path)
	{

		BasicFileAttributes attributes = getFileAttributes(path);
		if(attributes == null) return "file not found";
		return getFileAttributesString(attributes);
	}

	static public String getFileAttributesString(BasicFileAttributes attributes)
	{
		if(attributes.isDirectory())
			return "Directory Created: " + attributes.creationTime() + " Modified: " + attributes.lastModifiedTime();
		else
			return "File Created: " + attributes.creationTime() + " Modified: " + attributes.lastModifiedTime() + " Size: " + attributes.size();
	}
}
