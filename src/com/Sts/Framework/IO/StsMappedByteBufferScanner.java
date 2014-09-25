package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/30/11
 */
public class StsMappedByteBufferScanner
{
	/** file */
	public StsAbstractFile file;
	/** file buffer */
	StsMappedByteBuffer byteBuffer;
	/** read and write lock */
	// ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	/** size of file */
	long size;
	/** line bounding search start point; bytes converted to String (does not include the EOL) */
	Line line;
	/** line before line bounding startPoint (line); bytes converted to String (does not include the EOL) */
	Line prevLine;
	/** line before line bounding startPoint (line); bytes converted to String (does not include the EOL) */
	Line nextLine;

	static int nLineSeparatorBytes = StsMappedByteBuffer.nLineSeparatorBytes;

	static final boolean debug = true;
	static final boolean debugBytes = true;
	static byte[] allBytes;

	private StsMappedByteBufferScanner(String pathname)
	{
		//this.pathname = pathname;
		file = StsFile.constructor(pathname);
		byteBuffer = StsMappedByteBuffer.constructor(pathname, "rw");
	}

	public boolean mapBuffer() throws IOException
	{
		size = byteBuffer.randomAccessFile.length();
		return byteBuffer.map(0, size);
	}

	static public StsMappedByteBufferScanner constructor(String pathname)
	{
		try
		{
			return new StsMappedByteBufferScanner(pathname);
		}
		catch(Exception e)
		{
			StsException.systemError(StsMappedByteBufferScanner.class, "constructor", "Failed to construct scanner for " + pathname);
			return null;
		}
	}

	/** create and fill a buffer centered at the start index.  Then search backwards for the previous EOL and then
	 *  forward for the next EOL.  This defines the line which bounds the start point.  If the file layout hasn't changed,
	 *  the start Index should be at the end of an EOL (one or two bytes; two bytes on windows).  So we should only have to
	 *  search backwards for the previous byte from the start (or the start itself should be the EOL if the EOL is a single byte.
	 *  Note that the lineStart and the number of lineBytes is saved to be used by the next line search call.
	 * @param searchStart index where the search will start.
	 * @return the string which defines the line bounding the start index.
	 */
	public Line scanForLine(int searchStart)
	{
		int halfBufferSize = 200;
		int bufferSize = 2*halfBufferSize + 1;
		int fileBufferStart = Math.max(0, searchStart - halfBufferSize);
		bufferSize = (int)this.size - fileBufferStart;
		int fileBufferEnd = fileBufferStart + bufferSize -1;
		int bufferSearchStart = searchStart - fileBufferStart;
		byte[] bytes = new byte[bufferSize];
		String lineString = null;
		int bufferLineStart = -1;
		int bufferLineEnd = -1;
		int nLineBytes;

		try
		{
			if(debugBytes)
			{
				//byteBuffer.position(0);
				allBytes = new byte[(int)size];
				getBufferBytes(allBytes, 0);
			}

			byteBuffer.getBytesAtPosition(bytes, fileBufferStart, bufferSize);
			int prev;
			// find previous EOL
			int nLinesBackScanned = 0;
			for(prev = bufferSearchStart; prev > 0; prev--)
			{
				if(StsMappedByteBuffer.isEOL(bytes, prev-1, bufferSize))
				{
					bufferLineStart = prev-1 + nLineSeparatorBytes;
					nLinesBackScanned++;
					break;
				}
			}
			if(bufferLineStart == -1) return null;
			// find next EOL
			int nLinesForwardScanned = 0;
			for(int next = bufferSearchStart; next < bufferSize; next++)
			{
				if(StsMappedByteBuffer.isEOL(bytes, next, bufferSize))
				{
					bufferLineEnd = next - 1;
					nLinesForwardScanned++;
					break;
				}
			}
			if(debug) StsException.systemDebug(this, "scanForLine", "Scanned back " + nLinesBackScanned + " and forward " + nLinesForwardScanned);
			if(bufferLineEnd == -1) return null;
			nLineBytes = bufferLineEnd - bufferLineStart + 1;
			byte[] lineBytes = new byte[nLineBytes];
			System.arraycopy(bytes, bufferLineStart, lineBytes, 0, nLineBytes);
			lineString = new String(lineBytes);
			int fileLineStart = fileBufferStart+bufferLineStart;
			int fileLineEnd = fileBufferStart+bufferLineEnd;
			line = new Line(fileLineStart, fileLineEnd, lineString, searchStart);
			return line;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "scanForPreviousLine", "Failed for file: " + getPathname(), e);
			return null;
		}
	}

	/** file pathname */
	public String getPathname()
	{
		return file.getPathname();
	}

	public class Line
	{
		/** starting byte for this line in the file */
		public int fileLineStart;
		/** ending byte for this line in the file */
		public int fileLineEnd;
		/** line without the EOL bytes */
		public String lineString;
		/** this indicates line start is exactly at end of previous file byte position
		 *  meaning that this line starts the new lines
		 */
		public boolean startsNewLines = false;

		public Line(int fileLineStart, int fileLineEnd, String lineString, int searchStart)
		{
			this.fileLineStart = fileLineStart;
			this.fileLineEnd = fileLineEnd;
			this.lineString = lineString;
			startsNewLines = (searchStart == fileLineStart);
		}
	}

	public void close()
	{
		if(byteBuffer != null) byteBuffer.close();
	}

	public Line scanForPrevLine(Line line)
	{
		int fileLineEnd = line.fileLineStart - nLineSeparatorBytes - 1;
		int searchStart = fileLineEnd;
		int bufferSize = 400;
		int fileBufferEnd = fileLineEnd;
		int bufferEnd = Math.max(0, fileBufferEnd - bufferSize - 1);
		bufferSize = Math.min(fileBufferEnd + 1, bufferSize);
		int bufferStart = bufferEnd - bufferSize + 1;
		int fileBufferStart = fileBufferEnd - bufferSize + 1;
		int bufferSearchStart = searchStart - fileBufferStart;
		int bufferLineEnd = fileLineEnd - fileBufferStart;
		byte[] bytes = new byte[bufferSize];
		String lineString = null;
		int bufferLineStart = -1;
		int nLineBytes;

		try
		{
			if(debugBytes)
			{
				//byteBuffer.position(0);
				allBytes = new byte[(int)size];
				getBufferBytes(allBytes, 0);
			}
			byteBuffer.getBytesAtPosition(bytes, bufferStart, bufferSize);
			int prev;
			// find previous EOL
			int nLinesScanned = 0;
			for(prev = bufferSearchStart; prev > 0; prev--)
			{
				if(StsMappedByteBuffer.isEOL(bytes, prev-1, bufferSize))
				{
					bufferLineStart = prev-1 + nLineSeparatorBytes;
					nLinesScanned++;
					break;
				}
			}
			if(debug) StsException.systemDebug(this, "scanForPrevLine", "Scanned back " + nLinesScanned);
			if(bufferLineStart == -1) return null;
			nLineBytes = fileLineEnd - bufferLineStart + 1;
			byte[] lineBytes = new byte[nLineBytes];
			System.arraycopy(bytes, bufferLineStart, lineBytes, 0, nLineBytes);
			lineString = new String(lineBytes);
			int fileLineStart = fileBufferStart+bufferLineStart;
			prevLine = new Line(fileLineStart, fileLineEnd, lineString, searchStart);
			return prevLine;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "scanForPreviousLine", "Failed for file: " + getPathname(), e);
			return null;
		}
	}

	public Line scanForNextLine(Line line)
	{
		int fileLineStart = line.fileLineEnd + nLineSeparatorBytes + 1;
		if(fileLineStart >= size) return null;

		int fileLineEnd = -1;

		try
		{
			if(debugBytes)
			{
				//byteBuffer.position(0);
				int bufferSize = (int)size;
				allBytes = new byte[bufferSize];
				getBufferBytes(allBytes, 0);
			}

			//byteBuffer.position(fileLineStart);
			int nBytes = (int)(size - fileLineStart);
			byte[] bytes = getBufferBytes(nBytes, fileLineStart);
			//byte[] bytes = new byte[nBytes];
			//getBufferBytes(bytes, fileLineStart);
			int nLinesScanned = 0;
			if(nLineSeparatorBytes == 1)
			{
				for(int index = fileLineStart, n = 0; index < size; index++, n++)
				{
					byte b = bytes[n];
					//byte b = byteBuffer.get();
					if(StsMappedByteBuffer.isEOL(b))
					{
						fileLineEnd = index - 1;
						nLinesScanned++;
						break;
					}
				}
			}
			else // nLineSeparatorBytes == 2
			{
				byte b2 = bytes[0];
				//byte b2 = byteBuffer.get();
				for(int index = fileLineStart + 1, n = 1; index < size; index++, n++)
				{
					byte b1 = b2;
					b2 = bytes[n];
					//b2 = byteBuffer.get();
					if(StsMappedByteBuffer.isEOL(b1, b2))
					{
						fileLineEnd = index - nLineSeparatorBytes;
						nLinesScanned++;
						break;
					}
				}
			}
			if(debug) StsException.systemDebug(this, "scanForNextLine", "Scanned forward " + nLinesScanned);
			if(fileLineEnd == -1)
			{
				StsException.systemError(this, "scanForNextLine", "Failed to find EOL");
				return null;
			}
			int nLineBytes = fileLineEnd - fileLineStart + 1;
			//byte[] lineBytes = new byte[nLineBytes];
			//byteBuffer.clear();
			//byteBuffer.position(fileLineStart);
			//byteBuffer.get(lineBytes);
			//getBufferBytes(lineBytes, fileLineStart);
			byte[] lineBytes = getBufferBytes(nLineBytes, fileLineStart);
			String lineString = new String(lineBytes);
			nextLine = new Line(fileLineStart, fileLineEnd, lineString, fileLineStart);
			return nextLine;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "scanForNextLine", "Failed for file: " + getPathname(), e);
			return null;
		}
		finally
		{
			if(byteBuffer != null)
			byteBuffer.clear();
		}
	}

	private byte[] getBufferBytes(int nBytes, int position)
	{
		try
		{
			byte[] bytes = new byte[nBytes];
			byteBuffer.getFileBytes(bytes, position);
			return bytes;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getBufferBytes", e);
			return new byte[0];
		}
	}

	private boolean getBufferBytes(byte[] bytes, int position)
	{
		try
		{
		return byteBuffer.getFileBytes(bytes, position);
		/* these versions fails Java byteBuffer problem

		   version 1: bulk byte get
		byteBuffer.position(position);
		byteBuffer.get(bytes);

		   version 2: single byte get(s)
		byteBuffer.position(position);
		for(int n = 0; n < bytes.length; n++)
			bytes[n] = byteBuffer.get();

		return true;
		*/
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "getBufferBytes", e);
			return false;
		}
	}

	public ArrayList<Line> getNewLines(Line line)
	{
		ArrayList<Line> newLines = new ArrayList<Line>();
		while(line != null)
		{
			newLines.add(line);
			line = scanForNextLine(line);
		}
		return newLines;
	}
/*
	public Iterator<Line> getLineIterator(Line line) { return new LineIterator(line); }

	class LineIterator implements Iterator<Line>
	{
		Line line;

		public LineIterator(Line line)
		{
			this.line = line;
		}

		public boolean hasNext() { return line != null; }

		public Line next()
		{
			Line nextLine = line;
			line = scanForNextLine(line);
			return nextLine;
		}

		public void remove() { }
	}
*/
	static public void main(String[] args)
	{
		String pathname = args[0];

		try
		{
			StsMappedByteBufferScanner byteBufferScanner = new StsMappedByteBufferScanner(pathname);
			int guess = 1000;
			Line line = byteBufferScanner.scanForLine(guess);
			System.out.print("Line[" + line.fileLineStart + "-" + line.fileLineEnd + "]" + line.lineString);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsMappedCharBuffer.class, "main", e);
		}
	}
}
