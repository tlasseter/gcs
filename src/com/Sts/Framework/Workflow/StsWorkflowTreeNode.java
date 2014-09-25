package com.Sts.Framework.Workflow;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Icons.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;

public class StsWorkflowTreeNode extends DefaultMutableTreeNode implements Runnable
{
    String name;
    Color color = Color.lightGray;
    ImageIcon imageIcon;
    Class actionClass;
    String description;
    String requirements;
    String actionClassname;
    int type = ACTIVE;
    boolean isOptional = false;

    StsNodeBundle[] inputNodeBundles = null;
    StsWorkflowTreeNode[] outputNodes = null;

    static StsTreeModel treeModel;
	static ClassLoader classLoader;

    static final int CANNOT_START = StsModel.CANNOT_START;
    static final int CAN_START = StsModel.CAN_START;
    static final int STARTED = StsModel.STARTED;
    static final int ENDED = StsModel.ENDED;

    static public final int ACTIVE = 0;
    static public final int PASSIVE = 1;

    static public final String NO_DESCRIPTION = "No description available";
    static public final String NO_PREREQUISITES = "No prerequisites provided";

    int actionStatus = CANNOT_START;

    static StsModel model;

    static StsWorkflowTreeNode selectedNode = null;

    public StsWorkflowTreeNode(String actionClassname, String name, String description, String requirements, String iconName)
    {
        try
        {
            this.actionClassname = actionClassname;
            this.description = description;
            this.requirements = requirements;
            this.name = name;
            if (iconName != null)
                imageIcon = StsIcon.createIcon("Workflow/" + iconName);
        }
        catch (Exception e)
        {
            StsException.outputException("StsWorflowPanel.init() failed.",
                e, StsException.WARNING);
        }
    }

    public StsWorkflowTreeNode(Class actionClass, String name, String description, String requirements, String iconName)
    {
        try
        {
            this.actionClass = actionClass;
            this.description = description;
            this.requirements = requirements;
            this.name = name;
            if (iconName != null)
                imageIcon = StsIcon.createIcon("Workflow/" + iconName);
            if (actionClass == null) return;
            actionClassname = actionClass.getName();
        }
        catch (Exception e)
        {
            StsException.outputException("StsWorflowPanel.init() failed.",
                e, StsException.WARNING);
        }
    }

    public StsWorkflowTreeNode(String actionClassname, String name, String iconName)
    {
        try
        {
            this.actionClassname = actionClassname;
            this.description = NO_DESCRIPTION;
            this.requirements = NO_PREREQUISITES;
            this.name = name;
            if (iconName != null)
                imageIcon = StsIcon.createIcon("Workflow/" + iconName);
            if (actionClass == null) return;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWorflowPanel.init() failed.",
                e, StsException.WARNING);
        }
    }


    public StsWorkflowTreeNode(String name)
    {
        this(null, name, null);
    }

	public StsWorkflowTreeNode(String name, String iconName)
    {
        this(null, name, iconName);
    }

    public StsWorkflowTreeNode addChild(String name, String iconName)
    {
        StsWorkflowTreeNode child = new StsWorkflowTreeNode(name, iconName);
        add(child);
        return child;
    }

    public StsWorkflowTreeNode addChild(String actionClassname, String name, String iconName)
    {
        StsWorkflowTreeNode child = new StsWorkflowTreeNode(actionClassname, name, iconName);
        add(child);
        return child;
    }

    public StsWorkflowTreeNode addChild(String actionClassname, String name, String description, String prerequisites, String iconName)
    {
        StsWorkflowTreeNode child = new StsWorkflowTreeNode(actionClassname, name, description, prerequisites, iconName);
        if(child == null) return null;
        add(child);
        return child;
    }

    public void changeNode(Class actionClass, String name, String iconName)
    {
        if (actionClass != null)
            this.actionClass = actionClass;
        this.name = name;
        if (iconName != null)
            imageIcon = StsIcon.createIcon("Workflow/" + iconName);
    }

    public StsWorkflowTreeNode addChild(StsWorkflowTreeNode child)
    {
        add(child);
        return child;
    }

    static public void setModel(StsModel _model) { model = _model; }

    static public void setTreeModel(StsTreeModel _treeModel) { treeModel = _treeModel; }

    public Color getColor() { return color; }

    public void setColor(Color color) { this.color = color; }

    public String getName() { return name; }

    public String toString() { return name; }

    public ImageIcon getImageIcon() { return imageIcon; }

    public Class getActionClass()
	{
		try
		{
			if(classLoader == null)
				classLoader = StsWorkflowTreeNode.class.getClassLoader();
			if(actionClass == null)
				actionClass = classLoader.loadClass(actionClassname);
			return actionClass;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "getActionClass", "Failed to load class; " + actionClassname);
			return null;
		}
	}

    public int getActionType() { return type; }

    public boolean isAction() { return actionClassname != null; }

    public boolean isActionEnded() { return actionStatus == ENDED; }

    public String getNodeDescription() { return description; }

    public void setNodeDescription(String desc) { description = desc; }

    public String getNodeRequirements() { return requirements; }

    public void setNodeRequirements(String reqs) { requirements = reqs; }

    public void setSelectedNode() { selectedNode = this; }

    public boolean isNodeSelected() { return this == selectedNode; }

    public void isOptional(boolean isOptional) { this.isOptional = isOptional; }

    public boolean isCheckBoxSelectable()
    {
        return isOptional && actionStatus >= CAN_START;
    }

    public void run()
    {
        treeModel.nodeChanged(this);
    }

    public void setActionStatus(int status)
    {
        if (actionStatus != status)
        {
            actionStatus = status;
            setModelActionStatus();
            updateParentStatus();
            StsToolkit.runLaterOnEventThread(this);
        }
    }

    private void setModelActionStatus()
    {
        if (actionClassname != null) model.setActionStatus(actionClassname, actionStatus);
        else model.setActionStatusProperty(name, actionStatus);
    }

    public void setActionStatusEnded(boolean ended)
    {
        if (actionStatus != ENDED && ended)
            treeModel.adjustWorkflowPanelState(actionClassname, ENDED);
        else if (actionStatus == ENDED && !ended)
            treeModel.adjustWorkflowPanelState(actionClassname, STARTED);
    }

    /*
        protected void createIcon(String name)
        {
            imageIcon = StsIcon.createIcon("Workflow/" + name);
            /*
            try
            {
                String pathName = new String("Icons/" + name);
    //			String pathName = new String("Icons" + File.separator + name);
                java.net.URL url = getClass().getResource(pathName);
                if(url == null)
                {
                    System.out.println("StsWorkflowPanel.StsWorkflowTreeNode.createIcon() error. Couldn't find image: " + "Icons/" + name);
                    return;
                }
                else
                {
                    Image i = Toolkit.getDefaultToolkit().createImage(url);
                    imageIcon = new ImageIcon(i);
                }
            }
            catch (Exception ex) {	}
        }
    */
    protected void addInputNodeBundle(StsNodeBundle nodeBundle)
    {
        inputNodeBundles = (StsNodeBundle[]) StsMath.arrayAddElement(inputNodeBundles, nodeBundle);
    }

    protected void addOutputNode(StsWorkflowTreeNode outputNode)
    {
        outputNodes = (StsWorkflowTreeNode[]) StsMath.arrayAddElement(outputNodes, outputNode);
    }

    protected int getActionStatus()
    {
        return actionStatus;
//        if(model == null) return CANNOT_START;
//        if(actionClassname != null) return model.getActionStatus(actionClassname);
//        else return model.getActionStatus(name);
    }

    protected void initializeActionStatus()
    {
        int actionStatus;

        if (actionClassname != null)
            actionStatus = model.getActionStatus(actionClassname);
        else
            actionStatus = model.getActionStatus(name);

        if (actionStatus != CANNOT_START) setActionStatus(actionStatus);
    }

    protected boolean updateActionStatus()
    {
        if (!isLeaf()) return false;

        boolean changed = false;
        if (inputNodeBundles == null)
        {
            if (this.actionStatus == CANNOT_START)
            {
                setActionStatus(CAN_START);
                changed = true;
            }
        }
        else if (this.actionStatus == CANNOT_START)
        {
            for (int n = 0; n < inputNodeBundles.length; n++)
                if (!inputNodeBundles[n].constraintSatisfied()) return false;

            setActionStatus(CAN_START);
            changed = true;
        }
        return changed;
    }

    protected boolean updateParentStatus()
    {
        StsWorkflowTreeNode parent = (StsWorkflowTreeNode) getParent();
        if (parent == null) return false;
        return parent.updateParentStatusFromChildren();
    }

    protected boolean updateParentStatusFromChildren()
    {
        if (getChildCount() == 0) return false;

        boolean[] hasStatus = new boolean[4];

        Enumeration enumeration = children();
        boolean changed = false;

        int minStatus = ENDED;
        int maxStatus = CANNOT_START;
//        int actionStatus = ENDED;
        while (enumeration.hasMoreElements())
        {
            StsWorkflowTreeNode childNode = (StsWorkflowTreeNode) enumeration.nextElement();
            int status = childNode.actionStatus;
            hasStatus[status] = true;
            minStatus = Math.min(status, minStatus);
            maxStatus = Math.max(status, maxStatus);
        }

        int actionStatus = CANNOT_START;
        // if children all have same status, this is the parent status
        if (minStatus == maxStatus)
            actionStatus = minStatus;
        else
        {
            for (int n = 1; n < 3; n++)
                if (hasStatus[n])
                {
                    actionStatus = n;
                    break;
                }
//        else if(minStatus == CANNOT_START) actionStatus = maxStatus;
//        else if(maxStatus == ENDED) actionStatus = minStatus;
        }
        if (actionStatus != this.actionStatus)
        {
            setActionStatus(actionStatus);
            changed = true;
        }
        return changed;
    }

    protected boolean adjustedState(String actionClassname, int actionStatus)
    {
        if (this.actionClassname.equals(actionClassname) && this.actionStatus != actionStatus)
        {
            setActionStatus(actionStatus);
            return true;
        }
        return false;
    }

    public void setImageIcon(String iconName)
    {
        if (iconName != null)
            imageIcon = StsIcon.createIcon("Workflow/" + iconName);
    }
}
