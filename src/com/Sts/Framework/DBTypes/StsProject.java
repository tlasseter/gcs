package com.Sts.Framework.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: StsProject is the class that maintains all the boundary conditions for the Project.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author T.Lasseter
 * @version 1.0
 */

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.IO.WatchService.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.UI.Toolbars.*;
import com.Sts.Framework.Utilities.DataCube.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.DateTime.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

import static com.Sts.Framework.Utilities.StsException.systemError;


public class StsProject extends StsMainObject implements ActionListener, Serializable, StsTreeObjectI //, StsCultureDisplayable
{
    /** defines grid for model grids/zones */
    public StsGridDefinition gridDefinition = null;
    /** Default Units */
    private byte timeUnits = StsParameters.TIME_MSECOND;
    private byte depthUnits = StsParameters.DIST_FEET;
    private byte xyUnits = StsParameters.DIST_FEET;
    /** Current model time - set by user. */
    private long projectTime;
    /** Model time range - set by user. */
    private long projectTimeDuration = 0;
	/** Clock-time controls of display are enabled */
	private boolean timeEnabled = false;
	/** Capture images in high (bmp) or low (jpg) resolution */
    public boolean highResolution = false;
	 /** X increment locked: seismic volume or grid has been loaded; increment changes can only be smaller and congruent */
    public boolean xIncLocked = false;
	/** Y increment locked: seismic volume or grid has been loaded; increment changes can only be smaller and congruent */
    public boolean yIncLocked = false;
    /** Y increment locked: seismic loaded in depth; increment changes can only be smaller and congruent */
    public boolean depthIncLocked = false;
    /** Time increment set: seismic loaded in time; increment changes can only be smaller and congruent */
    public boolean timeIncLocked = false;
    /** domains which project supports: time, approx-depth, depth, time & depth, or approx-depth & depth */
    public byte zDomainSupported = TD_NONE;
    /** domain in which model was built if and when built */
    public byte modelZDomain = TD_NONE;
    /** current project domain (time or depth). */
    public byte zDomain = TD_NONE;
    /** Distance limit between project bounds and new data being added. If exceeded, probably data error */
    private double distanceLimit = 100000f;

    public float gridFrequency = AUTO;
    public float zGridFrequency = AUTO;
    public float labelFrequency = AUTO;

    private StsSeismicVelocityModel velocityModel = null;

    private float mapGenericNull = 1.e30f; // default to zmap null value
    private float logNull = -999;  // This is the value we expect in the file, not our internal null representation
    private float minZInc = 10.0f;

    private boolean displayContours = false;
    private float contourInterval = 10.0f;

    private boolean showIntersection = true;
    private boolean showGrid = true;
    private boolean show3dGrid = false;
    private boolean show2dGrid = false;

    // Time series plot defaults
    private boolean showTimeSeriesGrid = true;
    // Number of frames to leave accent up
    private StsColor timeSeriesBkgdColor = new StsColor(StsColor.BLACK);
    private StsColor timeSeriesFrgdColor = new StsColor(StsColor.LIGHT_GREY);
    private StsColor timeSeriesGridColor = new StsColor(StsColor.DARK_GREY);

    /** Show the labels in graphics environment */
    private boolean showLabels = false;
    private StsColor backgroundColor = new StsColor(StsColor.BLACK);
    private StsColor foregroundColor = new StsColor(StsColor.WHITE);
    private StsColor gridColor = new StsColor(StsColor.GRAY);
    private StsColor timingColor = new StsColor(StsColor.GRAY);
    private StsColor cogColor = new StsColor(StsColor.CYAN);
    private String labelFormatString = defaultLabelFormatString;
    // private String timeDateFormatString = defaultDateFormatString + " " + defaultTimeFormatString;
    private String timeFormatString = defaultTimeFormatString;
    private String dateFormatString = defaultDateFormatString;
    private int dateOrder = CalendarParser.DD_MM_YY;

    private int timeUpdateRate = 1000; // Time action update rate when running time.
    private int realtimeBarAt = 500; // If more than 500 events in update, show progress bar

    // Project datums
    // private float timeDatum = 0.0f;
    // private float depthDatum = 0.0f;

    /** all 3d views use perspective; othewise orthogonal */
    private boolean isPerspective = true;
    public byte NOLIMIT = -1;
    private float zoomLimit = NOLIMIT; // Limit the zoom minimum
    /** Display Compass in 3D view*/
    private boolean displayCompass = true;

    private byte gridLocation = 0;

    /** rotated box bounding surfaces and seismic which defines the 3 cursor planes */
    transient public StsRotatedGridBoundingBox rotatedBoundingBox;
    /** Unrotated box tightly bounding all project objects. This boundingBox carries the common origin which is shared with the rotatedBoundingBox. */
    transient public StsBoundingBox unrotatedBoundingBox;
    /** unrotated box whose range encompasses the unrotatedBoundingBox. Coordinates are "rounded" to even vector. This is the box which is displayed. */
    transient public StsDisplayBoundingBox displayBoundingBox;
    /** defines cropped volume for project */
    transient public StsCropVolume cropVolume;
	/** if false, the project is using the initial default boundingBoxes; these should be ignored rather than being added to when boxes are loaded. */
	transient private boolean boxesInitialized = false;
	/** Watch service watching for file changes. */
	transient StsSourceDirectoriesWatch sourceDirectoriesWatch;

	transient StsColorListFieldBean backgroundColorBean;
    transient StsColorListFieldBean gridColorBean;
    transient StsColorListFieldBean timingColorBean;
    //  transient StsColorListFieldBean wellPanelColorBean;
    transient StsColorListFieldBean cogColorBean;
    transient StsBooleanFieldBean isPerspectiveBean;

    /** Manages memory for seismic classes */
    transient protected StsBlocksMemoryManager blocksMemoryManager = null;

    transient private DecimalFormat labelFormat = new DecimalFormat(defaultLabelFormatString);
    transient private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(defaultDateFormatString + " " + defaultTimeFormatString);
    transient private SimpleDateFormat dateFormat = new SimpleDateFormat(defaultDateFormatString);
    transient private SimpleDateFormat timeFormat = new SimpleDateFormat(defaultTimeFormatString);

    transient private Properties userPreferences;

    transient private File rootDirectory = null;
	transient private StsFileSystem sourceFileSystem;
	transient private StsFileSystem stsFileSystem;
    transient private File sourceDataDirectory = new File(SOURCE_DATA_FOLDER);
	transient private File stsDataDirectory = new File(DATA_FOLDER);
    transient private File modelDirectory = new File(MODEL_FOLDER);
    transient private File archiveDirectory = new File(ARCHIVE_FOLDER);
    transient private File mediaDirectory = new File(MEDIA_FOLDER);
    transient private File exportModelsDirectory = new File(EXPORT_MODELS_FOLDER);

	transient private StsLoaderDataDirectorySets dataDirectorySets;
	// transient private StsMultiStringProperties dataDirectories;

    transient private String name = "null";

    transient StsAsciiTokensFile directoryAliasesFile = null;

    transient StsComboBoxFieldBean defaultToolbarTimeDepthBean = null;

    //transient StsTextureSurfaceFace basemapTextureObject = null;

    static float[] defaultBoxFloats  = new float[] { -50f, 50f, -50f, 50f, 0f, 100f };
    static float[] defaultBoxIncs  = new float[] { 1f, 1f, 1f };

    static private String[] projectDirectories = null;
    static private String[] projectDatabases = null;
    static private String EOL = StsParameters.EOL;

    transient private StsObjectPanel objectPanel = null;

    static final boolean debug = false;
	/** used in setting grid frequency */
	public static final float AUTO = -1.0f;
    /** Number of XY Grid increments */
    static public int approxNumXYIncs = 20;
    /** Number of Z Grid increments */
    static public int approxNumZTIncs = 200;
    /** max number of incs allowed on vertical scale (time or depth) */
    static public int nZTIncsMax = 1000;
    // file fields
    /** Default binary files folder name: used for storage of old style binaries */
    static public final String BINARY_FOLDER = "BinaryFiles";
    /** Default model file folder name */
    static public final String MODEL_FOLDER = "modelDBFiles";
    /** Default archive file folder name */
    static public final String ARCHIVE_FOLDER = "archiveFiles";
    /** Default media file folder name */
    static public final String MEDIA_FOLDER = "mediaFiles";
    /** Default data files folder name */
    static public final String SOURCE_DATA_FOLDER = "sourceFiles";
	/** group name for all data folders: data.dir.ProjectName/wells.dir.WellGroupName/well.dir.WellA/ */
	//static public final String DATA_DIR = "data.dir";
	/** format name for a single directory containing a a set of object class instances */
	//static public final String DATA_DIR_SUFFIX = ".dir";
	/** format name for a single directory containing subdirectories each of which corresponds to an object class instance */
	//static public final String DATA_DIRS_SUFFIX = ".dirs";
	/** Default sts data files folder name */
    static public final String DATA_FOLDER = "dataFiles";
    static public final String EXPORT_MODELS_FOLDER = "exportedModelFiles";
	/** Subfolder name where all stsBinaryFiles are stored.
	 *  For example, for the binaries for well AJAX would be stored in: ../project/StsWells/Ajax/Binaries. */
	static public final String stsBinarySubDirName = "BinaryFiles";
	/** filename containing object type name key and followed by a String[] value which contains dataDirectories for that type */
	static public final String dataDirectoriesFilename = "project.dataDirectories";
    /** maximum allowable difference between rotation angles of objects in project */
    static public float angleTol = 0.5f;

    /** List of possible background colors */
    static public final StsColor[] backgroundColors = new StsColor[]
            {
                    StsColor.BLACK, StsColor.WHITE, StsColor.GRAY, StsColor.LIGHT_GRAY, StsColor.DARK_GRAY, StsColor.RED, StsColor.BLUE, StsColor.GREEN
            };
    /** foreground colors typically for text and ticks which contrasts with background color */
    static public final StsColor[] foregroundColors = new StsColor[]
            {
                    StsColor.WHITE, StsColor.BLACK, StsColor.WHITE, StsColor.BLACK, StsColor.WHITE, StsColor.WHITE, StsColor.WHITE, StsColor.WHITE
            };
    /** List of possible center-of-gravity colors */
    static public final StsColor[] cogColors = new StsColor[]
            {
                    StsColor.CYAN, StsColor.WHITE, StsColor.GRAY, StsColor.LIGHT_GRAY, StsColor.DARK_GRAY, StsColor.MAGENTA, StsColor.BLACK
            };

    static public final String defaultLabelFormatString = "#,###.#";
    static public final String defaultTimeFormatString = "HH:mm:ss.SSS";
    static public final String defaultDateFormatString = "dd/MM/yy";

    /** List of display beans for StsProject */
    transient protected StsFieldBean[] displayFields;

    /** List of property fields for StsProject */
    transient protected StsFieldBean[] propertyFields;

    /** explicitly-named propertyField used frequently */
    transient protected StsStringFieldBean zDomainSupportedBean = new StsStringFieldBean(this, "zDomainSupported", false, "Supported Domains: ");

    /** List of default fields for StsProject.  Currently none. */
    transient protected StsFieldBean[] defaultFields;

    static public final byte TD_DEPTH = StsParameters.TD_DEPTH; // 0
    static public final byte TD_TIME = StsParameters.TD_TIME; // 1
    static public final byte TD_TIME_DEPTH = StsParameters.TD_TIME_DEPTH; // 2
    static public final byte TD_APPROX_DEPTH_AND_DEPTH = StsParameters.TD_APPROX_DEPTH_AND_DEPTH; // 4
    static public final byte TD_NONE = StsParameters.TD_NONE; // 5

    static final byte defaultDistUnits = StsParameters.DIST_FEET;
    static final byte defaultTimeUnits = StsParameters.TIME_MSECOND;

    static final long serialVersionUID = 5811416831478613787L;
    public static final String PreStackSeismicDirectory = "PreStackSeismicDirectory";

    public static final String BOTTOM_STRING = "BOTTOM";
    public static final String TOP_STRING = "TOP";
    public static final String CENTER_STRING = "CENTER";
    public static final String[] gridLocationStrings = new String[] { BOTTOM_STRING, TOP_STRING, CENTER_STRING };
    public static final byte BOTTOM = 0;
    public static final byte TOP = 1;
    public static final byte CENTER = 2;

	public static final String DIR_STRING = "." + StsLoader.FORMAT_DIR;

    private transient StsAsciiTokensFile directoryTableFile;

    /** Default Project constructor. Uses user.dir to construct the Project object */
    public StsProject()
    {
//        initializeProjectDirectory(System.getProperty("user.dir") + File.separator + "S2SCache" + File.separator);
    }

    public StsProject(boolean persistent)
    {
        super(persistent);
        initializeProjectDirectory(System.getProperty("user.dir") + File.separator + "S2SCache" + File.separator);
    }

    /**
     * Project constructor. Defines a default database name, directory and bounding box and initializes the graphics colors
     *
     * @parameters projectDirname the directory name where the database will be stored
     */
    public StsProject(String projectDirname)
    {
        super(false);
        initializeProjectDirectory(projectDirname);
    }

	public StsProject(String projectDirname, String dbName)
	{
		super(false);
		initializeProjectDirectory(projectDirname);
		setName(dbName);
	}

    private void initializeProjectDirectory(String projectDirname)
    {
        // constructDefaultBoundingBoxes();
        File rootDir = new File(projectDirname);
        initializeRootDirectory(projectDirname);
        //TODO this does not seem to work for the directory; lastMod time is not changed
        //TODO we probably need to save project list file with two tokens, directory name and time modified
        //TODO time modified would be set when a db is saved in the project
        rootDir.setLastModified(System.currentTimeMillis());
        initializeColors();
    }

    public boolean initialize(StsModel model)
    {
		model.setProject(this);
        String rootDirname = model.getDatabase().getProjectDirectory();
        if(!Main.isJarDB)
            initializeRootDirectory(rootDirname);
        else
            initializeRootDirectory(System.getProperty("user.home") + File.separator + "S2SCache" + File.separator);

        //        setColors(model);
        //constructDefaultBoundingBoxes();
        setLabelFormatString(labelFormatString);
        setIsDepth(zDomain == TD_DEPTH);
        if(displayBoundingBox != null) initializeGridLocation();
        return true;
    }

	public boolean initializeRootDirectory(String rootDirString)
	{
        rootDirectory = new File(rootDirString);
		if(!rootDirectory.exists())
		{
			try
			{
				rootDirectory.createNewFile();
			}
			catch(Exception e)
			{
				new StsMessage(currentModel.win3d, StsMessage.ERROR, "Failed to construct root directory " + rootDirString);
				return false;
			}
		}
		checkMakeProjectSubDirectories();
	    sourceFileSystem = StsFileSystem.createLocalFileSystem(getSourceDataDirString());
		stsFileSystem = StsFileSystem.createLocalFileSystem(getStsDataDirString());
		return true;
	}

	/** Currently sections don't include wells (though they could for sections built between wells).
	 *  Regardless, wells are not on sections so this geometric dependence does not exist for wells, only lines.
	 *  At this point, sections which are not on other sections will have been initialized already,
	 *  so we need only iteratively initialize sections on sections.  As this relationship can literally be a loop,
	 *  we will try only 3 iterations before leaving the geometry as computed.
	 */
/*
    public boolean postDbLoadInitializeGeometry()
    {
		int nSections = currentModel.getNObjects("com.Sts.PlugIns.Model.DBTypes.StsSection");
		if(nSections == 0) return true;
		//StsSectionClass sectionClass = (StsSectionClass)currentModel.getStsClass("com.Sts.PlugIns.Model.DBTypes.StsSection");
		StsLineClass lineClass = (StsLineClass)currentModel.getStsClass("com.Sts.PlugIns.Wells.DBTypesApp.StsLine");
        boolean allLinesInitialized = lineClass.initLinesOnSections();
        //boolean allSectionsInitialized = sectionClass.initSections();

        int noIter = 0;
        while (noIter < 3 && (!allLinesInitialized)) // || !allSectionsInitialized))
        {
            allLinesInitialized = lineClass.initLinesOnSections();
            //allSectionsInitialized = sectionClass.initSections();
            noIter++;
        }
        return allLinesInitialized; // && allSectionsInitialized;
    }
*/
    public void initializeGridLocation()
    {
        displayBoundingBox.setGridZLocation(gridLocation);
        displayBoundingBox.setGridTLocation(gridLocation);
    }

    private StsColor getColorProperty(StsModel model, String key)
    {
        String colorString = model.properties.getProperty(key);
        return StsColor.colorFromString(colorString);
    }

    public void setLabelFormatString(String string)
    {
        if(string.equals(labelFormatString)) return;
        labelFormatString = new String(string);
        dbFieldChanged("labelFormatString", labelFormatString);
        labelFormat = new DecimalFormat(labelFormatString);
    }

    public void setDateOrderString(String dateOrderString)
    {
        dateOrder = CalendarParser.getOrderFromString(dateOrderString);
        switch(dateOrder)
        {
            case CalendarParser.DD_MM_YY:
                setDateFormatString("dd/MM/yy");
                break;
            case CalendarParser.DD_YY_MM:
                setDateFormatString("dd/yy/MM");
                break;
            case CalendarParser.MM_DD_YY:
                setDateFormatString("MM/dd/yy");
                break;
            case CalendarParser.MM_YY_DD:
                setDateFormatString("MM/yy/dd");
                break;
            case CalendarParser.YY_DD_MM:
                setDateFormatString("yy/dd/MM");
                break;
            case CalendarParser.YY_MM_DD:
                setDateFormatString("yy/MM/dd");
                break;
        }
        doObjectPanelChanged();
    }

    public String getDateOrderString()
    {
        return CalendarParser.getStringFromDateOrder(dateOrder);
    }

    public void setTimeFormatString(String string)
    {
        try
        {
            if(string.equals(timeFormatString)) return;
            timeFormat = new SimpleDateFormat(string);
            timeFormatString = new String(string);
			dateTimeFormat = new SimpleDateFormat(dateFormatString + " " + timeFormatString);
            dbFieldChanged("timeFormatString", timeFormatString);
            resetTimeDateFormat();
        }
        catch(Exception e)
        {
            StsException.outputException("Error converting input string to time format", e, StsException.WARNING);
            timeFormat = new SimpleDateFormat(timeFormatString);
        }
    }

    public void setDateFormatString(String string)
    {
        try
        {
            if(string.equals(dateFormatString)) return;
            dateFormat = new SimpleDateFormat(string);
            dateFormatString = new String(string);
			dateTimeFormat = new SimpleDateFormat(dateFormatString + " " + timeFormatString);
            dbFieldChanged("dateFormatString", dateFormatString);
            resetTimeDateFormat();
        }
        catch(Exception e)
        {
            StsException.outputException("Error converting input string to date format", e, StsException.WARNING);
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    public void resetTimeDateFormat()
    {
        if(currentModel == null || currentModel.win3d == null) return;
        StsTimeActionToolbar timeActionToolbar = currentModel.win3d.getTimeActionToolbar();
        if(timeActionToolbar == null) return;
        timeActionToolbar.resetFormat();
        //currentModel.resetTimeViews();
        currentModel.viewObjectRepaint(this, this);
    }

	public void startProjectTime()
	{
		setTimeEnabled(true);
		StsTimeActionToolbar timeActionToolbar = currentModel.win3d.getTimeActionToolbar();
		timeActionToolbar.startTime();
	}

    public String getTimeDateFormatString()
    {
        return dateFormatString + " " + timeFormatString;
    }

    public String getDateStringFromLong(long time)
    {
        return dateFormat.format(new Date(time));
    }

    public String getTimeStringFromLong(long time)
    {
        return timeFormat.format(new Date(time));
    }

	public String getDateTimeStringFromLong(long time)
	{
		if(time < 0) time = 0L;
		return dateTimeFormat.format(new Date(time));
	}

	public String getProjectDateTimeString()
	{
		return dateTimeFormat.format(projectTime);
	}

    public int getDateOrder()
    {
        return dateOrder;
    }

    public boolean validateTimeDateString(String test)
    {
        try
        {
            getDateTimeFormat().parse(test);
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    public SimpleDateFormat getDateTimeFormat()
    {
        return dateTimeFormat;
    }

    public SimpleDateFormat getTimeDateFormatForOutput()
    {
        dateTimeFormat = new SimpleDateFormat(dateFormatString + ", " + timeFormatString);
        return dateTimeFormat;
    }

    public SimpleDateFormat getDateFormat()
    {
        dateFormat = new SimpleDateFormat(dateFormatString);
        return dateFormat;
    }

    public float getZoomLimit()
    {
        return zoomLimit;
    }

    public void setZoomLimit(int zLimit)
    {
        zoomLimit = zLimit;
        dbFieldChanged("zoomLimit", zoomLimit);
    }

    /** Set the Project Grid Frequency String */
    public void setZoomLimitString(String limit)
    {
        //		setDisplayField("zoomLimitAsString", limit);
        if(limit.equalsIgnoreCase("No") || limit.equalsIgnoreCase("No Limit") || limit.equalsIgnoreCase("None"))
        {
            zoomLimit = NOLIMIT;
        }
        else
        {
            zoomLimit = (new Float(limit)).floatValue();
        }
        //		dbFieldChanged("zoomLimit",zoomLimit);
        return;
    }

    /**
     * Get Project Label Frequency String
     *
     * @return label frequency in model units
     */
    public String getZoomLimitString()
    {
        if(zoomLimit == NOLIMIT)
        {
            return new String("No Limit");
        }
        else
        {
            return new Float(zoomLimit).toString();
        }
    }

    public double getDistanceLimit()
    {
        return distanceLimit;
    }

    public void setDistanceLimit(double limit)
    {
        if(limit == distanceLimit) return;
        distanceLimit = limit;
        dbFieldChanged("distanceLimit", distanceLimit);
    }

    public int getRealtimeBarAt()
    {
        return realtimeBarAt;
    }

    public void setRealtimeBarAt(int numEvents)
    {
        if(numEvents == realtimeBarAt) return;
        realtimeBarAt = numEvents;
        dbFieldChanged("realtimeBarAt", realtimeBarAt);
    }

    public int getTimeUpdateRate()
    {
        return timeUpdateRate;
    }

    public void setTimeUpdateRate(int rate)
    {
        if(rate == timeUpdateRate) return;
        timeUpdateRate = rate;
        dbFieldChanged("timeUpdateRate", timeUpdateRate);
    }

    public DecimalFormat getLabelFormat()
    {
        if(labelFormat == null)
            labelFormat = new DecimalFormat(labelFormatString);
        return labelFormat;
    }

    /** Initialize the background, grid and timing line colors to black, light gray, and gray respectively */
    private void initializeColors()
    {
        backgroundColor = new StsColor(StsColor.BLACK);
        gridColor = new StsColor(StsColor.LIGHT_GRAY);
        timingColor = new StsColor(StsColor.GRAY);
        cogColor = new StsColor(StsColor.CYAN);
    }

    public StsFieldBean[] getDisplayFields()
    {
        if(displayFields != null) return displayFields;

        backgroundColorBean = new StsColorListFieldBean(this, "backgroundColor", "Background Color: ", backgroundColors);
        gridColorBean = new StsColorListFieldBean(this, "gridColor", "Grid Color: ", backgroundColors);
        timingColorBean = new StsColorListFieldBean(this, "timingColor", "Timing Color: ", backgroundColors);
        //wellPanelColorBean = new StsColorListFieldBean(this, "wellPanelColor", "Well panel Color:", backgroundColors);
        cogColorBean = new StsColorListFieldBean(this, "cogColor", "COG Color: ", cogColors);
        isPerspectiveBean = new StsBooleanFieldBean(this, "isPerspective", "Perspective View");
        displayFields = new StsFieldBean[]
		{
			new StsBooleanFieldBean(this, "isVisible", "Show Bounding Box"),
			new StsBooleanFieldBean(this, "displayCompass", "Display Compass"),
			new StsBooleanFieldBean(this, "timeEnabled", "Enable time"),
			isPerspectiveBean,
			new StsBooleanFieldBean(this, "showGrid", "Show Floor Grid"),
			new StsBooleanFieldBean(this, "showIntersection", "Cursor Intersection"),
			backgroundColorBean,
			gridColorBean,
			timingColorBean,
			new StsBooleanFieldBean(this, "enableTsGrid", "Show Grid on Series Plot"),
			new StsColorListFieldBean(this, "timeSeriesBkgdColor", "Series Plot Background Color:", StsColor.bkgdColors),
			new StsColorListFieldBean(this, "timeSeriesFrgdColor", "Series Plot Foreground Color:", StsColor.bkgdColors),
			new StsColorListFieldBean(this, "timeSeriesGridColor", "Series Plot Grid Color:", StsColor.bkgdColors),
			//Bean,
			new StsBooleanFieldBean(this, "showLabels", "Show Labels on Cursor"),
			new StsBooleanFieldBean(this, "show3dGrid", "Show Grid in 3D Views"),
			new StsBooleanFieldBean(this, "show2dGrid", "Show Grid in 2D Views"),
			cogColorBean,
			new StsStringFieldBean(this, "labelFormatString", "Label Format: "),
			new StsStringFieldBean(this, "labelFrequencyString", "Label Frequency: "),
			new StsStringFieldBean(this, "gridFrequencyString", "XY Grid Frequency: "),
			new StsStringFieldBean(this, "zGridFrequencyString", "Z Grid Frequency: "),
			new StsComboBoxFieldBean(this, "dateOrderString", "Date Order: ", CalendarParser.dateOrderStrings),
			new StsStringFieldBean(this, "timeFormatString", "Time Format: "),
			new StsStringFieldBean(this, "dateFormatString", "Date Format: "),
			new StsIntFieldBean(this, "timeUpdateRate", 100, 600000, "Time Update Rate(ms): ", true),
			new StsIntFieldBean(this, "realtimeBarAt", 100, 100000, "Progress Bar At (nEvents): ", true),
			new StsDoubleFieldBean(this, "distanceLimit", 1000, 1000000, "Distance Limit (ft/m): ", true),
			new StsStringFieldBean(this, "zoomLimitString", "Zoom Limit(ft/m): "),
			new StsBooleanFieldBean(this, "highResolution", "Capture High Res Images"),
			new StsFloatFieldBean(this, "mapGenericNull", true, "Surface Null Z: "),
			new StsFloatFieldBean(this, "logNull", "Log Null Value: "),
			new StsFloatFieldBean(this, "minZInc", "Min time or depth inc: "),
			new StsBooleanFieldBean(this, "displayContours", "Display Contours"),
			new StsFloatFieldBean(this, "contourInterval", "Contour Interval: ")
		};

        return displayFields;
    }

    public StsFieldBean[] getPropertyFields()
    {
        if(propertyFields == null)
        {
            propertyFields = new StsFieldBean[]
                    {
                            //new StsFloatFieldBean(this, "timeDatum", true, "Time Datum: "),
                            //new StsFloatFieldBean(this, "depthDatum", true, "Depth Datum: "),
                            new StsFloatFieldBean(this, "displayXMin", false, "X Minimum: "),
                            new StsFloatFieldBean(this, "displayXMax", false, "X Maximum: "),
                            new StsFloatFieldBean(this, "displayYMin", false, "Y Minimum: "),
                            new StsFloatFieldBean(this, "displayYMax", false, "Y Maximum: "),
                            new StsFloatFieldBean(this, "angle", false, "XY Rot Angle: "),
                            //		new StsFloatFieldBean(this, "zStart", "Start Z", false),
                            new StsDoubleFieldBean(this, "xOrigin", false, "X Origin: "),
                            new StsDoubleFieldBean(this, "yOrigin", false, "Y Origin: "),
                              new StsFloatFieldBean(this, "depthMin", true, "Depth Min: "),
                              new StsFloatFieldBean(this, "depthMax", true, "Depth Max: "),
                            new StsFloatFieldBean(this, "depthInc", false, "Depth Inc: "),
                            //new StsButtonFieldBean("Reset","Reset the bounding box to user supplied limits.",this,"resetProjectBounds"),
                            new StsFloatFieldBean(this, "timeMin", false, "Time Min: "),
                            new StsFloatFieldBean(this, "timeMax", false, "Time Max: "),
                            new StsFloatFieldBean(this, "timeInc", false, "Time Inc: "),

                            new StsStringFieldBean(this, "timeUnitString", false, "Time Units: "),
                            new StsStringFieldBean(this, "depthUnitString", false, "Depth Units: "),
                            new StsStringFieldBean(this, "xyUnitString", false, "Distance Units: "),
                            //		zDomainBean,
                            zDomainSupportedBean,
                            new StsFloatFieldBean(this, "gridDX", false, "Grid dX: "),
                            new StsFloatFieldBean(this, "gridDY", false, "Grid dY: "),
                              new StsFloatFieldBean(this, "gridZ", true, "Grid Z: ", true)
                            //        new StsFloatFieldBean(this, "cultureZ", true, "Culture Z", true)
                    };
        }
        return propertyFields;
    }

    public StsFieldBean[] getDefaultFields() { return defaultFields; }

    /** Set the current Model related to this Project
     * @parameters model the current model
     */
    //    public void setModel(StsModel model) { this.model = model; }


	public void initializeDefaultBoundingBoxes()
    {
		constructDefaultBoundingBoxes();
		adjustDisplayBoundingBox();
	}

	/** Create the default bounding box */
    protected void constructDefaultBoundingBoxes()
    {
        unrotatedBoundingBox = new StsBoundingBox(false, "Project.unrotatedBoundingBox");
        rotatedBoundingBox = new StsRotatedGridBoundingBox(false, "Project.rotatedGridBoundingBox");
        displayBoundingBox = new StsDisplayBoundingBox(false, "Project.displayBoundingBox");
		cropVolume = new StsCropVolume(false, "Project.cropVolume");
        setToDefaultBoundingBoxes();
    }

	public void constructBoundingBoxes()
	{
       	unrotatedBoundingBox = new StsBoundingBox(false, "Project.unrotatedBoundingBox");
        rotatedBoundingBox = new StsRotatedGridBoundingBox(false, "Project.rotatedGridBoundingBox");
        displayBoundingBox = new StsDisplayBoundingBox(false, "Project.displayBoundingBox");
		cropVolume = new StsCropVolume(false, "Project.cropVolume");
	}

	private void setToDefaultBoundingBoxes()
	{
		setZDomain(TD_DEPTH);
        unrotatedBoundingBox.initialize(defaultBoxFloats, false);
        rotatedBoundingBox.initialize(unrotatedBoundingBox, defaultBoxIncs, false);
		cropVolume.reInitialize(rotatedBoundingBox);
		setBoxesInitialized(false);
		setZDomainSupported(TD_DEPTH);
	}

    /**
     * after db is defined, we need to set non-persistent boundingBoxes to persistent
     * so they will be written to db.
     */
    public void dbInitialize(StsModel model)
    {
        addToModel();
	/*
        if(rotatedBoundingBox != null)
        {
            rotatedBoundingBox.addToModel();
            dbFieldChanged("rotatedBoundingBox", rotatedBoundingBox);
        }
        if(unrotatedBoundingBox != null)
        {
            unrotatedBoundingBox.addToModel();
            dbFieldChanged("unrotatedBoundingBox", unrotatedBoundingBox);
        }
        if(displayBoundingBox != null)
        {
            displayBoundingBox.addToModel();
            dbFieldChanged("displayBoundingBox", displayBoundingBox);
        }

        if(cropVolume != null)
        {
            cropVolume.addToModel();
            dbFieldChanged("cropVolume", cropVolume);
        }
    */
        //         initializeZDomainAndRebuild();
    }

    /** call when any displayed project parameters have changed to update object panel */
    public void objectPanelChanged()
    {
        StsToolkit.runLaterOnEventThread(new Runnable()
        {
            public void run() { doObjectPanelChanged(); }
        });
    }

    private void doObjectPanelChanged()
    {
        objectPanel = getObjectPanel();
        objectPanel.refreshProperties();
    }

    /**
     * Get the project directories from the S2S user projects file. These are the directories to be displayed in the
     * File drop down.
     *
     * @returns projectDirectories a string array of project directories
     */
    public String[] getProjectDirectories()
    {
        if(projectDirectories == null)
        {
            String directory = getUserPropertiesDirectory();
            String filename = "s2s.user.projects";
            StsMessageFiles.infoMessage("Preferences will be stored in directory: " + directory);
            projectDirectories = readPathnamesFromFile(directory, filename);
        }
        return projectDirectories;
    }

    static public String getUserPropertiesDirectory()
    {
        return System.getProperty("user.home") + File.separator + "S2SCache" + File.separator;
    }

    /**
     * Add a project directory to the S2S user projects file. These directories will be displayed in the
     * File drop down.
     *
     * @parameters projectName the directory name
     */
    static public void addProjectDirectory(String projectName)
    {
        int nProjects = projectDirectories.length;
        for(int n = 0; n < nProjects; n++)
        {
            if(projectName.equals(projectDirectories[n]))
            {
                return;
            }
        }

        projectDirectories = (String[]) StsMath.arrayInsertElementBefore(projectDirectories, projectName, 0);
        String directory = getUserPropertiesDirectory();
        String filename = "s2s.user.projects";
        StsFile.checkDirectory(directory);
        StsFile file = StsFile.constructor(directory, filename);
        if(nProjects > 10)
            projectDirectories = (String[]) StsMath.trimArray(projectDirectories, 10);
        file.writeStringsToFile(projectDirectories);
    }

    /**
     * Get all the current user databases
     *
     * @returns projectDatabases an array of valid databases read from the user database file
     */
    static public String[] getDatabases()
    {
        if(projectDatabases == null)
        {
            String directory = getUserPropertiesDirectory();
            String filename = "s2s.user.databases";
            projectDatabases = readPathnamesFromFile(directory, filename);
        }
        return projectDatabases;
    }

      /**
       * Initialize the database information and add the database to the list of user databases
       *
       * @parameters dbFile the new Sts database file
       */
      public void setDatabaseInfo(StsDBFile dbFile)
      {
          try
          {
              //String modelDbPathname = dbFile.getURIString();
              //rootDirname = StsFile.getDirectoryFromPathname(modelDbPathname);
              //setRootDirectory(new File(rootDirname));
              String filename = dbFile.getFilename();
              name = filename.substring(filename.indexOf("db.")+3, filename.length());
              addDbPathname(dbFile.getURLPathname());
          }
          catch(Exception e)
          {
              StsException.outputWarningException(this, "setDatabaseInfo", e);
          }
      }

      private void initializeRootDirectory()
      {
          try
          {
            String modelDbPathname = currentModel.getDatabase().getURLDirectory();
            String rootDirname = StsFile.getDirectoryFromPathname(modelDbPathname);
            initializeRootDirectory(rootDirname);
          }
          catch(Exception e)
          {
              StsException.outputWarningException(this, "setDatabaseInfo", e);
          }
      }

      /**
       * Add a database pathname to the S2S user databases file.
       * The list is ordered from the most recent to the oldest.
       *
       * @parameters dbPathname pathname of the database
       */
      static public void addDbPathname(String dbPathname)
      {
          try
          {
              if(dbPathname == null) return;
              int nProjectDBs = 0;
              if(projectDatabases != null)
              {
                  nProjectDBs = projectDatabases.length;
                  for(int n = 0; n < nProjectDBs; n++)
                      if(dbPathname.equals(projectDatabases[n])) return;
              }

              projectDatabases = (String[])StsMath.arrayInsertElementBefore(projectDatabases, dbPathname, 0);
              String directory = getUserPropertiesDirectory();
              String filename = "s2s.user.databases";
              if(!StsFile.checkDirectory(directory)) return;
              StsFile file = StsFile.constructor(directory, filename);
              if(projectDatabases.length > 20)
                  projectDatabases = (String[])StsMath.trimArray(projectDatabases, 20);
              file.writeStringsToFile(projectDatabases);
          }
          catch(Exception e)
          {
              StsException.outputException("StsProject.addDbPathname() failed.",
                      e, StsException.WARNING);
          }
      }

    /**
     * Reads pathnames from the specified file. Validates that the line read is a file.
     *
     * @parameters directory directory name
     * @parameters filename file name
     * @returns pathnames an array of valid paths read from the file
     */
    static public String[] readPathnamesFromFile(String directory, String filename)
    {
        String[] pathnames = new String[0];
        String pathname;
        boolean rewrite = false;
        StsFile.checkDirectory(directory);

        StsFile file = StsFile.constructor(directory, filename);
        if(!file.openReader()) return pathnames;

        try
        {
            File f;

            while ((pathname = file.readLine()) != null)
            {
                if(pathname.startsWith("file:"))
                {
                    URL url = new URL(pathname);
                    f = new File(url.getFile());
                }
                else
                {
                    f = new File(pathname);

                }
                if(f == null || !f.exists())
                {
                    rewrite = true;
                    continue;
                }
                pathnames = (String[]) StsMath.arrayAddElement(pathnames, pathname);
            }
			file.closeReader();
            if(rewrite)
            {
                file.writeStringsToFile(pathnames);
            }
            file.close();
            return pathnames;
        }
        catch(Exception e)
        {
            systemError("Failed to read " + directory + filename);
            if(file != null)
            {
                file.closeWriter();
            }
            return null;
        }
    }

    /*
     OutputStream os = new OutputStream();
=======
 /*
     OutputStream os = new OutputStream();
>>>>>>> 1.314
     file.
     PersistentStrings persistentStrings = PersistentStrings.read(filename);
     ArrayList projectDirectories = persistentStrings.getStringsArrayList();
    rootDirname = StsSelectProjectDirectoryDialog.getProjectDirectory("Select project directory", projectDirectories);
     rootDirectory = new File(rootDirname);
     projectDirectories.remove(rootDirname);
     projectDirectories.add(0, rootDirname);
     persistentStrings.write(filename);
    }
    catch(Exception e)
    {
     StsException.outputException("StsProject.getUserPreferences() failed.",
    e, StsException.WARNING);
    }
    }
    */
    /*
       public void loadUserPreferences()
       {
        try
        {
         String filename = System.getProperty("java.home") + File.separator + "s2s.user.projects";
         PersistentStrings persistentStrings = PersistentStrings.read(filename);
         ArrayList projectDirectories = persistentStrings.getStringsArrayList();
       rootDirname = StsSelectProjectDirectoryDialog.getProjectDirectory("Select project directory", projectDirectories);
         rootDirectory = new File(rootDirname);
         projectDirectories.remove(rootDirname);
         projectDirectories.add(0, rootDirname);
         persistentStrings.write(filename);
        }
        catch(Exception e)
        {
         StsException.outputException("StsProject.getUserPreferences() failed.",
        e, StsException.WARNING);
        }
       }
       */

    /**
     * Add a user preference to the database
     *
     * @parameters key the name to refer to the user preference
     * @parameters value the current value for the key
     */
    public void addUserPreference(String key, String value)
    {
        userPreferences.setProperty(key, value);
    }

    /**
     * Get a user preference from the database
     *
     * @parameters key the name to refer to the user preference
     * @parameters value the current value for the key
     */
    public String getUserPreference(String key)
    {
        return userPreferences.getProperty(key);
    }

    /**
     * Has the bounding box origin been explicitly set
     *
     * @return true if set
     */
    public boolean isOriginSet()
    {
		if(rotatedBoundingBox == null) return false;
        return rotatedBoundingBox.originSet;
    }

	public boolean checkSetOrigin(double xOrigin, double yOrigin)
	{
		if(unrotatedBoundingBox == null) return false;
		if(unrotatedBoundingBox.originSet) return false;
		initializeOriginAndRange(xOrigin, yOrigin);
		return true;
	}

    public boolean checkSetOrigin(double[] xyOrigin)
    {
        if(unrotatedBoundingBox == null) return false;
        if(unrotatedBoundingBox.originSet) return false;
        initializeOriginAndRange(xyOrigin[0], xyOrigin[1]);
        return true;
    }

    /** set origin of bounding boxes and reinitialize the boundingBox limits */
    private void initializeOriginAndRange(double xOrigin, double yOrigin)
    {
        unrotatedBoundingBox.initializeOriginRange(xOrigin, yOrigin);
        rotatedBoundingBox.initializeOriginRange(xOrigin, yOrigin);
    }

    /**
     * if angle is already set, and new angle is not the same, return false indicated this object can't be loaded;
     * if angle is not already set and new angle is
     */
    public boolean checkSetAngle(float angle)
    {

        if(rotatedBoundingBox == null) return false;

        boolean angleSet = rotatedBoundingBox.getAngleSet();
        float currentAngle = rotatedBoundingBox.angle;
        if(angleSet)
        {
            if(!StsMath.sameAsTol(currentAngle, angle, angleTol))
            {
                new StsMessage(currentModel.win3d, StsMessage.WARNING,
                        "Grid angle: " + angle + " differs from current project angle: " + currentAngle + "\n" +
                                "by more than " + angleTol + " degrees.\n" +
                                "Can't load this object.");
                return false;
            }
        }
        else
        {
			rotatedBoundingBox.reinitializeBoundingBox();
            rotatedBoundingBox.setAngle(angle);

        }
        TreeSet<StsUnrotatedClass> unrotatedClasses = currentModel.unrotatedClasses;
        int nUnrotatedClasses = unrotatedClasses.size();
        if(currentAngle != angle)
        {
            for(StsUnrotatedClass unrotatedClass : unrotatedClasses)
                unrotatedClass.projectRotationAngleChanged();
        }
        return true;
    }

    /**
     * Initialize the rotated bounding box by setting the origin and rotation angle
     *
     * @parameters xOrigin actual x origin
     * @parameters yOrigin actual y origin
     * @parameters angle the angle off true North
     */
    public void setOriginAndAngle(double xOrigin, double yOrigin, float angle)
    {
        rotatedBoundingBox.checkSetOriginAndAngle(xOrigin, yOrigin, angle);
    }

    /**
     * Compute the relative origin of the rotated bounding box
     *
     * @return xOrigin, yOrigin
     */
    public float[] computeRelativeOrigin(double xOrigin, double yOrigin)
    {
        if(rotatedBoundingBox == null)
        {
            return null;
        }
        else
        {
            return rotatedBoundingBox.computeRelativeOrigin(xOrigin, yOrigin);
        }
    }

    //TODO Replaced with addToProject(StsBoundingBox).  Test!
/*
    public boolean addToProject(StsCultureObjectSet2D cSet)
    {
        try
        {
            setZDomainSupported(TD_TIME_DEPTH); // Always both for culture since it defaults to z of current slice.
            float timeMin = (float) cSet.getMinTime();
            float timeMax = (float) cSet.getMaxTime();
            float depthMin = (float) cSet.getMinDepth();
            float depthMax = (float) cSet.getMaxDepth();

            double xOrigin = cSet.getXMax();
            double yOrigin = cSet.getYMax();

            // this sets origin of rotated and unrotated boundingBoxes if not already set
            checkSetOrigin(xOrigin, yOrigin);

            int nObjectsInSet = cSet.getCultureObjects().length;
            for(int i = 0; i < nObjectsInSet; i++)
            {
                StsCultureObject2D object = cSet.getCultureObjects()[i];
                for(int j = 0; j < object.getNumPoints(); j++)
                    unrotatedBoundingBox.addXY(object.getPointAt(j), this.getXOrigin(), this.getYOrigin());
            }
            setBoundingBoxesZTRange(depthMin, depthMax, TD_DEPTH);
            setBoundingBoxesZTRange(timeMin, timeMax, TD_TIME);
            displayBoundingBox.addRotatedBoundingBox(unrotatedBoundingBox);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.addToProject(StsLine) failed.", e, StsException.WARNING);
            return false;
        }
    }
*/
    public float[] niceZTScale(float zMin, float zMax)
    {
        return StsMath.niceScale(zMin, zMax, approxNumZTIncs, true);
    }

    /** A set of objects have unrotated and rotated boundingBoxes around them.  Add these to the project.
     *  After adding the unrotatedBoundingBox, adjust the unrotated displayBoundingBox XY to be "nice" values.
     *  If the rotatedBoundingBox has had its angle set (it's still 0.0), then set it to the displayBoundingBox.
     */
    public void adjustBoundingBoxes(boolean changed)
    {
        adjustDisplayBoundingBox();
        rangeChanged();
		adjustRotatedBoundingBox();
        // if(changed) boundingBoxesChanged();
    }

	public boolean addToProjectUnrotatedBoundingBox(StsBoundingBox box)
    {
		return addToProjectUnrotatedBoundingBox(box, box.getZDomainOriginal());
	}

	public boolean addToProjectUnrotatedBoundingBox(StsBoundingBox box, byte zDomainPreferred)
    {
		return addToProjectUnrotatedBoundingBoxes(new StsBoundingBox[] { box }, zDomainPreferred);
    }

	public boolean addToProjectUnrotatedBoundingBox(StsBoundingBox box, int nFirstIndex, int nValues)
    {
		return addToProjectUnrotatedBoundingBox(box, box.getZDomainOriginal());
	}

    public boolean addToProjectUnrotatedBoundingBoxes(StsBoundingBox[] unrotatedBoundingBoxes, byte zDomainPreferred)
    {
        boolean changed = false;
        for (StsBoundingBox box : unrotatedBoundingBoxes)
        {
            changed = changed | addUnrotatedBoundingBox(box);
            // box.checkComputeRotatedPoints(getRotatedBoundingBox());
            adjustZDomainSupported(box.getZDomainSupported());
        }
        adjustBoundingBoxes(changed);
        changed = changed | displayBoundingBox.niceAdjustBoundingBoxXY();
		checkSetZDomainRun(zDomainPreferred);
        return true;
    }

	public boolean addToProjectUnrotatedBoundingBoxes(ArrayList<StsVectorSetObject> unrotatedBoundingBoxes, byte zDomainPreferred)
	{
		boolean changed = false;
		for (StsVectorSetObject vectorSetObject : unrotatedBoundingBoxes)
		{
			changed = changed | addUnrotatedBoundingBox(vectorSetObject);
			// box.checkComputeRotatedPoints(getRotatedBoundingBox());
			adjustZDomainSupported(vectorSetObject.getZDomainSupported());
		}
		adjustBoundingBoxes(changed);
		changed = changed | displayBoundingBox.niceAdjustBoundingBoxXY();
		checkSetZDomainRun(zDomainPreferred);
		return true;
	}

    private boolean addUnrotatedBoundingBox(StsBoundingBox objectUnrotatedBoundingBox)
    {
        checkInitializeBoxes();
        // checkSetOrigin(objectUnrotatedBoundingBox);
        // boolean changed = rotatedBoundingBox.addUnrotatedBoundingBox(objectUnrotatedBoundingBox);
        boolean changed =  unrotatedBoundingBox.addBoundingBox(objectUnrotatedBoundingBox);
		changed = changed | rotatedBoundingBox.addUnrotatedBoundingBox(objectUnrotatedBoundingBox);
		if(rotatedBoundingBox.adjustZTRange(objectUnrotatedBoundingBox))
		{
			resetCropVolume();
			changed = true;
		}
		return changed;
    }

    private void checkInitializeBoxes()
    {
        if(isBoxesInitialized()) return;
		reinitializeBoundingBoxes();
		zDomainSupported = TD_NONE;
		zDomain = TD_NONE;
        setBoxesInitialized(true);
    }

	/** If project origin is not set, set it for boundingBoxes and return true. Otherwise if set, return false. */
    public boolean checkSetOrigin(StsBoundingBox box)
    {
        if(displayBoundingBox.originSet) return false;
        displayBoundingBox.setOrigin(box.xOrigin, box.yOrigin);
        rotatedBoundingBox.setOrigin(box.xOrigin, box.yOrigin);
        unrotatedBoundingBox.setOrigin(box.xOrigin, box.yOrigin);
 		return true;
    }

	public boolean addToProject(StsRotatedGridBoundingBox rotatedBoundingBox, byte zDomainPreferred)
	{
		return addToProjectRotatedBoundingBox(rotatedBoundingBox, zDomainPreferred);
	}

    public boolean addToProject(StsBoundingBox boundingBox, byte zdomain)
    {
        if(!checkSetZDomainRun(zdomain)) return false;

        if(!unrotatedBoundingBox.isBoxOriginOutsideBoxXYLimit(boundingBox, distanceLimit))
        {
            if(!StsYesNoDialog.questionValue(currentModel.win3d, boundingBox.getName() + " has data out of range limit (" + distanceLimit + " " + this.getXyUnitString() + ").\n\n" +
                    "   Do you want to continue?\n"))
                return false;
        }
        unrotatedBoundingBox.addBoundingBox(boundingBox);
        displayBoundingBox.addBoundingBox(unrotatedBoundingBox);
        return true;
    }

    public boolean isBoundingBoxOutsideXYLimits(StsBoundingBox boundingBox)
    {
        if(!unrotatedBoundingBox.isBoxOriginOutsideBoxXYLimit(boundingBox, distanceLimit)) return false;
		return !StsYesNoDialog.questionValue(currentModel.win3d, boundingBox.getName() + " has data out of range limit (" +
				distanceLimit + " " + this.getXyUnitString() + ").\n\n" + "   Do you want to continue?\n");
    }

    static public boolean supportsTime(byte zDomain)
    {
        return zDomain == TD_TIME_DEPTH || zDomain == TD_TIME;
    }

    static public boolean supportsDepth(byte zDomain)
    {
        return zDomain == TD_TIME_DEPTH || zDomain == TD_DEPTH;
    }

    public boolean supportsTime()
    {
        return supportsTime(zDomainSupported);
    }

    public boolean supportsDepth()
    {
        return supportsDepth(zDomainSupported);
    }

    public boolean supportsZDomain(byte objectZDomain)
    {
        return zDomainSupported == TD_TIME_DEPTH || objectZDomain == zDomain;
    }

    public boolean canDisplayZDomain(byte objectZDomain)
    {
        return objectZDomain == TD_TIME_DEPTH || objectZDomain == zDomain;
    }

    public boolean hasVelocityModel()
    {
        if(velocityModel == null)
            return false;
        else
            return true;
    }
    /*
          public boolean addZToProject(StsSeismicBoundingBox box)
          {
              byte objectZDomain = StsParameters.getZDomainFromString(box.zDomain);
              if (objectZDomain == TD_DEPTH)
                  {
                      depthMin = rotatedBoundingBox.zMin;
                      depthMax = rotatedBoundingBox.zMax;
                      depthInc = rotatedBoundingBox.zInc;
      //				fieldChanged("depthIncLocked", true);
                  }
                  else if (zDomain == TD_TIME)
                  {
                      timeMin = rotatedBoundingBox.zMin;
                      timeMax = rotatedBoundingBox.zMax;
                      timeInc = rotatedBoundingBox.zInc;
      //				fieldChanged("timeIncLocked", true);
                  }
                  rangeChanged();
                  objectPanelChanged();
             return true;
          }
      */
    //TODO need to have a flag saying a time or depth congruent volume has been loaded and any changes to project z range need to be congruent with this
	//TODO Status:  angleSet should be true for 3d volumes and false for 2d lines; move the usage of this method to addToProjectRotatedBoundingBox
	//TODO and use this angleSet state as appropriate
    public boolean addToProject(StsSeismicBoundingBox volume, boolean setAngle)
    {
        try
        {
            // check data

            if(volume.getZMax() <= volume.getZMin())
            {
                new StsMessage(currentModel.win3d, StsMessage.WARNING, "Volume zMax " + volume.getZMax() + " <= zMin " + volume.getZMin() + "\nNot loading volume.");
                return false;
            }
            byte objectZDomain = StsParameters.getZDomainFromString(volume.zDomain);
            if(!checkSetZDomainRun(objectZDomain)) return false;

            if(setAngle)
            {
                boolean angleAlreadySet = rotatedBoundingBox.getAngleSet();
                if(!checkSetAngle(volume.angle)) return false;

                // If angle not already set, then the current rotatedBoundingBox is same as the
                // project.unrotatedBoundingBox. So if we have set the angle now, we start with
                // a new unrotatedBoundingbox and add this rotated object to it.
                if(!angleAlreadySet) rotatedBoundingBox.reinitializeBoundingBox();
            }
            double volumeXOrigin = volume.xOrigin;
            double volumeYOrigin = volume.yOrigin;
            boolean originChanged = checkSetOrigin(volumeXOrigin, volumeYOrigin);
            float[] xy = getRotatedRelativeXYFromUnrotatedAbsXY(volumeXOrigin, volumeYOrigin);
            volume.xMin += xy[0];
            volume.yMin += xy[1];
            volume.xMax += xy[0];
            volume.yMax += xy[1];
            volume.xOrigin = getXOrigin();
            volume.yOrigin = getYOrigin();

            if(zDomain != objectZDomain)
            {
                systemError("Error in StsProject.addToProject(StsSeismicBoundingBox).\n" +
                        "Cannot add new volume to current boundingBoxes as volume domain is " +
                        StsParameters.TD_ALL_STRINGS[objectZDomain] +
                        " and project zDomain is " + StsParameters.TD_ALL_STRINGS[zDomain] + ".");
                return false;
            }
			//TODO this congruence check should be done by the loader and load should be rejected if need be at that point
			if(!rotatedBoundingBox.isXYZCongruent(volume)) return false;
            boolean changed = rotatedBoundingBox.addRotatedGridBoundingBox(volume);
			if(changed) resetCropVolume();
            unrotatedBoundingBox.addRotatedBoundingBox(rotatedBoundingBox);
            checkChangeZRange(volume, objectZDomain);
//            adjustBoundingBoxes(true, false);
            //            runCompleteLoading();
            //setCursorDisplayXYAndGridCheckbox(false);  //how do you know this project is 3d? SWC 8/31/09
            setCursorDisplayXYAndGridCheckbox(false);  //reenable wrw
            resetCropVolume();
            //           currentModel.win3d.cursor3dPanel.setSliderValues();
            //            rangeChanged();
            objectPanelChanged();
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.addToProject() failed.", e, StsException.WARNING);
            return false;
        }
    }

    public void setCursorDisplayXYAndGridCheckbox(final boolean isXY)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                StsWin3dFull[] windows = currentModel.getParentWindows();
                for(int n = 0; n < windows.length; n++)
                    windows[n].setCursorDisplayXYAndGridCheckbox(isXY);
            }
        };
        StsToolkit.runLaterOnEventThread(runnable);
    }

    /**
     * This seismicBoundingBox bounds an unrotated seismic object such as a 2D line which has no inherent rotation angle
     * and need not be congruent with existing rotated objects (seismicVolumes and surfaces).
     * If an origin has already been set by some object, then compute x and y min and max relative to this origin to represent
     * an unrotated bounding box around this object.
     * If an origin has not been set, then set the origin from this unrotated seismic object.  Once fixed, the origin can't
     * be moved.
     * Subsequent to this operation if a rotatable seismic object is loaded it will use the same origin, but will set the
     * angle to something other than zero which redefines the local coordinate system.  Any unrotatedClasses will have
     * their appropriate coordinates recomputed in this new rotated coordinate system.  These unrotatedClasses will be
     * deleted from the current list indicating they have been dealt with.
     *
     * @return
     */
    public boolean addUnrotatedBoxToProject(StsSeismic volume)
    {
        try
        {
            byte objectZDomain = StsParameters.getZDomainFromString(volume.zDomain);
            if(!checkSetZDomainRun(objectZDomain)) return false;

            unrotatedBoundingBox.addBoundingBox(volume);

            if(zDomain != objectZDomain)
            {
                systemError("Error in StsProject.addToProject(StsSeismicBoundingBox).\n" +
                        "Cannot add new volume to current boundingBoxes as volume domain is " +
                        StsParameters.TD_ALL_STRINGS[objectZDomain] +
                        " and project zDomain is " + StsParameters.TD_ALL_STRINGS[zDomain] + ".");
                return false;
            }

            checkChangeZRange(volume, objectZDomain);
              rangeChanged();
              objectPanelChanged();
              return true;
          }
          catch(Exception e)
          {
              StsException.outputException("StsProject.addToProject() failed.", e, StsException.WARNING);
              return false;
          }
      }

    /**
     * if we already have a volume loaded that we need to be congruent with in Z, adjust z range accordingly.
     * Otherwise, this volume will be considered the congruent basis for any subsequent volume load or velocity model construction.
     */
    private void checkChangeZRange(StsSeismicBoundingBox volume, byte zDomain)
    {
        checkChangeZRange(volume.getZMin(), volume.getZMax(), volume.zInc, zDomain);
        setIncLock(zDomain);
    }

    private void checkChangeZRange(float ztMin, float ztMax, float ztInc, byte zDomain)
    {
        if(ztInc == 0.0f)
        {
            systemError(this, "checkChangeZRange", "ztInc cannot be zero.");
            return;
        }
        if(zDomain == TD_DEPTH)
        {
            if(depthIncLocked)
            {

				if(ztInc < rotatedBoundingBox.zInc && StsMath.isIntegralRatio(ztInc, rotatedBoundingBox.zInc, 0.0f))
					rotatedBoundingBox.zInc = ztInc;
				float newDepthMin = StsMath.intervalRoundDown(ztMin, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc);
                float newDepthMax = StsMath.intervalRoundUp(ztMax, rotatedBoundingBox.getZMax(), rotatedBoundingBox.zInc);
                rotatedBoundingBox.setZRange(newDepthMin, newDepthMax);
            }
            else
            {
                float newDepthMin = Math.min(ztMin, rotatedBoundingBox.getZMin());
                setDepthInc(ztInc);
                newDepthMin = StsMath.intervalRoundDown(newDepthMin, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc);
                float newDepthMax = Math.max(ztMax, rotatedBoundingBox.getZMax());
                newDepthMax = StsMath.intervalRoundUp(newDepthMax, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc);
                rotatedBoundingBox.setZRange(newDepthMin, newDepthMax);
                depthIncSet();
            }
        }
        else if(zDomain == TD_TIME)
        {
            if(timeIncLocked)
            {
				if(ztInc < rotatedBoundingBox.tInc && StsMath.isIntegralRatio(ztInc, rotatedBoundingBox.tInc, 0.0f))
					rotatedBoundingBox.tInc = ztInc;
                float newTimeMin = StsMath.intervalRoundDown(ztMin, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc);
                float newTimeMax = StsMath.intervalRoundUp(ztMax, rotatedBoundingBox.tMax, rotatedBoundingBox.tInc);
                rotatedBoundingBox.setTRange(newTimeMin, newTimeMax);
            }
            else
            {
                float newTimeMin = Math.min(ztMin, rotatedBoundingBox.tMin);
                setTimeInc(ztInc);
                newTimeMin = StsMath.intervalRoundDown(newTimeMin, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc);
                float newTimeMax = Math.max(ztMax, rotatedBoundingBox.tMax);
                newTimeMax = StsMath.intervalRoundUp(newTimeMax, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc);
                rotatedBoundingBox.setTRange(newTimeMin, newTimeMax);
                timeIncSet();
            }
        }
    }

    public void checkChangeZRange(float zMin, float zMax, byte zDomain)
    {
        if(zDomain == TD_DEPTH)
        {
            setDepthMin(StsMath.intervalRoundDown(zMin, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc));
            setDepthMax(StsMath.intervalRoundUp(zMax, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc));
        }
        else if(zDomain == TD_TIME)
        {
            setTimeMin(StsMath.intervalRoundDown(zMin, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc));
            setTimeMax(StsMath.intervalRoundUp(zMax, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc));
        }
    }

    public void checkChangeCongruentZMin(float zMin, byte zDomain)
    {
        if(zDomain == TD_DEPTH)
        {
            if(depthIncLocked)
                setDepthMin(StsMath.intervalRoundDown(zMin, rotatedBoundingBox.getZMin(), rotatedBoundingBox.zInc));
            else
                setDepthMin(zMin);
        }
        else if(zDomain == TD_TIME)
        {
            if(timeIncLocked)
                setTimeMin(StsMath.intervalRoundDown(zMin, rotatedBoundingBox.tMin, rotatedBoundingBox.tInc));
            else
                setTimeMin(zMin);
        }
    }

    public void checkSetDepthRangeForVelocityModel()
    {
        if(velocityModel == null) return;
        velocityModel.checkSetDepthRange();
    }

    public void checkSetDepthRange(float maxAvgVelocity, float depthDatum, float timeDatum)
    {
        float newDepthMin = depthDatum + maxAvgVelocity * (rotatedBoundingBox.tMin - timeDatum);
        float newDepthMax = depthDatum + maxAvgVelocity * (rotatedBoundingBox.tMax - timeDatum);
        checkSetDepthRange(newDepthMin, newDepthMax);
    }

    public void checkSetDepthRange(float depthMin, float depthMax)
    {
        if(depthMin >= rotatedBoundingBox.getZMin() && depthMax <= rotatedBoundingBox.getZMax()) return;
        float newDepthMin = Math.min(depthMin, rotatedBoundingBox.getZMin());
        float newDepthMax = Math.max(depthMax, rotatedBoundingBox.getZMax());
        int approxNInc;
        if(rotatedBoundingBox.zInc != 0.0f)
            approxNInc = StsMath.ceiling((newDepthMax - newDepthMin) / rotatedBoundingBox.zInc);
        else
            approxNInc = 100;

        float[] scale = StsMath.niceScale(newDepthMin, newDepthMax, approxNInc, true);
        setDepthMin(scale[0]);
        setDepthMax(scale[1]);
        setDepthInc(scale[2]);
        objectPanelChanged();
    }

    public void checkSetDepthMax(float depthMax)
    {
        if(depthMax < rotatedBoundingBox.getZMax()) return;

        int approxNInc;
        if(rotatedBoundingBox.zInc != 0.0f)
            approxNInc = StsMath.ceiling((rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin()) / rotatedBoundingBox.zInc);
        else
            approxNInc = 100;

        float[] scale = StsMath.niceScale(rotatedBoundingBox.getZMin(), depthMax, approxNInc, true);
        setDepthMin(scale[0]);
        setDepthMax(scale[1]);
        setDepthInc(scale[2]);
        objectPanelChanged();
    }


    public void setDepthMin(float depthMin)
    {
        if(displayBoundingBox.getZMin() <= depthMin) return;
        if(debug) StsException.systemDebug(this, "setDepthMin", "zMin changed from " + rotatedBoundingBox.getZMin() + " to " + depthMin);
        displayBoundingBox.setZMin(depthMin);
        displayBoundingBox.dbFieldChanged("zMin", depthMin);
        currentModel.win3dDisplay();
    }

    public float getDepthMin() { return displayBoundingBox.getZMin(); }

    public void setDepthMax(float depthMax)
    {
        if(displayBoundingBox.getZMax() >= depthMax) return;
        if(debug) StsException.systemDebug(this, "setDepthMax", "zMax changed from " + rotatedBoundingBox.getZMax() + " to " + depthMax);
        displayBoundingBox.setZMax(depthMax);
        displayBoundingBox.dbFieldChanged("zMax", depthMax);
        currentModel.win3dDisplay();
    }

    public float getDepthMax() { return displayBoundingBox.getZMax(); }

    public void setTimeMin(float timeMin)
    {
        if(displayBoundingBox.tMin <= timeMin) return;
        if(debug) StsException.systemDebug(this, "setTimeMin", "tMin changed from " + rotatedBoundingBox.tMin + " to " + timeMin);
        displayBoundingBox.setTMin(timeMin);
        displayBoundingBox.dbFieldChanged("tMin", timeMin);
        currentModel.win3dDisplay();
    }

    public float getTimeMin() { return displayBoundingBox.tMin; }

    public void setTimeMax(float timeMax)
    {
        setGridT(timeMax);
        if(displayBoundingBox.tMax >= timeMax) return;
        if(debug) StsException.systemDebug(this, "setTimeMax", "tMax changed from " + rotatedBoundingBox.tMax + " to " + timeMax);
        displayBoundingBox.setTMax(timeMax);
        displayBoundingBox.dbFieldChanged("tMax", timeMax);

        currentModel.win3dDisplay();
    }

    public float getTimeMax() { return displayBoundingBox.tMax; }

    ;

    public void checkSetTimeInc(float tInc)
    {
        if(rotatedBoundingBox.tInc == tInc) return;
        if(timeIncLocked || rotatedBoundingBox.tInc != 0.0f && tInc >= rotatedBoundingBox.tInc) return;
        rotatedBoundingBox.setTInc(tInc);
        rotatedBoundingBox.dbFieldChanged("tInc", rotatedBoundingBox.tInc);
    }

    public void checkSetDepthInc(float zInc)
    {
        if(rotatedBoundingBox.zInc == zInc) return;
        if(depthIncLocked || rotatedBoundingBox.zInc != 0.0f && zInc >= rotatedBoundingBox.zInc) return;
        rotatedBoundingBox.setZInc(zInc);
        rotatedBoundingBox.dbFieldChanged("zInc", zInc);
    }

    private void setDepthInc(float zInc)
    {
        int nDepthIncs = StsMath.ceiling((rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin()) / zInc);
        if(nDepthIncs > nZTIncsMax)
        {
            float[] scale = StsMath.niceScale(rotatedBoundingBox.getZMin(), rotatedBoundingBox.getZMax(), nZTIncsMax, true);
            zInc = scale[2];
        }
        zInc = Math.max(zInc, minZInc);
        rotatedBoundingBox.zInc = zInc;
        rotatedBoundingBox.dbFieldChanged("zInc", zInc);
    }

    private void setTimeInc(float tInc)
    {
        int nTimeIncs = StsMath.ceiling((rotatedBoundingBox.tMax - rotatedBoundingBox.tMin) / tInc);
        if(nTimeIncs > nZTIncsMax)
        {
            float[] scale = StsMath.niceScale(rotatedBoundingBox.tMin, rotatedBoundingBox.tMax, nZTIncsMax, true);
            tInc = scale[2];
        }
        tInc = Math.max(tInc, minZInc);
        rotatedBoundingBox.tInc = tInc;
        rotatedBoundingBox.dbFieldChanged("tInc", tInc);
    }

    public void setIncLock(byte zDomain)
    {
        if(zDomain == TD_DEPTH)
            depthIncSet();
        else if(zDomain == TD_TIME)
            timeIncSet();

    }

    public void depthIncSet()
    {
        if(depthIncLocked) return;
        fieldChanged("depthIncLocked", true);
    }

    public void timeIncSet()
    {
        if(timeIncLocked) return;
        fieldChanged("timeIncLocked", true);
    }


    public void rangeChanged()
    {
        resetCropVolume();
		if(currentModel.win3d == null) return;
        if(currentModel.win3d.cursor3dPanel == null) return;
		currentModel.cursorRangeChanged();
        // currentModel.win3d.cursor3dPanel.setSliderValues();
    }

    public boolean addToProjectRotatedBoundingBox(StsRotatedGridBoundingBox objectRotatedBoundingBox, byte zDomainPreferred)
    {
		currentModel.disableDisplay();
		try
		{
			if(!checkSetAngle(objectRotatedBoundingBox.angle)) return false;
			boolean changed;
			checkInitializeBoxes();
			checkSetOrigin(objectRotatedBoundingBox);
			changed = rotatedBoundingBox.addRotatedGridBoundingBox(objectRotatedBoundingBox);
			if(changed) resetCropVolume();
			// load operation should have already checked that congruent and incs set above in addRotatedGridBoundingBox
			// rotatedBoundingBox.checkMakeCongruent(objectRotatedBoundingBox);
			changed = changed | unrotatedBoundingBox.addRotatedBoundingBox(objectRotatedBoundingBox);
			if(changed)
			{
				adjustDisplayBoundingBox();
				displayBoundingBox.niceAdjustBoundingBoxXY();
				// displayBoundingBox.niceAdjustBoundingBoxZ(objectRotatedBoundingBox, approxNumZTIncs, zDomain);
				rangeChanged();
				// boundingBoxesChanged();
				adjustRotatedBoundingBox();
			}
            this.checkSetZDomain(objectRotatedBoundingBox.getZDomainSupported(), zDomainPreferred);
			objectPanelChanged();
			return true;
		}
		finally
		{
			currentModel.enableDisplay();
		}
    }

    public void adjustRotatedBoundingBoxOrigin(StsRotatedGridBoundingBox box)
    {
        float[] xy = getRotatedRelativeXYFromUnrotatedAbsXY(box.xOrigin, box.yOrigin);
        box.xMin = xy[0];
        box.yMin = xy[1];
        box.xMax = box.xMin + (box.nCols - 1) * box.xInc;
        box.yMax = box.yMin + (box.nRows - 1) * box.yInc;
        box.xOrigin = getXOrigin();
        box.yOrigin = getYOrigin();
    }

    /**
     * Get the Project minimum in X
     *
     * @return minimum X
     */
    public float getXMin()
    {
        return rotatedBoundingBox.xMin;
    }

    /**
     * Get Project maximum in X
     *
     * @return maximum X
     */
    public float getXMax()
    {
        return rotatedBoundingBox.xMax;
    }

    /**
     * Get the Project size in X dimension
     *
     * @return size in X
     */
    public float getXSize()
    {
        return rotatedBoundingBox.xMax - rotatedBoundingBox.xMin;
    }

    /**
     * Get the X increment - actual
     *
     * @return X increment
     */
    public float getXInc()
    {
        return rotatedBoundingBox.xInc;
    }

    /**
     * Get the project grid displayed x increment (dotted line spacing in project grid)
     *
     * @return project grid displayed x increment
     */
    public float getGridDX()
    {
        return displayBoundingBox.gridDX;
    }

    /**
     * Get absolute X origin
     *
     * @return absolute X origin
     */
    public double getXOrigin()
    {
        return rotatedBoundingBox.xOrigin;
    }

    /**
     * Get X at center of Project
     *
     * @return center of gravity X
     */
    public float getXCenter()
    {
        return rotatedBoundingBox.getXCenter();
    }

    /**
     * Get the Project minimum in Y
     *
     * @return minimum y
     */
    public float getYMin()
    {
        return rotatedBoundingBox.yMin;
    }

    /**
     * Get Project maximum in Y
     *
     * @return maximum Y
     */
    public float getYMax()
    {
        return rotatedBoundingBox.yMax;
    }

    /** get slice coordinate from local z coordinate */
    public int getNearestSliceCoor(boolean isDepth, float z)
    {
        return rotatedBoundingBox.getNearestSliceCoor(isDepth, z);
    }

    public String toString()
    {
        return "Project";
    }

    public boolean isPlanar()
    {
        return true;
    }

    /**
     * Get the Project Grid Frequency as String
     *
     * @return grid frequency in model units
     */
    public String getGridFrequencyString()
    {
        if(gridFrequency == AUTO)
        {
            return new String("Automatic");
        }
        else
        {
            return new Float(gridFrequency).toString();
        }
    }

    /**
     * Get the Project Grid Frequency as String
     *
     * @return grid frequency in model units
     */
    public String getZGridFrequencyString()
    {
        if(zGridFrequency == AUTO)
        {
            return new String("Automatic");
        }
        else
        {
            return new Float(zGridFrequency).toString();
        }
    }

    /**
     * Get Project Label Frequency String
     *
     * @return label frequency in model units
     */
    public String getLabelFrequencyString()
    {
        if(labelFrequency == AUTO)
        {
            return new String("Automatic");
        }
        else
        {
            return new Float(labelFrequency).toString();
        }
    }

    /**
     * Get the Project Grid Frequency
     *
     * @return grid frequency in model units
     */
    public float getGridFrequency()
    {
        return gridFrequency;
    }

    /**
     * Get Project Label Frequency
     *
     * @return label frequency in model units
     */
    public float getLabelFrequency()
    {
        return labelFrequency;
    }

    /** Set the Project Z Grid Frequency */
    public void setZGridFrequency(float freq)
    {
        zGridFrequency = freq;
        // dbFieldChanged("zGridFrequency", freq);
        return;
    }

    /** Set the Project Grid Frequency */
    public void setGridFrequency(float freq)
    {
        gridFrequency = freq;
        // dbFieldChanged("gridFrequency", freq);
        return;
    }

    /** Set Project Label Frequency */
    public void setLabelFrequency(float freq)
    {
        labelFrequency = freq;
        //		setDisplayField("labelFrequency", freq);
        //		dbFieldChanged("labelFrequency", freq);
        return;
    }

    /** Set the Project Grid Frequency String */
    public void setGridFrequencyString(String freq)
    {
        if(freq.equals("Auto") || freq.equals("Automatic"))
        {
            gridFrequency = AUTO;
        }
        else
        {
            gridFrequency = (new Float(freq)).floatValue();
            if((labelFrequency % gridFrequency != 0) && (labelFrequency != AUTO))
            {
                new StsMessage(currentModel.win3d, StsMessage.WARNING, "Label frequency must be a multiple of grid frequency.");
                setGridFrequency(AUTO);
            }
            currentModel.win3dDisplayAll();
        }
        // dbFieldChanged("gridFrequency", gridFrequency);
        return;
    }

    /** Set the Project Grid Frequency String */
    public void setZGridFrequencyString(String freq)
    {
        if(freq.equals("Auto") || freq.equals("Automatic"))
        {
            zGridFrequency = AUTO;
        }
        else
        {
            zGridFrequency = (new Float(freq)).floatValue();
            if((labelFrequency % zGridFrequency != 0) && (labelFrequency != AUTO))
            {
                new StsMessage(currentModel.win3d, StsMessage.WARNING, "Label frequency must be a multiple of grid frequency.");
                setGridFrequency(AUTO);
            }
            currentModel.win3dDisplayAll();
        }
        // dbFieldChanged("zGridFrequency", zGridFrequency);
        return;
    }

    public float getZGridFrequency()
    {
        return zGridFrequency;
    }

    /** Set Project Label Frequency String */
    public void setLabelFrequencyString(String freq)
    {
        if(freq.equals("Auto") || freq.equals("Automatic"))
        {
            labelFrequency = AUTO;
        }
        else
        {
            labelFrequency = (new Float(freq)).floatValue();
            if((labelFrequency % gridFrequency != 0) && (gridFrequency != AUTO))
            {
                new StsMessage(currentModel.win3d, StsMessage.WARNING, "Label frequency must be a multiple of grid frequency.");
                setLabelFrequency(AUTO);
            }
            currentModel.win3dDisplayAll();
        }
        // dbFieldChanged("labelFrequency", labelFrequency);
        return;
    }

    /**
     * Get the Project size in Y dimension
     *
     * @return size in Y
     */
    public float getYSize()
    {
        return rotatedBoundingBox.yMax - rotatedBoundingBox.yMin;
    }

    public float getSize()
    {
        return Math.max(getXSize(), getYSize());
    }

    /**
     * Get the Y increment - actual
     *
     * @return Y increment
     */
    public float getYInc()
    {
        return rotatedBoundingBox.yInc;
    }

    /**
     * Get the project grid displayed y increment (dotted line spacing in project grid)
     *
     * @return project grid displayed y increment
     */
    public float getGridDY()
    {
        return displayBoundingBox.gridDY;
    }

    /**
     * Get absolute Y origin
     *
     * @return absolute Y origin
     */
    public double getYOrigin()
    {
        return rotatedBoundingBox.yOrigin;
    }

    /**
     * Get Y at center of Project
     *
     * @return center of gravity Y
     */
    public float getYCenter()
    {
        return rotatedBoundingBox.getYCenter();
    }

    //    public float getAngle() { return angle; }

    /**
     * Get the Project minimum in Z
     *
     * @return minimum Z
     */
    public float getZorTMin()
    {
        return getZorTMin(isDepth);
    }

    public float getZorTMin(boolean isDepth)
    {
        if(isDepth)
            return rotatedBoundingBox.getZMin();
        else
            return rotatedBoundingBox.tMin;
    }

    /**
     * Get Project maximum in Z
     *
     * @return maximum Z
     */
    public float getZorTMax()
    {
        return getZorTMax(isDepth);
    }

    public float getZorTMax(boolean isDepth)
    {
        if(isDepth)
            return rotatedBoundingBox.getZMax();
        else
            return rotatedBoundingBox.tMax;
    }

    /**
     * Get the Z increment - actual
     *
     * @return Z increment
     */
    public float getZorTInc()
    {
        if(isDepth)
            return rotatedBoundingBox.zInc;
        else
            return rotatedBoundingBox.tInc;
    }

    public float getZorTInc(boolean isDepth)
    {
        if(isDepth)
            return rotatedBoundingBox.zInc;
        else
            return rotatedBoundingBox.tInc;
    }

    public float getGridZT()
    {
        if(isDepth)
            return displayBoundingBox.getGridZ();
        else
            return displayBoundingBox.getGridT();
    }

    public void getGridZT(float gridZT)
    {
        if(isDepth)
            displayBoundingBox.setGridZ(gridZT);
        else
            displayBoundingBox.setGridT(gridZT);
    }

    public float getCultureZ(float x, float y)
    {
        return getGridZT();
    }

    /**
     * Get angle from global +X to project +X axes
     *
     * @return angle
     */
    public float getAngle()
    {
        return rotatedBoundingBox.angle;
    }

    public float getDisplayXMin()
    {
        return displayBoundingBox.xMin;
    }

    public float getDisplayXMax()
    {
        return displayBoundingBox.xMax;
    }

    public float getDisplayYMin()
    {
        return displayBoundingBox.yMin;
    }

    public float getDisplayYMax()
    {
        return displayBoundingBox.yMax;
    }

    /*
        public float getDepthMin()
        {
            if(supportsDepth())
                return depthMin;
            else
                return 0.0f;
        }

        public float getDepthMax()
        {
            if(supportsDepth())
                return depthMax;
            else
                return 0.0f;
        }
    */
    public float getDepthInc()
    {
        return rotatedBoundingBox.zInc;
    }

    public long getProjectTime()
    {
		if(!timeEnabled) return -1;
        return projectTime;
    }

	public long getCurrentTime()
	{
		if(!timeEnabled)
			return System.currentTimeMillis();
		else
			return projectTime;
	}

    public String getProjectTimeAsString()
    {
        Date date = new Date(projectTime);
        SimpleDateFormat format = new SimpleDateFormat(getTimeDateFormatString());
        String time = format.format(date);
        return time;
    }

    public boolean setProjectTime(long time, boolean updateTb)
    {
        setProjectTime(time);
        if(updateTb)
        {
            StsWin3d window = currentModel.win3d;
            StsTimeActionToolbar tb = window.getTimeActionToolbar();
            tb.stopTime();
            if(tb != null)
                tb.updateTime();
        }
        return true;
    }

    public void stopProjectTime()
    {
        StsWin3d window = currentModel.win3d;
        StsTimeActionToolbar timeActionToolbar = window.getTimeActionToolbar();
        if(timeActionToolbar == null) return;
        if(timeActionToolbar.isRunning())
            timeActionToolbar.stopTime();
    }

    public boolean setProjectTime(long time)
    {
        projectTime = time;
		if(!timeEnabled) return false;

		if(debug) StsException.systemDebug(this, "setProjectTime", "time: " + getDateTimeStringFromLong(time));
		boolean viewChanged = false;
		//TODO create an StsClassCollection which has a collection of subClasses of this StsClass; so StsVectorSetClass
		//TODO would have in it StsLineVectorSetClass, StsDepthVectorSetClass, etc
		//TODO we would then iterate thru the StsClassCollection in this case, avoiding non-generic references to subclasses

		StsObject[] lineVectorSetObjects = currentModel.getObjectList("com.Sts.Framework.Utilities.DataVectors.StsLineVectorSet");
		if(lineVectorSetObjects != null)
		{
			for(StsObject vectorSetObject : lineVectorSetObjects)
				viewChanged = viewChanged | ((StsTimeVectorSet)vectorSetObject).checkSetProjectTime(time);
		}
		StsObject[] logVectorSetObjects = currentModel.getObjectList("com.Sts.Framework.Utilities.DataVectors.StsLogVectorSet");
		if(logVectorSetObjects != null)
		{
			for(StsObject vectorSetObject : logVectorSetObjects)
				viewChanged = viewChanged | ((StsTimeVectorSet)vectorSetObject).checkSetProjectTime(time);
		}
		StsObject[] tdVectorSetObjects = currentModel.getObjectList("com.Sts.Framework.Utilities.DataVectors.StsTdVectorSet");
		if(tdVectorSetObjects != null)
		{
			for(StsObject vectorSetObject : tdVectorSetObjects)
				viewChanged = viewChanged | ((StsTimeVectorSet)vectorSetObject).checkSetProjectTime(time);
		}
		if(viewChanged) currentModel.viewObjectRepaint(this, this);
        return true;
    }

   public boolean disableTime()
    {
		if(debug) StsException.systemDebug(this, "disableTime");
		boolean viewChanged = false;
		StsObject[] vectorSetObjects = currentModel.getObjectList("com.Sts.Framework.Utilities.DataVectors.StsVectorSet");
		if(vectorSetObjects != null)
		{
			for(StsObject vectorSetObject : vectorSetObjects)
				viewChanged = viewChanged | ((StsTimeVectorSet)vectorSetObject).disableTime();
		}
		StsObject[] lineVectorSetObjects = currentModel.getObjectList("com.Sts.Framework.Utilities.DataVectors.StsLineVectorSet");
		if(lineVectorSetObjects != null)
		{
			for(StsObject vectorSetObject : lineVectorSetObjects)
				viewChanged = viewChanged | ((StsCoorTimeVectorSet)vectorSetObject).disableTime();
		}
		if(viewChanged) currentModel.viewObjectRepaint(this, this);
        return true;
    }

    public boolean incrementProjectTime(long mseconds)
    {
        return setProjectTime(projectTime + mseconds);
    }

    public void setProjectTimeToCurrentTime(boolean updateToolbar)
    {
        setProjectTime(System.currentTimeMillis(), updateToolbar);
    }

    public void setProjectTimeToCurrentTime()
    {
        projectTime = System.currentTimeMillis();
        currentModel.viewObjectRepaint(this, this);
    }

    public void clearProjectTimeDuration()
    {
        projectTimeDuration = 0;
        currentModel.viewObjectRepaint(this, this);
        return;
    }

    public long getProjectTimeDuration()
    {
        return projectTimeDuration;
    }

	static public SimpleDateFormat getDefaultDateTimeFormat()
	{
		return new SimpleDateFormat(defaultDateFormatString + " " + defaultTimeFormatString);
	}
    public long getLongFromDateTimeString(String timeString)
    {
        try
        {
            return dateTimeFormat.parse(timeString).getTime();
        }
        catch(Exception ex)
        {
            systemError(this, "getLongFromDateTimeString", "StsProject:Failed to parse supplied time string: " + timeString);
			return -1L;
        }
    }

    public long getLongFromDateString(String timeString)
    {
        try
        {
            return dateFormat.parse(timeString).getTime();
        }
        catch(Exception ex)
        {
            systemError(this, "getLongFromDateString", "StsProject:Failed to parse time string: " + timeString);
			return -1L;
        }
    }

    public long getLongFromTimeString(String timeString)
    {
        try
        {
            return timeFormat.parse(timeString).getTime();
        }
        catch(Exception ex)
        {
            systemError(this, "getLongFromTimeString", "StsProject:Failed to parse time string: " + timeString);
			return -1L;
        }
    }

    public boolean setProjectTimeDuration(long startTime, long endTime)
    {
        projectTimeDuration = endTime - startTime;
        StsWin3d window = currentModel.win3d;
        StsTimeActionToolbar tb = (StsTimeActionToolbar) window.getTimeActionToolbar();
        if(tb == null) return true;
        if(tb.isRunning())
            tb.stopTime();
        return true;
    }

    public boolean isTimeRunning()
    {
        StsWin3d window = currentModel.win3d;
        StsTimeActionToolbar tb = (StsTimeActionToolbar) window.getTimeActionToolbar();
        return tb.isRunning();
    }

    public boolean isRealtime()
    {
        StsWin3d window = currentModel.win3d;
        if(window == null)
            return false;
        StsTimeActionToolbar tb = (StsTimeActionToolbar) window.getTimeActionToolbar();
        if(tb != null)
            return tb.isRealtime();
        else
            return false;
    }

    /*
        public float getTimeMin()
        {
            if(supportsTime())
                return timeMin;
            else
                return 0.0f;
        }

        public float getTimeMax()
        {
            if(supportsTime())
                return timeMax;
            else
                return 0.0f;
        }
    */
    public float getTimeInc()
    {
        return rotatedBoundingBox.tInc;
    }

    /**
     * Set the background color of the graphics environment
     *
     * @argument Java Color
     */
    public void setBackgroundColor(StsColor color)
    {
        if(backgroundColor == color) return;
        backgroundColor = new StsColor(color);
        //        currentModel.properties.set(backgroundColorBean.getBeanKey(), backgroundColor.toString());
        currentModel.win3dDisplayAll();
    }

    /**
     * Get the current background color of the graphics environment
     *
     * @return Java Color
     */
    public StsColor getBackgroundColor()
    {
        return backgroundColor;
    }

    /**
     * get current foreground color, which is inverse of background color
     *
     * @return StsColor
     */
    public StsColor getForegroundColor()
    {
        return StsColor.getInverseStsColor(backgroundColor);
    }

    /**
     * Get the current background color of the graphics environment as StsColor
     *
     * @return StsColor
     */
    public StsColor getBackgroundStsColor()
    {
        return backgroundColor;
    }

    /**
     * Set the grid color of the graphics environment
     *
     * @argument Java Color
     */
    public void setGridColor(StsColor color)
    {
        if(gridColor == color) return;
        gridColor = new StsColor(color);
        //		currentModel.properties.set(gridColorBean.getBeanKey(), gridColor.toString());
        currentModel.win3dDisplayAll();
    }

    /**
     * Get the current grid color of the graphics environment
     *
     * @return Java Color
     */
    public StsColor getGridColor()
    {
        return gridColor;
    }

    /**
     * Get the current grid color of the graphics environment as StsColor
     *
     * @return StsColor
     */
    public StsColor getStsGridColor()
    {
        return gridColor;
    }

    /**
     * Set the color of the COG in graphics environment
     *
     * @argument Java Color
     */
    public void setCogColor(StsColor color)
    {
        if(cogColor == color) return;
        cogColor = new StsColor(color);
        //		currentModel.properties.set(cogColorBean.getBeanKey(), cogColor.toString());
        currentModel.win3dDisplayAll();
    }

    /** Set the color of the timing lines in graphics environment */
    public void setTimingColor(StsColor color)
    {
        if(timingColor == color) return;
        timingColor = new StsColor(color);
        currentModel.win3dDisplayAll();
    }

    public StsColor getStsTimingColor()
    {
        return timingColor;
    }

    /**
     * Get the current COG color of the graphics environment
     *
     * @return StsColor
     */
    public StsColor getCogColor()
    {
        return cogColor;
    }

    /**Get the current COG color of the graphics environment as StsColor
     *
     * @return StsColor
     */
    public StsColor getStsCogColor()
    {
        return cogColor;
    }

    /**
     * Get the current timing line color of the graphics environment
     *
     * @return Java Color
     */
    public StsColor getTimingColor()
    {
        return timingColor;
    }

    public float getGridZ()
    {
       if(isDepth)
            return displayBoundingBox.getGridZ();
        else
            return displayBoundingBox.getGridT();
    }

    public void setGridZ(float gridZ)
    {
        displayBoundingBox.setGridZ(gridZ);
    }

    public void setGridT(float gridT)
    {
        displayBoundingBox.setGridT(gridT);
    }

    public String getGridLocationString() { return gridLocationStrings[gridLocation]; }

    public void setGridLocationString(String string)
    {
        if(displayBoundingBox == null) return;
        for (byte n = 0; n < 3; n++)
        {
            if (string == gridLocationStrings[n])
            {
                gridLocation = n;
                break;
            }
        }
        displayBoundingBox.setGridZTLocation(gridLocation, isDepth);
        currentModel.win3dDisplayAll();
    }
    /**
     * Show the intersections between the cursor slices
     *
     * @params true to show
     */
    public void setShowIntersection(boolean showIntersection)
    {
        if(this.showIntersection == showIntersection) return;
        this.showIntersection = showIntersection;
        //		setDisplayField("showIntersection", showIntersection);
        //		dbFieldChanged("showIntersection", showIntersection);
        currentModel.win3dDisplayAll();
    }

    public boolean getShowIntersection()
    {
        return showIntersection;
    }

    /**
     * Set the Project to visible
     *
     * @params true to show
     */
    public void setIsVisible(boolean b)
    {
		if(!setIsVisibleChanged(b)) return;
        currentModel.win3dDisplayAll();
    }


    /** Make the grid visible */
    public void setShowGrid(boolean showGrid)
    {
        if(this.showGrid == showGrid) return;
        this.showGrid = showGrid;
        //		setDisplayField("showGrid", showGrid);
        //		dbFieldChanged("showGrid", showGrid);
        currentModel.win3dDisplayAll();
    }

    /**
     * Determine if the Grid is visible
     *
     * @returns true for visible
     */
    public boolean getShowGrid()
    {
        return showGrid;
    }

    /**
     * Make the grid on 3D views visible
     *
     * @params true to show
     */
    public void setShow3dGrid(boolean show3dGrid)
    {
        if(this.show3dGrid == show3dGrid) return;
        this.show3dGrid = show3dGrid;
        //		setDisplayField("show3dGrid", show3dGrid);
        dbFieldChanged("show3dGrid", show3dGrid);
        currentModel.win3dDisplayAll();
    }

    /**
     * Determine if the grid on 3D views are visible
     *
     * @returns true for visible
     */
    public boolean getShow3dGrid()
    {
        return show3dGrid;
    }

    /**
     * Make the grid on 2D views visible
     *
     * @params true to show
     */
    public void setShow2dGrid(boolean show2dGrid)
    {
        if(this.show2dGrid == show2dGrid) return;
        this.show2dGrid = show2dGrid;
        //		setDisplayField("show2dGrid", show3dGrid);
        dbFieldChanged("show2dGrid", show2dGrid);
        currentModel.win3dDisplayAll();
    }

    /**
     * Determine if the grid on 2D views are visible
     *
     * @returns true for visible
     */
    public boolean getShow2dGrid()
    {
        return show2dGrid;
    }

    /**
     * Make the labels visible
     *
     * @params true to show
     */
    public void setShowLabels(boolean showLabels)
    {
        if(this.showLabels == showLabels) return;
        this.showLabels = showLabels;
        //		setDisplayField("showLabels", showLabels);
        //		dbFieldChanged("showLabels", showLabels);
        currentModel.win3dDisplayAll();
    }

    /**
     * Determine if the labels are visible
     *
     * @returns true for visible
     */
    public boolean getShowLabels()
    {
        return showLabels;
    }

    /**
     * Set the value used to represent a null in map data
     *
     * @params null value
     */
    public void setMapGenericNull(float nullValue)
    {
        mapGenericNull = nullValue;
    }

    /**
     * Get the value used to represent a null in map data
     *
     * @returns map null value
     */
    public float getMapGenericNull()
    {
        return mapGenericNull;
    }

    /**
     * Set the value used to represent a null in log data
     *
     * @params null value
     */
    public void setLogNull(float logNull)
    {
        this.logNull = logNull;
    }

    /**
     * Get the value that represents a null in log data
     *
     * @returns log null value
     */
    public float getLogNull()
    {
        return logNull;
    }

    /** Set the min value used for time and depth increment for the project; i.e., they can't be smaller than this number */
    public void setMinZInc(float min)
    {
        if(minZInc == min) return;
        minZInc = min;
        dbFieldChanged("minZInc", minZInc);
    }

    /** Get the value that represents a null in log data */
    public float getMinZInc()
    {
        return minZInc;
    }

    /**
     * Get the Z domain
     *
     * @returns the Z domain item (defaults to time)
     */
    public byte getZDomain()
    {
        return zDomain;
    }

    public boolean isModelZDomain()
    {
        return modelZDomain == TD_NONE || modelZDomain == zDomain;
    }

    public boolean setToModelZDomain()
    {
        if(isModelZDomain()) return false;
        setZDomainAndRebuild(modelZDomain);
        return true;
    }

    public String getTimeUnitString()
    {
        return StsParameters.getTimeUnitString(timeUnits);
    }

    public void setTimeUnits(byte units)
    {
        timeUnits = units;
        dbFieldChanged("timeUnits", timeUnits);
    }

    public byte getTimeUnits()
    {
        return timeUnits;
    }

    public float getTimeScalar(byte from)
    {
        if(from == StsParameters.TIME_NONE)
            return 1.0f;
        else if(from == StsParameters.DIST_NONE)
            return 1.0f;
        else
            return StsParameters.TIME_SCALES[timeUnits] / StsParameters.TIME_SCALES[from];
    }

    public float getTimeScalar(String fromString)
    {
        byte from = StsParameters.getTimeUnitsFromString(fromString);
        return getTimeScalar(from);
    }

    public float calculateVelScaleMultiplier(String velocityUnits)
    {
        float scaleMultiplier = 1.0f;
        if(velocityUnits == StsParameters.VEL_UNITS_NONE)
        {
            return scaleMultiplier;
        }
        String projectUnits = this.getVelocityUnits();
        if(projectUnits == velocityUnits)
        {
            scaleMultiplier = 1.0f;
        }
        else if(projectUnits == StsParameters.VEL_M_PER_MSEC)
        {
            if(velocityUnits == StsParameters.VEL_M_PER_SEC)
                scaleMultiplier = 0.001f;
            else if(velocityUnits == StsParameters.VEL_FT_PER_MSEC)
                scaleMultiplier = 1.0f / StsParameters.DIST_FEET_SCALE;
            else if(velocityUnits == StsParameters.VEL_FT_PER_SEC)
                scaleMultiplier = 0.001f / StsParameters.DIST_FEET_SCALE;
        }
        else if(projectUnits == StsParameters.VEL_FT_PER_SEC)
        {
            if(velocityUnits == StsParameters.VEL_FT_PER_MSEC)
                scaleMultiplier = 1000;
            else if(velocityUnits == StsParameters.VEL_M_PER_MSEC)
                scaleMultiplier = 1000 * StsParameters.DIST_FEET_SCALE;
            else if(velocityUnits == StsParameters.VEL_M_PER_SEC)
                scaleMultiplier = StsParameters.DIST_FEET_SCALE;
        }
        else if(projectUnits == StsParameters.VEL_FT_PER_MSEC)
        {
            if(velocityUnits == StsParameters.VEL_FT_PER_SEC)
                scaleMultiplier = 0.001f;
            else if(velocityUnits == StsParameters.VEL_M_PER_MSEC)
                scaleMultiplier = StsParameters.DIST_FEET_SCALE;
            else if(velocityUnits == StsParameters.VEL_M_PER_SEC)
                scaleMultiplier = StsParameters.DIST_FEET_SCALE / 1000;
        }
        return scaleMultiplier;
    }

    public String getDepthUnitString()
    {
        return StsParameters.getDistanceUnitString(depthUnits);
    }

    public void setDepthUnits(byte units)
    {
        depthUnits = units;
        dbFieldChanged("depthUnits", depthUnits);
    }

    public byte getDepthUnits()
    {
        return depthUnits;
    }

    public float getDepthScalar(byte from)
    {
        if(from == StsParameters.DIST_NONE)
            return 1.0f;
        else
            return StsParameters.DIST_SCALES[depthUnits] / StsParameters.DIST_SCALES[from];
    }

    public float getDepthScalar(String fromString)
    {
        byte from = StsParameters.getDistanceUnitsFromString(fromString);
        return getDepthScalar(from);
    }

    public String getXyUnitString()
    {
        return StsParameters.getDistanceUnitString(xyUnits);
    }

    public void setXyUnits(byte units)
    {
        xyUnits = units;
        dbFieldChanged("xyUnits", xyUnits);
    }

    public byte getXyUnits()
    {
        return xyUnits;
    }

    public float getXyScalar(byte from)
    {
        if(from == StsParameters.DIST_NONE)
            return 1.0f;
        else
            return StsParameters.DIST_SCALES[xyUnits] / StsParameters.DIST_SCALES[from];
    }

    public float getXyScalar(String fromString)
    {
        byte from = StsParameters.getDistanceUnitsFromString(fromString);
        return getXyScalar(from);
    }

    public String getVerticalUnitsString()
    {
        if(zDomain == TD_DEPTH) return StsParameters.DIST_STRINGS[depthUnits];
        else return StsParameters.TIME_STRINGS[timeUnits];
    }

    public String getVerticalUnitsString(byte zDomain)
    {
        if(zDomain == TD_DEPTH) return StsParameters.DIST_STRINGS[depthUnits];
        else return StsParameters.TIME_STRINGS[timeUnits];
    }

    public void setTimeSeriesBkgdColor(StsColor color)
    {
        if(timeSeriesBkgdColor.equals(color)) return;
        timeSeriesBkgdColor = color;
        currentModel.viewObjectChanged(this, this);
    }

    public StsColor getTimeSeriesBkgdColor()
    {
        return timeSeriesBkgdColor;
    }

    public void setTimeSeriesFrgdColor(StsColor color)
    {
        if(timeSeriesFrgdColor.equals(color)) return;
        timeSeriesFrgdColor = color;
        currentModel.viewObjectChanged(this, this);
    }

    public StsColor getTimeSeriesFrgdColor()
    {
        return timeSeriesFrgdColor;
    }

    public void setTimeSeriesGridColor(StsColor color)
    {
        if(timeSeriesGridColor.equals(color)) return;
        timeSeriesGridColor = color;
        currentModel.viewObjectChanged(this, this);
    }

    public StsColor getTimeSeriesGridColor()
    {
        return timeSeriesGridColor;
    }

    public void setEnableTsGrid(boolean enable)
    {
        if(this.showTimeSeriesGrid == enable) return;
        this.showTimeSeriesGrid = enable;
        currentModel.viewObjectChanged(this, this);
    }

    public boolean getEnableTsGrid()
    {
        return showTimeSeriesGrid;
    }

    public String getVelocityUnits()
    {
        if(depthUnits == StsParameters.DIST_METER)
        {
            if(timeUnits == StsParameters.TIME_MSECOND)
                return StsParameters.VEL_M_PER_MSEC;
            else if(timeUnits == StsParameters.TIME_SECOND)
                return StsParameters.VEL_M_PER_SEC;
            else
                return StsParameters.VEL_UNITS_NONE;
        }
        else if(depthUnits == StsParameters.DIST_FEET)
        {
            if(timeUnits == StsParameters.TIME_MSECOND)
                return StsParameters.VEL_FT_PER_MSEC;
            else if(timeUnits == StsParameters.TIME_SECOND)
                return StsParameters.VEL_FT_PER_SEC;
            else
                return StsParameters.VEL_UNITS_NONE;
        }
        else
            return StsParameters.VEL_UNITS_NONE;
    }

    /** should be called only when we are toggling between time and depth states */
    public void setZDomainString(String zDomainString)
    {
        setZDomainAndRebuild(StsParameters.getZDomainFromString(zDomainString));
        //        resetProjectView();
    }

    public void setZDomain()
    {
        if(zDomainSupported != TD_TIME_DEPTH)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Unable to switch domain without velocity information");
        }
        else
        {
            if(zDomain == TD_DEPTH)
            {
                setZDomainAndRebuild(TD_TIME);
            }
            else
            {
                setZDomainAndRebuild(TD_DEPTH);
            }
        }

    }

    public void initializeZDomainAndRebuild()
    {
        setIsDepth(zDomain == TD_DEPTH);

        //		convertGridZ(oldZDomain);
		// resetBoundingBoxes();
        setBoundingBoxesZRanges();
        // resetCropVolume();
        if(!windowInitialized()) return;
        //This looks redundant (z slider adjusted above and resetCropVolume() called).  Tom 12/12/07
        rangeChanged();
        currentModel.resetAllSliderValues();
        // if(!isModelBuilt()) return;
        //rebuildModel();
    }

	public void reinitializeBoundingBoxes()
	{
		unrotatedBoundingBox.reinitializeBoundingBox();
		rotatedBoundingBox.reinitializeBoundingBox();
		displayBoundingBox.reinitializeBoundingBox();
		cropVolume.reinitializeBoundingBox();
	}
/*
    public boolean isModelBuilt()
    {
        StsBuiltModel builtModel = (StsBuiltModel) currentModel.getCurrentObject(StsBuiltModel.class);
        return builtModel != null;
    }

    private void rebuildModel()
    {
        final StsStatusPanel statusPanel = StsStatusPanel.constructStatusDialog(currentModel.win3d, "Rebuilding model");
        StsToolkit.runRunnable(new Runnable()
		{
			public void run()
			{
				rebuildModel(statusPanel);
				StsStatusPanel.disposeDialog();
			}
		});
    }
*/
    /**
     * Set the string that represents the Z domain, generally time or depth.
     *
     * @params domain the byte representing Z (default is time)
     */
    private void setZDomainAndRebuild(byte domain)
    {
        setIsDepth(domain == TD_DEPTH);
        if(zDomain == domain) return;
        setZDomain(domain);
        //if(!isModelBuilt()) return;
        //rebuildModel();
    }

    public void setZDomain(byte domain)
    {
        setIsDepth(domain == TD_DEPTH);
        if(zDomain == domain) return;
        byte oldZDomain = zDomain;
        //        zDomainBean.setSelectedItem(StsParameters.TD_ALL_STRINGS[domain]);
        zDomain = domain;
        dbFieldChanged("zDomain", zDomain);

        StsWin3dFull[] parentWindows = currentModel.getParentWindows();
        if(parentWindows != null)      //for loading project that exited abnormally, prevents NPE
            for(StsWin3dFull parentWindow : parentWindows)
                parentWindow.setZDomain(domain);

//        convertGridZ(oldZDomain);

        setViewZRanges(oldZDomain);
		// cropVolume is a rotatedGridBoundingSubBox so now supports time and depth and is initialized to the
		// project.rotatedBoundingBox when that is reinitialized
//        resetCropVolume();
        if(!windowInitialized()) return;
        //This looks redundant (z slider adjusted above and resetCropVolume() called).  Tom 12/12/07
        rangeChanged();
        domainViewChanged();
    }
/*
    public boolean rebuildModel(StsStatusPanel statusPanel)
    {
        StsBuiltModel builtModel = (StsBuiltModel) currentModel.getCurrentObject(StsBuiltModel.class);
        builtModel.rebuildModel(currentModel, statusPanel);
        return true;
    }

      public boolean rebuildSectionsOK()
      {
          StsObject[] sectionObjects = currentModel.getObjectList(StsSection.class);
          if(velocityModel != null)
          {
              for(int n = 0; n < sectionObjects.length; n++)
              {
                  StsSection section = (StsSection)sectionObjects[n];
                  section.constructSection();
              }
              return true;
          }
          for(int n = 0; n < sectionObjects.length; n++)
          {
              StsSection section = (StsSection)sectionObjects[n];
              if(!section.isZDomainOriginal(zDomain)) return false;
          }
          return true;
      }
*/
    /**
     * Called when the project has changed between time and depth.
     * The new domain is defined by zDomain.
     */
    public void domainViewChanged()
    {
		if(!rotatedBoundingBox.initializedZ() || !rotatedBoundingBox.initializedT()) return;
        for(int i = 0; i < currentModel.viewPersistManager.getFamilies().length; i++)
        {
            StsWin3dBase[] windows = currentModel.getWindows(i);
            for(int w = 0; w < windows.length; w++)
            {
                StsWin3dBase window = windows[w];
                window.getCursor3d().resetDepthLabels(StsParameters.TD_ALL_STRINGS[zDomain]);
                StsView[] views = window.getDisplayedViews();
                for(int n = 0; n < views.length; n++)
                {
                    StsView view = views[n];
                    if(view instanceof StsView3d)
                    {
                        StsView3d view3d = (StsView3d) view;
                        float[] viewParams = view3d.getCenterAndViewParameters();
                        // these are the old vector
                        float centerZ = viewParams[2];
                        float distance = viewParams[3];
                        float azimuth = viewParams[4];
                        float elevation = viewParams[5];
                        float zscale = viewParams[6];
                        float dz = distance * (float) Math.sin(elevation);
                        // compute the fractional height in the old domain and compute the height in the new domain
                        // then adjust the scaling from the old domain to the new domain
                        if(zDomain == TD_DEPTH) // original was time, new one is depth
                        {
                            float f = (centerZ - rotatedBoundingBox.tMin) / (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
                            viewParams[2] = rotatedBoundingBox.getZMin() + f * (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
                            zscale *= (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin) / (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
                            viewParams[6] = zscale;
                        }
                        else // original was depth, new one is time
                        {
                            float f = (centerZ - rotatedBoundingBox.getZMin()) / (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
                            viewParams[2] = rotatedBoundingBox.tMin + f * (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
                            zscale *= (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin()) / (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
                            viewParams[6] = zscale;
                        }
                        view3d.setCenterAndViewParameters(viewParams);
                        view3d.glPanel3d.viewChanged();
                    }
                }
            }
        }
        //        currentModel.win3d.getCursor3d().rangeChanged();
        currentModel.resetAllSliderValues();
        currentModel.win3dDisplayAll();
    }

    public void setModelZDomainToCurrent()
    {
        setModelZDomain(zDomain);
    }

    public void setModelZDomain(byte zDomain)
    {
        if(modelZDomain == zDomain) return;
        modelZDomain = zDomain;
        dbFieldChanged("modelZDomain", modelZDomain);
    }

    private float convertZ(byte prevZDomain, float z)
    {
        if(prevZDomain == this.TD_TIME)
            return getDepthFromTimeFraction(z);
        else
            return getTimeFromDepthFraction(z);
    }

    public float getTimeFromDepth(float z)
    {
        if(rotatedBoundingBox.tMin == StsParameters.largeFloat) return 0.0f;
        float f = (z - rotatedBoundingBox.getZMin()) / (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
        return rotatedBoundingBox.tMin + f * (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
    }

    public float getDepthFromTime(float t)
    {
        if(rotatedBoundingBox.tMin == StsParameters.largeFloat) return 0.0f;
        float f = (t - rotatedBoundingBox.tMin) / (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
        return rotatedBoundingBox.getZMin() + f * (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
    }

    public float getTimeFromDepthFraction(float z)
    {
        if(rotatedBoundingBox.tMin == StsParameters.largeFloat) return 0.0f;
        float f = (z - rotatedBoundingBox.getZMin()) / (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
        return rotatedBoundingBox.tMin + f * (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
    }

    public float getDepthFromTimeFraction(float t)
    {
        if(rotatedBoundingBox.tMin == StsParameters.largeFloat) return 0.0f;
        float f = (t - rotatedBoundingBox.tMin) / (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
        return rotatedBoundingBox.getZMin() + f * (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
    }

    public boolean getHighResolution()
    {
        return highResolution;
    }

    public void setHighResolution(boolean value)
    {
        highResolution = value;
        //		setDisplayField("highResolution", highResolution);
        //		dbFieldChanged("highResolution", highResolution);
    }

    private float getZScaleMult(byte newDomain)
    {
        if(newDomain == TD_DEPTH)
        {
            return (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin()) / (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin);
        }
        else
        {
            return (rotatedBoundingBox.tMax - rotatedBoundingBox.tMin) / (rotatedBoundingBox.getZMax() - rotatedBoundingBox.getZMin());
        }
    }

    public boolean velocityUnitsChanged()
    {
        return velocityUnitsChanged(false);

    }

    public boolean velocityUnitsChanged(boolean silent)
    {
        if(!StsParameters.velocityUnitsChanged(defaultDistUnits, defaultTimeUnits, depthUnits, timeUnits))
            return false;
        if(!silent)
        {
            new StsMessage(currentModel.win3d, StsMessage.INFO,
                    "Velocity units being converted from " + StsParameters.getVelocityString(defaultDistUnits, defaultTimeUnits) + " to " +
                            StsParameters.getVelocityString(depthUnits, timeUnits));
        }
        return true;
    }

    public float convertVelocity(float velocity, StsModel model)
    {
        return StsParameters.convertVelocity(velocity, defaultDistUnits, defaultTimeUnits, depthUnits, timeUnits);
    }

    public float convertVelocityToProjectUnits(float velocity, String inputVelocityUnits)
    {
        return calculateVelScaleMultiplier(inputVelocityUnits) * velocity;
    }

    public boolean timeUnitsChanged()
    {
        if(!StsParameters.timeUnitsChanged(defaultTimeUnits, timeUnits)) return false;
        new StsMessage(currentModel.win3d, StsMessage.INFO,
                "Time units being converted from " + StsParameters.getTimeString(defaultTimeUnits) + " to " +
                        StsParameters.getTimeString(timeUnits));
        return true;
    }

    public float convertTime(float time, StsModel model)
    {
        return StsParameters.convertTime(time, defaultTimeUnits, timeUnits);
    }

    public boolean depthUnitsChanged()
    {
        if(!StsParameters.distanceUnitsChanged(defaultDistUnits, depthUnits)) return false;
        new StsMessage(currentModel.win3d, StsMessage.INFO,
                "Depth units being converted from " + StsParameters.getDepthString(defaultDistUnits) + " to " +
                        StsParameters.getDepthString(depthUnits));
        return true;
    }

    public float convertDepth(float depth, StsModel model)
    {
        return StsParameters.convertDistance(depth, defaultDistUnits, depthUnits);
    }

    private void resetWindowTimeOrDepth(StsWin3dBase window, byte newZDomain)
    {
    }

    private boolean windowInitialized()
    {
        return currentModel != null && currentModel.win3d != null && currentModel.win3d.getCursor3d() != null;
    }

    public byte getZDomainSupported()
    {
        return zDomainSupported;
    }

    public String[] getZDomainSupportedStrings()
    {
        return StsParameters.getSupportedDomainStrings(zDomainSupported);
    }

    private void setZDomainSupported(byte zDomainSupported)
    {
        if(this.zDomainSupported == zDomainSupported) return;
        this.zDomainSupported = zDomainSupported;
        zDomainSupportedBean.setValueObject(StsParameters.TD_ALL_STRINGS[zDomainSupported]);
        StsWin3dFull[] parentWindows = currentModel.getParentWindows();
        if(parentWindows != null)  //for loading project that exited abnormally, prevents NPE
            for(StsWin3dFull parentWindow : parentWindows)
                parentWindow.setZDomainSupported(zDomainSupported);
        dbFieldChanged("zDomainSupported", this.zDomainSupported);
    }

    /**
     * zDomainSupported indicates which domains can be handled: time, depth, or both.
     * zDomain indicates which domain is the current state: time or depth.
     * newDomain is the domain(s) which are supported by this object being added.
     * If not initialized (zDomain == TD_NONE), then the supportedZDomain is the new zDomain
     * and zDomain is also the new zDomain.
     * If newDomain and zDomain agree, set supportedZDomain to the same.
     * If newDomain is both and zDomain is one or the other,
     * then zDomain remains the same but supportedZDomain is set to zDomain.
     */
    static boolean retval = false;

    public boolean checkSetZDomainRun(byte newZDomainSupported)
    {
        final byte d = newZDomainSupported;
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                adjustZDomainSupported(d);
            }
        };
        StsToolkit.runWaitOnEventThread(runnable);
        return retval;
    }

    /** changes the supported zDomains for the project (TIME, DEPTH, or TIME_DEPTH), and sets the current project zDomain (TIME or DEPTH). */
    public boolean checkSetZDomain(byte newZDomainSupported, byte preferZDomain)
    {
        if(debug)
        {
            System.out.println("Current: zDomain " + getZDomainString(zDomain) + " zDomainSupported " + getZDomainString(zDomainSupported));
            System.out.println("   Input: zDomainSupported " + getZDomainString(newZDomainSupported) + "   preferZDomain " + getZDomainString(preferZDomain));
        }

		adjustZDomainSupported(newZDomainSupported);
        setZDomainFromObjectZDomainPreferred(preferZDomain);

        if(debug) StsException.systemDebug(this, "checkSetZDomain", "New zDomain status: zDomain " + zDomain + " supportedZDomain " + zDomainSupported);

        return true;
    }

	private void adjustZDomainSupported(StsRotatedGridBoundingBox objectRotatedBoundingBox)
	{
		adjustZDomainSupported(objectRotatedBoundingBox.getZDomainSupported());
	}

    /** An object may support either depth (D), time (T), or both (TD).  The project currently may support D, T, TD, or NONE.
     *  This method sets the project zDomainSupported according to this state table (where NOP means no-operation):
     *
     *  OBJECT      PROJECT     NEW PROJECT
     *
     *  T           T           NOP
     *  T           D           TD
     *  T           TD          NOP
     *  T           NONE        T
     *
     *  D           T           TD
     *  D           D           NOP
     *  D           TD          NOP
     *  D           NONE        D
     *
     *  TD          T           TD
     *  TD          D           TD
     *  TD          TD          NOP
     *  TD          NONE        TD
     *
     *
     * @param newZDomainSupported  zDomainSupported by this new object
     */
    private void adjustZDomainSupported(byte newZDomainSupported)
	{
		if(zDomainSupported == TD_NONE) // T-NONE, D-NONE, TD-NONE
			setZDomainSupported(newZDomainSupported);
		else if(zDomainSupported != newZDomainSupported) // T-D, T-TD, D-T, D-TD, TD-T, TD-D
			setZDomainSupported(TD_TIME_DEPTH);
        // else NOP:  T-T, D-D, TD-TD
	}

    /** This method sets the project zDomain (which is either T, D, or NONE) based on the object zDomainPreferred
     *  (which can be either T or D), using the following state table:
     *
     *  OBJECT      PROJECT     NEW PROJECT
     *  zDomainPref zDomain     zDomain
     *
     *  T           T           NOP
     *  T           D           T
     *  T           NONE        T
     *
     *  D           T           D
     *  D           D           NOP
     *  D           NONE        D
     *
     * @param preferZDomain
     */
	private void setZDomainFromObjectZDomainPreferred(byte preferZDomain)
	{
        if(preferZDomain == TD_NONE || preferZDomain == TD_TIME_DEPTH)
        {
            systemError(this, "setZDomainFromObjectZDomainPreferred", "preferZDomain cannot be " + preferZDomain);
            return;
        }
		if(zDomain != preferZDomain)
			setZDomain(preferZDomain);
	}

    public String getZDomainString()
    {
        return StsParameters.TD_ALL_STRINGS[zDomain];
    }

    public String getZDomainString(byte zDomain)
    {
        return StsParameters.TD_ALL_STRINGS[zDomain];
    }

    public boolean isDepth()
    {
        return zDomain == TD_DEPTH;
    }

    public int getPointsZIndex()
    {
        if(zDomain == TD_DEPTH) // points are x,y,d,m and we want z index
        {
            return 2;
        }
        else // points are x,y,d,m,t and we want t index
        {
            return 4;
        }
    }

    public boolean isInProjectBounds(double x, double y)
    {
        float[] xy = getRotatedRelativeXYFromUnrotatedAbsXY(x, y);
        return rotatedBoundingBox.isInsideXY(xy);
        /*
           double[] absMax = getAbsoluteXYCoordinates(getXMax(), getYMax());
           double[] absMin = getAbsoluteXYCoordinates(getXMin(), getYMin());
           if((x > absMax[0]) || (x < absMin[0]))
               return false;

           if((y > absMax[1]) || (y < absMin[1]))
               return false;

           return true;
       */
    }

    /*
          public String getLabelFormatAsString()
          {
              return labelFormat.toPattern();
          }

          public void setLabelFormat(DecimalFormat fmt)
          {
              labelFormat = fmt;
              return;
          }

          public void setLabelFormatAsString(String fmt)
          {
              labelFormat = new DecimalFormat(fmt);
              setDisplayField("labelFormatAsString", fmt);
              currentModel.win3dDisplayAll();
          }
      */
    public StsDisplayBoundingBox getDisplayBoundingBox()
    {
        return displayBoundingBox;
    }

    public StsRotatedGridBoundingBox getRotatedBoundingBox()
    {
        return rotatedBoundingBox;
    }

    public StsRotatedGridBoundingBox getZDomainRotatedBoundingBox(byte zDomain)
    {
        if(zDomain == TD_TIME && this.supportsTime())
            return getTimeRotatedBoundingBox();
        else if(zDomain == TD_DEPTH && supportsDepth())
            return getDepthRotatedBoundingBox();
        else
            return null;
    }

    public StsRotatedGridBoundingBox getTimeRotatedBoundingBox()
    {
        return new StsRotatedGridBoundingBox(rotatedBoundingBox, false);
    }

    public StsRotatedGridBoundingBox getDepthRotatedBoundingBox()
    {
        return new StsRotatedGridBoundingBox(rotatedBoundingBox, false);
    }

    public StsBoundingBox getUnrotatedBoundingBox()
    {
        return unrotatedBoundingBox;
    }

    public StsCropVolume getCropVolume()
    {
        return cropVolume;
    }

    public void resetCropVolume()
    {
        cropVolume.reInitialize(rotatedBoundingBox);
//        currentModel.instanceChange(cropVolume, "cropVolumeChanged");
    }

    /**
     * Is the value in between the supplied minimum and maximum. If not, display user error message.
     *
     * @params value the test value
     * @params minValue the minimum range value
     * @params maxValue the maximum range value
     * @params message the error message if out of range
     * @returns true if in range, false if outside range
     */
    private boolean valueOK(float value, float minValue, float maxValue, String message)
    {
        if(StsMath.betweenInclusive(value, minValue, maxValue))
        {
            return true;
        }
        new StsMessage(null, StsMessage.ERROR, message);
        return false;
    }

    /**
     * Get the location of the data directory as a Universal Resource Locator
     *
     * @returns URL string to data directory
     */
/*
    public String getDataDirURLName()
    {
        if(Main.isWebStart)
        {
            java.net.URL codeBaseURL = JNLPUtilities.getBasicService().getCodeBase();
            if(codeBaseURL == null)
            {
                return new String("");
            }
            else
            {
                return codeBaseURL.toString();
            }
        }
        else
        {
            return new String("file:" + getProjectDirString());
        }
    }
*/
    /** Make the Project root directory and the default data, binary and model folders under it.
     *  @params dir the parent directory for the Project as selected by the user
     */
    public void setRootDirectory(File dir)
    {
        try
        {
            if(dir == null)
            {
                rootDirectory = new File(".");
            }
            else if(!dir.isDirectory())
            {
                rootDirectory = new File(dir.getParent());
            }
            else
            {
                try
                {
                    rootDirectory = new File(dir.getCanonicalPath());
                }
                catch(IOException e)
                {
                    rootDirectory = new File(dir.getAbsolutePath());
                }
            }
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.setRootDirectory() failed.",
                    e, StsException.WARNING);
        }
    }

	private void checkMakeProjectSubDirectories()
	{
		checkMakeSubdir(SOURCE_DATA_FOLDER);
		checkMakeSubdir(BINARY_FOLDER);
		checkMakeSubdir(MODEL_FOLDER);
		checkMakeSubdir(ARCHIVE_FOLDER);
		checkMakeSubdir(MEDIA_FOLDER);
	}

	private boolean checkLoadDataDirectories()
	{
		if(dataDirectorySets != null) return true;
		String dataDirectoriesPathname = rootDirectory.getPath() + File.separator + dataDirectoriesFilename;
		dataDirectorySets = new StsLoaderDataDirectorySets();
		StsFile file = StsFile.constructor(dataDirectoriesPathname);
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				return true;
			}
			catch(Exception e)
			{
				systemError(this, "checkLoadDataDirectories", "Failed to construct file: " + dataDirectoriesPathname);
			}
		}
		if(dataDirectorySets.readFromAsciiFile(file)) return true; // if file exists and we can read it ok, return

		String message = "Couldn't constructFile " + dataDirectoriesPathname;
		StsMessageFiles.errorMessage(this, "checkLoadDataDirectories", message);
		systemError(this, "checkLoadDataDirectories", message);
		return false;
	}

	public String getLoaderDefaultDataDirectory(String classType, String ioType)
	{
		if(ioType.equals(StsLoaderDataDirectorySets.S2S))
			return this.getSourceDataDirString();
		else if(ioType.equals(StsLoaderDataDirectorySets.GC_FILES))
			return this.getStsDataDirString();
		else
			return null;
	}

	public StsLoaderDataDirectorySet getDataDirectories(String type, String[] inputTypes, String[] outputTypes)
	{
		if(!checkLoadDataDirectories()) return null;
		StsLoaderDataDirectorySet set = dataDirectorySets.getSet(type, inputTypes, outputTypes, this);
		return set;
	}

	public String getFirstSourceDataDirectory(String classType, String[] inputTypes, String[] outputTypes, String sourceType)
	{
		if(!checkLoadDataDirectories()) return "failed";
		StsLoaderDataDirectorySet directorySet = getDataDirectories(classType, inputTypes, outputTypes);
		String[] directories = directorySet.getSourceDirectories(sourceType);
		if(directories.length > 0) return directories[0];
		return null;
	}

	public void saveDataDirectories()
	{
		if(!checkLoadDataDirectories()) return;
		String dataDirectoriesPathname = rootDirectory.getPath() + File.separator + dataDirectoriesFilename;
		dataDirectorySets.writeToAsciiFile(dataDirectoriesPathname);
	}

    /** Get the fully qualified path of the project root directory. If not set, it will be defaulted to user.dir
     * @returns the pathname to the project root directory */
    public File getRootDirectory()
    {
        if(rootDirectory != null)
        {
            return rootDirectory;
        }
        else
        {
            return new File(System.getProperty("user.dir") + File.separator + "S2SCache" + File.separator);
        }
    }

	/** Returns the absolute path to objects of a particular class, ./project/classTypeName/.   For example for wells this would be: ../project/StsWells/ */
	public String getClassStsDataDirectoryPathname(String classSubDirectory)
	{
		return getStsDataDirString() + classSubDirectory;
	}

	public String getClassSourceDataDirectoryPathname(String classSubDirectory)
	{
		return getSourceDataDirString() + classSubDirectory + File.separator;
	}
	/** Returns the absolute path to objects of a particular class, ./project/classTypeName/.   This is also the folder where the ascii
	 *  header file(s) are stored.  For example for wells this would be: ../project/StsWells/ */
	public String getAsciiDirectoryPathname(String classSubDirectory, String name)
	{
		return getClassStsDataDirectoryPathname(classSubDirectory) + name + File.separator;
	}

	public String getSourceDirectoryPathname(String classSubDirectory, String name)
	{
		return getClassSourceDataDirectoryPathname(classSubDirectory) + name + File.separator;
	}

	public StsFile getAsciiDirectory(String classSubDirectory, String name)
	{
		String pathname = getAsciiDirectoryPathname(classSubDirectory, name);
		return StsFile.checkGetDirectory(pathname);
	}

	/** Returns the path to a particular object's collection of files.  For the Ajax well, for example, this would be ../project/StsWells/Ajax/ */
	public String getObjectDirectoryPathname(String classSubDirectory, String name)
	{
		return getAsciiDirectoryPathname(classSubDirectory, name);
	}

	public StsFile getObjectDirectory(String classSubDirectory, String name)
	{
		String pathname = getObjectDirectoryPathname(classSubDirectory, name);
		return StsFile.checkGetDirectory(pathname);
	}
	/** Returns the path to a particular object's collection of binary files in the "Binaries" subfolder.
	 *  For the Ajax well, for example, this would be ../project/StsWells/Ajax/Binaries/ */
	public String getBinaryDirectoryPathname(String classSubDirectory, String name)
	{
		return getObjectDirectoryPathname(classSubDirectory, name) + stsBinarySubDirName + File.separator;
	}

	public StsFile getBinaryDirectory(String classSubDirectory, String name)
	{
		String pathname = getBinaryDirectoryPathname(classSubDirectory, name);
		return StsFile.checkGetDirectory(pathname);
	}
	/** Returns the relative path from the project folder to a particular object's collection of binary files in the "Binaries" subfolder.
	 *  For the Ajax well, for example, this would be ../StsWells/Ajax/Binaries/ */
	static public String getRelativeBinaryDirectory(String classSubDirectory, String name)
	{
		return classSubDirectory + name + File.separator + stsBinarySubDirName + File.separator;
	}
	/** Returns the relative path from the project folder to a particular object's collection of ascii files.
	 *  For the Ajax well, for example, this would be ../StsWells/Ajax/Binaries/ */
	static public String getRelativeAsciiDirectory(String classSubDirectory, String name)
	{
		return classSubDirectory + name + File.separator;
	}

	static public String getGroupSubFolderName(String subFolderName)
	{
		return subFolderName + "_dir";
	}

    /**
     * Get the fully qualified path of the project root directory as a String.
     *
     * @returns the pathname to the project root directory
     */
    public String getProjectDirString()
    {
        return rootDirectory.getPath() + File.separator;
    }

    /**
     * Get the fully qualified path of the source data directory as a String.
     * @returns the pathname to the source data directory
     */
    public String getSourceDataDirString()
    {
        return getProjectDirString() + SOURCE_DATA_FOLDER + File.separator;
    }
    /**
     * Get the fully qualified path of the sts data directory as a String.
     * @returns the pathname to the sts data directory
     */
    public String getStsDataDirString()
    {
        return getProjectDirString() + DATA_FOLDER + File.separator;
    }
    /**
     * Get the fully qualified path of the model directory as a String
     *
     * @returns the pathname to the model directory
     */
    public String getModelDbDirString()
    {
        return getProjectDirString() + MODEL_FOLDER + File.separator;
    }

    /**
     * Get the fully qualified path of the archive directory as a String
     * @returns the pathname to the archvie directory
     */
    public String getArchiveDirString()
    {
        return getProjectDirString() + archiveDirectory.getPath() + File.separator;
    }

    /**
     * Get the fully qualified path of the media directory as a String
     *
     * @returns the pathname to the media directory
     */
    public String getMediaDirString()
    {
        return getProjectDirString() + MEDIA_FOLDER + File.separator;
    }

    /**
     * Get the fully qualified path of the 3D Models directory as a String (not used)
     *
     * @returns the pathname to the 3D model directory
     */
    public String getExportModelsDirString()
    {
        return getProjectDirString() + EXPORT_MODELS_FOLDER + File.separator;
    }

    /**
     * Get the fully qualified path of the binary directory
     * @return pathname for the binary directory
     */
    public String getBinaryDirString()
    {
        return getProjectDirString() + BINARY_FOLDER + File.separator;
    }

    /** Set the name of the current Project
     * @param name the new Project name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the Project name
     *
     * @return name of the Project
     */
    public String getName()
    {
        return name;
    }
    /** Starting with current boundingBox, adjust x, y, and z ranges so they are "nice".
     * @parameters model the current StsModel object
     */
    public void adjustDisplayBoundingBox()
    {
		displayBoundingBox.reinitializeBoundingBox();
        displayBoundingBox.addBoundingBox(unrotatedBoundingBox);
		if(rotatedBoundingBox.initializedXY())
			displayBoundingBox.addRotatedBoundingBox(rotatedBoundingBox);
		displayBoundingBox.niceAdjustBoundingBoxXY();
		displayBoundingBox.setGridZT();
    }

	public void adjustRotatedBoundingBox()
	{
		if(!rotatedBoundingBox.initializedXY())
		{
			rotatedBoundingBox.initialize(displayBoundingBox);
			resetCropVolume();
		}
		rotatedBoundingBox.checkSetNiceIncrements(approxNumXYIncs, approxNumZTIncs);
	}

    public void adjustDisplayBoundingBox(StsBoundingBox unrotatedBoundingBox)
    {
        displayBoundingBox.adjustBoundingBox(unrotatedBoundingBox);
    }

    public void adjustDisplayBoundingBox(double xOrigin, double yOrigin, float xMin, float xMax, float yMin, float yMax)
    {
        displayBoundingBox.adjustBoundingBoxXYRange(xOrigin, yOrigin, xMin, xMax, yMin, yMax);
    }

    private void setViewZRanges(byte prevZDomain)
    {
        // if(!resetBoundingBoxesZRanges()) return;

        StsWin3dFull[] parentWindows = currentModel.getParentWindows();
        if(parentWindows == null) return;
        for(int n = 0; n < parentWindows.length; n++)
        {
            if(parentWindows[n] == null) continue;

            // Must re-initialize the axis for Cursor views from time to depth and visa-versa
            StsView[] views = parentWindows[n].getDisplayedViews();
            for(int i = 0; i < views.length; i++)
            {
                if(views[i] instanceof StsViewCursor)
                    ((StsViewCursor) views[i]).initializeAxisRanges();
            }

            // Reconfigure the cursor
            StsCursor3d cursor3d = parentWindows[n].getCursor3d();
            float z = cursor3d.getCurrentDirCoordinate(StsCursor3d.ZDIR);
            z = convertZ(prevZDomain, z);
            parentWindows[n].adjustSlider(StsCursor3d.ZDIR, z);
            cursor3d.zRangeChanged();
            currentModel.viewPersistManager.getFamilies()[n].adjustCursor(StsCursor3d.ZDIR, z);
        }
    }

    private void setBoundingBoxesZRanges()
    {
        // if(!resetBoundingBoxesZRanges()) return;

        StsWin3dFull[] parentWindows = currentModel.getParentWindows();
        if(parentWindows == null) return;
        for(int n = 0; n < parentWindows.length; n++)
        {
            if(parentWindows[n] == null) continue;
            StsCursor3d cursor3d = parentWindows[n].getCursor3d();
            float z = cursor3d.getCurrentDirCoordinate(StsCursor3d.ZDIR);
            //			z = convertZ(prevZDomain, z);
            parentWindows[n].adjustSlider(StsCursor3d.ZDIR, z);
            cursor3d.zRangeChanged();
            currentModel.viewPersistManager.getFamilies()[n].adjustCursor(StsCursor3d.ZDIR, z);
        }
    }

    public void checkCreateZInc()
    {
		if(this.supportsDepth() && rotatedBoundingBox.zInc == 0.0f)
        {
			float[] zScale = niceZTScale(rotatedBoundingBox.getZMin(), rotatedBoundingBox.getZMax());
			float zInc = zScale[2];
			if(rotatedBoundingBox.zInc != zInc)
			{
				rotatedBoundingBox.setZInc(zInc);
				rotatedBoundingBox.dbFieldChanged("zInc", zInc);
			}
    	}
		if(this.supportsTime() && rotatedBoundingBox.tInc == 0.0f)
        {
			float[] tScale = niceZTScale(rotatedBoundingBox.tMin, rotatedBoundingBox.tMax);
			float tInc = tScale[2];
			if(rotatedBoundingBox.tInc != tInc)
			{
				rotatedBoundingBox.setTInc(tInc);
				rotatedBoundingBox.dbFieldChanged("TInc", tInc);
			}
    	}
	}
    /**
     * Build the full URL for the associated filename
     *
     * @params filename relative binary filename
     */
    public URL getBinaryFileURL(String filename)
    {
        String urlName = "null";

        try
        {
            urlName = "file:" + getBinaryDirString() + "/" + filename;
            return new URL(urlName);
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.getBinaryFileURL() failed.\n" +
                    "Couldn't find url: " + urlName, e, StsException.WARNING);
            return null;
        }
    }

    /**
     * Convert the XY coordinates in an StsPoint to absolute coordinates
     *
     * @params point the StsPoint to convert to absolute
     * @returns absolute X and Y coordinate
     */
    public double[] getAbsoluteXYCoordinates(StsPoint point)
    {
        return getAbsoluteXYCoordinates(point.v);
    }

    /**
     * Convert XY coordinates to absolute coordinates
     *
     * @params x the relative X position
     * @params y the relative Y position
     * @returns absolute X and Y coordinate
     */
    public double[] getAbsoluteXYCoordinates(float[] xy)
    {
        return getAbsoluteXYCoordinates(xy[0], xy[1]);
    }

    /**
     * Convert XY coordinates to absolute coordinates
     *
     * @params x the relative X position
     * @params y the relative Y position
     * @returns absolute X and Y coordinate
     */
    public double[] getAbsoluteXYCoordinates(float x, float y)
    {
        return rotatedBoundingBox.getAbsoluteXY(x, y);
    }

    /**
     * Convert absolute XY coordinates to local coordinates
     *
     * @params x the relative X position
     * @params y the relative Y position
     * @returns relative X and Y coordinate
     */
    public float[] getRelativeXY(double x, double y)
    {
        return rotatedBoundingBox.getRelativeXY(x, y);
    }

    /**
     * Given xy in rotated relative coordinate system,
     * return unrotated xy relative to project origin.
     */
    public float[] getUnrotatedRelativeXYFromRotatedXY(float x, float y)
    {
        return rotatedBoundingBox.getUnrotatedRelativeXYFromRotatedXY(x, y);
    }

    public float[] getUnrotatedRelativeXYFromAbsXY(double x, double y)
    {
        return rotatedBoundingBox.getUnrotatedRelativeXYFromAbsXY(x, y);
    }

    public float[] getRotatedRelativeXYFromUnrotatedAbsXY(double x, double y)
    {
        return rotatedBoundingBox.getRotatedRelativeXYFromUnrotatedAbsXY(x, y);
    }

	public double[] getRotatedAbsXYFromUnrotatedAbsXY(double[] origin)
	{
		return rotatedBoundingBox.getRotatedAbsXYFromUnrotatedAbsXY(origin);
	}

    public float[] getRotatedRelativeXYFromUnrotatedRelativeXY(float x, float y)
    {
        return rotatedBoundingBox.getRotatedRelativeXYFromUnrotatedRelativeXY(x, y);
    }
	
	public void rotatePoint(double[] point)
	{
		float[] xy = rotatedBoundingBox.getRotatedRelativeXYFromUnrotatedRelativeXY((float)point[0], (float)point[1]);
		point[0] = xy[0];
		point[1] = xy[1];
	}

    public boolean getDisplayCompass() { return displayCompass; }
    public void setDisplayCompass(boolean display)
    {
		if(displayCompass == display) return;
        displayCompass = display;
        this.dbFieldChanged("displayCompass", displayCompass);
        currentModel.win3dDisplay();
    }

    public boolean isInitialized()
    {
        return unrotatedBoundingBox.originSet;
    }

    public void setBoundingBoxesZTRange(float ztMin, float ztMax, byte zDomain)
    {
        unrotatedBoundingBox.setZTRange(ztMin, ztMax, zDomain);
        rotatedBoundingBox.setZTRange(ztMin, ztMax, zDomain);
    }

    public void adjustBoundingBoxes(boolean saveToDB, boolean adjustProjectZValues)
    {
        systemError(this, "adjustBoundingBoxes", "Don't call. Fix by redoing project.boundingBoxes construction for this class.");
    }
/*
    public void boundingBoxesChanged()
    {
        currentModel.instanceChange(unrotatedBoundingBox, "adjustUnrotatedBoundingBox");
        currentModel.instanceChange(rotatedBoundingBox, "adjustRotatedBoundingBox");
        currentModel.instanceChange(displayBoundingBox, "adjustDisplayBoundingBox");
    }
*/

    /**
     * Once a number of objects have been added to the model by a wizard for example,
     * we adjust the boundingBoxes and the display position and view.
     * This is more efficient than doing adjustments after every object is added as this is
     * more work in the database.
     */

    public void runCompleteLoading()
    {
        StsToolkit.runWaitOnEventThread(new Runnable()
        {
            public void run()
            {
                completeLoading();
            }
        });
    }

    public void completeLoading()
    {
		initializeViews();
        objectPanelChanged();
    }

	public void initializeViews()
	{
		setDefaultViews();
		initialize3dCursors();
	}

	private void initialize3dCursors()
	{
        StsWin3dFull[] parentWindows = currentModel.getParentWindows();
        for(StsWin3dFull parentWindow : parentWindows)
        {
            parentWindow.cursor3dPanel.gridCheckBoxSetVisibleAndSelected();
            parentWindow.cursor3d.initialize();
            parentWindow.cursor3d.resetInitialCursorPositions();
        }
	}

	private void setDefaultViews()
	{
		Iterator<StsView> familyWindowViewIterator = currentModel.getFamilyWindowViewIterator();
        while (familyWindowViewIterator.hasNext())
        {
            StsView view = familyWindowViewIterator.next();
            view.setDefaultView();
        }
	}

	/** If this unrotated box does not touch the boundary of the project unrotated box,
	 *  we don't need to adjust the project boxes.
	 *  If it does, determine if the union of the remaining unrotated boxes has an origin
	 *  "significantly" different than the original origin requiring the redefinition of the origin
	 *  and the recompute of local coordinates for the remaining unrotated bounding boxes
	 *  and rotated boxes.
 	 * @param removedBox
	 */
    public boolean removedUnrotatedBox(StsBoundingBox removedBox)
    {
		if(!removedBox.isBorderBox(unrotatedBoundingBox)) return false;
		return removeBox(removedBox);
	}

   	public boolean removedRotatedBox(StsRotatedBoundingBox removedBox)
    {
		if(!removedBox.isBorderBox(rotatedBoundingBox)) return false;
		return removeBox(removedBox);
	}

	/** remove this boundingBox object and reinitialize project boundingBoxes using the remaining boundingBoxes.
	 * @param removedBox  boundingBox object removed
	 */
	private boolean removeBox(StsBoundingBox removedBox)
	{
		zDomainSupported = TD_NONE;
		rotatedBoundingBox.reinitializeBoundingBox();
        TreeSet<StsRotatedClass> rotatedClasses = currentModel.rotatedClasses;
		StsObjectIterator iterator = new StsObjectIterator(rotatedClasses);
        boolean hasRotatedObjects = false;
        while(iterator.hasNext())
        {
            StsRotatedGridBoundingBox box = (StsRotatedGridBoundingBox)iterator.next();
			rotatedBoundingBox.addRotatedGridBoundingBox(box);
            adjustZDomainSupported(box.getZDomainSupported());
			hasRotatedObjects = true;
        }
        unrotatedBoundingBox.reinitializeBoundingBox();
		if(hasRotatedObjects) unrotatedBoundingBox.addRotatedBoundingBox(rotatedBoundingBox);
        TreeSet<StsUnrotatedClass> unrotatedClasses = currentModel.unrotatedClasses;
		iterator = new StsObjectIterator(unrotatedClasses);
        boolean hasUnrotatedObjects = false;
        while(iterator.hasNext())
        {
            StsBoundingBox box = (StsBoundingBox)iterator.next();
			unrotatedBoundingBox.addBoundingBox(box);
            adjustZDomainSupported(box.getZDomainSupported());
			hasUnrotatedObjects = true;
        }
		if(hasRotatedObjects && hasUnrotatedObjects)
		{
			rotatedBoundingBox.adjustZTRange(unrotatedBoundingBox);
			rotatedBoundingBox.niceAdjustZTScale(approxNumZTIncs);
		}
		// rotatedBoundingBox.addUnrotatedBoundingBox(unrotatedBoundingBox);

		if(!hasRotatedObjects && !hasUnrotatedObjects)
			setToDefaultBoundingBoxes();
		else
		{
			boolean resetOrigin = unrotatedBoundingBox.isBoxOriginOutsideBoxXYLimit(removedBox);
			if(resetOrigin)
			{
				ArrayList arrayList = new ArrayList();
				arrayList.addAll(rotatedClasses);
				arrayList.addAll(unrotatedClasses);
				iterator = new StsObjectIterator(arrayList);
				StsBoundingBox boundingBox = (StsBoundingBox)iterator.getFirst();
				double[] newOrigin = boundingBox.getAdjustedOrigin();
				while(iterator.hasNext())
        		{
            		StsBoundingBox box = (StsBoundingBox)iterator.next();
					box.resetOrigin(newOrigin);
				}
			}
		}
		adjustDisplayBoundingBox();
		rangeChanged();
		adjustRotatedBoundingBox();
		// boundingBoxesChanged();
		runCompleteLoading();
		return true;
	}

	public boolean initializeBoundingBoxes()
	{
		constructBoundingBoxes();
		zDomainSupported = TD_NONE;
		rotatedBoundingBox.reinitializeBoundingBox();
        TreeSet<StsRotatedClass> rotatedClasses = currentModel.rotatedClasses;
		StsObjectIterator iterator = new StsObjectIterator(rotatedClasses);
        boolean hasRotatedObjects = false;
        while(iterator.hasNext())
        {
            StsRotatedGridBoundingBox box = (StsRotatedGridBoundingBox)iterator.next();
			rotatedBoundingBox.addRotatedGridBoundingBox(box);
            adjustZDomainSupported(box.getZDomainSupported());
			hasRotatedObjects = true;
        }
        unrotatedBoundingBox.reinitializeBoundingBox();
		if(hasRotatedObjects) unrotatedBoundingBox.addRotatedBoundingBox(rotatedBoundingBox);
        TreeSet<StsUnrotatedClass> unrotatedClasses = currentModel.unrotatedClasses;
		iterator = new StsObjectIterator(unrotatedClasses);
        boolean hasUnrotatedObjects = false;
        while(iterator.hasNext())
        {
            StsBoundingBox box = (StsBoundingBox)iterator.next();
			unrotatedBoundingBox.addBoundingBox(box);
			//checkSetZDomain(box.getZDomainSupported());
			hasUnrotatedObjects = true;
        }
		if(hasRotatedObjects && hasUnrotatedObjects)
		{
			rotatedBoundingBox.adjustZTRange(unrotatedBoundingBox);
			rotatedBoundingBox.niceAdjustZTScale(approxNumZTIncs);
		}
        adjustZDomainSupported(((StsBoundingBox) iterator.getLast()).getZDomainSupported());
		// rotatedBoundingBox.addUnrotatedBoundingBox(unrotatedBoundingBox);

		if(!hasRotatedObjects && !hasUnrotatedObjects)
			constructDefaultBoundingBoxes();
		else
			this.setBoxesInitialized(true);

		adjustDisplayBoundingBox();
		rangeChanged();
		resetCropVolume();
		adjustRotatedBoundingBox();
		// boundingBoxesChanged();
		// runCompleteLoading();
		return true;
	}
	/** Returns true if removedBox origin is well outside remaining objects boundingBox.
	 *  Also checks if there are remaining rotatedBoundingBoxes; if not angle is reset to zero and angleSet is set to false.
	 *  @return true if origin needs to be moved.
	 */

	private boolean checkOriginRangeAndAngle(StsBoundingBox removedBox)
	{
		StsBoundingBox localOriginBoundingBox = new StsBoundingBox();
        TreeSet<StsUnrotatedClass> unrotatedClasses = currentModel.unrotatedClasses;
        int nUnrotatedClasses = unrotatedClasses.size();
        for(StsUnrotatedClass unrotatedClass : unrotatedClasses)
        {
            StsObject[] objects = unrotatedClass.getElements();
            for(StsObject object : objects)
				localOriginBoundingBox.addBoundingBox((StsBoundingBox) object);
        }
        TreeSet<StsRotatedClass> rotatedClasses = currentModel.rotatedClasses;
        int nRotatedClasses = rotatedClasses.size();
		boolean hasRotatedObjects = false;
        for(StsRotatedClass rotatedClass : rotatedClasses)
        {
            StsObject[] objects = rotatedClass.getElements();
            for(StsObject object : objects)
			{
                localOriginBoundingBox.addBoundingBox((StsBoundingBox) object);
				hasRotatedObjects = true;
			}
        }
		if(!hasRotatedObjects) rotatedBoundingBox.initializeAngle();
		return localOriginBoundingBox.isXYOutsideBoxLimit(removedBox.getLocalOrigin());
    }

    public boolean anyDependencies()
    {
        return true;
    }

    public void actionPerformed(ActionEvent e)
    {
        System.out.println("Project file initialized.");
    }

    /**
     * Make a directory if needed
     *
     * @params subdir directory to create if needed
     */
    public boolean checkMakeSubdir(String subdir)
    {
        if(subdir == null)
        {
            return false;
        }
        if(subdir.equals("."))
        {
            return true;
        }
        File dir = new File(rootDirectory, subdir);
        if(dir.exists())
        {
            if(!dir.isDirectory())
            {
                return false;
            }
            return true;
        }
        return dir.mkdir();
    }

    /** Display method for Project */
    public void display(StsGLPanel3d glPanel3d)
    {
        displayBoundingBox.display(glPanel3d, currentModel, rotatedBoundingBox.angle);
    }

    /**
     * Get the maximum Project dimensions
     *
     * @returns maximum projection distance in X, Y or Z
     */
    public float getMaxProjectDimension()
    {
        return rotatedBoundingBox.getDimensions();
    }

    /**
     * Vertical index is in increments of dZ with 0 at Z=0.0
     *
     * @param z value for which we want index just above
     * @return index above z
     */
    public int getIndexAbove(float z)
    {
        float sliceF = rotatedBoundingBox.getSliceCoor(z);
        return StsMath.below(sliceF);
    }

    /**
     * Vertical index is in increments of dZ with 0 at Z=0.0
     *
     * @param z value for which we want index just below
     * @return iBelow index below z
     */
    public int getIndexBelow(float z)
    {
        float sliceF = rotatedBoundingBox.getSliceCoor(z);
        return StsMath.above(sliceF);
    }


    public int getIndexAbove(float z, boolean isDepth)
    {
        float sliceF;
        if(isDepth)
            sliceF = (z - rotatedBoundingBox.getZMin()) / rotatedBoundingBox.zInc;
        else
            sliceF = (z - rotatedBoundingBox.tMin) / rotatedBoundingBox.tInc;
        return StsMath.floor(sliceF);
    }


    public int getIndexBelow(float z, boolean isDepth)
    {
        float sliceF;
        if(isDepth)
            sliceF = (z - rotatedBoundingBox.getZMin()) / rotatedBoundingBox.zInc;
        else
            sliceF = (z - rotatedBoundingBox.tMin) / rotatedBoundingBox.tInc;
        return StsMath.ceiling(sliceF);
    }


    /**
     * Get Z value at slice index
     *
     * @params index the slice index
     */
    public float getZAtIndex(int index)
    {
        return rotatedBoundingBox.getZCoor(index);
    }

    public float getZAtIndex(int index, boolean isDepth)
    {
        if(isDepth)
        {
            float z = (rotatedBoundingBox.getZMin() + index * rotatedBoundingBox.zInc);
            return StsMath.minMax(z, rotatedBoundingBox.getZMin(), rotatedBoundingBox.getZMax());
        }
        else
        {
            float z = (rotatedBoundingBox.tMin + index * rotatedBoundingBox.tInc);
            return StsMath.minMax(z, rotatedBoundingBox.tMin, rotatedBoundingBox.tMax);
        }
    }

    public StsGridDefinition getGridDefinition()
    {
        return gridDefinition;
    }

    public void setGridDefinition(StsGridDefinition gridDefinition)
    {
        this.gridDefinition = gridDefinition;
        dbFieldChanged("gridDefinition", gridDefinition);
    }

    // TODO: Ask user to give us the xInc, yInc, etc Rather than setting it here unconditionally
    // TODO: as we may subsequently load an object which has row col numbering
    public void checkCursor3d()
    {
        if(rotatedBoundingBox.rowNumMin == StsParameters.nullValue)
        {
            currentModel.getCursor3d().setIsGridCoordinates(false);
            float[] range = niceZTScale(rotatedBoundingBox.xMin, rotatedBoundingBox.xMax);
            rotatedBoundingBox.xInc = range[2];
            range = niceZTScale(rotatedBoundingBox.yMin, rotatedBoundingBox.yMax);
            rotatedBoundingBox.yInc = range[2];
        }
    }
/*
    public StsSeismicVolume constructVelocityVolume(StsSeismicVolume inputVelocityVolume,
                                                    float topTimeDatum, float topDepthDatum, float minVelocity, float maxVelocity, double scaleMultiplier, boolean useSonic, boolean useVelf, String[] velfList, StsProgressPanel panel)
    {
        panel.appendLine("Velocity volume construction is unimplemented.");
        return null;
    }

    public StsSeismicVelocityModel constructVelocityModelSV(float topTimeDatum, float topDepthDatum, float minVelocity, float maxVelocity, double scaleMultiplier, double newTimeInc, StsProgressPanel panel)
    {
        if(velocityModel != null) velocityModel.delete();
        try
        {
            velocityModel = new StsSeismicVelocityModel(topTimeDatum, topDepthDatum, minVelocity, maxVelocity,
                    scaleMultiplier, newTimeInc, panel);
            if(velocityModel != null)
            {
                panel.appendLine("Velocity model construction is complete.");
                fieldChanged("velocityModel", velocityModel);
            }
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.constructVelocityModel() failed.", e, StsException.WARNING);
            return null;
        }

        return velocityModel;

    }

    public StsSeismicVelocityModel constructVelocityModel(StsModelSurface[] surfacesWithMarkers, StsSeismicVolume inputVelocityVolume, float tMin,
                                                          float zMin, float minVel, float maxVel, double scaleMultiplier, float newTimeInc, float[] intervalVelocities, boolean useWellControl,

                                                          float markerFactor, int gridType, StsProgressPanel panel)
    {
        try
        {
            if(velocityModel != null) velocityModel.delete();
            velocityModel = new StsSeismicVelocityModel(surfacesWithMarkers, inputVelocityVolume, tMin, zMin, minVel, maxVel,
                    scaleMultiplier, newTimeInc, intervalVelocities, useWellControl, markerFactor, gridType, panel);

            panel.appendLine("Velocity model construction is complete.");
            fieldChanged("velocityModel", velocityModel);
            //panel.appendLine("Adjusting project Z ranges");
            //adjustZRange(velocityModel);
            return velocityModel;
        }
        catch(Exception e)
        {
            StsException.outputException("StsProject.constructVelocityModel() failed.", e, StsException.WARNING);
            return null;
        }
    }
*/
    public StsSeismicVelocityModel getSeismicVelocityModel()
    {
        return velocityModel;
    }

    public Object[] getChildren()
    {
        return new Object[0];
    }

    /**
     * Get the object panel. Object panel is where persistent data object properties
     * are exposed to the user
     *
     * @return object panel
     */
    public StsObjectPanel getObjectPanel()
    {
        if(objectPanel == null)
            objectPanel = StsObjectPanel.constructor(this, true);
        //        else
        //			objectPanel.setViewObject(this);
        return objectPanel;
    }

    public void deleteObjectPanel()
    {
        objectPanel = null;
    }

    /**
     * Is the Object Panel tree selected.
     *
     * @return true if selected
     */
    public void treeObjectSelected()
    {
    }

    /** Getters and Setters for all the default settings and user preferences. */
    /*
       public boolean getDefaultIsVisible()  { return defaultIsVisible; }
       public void setDefaultIsVisible(boolean value) { defaultIsVisible = value; fieldChanged("defaultIsVisible", defaultIsVisible); }
       public boolean getDefaultShowGrid()  { return defaultShowGrid; }
       public void setDefaultShowGrid(boolean value) { defaultShowGrid = value;  fieldChanged("defaultShowGrid", defaultShowGrid); }
       public boolean getDefaultShowLabels()  { return defaultShowLabels; }
       public void setDefaultShowLabels(boolean value) { defaultShowLabels = value;  fieldChanged("defaultShowLabels", defaultShowLabels); }
       public Color getDefaultBackgroundColor()  { return defaultBackgroundColor.getColor(); }
       public void setDefaultBackgroundColor(Color color) { defaultBackgroundColor.setBeachballColors(color);  fieldChanged("defaultBackgroundColor", defaultBackgroundColor); }
       public Color getDefaultGridColor()  { return defaultGridColor.getColor(); }
       public void setDefaultGridColor(Color color) { defaultGridColor.setBeachballColors(color);  fieldChanged("defaultGridColor", defaultGridColor); }
       public Color getDefaultTimingColor()  { return defaultTimingColor.getColor(); }
       public void setDefaultTimingColor(Color color) { defaultTimingColor.setBeachballColors(color);  fieldChanged("defaultTimingColor", defaultTimingColor); }
       public String getDefaultLabelFormatAsString() { return defaultLabelFormat; }
       public void setDefaultLabelFormatAsString(String value) { defaultLabelFormat = value;  fieldChanged("defaultLabelFormat", defaultLabelFormat); }
       */

    //JKF	public int index() { return -1; }
    //JKF	public void setIndex(int index) { }
    /*
      public StsDataCubeMemory getDataCubeMemory()
      {
          if(dataCubeMemory == null) dataCubeMemory = new StsDataCubeMemory();
          return dataCubeMemory;
      }
  */
    public StsBlocksMemoryManager getBlocksMemoryManager()
    {
        if(blocksMemoryManager == null)
        {
            blocksMemoryManager = new StsBlocksMemoryManager(1000); // jbw
            StsFileBlocks.blocksMemoryManager = blocksMemoryManager;
        }
        return blocksMemoryManager;
    }


    public void close()
    {
        if(blocksMemoryManager != null) blocksMemoryManager.clearAllBlocks();
        if(directoryAliasesFile != null) directoryAliasesFile.writeFile();
    }

    public String getDirectoryAlias(String directory)
    {
        if(directoryAliasesFile == null)
        {
            String fileDirectory = this.getProjectDirString() + File.separator;
            String filename = "s2s.user.directoryAliases";
            directoryAliasesFile = new StsAsciiTokensFile(fileDirectory, filename, 2);
        }
        String[] matchToken = directoryAliasesFile.getMatchToToken(directory);
        if(matchToken == null) return null;
        return matchToken[1];
    }

    public void addDirectoryAlias(String directory, String alias)
    {
        if(directoryAliasesFile == null)
        {
            String fileDirectory = this.getProjectDirString() + File.separator;
            String filename = "s2s.user.directoryAliases";
            directoryAliasesFile = new StsAsciiTokensFile(fileDirectory, filename, 2);
        }
        directoryAliasesFile.addLineTokens(new String[]{directory, alias});
    }

    public String getDirectoryPath(String directoryKeyName)
    {
        if(directoryTableFile == null)
        {
            String fileDirectory = this.getProjectDirString();
            String filename = "s2s.user.directoryTable";
            directoryTableFile = new StsAsciiTokensFile(fileDirectory, filename, 2);
        }
        String[] matchToken = directoryTableFile.getMatchToToken(directoryKeyName);
        if(matchToken == null) return null;
        return matchToken[1];
    }

    public void setDirectoryPath(String keyName, String directoryPath)
    {
        if(keyName == null || directoryPath == null) return;
        if(directoryTableFile == null)
        {
            String fileDirectory = this.getProjectDirString();
            String filename = "s2s.user.directoryTable";
            directoryTableFile = new StsAsciiTokensFile(fileDirectory, filename, 2);
        }
        directoryTableFile.setToken(keyName, directoryPath);
        directoryTableFile.writeFile();
    }

    static public void main(String[] args)
    {
        boolean ok;
        StsProject project = new StsProject();

        System.out.println("input seismic");
        project.zDomainSupported = StsProject.TD_NONE;
        project.setZDomainAndRebuild(StsProject.TD_NONE);
        ok = project.checkSetZDomain(StsProject.TD_TIME, StsProject.TD_TIME);

        System.out.println("input wells no td curves");
        project.zDomainSupported = StsProject.TD_NONE;
        project.setZDomainAndRebuild(StsProject.TD_NONE);
        ok = project.checkSetZDomain(StsProject.TD_DEPTH, StsProject.TD_DEPTH);

        System.out.println("input wells no td curves, no preferred domain");
        project.zDomainSupported = StsProject.TD_NONE;
        project.setZDomainAndRebuild(StsProject.TD_NONE);
        ok = project.checkSetZDomain(StsProject.TD_DEPTH, StsProject.TD_NONE);

        System.out.println("input wells with td curves");
        project.zDomainSupported = StsProject.TD_NONE;
        project.setZDomainAndRebuild(StsProject.TD_NONE);
        ok = project.checkSetZDomain(StsProject.TD_TIME_DEPTH, StsProject.TD_DEPTH);

        System.out.println("we have wells with no tds; input seismic - should fail");
        project.zDomainSupported = StsProject.TD_DEPTH;
        project.setZDomainAndRebuild(StsProject.TD_DEPTH);
        ok = project.checkSetZDomainRun(StsProject.TD_TIME);

        System.out.println("we have seismic (no td model); input wells without tds - should fail");
        project.zDomainSupported = StsProject.TD_TIME;
        project.setZDomainAndRebuild(StsProject.TD_TIME);
        ok = project.checkSetZDomainRun(StsProject.TD_DEPTH);

        System.out.println("we have seismic (no td model); input wells with tds - ok; leave in time");
        project.zDomainSupported = StsProject.TD_TIME;
        project.setZDomainAndRebuild(StsProject.TD_TIME);
        ok = project.checkSetZDomainRun(StsProject.TD_TIME_DEPTH);

        System.out.println(
                "we have seismic with td model; input wells with tds - ok; leave in time, supports time/depth");
        project.zDomainSupported = StsProject.TD_TIME_DEPTH;
        project.setZDomainAndRebuild(StsProject.TD_TIME);
        ok = project.checkSetZDomainRun(StsProject.TD_TIME_DEPTH);
    }

    public boolean getIsPerspective()
    {
        return isPerspective;
    }

    public void setIsPerspective(boolean perspective)
    {
        if(isPerspective == perspective) return;
        isPerspective = perspective;
        isPerspectiveBean.setValue(perspective);
        if(currentModel == null) return;
        if(currentModel.win3d == null) return;
        currentModel.win3d.toggleProjection(isPerspective);

    }

    public boolean getDisplayContours()
    {
        return displayContours;
    }

    public void setDisplayContours(boolean displayContours)
    {
        this.displayContours = displayContours;
    }

    public float getContourInterval()
    {
        return contourInterval;
    }

    public void setContourInterval(float contourInterval)
    {
        this.contourInterval = contourInterval;
    }

    public byte getGridLocation()
    {
        return gridLocation;
    }

	/** Indicates that project bounding boxes have been initialized to actual coordinates and not the default bounding boxes */
	public boolean isBoxesInitialized()
	{
		return boxesInitialized;
	}

	public void setBoxesInitialized(boolean boxesInitialized)
	{
		// if(this.boxesInitialized == boxesInitialized) return;
		this.boxesInitialized = boxesInitialized;
		//dbFieldChanged("boxesInitialized", boxesInitialized);
	}

	public StsSeismicVelocityModel getVelocityModel()
	{
		return velocityModel;
	}

	public void setVelocityModel(StsSeismicVelocityModel velocityModel)
	{
		if(this.velocityModel == velocityModel) return;
		this.velocityModel = velocityModel;
		fieldChanged("velocityModel", velocityModel);
	}

	public boolean loadSourceFile(Path path)
	{
		return StsLoader.loadSourceFile(currentModel, path, true);
	}

	public boolean changedSourceFile(Path path)
	{
		return StsLoader.changedSourceFile(currentModel, path);
	}

    public void watchProjectFiles(boolean selected)
    {
		try
		{
			if(selected)
				startSourceDirectoriesWatch();
			else
				stopSourceDirectoriesWatch();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "watchProjectFiles", e);
		}
	}

	public StsSourceDirectoriesWatch startSourceDirectoriesWatch()
	{
		try
		{
			ArrayList<Path> paths = getSourceDirectoryPaths();
			if(paths.size() == 0) return null;

			sourceDirectoriesWatch = new StsSourceDirectoriesWatch(paths, true, this);
			StsToolkit.runRunnable(new Runnable()
			{
				public void run() { sourceDirectoriesWatch.processEvents(); }
			});
			return sourceDirectoriesWatch;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "watchProjectFiles", e);
			return null;
		}
	}

    public StsSourceDirectoriesWatch watchProjectFiles()
    {
		String sourceDir = this.getSourceDataDirString();
		return startSourceDirectoryWatch(sourceDir);
	}

	public StsSourceDirectoriesWatch startSourceDirectoryWatch(String pathname)
	{
		try
		{
			Path path = Paths.get(pathname);
			if(path == null) return null;
			sourceDirectoriesWatch = new StsSourceDirectoriesWatch(path, true, this);
			StsToolkit.runNewRunnable(new Runnable()
			{
				public void run() { sourceDirectoriesWatch.processEvents(); }
			});
			return sourceDirectoriesWatch;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "watchProjectFiles", e);
			return null;
		}
	}

	public StsSourceDirectoriesWatch startSourceDirectoriesWatch(Path sourcePath)
	{
		try
		{
			ArrayList<Path> paths = new ArrayList<Path>();
			paths.add(sourcePath);
			sourceDirectoriesWatch = new StsSourceDirectoriesWatch(paths, true, this);
			StsToolkit.runNewRunnable(new Runnable()
			{
				public void run() { sourceDirectoriesWatch.processEvents(); }
			});
			return sourceDirectoriesWatch;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "watchProjectFiles", e);
			return null;
		}
	}

	public void stopSourceDirectoriesWatch()
	{
		if(sourceDirectoriesWatch == null) return;
		sourceDirectoriesWatch.cancel();
		sourceDirectoriesWatch = null;
	}

	public ArrayList<Path> getSourceDirectoryPaths()
	{
		if(!checkLoadDataDirectories()) return null;
		ArrayList<Path> sourceDirectoryPaths =  dataDirectorySets.getSourceDirectoryPaths();
		if(sourceDirectoryPaths.size() == 0)
			sourceDirectoryPaths.add(Paths.get(this.getSourceDataDirString()));
		return sourceDirectoryPaths;
	}

	public StsFile getCreateSourceFile(Path path)
	{
		return sourceFileSystem.getCreateFile(path);
	}

	public boolean originAndAngleSame(StsBoundingBox  boundingBox)
	{
		if(rotatedBoundingBox == null) return false;
        float angle = rotatedBoundingBox.getAngle();
		double[] unrotatedLineOrigin = boundingBox.getOrigin();
		boolean originSame = rotatedBoundingBox.originSame(unrotatedLineOrigin);
		return originSame && angle == 0.0f;
	}

	/** Clock-time display controls are enabled */
	final public boolean getTimeEnabled()
	{
		return timeEnabled;
	}

	public void setTimeEnabled(boolean timeEnabled)
	{
		this.timeEnabled = timeEnabled;
		if(timeEnabled)
			this.setProjectTime(projectTime);
		else
			this.disableTime();
	}

	public StsFileSystem getSourceFileSystem()
	{
		return sourceFileSystem;
	}

	public void setSourceFileSystem(StsFileSystem sourceFileSystem)
	{
		this.sourceFileSystem = sourceFileSystem;
	}

	public StsFileSystem getStsFileSystem()
	{
		return stsFileSystem;
	}

	public void setStsFileSystem(StsFileSystem stsFileSystem)
	{
		this.stsFileSystem = stsFileSystem;
	}
}