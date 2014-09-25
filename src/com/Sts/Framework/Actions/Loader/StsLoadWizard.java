//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Actions.Wizards.LoadComponents.*;
import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.Utilities.*;

abstract public class StsLoadWizard extends StsWizard
{
	public String typeName = "none";
	public boolean isSourceData = true;
	/** Exists stsFiles will be deleted and reloaded. */
	public boolean deleteStsData;
	/** group names for file filter */
	public String[] groups;
	/** format names for file filter */
	public String[] formats;
	/** directory where the selected files are */
	public String sourceDirectory;
	/** this describes the source file set: jar, webJar, or file directory; includes a filter for entries */
	public StsAbstractFileSet fileset;
	/** filter applied to fileset to get available files */
	public StsAbstractFilenameFilter filenameFilter;
	/** the set of files selected for loading */
	public StsAbstractFile[] files = new StsAbstractFile[0];
	/** data directory: directory where processed data files are stored; e.g., project/dataFiles/project.well_dirs/WellA.well_dir/ */
	public String dataDirectory;

    protected StsSourceSelectStep sourceSelectStep;
    protected StsVectorSetFilesSelectStep filesSelectStep;
    protected StsVectorSetFilesLoadStep filesLoadStep;

    private byte vUnits = StsParameters.DIST_NONE;
    private byte hUnits = StsParameters.DIST_NONE;
	private byte tUnits = StsParameters.TIME_NONE;
	private float datumShift = 0.0f;

	{
		StsProject project = model.getProject();
        hUnits = project.getXyUnits();
        vUnits = project.getDepthUnits();
		tUnits = project.getTimeUnits();
	}

	/** There are three steps in a vectorSetLoadWizard: 1) select source (file directory), 2) select files, 3) load files. */
    abstract protected StsSourceSelectStep initializeSourceSelectStep();
	abstract protected StsVectorSetFilesSelectStep initializeFilesSelectStep();
	abstract protected StsVectorSetFilesLoadStep initializeFilesLoadStep();
	abstract protected void initializeSourceSelectStepHeader(StsSourceSelectStep sourceSelectStep);
	abstract protected void initializeFilesSelectStepHeader(StsVectorSetFilesSelectStep fileSelectStep);
	abstract protected void initializeLoadStepHeader(StsVectorSetFilesLoadStep filesLoadStep);
	// abstract protected void initializeSelectedFiles();

	//TODO replace this with a method here which requests typename, groups, format, etc from static methods for the correct class (method invoke)
	/** Sets up file name parsing */
	abstract public void initializeNamesAndFilenameFilter();
	/** called when wizard is started to initialize things like current directory location, etc. */
	abstract public boolean initialize();
	/** gets the importer for this wizard */
	abstract public StsVectorSetImport getImporter(StsModel model, StsProgressPanel panel);

    public StsLoadWizard(StsActionManager actionManager, int width, int height)
    {
        super(actionManager, width, height);
        // StsLoader.initialize(model);
        addSteps();
    }

	public void addSteps()
	{
		constructSourceSelectStep();
		constructFilesSelectStep();
		constructFilesLoadStep();
		steps.add(sourceSelectStep);
		steps.add(filesSelectStep);
		steps.add(filesLoadStep);
	}

	private void constructSourceSelectStep()
	{
		sourceSelectStep = initializeSourceSelectStep();
		initializeSourceSelectStepHeader(sourceSelectStep);
	}

	private void constructFilesSelectStep()
	{
		filesSelectStep = initializeFilesSelectStep();
		initializeFilesSelectStepHeader(filesSelectStep);
	}

	private void constructFilesLoadStep()
	{
		filesLoadStep = initializeFilesLoadStep();
		initializeLoadStepHeader(filesLoadStep);
	}

    public boolean start()
    {

        System.runFinalization();
        System.gc();
        dialog.setTitle("Load " + typeName + " Data");
        initialize();
        this.disableFinish();
        return super.start();
    }

	public void previous()
    {
        gotoPreviousStep();
    }


    // public byte getFileType() {  return selectFileType.getType(); }
    public void finish()
    {
        success = true;
		model.getProject().saveDataDirectories();
		// model.refreshDataPanel();
        super.finish();
    }
	public boolean addFiles(StsAbstractFile[] files)
    {
        this.files = files;
        return true;
    }

    public void removeFile(StsAbstractFile file)
    {
        files = (StsAbstractFile[]) StsMath.arrayDeleteElement(files, file);
    }

	//TODO not very efficient for big sets:  if set is sorted, we could use a binary search, otherwise we need to generate a hashmap
	public void removeFiles(StsAbstractFile[] files)
	{
		for(StsAbstractFile file : files)
			this.files = (StsAbstractFile[]) StsMath.arrayDeleteElement(this.files, file);
	}

    public void removeFiles()
    {
        files = null;
    }

    public StsAbstractFile[] getSelectedFiles() { return files; }

    static public void main(String[] args)
    {
		StsToolkit.runLaterOnEventThread
		(
			new Runnable()
			{
				public void run()
				{
					Main.isDbDebug = false;
    				Main.isDbCmdDebug = true;
    				Main.isDbIODebug = false;
					StsModel model = StsModel.constructor("E:/Qclone/ClonedMicroseismic", "MicroseismicWizardTest");
					Main.model = model;
        			StsActionManager actionManager = new StsActionManager(model);
					actionManager.startAction(StsLoadWizard.class);
				}
			}
		);
    }

	public StsAbstractFile[] getFiles()
	{
		return files;
	}

	public void setFiles(StsAbstractFile[] files)
	{
		this.files = files;
	}

	public boolean isSourceData()
	{
		return isSourceData;
	}

	public void setSourceData(boolean sourceData)
	{
		isSourceData = sourceData;
	}

	public boolean getDeleteStsData()
	{
		return deleteStsData;
	}

	public void setDeleteStsData(boolean delete)
	{
		this.deleteStsData = delete;
	}

	public byte getUnitsV()
	{
		return vUnits;
	}

	public void setUnitsV(byte vUnits)
	{
		this.vUnits = vUnits;
	}

	public byte getUnitsH()
	{
		return hUnits;
	}

	public void setUnitsH(byte hUnits)
	{
		this.hUnits = hUnits;
	}

	public byte getUnitsT()
	{
		return tUnits;
	}

	public void setUnitsT(byte tUnits)
	{
		this.tUnits = tUnits;
	}

	public float getDatumShift()
	{
		return datumShift;
	}

	public void setDatumShift(float datumShift)
	{
		this.datumShift = datumShift;
	}

    public String getHorzUnitsString() { return StsParameters.DIST_STRINGS[hUnits]; }
    public String getVertUnitsString() { return StsParameters.DIST_STRINGS[vUnits]; }
    public String getTimeUnitsString() { return StsParameters.TIME_STRINGS[tUnits]; }

    public void setHorzUnitsString(String unitString)
    {
        hUnits = StsParameters.getDistanceUnitsFromString(unitString);
    }

    public void setVertUnitsString(String unitString)
    {
        vUnits = StsParameters.getDistanceUnitsFromString(unitString);
    }

    public void setTimeUnitsString(String unitString)
    {
        tUnits = StsParameters.getTimeUnitsFromString(unitString);
    }

	public void addFilesToDataPanel()
	{

	}
}
