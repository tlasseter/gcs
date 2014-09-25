package com.Sts.Framework.DBTypes.VectorSetObjects;

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.Utilities.DataVectors.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 12/20/11
 */

/** This class adds TimeSeriesDisplayable capability to StsVectorSetObject.
 * The vectorSet itself must be an instance of StsTimeVectorSet or subclass. */
abstract public class StsTimeVectorSetObject extends StsVectorSetObject implements StsTimeSeriesDisplayable
{
	/** When plotted in timeSeries, this is the yAxisType (linear, log10, or logE) */
	protected byte yAxisType = LINEAR;
	/** The maximum time duration (seconds) of real-time time series plots of this sensor */
	protected int timeSeriesDuration = 18000;  // default is 30 minutes

	static public final byte LINEAR = 0;
	static public final byte LOG10 = 1;
	static public final byte LOGE = 2;
	static public final String[] AXIS_STRINGS = new String[] { "Linear", "Log10", "LogE"};
	static public final byte[] AXIS_TYPES = new byte[] {LINEAR, LOG10, LOGE};

	StsTimeVectorSetObject()
	{
	}

	StsTimeVectorSetObject(boolean persistent, String name)
	{
		super(persistent, name);
	}

	StsTimeVectorSetObject(boolean persistent)
	{
		super(persistent);
	}

	public boolean addVectorSetToObject(StsVectorSet vectorSet)
	{
		StsTimeVectorSet timeVectorSet = (StsTimeVectorSet)vectorSet;
		timeVectorSet.addToModel();
		setTimeVectorSet(timeVectorSet);
		return timeVectorSet.checkAddVectors(true);
	}

	public void setTimeVectorSet(StsTimeVectorSet timeVectorSet)
	{
		super.setVectorSet(timeVectorSet);
		StsTimeVector clockTimeVector = getClockTimeVector();
		if(clockTimeVector == null) return;
		setBornDate(clockTimeVector.getFirst());
		setCompleteDate(clockTimeVector.getLast());
	}

	public StsTimeVector getClockTimeVector()
	{
		return getTimeVectorSet().getClockTimeVector();
	}

	/** vector set containing valueVectors and clockTimeVector */
	public StsTimeVectorSet getTimeVectorSet()
	{
		return (StsTimeVectorSet)vectorSet;
	}

	/**
	 * Get the number of points within the latest interval
	 * @param seconds - number of seconds to compute number of points.
	 * @return number of points within the last (interval) seconds.
	 */
	public int getNumIntervalPoints(long seconds, long endTime)
	{
		long startTime = endTime - (seconds * 1000L);
		StsTimeVector timeVector = getClockTimeVector();
		int minIndex = timeVector.getIndex(startTime);
		int maxIndex = timeVector.getIndex(endTime);
		return maxIndex - minIndex + 1;
	};

	//public String getAreaComputeTypeString()
	//{
	//    return Chart2D.timeEpochStrings[areaComputationMethod];
	//}
	public int getTimeSeriesDuration() { return timeSeriesDuration; }

	public void setTimeSeriesDuration(int numPts)
	{
		timeSeriesDuration = numPts;
		dbFieldChanged("timeSeriesDuration", timeSeriesDuration);
		currentModel.viewObjectRepaint(this, this);
	}

	/** Set the born and death dates based on the time vector. */
	 public void setBornDeathWithVector()
	 {
		 setBornDate(getTimeMin());
		 setDeathDate(getTimeMax());
	 }

	/**
	* Get time minimum.
	* @return time minimum
	*/
   public long getTimeMin() { return getClockTimeVector().getMinValue(); }

	public long getTimeMax()
	{
		StsTimeVector timeVector = getTimeVectorSet().getClockTimeVector();
		if(timeVector == null) return 0L;
		return timeVector.getMaxValue();
	}

	public StsTimeVector getTimeVector() { return getTimeVectorSet().getClockTimeVector(); }

	public long getTimeShift()
	{
		StsTimeVector timeVector = getClockTimeVector();
		if(timeVector != null)
			return timeVector.getOriginOffset()/60000; // Convert user specified minutes to milli-seconds
		return 0;
	}
}
