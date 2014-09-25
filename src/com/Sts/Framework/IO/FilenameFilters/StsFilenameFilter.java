package com.Sts.Framework.IO.FilenameFilters;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Utilities.*;

import java.nio.file.*;
import java.util.*;

public class StsFilenameFilter extends StsAbstractFilenameFilter
{
	public String[] okFormats = null; // txt (ascii), bin (binary)
	public String[] okSubnames = null; // name of subobject (example: curvename)
	int ORDER_FORMAT;
	int ORDER_SUBNAME;
	int[] ORDER_VERSIONS;
	{
		ORDER_GROUP = 0;
		ORDER_FORMAT = 1;
		ORDER_NAME = 2;
		ORDER_SUBNAME = 3;
		ORDER_VERSIONS = new int[] { 3, 4 };

	}
	boolean[] checkStringNumeric = new boolean[] { false, false, false, true, false };
	public boolean hasVersion = false;

	public String format;
	public String subname;
	public int version;

	static public final String FORMAT_TXT = StsLoader.FORMAT_TXT;
	static public final String FORMAT_BIN = StsLoader.FORMAT_BIN;

	static public final StsFilenameFilter FILENAME_FILTER = new StsFilenameFilter();

	public StsFilenameFilter()
	{
	}

	public StsFilenameFilter(String group, String format, String name)
	{
		super(group);
		this.okFormats = new String[] { format };
		this.okNames = new String[] { name };
	}

	public StsFilenameFilter(String group, String format)
	{
		this.okGroups = new String[] { group };
		this.okFormats = new String[] { format };
	}

	public StsFilenameFilter(String[] okGroups, String[] okFormats)
	{
		this.okGroups = okGroups;
		this.okFormats = okFormats;
	}

	public StsFilenameFilter(StsFilenameFilter filter)
	{
		if(filter == null) return;
		this.okGroups = StsMath.copy(filter.okGroups);
		this.okFormats = StsMath.copy(filter.okFormats);
	    this.okNames = StsMath.copy(filter.okNames);
		this.okSubnames = StsMath.copy(filter.okSubnames);
	}

	public StsFilenameFilter(String name)
	{
		this.okNames = new String[] { name };
	}

	static public StsFilenameFilter constructGroupFilter(String group)
	{
		return constructGroupsFilter(new String[] { group } );
	}

	static public StsFilenameFilter constructGroupsFilter(String[] groups)
	{
		StsFilenameFilter filenameFilter =  new StsFilenameFilter();
		filenameFilter.okGroups = groups;
		return filenameFilter;
	}

	static public StsFilenameFilter constructGroupsFormatFilter(String[] groups, String format)
	{
		StsFilenameFilter filenameFilter =  new StsFilenameFilter();
		filenameFilter.okGroups = groups;
		filenameFilter.okFormats = new String[] { format };
		return filenameFilter;
	}

	static public StsFilenameFilter constructTxtFormatFilter()
	{
		return new StsFilenameFilter("", FORMAT_TXT, "");
	}

	static public StsFilenameFilter constructBinaryFormatFilter()
	{
		return new StsFilenameFilter("", FORMAT_BIN, "");
	}

	static public StsFilenameFilter constructFormatFilter(String format)
	{
		return new StsFilenameFilter("", format, "");
	}


	static public StsFilenameFilter constructGroupTxtFilter(String group)
	{
		return new StsFilenameFilter(group, FORMAT_TXT, "");
	}

	static public StsFilenameFilter constructGroupFormatFilter(String group, String format)
	{
		return new StsFilenameFilter(group, format);
	}

	static public StsFilenameFilter constructNameFilter(String name)
	{
		return new StsFilenameFilter(name);
	}

	static public StsAbstractFilenameFilter addFilters(StsAbstractFilenameFilter filter1, StsAbstractFilenameFilter filter2)
	{
		StsAbstractFilenameFilter newParser = filter1.getNewInstance();
		newParser.addFilter(filter2);
		return newParser;
	}

	static public StsAbstractFilenameFilter addFilters(StsAbstractFilenameFilter filter, StsAbstractFilenameFilter[] addedFilters)
	{
		StsAbstractFilenameFilter newFilter = filter.getNewInstance();
		newFilter.addFilter(filter);
		for(StsAbstractFilenameFilter addedFilter : addedFilters)
			newFilter.addFilter(addedFilter);
		return newFilter;
	}

	static public StsFilenameFilter addFilters(StsFilenameFilter[] filters)
	{
		StsFilenameFilter newFilter = new StsFilenameFilter(filters[0]);
		for(int n = 1; n < filters.length; n++)
			newFilter.addFilter(filters[n]);
		return newFilter;
	}

	public void addFilter(StsFilenameFilter addedFilter)
	{
		super.addFilter(addedFilter);
		okFormats = StsMath.concat(okFormats, addedFilter.okFormats);
		okSubnames = StsMath.concat(okSubnames, addedFilter.okSubnames);
	}

	public void setFormat(String format) { okFormats = new String[] { format }; }
	public void setOkFormats(String[] okFormats) { this.okFormats = okFormats; }
	public void setSubname(String subname) { okSubnames = new String[] { subname }; }
	public void setOkSubnames(String subname[]) { okSubnames = okSubnames; }

	private boolean formatOk()
	{
		if(okFormats == null) return true;
		for(String okFormat : okFormats)
			if(this.format.equals(okFormat))
			{
				format = okFormat;
				return true;
			}
		return false;
	}

	private boolean subnameOk()
	{
		if(okSubnames == null) return true;
		for(String okSubname : okSubnames)
			if(this.subname.equals(okSubname))
			{
				subname = okSubname;
				return true;
			}
		return false;
	}

    public void setDelimiter(String delimit) { delimiter = delimit; }
    public String getDelimiter() { return delimiter; }

    public boolean isFileAscii(String filename)
    {
        return parseCheckFilename(filename);
    }

	/** parse this file.filename
	 * @param file
	 * @return ok is true if filename meets filter requirements
	 */
	public boolean parseFile(StsAbstractFile file)
	{
		if(!parseCheckFilename(file.filename)) return false;
		file.group = getGroup();
		file.name = name;
		file.format = format;
		file.subname = subname;
		file.version = version;
		return true;
	}

    /** these are multi-column files with name: prefix.format.name or prefix.format.name.version where version is an integer */
    public boolean parseCheckFilename(String filename)
    {
		group = null;
		format = null;
		name = null;
		subname = null;
		version = 0;

		String[] tokens = StsStringUtils.getTokens(filename, delimiter);
		if(tokens == null) return false;
        int tokenCount = tokens.length;
        // if(tokenCount < 3 || tokenCount > 5) return false;

		group = getStringToken(tokens, ORDER_GROUP);
        format = getStringToken(tokens, ORDER_FORMAT);
        name = getStringToken(tokens, ORDER_NAME);
		subname = getStringToken(tokens, ORDER_SUBNAME);
		version = getIntToken(tokens, ORDER_VERSIONS);

        if(!groupOk()) return false;
		if(!formatOk()) return false;
		if(!nameOk()) return false;
		if(!subnameOk()) return false;
		return true;
    }

	private String getStringToken(String[] tokens, int nToken)
	{
		if(nToken >= tokens.length) return null;
		String token = tokens[nToken];
		if(checkStringNumeric[nToken] && StsStringUtils.isNumeric(token)) return null;
		return tokens[nToken];
	}

	private int getIntToken(String[] tokens, int[] positions)
	{
		for(int position : positions)
		{
			if(position < tokens.length && StsStringUtils.isNumeric(tokens[position]))
			{
				try
				{
					version = Integer.parseInt(tokens[position]);
					hasVersion = true;
					return version;
				}
				catch(Exception e) { }
			}
		}
		return 0;
	}

    public String getFileEndString(String filename)
    {
        if(!parseCheckFilename(filename)) return null;
        int prefixLength = new String(group + delimiter + format + delimiter).length();
        return filename.substring(prefixLength);
    }

    public String getFileStemName(String filename)
    {
        if(!parseCheckFilename(filename)) return null;
        return name;
    }

	static public String staticGetFileStemName(String filename)
	{
		StsFilenameFilter filter = new StsFilenameFilter();
		return filter.name;
	}

    public String constructFilename(String group, String format, String name, String subname, int version)
    {
        return group + delimiter + format + delimiter + name + delimiter + subname + delimiter + version;
    }

	public StsAbstractFile[] filterFiles(StsAbstractFile[] inputFiles)
	{
		ArrayList<StsAbstractFile> outputFiles = new ArrayList<StsAbstractFile>();
		for(StsAbstractFile file : inputFiles)
		{
			if(accept(file.filename))
				outputFiles.add(file);
		}
		return outputFiles.toArray(new StsAbstractFile[0]);
	}

	static public boolean staticIsFileAscii(String filename)
	{
		StsFilenameFilter filter = new StsFilenameFilter();
		return filter.format.equals(FORMAT_TXT);
	}

	static public StsFilenameFilter parseStsFilename(String filename)
	{
		StsFilenameFilter filter = new StsFilenameFilter();
		if(!filter.parseCheckFilename(filename)) return null;
		return filter;
	}

	static public StsFilenameFilter parseStsFilename(Path path)
	{
		return parseStsFilename(path.getFileName().toString());
	}

	public String getFormat()
	{
		return format;
	}

	public String getSubname()
	{
		return subname;
	}

	public int getVersion()
	{
		return version;
	}
}
