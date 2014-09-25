package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/2/11
 */
public class StsLoaderDataDirectorySet
{
	/** class type such as "well" which uses these directorySets for loading */
	public String classType;
	/** source types: one sourceTypeDirectories object is built for each type */
	public String[] sourceTypes;
	/** output types: one dataTypeDirectories object is built for each type */
	public String[] dataTypes;
	/** source types and associated directories; generally the various format types for input */
	public StsLoaderDataTypeDirectories[] sourceTypeDirectories = new StsLoaderDataTypeDirectories[0];
	/** source types and associated directories; generally the various format types for output */
	public StsLoaderDataTypeDirectories[] dataTypeDirectories = new StsLoaderDataTypeDirectories[0];

	public StsLoaderDataDirectorySet() { }

	public StsLoaderDataDirectorySet(String classType, String[] sourceTypes, String[] dataTypes, StsProject project)
	{
		this.classType = classType;
		this.sourceTypes = sourceTypes;
		this.dataTypes = dataTypes;
		sourceTypeDirectories = createTypeDirectories(sourceTypes, project);
		dataTypeDirectories = createTypeDirectories(dataTypes, project);
	}

	private StsLoaderDataTypeDirectories[] createTypeDirectories(String[] types, StsProject project)
	{
		if(types == null) return new StsLoaderDataTypeDirectories[0];
		int nTypes = types.length;
		StsLoaderDataTypeDirectories[] typeDirectories = new StsLoaderDataTypeDirectories[nTypes];
		for(int n = 0; n < nTypes; n++)
		{
			typeDirectories[n] = new StsLoaderDataTypeDirectories(types[n]);
			String defaultDirectory = project.getLoaderDefaultDataDirectory(classType, types[n]);
			if(defaultDirectory != null)
				typeDirectories[n].addDirectory(defaultDirectory);
		}
		return typeDirectories;
	}
/*
	public void addOutputDataDirectory(String directory)
	{
		outputDataDirectories = (String[])StsMath.arrayAddElement(outputDataDirectories, directory);
	}
*/
	public void addSourceTypeDirectory(String type, String source)
	{
		StsLoaderDataTypeDirectories typeDirectory = getSourceTypeDirectory(type);
		typeDirectory.addDirectory(source);
	}

	public StsLoaderDataTypeDirectories getSourceTypeDirectory(String type)
	{
		for(StsLoaderDataTypeDirectories typeDirectory : sourceTypeDirectories)
		{
			if(typeDirectory.type.equals(type))
				return typeDirectory;
		}
		StsLoaderDataTypeDirectories newType = new StsLoaderDataTypeDirectories(type);
		sourceTypeDirectories = (StsLoaderDataTypeDirectories[])StsMath.arrayAddElement(sourceTypeDirectories, newType);
		return newType;
	}

	public void addOutputTypeDirectory(String type, String source)
	{
		StsLoaderDataTypeDirectories typeDirectory = getDataDirectoryType(type);
		typeDirectory.addDirectory(source);
	}

	public StsLoaderDataTypeDirectories getDataDirectoryType(String type)
	{
		for(StsLoaderDataTypeDirectories typeDirectory : dataTypeDirectories)
		{
			if(typeDirectory.type.equals(type))
				return typeDirectory;
		}
		StsLoaderDataTypeDirectories newType = new StsLoaderDataTypeDirectories(type);
		dataTypeDirectories = (StsLoaderDataTypeDirectories[])StsMath.arrayAddElement(dataTypeDirectories, newType);
		return newType;
	}
/*
	public String[][] constructSourceStringsArray()
	{
		sourceStringsArray = new String[nTypes][0];
		int nSourceStrings = sourceStrings.length;
		for(int n = 0; n < nSourceStrings; n++)
		{
			int nType = sourceStringTypes[n];
			String sourceString = sourceStrings[n];
			sourceStringsArray[nType] = (String[])StsMath.arrayAddElement(sourceStringsArray[nType], sourceString);
		}
		return sourceStringsArray;
	}
*/
	public String[] getSourceDirectories(String sourceType)
	{
		StsLoaderDataTypeDirectories typeDirectory = getSourceTypeDirectory(sourceType);
		return typeDirectory.pathnames;
	}

	public String[] getDataDirectories(String dataType)
	{
		StsLoaderDataTypeDirectories typeDirectory = getDataDirectoryType(dataType);
		return typeDirectory.pathnames;
	}

	public String getFirstSourceDirectory()
	{
		if(sourceTypes == null || sourceTypes.length == 0) return null;
		String[] sourceDirectories = getSourceDirectories(sourceTypes[0]);
		if(sourceDirectories.length == 0) return null;
		return sourceDirectories[0];
	}

	public String getFirstDataDirectory()
	{
		String[] dataDirectories = getDataDirectories(dataTypes[0]);
		if(dataDirectories.length == 0) return null;
		return dataDirectories[0];
	}
	
	public void debugPrint()
	{
		if(sourceTypeDirectories != null)
		{
			System.out.println("Input Type Directories:");
			debugPrint(sourceTypeDirectories);
		}
		if(dataTypeDirectories != null)
		{
			System.out.println("Input Type Directories:");
			debugPrint(dataTypeDirectories);
		}
	}

	private void debugPrint(StsLoaderDataTypeDirectories[] typeDirectories)
	{
		for(int n = 0; n < typeDirectories.length; n++)
		{
			System.out.print(typeDirectories[n].type + " ");
			String[] directories = typeDirectories[n].pathnames;
			if(directories != null)
			{
				for(String directory : directories)
					System.out.print(directory + " ");
			}
			System.out.println();
		}
	}
}
