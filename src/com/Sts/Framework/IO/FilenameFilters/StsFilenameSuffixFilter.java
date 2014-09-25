package com.Sts.Framework.IO.FilenameFilters;

/**
 * <p>Title: S2S development</p>
 * <p>Description: Integrated seismic to simulation software</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version c51c
 */
public class StsFilenameSuffixFilter extends StsAbstractFilenameFilter
{
	{
		ORDER_GROUP = 1;
		ORDER_NAME = 0;
	}

	public StsFilenameSuffixFilter()
	{
	}

	public StsFilenameSuffixFilter(String[] groups)
	{
        super(groups);
	}

	public StsFilenameSuffixFilter(String group)
	{
		super(group);
	}

	static public boolean suffixParseCheckFilename(String filename)
	{
		StsFilenameSuffixFilter filter = new StsFilenameSuffixFilter();
		return filter.parseCheckFilename(filename);
	}
}
