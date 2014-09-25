package com.Sts.Framework.IO;

import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

/** The user typically loads S2S files of a particular type (e.g. surfaces) from potentially several different ascii formats.
 *  These asciiFiles are read and written out to the binary file directory.  On a subsequent load request, we first list the binary files
 *  available and then ascii files which are not available as equivalent binary files.  If reloadFromAscii is true, however,
 *  we ignore the binaries for which we have an ascii available, read the ascii files and rewrite the binaries.
 *  All S2S files have the same filename format: group.format.name.subname.version.  For example, for surfaces, there are two ascii file groups:
 *  "grid-seismic" for autopicked surfaces, and "grid" for zmap-like surfaces.  The format for both of these is "txt" i.e., ascii.
 *  These files are written out as binary files with "txt" format replaced with "bin" with the same group and name.
 *
 *  This class updates the list of filenames for a given type.  If a binary directory is available, all files which are members of the group
 *  and have a binary format are added to a working list.  We then add all ascii files which are members of the group to the list.
 *  If reloadAscii is true, then we drop any binaries which have the same name as a corresponding ascii.  If reloadAscii is false,
 *  then we drop the ascii files which have corresponding binaries.
 */
public class StsAsciiBinaryFileSet extends StsAbstractFileSet
{
	String directory;
	String currentDirectory;
    String[] groups;
    public String binaryDirectory;
    String currentGroup;
    String currentFormat;
    /** used when binary files are to be ignored and original ascii files are to be reloaded */
    public boolean reloadAscii = false;
	StsFilenameFilter filenameFilter =  new StsFilenameFilter();

    protected StsAsciiBinaryFileSet(String directory, String binaryDirectory, String[] groups)
    {
        this.directory = directory;
        this.binaryDirectory = binaryDirectory;
        this.groups = groups;
    }

    static public StsAsciiBinaryFileSet constructor(String directory, String binaryDirectory, String[] groups)
    {
        StsAsciiBinaryFileSet fileSet = new StsAsciiBinaryFileSet(directory, binaryDirectory, groups);
        return fileSet;
    }

    public StsFile[] initializeAvailableFiles()
    {
        TreeSet<StsAbstractFile> files = new TreeSet<StsAbstractFile>(StsAbstractFileNameComparator.comparator);
		if(reloadAscii)
		{
			files = checkAddAsciiFiles(files);
        	files = checkAddBinaryFiles(files);
		}
		else
		{
        	files = checkAddBinaryFiles(files);
			files = checkAddAsciiFiles(files);
		}
        return files.toArray(new StsFile[0]);  // awkward syntax which forces toArray method to return a cast list
    }

	public void constructSubSets() { }

    private TreeSet<StsAbstractFile> checkAddBinaryFiles(TreeSet<StsAbstractFile> files)
    {
        if(binaryDirectory == null) return new TreeSet<StsAbstractFile>(StsAbstractFileNameComparator.comparator);
        currentFormat = StsAbstractFile.binaryFormat;
		((StsFilenameFilter)filenameFilter).format = currentFormat;
        currentDirectory = binaryDirectory;
        for(String group : groups)
        {
            currentGroup = group;
			filenameFilter.setGroup(currentGroup);
            TreeSet<StsAbstractFile> newFiles = getSortedParsedFilenames();
            for(StsAbstractFile newFile : newFiles)
            	files.add(newFile);
        }
		return files;
    }

    private TreeSet<StsAbstractFile> checkAddAsciiFiles(TreeSet<StsAbstractFile> files)
    {
        currentFormat = StsAbstractFile.asciiFormat;
		((StsFilenameFilter)filenameFilter).setFormat(currentFormat);
        currentDirectory = directory;
        for(String group : groups)
        {
            currentGroup = group;
			filenameFilter.setGroup(currentGroup);
            TreeSet<StsAbstractFile> newFiles = getSortedParsedFilenames();
			for(StsAbstractFile newFile : newFiles)
            files.add(newFile);
        }
		return files;
    }

    private TreeSet<StsFile> addFilesRemoveLastRepeat(TreeSet<StsFile> files, TreeSet<StsFile> newFiles)
    {
        for(StsFile newFile : newFiles)
            files.add(newFile);
		return files;
    }

    private TreeSet<StsFile> addFilesRemoveFirstRepeat(TreeSet<StsFile> files, TreeSet<StsFile> newFiles)
    {
        for(StsFile file : files)
			newFiles.add(file);
		return newFiles;
    }

    public ArrayList<StsFile> getParsedFilenames()
    {
        ArrayList<StsFile> files = new ArrayList<StsFile>();
        try
        {
            File directoryFile = new File(currentDirectory);
            String[] filenames = directoryFile.list(filenameFilter);
            if(filenames == null) return files;

            int nFilenames = filenames.length;
            for(int n = 0; n < nFilenames; n++)
            {
                StsFile file = StsFile.constructor(currentDirectory, filenames[n]);
                if(!file.exists()) continue;
				filenameFilter.parseFile(file);
                files.add(file);
            }
            return files;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "getParsedFilenames", e);
            return files;
        }
    }

	public TreeSet<StsAbstractFile> getSortedParsedFilenames()
    {
        TreeSet<StsAbstractFile> files = new TreeSet<StsAbstractFile>();
        try
        {
            File directoryFile = new File(currentDirectory);
            String[] filenames = directoryFile.list(filenameFilter);
            if(filenames == null) return files;

            int nFilenames = filenames.length;
            for(int n = 0; n < nFilenames; n++)
            {
                StsFile file = StsFile.constructor(currentDirectory, filenames[n]);
                if(!file.exists()) continue;
				filenameFilter.parseFile(file);
                files.add(file);
            }
            return files;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "getParsedFilenames", e);
            return files;
        }
    }
    public String getDescription()
    {
        return new String("files in directory: " + directory);
    }

	public StsAbstractFile getCompareObject(String filename)
	{
		return null;
	}

	public boolean addFiles(String directory)
	{
        File directoryFile = new File(directory);
        File[] files = directoryFile.listFiles();
		int nFiles = files.length;
		abstractFiles = new ArrayList<StsAbstractFile>();
		for(int n = 0; n < nFiles; n++)
			abstractFiles.add(StsFile.constructor(files[n].getPath()));
		return true;
	}
}