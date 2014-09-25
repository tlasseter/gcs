package com.Sts.Framework.IO;

import java.util.*;

public class StsAbstractFileTypeComparator implements Comparator<StsAbstractFile>
{
	static final StsAbstractFileTypeComparator comparator = new StsAbstractFileTypeComparator();

	public StsAbstractFileTypeComparator()
	{
	}

	public int compare(StsAbstractFile f1, StsAbstractFile f2)
	{
		int compare = f1.group.compareTo(f2.group);
		if(compare != 0) return compare;
		return f1.format.compareTo(f2.format);
	}
}