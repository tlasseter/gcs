//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Wells.DBTypes;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.DBTypes.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;
import com.Sts.PlugIns.Seismic.Types.*;
import com.Sts.PlugIns.Wells.UI.*;
import com.magician.fonts.*;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class StsWell extends StsLine implements StsSelectable, StsTreeObjectI, StsViewSelectable // , StsMonitorable
{
    // instance fields
	protected StsObjectRefList logCurveVectorSets = null;
    protected StsObjectRefList logCurves = null;
    protected StsObjectRefList lithZones = null;
    protected StsObjectRefList markers = null;
    /** StsSurfaceVertex at intersection of well, surface, block */
    //    protected StsObjectRefList surfaceVertices = null;
    protected boolean drawMarkers = true;
    protected boolean drawSurfaceMarkers = true;
    protected boolean drawPerfMarkers = true;
    protected boolean drawFMIMarkers = true;
    protected boolean drawEquipmentMarkers = true;
    protected boolean hasVsp = false;  // used in vsp display on well window; should be String vspName

	transient protected StsLogCurve[] leftDisplayLogCurves;
	transient protected StsLogCurve[] rightDisplayLogCurves;

    transient public StsWellFrameViewModel wellViewModel;

    transient protected StsSeismicCurtain seismicCurtain = null;

    protected String drawLabelString = NO_LABEL;
    protected float labelInterval = 100.0f;
    /** Alarms */
    //protected StsAlarm[] alarms = null;

    static final String[] TD_BOTH_STRINGS = StsParameters.TD_BOTH_STRINGS;

    protected float curtainOffset = 0.0f;

    protected String operator = "unknown";
    protected String company = "unknown";
    protected String field = "unknown";
    protected String area = "unknown";
    protected String state = "unknown";
    protected String county = "unknown";
    protected String wellNumber = "unknown";
    protected String wellLabel = "unknown";
    protected String api = "00000000000000";
    protected String uwi = "00000000000000";
    protected String date = "unknown";
    protected float kbElev = 0.0f;
    protected float elev = 0.0f;
    protected String elevDatum = "unknown";
    protected long spudDate = 0L;
    protected long completionDate = 0L;
    protected long permitDate = 0L;


    public boolean isDrawingCurtain = false;
    public boolean drawCurtainTransparent = false;

    //	private transient float[] verticesDepths = null;
    //	private transient float[] verticesMDepths = null;
    //	private transient float[] verticesTimes = null;
    //	private transient StsPoint[] timePoints = null;

    //    private transient String exportName = null;
    //    private transient boolean exportLogData;
    //    private transient boolean exportDeviationData;
    //    private transient boolean exportSeisAttData;

    static final float maxMDepthError = 10.0f;

    static protected StsWell currentWell = null;
    static protected StsObjectPanel objectPanel = null;

    static public final float nullValue = StsParameters.nullValue;
    static public final String TIME = StsLoader.SEISMIC_TIME;

	/** group the master file must belong to. */
	static public final String masterGroup = StsLoader.GROUP_WELL;
	/** subFiles must belong to one of these groups. */
	static public final String[] subFileGroups = StsLoader.wellSubfileGroups;

    static public final String NO_LABEL = "No Label";
    static public final String MDEPTH_LABEL = "Measured Depth";
    static public final String DEPTH_LABEL = "TVD";
    static public final String TIME_LABEL = "Time";

    static public final String[] LABEL_STRINGS = new String[]{NO_LABEL, MDEPTH_LABEL, DEPTH_LABEL, TIME_LABEL};

    static StsDateFieldBean bornField = null;
    static StsDateFieldBean deathField = null;
    static StsFloatFieldBean curtainOffsetBean = null;
	
    static public StsFieldBean[] displayFields = null;
    static public StsFieldBean[] propertyFields = null;

	static public final String groupName = "well";
	static public final String stsJarFilename = "StsWells.jar";
    /** default constructor */
    public StsWell() // throws StsException
    {
    }

	public StsWell(boolean persistent) // throws StsException
    {
		super(persistent);
		initializeDataSource();
    }

    public StsWell(String name, boolean persistent)
    {
        this(name, persistent, getWellClass().getDefaultNColor());
    }

    public StsWell(String name, boolean persistent, int nColor)
    {
        super(name, persistent);
		initializeDataSource();
        setZDomainOriginal(StsParameters.TD_DEPTH);
        isDrawingCurtain = getWellClass().getDefaultIsDrawingCurtain();
        drawZones = getWellClass().getDefaultIsDrawZones();
        // setVerticesRotated(false);
		setStsColorIndex(nColor);
    }

    static public StsWell nullWellConstructor(String name)
    {
        return new StsWell(name, false);
    }
/*
	public void addToModel()
	{
		if(logCurveVectorSets != null) logCurveVectorSets.addToModel();
    	if(logCurves != null) logCurves.addToModel();
    	if(lithZones != null) lithZones.addToModel();
    	if(markers != null) markers.addToModel();
		super.addToModel();
	}
*/
    public void initializeColor()
    {
        if (currentModel != null)
            stsColor = new StsColor(getWellClass().getDefaultColor());
        else
            stsColor = new StsColor(Color.RED);
    }

    public long getBornDate()
    {
        if(getLineVectorSet() == null) return getBornDate();
		StsTimeVector timeVector = getLineVectorSet().getClockTimeVector();
		if(timeVector == null) return getBornDate();
        return timeVector.getMinValue();
    }

    public long getDeathDate()
    {
        if(getLineVectorSet() == null) return getDeathDate();
		StsTimeVector timeVector = getLineVectorSet().getClockTimeVector();
		if(timeVector == null) return getDeathDate();
        return timeVector.getMaxValue();
    }

    public float getTopZ()
    {
        if(getLineVectorSet() == null) return 0.0f;
        return getTopPoint().getZ();
    }

    public float getBotZ()
    {
        if(getLineVectorSet() == null) return 0.0f;
        return getBotPoint().getZ();
    }

    public float getCurtainOffset() { return curtainOffset; }

    public void setCurtainOffset(float val)
    {
        if (isVertical && val > 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.INFO, "Can't offset vertical well: resetting curtain offset to zero");
//            StsMessageFiles.errorMessage("Can't offset vertical well: resetting curtain offset to zero");
            curtainOffset = 0.0f;
            curtainOffsetBean.setValue(curtainOffset);
            dbFieldChanged("curtainOffset", curtainOffset);
            return;
        }

        if (curtainOffset == val) return;

        curtainOffset = val;
        dbFieldChanged("curtainOffset", curtainOffset);
        deleteSeismicCurtain();
        createCurtain();
        currentModel.viewObjectChanged(this, this);
    }

    public String getOperator() { return operator; }

    public String getField() { return field; }

    public String getArea() { return area; }

    public String getState() { return state; }

    public String getCounty() { return county; }

    public String getWellNumber() { return wellNumber; }

    public String getWellLabel() { return wellLabel; }

    public String getApi() { return api; }

    public String getUwi() { return uwi; }

    public String getDate() { return date; }

    public String getCompany() { return company; }

    public float getKbElev() { return kbElev; }

    public float getElev() { return elev; }

    public String getElevDatum() { return elevDatum; }

    public long getSpudDate() { return spudDate; }

    public String getSpudDateString()
    {
        if (spudDate == 0l)
            return "Undefined";
        else
            return currentModel.getProject().getDateFormat().format(new Date(spudDate));
    }

    public long getCompletionDate() { return completionDate; }

    public String getCompletionDateString()
    {
        if (completionDate == 0l)
            return "Undefined";
        else
            return currentModel.getProject().getDateFormat().format(new Date(completionDate));
    }

    public long getPermitDate() { return permitDate; }

    public String getPermitDateString()
    {
        if (permitDate == 0l)
            return "Undefined";
        else
            return currentModel.getProject().getDateFormat().format(new Date(permitDate));
    }

    public void setKbElev(float kbEl) { kbElev = kbEl; }

    public void setElev(float el) { elev = el; }

    public void setElevDatum(String ed) { elevDatum = ed; }

    public void setOperator(String op) { operator = op; }

    public void setField(String fld) { field = fld; }

    public void setArea(String a) { area = a; }

    public void setState(String st) { state = st; }

    public void setCounty(String cnty) { county = cnty; }

    public void setWellNumber(String num) { wellNumber = num; }

    public void setWellLabel(String label) { wellLabel = label; }

    public void setApi(String api) { this.api = api; }

    public void setUwi(String uwi) { this.uwi = uwi; }

    public void setDate(String date) { this.date = date; }

    public void setCompany(String company) { this.company = company; }

    public void setSpudDate(long spud) { this.spudDate = spud; }

    public void setCompletionDate(long complete) { this.completionDate = complete; }

    public void setPermitDate(long permit) { this.permitDate = permit; }

    public void setSpudDateString(String spud) { }

    public void setCompletionDateString(String complete) { }

    public String getNativeVerticalUnitsString()
    {
        return StsParameters.DIST_STRINGS[nativeVerticalUnits];
    }

    public byte getNativeVerticalUnits()
    {
        return nativeVerticalUnits;
    }

    public String getNativeHorizontalUnitsString()
    {
        return StsParameters.DIST_STRINGS[nativeHorizontalUnits];
    }

    public byte getNativeHorizontalUnits()
    {
        return nativeHorizontalUnits;
    }

    static public void initColors()
    {
        StsColorListFieldBean colorList;
        StsSpectrum spectrum = currentModel.getSpectrum("Basic");
        colorList = (StsColorListFieldBean) StsFieldBean.getBeanWithFieldName(displayFields, "stsColor");
        colorList.setListItems(spectrum);
    }

    public StsFieldBean[] getDisplayFields()
    {
        if (displayFields == null)
        {
            // bornField = new StsDateFieldBean(StsWell.class, "bornDate", "Born Date:");
            // deathField = new StsDateFieldBean(StsWell.class, "deathDate", "Death Date:");
            curtainOffsetBean = new StsFloatFieldBean(StsWell.class, "curtainOffset", true, "CurtainOffset", true);
            curtainOffsetBean.setRangeFixStep(-1000.0, 1000.0, getWellClass().getCurtainStep());
            displayFields = new StsFieldBean[]
                {
                    new StsBooleanFieldBean(StsWell.class, "isVisible", "Enable"),
                    //bornField,
                    //deathField,
                    new StsBooleanFieldBean(StsWell.class, "drawZones", "Show Zones"),
                    new StsBooleanFieldBean(StsWell.class, "drawMarkers", "Show Markers"),
                    new StsBooleanFieldBean(StsWell.class, "drawPerfMarkers", "Show Perforations"),
                    new StsBooleanFieldBean(StsWell.class, "drawEquipmentMarkers", "Show Equipment"),
                    new StsBooleanFieldBean(StsWell.class, "drawCurtain", "Show Curtain"),
                    new StsBooleanFieldBean(StsWell.class, "drawCurtainTransparent", "Make Curtain Transparent"),
                    curtainOffsetBean,
                    new StsButtonFieldBean("Select left logs", "Select logs to display on left of well from dialog.", this, "logDisplay3dLeft"),
                    new StsButtonFieldBean("Select right logs", "Select logs to display on right of well from dialog.", this, "logDisplay3dRight"),
                    new StsBooleanFieldBean(StsWell.class, "highlighted", "Highlight Well"),
                    new StsComboBoxFieldBean(StsWell.class, "drawLabelString", "Label Type:", LABEL_STRINGS),
                    new StsFloatFieldBean(StsWell.class, "labelInterval", true, "Label Interval:"),
                    new StsColorIndexFieldBean(StsWell.class, "stsColorIndex", "Color:", colorList),
               };
        }
        return displayFields;
    }

    public StsFieldBean[] getPropertyFields()
    {
        if(propertyFields == null)
        {
            propertyFields = new StsFieldBean[]
            {
                    new StsFloatFieldBean(StsWell.class, "topZ", false, "Min Depth:"),
                    new StsFloatFieldBean(StsWell.class, "botZ", false, "Max Depth:"),
                    new StsDoubleFieldBean(StsWell.class, "originalXOrigin", false, "X Origin:"),
                    new StsDoubleFieldBean(StsWell.class, "originalYOrigin", false, "Y Origin:"),
                    new StsStringFieldBean(StsWell.class, "operator", false, "Operator:"),
                    new StsStringFieldBean(StsWell.class, "field", false, "Field:"),
                    new StsStringFieldBean(StsWell.class, "area", false, "Area:"),
                    new StsStringFieldBean(StsWell.class, "state", false, "State:"),
                    new StsStringFieldBean(StsWell.class, "county", false, "County:"),
                    new StsStringFieldBean(StsWell.class, "wellNumber", false, "Well Number:"),
                    new StsStringFieldBean(StsWell.class, "wellLabel", false, "Well Label:"),
                    new StsStringFieldBean(StsWell.class, "api", false, "API Number:"),
                    new StsStringFieldBean(StsWell.class, "uwi", false, "UWI Number:"),
                    new StsStringFieldBean(StsWell.class, "date", false, "Date:"),
                    new StsStringFieldBean(StsWell.class, "company", false, "Company:"),
                    new StsFloatFieldBean(StsWell.class, "kbElev", false, "Kelly Elevation:"),
                    new StsFloatFieldBean(StsWell.class, "elev", false, "Elevation:"),
                    new StsStringFieldBean(StsWell.class, "elevDatum", false, "Elevation Datum:"),
                    new StsStringFieldBean(StsWell.class, "nativeHorizontalUnitsString", false, "Native Horizontal Units:"),
                    new StsStringFieldBean(StsWell.class, "nativeVerticalUnitsString", false, "Native Vertical Units:"),
                    new StsStringFieldBean(StsWell.class, "spudDateString", false, "Spud Date:"),
                    new StsStringFieldBean(StsWell.class, "completionDateString", false, "Completion Date:"),
                    new StsStringFieldBean(StsWell.class, "permitDateString", false, "Permit Date:")
                };
        }
        return propertyFields;
    }

    public double getOriginalXOrigin()
    {
        return xOrigin;
    }

    public double getOriginalYOrigin()
    {
        return yOrigin;
    }

    public void reconfigureCurtainOffsetBeans()
    {
        if (curtainOffsetBean != null)
            curtainOffsetBean.setRangeFixStep(-1000.0, 1000.0, getWellClass().getCurtainStep());
    }

    public Object[] getChildren()
    {
        return new Object[0];
    }

    public boolean anyDependencies()
    {
        return false;
    }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null)
        {
            objectPanel = StsObjectPanel.constructor(this, true);
        }
        return objectPanel;
    }

    public void treeObjectSelected()
    {
        getWellClass().selected(this);
    }

    static public StsWellClass getWellClass()
    {
        return (StsWellClass)currentModel.getCreateStsClass(StsWell.class);
    }

    public boolean projectRotationAngleChanged()
    {
        return checkComputeRelativePoints();
    }

    public boolean checkComputeRelativePoints()
    {
		if (getLineVectorSet() == null) return false;
		if(!getLineVectorSet().checkComputeRelativePoints(this)) return false;
		return setMarkerLocations();
    }

    public boolean setMarkerLocations()
    {
		try
		{
        	if (markers != null) markers.forEach(StsWellMarker.class, "setLocation");
			return true;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "setMarkerLocations");
			return false;
		}
    }

    public void display2d(StsGLPanel3d glPanel3d, boolean displayName, int dirNo,
                          float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed,
                          boolean displayIn2d, boolean displayAllMarkers,
                          boolean displayPerfMarkers, boolean displayFmiMarkers)
    {
        if (!currentModel.getProject().canDisplayZDomain(getZDomainSupported()))
        {
            return;
        }
        if (glPanel3d == null)
        {
            return;
        }
        super.display2d(glPanel3d, displayName, dirNo, dirCoordinate, axesFlipped, xAxisReversed, yAxisReversed);

        if ((markers != null) && (displayIn2d))
        {
            int nMarkers = markers.getSize();
            int nPerfs = 0;
            for (int n = 0; n < nMarkers; n++)
            {
                StsWellMarker marker = (StsWellMarker) markers.getElement(n);
                boolean drawDifferent = marker.getMarker().getModelSurface() != null;
                if (marker instanceof StsPerforationMarker)
                {
                    if ((displayPerfMarkers) && (drawPerfMarkers))
                        ((StsPerforationMarker) marker).display2d(glPanel3d, dirNo, displayName, StsWell.getWellClass().getDefaultColor(nPerfs) , 1.0f);
                    nPerfs++;
                }
                else if (marker instanceof StsEquipmentMarker)
                {
                    if (drawEquipmentMarkers)
                        ((StsEquipmentMarker) marker).display2d(glPanel3d, dirNo, displayName, drawDifferent);
                }
                else if (marker instanceof StsFMIMarker)
                {
                    if (displayFmiMarkers)
                        ((StsFMIMarker) marker).display2d(glPanel3d, dirNo, displayName);
                }
                else
                {
                    if ((displayAllMarkers) && (drawMarkers))
                        marker.display2d(glPanel3d, dirNo, displayName, drawDifferent);
                }
            }
        }
    }

    public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean displayAllMarkers,
						boolean displayPerfMarkers, boolean displayFmiMarkers, DecimalFormat labelFormat)
    {
        if (!currentModel.getProject().canDisplayZDomain(getZDomainSupported())) return;
        if (glPanel3d == null) return;

        if (isDrawingCurtain)
        {
            glPanel3d.setViewShift(glPanel3d.getGL(), 2.0f);
            if (displayName)
                super.display(glPanel3d, true, getName());
            else
                super.display(glPanel3d, true, null);
        }
        else
        {
            if (displayName)
                super.display(glPanel3d, getName());
            else
                super.display(glPanel3d, null);
        }
        if (isDrawingCurtain)
        {
            glPanel3d.resetViewShift(glPanel3d.getGL());
        }
        GL gl = glPanel3d.getGL();

        if (markers != null)
        {
            int nMarkers = markers.getSize();
            int nPerfs = 0;
            for (int n = 0; n < nMarkers; n++)
            {
                StsWellMarker marker = (StsWellMarker) markers.getElement(n);
				//if(getCurrentProject().getTimeEnabled() && marker.getMDepth() > this.mdepthAtMaxClockTime)  continue;
                boolean drawDifferent = marker.getMarker().getModelSurface() != null;
                if (marker instanceof StsPerforationMarker)
                {
                    if ((displayPerfMarkers) && (drawPerfMarkers))
                        marker.display(glPanel3d, displayName, isDrawingCurtain, StsWell.getWellClass().getDefaultColor(nPerfs));
                    nPerfs++;
                }
                else if (marker instanceof StsEquipmentMarker)
                {
                    if (drawEquipmentMarkers)
                        marker.display(glPanel3d, displayName, isDrawingCurtain, drawDifferent);
                }
                else if (marker instanceof StsFMIMarker)
                {
                    if (displayFmiMarkers)
                        marker.display(glPanel3d, displayName, isDrawingCurtain, drawDifferent);
                }
                else
                {
                    if ((displayAllMarkers) && (drawMarkers))
                        marker.display(glPanel3d, displayName, isDrawingCurtain, drawDifferent);
                }
            }
        }

        logDisplay3d(leftDisplayLogCurves, -1f, glPanel3d);
        logDisplay3d(rightDisplayLogCurves, 0f, glPanel3d);

        if (!drawLabelString.equalsIgnoreCase(NO_LABEL) && (labelInterval >= 0.0f))
        {
            StsPoint point = null;
            float md = 0.0f;
            String label = null;
            int nLabels = (int) (getMaxMDepth() / labelInterval);

            if (isDrawingCurtain)
            {
                StsColor.BLACK.setGLColor(gl);
                glPanel3d.setViewShift(gl, 10.0f);
            }
            else
            {
                stsColor.setGLColor(gl);
                glPanel3d.setViewShift(gl, 1.0f);
            }

            GLBitmapFont font = GLHelvetica10BitmapFont.getInstance(gl);
            int numChars = font.getNumChars();
            for (int i = 0; i < nLabels; i++, md += labelInterval)
            {
                point = getPointAtMDepth((float) (i * labelInterval), true);
                float[] xyz = point.getXYZorT();
                if ((md % (5.0f * labelInterval)) != 0.0f)
                {
                    StsGLDraw.drawPoint(xyz, null, glPanel3d, 5, 1, 0.0f);
                }
                else
                {
                    StsGLDraw.drawPoint(xyz, null, glPanel3d, 10, 2, 0.0f);
                    float value = 0.0f;
                    if (drawLabelString == MDEPTH_LABEL)
                    {
                        value = md;
                        //                       label = Float.toString(md);
                    }
                    else if (drawLabelString == DEPTH_LABEL)
                    {
                        value = point.getZ();
                        //                        label = Float.toString(point.getZ());
                    }
                    else if (drawLabelString == TIME_LABEL)
                    {
                        value = point.getT();
                        //                       label = Float.toString(point.getT());
                    }
                    label = labelFormat.format(value);
                    StsGLDraw.fontOutput(gl, xyz, label, font);
                }
            }
            glPanel3d.resetViewShift(gl);
        }
		displaySeismicCurtain(glPanel3d);
	}

	public boolean displayTexture(StsGLPanel3d glPanel3d, long time)
	{
 //       displaySeismicCurtain(glPanel3d);
		return true;
    }

	public boolean textureChanged()
	{
		if(seismicCurtain == null) return false;
		return seismicCurtain.textureChanged();
	}

    //TODO this is called on db reload, so we don't want to call dbFieldChanged every time; persist as a property?
    public void setLogTypeDisplay3dLeft(String logTypeName)
    {
         leftDisplayLogCurves = getAllLogCurvesOfType(logTypeName);
         // dbFieldChanged("leftDisplayLogCurves", leftDisplayLogCurves);
    }

    //TODO this is called on db reload, so we don't want to call dbFieldChanged every time; persist as a property?
    public void setLogTypeDisplay3dRight(String logTypeName)
    {
         rightDisplayLogCurves = getAllLogCurvesOfType(logTypeName);
    //     dbFieldChanged("rightDisplayLogCurves", rightDisplayLogCurves);
    }

	
    public void logDisplay3dLeft()
    {
        StsObject[] logCurveObjects = logCurves.getElements();
        Object[] selectedLogCurveObjects = StsListSelectionDialog.getMultiSelectFromListDialog(currentModel.win3d,  "Log Selection", "Select left side log(s)", logCurveObjects);
        if(selectedLogCurveObjects.length == 0) return;
        leftDisplayLogCurves = (StsLogCurve[])StsMath.arrayCastCopy(selectedLogCurveObjects, StsLogCurve.class);
        dbFieldChanged("leftDisplayLogCurves", leftDisplayLogCurves);
        currentModel.viewObjectRepaint(this, this);
    }

    public void logDisplay3dRight()
    {
        StsObject[] logCurveObjects = logCurves.getElements();
        Object[] selectedLogCurveObjects = StsListSelectionDialog.getMultiSelectFromListDialog(currentModel.win3d,  "Log Selection", "Select right side log(s)", logCurveObjects);
        if(selectedLogCurveObjects == null) return;
        if(selectedLogCurveObjects.length == 0) return;
        rightDisplayLogCurves = (StsLogCurve[])StsMath.arrayCastCopy(selectedLogCurveObjects, StsLogCurve.class);
        dbFieldChanged("rightDisplayLogCurves", rightDisplayLogCurves);
        currentModel.viewObjectRepaint(this, this);
    }

    public void logDisplay3d(StsLogCurve[] logCurves, float origin, StsGLPanel3d glPanel3d)
    {
        if (logCurves == null)return;
        for(StsLogCurve logCurve : logCurves)
            logCurve.display3d(glPanel3d, this, origin);
    }

    public void displaySeismicCurtain(StsGLPanel3d glPanel3d)
    {
        if(!getDrawCurtain()) return;
        if ((seismicCurtain != null) && (glPanel3d != null))
        {
            seismicCurtain.displayTexture3d(glPanel3d, drawCurtainTransparent);
            seismicCurtain.display(glPanel3d);
        }
    }

    public void createDisplaySeismicCurtain(StsGLPanel3d glPanel3d)
    {
        if(seismicCurtain == null)
        {
            StsSeismicVolume seismicVolume = (StsSeismicVolume)currentModel.getCurrentObject(StsSeismicVolume.class);
            if(seismicVolume == null) return;
            seismicCurtain = this.getCreateSeismicCurtain(seismicVolume);
        }
        displaySeismicCurtain(glPanel3d);
    }

    /** remove a well from the instance list and in the 3d window */
    public boolean delete()
    {
        // Remove well from any platforms it is assigned to
		/*
        StsPlatformClass pc = (StsPlatformClass) currentModel.getStsClass(StsPlatform.class);
        if (pc != null)
            pc.deleteWellFromPlatform(getName());

        if (!super.delete())
        {
            return false;
        }
        */
        StsObjectRefList.deleteAll(logCurves);
        StsObjectRefList.deleteAll(lithZones);
        StsObjectRefList.deleteAll(markers);
		/*
        StsObject editTdSet = currentModel.getObjectWithName(StsEditTdSet.class, getName());
        if (editTdSet != null)
        {
            editTdSet.delete();
        }
        */
        getWellClass().delete(this);
        getCurrentProject().removedUnrotatedBox(this);
		if(wellViewModel != null) wellViewModel.closeWindow();
        return true;
    }

    public void deleteMarker(StsWellMarker wellMarker)
    {
        markers.delete(wellMarker);
    }

    public boolean xyRangeOk(StsAbstractFloatVector xVector, StsAbstractFloatVector yVector)
    {
        double[] lineRange;
        double[] boxRange;

        StsProject project = getCurrentProject();
        StsBoundingBox projectUnrotatedBoundingBox = project.getUnrotatedBoundingBox();
        if(!project.isBoxesInitialized()) return true;
        double limit = project.getDistanceLimit();
        lineRange = xVector.getAbsoluteRange();
        boxRange = projectUnrotatedBoundingBox.getAbsoluteXRange();
        if(!rangeOutsideLimit(lineRange, boxRange, limit)) return false;
        lineRange = yVector.getAbsoluteRange();
        boxRange = projectUnrotatedBoundingBox.getAbsoluteYRange();
        if(!rangeOutsideLimit(lineRange, boxRange, limit)) return false;

        return true;
    }

    private boolean rangeOutsideLimit(double[] lineRange, double[] boxRange, double limit)
    {
        if(boxRange[0] - lineRange[0] > limit) return false;
        if(lineRange[1] - boxRange[1] > limit) return false;
        return true;
    }

    private boolean rangeOutsideLimit(float[] lineRange, float[] boxRange, double limit)
    {
        if(boxRange[0] - lineRange[0] > limit) return false;
        if(lineRange[1] - boxRange[1] > limit) return false;
        return true;
    }

	public void checkSetZDomainSupported(byte zDomain)
    {
        if (getZDomainSupported() == StsProject.TD_TIME_DEPTH) return;
        setZDomainSupported(zDomain);
        dbFieldChanged("zDomainSupported", zDomain);
    }

    public boolean isInTime()
    {
        return getZDomainSupported() == TD_TIME_DEPTH;
    }
/*
    public void saveVertexTimesToDB(float[] times)
    {
        currentModel.addMethodCmd(this, "updateVertexTimes", new Object[]{times});
    }

    public void updateVertexTimes(float[] times)
    {
        int nVertices = lineVectorSet.getSize();
        for (int n = 0; n < nVertices; n++)
        {
            StsSurfaceVertex vertex = (StsSurfaceVertex) lineVectorSet.getElement(n);
            StsPoint point = vertex.getPoint();
            point.setT(times[n]);
        }
        computeXYZPoints();
    }
*/
    public void computeMarkerTimesFromMDepth(StsSeismicVelocityModel velocityModel)
    {
        if (markers == null) return;
        StsProject project = currentModel.getProject();
        int nMarkers = markers.getSize();
        for (int n = 0; n < nMarkers; n++)
        {
            StsWellMarker wellMarker = (StsWellMarker) markers.getElement(n);
            computeMarkerTimeFromMDepth(wellMarker, velocityModel);
        }
    }

    private boolean computeMarkerTimeFromMDepth(StsWellMarker wellMarker, StsSeismicVelocityModel velocityModel)
    {
        try
        {
            StsPoint location = wellMarker.getLocation();
            location.setT(getTimeFromMDepth(location.getM()));
            // wellMarker.dbFieldChanged("location", location);
            return true;
        }
        catch (Exception e)
        {
            return false;
    	}
    }
/*
    public boolean constructLogCurvesCheckVersions(StsDataVector[] dataVectors, float curveNullValue)
    {
        try
        {
            if (dataVectors == null)
            {
                return false;
            }
            int nLogVectors = dataVectors.length;
            if (nLogVectors == 0)
            {
                return false;
            }

            // sort the dataVectors by version
            ArrayList objects = new ArrayList(nLogVectors);
            for (int n = 0; n < nLogVectors; n++)
            {
                objects.add(dataVectors[n]);

            }
            Comparator comparator = new VersionComparator();
            Collections.sort(objects, comparator);

            Iterator iter = objects.iterator();
            int currentVersion = StsParameters.nullInteger;
            int nVersionVectors = 0;
            while (iter.hasNext())
            {
                StsDataVector dataVector = (StsDataVector) iter.next();
                int version = dataVector.getVersion();
                if (version == currentVersion)
                {
                    dataVectors[nVersionVectors++] = dataVector;
                }
                else
                {
                    if (nVersionVectors > 0)
                    {
                        dataVectors = (StsDataVector[]) StsMath.trimArray(dataVectors, nVersionVectors);
                        StsLogCurve[] logCurves = StsLogCurve.processVectors(dataVectors, curveNullValue, version, well);
                        addLogCurves(logCurves);
                        nVersionVectors = 0;
                    }
                    currentVersion = version;
                    dataVectors = new StsDataVector[nLogVectors];
                    dataVectors[nVersionVectors++] = dataVector;
                }
            }
            if (nVersionVectors > 0)
            {
                dataVectors = (StsDataVector[]) StsMath.trimArray(dataVectors, nVersionVectors);
                int version = dataVectors[0].getVersion();
                StsLogCurve[] logCurves = StsLogCurve.processVectors(dataVectors, curveNullValue, version, well);
                addLogCurves(logCurves);
            }
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsLogCurve.constructWellDevCurvesCheckVersions() failed.",
                e, StsException.WARNING);
            return false;
        }
    }
*/
    static public final class VersionComparator implements Comparator
    {
        VersionComparator()
        {
        }

        // order by versions and then order alphabetically
        public int compare(Object o1, Object o2)
        {
            StsFloatDataVector v1 = (StsFloatDataVector) o1;
            if (v1 == null)
            {
                return -1;
            }
            StsFloatDataVector v2 = (StsFloatDataVector) o2;
            if (v2 == null)
            {
                return 1;
            }

            int vv1 = v1.getVersion();
            int vv2 = v2.getVersion();

            // compa
            if (vv1 > vv2)
            {
                return 1;
            }
            if (vv1 < vv2)
            {
                return -1;
            }

            String s1 = v1.getName();
            String s2 = v2.getName();
            return s1.compareTo(s2);
        }
    }

	public boolean addVectorSetToObject(StsLogVectorSet logVectorSet)
	{
		if(!logVectorSet.checkAddVectors(false)) return false;
		logVectorSet.checkBuildTimeVector(getLineVectorSet());
		logVectorSet.addToModel();
		addLogCurveVectorSet(logVectorSet);
		// getLineVectorSet().checkComputeXYLogVectors(logVectorSet);
		return true;
	}

	public boolean addVectorSetToObject(StsTdVectorSet vectorSet)
	{
		if(!vectorSet.checkAddVectors(false)) return false;
		checkBuildTimeVector(vectorSet);
		vectorSet.addToModel();
		addLogCurveVectorSet(vectorSet);
		return true;
	}

	private void addLogCurveVectorSet(StsLogVectorSet vectorSet)
	{
		if(logCurveVectorSets == null)
			logCurveVectorSets = StsObjectRefList.constructor(1, 1, "logCurveVectorSets", this, true);
		logCurveVectorSets.add(vectorSet);
	}

	public boolean hasLogCurveVectorSet(String name, int version)
	{
		if(logCurveVectorSets == null) return false;
		for(Object object : logCurveVectorSets)
			if(((StsLogVectorSet)object).isNameAndVersion(name, version)) return true;
		return false;
	}

    public void addLogCurves(StsLogCurve[] logCurves)
    {
		// checkAddMDepthToDev(logCurves[0]);
        for (int n = 0; n < logCurves.length; n++)
            addLogCurve(logCurves[n]);
    }

    public void addLogCurves(ArrayList<StsLogCurve> logCurves)
    {
		addLogCurves(logCurves.toArray(new StsLogCurve[0]));
    }

    public void addLogCurve(StsLogCurve logCurve)
    {
        if (logCurve == null) return;
        if (logCurves == null)
            logCurves = StsObjectRefList.constructor(10, 1, "logCurves", this);
        logCurve.setWell(this);
//         logCurve.checkAddMDepth(this);
        logCurves.add(logCurve);
    }

    public StsObjectRefList getLogCurves()
    {
        if (logCurves == null)
        {
            logCurves = StsObjectRefList.constructor(2, 2, "logCurves", this);
        }
        return logCurves;
    }

    public void deleteLogCurve(StsLogCurve logCurve)
    {
        logCurves.delete(logCurve);
        logCurve.setWell(null);
    }

    public StsLogCurve[] getUnusedLogCurves(StsLogCurve[] usedLogCurves)
    {
        StsLogCurve[] unusedLogCurves = null;

        int nLogCurves = logCurves.getSize();
        if (usedLogCurves == null || usedLogCurves.length == 0)
        {
            unusedLogCurves = new StsLogCurve[nLogCurves];
            for (int n = 0; n < nLogCurves; n++)
            {
                unusedLogCurves[n] = (StsLogCurve) logCurves.getElement(n);
            }
            return unusedLogCurves;
        }

        int nUsedLogCurves = usedLogCurves.length;
        int nUnusedLogCurves = nLogCurves - nUsedLogCurves;
        unusedLogCurves = new StsLogCurve[nUnusedLogCurves];
        int nn = 0;
        for (int n = 0; n < nLogCurves; n++)
        {
            StsLogCurve logCurve = (StsLogCurve) logCurves.getElement(n);
            if (logCurve.isInList(usedLogCurves))
            {
                continue;
            }
            unusedLogCurves[nn++] = logCurve;
        }
        return unusedLogCurves;
    }

    public int getNLogCurves()
    {
        return (logCurves == null) ? 0 : logCurves.getSize();
    }

    public StsLogCurve[] copyLogCurveArray()
    {
        if (logCurves == null)
            return new StsLogCurve[0];
        return (StsLogCurve[]) logCurves.copyArrayList(StsLogCurve.class);
    }

    /*
     int nCurves = getNLogCurves();
     if (nCurves==0) return new StsLogCurve[0];
     StsLogCurve[] curves = new StsLogCurve[nCurves];
     for (int i=0; i<nCurves; i++)
     {
      curves[i] = (StsLogCurve)logCurves.getElement(i);
     }
     return curves;
    }
    */
    public StsLogCurve[] getLogCurveList()
    {
        return copyLogCurveArray();
    }

    static public void printLogCurves(StsWell well) throws StsException
    {
        printLogCurves(well, 50);
    }

    /** print out log curve vector */
    static public void printLogCurves(StsWell well, int increment)
    {
        StsLogCurve[] curves = null;
        try
        {
            curves = well.copyLogCurveArray();
        }
        catch (Exception e)
        {
            System.out.println("No log curves found.");
            return;
        }

        StringBuffer buffer = new StringBuffer("Well: " + well.getName() + "\n");
        buffer.append(" \n");
        if (curves == null)
        {
            buffer.append("No log curves found.\n");
        }
        else
        {
            String depthFmtPattern = "00000.00";
            String valueFmtPattern = "00000.0000";
            StsDecimalFormat depthFormat = new StsDecimalFormat(depthFmtPattern);
            StsDecimalFormat valueFormat = new StsDecimalFormat();
            for (int i = 0; i < curves.length; i++)
            {
                try
                {
                    String mdepthName = curves[i].getMDepthVector().getName();
                    String depthName = curves[i].getDepthVector().getName();
                    String curveName = curves[i].getName();
                    if (curveName.equals(mdepthName) || curveName.equals(depthName))
                        valueFormat.applyPattern(depthFmtPattern);
                    else
                        valueFormat.applyPattern(valueFmtPattern);
                    int nValues = curves[i].getValueFloatVector().getSize();
                    buffer.append("Curve: " + curveName);
                    buffer.append("\nNumber of vector: " + nValues + "\n \n");
                    buffer.append("index\t" + mdepthName + "\t\t" + depthName + "\t\t" + curveName + "\n");
                    StsAbstractFloatVector mdepthValues = curves[i].getMDepthFloatVector();
                    StsAbstractFloatVector depthValues = curves[i].getDepthFloatVector();
                    StsAbstractFloatVector curveValues = curves[i].getValueFloatVector();
                    for (int j = 0; j < nValues; j += increment)
                    {
                        String mdepth = depthFormat.stripLeadingZeroes(mdepthValues.getElement(j));
                        String depth = depthFormat.stripLeadingZeroes(depthValues.getElement(j));
                        String value = valueFormat.stripLeadingZeroes(curveValues.getElement(j));
                        buffer.append(j + "\t" + mdepth + "\t" + depth + "\t" + value + "\n");
                    }
                    // print last value
                    if (nValues - 1 % increment != 0)
                    {
                        String mdepth = depthFormat.stripLeadingZeroes(mdepthValues.getElement(nValues - 1));
                        String depth = depthFormat.stripLeadingZeroes(depthValues.getElement(nValues - 1));
                        String value = valueFormat.stripLeadingZeroes(curveValues.getElement(nValues - 1));
                        buffer.append((nValues - 1) + "\t" + mdepth + "\t" + depth + "\t" + value + "\n");
                    }
                    buffer.append(" \n");
                }
                catch (Exception e)
                {
                    buffer.append("\nError in curve #" + i + ".  Continuing...\n");
                    continue;
                }
            }
        }

        // print out depth and curve names
        PrintWriter out = new PrintWriter(System.out, true); // needed for correct formatting
        out.println(buffer.toString());

        // display dialog box
        StsTextAreaDialog dialog = new StsTextAreaDialog(null, "Log Curve Listing for " + well.getName(),
            buffer.toString(), 40, 60, false);
        dialog.setLocationRelativeTo(getCurrentModel().win3d);
        dialog.setVisible(true);
    }

    public boolean hasLogCurves() { return logCurves != null && logCurves.getSize() > 0; }

    /** find a log curve in the list */
    public StsLogCurve getLastLogCurveOfType(String name)
    {
        if (name == null)
        {
            return null;
        }
        if (name.equalsIgnoreCase("none")) return null;
        if (logCurves == null)
        {
            return null;
        }
        //if(name.equalsIgnoreCase(StsWellClass.LOG_TYPE_NONE)) // Have to check here since "None" is the default alias
        //	return null;

        int nCurves = logCurves.getSize();
        for (int i = nCurves-1; i >= 0; i--)
        {
            StsLogCurve curve = (StsLogCurve) logCurves.getElement(i);
            if (curve.matchesName(name)) return curve;
        }
        return null;
    }

    public StsLogCurve[] getAllLogCurvesOfType(String curveTypeName)
    {
        if (curveTypeName == null)
        {
            return null;
        }
        if (curveTypeName.equalsIgnoreCase("none")) return null;
        if (logCurves == null)
        {
            return null;
        }
        //if(curveTypeName.equalsIgnoreCase(StsWellClass.LOG_TYPE_NONE)) // Have to check here since "None" is the default alias
        //	return null;

        int nCurves = logCurves.getSize();
        StsLogCurve[] matchingCurves = new StsLogCurve[0];
        for (int i = 0; i < nCurves; i++)
        {
            StsLogCurve curve = (StsLogCurve) logCurves.getElement(i);
            if (curve.logCurveTypeNameMatches(curveTypeName))
                matchingCurves = (StsLogCurve[])StsMath.arrayAddElement(matchingCurves, curve);

        }
        return matchingCurves;
    }

    public StsLogCurve getTdCurve()
    {
        return getLastLogCurveOfType(StsLoader.SEISMIC_TIME);
    }

    public StsTdVectorSet getTdVectorSet()
    {
        StsLogCurve tdCurve = getLastLogCurveOfType(StsLoader.SEISMIC_TIME);
        if(tdCurve == null) return null;
        return (StsTdVectorSet)tdCurve.logVectorSet;
    }
    // Zone routines

    public void checkConstructWellZones()
    {
		StsList zones = StsLineSections.getZoneList(this);
		if (zones != null)return;

        StsClass modelZones = currentModel.getCreateStsClass(StsZone.class); // these are zones in top to bottom order
        int nZones = zones.getSize();
        zones = new StsList(nZones, 1);
        for (int z = 0; z < nZones; z++)
        {
            StsZone zone = (StsZone) modelZones.getElement(z);
            StsWellZone wellZone = constructStratZone(zone);
            if (wellZone != null)
            {
                zones.add(wellZone);
            }
        }
		StsLineSections.setZoneList(this, zones);
    }

    private StsWellZone constructStratZone(StsZone zone)
    {
        StsModelSurface topHorizon = zone.getTopModelSurface();
        String topMarkerName = topHorizon.getMarkerName();
        StsWellMarker topMarker = getMarker(topMarkerName, StsParameters.STRAT);
        if (topMarker == null)
        {
            topMarker = StsWellMarker.constructor(topHorizon, this);
        }
        if (topMarker == null)
        {
            return null;
        }

        StsModelSurface baseHorizon = zone.getBaseModelSurface();
        String baseMarkerName = baseHorizon.getMarkerName();
        StsWellMarker baseMarker = getMarker(baseMarkerName, StsParameters.STRAT);
        if (baseMarker == null)
        {
            baseMarker = StsWellMarker.constructor(baseHorizon, this);
        }
        if (baseMarker == null)
        {
            return null;
        }

        return StsWellZone.constructor(this, StsParameters.STRAT, topMarkerName, topMarker, baseMarker, zone);
    }

    public StsWellMarker getMarker(String markerName, int type)
    {
        if (markers == null)
        {
            return null;
        }
        int nMarkers = markers.getSize();
        for (int n = 0; n < nMarkers; n++)
        {
            StsWellMarker marker = (StsWellMarker) markers.getElement(n);
            if (marker.getType() == type && marker.getName() == markerName)
            {
                return marker;
            }
        }
        return null;
    }

    public void setStsColor(StsColor color)
    {
        super.setStsColor(color);
        currentModel.viewObjectRepaint(this, this);
    }
    public float getDepthFromMDepth(float mdepth)
    {
        return getDepthFromMDepth(mdepth, false);
    }

    public float getTimeFromMDepth(float mdepth)
    {
        try
        {
            StsSeismicVelocityModel velocityModel = currentModel.getProject().getSeismicVelocityModel();
            if(velocityModel != null) return StsParameters.nullValue;
            float[] xyzmt = getLineVectorSet().getCoordinatesAtMDepth(mdepth, true);
			return xyzmt[4];
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "getTimeFromMDepth", e);
            return StsParameters.nullValue;
        }
    }

    public float getDepthFromTime(float time)
    {
        return getLineVectorSet().getDepthFromTime(time, true);
    }

    // Not reliable for horizontal well
    public StsPoint getPointFromDepth(float depth, boolean extrapolate)
    {
        return getLineVectorSet().getPointAtDepth(depth, extrapolate);
    }

    public StsPoint getPointFromDepth(float depth)
    {
        return getPointFromDepth(depth, true);
    }

    public void setIsVisible(boolean b)
    {
        if(!setIsVisibleChanged(b)) return;
        dbFieldChanged("isVisible", isVisible());
        currentModel.win3dDisplayAll();
    }

    /** get measured depth from tvd by linear interpolation of tvd */
    public float getMDepthFromDepth(float depth)
    {
        return getLineVectorSet().getMDepthFromDepth(depth, true);
    }

    /** get measured depth from tvd by linear interpolation of tvd */
    public float getMDepthFromTime(float time)
    {
        return getLineVectorSet().getMDepthFromTime(time, true);
    }

    /** get interpolated value for one array given another */
    static public float getInterpolatedValue(float av, float[] a, float[] b)
    {
        if (a == null || b == null || a.length != b.length)
        {
            return nullValue;
        }
        return StsMath.interpolateValue(av, a, b, a.length);
    }
	
    public float[] getDepthsFromMDepths(float[] mdepths)
    {
        try
        {
            int nValues = mdepths.length;
            float[] depthValues = new float[nValues];
            for (int i = 0; i < nValues; i++)
                depthValues[i] = getDepthFromMDepth(mdepths[i]);
            return depthValues;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getDepthsFromMDepths", e);
            return null;
        }
    }
	
    public StsFloatDataVector getDepthVectorFromMDepthVector(StsAbstractFloatVector mdepths)
    {
        try
        {
            float[] mdepthValues = mdepths.getValues();
            int nValues = mdepthValues.length;
            float[] depthValues = new float[nValues];
            for (int i = 0; i < nValues; i++)
                depthValues[i] = getDepthFromMDepth(mdepthValues[i]);
            StsFloatDataVector depthVector = new StsFloatDataVector(depthValues);
            return depthVector;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getDepthsFromMDepths", e);
            return null;
        }
    }

    public double[] getMDepthsFromTimes(float tMin, float tInc, int nValues)
    {
        try
        {
            double[] mdepthValues = new double[nValues];
            float time = tMin;
            for (int n = 0; n < nValues; n++, time += tInc)
                mdepthValues[n] = getMDepthFromTime(time);
            return mdepthValues;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWelLine.getMDepthsFromTimes() failed.", e, StsException.WARNING);
            return null;
        }
    }

    public double[] getMDepthsFromDepths(float zMin, float zInc, int nValues)
    {
        try
        {
            double[] mdepthValues = new double[nValues];
            float z = zMin;
            for (int n = 0; n < nValues; n++, z += zInc)
                mdepthValues[n] = getMDepthFromDepth(z);
            return mdepthValues;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWelLine.getMDepthsFromTimes() failed.", e, StsException.WARNING);
            return null;
        }
    }

    public StsPoint getPointFromLocation(float x, float y, float depth)
    {
        StsPoint[] points = getAsCoorPoints();
        StsPoint point = StsMath.getNearestPointOnLine(new StsPoint(x, y, depth), points, 3);
        return point;
    }

	/*
	 public boolean checkAddMDepthToDev(StsLogCurve logCurve)
	 {
		 if(hasMDepths) return false;
		 StsAbstractFloatVector mDepthVector = logCurve.getMDepthVector();
		 StsAbstractFloatVector depthVector = logCurve.getDepthVector();
		 return checkAddMDepthVector(mDepthVector, depthVector);
	 }
 */

	public boolean checkAddMDepthVector(StsAbstractFloatVector mDepthVector, StsAbstractFloatVector depthVector)
    {
        StsPoint[] points = getLineVectorSet().getCoorsAsPoints();
        int nValues = getLineVectorSet().getVectorsSize();
        float vScalar = currentModel.getProject().getDepthScalar(mDepthVector.getUnits());
        float[] curveMDepths = mDepthVector.getValues();
        float[] curveDepths = depthVector.getValues();
        for (int j = 0; j < curveMDepths.length; j++)
        {
            curveMDepths[j] = curveMDepths[j] * vScalar;
            curveDepths[j] = curveDepths[j] * vScalar;
        }
        float[] newMDepths = new float[nValues];
        float mDepthError = 0.0f;

        // get the measured offset adjustment above and below the logged interval
        float curveDepthMin = curveDepths[0];
        float devMDepth = getMDepthFromDepth(curveDepthMin);
        float aboveOffset = (curveMDepths[0]) - devMDepth;
        int n = 0;
        // adjust dev mDepths above the first logCurve depth
        for (; n < nValues; n++)
        {
            float devDepth = points[n].getZ();
            if (devDepth >= curveDepthMin)
            {
                break;
            }
            devMDepth = points[n].getM();
            newMDepths[n] = devMDepth + aboveOffset;
        }
        int nLastCurveValue = curveMDepths.length - 1;
        float curveDepthMax = curveDepths[nLastCurveValue];
        for (; n < nValues; n++)
        {
            float devDepth = points[n].getZ();
            if (devDepth > curveDepthMax)
            {
                break;
            }
            newMDepths[n] = StsMath.interpolateValue(points[n].getZ(), curveDepths, curveMDepths);
 //           if (hasMDepths)
            {
                mDepthError = Math.max(mDepthError, (Math.abs(newMDepths[n] - points[n].getM())));
            }
        }
        devMDepth = getMDepthFromDepth(curveDepthMax);
        float belowOffset = curveMDepths[nLastCurveValue] - devMDepth;
        for (; n < nValues; n++)
        {
            devMDepth = points[n].getM();
            newMDepths[n] = devMDepth + belowOffset;
        }
        if (mDepthError > maxMDepthError)
        {
            boolean answer = StsYesNoDialog.questionValue(currentModel.win3d, "Measured depth vector differ between " +
                "deviation survey files and log curve files: error " + mDepthError + " max allowed " + maxMDepthError);
            if (answer == false)
            {
                return false;
            }
        }
        for (n = 0; n < nValues; n++)
        {
            points[n].setM(newMDepths[n]);
        }
        return true;
    }

	public void addZone(StsWellZone zone)
	{
		if (zone.getZoneType() == StsWellZoneSet.LITH)
		{
			if (lithZones == null)
			{
				lithZones = StsObjectRefList.constructor(10, 1, "lithZones", this);
			}
			lithZones.add(zone);
		}
		else
			StsLineSections.addZone(this, zone);
		//        setWellLineNeedsRebuild();
	}
    /* get an array of well zones that need display (doesn't handle fault zones) */
    public Iterator getDisplayZoneIterator(int type)
    {
        if (type == StsWellZoneSet.LITH && lithZones != null)
			return new DisplayZoneIterator(lithZones.getList());
		else if (type == StsWellZoneSet.STRAT)
		{
			StsList list = StsLineSections.getZoneList(this);
			return new DisplayZoneIterator(list);
		}
		else
			return null;
    }

    class DisplayZoneIterator implements Iterator
    {
        StsList zones;
        StsWellZone next = null;
        int n = 0;

        DisplayZoneIterator()
        {
        }

        DisplayZoneIterator(StsList zones)
        {
            this.zones = zones;
            setNext();
        }

        private void setNext()
        {
            if (zones == null)
            {
                return;
            }
        }

        public boolean hasNext()
        {
            return next != null;
        }

        public Object next()
        {
            StsWellZone current = next;
            while ((next = (StsWellZone) zones.getElement(n++)) != null)
            {
                if (next.getDisplayOnWellLine())
                {
                    break;
                }
            }
            return current;
        }

        public void remove()
        {}
    }

    public void setDrawMarkers(boolean b)
    {
        if (drawMarkers == b)
        {
            return;
        }
        drawMarkers = b;
        currentModel.win3dDisplay();
    }

    public boolean getDrawMarkers()
    {
        return drawMarkers;
    }

    public void setDrawPerfMarkers(boolean b)
    {
        if (drawPerfMarkers == b)
        {
            return;
        }
        drawPerfMarkers = b;
        currentModel.win3dDisplay();
    }

    public boolean getDrawPerfMarkers()
    {
        return drawPerfMarkers;
    }

    public void setDrawEquipmentMarkers(boolean b)
    {
        if (drawEquipmentMarkers == b)
        {
            return;
        }
        drawEquipmentMarkers = b;
        currentModel.win3dDisplay();
    }

    public boolean getDrawEquipmentMarkers()
    {
        return drawEquipmentMarkers;
    }

    public boolean getDrawCurtainTransparent()
    {
        return drawCurtainTransparent;
    }

    public void setDrawCurtainTransparent(boolean value)
    {
        drawCurtainTransparent = value;
        dbFieldChanged("drawCurtainTransparent", drawCurtainTransparent);
    }

    public boolean getDrawCurtain()
    {
        return isDrawingCurtain;
    }

    public void setDrawCurtain(boolean curtain)
    {
        if (isDrawingCurtain == curtain)
        {
            return;
        }
        isDrawingCurtain = curtain;
        dbFieldChanged("isDrawingCurtain", isDrawingCurtain);
        if (isDrawingCurtain)
            createCurtain();
        else
        {
            deleteSeismicCurtain();
            currentModel.win3dDisplay();
        }
    }

    public void createCurtain()
    {
        Runnable runCreateCurtain = new Runnable()
        {
            public void run()
            {
                createSeismicCurtain();
                currentModel.win3dDisplay();
            }
        };
        Thread runCreateCurtainThread = new Thread(runCreateCurtain);
        runCreateCurtainThread.start();
    }

    public String getDrawLabelString()
    {
        return drawLabelString;
    }

    public void setDrawLabelString(String labelString)
    {
        drawLabelString = labelString;
        currentModel.win3dDisplay();
        return;
    }

    public float getLabelInterval()
    {
        return labelInterval;
    }

    public void setLabelInterval(float value)
    {
        labelInterval = value;
        dbFieldChanged("labelInterval", labelInterval);
        currentModel.win3dDisplay();
        return;
    }

    /** marker methods */

    public void addModelSurfaceMarkers()
    {
        StsObject[] surfaces = currentModel.getObjectList(StsModelSurface.class);
        int nSurfaces = surfaces.length;
        for (int n = 0; n < nSurfaces; n++)
        {
            StsModelSurface surface = (StsModelSurface) surfaces[n];
            if (getMarker(surface.getName()) != null)
            {
                continue; // already have it
            }
            try
            {
                StsWellMarker.constructor(surface, this);
            }
            catch (Exception e)
            {}
        }
    }

    public void addMarker(StsWellMarker marker)
    {
        if (markers == null)
        {
            markers = StsObjectRefList.constructor(10, 1, "markers", this);
        }
        markers.add(marker);
    }

    public void addMarkers(StsWellMarker[] newMarkers)
    {
        if (markers == null)
            markers = StsObjectRefList.constructor(10, 1, "markers", this);
        markers.add(newMarkers);
    }

    public StsObjectRefList getMarkers()
    {
        return markers;
    }

    public boolean hasMarkers()
    {
        return getNMarkers() > 0;
    }

    public int getNMarkers()
    {
        return (markers == null) ? 0 : markers.getSize();
    }

    public StsWellMarker[] getMarkerArray()
    {
        int nMarkers = getNMarkers();
        if (nMarkers == 0)
        {
            return null;
        }
        StsWellMarker[] markerArray = new StsWellMarker[nMarkers];
        for (int i = 0; i < nMarkers; i++)
        {
            markerArray[i] = (StsWellMarker) markers.getElement(i);
        }
        return markerArray;
    }

    public String[] getMarkerList()
    {
        StsWellMarker[] markerArray = getMarkerArray();
        if (markerArray == null)
        {
            return null;
        }
        String[] markerList = new String[markerArray.length];
        for (int i = 0; i < markerArray.length; i++)
        {
            markerList[i] = markerArray[i].getName();
        }
        return markerList;
    }

    public StsWellMarker getMarker(String name)
    {
        if (name == null)
        {
            return null;
        }
        int nMarkers = getNMarkers();
        if (nMarkers == 0)
        {
            return null;
        }
        for (int i = 0; i < nMarkers; i++)
        {
            StsWellMarker marker = (StsWellMarker) markers.getElement(i);
            if (name.equals(marker.getName()))
            {
                return marker;
            }
        }
        return null;
    }

    public void adjustTimes(float[] adjustedTimes)
    {
        if (adjustedTimes == null)
        {
            return;
        }
        StsPoint[] lineVertexPoints = getLineVertexPoints();
        for (int n = 0; n < lineVertexPoints.length; n++)
        {
            lineVertexPoints[n].setT(adjustedTimes[n]);
        }
        checkComputeRelativePoints();
        adjustMarkerTimes();
    }

    public void adjustMarkerTimes()
    {
        if (markers == null) return;
        int nMarkers = markers.getSize();
        for (int n = 0; n < nMarkers; n++)
        {
            StsWellMarker marker = (StsWellMarker) markers.getElement(n);
            marker.adjustTime();
        }
    }

    /** Build a wellViewModel and a frameView if we don't have one. If we have a wellViewModel, but no frameView, built that. */
    public void openOrPopWindow()
    {
        if(wellViewModel == null)
            wellViewModel = new StsWellFrameViewModel(this);
        else if(wellViewModel.wellWindowFrame == null)
            wellViewModel.buildFrameView();
    }

    public void close()
    {
        wellViewModel.wellWindowFrame = null;
    }

    public void setWellFrameViewModel(StsWellFrameViewModel wellViewModel)
    {
        this.wellViewModel = wellViewModel;
    }

    public void pick(GL gl, StsGLPanel glPanel)
    {
        super.pick(gl, glPanel);
    }

    public void mouseSelectedEdit(StsMouse mouse)
    {
        logMessage();
        openOrPopWindow();
    }
/*
    public boolean initialize(StsModel model)
    {
		timeIndex = timeVector.getSize() -1;
        return initialize();
    }
*/
    /**
     * Initialize well even if on uninitialized section. Return true only if
     * not on section or section is initialized
     */
    public boolean initialize()
    {
        //        wellViewModel = null;
        if (initialized)return true;
        if(!initializeLine()) return false;
        if(!initializeSection()) return false;

        StsSeismicVelocityModel velocityModel = currentModel.getProject().getSeismicVelocityModel();
        if (velocityModel == null)
            initialized = checkBuildTimeVector();
        else
            initialized = checkAdjustFromVelocityModel();

        if(!initialized) return false;
        if (isDrawingCurtain)
            getCreateSeismicCurtain();

        return true;
    }

    public boolean checkAdjustFromVelocityModel()
    {
        StsSeismicVelocityModel velocityModel = currentModel.getProject().getSeismicVelocityModel();
        if (velocityModel != null)
            return adjustFromVelocityModel(velocityModel);
        // else if(this.hasTDCurve())
        {

        }

            return true;
    }

    public boolean checkBuildTimeVector()
    {
        StsTdVectorSet tdVectorSet = getTdVectorSet();
        if(tdVectorSet == null) return false;
        return getLineVectorSet().checkBuildTimeVectorFromTdCurve(tdVectorSet);
    }

	public boolean checkBuildTimeVector(StsTdVectorSet tdVectorSet)
	{
		if(getTVector() != null) return true;
		if(/* !getLineVectorSet().checkBuildTimeVectorFromVelocityModel() && */ !getLineVectorSet().checkBuildTimeVectorFromTdCurve(tdVectorSet))
			return false;
		setZDomainSupported(StsProject.TD_TIME_DEPTH);
		adjustBoundingBox();
		return true;
	}

	public boolean checkBuildTimeVector(StsLogVectorSet logVectorSet)
	{
		return logVectorSet.checkBuildTimeVector(getLineVectorSet());
	}

    public void logMessage()
    {
        logMessage("Well: " + this.getName() + " " + lineOnSectionLabel());
    }

    /*
    public float[] getVerticesTimes()
    {
     if(verticesTimes != null) return verticesTimes;
     StsLogCurve timeCurve = getLogCurve(TIME);
     float[] wellDepths = getVerticesDepths();
     int nValues = wellDepths.length;
     verticesTimes = new float[nValues];
     for(int n = 0; n < nValues; n++)
      verticesTimes[n] = timeCurve.interpolatedValue(wellDepths[n]);
     return verticesTimes;
    }

    public float[] getVerticesDepths()
    {
     if(verticesDepths != null) return verticesDepths;
     verticesDepths = this.getDepthArrayFromVertices();
     return verticesDepths;
    }

    public float[] getVerticesMDepths()
    {
     if(verticesMDepths != null) return verticesMDepths;
     verticesMDepths = getMDepthArrayFromVertices();
     return verticesDepths;
    }
    */
    /*
    public StsPoint[] getTimePoints()
    {
     if(timePoints != null) return timePoints;

     StsLogCurve timeCurve = getLogCurve(TIME);
     if(timeCurve == null) return null;
     float[] depths = getVerticesDepths();
     float[] times = getVerticesTimes();
     int nPoints = points.length;
     timePoints = new StsPoint[nPoints];
     for(int n = 0; n < nPoints; n++)
     {
      timePoints[n] = new StsPoint(depthPoints[n]);
      float time = StsLogCurve.interpolatedValue(depths, times, depthPoints[n].getZ());
      timePoints[n].setZ(time);
     }
     return timePoints;
    }
    */
    public float getValueFromMDepth(float mdepth, String typeString)
    {
        if (typeString == StsWellViewModel.MDEPTH)
        {
            return mdepth;
        }
        if (typeString == StsWellViewModel.DEPTH)
        {
            return getDepthFromMDepth(mdepth);
        }
        else if (typeString == StsWellViewModel.TIME)
        {
            return getTimeFromMDepth(mdepth);
        }
        else
        {
            return nullValue;
        }
    }

    public float getMinMDepth()
    {
		return getLineVectorSet().checkGetWellMDepthVector().getFirst();
    }

    public StsPoint getPoint(int index)
    {
        return getLineVectorSet().getCoorPoint(index);
    }
	
    public float getMaxMDepth()
    {
		return getLineVectorSet().getMVector().getLast();
    }

    public void createSeismicCurtain()
    {
        StsSeismicVolume seismicVolume = (StsSeismicVolume) currentModel.getCurrentObject(StsSeismicVolume.class);
        createSeismicCurtain(seismicVolume);
    }

    public void createSeismicCurtain(StsSeismicVolume vol)
    {
        if (seismicCurtain != null)
        {
            if (seismicCurtain.getSeismicVolume() == vol)
                return;
        }
        if(vol == null)
        {
            new StsMessage(currentModel.win3d, StsMessage.ERROR, "Well curtain requires a seismic volume to be loaded into the project.");
            isDrawingCurtain = false;
            return;
        }
        else
        {
            StsPoint[] rotatedPoints = getAsCoorPoints();
            isDrawingCurtain = true;
            if (isVertical)
                seismicCurtain = new StsSeismicCurtain(currentModel, rotatedPoints, vol);
            else
            {
                // Pass the line for the curtain extraction
                StsPoint[] shiftedPoints = shiftPoints(rotatedPoints, curtainOffset);
                seismicCurtain = new StsSeismicCurtain(currentModel, shiftedPoints, vol);
            }
        }
    }

    /** shift points along normal to line between first and last points */
    private StsPoint[] shiftPoints(StsPoint[] rotatedPoints, float offset)
    {
        float[] normal = StsMath.horizontalNormal(rotatedPoints[0].getPointXYZ(), rotatedPoints[rotatedPoints.length - 1].getXYZ(), 1);
        float xShift = normal[0] * offset;
        float yShift = normal[1] * offset;
        StsPoint[] shiftedPoints = StsPoint.copy(rotatedPoints);
        for (int n = 0; n < shiftedPoints.length; n++)
        {
            shiftedPoints[n].v[0] += xShift;
            shiftedPoints[n].v[1] += yShift;
        }
        return shiftedPoints;
    }

    public void deleteSeismicCurtain()
    {
        if (seismicCurtain != null)
        {
            seismicCurtain.delete();
            //           currentModel.win3dDisplay();
        }
        seismicCurtain = null;
    }

	public void addTimeDepthToVels(StsSeismicVelocityModel velocityModel)
	{

    }

    public void checkAdjustWellTimes(StsSeismicVelocityModel velocityModel)
    {
        // domain should always be depth actually
        //        if (zDomainOriginal == StsParameters.TD_TIME)
        {
            adjustFromVelocityModel(velocityModel);
            adjustNonSurfaceWellMarkerTimes(velocityModel);
        }
        checkSetZDomainSupported(StsParameters.TD_TIME_DEPTH);
    }

    /*
          public boolean checkConvertToTime(StsSeismicVelocityModel velocityModel)
         {
             if(lineVectorSet == null) return false;
             int nVertices = lineVectorSet.getSize();
             StsProject project = currentModel.getProject();
             float[] times = new float[nVertices];
             for (int n = 0; n < nVertices; n++)
             {
                 StsSurfaceVertex vertex = (StsSurfaceVertex) lineVectorSet.getElement(n);
                 StsPoint point = vertex.getPoint();
                StsPoint newPoint = adjustPointTime(point, project, velocityModel);
                if (newPoint == null)
                {
                    StsMessageFiles.errorMessage("Failed to convert well " + getName() + " to time.");
                    return false;
                }
                vertex.setPoint(newPoint);
                times[n] = newPoint.getT();
             }
             saveVertexTimesToDB(times);
     //       currentModel.addMethodCmd(this, "computeXYZPoints", new Object[0] );
             computeXYZPoints(); // generate splined points between vertices
             setZDomainSupported(TD_TIME_DEPTH);
             dbFieldChanged("zDomainSupported", TD_TIME_DEPTH);
     //        convertMarkersToTime(velocityModel);
             return true;
         }
    */
    public void saveVertexDepthsToDB(float[] depths)
    {
        currentModel.addMethodCmd(this, "updateVertexDepths", new Object[]
            {depths});
    }

    public void adjustNonSurfaceWellMarkerTimes(StsSeismicVelocityModel velocityModel)
    {
        float oldTime = 0.0f;
        float time;
        StsWellMarker wellMarker;

        StsObjectRefList markers = getMarkers();
        if (markers == null)
        {
            return;
        }
        int nMarkers = markers.getSize();
        for (int m = 0; m < nMarkers; m++)
        {
            wellMarker = (StsWellMarker) markers.getElement(m);
            StsModelSurface surface = wellMarker.getMarker().getModelSurface();
            if (velocityModel.hasModelSurface(surface)) continue;
            if (velocityModel.debug)
                oldTime = wellMarker.getLocation().getT();
			wellMarker.clearLocation();
            if (velocityModel.debug)
                System.out.println("    well marker " + wellMarker.getName() + " well " + getName() + " readjusted from time " + oldTime + " to " + wellMarker.getLocation().getT());
        }
    }

    private void debugCheckMarkers()
    {
        StsObjectRefList markers = getMarkers();
        if (markers == null) return;
        int nMarkers = markers.getSize();
        for (int m = 0; m < nMarkers; m++)
        {
            StsWellMarker wellMarker = (StsWellMarker) markers.getElement(m);
            StsPoint location = wellMarker.getLocation();
            float markerT = location.getT();
            StsPoint markerPointOnWell = getPointAtZ(location.getZ(), false);
            float wellT = markerPointOnWell.getT();
            float timeError = markerT - wellT;
            //            if(timeError > 1.0f || timeError < -1.0f)
            System.out.println("DEBUG. Marker on surface for well " + getName() + " marker " + wellMarker.getName() + " time error " + timeError +
                " marker " + markerT + " well " + wellT);
        }
    } // after velocity model has been updated, compute depth vector from time vector

    // used for planned wells which are fixed in time
    public boolean adjustDepthPoints(StsSeismicVelocityModel velocityModel)
    {
        StsPoint[] points = getAsCoorPoints();
        if (points == null) return false;
        for (int n = 0; n < points.length; n++)
        {
            float z;
            try
            {
                float[] xyztm = points[n].v;
                z = (float) velocityModel.getZ(xyztm[0], xyztm[1], xyztm[3]);
            }
            catch (Exception e)
            {
                StsMessageFiles.errorMessage("Failed to adjust well depth points for well " + getName());
                return false;
            }
            points[n].setZ(z);
        }
        return true;
    }

    public boolean canExport() { return true; }

    public boolean export()
    {
        return export(StsParameters.TD_ALL_STRINGS[getZDomainSupported()]);
    }

    public boolean export(String timeOrDepth)
    {
        return StsWellExportDialog.exportWell(currentModel, currentModel.win3d, "Well Export Utility", true, this, timeOrDepth);
    }


    /*
        private void outputPoint(StsPoint point0, StsPoint point1, float mDepth, boolean writeTime, boolean writeDepth, boolean exportLogData, StsObjectRefList curves, StsAsciiFile asciiFile)
        {
            String valLine = null;

            float m0 = point0.getM();
            float m1 = point1.getM();
            float x0 = point0.getX();
            float x1 = point1.getX();
            float y0 = point0.getY();
            float y1 = point1.getY();
            float z0 = point0.getZ();
            float z1 = point1.getZ();
            float t0 = point0.getT();
            float t1 = point1.getT();
            float f = (mDepth - m0)/(m1 - m0);
            float x = x0 + f*(x1 - x0);
            float y = y0 + f*(y1 - y0);
            float z = z0 + f*(z1 - z0);
            float t = t0 + f*(t1 - t0);
            float m = m0 + f*(m1 - m0);
            if (writeTime && writeDepth)
                 valLine = new String(x + " " + y + " " + z + " " + m + " " + t);
             else if (writeDepth)
                 valLine = new String(x + " " + y + z + " " + m);
             else if (writeTime)
                 valLine = new String(x + " " + y + " " + t);

            if(exportLogData)
            {
                for(int i=0; i<getNLogCurves(); i++)
                    valLine = valLine + " " + ((StsLogCurve)curves.getElement(i)).interpolatedValue(z);
            }
            try
            {
                asciiFile.writeLine(valLine);
            }
            catch(Exception e)
            {
            }
        }

        private void outputPoint(StsPoint point, boolean writeTime, boolean writeDepth, boolean exportLogData, StsObjectRefList curves, StsAsciiFile asciiFile)
        {
            String valLine = null;

            float m = point.getM();
            float x = point.getX();
            float y = point.getY();
            float z = point.getZ();
            float t = point.getT();
            if (writeTime && writeDepth)
                 valLine = new String(x + " " + y + " " + z + " " + m + " " + t);
             else if (writeDepth)
                 valLine = new String(x + " " + y + z + " " + m);
             else if (writeTime)
                 valLine = new String(x + " " + y + " " + t);
             if(exportLogData)
             {
                 for(int i=0; i<getNLogCurves(); i++)
                     valLine = valLine + " " + ((StsLogCurve)curves.getElement(i)).interpolatedValue(z);
             }
             try
             {
                 asciiFile.writeLine(valLine);
             }
             catch(Exception e)
             {
             }
         }
    */
    /*
        public void setExportName(String exportName)
        {
            this.exportName = exportName;
        }

        public String getExportName()
        {
            return exportName;
        }
    */
    public float getWellDirectionAngle()
    {
        StsPoint topPoint = getLineVectorSet().getFirstCoorPoint();
        StsPoint botPoint = getLineVectorSet().getLastCoorPoint();
        float deltaX = topPoint.getX() - botPoint.getX();
        float deltaY = topPoint.getY() - botPoint.getY();
        return StsMath.atan2(deltaY, deltaX);
    }

    public boolean hasVsp() { return hasVsp; }

    public StsSeismicCurtain getSeismicCurtain() { return seismicCurtain; }

    public StsSeismicCurtain getCreateSeismicCurtain()
    {
        if (seismicCurtain == null)
        {
            isDrawingCurtain = true;
            createSeismicCurtain();
            currentModel.win3dDisplay();
        }
        return seismicCurtain;
    }

    public float[] getXYZForVertex(int vertexNum)
    {
        StsPoint[] points = getAsCoorPoints();
        if (points == null) return null;
        if (points.length < vertexNum) return null;
        return points[vertexNum].getXYZorT();
    }

    public StsSeismicCurtain getCreateSeismicCurtain(StsSeismicVolume vol)
    {
        if (seismicCurtain == null)
            createSeismicCurtain(vol);
        return seismicCurtain;
    }

    public void setBornDate(String born)
    {
        if (!StsDateFieldBean.validateDateInput(born))
        {
            bornField.setValue(StsDateFieldBean.convertToString(getBornDate()));
            return;
        }
        super.setBornDate(born);
    }

    public void setDeathDate(String death)
    {
        if (!deathField.validateDateInput(death))
        {
            deathField.setValue(getCurrentProject().getDateTimeStringFromLong(getDeathDate()));
            return;
        }
        super.setDeathDate(death);
    }

    // StsMonitorable Interface
    public int addNewData(StsObject object)
    {
        StsMessageFiles.errorMessage("addNewObject is not supportted for StsWell type.");
        return 0;
    }

    public int addNewData(double[] attValues, long time, String[] attNames)
    {
        StsMessageFiles.errorMessage("addNewData is not supportted for StsWell type.");
        return 0;
    }

    public int addNewData(StsPoint point, long time, String[] attNames)
    {
        StsMessageFiles.errorMessage("addNewData is not supportted for StsWell type.");
        return 0;
    }

    public int addNewData(String source, byte sourceType, long lastPollTime, boolean reload, boolean replace)
    {
        StsMessageFiles.errorMessage("addNewData is not supportted for StsWell type.");
        return 0;
    }

    public StsPerforationMarker[] getPerforationMarkers()
    {
        StsPerforationMarker[] list = null;
        if (markers != null)
        {
            int nMarkers = markers.getSize();
            for (int n = 0; n < nMarkers; n++)
            {
                StsWellMarker marker = (StsWellMarker) markers.getElement(n);
                if (marker instanceof StsPerforationMarker)
                    list = (StsPerforationMarker[])StsMath.arrayAddElement(list, marker);
            }
        }
        return list;
    }

    /* current marker file output example
    1G-03
    CURVE
    DEPTH
    VALUE
    TUZB 2710.790000
    WS2 3020.890000
    TWSK 3255.270000
    TWZD 3255.270000
    */

    public XMLObject getXMLobject()
    {
        return new XMLObject();
    }

    class XMLObject
    {
        String wellname;
        String depthType;
        StsWellMarker.XMLobject[] markerObjects;

        XMLObject()
        {
            wellname = name;
            depthType = StsLoader.MDEPTH;
            int nMarkers = markers.getSize();
            {
                markerObjects = new StsWellMarker.XMLobject[nMarkers];
                for (int n = 0; n < nMarkers; n++)
                {
                    StsWellMarker marker = (StsWellMarker) markers.getElement(n);
                    markerObjects[n] = marker.getXMLobject();
                }
            }
        }
    }
/*
    public StsAlarm[] getAlarms() { return alarms; }
    public boolean addAlarm(StsAlarm alarm)
    {
        if(alarms == null)
        {
            alarms = new StsAlarm[1];
            alarms[0] = alarm;
        }
        else
            alarms = (StsAlarm[])StsMath.arrayAddElement(alarms, alarm);

        return true;
    }
    public void checkAlarms()
    {
        for(int i=0; i<alarms.length; i++)
        {
            ;
        }
    }
	
    public boolean hasAlarms()
    {
        if(alarms != null)
            return true;
        else
            return false;
    }
*/
    public void showPopupMenu(StsGLPanel glPanel, StsMouse mouse)
    {
        JPopupMenu tp = new JPopupMenu("Selection Popup");
        glPanel.add(tp);
        StsMenuItem wellWindow = new StsMenuItem();
        wellWindow.setMenuActionListener("Create Well Window", this, "createWellWindow", null);
        tp.add(wellWindow);
	/*
        if(currentModel.getStsClass(StsSeismicVolume.class) != null)
        {
            StsMenuItem curtainWindow = new StsMenuItem();
            curtainWindow.setMenuActionListener("Aux Curtain View", this, "addCurtainWindow", glPanel);
            tp.add(curtainWindow);
        }
    */
        tp.show(glPanel, mouse.getX(), mouse.getY());
    }

    public void createWellWindow()
    {
        openOrPopWindow();
    }
/*
    public void addCurtainWindow(StsGLPanel3d glPanel3d)
    {
		StsSeismicCurtainView seismicCurtainView = new StsSeismicCurtainView(currentModel, this);
		if(seismicCurtainView == null) return;
		StsWin3dBase window = glPanel3d.window;
            currentModel.createAuxWindow(window, StsSeismicCurtainView.shortViewNameCurtain, seismicCurtainView);
    }

    private void addCurtainView(StsGLPanel3d glPanel3d)
    {
    }
*/
	public String getAsciiDirectoryPathname()
	{
		return getProject().getAsciiDirectoryPathname(getClassStsSubDirectoryString(), getStsFolderName(name));
	}

	public String getBinaryDirectoryPathname()
	{
		return getProject().getBinaryDirectoryPathname(getClassStsSubDirectoryString(), getStsFolderName(name));
	}

	static public String staticGetAsciiDirectoryPathname(String name)
	{
		String folderName = getStsFolderName(name);
		return currentModel.getProject().getAsciiDirectoryPathname(getClassStsSubDirectoryString(), folderName);
	}

	static public String staticGetBinaryDirectoryPathname(String name)
	{
		String folderName = getStsFolderName(name);
		return currentModel.getProject().getBinaryDirectoryPathname(getClassStsSubDirectoryString(), folderName);
	}
	
	public boolean hasLogCurveFileObject(StsAbstractFile logCurveFile)
	{
		if(logCurveVectorSets == null) return false;
		StsFilenameFilter filenameFilter = StsFilenameFilter.parseStsFilename(logCurveFile.getFilename());
		Iterator vectorSetIterator = logCurveVectorSets.iterator();
		while(vectorSetIterator.hasNext())
		{
			StsTimeVectorSet logCurveVectorSet = (StsTimeVectorSet)vectorSetIterator.next();
			if(logCurveVectorSet.matchesParsedFilename(filenameFilter)) return true;
		}
		return false;
	}

	static public String getClassSourceSubDirectoryString()
	{
	  	return groupName + File.separator;
	}

	static public String getClassStsSubDirectoryString()
	{
	  	return groupName + "." + StsLoader.FORMAT_DIRS + File.separator;
	}

	static public String getStsFolderName(String name)
	{
	  	return groupName + "." + StsLoader.FORMAT_DIR + "." + name;
	}

	static public String getGroupSubFolderName()
	{
		return groupName + "." + StsLoader.FORMAT_DIR;
	}

	static public StsFilenameFilter getSubFileGroupsFilter()
	{
		return StsFilenameFilter.constructGroupsFormatFilter(subFileGroups, StsLoader.FORMAT_TXT);
	}

    public StsFile checkCreateStsAsciiFile()
    {
        return StsFile.checkCreateFile(getClassStsSubDirectoryString(),getStsFolderName(name));
    }
}
