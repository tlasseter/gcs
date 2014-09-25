package com.Sts.Framework.Interfaces;

import com.Sts.Framework.Utilities.DataVectors.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 12/28/11
 */
public interface StsTimeSeriesDisplayable
{
	abstract StsTimeVectorSet getTimeVectorSet();
	abstract public int getNValues();
	abstract public StsAbstractFloatVector[] getValueVectors();
	abstract public int getNumIntervalPoints(long intervalSeconds, long endTime);
	public int getTimeSeriesDuration();
}
