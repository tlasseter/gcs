package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;

import javax.swing.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2011</p>
 * <p>Company: S2S Systems</p>
 * @author T.J.Lasseter
 * @version 1.0
 */

public class StsFileTypePanel extends StsJPanel
{
	StsProject project;
	StsLoaderDataDirectorySet directorySet;
	private String dataSelection = SOURCE_DATA;
	private boolean sourceData = true;
	private String stsDataDirectory;
	private String sourceType = StsLoaderDataDirectorySets.GC_FILES;
	private String sourceString;
	private boolean deleteStsData = false;

	protected String[] stsDataDirectories;
	protected String[] sourceTypes;
	protected String[] sourceStrings;

	transient String type;
	transient private StsFileType wizardStep;

	StsGroupBox typeBox = new StsGroupBox("Select Data Source");
	ButtonGroup buttonGroup = new ButtonGroup();

	StsRadioButtonFieldBean sourceDataBean;
	StsComboBoxFieldBean sourceTypesBean;
	StsComboBoxFieldBean sourceStringBean;
	StsButtonFieldBean addSourceDataButtonBean;

	StsRadioButtonFieldBean stsDataBean;
	StsBooleanFieldBean deleteStsDataBean;
	StsComboBoxFieldBean stsDataDirBean;
	StsButtonFieldBean addStsDataButtonBean;

	static final String PROCESSED_DATA = "Sts Files DB";
	static final String SOURCE_DATA = "Source Data";

	public StsFileTypePanel(StsWizard wizard, StsWizardStep wizardStep, StsLoaderDataDirectorySet directorySet)
	{
		this.wizardStep = (StsFileType) wizardStep;
		project = wizard.model.getProject();
		this.directorySet = directorySet;
		initialize();
		initializeBeans();

		try
		{
			constructPanel();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	private void initializeBeans()
	{
		sourceDataBean = new StsRadioButtonFieldBean(this, "dataSelection", SOURCE_DATA, buttonGroup);
		sourceDataBean.setSelected(true);
		deleteStsDataBean = new StsBooleanFieldBean(this, "deleteStsData", "Delete Object DB Files");
		deleteStsDataBean.setToolTipText(StsLoader.stsDataActionToolTip);
		sourceTypesBean = new StsComboBoxFieldBean(this, "sourceType", directorySet.sourceTypes);
		sourceStringBean = new StsComboBoxFieldBean(this, "sourceString", null, "sourceStrings");
		addSourceDataButtonBean = new StsButtonFieldBean("New", "Select directory containing source files.", this, "addSourceData");

		stsDataBean = new StsRadioButtonFieldBean(this, "dataSelection", PROCESSED_DATA, buttonGroup);
		stsDataBean.setSelected(false);
		stsDataDirBean = new StsComboBoxFieldBean(this, "stsDataDirectory", null, "stsDataDirectories");
		addStsDataButtonBean = new StsButtonFieldBean("New", "Select directory containing S2S processed files.", this, "addStsData");
	}

	public void initialize()
	{
		setDataSelection(SOURCE_DATA);
		initializeStsDataDirectory();
		initializeSourceDataDirectory();
	}

	private void initializeStsDataDirectory()
	{
	/*
		if(directorySet != null)
			stsDataDirectory = directorySet.getFirstOutputDataDirectory();
		if(stsDataDirectory == null) stsDataDirectory = project.getProjectDirString();
	*/
	}

	private void initializeSourceDataDirectory()
	{
		if(directorySet != null)
			sourceString = directorySet.getFirstSourceDirectory();
		if(sourceString == null) sourceString = project.getProjectDirString();

	}

	void constructPanel() throws Exception
	{
		this.gbc.fill = gbc.NONE;
		add(typeBox);
		typeBox.gbc.anchor = gbc.WEST;
		typeBox.gbc.fill = gbc.NONE;

		typeBox.add(sourceDataBean);
		typeBox.add(sourceTypesBean);
		typeBox.addToRow(sourceStringBean);
		typeBox.addEndRow(addSourceDataButtonBean);
		typeBox.add(deleteStsDataBean);
		typeBox.add(stsDataBean);
		typeBox.addToRow(stsDataDirBean);
		typeBox.addEndRow(addStsDataButtonBean);
	}

 	public String directoryBrowse(String currentDirectory)
	{
		StsFileChooser chooseDirectory = new StsFileChooser(wizardStep.model.win3d, "Choose source directory", currentDirectory);
		chooseDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		chooseDirectory.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooseDirectory.setDialogTitle("Select or Enter Desired Directory and Press Open");
		chooseDirectory.setApproveButtonText("Open Directory");
		while(true)
		{
			int retVal = chooseDirectory.showOpenDialog(null);
			if(retVal != chooseDirectory.APPROVE_OPTION)
				return null;
			File newDirectory = chooseDirectory.getSelectedFile();
			if(newDirectory == null) return currentDirectory;
			if(newDirectory.isDirectory())
			{
				return newDirectory.getAbsolutePath();
			}
			else
			{
				// File or nothing selected, strip off file name
				String dirString = newDirectory.getPath();
				newDirectory = new File(dirString.substring(0, dirString.lastIndexOf(File.separator)));
				if(newDirectory.isDirectory())
				{
					return newDirectory.getAbsolutePath();
				}
				if(!StsYesNoDialog.questionValue(this,
						"Must select the directory that\n contains the desired files.\n\n Continue?"))
					return null;
			}
		}
	}

	public void addStsData()
	{
		if(stsDataDirectory == null)
			initializeStsDataDirectory();
		String currentDirectory = stsDataDirectory;
		currentDirectory = directoryBrowse(currentDirectory);
		if(currentDirectory == null) return;
		// use JSE 7.0 path comparison instead when available
		if(stsDataDirectory.equals(currentDirectory)) return;
		stsDataDirectory = currentDirectory;
		//directorySet.addOutputDataDirectory(stsDataDirectory);
		stsDataDirBean.setValueFromPanelObject(this);
	}

	public void addSourceData()
	{
		if(sourceString == null)
			initializeSourceDataDirectory();
		String currentString = sourceString;
		// need to replace this with browser(s) for difference source types (network files, databases, etc)
		currentString = directoryBrowse(currentString);
		if(currentString == null) return;
		// use JSE 7.0 path comparison instead when available
		if(sourceString.equals(currentString)) return;
		sourceString = currentString;
		directorySet.addSourceTypeDirectory(sourceType, sourceString);
		sourceStringBean.setValueFromPanelObject(this);
	}

	public byte getType()
	{
		return (byte) -1;
	}

	public String getDataSelection()
	{
		return dataSelection;
	}

	public boolean isSourceData() { return dataSelection == SOURCE_DATA; }

	public String getInputDirectory()
	{
		if(isSourceData())
			return sourceString;
		else
			return stsDataDirectory;
	}

	public void setDataSelection(String dataSelection)
	{
		if(this.dataSelection == dataSelection) return;
		this.dataSelection = dataSelection;
		enableProcessedData(dataSelection == PROCESSED_DATA);
	}

	private void enableProcessedData(boolean enable)
	{
		stsDataDirBean.setEnabled(enable);
		addStsDataButtonBean.setEnabled(enable);
		sourceTypesBean.setEnabled(!enable);
		sourceStringBean.setEnabled(!enable);
		addSourceDataButtonBean.setEnabled(!enable);
	}
/*
	public String getStsDataDirectory()
	{
		return stsDataDirectory;
	}

	public void setStsDataDirectory(String stsDataDirectory)
	{
		this.stsDataDirectory = stsDataDirectory;
	}

	public String[] getStsDataDirectories()
	{
		return directorySet.outputDataDirectories;
	}

	public String getSourceString()
	{
		return sourceString;
	}

	public void setSourceString(String sourceString)
	{
		this.sourceString = sourceString;
	}

	public String[] getSourceStrings()
	{
		return directorySet.getSourceDataDirectories(sourceType);
	}

	public String getSourceType()
	{
		return sourceType;
	}

	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}

	public boolean getDeleteStsData()
	{
		return deleteStsData;
	}

	public void setDeleteStsData(boolean delete)
	{
		this.deleteStsData = delete;
	}
*/
}
