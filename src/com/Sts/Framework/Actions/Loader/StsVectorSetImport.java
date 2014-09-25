//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.Actions.Loader.*;

import java.awt.*;
import java.util.*;

abstract public class StsVectorSetImport
{
    public StsModel model;
	public StsLoadWizard wizard;
	public StsVectorSetLoader vectorSetLoader;
	public boolean isSourceData = true;
    public int nLoaded = 0;
	public String messageName;
    public StsAbstractFileSet fileSet;
	public String currentDirectory = ".";
	public float datumShift = 0.0f;
	public byte hUnits, vUnits, tUnits;

	public StsProgressPanel progressPanel;
	protected StsFilenameFilter filesFilter;

	static protected boolean showDetailedProgress = true;
	static public final String FORMAT_TXT = StsLoader.FORMAT_TXT;


	static public final boolean debug = false;

	abstract protected void setFileFilter();
	abstract public String getGroupName();
	abstract protected Class getObjectClass();
	abstract protected StsVectorSetLoader getLoader(StsModel model, StsLoadWizard wizard, boolean isSourceData, StsProgressPanel progressPanel);

	public StsVectorSetImport() { }

	public StsVectorSetImport(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		initialize(model, wizard);
		this.progressPanel = progressPanel;
		this.isSourceData = wizard.isSourceData();
	}

	protected void initialize(StsModel model, StsLoadWizard wizard)
	{
        this.model = model;
		this.wizard = wizard;
        StsProject project = model.getProject();
		currentDirectory = wizard.sourceDirectory;
		//setCurrentDirectory(project.getProjectDirString());
        StsWellLoader.currentDirectory = project.getProjectDirString();
        StsWellLoader.binaryDataDir = project.getBinaryDirString();
        nLoaded = 0;
		setFileFilter();
		initializeDatumAndUnits();
    }

	protected void initializeDatumAndUnits()
	{
		datumShift = wizard.getDatumShift();
		this.hUnits = wizard.getUnitsH();
		this.vUnits = wizard.getUnitsV();
		this.tUnits = wizard.getUnitsT();
	}

    public String getCurrentDirectory()
    {
        return currentDirectory;
    }

    public void setCurrentDirectory(String dirPath)
    {
		this.currentDirectory = dirPath;
        StsLoader.currentDirectory = dirPath;
    }

	public boolean importStsFiles(ArrayList<StsVectorSetObject> vectorSetObjects)
	{
		try
		{
			// turn off redisplay
			model.disableDisplay();
			// panel.appendLine("Starting " + messageName + " loading...");

			// turn on the wait cursor
			StsCursor cursor = new StsCursor(model.win3d, Cursor.WAIT_CURSOR);

			int nSuccessfulObjects = vectorSetObjects.size();
			if(!model.getProject().addToProjectUnrotatedBoundingBoxes(vectorSetObjects, StsProject.TD_DEPTH)) return false;
		 /*
			panel.appendLine("Loading  " + messageName + " is complete. Press the Finish> button");

			if(nSuccessfulObjects == 0)
				panel.setDescriptionAndLevel("All " + messageName + " data sets failed to load.", StsProgressBar.ERROR);
			else if(nSuccessfulObjects < vectorSetObjects.size())
				panel.setDescriptionAndLevel("Some  " + messageName + " set(s) failed to load.", StsProgressBar.WARNING);
			else
				panel.setDescriptionAndLevel("All " + messageName + " sets loaded successfully.", StsProgressBar.INFO);
			panel.finished();
         */
			cursor.restoreCursor();
			if(nSuccessfulObjects > 0)
			{
				StsProject project = model.getProject();
				project.addToProjectUnrotatedBoundingBoxes(vectorSetObjects, StsProject.TD_DEPTH);
				project.runCompleteLoading();
			}
			model.enableDisplay();
			model.win3dDisplay();

			//loadWizard.enableFinish();
			return true;
		}
		catch (Exception e)
		{
			//panel.appendLine("Failed to load  " + messageName + ".");
			//panel.appendLine("Error message: " + e.getMessage());
			StsException.outputWarningException(this, "run", e);
			return false;
		}
	}

    public ArrayList<StsVectorSetObject> constructObjects(StsLoadWizard wizard)
    {
        try
        {
			Class objectClass = getObjectClass();
			StsAbstractFile[] files = wizard.getFiles();
            int nFiles = files.length;
			showDetailedProgress = nFiles < 100;
            ArrayList<StsVectorSetObject> vectorSetObjectList = new ArrayList<StsVectorSetObject>();
            progressPanel.initialize(nFiles);
			int n = 0;
			boolean deleteStsData = wizard.getDeleteStsData();
			vectorSetLoader = getLoader(model, wizard, isSourceData, progressPanel);
			//vectorSetLoader.preprocessInputFiles(currentDirectory, files);
            for (StsAbstractFile file : files)
            {
                String objectName = file.name;

                // read and build a microseismic (if we don't already have it)
				boolean objectExists = model.getObjectWithName(objectClass, objectName) != null;
				if(objectExists)
				{
					if(!deleteStsData)
					{
						vectorSetLoader.appendLine("  Object " + objectName + " already exists. Will not delete/reload.");
						progressPanel.setDescriptionAndLevel("Microseismic: " + objectName + " already exists. Will not delete/reload...", StsProgressBar.INFO);
						continue;
					}
					else // object exists and we want to delete sts data which then requires a reload from ascii
					{
						StsObject object = model.getObjectWithName(objectClass, objectName);
						if(object == null)
						{
							vectorSetLoader.appendLine("  Failed to find/delete existing object " + objectName);
							vectorSetLoader.setDescriptionAndLevel("Object name: " + objectName + " Failed to find/delete...", StsProgressBar.WARNING);
							continue;
						}
					}
				}
				StsVectorSetObject vectorSetObject = constructObject(file, progressPanel, isSourceData);
				if (vectorSetObject != null)
				{
					if (showDetailedProgress)
						vectorSetLoader.appendLine("   Successfully processed " + file.format + " formatted " + file.group + ": " + file.name + "...");
					vectorSetObjectList.add(vectorSetObject);
					nLoaded++;
				}
				else if(showDetailedProgress)
					vectorSetLoader.appendLine("  Failed to process " + file.format + " formatted " + file.group + ": " + file.name + "...");

                progressPanel.setValue(++n);
                if(showDetailedProgress) vectorSetLoader.setDescription("Loaded " + file.group + " #" + nLoaded + " of " + nFiles);
            }
            return vectorSetObjectList;

        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsVectorSetImport.class, "constructWells", e);
            progressPanel.setDescriptionAndLevel("StsMicroseismicImport.constructMicroseismicSets() failed.\n", StsProgressBar.WARNING);
            return null;
        }
    }

	/** Override in concrete subclasses if subFiles need to be added to each file.  */
	protected void addSubFiles(StsAbstractFile[] files)
	{
	}

    public StsVectorSetObject constructObject(StsAbstractFile file, StsProgressPanel progressPanel, boolean isSourceData)
    {
        try
        {
            if (showDetailedProgress) vectorSetLoader.appendLine("Processing S2S formatted " + file.group + ": " + file.name + "...");
			if(!vectorSetLoader.loadFile(file, false, false, isSourceData)) return null;
			return vectorSetLoader.getVectorSetObject();
        }
        catch (Exception e)
        {
            StsException.outputException(StsToolkit.getSimpleClassname(this) + " failed.", e, StsException.WARNING);
            StsMessageFiles.logMessage("Unable to process file  " + file.filename);
            if (progressPanel != null) progressPanel.setDescriptionAndLevel("Unable to read " + file.group + " file(s).\n", StsProgressBar.ERROR);
            return null;
        }
    }
}
