package com.Sts.Framework.UI.ObjectPanel;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Toolbars.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

public class StsTreeNode extends DefaultMutableTreeNode
{
	String label;
	boolean isStatic = false;
	private boolean explored = false;
	boolean staticChildren = false;

	static StsObjectPanel emptyObjectPanel = StsObjectPanel.constructEmptyPanel();

	private StsTreeNode(StsTreeObjectI object, String label, boolean isStatic)
	{
		super();
		userObject = object;
		this.label = label;
		this.isStatic = isStatic;
	}

	public static StsTreeNode staticNode(StsTreeObjectI object, String label)
	{
		return new StsTreeNode(object, label, true);
	}

	private StsTreeNode(Object object, String label, boolean isStatic)
	{
		super();
		userObject = object;
		this.label = label;
		this.isStatic = isStatic;
	}

	public static StsTreeNode staticNode(Object object, String label)
	{
		return new StsTreeNode(object, label, true);
	}

	public StsTreeObjectI getTreeNodeObject() { return (StsTreeObjectI)userObject; }
/*
	static StsTreeNode staticNode(String classname, String label)
	{
		StsTreeNodeProxy treeNodeProxy = new StsTreeNodeProxy(classname);
		return new StsTreeNode(treeNodeProxy, label, true);
	}
*/
	static public StsTreeNode constructDynamicNode(StsTreeObjectI object)
	{
		return new StsTreeNode(object, object.getName(), false);
	}

    /*
    static public StsTreeNode constructDynamicNode(StsTreeNode treeNode)
	{
        return constructDynamicNode(treeNode.getTreeNodeObject());
	}

    static public StsTreeNode constructDynamicNode(StsTreeObjectI object)
    {
        return new StsTreeNode(object, object.getName(), false);
    }
     */
	static public StsTreeNode constructDynamicNode(StsObjectPanelClass stsPanelClass)
	{
		StsTreeNode treeNode = new StsTreeNode(stsPanelClass, stsPanelClass.getObjectPanelLabel(), false);
		stsPanelClass.objectPanelNode = treeNode;
		return treeNode;
	}

	public StsTreeNode addDynamicNode(StsTreeObjectI userObject, String label)
	{
		StsTreeNode treeNode = new StsTreeNode(userObject, label, false);
		staticChildren = true;  // a dynamic node with dynamic children is static (children actually have dynamic lists of their children)
		add(treeNode);
		return treeNode;
	}

	public void addChild(StsTreeNode child)
	{
		staticChildren = true;  // a dynamic node with dynamic children is static (children actually have dynamic lists of their children)
		add(child);
	}

	public StsTreeNode addStaticNode(StsTreeObjectI userObject, String label)
	{
		StsTreeNode treeNode = StsTreeNode.staticNode(userObject, label);
		add(treeNode);
		return treeNode;
	}

	public StsTreeNode addStaticNode(String label)
	{
		StsTreeNode treeNode = StsTreeNode.staticNode((StsTreeObjectI) null, label);
		add(treeNode);
		return treeNode;
	}

	public StsTreeNode getParent() { return (StsTreeNode)parent; }

	public boolean hasStaticChildren() { return staticChildren; }

	public StsTreeObjectI getTreeObjectI() { return (StsTreeObjectI)getUserObject(); }

	public boolean isExplored() { return explored; }

	public String toString()
	{
		return label;
	}

	public void explore()
	{
		if(isLeaf()) return;

		if(!isExplored())
		{
			explored = true;
			if(isStatic) return;
			StsTreeObjectI treeObjectI = getTreeObjectI();
			if(treeObjectI == null) return;
			checkAddChildren(treeObjectI);
		}
	}

	boolean checkAddChildren(StsTreeObjectI treeObject)
	{
        int nObjectChildren;

        if(treeObject == null) return false;
		if(staticChildren) return false;
		Object[] childObjects = treeObject.getChildren();
        Enumeration<TreeNode> childNodes = children();

        if(childObjects == null)
            nObjectChildren = 0;
        else
            nObjectChildren = childObjects.length;

        if(childrenOK(childNodes, nObjectChildren)) return false;

		removeAllChildren();

		for(int n = 0; n < nObjectChildren; n++)
		{
			StsTreeObjectI child = (StsTreeObjectI) childObjects[n];
			add(StsTreeNode.constructDynamicNode(child));
		}
		return true;

	}

    private boolean childrenOK(Enumeration<TreeNode> childNodes, int nObjectChildren)
    {
        int nCurrentChildren = getChildCount();
        if(nObjectChildren != nCurrentChildren) return false;
        while(childNodes.hasMoreElements())
        {
            StsTreeNode childNode = (StsTreeNode)childNodes.nextElement();
            if (!childNode.isExplored()) return false;
        }
        return true;
    }

	/* two methods below used for debugging treeNode child add/remove
	  public void add(MutableTreeNode newChild)
	  {
		  int listLocation;

		  if(newChild != null && newChild.getParent() == this)
			  listLocation = getChildCount() - 1;
		  else
			  listLocation = getChildCount();

		 insert(newChild, listLocation);
		 int objectIndex = -1;
		 if(newChild instanceof StsTreeNode)
		 {
			 StsTreeNode treeNode = (StsTreeNode)newChild;
			 Object userObject = treeNode.getUserObject();
			 if(userObject instanceof StsObject)
				 objectIndex = ((StsObject)userObject).index();
		 }
		 System.out.println("StsTreeNode inserted " + newChild.toString() + " index: " + objectIndex + " at list location " + listLocation);
	  }

	  public void remove(int childIndex)
	  {
		  MutableTreeNode child = (MutableTreeNode) getChildAt(childIndex);
		  children.removeElementAt(childIndex);
		  child.setParent(null);
		  int objectIndex = -1;
		  if(child instanceof StsTreeNode)
		   {
			   StsTreeNode treeNode = (StsTreeNode)child;
			   Object userObject = treeNode.getUserObject();
			   if(userObject instanceof StsObject)
				   objectIndex = ((StsObject)userObject).index();
		 }
		 System.out.println("StsTreeNode removed " + child.toString() + " index: " + objectIndex + " at list location " + childIndex);
	  }
 */
/*
    boolean checkAddChildren(boolean check)
    {
        return checkAddChildren((StsTreeObjectI)getUserObject(), check);
    }

    boolean checkAddChildren(StsTreeObjectI treeObject, boolean check)
    {
        if(treeObject == null) return false;
        Object[] children = treeObject.getChildren();
        if(children == null) return false;
        int nObjectChildren = children.length;

        if(check)
        {
            int nCurrentChildren = getChildCount();
            if(nObjectChildren == nCurrentChildren) return false;
        }

        removeAllChildren();

        for(int n = 0; n < nObjectChildren; n++)
        {
            StsTreeObjectI child = (StsTreeObjectI)children[n];
            add(StsTreeNode.constructDynamicNode(child, child.getName()));
        }
        return true;
    }
 */
	public StsObjectPanel getPanel()
	{
		StsTreeObjectI userObject = (StsTreeObjectI) getUserObject();
		if(userObject == null) return null;
		StsObjectPanel panel = userObject.getObjectPanel();
		StsTreeObjectI currentObject = panel.getObject();
		if(currentObject != userObject)
		{
			panel.setObject(userObject);
			userObject.treeObjectSelected();
		}
		return panel;
	}

	protected void selected(StsModel model, JScrollPane detailPane)
	{
		StsTreeObjectI treeObject = (StsTreeObjectI) getUserObject();
		if(treeObject == null) return;
		displayDetailPane(treeObject, detailPane);

		if(treeObject instanceof StsObject)
		{
			StsObject stsObject = (StsObject) treeObject;
			if(stsObject.getIndex() < 0) return;
			StsClass stsClass = model.getCreateStsClass(stsObject);
			stsClass.setCurrentObject(stsObject);
			boolean changed = model.win3d.getCursor3d().setObject(stsObject);
			if(changed) model.win3d.repaint();
			StsComboBoxToolbar toolbar = (StsComboBoxToolbar) model.win3d.getToolbarNamed(StsComboBoxToolbar.NAME);
			if(toolbar != null) toolbar.setSelectedObject(stsObject);
		}
	}

	protected void displayDetailPane(JScrollPane detailPane)
	{
		StsTreeObjectI treeObject = (StsTreeObjectI) getUserObject();
		if(treeObject == null) return;
		displayDetailPane(treeObject, detailPane);
	}

	private void displayDetailPane(StsTreeObjectI treeObject, JScrollPane detailPane)
	{
		StsObjectPanel panel = treeObject.getObjectPanel();
		if(panel == null)
			panel = emptyObjectPanel;
		else
			panel.setObject(treeObject);
		panel.refreshProperties();
		detailPane.getViewport().setView(panel);
	}

	public void refresh(JScrollPane detailPane, int[] selected)
	{
		StsObjectPanel panel = getPanel();
		if(panel == null) return;
		panel.setSelected(selected);
		panel.refreshProperties();
		if(!panel.isVisible()) panel.setVisible(true);
		detailPane.getViewport().setView(panel);
	}

	/*
	 private void selected(StsTreeNode node)
	 {
		 node.selected(model, detailPane);
	 }
 */
	public void expanded(StsModel model, TreePath path, TreeSelectionModel selectionModel, JScrollPane detailPane)
	{
		Object userObject;
		StsObjectPanelClass stsObjectPanelClass;
		StsObject currentObject;
		StsTreeNode currentChildNode;
		try
		{
			userObject = getUserObject();
			if(userObject == null) return;
			if(!(userObject instanceof StsObjectPanelClass)) return;
			stsObjectPanelClass = (StsObjectPanelClass) userObject;
			currentObject = stsObjectPanelClass.getCurrentObject();
			if(currentObject == null) return;
			currentChildNode = getCurrentChildNode();
			if(currentChildNode == null) return;
			TreePath childPath = path.pathByAddingChild(currentChildNode);
			selectionModel.addSelectionPath(childPath);
			currentChildNode.setSelected(model, true, selectionModel, detailPane);
		}
		catch(Exception e)
		{
			StsException.outputException("StsTreeNode.expanded() failed.", e, StsException.WARNING);
		}
	}

	public boolean setSelected(StsModel model, boolean select, TreeSelectionModel selectionModel, JScrollPane detailPane)
	{
		try
		{
			StsTreeObjectI treeObject = (StsTreeObjectI) getUserObject();
			if(treeObject instanceof StsObject)
			{
				model.setCurrentObject((StsObject) treeObject);
			}
			// following doesn't work, but should or could

			TreePath treePath = new TreePath(this);
			if(selectionModel.isPathSelected(treePath)) return false;
			selectionModel.setSelectionPath(treePath);

			displayDetailPane(treeObject, detailPane);
			return true;

//        TreePath childPath = path.pathByAddingChild(currentChildNode);
//        selectionModel.addSelectionPath(childPath);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public StsTreeNode getCurrentChildNode()
	{
		StsObject stsObject = null;
		StsTreeObjectI treeObject = (StsTreeObjectI) getUserObject();
		if(treeObject instanceof StsObjectPanelClass)
		{
			StsObjectPanelClass stsObjectPanelClass = (StsObjectPanelClass) treeObject;
			stsObject = stsObjectPanelClass.getCurrentObject();
			if(stsObject == null) return null;
		}
		Enumeration enumeration = children();
		while (enumeration.hasMoreElements())
		{
			StsTreeNode childNode = (StsTreeNode) enumeration.nextElement();
			if(childNode.getUserObject() == stsObject) return childNode;
		}
		return null;
	}

/*
    public void delete(StsTreeObjectI treeObject)
    {
        if(treeObject.anyDependencies()) return;

        // Generalize these
        if(treeObject instanceof StsSpectrum)
        {
            StsSpectrum spectrum = (StsSpectrum) treeObject;
            spectrum.delete();
        }
        else if(treeObject instanceof StsCrossplot)
        {
            StsCrossplot cp = (StsCrossplot) treeObject;
            cp.delete();
        }
        else if(treeObject instanceof StsSeismicVolume)
        {
            StsSeismicVolume sv = (StsSeismicVolume) treeObject;
            sv.delete();
        }
    }

    public void export(StsTreeObjectI treeObject)
    {
        // Generalize these
        if(treeObject instanceof StsSpectrum)
        {
            StsSpectrum sp = (StsSpectrum) treeObject;
            sp.export();
        }
        else if(treeObject instanceof StsCrossplot)
        {
            StsCrossplot cp = (StsCrossplot) treeObject;
            cp.export();
        }
        else if(treeObject instanceof StsSeismicVolume)
        {
            StsSeismicVolume sv = (StsSeismicVolume) treeObject;
            sv.export();
        }
    }
*/
}
