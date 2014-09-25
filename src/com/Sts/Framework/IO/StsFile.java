package com.Sts.Framework.IO;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;

public class StsFile extends StsAbstractFile
{
	RandomAccessFile raf;
	// ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

	static final boolean debugTimer = true;
	static StsNanoTimer timer;
	static
	{
		if(debugTimer) { timer = new StsNanoTimer("StsFile Lock timer"); };
	}

	protected StsFile()
	{
	}

	protected StsFile(String directory, String filename) throws URISyntaxException, MalformedURLException, StsException
	{
		this.filename = filename;
		String pathname = new String(directory + filename);
		constructURL(pathname);
		parseFilename();
	}

	private StsFile(String pathname) throws URISyntaxException, MalformedURLException, StsException
	{
		filename = getFilenameFromPathname(pathname);
		constructURL(pathname);
		parseFilename();
	}

	private StsFile(Path path) throws MalformedURLException, StsException
	{
		filename = getFilenameFromPath(path);
		if(!isFilenameValid(filename)) throw new StsException(StsException.WARNING, "filename is invalid: " + filename);
		constructURL(path);
		parseFilename();
	}

	private void constructURL(String pathname) throws URISyntaxException, MalformedURLException, StsException
	{
		if(pathname.startsWith("file:"))
			pathname = StsStringUtils.trimPrefix(pathname, "file:");
		File file = new File(pathname);
		url = file.toURI().toURL();
//        url = new URL("file", "", pathname);
	}

	private void constructURL(Path path) throws MalformedURLException
	{
		url = path.toUri().toURL();
	}

	static public StsFile constructor(String directory, String filename)
	{
		try
		{
			if(filename == null) return null;
			if(!isFilenameValid(filename))
				return null;

			if(!directory.endsWith(File.separator))
				return new StsFile(directory + File.separator, filename);
			else
				return new StsFile(directory, filename);
		}
		catch(Exception e)
		{
			StsException.outputException("StsFile.constructor(directory, filename) failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	static public StsFile constructor(String pathname)
	{
		try
		{
			String filename = getFilenameFromPathname(pathname);
			if(!isFilenameValid(filename))
				return null;
			return new StsFile(pathname);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "constructor", "Failed to construct file: " + pathname, e);
			return null;
		}
	}

	static public StsFile constructor(Path path)
	{
		try
		{
			return new StsFile(path);
		}
		catch(Exception e)
		{
			StsException.systemError(StsFile.class, "constructor(path)", "Failed to construct file for: " + path.toString());
			return null;
		}
	}

	static public DataInputStream constructDIS(String pathname)
	{
		try
		{
			String filename = getFilenameFromPathname(pathname);
			if(!isFilenameValid(filename)) return null;
			StsFile file = new StsFile(pathname);
			return file.getDataInputStream();
		}
		catch(Exception e)
		{
			StsException.outputException("StsFile.constructor(pathname) failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	static public DataOutputStream constructDOS(String pathname)
	{
		try
		{
			String filename = getFilenameFromPathname(pathname);
			if(!isFilenameValid(filename)) return null;
			StsFile file = new StsFile(pathname);
			return file.getDataOutputStream();
		}
		catch(Exception e)
		{
			StsException.outputException("StsFile.constructor(pathname) failed.",
					e, StsException.WARNING);
			return null;
		}
	}

	static public String getFilenameFromPathname(String pathname)
	{
		String fileSeparator = File.separator;
		int separatorIndex = pathname.lastIndexOf(fileSeparator);
		if(separatorIndex == -1 && !fileSeparator.equals("/"))
			separatorIndex = pathname.lastIndexOf("/");
		if(separatorIndex == -1)
		{
			//throw new StsException(StsException.WARNING, "StsFile(pathname) failed. Didn't find separator.");
			return pathname; // May just be a filename with no path.
		}
		int length = pathname.length();
		return pathname.substring(separatorIndex + 1, length);
	}

	static public String getFilenameFromPath(Path path)
	{
		return path.getFileName().toString();
	}

	public FileInputStream getLockedFileInputStream()
	{
		try
		{
			FileInputStream fis = new FileInputStream(getFile());
			inputChannel = fis.getChannel();
			inputFileLock = inputChannel.lock();
			return fis;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getInputLockFileChannel", e);
			return null;
		}
	}

	public FileOutputStream getLockedFileOutputStream()
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(getFile());
			outputChannel = fos.getChannel();
			outputFileLock = outputChannel.lock();
			return fos;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getOutputLockFileChannel", e);
			return null;
		}
	}

	public InputStream getInputStream() throws IOException
	{
		return new FileInputStream(getFile());
	}

	public FileInputStream getFileInputStream()
	{
		try
		{
			return (FileInputStream) url.openStream();
		}
		catch(Exception e)
		{
			StsException.systemError(this, "getFileInputStream", "file not found: " + filename);
			return null;
		}
	}

	public InputStream getMonitoredInputStream(Component parentComponent)
	{
		try
		{
			InputStream is = url.openStream();
			return new BufferedInputStream(
					new ProgressMonitorInputStream(parentComponent, "Reading " + url.getPath(), is));
		}
		catch(Exception e)
		{
//            StsException.systemError("StsFile.getInputStream() failed, file not found: " + filename);
			return null;
		}
	}

	public DataInputStream getDataInputStream() throws IOException
	{
		InputStream is = getInputStream(); // true: append write to end of file
		BufferedInputStream bis = new BufferedInputStream(is);
		return new DataInputStream(bis);
	}


	public OutputStream getOutputStream() throws FileNotFoundException
	{
		return getOutputStream(false);
	}

	public OutputStream getOutputStream(boolean append) throws FileNotFoundException
	{
		File file = getFile();
		if(file == null) return null;
		return new FileOutputStream(file, append);
	}

	public OutputStream getOutputStreamAndPosition(long position) throws FileNotFoundException
	{
		File file = getFile();
		try
		{
			FileOutputStream outputStream = new FileOutputStream(file, true);
			FileChannel channel = outputStream.getChannel();
			channel.position(position);
			long filePosition = channel.position();
			if(filePosition != position)
			{
				StsException.systemError(this, "openWrite(position", "Failed to set file position: " + filePosition + " to desired position: " + position);
			}
			System.out.println("FILE DEBUG: Open output file and position " + filename +
					" position: " + filePosition + " length: " + file.length() +
					" thread: " + Thread.currentThread().getName() +
					" time: " + System.currentTimeMillis());
			return outputStream;
		}
		catch(Exception e)
		{
			return getOutputStream(true);
		}
	}

	public DataOutputStream getDataOutputStream() throws FileNotFoundException
	{
		OutputStream os = getOutputStream(true); // true: append write to end of file
		BufferedOutputStream bos = new BufferedOutputStream(os);
		return new DataOutputStream(bos);
	}

	static public boolean checkDirectory(String directory)
	{
		StsFile file = StsFile.constructor(directory);
		if(file.exists()) return true;
		try
		{
			file.getFile().mkdirs();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "checkDirectory", e);
			return false;
		}
	}

	static public StsFile checkGetDirectory(String directory)
	{
		StsFile file = StsFile.constructor(directory);
		if(file.exists()) return file;
		try
		{
			file.getFile().mkdirs();
			return file;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "checkDirectory", e);
			return null;
		}
	}

	static public StsFile checkCreateFile(String directory, String filename)
	{
		String pathname = null;
		try
		{
			StsFile dir = StsFile.constructor(directory);
			if(!dir.exists()) dir.getFile().mkdirs();
			pathname = directory + filename;
			return StsFile.constructor(pathname);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "checkCreateFile", "Failed for file: " + pathname, e);
			return null;
		}
	}

	public boolean delete()
	{
		File file = getFile();
		if(file == null) return false;
		return file.delete();
	}

	static public boolean deleteDirectory(String pathname)
	{
	 	File directory = new File(pathname);
	 	return deleteDirectory(directory);
	}

	static public boolean deleteDirectory(Path path)
	{
	 	File directory = path.toFile();
	 	return deleteDirectory(directory);
	}

	public boolean deleteDirectoryFiles()
	{
		return deleteDirectoryFiles(getFile());
	}

	static public boolean deleteDirectoryFiles(String pathname)
	{
		File directory = new File(pathname);
		return deleteDirectoryFiles(directory);
	}

	static public boolean deleteDirectoryFiles(File directory)
	{
		if(directory == null) return false;
		if(!directory.exists()) return true;
		if(!directory.isDirectory()) return false;
		String[] list = directory.list();
		if(list == null) return true;
		for(int i = 0; i < list.length; i++)
		{
			File entry = new File(directory, list[i]);
			if(entry.isDirectory())
			{
				if(!deleteDirectory(entry))
					return false;
			}
			else
			{
				if(!entry.delete())
					return false;
			}
		}
		return true;
	}

	public boolean deleteDirectory()
	{
		return deleteDirectory(getFile());
	}

	static public boolean deleteDirectory(File directory)
	{
		if(directory == null)
			return false;
		if(!directory.exists())
			return true;
		if(!directory.isDirectory())
			return false;

		String[] list = directory.list();

		// Some JVMs return null for File.list() when the
		// directory is empty.
		if(list != null)
		{
			for(int i = 0; i < list.length; i++)
			{
				File entry = new File(directory, list[i]);

				//        System.out.println("\tremoving entry " + entry);

				if(entry.isDirectory())
				{
					if(!deleteDirectory(entry))
						return false;
				}
				else
				{
					if(!entry.delete())
						return false;
				}
			}
		}

		return directory.delete();
	}

	static public boolean deleteFile(String pathname)
	{
		return deleteFile(Paths.get(pathname));
	}

	static public boolean deleteFile(String directory, String filename)
	{
		return deleteFile(Paths.get(directory, filename));
	}

	static public boolean deleteFile(Path path)
	{
		File file = path.toFile();
		if(!file.exists()) return false;
		return file.delete();
	}

	public void writeStringsToFile(String[] strings)
	{
		try
		{
			if(strings == null) return;
			if(!openWriter()) return;
			for(int n = 0; n < strings.length; n++)
				writeLine(strings[n]);
		}
		catch(Exception e)
		{
			StsException.outputException("StsProject.writeStringsToFile() failed.",
					e, StsException.WARNING);
		}
		finally
		{
			closeWriter();
		}
	}

	public String[] readStringsFromFile() throws IOException
	{
		String[] strings = new String[0];
		String string;

		if(!openReader()) return strings;
		try
		{
			while ((string = readLine()) != null)
			{
				strings = (String[]) StsMath.arrayAddElement(strings, string);
			}
			return strings;
		}
		catch(Exception e)
		{
			StsException.systemError("Failed to read " + getPathname());
			return strings;
		}
		finally
		{
			close();
		}
	}

	public String getFilenameStem()
	{
		return name;
	}

	static public boolean copy(String source, String destination) throws IOException
	{
		File testFile = new File(source);
		if(!testFile.exists())
			return false;

		FileChannel srcChannel = new FileInputStream(source).getChannel();

		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(destination).getChannel();

		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		// Close the channels
		srcChannel.close();
		dstChannel.close();

		return true;
	}

	void removeLastRepeat(ArrayList<StsFile> files)
	{
		for(StsAbstractFile file : files)
			if(nameSame(file)) return;
		files.add(this);
	}

	void removeFirstRepeat(ArrayList<StsFile> files)
	{
		for(StsAbstractFile file : files)
		{
			if(nameSame(file))
			{
				files.remove(file);
				files.add(this);
				return;
			}
		}
		files.add(this);
	}

	static public void clearDirectoryAndFiles(String pathname)
	{
		clearDirectoryAndFiles(new File(pathname));
	}

	/** recursively removes all files in this directory tree */
	static public void clearDirectoryAndFiles(File parentFile)
	{
		if(!parentFile.exists()) return;
		if(parentFile.isDirectory())
		{
			File[] files = parentFile.listFiles();
			for(File file : files)
				clearDirectoryAndFiles(file);
		}
		parentFile.delete();
	}

	public boolean isWritable() { return true; }

	public long lastModified()
	{
		File file = new File(getPathname());
		return file.lastModified();
	}

	static public void recursiveDelete(String pathname)
	{
		File file = new File(pathname);
		if(!file.exists()) return;
		recursiveDelete(file);
	}

	static public void recursiveDelete(File file)
	{
		try
		{
			if(file.isDirectory())
			{
				for(File childFile : file.listFiles())
				{
					recursiveDelete(childFile);
				}
			}
			if(file.exists()) file.delete();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "recursiveDelete", e);
		}
	}

	public String toString() { return filename; }

	static public void main(String[] args)
	{
		String directory = null;
		String filename = null;
		try
		{
			directory = "G:\\RealTimeTest\\StsWells\\1G-03RTwTime\\Binaries\\";
			StsFile.checkDirectory(directory);
			filename = "well.bin.1G-03RTwTime.X.0";
			StsFile stsFile = StsFile.constructor(directory, filename);
			stsFile.createNewFile();
			/*
						String s = File.separator;
						String dir = "c:";
						String one = "data";
						String two = "Q" + s + "WellsTest";
						String filename = "well.txt.1G#03";
						String name, parent, file;

						name = dir + s + one + s + two + s + filename;
						StsFile ff = StsFile.constructor(name);
					*/
			/*
						file = getFilenameFromPathname(name);
						parent = getDirectoryFromPathname(name);
						name = dir + s + one + s + two + s;
						parent = getDirectoryFromPathname(name);
						name = dir + s + one + s + two;
						parent = getDirectoryFromPathname(name);
						name = dir + s;
					*/
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsFile.class, "main", "directory: " + directory + " filename: " + filename, e);
		}
	}
}