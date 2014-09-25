package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2011</p>
 * <p>Company: S2S Systems</p>
 * @author T.J.Lasseter
 * @version 1.0
 */

public class StsSourceSelectPanel extends StsJPanel
{
	StsProject project;
	String sourceKeyword;
	StsLoaderDataDirectorySet dataDirectorySet;
//	private String dataSelection = SOURCE_DATA;
	private String sourceType;
	private String sourceDirectory;
	protected String[] sourceTypes;

	private String dataType;
	private String dataDirectory;
	protected String[] dataTypes;

	private boolean deleteStsData = false;

	transient private StsSourceSelectStep wizardStep;

	StsJPanel panel = new StsJPanel();
	StsGroupBox inputBox = new StsGroupBox("Source Data");
	StsComboBoxFieldBean sourceTypesBean;
	StsComboBoxFieldBean sourceDirectoryBean;
	StsButtonFieldBean addInputDataButtonBean;

	StsGroupBox outputBox = new StsGroupBox("Processed Data");
	StsBooleanFieldBean deleteStsDataBean;
	StsComboBoxFieldBean dataTypesBean;
	StsComboBoxFieldBean dataDirectoryBean;
	StsButtonFieldBean addDataDirectoryButtonBean;

	static final String S2S = StsLoaderDataDirectorySets.S2S;
	static final String GC_FILES = StsLoaderDataDirectorySets.GC_FILES;
	static final String GC_SERVERS = StsLoaderDataDirectorySets.GC_SERVERS;

	public StsSourceSelectPanel(StsWizard wizard, StsWizardStep wizardStep, String sourceKeyword, String[] sourceTypes, String[] dataTypes)
	{
		project = wizard.model.getProject();
		this.wizardStep = (StsSourceSelectStep) wizardStep;
		this.sourceKeyword = sourceKeyword;
		this.sourceTypes = sourceTypes;
		if(sourceTypes != null)
			sourceType = sourceTypes[0];
		this.dataTypes = dataTypes;
		if(dataTypes != null)
			dataType = dataTypes[0];
		dataDirectorySet = project.getDataDirectories(sourceKeyword, sourceTypes, dataTypes);
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

	private void initialize()
	{
		sourceDirectory = dataDirectorySet.getFirstSourceDirectory();
		dataDirectory = dataDirectorySet.getFirstDataDirectory();
	}

	private void initializeBeans()
	{
		deleteStsDataBean = new StsBooleanFieldBean(this, "deleteStsData", "Delete Object DB Files");
		deleteStsDataBean.setToolTipText(StsLoader.stsDataActionToolTip);
		if(sourceTypes != null)
		{
			sourceTypesBean = new StsComboBoxFieldBean(this, "sourceType", sourceTypes);
			sourceDirectoryBean = new StsComboBoxFieldBean(this, "sourceDirectory", null, "inputDirectories");
			addInputDataButtonBean = new StsButtonFieldBean("New", "Select directory containing source files.", this, "addSourceDirectory");
		}
		if(dataTypes != null)
		{
			dataTypesBean = new StsComboBoxFieldBean(this, "dataType", null, dataTypes);
			dataDirectoryBean = new StsComboBoxFieldBean(this, "dataDirectory", null, "dataDirectories");
			addDataDirectoryButtonBean = new StsButtonFieldBean("New", "Select directory or file-system containing S2S processed files.", this, "addDataDirectory");
		}
	}
/*
	public void initialize()
	{
		setDataSelection(SOURCE_DATA);
		sourceDirectory = dataDirectorySet.getFirstInputDataDirectory();
		if(dataTypes != null)
			dataDirectory = dataDirectorySet.getFirstOutputDataDirectory();
		//initializeStsDataDirectory();
		//initializeSourceDataDirectory();
	}
*/
	private void initializeStsDataDirectory()
	{
		if(dataDirectorySet == null) return;
			dataDirectory = dataDirectorySet.getFirstDataDirectory();
		return;
		//if(dataType == GC_FILES || dataType == GC_SERVERS)
		//	dataDirectory = project.getStsDataDirString();
		//dataDirectorySet.addOutputTypeDirectory(dataType, dataDirectory);

		//outputStringBean.setItemsFromBeanObject();
	/*
		if(sourceDirectorySet != null)
			outputString = sourceDirectorySet.getFirstOutputDataDirectory();
		if(outputString == null)
			outputString = project.getClassStsDataDirectoryPathname(sourceKeyword);
	*/
	}

	private void initializeSourceDataDirectory()
	{
		if(dataDirectorySet == null) return;
			sourceDirectory = dataDirectorySet.getFirstSourceDirectory();
		return;
	}

	void constructPanel() throws Exception
	{
		this.gbc.fill = gbc.HORIZONTAL;
		this.add(panel);

		if(sourceTypes != null)
		{
			inputBox.gbc.anchor = gbc.WEST;
			inputBox.gbc.fill = gbc.NONE;
			inputBox.add(sourceTypesBean);
			inputBox.addToRow(sourceDirectoryBean);
			inputBox.addEndRow(addInputDataButtonBean);

			panel.add(inputBox);

			if(dataTypes != null)
			{
				panel.add(Box.createVerticalStrut(10));
				panel.add(deleteStsDataBean);
				panel.add(Box.createVerticalStrut(10));
			}
		}

		if(dataTypes != null)
		{
			outputBox.gbc.anchor = gbc.WEST;
			outputBox.gbc.fill = gbc.NONE;
			outputBox.add(dataTypesBean);
			outputBox.addToRow(dataDirectoryBean);
			outputBox.addEndRow(addDataDirectoryButtonBean);

			panel.add(outputBox);
		}
	}

 	public String directoryBrowse(String currentDirectory, String ioType)
	{
		if(ioType == S2S || ioType == GC_FILES)
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
		else if(ioType == GC_SERVERS)
		{
			GcServerObject serverObject = new GcServerObject();
			StsOkApplyCancelDialog dialog = new StsOkApplyCancelDialog(wizardStep.wizard.frame, serverObject, "Define Server", true);
			String host = serverObject.getHost();
			String directory = serverObject.getDirectory();
			return "file://" + host + "/" + directory + "/";
		}
		else
			return null;
	}

	public String getDataType()
	{
		return dataType;
	}

	public void setDataType(String dataType)
	{
		this.dataType = dataType;
	}

	public String getSourceType()
	{
		return sourceType;
	}

	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}

	public void setSourceDirectory(String sourceDirectory)
	{
		this.sourceDirectory = sourceDirectory;
	}

	public void setDataDirectory(String dataDirectory)
	{
		this.dataDirectory = dataDirectory;
	}

	class GcServerObject implements StsDialogFace
	{
		String host = "127.0.0.1";
		String directory = "G:/Q";
		StsStringFieldBean hostBean = new StsStringFieldBean(this, "host", "Server URL");
		StsStringFieldBean directoryBean = new StsStringFieldBean(this, "directory", "Server directory");

		public GcServerObject()
		{
		}

		public StsDialogFace getEditableCopy()
		{
			return this;
		}

		public void dialogSelectionType(int type)
		{
			System.out.println("Selection Type " + type);
		}
		public Component getPanel(boolean val) { return getPanel(); }
		public Component getPanel()
		{
			StsGroupBox groupBox = new StsGroupBox("Define Server Source Directory");
			groupBox.add(hostBean);
			groupBox.add(directoryBean);
			return groupBox;
		}

		public void setHost(String s) { host = s; }
		public String getHost() { return host; }


		public void setDirectory(String s) { directory = s; }
		public String getDirectory() { return directory; }
	}

	public void addDataDirectory()
	{
		if(dataDirectory == null)
			initializeStsDataDirectory();
		String currentDirectory = dataDirectory;
		currentDirectory = directoryBrowse(currentDirectory, dataType);
		if(currentDirectory == null) return;
		// use JSE 7.0 path comparison instead when available
		if(dataDirectory.equals(currentDirectory)) return;
		dataDirectory = currentDirectory;
		//sourceDirectorySet.dataDataDirectoryDirectory(dataDirectory);
		dataTypesBean.setValueFromPanelObject(this);
	}

	public void addSourceDirectory()
	{
		if(sourceDirectory == null)
			initializeSourceDataDirectory();
		String currentString = sourceDirectory;
		// need to replace this with browser(s) for difference source types (network files, databases, etc)
		currentString = directoryBrowse(currentString, sourceType);
		if(currentString == null) return;
		// use JSE 7.0 path comparison instead when available
		if(sourceDirectory.equals(currentString)) return;
		sourceDirectory = currentString;
		dataDirectorySet.addSourceTypeDirectory(sourceType, sourceDirectory);
		sourceDirectoryBean.setValueFromPanelObject(this);
	}

	public byte getType()
	{
		return (byte) -1;
	}
/*
	public String getDataSelection()
	{
		return dataSelection;
	}

	public boolean isSourceData() { return dataSelection == SOURCE_DATA; }
*/
	public String getSourceDirectory()
	{
		return sourceDirectory;
	}

	public String[] getDataDirectories()
	{
		return dataDirectorySet.getDataDirectories(dataType);
	}

	public String getDataDirectory()
	{
		return dataDirectory;
	}

	public String[] getInputDirectories()
	{
		return dataDirectorySet.getSourceDirectories(sourceType);
	}
/*
	public void setDataSelection(String dataSelection)
	{
		if(this.dataSelection == dataSelection) return;
		this.dataSelection = dataSelection;
		enableProcessedData(dataSelection == PROCESSED_DATA);
	}

	private void enableProcessedData(boolean enable)
	{
		dataTypesBean.setEnabled(enable);
		dataDataDirectoryButtonBean.setEnabled(enable);
		sourceTypesBean.setEnabled(!enable);
		inputStringBean.setEnabled(!enable);
		addInputDataButtonBean.setEnabled(!enable);
	}
*/
/*
	public void setOutputString(String outputString)
	{
		this.outputString = outputString;
	}

	public String[] getOutputStrings()
	{
		String defaultStsDataDirectory = project.getClassStsDataDirectoryPathname(sourceKeyword);
		String[] userStsDataDirectories =  sourceDirectorySet.outputDataDirectories;
		int nUserDirectories = userStsDataDirectories.length;
		if(nUserDirectories == 0)
			return new String[] { defaultStsDataDirectory };
		String[] stsDataDirectories = new String[1 + userStsDataDirectories.length];
		stsDataDirectories[0] = defaultStsDataDirectory;
		System.arraycopy(userStsDataDirectories, 0, stsDataDirectories, 1, nUserDirectories);
		return stsDataDirectories;
	}

	public String getInputString()
	{
		return inputString;
	}

	public void setInputString(String inputString)
	{
		this.inputString = inputString;
	}

	public String[] getInputStrings()
	{
		return sourceDirectorySet.getSourceDataDirectories(sourceType);
	}

	public String getInputType()
	{
		return sourceType;
	}

	public void setInputType(String sourceType)
	{
		this.sourceType = sourceType;
	}
*/
	public boolean getDeleteStsData()
	{
		return deleteStsData;
	}

	public void setDeleteStsData(boolean delete)
	{
		this.deleteStsData = delete;
	}
}
