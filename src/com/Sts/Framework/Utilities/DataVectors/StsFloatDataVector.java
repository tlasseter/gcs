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
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

//TODO constructor should check if a file with the same name other than version exists; if it does, increment version
public class StsFloatDataVector extends StsFloatTransientValuesVector implements StsDataVectorFace, Cloneable
{
	StsVectorSet vectorSet;
	transient public int fileColumnIndex = -1;

	public StsFloatDataVector()
	{
	}

	public StsFloatDataVector(boolean persistent)
	{
		super(persistent);
	}

	public StsFloatDataVector(int capacity, int growInc)
	{
		this.capacity = capacity;
		this.growInc = growInc;
	}

	public StsFloatDataVector(StsVectorSet vectorSet, String name)
	{
		super(false);
		this.vectorSet = vectorSet;
		this.name = name;
	}

	public StsFloatDataVector(StsVectorSet vectorSet, StsColumnName columnName)
	{
		super(false);
		this.vectorSet = vectorSet;
		name = columnName.name;
		this.version = vectorSet.getVersion();
		fileColumnIndex = columnName.fileColumnIndex;
	}

	public StsFloatDataVector(String name, ArrayList<StsPoint> points, int index)
	{
		this.name = name;
		int nValues = points.size();
		float[] values = new float[nValues];
		for(int n = 0; n < nValues; n++)
			values[n] = points.get(n).v[index];
		checkSetValues(values);
	}

	public StsFloatDataVector(String name, float[] values)
	{
		this.name = name;
		// reinitializeName();
		checkSetValues(values);
	}

	public StsFloatDataVector(float[] values)
	{
		checkSetValues(values);
	}

	public StsFloatDataVector(int capacity, int inc, float nullValue)
	{
		super(capacity, inc, nullValue);
	}

	public StsFloatDataVector(String asciiDir, String binaryDir, String group, String name, String subname, int version)
	{

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
			getBinaryPathname();
			File binaryFile = new File(vectorSet.getBinaryDirectoryPathname() + getBinaryFilename());
			long binaryFileDate = binaryFile.lastModified();
			return binaryFileDate >= asciiFileDate;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "binaryFileDateOK", e);
			return false;
		}
	}

	static public StsFloatDataVector getVectorWithName(StsObjectRefList abstractVectors, String name)
	{
		return (StsFloatDataVector) StsAbstractVector.getVectorWithName(abstractVectors, name);
	}

	public boolean hasBinaryFile()
	{
		return vectorSet.hasBinaryFile(this);
	}

	public boolean writeBinaryFileAndClear()
	{
		if(!checkWriteBinaryFile()) return false;
		clearValues();
		return true;
	}

	static public boolean checkWriteBinaryFiles(ArrayList<StsFloatDataVector> dataVectors)
	{
		boolean writeOK = true;
		for(StsDataVectorFace dataVector : dataVectors)
			if(!dataVector.checkWriteBinaryFile()) writeOK = false;
		return writeOK;
	}

	//TODO rewrite this to use mapped byte buffers
	public boolean checkWriteBinaryFile()
	{
		StsBinaryFile binaryFile = null;
		StsFile file = null;
		try
		{
			// if(getBinaryFilename() == null) return true;
			String binaryPathname = getBinaryPathname();
			file = StsFile.constructor(binaryPathname);
			boolean fileExists = file.exists();
			if(fileExists) file.delete();
			// if(!delete && fileExists) return true;
			file.createNewFile();
			binaryFile = new StsBinaryFile(file);
			if(!binaryFile.openWrite()) return false;
			//if(!binaryFile.openWrite(append)) return false;
			binaryFile.writeVector(this);
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

	public byte[] getFileHeaderBytes()
	{
        try
        {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeByte(units);
            dos.writeInt(size);  // save the size
			dos.writeFloat(getMinValue());
			dos.writeFloat(getMaxValue());
			return baos.toByteArray();
       }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getFileHeaderBytes", "Failed for " + name, e);
			return null;
        }
	}

	public int getNValueBytes() { return size*4; }

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
		return binaryFile.readVectorHeader(this);
	}

	public float[] getValues()
	{
		if(!checkLoadVector()) return null;
		return values;
	}

	public void appendTokens(String[] tokens)
	{
		float value;
		try
		{
			value = Float.parseFloat(tokens[fileColumnIndex]);
		}
		catch(Exception e)
		{
			value = StsParameters.nullValue;
		}
		append(value);
	}

	public void appendTokenTable(String[][] tokenTable, int nTokenLines)
	{
		// this might be a required column, but it doesn't exist; we will construct it later (e.g., mdepth from depth)
		if(fileColumnIndex == -1) return;
		float[] values = new float[nTokenLines];
		for(int n = 0; n < nTokenLines; n++)
		{
			float value = Float.parseFloat(tokenTable[n][fileColumnIndex]);
			if(isNullValue(value)) value = StsParameters.nullValue;
			values[n] = value;
		}
		append(values);
		byte[] valueBytes = StsMath.floatsToBytes(values);
		updateBinaryFile(this, valueBytes);
	}

	private String getToken(String[] tokens, int index)
	{
		if(tokens == null) return "null";
		if(index < 0) return "index=" + index;
		if(index >= tokens.length) return "index >= nTokens " + tokens.length;
		return tokens[index];
	}

	public boolean writeVector(StsBinaryFile binaryFile)
	{
		return binaryFile.writeVector(this);
	}

	public boolean readVector(StsBinaryFile binaryFile)
	{
		return binaryFile.readVector(this);
	}

	public String toString()
	{
		if(vectorSet == null)
			return name;
		else
			return vectorSet.name + "." + name;
	}
}

































