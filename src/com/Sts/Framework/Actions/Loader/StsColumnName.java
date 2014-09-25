package com.Sts.Framework.Actions.Loader;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:04 AM
 *
 * StsNameVectors(s) are used in construction from S2S standard ASCII files.
 * The header file contains a series of curve names (one per line).
 * After all curveNames have been read, lines with one value per curve in sequence defined are read;
 * these values are assigned to the corresponding vector for each curve.
 * StsNameVectors are grouped in StsNameVectorSet which is a HashMap of name and nameVector pairs
 * which allows for the lookup of a nameVector by name.
 */
public class StsColumnName implements Cloneable
{
	/** standard name for this vector */
	final public String name;
	/** possible alias names found in files */
	final String[] aliases;
	/** column index for this vector in the file being loaded. */
	public int fileColumnIndex;
	/** column index in the vector set.  If -1: then vectors are simply added to the vector set in the order read
	 *  if -2, it is a clockTime vector
	 *  if >= 0 this is a vector in a coorVectorSet at that position in the set
	 *  */
	public int columnIndexFlag = -1;
	/**
	/** alias found for this vector */
	String alias;

	static public final Comparator fileColumnIndexComparator = new FileColumnIndexComparator();

	public StsColumnName(String name, String[] aliases)
	{
		this.name = name;
		this.alias = name;
		this.aliases = aliases;
	}

	static public StsColumnName constructColumnName(String name, String[] aliases, int columnIndexFlag)
	{
		return new StsColumnName(name, aliases, columnIndexFlag);
	}

	private StsColumnName(String name, String[] aliases, int columnIndexFlag)
	{
		this.name = name;
		this.alias = this.name;
		this.columnIndexFlag = columnIndexFlag;
		this.aliases = aliases;
	}

	public StsColumnName(String name)
	{
		this.name = name;
		alias = name;
		aliases = null;
	}

	public StsColumnName(StsColumnName columnName, int fileColumnIndex)
	{
		this.name = columnName.name;
		alias = columnName.name;
		aliases = columnName.aliases;
		columnIndexFlag = columnName.columnIndexFlag;
		this.fileColumnIndex = fileColumnIndex;
	}
/*
	public StsColumnName(String name, String[] aliases, int vectorSetColumnIndex)
	{
		this.name = name;
		this.type = -1;
		this.alias = name;
		this.vectorSetColumnIndex = vectorSetColumnIndex;
		this.aliases = aliases;
	}

	public StsColumnName(String name, String alias)
	{
		this.name = name;
		this.type = -1;
		this.alias = name;
		this.aliases = new String[] { alias };
	}
*/
	public StsColumnName(String name, int columnIndex)
	{
		this.name = name;
		this.alias = name;
		this.fileColumnIndex = columnIndex;
		aliases = null;
	}

	public boolean equalsColumnName(String name)
	{
		return name.equals(name);
	}
/*
	public StsColumnName(StsColumnName matchColumn, int columnIndex)
	{
		this.name = matchColumn.name;
		this.type = matchColumn.type;
		this.alias = name;
		this.fileColumnIndex = columnIndex;
		this.aliases = null;
	}
*/
	public String toString() { return name; }

	static public StsColumnName[] fileColumnIndexSort(Collection<StsColumnName> values)
	{
		StsColumnName[] columnNames = values.toArray(new StsColumnName[0]);
		Arrays.sort(columnNames, fileColumnIndexComparator);
		return columnNames;
	}
}

class FileColumnIndexComparator implements Comparator<StsColumnName>
{
	FileColumnIndexComparator() { }

	public int compare(StsColumnName column1, StsColumnName column2)
	{
		return column1.fileColumnIndex - column2.fileColumnIndex;
	}
}