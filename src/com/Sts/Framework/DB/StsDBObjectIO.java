package com.Sts.Framework.DB;

import java.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: S2S Systems LLC</p>
 *
 * @author John Friar
 * @version 1.0
 */

public interface StsDBObjectIO
{
	abstract public void writeObject(StsDBOutputStream out, Object obj) throws IOException, IllegalAccessException;
	abstract public Object readObject(StsDBInputStream in, Object obj) throws IOException;
	abstract public Object readObject(StsDBInputStream in) throws IOException;
	abstract public Object copyObject(Object oldObject) throws IOException, IllegalAccessException;
	abstract public void exportObject(StsObjectDBFileIO objectFileIO, Object obj) throws IllegalAccessException;
}
