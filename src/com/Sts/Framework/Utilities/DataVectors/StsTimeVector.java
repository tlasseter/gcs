
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Utilities.DateTime.*;
import com.Sts.Framework.Utilities.*;


public class StsTimeVector extends StsLongDataVector implements Cloneable
{
    protected int fileColumnIndexDate = -1;
    // protected int timeShift = 0;
	private int dateOrder = CalendarParser.DD_MM_YY;
	// public byte timeType = StsSensorKeywordIO.NO_TIME;
	transient public long currentTime = 0L;
    // public long startTime = 0L;
	transient public int currentTimeIndex;
	/** name of the column or columns defining the clock time */
	transient String inputName;

	public static final byte NO_TIME = -1;
	public static final byte TIME_AND_DATE = 0;
	public static final byte TIME_ONLY = 1;
	public static final byte ELAPSED_TIME = 2;
	public static final byte TIME_OR_DATE = 3;

	public StsTimeVector()
   {
   }

	public StsTimeVector(StsTimeVectorSet vectorSet, StsColumnName columnName, int dateOrder)
	{
		super(false);
		this.vectorSet = vectorSet;
		name = columnName.name;
		inputName = name;
		fileColumnIndex = columnName.fileColumnIndex;
		this.dateOrder = dateOrder;
	}

	public StsTimeVector(StsTimeVectorSet vectorSet, StsDualColumnName columnName, int dateOrder)
	{
		super(false);
		this.vectorSet = vectorSet;
		inputName = columnName.inputName;
		name = columnName.name;
		fileColumnIndex = columnName.columnName1.fileColumnIndex;
		fileColumnIndexDate = columnName.columnName2.fileColumnIndex;
		this.dateOrder = dateOrder;
	}

	public StsTimeVector(long[] values)
	{
		super(values);
	}

	public StsTimeVector(String asciiDir, String binaryDir, String group, String name, String subname, int version)
	{
	}

	public boolean initialize(StsModel model)
	{
//		size = readSize();
		initializeTimeIndex();
		return true;
	}

	public void initializeTimeIndex()
	{
		currentTimeIndex = getSize() - 1;
		currentTime = maxValue;
	}

	public int getCurrentTimeIndex()
	{
		return currentTimeIndex;
	}

	public boolean checkSetProjectTime(long time)
    {
		checkLoadVector();
		if(time == -1 || time > currentTime)
		{
			if(currentTimeIndex >= size-1)
			{
				currentTimeIndex = size-1;
				return false;
			}

			if(time < values[currentTimeIndex + 1]) return false;

			currentTime = time;
			if(currentTimeIndex + 2 < size && time <= values[currentTimeIndex + 2])
			{
				currentTimeIndex += 1;
				return true;
			}
			//TODO need to write an indexBinarySearch which assumes values are roughly evenly-spaced
			currentTimeIndex = StsMath.binarySearch(values, currentTime);
			return true;
		}
		else if(time < currentTime)
		{
			if(currentTimeIndex <= 0) return false;

			currentTime = time;
			if(currentTimeIndex - 1 >= size && time >= values[currentTimeIndex -1])
			{
				currentTimeIndex -= 1;
				return true;
			}
			currentTimeIndex = StsMath.binarySearch(values, currentTime);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setTimeDateColumns(int timeIndex, int dateIndex)
	{
		fileColumnIndex = timeIndex;
		fileColumnIndexDate = dateIndex;
	}
    /** Set the native units of the ASCII log vector */
// timeShift is replaced by originOffset (a long in StsAbstractLongDataVector)
/*
	public void setTimeShift(int shift)
    {
        if(shift == timeShift) return;
        timeShift = shift;
        clearArray();
        checkLoadVector();
    }
    public int getTimeShift() { return timeShift; }
*/
	public int getAsciiFileTimeColumn() { return fileColumnIndex; }
	public int getAsciiFileDateColumn() { return fileColumnIndexDate; }

	public long computeValue(String[] tokens)
	{
		long value = -1L;

		try
		{
			if(inputName == StsLoader.LONG_TIME)
				value = Long.parseLong(tokens[fileColumnIndex]);
			else if(inputName == StsLoader.TIME_DATE)
				value = CalendarParser.parse(tokens[fileColumnIndex], dateOrder).getTimeInMillis();
			else if(inputName == StsLoader.DUAL_TIME_DATE)
			{
				String dateAndTime = tokens[fileColumnIndexDate] + " " + tokens[fileColumnIndex];
				value = CalendarParser.parse(dateAndTime, dateOrder).getTimeInMillis();
			}
			return value;
		}
		catch(Exception e)
		{
			return StsParameters.nullLongValue;
		}
	}

	public void appendTokens(String[] tokens)
	{
		append(computeValue(tokens));
	}

	public void appendTokenTable(String[][] tokenTable, int nTokenLines)
	{
		long[] values = new long[nTokenLines];
		for(int n = 0; n < nTokenLines; n++)
			values[n] = computeValue(tokenTable[n]);
		append(values);
		byte[] valueBytes = StsMath.longsToBytes(values);
		updateBinaryFile(this, valueBytes);
	}

	public long computeValue(String line)
	{
		String[] tokens = StsStringUtils.getTokens(line);
		return computeValue(tokens);
	}

	private String getTimeTokens(String[] tokens)
	{
		if(inputName == StsLoader.LONG_TIME)
			return "LONG_TIME: " + tokens[fileColumnIndex];
		else if(inputName == StsLoader.TIME_DATE)
				return "TIME_DATE: " + tokens[fileColumnIndex];
		else if(inputName == StsLoader.DUAL_TIME_DATE)
				return "TIME: " + tokens[fileColumnIndex] + " DATE: " + tokens[fileColumnIndexDate];
		else
				return "NONE";
	}
}

































