package com.Sts.Framework.Actions.WizardComponents;

import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.*;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jan 22, 2010
 * Time: 9:35:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsRelativeTimeGroupBox extends StsGroupBox
{
    private long relativeTime;
	private int minutes, hours, days;
	StsIntFieldBean dayFieldBean;
	StsIntFieldBean hourFieldBean;
	StsIntFieldBean minuteFieldBean;

	static final long msecsPerDay = StsParameters.msecsPerDay;
	static final long msecsPerHour = 1000*3600;
	static final long msecsPerMinute = 1000*60;

    public StsRelativeTimeGroupBox()
    {
		this("Relative time control box");
	}

	public StsRelativeTimeGroupBox(String boxTitle)
    {
        super(boxTitle);
        constructPanel();
    }

	private void updateTime()
	{
		relativeTime = msecsPerMinute*minutes + msecsPerHour*hours + msecsPerDay*days;
	}

    private void constructPanel()
    {
		dayFieldBean = new StsIntFieldBean(this, "day", 0, 1000, "Days", true);
		hourFieldBean = new StsIntFieldBean(this, "hour", 0, 1000, "Hours", true);
		minuteFieldBean = new StsIntFieldBean(this, "minute", 0, 1000, "Minutes", true);

		gbc.anchor = gbc.EAST;
		gbc.fill = gbc.HORIZONTAL;
		add(dayFieldBean);
		add(hourFieldBean);
		add(minuteFieldBean);
    }

	public long getRelativeTime()
	{
		return relativeTime;
	}

	public int getDay()
	{
		return days;
	}

	public void setDay(int days)
	{
		this.days = days;
		updateTime();
	}

	public int getHour()
	{
		return hours;
	}

	public void setHour(int hours)
	{
		this.hours = hours;
		updateTime();
	}

	public int getMinute()
	{
		return minutes;
	}

	public void setMinute(int minutes)
	{
		this.minutes = minutes;
		updateTime();
	}

	static public void main(String[] args)
	{
		String defaultTimeFormatString = StsProject.defaultTimeFormatString;
    	String defaultDateFormatString = StsProject.defaultDateFormatString;
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(defaultDateFormatString + " " + defaultTimeFormatString);
		final StsRelativeTimeGroupBox box = new StsRelativeTimeGroupBox();
		// box.setTime(System.currentTimeMillis());
		StsToolkit.runLaterOnEventThread
		(
			new Runnable()
			{
				public void run()
				{
					StsToolkit.createDialog(box);
				}
			}
		);
	}
}
