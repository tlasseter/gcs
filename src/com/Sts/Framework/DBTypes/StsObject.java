//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.DBTypes;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.media.opengl.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/** Base class for all Sts Objects
  * @author TJLasseter
  * Index number of this geometric object uniquely identifies it.
  * The current lastIndex is maintained by the corresponding StsClass which manages the
  * instances of a particular type of StsObject (StsWell instances are managed by StsWellClass).
  * So this lastIndex is incremented and assigned to a new instance of that type.
  * If object is transient, index stays -1.
  */
abstract public class StsObject extends StsSerialize implements Cloneable, ActionListener, StsSerializable, Comparable<StsObject>
{
    private transient int index = -1;

	static public String getObjectName(StsMainObject object)
	{
		if(object == null) return "null";
		else return object.getName();
	}

	final public void setIndex(int index) { this.index = index; }
    final public int getIndex() { return this.index; }

    public StsObject()
    {
        try
        {
            if (currentModel != null) currentModel.add(this);
        }
        catch (Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

    static public StsProject getProject() { return currentModel.getProject(); }

    public void addToModel()
    {
		if(currentModel == null) return;
		currentModel.add(this);
//        refreshObjectPanel();
    }

    /** Override this in concrete subclass to respond when currentObject is set by corresponding StsClass. */
    public void setToCurrentObject()
    {

    }

	public void addCopyToModel()
	{
		StsObject object = (StsObject)StsToolkit.copyAllObjectFields(this, false);
	}

    public void refreshObjectPanel()
    {
        currentModel.refreshObjectPanel(this);
    }

    public static int getObjectIndex(StsObject object)
    {
        if (object == null)
        {
            return -99;
        }
        else
        {
            return object.getIndex();
        }
    }

    public static String getObjectIndexString(StsObject object)
    {
        if (object == null)
        {
            return new String("null");
        }
        int index = object.getIndex();
        if (index >= 0)
        {
            return new String("" + index);
        }
        else
        {
            return new String("transient");
        }
    }

    public StsObject(boolean persistent)
    {
        if (persistent)
        {
            try
            {
                if (currentModel != null)
                {
                    currentModel.add(this);
                }
            }
            catch (Exception e)
            {
                StsException.outputException(e, StsException.FATAL);
            }
        }
    }

    final public boolean isPersistent()
    {
        return index >= 0;
    }

	public boolean isPersistable() { return true; }

    final public boolean indexOK()
    {
        return index >= 0;
    }

    public StsClass getStsClass()
    {
        try
        {
            return currentModel.getStsClass(this.getClass());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public StsClass getCreateStsClass()
    {
        try
        {
            return currentModel.getCreateStsClass(this.getClass());
        }
        catch (Exception e)
        {
            return null;
        }
    }

	/** override in subclasses */
	public boolean getIsVisible(){ return true; }

	/** override in subclasses */
	public void setIsVisible(boolean isVisible) { }

/*
    public StsObject getStsClassObjectWithIndex(Class c, int index)
    {
        StsClass instanceList = currentModel.getCreateStsClass(c);
        if (instanceList == null)
        {
            return null;
        }
        if (index < 0 || index >= instanceList.getSize())
        {
            return null;
        }
        return instanceList.getElementWithIndex(index);
    }
*/
    /*
     static public StsClass getCreateStsClass(Class c)
     {
      try	{ return currentModel.getCreateStsClass(c); }
      catch(Exception e) { return null; }
     }
     */


    /*
     public StsObject getStsClassObjectWithName(StsClass list, String name)
     {
      int nObjects = list.getSize();
      if(nObjects == 0) return null;
      Object object = list.getElement(0);
      if(object instanceof StsMainObject)
      {
       for(int n = 0; n < nObjects; n++)
       {
        StsMainObject mainObject = (StsMainObject)list.getElement(n);
        if(mainObject.getName().equals(name)) return mainObject;
       }
      }
      else if(object instanceof StsObject)
      {
       for(int n = 0; n < nObjects; n++)
       {
        StsObject stsObject = (StsObject)list.getElement(n);
        if(stsObject.getName().equals(name)) return stsObject;
       }
      }
      return null;
     }
     */
    /** Methods to be implemented by subclasses as needed */

    public void display(StsGLPanel glPanel)
    {
		StsException.notImplemented(this, "display");
    }

    public boolean displayTexture(StsGLPanel3d glPanel, long time)
    {
		StsException.notImplemented(this, "displayTexture");
		return false;
    }

    public boolean initialize(StsModel model)
    {
        StsException.notImplemented(this, "initialize");
        return false;
    }

    public void pick(GL gl, StsGLPanel glPanel)
    {
		StsException.notImplemented(this, "pick(GL)");
    }

    public boolean setHighlight(boolean state)
    {
		StsException.notImplemented(this, "setHighlight(boolean)");
        return false;
    }

    /** clone this StsObject and add it to the appropriate instance list */
    public Object clone()
    {
        return clone(true);
    }

    public Object clone(boolean persistence)
    {
        try
        {
            Object newObject = super.clone();
            StsObject newStsObject = (StsObject)newObject;
            newStsObject.setIndex(-1);
            if(persistence && currentModel != null) currentModel.add(newStsObject);
            return newObject;
        }
        catch (Exception e)
        {
            System.out.println("Exception in StsObject()\n" + e);
            return null;
        }
    }

    public boolean delete()
    {
        if (getIndex() == -1)
        {
            return false;
        }
        return currentModel.delete(this);
    }

    public float compare(StsObject object)
    {
        return (float) StsParameters.UNDEFINED;
    }

    public String getLabel()
    {
        return new String(getClass().toString() + "-" + getIndex());
    }

    public String toString()
    {
        return new String(getClass().toString() + "[" + getIndex()+ "]");
    }

    public String toDebugString()
    {
        return getSimpleClassname() + "[" + getIndex()+ "]";
    }

    // generally redundant as getName returns className-index.  TJL 2/7/07
    // suggest just using toString()
    public String getClassAndNameString()
	{
		return getClass().toString() + "[" + getIndex()+ "]: " + getName();
    }

	public String getClassname()
	{
		return getClass().getName();
    }

	public int compareTo(StsObject other)
	{
		return getName().compareTo(other.getName());
	}

    /** methods to override */

    public void setStsColor(StsColor color)
    {
		StsException.notImplemented(this, "setStsColor(StsColor)");
    }

    public StsColor getStsColor()
    {
		StsException.notImplemented(this, "getStsColor");
        return null;
    }

    //TODO remove all calls to getColor and setBeachballColors so we are consistently working with StsColor(s)
    public void setColor(Color color)
    {
		StsException.notImplemented(this, "setColor(Color)");
    }

    public float getOrderingValue()
    {
        return StsParameters.nullValue;
    }

    public boolean isInList(StsObject[] objects)
    {
        if (objects == null)
        {
            return false;
        }
        int nObjects = objects.length;
        for (int n = 0; n < nObjects; n++)
        {
            if (this == objects[n])
            {
                return true;
            }
        }
        return false;
    }

    public void logMessage(String msg)
    {
        StsMessageFiles.logMessage(msg);
    }

    public void infoMessage(String msg)
    {
        StsMessageFiles.infoMessage(msg);
    }

    public void errorMessage(String msg)
    {
        StsMessageFiles.errorMessage(msg);
    }

    public boolean launch()
    {
        String className = getClassname();
        StsMessageFiles.infoMessage("Objects of class " + className + " cannot be launched.");
        return false;
    }
    public boolean goTo()
    {
        String className = getClassname();
        StsMessageFiles.infoMessage("Cannot change location to objects of class " + className + ".");
        return false;
    }    
    public boolean canExport() { return true; }

    public boolean export()
	{
		 String className = getSimpleClassname();
		 try
		 {
			 String directory = currentModel.getProject().getSourceDataDirString();
			 String filename = className + ".obj." + getName();
			 StsObjectDBFileIO.exportStsObject(directory + File.separator + filename, this, null);
			 StsMessageFiles.infoMessage("Successfully exported file:" + directory + File.separator + filename);
			 return true;
		 }
		 catch(Exception e)
		 {
			 StsException.outputException("StsObject export failed for " + getName(), e, StsException.WARNING);
			 return false;
		 }
	}

	public String getSimpleClassname()
	{
		return StsToolkit.getSimpleClassname(this);
	}

    public boolean canLaunch() { return false; }

    public void popupPropertyPanel()
    {
        if(!StsTreeObjectI.class.isAssignableFrom(getClass())) return;

		StsFieldBean[] beans = ((StsTreeObjectI)this).getDisplayFields();
		StsFieldBean[] tBeans = new StsFieldBean[beans.length];
		StsGroupBox box = new StsGroupBox("Properties");
		box.gbc.fill = box.gbc.HORIZONTAL;
		for(int i=0; i<beans.length; i++)
		{
			if((beans[i].getEditable()) || (beans[i] instanceof StsButtonFieldBean))
			{
				if(beans[i] instanceof StsEditableColorscaleFieldBean)      // Problem putting colorscales on popup so skipping for now
					continue;
				StsFieldBean beanCopy = beans[i].copy(this);
				if(beanCopy == null) continue;
				tBeans[i] = beanCopy;
				box.addEndRow(tBeans[i]);
			}
		}
		new StsOkDialog(currentModel.win3d, box, getName() + " Properties", false);
    }
 
    public void close()
    {
    }

    public void instanceChanged(String reason)
    {
        currentModel.instanceChange(this, reason);
    }

	public void actionPerformed(ActionEvent e)
	{
		StsException.notImplemented(this, "actionPerformed(ActionEvent)");
	}

	public void objectPropertiesChanged(Object object) { }

    public StsColorscale getColorscaleWithName(String name)
    {
		StsException.notImplemented(this, "getColorscaleWithName");
        return null;
    }

	public String getName()
	{
		String classname = getClass().getSimpleName();
		if(classname.startsWith("Sts"))
			classname = classname.substring(3);
		return classname + "-" + getIndex();
	}
}