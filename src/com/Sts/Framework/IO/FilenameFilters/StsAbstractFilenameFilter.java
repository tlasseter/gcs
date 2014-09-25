package com.Sts.Framework.IO.FilenameFilters;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/26/11
 * Time: 7:21 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsAbstractFilenameFilter implements FilenameFilter
{
 	public String[] okGroups;
	String[] okNames;
	public String group;
	public String name;
	String delimiter = ".";
	int ORDER_GROUP;
	int ORDER_NAME;

	public StsAbstractFilenameFilter()
	{
	}

	public StsAbstractFilenameFilter(String[] okGroups)
	{
		this.okGroups = new String[okGroups.length];
		for (int i = 0; i < okGroups.length; i++)
			this.okGroups[i] = okGroups[i].toLowerCase();
	}

	public StsAbstractFilenameFilter(String group)
	{
		this.okGroups = new String[] { group.toLowerCase() };
	}

	public StsAbstractFilenameFilter getNewInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getNewInstance", e);
			return null;
		}
	}

	public String getName(String filename)
	{
		if(!parseCheckFilename(filename)) return null;
		return name;
	}

	public boolean parseFile(StsAbstractFile file)
	{
		if(!parseCheckFilename(file.filename)) return false;
		file.group = group;
		file.name = name;
		return true;
	}

	public boolean parseFile(File file)
	{
		return parseCheckFilename(file.toPath().getFileName().toString());
	}

	public boolean parseCheckFilename(String filename)
	{
		String[] tokens = StsStringUtils.getTokens(filename, delimiter);
		int tokenCount = tokens.length;
		if(tokenCount != 2)
		{
			group = null;
			name = null;
			return false;
		}
		group = tokens[ORDER_GROUP];
		name = tokens[ORDER_NAME];

		if(!groupOk()) return false;
		return nameOk();
	}

	public boolean groupOk()
	{
		if(okGroups == null) return true;
		for(String okGroup : okGroups)
			if(this.group.equals(okGroup))
			{
				group = okGroup;
				return true;
			}
		return false;
	}

	public boolean nameOk()
	{
		if(okNames == null) return true;
		for(String okName : okNames)
			if(this.name.equals(okName))
			{
				name = okName;
				return true;
			}
		return false;
	}

	public boolean accept(File file, String filename)
	{
		return parseCheckFilename(filename);
	}

	public boolean accept(String filename)
	{
		return parseCheckFilename(filename);
	}

	public void setGroup(String group) { okGroups = new String[] { group }; }
	public void setGroups(String[] okGroups) { this.okGroups = okGroups; }
	public void setName(String name) { okNames = new String[] { name }; }
	public void setNames(String[] okNames) { this.okNames = okNames; }

	public String getFormat()
	{
		StsException.systemError(this, "getFormat", "Format not defined for this type of parser: " + StsToolkit.getSimpleClassname(this));
		return "";
	}

	public String getSubname()
	{
		StsException.systemError(this, "getSubname", "Subname not defined for this type of parser: " + StsToolkit.getSimpleClassname(this));
		return "";
	}

	public int getVersion()
	{
		StsException.systemError(this, "getVersion", "Version not defined for this type of parser: " + StsToolkit.getSimpleClassname(this));
		return -1;
	}

	public String getGroup()
	{
		return group;
	}

	public String getName()
	{
		return name;
	}

	static public boolean suffixParseCheckFilename(String filename)
	{
		return StsFilenameSuffixFilter.suffixParseCheckFilename(filename);
	}

	static public boolean prefixParseCheckFilename(String filename)
	{
		return StsFilenamePrefixFilter.prefixParseFilename(filename);
	}

	static public StsAbstractFilenameFilter parseStsFilename(String filename)
	{
		return StsFilenameFilter.parseStsFilename(filename);
	}

	public void addFilter(StsAbstractFilenameFilter addedFilter)
	{
		okGroups = StsMath.concat(okGroups, addedFilter.okGroups);
		okNames = StsMath.concat(okNames, addedFilter.okNames);
	}

	protected boolean stringsEqual(String s1, String s2)
	{
		if(s1 == null && s2 == null)
			return true;
		return s1.equals(s2);
	}
}
