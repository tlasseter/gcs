package com.Sts.Framework.UI.DataTransfer;

import com.Sts.PlugIns.Wells.Actions.Loader.*;

import javax.swing.*;

/**
 * <p>Title: S2S development</p>
 * <p/>
 * <p>Description: Integrated seismic to simulation software</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: S2S Systems LLC</p>
 *
 * @author TJLasseter
 * @version c62e
 */


/**
 * This abstract panel provides a browse button for data directories.
 * The user uses the add, add-all to move files from the left list to the right,  and remove, remove-all buttons to move objects
 * from the right list to the left.
 * The StsObjectTransferListener (interface) can be provided which listens to events as objects are added and removed
 * from the right-hand side list.
 */
abstract public class StsDirectoryObjectsTransferPanel extends StsObjectsTransferPanel implements StsDirectorySelectListener
{
	String directory = null;
    /**
     * This method is called when the browseDatastore button is pushed.
     * The implementation should do whatever is necessary to find the datastore the user is looking for.
     * It could be a fileBrower, or databaseConnector and might involve bringing up a dialog box or whatever.
     *
     * @return datstore the user selects
     */

    public StsDirectoryObjectsTransferPanel()
    {
    }

    public void initialize(String title, String currentDirectory, StsObjectTransferListener listener, int width, int height)
    {
		setDirectory(currentDirectory);
		this.listener = listener;
        this.width = width;
        this.height = height;
        super.initialize(title, true);
        constructTransferPanel();
    }

    public void constructTransferPanel()
    {
//        directoryGroupBox = new StsDirectoryBrowseGroupBox(this, "currentDirectory", currentDirectory);
        gbc.fill = gbc.HORIZONTAL;
        super.constructTransferPanel();
    }

    public void initialize()
    {
        // setCurrentDirectory(currentDirectory);
    }

    public int getSelectedIndex() { return selectedList.getSelectedIndex(); }

    public String getDirectory()
    {
        return directory;
    }

    public void setDirectory(String directory)
    {
		this.directory = directory;
        initializeSetAvailableObjects();
    }

    public void initializeSetAvailableObjects()
    {
        Object[] availableObjects = initializeAvailableObjects();
        setAvailableObjects(availableObjects);    
    }

    public static void main(String[] args)
    {
        TestDirectoryFoldersTransferPanelListener listener = new TestDirectoryFoldersTransferPanelListener();
        String currentDirectory = "c:\\data\\FractureAnalysis\\Surfaces";
        try
        {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        }
        catch (Exception e) { }
        StsDirectoryBrowserFileTransferPanel panel = new StsDirectoryBrowserFileTransferPanel(currentDirectory, listener,
				StsWellDevLoader.masterFileParser, StsWellDevLoader.subFilesParser, StsWellDevLoader.folderParser, 400, 100);
        com.Sts.Framework.Utilities.StsToolkit.createDialog(panel);
    }
/*
    public static void main(String[] args)
    {
        TestDirectoryObjectsTransferPanelListener listener = new TestDirectoryObjectsTransferPanelListener();
        String currentDirectory = "c:\\data\\FractureAnalysis\\Surfaces";
        try
        {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        }
        catch (Exception e) { }
        TestDirectoryObjectsTransferPanel panel = new TestDirectoryObjectsTransferPanel(currentDirectory, listener, 400, 100);
        com.Sts.Framework.Utilities.StsToolkit.createDialog(panel);
    }
*/
}

