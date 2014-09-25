package com.Sts.Framework.UI.DataTransfer;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;

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
public class StsAbstractFileSetGraphicBrowserTransferPanel extends StsAbstractFileSetBrowserTransferPanel
{
	StsIntFieldBean fileCountBean;
	StsButton displaySelectionButton;
	StsGraphicObjectTransferListener graphicListener;

    public StsAbstractFileSetGraphicBrowserTransferPanel(String title, String currentDirectory, StsGraphicObjectTransferListener listener, int width, int height, StsAbstractFileSet fileset)
    {
		super(title, currentDirectory, listener, width, height);
		this.graphicListener = listener;
    }

    public void constructTransferPanel()
    {
		super.constructTransferPanel();
		fileCountBean = new StsIntFieldBean(this, "fileCount", false, "Num Files");
		displaySelectionButton = new StsButton("Display", "Displays selections in 3D view.", this, "displayObjects");
		directoryBrowser.addToRow(fileCountBean);
		directoryBrowser.addToRow(displaySelectionButton);
	}

	public int getFileCount() { return fileset.size(); };

    public void setDirectory(String directory)
    {
        if(fileset == null) return;
        fileset.setDirectory(directory);
        initializeSetAvailableObjects();
		fileCountBean.setValue(getFileCount());
    }

    public StsAbstractFile[] initializeAvailableObjects()
    {
        if(fileset == null) return null;
        return fileset.getFiles();
    }

	public void displayObjects()
	{
		graphicListener.displayAvailableObjects();
	}
/*
    public static void main(String[] args)
    {
        TestAbstractFileSetTransferPanelListener listener = new TestAbstractFileSetTransferPanelListener();
        String currentDirectory = "H:\\FractureAnalysis\\Surfaces";
        StsFilenameFilter filenameFilter = new StsFilenameFilter(StsSurface.seismicGrp, StsLoader.FORMAT_TXT);
        StsFileSet fileset = StsFileSet.constructor(currentDirectory, filenameFilter);
        try
        {
            UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
        }
        catch (Exception e) { }
        TestAbstractFileSetBrowserTransferPanel panel = new TestAbstractFileSetBrowserTransferPanel(currentDirectory, listener, 400, 100, fileset);
        com.Sts.Framework.Utilities.StsToolkit.createDialog(panel);
    }
*/
}

