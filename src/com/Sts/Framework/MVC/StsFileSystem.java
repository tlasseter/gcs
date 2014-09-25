package com.Sts.Framework.MVC;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 10/5/11
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;

import java.net.*;
import java.nio.file.*;
import java.util.*;

/** An StsFileSystem is a hierarchical collection of folders and files.  Instances of this class are currently a
 *  sourceFileSystem and a processedFileSystem.
 *
 */
public class StsFileSystem extends StsObject implements StsSerializable
{
    String rootPathname;

    transient byte type = LOCAL;
    transient URI uri;
	transient public FileSystem fileSystem;
	transient public Path path;
	transient public TreeMap<Path, StsFile> activeFiles = new TreeMap<Path, StsFile>();

	static public final byte LOCAL = 1;
	static public final byte REMOTE = 2;

	public StsFileSystem()
	{
	}

	public StsFileSystem(FileSystem fileSystem, String rootPathname)
	{
		this.fileSystem = fileSystem;
		this.rootPathname = rootPathname;
		path = Paths.get(rootPathname);
	}

	static public StsFileSystem createLocalFileSystem(String rootPathname)
	{
		FileSystem fileSystem = FileSystems.getDefault();
		return new StsFileSystem(fileSystem, rootPathname);

	}


	static public StsFileSystem createRemoteFileSystem(URI uri, String rootPathname)
	{
		try
		{
			FileSystem fileSystem = FileSystems.newFileSystem(uri, null);
			return new StsFileSystem(fileSystem, rootPathname);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public StsFile getCreateFile(Path path)
	{
		StsFile file = activeFiles.get(path);
		if(file != null) return file;
		file = StsFile.constructor(path);
		file.parseFilename();
		activeFiles.put(path, file);
		return file;
	}

	public void deleteFile(Path path)
	{
		StsFile file = activeFiles.get(path);
		if(file == null) return;
		file.delete();
		activeFiles.remove(path);
	}

	static public void main(String[] args)
	{
		String rootString = args[0];

	}
}
