package com.Sts.Framework.UI.DataTransfer;

/**
 * <p>Title: S2S development</p>
 * <p>Description: Integrated seismic to simulation software</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version c62e
 */

/** Interface implemented by objects which are listeners on the StsObjectTransferPanel */
public interface StsGraphicObjectTransferListener extends StsObjectTransferListener
{
    /** display all available objects for potential transfer to selected list */
    public void displayAvailableObjects();
	/** add these objects selected graphically to the selected list */
	public Object[] addGraphicSelectedObjects();
}