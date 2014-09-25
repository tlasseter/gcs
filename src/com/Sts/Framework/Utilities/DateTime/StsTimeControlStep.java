package com.Sts.Framework.Utilities.DateTime;

import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/23/11
 */
public class StsTimeControlStep implements Cloneable
{
	public String name;
	public int calendarFieldNumber;
	public String shortName;
	public long msecPerStep;
	public int multiplier = 1;

	static public final StsTimeControlStep secondStep = new StsTimeControlStep("second", "s", Calendar.SECOND, 1);
	static public final StsTimeControlStep minuteStep = new StsTimeControlStep("minute", "m", Calendar.MINUTE, 60);
	static public final StsTimeControlStep hourStep = new StsTimeControlStep("hour", "h", Calendar.HOUR, 3600);
	static public final StsTimeControlStep dayStep = new StsTimeControlStep("day", "d", Calendar.DAY_OF_MONTH, StsParameters.secondsPerDay);
	static public final StsTimeControlStep yearStep = new StsTimeControlStep("year", "y", Calendar.YEAR, StsParameters.secondsPerYear);

	static public final Calendar calendar = Calendar.getInstance();

	public StsTimeControlStep() { }

	public StsTimeControlStep(String name, String shortName, int calendarFieldNumber, long secPerStep)
	{
		this.name = name;
		this.shortName = shortName;
		this.calendarFieldNumber = calendarFieldNumber;
		this.msecPerStep = 1000*secPerStep;
	}

	public StsTimeControlStep(StsTimeControlStep incrementalStep, String compositeShortName, int multiplier)
	{
		if(!StsToolkit.copy(incrementalStep, this))
		{
			StsException.systemError(this, "constructor", "Failed to construct multiplier step from " + incrementalStep.name + " multiplier: " + multiplier);
		}
		name = multiplier + " " + name;
		shortName = compositeShortName;
		msecPerStep = multiplier*msecPerStep;
		this.multiplier = multiplier;
	}

	/** create a series of time control steps based on short names, where shortnames are multiplier followed by standard shortname. */
	static public StsTimeControlStep[] constructTimeControlSteps(String[] shortNames)
	{
		int nSteps = shortNames.length;
		ArrayList<StsTimeControlStep> steps = new ArrayList<StsTimeControlStep>();
		int n = 0;
		for(String compositeShortName : shortNames)
		{
			int length = compositeShortName.length() - 1;
			String shortName = compositeShortName.substring(length);
			String multiplierString = compositeShortName.substring(0, length);
			multiplierString = StsStringUtils.removeLeadingTrailingSpaces(multiplierString);
			int multiplier = Integer.parseInt(multiplierString);
			if(shortName.equals("s"))
				steps.add(new StsTimeControlStep(secondStep, compositeShortName, multiplier));
			else if(shortName.equals("m"))
				steps.add(new StsTimeControlStep(minuteStep, compositeShortName, multiplier));
			else if(shortName.equals("h"))
				steps.add(new StsTimeControlStep(hourStep, compositeShortName, multiplier));
			else if(shortName.equals("d"))
				steps.add(new StsTimeControlStep(dayStep, compositeShortName, multiplier));
			else if(shortName.equals("y"))
				steps.add(new StsTimeControlStep(yearStep, compositeShortName, multiplier));
			else
			{
				StsException.systemError(StsTimeControlStep.class, "constructTimeControlSteps", "Failed to construct timeStep from " + compositeShortName);
				continue;
			}
		}
		return steps.toArray(new StsTimeControlStep[0]);
	}

	public long adjustTime(long time)
	{
		calendar.setTimeInMillis(time);
		return adjustCalendarTime();
	}

	public long adjustCalendarTime()
	{
		switch(calendarFieldNumber)
		{
			case Calendar.SECOND:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + multiplier);
				break;
			case Calendar.MINUTE:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + multiplier);
				break;
			case Calendar.HOUR:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + multiplier);
				break;
			case Calendar.DAY_OF_MONTH:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR, 0);
				calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + multiplier);
				break;
			case Calendar.YEAR:
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.HOUR, 0);
				calendar.set(Calendar.DAY_OF_MONTH, 0);
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + multiplier);
				break;
		}
		return calendar.getTimeInMillis();
	}


	public String toString() { return shortName; }
}
