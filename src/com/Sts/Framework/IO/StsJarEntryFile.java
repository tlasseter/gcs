package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

public class StsJarEntryFile extends StsAbstractFile
{
    public StsJarEntryFile(String jarPath, String entryName) throws MalformedURLException, IOException
    {
        String urlEntryName = "jar:" + jarPath + "!/" + entryName;
        this.filename = entryName;
//        System.out.println("StsJarEntryFile.init() urlEntryName: " + urlEntryName);
        url = new URL(urlEntryName);
        fileType = JAR;
    }

	public StsJarEntryFile(String entryName)
	{
		this.filename = entryName;
	}

    public OutputStream getOutputStream(boolean append)
    {
        return null;
    /*
        try
        {
            URLConnection connection = url.openConnection();
            return connection.getOutputStream();
        }
        catch(Exception e)
        {
            StsException.outputException("StsJarEntryFile.getOutputStream() failed.",
                e, StsException.WARNING);
            return null;
        }
    */
    }

    public OutputStream getOutputStreamAndPosition(long position)
    {
        return null;
    }

    public InputStream getInputStream()
    {
        try
        {
            JarURLConnection jarConnection = (JarURLConnection)(url.openConnection());
            return jarConnection.getInputStream();
//            InputStream is = url.openStream();
//            return new JarInputStream(is);
        }
        catch(Exception e)
        {
            StsException.outputException("StsJarEntryFile.getInputStream() failed. URL: " + url.toString(),
                e, StsException.WARNING);
            return null;
        }
    }

    public InputStream getMonitoredInputStream(Component component)
    {
        try
        {
            JarURLConnection jarConnection = (JarURLConnection)(url.openConnection());
            InputStream is = jarConnection.getInputStream();
            return new BufferedInputStream(
                    new ProgressMonitorInputStream(component, "Reading " + filename, is));
        }
        catch(Exception e)
        {
            StsException.outputException("StsJarEntryFile.getInputStream() failed. URL: " + url.toString(),
                e, StsException.WARNING);
            return null;
        }
    }

	public boolean isWritable() { return false; }

	public long lastModified()
	{
		return 0L;
	}
}
