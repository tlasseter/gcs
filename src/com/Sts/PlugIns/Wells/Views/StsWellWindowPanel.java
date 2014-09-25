package com.Sts.PlugIns.Wells.Views;

import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.MultiSplitPane.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Title: jS2S development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: S2S Systems LLC</p>
 * @author Tom Lasseter
 * @version 1.0
 */

public class StsWellWindowPanel extends StsMultiSplitPane
{
    StsWellViewModel wellViewModel;
    StsWellTrackView wellTrackView;
	StsJPanel innerPanel;
	// transient Color color = Color.WHITE;
	Integer counter = 0;

   public StsWellWindowPanel()
    {
	//	this.color=color;
		initLayout();
    }

	private void initLayout()
	{

    }

	public StsJPanel getRootPanel()
	{
	innerPanel = new StsJPanel();
	// innerPanel.setBackground(c);
	innerPanel.setLayout(new BoxLayout(innerPanel,BoxLayout.Y_AXIS));
	StsMultiSplitPaneLayout layout = getMultiSplitLayout();
	layout.addRootLeaf("root");
	innerPanel.setName("root");
	addRoot();
	return innerPanel;
    }

	public StsJPanel getNewPanel(int width, int height)
	{
		innerPanel = new StsJPanel();
		// innerPanel.setBackground(c);

		innerPanel.setPreferredSize(new Dimension(width,height));

		innerPanel.setLayout(new BoxLayout(innerPanel,BoxLayout.Y_AXIS));
		counter++;
		innerPanel.setName(counter.toString());
		addInner();
		return innerPanel;
	}

	public StsJPanel innerPanel()
	{
		if (innerPanel == null)
			return getNewPanel(166,500);
		else
			return innerPanel;

	}

	public void removeInner(StsJPanel inner)
	{
		StsMultiSplitPaneLayout layout = getMultiSplitLayout();
		layout.removeLayoutLeaf(inner.getName());
//		this.remove(inner);
    }

	public void addInner()
	{
		StsMultiSplitPaneLayout layout = getMultiSplitLayout();
		String counterString = counter.toString();
		layout.addRootRowSplit(counterString);
		innerPanel.setName(counterString);
		add(innerPanel,counterString);
	}


	public void addRoot()
	{
		StsMultiSplitPaneLayout layout = getMultiSplitLayout();
		add(innerPanel,"root");

	}

    public void setBounds(int x, int y, int width, int height)
    {

		innerPanel.setBounds(x,y,width,height);
        super.setBounds(x, y, width, height);
    }
/*
    private boolean initializeView(StsModel model)
    {
        try
        {
            wellTrackView = new StsWellTrackView(wellViewModel, model);
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWellWindowFrame.initializeView() failed.", e, StsException.WARNING);
            return false;
        }
    }
*/
    public StsWellTrackView getWellTrackView() { return wellTrackView; }

    //	public StsWellTrackGLPanel getWellTrackDisplayPanel() { return wellTrackDisplayPanel; }
    public void rebuild()
    {
        invalidate();
        repaint();
    }
}
