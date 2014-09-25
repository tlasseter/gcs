//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Wells.Actions;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.Actions.Loader.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.awt.*;
import java.io.*;

public class StsWellImport extends StsVectorSetImport
{
	StsMarkerLoader markerLoader;

    static public String name = null; // name of object (example: wellname)
	static public final String FORMAT_BIN = StsLoader.FORMAT_BIN;
	static public final String FORMAT_UT = "ut";
	static public final String FORMAT_LAS = "las";

/** Multiple Well Files Definistions */
    static public final byte UWI = 0;
    static public final byte NAME = 1;
    static public final byte MX = 2;
    static public final byte MY = 3;
    static public final byte SYMBOL = 4;
    static public final byte KB = 5;
    static public final byte GRD = 6;
    static public final byte TVD = 7;
    static public final byte DATUM = 8;
    static public final byte[] headerCols = new byte[] {UWI, NAME, MX, MY, SYMBOL, KB, GRD, TVD, DATUM};
    static public final byte MD = 0;
    static public final byte AZIM = 1;
    static public final byte DIP = 2;
    static public final byte[] surveyCols = new byte[] {MD,AZIM,DIP};
    static public final byte TD_TIME = 1;
    static public final byte[] tdCols = new byte[] {MD,TD_TIME};
    static public final byte TOP = 1;
    static public final byte[] topsCols = new byte[] {MD,TOP};

//	static boolean[] applyShift = new boolean[] {false, false, false, false, false, false, false };

    static public final boolean debug = false;
/*
    static StsTimer timer = new StsTimer();
    static public final String[] typeNames = new String[]
        {"WEBJARFILE", "JARFILE", "BINARYFILES", "ASCIIFILES", "LASFILES", "UTFILES"};

	static public String filename = null;
    static public String subname = null; // name of subobject (example: curvename)
    static public int version = 0;  // version numbers if there identical filenames

    static public String getWellName() { return name; }
    static public String getCurveName() { return subname; }

	static StsFilenameFilter subFilesFilter;
	static
	{
		subFilesFilter = new StsFilenameFilter();
		subFilesFilter.setFormat(StsFilenameFilter.FORMAT_TXT);
		subFilesFilter.setOkGroups(fileGroups);
	}
	static StsFilenameFilter masterFileFilter;
	static
	{
		masterFileFilter = new StsFilenameFilter();
		masterFileFilter.setFormat(StsFilenameFilter.FORMAT_TXT);
		masterFileFilter.setGroup(StsWellLoader.GROUP_WELL_DEV.name);
	}

    public StsWellImport(StsModel model)
    {
        this.model = model;
        StsProject project = model.getProject();
		setCurrentDirectory(project.getProjectDirString());
        StsWellLoader.currentDirectory = project.getProjectDirString();
        StsWellLoader.binaryDataDir = project.getBinaryDirString();
        nLoaded = 0;
    }
*/
	public StsWellImport(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, progressPanel);
		initialize(model, wizard);
	}
/*
	public StsWellImport(StsModel model, StsProgressPanel progressPanel, StsLoadWizard wizard)
	{
		this.model = model;
        StsProject project = model.getProject();
		setCurrentDirectory(project.getProjectDirString());
        StsLoader.currentDirectory = project.getProjectDirString();
        StsLoader.binaryDataDir = project.getBinaryDirString();
        nLoaded = 0;

		datumShift = wizard.getDatumShift();
		this.isSourceData = wizard.isSourceData();
		this.hUnits = wizard.getUnitsH();
		this.vUnits = wizard.getUnitsV();
		this.tUnits = wizard.getUnitsT();
		this.progressPanel = progressPanel;
	}
*/

	@Override

	protected void setFileFilter()
	{
		filesFilter = new StsFilenameFilter();
		filesFilter.setFormat(StsFilenameFilter.FORMAT_TXT);
		filesFilter.setGroup(StsLoader.GROUP_WELL);
	}

	@Override
	public String getGroupName()
	{
		return StsLoader.GROUP_WELL;
	}

	@Override
	protected Class getObjectClass()
	{
		return StsWell.class;
	}

	@Override
	protected StsVectorSetLoader getLoader(StsModel model, StsLoadWizard wizard, boolean isSourceData, StsProgressPanel progressPanel)
	{
		return new StsWellDevLoader(model, wizard, progressPanel);
	}

	public String getCurrentDirectory()
    {
        return currentDirectory;
    }

    public void setCurrentDirectory(String dirPath)
    {
		this.currentDirectory = dirPath;
        StsWellLoader.currentDirectory = dirPath;
    }

	protected void addSubFiles(StsAbstractFile[] files)
	{

	}
/*
    public StsWell[] constructWells(StsWellWizard wizard)
    {
        StsWell well;

        try
        {
			StsAbstractFile[] wellFiles = wizard.getFiles();
            int nWells = wellFiles.length;
			showDetailedProgress = nWells < 100;
            ArrayList<StsWell> wells = new ArrayList<StsWell>();
            progressPanel.initialize(nWells);
			int n = 0;
			vectorSetLoader = new StsWellDevLoader(model, progressPanel, wizard);
			deleteStsData = vectorSetLoader.deleteStsData;
            for (StsAbstractFile wellFile : wellFiles)
            {
				if(!wellFile.isAFileAndNotDirectory())
					wellFile = StsWellDevLoader.constructMasterFileFromFolder(wellFile);
				if(wellFile == null) continue;

                String wellname = wellFile.name;

                // read and build a well (if we don't already have it)
				boolean objectExists = model.getObjectWithName(StsWell.class, wellname) != null;
				if(objectExists)
				{
					if(!deleteStsData)
					{
						vectorSetLoader.appendLine("  Object " + wellname + " already exists. Will not delete/reload.");
						vectorSetLoader.setDescriptionAndLevel("Microseismic: " + wellname + " already exists. Will not delete/reload...", StsProgressBar.INFO);
						continue;
					}
					else // object exists and we want to delete sts data which then requires a reload from ascii
					{
						well = (StsWell) model.getObjectWithName(StsWell.class, wellname);
						if(well == null)
						{
							progressPanel.appendLine("  Failed to find/delete existing object " + wellname);
							progressPanel.setDescriptionAndLevel("Microseismic: " + wellname + " Failed to find/delete...", StsProgressBar.WARNING);
							continue;
						}
						well.delete();
					}
				}
				well = constructWell(wellFile, progressPanel);
				if (well != null)
				{
					if (showDetailedProgress)
						vectorSetLoader.appendLine("   Successfully processed " + wellFile.format + " formatted deviation file for well " + name + "...");
					wells.add(well);
					nLoaded++;
				}
				else if(showDetailedProgress)
					vectorSetLoader.appendLine("  Failed to process " + wellFile.format + " formatted deviation file for well " + name + "...");

                progressPanel.setValue(++n);
                if(showDetailedProgress) vectorSetLoader.setDescription("Loaded well #" + nLoaded + " of " + nWells);
            }
            return wells.toArray(new StsWell[wells.size()]);

        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsWellImport.class, "constructWells", e);
            progressPanel.setDescriptionAndLevel("StsWellWizard.constructWells() failed.\n", StsProgressBar.WARNING);
            return null;
        }
    }
*/
    /** read in multiple well deviations from a list of Ascii files */
/*
    public StsWell constructWell(StsAbstractFile wellFile, StsProgressPanel progressPanel)
    {
        name = wellFile.name;

        try
        {
            if (showDetailedProgress) vectorSetLoader.appendLine("Processing S2S formatted well: " + name + "...");
			if(!vectorSetLoader.loadSourceFile(wellFile, true, false)) return null;
			return (StsWell)vectorSetLoader.getTimeVectorSetObject();
        }
        catch (Exception e)
        {
            StsException.outputException("StsWellWizard failed.", e, StsException.WARNING);
            StsMessageFiles.logMessage("Unable to read well deviation files.");
            if (progressPanel != null) progressPanel.setDescriptionAndLevel("Unable to read well deviation files.\n", StsProgressBar.ERROR);
            return null;
        }
    }

	private void checkApplyDatumShift(int shiftType)
	{
        if(applyShift[shiftType])
        	StsWellLoader.appliedShift = datumShift;
		else
			StsWellLoader.appliedShift = 0.0f;
	}
*/
/*
	private void addTdCurve(StsAbstractFile wellFile, StsWell well, StsProgressPanel progressPanel)
	{
		checkApplyDatumShift(TD);
		StsAbstractFile[] tdFiles = wellFile.getGroupSubFiles(StsWellLoader.GROUP_WELL_TD.name);
		if(tdFiles.length == 0) return;
		StsTdCurveLoader tdFileLoader = getTdCurveLoader(well);
		boolean success = false;
		for(StsAbstractFile tdFile : tdFiles)
		{
			success = success | tdFileLoader.processVectorFile(tdFile, well, true);
			if(progressPanel != null)
			{
				if(!success)
					progressPanel.appendLine("  Failed to process log curves from file(s) for well " + name + "...");
				else if(showDetailedProgress)
					progressPanel.appendLine("   Successfully processed txt formatted time-depth curve for well " + name + "...");
			}
		}
		if(success)
		{
			well.setZDomainSupported(StsProject.TD_TIME_DEPTH);
			well.checkBuildTimeVector();
		}
	}

	private void addLogCurves(StsAbstractFile wellFile, StsWell well, StsProgressPanel progressPanel)
	{
		StsLogCurvesLoader logCurveLoader = getLogCurvesLoader(well);
		checkApplyDatumShift(LOG);
		StsAbstractFile[] files = wellFile.getGroupSubFiles(StsWellLoader.GROUP_WELL_LOG.name);

		for(StsAbstractFile file : files)
		{
			StsLogCurve[] logCurves = new StsLogCurve[0];

			if (file.format.equals(FORMAT_TXT))
			{
				//TODO should pass depthUnits
				if(!logCurveLoader.processVectorFile(file, well, false))
					if (progressPanel != null) progressPanel.appendLine("  Failed to process log curves from file(s) for well " + name + "...");

			}
			else if (showDetailedProgress && progressPanel != null) progressPanel.appendLine("   Successfully processed " + logCurves.length + " logs from file(s) for well " + name + "...");
		}
	}

	private StsLogCurvesLoader getLogCurvesLoader(StsWell well)
	{
		if(logCurvesLoader != null) return logCurvesLoader;
		 	logCurvesLoader = new StsLogCurvesLoader(model, isSourceData);
		logCurvesLoader.setProjectObject(well);
		return logCurvesLoader;
	}

	private StsTdCurveLoader getTdCurveLoader(StsWell well)
	{
		if(tdCurveLoader != null) return tdCurveLoader;
		 	tdCurveLoader = new StsTdCurveLoader(model, isSourceData);
		tdCurveLoader.setProjectObject(well);
		return tdCurveLoader;
	}
*/
	private StsMarkerLoader getMarkerLoader(StsWell well)
	{
		if(markerLoader != null) return markerLoader;
		 	markerLoader = new StsMarkerLoader(model, name, well, wizard.deleteStsData, isSourceData, progressPanel);
		markerLoader.setStsMainObject(well);
		return markerLoader;
	}

	protected void addMarkers(StsAbstractFile wellFile, StsWell well, StsProgressPanel progressPanel)
	{
		loadWellMarkers(wellFile, well, progressPanel);
        //loadPerforationMarkers(wellFile, well, progressPanel);
        //loadFmiMarkers(wellFile, well, progressPanel);
        loadEquipmentMarkers(wellFile, well, progressPanel);
	}
/*
    public void setApplyDatumShift(boolean[] apply)
    {
        applyShift = apply;
    }

	public StsAbstractFileSet constructWellDevFilenameSet(String directory, boolean isSourceData)
	{
		StsWellLoader.currentDirectory = directory;
		byte fileOrganization = isSourceData ? StsAbstractFileSetConstructor.SINGLE_DIRECTORY : StsAbstractFileSetConstructor.SUB_DIRECTORIES;
		fileSet = StsAbstractFileSetConstructor.constructor(fileOrganization, StsWell.class, directory, masterGroup, subFileGroups);
		return fileSet;
	}
*/
/*
    public void compressWellSet()
    {
        Iterator iter;
        WellFilenameSet wellFilenameSet;
        if (debug)
        {
            timer.start();
        }

        iter = wellFilenameSets.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            wellFilenameSet = (WellFilenameSet) entry.getFloat();
            if (!wellFilenameSet.isOK())
            {
                iter.remove();
                continue;
            }

            // If we don't want to reload it, delete it from the list of available wells.
            StsWell existingWell = (StsWell) model.getObjectWithName(StsWell.class, wellFilenameSet.wellname);
            if (existingWell != null)
            {
                if(!reloadAscii)
                {
                    iter.remove();
                }
            }
        }
        if (debug)
        {
            timer.stopPrint("Time for compressWellSet.");
        }
        return;
    }

    public boolean addWellFilenameSets(String[] filenames, byte type)
    {
        String filename = null;
        WellFilenameSet wellFilenameSet;

        try
        {
            if (debug)
            {
                timer.start();
            }

            if (filenames == null)
            {
                return false;
            }

            if (wellFilenameSets == null)
            {
                wellFilenameSets = new TreeMap();
            }

            int nFilenames = filenames.length;
            for (int n = 0; n < nFilenames; n++)
            {
                filename = filenames[n];
                if ( (type == ASCIIFILES) || (type == LASFILES) || (type == UTFILES))
                {
                    StsWellKeywordIO.parseAsciiFilename(filename);
                }
                else if(type == GEOGRAPHIX)
                {
                    StsWellKeywordIO.name = filename.substring(0,filename.indexOf("."));
                }
                else
                {
                    StsWellKeywordIO.parseBinaryFilename(filename);
                }
                String wellname = StsWellKeywordIO.getWellName();
                if (wellFilenameSets.containsKey(wellname))
                {
                    wellFilenameSet = (WellFilenameSet) wellFilenameSets.get(wellname);
                    if ( (type == ASCIIFILES) || (type == LASFILES) || (type == UTFILES))
                    {
                        if (!wellFilenameSet.asciiAlreadyInList(filename, StsWellKeywordIO.group))
                        {
                            wellFilenameSet.addAsciiFilename(filename, StsWellKeywordIO.group, type, filePrefixes);
                        }
                    }

                    else
                    {
                        if (!wellFilenameSet.binaryAlreadyInList(filename, StsWellKeywordIO.group, StsWellKeywordIO.getCurveName()))
                        {
                            wellFilenameSet.addBinaryFilename(filename, StsWellKeywordIO.group, StsWellKeywordIO.getCurveName());
                        }
                    }
                }
                else
                {
                    wellFilenameSet = new WellFilenameSet(wellname, type);
                    wellFilenameSets.put(wellname, wellFilenameSet);
                    if ( (type == ASCIIFILES) || (type == LASFILES) || (type == UTFILES))
                    {
                        wellFilenameSet.addAsciiFilename(filename, StsWellKeywordIO.group, type, filePrefixes);
                    }
                    else if(type == GEOGRAPHIX)
                    {
                        wellFilenameSet.addDevFilename(filename);
                        wellFilenameSet.welltype = type;
                    }
                    else
                    {
                        wellFilenameSet.addBinaryFilename(filename, StsWellKeywordIO.group, StsWellKeywordIO.getCurveName());
                    }
                }
            }
            if (debug)
            {
                timer.stopPrint("Time to initializeWellFielnameSets for " + nFilenames + "files");
            }

            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWellWizard.initializeWellFilenameSets() failed for file: " + filename + ".", e,
                                         StsException.WARNING);
            return false;
        }
    }

    public class WellFilenameSet
    {
        public String wellname;
        public byte welltype = -1;
		Hashtable<String, ArrayList<String>> filenamesTable = new Hashtable<String, ArrayList<String>>();
        public boolean hasDepth = false;

        public WellFilenameSet(String wellname, byte welltype)
        {
            this.wellname = wellname;
            this.welltype = welltype;
        }

		public void addFilename(String key, String filename)
		{
			ArrayList<String> filenameList = filenamesTable.get(key);
			if(filenameList == null)
			{
				filenameList = new ArrayList<String>();
				filenamesTable.put(key, filenameList);
			}
			filenameList.add(filename);
		}

		private String[] getFilenames(String key)
		{
			ArrayList<String> filenameList = filenamesTable.get(key);
			if(filenameList == null) return new String[0];
			return filenameList.toArray(new String[filenameList.size()]);
		}

        // add entries according to type; see StsDataVector for type definitions
        // types: 0 = X, 1 = Y, 2 = DEPTH, 3 = MDEPTH

        public void addBinaryFilename(String filename, String group, String curvename)
        {
			addFilename(group + ".bin." +  curvename, filename);
			if (type == StsDataVector.DEPTH || type == StsDataVector.MDEPTH)
                hasDepth = true;
		}

        public void addAsciiFilename(String filename, String group, byte type, String[] filePrefixs)
        {
			addFilename(group + ".txt." + , filename);
//			System.out.println("add Ascii "+filename+" "+prefix);
            if (group.equals(WELL_DEV_GROUP))
                welltype = type;
            }
        }

        public boolean binaryAlreadyInList(String filename, String group, String curvename)
        {
			// return getFilename(group + ".bin." + curvename) != null;
			return false;
        }

        public boolean asciiAlreadyInList(String filename, String group, String format, String wellname)
        {
			return wellFilenameSet.getFilenames(group);
			return getFilenames(group + format + wellname) != null;
        }

        public boolean isOK()
        {
			String[] devFilenames = wellFilenameSet.getFilenames(WELL_DEV_GROUP);
            if ( (welltype == ASCIIFILES) || (welltype == LASFILES) || (welltype == UTFILES) || (welltype == GEOGRAPHIX))
            {
				if(devFilenames.length == 0) return false;
            }
            else
            {
                if(devFilenames.length < 4) return false;
                Collections.sort(getFilenames(WELL_LOG_GROUP));
                return true;
            }
        }
    }

     public StsWell createWellFromBinaryFile(String wellname, String dataDir, String binDir)
     {
      try
      {
       StsDataVector[] xyztmVectors = null;

       xyztmVectors = getBinaryWellDeviationVectors(wellname);

                StsDataVector vector = StsDataVector.getVectorOfType(xyztmVectors, StsDataVector.X);
                vUnits = vector.getVerticalUnits();
                vector = StsDataVector.getVectorOfType(xyztmVectors, StsDataVector.MDEPTH);
                hUnits = vector.getHorizontalUnits();

                if (xyztmVectors == null)
                {
                    return null;
                }
                StsDataVector tVector = StsDataVector.getVectorOfType(xyztmVectors, StsDataVector.TIME);
                StsWell well = processVectorFile(wellname, xyztmVectors, null);
                if (well == null) return null;
                well.addToProject();

                StsDataVector[] logCurveVectors = getBinaryLogCurveVectors(well);
                vUnits = logCurveVectors[0].getVerticalUnits();
                hUnits = logCurveVectors[0].getHorizontalUnits();

                if (logCurveVectors != null)
                {
                    well.constructLogCurvesCheckVersions(logCurveVectors, StsParameters.nullValue);
                }
                else if (!Main.isJarFile)
                {
//             StsWellKeywordIO.classInitialize(model);
     StsLogCurve[] logCurves = StsWellKeywordIO.processVectorFile(well, wellname, dataDir, binDir, StsDataVector.WELL_LOG_GROUP, StsParameters.nullValue);
                    well.addLogCurves(logCurves);
                }
                loadWells.panel.setText("Processing binary well: " + wellname + "...", false);

                return well;
            }
            catch (Exception e)
            {
                StsException.outputException("StsWellWizard.createWellsFromBinaryJar() failed.", e, StsException.WARNING);
                return null;
            }
        }
     */
    // read the 4 logVectors: X, Y, DEPTH, MDEPTH.  First 3 are required.
    /*	private StsDataVector[] getBinaryWellDeviationVectors(String wellname)
     {
      try
      {
       WellFilenameSet wellFilenameSet = (WellFilenameSet) wellFilenameSets.get(wellname);
       if (wellFilenameSet == null)
       {
        return null;
       }
       String[] filenames = wellFilenameSet.devFilenames;
       StsDataVector[] logVectors = getBinaryWellVectors(wellname, filenames);
       if (!StsDataVector.deviationVectorsOK(logVectors))
       {
        return null;
       }
       return logVectors;
      }
      catch (Exception e)
      {
       StsException.outputException("StsWellWizard.getBinaryWellDeviationVectors() failed.", e, StsException.WARNING);
       return null;
      }
     }
     */
    /*	private StsDataVector[] getBinaryWellVectors(String wellname, String[] filenames)
     {
      InputStream is;
      URL url;
      URLConnection urlConnection;

      try
      {
       boolean ok = true;

       if (filenames == null)
       {
        return null;
       }

       int nFilenames = filenames.length;
       StsDataVector[] logVectors = new StsDataVector[nFilenames];

       for (int n = 0; n < nFilenames; n++)
       {
        String filename = filenames[n];
        if (filename == null)
        {
         continue;
        }
        StsAbstractFile file = fileSet.getFile(filename);
        logVectors[n] = new StsDataVector(filename);
        if (!logVectors[n].readBinaryFile(file, true))
        {
         ok = false;
        }
       }
       if (!ok)
       {
        return null;
       }
       return logVectors;
      }
      catch (Exception e)
      {
       StsException.outputException("StsWellWizard.getWellDeviationVectors() failed.", e, StsException.WARNING);
       return null;
      }
     }

     private StsDataVector[] getBinaryLogCurveVectors(StsWell well)
     {
      try
      {
       if (well == null)
       {
        return null;
       }
       String wellname = well.getName();
       WellFilenameSet wellFilenameSet = (WellFilenameSet) wellFilenameSets.get(wellname);
       if (wellFilenameSet == null)
       {
        return null;
       }
       String[] filenames = wellFilenameSet.logFilenames;
       return getBinaryWellVectors(wellname, filenames);
      }
      catch (Exception e)
      {
       StsException.outputException("StsWellWizard.getBinaryLogCurveVectors() failed.", e, StsException.WARNING);
       return null;
      }
     }
     */
/*
    public StsWell[] constructFromOSWells(StsProgressPanel progressPanel, StsOSWellDatastore datastore, Object[] osWells)
    {
        try
        {
            if (osWells == null) return null;

            int nSelected = osWells.length;

            if(showDetailedProgress)
			{
				vectorSetLoader.appendLine("Preparing to load " + nSelected + " well from OpenSpirit ...");
				StsMessageFiles.logMessage("Preparing to load " + nSelected + " well from OpenSpirit ...");
			}
            StsWell[] wells = null;
            StsOSWell[] ospWells = (StsOSWell[])osWells;

            ospWells = datastore.getOpenSpiritImport().createStsOSWells(ospWells, progressPanel);
            return wells;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWellWizard.constructWells() failed.", e, StsException.WARNING);
            return null;
        }
    }
 */
	int checkGetCloneNumber(String wellname)
	{
		int index = wellname.lastIndexOf("#");
		if(index < 0) return index;
		String numberString = wellname.substring(index+1, wellname.length());
		try { return Integer.parseInt(numberString); }
		catch(Exception e) { return -1; }
	}

    /** read a marker file and construct markers */
    public int loadEquipmentMarkers(StsAbstractFile wellFile, StsWell well, StsProgressPanel progressPanel)
    {
        BufferedReader bufRdr = null;
		StsAbstractFile[] markerFiles = wellFile.getGroupSubFiles(StsLoader.GROUP_WELL_EQUIP);
        int nMarkersLoaded = 0;
        try
        {
			StsEquipmentMarkerLoader markerLoader = new StsEquipmentMarkerLoader(model, wellFile.name, well, wizard.deleteStsData, isSourceData, progressPanel);
			for(StsAbstractFile markerFile : markerFiles)
				if(markerLoader.processFile(markerFile, progressPanel))
					nMarkersLoaded += markerLoader.nLoaded;
			return nMarkersLoaded;
        }
        catch (Exception e)
        {
            StsMessageFiles.logMessage("constructMarkers() failed for well " + well.getName());
            vectorSetLoader.appendLine("   Failed to process equipment markers for well " + well.getName());
            return nMarkersLoaded;
        }
        finally
        {
            closeBufRdr(bufRdr);
        }
    }

    /** read a marker file and construct markers */
    public int loadWellMarkers(StsAbstractFile wellFile, StsWell well, StsProgressPanel progressPanel)
    {
		BufferedReader bufRdr = null;
		StsAbstractFile[] markerFiles = wellFile.getGroupSubFiles(StsLoader.GROUP_WELL_REF);
        int nMarkersLoaded = 0;
		StsMarkerLoader markerLoader = getMarkerLoader(well);
        try
        {
			for(StsAbstractFile markerFile : markerFiles)
				if(markerLoader.processFile(markerFile, progressPanel))
					nMarkersLoaded += markerLoader.nLoaded;
			return nMarkersLoaded;
        }
        catch (Exception e)
        {
            StsMessageFiles.logMessage("constructWellMarkers() failed for well " + well.getName());
            markerLoader.appendLine("   Failed to process markers for well " + well.getName());
            return nMarkersLoaded;
        }
        finally
        {
            closeBufRdr(bufRdr);
        }
    }

    // test program
  	static public void main(String[] args)
  	{
        StsDataVectorFace[] curves = null;
    	try
        {
            // Create a file dialog to query the user for a filename.
    	    Frame frame = new Frame();
   	 	    FileDialog f = new FileDialog(frame, "choose a well deviation file", FileDialog.LOAD);
            f.setVisible(true);
    	    String path = f.getDirectory();
            String filename = f.getFile();
			StsFile file = StsFile.constructor(path, filename);
            // make a database
        	StsModel model = new StsModel();
            // read the file
			StsWellDevLoader wellConstructor = new StsWellDevLoader(model);
			StsWell well = new StsWell("TestWell", false);
			wellConstructor.processVectorFile(file, well, true, true);
        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsWellImport.class, "main", e);
        }
  	}
/*
	public int checkIndex(String token, int currentIndex, int index, String[] keywords)
	{
        if(index == -1)
        {
            for(int j=0; j<keywords.length; j++)
            {
                if(token.equalsIgnoreCase(keywords[j]))
                    return currentIndex;
            }
            return index;
        }
        else
        	return index;
	}

	protected boolean findKeyword(BufferedReader bufRdr, String keyword)
	{
		String line;
		try
		{
			while( (line = bufRdr.readLine().trim()) != null)
				if(line.indexOf(keyword) >= 0) return true;
			return false;
		}
		catch(Exception e)
		{
			StsException.outputException("StsLoader.findKeyword() failed.",
				e, StsException.WARNING);
			return false;
		}
	}
*/
    protected void closeBufRdr(BufferedReader bufRdr)
    {
        try
        {
            if(bufRdr == null) return;
            bufRdr.close();
        }
        catch(Exception e) { }
    }
}
