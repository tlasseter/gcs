package com.Sts.Framework.UI.Toolbars;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
* User: Tom Lasseter
* Date: Jan 22, 2008
* Time: 7:18:54 PM
*/

/** This class is a JPanel with a comboBox containing a list of instances belong to this class and optionally a list of subclasses
 *  selectedObject is persistent and is recovered when the objectSelectPanel is built as a component of an StsComboBoxToolbar.
 */
public class StsComboBoxToolbarObjectSelectPanel extends StsJPanel implements StsSerializable
{
    private StsObject selectedObject = null;
    transient StsModel model;
	transient StsWin3dBase win3dBase;
    transient StsClass parentClass = null;
    transient StsToggleButton toggleButton;
    transient StsComboBoxFieldBean comboBoxBean;

    public StsComboBoxToolbarObjectSelectPanel()
    {
    }

    public StsComboBoxToolbarObjectSelectPanel(StsModel model, StsWin3dBase win3dBase, StsClass parentClass)
    {
		this.model = model;
        this.parentClass = (StsClass)parentClass;
		this.win3dBase = win3dBase;
		initialize();
    }

    public void initialize()
    {
		String[] iconNames = ((StsClassObjectSelectable)parentClass).getSelectableButtonIconNames();
		String selectedIconName = iconNames[0];
		String deselectedIconName = iconNames[1];
        toggleButton = new StsToggleButton(selectedIconName, "toggle visibility of " + selectedIconName, this, "toggleOn", "toggleOff");
        toggleButton.addIcons(selectedIconName, deselectedIconName);
        toggleButton.setSelected(true);
        addToRow(toggleButton);
		comboBoxBean = new StsComboBoxFieldBean();
        comboBoxBean.initialize(this, "selectedObject", null, "comboBoxList");
        getComboBoxList();
        addEndRow(comboBoxBean);
    }

    public void setVisible(boolean visible)
    {
        if(isVisible() == visible) return;
        super.setVisible(visible);
        comboBoxBean.setVisible(visible);
    }

    private void comboBoxSelectObject(StsObject object)
    {
        if(selectedObject == object) return;
//        stsComboBoxToolbar.win3d.glPanel3d.setObject(object);   // Don't comment this line out -- required for multiple windows.
        selectedObject = object;
        // objectTreePanelSelectObject(object);
		// this call may not be redundant as objectTreePanelSelectObject eventually calls it as well
		// if setCurrentObject checks that object is already current, no problem;
		// otherwise we might have an extra redraw
		model.setCurrentObject(object);
		//win3dBase.windowFamilyViewObjectChangedAndRepaint(this, object);
		toggleButton.setSelected(true);
    }

    private void objectTreePanelSelectObject(Object object)
    {
        if(object == null) return;

        if(win3dBase.isMainWindow())
        {
            StsObjectTreePanel objectTreePanel = ((StsWin3d)win3dBase).objectTreePanel;
            if(objectTreePanel != null)
                objectTreePanel.selected((StsObject)object);
        }
    }

    public void setSelectedObject(StsObject object)
    {
        if(object == selectedObject) return;
		selectedObject = object;
		model.setCurrentObject(object);
		setComboBoxItem(object);
    }

	public void setComboBoxItem(StsObject object)
	{
		comboBoxBean.setSelectedItemNoActionEvent(object);
		toggleButton.setSelected(true);
	}


    public void toggleOn()
    {
        if(selectedObject != null) model.toggleOnCursor3dObject(selectedObject);
    }

    public void toggleOff()
    {
        if(selectedObject != null) model.toggleOffCursor3dObject(selectedObject);
    }

    public StsObject deleteComboBoxObject(StsObject object)
    {
        StsObject[] objects = getComboBoxList();
        int index = StsMath.arrayGetIndex(objects, object);
        if(index == -1) return null;
        int length = objects.length;
        if(length == 1)
        {
            selectedObject = null;
            setVisible(false);
            return null;
        }
        if(index > 0)
            selectedObject = objects[index-1];
        else // index == 0;
            selectedObject = objects[index+1];
        return selectedObject;
    }

    private StsObject[] getComboBoxList()
    {
        StsObject[] objects = new StsObject[0];
		return getObjectsFromStsClass(parentClass, objects);
	}

	private StsObject[] getObjectsFromStsClass(StsClass parentClass, StsObject[] objects)
	{
        try
        {
			objects = (StsObject[])StsMath.arrayAddArray(objects, parentClass.getObjectList());
			ArrayList<StsClass> childClasses = parentClass.subClasses;
			if(childClasses == null) return objects;
            for(StsClass stsClass : childClasses)
				objects = getObjectsFromStsClass(stsClass, objects);
            return objects;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "getComboBoxObjects", e);
            return objects;
        }
    }

    public boolean hasStsClass(StsClass objectClass)
    {
        if(objectClass == parentClass)
            return true;
		ArrayList<StsClass> childClasses = objectClass.subClasses;
		if(childClasses == null) return false;
		for(StsClass stsClass : childClasses)
			return hasStsClass(stsClass);
        return false;
    }

    private void initializeSelectedItem()
    {
        if(selectedObject != null)
        {
            comboBoxBean.setSelectedItem(selectedObject);
            return;
        }

		initializeSelectedItem(parentClass);
	}

	private boolean initializeSelectedItem(StsClass parentClass)
	{
        // selectedObject is currently null, so find one
        selectedObject = parentClass.getCurrentObject();

        if(selectedObject != null)
        {
            comboBoxBean.setSelectedItem(selectedObject);
            return true;
        }
		ArrayList<StsClass> childClasses = parentClass.subClasses;
		if(childClasses == null) return false;

		for(StsClass stsClass : childClasses)
			if(initializeSelectedItem(stsClass)) return true;
        return false;
    }

	public StsObject getSelectedObject()
	{
		return selectedObject;
	}

	public String toString()
	{
		if(parentClass != null) return parentClass.toString();
		else return "none";
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
						//new StsSeismicVolume(true, "volOne");
						//new StsSeismicVolume(true, "volTwo");
						StsClassObjectSelectable[] selectableClasses = model.objectSelectableClasses.toArray(new StsClassObjectSelectable[0]);
						int nPanels = selectableClasses.length;
						StsComboBoxToolbarObjectSelectPanel selectPanel = new StsComboBoxToolbarObjectSelectPanel(model, null, (StsClass)selectableClasses[0]);
						JDialog dialog = StsToolkit.createDialog(selectPanel, false);
					}
				}
			);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsComboBoxToolbarObjectSelectPanel.class, "main", e);
		}
	}
}