package com.Sts.Framework.UI.Toolbars;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

/** ComboBoxToolbar is actually a set of comboBoxes.  Each comboBox is a set of instances of a particular group.
 *  When an instance of an stsClass belonging to this group is instantiated, the comboBox is created and this instance is added.
 *  See ObjectSelectPanel (innerclass) for the details of the comboBox.
 */
public class StsComboBoxToolbar extends StsToolbar implements StsSerializable
{
    transient public ArrayList<StsComboBoxToolbarObjectSelectPanel> objectSelectPanels = new ArrayList<StsComboBoxToolbarObjectSelectPanel>();
    transient StsModel model = null;
    transient StsWin3dBase win3d = null;

    static public final String NAME = "Object Selection Toolbar";
    static public final boolean defaultFloatable = true;

    static final String NONE = "none";
    static final String[] noneString = new String[] { NONE };

    static final boolean debug = false;

    public StsComboBoxToolbar()
    {
		super(NAME);
    }

    private StsComboBoxToolbar(StsModel model, StsWin3dBase win3d)
    {
        super(NAME);
        initialize(model, win3d);
    }

    public boolean initialize(StsWin3dBase win3d, StsModel model)
    {
		return initialize(win3d.model, win3d);
	}

    public boolean initialize(StsModel model, StsWin3dBase win3d)
    {
		this.model = model;
        this.win3d = win3d;
        setName(NAME);
        setFloatable(true);

        Border border = BorderFactory.createEtchedBorder();
        setBorder(border);

		TreeSet<StsClass> selectableClasses = model.objectSelectableClasses;
        for(StsClass selectableClass : selectableClasses)
		{
			StsComboBoxToolbarObjectSelectPanel objectSelectPanel = new StsComboBoxToolbarObjectSelectPanel(model, win3d, selectableClass);
			addObjectSelectPanel(objectSelectPanel);
		}
        addCloseIcon(win3d);
		if(objectSelectPanels.size() > 0)
		{
			isVisible = true;
			validate();
		}
        return true;
    }

	private void addObjectSelectPanel(StsComboBoxToolbarObjectSelectPanel objectSelectPanel)
	{
		//add objectSelectPanel component to Toolbar with a separator
		add(objectSelectPanel);
		addSeparator();
		// add panels to list
		objectSelectPanels.add(objectSelectPanel);
		validate();
	}

	public StsComboBoxToolbarObjectSelectPanel addObjectSelectableClass(StsClass objectSelectableClass, StsWin3dBase win3d, StsModel model)
	{
		StsComboBoxToolbarObjectSelectPanel objectSelectPanel = new StsComboBoxToolbarObjectSelectPanel(model, win3d, objectSelectableClass);
		addObjectSelectPanel(objectSelectPanel);
		isVisible = true;
		return objectSelectPanel;
	}

    static public StsComboBoxToolbar constructor(StsWin3dBase win3d)
    {
        StsComboBoxToolbar toolbar = new StsComboBoxToolbar(win3d.model, win3d);
        return toolbar;
    }
	/** constructor for unit testing without a window */
    static public StsComboBoxToolbar constructor(StsModel model)
    {
        StsComboBoxToolbar toolbar = new StsComboBoxToolbar(model, null);
        return toolbar;
    }
    /*
    *  Called when comboBox item has been selected. Propagate this change
    *  to other interested parties and repaint.
    */

    private StsComboBoxToolbarObjectSelectPanel getComboBoxPanel(Object object)
    {
        StsClass stsClass = model.getStsClass(object);
		return getComboBoxPanel(stsClass);
    }

    private StsComboBoxToolbarObjectSelectPanel getComboBoxPanel(StsClass stsClass)
    {
		if(stsClass == null) return null;
        for(StsComboBoxToolbarObjectSelectPanel objectSelectPanel  : objectSelectPanels)
        {
            if(objectSelectPanel.hasStsClass(stsClass))
                return objectSelectPanel;
        }
        return null;
    }
    /**
     * Called when a comboBox item has been changed externally and we need to
     * change the item displayed in the comboBox.  By changing the model, we do
     * not fire an item stateChanged causing an endless loop.
     */
    public void setSelectedObject(StsObject object)
    {
        if(object == null) return;
		model.setCurrentObject(object);
        //StsComboBoxToolbarObjectSelectPanel objectSelectPanel = getComboBoxPanel(object);
        //if(objectSelectPanel == null) return;
        //objectSelectPanel.setSelectedObject(object);
    }

	public void setComboBoxItem(StsObject object)
    {
        if(object == null) return;
		StsComboBoxToolbarObjectSelectPanel objectSelectPanel = getComboBoxPanel(object);
		if(objectSelectPanel == null) return;
		objectSelectPanel.setComboBoxItem(object);
	}

    public StsObject deleteComboBoxObject(StsObject object)
    {
        if(object == null) return null;
        StsComboBoxToolbarObjectSelectPanel objectSelectPanel = getComboBoxPanel(object);
        if(objectSelectPanel == null) return null;
        return objectSelectPanel.deleteComboBoxObject(object);
    }

	static public void main(String[] args)
	{
		try
		{
			StsToolkit.runLaterOnEventThread
			(
				new Runnable()
				{
					public void run()
					{
						StsModel model = StsModel.constructor("test");
						//StsSeismicVolume volume = new StsSeismicVolume(true, "volOne");
						//StsSeismicVolume volume = new StsSeismicVolume(false);
						//volume.setName("volOne");
						//model.add(volume);
						StsComboBoxToolbar toolbar = StsComboBoxToolbar.constructor(model);
						StsToolkit.createDialog(toolbar, true, 200, 50);
					}
				}
			);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsComboBoxToolbar.class, "main", e);
		}
	}

	public String toString() { return NAME; }
}
