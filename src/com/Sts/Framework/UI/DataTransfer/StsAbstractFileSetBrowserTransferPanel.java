package com.Sts.Framework.UI.DataTransfer;

import com.Sts.Framework.IO.*;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jan 14, 2010
 * Time: 8:26:08 AM
 * To change this template use File | Settings | File Templates.
 */

/** An abstractFileset is either a directory containing a set of files which satisfy a filenameFilter or a set of files in an StsJar file
 *  or in an StsWebStartJar file. This transfer panel allows the user to select the files s/he wishes to load.
 */
public class StsAbstractFileSetBrowserTransferPanel extends StsDirectoryBrowserObjectsTransferPanel
{
    StsAbstractFileSet fileset;

    public StsAbstractFileSetBrowserTransferPanel(String title, String currentDirectory, StsObjectTransferListener listener, int width, int height)
    {
        super.initialize(title, currentDirectory, listener, width, height);
    }

    public StsAbstractFile[] getAvailableObjects()
    {
        return fileset.getFiles();
    }

    public void setDirectory(String directory)
    {
		this.directory = directory;
        if(fileset == null) fileset = StsFileSet.constructor(directory);
        fileset.setDirectory(directory);
		directoryBrowser.initializeDirectory(directory);
        initializeSetAvailableObjects();
    }

    public StsAbstractFile[] initializeAvailableObjects()
    {
        if(fileset == null) return null;
        if(!fileset.addFiles(directory)) return null;
		return fileset.getFiles();
    }
    
    public static void main(String[] args)
    {
        TestAbstractFileSetTransferPanelListener listener = new TestAbstractFileSetTransferPanelListener();
        String currentDirectory = "G:\\FractureAnalysis\\Surfaces";
        //StsFilenameFilter filenameFilter = new StsFilenameFilter(StsSurface.seismicGrp, StsLoader.FORMAT_TXT);
        //StsFileSet fileset = StsFileSet.constructor(currentDirectory, filenameFilter);
        try
        {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        }
        catch (Exception e) { }
        //TestAbstractFileSetBrowserTransferPanel panel = new TestAbstractFileSetBrowserTransferPanel(currentDirectory, listener, 400, 100, fileset);
        //com.Sts.Framework.Utilities.StsToolkit.createDialog(panel);
    }
}

class TestAbstractFileSetBrowserTransferPanel extends StsAbstractFileSetBrowserTransferPanel
{
    TestAbstractFileSetBrowserTransferPanel(String currentDirectory, TestAbstractFileSetTransferPanelListener listener, int width, int height, StsAbstractFileSet fileset)
    {
        super("Test Directory Objects Panel", currentDirectory, listener, width, height);
    }
}

class TestAbstractFileSetTransferPanelListener implements StsObjectTransferListener
{

    TestAbstractFileSetTransferPanelListener()
    {
    }

    public void addObjects(Object[] objects)
    {
        System.out.println("add Objects:");
        printObjects(objects);
    }

    private void printObjects(Object[] objects)
    {
        for (int n = 0; n < objects.length; n++)
            System.out.println("    " + objects[n].toString());
    }

    public void removeObjects(Object[] objects)
    {
        System.out.println("remove Objects:");
        printObjects(objects);
    }
    
    public void objectSelected(Object selectedObject)
    {
        System.out.println("selected Object:" + selectedObject.toString());
    }

    public boolean addAvailableObjectOk(Object object) { return true; }
}

