package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.DateTime.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.Actions.Loader.StsSeismicVolumeLoader;
import com.Sts.PlugIns.Seismic.DBTypes.StsSeismicVolume;
import com.Sts.PlugIns.Wells.Actions.Loader.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsLoader
{
	/** name of object being loaded, e.g., for wells, the well name. When loading a modified file, this name will be used to
	 *  identify the loader to be used since it already has information on loading this object (previous file-size, etc).
	 *  For multiple object loading, this name will be reset as each object is loaded. */
	public String name;
	public double xOrigin;
	public double yOrigin;
	public String[] columnNames;
	public boolean originInitialized = false;
	long sourceSize = 0;
	long newSourceSize = 0;

	transient public boolean deleteStsData = false;
	transient public String group;
	transient public int type;
	transient public int version = -1;
	transient public int nVectors = 0;
	transient protected int nRequired;

	transient public StsNameSet acceptableNameSet = new StsNameSet();
	transient public StsNameSet nameSet = new StsNameSet();

	transient public float nullValue = StsParameters.nullValue;
	transient public byte hUnits = StsParameters.DIST_NONE;
	transient public byte vUnits = StsParameters.DIST_NONE;
	transient public byte tUnits = StsParameters.TIME_NONE;
	transient public float datumShift = 0.0f;

	transient public StsProgressPanel progressPanel;
	/** These are the fieldKeyValues which were found in the file.  The String value is then converted
	 *  to the property type and used in setting the object field.
	 */
	transient public ArrayList<StsFieldKeyValue> foundFieldKeyValues = new ArrayList<StsFieldKeyValue>();

	static public final StsKeyList keyNames = new StsKeyList();
	static public final String X = keyNames.addKey("X");
	static public final String Y = keyNames.addKey("Y");
	static public final String Z = keyNames.addKey("Z");
	static public final String T = keyNames.addKey("T");
	static public final String M = keyNames.addKey("M");
	static public final String DEPTH = keyNames.addKey("DEPTH");
	static public final String MDEPTH = keyNames.addKey("MDEPTH");
	static public final String SEISMIC_TIME = keyNames.addKey("SEISMIC_TIME");
	static public final String SUBSEA = keyNames.addKey("SUBSEA");
	static public final String LOG = keyNames.addKey("LOG");
	static public final String DRIFT = keyNames.addKey("DRIFT");
	static public final String TWT = keyNames.addKey("TWT");
	static public final String OWT = keyNames.addKey("OWT");
	static public final String DIP = keyNames.addKey("DIP");
	static public final String AZIMUTH = keyNames.addKey("AZIMUTH");
	static public final String SUB_TYPE = keyNames.addKey("SUB_TYPE");
	static public final String STAGE = keyNames.addKey("STAGE");
	static public final String PERF_LENGTH = keyNames.addKey("PERF_LENGTH");
	static public final String PERF_NSHOTS = keyNames.addKey("PERF_NSHOTS");
	static public final String CLOCK_TIME = keyNames.addKey("CLOCK_TIME");
	static public final String DATE = keyNames.addKey("DATE");
	static public final String TIME_DATE = keyNames.addKey("TIME_DATE");
	static public final String LONG_TIME = keyNames.addKey("LONG_TIME");
	static public final String DUAL_TIME_DATE = keyNames.addKey("DUAL_TIME_DATE");
	static public final String[] DRIFT_KEYWORDS = {DRIFT};
	static public final String[] AZIMUTH_KEYWORDS = {AZIMUTH};
	static public final String[] DIP_KEYWORDS = {DIP};
	static public final String[] SENSOR_DEPTH_KEYWORDS = {DEPTH, MDEPTH, Z, "DZ", "ZEVT", "DOWN"};
	static public final String[] PERF_LENGTH_KEYWORDS = {PERF_LENGTH, "LEN", "PERF_LEN"};
	static public final String[] PERF_NSHOTS_KEYWORDS = {PERF_NSHOTS, "NSHOTS"};
	static public final String[] TIME_KEYWORDS = {"HOUR", "HMS", "TIMESTAMP", "TIMEVT"};
	static public final String[] DATE_KEYWORDS = {DATE, "DAY", "DATEVT"};
	static public final String[] CT_KEYWORDS = {CLOCK_TIME, "CT", "CTIME"};
	static public final String[] CT_TIME_KEYWORD = {"TIME"};
	static public final String[] TIME_DATE_KEYWORDS = {TIME_DATE};
	static public final String[] LONG_TIME_KEYWORDS = {LONG_TIME};

	static public final String[] xyztVectorNames = new String[] { StsLoader.X, Y, Z, T };
	static public final String[] xyztmVectorNames = new String[] { StsLoader.X, Y, Z, T, M };

	static public final int clockTimeColumnFlag = -2;
	static public final StsColumnName clockColumnNameTIME = StsColumnName.constructColumnName(CLOCK_TIME, CT_TIME_KEYWORD, clockTimeColumnFlag);
	static public final StsColumnName longTimeColumnName = StsColumnName.constructColumnName(LONG_TIME, LONG_TIME_KEYWORDS, clockTimeColumnFlag);
	static public final StsColumnName timeDateColumnName = StsColumnName.constructColumnName(TIME_DATE, TIME_DATE_KEYWORDS, clockTimeColumnFlag);
	static public final StsColumnName dateColumnName = StsColumnName.constructColumnName(DATE, DATE_KEYWORDS, clockTimeColumnFlag);
	static public final StsColumnName clockColumnName = StsColumnName.constructColumnName(CLOCK_TIME, CT_KEYWORDS, clockTimeColumnFlag);

	static public final StsColumnName[] clockTimeColumns = new StsColumnName[] { clockColumnName, dateColumnName, timeDateColumnName, longTimeColumnName };
	static public final String IGNORE_STRING = "IGNORE";
	static public final String WELLNAME = "WELLNAME";
	static public final String ORIGIN = "ORIGIN";
	static public final String XY = "XY";
	static public final String YX = "YX";
	static public final String CURVE = "CURVE";
	static public final String VALUE = "VALUE";
	static public final String NULL_VALUE = "NULL";
	public static final byte NO_TIME = -1;
	public static final byte TIME_AND_DATE = 0;
	public static final byte TIME_ONLY = 1;
	public static final byte ELAPSED_TIME = 2;
	public static final byte TIME_OR_DATE = 3;

	static public StsModel model;
	static public StsProject project;
    static public String currentDirectory = null;
	static public boolean isSourceData = true;
    static public String binaryDataDir;
	static public StsProgressPanel simulatorPanel = null;

	static ArrayList<StsLoader> loaders = new ArrayList<StsLoader>();

	static public StsMappedByteBufferScanner mappedByteBufferScanner;

	static public final String UNDEFINED = "UNDEFINED";

	static public final String[] X_KEYWORDS = {X, "EAST", "EASTING", "DX", "XEVT"};
	static public final StsColumnName xColumnName = StsColumnName.constructColumnName(X, X_KEYWORDS, 0);
	static public final String[] Y_KEYWORDS = {Y, "NORTH", "NORTHING", "DY", "YEVT"};
	static public final StsColumnName yColumnName = StsColumnName.constructColumnName(Y, Y_KEYWORDS, 1);
	// static public final String[] Z_KEYWORDS = {DEPTH.name, MDEPTH.name, SUBSEA.name, TIME.name};
	static public final String[] DEPTH_KEYWORDS = {DEPTH, SUBSEA, "Z", "D", "DEPT", "TVD" };
	static public final StsColumnName depthColumnName = StsColumnName.constructColumnName(DEPTH, DEPTH_KEYWORDS, 2);
	// static public final String[] Z_KEYWORDS = Z_KEYWORDS;
	static public final String[] MD_KEYWORDS = {MDEPTH, "MD", "M"};
	static public final StsColumnName mdepthColumnName = StsColumnName.constructColumnName(MDEPTH, MD_KEYWORDS, 4);
	static public final String[] T_KEYWORDS = {SEISMIC_TIME, TWT, OWT, "TIME", "T"};
	static public final StsColumnName seismicTimeColumnName = StsColumnName.constructColumnName(SEISMIC_TIME, T_KEYWORDS, 3);
	static public final String[] SUB_TYPE_KEYWORDS = {SUB_TYPE, "TYPE", "SUBTYPE", "SUB_TYPE", "LEN"};
	static public final String[] STAGE_KEYWORDS = {STAGE };

	static public final String FORMAT_TXT = "txt";
	static public final String FORMAT_BIN = "bin";
	static public final String FORMAT_DIR = "dir";
	static public final String FORMAT_DIRS = "dirs";

	static public final String GROUP_WELL = "well";
	static public final String GROUP_WELL_DEV = "well-dev";
	static public final String GROUP_WELL_LOGS = "well-logs";
	static public final String GROUP_WELL_TD = "well-td";
	static public final String GROUP_WELL_REF = "well-ref";
	static public final String GROUP_WELL_PERF = "well-perf";
	static public final String GROUP_WELL_FMI = "well-fmi";
	static public final String GROUP_WELL_EQUIP = "well-equipment";

	static public final String GROUP_MICROSEISMIC = "microseismic";
	static public final String GROUP_SENSOR = "sensor";
    static public final String GROUP_SEIS3D = "seis3d";

	static public final String[] wellSubfileGroups = new String[] { GROUP_WELL_LOGS, GROUP_WELL_TD, GROUP_WELL_REF, GROUP_WELL_PERF, GROUP_WELL_FMI, GROUP_WELL_EQUIP };
	static public final String STS_DATA_ACTION_DONT_DELETE = "Don't delete DB file(s) or reload object(s).";
	static public final String STS_DATA_ACTION_DELETE = "Delete DB file(s) & load/reload object(s).";
	// static public final String STS_DATA_ACTION_DONT_DELETE_IF_EXISTS = "If object(s) don't exist, delete DB file(s) and load object(s).";
	static public final String[] stsDataActions = new String[] { STS_DATA_ACTION_DONT_DELETE, STS_DATA_ACTION_DELETE };
	static public String stsDataActionToolTip = "User can choose to delete existing DB files.  If the associated object is already loaded, it will be deleted and reloaded.\n";

	static public final long nullLong = StsParameters.nullLongValue;

	static public final String tokenDelimiters = " \\,\t\n\r\f";

	static final boolean debug = true;

	abstract public void setGroup();
	abstract public StsMainObject getStsMainObject();
	abstract public boolean loadFile(StsAbstractFile sourceFile, boolean loadValues, boolean addToProject, boolean isSourceData);
	abstract public boolean changeSourceFile(StsAbstractFile file, boolean loadValues);
	abstract public boolean processColumnNames();
	abstract public boolean readFileHeader(StsAbstractFile file);
	abstract public boolean processStsFile(StsAbstractFile stsHeaderFile, boolean loadValues);
    abstract public void setNullValue();

	public StsLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		this(model, null, deleteStsData, isSourceData, progressPanel);
	}

	public StsLoader(StsModel model, String name, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		initialize(model, name, progressPanel);
		this.deleteStsData = deleteStsData;
        this.isSourceData = isSourceData;
		initializeFromProject();
	}

	private void initialize(StsModel model, String name, StsProgressPanel progressPanel)
	{
		this.model = model;
		this.name = name;
		this.progressPanel = progressPanel;
		project = model.getProject();
        setNullValue();
		setGroup();
	}

	private void initializeFromProject()
	{
		hUnits = project.getXyUnits();
		vUnits = project.getDepthUnits();
		tUnits = project.getTimeUnits();
	}

	public StsLoader(StsModel model, String name, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		initialize(model, name, progressPanel);
		initializeFromWizard(wizard);
		this.hUnits = wizard.getUnitsH();
		this.vUnits = wizard.getUnitsV();
		this.tUnits = wizard.getUnitsT();
		this.datumShift = wizard.getDatumShift();
		this.deleteStsData = wizard.getDeleteStsData();
	}

	public StsLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		initialize(model, null, progressPanel);
		initializeFromWizard(wizard);
		this.hUnits = wizard.getUnitsH();
		this.vUnits = wizard.getUnitsV();
		this.tUnits = wizard.getUnitsT();
		this.datumShift = wizard.getDatumShift();
		this.deleteStsData = wizard.getDeleteStsData();
	}

	public StsLoader(StsModel model)
	{
		initialize(model, null, null);
	}

	static public boolean constructObjects(StsAbstractFile[] files, boolean loadValues, StsProgressPanel progressPanel)
	{
		try
		{
			int nFiles = files.length;
			boolean showDetailedProgress = nFiles < 100;
			if(progressPanel != null) progressPanel.initialize(nFiles);
			int nLoaded = 0;

			for (StsAbstractFile file : files)
			{
				String objectName = file.name;
				StsLoader loader = StsLoader.getLoader(file, model, true);

				if (loader.processStsFile(file, loadValues))
				{
					if(showDetailedProgress && progressPanel != null)
						progressPanel.appendLine("   Successfully processed " + file.format + " formatted " + file.group + ": " + file.name + "...");
					nLoaded++;
				}
				else if(showDetailedProgress && progressPanel != null)
					progressPanel.appendLine("  Failed to process " + file.format + " formatted " + file.group + ": " + file.name + "...");

				progressPanel.setValue(++nLoaded);
				if(showDetailedProgress && progressPanel != null)
					loader.setDescription("Loaded " + file.group + " #" + nLoaded + " of " + nFiles);
			}
			return true;
		}
		catch (Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "constructObjects", e);
			progressPanel.setDescriptionAndLevel("StsLoader.constructObjects() failed.\n", StsProgressBar.WARNING);
			return false;
		}
	}

	private void initializeFromWizard(StsLoadWizard wizard)
	{
		this.hUnits = wizard.getUnitsH();
		this.vUnits = wizard.getUnitsV();
		this.tUnits = wizard.getUnitsT();
		this.datumShift = wizard.getDatumShift();
		this.deleteStsData = wizard.getDeleteStsData();
		this.isSourceData = wizard.isSourceData;
	}

	public boolean checkDeleteAsciiDirectory()
	{
		String asciiDirectoryPathname = getAsciiDirectoryPathname();
		if(!StsFile.checkDirectory(asciiDirectoryPathname))
		{
			StsException.systemError(this, "checkDeleteAsciiDirectory", "Failed to find or construct directory: " + asciiDirectoryPathname);
			return false;
		}
		// this.asciiDirectoryPathname = asciiDirectoryPathname;
		return true;
	}

	public boolean writeStsAsciiHeader(String directory, String filename) throws StsException
	{
		try
		{
			StsFile stsFile = StsFile.checkCreateFile(directory, filename);
			if(stsFile == null) return false;
			StsParameterFile.writeObjectFields(stsFile, this, StsLoader.class);
			return true;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "writeStsAsciiHeader", "Failed to write file: " + directory + "//" + filename);
			return false;
		}
	}

	/** leave the reader locked as the body reader will pick up where this reader left off and finally unlock it. */
	protected boolean readSourceFileHeader(StsAbstractFile sourceFile)
	{
		if(sourceFile == null) return false;
		name = sourceFile.name;
		//vectorSetObject.setDataSource(sourceFile);
		try
		{
			sourceFile.lockReader();
			if(!sourceFile.openReader()) return false;
			return readFileHeader(sourceFile);
			//if(!readFileHeader(sourceFile)) return false;
			//return writeAsciiFile(sourceFile);
		}
		catch(Exception e)
		{
			StsException.systemError(this, "readSourceFileHeader", "Failed to read file " + sourceFile.getPathname());
			sourceFile.unlockReader();
			return false;
		}
		finally
		{
//			file.closeReader();
		}
	}

	protected boolean stsHeaderFileOk(StsAbstractFile sourceFile)
	{
		StsAbstractFile stsHeaderFile = getMatchingAsciiFile(sourceFile, true);
		return stsHeaderFile != null && stsFileDateOk(stsHeaderFile, sourceFile);
	}
/*
	protected StsAbstractFile getSourceFile(StsAbstractFile stsFile)
	{
		if(!(stsFile instanceof StsFile)) return null;
		String sourceURIString = vectorSetObject.getSourceURI();
		StsFile sourceFile = StsFile.constructor(sourceURIString);
		if(!sourceFile.exists()) return null;
		return sourceFile;
	}
*/
	protected boolean stsFileDateOk(StsAbstractFile stsFile, StsAbstractFile sourceFile)
	{
		return stsFile.lastModified() >= sourceFile.lastModified();
	}

	protected boolean writeAsciiFile(StsAbstractFile sourceFile)
	{
		String asciiHeaderFilename = null;

		fillFoundFields(getStsMainObject());
		try
		{
			asciiHeaderFilename = constructAsciiFilename(group, sourceFile.name, version);
	        // delete this file if it already exists
			StsFile.deleteFile(getAsciiDirectoryPathname(), asciiHeaderFilename);
			writeStsAsciiHeader(getAsciiDirectoryPathname(), asciiHeaderFilename);
		}
		catch(Exception e)
		{
			String message = "Failed to write ascii header file: " + getAsciiDirectoryPathname() + "//" + asciiHeaderFilename;
			StsException.systemError(this, "readFileHeader", message);
			new StsMessage(null, StsMessage.ERROR, message);
			return false;
		}
		return true;
	}


	/** An stsAsciiFile must have the same group, name, sourceFile (String), and the creation date must be newer than the sourceFile. */
	public boolean readStsHeader(StsAbstractFile stsHeaderFile)
	{
		if(stsHeaderFile == null) return false;
		version = stsHeaderFile.version;
		nameSet.clear();
		try
		{
			stsHeaderFile.openReader();
			if(!StsParameterFile.readObjectFields(stsHeaderFile, this, StsLoader.class)) return false;
			return processColumnNames();
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "readStsHeader", e);
			return false;
		}
	}

	//TODO need to manage multiple versions: for now we are taking the last one
	protected StsAbstractFile getMatchingAsciiFile(StsAbstractFile sourceFile, boolean deleteIfMultiple)
	{
		StsFilenameFilter filter = new StsFilenameFilter(sourceFile.group, sourceFile.format, sourceFile.name);
		StsAbstractFileSet fileSet = StsFileSet.constructor(getAsciiDirectoryPathname(), filter);
		StsAbstractFile[] files = fileSet.getFiles();
		int nFiles = files.length; // there may be multiple files with different versions
		if(nFiles == 0) return null;
		return files[nFiles-1];
	}

	protected int getCurrentAsciiFileVersion(StsAbstractFile sourceFile)
	{
		StsFilenameFilter filter = new StsFilenameFilter(sourceFile.group, sourceFile.format, sourceFile.name);
		StsAbstractFile[] files = StsFileSet.constructor(getAsciiDirectoryPathname(), filter).getFiles();
		int maxVersion = -1;
		for(StsAbstractFile file : files)
			maxVersion = Math.max(file.version, maxVersion);
		return maxVersion;
	}

	private void deleteAsciiFiles(ArrayList<StsAbstractFile> files)
	{
		for(StsAbstractFile file : files)
			if(file instanceof StsFile) ((StsFile)file).delete();
	}

	public void addClockTimeNames()
	{
		acceptableNameSet.addAliases(clockColumnName);
		acceptableNameSet.addAliases(dateColumnName);
		acceptableNameSet.addAliases(timeDateColumnName);
		acceptableNameSet.addAliases(longTimeColumnName);
	}


	public void addClockTimePlusNames()
	{
		addClockTimeNames();
		acceptableNameSet.addAliases(clockColumnNameTIME);
	}

	static public boolean isTimeOrDateColumn(StsColumnName columnName)
	{
		return columnName.columnIndexFlag == clockTimeColumnFlag;
	}

    protected void closeBufRdr(BufferedReader bufRdr)
    {
        try
        {
            if(bufRdr == null) return;
            bufRdr.close();
        }
        catch(Exception e) { }
    }

	static public boolean lineHasKeyword(String line, String keyword)
	{
		return line.indexOf(keyword) >= 0;
	}


	static public boolean curveNameOk(String name)
	{
		return !name.equals("") && !name.equals(IGNORE_STRING);
	}

	static public String getBinaryFilename(String group, String name, String subname, int version)
	{
		return group + "." + FORMAT_BIN + "." + name + "." + subname + "." + version;
	}

	static public StsTimeVector checkConstructClockTimeVector(StsTimeVectorSet vectorSet, StsNameSet nameSet)
	{
		StsColumnName columnName;

		int dateOrder = model.getProject().getDateOrder();
		columnName = nameSet.getColumn(LONG_TIME);
		if(columnName != null)
			return new StsTimeVector(vectorSet, columnName, dateOrder);

		columnName = nameSet.getColumn(TIME_DATE);
		if(columnName != null)
			return new StsTimeVector(vectorSet, columnName, dateOrder);

		StsColumnName timeColumnName = nameSet.getColumn(CLOCK_TIME);
		if(timeColumnName == null) return null;
		StsColumnName dateColumnName = nameSet.getColumn(DATE);
		if(dateColumnName == null) return null;
		StsDualColumnName dualTimeColumnsName = new StsDualColumnName(DUAL_TIME_DATE, LONG_TIME, timeColumnName, dateColumnName);
			return new StsTimeVector(vectorSet, dualTimeColumnsName, dateOrder);
	}

	public boolean readMultiLineColumnNames(StsAbstractFile file, String endKeyword)
	{
		try
		{
			String line = file.readLine().trim();
			int columnIndex = 0;
			while (!lineHasKeyword(line, endKeyword))
			{
				// from all possible vectors (including aliases), find a matching nameVector and return a copy of it
				// with name and matching alias (if different), and current column index
				StsColumnName matchName = acceptableNameSet.getMatchName(line, columnIndex);
				if(matchName != null) // add this as an acceptable (standard) column name
				{
					matchName = new StsColumnName(matchName, columnIndex);
					nameSet.add(matchName);
				}
				else if(curveNameOk(line)) // foundVector == null: add this as an ok but non-standard column name
				{
					matchName = new StsColumnName(line, columnIndex);
					nameSet.add(matchName);
				}
				columnIndex++;
				nRequired++;
				line = file.readLine().trim();
			}
			columnNames = nameSet.getColumnNames();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "readMultiLineColumnNames", e);
			return false;
		}
	}

	public boolean readSingleLineColumnNames(String[] tokens)
	{
		try
		{
			int columnIndex = 0;
			for(String line : tokens)
			{
				// from all possible vectors (including aliases), find a matching nameVector and return a copy of it
				// with name and matching alias (if different), and current column index
				line = line.toUpperCase();
				StsColumnName matchName = acceptableNameSet.getMatchName(line, columnIndex);
				if(matchName != null) // add this as an acceptable (standard) column name
				{
					matchName = new StsColumnName(matchName, columnIndex);
					nameSet.add(matchName);
				}
				else if(curveNameOk(line)) // foundVector == null: add this as an ok but non-standard column name
				{
					matchName = new StsColumnName(line, columnIndex);
					nameSet.add(matchName);
				}
				columnIndex++;
				nRequired++;
			}
			columnNames = nameSet.getColumnNames();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "readMultiLineColumnNames", e);
			return false;
		}
	}

	protected void fillFoundFields(Object object)
	{
		for(StsFieldKeyValue fieldKeyValue : foundFieldKeyValues)
		{
			Field field = fieldKeyValue.field;
			try
			{
				StsToolkit.setObjectFieldValue(object, field, fieldKeyValue.fieldValue, fieldKeyValue.fieldName);
			}
			catch(Exception e)
			{
				StsException.systemError(this, "fillFoundFields", "Failed for " + fieldKeyValue);
			}
		}
	}

	static public boolean loadSourceFile(StsModel model, Path path, boolean loadValues)
	{
		StsFile file = model.getProject().getCreateSourceFile(path);

		StsLoader loader = getLoader(file, model, true);
		if(loader == null) return false;

		try
		{
			return loader.loadSourceFile(model, file, loadValues);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "loadSourceFile", e);
			return false;
		}
	}

	public boolean loadSourceFile(StsModel model, StsAbstractFile sourceFile, boolean loadValues)
	{
		try
		{
			model.getCreateCurrentTransaction("loadSourceFile " + sourceFile.filename); // get existing transaction or create a new on
			sourceSize = sourceFile.length();
			boolean ok = loadFile(sourceFile, loadValues, true, true);
			if(simulatorPanel != null)
			{
				if(ok)
					simulatorPanel.appendLine(sourceFile.filename + " loaded by " + StsToolkit.getSimpleClassname(this));
				else
					simulatorPanel.appendErrorLine(sourceFile.filename + " ERROR loading by " + StsToolkit.getSimpleClassname(this));

			}
			if(!ok) StsMessageFiles.errorMessage("Failed to load source file: " + sourceFile.filename);
			model.commit(); // commits any other commands which might have been successfully added to transaction
			if(!ok) return false;
			if(originInitialized)
			{
				model.getProject().initializeViews();
				originInitialized = false;
			}
			model.getProject().objectPanelChanged();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "loadSourceData", e);
			return false;
		}
	}

	static public boolean loadStsFile(StsModel model, StsAbstractFile stsFile, boolean loadValues)
	{
		StsLoader loader = getLoader(stsFile, model, true);
		if(loader == null) return false;

		try
		{
			return loader.doLoadStsFile(model, stsFile, loadValues);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "loadSourceFile", e);
			return false;
		}
	}

	public boolean doLoadStsFile(StsModel model, StsAbstractFile stsFile, boolean loadValues)
	{
		try
		{
			model.getCreateCurrentTransaction("loadSourceFile " + stsFile.filename); // get existing transaction or create a new on
			boolean ok = loadFile(stsFile, loadValues, true, false);
			if(simulatorPanel != null)
			{
				if(ok)
					simulatorPanel.appendLine(stsFile.filename + " loaded by " + StsToolkit.getSimpleClassname(this));
				else
					simulatorPanel.appendErrorLine(stsFile.filename + " ERROR loading by " + StsToolkit.getSimpleClassname(this));

			}
			if(!ok) StsMessageFiles.errorMessage("Failed to load data file: " + stsFile.filename);
			model.commit(); // commits any other commands which might have been successfully added to transaction
			if(!ok) return false;
			if(originInitialized)
			{
				model.getProject().initializeViews();
				originInitialized = false;
			}
			model.getProject().objectPanelChanged();
            name = null;  // clears this loader so it can be reused
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "loadSourceData", e);
			return false;
		}
	}

	/** gets loader for this file type (group & name).
	 *
	 * @param model
	 * @param fileCreated indicates that this is a new file just created; otherwise it's an old file which has just been changed
	 * @return
	 */
    //TODO we need to restore loaders.addLoader(StsLoader loader) operation for real-time loading
	static public StsLoader getLoader(StsAbstractFile file, StsModel model, boolean fileCreated)
	{
		file.parseFilename();
		String group = file.group;
		String name = file.name;
		int version = file.version;
		StsLoader loader = null;
		if(group.equals(GROUP_WELL))
		{
			StsWell well = (StsWell)model.getObjectWithName(StsWell.class, name);
			// return null if file was just created, but we already have the object created
			// otherwise it is a file changed and we must already have an existingWell
			if(fileCreated == (well != null)) return null;

			loader = StsLoader.getExistingLoader(name, group);
			if(loader != null) return loader;
			loader = new StsWellDevLoader(model, name);
		}
		else if(group.equals(GROUP_WELL_TD))
		{
			StsWell well = (StsWell)model.getObjectWithName(StsWell.class, name);
			if(well == null) return null;
			loader = StsLoader.getExistingLoader(name, group);
			if(loader != null) return loader;
		 	loader = new StsTdCurveLoader(model, well, false, null);
		}
		else if(group.equals(GROUP_WELL_LOGS))
		{
			StsWell well = (StsWell)model.getObjectWithName(StsWell.class, name);
			if(well == null) return null;
			// If this is a new file (fileCreated==true), then this logCurveVectorSet shouldn't exist;
			// if it does, it must be a duplicate, so ignore this file (return null);
			// if not a new file (file is being modified), then logCurveVectorSet must exist;
			// return null if it doesn't.
			boolean logCurveVectorSetExists = well.hasLogCurveVectorSet(name, version);
			if(fileCreated == logCurveVectorSetExists) return null;
			loader = StsLoader.getExistingLoader(name, group);
			if(loader != null) return loader;
		 	loader = new StsLogCurvesLoader(model, well, false, null);
		}
        else if(group.equals(GROUP_SEIS3D))
        {
            StsSeismicVolume volume = (StsSeismicVolume)model.getObjectWithName(StsSeismicVolume.class, name);
            // return null if file was just created, but we already have the object created
            if(fileCreated == (volume != null)) return null;
            loader = new StsSeismicVolumeLoader(model, name);
        }
		else
		{
			StsException.systemError(StsLoader.class, "getLoader", "Failed to find loader for file group: " + group);
			return null;
		}
		return loader;
	}

	static public Class getClassFromGroup(String group)
	{
		if(group == GROUP_WELL)
			return StsWell.class;
		else if(group == GROUP_WELL_TD)
			return StsWell.class;
		else if(group == GROUP_WELL_LOGS)
			return StsWell.class;
		else
			return null;
	}

	/** gets or creates the projectObjectLock and attempts to lock it; if it can't lock, it will wait until lock acquired. */
/*
	private void createProjectObjectLock()
	{
		if(projectObject == null)
		{
			StsException.systemError(this, "lockProjectObject", getLoaderDebugName() + " loader has null projectObject, so cannot lock.");
		}
		projectObjectLock = projectObjectLocks.get(projectObject);
		if(projectObjectLock == null)
		{
			projectObjectLock = new StsAbstractFileLock(projectObject);
			projectObjectLocks.put(projectObject, projectObjectLock);
		}
	}
*/
	/** gets or creates the projectObjectLock and attempts to lock it; if it can't lock, it will wait until lock acquired. */
/*
	protected void unlockProjectObject()
	{
		if(projectObject == null)
		{
			StsException.systemError(this, "lockProjectObject", getLoaderDebugName() + " loader has null projectObject, so cannot unlock.");
			return;
		}
		StsAbstractFileLock projectObjectLock = projectObjectLocks.get(projectObject);
		if(projectObjectLock == null)
		{
			StsException.systemError(this, "lockProjectObject", StsToolkit.getSimpleClassname(this) + "[" + this.name + "] cannot find projectObjectLock, so cannot unlock.");
		}
		projectObjectLock.unlock();
	}
*/
	static public StsLoader getExistingLoader(String name, String group)
	{
		for(StsLoader loader : loaders)
			if((loader.name != null && loader.name.equals(name)) && loader.group.equals(group)) return loader;
		return null;
	}

	static public boolean changedSourceFile(StsModel model, Path path)
	{
		StsFile file = model.getProject().getCreateSourceFile(path);
		StsLoader loader = getLoader(file, model, false);
		if(loader == null) return false;
		// loader.sourceSize = file.length();
		long size = file.length();
		if(debug) StsException.systemDebug(StsLoader.class, "changedSourceFile", file.filename + " size " + size + " loader " + StsToolkit.getSimpleClassname(loader));
		file.lockReader();
		try
		{
			return loader.changeSourceFile(file, true);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsLoader.class, "loadSourceData", e);
			return false;
		}
		finally
		{
			file.unlockReader();
		}
	}

	public boolean constructMappedBufferScanner(String pathname)
	{
		mappedByteBufferScanner = StsMappedByteBufferScanner.constructor(pathname);
		if(mappedByteBufferScanner == null) return false;
		try
		{
			return mappedByteBufferScanner.mapBuffer();
		}
		catch(Exception e)
		{
			StsException.systemError(this, "initializeMappedBufferScanner", "Failed to map byteBuffer " + mappedByteBufferScanner.getPathname());
			return false;
		}
	}
/*
	public boolean getCreateMappedBufferScanner()
	{
		if(mappedByteBufferScanner != null) return true;
		String sourceURIString = getStsMainObject().getSourceURI();
		mappedByteBufferScanner = StsMappedByteBufferScanner.constructor(sourceURIString);
		return mappedByteBufferScanner != null;
	}
*/
	public boolean initializeMappedBufferScanner()
	{
		if(mappedByteBufferScanner == null) return false;
		try
		{
			return mappedByteBufferScanner.mapBuffer();
		}
		catch(Exception e)
		{
			StsException.systemError(this, "initializeMappedBufferScanner", "Failed to map byteBuffer " + mappedByteBufferScanner.getPathname());
			return false;
		}
	}

	public void clearMappedBufferScanner()
	{
		if(mappedByteBufferScanner != null)
			mappedByteBufferScanner.close();
	}

	protected boolean addToModelAndProject(StsVectorSetObject vectorSetObject, boolean addToProject)
	{
		vectorSetObject.addToModel();
		if(!addToProject) return true;
		return vectorSetObject.addToProject();
	}

	public String getLoaderDebugName()
	{
		return "StsLoader " + StsToolkit.getSimpleClassname(this) + "[" + this.name + "] ";
	}

	public void appendLine(String line)
	{
		if(progressPanel == null) return;
		progressPanel.appendLine(line);
	}

    public void setDescriptionAndLevel(String progressDescription, int level)
    {
		if(progressPanel == null) return;
        progressPanel.setDescriptionAndLevel(progressDescription, level);
    }

    public void setDescription(String progressDescription)
    {
		if(progressPanel == null) return;
        progressPanel.setDescription(progressDescription);
    }

	public void appendErrorLine(String line)
	{
		if(progressPanel == null) return;
		progressPanel.appendErrorLine(line);
	}

	public String getAsciiDirectoryPathname()
	{
		StsMainObject stsMainObject = getStsMainObject();
		if(stsMainObject == null)
		{
			StsException.systemError(this, "getAsciiDirectoryPathname", "stsMainObject is null!");
			return null;
		}
		return stsMainObject.getAsciiDirectoryPathname();
	}

	static public String constructBinaryFilename(String group, String name, String subname, int version)
	{
		return group + "." + FORMAT_BIN + "." + name + "." + subname + "." + version;
	}

	static public String constructAsciiFilename(String group, String name, String subname, int version)
	{
		return group + "." + FORMAT_TXT + "." + name + "." + subname + "." + version;
	}

	static public String constructAsciiFilename(String group, String name, int version)
	{
		return group + "." + FORMAT_TXT + "." + name +  "." + version;
	}

	static public String constructAsciiFilename(String group, String name)
	{
		return group + "." + FORMAT_TXT + "." + name;
	}

	static public boolean isClockTimeVector(int columnIndexFlag)
	{
		for(StsColumnName clockTimeColumn : clockTimeColumns)
			if(columnIndexFlag == clockTimeColumn.columnIndexFlag) return true;
		return false;
	}

	public static long getTime(byte timeType, String timeToken, String dateToken, long start, int dateOrder)
	{
		String sTime = null;
		Date date = null;
		long lvalue = 0L;
		Calendar cal = null;
		try
		{
			if(timeType == ELAPSED_TIME)
			{
				lvalue = start + (long) (Double.valueOf(timeToken) * 60000); // Assuming minutes
			}
			else if(timeType == TIME_OR_DATE)
			{
				sTime = dateToken.trim() + " " + timeToken.trim();
				cal = CalendarParser.parse(sTime, dateOrder, true);
				lvalue = cal.getTimeInMillis();
			}
			else if(timeType == TIME_ONLY)
			{
				dateToken = project.getDateStringFromLong(start);
				sTime = dateToken.trim() + " " + timeToken.trim();
				cal = CalendarParser.parse(sTime, dateOrder, true);
				lvalue = cal.getTimeInMillis();
			}
			else
			{
				cal = CalendarParser.parse(timeToken, dateOrder, true);
				lvalue = cal.getTimeInMillis();
			}
			return lvalue;
		}
		catch(Exception ex)
		{
			StsMessageFiles.infoMessage("Unable to parse the time value, setting to 12-31-68 16:00:00");
			return lvalue;
		}
	}

	/** Because on reload the name string for a vector has a handle different from the static vector name of the same name,
	 *  reinitialize name to correspond.
	 */
	static public String reinitializeName(String name)
	{
		for(String keyName : keyNames)
			if(keyName.equals(name)) return keyName;
		return name;
	}
}