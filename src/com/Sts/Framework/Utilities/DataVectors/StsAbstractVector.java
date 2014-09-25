package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/18/11
 * Time: 7:00 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsAbstractVector extends StsObject implements Cloneable
{
	public String name = "";
	protected int size;
    protected byte units = StsParameters.DIST_NONE;
    protected int version = 0;
	protected byte monotonic = MONOTONIC_UNKNOWN;

	transient protected boolean isNull = true;
    transient protected int growInc = 100;
    transient protected int capacity;

	static public boolean reloadAscii = false;

    static public final byte MONOTONIC_UNKNOWN = 0;
    static public final byte MONOTONIC_NOT = 1;
    static public final byte MONOTONIC_INCR = 2;
    static public final byte MONOTONIC_DECR = 3;

	static public final int UNDEFINED = -1;

	public StsAbstractVector()
	{
	}

	public StsAbstractVector( boolean persistent)
	{
		super(persistent);
	}

	public boolean initialize(StsModel model) { return true; }

    /** Set the native units of the ASCII log vector */
	public void setUnits(byte units) { this.units = units; }

	/** Get the native units of the ASCII log vector */
	public byte getUnits() { return units; }

    /** how many values are in the vector? */
    public int getSize() { return size; }

    /** how much to grow array on append? */
    public void setGrowIncrement(int inc) { growInc = inc; }

    /** get monotonic value */
    public int getMonotonic() { return monotonic; }

    public boolean isMonotonicIncreasing() { return monotonic == MONOTONIC_INCR; }

    public void setMonotonic(byte monotonic)
    {
        this.monotonic = monotonic;
    }

	public boolean getIsNull()
	{
		return isNull;
	}

	/** Because on reload the name string for a vector has a handle different from the static vector name of the same name,
	 *  reinitialize name to correspond.
	 */
    protected void reinitializeName()
    {
		name = StsLoader.reinitializeName(name);
    }

    // Accessors
    public String getName(){ return name; }

    public void setVersion(int version) { this.version = version; }
    public int getVersion() { return version; }

    public void setName(String name) { this.name = name; }

    static public void setReloadAscii(boolean reload)
    {
        reloadAscii = reload;
    }

//    public void setLogCurveType(StsLogCurveType curveType) { this.logCurveType = curveType; }

	public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "clone", e);
            return null;
        }
    }

    static public StsAbstractVector getVectorWithName(StsAbstractVector[] abstractVectors, String name)
    {
        if(abstractVectors == null) return null;
        int nVectors = abstractVectors.length;
        for(int n = 0; n < nVectors; n++)
        {
        	if(abstractVectors[n] != null && abstractVectors[n].name.equals(name))
        		return abstractVectors[n];

        }
        return null;
    }

    static public StsAbstractVector getVectorWithName(StsObjectRefList abstractVectors, String name)
    {
        if(abstractVectors == null) return null;
        int nVectors = abstractVectors.getSize();
        for(Object vectorObject : abstractVectors)
        {
			StsAbstractVector vector = (StsAbstractVector)vectorObject;
        	if(vector != null && vector.name.equals(name))
        		return vector;

        }
        return null;
    }

    static public StsAbstractVector getVectorWithNameInList(StsObjectRefList abstractVectors, String[] names)
    {
        if(abstractVectors == null) return null;
        int nVectors = abstractVectors.getSize();
        for(Object vectorObject : abstractVectors)
        {
			StsAbstractVector vector = (StsAbstractVector)vectorObject;
			if(StsStringUtils.stringListHasString(names, vector.name))
        		return vector;
        }
        return null;
    }

    static public StsAbstractVector getVectorWithNameInList(StsAbstractVector[] abstractVectors, String[] names)
    {
        if(abstractVectors == null) return null;
        for(StsAbstractVector vector : abstractVectors)
        {
			if(StsStringUtils.stringListHasString(names, vector.name))
        		return vector;
        }
        return null;
    }

    static public StsAbstractVector getElement(StsObjectRefList abstractVectors, int i)
    {
        if(abstractVectors == null) return null;
		return (StsAbstractVector)abstractVectors.getElement(i);
    }

    /** append value to end of growable vector with optional min/max & monotonic calculation */
    public void append(double value, boolean offsetFromOrigin)
	{
		StsException.notImplemented(this, "append(double, boolean)");
	}

	public void appendOffset(float value)
	{
		StsException.notImplemented(this, "append(float, boolean)");
	}
	public boolean append(float value)
	{
		StsException.notImplemented(this, "append(float)");
		return false;
	}
    public boolean checkSetMinMax()
    {
		StsException.notImplemented(this, "checkSetMinMax");
		return false;
	}

    public void checkSetMinMax(float value)
    {
		StsException.notImplemented(this, "checkSetMinMax(float)");
    }

    public void checkSetMinMax(long value)
    {
		StsException.notImplemented(this, "checkSetMinMax(long)");
    }

	public void appendTokens(String[] tokens)
	{
		StsException.notImplemented(this, "appendTokens");
	}

	public void appendTokenTable(String[][] tokenTable, int nLines)
	{
		StsException.notImplemented(this, "tokenTable");
	}

	public boolean appendToken(String token)
	{
		StsException.notImplemented(this, "appendToken");
		return false;
	}

	public void append(long value, boolean offsetFromOrigin)
	{
		StsException.notImplemented(this, "append(long, boolean)");
	}

	public boolean append(long value)
	{
		StsException.notImplemented(this, "append(long)");
		return false;
	}

	public void trimToSize()
	{
		StsException.notImplemented(this, "trimToSize");
	}
	public boolean hasValues()
	{
		StsException.notImplemented(this, "\tpublic boolean hasValues()");
		return false;
	}

    static protected void closeBufRdr(BufferedReader bufRdr)
    {
        try
        {
            if(bufRdr == null) return;
            bufRdr.close();
        }
        catch(Exception e) { }
    }

    static public boolean readAsciiCurveValues(String filename, BufferedReader bufRdr, ArrayList<StsDataVectorFace> vectorList, boolean loadValues, int capacity, int growInc)
	{
		try
		{
		    String line;
			int nLines = 0;
			int badLineCount = 0;
			int nValuesRequired = vectorList.size();
            while ((line = bufRdr.readLine()) != null)
            {
                line = line.trim();
                if (line.equals("")) continue;  // blank line
                // line = StsStringUtils.deTabString(line);
				String[] tokens = StsStringUtils.getTokens(line);
                int nTokens = tokens.length;
				if(nTokens < nValuesRequired)
				{
					badLineCount++;
					if(badLineCount < 10)
					{
						StsMessageFiles.errorMessage("Skipping bad line. Needed " + nValuesRequired + " values but found only " + nTokens + " for line " + line);
						continue;
					}
					else
					{
						StsMessageFiles.errorMessage("Too may bad lines.  Giving up.");
						return false;
					}
				}
				for(StsDataVectorFace dataVector : vectorList)
					dataVector.appendTokens(tokens);
				if(++nLines%1000 == 0) StsMessageFiles.logMessage("File: " + filename + ": " + nLines + " vector read.");
            }
            // complete load of vectors: trim, min, max, null
            for(StsDataVectorFace dataVector : vectorList)
			{
               dataVector.trimToSize();
               dataVector.checkSetMinMax();
            }
            return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsAbstractVector.class, "readAsciiCurveValues", e);
            return false;
        }
    }

	public void setSizeAndCapacity(int size)
	{
		this.size = size;
		this.capacity = size;
	}

	static public boolean updateBinaryFile(StsDataVectorFace dataVector, byte[] valueBytes)
	{
		StsFile file = null;
		try
		{
			// if(getBinaryFilename() == null) return true;
			String binaryPathname = dataVector.getBinaryFilename();
			file = StsFile.constructor(binaryPathname);
			if(!file.exists() && !file.createNewFile()) return false;
			RandomAccessFile raf = new RandomAccessFile(file.getFile(), "rw");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] fileHeaderBytes = dataVector.getFileHeaderBytes();
			raf.write(fileHeaderBytes);
			long fileLength = (long)(fileHeaderBytes.length + dataVector.getNValueBytes());
			raf.seek(fileLength);
			raf.write(valueBytes);
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
			try { if(file != null) file.close(); } catch(Exception e) { }
		}
	}

	/** Given a monotonically increasing vector of values and a given value,
	 *  we compute an index and interpolation factor f (0.0 to 1.0 on the interval from index to index + 1 and assign
	 *  it to members of an IndexF.
	 *  This index and f can then be used to interpolate some other vector with the same sampling as the initial vector.
	 *  For example, we might have a timeVector from 0 to 100,000 with variable spacing.  We use a binary search to
	 *  compute index and f and assign those to members of IndexF.  If we have a vector of values at these given time values,
	 *  we can then return the interpolated value at the given time.
	 */
    public class IndexF
    {
        public int index;
        public float f;
		public float indexF;

        public IndexF(int index, float f)
        {
            this.index = index;
            this.f = f;
			this.indexF = index + f;
        }

        public IndexF(float indexF)
        {
			this.indexF = indexF;
			index = (int)indexF;
            f = indexF - index;
        }

		public float getInterpolatedValue(StsAbstractFloatVector floatVector)
		{
			return getInterpolatedValue(floatVector.getValues());
		}

		public float getInterpolatedValue(float[] values)
		{
			return values[index] + f*(values[index+1] - values[index]);
		}

		public float getInterpolatedValue(StsAbstractLongVector longVector)
		{
			return getInterpolatedValue(longVector.getValues());
		}

		public long getInterpolatedValue(long[] values)
		{
			return (long)(values[index] + f*(values[index+1] - values[index]));
		}
    }

    public String toString()
	{
		return name;
	}
}
