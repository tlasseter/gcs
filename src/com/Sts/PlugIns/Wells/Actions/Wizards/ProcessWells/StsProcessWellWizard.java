//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Wells.Actions.Wizards.ProcessWells;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.Actions.Wizards.LoadComponents.*;
import com.Sts.PlugIns.Wells.Actions.Wizards.LoadWells.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.DataTransfer.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.Actions.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class StsProcessWellWizard extends StsLoadWizard
{
	static final String GC_FILES = StsLoaderDataDirectorySets.GC_FILES;
	static final String GC_SERVERS = StsLoaderDataDirectorySets.GC_SERVERS;

    public StsProcessWellWizard(StsActionManager actionManager)
    {
        super(actionManager, 600, 700);
    }

	public StsProcessWellWizard(StsActionManager actionManager, int width, int height)
    {
        super(actionManager, width, height);
	}

	public void initializeNamesAndFilenameFilter()
	{
		typeName = StsLoader.GROUP_WELL;
		groups = new String[]{StsLoader.GROUP_WELL, StsLoader.GROUP_WELL_DEV};
		formats = new String[]{StsLoader.FORMAT_DIRS, StsLoader.FORMAT_DIR, StsLoader.FORMAT_TXT};
		filenameFilter = new StsFilenameFilter(groups, formats);
	}

	public StsWellImport getImporter(StsModel model, StsProgressPanel panel)
	{
		return new StsWellImport(model, this, panel);
	}

	protected StsSourceSelectStep initializeSourceSelectStep()
	{
		return new StsSourceSelectStep(this, StsWell.groupName, StsLoaderDataDirectorySets.wellInputTypes, StsLoaderDataDirectorySets.wellOutputTypes);
	}

	protected StsVectorSetFilesSelectStep initializeFilesSelectStep()
	{
		StsAbstractFilesSelectPanel panel = new StsLoadWellFilesSelectPanel(this, 600, 200);
		return new StsVectorSetFilesSelectStep(this, panel);
	}

	protected StsVectorSetFilesLoadStep initializeFilesLoadStep()
	{
		return new StsVectorSetFilesLoadStep(this);
	}

	protected void initializeSourceSelectStepHeader(StsSourceSelectStep sourceSelectStep)
	{
		StsHeaderPanel header = sourceSelectStep.header;
       	header.setTitle("Wells Source Selection");
       	header.setSubtitle("Select source type and URI.");
       	header.setLink("http://www.GeoCloudRealTime.com/Protected/Marketing/AppLinks.html#Wells");
       	header.setInfoText(sourceSelectStep.wizardDialog, "(1) Once complete, press the Next Button to advance to file selection.");
	}

	protected void initializeFilesSelectStepHeader(StsVectorSetFilesSelectStep filesSelectStep)
	{
		StsHeaderPanel header = filesSelectStep.header;
		header.setTitle("Well Selection");
        header.setSubtitle("Selecting Available Wells");
        header.setLink("http://www.GeoCloudRealTime.com/Protected/Marketing/AppLinks.html#Well");
        header.setInfoText(filesSelectStep.wizardDialog,"(1) Navigate to the directory containing the wells using the Dir button. \n" +
                           " **** All wells in supported formats in the selected directory will be placed in the left list.\n" +
                           " ****     S2S Format (md, deltaX, deltaY) + logs + markers + time-depth\n" +
                           " ****     LAS Format (md, deltaX, deltaY) + logs\n" +
                           " ****     UT Format (md, drift, azimuth)\n" +
                           " ****     WLS Geographix formatted files\n" +
                           " **** If not geographix, any combination of formats can be used as long as they adhere to the S2S naming convention\n" +
                           " ****     <type>.<format>.<name>.<version>\n" +
                           " ****     <type> = well, well-logs, well-ref, and well-td\n" +
                           " ****     <format> = txt, las, ut\n" +
                           " ****     <name> = User defined and must be same for all related files.\n" +
                           " ****     <version> = Optional integer to allow version control\n" +
                           "(3) Select the desired wells and place them in the right list using the provided controls\n " +
                           " **** Only one wls file can be selected and loaded at a time, any number of wells can be contained in the file.\n" +
                           "(4) Well file text can be viewed prior to loading by pressing the View File Button\n" +
                           "(5) Specify datum shift to apply to all read MD and Depth values. Must reload from ASCII to apply.\n" +
                           "(6) Once well selections are complete, press the Next>> Button.");
	}

	protected void initializeLoadStepHeader(StsVectorSetFilesLoadStep filesLoadStep)
	{
		StsHeaderPanel header = filesLoadStep.header;
		header.setTitle("Well Selection");
        header.setSubtitle("Load Well(s)");
        header.setLink("http://www.GeoCloudRealTime.com/Protected/Marketing/AppLinks.html#Well");
        header.setInfoText(filesLoadStep.wizardDialog, "(1) Once complete, press the Finish Button to dismiss the screen.");
	}

	public void initializeSelectedFiles()
	{
		File directoryFile = new File(sourceDirectory);
		TreeMap<String, ArrayList<StsAbstractFile>> fileMap = new TreeMap<String, ArrayList<StsAbstractFile>>();
		int nFiles = files.length;
		String[] names = new String[nFiles];

		// construct an ArrayList of subFiles keyed by masterFile name
		for(int n = 0; n < nFiles; n++)
		{
			StsAbstractFile file = files[n];
			filenameFilter.parseFile(file);
			String name = file.name;
			names[n] = name;
			fileMap.put(name, new ArrayList<StsAbstractFile>());
		}
		StsFilenameFilter subFileGroupsFilter = StsWell.getSubFileGroupsFilter();
		subFileGroupsFilter.setNames(names);
		String[] subFilenames = directoryFile.list(subFileGroupsFilter);
		if(subFilenames == null) return;
		int nSubFiles = subFilenames.length;
		if(nSubFiles == 0) return;
		Path directoryPath = directoryFile.toPath();
		for(String subFilename : subFilenames)
		{
			// resolve here combines the folder and filename paths
			Path subFilePath = directoryPath.resolve(Paths.get(subFilename));
			StsAbstractFile abstractSubfile = StsFile.constructor(subFilePath);
			String subfileName = abstractSubfile.name;
			ArrayList<StsAbstractFile> subFileArrayList = fileMap.get(subfileName);
			subFileArrayList.add(abstractSubfile);
		}
		for(int n = 0; n < nFiles; n++)
		{
			StsAbstractFile file = files[n];
			file.setSubFiles(fileMap.get(file.name));
		}
	}

    public boolean initialize()
        {
        if(steps == null || steps.size() == 0)
        {
            StsException.systemError("StsWizard.initialize() failed. No wizard steps.");
            return false;
        }
        return gotoFirstStep();
    }
/*
    public boolean initialize()
    {
        return true;
    }
*/
	public void next()
	{
		if(currentStep == sourceSelectStep)
		{
			isSourceData = true;
			deleteStsData = true;
			sourceDirectory = sourceSelectStep.panel.getSourceDirectory();
			dataDirectory = sourceSelectStep.panel.getDataDirectory();
			initializeNamesAndFilenameFilter();
			fileset = StsFileSet.constructor(sourceDirectory, filenameFilter);
			files = fileset.getFiles();
			filesSelectStep.panel.setAvailableFiles(files);
		}
		else if(currentStep == filesSelectStep)
		{
			StsAbstractFilesSelectPanel panel = filesSelectStep.panel;
			files = panel.getSelectedFiles();
			if(files == null)
			{
				new StsMessage(this.dialog, StsMessage.WARNING, "No files selected: select on left and add to right.");
				return;
			}
			// initializeSelectedFiles();
		}

		gotoNextStep();
	}

	/*
    public void next()
    {
		if(currentStep == fileType)
		{
			isSourceData = fileType.panel.isSourceData();
			deleteStsData = fileType.panel.getDeleteStsData();
			String inputDirectory = fileType.panel.getInputDirectory();
			selectWells.panel.initializeInputDirectory(inputDirectory);
		}
		else if(currentStep == selectWells)
		{
			StsWellSelectPanel panel = selectWells.panel;
			wellFiles = panel.getSelectedFiles();
			hUnits = panel.getHorzUnits();
			vUnits = panel.getVertUnits();
			tUnits = panel.getTimeUnits();
		}

		gotoNextStep();
    }
*/
    // public byte getFileType() {  return selectFileType.getType(); }
/*
    public boolean addFile(StsAbstractFile file)
    {
*/
		/*
        if(selectFileType.getType() == GEOGRAPHIX_WELLS)
        {
            if(wellFiles.length > 0)
            {
                new StsMessage(frame,StsMessage.WARNING,"Currently only able to load one GeoGraphix file at a time.");
                return false;
            }
        }
        */
/*
		constructSubFiles(file);
        files = (StsAbstractFile[]) StsMath.arrayAddElement(files, file);
        return true;
    }
*/
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
					StsModel model = StsModel.constructor("E:/Qclone/ClonedWell", "WellWizardTest");
					Main.model = model;
        			StsActionManager actionManager = new StsActionManager(model);
					actionManager.startAction(StsProcessWellWizard.class);
        			//StsWellWizard wellWizard = new StsWellWizard(actionManager);
        			//wellWizard.start();
				}
			}
		);
    }
}
