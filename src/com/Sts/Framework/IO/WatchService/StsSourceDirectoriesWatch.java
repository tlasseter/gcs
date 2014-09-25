package com.Sts.Framework.IO.WatchService;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/10/11
 */
public class StsSourceDirectoriesWatch
{
	private StsProject project;
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private final boolean recursive;

	private HashMap<String, BasicFileAttributes> fileAttributesMap = new HashMap<String, BasicFileAttributes>();

	static public final WatchEvent.Kind<Path> ENTRY_CREATE = StandardWatchEventKinds.ENTRY_CREATE;
	static public final WatchEvent.Kind<Path> ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE;
	static public final WatchEvent.Kind<Path> ENTRY_MODIFY = StandardWatchEventKinds.ENTRY_MODIFY;
	static public final WatchEvent.Kind<Object> OVERFLOW = StandardWatchEventKinds.OVERFLOW;
	static public final LinkOption NOFOLLOW_LINKS = LinkOption.NOFOLLOW_LINKS;

	static final boolean debug = true;

	/** Creates a WatchService and registers the given directory */
	public StsSourceDirectoriesWatch(Path path, boolean recursive) throws IOException
	{
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		this.recursive = recursive;
		addPath(path);
	}

	public StsSourceDirectoriesWatch(ArrayList<Path> paths, boolean recursive, StsProject project) throws IOException
	{
		this.recursive = recursive;
		this.project = project;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		for(Path path : paths)
			addPath(path);
	}

	public StsSourceDirectoriesWatch(Path path, boolean recursive, StsProject project) throws IOException
	{
		this.recursive = recursive;
		this.project = project;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		addPath(path);
	}

	public void addPath(Path path) throws IOException
	{
		if(recursive)
		{
			registerAll(path);
		}
		else
		{
			register(path);
		}
	}
	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event)
	{
		return (WatchEvent<T>) event;
	}

	/** Register the given directory with the WatchService */
	public void register(Path dir) throws IOException
	{
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if(this.debug)
		{
			Path prev = keys.get(key);
			if(prev == null)
			{
				System.out.format("register: %s\n", dir);
			}
			else
			{
				if(!dir.equals(prev))
				{
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	public void registerAll(final Path start) throws IOException
	{
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				File dirFile = dir.toFile();
				if(!dirFile.exists())
					dirFile.mkdir();
				register(dir);
				return FileVisitResult.CONTINUE;
			}
			public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
			{
				if (attr.isSymbolicLink())
					System.out.format("Symbolic link: %s \n", file);
				else if (attr.isDirectory())
				{
					System.out.format("Directory: %s \n", file);
					register(file);
				}
				else if(attr.isRegularFile())
				{
					System.out.format("Regular file: %s size: %d \n", file, attr.size());
				}
				else
					System.out.format("Other: %s ", file);
        		return FileVisitResult.CONTINUE;
    		}

		});
	}
	// used for debugging: doesn't call file load/change
	public void processEventsTest()
	{
		for(; ; )
		{

			// wait for key to be signalled
			WatchKey key;
			try
			{
				key = watcher.take();
			}
			catch(InterruptedException x)
			{
				return;
			}

			Path dir = keys.get(key);
			if(dir == null)
			{
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for(WatchEvent<?> event : key.pollEvents())
			{
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if(kind == OVERFLOW)
				{
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				System.out.format("%s: %s\n", event.kind().name(), child);

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				if(recursive && (kind == ENTRY_CREATE))
				{
					try
					{
						if(Files.isDirectory(child, NOFOLLOW_LINKS))
						{
							registerAll(child);
						}
					}
					catch(IOException x)
					{
						// ignore to keep sample readbale
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if(!valid)
			{
				keys.remove(key);

				// all directories are inaccessible
				if(keys.isEmpty())
				{
					break;
				}
			}
		}
	}
	/** Process all events for keys queued to the watcher */
	public void processEvents()
	{
		for(; ; )
		{

			// wait for key to be signalled
			WatchKey key;
			try
			{
				key = watcher.take();
			}
			catch(InterruptedException x)
			{
				return;
			}

			Path dir = keys.get(key);
			if(dir == null)
			{
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for(WatchEvent<?> event : key.pollEvents())
			{
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if(kind == OVERFLOW)
				{
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path filePath = dir.resolve(name);
				String filename = filePath.getFileName().toString();
				boolean isFile = !Files.isDirectory(filePath, NOFOLLOW_LINKS);

				// if a file, attempt to load it
				if(project != null && isFile)
				{
					BasicFileAttributes fileAttributes = null;
					if(kind != ENTRY_DELETE)
					{
						try
						{
							if(!filePath.toFile().exists())
							{
								StsException.systemError(this, "processEvents", "File " + filename + " doesn't exist!");
								return;
							}
							fileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
						}
						catch(Exception e)
						{
							StsException.systemError(this, "processEvents", "Failed to find attributes for " + filePath.toString());
							return;
						}
					}
					if(kind == ENTRY_CREATE)
					{
						if(debug) StsException.systemDebug(this, "processEvents", "File: " + filename + " CREATED");
						fileAttributesMap.put(filename, fileAttributes);
						StsToolkit.sleep(1000);
						project.loadSourceFile(filePath);
					}
					else if(kind == ENTRY_MODIFY)
					{
						if(debug) StsException.systemDebug(this, "processEvents", "File: " + filename + " MODIFIED");
						project.changedSourceFile(filePath);
					}
					else if(kind == ENTRY_DELETE)
					{
						 if(debug) StsException.systemError(this, "processEvents", "File: " + filename + " DELETED");
					}
				}
				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				else if(kind == ENTRY_CREATE && recursive)
				{
					if(debug) StsException.systemDebug(this, "processEvents", "Directory : " + filename + " CREATED ");
					try
					{
						registerAll(filePath);
					}
					catch(IOException x)
					{
						// ignore to keep sample readable
					}
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if(!valid)
			{
				keys.remove(key);

				// all directories are inaccessible
				if(keys.isEmpty())
				{
					break;
				}
			}
		}
	}

	private boolean isModified(BasicFileAttributes fileAttributes, String filename)
	{
		BasicFileAttributes oldFileAttributes = fileAttributesMap.get(filename);
		fileAttributesMap.put(filename, fileAttributes);
		if(oldFileAttributes == null) return true;
		return fileAttributes.lastModifiedTime().compareTo(oldFileAttributes.lastModifiedTime()) > 0;
	}

	public void cancel()
	{
		Set<WatchKey> watchKeys = keys.keySet();
		for(WatchKey watchKey : watchKeys)
			watchKey.cancel();
	}

	static void usage()
	{
		System.err.println("usage: java StsSourceDirectoriesWatch [-r] dir");
		System.exit(-1);
	}

	public static void main(String[] args) throws IOException
	{
		// parse arguments
		if(args.length == 0 || args.length > 2)
			usage();
		boolean recursive = false;
		int dirArg = 0;
		if(args[0].equals("-r"))
		{
			if(args.length < 2)
				usage();
			recursive = true;
			dirArg++;
		}

		// register directory and process its events
		Path dir = Paths.get(args[dirArg]);
		new StsSourceDirectoriesWatch(dir, recursive).processEvents();
	}
}