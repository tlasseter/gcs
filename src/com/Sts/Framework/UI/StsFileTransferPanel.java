package com.Sts.Framework.UI;

import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

/**
 * <p>Title: S2S development</p>
 *
 * <p>Description: Integrated seismic to simulation software</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: S2S Systems LLC</p>
 *
 * @author TJLasseter
 * @version c62e
 */
public class StsFileTransferPanel extends StsGroupBox
{
	/** instance listening to changes on this panel */
	private StsAbstractFileSet availableFileSet;
	private StsAbstractFile[] availableFiles;
	private String currentDirectory = null;
	private StsAbstractFilenameFilter filenameFilter;
	private StsFileTransferObjectFaceNew listener;

	private StsJPanel directoryGroupBox = StsJPanel.addInsets();
    private StsJPanel controlGroupBox = new StsGroupBox();

	private StsJPanel transferPanel = StsJPanel.addInsets();

	private StsButton directoryBrowseButton = new StsButton("dir16x32", "Browse for SEGY volume directory.", this, "directoryBrowse");
	private StsStringFieldBean currentDirectoryBean = null;
	private StsBooleanFieldBean reloadCheckbox;
    private StsBooleanFieldBean archiveItCheckbox;
    private StsBooleanFieldBean overrideCheckbox;

	private StsButton addBtn = new StsButton("Add >", "Add selected file to right side.", this, "addFiles");
	private StsButton removeBtn = new StsButton("< Remove", "Removed selected file from right side.", this, "removeFiles");
	private StsButton addAllBtn = new StsButton("Add all >", "Add all files to right side.", this, "addAllFiles");
	private StsButton removeAllBtn = new StsButton("< Remove all", "Remove all files from right side.", this, "removeAllFiles");

	private StsJPanel selectFilesPanel = StsJPanel.addInsets();
	private JFileChooser chooseDirectory = null;

	private DefaultListModel availableListModel = new DefaultListModel();
	private DefaultListModel selectedListModel = new DefaultListModel();
	private JList availableList = new JList(availableListModel);
	private JList selectedList = new JList(selectedListModel);

	private JScrollPane availableScrollPane = new JScrollPane();
	private JScrollPane selectedScrollPane = new JScrollPane();

    public StsFileTransferPanel(String currentDirectory, StsAbstractFilenameFilter filenameFilter, StsFileTransferObjectFaceNew listener,
                                int width, int height, boolean singleSelect)
    {
        this(currentDirectory, filenameFilter, listener, width, height);
        if(singleSelect)
        {
            addAllBtn.setEnabled(false);
            removeAllBtn.setEnabled(false);
            availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
	}

	public StsFileTransferPanel(String currentDirectory, StsAbstractFilenameFilter filenameFilter, StsFileTransferObjectFaceNew listener, int width, int height)
	{
		this(currentDirectory, filenameFilter, listener);
		int buttonPanelWidth = 100;
		transferPanel.setPreferredSize(new Dimension(buttonPanelWidth, height));
		int panelWidth = (width-buttonPanelWidth)/2;
		availableScrollPane.getViewport().setPreferredSize(new Dimension(panelWidth, height));
		selectedScrollPane.getViewport().setPreferredSize(new Dimension(panelWidth, height));
	}

	public StsFileTransferPanel(String currentDirectory, StsAbstractFilenameFilter filenameFilter, StsFileTransferObjectFaceNew listener)
	{
		super("File Transfer Selection");
        this.currentDirectory = currentDirectory;
        currentDirectoryBean = new StsStringFieldBean(this, "currentDirectory", "Directory: ");

		this.filenameFilter = filenameFilter;
		this.listener = listener;
        setAvailableFiles();

        if(listener.hasDirectorySelection())
        {
            directoryGroupBox.gbc.fill = GridBagConstraints.NONE;
            directoryGroupBox.gbc.anchor = GridBagConstraints.WEST;
            directoryGroupBox.gbc.weightx = 0.0;
            directoryGroupBox.gbc.weighty = 0.0;
            directoryGroupBox.addToRow(directoryBrowseButton);

            directoryGroupBox.gbc.weightx = 1.0;
            directoryGroupBox.gbc.fill = GridBagConstraints.HORIZONTAL;
            directoryGroupBox.gbc.anchor = GridBagConstraints.EAST;
            currentDirectoryBean.setColumns(30);
            directoryGroupBox.addEndRow(currentDirectoryBean);

            controlGroupBox.gbc.anchor = GridBagConstraints.WEST;
            boolean addControl = false;
            if (listener.hasReloadButton())
            {
                addControl = true;
                reloadCheckbox = new StsBooleanFieldBean(listener, "reload", listener.getReload(), "Reload Original Files");
                if(listener.hasArchiveItButton()) // || listener.hasOverrideButton())
                    controlGroupBox.addToRow(reloadCheckbox);
                else
                    controlGroupBox.addEndRow(reloadCheckbox);
            }
            if (listener.hasArchiveItButton())
            {
                addControl = true;
                archiveItCheckbox = new StsBooleanFieldBean(listener, "archiveIt", listener.getArchiveIt(), "Archive Files");
                //if(listener.hasOverrideButton())
                //    controlGroupBox.addToRow(archiveItCheckbox);
                //else
                    controlGroupBox.addEndRow(archiveItCheckbox);
            }
			/*
            if (listener.hasOverrideButton())
            {
                addControl = true;
                overrideCheckbox = new StsBooleanFieldBean(listener, "overrideFilter", listener.getOverrideFilter(), "Override File Filter");
                if(listener.hasOverrideButton())
                    controlGroupBox.addToRow(overrideCheckbox);
                else
                    controlGroupBox.addEndRow(overrideCheckbox);
            }
            */
            if(addControl)
            {
                directoryGroupBox.gbc.gridwidth = 3;
                directoryGroupBox.gbc.anchor = GridBagConstraints.WEST;
                directoryGroupBox.gbc.fill = GridBagConstraints.HORIZONTAL;
                directoryGroupBox.addEndRow(controlGroupBox);
            }
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;
            addEndRow(directoryGroupBox);
        }
        transferPanel.gbc.weightx = 0.0;
		transferPanel.gbc.fill = GridBagConstraints.HORIZONTAL;
		transferPanel.add(addBtn);
		transferPanel.add(addAllBtn);
		transferPanel.add(removeBtn);
		transferPanel.add(removeAllBtn);

		availableScrollPane.getViewport().add(availableList, null);
		availableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		availableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		selectedScrollPane.getViewport().add(selectedList, null);
		selectedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		selectedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		selectFilesPanel.gbc.fill = GridBagConstraints.BOTH;
        selectFilesPanel.gbc.weighty = 1.0;
        selectFilesPanel.gbc.weightx = 0.5;
		selectFilesPanel.addToRow(availableScrollPane);
        selectFilesPanel.gbc.fill = GridBagConstraints.NONE;
        selectFilesPanel.gbc.weightx = 0.0;
		selectFilesPanel.addToRow(transferPanel);
        selectFilesPanel.gbc.fill = GridBagConstraints.BOTH;
        selectFilesPanel.gbc.weightx = 0.5;
		selectFilesPanel.addToRow(selectedScrollPane);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
		addEndRow(selectFilesPanel);

		availableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		availableList.addListSelectionListener
		(
		    new ListSelectionListener()
	        {
				public void valueChanged(ListSelectionEvent e)
				{
					availableVolSelected(e);
				}
			}
		);		
		selectedList.addListSelectionListener
		(
		    new ListSelectionListener()
	        {
				public void valueChanged(ListSelectionEvent e)
				{
					selectedVolSelected(e);
				}
			}
		);
	}
	
	public void availableVolSelected(ListSelectionEvent e)
	{
		Object source = e.getSource();
		if(!(source instanceof JList)) return;
        StsAbstractFile availableFile = (StsAbstractFile)availableList.getSelectedValue();
		if(availableFile == null) return;
		listener.availableFileSelected(availableFile);
	}
	
	public void selectedVolSelected(ListSelectionEvent e)
	{
		Object source = e.getSource();
		if(!(source instanceof JList)) return;
        StsAbstractFile selectedFile = (StsAbstractFile)selectedList.getSelectedValue();
		if(selectedFile == null) return;
		listener.fileSelected(selectedFile);
	}

	public void selectSingleVolProgrammatically(String selectedString)
	{
		selectedList.setSelectedIndex(selectedListModel.indexOf(selectedString));
	}

	public void selectSingleVolProgrammatically(int index)
	{
		selectedList.setSelectedIndex(index);
	}

	public int getSelectedCount()
	{
		return selectedListModel.size();
	}

    public int[] getSelectedIndices() { return selectedList.getSelectedIndices(); }

    public void setFilenameFilter(StsAbstractFilenameFilter filter)
    {
        filenameFilter = filter;
    }
    
	public void setAvailableFiles()
	{
        availableListModel.clear();
		availableFileSet = StsFileSet.constructor(currentDirectory, filenameFilter);
		availableFiles = availableFileSet.getFiles();
		for(int n = 0; n < availableFiles.length; n++)
        {
            availableListModel.addElement(availableFiles[n]);
        }
    }

	public void directoryBrowse()
	{
		int[] selectedIndices = null;

		if(chooseDirectory == null) initializeChooseDirectory();

		chooseDirectory = new JFileChooser(currentDirectory);
		chooseDirectory.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooseDirectory.setDialogTitle("Select or Enter Desired Directory and Press Open");
		chooseDirectory.setApproveButtonText("Open Directory");
		while(true)
		{
			int retVal = chooseDirectory.showOpenDialog(null);
			if(retVal != chooseDirectory.APPROVE_OPTION)
				break;
			File newDirectory = chooseDirectory.getSelectedFile();
			if(newDirectory == null) return;
			if(newDirectory.isDirectory())
			{
				setCurrentDirectory(newDirectory.getAbsolutePath());
				break;
			}
			else
			{
				// File or nothing selected, strip off file name
				String dirString = newDirectory.getPath();
				newDirectory = new File(dirString.substring(0, dirString.lastIndexOf(File.separator)));
				if(newDirectory.isDirectory())
				{
					setCurrentDirectory(newDirectory.getAbsolutePath());
					break;
				}
				if(!StsYesNoDialog.questionValue(this,
											 "Must select the directory that\n contains the SegY Files.\n\n Continue?"))
					break;
			}
		}
	}

	private void initializeChooseDirectory()
	{
		chooseDirectory = new JFileChooser(currentDirectory);
		chooseDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}

	public void setCurrentDirectory(String directory)
	{
        if(directory == null) return;
		currentDirectory = directory;
		if(currentDirectoryBean != null)
            currentDirectoryBean.setValue(currentDirectory);
		setAvailableFiles();
	}

	public String getCurrentDirectory()
	{
		return currentDirectory;
	}

	public void addFiles()
	{
		int[] selectedIndices = availableList.getSelectedIndices();
        if(selectedIndices.length == 0)
        {
            new StsMessage(this, StsMessage.INFO,  "No files selected.");
            return;
        }
        StsAbstractFile[] addedFiles = new StsAbstractFile[selectedIndices.length];
		for (int i = 0; i < selectedIndices.length; i++)
			addedFiles[i] = (StsAbstractFile)availableListModel.getElementAt(selectedIndices[i]);
		for (int i = 0; i < selectedIndices.length; i++)
		{
			selectedListModel.addElement(addedFiles[i]);
			availableListModel.removeElement(addedFiles[i]);
		}
		listener.addFiles(addedFiles);
	}

    public void moveToAvailableList()
    {
        int[] selectedIndices = selectedList.getSelectedIndices();
        String[] removedStrings = new String[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++)
            removedStrings[i] = (String)selectedListModel.getElementAt(selectedIndices[i]);
        for(int i = 0; i < selectedIndices.length; i++)
        {
            selectedListModel.removeElement(removedStrings[i]);
            availableListModel.addElement(removedStrings[i]);
        }
        if (selectedListModel.size() > 0) selectedList.setSelectedIndex(0);
    }

    public void removeFiles()
    {
        int[] selectedIndices = selectedList.getSelectedIndices();
        StsAbstractFile[] removedFiles = new StsAbstractFile[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++)
            removedFiles[i] = (StsAbstractFile)selectedListModel.getElementAt(selectedIndices[i]);
        for(int i = 0; i < selectedIndices.length; i++)
        {
            selectedListModel.removeElement(removedFiles[i]);
            availableListModel.addElement(removedFiles[i]);
        }
        if (selectedListModel.size() > 0) selectedList.setSelectedIndex(0);
        listener.removeFiles(removedFiles);
    }

	public void addAllFiles()
	{
		int nFiles = availableListModel.size();
        Object[] availableFiles = availableListModel.toArray();
        StsAbstractFile[] addedFiles  = (StsAbstractFile[])availableFileSet.getFiles();
        availableListModel.clear();
        for(int n = 0; n < nFiles; n++)
            selectedListModel.addElement(availableFiles[n]);
        listener.addFiles(addedFiles);        
	}

	public void removeAllFiles()
	{
		int nFiles = selectedListModel.size();
		Object[] selectedFiles = selectedListModel.toArray();
		for(int i = 0; i < nFiles; i++)
		{
			String removedFile = (String)selectedFiles[i];
			availableListModel.addElement(removedFile);
		}
		selectedListModel.clear();
		listener.removeAllFiles();
	}

	public void removeFile(StsAbstractFile file)
	{
	    availableListModel.addElement(file);
		selectedListModel.removeElement(file);
    }

	public void removeSelectedFiles()
	{
		selectedListModel.clear();
		listener.removeAllFiles();
	}

	public void addAvailableFile(StsAbstractFile file)
	{
		availableListModel.addElement(file);
	}

    public void refreshAvailableList()
    {
        setAvailableFiles();
    }

	public static void main(String[] args)
	{
		String[] filterStrings = new String[] {"sgy", "segy", "Segy", "SegY"};
		StsFilenameSuffixFilter filter = new StsFilenameSuffixFilter (filterStrings);
		String currentDirectory = System.getProperty("user.dirNo");
		TransferPanelTest panelTest = new TransferPanelTest();
		try{ UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel()); } catch(Exception e) { }
		StsFileTransferPanel panel = new StsFileTransferPanel(currentDirectory, filter, panelTest);
//		panel.setForeground(Color.gray);
//		panel.setFont(new java.awt.Font("Dialog", 1, 11));
		StsToolkit.createDialog(panel);
	}
}

class TransferPanelTest implements StsFileTransferObjectFaceNew
{
	public TransferPanelTest()
	{
	}
    
    public void availableFileSelected(StsAbstractFile selectedFile)
    {
    }
	public void addFiles(StsAbstractFile[] files)
	{
	}
	public void removeFiles(StsAbstractFile[] files)
	{
	}
	public void removeAllFiles()
	{
	}

	public void fileSelected(StsAbstractFile selectedFile)
	{
	}
    public boolean hasDirectorySelection() { return true;  }
	public boolean hasReloadButton() { return true; }
    public boolean hasArchiveItButton() { return true; }
    public boolean hasOverrideButton() { return true; }

	public void setReload(boolean reload) { }
	public boolean getReload() { return true; }
    public void setArchiveIt(boolean archiveIt) { }
	public boolean getArchiveIt() { return true; }
    public void setOverrideFilter(boolean override) { }
	public boolean getOverrideFilter() { return true; }
}
