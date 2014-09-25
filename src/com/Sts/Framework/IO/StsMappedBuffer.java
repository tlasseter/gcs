package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

abstract public class StsMappedBuffer
{
	public RandomAccessFile randomAccessFile = null;
	public MappedByteBuffer byteBuffer = null;
	public FileChannel channel;
	public FileChannel.MapMode mapMode = FileChannel.MapMode.READ_ONLY;
    public String pathname;
    public long position; // position in file in bytes

    static public final String READ = "r";
    static public final String WRITE = "w";
    static public final String READ_WRITE = "rw";

    abstract public void rewind();
	abstract public void clear0();
	abstract public boolean map(long position, long nSamples);
	abstract public void get(float[] floats);
	abstract public float getFloat();
	abstract public void position(int position);
    abstract public long getBufferPosition();
	abstract public void get(double[] doubles);
	abstract public double getDouble();
    abstract public long getCapacity();

	public StsMappedBuffer(String directory, String filename, String mode) throws FileNotFoundException
	{
		this(directory + filename, mode);
	}

	public StsMappedBuffer(String pathname, String mode) throws FileNotFoundException
	{
		try
		{
			randomAccessFile = new RandomAccessFile(pathname, mode);
			if (!mode.equals(READ)) mapMode = FileChannel.MapMode.READ_WRITE;
			channel = randomAccessFile.getChannel();
		}
		catch(IllegalArgumentException e)
		{
			StsException.systemError("StsMappedBuffer.constructor() failed. Illegal mode: " + mode + " must be \"r\" , \"w\" or \"rw\" ");
		}
	}

	public StsMappedBuffer(Path path, String mode) throws FileNotFoundException
	{
		try
		{
			randomAccessFile = new RandomAccessFile(path.toFile(), mode);
			if (!mode.equals(READ)) mapMode = FileChannel.MapMode.READ_WRITE;
			channel = randomAccessFile.getChannel();
		}
		catch(IllegalArgumentException e)
		{
			StsException.systemError("StsMappedBuffer.constructor() failed. Illegal mode: " + mode + " must be \"r\" , \"w\" or \"rw\" ");
		}
	}

	public StsMappedBuffer(RandomAccessFile randomAccessFile, String mode) throws FileNotFoundException
	{
		try
		{
			this.randomAccessFile = randomAccessFile;
			if (!mode.equals(READ)) mapMode = FileChannel.MapMode.READ_WRITE;
			channel = randomAccessFile.getChannel();
		}
		catch(IllegalArgumentException e)
		{
			StsException.systemError("StsMappedBuffer.constructor() failed. Illegal mode: " + mode + " must be \"r\" , \"w\" or \"rw\" ");
		}
	}

	public void clean()
	{
		StsToolkit.clean(byteBuffer);
	}

    public void clear()
	{
		try
		{
            if(byteBuffer == null) return;
            byteBuffer.force();
            StsToolkit.clean(byteBuffer);
            channel.force(false);
		}
		catch(Exception e)
		{
		}
	}
    public void clearDebug(String message, StsTimer timer)
	{
		try
		{
            timer.start();
            byteBuffer.force();
            timer.stopPrint(message + " byteBuffer.force()");

            timer.start();
            StsToolkit.clean(byteBuffer);
            timer.stopPrint(message + " clean(byteBuffer)");

            timer.start();
            channel.force(false);
            timer.stopPrint(message + " channel.force()");
        }
		catch(Exception e)
		{
		}
	}

    public void close()
	{
		try
		{
            clear();
			channel.close();
			randomAccessFile.close();
			randomAccessFile = null;
//			clear0();
		}
		catch(Exception e)
		{
		}
	}

	public boolean getFileBytes(byte[] bytes, int position)
	{
		return getFileBytes(bytes, position, bytes.length);
	}

	public boolean getFileBytes(byte[] bytes, int position, int nBytes)
	{
		try
		{
			randomAccessFile.seek(position);
			int nBytesRead = randomAccessFile.read(bytes, 0, nBytes);
			if(nBytesRead < nBytes)
			{
				StsException.systemError(this, "getFileBytes", "Failed to read " + nBytes + " bytes into byte array of length " + bytes.length +
						" at position " + position + ". file length: " + randomAccessFile.length());
				return false;
			}
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getFileBytes", e);
			return false;
		}
	}

    public void force()
    {
        if(byteBuffer != null) byteBuffer.force();
    }

    public long getFilePosition() { return position; }

    protected boolean checkPosition(int nSamples, long blockSize)
    {
        long currentBufferPosition = getBufferPosition();
        long capacity = getCapacity();
        if(currentBufferPosition + nSamples > capacity)
        {
            long currentFilePosition = getFilePosition();
            clear();
            map(currentFilePosition, blockSize);
        }
        return true;
    }

    protected boolean checkPositionDebug(int nSamples, long blockSize, String debugString)
    {
        long currentBufferPosition = getBufferPosition();
        long currentFilePosition = getFilePosition();
        long capacity = getCapacity();
        if(currentBufferPosition + nSamples > capacity)
        {
            clear();
            map(currentFilePosition, blockSize);
        }
        System.out.println(debugString + "  file position: " + currentFilePosition + " nSamples: " + nSamples);
        return true;
    }
}
