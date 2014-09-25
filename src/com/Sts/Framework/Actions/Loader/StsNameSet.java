package com.Sts.Framework.Actions.Loader;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 5/7/11
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsNameSet extends TreeMap<String, StsColumnName> implements Iterable<StsColumnName>
{
	int index = 0;

	public void add(StsColumnName nameVector)
	{
		put(nameVector.name, nameVector);
	}

	public void addAliases(StsColumnName nameVector)
	{
		String[] aliases = nameVector.aliases;
		if(aliases != null)
		{
			for(String alias : nameVector.aliases)
			super.put(alias, nameVector);
		}
		else
			super.put(nameVector.name, nameVector);
	}

	public Iterator<StsColumnName> iterator()
	{
		return values().iterator();
	}

	public void add(StsNameSet addVectorSet)
	{
		for(StsColumnName nameVector : addVectorSet)
			add(nameVector);
	}

	public StsColumnName getColumn(String name)
	{
		return get(name);
	}

	public StsColumnName getMatchName(String columnName)
	{
		return get(columnName);
	}

	public StsColumnName getMatchName(String name, int currentIndex)
	{
		StsColumnName columnName = get(name);
		if(columnName == null) return null;
		columnName.fileColumnIndex = currentIndex;
		return columnName;
	}

	public String[] getColumnNames()
	{
		StsColumnName[] columns = StsColumnName.fileColumnIndexSort(values());
		int nColumns = columns.length;
		String[] columnNames = new String[nColumns];
		int n = 0;
		for(StsColumnName column : columns)
			columnNames[n++] = column.name;
		return columnNames;
	}
}
