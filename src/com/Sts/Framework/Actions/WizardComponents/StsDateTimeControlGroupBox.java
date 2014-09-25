package com.Sts.Framework.Actions.WizardComponents;

import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.DateTime.CalendarParser;
import com.Sts.Framework.Utilities.StsToolkit;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jan 22, 2010
 * Time: 9:35:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsDateTimeControlGroupBox extends StsGroupBox
{
    private long time;

	StsDateFieldBean timeBean;
	StsIntFieldBean dayFieldBean;
	StsIntFieldBean monthFieldBean;
	StsIntFieldBean yearFieldBean;
	StsIntFieldBean hourFieldBean;
	StsIntFieldBean minuteFieldBean;

	private SimpleDateFormat dateTimeFormat;
	private Calendar calendar = Calendar.getInstance();

    public StsDateTimeControlGroupBox(SimpleDateFormat dateTimeFormat)
    {
		this("Date and time control box", dateTimeFormat);
	}

	public StsDateTimeControlGroupBox(String boxTitle, SimpleDateFormat dateTimeFormat)
    {
        super(boxTitle);
		this.dateTimeFormat = dateTimeFormat;
		time = System.currentTimeMillis();
		calendar.setTimeInMillis(time);
        constructPanel();
    }

	private void initializeTime()
	{
		time = System.currentTimeMillis();
		calendar.setTimeInMillis(time);
	}

	private void updateTime()
	{
		time = calendar.getTimeInMillis();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		initializeBeans();
	}

    private void constructPanel()
    {
		timeBean = new StsDateFieldBean(this, "dateTimeString", true, "Date and time");
		timeBean.setColumns(20);
		dayFieldBean = new StsIntFieldBean(this, "day", 1, 31, "Day", true);
		monthFieldBean = new StsIntFieldBean(this, "month", 1, 12, "Month", true);
		yearFieldBean = new StsIntFieldBean(this, "year", 1970, 2100, "Year", true);
		hourFieldBean = new StsIntFieldBean(this, "hour", 0, 23, "Hour", true);
		minuteFieldBean = new StsIntFieldBean(this, "minute", 0, 59, "Minute", true);

		gbc.anchor = gbc.EAST;
		gbc.fill = gbc.HORIZONTAL;
		add(timeBean);
		add(yearFieldBean);
		add(monthFieldBean);
		add(dayFieldBean);
		add(hourFieldBean);
		add(minuteFieldBean);
    }

	public String getDateTimeString()
	{
		return CalendarParser.getDateTimeStringFromLong(time, dateTimeFormat);
	}

	public void setDateTimeString(String dateTimeString)
	{
		setTime(CalendarParser.getLongFromDateTimeString(dateTimeString));
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
		calendar.setTimeInMillis(time);
		initializeBeans();

	}

	public int getDay()
	{
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public void setDay(int day)
	{
		calendar.set(Calendar.DAY_OF_MONTH, day);
		updateTime();
	}

	public int getMonth()
	{
		return calendar.get(Calendar.MONTH) + 1;
	}

	public void setMonth(int month)
	{
		calendar.set(Calendar.MONTH, month-1);
		updateTime();
	}

	public int getYear()
	{
		return calendar.get(Calendar.YEAR);
	}

	public void setYear(int year)
	{
		calendar.set(Calendar.YEAR, year);
		updateTime();
	}

	public int getHour()
	{
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public void setHour(int hour)
	{
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		updateTime();
	}

	public int getMinute()
	{
		return calendar.get(Calendar.MINUTE);
	}

	public void setMinute(int minute)
	{
		calendar.set(Calendar.MINUTE, minute);
		updateTime();
	}

	static public void main(String[] args)
	{
		String defaultTimeFormatString = StsProject.defaultTimeFormatString;
    	String defaultDateFormatString = StsProject.defaultDateFormatString;
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(defaultDateFormatString + " " + defaultTimeFormatString);
		final StsDateTimeControlGroupBox box = new StsDateTimeControlGroupBox(dateTimeFormat);
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
