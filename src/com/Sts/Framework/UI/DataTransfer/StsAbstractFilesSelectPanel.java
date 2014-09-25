package com.Sts.Framework.UI.DataTransfer;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

abstract public class StsAbstractFilesSelectPanel extends StsJPanel implements StsObjectTransferListener
{
    protected StsLoadWizard wizard;
	protected StsModel model;
	protected StsProject project;
	private String currentDirectory = null;
    protected StsAbstractFile currentSelectedFile = null;

	protected StsAbstractFileSetTransferPanel selectionPanel;

    private StsButton viewButton = null;

	abstract public void addToPanel();

	public StsAbstractFilesSelectPanel(StsLoadWizard wizard, String title, int transferPanelWidth, int transferPanelHeight)
	{
        this.wizard = wizard;
		this.model = wizard.model;
		if(model != null) this.project = model.getProject();
        try
        {
			constructPanel(null, title, transferPanelWidth, transferPanelHeight);
        	wizard.rebuild();
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "constructor", e);
        }
    }

	protected String getCurrentDirectory()
	{
		if(project != null)
		{
			String currentDirectory = project.getFirstSourceDataDirectory(StsWell.groupName, StsLoaderDataDirectorySets.wellInputTypes, StsLoaderDataDirectorySets.wellOutputTypes, StsLoaderDataDirectorySets.GC_FILES);
			return checkGetCurrentDirectory(currentDirectory);
		}
		else
			return System.getProperty("user.dirNo"); // standalone testing
	}

	protected String checkGetCurrentDirectory(String currentDirectory)
	{
		if(currentDirectory == null || !StsAbstractFile.directoryExists(currentDirectory))
			currentDirectory = project.getProjectDirString();
		return currentDirectory;
	}

    protected void constructPanel(String directory, String title, int transferPanelWidth, int transferPanelHeight)
    {
		if(directory != null)
			currentDirectory = directory;
	/*
		else
		{
			if (model == null)
				currentDirectory = System.getProperty("user.dirNo"); // standalone testing
			else
				currentDirectory = model.getProject().getProjectDirString();
		}
    */
        //fileset = initializeFileSet();
		selectionPanel = new StsAbstractFileSetTransferPanel(title, currentDirectory, this, transferPanelWidth, transferPanelHeight);

		viewButton = new StsButton("View Selected File", "Review selected file contents", this, "showFile");

        gbc.fill = gbc.BOTH;
        gbc.anchor = gbc.NORTH;
        gbc.weighty = 1.0;
        addEndRow(selectionPanel);

        gbc.weighty = 0.0;
        gbc.fill = gbc.NONE;
        gbc.anchor = gbc.CENTER;
        addEndRow(viewButton);

		addToPanel();
    }

    public void initialize(String currentDirectory)
    {
		this.currentDirectory = currentDirectory;
		if(currentDirectory == null)
		{
			if(model != null)
				currentDirectory = wizard.getModel().getProject().getProjectDirString();
			else
				currentDirectory = System.getProperty("user.dirNo"); // standalone testing
		}
        selectionPanel.setDirectory(currentDirectory);
        wizard.enableNext(false);
    }

	public void setAvailableFiles(StsAbstractFile[] files)
	{
		selectionPanel.setAvailableObjects(files);
	}
 /*
    private StsAbstractFileSet initializeFileSet()
    {
        // Get files from a webstart jar
        if(Main.isWebStart && Main.isJarFile)
             return StsWebStartJar.constructor(StsMicroseismic.jarFilename);
         // Load from jar files
         else if(Main.isJarFile)
             return StsJar.constructor(currentDirectory, StsMicroseismic.jarFilename);
         // Load from ASCII/Binary files
         else
            return StsFileSet.constructor(currentDirectory, StsTimeVectorSetLoader.fileParser);
    }
  */
	//TODO this could display info about the selected object (first?) in the wizardStep.infoPanel
    public void addObjects(Object[] objects)
    {
        System.out.println("add Objects:");
        printObjects(objects);
        wizard.enableNext(selectionPanel.getNSelectedObjects() > 0);
    }

	//TODO this could display info about the selected object (first?) in the wizardStep.infoPanel
	private void printObjects(Object[] objects)
	{
		for (int n = 0; n < objects.length; n++)
			System.out.println("    " + objects[n].toString());
	}

	public void removeObjects(Object[] objects)
	{
		System.out.println("remove Objects:");
		printObjects(objects);
		wizard.enableNext(selectionPanel.getNSelectedObjects() > 0);
	}

	//TODO this could display info about the selected object (first?) in the wizardStep.infoPanel
	public void objectSelected(Object selectedObject)
	{
		System.out.println("selected Object:" + selectedObject.toString());
	}

    public void showFile()
    {
        String line;
        if(currentSelectedFile == null) return;
        StsAsciiFile selectedFile = new StsAsciiFile(currentSelectedFile);
        if(selectedFile == null)
        {
            new StsMessage(wizard.getModel().win3d, StsMessage.WARNING, "No file selected in list, must be in selected (left) list");
            return;
        }
        if(!selectedFile.openReadWithErrorMessage()) return;

        StsFileViewDialog dialog = new StsFileViewDialog(wizard.frame,"Well File View", false);
        dialog.setVisible(true);
        dialog.setViewTitle("File - " + currentSelectedFile.getFilename());
        try {
            while (true)
            {
                line = selectedFile.readLine();
                if (line == null)
                    break;
                dialog.appendLine(line);
            }
        }
        catch (Exception ex)
        {
            StsMessageFiles.infoMessage("Unable to view selected file.");
        }
        finally
        {
            selectedFile.close();
        }
    }

	public StsAbstractFile[] getSelectedFiles()
	{
		Object[] selectedObjects = selectionPanel.getSelectedObjects();
		if(selectedObjects == null) return null;
		int nSelectedObjects = selectedObjects.length;
		StsAbstractFile[] selectedFiles = new StsAbstractFile[nSelectedObjects];
		for(int n = 0; n < nSelectedObjects; n++)
			selectedFiles[n] = (StsAbstractFile)selectedObjects[n];
		return selectedFiles;
	}

    public void fileSelected(StsAbstractFile selectedFile)
    {
        if(selectedFile == null)
        {
            currentSelectedFile = null;
        }
        if (selectedFile != currentSelectedFile)
        {
            currentSelectedFile = selectedFile;
        }
    }
}
