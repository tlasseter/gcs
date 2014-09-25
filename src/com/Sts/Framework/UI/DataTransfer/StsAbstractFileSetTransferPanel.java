package com.Sts.Framework.UI.DataTransfer;

import com.Sts.Framework.IO.*;

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
public class StsAbstractFileSetTransferPanel extends StsDirectoryObjectsTransferPanel
{
    StsAbstractFileSet fileset;

    public StsAbstractFileSetTransferPanel(String title, String currentDirectory, StsObjectTransferListener listener, int width, int height)
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
        initializeSetAvailableObjects();
    }

    public StsAbstractFile[] initializeAvailableObjects()
    {
        if(fileset == null || directory == null) return null;
        if(!fileset.addFiles(directory)) return null;
		return fileset.getFiles();
    }
}

