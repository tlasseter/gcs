
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.UI;

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;

/** Adds a convenient setValues method to the MenuItem class. */

public class StsJMenuItem extends JMenuItem
{

    public StsJMenuItem()
    {
    }

    /** For an instance method listener, sets the label and attach to the listener
      * @param label menuItem label
      * @param target instance object listening to to this menuItem
      * @param methodName String name of the instance/method listening to this menuItem
      * @param args Object[] args passed to the listener class/method
      */
    public void setMenuActionListener(String label, Object target, String methodName, Object[] args)
    {
        try
        {
            setText(label);
            StsMethod method = new StsMethod(target, methodName, args);
            addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }


    public void setMenuAction(String label, Class actionClass, StsModel model, StsGLPanel glPanel)
    {
        try
        {
            setText(label);
                        Object[] arguments = new Object[]{model, glPanel, actionClass, new Object[0]};
               StsMethod method = new StsMethod(StsActionManager.class, "startAction", arguments);
            addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

    // menu action is class constuctor which will have model,glPanel as arguments
    public void setMenuAction(String label, Class actionClass, StsActionManager actionManager)
    {
        try
        {
            setText(label);
               StsMethod method = new StsMethod(actionManager, "startAction", new Object[] { actionClass });
            addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

    // menu action is class constuctor which will have model,glPanel,args as arguments
    public void setMenuAction(String label, Class actionClass, StsActionManager actionManager, Object[] args)
    {
        try
        {
            setText(label);
            Object[] arguments = new Object[] { actionClass, args };
               StsMethod method = new StsMethod(actionManager, "startAction", arguments);
            addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

    /** For an instance method listener, with a single arg, make Object[] arg array
      * and call actual constructor.
      * @param label menuItem label
      * @param target instance object listening to to this menuItem
      * @param methodName String name of the instance/method listening to this menuItem
      * @param arg Object arg passed to the listener class/method
      */

    public void setMenuActionListener(String label, Object target, String methodName, Object arg)
    {
        setMenuActionListener(label, target, methodName, new Object[]{arg});
    }

    /** For a static method listener, sets the label and attach to the listener
      * @param label menuItem label
      * @param c class of the static class/method listening to to this menuItem
      * @param methodName String name of the method listening to this menuItem
      * @param args Object[] args passed to the listener class/method
      */
    public void setMenuActionListener(String label, Class c, String methodName, Object[] args)
    {
        try
        {
            setText(label);
               StsMethod method = new StsMethod(c, methodName, args);
             addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

   /** For a static method listener, with a single arg, make Object[] arg array
      * and call actual constructor.
      * @param label menuItem label
      * @param c class of the static class/method listening to to this menuItem
      * @param methodName String name of the method listening to this menuItem
      * @param arg Object arg passed to the listener class/method
      */
    public void setMenuActionListener(String label, Class c, String methodName, Object arg)
    {
        setMenuActionListener(label, c, methodName, new Object[]{arg});
    }

    /** For a static method listener, sets the label and attach to the listener
      * @param label menuItem label
      * @param className name of the static class listening to to this menuItem
      * @param methodName String name of the method listening to this menuItem
      * @param args Object args passed to the listener class/method
      */
    public void setMenuActionListener(String label, String className, String methodName, Object[] args)
    {
        try
        {
            setText(label);
               StsMethod method = new StsMethod(className, methodName, args);
            addActionListener(new StsActionListener(method));
        }
        catch(Exception e)
        {
            StsException.outputException(e, StsException.FATAL);
        }
    }

    /** For a static method listener, with a single arg, make Object[] arg array
      * and call actual constructor.
      * @param label menuItem label
      * @param className name of the static class listening to to this menuItem
      * @param methodName String name of the method listening to this menuItem
      * @param arg Object arg passed to the listener class/method
      */
    public void setMenuActionListener(String label, String className, String methodName, Object arg)
    {
        setMenuActionListener(label, className, methodName, new Object[]{arg});
    }

    public void setText(String label)
    {
        super.setText(label);
        setName(label);
    }

    public String toString() { return getName(); }
}
