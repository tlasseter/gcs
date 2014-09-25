package com.Sts.Framework.IO;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.Locks.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

/**
 * This class handles data files processed by S2S data loaders: wells, surfaces, seismic, etc.
 * All data files have a standard name structure:  group.format.name.subname.version where subname and version are optional.
 * "group" identifies the type of file, e.g. "seismic-grid" is the group name for surface grids in seismic format.
 * "format" for these files is either "txt" for ASCII files or "bin" for binary files.  Other files may be in Java object format "obj", for example.
 * "name" is the name of the object being read in and instantiated.
 * "subname" is used for objects which are part of another object identified by name.
 * "version" is an integer version number which is incremented as output files with changes for this same object are written so as not to overwrite
 * the original file.
 * The fileType is either an ascii file whose type is the same as group, or a binary file which is either an ordinary binary file, or an entry in a jar
 * file set or a webstart jar file set.
 */
abstract public class StsAbstractFile implements Comparable<StsAbstractFile>
{
	public String filename;
	public URL url;
	public String group;
	public String format;
	public String name;
	public String subname;
	public int version;
	/** fileType is set by the wizard and is used by data loaders to specify the type of loader to use. */
	public String fileType;
	/** Optional set of subFiles associated with this file used by the loader if needed in construction */
	public StsAbstractFile[] subFiles;

	BufferedReader reader = null;
    BufferedWriter writer = null;
    String line = null;
    int nLines = 0;
    long nBytes = 0;
	FileChannel inputChannel = null;
	FileLock inputFileLock = null;
	FileChannel outputChannel = null;
	FileLock outputFileLock = null;

	private StsAbstractFileLock readWriteLock = new StsAbstractFileLock(this);

	static public final String asciiFormat = "txt";
	static public final String binaryFormat = "bin";

	static public final String WEBJAR = "webJar";
	static public final String JAR = "jar";

	static public final String[] invalidCharacters = {"\\", "/", "*", "?", "\"", "<", ">", "|", "#"};

	static final boolean debug = false;

	abstract public InputStream getInputStream() throws IOException;
	abstract public OutputStream getOutputStream(boolean append) throws FileNotFoundException;
	abstract public OutputStream getOutputStreamAndPosition(long position) throws FileNotFoundException;
	abstract public InputStream getMonitoredInputStream(Component component);
	abstract public long lastModified();

	public StsAbstractFile()
	{
	}

	protected StsAbstractFile(String filename)
	{
		this.filename = filename;
	}

	public String getFilename() { return filename; }

	public URL getURL() { return url; }

	public URI getURIuri() throws URISyntaxException { return url.toURI(); }

	public int compareTo(StsAbstractFile other)
	{
		String otherFilename = other.getFilename();
		return filename.compareTo(otherFilename);
	}

	public String getURIString()
	{
		try
		{
			return url.toURI().getPath();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getURIString", e);
			return null;
		}
	}

	public String getURIDirectoryString()
	{
		try
		{
			return Paths.get(url.toURI()).getParent().toString();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getURIDirectoryString", e);
			return null;
		}
	}

	public String getPathname() { return getURLPathname(); }

	public String getURLPathname()
	{
		try
		{
			if(Main.isJarDB)
				return url.getPath();
			else
				return getFile().getPath();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getURIString", e);
			return null;
		}
	}

	static public String getDirectoryFromPathname(String pathname) throws StsException
	{
		Path path = Paths.get(pathname);
		return path.getParent().toString() + File.separator;
	}

	public String getDirectoryFromPathname()
	{
		try
		{
			return Paths.get(url.toURI()).getParent().toString() + File.separator;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getDirectoryFromPathname", "Failed getting URI from url: " + url.toString(), e);
			return null;
		}
	}

	public long length()
	{
		File file = getFile();
		if(file == null) return 0;
		return file.length();
	}

	public File getFile()
	{
		try
		{
			if(this instanceof StsJarEntryFile)
			{
				return new File(url.toExternalForm());
			}
			else
			{
				URI uri = url.toURI();
				return new File(uri);
			}
		}
		catch(Exception e)
		{
			StsException.systemError(this, "getFile", "Failed for URL: " + url.getPath());
			return null;
		}
	}

	public boolean createNewFile() throws Exception
	{
		File file = getFile();
		if(file == null) return false;
		file.createNewFile();
		return true;
	}

	public boolean isAFile()
	{
		File file = getFile();
		if(file == null) return false;
		return file.isFile();
	}

	public boolean isAFileAndNotDirectory()
	{
		File file = getFile();
		if(file == null) return false;
		return !file.isDirectory() && file.isFile();
	}

	public StsAbstractFile[] listFiles()
	{
		if(isAFileAndNotDirectory())
			return new StsAbstractFile[0];
		File[] files = getFile().listFiles();
		if(files == null) return new StsAbstractFile[0];
		StsAbstractFile[] abstractFiles = new StsAbstractFile[files.length];
		int n = 0;
		for(File file : files)
			abstractFiles[n++] = StsFile.constructor(file.getPath());
		return abstractFiles;
	}

	public boolean exists()
	{
		File file = getFile();
		if(file == null) return false;
		return file.exists();
	}

	static public boolean directoryExists(String pathname)
	{
		StsFile file = StsFile.constructor(pathname);
		if(!file.exists()) return false;
		return !file.isAFileAndNotDirectory();
	}

	public String toString()
	{
		if(format == null || format.equals("")) return filename;

		String fullname = name;
		if(subname != null)
			fullname = fullname + "." + subname;
		if(version > 0)
			fullname = fullname + "." + version;

		if(format.equals(StsAbstractFile.binaryFormat))
			return fullname + " (binary)";
		else
			return fullname;
	}

	public static boolean isFilenameValid(String fullFilename)
	{
		if(fullFilename == null) return false;
		String fname = null;
		for(int i = 0; i < invalidCharacters.length; i++)
		{
			// Need to just check the filename since some characters are allowed in directory names that are not in filenames
			// ex. URL (which we use in StsFile) does not handle spaces in filenames but is okay if in directory name
			try
			{fname = StsFile.getFilenameFromPathname(fullFilename);}
			catch(Exception e) { System.out.println("Error parsing filename from fullname.");}
			if(fname.indexOf(invalidCharacters[i]) > 0)
			{
				new StsMessage(null, StsMessage.ERROR, "StsFile: Invalid character (" + invalidCharacters[i] + ") in name " + fullFilename);
				return false;
			}
		}
		return true;
	}

	public void parseFilename(StsFilenameFilter filenameFilter)
	{
		if(!filenameFilter.parseCheckFilename(filename)) return;
		this.group = filenameFilter.getGroup();
		this.format = filenameFilter.getFormat();
		this.name = filenameFilter.getName();
		this.subname = filenameFilter.getSubname();
		this.version = filenameFilter.getVersion();
		setFileType();
	}

	public void parseFilename()
	{
		StsFilenameFilter filenameFilter = StsFilenameFilter.parseStsFilename(filename);
		if(filenameFilter == null) return;
		this.group = filenameFilter.getGroup();
		this.format = filenameFilter.getFormat();
		this.name = filenameFilter.getName();
		this.subname = filenameFilter.getSubname();
		this.version = filenameFilter.getVersion();
		setFileType();
	}


	public void setFileType()
	{
		if(format != null && format.equals(binaryFormat))
			fileType = binaryFormat;
		else
			fileType = group;
	}

	boolean nameSame(StsAbstractFile otherFilename)
	{
		return name.equals(otherFilename.name);
	}

	String getFullName()
	{
		return name + "." + subname + "." + version;
	}

	String getType()
	{
		return group + "." + format;
	}

	static public String[] getFileStemNames(StsAbstractFile[] files)
	{
		if(files == null) return new String[0];
		int nFiles = files.length;
		String[] names = new String[nFiles];
		for(int n = 0; n < nFiles; n++)
			names[n] = files[n].name;
		return names;
	}

	public StsAbstractFile[] getGroupFormatSubFiles(String group, String format)
	{
		if(subFiles == null) return new StsAbstractFile[0];
		int nSubFiles = subFiles.length;
		StsAbstractFile[] typeFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile subFile : subFiles)
		{
			if(subFile.compareTypeTo(group, format) == 0)
				typeFiles[n++] = subFile;
		}
		return (StsAbstractFile[]) StsMath.trimArray(typeFiles, n);
	}

	static public StsAbstractFile[] getGroupFormatSubFiles(File directory, String group, String format)
	{
		String dirString = directory.getPath() + File.separator;
		if(!directory.exists())
		{
			StsException.systemError(StsAbstractFile.class, "getGroupFormatSubFiles(dirPath...)", "File " + dirString + " doesn't exist.");
			return new StsAbstractFile[0];
		}
		String[] filenames = directory.list();
		if(filenames == null) return new StsAbstractFile[0];
		int nFilenames = filenames.length;
		if(nFilenames == 0) return new StsAbstractFile[0];
		StsAbstractFile[] typeFiles = new StsAbstractFile[nFilenames];
		StsFilenameFilter filter = StsFilenameFilter.constructGroupFormatFilter(group, format);
		int n = 0;
		for(String filename : filenames)
		{
			if(filter.accept(filename))
				try
				{
					StsFile file = new StsFile(dirString, filename);
					file.parseFilename();
					typeFiles[n++] = file;
				}
				catch(Exception e)
				{
					StsException.systemError(StsAbstractFile.class, "getGroupFormatSubFiles(dirPath...)", "Failed to construct file " + dirString + filename);
				}
		}
		return (StsAbstractFile[]) StsMath.trimArray(typeFiles, n);
	}

	public StsAbstractFile[] getDirectoryFiles()
	{
		String dirString = getPath() + File.separator;
		String[] filenames = getFile().list();
		if(filenames == null) return new StsAbstractFile[0];
		int nFilenames = filenames.length;
		if(nFilenames == 0) return new StsAbstractFile[0];
		StsAbstractFile[] files = new StsAbstractFile[nFilenames];
		for(int n = 0; n < nFilenames; n++)
		{
			try
			{
				StsFile file = new StsFile(dirString, filenames[n]);
				file.parseFilename();
				files[n] = file;
			}
			catch(Exception e)
			{
				StsException.systemError(StsAbstractFile.class, "getGroupFormatSubFiles(dirPath...)", "Failed to construct file " + dirString + filenames[n]);
			}
		}
		return files;
	}

	public StsAbstractFile[] getGroupSubFiles(String group)
	{
		if(subFiles == null) return new StsAbstractFile[0];
		int nSubFiles = subFiles.length;
		StsAbstractFile[] groupFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile subFile : subFiles)
		{
			if(subFile.group.equals(group) && !subFile.format.equals(StsLoader.FORMAT_BIN))
				groupFiles[n++] = subFile;
		}
		return (StsAbstractFile[]) StsMath.trimArray(groupFiles, n);
	}

	static public StsAbstractFile[] getGroupFiles(StsAbstractFile[] files, String group)
	{
		int nSubFiles = files.length;
		StsAbstractFile[] groupFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile file : files)
		{
			if(file.compareGroupTo(group) == 0)
				groupFiles[n++] = file;
		}
		return (StsAbstractFile[]) StsMath.trimArray(groupFiles, n);
	}

	static public StsAbstractFile[] getGroupTextFiles(StsAbstractFile[] files, String group)
	{
		int nSubFiles = files.length;
		StsAbstractFile[] groupFiles = new StsAbstractFile[nSubFiles];
		int n = 0;
		for(StsAbstractFile file : files)
		{
			if(file.group.equals(group) && !file.format.equals(StsLoader.FORMAT_BIN))
				groupFiles[n++] = file;
		}
		return (StsAbstractFile[]) StsMath.trimArray(groupFiles, n);
	}

	public int compareTypeTo(String group, String format)
	{
		int compare = this.group.compareTo(group);
		if(compare != 0) return compare;
		return this.format.compareTo(format);
	}

	public int compareGroupTo(String group)
	{
		return this.group.compareTo(group);
	}

	public String getGroup()
	{
		if(group == null) parseFilename();
		return group;
	}

	public boolean isGroupAndFormat(String matchGroup, String matchFormat)
	{
		if(this.group == null) parseFilename();
		return group.equals(matchGroup) && format.equals(matchFormat);
	}

	public void setSubFiles(int firstIndex, int lastIndex, ArrayList<StsAbstractFile> files)
	{
		this.name = name;
		int nSubFiles = lastIndex - firstIndex + 1;
		subFiles = new StsAbstractFile[nSubFiles];
		// System.arrayCastCopy(files, firstIndex, subFiles, 0, nSubFiles);
		files.subList(firstIndex, lastIndex + 1).toArray(subFiles);
		Arrays.sort(subFiles, StsAbstractFileTypeComparator.comparator);
	}

	public void setSubFiles(ArrayList<StsAbstractFile> files)
	{
		this.subFiles = files.toArray(new StsAbstractFile[0]);
	}

	public void setSubFiles(StsAbstractFile[] subFiles)
	{
		this.subFiles = subFiles;
	}

	public String getDirectory()
	{
		try
		{
			return getDirectoryFromPathname();
		}
		catch(Exception e)
		{
			StsException.systemError("StsFile.getDirectory() failed.");
			return null;
		}
	}

	public BufferedReader getReader()
	{
		return this.reader;
	}

	public StsAbstractFileLock getReadWriteLock()
	{
		if(readWriteLock == null)
			readWriteLock = new StsAbstractFileLock(this);
		return readWriteLock;
	}

	public class AbstractFileSortedSet<StsAbstractFile> extends TreeSet<StsAbstractFile>
	{

		public AbstractFileSortedSet(Comparator<StsAbstractFile> comparator)
		{
			super(comparator);
		}
	}


	public BufferedReader getOpenReader()
	{
		if(reader != null) return reader;
		if(!openReader()) return null;
		else return reader;
	}

	public boolean openReader()
	{
		return openReader(false);
	}

	public boolean openReader(boolean showMessage)
	{
		try
		{
			InputStream is = getInputStream();
			if(is == null) return false;
			this.reader = new BufferedReader(new InputStreamReader(is));
			return true;
		}
		catch(Exception e)
		{
			if(showMessage) new StsMessage(null, StsMessage.WARNING, "Failed to open file " + getURIString());
			StsException.systemError(this, "read", "Failed to open file for reading " + getURIString());
			return false;
		}
	}

    public void closeReader()
    {
        try
        {
            if(this.reader == null) return;
            this.reader.close();
			this.reader = null;
        }
        catch(Exception e) { }
    }

	public boolean openReaderWithErrorMessage()
	{
		try
		{
			if(reader != null) reader.close();
			InputStream is = getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			return true;
		}
		catch(Exception e)
		{
			new StsMessage(null, StsMessage.WARNING, "Failed to open file " + getPathname());
			return false;
		}
	}

	public BufferedWriter getOpenWriter()
	{
		if(writer != null) return writer;
		if(!openWriter()) return null;
		else return writer;
	}

	public boolean openWriter()
	{
		try
		{
			OutputStream os = getOutputStream(false);
			if(os == null) return false;
			writer = new BufferedWriter(new OutputStreamWriter(os));
			return true;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "openWriter", "Failed to open file for writing " + getURIString());
			return false;
		}
	}


    public void closeWriter()
    {
        try
        {
            if(writer == null) return;
            writer.close();
			writer = null;
        }
        catch(Exception e) { }
    }

	public boolean openWriterWithErrorMessage()
	{
		try
		{
			if(writer != null) writer.close();
			OutputStream os = getOutputStream(false);
			if(os == null) return false;
			writer = new BufferedWriter(new OutputStreamWriter(os));
			return true;
		}
		catch(Exception e)
		{
			new StsMessage(null, StsMessage.WARNING, "File not found " + getPathname());
			return false;
		}
	}

	public Writer getOpenWriterAppend()
	{
		if(writer != null) return writer;
		if(!openWriterAppend()) return null;
		return writer;
	}

	public boolean openWriterAppend()
	{
		try
		{
			if(writer != null) writer.close();
			OutputStream os = getOutputStream(true);
			writer = new BufferedWriter(new OutputStreamWriter(os));
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsAsciiFile.openWrite() failed." +
				"Can't write: " + filename, e, StsException.WARNING);
			return false;
		}
	}

	public boolean openWriterAppend(FileOutputStream fos)
	{
		try
		{
			if(writer != null) writer.close();
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsAsciiFile.openWrite() failed." +
				"Can't write: " + filename, e, StsException.WARNING);
			return false;
		}
	}

	public boolean openReadWrite() throws IOException
	{
		return openReader() && openWriter();
	}

	/** close this binary file */
	public boolean close()
	{
		try
		{
			if (writer != null)
			{
				writer.flush();
				writer.close();
				writer = null;
			}
			if (reader != null)
			{
				reader.close();
				reader = null;
			}
			return true;
		}
		catch (Exception e)
		{
			StsException.outputException("StsAsciiFile.close() failed."
					+ "Unable to close file " + filename, e, StsException.WARNING);
			return false;
		}
	}

	public String readLine() throws IOException
	{
		line = reader.readLine();
		if(debug) System.out.println("line " + nLines + ": " + line);
		if(line == null) return null;
		nLines++;
		nBytes += line.length();
		return line;
	}

	public String readNextNonBlankLine() throws IOException
	{
		line = reader.readLine();
		if(debug) System.out.println("line " + nLines + ": " + line);
		if(line == null) return null;
		nLines++;
		if(line.length() == 0)
			return readNextNonBlankLine();
		else
		{
			nBytes += line.length();
			return line;
		}
	}

	public String[] getTokens(String delimiters) throws IOException
	{
		line = reader.readLine();
		if(line == null) return null;
		line.trim();
		int nTokens = 0;
		StringTokenizer stringTokenizer = null;

		stringTokenizer = new StringTokenizer(line, delimiters);
		nTokens = stringTokenizer.countTokens();

		if(nTokens == 0) return null;

		nLines++;
		nBytes += line.length();
		String[] tokens = new String[nTokens];
		int n = 0;
		while(stringTokenizer.hasMoreTokens())
			tokens[n++] = stringTokenizer.nextToken();
		return tokens;
	}

	public String[] getTokens(String[] delimiters) throws IOException
	{
		line = reader.readLine();
		if(line == null) return null;
		line.trim();
		int nTokens = 0;
		StringTokenizer stringTokenizer = null;
		for(int i=0; i< delimiters.length; i++)
		{
			if(delimiters[i].equals(" "))
				stringTokenizer = new StringTokenizer(line);    // tab, space, cr, lf
			else
				stringTokenizer = new StringTokenizer(line, delimiters[i]);
			nTokens = stringTokenizer.countTokens();
			if(nTokens > 1) break;
		}
		if(nTokens == 0) return null;

		nLines++;
		nBytes += line.length();
		String[] tokens = new String[nTokens];
		int n = 0;
		while(stringTokenizer.hasMoreTokens())
			tokens[n++] = stringTokenizer.nextToken();
		return tokens;
	}

	public String[] getTokens() throws IOException
	{
		line = reader.readLine();
		if(line == null) return null;
		line.trim();

		StringTokenizer stringTokenizer = new StringTokenizer(line);
		int nTokens = stringTokenizer.countTokens();
		if(nTokens == 0) return null;

		nLines++;
		nBytes += line.length();
		String[] tokens = new String[nTokens];
		int n = 0;
		while(stringTokenizer.hasMoreTokens())
			tokens[n++] = stringTokenizer.nextToken();
		return tokens;
	}

	public Iterator getTokenIterator()
	{
		try
		{
			return new TokenIterator();
		}
		catch(Exception e)
		{
			StsException.outputException("StsAsciiFile.getTokenIterator() failed.",
				e, StsException.WARNING);
			return null;
		}
	}

	class TokenIterator implements Iterator
	{
		StringTokenizer stringTokenizer;
		Object nextToken = null;

		TokenIterator() throws IOException
		{
			line = reader.readLine();
			nLines++;
			if(line == null) return;
			line = line.trim();
			stringTokenizer = new StringTokenizer(line);
		}

		public boolean hasNext()
		{
			try
			{
				if(stringTokenizer.hasMoreElements())
				{
					nextToken = stringTokenizer.nextElement();
					return true;
				}
				line = reader.readLine();
				nLines++;
				if(line == null) return false;
				stringTokenizer = new StringTokenizer(line);
				if(!stringTokenizer.hasMoreElements()) return false;
				nextToken = stringTokenizer.nextElement();
				return true;
			}
			catch(Exception e)
			{
				StsException.outputException("StsAsciiFile.TokenIterator.hasNext() failed.",
					e, StsException.WARNING);
				return false;
			}
		}

		public Object next()
		{
			return nextToken;
		}

		public void remove()
		{
		}
	}

	public String getLine() { return line; }
	public int getNLines() { return nLines; }
	public long getNBytes() { return nBytes; }

	public void writeString(String string) throws IOException
	{
		writer.write(string + " ");
	}

	public void writeLine(String string) throws IOException
	{
		writer.write(string);
		writer.newLine();
	}

	public void endLine() throws IOException
	{
		writer.newLine();
	}

	public Path getPath() { return getFile().toPath(); }

	public void lockReader()
	{
		readWriteLock.lockReader();
	}

	public void unlockReader()
	{
		readWriteLock.unlockReader();
	}

	public void lockWriter()
	{
		readWriteLock.lockWriter();
	}

	public void unlockWriter()
	{
		readWriteLock.unlockWriter();
	}

	public boolean tryLockReader()
	{
		return readWriteLock.tryLockReader();
	}

	public boolean tryLockWriter()
	{
		return readWriteLock.tryLockWriter();
	}

	public BasicFileAttributes getFileAttributes()
	{
		Path filePath = getFile().toPath();
		try
		{
    		return Files.readAttributes(filePath, BasicFileAttributes.class);
		}
		catch(IOException e)
		{
			StsException.systemError(this, "getFileAttributes", "Filed to readAttributes for file " + filePath.toString());
			return null;
		}
	}

	public boolean copyTo(String destination) throws IOException
	{
		FileChannel srcChannel = new FileInputStream(this.getPathname()).getChannel();

		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(destination).getChannel();

		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		// Close the channels
		srcChannel.close();
		dstChannel.close();

		return true;
	}

	public boolean copyTo(StsAbstractFile destination) throws IOException
	{
		FileChannel srcChannel = new FileInputStream(this.getPathname()).getChannel();

		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(destination.getPathname()).getChannel();

		// Copy file contents from source to destination
		dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		// Close the channels
		srcChannel.close();
		dstChannel.close();

		return true;
	}

}
