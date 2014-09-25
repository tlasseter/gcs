package com.Sts.Framework.IO;

import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

/** Constructs a set of abstractFiles which can be StsFile(s), JarEntry(s), or web JarEntry(s).
 *  The subFileSets and filters are used in the construction to identify the files in the set.
 *  Methods in the StsAbstractFileSet (StsFileSet, StsJarSet, or StsWebJarSet) are used by the constructor
 *  to find files. On completion of the construction, the found files are put into the fileSet.
 *  In the case of wells, there are master files (well_dev deviation files) which have subFiles
 *  (logs, td, markers, etc).  These subFiles are arrays within the masterFile and are fetched from
 *  there by the actual data loaders (StsWellDevLoader).  */
public class StsAbstractFileSetConstructor
{
	/** File organization: all files are in a single directory or in separate subdirectory.
	 *  Name of each subdirectory is same name as all files in the subdirectory.
	 */
	protected byte fileOrganization = SINGLE_DIRECTORY;
	/** fileSet constructed: could be an StsFileSet, StsJar, or StsWebJar. */
	StsAbstractFileSet fileSet;
	/** A set of file maps of StsAbstractFile(s) whose name is the key. All files in a set belong to the same group. */
	protected TreeMap<String, StsSubFileSet> subFileSets = new TreeMap<String, StsSubFileSet>();
	/** top level directory where this fileSet is stored. */
	protected String directory;
	/** group the master file must belong to. */
	String masterGroup;
	/** subFiles must belong to one of these groups. */
	String[] subFileGroups = null;

	static public final StsFilenameFilter FILENAME_FILTER = new StsFilenameFilter();
	{
	}
	static public final int ASCIIFILES = 0;
	static public final int JARFILE = 1;
	static public final int WEBJARFILE = 2;

	static public final byte SINGLE_DIRECTORY = 0;
	static public final byte SUB_DIRECTORIES = 1;

	public StsAbstractFileSetConstructor(Class objectClass, String dirName, String masterGroup, String[] subFileGroups)
	{
		this.directory = dirName;
		this.masterGroup = masterGroup;
		this.subFileGroups = subFileGroups;

		int fileSetType = getFileSetType();
		switch(fileSetType)
		{
			//case WEBJARFILE:
			//	fileSet = StsWebStartJar.constructor(getJarFilename(objectClass));
			case JARFILE:
				fileSet =  StsJar.constructor(dirName, getJarFilename(objectClass));
			case ASCIIFILES:
				fileSet =  StsFileSet.constructor(dirName);
		}
	}
	/**
	 *  This constructor assumes that all files to be processed are in a single directory and is generally used for processing source files or
	 *  are in subdirectories where each subdirectory is associated with a single object to be constructed and contains all relevant files.
	 *  When processing, the constructor will examine each file and add it to an StsSubFileSet which contains a masterFile and a set of subFiles.
	 * @param fileOrganization specifies whether all files are in a single directory used for source files, or are organized into subdirectories,
	 * one for each object to be created which is used for processed files.
	 * @param objectClass class of the StsObject(s) being constructed, e.g., StsWell.class
	 * @param directoryPathname pathname to the single directory containing all the source files to be processed, e.g., "C:\Project\Wells"
	 * @param masterGroup group that the primary file belongs to, e.g., "well_dev"
	 * @param subFileGroups groups that the secondary files belong to, e.g., "well_logs", well_td", etc
	 * @return constructor used in processing files which will create an StsAbstractFileSet containing subsets of files for each object
	 */
 	static public StsAbstractFileSet constructor(byte fileOrganization, Class objectClass, String directoryPathname, String masterGroup, String[] subFileGroups)
	{
		StsAbstractFileSetConstructor constructor = new StsAbstractFileSetConstructor(objectClass, directoryPathname, masterGroup, subFileGroups);
		constructor.constructFileSets(fileOrganization);
		return constructor.fileSet;
	}
	/**
	 *  This constructor assumes that all files to be processed are in a single directory and there are no subfile types.
	 *  When processing, the constructor will examine each file and add it to an StsSubFileSet which contains a masterFile and a set of subFiles.
	 * @param objectClass class of the StsObject(s) being constructed, e.g., StsWell.class
	 * @param directoryPathname pathname to the single directory containing all the source files to be processed, e.g., "C:\Project\Wells"
	 * @param masterGroup group that the primary file belongs to, e.g., "well_dev"
	 * @return constructor used in processing files which will create an StsAbstractFileSet containing subsets of files for each object
	 */
 	static public StsAbstractFileSet singleDirectoryConstructor(Class objectClass, String directoryPathname, String masterGroup)
	{
		StsAbstractFileSetConstructor constructor = new StsAbstractFileSetConstructor(objectClass, directoryPathname, masterGroup, null);
		constructor.constructFileSets(SINGLE_DIRECTORY);
		return constructor.fileSet;
	}

	/**
	 *  This constructor assumes that files to be processed are in a subdirectories, one subdirectory per object, and there are no subfile types.
	 *  When processing, the constructor will examine each file and add it to an StsSubFileSet which contains a masterFile and a set of subFiles.
	 * @param objectClass class of the StsObject(s) being constructed, e.g., StsWell.class
	 * @param directoryPathname pathname to the single directory containing all the source files to be processed, e.g., "C:\Project\Wells"
	 * @param masterGroup group that the primary file belongs to, e.g., "well_dev"
	 * @return constructor used in processing files which will create an StsAbstractFileSet containing subsets of files for each object
	 */
 	static public StsAbstractFileSet subDirectoriesConstructor(Class objectClass, String directoryPathname, String masterGroup)
	{
		StsAbstractFileSetConstructor constructor = new StsAbstractFileSetConstructor(objectClass, directoryPathname, masterGroup, null);
		constructor.constructFileSets(SUB_DIRECTORIES);
		return constructor.fileSet;
	}

	static public int getFileSetType()
	{
		if(!Main.isJarFile)
			return ASCIIFILES;
		else if(Main.isWebStart)
			return WEBJARFILE;
		else
			return JARFILE;
	}

	static private String getJarFilename(Class objectClass)
	{
	 	try
		{
			return (String)StsToolkit.getStaticFieldObject(objectClass, "stsJarFilename");
		}
		catch(Exception e)
		{
			StsException.systemError(StsAbstractFileSetConstructor.class, "getJarFilename", "Failed to return String field: " + objectClass.getSimpleName() + ".stsJarFilename");
			return null;
		}
	}

    public int size()
    {
        if(subFileSets == null) return 0;
        else return subFileSets.size();
    }

	public StsSubFileSet[] getFiles()
	{
		return subFileSets.values().toArray(new StsSubFileSet[0]);
	}

    public String[] getFilenames()
    {
        return subFileSets.keySet().toArray(new String[0]);
    }

	public boolean constructFileSets(byte fileOrganization)
	{
		this.fileOrganization = fileOrganization;

		if(fileOrganization == SINGLE_DIRECTORY)
			return constructDirectoryFileSets();
		else
			return constructFolderFileSets();
	}

	private boolean constructDirectoryFileSets()
	{
		if(!fileSet.addFiles(directory)) return false;
		ArrayList<StsAbstractFile> abstractFiles = fileSet.getFileList();
		for(StsAbstractFile abstractFile : abstractFiles)
		{
			FILENAME_FILTER.parseFile(abstractFile);
			checkAddToSubFileSet(abstractFile);
		}
		completeSubSets();
		return true;
	}

	private boolean constructFolderFileSets()
	{
		fileSet.addFiles(directory);
		ArrayList<StsAbstractFile> abstractFiles = fileSet.getFileList();
		for(StsAbstractFile abstractFile : abstractFiles)
		{
			String directoryName = abstractFile.getFilename();
			StsAbstractFile[] subFiles = abstractFile.listFiles();
			for(StsAbstractFile subFile : subFiles)
			{
				FILENAME_FILTER.parseFile(subFile);
				checkAddToSubFileSet(directoryName, subFile);
			}
		}
		completeSubSets();
		return true;
	}

	private void checkAddToSubFileSet(String directoryName, StsAbstractFile abstractFile)
	{
		if(!directoryName.equals(abstractFile.name)) return;
		checkAddToSubFileSet(abstractFile);
	}

	private void checkAddToSubFileSet(StsAbstractFile abstractFile)
	{
		String fileGroup = abstractFile.group;
		boolean isMasterFile = masterGroup.equals(fileGroup);
		boolean isSubFile = StsStringUtils.stringListHasString(subFileGroups, fileGroup);
		if(!isMasterFile && !isSubFile) return;
		String name = abstractFile.name;
		StsSubFileSet subFileSet = getCreateSubFileSets(name);
		if(isMasterFile)
		{
			subFileSet.setMasterFile(abstractFile);
			subFileSets.put(name, subFileSet);
		}
		else
			subFileSet.addSubFile(abstractFile);
	}

	private StsSubFileSet getCreateSubFileSets(String name)
	{
		StsSubFileSet subFileSet = subFileSets.get(name);
		if(subFileSet != null) return subFileSet;
		subFileSet = new StsSubFileSet();
		return subFileSet;
	}

	private void completeSubSets()
	{
		ArrayList<StsAbstractFile> abstractFiles = fileSet.abstractFiles;
		abstractFiles.clear();
		for(StsSubFileSet subFileSet : subFileSets.values())
		{
			StsAbstractFile abstractFile = subFileSet.setSubFilesInMaster();
			abstractFiles.add(abstractFile);
		}
	}
}
