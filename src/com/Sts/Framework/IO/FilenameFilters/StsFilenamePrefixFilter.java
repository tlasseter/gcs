package com.Sts.Framework.IO.FilenameFilters;

/**
 * <p>Title: S2S development</p>
 * <p>Description: Integrated seismic to simulation software</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version c51c
 */
public class StsFilenamePrefixFilter extends StsAbstractFilenameFilter
{
	{
		ORDER_GROUP = 0;
		ORDER_NAME = 1;
	}


	public StsFilenamePrefixFilter()
	{
	}

	public StsFilenamePrefixFilter(String[] groups)
	{
        super(groups);
	}

	public StsFilenamePrefixFilter(String group)
	{
		super(group);
	}

	static public boolean prefixParseFilename(String filename)
	{
		StsFilenamePrefixFilter filter = new StsFilenamePrefixFilter();
		return filter.parseCheckFilename(filename);
	}
}
