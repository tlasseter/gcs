
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.UI.Toolbars;

import com.Sts.Framework.Actions.*;
import com.Sts.Framework.DB.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class StsMouseActionToolbar extends StsToolbar implements StsSerializable
{
    transient private StsWin3dBase win3d;
    transient private StsModel model;
	boolean mouseReadoutSelected = false;
	transient private String readoutClassSelected = seismicReadout;
	transient private StsJPanel readoutComboBoxPanel;
    transient ButtonGroup mouseMotionButtonGroup = new ButtonGroup();
    transient private StsToggleButton zoomButton;
	transient private StsToggleButton rectzoomButton;
	transient private StsButton tetherButton;
	transient private StsToggleButton measureButton;
	transient private StsToggleButton panButton;
	transient private StsToggleButton rotateButton;

	transient private StsButton verticalStretchButton;
	transient private StsButton verticalShrinkButton;
	transient private StsButton horizontalStretchButton;
	transient private StsButton horizontalShrinkButton;

	transient StsButton topViewButton;

    transient private StsComboBoxFieldBean readoutComboBox;
    transient private StsToggleButton readoutToggleButton;
    transient private StsAction currentReadoutAction = null;

    static private final String seismicReadout = new String("seismic");
    static private final String surfaceReadout = new String("surfaces");
	static private final String sensorReadout = new String("sensors");

    static public final String NAME = "Mouse Action Toolbar";
    static public final boolean defaultFloatable = true;

    /** button gif filenames (also used as unique identifier button names) */
    public static final String ZOOM = "zoom";
	public static final String RECTZOOM = "rectzoom";
	public static final String TETHER = "tether";
	public static final String MEASURE = "measure";	
    public static final String PAN = "pan";
    public static final String ROTATE = "rotate";
    public static final String VERTICAL_STRETCH = "verticalStretch";
    public static final String VERTICAL_SHRINK = "verticalShrink";
	public static final String HORIZONTAL_STRETCH = "horizontalStretch";
	public static final String HORIZONTAL_SHRINK = "horizontalShrink";
    public static final String TOP_VIEW = "topView";
    public static final String INFO_OUTPUT = "info";

//    private transient int savedMouseMode = -1;
//    private transient int currentMouseMode = -1;

    public StsMouseActionToolbar()
     {
         super(NAME);
     }

	public StsMouseActionToolbar(StsWin3dBase win3d)
    {
        super(NAME);
        buildToolbar(win3d);
    }

    public boolean buildToolbar(StsWin3dBase win3d)
    {
        Border border = BorderFactory.createEtchedBorder();
        setBorder(border);

        this.win3d = win3d;
        this.model = win3d.getModel();
//        this.glPanel3d = win3d.glPanel3d;
        windowActionManager = win3d.getActionManager();

        // construct and add mouseMotion buttonGroup buttons to toolbar
//        boolean hasView3d = win3d.hasView(StsView3d.class);
//        boolean hasView2d = win3d.hasView(StsView2d.class);
        zoomButton = addToggleButton(ZOOM, "Change distance to data center of view (right mouse push/pull + Z key down).", ZOOM, mouseMotionButtonGroup);
		panButton = addToggleButton(PAN, "Pan scene vertically and horizontally (right mouse motion + X key down).", PAN, mouseMotionButtonGroup);
 //       if(hasView3d)
 //       {
        rotateButton = addToggleButton(ROTATE, "Rotate scene vertically and horizontally (right mouse motion).", ROTATE, mouseMotionButtonGroup);
        //measureButton = addActionToggleButton(MEASURE, "Select an object and measure distance to another object.", StsMeasureDistance.class);
        //tetherButton = addActionButton(TETHER, "Tether to an object in 3D", StsCenterObject.class);
		// rectzoomButton = addToggleButton(RECTZOOM, "Stretchy box zoom in 2D", RECTZOOM, mouseMotionButtonGroup);
		rotateButton.setSelected(true);
 //       }
	/*
        if(hasView2d)
        {
            rectzoomButton = addToggleButton(RECTZOOM, "Stretchy box zoom in 2D", RECTZOOM, mouseMotionButtonGroup);
            zoomButton.setSelected(true);
        }
    */
        // reconfigure();
        
		JPanel verticalPanel = new JPanel(new BorderLayout());
        verticalStretchButton = addButton(VERTICAL_STRETCH, "Vertical stretch (right mouse + A key press).");
		verticalStretchButton.setSize(20, 10);
		verticalPanel.add(verticalStretchButton, BorderLayout.NORTH);
        verticalShrinkButton = addButton(VERTICAL_SHRINK, "Vertical shrink (right mouse + SHIFT-A key press).");
		verticalShrinkButton.setSize(20, 10);
		verticalPanel.add(verticalShrinkButton, BorderLayout.SOUTH);
		add(verticalPanel);

		JPanel horizontalPanel = new JPanel(new BorderLayout());
		horizontalStretchButton = addButton(HORIZONTAL_STRETCH, "Horizontal stretch (right mouse + S key press).");
		horizontalStretchButton.setSize(20, 10);
		horizontalPanel.add(horizontalStretchButton, BorderLayout.EAST);
		horizontalShrinkButton = addButton(HORIZONTAL_SHRINK, "Horizontal shrink (right mouse + SHIFT-S key press).");
		horizontalShrinkButton.setSize(20, 10);
		horizontalPanel.add(horizontalShrinkButton, BorderLayout.WEST);
		add(horizontalPanel);

        topViewButton = addButton(TOP_VIEW, "Jump to top down view of project.", TOP_VIEW);
        addSeparator();
        addReadoutButtonBox();
        StsToggleButton infoBtn = new StsToggleButton(INFO_OUTPUT, "Information Tab display of positioning information.", this, "infoOutput");
        infoBtn.addIcons("infoSelect", "infoDeselect");
        add(infoBtn);
        addSeparator();
        addCloseIcon(win3d);
        setMinimumSize();
        revalidate();
        return true;
    }

	private String[] getReadoutClasses()
	{
		return new String[] { seismicReadout, surfaceReadout, sensorReadout };
	}

    /** toolbar has been built; check if it needs to be changed and rebuild if necessary */
    public boolean initialize(StsWin3dBase win3d, StsModel model)
    {
        buildToolbar(win3d);
        return true;
    }

    public void reconfigure()
    {
        removeAll();
        buildToolbar(win3d);
    }

	private void addReadoutButtonBox()
    {
        readoutToggleButton = new StsToggleButton(null, "Display mouse position readout.", this, "readoutSelected", "readoutDeselected");
        readoutToggleButton.addIcons("mouseReadoutSelect", "mouseReadoutDeselect");
        readoutComboBox = new StsComboBoxFieldBean(this, "readoutClassSelected", null,  "readoutClasses");


        readoutComboBoxPanel = new StsJPanel();

        readoutComboBoxPanel.addToRow(readoutToggleButton);
        readoutComboBoxPanel.addEndRow(readoutComboBox);
        add(readoutComboBoxPanel);
    }

	public void readoutSelected()
	{
		setReadoutClassSelected(readoutClassSelected);
	}

	public void readoutDeselected()
	{
		if(currentReadoutAction == null) return;
		windowActionManager.endAction(currentReadoutAction);
        currentReadoutAction = null;
	}

	public String getReadoutClassSelected() { return readoutClassSelected; }

    public void setReadoutClassSelected(String readoutClassSelected)
    {
		boolean isSelected = readoutToggleButton.isSelected();
		if(!isSelected)
		{
			readoutToggleButton.setSelected(true);
			isSelected = true;
		}
		if(this.readoutClassSelected == readoutClassSelected) return;
        windowActionManager.endAction(currentReadoutAction);
        currentReadoutAction = null;
		this.readoutClassSelected = readoutClassSelected;
        if(readoutClassSelected == seismicReadout)
        {
            if(isSelected)
                currentReadoutAction = windowActionManager.startAction(StsCursor3dReadout.class);
            else
            {
                windowActionManager.endAction(currentReadoutAction);
                currentReadoutAction = null;
            }
        }
	/*
        else if(readoutClassSelected == surfaceReadout)
        {
            if(isSelected)
                currentReadoutAction = windowActionManager.startAction(StsSurfaceReporting.class);
            else
            {
                windowActionManager.endAction(currentReadoutAction);
                currentReadoutAction = null;
            }
        }
        else if(readoutClassSelected == sensorReadout)
        {
            if(isSelected)
                currentReadoutAction = windowActionManager.startAction(StsSensorReporting.class);
            else
            {
                windowActionManager.endAction(currentReadoutAction);
                currentReadoutAction = null;
            }
        }
     */
    }

	/** Reset views to appropriate mouseMode (ZOOM, ROTATE, PAN, PANVERTICAL), but reset this button here to rotate.
	 *  Perhaps should decide whether there is a consistent mouseMode if there is more than one panel in this window
	 *  and they have different default modes.
	 */
	public void resetMouseToggle()
	{
        win3d.setViewsDefaultMouseModes();
		rotateButton.setSelected(true);
//        currentMouseMode = savedMouseMode;
//        getCurrentToggleButton(currentMouseMode).setSelected(true);
    }

    /** Change Mouse to Zoom */
    public void zoom()
    {
        setMouseMode(StsCursor.ZOOM);
    }

    private void setMouseMode(int mouseMode)
    {
//        savedMouseMode = currentMouseMode;
 //       currentMouseMode = mouseMode;
        for(StsView view : win3d.getDisplayedViews())
            view.setMouseModeFromToolbar(mouseMode);
    }

	/** Change Mouse to RectangleZoom */
	public void rectzoom()
	{
        setMouseMode(StsCursor.RECTZOOM);
    }
    /** Change Mouse to Pan */
    public void pan()
    {
		setMouseMode(StsCursor.PAN);
    }

    /** Change Mouse to Rotate */
    public void rotate()
    {
		setMouseMode(StsCursor.ROTATE);
    }

    /** Stretch view vertically */
    public void verticalStretch()
    {
        for(StsGLPanel3d glPanel3d : win3d.getDisplayedGLPanels())
            glPanel3d.verticalStretch(null);
    }

    /** Shrink view vertically */
    public void verticalShrink()
    {
        for(StsGLPanel3d glPanel3d : win3d.getDisplayedGLPanels())
            glPanel3d.verticalShrink(null);
    }

	/** Stretch view vertically */
	public void horizontalStretch()
	{
        for(StsGLPanel3d glPanel3d : win3d.getDisplayedGLPanels())
            glPanel3d.horizontalStretch(null);
	}

	/** Shrink view vertically */
	public void horizontalShrink()
	{
        for(StsGLPanel3d glPanel3d : win3d.getDisplayedGLPanels())
            glPanel3d.horizontalShrink(null);
    }
    /** Output position information to info panel */
    public void infoOutput()
    {
        for(StsGLPanel3d glPanel3d : win3d.getDisplayedGLPanels())
        {
            if(glPanel3d.mouseInfo)
                glPanel3d.mouseInfo = false;
            else
                glPanel3d.mouseInfo = true;
        }
        return;
    }

    /** Jump to top down view */
    public void topView()
    {
        win3d.getGlPanel3d().checkAddView(StsView3d.class);
        StsView3d view = (StsView3d) win3d.getGlPanel3d().getView();
        view.setTopView();
        win3d.getGlPanel3d().changeModelView();
        return;
    }

    public void tether()
    {
    	;
    }
    public void measure()
    {
    	if(!measureButton.isSelected())
    		clearHighlights();
    }
    public void clearHighlights()
    {
    	//StsMicroseismicClass microseismicClass = (StsMicroseismicClass)model.getStsClass("com.Sts.PlugIns.HorizonPick.DBTypes.StsSensor");
    	//microseismicClass.clearHighlights();
    }
    public boolean isTethered()
    {
    	return tetherButton.isSelected();
    }
    public boolean isMeasuring()
    {
    	return measureButton.isSelected();
    } 
    public void resetTether()
    {
    	tetherButton.setSelected(false);
    	tetherButton.revalidate();
    }    
    public void resetMeasuring()
    {
    	measureButton.setSelected(false);
    	measureButton.revalidate();    	
    }

    /** selected view has changed; possibly change mouseActionToolbar response */
    public void viewChanged(boolean is3d)
	{
//		checkSetIs3d(is3d);
	}

    public boolean forViewOnly()
    {
        remove(horizontalStretchButton);
        remove(horizontalShrinkButton);
        remove(topViewButton);
        remove(readoutComboBoxPanel);
        remove(readoutToggleButton);
        return true;
    }
}

