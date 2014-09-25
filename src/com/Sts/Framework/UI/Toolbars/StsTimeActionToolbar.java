
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.UI.Toolbars;

import com.Sts.Framework.Actions.Time.*;
import com.Sts.Framework.DB.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DateTime.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

public class StsTimeActionToolbar extends StsToolbar implements MouseListener, ChangeListener, StsSerializable
{
    transient private StsWin3dBase win3d;
    transient private StsModel model;
    transient private StsActionManager windowActionManager;

    transient private JSpinner speedSpinner;
    transient SpinnerListModel spinnerModel;
	transient int spinnerIndex = 0;

	static String[] spinnerValues = new String[] {"   1s", "   5s", " 10s", " 30s", "   1m", "   5m", " 10m", " 30m", "   1h", "   5h",
                             " 10h", "   1d", "   2d", "   5d", " 10d", " 30d", "180d", "   1y", "   2y", "   5y", " 10y" };
	static StsTimeControlStep[] timeSteps =  StsTimeControlStep.constructTimeControlSteps(spinnerValues);


    static public String realtimeModule = "com.sts.StsRealtime";

    transient private StsButton realtimeButton;
    transient private StsButton stopButton;
    transient private StsButton startButton;
    transient private StsToggleButton watchButton;
    transient private StsToggleButton timeLineButton;

    transient private long speed = 1; // Multiple of real time.
	transient private StsTimeControlStep timeControlStep = timeSteps[0];

    transient private StsTimeAction timeAction = null;

    transient private StsStringFieldBean currentTimeField;
    // transient private String modelTime = "12-11-06 12:00:00.0";
    private static final int NONE = -1;
    private static final int DAY = 2;
    private static final int MONTH = 5;
    private static final int YEAR = 8;
    private static final int HOUR = 11;
    private static final int MINUTE = 14;
    private static final int SECOND = 17;
    private int currentField = NONE;

	//TODO realtime should be moved to project as it is very expensive to call isRealTime() as we do all the time and
	//TODO have to search thru the toolbars to get this one and return "realtime".
    transient boolean realtime = false;

    public static final String NAME = "Time Action Toolbar";
    public static final boolean defaultFloatable = true;
    // transient StsWatchMonitorsDialog watchDialog = null;
    //transient StsViewTimeLine viewTimeLine = null;

    /** button gif filenames (also used as unique identifier button names) */
    public static final String SPEED_INCREASE = "speedIncrease";
	public static final String SPEED_DECREASE = "speedDecrease";
    public static final String REAL_TIME = "realTime";
    public static final String STOP_TIME = "stopTime";
    public static final String START_TIME = "startTime";

    public static final String INCREASE = "verticalStretch";
    public static final String DECREASE = "verticalShrink";
    
    public static final String WATCH = "watchProjectFiles";
    public static final String TIMELINE = "timeLine";

    transient SimpleDateFormat format = null;
    transient boolean informed = false;

    transient JPopupMenu popup = null;

    public StsTimeActionToolbar()
     {
         super(NAME);
     }

	public StsTimeActionToolbar(StsWin3dBase win3d)
	{
        super(NAME);
        initialize(win3d, model);
	}

	public static StsTimeControlStep[] getTimeSteps()
	{
		return timeSteps;
	}

	public boolean initialize(StsWin3dBase win3d, StsModel model)
    {
        this.win3d = win3d;
        this.model = win3d.getModel();
        windowActionManager = win3d.getActionManager();
        format = new SimpleDateFormat(win3d.getModel().getProject().getTimeDateFormatString());
        
        Border border = BorderFactory.createEtchedBorder();
        Font font = new Font("Dialog", Font.BOLD, 11);
        setBorder(border);

        //Add Speed Field
        JPanel speedPanel = new JPanel(new BorderLayout());
        spinnerModel = new SpinnerListModel(timeSteps);
        speedSpinner = new JSpinner(spinnerModel);
        speedSpinner.setFont(font);
        speedPanel.add(speedSpinner);
        speedSpinner.addChangeListener(this);

        add(speedPanel);
        addSeparator();

		String currentTime = getCurrentTime().toString();
        currentTimeField = new StsStringFieldBean(this, "modelTimeString", currentTime, true, null);
        currentTimeField.setTextBackground(Color.BLACK);
        currentTimeField.setTextForeground(Color.GREEN);
        currentTimeField.setTextFont(font);
        currentTimeField.setToolTipText("Enter model date and time.");
        currentTimeField.getTextField().addMouseListener(this);
        add(currentTimeField);
        addSeparator();

        startButton = addButton("moviePlay", "Start playing from current setting.", START_TIME);
        stopButton = addButton("movieStop", "Stop playing at current setting.", STOP_TIME);
        realtimeButton = addButton("realtimePlay", "Set clock to current time and play.", REAL_TIME);
		//TODO create watchSelect and watchDeselect gif icons
        watchButton = addToggleButton(WATCH, "Watch the source data directory", WATCH, WATCH);
		//TODO create timelineSelect and timeLineDeselect gif icons
//        timeLineButton = addToggleButton(TIMELINE, "View Time Line", TIMELINE, TIMELINE);
        addSeparator();
        
        addCloseIcon(win3d);
        setMinimumSize();
        this.model.getProject().setProjectTimeToCurrentTime();
		getSetSpinnerIndex();
        updateTime();
        return true;
    }

    public void initializeAction(StsWin3d win3d)
    {
	   model = win3d.model;
       timeAction = new StsTimeAction(windowActionManager, this, model.getProject());
    }

	public void setTimeAction(StsTimeAction ta)
    {
        timeAction = ta;
    }

    public long getSpeed()
    {
        return speed;
    }

    /** Decrease speed by half */
    public void speedDecrease()
    {
        if(spinnerIndex > 0)
			setSpinnerIndex(spinnerIndex-1);
    }
    /** Increase speed by 2x */
    public void speedIncrease()
    {
        if(spinnerIndex < timeSteps.length-2)
            setSpinnerIndex(spinnerIndex+1);
    }

    /** Get/Set current time */
    public Date getModelTime()
    {
		long projectTime = model.getProject().getCurrentTime();
		return new Date(projectTime);
    }

    public long getModelTimeLong()
    {
        return model.getProject().getCurrentTime();
    }

	public Date getCurrentTime()
	{
		long projectTime = model.getProject().getCurrentTime();
		return new Date(projectTime);
	}

    public String getModelTimeString()
    {
        return getModelTime().toString();
    }
    public void setModelTimeString(String value)
    {
        // Convert to required format
        try
        {
            Date date = format.parse(value);
            model.getProject().setProjectTime(date.getTime());
            currentTimeField.setTextBackground(Color.BLACK);
            currentTimeField.setTextForeground(Color.GREEN);
            realtime = false;
            model.viewObjectChanged(this, this);
        }
        catch(Exception e)
        {
            StsMessageFiles.infoMessage("Unable to parse date: " + value);
        }
        model.win3dDisplayAll();
    }

    /** Stop at current time */
    public void stopTime()
    {
        timeAction.stopAction();
        realtime = false;
        currentTimeField.setTextBackground(Color.BLACK);
        currentTimeField.setTextForeground(Color.GREEN);
        model.viewObjectChanged(this, this);
    }
    public boolean isRunning()
    {
        return timeAction.isRunning();
    }

    public void watchProjectFiles()
    {
		model.getProject().watchProjectFiles(watchButton.isSelected());
	}

    /** Watch all monitor objects */
/*
    public void watchMonitors()
    {
        if(watchDialog != null)
        {
            if(watchDialog.isVisible())
            {
                watchDialog.setVisible(false);
                return;
            }
        }
        StsObject[] monitors = (StsObject[])model.getCastObjectList(StsMonitor.class);
        if(monitors.length == 0)
        {
            StsGroupBox panel = new StsGroupBox();
            panel.addEndRow(new JLabel("No Monitors are available.\nWould you like to create one?"));
		    StsOkDialog dialog = new StsOkDialog(win3d, panel,"No Monitors", true);
            if(!dialog.okPressed())
                return;
            else
            {
                win3d.getActionManager().launchWizard("com.Sts.Framework.Actions.Wizards.Monitor.StsMonitorWizard", "Monitors");
                return;
            }
        }
        watchDialog = new StsWatchMonitorsDialog(model, false, true);
        watchDialog.setVisible(true);
    }
*/
    /** Watch all monitor objects */
	/*
    public void timeLine()
    {
		boolean isSelected = timeLineButton.isSelected();
		if(isSelected)
        {
			viewTimeLine = new StsViewTimeLine(model.win3d);
			StsToolkit.centerComponentOnScreen(viewTimeLine);
			viewTimeLine.setVisible(true);
        }
		else if(viewTimeLine.isVisible())
            viewTimeLine.setVisible(false);
    }
   */
    /** Start at current time */
    public void startTime()
    {
        timeAction.playAction();
    }

    public void runRealTime()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                realTime();
            }
        };
        StsToolkit.runLaterOnEventThread(runnable);
    }

    /** Set to current time */
    public void realTime()
    {
        if(Main.viewerOnly && !informed)
        {
            StsGroupBox panel = new StsGroupBox();
            panel.addEndRow(new JLabel("Standard usage charges will apply to real-time operations of the software."));
		    StsOkDialog dialog = new StsOkDialog(win3d, panel,"Real-time Usage Charges", true);
            if(!dialog.okPressed())
                return;
            informed = true;
        }
        realtime = true;
        //Main.logUsageTimer(realtimeModule, "Realtime");
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        model.getProject().setProjectTime(time);
        String modelTime = format.format(date);
        currentTimeField.setText(modelTime);
        currentTimeField.setTextBackground(Color.RED);
        currentTimeField.setTextForeground(Color.BLACK);                    
        speedSpinner.setValue(timeSteps[0]);

        // Set playback speed to 100%
		setSpinnerIndex(0);
        if(timeAction != null) timeAction.playAction();
        model.getProject().setProjectTime(getModelTimeLong());
        model.viewObjectChanged(this, this);
    }

    public boolean isRealtime() { return realtime; }

    public void updateTime()
    {
		String modelTime = null;
        try
        {
            if(realtime)
            {
               model.getProject().setProjectTimeToCurrentTime();
               //Main.logUsageTimer(realtimeModule, "Realtime");
            }

            Date date = new Date(model.getProject().getCurrentTime());
            currentTimeField.setValue(format.format(date));
            modelTime = format.format(date);
            
            // Update the watch dialog if up
		/*
            if(watchDialog != null)
                if(watchDialog.isVisible())
            	    watchDialog.updateDialog();
        */
			/*
            if(viewTimeLine != null)
                if(viewTimeLine.isVisible())
                    viewTimeLine.setTime(model.getProject().getCurrentTime());
            */

        }
        catch(Exception e)
        {
            StsMessageFiles.infoMessage("Unable to parse date: " + modelTime);
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        int clicks = e.getClickCount();
        if(clicks == 2)
        {
            int selectedEnd = currentTimeField.getTextField().getSelectionEnd();
            String selectedValue = currentTimeField.getTextField().getSelectedText();
            if(selectedValue == null)
            	return;
            if(selectedValue.equals(":") || selectedValue.equals("-") || (selectedValue.equals("/")))
                return;
            if(selectedValue.startsWith("0"))
                selectedValue = selectedValue.substring(1,2);
            if(selectedValue.length() > 2)
                selectedValue = selectedValue.substring(0,2);
            int value = Integer.parseInt(selectedValue);
            String subString = currentTimeField.getTextField().getText().substring(0,selectedEnd);
            switch(subString.length())
            {
                case DAY:
                    currentField = DAY;
                    showSlider("Day", e.getX(), e.getY(), 1, 31, 5, 0, value);
                    break;
                case MONTH:
                    currentField = MONTH;
                    showSlider("Month", e.getX(), e.getY(), 1, 12, 1, 0, value);
                    break;
                case YEAR:
                    currentField = YEAR;
                    Calendar c = Calendar.getInstance();
                    int year = c.get(c.YEAR);
                    if(value > 25)
                        value = value + 1900;
                    else
                        value = value + 2000;
                    showSlider("Year", e.getX(), e.getY(), 1971, year, 10, 5, value);
                    break;
                case HOUR:
                    currentField = HOUR;
                    showSlider("Hour", e.getX(), e.getY(), 0, 23, 4, 2, value);
                    break;
                case MINUTE:
                    currentField = MINUTE;
                    showSlider("Minute", e.getX(), e.getY(), 0, 60, 10, 5, value);
                    break;
                default:
                    currentField = SECOND;
                    showSlider("Second", e.getX(), e.getY(), 0, 60, 10, 5, value);
                    break;
            }
            realtime = false;
            model.viewObjectChanged(this, this);
        }
    }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e)
    {
        if(e.getSource() instanceof JSlider)
        {
            popup.setVisible(false);
        }
    }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    private void showSlider(String label, int xpos, int ypos, int minValue, int maxValue, int major, int minor, int value)
    {
        popup = new JPopupMenu();
        popup.setLayout(new GridBagLayout());
        JLabel jlabel = new JLabel(label);
        jlabel.setFont(new Font("Dialog", Font.BOLD, 10));
        popup.add(jlabel);
        popup.add(jlabel,  new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        JSlider slider = new JSlider(JSlider.VERTICAL, minValue, maxValue, value);
        slider.setInverted(true);
        slider.setPaintTrack(false);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(major);
        slider.setMinorTickSpacing(minor);
        slider.setFont(new Font("Dialog", Font.PLAIN, 10));
        slider.setBackground(Color.LIGHT_GRAY);
        slider.setBorder(BorderFactory.createEtchedBorder());
        slider.addChangeListener(this);
        slider.addMouseListener(this);
        popup.add(slider,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0 ,GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

        currentTimeField.getTextField().add(popup);
        popup.show(currentTimeField.getTextField(), xpos + 12, ypos);
    }

    public void stateChanged(ChangeEvent e)
    {
        if(e.getSource() instanceof JSlider)
        {
            String sValue = null;
            int value = ((JSlider)e.getSource()).getValue();
            String cTime = currentTimeField.getValue();
            sValue = Integer.toString(value);
            if(sValue.length() == 1)
                sValue = "0" + sValue;
            if(currentField == YEAR)
                sValue = sValue.substring(2,4);
            cTime = cTime.substring(0,currentField-2) + sValue + cTime.substring(currentField,cTime.length());

            try
            {
                Date date = format.parse(cTime);
                if (date.getTime() >= System.currentTimeMillis())
                    runRealTime();
                else
                {
                    currentTimeField.setValue(cTime);
                    model.getProject().setProjectTime(getModelTimeLong());
                }
            }
            catch(Exception ex)
            {
                StsMessageFiles.infoMessage("Unable to parse date: " + cTime);
            }
        }
        else if(e.getSource() instanceof JSpinner)
        {
            this.revalidate();
			getSetSpinnerIndex();
        }
    }

	private void getSetSpinnerIndex()
	{
		int newIndex = 0;
		StsTimeControlStep selection = (StsTimeControlStep)spinnerModel.getValue();
		for(int n = 0; n < timeSteps.length; n++)
			if(selection == timeSteps[n])
			{
				newIndex = n;
				setSpinnerIndex(newIndex);
				break;
			}
	}

	private void setSpinnerIndex(int index)
	{
		spinnerIndex = index;
		timeControlStep = timeSteps[index];
		long projectTime = model.getProject().getCurrentTime();
		projectTime = timeControlStep.adjustTime(projectTime);
		model.getProject().setProjectTime(projectTime);
		speed = timeSteps[index].msecPerStep;
	}
    
    public void resetFormat()
    {	
    	format = new SimpleDateFormat(model.getProject().getTimeDateFormatString());
    	updateTime();
    }
    
    public void endAction()
    {
        if(timeAction == null) return;
        timeAction.end();
    }

    public boolean forViewOnly()
    {
        return true;
    }

	public StsTimeControlStep getTimeControlStep()
	{
		return timeControlStep;
	}
}
