package com.Sts.Framework.IO;

import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */


public class StsJar extends StsAbstractFileSet
{
    protected String jarFilename;
    protected URL jarURL;
	protected String group;
	protected String format;

    static final int bufSize = 1024;
	static final StsFilenameFilter filenameFilter = new StsFilenameFilter();

    protected StsJar(String directory, String jarFilename)
    {
		super(directory);
        this.jarFilename = jarFilename;
    }

    static public StsJar constructor(String directory, String jarFilename)
    {
        StsJar jar = new StsJar(directory, jarFilename);
        if(!jar.initialize()) return null;
        return jar;
    }

    static public StsJar getJarInClassPath(String jarFilename)
    {
        boolean debug = false;

        StringBuffer buffer = new StringBuffer(System.getProperty("java.class.path"));

        try
        {
            if(debug) System.out.println("StsJar.getJarInClassPath(): ");
            StringTokenizer pathTokens = new StringTokenizer(buffer.toString(), File.pathSeparator);

            while(pathTokens.hasMoreElements())
            {
                String pathname = (String)pathTokens.nextElement();
                if(!pathname.endsWith(jarFilename)) continue;
                StsJar jar = new StsJar("", pathname);
                if(!jar.initialize()) return null;
                return jar;
            }
            return null;
        }
        catch(Exception e)
        {
            StsException.systemError("StsJar.getJarInClassPath() failed for file: " + jarFilename);
            return null;
        }
    }

    public boolean initialize()
    {
        try
        {
            String jarPath = "file:" + directory + jarFilename;
//            String jarPath = "jar:" + directory + jarFilename + "!/";
            jarURL = new URL(jarPath);
            if(jarURL == null)
            {
                StsException.systemError("StsJar.classInitialize() failed." +
                    "Could not find " + directory + jarFilename);
                return false;
            }

            return createFileSet(jarPath);
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.classInitialize() failed." +
                "Could not find " + directory + jarFilename,
                e, StsException.WARNING);
            return false;
        }
    }

    protected boolean createFileSet(String jarPath)
    {
        JarEntry jarEntry;
        try
        {
            URLConnection connection = jarURL.openConnection();
//            System.out.println("StsJar:Opened connection to URL:" + connection.getURL() );
            InputStream is = connection.getInputStream();
//            System.out.println("StsJar:Got InputStream");
            JarInputStream jis = new JarInputStream(is, false);
//            System.out.println("StsJar:New JarInputStream");
            if(getManifestEntries(jarPath, jis))
            {
                System.out.println("Has manifest...");
                return true;
            }
            while( (jarEntry = jis.getNextJarEntry()) != null)
            {
                String entryName = jarEntry.getName();
				if(accepted(entryName))
				{
//                System.out.println("Found entry=" + entryName);
					StsJarEntryFile jarEntryFile = new StsJarEntryFile(jarPath, entryName);
					abstractFiles.add(jarEntryFile);
				}
            }
            return abstractFiles.size() > 0;
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.classInitialize() failed." +
                "Could not find " + jarPath,
                e, StsException.WARNING);
            return false;
        }
    }

	public boolean addFiles(String jarPath)
	{
		JarEntry jarEntry;
		abstractFiles = new ArrayList<StsAbstractFile>();
		try
		{
			URLConnection connection = jarURL.openConnection();
//            System.out.println("StsJar:Opened connection to URL:" + connection.getURL() );
			InputStream is = connection.getInputStream();
//            System.out.println("StsJar:Got InputStream");
			JarInputStream jis = new JarInputStream(is, false);
//            System.out.println("StsJar:New JarInputStream");
			if(!getManifestEntries(jarPath, jis))
			{
				System.out.println("Has manifest...");
				return false;
			}
			while( (jarEntry = jis.getNextJarEntry()) != null)
			{
				String entryName = jarEntry.getName();
//              System.out.println("Found entry=" + entryName);
				StsJarEntryFile jarEntryFile = new StsJarEntryFile(jarPath, entryName);
				abstractFiles.add(jarEntryFile);
			}
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getFiles", "jarPath " + jarPath, e);
			return false;
		}
	}

	public void addFiles(StsAbstractFilenameFilter filenameFilter)
	{
	}

	public void constructSubSets() { }

	private boolean accepted(String entryName)
	{
		filenameFilter.parseStsFilename(entryName);
		return group.equals(filenameFilter.group) && format.equals(filenameFilter.format);
	}

    private boolean getManifestEntries(String jarPath, JarInputStream jis)
    {
        try
        {
            Manifest manifest = jis.getManifest();
            if(manifest == null) return false;

            Object[] jarEntryObjectNames = manifest.getEntries().keySet().toArray();
            if(jarEntryObjectNames == null) return false;
            int nEntries = jarEntryObjectNames.length;
            if(nEntries == 0) return false;
            StsJarEntryFile[] files = new StsJarEntryFile[nEntries];
            for(int n = 0; n < nEntries; n++)
            {
                String entryName = (String)jarEntryObjectNames[n];
                files[n] = new StsJarEntryFile(jarPath, entryName);
            }
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.getManifestEntries() failed.",
                e, StsException.WARNING);
            return false;
        }
    }

    public String getDescription()
    {
        return new String("jar file: " + jarFilename + " in directory: " + directory);
    }

	public StsJarEntryFile getCompareObject(String entryName)
	{
		return new StsJarEntryFile(entryName);
	}

    public Set getJarFileEntries()
    {
        try
        {
            if(jarURL == null) return null;
            URLConnection connection = jarURL.openConnection();
            InputStream is = connection.getInputStream();
            JarInputStream jis = new JarInputStream(is, false);
            Manifest manifest = jis.getManifest();
            return manifest.getEntries().keySet();
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.getJarFileEntries() failed.",
                e, StsException.WARNING);
            return null;
        }
    }

    public StsAbstractFile[] initializeAvailableFiles()
    {
        initialize();
        return abstractFiles.toArray(new StsAbstractFile[0]);
    }

/*
    public URL getJarFileEntryURL(String entryName)
    {
        String urlEntryName;

        try
        {
            urlEntryName = "jar:file:" + directory + "/" + jarFilename + "!/" + entryName;
            System.out.println("StsJar.getJarFileEntryURL() urlEntryName: " + urlEntryName);
            return new URL(urlEntryName);
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.getJarFileEntryURL() failed." +
            "Couldn't find jar file entry: " + jarFilename + "/" + entryName,
            e, StsException.WARNING);
            return null;
        }
    }
*/
    public File uncompressTo(String dirName, String filename)
    {
        InputStream is = null;
        BufferedInputStream bis;
        FileOutputStream fos = null;
        byte[] buf = null;

        try
        {
            File unpackDir = new File(dirName);
            boolean unpackDirExists = unpackDir.exists();
            if(unpackDirExists)
            {

                if(!unpackDir.isDirectory())
                {
                    StsException.systemError("StsJar.checkUnpack() failed." + "File: " + dirName + " is not a directory.");
                    return null;
                }
            }
            else
                unpackDir.mkdir();

            StsAbstractFile jarFileEntry = getFileEndingWith(filename);
            File outfile = new File(dirName, filename);
            if(!outfile.exists())
            {
                outfile.createNewFile();

                System.out.println("Copying jar file entry: " + directory + "/" + jarFilename + "!/" + filename + " to: " + outfile.getAbsolutePath());

                URL url = jarFileEntry.getURL();
                URLConnection urlConnection = url.openConnection();
                is = urlConnection.getInputStream();
                bis = new BufferedInputStream(is);

                fos = new FileOutputStream(outfile);
                if(buf == null) buf = new byte[bufSize];
                int len = 0;
                while( (len = bis.read(buf, 0, bufSize)) != -1)
                {
                    fos.write(buf, 0, len);
                }
            }
            return outfile;
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.uncompressTo() failed.", e, StsException.WARNING);
            return null;
        }
        finally
        {
            try
            {
                if(is != null) is.close();
                if(fos != null)
                {
                    fos.flush();
                    fos.close();
                }
            }
            catch(Exception e) { }
        }
    }
/*
    public boolean checkUnpack(String unpackDirname)
    {
        boolean unpackDirExists = false;
        InputStream is = null;
        BufferedInputStream bis;
        FileOutputStream fos = null;
        byte[] buf = null;

        try
        {
            File unpackDir = new File(unpackDirname);
            unpackDirExists = unpackDir.exists();
            if(unpackDirExists)
            {

                if(!unpackDir.isDirectory())
                {
                    StsException.systemError("StsJar.checkUnpack() failed." +
                        "File: " + unpackDirname + " is not a directory.");
                    return false;
                }
            }
            else
                unpackDir.mkdir();

            Set jarEntries = getJarFileEntries();
            int nJarEntries = jarEntries.size();
            if(nJarEntries == 0) return true;
            Iterator iter = jarEntries.iterator();
            while(iter.hasNext())
            {
                String entryName = (String)iter.next();
                File file = new File(unpackDirname, entryName);
                if(!file.exists())
                {
                    file.createNewFile();

                    System.out.println("Copying jar file entry: " + directory + "/" + jarFilename + "!/" + entryName +
                        " to: " + file.getAbsolutePath());

                    URL url = getJarFileEntryURL(entryName);
                    URLConnection urlConnection = url.openConnection();
                    is = urlConnection.getInputStream();
                    bis = new BufferedInputStream(is);

                    fos = new FileOutputStream(file);

                    if(buf == null) buf = new byte[bufSize];
                    int len = 0;
                    while( (len = bis.read(buf, 0, bufSize)) != -1)
                    {
                        fos.write(buf, 0, len);
                    }
                }
            }
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsJar.checkUnpack() failed.",
                e, StsException.WARNING);
            return false;
        }
        finally
        {
            try
            {
                if(is != null) is.close();
                if(fos != null)
                {
                    fos.flush();
                    fos.close();
                }
            }
            catch(Exception e) { }
        }
    }
*/
}
