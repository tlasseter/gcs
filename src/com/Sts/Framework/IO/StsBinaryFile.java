

//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;

public class StsBinaryFile
{
    // instance fields
    StsAbstractFile file = null;
    public String filename = null;
    public DataInputStream dis = null;
    public DataOutputStream dos = null;
    boolean includeSizeInFile = true;

	static final boolean debug = true;

    public StsBinaryFile(StsAbstractFile file)
    {
        this.file = file;
        filename = file.getFilename();
    }

	public boolean openLockRead()
	{
		file.lockReader();
		if(openRead()) return true;
		file.unlockReader();
		return false;
	}

    public boolean openRead()
    {
        try
        {
            if(dis != null) close();
            InputStream is = file.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            dis = new DataInputStream(bis);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsBinaryFile.openReadAndCheck() failed." +
                "Can't read: " + file.getFilename(), e, StsException.WARNING);
			file.close();
            return false;
        }
    }

    public boolean openLockWrite()
    {
		file.lockWriter();
		if(openWrite(true)) return true;
		file.unlockWriter();
		return false;
    }

    public boolean openWrite()
    {
        return openWrite(true);
    }

    public boolean openWrite(boolean append)
    {
        try
        {
            if(dos != null) close();
            OutputStream os = file.getOutputStream(append); // true: append write to end of file
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsBinaryFile.openWrite() failed." +
                "Can't write: " + filename, e, StsException.WARNING);
            return false;
        }
    }

	public boolean openLockReadWrite()
	{
		if(!openLockRead()) return false;
		if(openLockWrite()) return true;
		return closeUnlockRead();
	}

    public boolean openReadWrite()
    {
        return openRead() && openWrite();
    }

	public boolean closeUnlockReadWrite()
	{
		return closeUnlockRead() && closeUnlockWrite();
	}

	public boolean closeUnlockRead()
	{
		file.unlockReader();
		return close();
	}

	public boolean closeUnlockWrite()
	{
		file.unlockWriter();
		return close();
	}

    /** close this binary file */
    public boolean close()
    {
        try
        {
            if (dos != null)
            {
                dos.flush();
                dos.close();
                dos = null;
            }
            if (dis != null)
            {
                dis.close();
                dis = null;
            }

			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.close() failed."
                    + "Unable to close file " + filename, e, StsException.WARNING);
			return false;
        }
    }
    /** set/get an float values in this binary file */
    public boolean setFloatValues(float[] vector, boolean incSize)
    {
        includeSizeInFile = incSize;
        return setFloatValues(vector);
    }
    /** set/get an float values in this binary file */
    public boolean setLongValues(long[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setLongValues() failed."
                + "File " + filename + " not properly opened for writing");
            return false;
        }

        try
        {
            if(includeSizeInFile)
                dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeLong(vector[i]);
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setLongValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
            return false;
        }
    }

    public boolean writeVector(StsLongDataVector longVector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.writeVector() failed."
                + "File " + filename + " not properly opened for writing");
            return false;
        }

        try
        {
            if(longVector == null || longVector.getValues() == null)
            {
                dos.writeInt(0);
                dos.writeLong(0);
                dos.writeLong(0);
                return true;
            }

            long[] values = longVector.getValues();
            int size = values.length;
            dos.writeInt(size);  // save the size
            dos.writeLong(longVector.getMinValue());
            dos.writeLong(longVector.getMaxValue());
            for (int n=0; n<size; n++) dos.writeLong(values[n]);
			if(debug) StsException.systemDebug(this, "writeVector", this.filename + " wrote " + size + " values.");
            return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readVector" + "Unable to read vector from file " + filename, e);
            return false;
        }

    }

	public boolean readVector(StsLongDataVector dataVector)
    {
		if(!openLockRead()) return false;
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.readVector() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
			int size = dis.readInt();
			dataVector.setSizeAndCapacity(size);
			dataVector.setMinValue(dis.readLong());
			dataVector.setMaxValue(dis.readLong());
			long[] values = new long[size];
            for (int n=0; n<size; n++)
				values[n] = dis.readLong();
			dataVector.setValues(values); // this also sets size
			if(debug) StsException.systemDebug(this, "readVector", this.filename + " read " + size + " values.");
			return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readVector" + "Unable to read vector from file " + filename, e);
			return false;
        }
		finally
		{
			closeUnlockRead();
		}
    }

	public boolean readVectorHeader(StsFile file, StsLongDataVector dataVector)
    {
		if(!openLockRead()) return false;
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.readVector() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
			int nValues = dis.readInt();
			dataVector.setMinValue(dis.readLong());
			dataVector.setMaxValue(dis.readLong());
			dataVector.setSizeAndCapacity(nValues);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readVectorHeader" + "Unable to read vector from file " + filename, e);
			return false;
        }
		finally
		{
			closeUnlockRead();
		}
    }

    /** set/get an float values in this binary file */
    public boolean setLongValues(long[] vector, byte[] nullFlags, byte notNullFlag, long nullValue)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setLongValues() failed."
                + "File " + filename + " not properly opened for writing");
            return false;
        }

        try
        {
            dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++)
            {
                if(nullFlags[i] == notNullFlag)
                    dos.writeLong(vector[i]);
                else
                    dos.writeLong(nullValue);
            }
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setLongValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
            return false;
        }
    }
    /** set/get an float values in this binary file */
    public boolean setFloatValues(float[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setFloatValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            if(includeSizeInFile)
                dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeFloat(vector[i]);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setFloatValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

    /** set/get a float vector in this binary file */
    public boolean writeVector(StsFloatTransientValuesVector floatVector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.writeVector() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            if(floatVector == null || floatVector.getValues() == null)
            {
				dos.writeByte(0);
                dos.writeInt(0);
                dos.writeFloat(0.0f);
                dos.writeFloat(0.0f);
				dos.writeDouble(0.0);
                return true;
            }
			dos.writeByte(floatVector.getUnits());
			float[] values = floatVector.getValues();
			int size = floatVector.getSize();
            dos.writeInt(size);  // save the size
			dos.writeFloat(floatVector.getMinValue());
			dos.writeFloat(floatVector.getMaxValue());
			dos.writeDouble(floatVector.getOrigin());
            for (int n=0; n<size; n++) dos.writeFloat(values[n]);
			if(debug) StsException.systemDebug(this, "writeVector", this.filename + " wrote " + size + " values.");
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.writeVector() failed." +
                "Unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

	public boolean readVector(StsFloatDataVector dataVector)
    {
		if(!openLockRead()) return false;
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.readVector() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
			dataVector.setUnits(dis.readByte());
			int size = dis.readInt();
			dataVector.setSizeAndCapacity(size);
			dataVector.setMinValue(dis.readFloat());
			dataVector.setMaxValue(dis.readFloat());
			dataVector.setOrigin(dis.readDouble());
			float[] values = new float[size];
            for (int n=0; n<size; n++)
				values[n] = dis.readFloat();
			dataVector.setValues(values);
			if(debug) StsException.systemDebug(this, "readVector", this.filename + " read " + size + " values.");
			return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readVector" + "Unable to read vector from file " + filename, e);
			return false;
        }
		finally
		{
			closeUnlockRead();
		}
    }

	public boolean readVectorHeader(StsFloatDataVector dataVector)
    {
		if(!openLockRead()) return false;
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.readVector() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
			dataVector.setUnits(dis.readByte());
			int nValues = dis.readInt();
			dataVector.setMinValue(dis.readFloat());
			dataVector.setMaxValue(dis.readFloat());
			dataVector.setOrigin(dis.readDouble());
			dataVector.setSizeAndCapacity(nValues);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readVectorHeader" + "Unable to read vector from file " + filename, e);
			return false;
        }
		finally
		{
			closeUnlockRead();
		}
    }
	
	public int readSize(StsFloatDataVector floatVector)
    {
		if(!openRead()) return 0;
        if (dis==null) return 0;

        try
        {
			dis.readByte();
			return dis.readInt();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readSize(StsFloatDataVector" + "Unable to read vector from file " + filename, e);
			return 0;
        }
    }

	public int readSize(StsLongDataVector floatVector)
    {
		if(!openRead()) return 0;
        if (dis==null) return 0;

        try
        {
			return dis.readInt();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "readSize(StsFloatDataVector" + "Unable to read vector from file " + filename, e);
			return 0;
        }
    }
    /** set/get an float values in this binary file */
    public boolean setFloatValues(float[] vector, byte[] nullFlags, byte notNullFlag, float nullValue)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setFloatValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++)
            {
                if(nullFlags[i] == notNullFlag)
                    dos.writeFloat(vector[i]);
                else
                    dos.writeFloat(nullValue);
            }
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setFloatValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }
    public float[] getFloatValues()
    {
        try
        {
            int nValues = dis.readInt(); // get the size
//            System.out.println("StsBinaryFile.getFloatValues() nValues: " + nValues);
            float[] values = new float[nValues];
            for (int i = 0; i < nValues; i++) values[i] = dis.readFloat();
            return values;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getFloatValues", e);
            return null;
        }
    }

    public boolean getFloatVector(StsFloatDataVector floatVector, boolean loadValues)
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getFloatVector() failed."
                + "File " + filename + " not properly opened for reading");
			return false;
        }

        try
        {
            int size = dis.readInt(); // get the size
			floatVector.setMinValue(dis.readFloat());
			floatVector.setMaxValue(dis.readFloat());
			floatVector.setOrigin(dis.readDouble());
			if(loadValues)
			{
                float[] values = new float[size];
                for (int i=0; i<size; i++) values[i] = dis.readFloat();
                floatVector.checkSetValues(values);
			}
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getFloatVector() failed." +
                "Unable to read vector from file " + filename, e, StsException.WARNING);
			return false;
        }
    }
    public long[] getLongValues()
    {
        try
        {
            int nValues = dis.readInt(); // get the size
            long[] values = new long[nValues];
            for (int i = 0; i < nValues; i++) values[i] = dis.readLong();
            return values;
        }
        catch (Exception e)
        {
            StsException.outputException("getLongValues() failed.", e, StsException.WARNING);
            return null;
        }
    }

    public boolean getLongVector(StsLongDataVector longVector, boolean loadValues)
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getLongVector() failed."
                + "File " + filename + " not properly opened for reading");
            return false;
        }

        try
        {
            int size = dis.readInt(); // get the size
            longVector.setMinValue(dis.readLong());
            longVector.setMaxValue(dis.readLong());
            if(loadValues)
            {
                long[] values = new long[size];
                for (int i=0; i<size; i++)
                {
                    values[i] = dis.readLong();
                }
                longVector.setValues(values);
            }
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getLongVector() failed." +
                "Unable to read vector from file " + filename, e, StsException.WARNING);
            return false;
        }
    }
    /** set/get an float values in this binary file */
    public boolean setByteValues(byte[] vector, boolean incSize)
    {
        includeSizeInFile = incSize;
        return setByteValues(vector);
    }
    /** set/get a byte vector in this binary file */
    public boolean setByteValues(byte[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setByteValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            if(includeSizeInFile)
                dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeByte(vector[i]);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setByteValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

    public byte[] getByteValues(int size)
    {
        int i = 0;

        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getByteValues() failed."
                + ": file " + filename + " not properly opened for reading");
			return null;
        }

        byte[] vector = null;
        try
        {
            vector = new byte[size];
            for (i=0; i<size; i++) vector[i] = dis.readByte();
            return vector;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getByteValues() failed." +
                ": unable to read vector from file " + filename + ".\n" +
                i + " values read. Expected " + size,
                e, StsException.WARNING);
			return null;
        }
    }

    public byte[] getByteValues()
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getByteValues() failed."
                + ": file " + filename + " not properly opened for reading");
			return null;
        }

        byte[] vector = null;
        try
        {
            int size = dis.readInt();
            vector = new byte[size];
            for (int i=0; i<size; i++) vector[i] = dis.readByte();
            return vector;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getByteValues() failed." +
                ": unable to read vector from file " + filename, e, StsException.WARNING);
			return null;
        }
    }

    /** set/get an integer vector in this binary file */
    public boolean setIntegerValues(int[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setIntegerValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeInt(vector[i]);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setIntegerValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

    public int[] getIntegerValues()
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getIntegerValues() failed."
                + ": file " + filename + " not properly opened for reading");
			return null;
        }

        int[] vector = null;
        try
        {
            int size = dis.readInt();
            vector = new int[size];
            for (int i=0; i<size; i++) vector[i] = dis.readInt();
            return vector;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getIntegerValues() failed." +
                ": unable to read vector from file " + filename, e, StsException.WARNING);
			return null;
        }
    }

    /** set/get double values in this binary file */
    public boolean setDoubleValues(double[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setDoubleValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeDouble(vector[i]);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setDoubleValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

    public double[] getDoubleValues()
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getDoubleValues() failed."
                + ": file " + filename + " not properly opened for reading");
			return null;
        }

        double[] vector = null;
        try
        {
            int size = dis.readInt();
            vector = new double[size];
            for (int i=0; i<size; i++) vector[i] = dis.readDouble();
            return vector;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getDoubleValues() failed." +
                ": unable to read vector from file " + filename, e, StsException.WARNING);
			return null;
        }
    }

    /** set/get boolean values in this binary file */
    public boolean setBooleanValues(boolean[] vector)
    {
        if (dos==null)
        {
            StsException.systemError("StsBinaryFile.setBooleanValues() failed."
                + "File " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            dos.writeInt(vector.length);  // save the size
            for (int i=0; i<vector.length; i++) dos.writeBoolean(vector[i]);
			return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.setBooleanValues() failed." +
                ": unable to write vector to file " + filename, e, StsException.WARNING);
			return false;
        }
    }

    public boolean[] getBooleanValues()
    {
        if (dis==null)
        {
            StsException.systemError("StsBinaryFile.getBooleanValues() failed."
                + ": file " + filename + " not properly opened for reading");
			return null;
        }

        boolean[] vector = null;
        try
        {
            int size = dis.readInt();
            vector = new boolean[size];
            for (int i=0; i<size; i++) vector[i] = dis.readBoolean();
            return vector;
        }
        catch (Exception e)
        {
            StsException.outputException("StsBinaryFile.getBooleanValues() failed." +
                ": unable to read vector from file " + filename, e, StsException.WARNING);
			return null;
        }
    }

   /** set/get boolean values in this binary file */
    public boolean setByteValue(byte value)
    {
        if (dos==null)
        {
            StsException.systemError(this, "setByteValue",
                "file " + filename + " not properly opened for writing");
			return false;
        }

        try
        {
            dos.writeByte(value);  // save the size
			return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "setByteValue",
                "unable to read vector from file " + filename, e);
			return false;
        }
    }

    public byte getByteValue()
    {
        if (dis==null)
        {
            StsException.systemError(this, "getByteValue",
                "file " + filename + " not properly opened for reading");
			return -1;
        }
        try
        {
            return dis.readByte();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getByteValue",
                "unable to read vector from file " + filename, e);
			return -1;
        }
    }
}
