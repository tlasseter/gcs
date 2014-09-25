package com.Sts.Framework.IO;

import java.util.*;

public class StsAbstractFileNameComparator implements Comparator<StsAbstractFile>
{
	static public final Comparator<StsAbstractFile> comparator = new StsAbstractFileNameComparator();

	public StsAbstractFileNameComparator()
	{
	}

	public int compare(StsAbstractFile f1, StsAbstractFile f2)
	{
		return f1.name.compareTo(f2.name);
	}
}