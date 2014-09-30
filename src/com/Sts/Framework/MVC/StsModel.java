//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.MVC;

import com.Sts.Framework.Actions.*;
import com.Sts.Framework.DB.DBCommand.*;
import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.DataFilesPanel.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.UI.Toolbars.*;
import com.Sts.Framework.Utilities.Shaders.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.Framework.Workflow.*;
import com.Sts.PlugIns.Wells.DBTypes.*;
import com.Sts.PlugIns.Wells.Views.*;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

/**
 *
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002-2003</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 *
 */

/**
 * This is the central class for the framework and any applications.
 * Instances of primary database objects are stored in lists maintained
 * by an array of StsClass instances: each StsClass instance contains a
 * list of instances of a particular primary DB object class.  These DB
 * object classes must be subclassed from StsObject.  A class diagram for
 * this model-view-controller (MVC) architecture is shown in
 * <a href="S2S-MVC.htm">MVC Architecture</a>.
 */
//TODO StsModel extends StsSerialize only to access isDepth().  It should extend StsSerialize so
//TODO StsViewPersistManager can persist StsModel rather than select views (simpler cleaner)
public class StsModel extends StsSerialize
{
	/** name of model */
	private String name = null;
	/** stores file containing launch information */
	public String launchFileName;
	/** sequential numbering of transactions for id purposes */
	private int nTransaction = 0;
	/** convenience copy: defines bounding boxes for project and miscellaneous project parameters */
	transient private StsProject project = null;
	/** actionClass-actionStatus pairs */
	transient public StsProperties actionStatusProperties = new StsProperties(20);
	/** views persistence manager */
	transient public StsViewPersistManager viewPersistManager = new StsViewPersistManager(this);
	/** properties persistence manager */
	transient public StsPropertiesPersistManager propertiesPersistManager = new StsPropertiesPersistManager(this);
	/** miscellaneous properties saved by properties persist manager */
	transient public StsProperties properties = null;
	/** objects which represent primary classes in model */
	transient public ArrayList<StsClass> classList = new ArrayList<>();
	/** actionManager: main window actionManager constructed here and passed to win3d and glPanel */
	transient public StsActionManager mainWindowActionManager = null;
	/** database transaction to which database commands are written; null if no current action */
	transient public StsTransaction currentTransaction = null;
	/** user Preferences window geometry */
	transient protected WindowGeometry winGeom = new WindowGeometry();
	/** plug-in for the workflow process for this model */
	transient public StsWorkflow workflowPlugIn;
	/** plug-in for the workflow process for this model */
	transient public String[] workflowPlugInNames;
	/** plug-in for the workflow process for this model */
	transient public boolean[] workflowPlugInStatus;
	/** the principle 3d window where action takes place */
	transient public StsWin3d win3d = null;
	/** convenience copy of objectTreePanel; this can be set explicitly in testing the objectTreePanel */
	transient public StsObjectTreePanel objectTreePanel;
	/** convenience copy of objectTreePanel; this can be set explicitly in testing the objectTreePanel */
	transient public StsDataFilesTreePanel dataFilesTreePanel;
	/** openGL panel in win3d */
	transient private StsGLPanel3d glPanel3d = null;
	/** common GLContext for all graphics in main family */
	// jbw moved to Main class transient public GLContext glContext;
	/** ok to call the windows display methods; turn off when model is in indeterminate state */
	transient public boolean displayOK = false;
	/** Indicates waiting for display */
	transient boolean displayWait = false;
	/** Lock object for display */
	transient Object displayLock = new Object();
	/** primary classes (values) and class names (keys) */
	transient public HashMap<String, StsClass> classes = new HashMap<>(25);
	/** database file */
	transient private StsDBFile db = null;

	/** list of classes which have vector elements which should be drawn before textureDisplayableClasses */
	transient public TreeSet<StsClassDisplayable> displayableClasses = new TreeSet<>();
	/** list of classes which have texture elements drawn after displayableClasses */
	transient public TreeSet<StsClassTextureDisplayable> textureDisplayableClasses = new TreeSet<>();
	/** extra list of instances which are to be visible */
	transient public TreeSet<StsInstance3dDisplayable> displayableInstances = new TreeSet<>();
	/** classes which are visible as textures on 3d cursors */
	transient public TreeSet<StsClassCursor3dTextureDisplayable> cursor3dTextureDisplayableClasses = new TreeSet<>();
	/** classes which are visible on 3d cursors using conventional drawing */
	transient public TreeSet<StsClassCursorDisplayable> cursorDisplayableClasses = new TreeSet<>();
	/** classes using displayLists; called to delete lists when model is exited */
	transient public TreeSet<StsClass> classesUsingDisplayLists = new TreeSet<>();
	/** classes which are visible on surfaces */
	transient public TreeSet<StsClassSurfaceDisplayable> surfaceDisplayableClasses = new TreeSet<>();
	/** classes which are visible on time-series plot */
	transient public TreeSet<StsClassTimeSeriesDisplayable> timeSeriesDisplayableClasses = new TreeSet<>();
	/** classes which are are groups of objects selectable by StsClassObjectPanel(s) */
	transient public TreeSet<StsClass> objectSelectableClasses = new TreeSet<>();
	/** classes whose istances are not rotated and are added/deleted to/from the project expanding/shrinking the project boundingBoxes */
	transient public TreeSet<StsUnrotatedClass> unrotatedClasses = new TreeSet<>();
	/** classes whose istances are rotated and are added/deleted to/from the project expanding/shrinking the project boundingBoxes */
	transient public TreeSet<StsRotatedClass> rotatedClasses = new TreeSet<>();
	/** classes which are pickable in 3d view */
	transient public TreeSet<StsClassViewSelectable> selectable3dClasses = new TreeSet<>();
	/** classes which are volumes or virtual volumes; generally subclassed from StsSeismicClass */
	//transient public TreeSet<StsSeismicClass> volumeClasses = new TreeSet<StsSeismicClass>();
	/** User preferences saved in user.preferences file in home directory */
	transient Properties preferences = null;
	/** splash screen visible while db is being loaded */
	transient StsSplashScreen splash;
	/** use display lists (recommended) when true */
	transient public boolean useDisplayLists = true;

	static final int initialSize = 10;
    static final int initialSizeInc = 10;

	/** constructs a singleton StsModel database class descriptor */
	//	static public final StsDBClassType modelDBClass = StsDBClass.constructor(StsModel.class);

	/** general purpose microsecond timer */
	static private StsTimer timer;

	/** minimum allowed time (msec) between draw commands from master in collaboration */
	static double minElapsedTime = 100;

	/** version number (changed with each version release) */
	static public final String version = "1.0.1.cgg";

	/** CANNOT_START (red) - required input not available */
	static public final int CANNOT_START = 0;

	/** CAN_START (yellow) - required input available, but step has not been executed */
	static public final int CAN_START = 1;

	/** STARTED (green) - step has been executed one or more times and can be repeated */
	static public final int STARTED = 2;

	/**
	 * ENDED (blue) - step has been terminated because: 1) it can be executed only once,
	 * 2) a next step using this one has been executed thus locking it from further execution
	 */
	static public final int ENDED = 3;

	/** key in preferences for the full classname of the current workflow plug-in */
	static public final String WORKFLOW_PLUGIN = "WorkflowPlugIn";

	static final boolean debug = false;

	//	static public StsModel currentModel = null;

	/** default constructor */
	public StsModel()
	{
	}

	/** default constructor - added for new database design JKF */
	/*
        public StsModel(boolean b)
        {
        }
    */

	/**
	 * constructor called when application started: no current project or model database is loaded.
	 * The project is generally a default project to be replaced when actual project or db is loaded.
	 */
	static public StsModel constructor(boolean initialPanelConfiguration)
	{
		try
		{
			return new StsModel(initialPanelConfiguration);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor(StsProject) failed.", e, StsException.WARNING);
			return null;
		}
	}

	static public StsModel constructDefaultMainWindow()
	{
		try
		{
			return new StsModel(false);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsModel.class, "constructorDefaultMainWindow", e);
			return null;
		}
	}

	static public StsModel constructSplashMainWindow()
	{
		try
		{
			StsModel model = new StsModel(true);
			if(getLaunchFileName() != null)
			{
				return newModelFromLaunchFile(model);
			}
			return model;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsModel.class, "constructSplashMainWindow", e);
			return null;
		}
	}

	/**
	 * method to create a new model based on parameters from a launch file
	 * The name of the Launch File is found in Main.S2SArgs using getLaunchFileName()
	 * <p/>
	 * If model already exists, just opens it instead of creating a new one.
	 * <p/>
	 * Parameters in the Launch File are accessed through the StsProperties class.
	 * @param model - this is needed to get the mainWindowActionManager
	 * @return new model created
	 * @throws IOException if it has problems reading the Launch File
	 */
	private static StsModel newModelFromLaunchFile(StsModel model) throws IOException
	{
		String errorMessages = "";
		StsProperties properties = new StsProperties();
		try
		{
			properties.readFromFile(new StsAsciiFile(StsFile.constructor(getLaunchFileName())));
		}
		catch(Exception e)
		{
			errorMessages = "StsModel.newModelFromLaunchFile\n\nFailed to Read Launch File!\n\nLaunch File: " + getLaunchFileName();
			System.err.println(errorMessages);
			new StsMessage(null, StsMessage.ERROR, errorMessages);
			return model;
		}

		//... Test for existing project - if it already exists, just open and return
		String dbPathName = null;
		try
		{
			dbPathName = properties.getProperty("dbPathName");
		}
		catch(Exception e)
		{
			errorMessages += " dbPathName";
		}
		if(dbPathName == null)
		{
			new StsMessage(null, StsMessage.ERROR, "Database Path Not Found In Launch File!\n\n" +
					"Launch File: " + getLaunchFileName());
			return model;
		}
		File file = new File(dbPathName);
		if(file.exists())
		{
			if(StsDBFile.fileOK(StsFile.constructor(dbPathName)))
			{
				System.out.println("Loading existing model \"" + dbPathName + "\" ...");
				try
				{
					model.mainWindowActionManager.startAction(StsOpenModel.class, new Object[]{dbPathName});
					model.launchFileName = getLaunchFileName();
					return model;
				}
				catch(Exception e)
				{
					new StsMessage(null, StsMessage.ERROR, "Error while reading database!\n\nDatabase File: " + dbPathName +
							"\n\nCreating new database...");
				}
			}
			new StsMessage(null, StsMessage.ERROR, "Error while reading database!\n\nDatabase File: " + dbPathName +
					"\n\nCreating new database...");
		}
		else
		//... No existing project (or open failed) - create new one instead
		{
			try
			{
				File dbPath = new File(dbPathName);
				if(!dbPath.exists())
				{
					dbPath.mkdirs();
					dbPath.createNewFile();
				}
			}
			catch(Exception e)
			{
				errorMessages += " dbPathName";
			}
		}

		StsProject project = new StsProject();

		System.out.println("Creating new model \"" + dbPathName + "\" ...");
		try
		{
			project.setName(properties.getProperty("projectName")); //these methods throw NPE if property not there
		}
		catch(Exception e)
		{
			errorMessages += " projectName";
		}
		try
		{
			project.setTimeUnits(StsParameters.getTimeUnitsFromString(properties.getProperty("timeUnits")));
			if(project.getTimeUnits() == StsParameters.TIME_NONE) errorMessages += " timeUnits";
		}
		catch(Exception e)
		{
			errorMessages += " timeUnits";
		}
		try
		{
			project.setXyUnits(StsParameters.getDistanceUnitsFromString(properties.getProperty("xyUnits")));
			if(project.getXyUnits() == StsParameters.DIST_NONE) errorMessages += " xyUnits";
		}
		catch(Exception e)
		{
			errorMessages += " xyUnits";
		}
		try
		{
			project.setDepthUnits(StsParameters.getDistanceUnitsFromString(properties.getProperty("depthUnits")));
			if(project.getDepthUnits() == StsParameters.DIST_NONE) errorMessages += " depthUnits";
		}
		catch(Exception e)
		{
			errorMessages += " depthUnits";
		}
		if(!errorMessages.equals(""))
		{
			errorMessages = "StsModel.newModelFromLaunchFile\n\nError loading: " + errorMessages + "!\n\nInput File: " + getLaunchFileName();
			System.err.println(errorMessages);
			new StsMessage(null, StsMessage.ERROR, errorMessages);
		}
		StsProject.addDbPathname(dbPathName);
		model.close();
		model = StsModel.constructor(project, dbPathName);
		try
		{
			model.getProject().setDirectoryPath(StsProject.PreStackSeismicDirectory, properties.getProperty(StsProject.PreStackSeismicDirectory));
		}
		catch(Exception e)
		{
			errorMessages += " PreStackSeismicDirectory";
		}
		//model.mainWindowActionManager.startAction(StsModel.class, new Object[] {project, dbPathName});
		model.launchFileName = getLaunchFileName();
		return model;
	}

	/**
	 * Searches command line arguments for "launchFile" string
	 * If it is found, following argument is interpreted to be filename
	 * @return name of file or null
	 */
	private static String getLaunchFileName()
	{
		String[] args = Main.S2Sargs;
		String launchFileName = null;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].toLowerCase().contains(Main.LauchFileFlag.toLowerCase()) && (i + 1) < args.length)
			{
				launchFileName = args[i + 1];
				break;
			}
		}
		return launchFileName;
	}

	static public StsModel constructor(String projectDirName)
	{
		try
		{
			StsProject project = new StsProject(projectDirName);
			return new StsModel(project);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor(StsProject) failed.", e, StsException.WARNING);
			return null;
		}
	}

	static public StsModel constructor(String projectDirName, String dbName)
	{
		try
		{
			StsProject project = new StsProject(projectDirName);
			project.setName(dbName);
			// String dbPathname = project.getRootDirectory() + File.separator + StsProject.MODEL_FOLDER + File.separator + dbName;
			return new StsModel(project);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor(StsProject) failed.", e, StsException.WARNING);
			return null;
		}
	}

	/** Used for stand-alone testing only.  Does not have usage initiated and may therefore be a security issue. */
	private StsModel(StsProject project)
	{
		StsObject.setCurrentModel(this);
		addShutdownHook();
		if(Main.isSwingCheck)
			RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
		workflowPlugInNames = new String[]{"com.Sts.PlugIns.Model.Workflows.StsModelConstructionWorkflow"};
		//        if(loadWorkflowPlugIn(workflowPlugInNames[0]))
		this.project = project;
		initializeDB(project.getName());
		initializeModel(); // create required stsClasses and initializes
		createSpectrums(); // standard spectrums; not persisted to db
		mainWindowActionManager = new StsActionManager(this); // actionManager is the controller
		getCreateCurrentTransaction("newModel"); // start initial transaction
		project.dbInitialize(this); // make project persistable for db writes
		project.initializeZDomainAndRebuild();
		//        commit(); // commit this initial transaction to db
	}

	public StsModel restart()
	{
		StsModel model = null;
		if(db == null)
			return StsModel.constructor(true);
		else
		{
			String pathname = db.getURLPathname();
			return StsModel.constructFromDb(pathname);
		}
	}

	// hack for now until we get rid of glPanel3d in model (I think).
	// returns main window glPanel3d containing a view3d, a viewCursor, or lastly the first glPanel
	public StsGLPanel3d getGlPanel3d()
	{
		StsGLPanel3d glPanel3d = null;
		glPanel3d = win3d.getGlPanelWithView(StsView3d.class);
		if(glPanel3d != null) return glPanel3d;
		glPanel3d = win3d.getGlPanelWithView(StsViewCursor.class);
		if(glPanel3d != null) return glPanel3d;
		return win3d.glPanel3ds[0];
	}

	public void setGlPanel3d(StsGLPanel3d glPanel3d)
	{
		this.glPanel3d = glPanel3d;
	}

	public TreeSet<StsClassTimeSeriesDisplayable> getTimeSeriesDisplayableClasses()
	{
		return timeSeriesDisplayableClasses;
	}

	public TreeSet<StsClass> getStsClassObjectSelectableClasses()
	{
		return objectSelectableClasses;
	}

	public void setProject(StsProject project)
	{
		this.project = project;
	}

	public class ThreadCheckingRepaintManager extends RepaintManager
	{
		public synchronized void addInvalidComponent(JComponent jComponent)
		{
			checkThread(jComponent);
			super.addInvalidComponent(jComponent);
		}

		private void checkThread(JComponent jComponent)
		{
			if(jComponent.isDisplayable() && !SwingUtilities.isEventDispatchThread())
			{
				String currentThreadName = Thread.currentThread().getName();
				System.err.println("Swing thread error. Not on event thread. Current thread: " + currentThreadName + " component: " + jComponent.getName());
				Exception e = new Exception();
				e.printStackTrace();
				String rootParentName = getRootParentName(jComponent);
				System.err.println("    Parent component: " + rootParentName);
			}
		}

		private String getRootParentName(Component component)
		{
			Component parentComponent;
			while ((parentComponent = component.getParent()) != null)
				component = parentComponent;
			return component.toString();
		}

		public synchronized void addDirtyRegion(JComponent jComponent,
												int i, int i1, int i2, int i3)
		{
			checkThread(jComponent);
			super.addDirtyRegion(jComponent, i, i1, i2, i3);
		}
	}


	/**
	 * Private constructor called by corresponding static constructor.
	 * /** constructor called when application started: no current project or model database is loaded.
	 * The project is generally a default project to be replaced when actual project or db is loaded.
	 */
	private StsModel(boolean initialPanelConfiguration)
	{
		//		 super(false);
		//        splash = StsSplashScreen.createAndShow(version);
		try
		{
			StsObject.setCurrentModel(this);
			addShutdownHook();
			//Main.startUsage();
			if(Main.isSwingCheck)
				RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());

			//        actionManager = new StsWindowActionManager(this);
			boolean persistProject = !initialPanelConfiguration;
			project = new StsProject(persistProject);
			if(!initialPanelConfiguration)
				initializeDB(project.getName());

			loadWorkflowPlugIn();

			initializeModel(); // create required stsClasses and initializes
			if(persistProject) add(project);
			createSpectrums(); // standard spectrums; not persisted to db

			getCreateCurrentTransaction("newModel"); // start initial transaction

			win3d = viewPersistManager.newStsWin3d(this, initialPanelConfiguration);
			initializeTabbedPanels();

			mainWindowActionManager = win3d.getActionManager();
			//         StsUserPreferencesDialog.assignUserPreferences(this);
			//         project.initializeUserPreferences();

			if(!initialPanelConfiguration)
			{
				project.dbInitialize(this); // make project persistable for db writes
				project.initializeZDomainAndRebuild();
			}

			//           workflowPlugIn.createObjectsPanel(win3d.objectTreePanel, this);
			//        win3d.objectTreePanel.createTreeModel(this);

			commit(); // commit this initial transaction to db

			//        createTypeLibrary(); // default type library; not persisted in db

			enableDisplay();

			//        win3d.win3dDisplay();
		}
		finally
		{
			//            splash.dispose();
		}
	}

	/** constructor called when a new project has been selected. */
	static public StsModel constructor(StsProject project, String dbPathname)
	{
		try
		{
			return new StsModel(project, dbPathname);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor(project, dbPathname) failed.", e, StsException.WARNING);
			return null;
		}
	}

	/**
	 * Private constructor called by corresponding static constructor.
	 * constructor called when a new project has been selected.
	 */
	private StsModel(StsProject project, String dbPathname)
	{
		//		super(false);
		//        StsSplashScreen splash = null;
		try
		{
			StsObject.setCurrentModel(this);
			addShutdownHook();
			//            splash = StsSplashScreen.createAndShow(version);
			//Main.startUsage();

			this.project = project;
			initializeDbFromPath(dbPathname);
			loadWorkflowPlugIn();

			initializeModel(); // classInitialize model for new project
			createSpectrums(); // standard spectrums; not persisted to db

			// mainWindowActionManager = new StsActionManager(this, win3d); // actionManager is the controller
			getCreateCurrentTransaction("newModel"); // start initial transaction

			viewPersistManager = new StsViewPersistManager(this);
			win3d = viewPersistManager.constructDefaultMainWindow();
			initializeTabbedPanels();
			mainWindowActionManager = win3d.getActionManager();

			propertiesPersistManager = new StsPropertiesPersistManager(this);

			getGlPanel3d().checkAddView(StsView3d.class);
			// win3d.start();

			setActionStatus(StsNewModel.class.getName(), STARTED);

			project.dbInitialize(this); // make project persistable for db writes
			project.initializeZDomainAndRebuild();
			//		setWriteTransaction(true);
			//		setProperty("writeTransaction", true);
			if(db != null)
			{
				db.writeModel(this);
			}
			// workflowPlugIn.createObjectsPanel(win3d.objectTreePanel, this);
			//        win3d.objectTreePanel.createTreeModel(this);

			commit(); // commit initial transaction to db

			//        createTypeLibrary(); // default type library; not persisted in db

			/*
                 checkLoadWindowPreferences(true);
                 doInitializePreferences();

                 // initial prefs for initial window
                 winGeom.setWindowPrefs(win3d);
            */
			enableDisplay();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "constructor(StsProject project, String dbPathname", e);
		}
		finally
		{
			//            splash.dispose();
		}
	}

	/**
	 * constructor called when existing database has been selected.
	 * Must be called on event thread so any GUI components (splash screen, etc) are created on event thread.
	 * The worker thread is then run so it can update GUIs on event thread without blocking it.
	 */
	static public StsModel constructor(StsAbstractFile file)
	{
		try
		{
			StsModel model = new StsModel();
			model.addShutdownHook();
			if(!model.initialize(file)) return null;
			StsMessageFiles.logMessage("Read DB for model : " + model.getName());
			return model;
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor() failed.", e, StsException.WARNING);
			return null;
		}
	}

	static public StsModel constructFromDb(String dbPathname)
	{
		StsFile file = StsFile.constructor(dbPathname);
		if(file == null) return null;
		if(!StsDBFile.fileOK(file)) return null;
		return StsModel.constructor(file);
	}
	/**
	 * it was hoped that this method would be called when app is abnormally terminated (by the IDE for example).
	 * It isn't called unfortunately, but may be a windows-only issue.
	 */
	private void addShutdownHook()
	{
		/* commented out temporarily to see if it's causing hang problem.  TJL 11/25/09
        Thread shutdownThread = new Thread
            (
                new Runnable()
                {
                    public void run()
                    {
                        close();
                    }
                }
            );
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        */
	}

	private boolean initialize(StsAbstractFile file) throws StsException
	{
		try
		{
			splash = StsSplashScreen.createAndShow(version);
			//Main.startUsage();
			disableDisplay();
			StsObject.setCurrentModel(this);

			db = StsDBFile.openRead(this, file, splash.progressBar, false);
			if(db == null) return false;

			try
			{
				Thread.sleep(100);
			}
			catch(InterruptedException ex)
			{
				ex.printStackTrace();
			}
			splash.setValue(1);

			initializeClassLists();
			//		initializeClassFields();
			//		initializeClasses();
			createSpectrums(); // standard spectrums; not persisted to db

			StsTimer overallTimer = new StsTimer();
			overallTimer.start();

			db.blockTransactions();
			//		setProperty("writeTransaction", false);
			StsObject.setCurrentModel(this);
			db.readModel();
			initializeProject();
			db.closeReadOpenWritePosition();
			db.saveTransactions();
			//		setProperty("writeTransaction", true);
			// properties must be loaded first
			propertiesPersistManager.setModel(this);
			propertiesPersistManager.restore();
 			project.setDatabaseInfo(db);
			project.initialize(this);
			initializeClassFields();
			postDbLoadInitializeClasses();

			StsGLDraw.initialize();  // jbw hack fix to initialize sphere drawing


			if(Main.isDbIODebug) db.debugCheckWritePosition("model after dbInitializeClasses");
			initializeDisplayableInterfaceLists();
			//        project.setModel(this);
			overallTimer.stop();
			StsMessageFiles.logMessage("Loaded model:  " + name + " from db file:  " + db.getFilename());
			StsMessageFiles.logMessage("Model load time: " + overallTimer.getTimeString());

			loadWorkflowPlugIn();

			StsMessageFiles.logMessage("Initializing the 3D window ...");
			//             mainWindowActionManager = new StsActionManager(this, win3d);
			//            dbInitializeClasses();
			viewPersistManager.setModel(this);
			win3d = viewPersistManager.restore(false); // view persistence
			mainWindowActionManager = win3d.getActionManager();
			getProject().initializeZDomainAndRebuild();
			// project.dbInitialize(this);
			initializeProperties();
			setGlPanel3d(win3d.getGlPanel3d());
			//			win3d.start();
			if(debug) StsException.systemDebug(this, "initialize", "win3d started.");

			//        StsUserPreferencesDialog.assignUserPreferences(this);
			//        project.initializeUserPreferences();

			// workflowPlugIn.createObjectsPanel(win3d.objectTreePanel, this);
			workflowPlugIn.addAdditionalToolbars(win3d);
			initializeActionStatus();
			initializeTabbedPanels();
			// enable preferences for future
			checkLoadWindowPreferences(false);
			enableDisplay();
			finalInitializeClasses();
			if(Main.isDbIODebug) db.debugCheckWritePosition("model after finalInitializeClasses");
			return true;
		}
		finally
		{
			splash.dispose();
			win3dDisplayAll();
		}
	}

	/** create convenience copies of object and data panels */
	private void initializeTabbedPanels()
	{
		// initialize the object panel with existing objects
		objectTreePanel = win3d.objectTreePanel;
		//objectTreePanel.createObjectPanelNodes(this);
		// initialize the hierarchical file data panel
		dataFilesTreePanel = win3d.dataFilesTreePanel;
		//dataFilesTreePanel.createFileTree(this);
	}

	private void initializeProject()
	{

		setProject((StsProject) getStsClassObjectWithIndex(StsProject.class, 0));
		project.initializeBoundingBoxes();
	}

	static public StsModel constructor(DataInputStream dis_)
	{
		try
		{
			final DataInputStream dis = dis_;
			final StsModel model = new StsModel();
			model.addShutdownHook();
			Runnable runnable = new Runnable()
			{
				public void run()
				{
					try
					{
						model.initialize(dis);
						StsMessageFiles.logMessage("Read DB for model : " + model.getName());
					}
					catch(Exception e)
					{
						StsException.systemError(e.getMessage());
					}
				}
			};
			StsToolkit.runLaterOnEventThread(runnable);
			return model;
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.constructor() failed.", e, StsException.WARNING);
			return null;
		}
	}

	private void initialize(DataInputStream dis) throws StsException
	{
		try
		{
			splash = StsSplashScreen.createAndShow(version);
			//Main.startUsage();
			disableDisplay();
			initializeDB("temp");
			db = StsDBFile.openRead(dis, splash.progressBar);
			if(db == null)
			{
				throw new StsException(StsException.WARNING, "StsModel.constructor(StsAbstractFile) failed.");
			}

			StsObject.setCurrentModel(this);

			initializeClassLists();
			//		initializeClasses();
			createSpectrums(); // standard spectrums; not persisted to db

			StsTimer overallTimer = new StsTimer();
			overallTimer.start();

			db.readModel();
			db.closeReadOpenWritePosition();
			db.saveTransactions();

			initializeDisplayableInterfaceLists();
			overallTimer.stop();
			StsMessageFiles.logMessage("Loaded model:  " + name + " from db file:  " + db.getFilename());
			StsMessageFiles.logMessage("Model load time: " + overallTimer.getTimeString());

			loadWorkflowPlugIn();

			getProject().setDatabaseInfo(db);

			propertiesPersistManager.setModel(this);
			propertiesPersistManager.restore(); // from dataInputStream??

			initializeClassFields();
			postDbLoadInitializeClasses();
			initializeDisplayableInterfaceLists();
			overallTimer.stop();
			StsMessageFiles.logMessage("Loaded model:  " + name + " from db file:  " + db.getFilename());
			StsMessageFiles.logMessage("Model load time: " + overallTimer.getTimeString());

			StsMessageFiles.logMessage("Initializing the 3D window ...");

			viewPersistManager.setModel(this);
			win3d = viewPersistManager.newStsWin3d(this, false);

			initializeProperties();

			setGlPanel3d(win3d.getGlPanel3d());
			//glPanel3d.checkAddView(StsView3d.class);
			// win3d.start();

			// .createObjectsPanel(win3d.objectTreePanel, this);

			//        win3d.objectTreePanel.createTreeModel(this);

			//        createTypeLibrary(); // default type library

			enableDisplay();
			finalInitializeClasses();
			//        win3d.win3dDisplay();
		}
		finally
		{
			splash.dispose();
		}
	}

	/** Called when model object is loaded from a database for initialization. */
	public boolean initialize(StsModel model)
	{
		getProject().initialize(this);
		return true;
	}

	public boolean delete()
	{
		return false;
	}

	/** classInitialize model classInitialize after project is loaded */
	public void initializeModel()
	{
		// StsObject.setCurrentModel(this);
		project.initializeDefaultBoundingBoxes();
		initProperties();
		initStateProperties();
		initializeClassLists();
		initializeClassFields();
		projectInitializeClasses();
		initializePreferences();
		this.name = project.getName();
	}

	public void setClasses(StsClass[] classList)
	{
		if(classList == null) return;
		for(int n = 0; n < classList.length; n++)
		{
			StsClass stsClass = classList[n];
			Class instanceClass = StsToolkit.getClassForName(stsClass.instanceClassname);
			if(instanceClass == null) continue;
			StsClass currentStsClass = getCreateStsClass(instanceClass);
			StsToolkit.copyAllObjectNonTransientFields(stsClass, currentStsClass);
//			currentStsClass.dbInitialize();
		}
	}

	private void checkLoadUserPreferences()
	{
		try
		{
			if(preferences != null) return;
			preferences = new Properties();
			String directory = System.getProperty("user.home");
			StsFile file = StsFile.constructor(directory, "user.preferences");
			if(!file.exists())
			{
				System.out.println("No user preferences available, defaulting to program settings.");
				return;
			}
			preferences.load(file.getInputStream());
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.checkLoadUserPReferences() failed.", e, StsException.WARNING);
		}
	}

	private void checkLoadWindowPreferences(boolean setNow)
	{
		try
		{
			String directory = System.getProperty("user.home");
			StsFile file = StsFile.constructor(directory, getWorkflowPlugIn().name + ".windows");
			if(!file.exists())
				return;

			winGeom.loadWindowGeometry();
			//if (setNow)
			//   winGeom.launchWindows(this);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.checkLoadUserPReferences() failed.", e, StsException.WARNING);
		}
	}

	/**
	 * Private method used to create all the default color spectrums
	 * standard spectrums; not persisted to database
	 */
	public void createSpectrums()
	{
		StsSpectrumClass spectrumClass = (StsSpectrumClass) getCreateStsClass(StsSpectrum.class);
		spectrumClass.createSpectrums();
	}

	/** load the available Workflow plug-in(s). A workflow plug-in must be in the package "com.Sts.PlugIns" and end with "Workflow".
     *  For example: com.Sts.PlugIns.Model.Workflows.StsModelConstructionWorkflow. Note that the "Workflows" subpackage name is optional but organizationally useful.
     *  Beginning with the rootPackageName "com.Sts.PlugIns", this method searches for all "*.class" files ending with "Workflow.class"
     *  and returns a list of all classNames that meet these requirements.  These classes are then loaded and the list of classNames are saved */
	private void loadWorkflowPlugIn()
	{
		try
		{
            String rootPackageName = "com.Sts.PlugIns";
            String ending = "Workflow";
            List<String> workflowClassNamesList = new ArrayList<>();
            getClassNamesEndingWith(rootPackageName, ending, workflowClassNamesList);
            workflowPlugInNames = workflowClassNamesList.toArray(new String[0]);
            workflowPlugInStatus = new boolean[workflowPlugInNames.length];
            Arrays.fill(workflowPlugInStatus, true);

			if(workflowPlugInNames.length == 0)
			{
				new StsMessage(this.win3d, StsMessage.WARNING, "Failed to find any workflow plug-ins.");
				return;
			}

			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String userPrefPlugIn = prefs.get(WORKFLOW_PLUGIN, null);
			// load the last userPrefPlugIn or if not found, load the first available plugin
			if(userPrefPlugIn != null)
			{
				for(String workflowPlugInName : workflowPlugInNames)
				{
					if(workflowPlugInName.indexOf(userPrefPlugIn) != -1 && loadWorkflowPlugIn(workflowPlugInName))
						return;
				}
			}
			// if selected workflow fails to load, load the first available one
			for(String workflowPlugInName : workflowPlugInNames)
				if(loadWorkflowPlugIn(workflowPlugInName)) return;
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.loadWorkflowPlugIn() failed.", e, StsException.WARNING);
		}
	}

    private static void getClassNamesEndingWith(String parentPackageName, String ending, List<String> classNames) throws ClassNotFoundException, IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = parentPackageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements())
        {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (File directory : dirs)
            getClassNamesEndingWith(directory, parentPackageName, ending, classNames);
    }
    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *  @param directory   The base directory
     * @param parentPackageName
     * @param classNames   @return The classes
     * @throws ClassNotFoundException
     */
    private static void getClassNamesEndingWith(File directory, String parentPackageName, String ending, List<String> classNames) throws ClassNotFoundException
    {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                String dirName = file.getName();
                if(!dirName.contains("."))
                    getClassNamesEndingWith(file, parentPackageName + "." + dirName, ending, classNames);
            }
            else if (file.getName().endsWith(ending + ".class"))
            {
                classNames.add(parentPackageName + "." + file.getName().substring(0, file.getName().length() - 6));
            }
        }
    }

	// Needed on open or creation of new project to kill time Thread if still running.
	public boolean stopTime()
	{
		getProject().setProjectTimeToCurrentTime();
		StsTimeActionToolbar toolbar = win3d.getTimeActionToolbar();
		if(toolbar != null) toolbar.endAction();
		return true;
	}

	/** commits transactions to the database */
	public boolean commit()
	{
		if(currentTransaction == null) return false;
		boolean ok = currentTransaction.commit(this);
		currentTransaction = null;
		return ok;
	}

	public boolean repeatAction()
	{
		if(currentTransaction == null) return false;
		String transactionName = currentTransaction.getName();
		commit();
		currentTransaction = new StsTransaction(transactionName);
		return true;
	}

	/** returns the current transaction or creates a new one with name */
	public StsTransaction getCreateCurrentTransaction(String name)
	{
		if(currentTransaction == null)
			currentTransaction = new StsTransaction(name + nTransaction++);
		return currentTransaction;
	}

	public void initializeTransaction(String name)
	{
		if(currentTransaction != null) commit();
		currentTransaction = new StsTransaction(name + nTransaction++);
	}

	/**
	 * if transaction exists, adds this dbCommand;
	 * otherwise, create a new transaction, add command and commit
	 */
	public void getCreateTransactionAddCmd(String name, StsDBCommand cmd)
	{
		if(mainWindowActionManager == null || db == null) return;

		if(db.transactionsBlocked()) return;

		if(currentTransaction == null)
		{
			currentTransaction = new StsTransaction(name + "-" + nTransaction++);
			currentTransaction.add(cmd);
			commit();
		}
		else
			currentTransaction.add(cmd);
	}


	public void addCmdCommit(String name, StsDBCommand cmd)
	{
		if(mainWindowActionManager == null) return;

		if(currentTransaction == null)
		{
			currentTransaction = new StsTransaction(name + "-" + nTransaction++);
			currentTransaction.add(cmd);
			commit();
		}
		else
		{
			currentTransaction.add(cmd);
			commit();
		}
	}

	/*
    private void loadWorkflowPlugIn()
    {
     ArrayList workflowPlugIns = new ArrayList();
     ArrayList plugIns = new ArrayList();
     String plugInPackageName = "com.Sts.WorkflowPlugIn.PlugIns";
//        String[] jarnames = new String[] {"Workflow.jar"};

    try
     {
      if(Main.isWebStart)
      {
    ClassLoader classLoader = getClass().getClassLoader();
    String[] resourceFilenames = JNLPUtilities.getResourceFilenames(classLoader);
    int nResourceFilenames = resourceFilenames.length;
    if(nResourceFilenames == 0) return;
    String[] workflowFilenames = new String[nResourceFilenames];
    int nWorkflowFilenames = 0;
    for(int n = 0; n < nResourceFilenames; n++)
    {
     String lowerCaseFilename = resourceFilenames[n].toLowerCase();
     if(lowerCaseFilename.indexOf("workflow") < 0) continue;
     workflowFilenames[nWorkflowFilenames++] = resourceFilenames[n];
     System.out.println("    jnlpResourceName: " + resourceFilenames[n]);
    }
    workflowFilenames = (String[])StsMath.trimArray(workflowFilenames, nWorkflowFilenames);

    for(int i=0; i<workflowFilenames.length; i++)
    {
     StsAbstractFileSet fileSet = StsWebStartJar.construct(workflowFilenames[i]);
     if(fileSet == null) continue;
     System.out.println("Found jar file: " + workflowFilenames[i]);
     String[] filenames = fileSet.getFilenames();

     int nFilenames = filenames.length;
     for(int n = 0; n < nFilenames; n++)
      System.out.println("    file: " + filenames[n]);

     plugIns = StsToolkit.truncateClassnames(filenames, plugInPackageName);
     System.out.println("Jar=" + workflowFilenames[i]);
     for(int j=0; j<plugIns.size(); j++)
      workflowPlugIns.add(plugIns.get(j));
     System.out.println("Total plugIns=" + workflowPlugIns.size());
    }
      }
      else
    workflowPlugIns = StsToolkit.findFilePackageClasses(plugInPackageName);

      int nWorkflowPlugIns = workflowPlugIns.size();
      if(nWorkflowPlugIns == 0)
      {
    StsException.systemError("StsModel.loadkWorkflowPlugIn() failed: no workflow plugins found in package: " + plugInPackageName);
    return;
      }

      workflowPlugInNames = new String[nWorkflowPlugIns];
      for(int n = 0; n < nWorkflowPlugIns; n++)
    workflowPlugInNames[n] = plugInPackageName + "." + (String)workflowPlugIns.get(n);

      Preferences prefs = Preferences.userNodeForPackage(getClass());
      String userPrefPlugIn = prefs.get(WORKFLOW_PLUGIN, null);
      if(userPrefPlugIn != null)
      {
    for(int n = 0; n < nWorkflowPlugIns; n++)
     if( workflowPlugInNames[n].indexOf(userPrefPlugIn) != -1 &&
      loadWorkflowPlugIn(workflowPlugInNames[n]) ) return;
      }
      loadWorkflowPlugIn(workflowPlugInNames[0]);
     }
     catch(Exception e)
     {
      StsException.outputException("StsModel.loadWorkflowPlugIn() failed.",
    e, StsException.WARNING);
     }
    }
    */
	public int getWorkflowPlugInIndex()
	{
		if(workflowPlugInNames == null)
		{
			return -1;
		}
		int nPlugIns = workflowPlugInNames.length;
		if(nPlugIns == 0)
		{
			return -1;
		}
		if(workflowPlugIn == null)
		{
			return -1;
		}
		for(int n = 0; n < nPlugIns; n++)
		{
			if(workflowPlugInNames[n].indexOf(workflowPlugIn.name) != -1)
			{
				return n;
			}
		}
		return -1;
	}

	/*
    public boolean setWorkflowPlugInByName(String pluginName)
    {
     if(workflowPlugInNames == null) return false;
     int nPlugIns = workflowPlugInNames.length;
     if(nPlugIns == 0) return false;
     for(int n = 0; n < nPlugIns; n++)
     {
      if (workflowPlugInNames[n].indexOf(pluginName) != -1)
      {
    Preferences prefs = Preferences.userNodeForPackage(getClass());
    prefs.put(WORKFLOW_PLUGIN, pluginName);
    return true;
      }
     }
     return false;
    }
    */
	public void setWorkflowPlugIn(StsWorkflow workflowPlugIn)
	{
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put(WORKFLOW_PLUGIN, workflowPlugIn.name);
		if(this.workflowPlugIn.name.equals(workflowPlugIn.name)) return;
		this.workflowPlugIn = workflowPlugIn;
		if(win3d != null) win3d.rebuildWorkflow(workflowPlugIn);
	}

	public StsWorkflow getWorkflowPlugIn()
	{
		return workflowPlugIn;
	}

	/** load this plug-in */
	public boolean loadWorkflowPlugIn(String plugInName)
	{
		try
		{
			ClassLoader classLoader = getClass().getClassLoader();
			Class plugInClass = classLoader.loadClass(plugInName);
			if(plugInClass == null)
			{
				return false;
			}
			StsMessageFiles.logMessage("Loading plug-in: " + plugInName);
			workflowPlugIn = (StsWorkflow) plugInClass.newInstance();
			setWorkflowPlugIn(workflowPlugIn);
			return true;
		}
		catch(Exception e)
		{
			StsException.systemError("Failed to load plug-in: " + plugInName);
			return false;
		}
	}

	/** Get a list of all current plugins */
	//    public ArrayList getPlugIns() { return plugIns; }

	/** Return parent and all children of base window (win3d) */
	public StsWin3dBase[] getMainFamilyWindows()
	{
		return viewPersistManager.getFamilies()[0].getWindows();
	}

	public Iterator getMainWindowFamilyViewIterator()
	{
		return getMainWindowFamily().getWindowViewIterator();
	}

	public StsWin3dBase[] getWindows()
	{
		return viewPersistManager.getWindows();
	}

	public StsWindowFamily getMainWindowFamily()
	{
		return viewPersistManager.getFamilies()[0];
	}

	/** Return the windows associated with a particular parent window (StsWin3d or StsWin3dFull) */
	public StsWin3dBase[] getWindows(StsWin3dBase parent)
	{
		StsWindowFamily family = getWindowFamily(parent);
		if(family == null) return null;
		return family.getWindows();
	}

	public StsWin3dBase[] getFamilyWindows(StsWin3dBase win3d)
	{
		StsWin3d parent = (StsWin3d) getWindowFamilyParent(win3d);
		return parent.getFamilyWindows();
	}

	/** Return the windows associated with a particular parent index window (StsWin3d or StsWin3dFull) */
	public StsWin3dBase[] getWindows(int familyIndex)
	{
		StsWindowFamily family = viewPersistManager.getFamilies()[familyIndex];
		return family.getWindows();
	}

	public void resetWindowTitles(String dbName)
	{
		Iterator<StsWin3dBase> windowIterator = getFamilyWindowIterator();
		while (windowIterator.hasNext())
		{
			StsWin3dBase window = windowIterator.next();
			String windowName = getWindowFamilyTitle(window);
			window.setTitle(dbName + " " + windowName);
		}
	}

	public String getWindowFamilyTitle(StsWin3dBase window)
	{
		if(window == win3d)
		{
			return new String("    Main Window");
		}
		for(int f = 0; f < viewPersistManager.getFamilies().length; f++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[f];
			StsWin3dFull parent = family.getParent();
			if(parent == null)
			{
				StsException.systemError(this, "getWindowFamilyTitle", "family.parent is null!");
				continue;
			}
			int groupIndex = f + 1;

			if(parent == window)
			{
				return new String("   Master Window, Group " + groupIndex);
			}

			StsWin3dBase[] familyWindows = family.getWindows();
			for(int w = 1; w < familyWindows.length; w++)
			{
				StsWin3dBase auxWindow = familyWindows[w];
				if(window == auxWindow)
				{
					if(parent == win3d)
						return new String("   Auxiliary Window, Main Group, Window " + w);
					else
						return new String("   Auxiliary Window, Group " + groupIndex + ", Window " + w);
				}
			}
		}
		StsException.systemError("StsModel.getWindowFamilyTitle failed for window: " + window.getName());
		return "    Unknown Window";
	}

	/** Create an additional auxiliary window in same family as the one being copied */
	public StsWin3dBase createAuxWindow(StsWin3dBase win3d, StsViewItem viewItem)
	{
		disableDisplay();
		StsWin3dBase newWin3d = new StsWin3dBase(this, win3d);
		newWin3d.constructViewPanel(viewItem);
		newWin3d.initializeWindowLayout();
		newWin3d.start();
		enableDisplay();
		newWin3d.setVisible(true);
		return newWin3d;
	}

	public StsWin3dBase createAuxWindow(StsWin3dBase win3d, String viewName, StsView view)
	{
		disableDisplay();
		StsWin3dBase newWin3d = new StsWin3dBase(this, win3d);
		newWin3d.constructViewPanel(viewName, new StsView[]{view});
		newWin3d.initializeWindowLayout();
		newWin3d.start();
		enableDisplay();
		newWin3d.setVisible(true);
		return newWin3d;
	}

	/** Create an additional window family by copying this one and making it parent of new family */
	public StsWin3dFull createFullWindow(StsViewItem viewItem)
	{
		disableDisplay();
		StsWin3dFull newWin3d = new StsWin3dFull(this);
		newWin3d.constructViewPanel(viewItem);
		newWin3d.initializeWindowLayout();
		newWin3d.start();
		enableDisplay();
		newWin3d.setVisible(true);
		return newWin3d;
	}

	/* set a view via preferences */
	/*
        public StsView setViewPreferred(Class viewClass, String viewClassname)
        {
            return winGeom.setWindowPrefs(viewClass, viewClassname);

        }
    */
	/*
        public StsView setViewPreferred(StsView view)
        {
            // if all else fails, just set primary view
            return glPanel3d.checkAddView(view);
        }
    */

	/** Request each window from all families to set its view to the default view */
	/*
        public void setDefaultView()
        {
            for(int n = 0; n < viewPersistManager.families.length; n++)
            {
                StsWindowFamily family = viewPersistManager.families[n];
                family.setDefaultView();
            }
        }
    */
	public void validateToolbarStates()
	{
		StsWin3dBase[] windows = getMainFamilyWindows();
		for(int n = 0; n < windows.length; n++)
		{
			StsWin3dBase window = windows[n];
			window.validateToolbars();
		}
	}

	public void resetCursorPanel()
	{
		win3d.resetCursorPanel();
	}

	/*
    public void setCursorPanel()
    {
     win3d.setCursorPanel();
    }
    */
	public StsCursor3d getCursor3d()
	{
		return getCursor3d(win3d);
	}

	public StsCursor3d getCursor3d(StsWin3dBase window)
	{
		if(window == null)
		{
			return null;
		}
		else
		{
			StsWin3dBase parentWindow = this.getWindowFamilyParent(window);
			if(parentWindow instanceof StsWin3d)
				return ((StsWin3d) parentWindow).getCursor3d();
			else
				return ((StsWin3dFull) parentWindow).getCursor3d();
		}
	}

	/** classInitialize database when a new project is defined */
	private void initializeDB(String projectName)
	{
		try
		{
			if(projectName == null)
			{
				return;
			}
			String dirname = getProject().getProjectDirString() + StsProject.MODEL_FOLDER;
			String filename = "db." + projectName;
			File f = new File(dirname + filename);
			if(f.exists())
			{
				f.delete();
				//            db = new StsDBFile(f);
			}
			StsFile file = StsFile.constructor(dirname, filename);
			db = StsDBFile.openWrite(this, file, null);
			db.saveTransactions();
			//			setProperty("writeTransaction", true);
			System.out.println("DB file " + filename + " initialized.");
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.initializeDB() failed.", e, StsException.WARNING);
		}
	}

	/** Given a new database filename, classInitialize the database. */
	private void initializeDbFromPath(String dbPathname)
	{
		try
		{
			if(dbPathname == null)
			{
				return;
			}
			String filename = StsFile.getFilenameFromPathname(dbPathname);
			File f = new File(dbPathname);
			if(f.exists())
			{
				f.delete();
			}
			StsFile file = StsFile.constructor(dbPathname);
			db = StsDBFile.openWrite(this, file, null);
			db.saveTransactions();
			//			setProperty("writeTransaction", true);
			getProject().setDatabaseInfo(db);
			System.out.println("DB file " + filename + " initialized.");
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.initializeDB() failed.", e, StsException.WARNING);
		}
	}

	//	public StsStatusArea getStatusArea() { return status; }

	/** Redisplay just the main window */
	public void win3dDisplay()
	{
		boolean displayOK = this.displayOK && win3d != null;
		if(debug || Main.isDbDebug || Main.isGLDebug)
			if(debug) StsException.systemDebug(this, "win3dDisplay", "called.");
		if(!displayOK) return;
		win3d.win3dDisplay();
	}

	/** Redisplay all 3d windows from all families */
	public void win3dDisplayAll()
	{
		if(viewPersistManager == null || viewPersistManager.getFamilies() == null) return;
		if(debug || Main.isDbDebug || Main.isGLDebug)
			if(debug) StsException.systemDebug(this, "win3dDisplayAll", "display all families");
		if(!displayOK) return;
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			viewPersistManager.getFamilies()[n].win3dDisplay();
		}
		//   StsWellClass wc = (StsWellClass) getCreateStsClass(StsWell.class);
		//   wc.displayClass(null, getProject().getProjectTime());

		//Main.logUsageTimer();
	}

	/** Redisplay family of 3d windows */
	public void win3dDisplayAll(StsWin3dBase window)
	{
		if(debug || Main.isDbDebug || Main.isGLDebug)
			if(debug) StsException.systemDebug(this, "win3dDisplayAll(window)", "display family.");
		if(!displayOK) return;
		StsWindowFamily family = getWindowFamily(window);
		family.win3dDisplay();
	}

	public void displayIfCursor3dObjectChanged(StsObject object)
	{
		for(StsWindowFamily windowFamily : getWindowFamilies())
		{
			StsWin3dFull parentWindow = windowFamily.getParent();
			if(parentWindow.cursor3d.objectChanged(object))
			{
				for(StsWin3dBase window : windowFamily.getWindows())
					window.win3dDisplay();
			}
		}
	}

	public void viewObjectRepaint( Object object)
	{
		viewObjectRepaint(object, object);
	}

	public void viewObjectRepaint(Object source, Object object)
	{
		if(!displayOK) return;
		//        if(getGlPanel3d() == null) return;
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[i];
			family.viewObjectRepaint(source, object);
		}
		StsClass wc = getStsClass("com.Sts.PlugIns.Wells.DBTypesApp.StsWell");
		if(wc == null) return;
		wc.viewObjectRepaint(source, object);
	}

	public void repaintViews(Class viewClass)
	{
		if(!displayOK) return;

		if(StsWellView.class.isAssignableFrom(viewClass))
		{
			StsWellClass wc = (StsWellClass) getCreateStsClass(StsWell.class);
			Iterator wellIterator = wc.getObjectIterator();
			if(wellIterator.hasNext())
			{
				StsWell well = (StsWell) wellIterator.next();
				StsWellViewModel wellViewModel = well.wellViewModel;
				if(wellViewModel != null) wellViewModel.repaintViews(viewClass);
			}
		}
		else
		{
			for(StsWindowFamily windowFamily : getWindowFamilies())
				windowFamily.repaintViews(viewClass);
		}
	}

	public void viewObjectRepaintFamily(StsWin3dBase parentWindow, Object object)
	{
		if(!displayOK) return;
		if(parentWindow == null) return;
		getWindowFamily(parentWindow).viewObjectRepaint(parentWindow, object);

		StsWellClass wc = (StsWellClass) getCreateStsClass(StsWell.class);
		wc.viewObjectRepaint(parentWindow, object);
	}

	/** Call all views in all families and notify them that an object of possible interest has changed. */
	public boolean viewObjectChanged(Object source, Object object)
	{
		boolean changed = false;
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[i];
			changed = changed | family.viewObjectChanged(source, object);
		}
		StsClass stsClass = getStsClass("com.Sts.PlugIns.DBTypes.Well");
		if(stsClass == null) return changed;
		StsWellClass wc = (StsWellClass)stsClass;
		changed = changed | wc.viewObjectChanged(source, object);
		// StsWellClass lwc = (StsLiveWellClass) getCreateStsClass(StsLiveWell.class);
		// changed = changed | lwc.viewObjectChanged(source, object);
		return changed;
	}

	public boolean viewObjectChangedAndRepaint(Object source, Object object)
	{
		boolean changed = viewObjectChanged(source, object);
		if(changed) viewObjectRepaint(source, object);
		return changed;
	}
 /*
	public void clearTimeViews()
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			StsViewTimeSeries[] tWindows = viewPersistManager.getFamilies()[n].getTimeWindows();
			if(tWindows == null)
				continue;
			for(int i = 0; i < tWindows.length; i++)
			{
				tWindows[i].clearView();
			}
		}
	}

	public void resetTimeViews()
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			StsViewTimeSeries[] tWindows = viewPersistManager.getFamilies()[n].getTimeWindows();
			if(tWindows == null)
				continue;
			for(int i = 0; i < tWindows.length; i++)
			{
				tWindows[i].resetView(getProject());
			}
		}
	}
*/
	public void changeCursor3dObject(StsObject object)
	{
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWin3dBase[] windows = getWindows(i);
			for(int n = 0; n < windows.length; n++)
			{
				StsWin3dBase window = (StsWin3dBase) windows[n];
				window.getCursor3d().objectChanged(object);
			}
		}
	}

	/** newVolume could be an StsSeismicVolume or an StsVirtualSeismicVolume */
	public void toggleOnCursor3dObject(StsObject newVolume)
	{
		for(StsWindowFamily windowFamily : getWindowFamilies())
		{
			if(windowFamily.getCursor3d().toggleOn(newVolume))
				windowFamily.repaintViews();
		}
	}

	//TODO awkward design; win3d.cursor3d should each have a set of ObjectSelectGroups (currently seismicVolume and all subclasses,
	//TODO and crossplot; the instances of each group are visible as a separate ComboBox on the comboBoxToolbar
	//TODO Here, one of these may have been deleted and we need to select and toggle on another
	//TODO We may decide to have an StsComboBoxToolbar on each window with different selections so we can compare volumes, 2dViews, etc
	//TODO If we delete an object required by another object (like a virtualVolume), we need to delete that object as well
	public StsObject deleteCursor3dObject(StsObject object)
	{
		StsClass objectStsClass = object.getStsClass();
		if(!isStsClassCursor3dTextureDisplayable(objectStsClass)) return null;
		StsComboBoxToolbar comboBoxToolbar = (StsComboBoxToolbar) win3d.getToolbarNamed(StsComboBoxToolbar.NAME);
		StsObject newSelectedObject = comboBoxToolbar.deleteComboBoxObject(object);
		toggleOnCursor3dObject(newSelectedObject);
		if(newSelectedObject == null) deleteClassTextureDisplays(object.getClass());
		return newSelectedObject;
	}

	public void toggleOffCursor3dObject(StsObject newVolume)
	{
		for(StsWindowFamily windowFamily : getWindowFamilies())
		{
			if(windowFamily.getCursor3d().toggleOff(newVolume))
				windowFamily.repaintViews();
		}
	}

	public void toggleOffCursor3dObject(StsObject newVolume, int dirNo)
	{
		for(StsWindowFamily windowFamily : getWindowFamilies())
		{
			if(windowFamily.getCursor3d().toggleOffCursor(newVolume, dirNo))
				windowFamily.repaintViews();
		}
	}

	public void toggleOnCursor3dObject(StsObject newVolume, int dirNo)
	{
		for(StsWindowFamily windowFamily : getWindowFamilies())
		{
			if(windowFamily.getCursor3d().toggleOnCursor(newVolume, dirNo))
				windowFamily.repaintViews();
		}
	}

	public void deleteDisplayLists()
	{
		StsClass[] stsClasses = (StsClass[])classes.values().toArray(new StsClass[0]);
		for(StsClass stsClass : stsClasses)
			stsClass.deleteDisplayLists();
	}

	/** Set background color for all 3d windows from all families */
/*
    public void setClearColor(StsColor color)
    {
        for (int n = 0; n < viewPersistManager.families.length; n++)
            viewPersistManager.families[n].setClearColor(color);
        WellViewModelIterator wellViewModelIterator = new WellViewModelIterator();
        while(wellViewModelIterator.hasNext())
        {
            StsWellViewModel wellViewModel = wellViewModelIterator.next();
            wellViewModel.glPanel.setClearColor(color);
        }
    }
*/

	/**
	 * recompute values on cursor3d for each parent member in the fanily
	 * This is called when the grid check box is toggled requiring cursor
	 * values to be reset.
	 */
	public void resetAllSliderValues()
	{
		if(win3d.cursor3dPanel == null) return;
		this.win3d.cursor3dPanel.setSliderValues();
		for(int n = 1; n < viewPersistManager.getFamilies().length; n++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[n];
			StsWin3dFull win3dFull = family.getParent();
			if(win3dFull != null)
				win3dFull.cursor3dPanel.setSliderValues();
		}
	}

	public StsWin3dFull[] getParentWindows()
	{
		if(viewPersistManager == null || viewPersistManager.getFamilies() == null) return new StsWin3dFull[0];
		int nFamilies = viewPersistManager.getFamilies().length;
		StsWin3dFull[] parentWindows = new StsWin3dFull[nFamilies];
		for(int n = 0; n < nFamilies; n++)
			parentWindows[n] = viewPersistManager.getFamilies()[n].getParent();
		return parentWindows;
	}

	/**
	 * Adjust cursor and slider display for all 3d windows in same family as window argument.
	 * Call this when you want to programmaticaly set the slider.
	 * adjustCursor is called directly from the slider.
	 */
	public void adjustAllCursorsAndSliders(int dir, float dirCoor)
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[n];
			family.adjustCursor(dir, dirCoor);
			StsWin3dFull parent = family.getParent();
			parent.adjustSlider(dir, dirCoor);
		}
	}

	public void cursorRangeChanged()
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[n];
			StsWin3dFull parent = family.getParent();
			parent.getCursor3d().rangeChanged();
		}
	}

	public void cursorZRangeChanged()
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[n];
			StsWin3dFull parent = family.getParent();
			parent.getCursor3d().zRangeChanged();
		}
	}

	public void createTransientCursorChangeTransaction(int dir, float dirCoor)
	{
		StsDBMethodCmd cmd = new StsDBMethodCmd(this, "adjustCursorAndSlider", new Object[]
				{new Integer(dir), new Float(dirCoor)});
		addTransientTransactionCmd("cursorChange", cmd);
	}

	public void adjustCursorAndSlider(int dir, float dirCoor)
	{
		win3d.adjustCursorAndSlider(dir, dirCoor);
	}

	/** Add a new window family with argument as the parent */
	public void addWindowFamily(StsWin3d window)
	{
		StsWindowFamily newFamily = viewPersistManager.newWindowFamily(window);
		viewPersistManager.addFamily(newFamily);
	}

	/** Add a new window family with argument as the parent */
	public boolean addWindowFamily(StsWin3dFull window)
	{
		StsWindowFamily newFamily = viewPersistManager.newWindowFamily(window);
		viewPersistManager.addFamily(newFamily);
		return true;
	}

	public void subVolumeChanged()
	{
		// Get the singleton placeholder subVolumeClass
		StsSubVolumeClass subVolumeClass = (StsSubVolumeClass) getStsClass(StsSubVolume.class);
		// notify objects that subVolume isApplied has changed
		subVolumeClass.firePropertyChanged(new PropertyChangeEvent(subVolumeClass, "StsSubVolumeClass.isApplied",
				new Boolean(false), new Boolean(true)));

		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			viewPersistManager.getFamilies()[n].subVolumeChanged();
		}
		win3dDisplayAll();
	}

	/** Add an auxiliary window to the default window family (StsWin3d parent) */
	public boolean addWindow(StsWin3dBase window)
	{
		viewPersistManager.getFamilies()[0].addWindow(window);
		return true;
	}

	/**
	 * Add an auxiliary window to the family that argument belongs to.
	 * Limited to a maximum of 15 windows due to some graphics constraints
	 */
	public boolean addWindow(StsWin3dBase parent, StsWin3dBase window)
	{
		StsWindowFamily family = getWindowFamily(parent);
		family.addWindow(window);
		return true;
	}

	/**
	 * Add an auxiliary window to the family that argument belongs to.
	 * Limited to a maximum of 15 windows due to some graphics constraints
	 */
/*
	public boolean addTimeWindow(StsWin3dBase parent, StsViewTimeSeries window)
	{
		StsWindowFamily family = getWindowFamily(parent);
		family.addTimeSeriesWindow(window);
		return true;
	}
*/
	public StsWin3dFull getParentWindow(StsWin3dBase window)
	{
		return getWindowFamily(window).getParent();
	}

	/** Get the family that the supplied window is from */
	public StsWindowFamily getWindowFamily(StsWin3dBase window)
	{
		if(viewPersistManager == null || viewPersistManager.getFamilies() == null) return null;
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[i];
			StsWin3dBase[] windows = family.getWindows();
			if(windows == null) continue;
			for(int j = 0; j < windows.length; j++)
				if(windows[j] == window) return family;
		}
		return null;
	}

	public StsWin3dFull getWindowFamilyParent(StsWin3dBase window)
	{
		StsWindowFamily family = getWindowFamily(window);
		if(family == null) return null;
		return family.getParent();
	}
/*
	public int getFamilyIndex(StsViewTimeSeries window)
	{
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWindowFamily family = (StsWindowFamily) viewPersistManager.getFamilies()[i];
			for(int j = 0; j < family.timeWindows.length; j++)
			{
				if((StsViewTimeSeries) family.timeWindows[j] == window)
				{
					return i;
				}
			}
		}
		return -1;
	}
*/
	public int getFamilyIndex(StsWin3dBase window)
	{
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWindowFamily family = viewPersistManager.getFamilies()[i];
			StsWin3dBase[] windows = family.getWindows();
			for(int j = 0; j < windows.length; j++)
			{
				if(windows[j] == window)
				{
					return i;
				}
			}
		}
		return -1;
	}

	public StsWin3dBase[] getWindowsWithViewOfType(Class viewClass)
	{
		StsWin3dBase[] windowsWithView = new StsWin3dBase[0];
		StsWindowFamily[] windowFamilies = getWindowFamilies();
		for(int f = 0; f < windowFamilies.length; f++)
		{
			StsWindowFamily family = windowFamilies[f];
			StsWin3dBase[] windows = family.getWindows();
			for(int n = 0; n < windows.length; n++)
			{
				if(windows[n].hasView(viewClass))
					windowsWithView = (StsWin3dBase[]) StsMath.arrayAddElement(windowsWithView, windows[n]);
			}
		}
		return windowsWithView;
	}

	public StsView[] getViewsOfType(Class viewClass)
	{
		StsView[] viewsOfType = new StsView[0];
		Iterator<StsView> familyWindowViewIterator = getFamilyWindowViewIterator();
		while (familyWindowViewIterator.hasNext())
		{
			StsView view = familyWindowViewIterator.next();
			if(view.getClass() == viewClass)
				viewsOfType = (StsView[]) StsMath.arrayAddElement(viewsOfType, view);
		}
		return viewsOfType;
	}

	/** Get all the window families */
	public StsWindowFamily[] getWindowFamilies()
	{
		return viewPersistManager.getFamilies();
	}

	public Iterator<StsWindowFamily> getFamilyIterator()
	{
		return new FamilyIterator();
	}

	public class FamilyIterator implements Iterator<StsWindowFamily>
	{
		StsWindowFamily[] families;
		int nFamilies = 0;
		int n = 0;
		StsWindowFamily next = null;

		FamilyIterator()
		{
			families = viewPersistManager.getFamilies();
			nFamilies = families.length;
		}

		public boolean hasNext()
		{
			if(n < nFamilies)
			{
				next = families[n++];
				return true;
			}
			else
			{
				next = null;
				return false;
			}
		}

		public StsWindowFamily next()
		{
			return next;
		}

		public void remove()
		{
		}
	}

	public Iterator<StsView> getFamilyWindowViewIterator()
	{
		return new FamilyWindowViewIterator();
	}

	private class FamilyWindowViewIterator implements Iterator<StsView>
	{
		Iterator<StsWindowFamily> familyIterator;
		Iterator<StsView> windowViewIterator;

		public FamilyWindowViewIterator()
		{
		}

		public boolean hasNext()
		{
			if(familyIterator == null)
			{
				familyIterator = getFamilyIterator();
				if(!familyIterator.hasNext()) return false;
				StsWindowFamily family = familyIterator.next();
				windowViewIterator = family.getWindowViewIterator();
			}
			else
			{
				if(windowViewIterator.hasNext()) return true;
				if(!familyIterator.hasNext()) return false;
				StsWindowFamily family = familyIterator.next();
				windowViewIterator = family.getWindowViewIterator();
			}
			return windowViewIterator.hasNext();
		}

		public StsView next()
		{
			return windowViewIterator.next();
		}

		public void remove()
		{
		}
	}

	public Iterator<StsWin3dBase> getFamilyWindowIterator()
	{
		return new FamilyWindowIterator();
	}

	private class FamilyWindowIterator implements Iterator<StsWin3dBase>
	{
		Iterator<StsWindowFamily> familyIterator;
		Iterator<StsWin3dBase> windowIterator;

		public FamilyWindowIterator()
		{
		}

		public boolean hasNext()
		{
			if(familyIterator == null)
			{
				familyIterator = getFamilyIterator();
				if(!familyIterator.hasNext()) return false;
				StsWindowFamily family = familyIterator.next();
				windowIterator = family.getWindowIterator();
			}
			else
			{
				if(windowIterator.hasNext()) return true;
				if(!familyIterator.hasNext()) return false;
				StsWindowFamily family = familyIterator.next();
				windowIterator = family.getWindowIterator();
			}
			return windowIterator.hasNext();
		}

		public StsWin3dBase next()
		{
			return windowIterator.next();
		}

		public void remove()
		{
		}
	}

	/** Delete an auxiliary window from the default window family (StsWin3d) */
	public void deleteAuxWindow(StsWin3dBase auxWindow)
	{
		int familyIndex = this.getFamilyIndex(auxWindow);
		if(familyIndex == -1) return;
		viewPersistManager.getFamilies()[familyIndex].deleteAuxWindow(auxWindow, familyIndex);
	}

	/** Delete an auxiliary time series window from the default window family (StsWin3d) */
/*
	public void deleteAuxTimeWindow(StsViewTimeSeries auxWindow)
	{
		int familyIndex = this.getFamilyIndex(auxWindow);
		if(familyIndex == -1) return;
		viewPersistManager.getFamilies()[familyIndex].deleteAuxTimeWindow(auxWindow, familyIndex);
	}
*/
	/** Delete an auxiliary window from the family that argument parent belongs to.
	 **/
	/*
        public void deleteAuxWindow(StsWin3dBase parent, StsWin3dBase window)
        {
            StsWindowFamily family = getFamily(parent);
            family.deleteAuxWindow(window);
        }
    */

	/** Get the total number of windows */
	public int getNumberWindows()
	{
		int totalWindows = 0;
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			totalWindows += ((StsWindowFamily) viewPersistManager.getFamilies()[n]).getNumberWindows();
		}
		return totalWindows;
	}

	/** Enable this direction 3d cursor plane in all windows in all families */
	public void setCursor3dDisplayEnable(int currentDirection, boolean enable)
	{
		for(StsWindowFamily family : viewPersistManager.getFamilies())
			family.setSelectedDirection(currentDirection, enable);
	}

	/** Make this direction current for the 3d cursor in all windows in all families */
	public void setCursor3dCurrentDirNo(StsWin3dBase currentWindow, int currentDirection)
	{
		for(StsWindowFamily family : viewPersistManager.getFamilies())
			family.setCursor3dCurrentDirNo(currentWindow, currentDirection);
	}

	/**
	 * Returns the prefix of windows which are locked together with this one;
	 * not implemented yet.  Currently returns all windows.
	 */
	public StsWin3dBase[] getLockedWindows(StsWin3dBase currentWindow)
	{
		// todo: depending on whether windows are locked together, return the
		// prefix locked with this window
		StsWindowFamily family = getWindowFamily(currentWindow);
		return family.getWindows();
	}

	/** Iconifies or deiconifies a prefix of windows */
	public void iconifyWindowFamily(StsWin3dBase currentWindow, int state)
	{
		StsWin3dBase[] windows = getWindowFamily(currentWindow).getWindows();
		for(int i = 0; i < windows.length; i++)
		{
			windows[i].getFrame().setState(state);
		}
		/*
		StsViewTimeSeries[] tWindows = getWindowFamily(currentWindow).getTimeWindows();
		if(tWindows == null) return;
		for(int i = 0; i < tWindows.length; i++)
		{
			tWindows[i].getFrame().setState(state);
		}
		*/
		return;
	}

	/**
	 * get the GL state machine for the glPanel for the main window.
	 * This is often the most convenient way for an stsObject to access GL.
	 */
	public GL getWin3dGL()
	{
		if(getGlPanel3d() == null)
		{
			return null;
		}
		return getGlPanel3d().getGL();
	}

	public StsTransaction getCurrentTransaction()
	{
		return currentTransaction;
	}

	/** The state for each action is stored in the model.properties array.
	 *  This allows these states to be easily persisted in the database.
	 *  When the database is reloaded, these states can be correctly set so
	 *  workflow panel lights can be correctly visible.
	 */
	/*
    public void actionEnded(StsAction actionClass)
    {
     stateProperties.set(actionClass.getClass().getName(), ENDED);
    }
    */

	/**
	 * The state for each action is stored in the model.properties array.
	 * This allows these states to be easily persisted in the database.
	 * When the database is reloaded, these states can be correctly set so
	 * workflow panel lights can be correctly visible.
	 */
	/*
    public void actionStarted(StsAction actionClass)
    {
     stateProperties.set(actionClass.getClass().getName(), STARTED);
    }
    */
	public void setDatabase(StsDBFile db)
	{
		this.db = db;

	}

	public StsDBFile getDatabase()
	{
		return this.db;
	}

	public TreeSet<StsClassDisplayable> getTimeDisplayableClasses()
	{
		return displayableClasses;
	}

	public TreeSet<StsClassCursor3dTextureDisplayable> getCursor3dTextureDisplayableClasses()
	{
		return cursor3dTextureDisplayableClasses;
	}

	public StsClassCursor3dTextureDisplayable getCursor3dTextureDisplayableClass(StsObject object)
	{
		StsClass stsClass = getCreateStsClass(object);
		for(StsClassCursor3dTextureDisplayable displayableClass : cursor3dTextureDisplayableClasses)
		{
			if(displayableClass == stsClass)
				return displayableClass;
		}
		return null;
	}

	public boolean isStsClassCursor3dTextureDisplayable(Object object)
	{
		return isStsClassCursor3dTextureDisplayable(getStsClass(object));
	}

	public boolean isStsClassCursor3dTextureDisplayable(StsClass stsClass)
	{
		if(stsClass == null) return false;
		return StsClassCursor3dTextureDisplayable.class.isAssignableFrom(stsClass.getClass());
	}

	final public boolean isStsClassTextureDisplayable(Object object)
	{
		return isStsClassTextureDisplayable(getStsClass(object));
	}

	final public boolean isStsClassTextureDisplayable(StsClass stsClass)
	{
		if(stsClass == null) return false;
		return StsClassTextureDisplayable.class.isAssignableFrom(stsClass.getClass());
	}

	public StsClassTextureDisplayable getStsClassTextureDisplayable(Object object)
	{
		StsClass stsClass = getStsClass(object);
		if(!isStsClassTextureDisplayable(stsClass)) return null;
		return (StsClassTextureDisplayable) stsClass;
	}

	public boolean isClassTextureDisplayable(StsClass stsClass)
	{
		return StsClassTextureDisplayable.class.isAssignableFrom(stsClass.getClass());
	}

	public void deleteCursor3dTextures(StsObject object)
	{
		if(viewPersistManager == null || viewPersistManager.getFamilies() == null) return;
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
			viewPersistManager.getFamilies()[n].deleteCursor3dTextures(object);
	}

	public TreeSet<StsClassCursorDisplayable> getCursorDisplayableClasses()
	{
		return cursorDisplayableClasses;
	}

	public StsClassCursorDisplayable getCursorDisplayableClass(StsObject object)
	{
		StsClass stsClass = getCreateStsClass(object);
		for(StsClassCursorDisplayable displayableClass :cursorDisplayableClasses)
		{
				if(displayableClass == stsClass)
					return displayableClass;
		}
		return null;
	}

	/*
    public StsSurfaceDisplayable getCurrentSurfaceDisplayableObject()
    {
     int nSurfaceDisplayableClasses = surfaceDisplayableClasses.size();
     if(nSurfaceDisplayableClasses == 0) return null;
     for(int n = 0; n < nSurfaceDisplayableClasses; n++)
     {
      StsClass surfaceDisplayableClass = (StsClass)surfaceDisplayableClasses.get(n);
      StsObject currentObject = surfaceDisplayableClass.getCurrentObject();
      if(currentObject != null) return (StsSurfaceDisplayable)currentObject;
     }
     return null;
    }
    */
	public void addDisplayableInstance(StsInstance3dDisplayable instance3dDisplayable)
	{
		if(instance3dDisplayable == null)
		{
			return;
		}
		displayableInstances.add(instance3dDisplayable);
	}

	public void removeDisplayableInstance(StsInstance3dDisplayable instance3dDisplayable)
	{
		displayableInstances.remove(instance3dDisplayable);
	}

	/** basic classes have to be initialized for the project to be started */
	private void initializeClassLists()
	{
		try
		{
			ArrayList classes = new ArrayList();

			// colors and spectrums need to be initialized first
			//            addStsClass(StsColor.class, 100, 100);
			addStsClass(StsSpectrum.class, 2, 2);
			addStsClass(StsObjectRefList.class, 100, 100);
			addStsClass(StsProject.class, 1, 1);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.initializeClassLists() failed.", e, StsException.WARNING);
		}
	}

	/** each StsClass instance needs to be called to classInitialize anything required. */
	private void initializeClassFields()
	{
		for(StsClass stsClass : classList)
			stsClass.initializeFields();
		//		if(project != null)project.initializeFields();
	}

	private boolean projectInitializeClasses()
	{
		boolean initialized = true;
		for(StsClass stsClass : classList)
			if(!stsClass.projectInitialize(this)) initialized = false;
		return initialized;
	}

	/**
	 * Called after db has been loaded but before displays have been enabled
	 * to initialize class members which could not be initialized until all other classes were loaded.
	 * This is the case when the initialize(StsModel) method references a transient which is set by
	 * some other class.
	 * @return
	 */
	private boolean postDbLoadInitializeClasses()
	{
		boolean initialized = true;
		for(StsClass stsClass : classList)
			if(!stsClass.postDbLoadInitialize()) initialized = false;
		//        project.dbInitialize(this);
		//project.postDbLoadInitializeGeometry();
		return initialized;
	}

	/**
	 * Called after displays have been enabled for a final initialization for completed displays
	 * such as building toolbars which might be display dependent.
	 */
	private void finalInitializeClasses()
	{
		for(StsClass stsClass : classList)
			stsClass.finalInitialize();
	}

	public void initializePreferences()
	{
		StsToolkit.runWaitOnEventThread(new Runnable()
		{
			public void run()
			{
				doInitializePreferences();
			}
		});
	}

	private void doInitializePreferences()
	{
		checkLoadUserPreferences();
		if(preferences == null) return;
		for(StsClass stsClass : classList)
			initializeClassPreferences(stsClass);
		initializeFieldPreferences(getProject().getDisplayFields());
		initializeFieldPreferences(getProject().getDefaultFields());
	}

	private void initializeClassPreferences(StsClass stsClass)
	{
		initializeFieldPreferences(stsClass.getDisplayFields());
		initializeFieldPreferences(stsClass.getDefaultFields());
	}

	private void initializeProperties()
	{
		for(StsClass stsClass : classList)
		{
			initializeFieldProperties(stsClass.getDisplayFields());
			initializeFieldProperties(stsClass.getDefaultFields());
		}
		initializeFieldProperties(getProject().getDisplayFields());
		initializeFieldProperties(getProject().getDefaultFields());
	}

	/** Utility method used by StsClasses to classInitialize beans to preference values */
	public void initializeFieldPreferences(StsFieldBean[] fieldBeans)
	{
		if(fieldBeans == null || preferences == null) return;
		for(int n = 0; n < fieldBeans.length; n++)
		{
			StsFieldBean bean = fieldBeans[n];
			String beanKey = bean.getBeanKey();
			if(beanKey == null) continue;
			String defaultObjectString = preferences.getProperty(beanKey);
			if(defaultObjectString != null)
			{
				bean.setValueObjectFromString(defaultObjectString); // initializes bean to this preference
				//				setProperty(beanKey, defaultObjectString); // saves the preference in the database
			}
		}
	}

	private void updateProperties()
	{
		for(StsClass stsClass : classList)
		{
			updateFieldProperties(stsClass.getDisplayFields());
			updateFieldProperties(stsClass.getDefaultFields());
		}
		updateFieldProperties(getProject().getDisplayFields());
		updateFieldProperties(getProject().getDefaultFields());
	}

	/** Utility method used by StsClasses to classInitialize beans to preferences saved in db as properties */
	public void initializeFieldProperties(StsFieldBean[] fieldBeans)
	{
		if(fieldBeans == null || properties == null) return;
		for(int n = 0; n < fieldBeans.length; n++)
		{
			StsFieldBean bean = fieldBeans[n];
			String beanKey = bean.getBeanKey();
			if(beanKey == null) continue;
			String defaultObjectString = properties.getProperty(beanKey);
			if(defaultObjectString != null)
			{
				bean.setValueObjectFromString(defaultObjectString);
			}
		}
	}

	public void updateFieldProperties(StsFieldBean[] fieldBeans)
	{
		if(fieldBeans == null) return;
		for(int n = 0; n < fieldBeans.length; n++)
		{
			StsFieldBean bean = fieldBeans[n];
			String beanKey = bean.getBeanKey();
			if(beanKey == null) continue;
			String valueString = bean.toString();
			if(valueString != StsFieldBean.NONE_STRING)
				setProperty(beanKey, valueString);
		}
	}

	/**
	 * Check all StsClass instances to see if they implement one of the displayable interfaces.
	 * If so, add them to the appropriate list.
	 */
	private void initializeDisplayableInterfaceLists()
	{
		for(StsClass stsClass : classList)
			addToInterfaceLists(stsClass);
	}

	private StsClass addStsClass(Class instanceClass)
	{
		return addStsClass(instanceClass, initialSize, initialSizeInc);
	}
	/**
	 * Add this StsClass or subclass to the classes list.
	 * Check if the correctly-named subclass exists: "className+Class",
	 * for example, StsSurfaceClass for objects of class StsSurface.
	 * If so, create an instance of this subclass and add;
	 * if not, create an instance of StsClass and add.
	 */
	private StsClass addStsClass(Class instanceClass, int size, int inc)
	{
		StsClass stsClass = getStsClass(instanceClass);
		if(stsClass != null) return stsClass;
		String instanceClassname = instanceClass.getName();
		try
		{
			// try to load/construct "classname+Class" class
			// if it fails construct an instance of StsClass
			Class stsClassClass = Class.forName(instanceClassname + "Class");
			Constructor constructor = stsClassClass.getConstructor(new Class[0]);
			stsClass = (StsClass)constructor.newInstance(new Object[0]);
		}
		catch(Exception e) // failed to find subClass with name className+Class: make instance of StsClass below
		{
			stsClass = new StsClass();
		}
		finally
		{
			stsClass.setInstanceClass(instanceClass);
			checkAddToSubClasses(instanceClass, stsClass);
			stsClass.initializeListSize(size, inc);
			addStsClassToModel(instanceClass, stsClass, true);
			checkAddToObjectSelectableParent(stsClass);
			return stsClass;
		}
	}

	private void checkAddToObjectSelectableParent(StsClass stsClass)
	{
		Class instanceClass = stsClass.getStsClassObjectSelectableParent();
		if(instanceClass == null) return;
		getCreateStsClass(instanceClass);
	}

	private void checkAddToSubClasses(Class instanceClass, StsClass subClassStsClass)
	{
		try
		{
			Class parentInstanceClass = instanceClass.getSuperclass();
			StsClass parentStsClass = getStsClass(parentInstanceClass);
			if(parentStsClass == null) return;
			if(parentStsClass == null || !StsClass.class.isAssignableFrom(parentStsClass.getClass())) return;
			parentStsClass.addSubClass(subClassStsClass);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "checkAddToSubClasses", e);
		}
	}


	private void addStsClassToModel(Class key, StsClass stsClass, boolean refresh)
	{
		try
		{
			stsClass.projectInitialize(this);
			classList.add(stsClass);
			classes.put(key.getName(), stsClass);
			addToInterfaceLists(stsClass);
			stsClass.initializeFields();
			initializeClassPreferences(stsClass);
			if(!(stsClass instanceof StsObjectPanelClass)) return;
			if(objectTreePanel != null)
			{
				StsObjectPanelClass stsPanelClass = (StsObjectPanelClass)stsClass;
				StsTreeNode parentNode = stsPanelClass.getParentNode(objectTreePanel);
				StsTreeNode childNode = StsTreeNode.constructDynamicNode(stsPanelClass);
				objectTreePanel.addNodeToParent(childNode, parentNode);
			}
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.addStsClass() failed.", e, StsException.WARNING);
			//            StsException.outputException("StsModel.addStsClass(stsClass) failed.");
		}
	}
	/**
	 * Each StsClass instance which has a list of instances of a particular subclass
	 * of StsClass has a currentObject.  Set it here.
	 */
	public void setCurrentObject(StsObject object)
	{
		StsClass stsClass = getCreateStsClass(object);
		stsClass.setCurrentObject(object);
	}

	public void setCurrentObjectDisplayAndToolbar(StsObject object)
	{
		//		if(getCurrentObject(object.getClass()) == object) return;
		setCurrentObject(object);
		win3d.getCursor3d().setObject(object);
		StsComboBoxToolbar toolbar = (StsComboBoxToolbar) win3d.getToolbarNamed(StsComboBoxToolbar.NAME);
		toolbar.setSelectedObject(object);
	}

	/**
	 * Subsets of StsObjects which implement a particular interface are important
	 * to collect together.  Display routines need to call explicit classes to
	 * display in a particular situation, and simply need to call each of the classes
	 * implementing a particular interface.  @see #displayClasses
	 */
	private void addToInterfaceLists(StsClass stsClass)
	{
		Class c = stsClass.getClass();
		//if(StsSeismicClass.class.isAssignableFrom(c))
		//	volumeClasses.add((StsSeismicClass)stsClass);
		// class can be either timeDisplayable or generally displayable
		if(StsClassDisplayable.class.isAssignableFrom(c))
			displayableClasses.add((StsClassDisplayable)stsClass);

		// class be either unrotated, rotated, or neither
		if(StsUnrotatedClass.class.isAssignableFrom(c))
			unrotatedClasses.add((StsUnrotatedClass)stsClass);
		else if(StsRotatedClass.class.isAssignableFrom(c))
			rotatedClasses.add((StsRotatedClass)stsClass);

		// a class can have one or more of these interfaces
		if(StsClassTextureDisplayable.class.isAssignableFrom(c))
			textureDisplayableClasses.add((StsClassTextureDisplayable)stsClass);
		if(StsClassViewSelectable.class.isAssignableFrom(c))
			selectable3dClasses.add((StsClassViewSelectable)stsClass);
		if(StsClassSurfaceDisplayable.class.isAssignableFrom(c))
			surfaceDisplayableClasses.add((StsClassSurfaceDisplayable)stsClass);
		if(StsClassCursor3dTextureDisplayable.class.isAssignableFrom(c))
			cursor3dTextureDisplayableClasses.add((StsClassCursor3dTextureDisplayable)stsClass);
		if(StsClassCursorDisplayable.class.isAssignableFrom(c))
			cursorDisplayableClasses.add((StsClassCursorDisplayable)stsClass);
		if(StsClassTimeSeriesDisplayable.class.isAssignableFrom(c))
			timeSeriesDisplayableClasses.add((StsClassTimeSeriesDisplayable)stsClass);
		if(StsClassObjectSelectable.class.isAssignableFrom(c))
		{
			if(objectSelectableClasses.add(stsClass))
				comboBoxToolbarsAddSelectableClass(stsClass);
		}
	}

	private void comboBoxToolbarsAddSelectableClass(StsClass objectSelectableClass)
	{
		final StsClass objectSelectableClass_ = objectSelectableClass;
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				StsWindowFamily[] windowFamilies = getWindowFamilies();
				for(StsWindowFamily windowFamily : windowFamilies)
				{
					StsWin3dFull parentWindow = windowFamily.getParent();
					StsComboBoxToolbar comboBoxToolbar = (StsComboBoxToolbar)parentWindow.getToolbarNamed(StsComboBoxToolbar.NAME);
					if(comboBoxToolbar == null) continue;
					comboBoxToolbar.addObjectSelectableClass(objectSelectableClass_, parentWindow, StsModel.this);
				}
			}
		};
		StsToolkit.runLaterOnEventThread(runnable);
	}


	public void setCurrentObjectInPanels(StsObject stsObject)
	{
		if(objectTreePanel != null) objectTreePanel.selected(stsObject);
		comboBoxToolbarsSetObject(stsObject);
	}

	public void comboBoxToolbarsSetObject(StsObject stsObject)
	{
		StsWindowFamily[] windowFamilies = getWindowFamilies();
		for(StsWindowFamily windowFamily : windowFamilies)
		{
			StsWin3dFull parentWindow = windowFamily.getParent();
			StsComboBoxToolbar comboBoxToolbar = (StsComboBoxToolbar)parentWindow.getToolbarNamed(StsComboBoxToolbar.NAME);
			if(comboBoxToolbar == null) continue;
			comboBoxToolbar.setComboBoxItem(stsObject);
		}
	}

	/** This will create a Collection without any repeats */
	public TreeSet<Class> getDisplayableViewClasses()
	{
		TreeSet<Class> viewClasses = new TreeSet<Class>();
		for(StsClass stsClass : classList)
		{
			Class[] stsClassViews = stsClass.getViewClasses();
			for(Class stsClassView : stsClassViews)
				viewClasses.add(stsClassView);
		}
		return viewClasses;
	}

	private void clearInterfaceLists()
	{
		textureDisplayableClasses.clear();
		surfaceDisplayableClasses.clear();
		cursor3dTextureDisplayableClasses.clear();
		cursorDisplayableClasses.clear();
		selectable3dClasses.clear();
	}

	// These are convenience methods
	public StsSpectrumClass getSpectrumClass()
	{
		return (StsSpectrumClass) getCreateStsClass(StsSpectrum.class);
	}

	public StsSpectrum getSpectrum(String name)
	{
		StsSpectrum spectrum = getSpectrumClass().getSpectrum(name);
		if(spectrum == null)
		{
			StsException.systemError("StsModel.getSpectrum() failed for spectrum name: " + name);
		}
		return spectrum;
	}

	public StsColor getCurrentSpectrumColor(String name)
	{
		return getSpectrumClass().getCurrentSpectrumColor(name);
	}

	public StsColor incrementSpectrumColor(String name)
	{
		return getSpectrum(name).incrementCurrentColor();
	}

	public StsClass getCreateStsClass(Class instanceClass)
	{
		StsClass stsClass;
		stsClass = getStsClass(instanceClass);
		if(stsClass == null)
			stsClass = addStsClass(instanceClass);
		return stsClass;
	}

	public StsClass getCreateStsClass(StsObject object)
	{
		return getCreateStsClass(object.getClass());
	}

	public StsClass getStsClass(Class instanceClass)
	{
		return classes.get(instanceClass.getName());
	}

	public StsClass getStsClass(String instanceName)
	{
		return classes.get(instanceName);
	}

	final public StsClass getStsClass(Object object)
	{
		if(!(object instanceof StsObject)) return null;
		return getStsClass(object.getClass());
	}

	public boolean classHasObjects(Class instanceClass)
	{
		StsClass stsClass = getStsClass(instanceClass);
		if(stsClass == null) return false;
		return stsClass.hasObjects();
	}

	public StsObject getStsClassObjectWithIndex(Class c, int index)
	{
		try
		{
			return (StsObject) getCreateStsClass(c).getElementWithIndex(index);
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public StsObject getFirstStsClassObject(Class c)
	{
		try
		{
			return getCreateStsClass(c).getFirst();
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public int getStsClassSize(Class instanceClass)
	{
		StsClass stsClass = getStsClass(instanceClass);
		if(stsClass == null)
		{
			return 0;
		}
		return stsClass.getSize();
	}

	public StsObject getCurrentObject(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null)
			return null;
		return stsClass.getCurrentObject();
	}

	public StsObjectList getInstances(Class c)
	{
		return getCreateStsClass(c).getStsObjectList();
	}

	public String[] getInstanceNames(Class c)
	{
		StsObjectList list = getCreateStsClass(c).getStsObjectList();
		int nInstances = list.getSize();
		String[] names = new String[nInstances];
		for(int n = 0; n < nInstances; n++)
			names[n] = ((StsObject) list.getElement(n)).getName();
		return names;
	}

	// instanceList iterators with optional qualifying arguments (visibility and type)

	public StsObjectList.ObjectIterator getObjectIterator(Class c)
	{
		StsObjectList objectList = getCreateStsClass(c).getStsObjectList();
		return objectList.getObjectIterator();
	}

	public StsObjectList.ObjectIterator getVisibleObjectIterator(Class c)
	{
		StsObjectList objectList = getCreateStsClass(c).getStsObjectList();
		return objectList.getVisibleObjectIterator();
	}

	public StsObjectList.ObjectIterator getObjectOfTypeIterator(Class c, byte type)
	{
		StsObjectList objectList = getCreateStsClass(c).getStsObjectList();
		return objectList.getObjectOfTypeIterator(type);
	}

	public StsObjectList.ObjectIterator getVisibleObjectOfTypeIterator(Class c, byte type)
	{
		StsObjectList objectList = getCreateStsClass(c).getStsObjectList();
		return objectList.getVisibleObjectOfTypeIterator(type);
	}

	// returns array of StsObject
	public StsObject[] getObjectList(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null || stsClass.getSize() == 0)
			return new StsObject[0];
		else
			return stsClass.getElements();
	}

	public StsObject[] getObjectList(String instanceName)
	{
		StsClass stsClass = getStsClass(instanceName);
		if(stsClass == null || stsClass.getSize() == 0)
			return new StsObject[0];
		else
			return stsClass.getElements();
	}

	public StsObject[] getChildObjectList(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null || stsClass.getSize() == 0)
			return new StsObject[0];
		StsObjectList objectList = new StsObjectList();
		objectList.add(stsClass.getElements());
		ArrayList<StsClass> subClasses = stsClass.getSubClasses();
		for(StsClass subClass : subClasses)
			objectList.add(subClass.getElements());
		return objectList.getElements();
	}

	public Object[] getTrimmedList(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null || stsClass.getSize() == 0)
			return new Object[0];
		else
			return stsClass.getTrimmedList();
	}

	// returns array of actual class (i.e., StsEdge[] )
	public Object getCastObjectList(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null)
			return Array.newInstance(c, 0);
		else
			return stsClass.getCastObjectList();
	}

	public StsMainObject[] getVisibleObjectList(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		return getVisibleObjectList(stsClass);
	}

	public StsMainObject[] getVisibleObjectList(StsClass stsClass)
	{
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getVisibleObjectList();
	}

	public StsMainObject[] getObjectListOfType(Class c, byte type)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getObjectListOfType(type);
	}

	public Object[] getObjectListOrderedByName(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getObjectListOrderedByName();
	}

	public StsMainObject[] getVisibleObjectListOfType(Class c, byte type)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getVisibleObjectListOfType(type);
	}

	public StsObject getObjectWithName(Class c, String name)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getObjectWithName(name);
	}

	public StsObject getObjectWithName(String classname, String name)
	{
		StsClass stsClass = getStsClass(classname);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getObjectWithName(name);
	}

	public StsMainObject getObjectOfType(Class c, byte type, int index)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getObjectOfType(type, index);
	}

	public StsMainObject getFirstObjectOfType(Class c, byte type)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return null;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.getFirstObjectOfType(type);
	}

	public boolean hasObjectsOfType(Class c, byte type)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return false;
		StsObjectList objectList = stsClass.getStsObjectList();
		return objectList.hasObjectsOfType(type);
	}

	public boolean hasClassObjects(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return false;
		return stsClass.hasObjects();
	}

	public boolean hasClassObjects(String instanceName)
	{
		StsClass stsClass = getStsClass(instanceName);
		if(stsClass == null) return false;
		return stsClass.hasObjects();
	}

	public int getNObjects(Class c)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null)
			return 0;
		return stsClass.getSize();
	}

	public int getNObjects(String instanceName)
	{
		StsClass stsClass = getStsClass(instanceName);
		if(stsClass == null)
			return 0;
		return stsClass.getSize();
	}

	public int getNObjectsOfType(Class c, byte type)
	{
		StsObjectList objectList = getCreateStsClass(c).getStsObjectList();
		return objectList.getNObjectsOfType(type);
	}

	public boolean deleteObjectsOfType(Class c, byte type)
	{
		StsClass stsClass = getStsClass(c);
		if(stsClass == null) return false;
		StsMainObject[] objects = stsClass.getObjectListOfType(type);
		if(objects == null) return false;
		int nObjects = objects.length;
		for(int n = nObjects - 1; n >= 0; n--)
		{
			StsObject stsObject = objects[n];
			stsObject.delete();
		}
		return true;
	}

	// Suggested improvement: for each displayableClasses list, have a corresponding list of activeDisplayableClasses
	// which are classes which have active instances.  So here, if this is the first obj added to the class, add the
	// corresponding displayableClass to the activeDisplayableClasses.  Conversely on delete, if this is last instance
	// of class, remove from the activeDisplayableClasses list.  So in drawing routines which ask for a displayableClasses
	// list, give them the activeDisplayableClasses to reduce the number of classes they need to cycle thru.
	public boolean add(StsObject obj)
	{
		try
		{
			StsClass stsClass = getCreateStsClass(obj.getClass());
			if(stsClass == null) return false;
			stsClass.add(obj);
			// objectTreePanel.refreshTree();
			// checkRefreshObjectPanel(stsClass);
			return true;
		}
		catch(Exception e)
		{
		}
		return false;
	}

	public StsObject getEmptyStsClassObject(Class c, int index)
	{
		StsClass stsClass = getCreateStsClass(c);
		if(stsClass == null)
		{
			return null;
		}
		return stsClass.getEmptyStsClassObject(index);
	}

	public void addStsObjectToObjectPanel(StsTreeNode parentNode, StsObject stsObject)
	{
		StsTreeNode childNode = StsTreeNode.constructDynamicNode((StsTreeObjectI) stsObject);
		objectTreePanel.addNodeToParent(childNode, parentNode);
	}

	public boolean deleteStsClassObject(Class c, int index)
	{
		try
		{
			StsClass stsClass = getCreateStsClass(c);
			if(stsClass == null)
			{
				return false;
			}
			//            StsObject object = stsClass.getElement(index);
			//            return object.delete();
			return stsClass.deleteObjectWithIndex(index);
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.deleteStsClassObject() failed.", e, StsException.WARNING);
			return false;
		}
	}

	public boolean deleteStsClass(Class c)
	{
		try
		{
			StsClass stsClass = getStsClass(c);
			if(stsClass == null)
			{
				return false;
			}
			stsClass.deleteAll();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.deleteStsClass() failed.", e, StsException.WARNING);
			return false;
		}
	}

	public boolean deleteNonPersistentObjects(Class c)
	{
		try
		{
			StsClass stsClass = getStsClass(c);
			if(stsClass == null) return false;
			stsClass.deleteNonPersistentObjects();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputException("StsModel.deleteStsClass() failed.", e, StsException.WARNING);
			return false;
		}
	}

	public boolean delete(StsObject obj)
	{
		try
		{
			StsClass stsClass = getCreateStsClass(obj.getClass());
			stsClass.delete(obj);
			refreshObjectPanel(obj);
			win3d.win3dDisplay();
			return true;
		}
		catch(Exception e)
		{
		}
		return false;
	}

	public boolean update(StsObject obj)
	{
		try
		{
			StsClass stsClass = getCreateStsClass(obj.getClass());
			//    	    stsClass.update(obj);
			return true;
		}
		catch(Exception e)
		{
		}
		return false;
	}

	/**
	 * A list of classNames (Strings) which have been initialized so far.
	 * Used locally by only the two classInitialize methods below.
	 */
	transient Vector initClasses;

	/* classInitialize all non-persistent data */
	private void initialize(Class c)
	{
		if(initClasses.contains(c))
		{
			return;
		}

		initClasses.addElement(c);

		try
		{
			StsClass stsClass = getCreateStsClass(c);
			for(int i = 0; i < stsClass.getSize(); i++)
			{
				StsObject obj = stsClass.getElement(i);
				obj.initialize(this);
			}
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "StsModel.initialize(c)", e);
		}
	}

	public boolean stsClassHasThisFieldObject(Class c, String fieldname, Object fieldObject)
	{
		StsObject object = getStsObjectWithThisFieldObject(c, fieldname, fieldObject);
		return object != null;
	}

	public StsObject getStsObjectWithThisFieldObject(Class c, String fieldname, Object fieldObject)
	{
		Field field = StsToolkit.getField(c, fieldname);
		if(field == null) return null;
		StsObject[] classObjects = getObjectList(c);
		for(StsObject stsObject : classObjects)
		{
			try
			{
				if(field.get(stsObject) == fieldObject) return stsObject;
			}
			catch(Exception e)
			{
				StsException.outputWarningException(this, "stsClassHasThisFieldObject", e);
				return null;
			}
		}
		return null;
	}

	private void classInitialized(Class c)
	{
		initClasses.addElement(c);
	}

	private void initializeWells()
	{
		forEachInstance(StsWell.class, "classInitialize", null);
		classInitialized(StsWell.class);
	}

	/** convenience accessors */

	public final StsProject getProject()
	{
		if(project == null)
		{
			StsException.systemError(this, "getProject", "Database error: No project in model.");
			project = (StsProject) getFirstStsClassObject(StsProject.class);
		}
		return project;
	}

	public final byte getProjectZDomain()
	{
		return getProject().getZDomain();
	}

	public double getXOrigin()
	{
		return getProject().getXOrigin();
	}

	public double getYOrigin()
	{
		return getProject().getYOrigin();
	}

	public StsProperties getProperties()
	{
		return properties;
	}

	public void setProperties(StsProperties properties)
	{
		this.properties = properties;
	}

	public void setProperty(String name, boolean value)
	{
		setProperty(name, Boolean.toString(value));
	}

	public void setProperty(String name, Boolean value)
	{
		setProperty(name, value.booleanValue());
	}

	public void setProperty(String name, String value)
	{
		if(properties == null) initProperties();
		if(mainWindowActionManager == null) return;
		properties.set(name, value);
		//		StsDBMethodCmd cmd = new StsDBMethodCmd(this, "loadProperty", new Object[] {name, value});
		//		actionManager.getCreateTransactionAddCmd("saveProperty", cmd, this);
		if(debug) System.out.println("Saved property " + name + " = " + value);
	}

	public void loadProperty(String name, String value)
	{
		if(debug) System.out.println("Loaded property " + name + " = " + value);
		setProperty(name, value);
	}

	public void setProperty(String name, int value)
	{
		setProperty(name, Integer.toString(value));
	}

	public void setProperty(String name, byte value)
	{
		setProperty(name, Byte.toString(value));
	}

	public void setProperty(String name, Integer value)
	{
		setProperty(name, value.toString());
	}

	public boolean getBooleanProperty(String name)
	{
		if(properties == null) return false;
		return properties.getBoolean(name);
	}

	public int getIntProperty(String name)
	{
		if(properties == null) return 0;
		return properties.getInteger(name);
	}

	public byte getByteProperty(String name)
	{
		if(properties == null) return 0;
		return properties.getByte(name);
	}

	public String getStringProperty(String name)
	{
		if(properties == null) return "null";
		return properties.getProperty(name);
	}

	public boolean hasProperty(String name)
	{
		if(properties == null) return false;
		return properties.hasProperty(name);
	}

	public StsGridDefinition getGridDefinition()
	{
		return getProject().getGridDefinition();
	}

	public void setName(String name)
	{
		this.name = name;
		this.viewPersistManager.setModelAs(this);
		propertiesPersistManager.setModelAs(this);
	}

	public String getName()
	{
		if(getProject() != null)
		{
			return getProject().getName();
		}
		if(name != null)
		{
			return name;
		}

		StsException.systemError("StsModel.getName() failed: project is null");
		return null;
	}

	/**
	 * A persistent storage area for integers and booleans used to define
	 * properties associated with the model
	 */
	private void initProperties()
	{
		if(properties != null)
		{
			return;
		}
		properties = new StsProperties(20);
		//        properties.set("Use Display Lists", true);
		//        properties.set("isGridCoordinates", false);
		if(db != null) db.saveTransactions();
		//		properties.set("writeTransaction", true);
	}

	/** A persistent storage area for actionClassName-actionStatus pairs */
	private void initStateProperties()
	{
		//stateProperties = new StsProperties(20);
	}

	/** toggles this property and redisplays all 3d windows */
	public void togglePropertyDisplayAll(ItemEvent e, String name)
	{
		boolean state = (e.getStateChange() == ItemEvent.SELECTED);
		setProperty(name, state);
		win3dDisplayAll();
	}

	/** toggles this property */
	public void toggleProperty(ItemEvent e, String name)
	{
		try
		{
			boolean state = (e.getStateChange() == ItemEvent.SELECTED);
			setProperty(name, state);
		}
		catch(Exception ex)
		{
			StsException.outputException("StsModel.toggleProperty() failed.",
					ex, StsException.WARNING);
		}
	}

	public void toggleUseDisplayLists(ItemEvent e, String name)
	{
		boolean state = (e.getStateChange() == ItemEvent.SELECTED);
		if(state == useDisplayLists) return;
		useDisplayLists = state;
		win3dDisplayAll();
	}

	/** get the state of this action or return 0 (CANNOT_START) if not found */
	public int getActionStatus(String actionClassName)
	{
		if(actionStatusProperties == null)
		{
			return CANNOT_START;
		}
		return actionStatusProperties.getInteger(actionClassName);
	}

	/** set the state of this action */
	public void setActionStatus(String actionClassName, int actionStatus)
	{
		if(actionClassName == null)
		{
			return;
		}
		setActionStatusProperty(actionClassName, actionStatus);
		if(win3d == null)
		{
			return;
		}
		StsWorkflowPanel workflowPanel = win3d.getWorkflowPanel();
		if(workflowPanel != null)
		{
			workflowPanel.adjustWorkflowPanelState(actionClassName, actionStatus);

		}
	/*
		StsCollaboration collaboration = StsCollaboration.getCollaboration();
		if(collaboration != null && collaboration.hasPeers())
		{
			createTransientActionStatusTransaction(actionClassName, actionStatus);
		}
	*/
	}

	/**
	 * Called when actionStatusTransaction executed for this peer.  setActionStatusProperty has
	 * already been sent in a previous transaction, so simply complete the actionStatus operation.
	 */
	public void setActionStatus(String actionClassName, Integer actionStatus)
	{
		if(actionClassName == null)
		{
			return;
		}
		if(win3d == null)
		{
			return;
		}
		StsWorkflowPanel workflowPanel = win3d.getWorkflowPanel();
		if(workflowPanel != null)
		{
			workflowPanel.adjustWorkflowPanelState(actionClassName, actionStatus.intValue());
		}
	}

	private void createTransientActionStatusTransaction(String actionClassName, int actionStatus)
	{
		StsDBMethodCmd cmd = new StsDBMethodCmd(this, "setActionStatus", new Object[]
				{actionClassName, new Integer(actionStatus)});
		addTransientTransactionCmd("setActionStatus", cmd);
	}

	/** classInitialize the states of all actions on the workflow panel */
	public void initializeActionStatus()
	{
		StsWorkflowPanel workflowPanel = win3d.getWorkflowPanel();
		workflowPanel.initializeActionStatus();
	}

	/** set this integer property */
	public void setActionStatusProperty(String name, Integer value)
	{
		setActionStatusProperty(name, value.intValue());
	}

	/** set this integer state property */
	public void setActionStatusProperty(String name, int value)
	{
		int oldValue = actionStatusProperties.getInteger(name);
		if(oldValue == value) return;
		actionStatusProperties.set(name, value);

		if(mainWindowActionManager == null) return;
		StsDBMethodCmd cmd = new StsDBMethodCmd(this, "setActionStatusProperty", new Object[]
				{name, new Integer(value)});
		getCreateTransactionAddCmd("setActionStatusProperty", cmd);
	}

	/** set this boolean state property */
	public void setActionStatusProperty(String name, boolean value)
	{
		actionStatusProperties.set(name, Boolean.toString(value));
		if(mainWindowActionManager == null)
		{
			return;
		}
		StsDBMethodCmd cmd = new StsDBMethodCmd(this, "setActionStatusProperty", new Object[]
				{name, new Boolean(value)});
		getCreateTransactionAddCmd("setActionStatusProperty", cmd);
	}

	/** get this boolean property */
	public boolean getBooleanActionStatusProperty(String name)
	{
		return actionStatusProperties.getBoolean(name);
	}

	/** get this integer property */
	public int getIntActionStatusProperty(String name)
	{
		return actionStatusProperties.getInteger(name);
	}


	public void enableDisplay()
	{
		if(displayOK) return;
		if(win3d != null && win3d.cursor3dPanel != null)
		{
			win3d.cursor3dPanel.setEditable(true);
			displayOK = true;
		}
	}

	/**
	 * Disable the display; typically turned off during model modification.
	 * Turn the cursors off since they may be updated.  Wait until the
	 * redisplay is complete at which point the displayState is changed.
	 */
	public void disableDisplay()
	{
		//        win3dDisplay(); // would be better to use win3dDisplayAndWait() but it currently locks so need to mainDebug
		if(!displayOK) return;
		if(win3d != null && win3d.cursor3dPanel != null)
		{
			win3d.cursor3dPanel.setEditable(false);
			displayOK = false;
		}
		if(debug || Main.isGLDebug) StsException.systemDebug(this, "disableDisplay", "draw disabled");
	}

	/**
	 * Redraw the 3d display, but wait until its finished before proceeding.
	 * When glPanel3d.display() finishes, it will call clearWin3dDisplayWait()
	 * to release the lock.
	 * Not currently used.  To use, uncomment StsGLPanel3d.display().clearDisplayAndWait.
	 */

	public void win3dDisplayAndWait()
	{
		try
		{
			synchronized(displayLock)
			{
				if(Main.isGLDebug) System.out.println("model.win3dDisplayAndWait: waiting");
				displayWait = true;
				win3dDisplay();
				while (this.displayWait == true)
					displayLock.wait();
			}
		}
		catch(InterruptedException ie)
		{
		}
	}

	public void clearWin3dDisplayWait()
	{
		synchronized(displayLock)
		{
			if(Main.isGLDebug) System.out.println("model.clearWin3dDisplayWait: cleared");
			displayWait = false;
			displayLock.notify();
		}
	}

	/** the central model display method; called only by StsView3d to display 3d model. */
	public void display(StsGLPanel3d glPanel3d) throws StsException
	{
		boolean displayOK = this.displayOK && glPanel3d != null;
		if(debug || Main.isDbDebug || Main.isGLDebug)
		{
			StsException.systemDebug(this, "display", "called");
			glPanel3d.debugPrintMatrixMode("StsModel.display()");
		}
		if(!displayOK) return;
		getProject().display(glPanel3d);
		displayClasses(glPanel3d);
		displayInstances(glPanel3d);
		// displayTestObjects(glPanel3d);
	}

	private void displayTestObjects(StsGLPanel3d glPanel3d)
	{
		StsBeachballShader beachballShader = StsBeachballShader.getShader(glPanel3d);
		beachballShader.display(glPanel3d, new float[]{0.0f, -50.0f, 0.0f}, new float[]{0.0f, -45.0f, -45.0f});
		beachballShader.display(glPanel3d, new float[]{0.0f, 0.0f, 0.0f}, new float[]{0.0f, 0.0f, 0.0f});
		beachballShader.display(glPanel3d, new float[]{0.0f, 50.0f, 0.0f}, new float[]{0.0f, 45.0f, 45.0f});
		StsShader.disableCurrentShader(glPanel3d.getGL());
	}

	/** clear all 3d displays and 3d cursor textures */
	/*
    public void clearTextureDisplays()
    {
        for(int n = 0; n < viewPersistManager.families.length; n++)
        {
            ((StsWindowFamily)viewPersistManager.families[n]).clearTextureDisplays();
        }
    }
    */

	/** clear all 3d displays and 3d cursor textures */
	public void deleteClassTextureDisplays(Class displayableClass)
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			viewPersistManager.getFamilies()[n].deleteClassTextureDisplays(displayableClass);
		}
	}

	public void clearDisplayTextured3dCursors(Object object)
	{
		if(viewPersistManager.getFamilies() == null) return;

		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWin3dBase[] windows = getWindows(i);
			for(int w = 0; w < windows.length; w++)
			{
				StsWin3dBase auxWindow = windows[w];
				StsCursor3d cursor3d = auxWindow.getCursor3d();
				cursor3d.clearTextureDisplays(object);
				//		if(cursor3d.clearTextureDisplays(object))auxWindow.win3dDisplay();
			}
		}
	}

	public void clearTextureClassDisplays(Class objectClass)
	{
		if(viewPersistManager.getFamilies() == null) return;

		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWin3dBase[] windows = getWindows(i);
			for(int w = 0; w < windows.length; w++)
			{
				StsWin3dBase auxWindow = windows[w];
				StsCursor3d cursor3d = auxWindow.getCursor3d();
				cursor3d.clearTextureClassDisplays(objectClass);
				//		if(cursor3d.clearTextureDisplays(object))auxWindow.win3dDisplay();
			}
		}
	}

	public void checkAddToCursor3d(StsObject object)
	{
		if(viewPersistManager.getFamilies() == null) return;
		for(int i = 0; i < viewPersistManager.getFamilies().length; i++)
		{
			StsWin3dBase[] windows = getWindows(i);
			for(int w = 0; w < windows.length; w++)
			{
				StsWin3dBase win3d = windows[w];
				StsCursor3d cursor3d = win3d.getCursor3d();
				if(cursor3d != null) cursor3d.initializeCursorSections();
			}
		}
	}

	public void cropChanged()
	{
		for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
		{
			((StsWindowFamily) viewPersistManager.getFamilies()[n]).cropChanged();
		}
	}

	public void addTransientTransactionCmd(String transactionName, StsDBCommand cmd)
	{
		db.peerSendTransactions();
		addTransactionCmd("changeView", cmd);
		db.saveTransactions();
	}

	/**
	 * Add this command to the current transaction if it exists; if not,
	 * create a new transaction with this name, add the command to it and commit.
	 */
	public void addTransactionCmd(String transactionName, StsDBCommand cmd)
	{
		//		if(actionManager == null) return;
		getCreateTransactionAddCmd(transactionName, cmd);
	}

	/** an instance of an StsObject has changed; create command and add to transaction */
	public void instanceChange(StsObject obj, String cmdName)
	{
		StsClass stsClass = getCreateStsClass(obj.getClass());
		StsDBCommand cmd = stsClass.createInstanceChgCmd(obj);
		addTransactionCmd(cmdName, cmd);
	}

	public void addMethodCmd(StsObject obj, String methodName, Object[] args, String cmdName)
	{
		StsDBMethodCmd cmd = new StsDBMethodCmd(obj, methodName, args);
		this.addTransactionCmd(cmdName, cmd);
	}

	public void addMethodCmd(StsObject obj, String methodName, Object[] args)
	{
		addMethodCmd(obj, methodName, args, methodName);
	}

	public void addMethodCmd(StsObject obj, String methodName)
	{
		addMethodCmd(obj, methodName, new Object[0], methodName);
	}

	private void displayClasses(StsGLPanel3d glPanel3d)
	{
		try
		{
			displayClasses(displayableClasses, glPanel3d);
			displayTextureClasses(textureDisplayableClasses, glPanel3d);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "displayClasses", e);
		}
	}

	/** display all instances of these classes */
	private void displayClasses(TreeSet<StsClassDisplayable> displayableClasses, StsGLPanel3d glPanel3d)
	{
		if(displayableClasses == null) return;
		long time = getProject().getProjectTime();
		try
		{
			for(StsClassDisplayable displayableClass : displayableClasses)
				displayableClass.displayClass(glPanel3d, time);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "displayClasses", e);
		}
	}

	private void displayTextureClasses(TreeSet<StsClassTextureDisplayable> displayableClasses, StsGLPanel3d glPanel3d)
	{
		if(displayableClasses == null) return;
		long time = getProject().getProjectTime();
		try
		{
			for(StsClassTextureDisplayable displayableClass : displayableClasses)
				displayableClass.displayClassTexture(glPanel3d, time);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "displayClasses", e);
		}
	}
	/**
	 * display these individual instances; typically used during an action
	 * when a transient or special purpose class is used for display.
	 */
	private void displayInstances(StsGLPanel3d glPanel3d)
	{
		if(displayableInstances == null)
		{
			return;
		}

		Iterator iter = displayableInstances.iterator();
		while (iter.hasNext())
		{
			StsInstance3dDisplayable instance3dDisplayable = (StsInstance3dDisplayable) iter.next();
			instance3dDisplayable.display(glPanel3d);
		}
	}

	/**
	 * General routine which calls the method on all instances of Class c,
	 * passing in a list of args
	 */
	public void forEachInstance(Class c, String methodName, Object[] args)
	{
		StsMethod method = new StsMethod(c, methodName, args);
		Method m = method.getMethod();

		StsClass stsClass = getCreateStsClass(c);

		for(int n = 0; n < stsClass.getSize(); n++)
		{
			try
			{
				Object instance = (Object) stsClass.getElement(n);
				m.invoke(instance, args);
			}
			catch(Exception e)
			{
				StsException.outputWarningException(this, "forEachInstance", "Method: " + methodName + " failed " + "in class: " + StsToolkit.getSimpleClassname(c), e);
			}
		}
	}

	/** If a single argument, make an array of length 1 for compatibility */
	public void forEachInstance(Class c, String methodName, Object arg)
	{
		forEachInstance(c, methodName, new Object[]
				{arg});
	}

	/** Method for default object popups editing */
	/*
        public StsObject objectPopup(StsMouse mouse)
        {
            StsSection section;
            try
            {
                StsAncillaryData aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataDocument.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                   aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataImage.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                    aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataMultiMedia.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                    aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataOther.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData != null)
                {
                    aData.showPopup(mouse);
                    return aData;
                }
            }
            catch(Exception e)
            {
                StsException.outputException("Exception in StsModel.defaultPopup()", e, StsException.WARNING);
            }
            return null;
        }
    */

	/** Method for default model editing */
	/*
        public StsObject mouseSelectedEdit(StsMouse mouse)
        {
            StsSection section;
            try
            {
                StsWell well = (StsWell)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsWell.class), StsJOGLPick.PICKSIZE_MEDIUM, StsJOGLPick.PICK_CLOSEST);
                if(well != null)
                {
                    well.logMessage();
                    well.openOrPopWindow();
                    return well;
                }

                StsFaultLine fault = (StsFaultLine)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsFaultLine.class), StsJOGLPick.PICKSIZE_MEDIUM, StsJOGLPick.PICK_CLOSEST);
                if(fault != null)
                {
                    fault.logMessage();
                    return fault;
                }

                StsSurfaceEdge surfaceEdge = (StsSurfaceEdge)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsSurfaceEdge.class), StsJOGLPick.PICKSIZE_MEDIUM,
                    StsJOGLPick.PICK_CLOSEST);
                if(surfaceEdge != null)
                {
                    surfaceEdge.setCurrentEdge();
                    surfaceEdge.logMessage();
    //                section = surfaceEdge.getSection();
    //                if(section != null) section.printToStatusArea();
                    win3dDisplayAll();
                    return surfaceEdge;
                }

                StsSectionEdge sectionEdge = (StsSectionEdge)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsSectionEdge.class), StsJOGLPick.PICKSIZE_MEDIUM,
                    StsJOGLPick.PICK_CLOSEST);
                if(sectionEdge != null)
                {
                    sectionEdge.setCurrentEdge();
                    section = sectionEdge.getSection();

                    if(section != null)
                    {
                        section.logMessage();
                    }
                    else
                    {
                        sectionEdge.logMessage();

                    }
                    win3dDisplayAll();
                    return sectionEdge;
                }

                int pointIdx = 0;
                StsPointList pointSet = (StsPointList)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsPointList.class), StsJOGLPick.PICKSIZE_MEDIUM,
                    StsJOGLPick.PICK_CLOSEST);
                if(pointSet != null)
                {
                    StsPickItem items = StsJOGLPick.pickItems[0];
                    pointIdx = items.names[1];
                    pointSet.logMessage(pointIdx);
                    return pointSet;
                }

                StsAncillaryData aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataDocument.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                   aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataImage.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                    aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataMultiMedia.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData == null)
                    aData = (StsAncillaryData)StsJOGLPick.pickClass3d(glPanel3d, getVisibleObjectList(StsAncillaryDataOther.class), StsJOGLPick.PICKSIZE_MEDIUM,StsJOGLPick.PICK_CLOSEST);
                if(aData != null)
                {
                    aData.reportMessage();
                    return aData;
                }
            }
            catch(Exception e)
            {
                StsException.outputException("Exception in StsModel.mouseSelectedEdit()", e, StsException.WARNING);
            }
            return null;
        }
    */
	public static void main(String[] args)
	{
		try
		{
			// get the operating system name
			String OS = System.getProperty("os.name");
			System.out.println("OS: " + OS);

			// set the look and feel
			if(OS.equals("Windows NT"))
			{
				UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
			}
			else if(OS.equals("Solaris"))
			{
				UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());
			}
			else if(OS.equals("Irix"))
			{
				//            	UIManager.setLookAndFeel(new com.sun.java.swing.plaf.metal.MetalLookAndFeel());
				UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());
			}

			StsModel model = new StsModel();
			model.win3d.setTitle("StsModel test main");

			// Test procedure below: normally commented out
			/*
             StsModel model = new StsModel();
             String path = "../data/texas_seg/";
             model.readProjectParameters(path + "proj.texasSmall");
             model.win3dDisplay();
             // read surface file(s)
             float nullZValue = model.project.getMapGenericNull();
             StsModelSurface.createSurface(path, "map.generic.top_10000sand", StsColor.BLUE, nullZValue);
             model.win3dDisplay();
             StsWell.getWellLine(model, path, "well.dev.w109");
             StsWell.getWellLine(model, path, "well.dev.w17");
            */
		}
		catch(Exception e)
		{
			System.out.println("Exception in StsModel.main()\n" + e);
		}
	}
/*
	public boolean isModelBuilt()
	{
		return getActionStatus(StsBuiltModel.class.getName()) == ENDED;
	}
*/
	/** Given these view parameters, change the view. */
	public void changeModelView3d(float[] parameters)
	{
		if(win3d == null)
		{
			return;
		}

		if(minElapsedTime == 0L)
		{
			if(!win3d.constructViewPanel(StsView3d.shortViewName3d, StsView3d.class)) return;
			StsView3d view3d = (StsView3d) win3d.getView(StsView3d.class);
			view3d.changeModelView3d(parameters); // unconditionally cause the change.
			win3d.win3dDisplay();
		}
		else
		{
			if(timer == null)
			{
				timer = new StsTimer();
			}
			else
			{
				double elapsedTime = timer.stop();
				if(elapsedTime < minElapsedTime)
				{
					return;
				}
			}
			if(!win3d.constructViewPanel(StsView3d.shortViewName3d, StsView3d.class)) return;
			StsView3d view3d = (StsView3d) win3d.getView(StsView3d.class);
			view3d.glPanel3d.changeModelView3d(parameters); // unconditionally cause the change.
			timer.restartElapsedTime();
		}
	}

	/** setBounds the view port using these parameters */
	public void reshape3d(int[] parameters)
	{
		if(win3d == null) return;
		//        StsView3d view3d = (StsView3d)win3d.getSinglePanelView(StsView3d.class, StsView3d.viewClassname3d);
		//        if(view3d == null) return;
		win3d.getGlPanel3d().reshape3d(parameters);
	}

	/** check if we can end actions; return false if we can't */
	public boolean endAllAction()
	{
		return mainWindowActionManager.endAllAction();
	}

	/** exit the model: close the database */
	public void closeDatabase()
	{
		if(db != null)
		{
			db.close();
			db = null;
		}
		//Main.endUsage();
	}

	/** Close a family of windows */
	public void closeFamily(StsWin3dBase window)
	{
		StsWindowFamily family = getWindowFamily(window);
		family.close(getRootGL());
		viewPersistManager.deleteFamily(family);
		renumberFamilies();
	}

	/**
	 * Close this window.  If it is parent of family,
	 * close family and remove family.  Othewise,
	 * just remove this window from family.
	 */

	public void closeWindows(StsWin3dBase window)
	{
		int familyIndex = getFamilyIndex(window);
		if(familyIndex == -1) return;

		StsWindowFamily family = viewPersistManager.getFamilies()[familyIndex];
		if(window == family.getParent())
		{
			family.close(getRootGL());
			viewPersistManager.deleteFamily(family);
			renumberFamilies();
		}
		else
		{
			family.deleteAuxWindow(window, familyIndex);
			window.dispose();
		}
		closeWellWindows();
	}

	private void closeWellWindows()
	{
		StsWellClass wc = (StsWellClass) getStsClass(StsWell.class);
		if(wc != null) wc.closeWindows();
	}

	public void appClose()
	{
		endAllAction();
		close();
		System.exit(0);
	}

	public void loadArchive(StsActionManager actionManager)
	{
		StsModel oldModel = this;
		if(oldModel != null)
		{

			actionManager.endCurrentAction();
			oldModel.stopTime();
			oldModel.close();
			String message = "Closed old model : " + oldModel.getName();
			StsMessageFiles.logMessage(message);
			if(Main.isDbCmdDebug) StsException.systemDebug(this, "start", message);
		}
		Main.isJarDB = true;
		Main.jarDBFilename = Main.userSpecifiedJar();
        String dbName = Main.jarDBFilename.substring(Main.jarDBFilename.lastIndexOf("\\")+1,Main.jarDBFilename.indexOf(".jar"));
        if(!Main.openJarDatabase("db."+dbName, StsJar.constructor("", Main.jarDBFilename)))
			new StsMessage(this.win3d, StsMessage.ERROR, "Failed to open database from selected file.");
	}

	public void createArchive()
	{
		if(project.getName().equals("null"))
		{
			new StsMessage(this.win3d, StsMessage.INFO, "Must first open the project to be archived.");
			return;
		}
		writePersistManagerData();
		String archiveRoot = project.getProjectDirString();
		String archiveName = project.getName() + ".jar";
		db.close();
		new StsMessage(this.win3d, StsMessage.INFO, "Creating archive: " + archiveRoot + File.separator + "archiveFiles" + File.separator + archiveName + "\n\nAll projects in this directory will be included in the archive.");
		outputJar(archiveName, archiveRoot);
	}

    public void outputJar(String jarName, String rootDirectory)
    {
        final String _jarName = jarName;
        final String _root = rootDirectory;
        final StsProgressBarDialog dialog = StsProgressBarDialog.constructor(new Frame(), "Creating " + jarName + " archive.", false,10,50,400,100);
        final int numFilesInJar = StsToolkit.numFilesInDirectory(new File(rootDirectory));
        dialog.setProgressMax(numFilesInJar);
        dialog.setSize(500, dialog.getHeight());
        dialog.setLabelText("Creating " + jarName + " archive.");
        dialog.pack();
        dialog.setVisible(true);
		Runnable runJarOutput = new Runnable()
        {
            public void run()
            {
                StsToolkit.doOutputJar(_jarName, _root, dialog);
                dialog.setLabelText("Archive Complete");
                dialog.progressPanel.setValue(numFilesInJar);
                dialog.progressPanel.appendLine("Completed archiving " + numFilesInJar + " files to " + _root + File.separator + "archiveFiles" + File.separator + _jarName);
				db.openAndPositionToLastDBWrite();
            }
        };
		StsToolkit.runRunnable(runJarOutput);
    }

	private void renumberFamilies()
	{
		StsWindowFamily[] families = viewPersistManager.getFamilies();
		for(int n = 0; n < families.length; n++)
			families[n].renumberWindows(n);
	}

	/** close down all the 3d windows, save window and properties states */
	public void close()
	{
		if(project != null) project.close();
		for(StsClass stsClass : classList)
			stsClass.close();

		if(db != null)
		{
			db.commitCmd("End DB", new StsEndDBCmd());
			updateProperties();
			propertiesPersistManager.save(db);
			viewPersistManager.save(db);
		}

		GL gl = getRootGL();

		if(viewPersistManager.getFamilies() != null)
		{
			for(int n = 0; n < viewPersistManager.getFamilies().length; n++)
				viewPersistManager.getFamilies()[n].close(gl);
		}
		StsWellClass wc = (StsWellClass) getStsClass(StsWell.class);
		if(wc != null) wc.closeWindows();

		closeDatabase();
	}
	private void writePersistManagerData()
	{
		if(db == null) return;
		db.commitCmd("End DB", new StsEndDBCmd());
		updateProperties();
		propertiesPersistManager.save(db);
		viewPersistManager.save(db);
	}

	private GL getRootGL()
	{
		if(Main.sharedContext == null) return null;
		return Main.sharedContext.getGL();
	}

	public void refreshObjectPanel()
	{
		if(win3d == null || objectTreePanel == null) return;
		objectTreePanel.refreshTree();
	}

	public void constructObjectPanel()
	{
		if(win3d == null || objectTreePanel == null) return;
		objectTreePanel.refreshTree();
	}

	public void refreshObjectPanel(StsObject stsObject)
	{
		//		if(!stsObject.isPersistent()) return;
		if(objectTreePanel == null) return;

		StsClass stsClass = getStsClass(stsObject.getClass());
		if(stsClass instanceof StsObjectPanelClass)
			objectTreePanel.refreshTreeNode(((StsObjectPanelClass) stsClass).objectPanelNode);
	}

	public void refreshDataPanel()
	{
		if(win3d == null || dataFilesTreePanel == null) return;
		dataFilesTreePanel.refreshTree();
	}

	public Object resolveReference(int index, Class c)
	{
		StsObject obj = null;
		try
		{
			StsClass stsClass = getCreateStsClass(c);
			if(stsClass == null)
				System.out.println("Instancelist not found for " + StsToolkit.getSimpleClassname(c));
			else
			{
				obj = stsClass.getElementWithIndex(index);
				if(obj == null)
				{
					obj = stsClass.getEmptyStsClassObject(index);
					if(Main.isDbDebug)
						System.out.println("     initializing instance list object " + StsToolkit.getSimpleClassname(c) + "[" + index + "]");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception in StsObject.resolveReference()\n" + e);
		}
		return obj;
	}

	public void savePreferences()
	{
		StsUserPreferencesDialog dialog = new StsUserPreferencesDialog(this, false);
	}

	public boolean outputWindowGeometry()
	{
		WindowGeometry winGeom = new WindowGeometry();
		StsWindowFamily[] families = getWindowFamilies();
		for(int i = 0; i < families.length; i++)
		{
			StsWindowFamily family = families[i];
			StsWin3dBase[] windows = family.getWindows();
			for(int j = 0; j < windows.length; j++)
				winGeom.addWindow(windows[j]);
		}
		return winGeom.outputWindowGeometry();
	}

	public class WindowGeometry implements Serializable
	{
		int nWindows;
		public String[] winType;
		public boolean[] fullWindow;
		public int[] x, y, width, height;
		public transient StsWin3dBase[] wins;

		public WindowGeometry()
		{
			nWindows = 0;
			winType = new String[20];
			fullWindow = new boolean[20];
			x = new int[20];
			y = new int[20];
			width = new int[20];
			height = new int[20];
			wins = new StsWin3dBase[20];
		}

		public void addWindow(StsWin3dBase window)
		{
			wins[nWindows] = window;
			winType[nWindows] = window.getCurrentView().getViewName();
			if((window instanceof StsWin3d) || (window instanceof StsWin3dFull))
				fullWindow[nWindows] = true;
			else
				fullWindow[nWindows] = false;

			x[nWindows] = window.getX();
			y[nWindows] = window.getY();
			width[nWindows] = window.getWidth();
			height[nWindows] = window.getHeight();
			nWindows++;
		}

		public boolean outputWindowGeometry()
		{
			try
			{
				winType = (String[]) StsMath.trimArray(winType, nWindows);
				fullWindow = (boolean[]) StsMath.trimArray(fullWindow, nWindows);
				x = (int[]) StsMath.trimArray(x, nWindows);
				y = (int[]) StsMath.trimArray(y, nWindows);
				width = (int[]) StsMath.trimArray(width, nWindows);
				height = (int[]) StsMath.trimArray(height, nWindows);
				String filename = new String(System.getProperty("user.home") + File.separator + getWorkflowPlugIn().name + ".windows");
				StsParameterFile.writeObjectFields(filename, this);
				return true;
			}
			catch(Exception ex)
			{
				StsException.outputException("Error outputing window geometry", ex, StsException.FATAL);
				return false;
			}
		}

		public boolean loadWindowGeometry()
		{
			try
			{
				String filename = new String(System.getProperty("user.home") + File.separator + getWorkflowPlugIn().name + ".windows");
				StsParameterFile.initialReadObjectFields(filename, this);
				winType = (String[]) StsMath.trimArray(winType, nWindows);
				fullWindow = (boolean[]) StsMath.trimArray(fullWindow, nWindows);
				x = (int[]) StsMath.trimArray(x, nWindows);
				y = (int[]) StsMath.trimArray(y, nWindows);
				width = (int[]) StsMath.trimArray(width, nWindows);
				height = (int[]) StsMath.trimArray(height, nWindows);
				return true;
			}
			catch(Exception ex)
			{
				StsException.outputException("Error loading window geometry", ex, StsException.FATAL);
				return false;
			}
		}

		/* sets a new view based on window preferences. May create an aux window and set the view there. */
		/*
             public StsView setWindowPrefs(Class viewClass, String viewClassname)
             {
                 int reqWin = 0;

                 if(viewClassname == null)
                 {
                     // if all else fails, just set primary view
                     return glPanel3d.checkAddView(viewClass);
                 }

                 for(int i = 0; i < nWindows; i++)
                 {
                     //System.out.println("Compare "+reqType+" "+winType[i]);
                     if(viewClassname.equals(winType[i]))
                     {
                         reqWin = i;
                         break;
                     }
                 }

                 int kount = 0;
                 // is the preferred n'th window already available ?
                 if(reqWin < nWindows)
                 {
                     StsWindowFamily[] families = getWindowFamilies();

                     for(int i = 0; i < families.length; i++)
                     {
                         StsWindowFamily family = (StsWindowFamily)families[i];
                         StsWin3dBase[] windows = family.getWindows();
                         for(int j = 0; j < windows.length; j++)
                         {
                             if(kount == reqWin)
                             {
                                 return ((StsWin3dBase)windows[j]).glPanel3d.checkAddView(viewClass);
                             }
                             kount++;
                         }
                     }


                 }
                 if(kount == reqWin)
                 {
                     StsWin3dBase newWin = copyViewPreferred(win3d);
                     return newWin.glPanel3d.checkAddView(viewClass);
                 }
                 else
                 {
                     return glPanel3d.checkAddView(viewClass);
                 }

             }
        */
		/*  sets the preferred type and location based on which n'th window it is */
		/*
            public void setWindowPrefs(StsWin3dBase win)
            {
                StsWindowFamily[] families = getWindowFamilies();
                int index = -1;
                int kount = 0;
                for(int i = 0; i < families.length; i++)
                {
                    StsWindowFamily family = (StsWindowFamily)families[i];
                    StsWin3dBase[] windows = family.getWindows();
                    for(int j = 0; j < windows.length; j++)
                    {
                        if(win == ((StsWin3dBase)windows[j]))
                        {
                            index = kount;
                            break;
                        }
                        kount++;
                    }
                }
                if(index >= 0 && index < nWindows)
                {
                    if(index != 0)
                        this.setWindowView(winType[index], win.glPanel3d);
                    win.centerComponentOnScreen(x[index], y[index]);
                    win.setSize(width[index], height[index]);
                    win.validate();

                }
            }
        */
		/* this approach causes enormous problems as the new window types aren't yet usable until
        * data exists
        */
		/*
              public void launchWindows(StsModel model)
              {
                  StsWin3dBase currentParent = null, window = null;
                  for(int i = 0; i < nWindows; i++)
                  {
                      // Main window (win3d)
                      if(i == 0)
                      {
                          window = model.win3d;
                          if(window == null)
                              return;
                      }
                      else
                      {
                          // Launch a child window
                          if(fullWindow[i] == false)
                              window = model.copyView(currentParent);

                              // Launch a full function window
                          else
                              window = model.copyFullWindow(currentParent);
                      }

                      setWindowView(winType[i], window.glPanel3d);

                      // bad side effects
                      //setupToolbars(window, i);

                      // the above can trigger the window to change views; put back
                      // setWindowView(winType[i], window.glPanel3d);

                      window.centerComponentOnScreen(x[i], y[i]);
                      window.setSize(width[i], height[i]);
                      window.validate();

                      // Launch a child window
                      if(fullWindow[i] == true)
                          currentParent = window;
                  }
              }
        */
		/*
           private void setWindowView(String winType, StsGLPanel3d glPanel3d)
           {
               if(winType.equals(StsViewCursor.viewClassname2d))
                   glPanel3d.getView(StsViewCursor.class); // VIEW_2D is abstract
               else if(winType.equals(StsView3d.viewClassname3d))
                   glPanel3d.getView(StsView3d.class);
               else if(winType.equals(StsViewXP.viewClassnameXP))
                   glPanel3d.getView(StsViewXP.class);
               else if(winType.equals(StsViewSemblance3d.viewClassnameSemblance3d))
                   glPanel3d.getView(StsViewSemblance3d.class);
               else if(winType.equals(StsViewResidualSemblance3d.viewClassnameBackbone3d))
                   glPanel3d.getView(StsViewResidualSemblance3d.class);
               else if(winType.equals(StsViewGather3d.viewClassnameGather3d))
                   glPanel3d.getView(StsViewGather3d.class);
               else if(winType.equals(StsViewCVStacks.viewClassnameCVS))
                   glPanel3d.getView(StsViewCVStacks.class);
               else if(winType.equals(StsViewVVStacks.viewClassnameVVS))
                   glPanel3d.getView(StsViewVVStacks.class);
           }
        */
	}

	//public int index() { return -1; }
	//public void setIndex(int index) { }

	// JKF Added 22JUNE06
	// Not used.  Bad manners to put app specific classes in StsModel anyways...TJL 4/2/07
	/*
        public boolean hasDefinedGeometry()
        {
            return (getNObjects(StsSeismicVolume.class) + getNObjects(StsPreStackLineSet3d.class)) > 0;
        }
    */
	/*
        public StsObject getModelObject(Class c, int index)
        {
            if(c == StsModel.class)
                return this;
            else if(c == StsProject.class)
                return project;
            else if(index < 0)
                return null;
            else
            {
                StsObject object;
                object = getStsClassObjectWithIndex(c, index);
                if(object != null)return object;
                return getEmptyStsClassObject(c, index);
            }
        }
    */
}
