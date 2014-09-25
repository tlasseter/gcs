package com.Sts.Framework.Utilities.DataVectors;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 1/4/12
 */
public class StsLogSurveyVectorSet extends StsLogVectorSet
{
	private StsLongDataVector surveyTimesVector;

	public StsLongDataVector getSurveyTimesVector()
	{
		return surveyTimesVector;
	}

	public void setSurveyTimesVector(StsLongDataVector surveyTimesVector)
	{
		this.surveyTimesVector = surveyTimesVector;
	}
}
