package com.Sts.Framework.UI.DataTransfer;

import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.UI.*;

import java.io.*;
import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/20/11
 */
public class StsDirectoryBrowserFileTransferPanel extends StsDirectoryBrowserObjectsTransferPanel
{
	FolderFilter filter = new FolderFilter();
	StsAbstractFilenameFilter masterFileFilter;
	StsAbstractFilenameFilter subFilesFilter;
	StsAbstractFilenameFilter fileFilter;
	StsAbstractFilenameFilter folderFilter;

    public StsDirectoryBrowserFileTransferPanel(String currentDirectory, StsObjectTransferListener listener,
												StsAbstractFilenameFilter masterFileFilter, StsAbstractFilenameFilter subFilesFilter, StsAbstractFilenameFilter folderFilter, int width, int height)
    {
        super.initialize("Test Directory Objects Panel", currentDirectory, listener, width, height);
		this.masterFileFilter = masterFileFilter;
		this.subFilesFilter = subFilesFilter;
		this.folderFilter = folderFilter;
		fileFilter = StsFilenameFilter.addFilters(masterFileFilter, new StsAbstractFilenameFilter[] { subFilesFilter, folderFilter });
    }

    public Object[] getAvailableObjects()
    {
		if(directory == null) return null;
		File directoryFile = new File(directory);
		File[] files = directoryFile.listFiles();      // fileFilter
		if(files == null) return new StsFile[0];
		HashMap<String, StsSubFileSet> subFileSets = new HashMap<String, StsSubFileSet>();
		for(File file : files)
		{
			try
			{
				if(file.isDirectory())
				{
					StsFile masterFile = masterFileFromFolder(file);
					if(masterFile != null)
					{
						String name = masterFile.name;
						StsSubFileSet existingSet = subFileSets.get(name);
						if(existingSet != null)
						{
							StsMessageFiles.errorMessage(this, "getAvailableObjects", "A deviation survey already exists for " + masterFileFilter.name);
							continue;
						}
						StsSubFileSet subFileSet = new StsSubFileSet();
						subFileSet.setMasterFile(masterFile);
						subFileSet.isDirectory = true;
						subFileSets.put(name, subFileSet);
					}
				}
				else // not a directory, add this file as either masterFile or subFile to new or existing subFileSet
				{
					StsFile stsFile = StsFile.constructor(file.toPath());
					if(stsFile == null) continue;
					masterFileFilter.parseFile(stsFile);
					String name = masterFileFilter.name;
					if(masterFileFilter.groupOk()) // this is a masterFile
					{
						StsSubFileSet existingSet = subFileSets.get(name);
						if(existingSet != null)
						{
							StsMessageFiles.errorMessage(this, "getAvailableObjects", "A deviation survey already exists for " + masterFileFilter.name);
							continue;
						}
						StsSubFileSet subFileSet = new StsSubFileSet();
						subFileSet.setMasterFile(stsFile);
						subFileSets.put(name, subFileSet);
					}
					else // this is a subFile
					{
						StsSubFileSet subFileSet = subFileSets.get(name);
						if(subFileSet == null)
						{
							subFileSet = new StsSubFileSet();
							subFileSets.put(name, subFileSet);
						}
						subFileSet.addSubFile(stsFile);
					}
				}
			}
			catch(Exception e) { }
		}
        StsSubFileSet[] values =  subFileSets.values().toArray(new StsSubFileSet[0]);
		Arrays.sort(values);
		return values;
    }

	private StsFile masterFileFromFolder(File folder)
	{
		File[] files = folder.listFiles(masterFileFilter);
		if(files == null || files.length == 0) return null;
		if(files.length > 1)
		{
			StsMessageFiles.errorMessage("More than one deviation survey file found in folder " + folder.getPath());
			return null;
		}
		if(!folderFilter.parseFile(folder)) return null;
		String folderName = masterFileFilter.name;
		StsFile masterFile = StsFile.constructor(files[0].toPath());
		if(masterFile == null) return null;
		masterFileFilter.parseFile(masterFile);
		if(folderName.equals(masterFileFilter.name))
			return masterFile;
		else
			return null;
	}

    public Object[] initializeAvailableObjects()
    {
       return getAvailableObjects();
    }

	class FolderFilter implements FilenameFilter
	{
		FolderFilter() { }

		public boolean accept(File file) { return file.isDirectory(); }
		public boolean accept(File file, String filename) { return file.isDirectory(); }

	}
}
