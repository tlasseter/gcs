//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;

//TODO constructor should check if a file with the same name other than version exists; if it does, increment version
public class StsLongDataVector extends StsLongTransientValuesVector implements StsDataVectorFace, Cloneable
{
	StsTimeVectorSet vectorSet;
	transient public int fileColumnIndex;

	public StsLongDataVector()
	{
	}

	public StsLongDataVector(boolean persistent)
	{
		super(persistent);
	}

	public StsLongDataVector(StsTimeVectorSet vectorSet, StsColumnName columnName)
	{
		super(false);
		this.vectorSet = vectorSet;
		name = columnName.name;
		fileColumnIndex = columnName.fileColumnIndex;
	}

	public StsLongDataVector(long[] values)
	{
		setValues(values);
	}

	public String getBinaryFilename()
	{
		return vectorSet.getBinaryFilename(name);
	}

	public StsFile checkBinaryFile()
	{
		if(getBinaryFilename() == null) return null;
		StsFile file = StsFile.constructor(getBinaryPathname());
		if(file.exists())
			return file;
		else
			return null;
	}

	public String getBinaryPathname()
	{
		return vectorSet.getBinaryFilePathname(this);
	}

	public boolean binaryFileDateOK()
	{
		try
		{
			long asciiFileDate = vectorSet.vectorSetObject.dataSource.getSourceCreationTime();
			File binaryFile = new File(getBinaryPathname());
			long binaryFileDate = binaryFile.lastModified();
			return binaryFileDate >= asciiFileDate;
		}
		catch (Exception e)
		{
			StsException.outputWarningException(this, "binaryFileDateOK", e);
			return false;
		}
	}

	public long[] checkGetValues()
	{
		long[] values = super.checkGetValues();
		if(values != null) return values;
		if(!checkReadBinaryFile(true)) return null;
		return getValues();
	}

	static public StsLongDataVector getVectorWithName(StsObjectRefList abstractVectors, String name)
	{
		return (StsLongDataVector) StsAbstractVector.getVectorWithName(abstractVectors, name);
	}

	public boolean hasBinaryFile()
	{
		return vectorSet.hasBinaryFile(this);
	}

	public boolean checkWriteBinaryFile()
	{
		StsBinaryFile binaryFile = null;
		StsFile file = null;
		try
		{
			if(getBinaryFilename() == null) return true;
			String binaryPathname = getBinaryPathname();
			file = StsFile.constructor(binaryPathname);
			boolean fileExists = file.exists();
			if(fileExists) file.delete();
			//if(!delete && fileExists) return true;
			file.createNewFile();
			binaryFile = new StsBinaryFile(file);
			if(!binaryFile.openWrite()) return false;
			//if(!binaryFile.openWrite(append)) return false;
			writeVector(binaryFile);
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsDataVector.writeBinaryFile() failed.",
					e, StsException.WARNING);
			return false;
		}
		finally
		{
			try { if(binaryFile != null) binaryFile.close(); }
			catch(Exception e) { }
		}
	}

	public boolean checkReadBinaryFile(boolean loadValues)
	{
		StsFile file = checkBinaryFile();
		if(file == null) return false;
		if(loadValues)
			return loadVector(file);
		else
			return loadVectorHeader(file);
	}

	public boolean checkLoadVector()
	{
		if(values != null) return true;
		StsFile file = checkBinaryFile();
		if(file == null) return false;
		return loadVector(file);
	}

	public boolean loadVector(StsFile file)
	{
		StsBinaryFile binaryFile = new StsBinaryFile(file);
		return binaryFile.readVector(this);
	}

	public boolean loadVectorHeader(StsFile file)
	{
		StsBinaryFile binaryFile = new StsBinaryFile(file);
		return binaryFile.readVectorHeader(file, this);
	}

	public long[] getValues()
	{
		if(checkLoadVector())
			return values;
		else
			return null;
	}

	public boolean writeVector(StsBinaryFile binaryFile)
	{
		return binaryFile.writeVector(this);
	}

	public byte[] getFileHeaderBytes()
	{
        try
        {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
            dos.writeInt(size);  // save the size
			dos.writeLong(minValue);
			dos.writeLong(maxValue);
			return baos.toByteArray();
       }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getFileHeaderBytes", "Failed for " +name, e);
			return null;
        }
	}

	public int getNValueBytes() { return size*8; }

	public void appendTokenTable(String[][] tokenTable, int nTokenLines)
	{
		long[] values = new long[nTokenLines];
		for(int n = 0; n < nTokenLines; n++)
			values[n] = Long.parseLong(tokenTable[n][fileColumnIndex]);
		append(values);
		byte[] valueBytes = StsMath.longsToBytes(values);
		updateBinaryFile(this, valueBytes);
	}


    public String toString()
	{
		return vectorSet.name + "." + vectorSet.getGroup() + "." + name;
	}
}

































