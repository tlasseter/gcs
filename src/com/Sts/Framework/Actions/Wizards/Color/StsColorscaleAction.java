package com.Sts.Framework.Actions.Wizards.Color;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

import com.Sts.Framework.Actions.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.awt.*;

public class StsColorscaleAction extends StsAction implements Runnable
{
	StsColorscale originalColorscale;
	StsColorscaleDialog colorscaleDialog = null;
//    StsColorscale colorscale = null;
	StsColorscalePanel observerColorscalePanel = null;
	Frame frame = null;
//    float[] dataDist = null;
	boolean success = false;

	public StsColorscaleAction(StsActionManager actionManager, StsColorscalePanel observerColorscalePanel)
	{
		super(actionManager, true);
		// frame = actionManager.getModel().win3d;
		this.observerColorscalePanel = observerColorscalePanel;
		originalColorscale = new StsColorscale(false);
		originalColorscale.copySettingsFrom(observerColorscalePanel.getColorscale());
//		this.colorscale = observerColorscalePanel.getColorscale();
//		dataDist = observerColorscalePanel.getHistogram();
	}

	public StsColorscaleAction(StsActionManager actionManager, StsColorscalePanel observerColorscalePanel, StsWin3d win3d)
	{
		super(actionManager, true);
		this.observerColorscalePanel = observerColorscalePanel;
		this.frame = win3d;
//		this.colorscale = observerColorscalePanel.getColorscale();
//		dataDist = observerColorscalePanel.getHistogram();
	}

	public boolean start()
	{
		run();
		return true;
	}

	public void run()
	{
		try
		{
            StsToolkit.runLaterOnEventThread
            (
                new Runnable()
                {
                    public void run()
                    {
                        if(observerColorscalePanel == null)
                        {
                            StsMessageFiles.infoMessage("Need to define object first.");
                            return;
                        }
                        if(model == null)
                            colorscaleDialog = new StsColorscaleDialog("Color Spectrum Editor", observerColorscalePanel, frame);
                        else
                            colorscaleDialog = new StsColorscaleDialog("Color Spectrum Editor", model, observerColorscalePanel, frame);

                        int yPos = colorscaleDialog.getY() - 200;
                        if(yPos < 0)
                            yPos = 0;
                        colorscaleDialog.setLocation(colorscaleDialog.getX(), yPos);
                        colorscaleDialog.setVisible(true);
            //            colorscaleDialog.initData(dataDist);
                    }
                }
            );
		}
		catch(Exception e)
		{
			StsException.outputException("StsColorscaleAction.run() failed.", e, StsException.WARNING);
			return;
		}

	}

	// not referenced, not needed TJL 1/31/06
	/*
	 public void setColorscale(StsColorscale cs)
	 {
	  this.colorscale = cs;
	 }
	 */
	public boolean end()
	{
		StsColorscale editedColorscale = observerColorscalePanel.getColorscale();
		success = colorscaleDialog.getSuccess();
		if(success)
		{
			// Change colorscale of first selected object or all selected objecets if they are seismicBoundingBoxes
			// should fix this to deal with any number of selected objects of any kind (assuming they all have colorscales).
			// However, if we have multiselected on the objectPanel, we want to update colorscales associated with all objects selected.
			// The only issue is the scaling: do we want to set the ranges to the same.  If not, how do we indicate that?
			// As it is, each of the colorscales other than the first in the selected list will be copied from the first in the list.
			StsObject[] stsObjects = model.win3d.objectTreePanel.getSelectedObjects();
		/*
			if(stsObjects.length <= 1 || !(stsObjects[0] instanceof StsSeismicBoundingBox))
			{
				editedColorscale.commitChanges(originalColorscale);
				editedColorscale.colorsChanged();
			}
			else
		*/
			{
				for(StsObject stsObject : stsObjects)
					updateColorscale(stsObject, editedColorscale);
			}
		}
		else // action has been canceled or otherwise aborted: restore original colorscale
		{
			editedColorscale.copySettingsFrom(originalColorscale);
			editedColorscale.colorsChanged();
		}
		observerColorscalePanel.repaint();
		return success;
	}

	private void updateColorscale(StsObject obj, StsColorscale editedColorscale)
	{
		/*
		StsColorscale colorscale = ((StsSeismicBoundingBox)obj).getColorscaleWithName(editedColorscale.getName());
		if(colorscale == null) return;
		colorscale.commitChanges(editedColorscale);
		colorscale.colorsChanged();
		*/
	}
}