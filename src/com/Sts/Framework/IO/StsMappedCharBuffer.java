package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.nio.*;


public class StsMappedCharBuffer extends StsMappedBuffer
{
	public CharBuffer charBuffer = null;

	static final char[] EOL = System.lineSeparator().toCharArray();
	static final int nEOLchars = EOL.length;

	private StsMappedCharBuffer(String directory, String filename, String mode) throws FileNotFoundException
	{
		super(directory, filename, mode);
	}

	private StsMappedCharBuffer(String pathname, String mode) throws FileNotFoundException
	{
		super(pathname, mode);
	}

	static public StsMappedCharBuffer constructor(String directory, String filename, String mode) throws FileNotFoundException
	{
		return new StsMappedCharBuffer(directory, filename, mode);
	}

	static public StsMappedCharBuffer constructor(String pathname, String mode) throws FileNotFoundException
	{
		return new StsMappedCharBuffer(pathname, mode);
	}

	public boolean map(long position, long nSamples)
	{
		try
		{
			this.position = position;
			byteBuffer = channel.map(mapMode, position, nSamples);
			charBuffer = byteBuffer.asCharBuffer();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsMappedDoubleBuffer.map() failed for position: " + position + " nSamples: " + nSamples,
										 e, StsException.WARNING);
			return false;
		}
	}

    public long getBufferPosition()
    {
        if(charBuffer == null) return 0;
        return charBuffer.position();
    }

    final public long getCapacity()
	{
        if(charBuffer == null) return 0;
        return charBuffer.capacity();
	}
	public void put(char[] chars, int offset, int length)
	{
		charBuffer.put(chars, offset, length);
	}
	public void put(char d)
	{
		charBuffer.put(d);
	}

	final public void get(char[] chars)
	{
		charBuffer.get(chars);
	}

	final public char getChar()
	{
		return charBuffer.get();
	}

    /** if the mappedBuffer capacity is exceeded or doesn't exist, it is cleared and remapped so writing can continue */
    public void checkPut(char[] chars, long blockSize)
	{
		try
		{
            int nChars = chars.length;
            checkPosition(nChars, blockSize);
            charBuffer.put(chars);
		}
		catch(Exception e)
		{
			StsException.systemError(this, "put(chars[])", "Position: " + charBuffer.position() + " buffer remaining: " + charBuffer.remaining() + " array length " + chars.length);
		}
	}

    /** if the mappedBuffer capacity is exceeded or doesn't exist, it is cleared and remapped so reading can continue */
    final public void checkGet(char[] chars, long blockSize)
	{
        int nChars = chars.length;
        checkPosition(nChars, blockSize);
        charBuffer.get(chars);
	}

	final public void position(int position)
	{
		charBuffer.position(position);
	}

	final public void rewind() { charBuffer.rewind(); }

	public void clear0()
	{
		if(charBuffer != null) charBuffer.clear();
	}

	public char[] readCharData(String pathname, int nChars)
    {
        try
        {
            StsMappedCharBuffer charBuffer = StsMappedCharBuffer.constructor(pathname, "r");
            if(!charBuffer.map(0, nChars)) return null;
            char[] chars = new char[nChars];
            charBuffer.get(chars);
            charBuffer.close();
            return chars;
        }
        catch(Exception e)
        {
            StsException.systemError("StsSeismicLine2d.readFloatData() failed to find file " + pathname);
            return null;
        }
    }

	final public void get(float[] floats) { }
	final public float getFloat() { return StsParameters.nullValue; }
	final public void get(double[] doubles) { }
	final public double getDouble() { return StsParameters.nullDoubleValue; }

	static public void main(String[] args)
	{
		String pathname = args[0];
		try
		{
			String EOL = System.lineSeparator();
			char[] EOLchars = EOL.toCharArray();
			StsMappedCharBuffer charBuffer = StsMappedCharBuffer.constructor(pathname, "rw");
			long length = charBuffer.randomAccessFile.length();
			if(!charBuffer.map(0, length)) return;
			int n = 0;
			StringBuffer lineBuffer = new StringBuffer(1000);
			while(true)
			{
				try
				{
					char c = charBuffer.getChar();
					System.out.println("char " + c);
					lineBuffer.append(charBuffer.getChar());
					n++;
					if(isEOL(lineBuffer, n))
					{
						System.out.println("Line " + n + lineBuffer.toString());
						lineBuffer.setLength(0);
					}
				}
				catch(Exception e)
				{
					System.out.println("EOL  " + n + lineBuffer.toString());
				}
			}

		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsMappedCharBuffer.class, "main", e);
		}
	}

	static private boolean isEOL(StringBuffer lineBuffer, int lastIndex)
	{
		if(nEOLchars == 1)
			return lineBuffer.charAt(lastIndex) == EOL[0];
		else if(lastIndex == 0)
			return false;
		else
			return lineBuffer.charAt(lastIndex - 1) == EOL[0] && lineBuffer.charAt(lastIndex) == EOL[1];

	}
}
