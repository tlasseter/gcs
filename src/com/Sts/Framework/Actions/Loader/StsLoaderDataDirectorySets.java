package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;

import java.nio.file.*;
import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/2/11
 */
public class StsLoaderDataDirectorySets
{
	StsLoaderDataDirectorySet[] directorySets = new StsLoaderDataDirectorySet[0];

	static public final String S2S = "S2S";
	static public final String OPEN_SPIRIT = "OpenSpirit";
	static public final String PETREL = "Petrel";
	static public final String OPEN_WORKS = "OpenWorks";
	static public final String GEOGRAPHIX = "Geographix";

	static public final String GC_FILES = "GC-Files";
	static public final String GC_SERVERS = "GC-Servers";

	static public final String[] wellInputTypes = new String[] {S2S, GC_FILES, OPEN_SPIRIT, OPEN_WORKS, PETREL, GEOGRAPHIX };
	static public final String[] wellOutputTypes = new String[] { GC_FILES, GC_SERVERS };
	static public final String[] microseismicSourceTypes = new String[] {GC_FILES};
	static public final String MSI = "MSI";
	static public final String GOLDER = "Golder";
	static public final String[] sensorSourceTypes = new String[] {GC_FILES, MSI, GOLDER };

	public StsLoaderDataDirectorySets() {}

	public void addDirectorySet(StsLoaderDataDirectorySet set)
	{
		directorySets = (StsLoaderDataDirectorySet[]) StsMath.arrayAddElement(directorySets, set);
	}

	public StsLoaderDataDirectorySet getSet(String type, String[] inputTypes, String[] outputTypes, StsProject project)
	{
		for(StsLoaderDataDirectorySet set : directorySets)
			if(set.classType.equals(type)) return set;
		StsLoaderDataDirectorySet set = new StsLoaderDataDirectorySet(type, inputTypes, outputTypes, project);
		this.addDirectorySet(set);
		return set;
	}

	public boolean writeToAsciiFile(String pathname)
	{
		try
		{
			StsParameterFile.writeObjectFields(pathname, this);
			return true;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "writeToAsciiFile", "Failed to write file: " + pathname);
			return false;
		}
	}

	/** Reads name-object pairs and populates this object. We should have already checked for existence of StsFile,
	 *  as we assume that returning false indicates that an existing file could not be properly read.
	 * @param file StsFile (actual file) containing key-value pairs. Should have been checked for existence.
	 * @return true if file successfully read
	 */
	public boolean readFromAsciiFile(StsFile file)
	{
		try
		{
			file.openReader();
			return StsParameterFile.readObjectFields(file, this);
		}
		catch(Exception e)
		{
			StsException.systemError(this, "readFromAsciiFile", "Failed to read file " + file.getPathname());
			return false;
		}
		finally
		{
			file.closeReader();
		}
	}

	public ArrayList<Path> getSourceDirectoryPaths()
	{
		ArrayList<Path> paths = new ArrayList<Path>();
		for(StsLoaderDataDirectorySet directorySet : directorySets)
		{
			String[] pathnames = directorySet.getSourceDirectories(GC_FILES);
			for(String pathname : pathnames)
				paths.add(Paths.get(pathname));
		}
		return paths;
	}

	static public void main(String[] args)
	{
		StsLoaderDataDirectorySets sets = new StsLoaderDataDirectorySets();
		StsProject project = new StsProject();
		StsLoaderDataDirectorySet set = new StsLoaderDataDirectorySet("wells", wellInputTypes, wellOutputTypes, project);
		//set.addOutputDataDirectory("G:\\Q\\StsWells");
		//set.addOutputDataDirectory("G:\\QTest\\StsWells");
		set.addSourceTypeDirectory(GC_FILES, "G:\\Q\\StsWells");
		set.addSourceTypeDirectory(GC_FILES, "G:\\QTest\\StsWells");
		set.addSourceTypeDirectory(GC_SERVERS, "file://127.0.0.1/G:/Q/stsFiles/StsWells/");
		set.debugPrint();
		sets.addDirectorySet(set);

		set = new StsLoaderDataDirectorySet("sensors", sensorSourceTypes, null, project);
		set.addSourceTypeDirectory(GC_FILES, "G:\\Q\\StsSensors");
		set.addSourceTypeDirectory(GC_FILES, "G:\\QTest\\StsSensors");
		set.debugPrint();
		sets.addDirectorySet(set);

		try
		{
			StsParameterFile.writeObjectFields("G:\\Q\\project.dataDirectories", sets);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoaderDataDirectorySets.class, "main", e);
		}
	}
}
