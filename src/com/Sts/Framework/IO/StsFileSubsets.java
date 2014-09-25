package com.Sts.Framework.IO;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/19/11
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsFileSubsets extends ArrayList<StsFileSubset>
{
	Comparator<StsAbstractFile> comparator;
	int searchIndex = 0;

	abstract public void constructSubSets();

	StsFileSubsets() {}

	StsFileSubsets(Comparator<StsAbstractFile> comparator)
	{
		this.comparator = comparator;
	}

	public StsFileSubset getNameFileSet(String name)
	{
		StsFileSubset subset = get(searchIndex);
		int searchDirection = name.compareTo(subset.name);
		if(searchDirection == 0) return subset;
		if(searchDirection  > 0)
		{
			for(searchIndex += 1; searchIndex < size(); searchIndex++)
			{
				subset = get(searchIndex);
				if(subset.name.equals(name))
					return subset;
			}
		}
		else
		{
			for(searchIndex -= 1; searchIndex >= 0; searchIndex--)
			{
				subset = get(searchIndex);
				if(subset.name.equals(name))
					return subset;
			}
		}
		return new StsFileSubset();
	}

	public StsAbstractFile[] getSetFiles(String name)
	{
		StsFileSubset subset = getNameFileSet(name);
		return subset.subFiles;
	}
}
