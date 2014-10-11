package com.Sts.Framework.UI.ObjectPanel;

//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

import com.Sts.Framework.Actions.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class StsObjectTreePanel extends JPanel implements ChangeListener // , ActionListener
{
	StsModel model = null;
	StsMouse mouse = StsMouse.getInstance();

	BorderLayout borderLayout1 = new BorderLayout();
	JSplitPane split = new JSplitPane();
	JScrollPane treePane = new JScrollPane();
	JScrollPane detailPane = new JScrollPane();
	JTree tree;
	DefaultTreeModel treeModel;
	TreeSelectionModel selectionModel;
	public StsTreeNode rootNode, cropNode, dataNode, modelNode;
	Component selectedComponent = null;

	int currentButton = StsMouse.NONE;

	private StsMenuItem objectDeleteBtn = new StsMenuItem();
	private StsMenuItem objectExportBtn = new StsMenuItem();
	private StsMenuItem objectExportViewBtn = new StsMenuItem();
	private StsMenuItem objectLaunchBtn = new StsMenuItem();
	private StsMenuItem objectGoToBtn = new StsMenuItem();

	static double treePanePortion = 0.5; // 1.0 if treePane takes all the space

	private StsTreeNode runTreeNode = null;

	public StsObjectTreePanel(StsModel model)
	{
		this.model = model;

// Setting preferred size causes problems with splitPane division line location
//      int treePaneHeight = (int)(treePanePortion*size.height);
//      int detailPaneHeight = size.height - treePaneHeight;
//		detailPane.setPreferredSize(new Dimension(size.width, treePaneHeight));
//		treePane.setPreferredSize(new Dimension(size.width, detailPaneHeight));

		split.setOrientation(0);
		split.setOneTouchExpandable(true);

		setLayout(borderLayout1);
		add(split, BorderLayout.CENTER);

		split.add(treePane, JSplitPane.TOP);
		split.add(detailPane, JSplitPane.BOTTOM);

		split.setResizeWeight(treePanePortion); // top component gets proportion of the extra space

		createTreeModel(model);
		tree = new JTree(treeModel);
		tree.setEditable(true);
//        treeModel = (DefaultTreeModel)tree.getModel();
		treePane.add(tree);
		treePane.getViewport().setView(tree);

		selectionModel = tree.getSelectionModel();
		selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setDragEnabled(true);
		tree.setScrollsOnExpand(true);

		tree.addTreeSelectionListener(new TreeSelectionListener()
		{

			public void valueChanged(TreeSelectionEvent e)
			{
				setPanelObjects();
			}
			/*
						public void valueChanged(TreeSelectionEvent e)
						{
							TreePath path = e.getPath();
							StsTreeNode node = (StsTreeNode)path.getLastPathComponent();
							selected(node);
						}
				*/
		});

		tree.addTreeExpansionListener(new TreeExpansionListener()
		{
			public void treeCollapsed(TreeExpansionEvent e)
			{
			}

			public void treeExpanded(TreeExpansionEvent e)
			{
				TreePath path = e.getPath();
				StsTreeNode node = (StsTreeNode) path.getLastPathComponent();

				if(!node.isExplored())
				{
					node.explore();
					treeModel.nodeStructureChanged(node);
				}
				expanded(path, node);
//                node.expanded(path, selectionModel);
			}
		});

		tree.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				boolean selected;
				mouse.setState(e);
				if(mouse.currentButton == StsMouse.POPUP)
				{
					int row = tree.getRowForLocation(e.getX(), e.getY());
					if(row != -1)
					{
						TreePath path = tree.getPathForRow(row);
						StsTreeNode treeNode = (StsTreeNode) path.getLastPathComponent();
						if(tree.getSelectionCount() == 0)
							selected = treeNode.setSelected(StsObjectTreePanel.this.model, true, selectionModel, detailPane);
						setPanelObjects();
						addObjectPopup(e.getX(), e.getY() - treePane.getVerticalScrollBar().getValue());
					}
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				mouse.setState(e);
			}
		});

		objectDeleteBtn.setMenuActionListener("Delete", this, "delete", null);
		objectExportBtn.setMenuActionListener("Export", this, "export", null);
		objectExportViewBtn.setMenuActionListener("Export View", this, "exportView", null);
		objectLaunchBtn.setMenuActionListener("Launch", this, "launch", null);
		objectGoToBtn.setMenuActionListener("Go To", this, "goTo", null);
	}

	public boolean createTreeModel(StsModel model)
	{
		try
		{
			rootNode = createRootNode(model.getProject(), "Project");
			cropNode = rootNode.addDynamicNode(model.getProject().getCropVolume(), "Crop Volume");
			dataNode = rootNode.addStaticNode("Data");
			modelNode = rootNode.addStaticNode("Model");

			for(StsClass stsClass : model.classList)
			{
				if(!(stsClass instanceof StsObjectPanelClass)) continue;
				StsObjectPanelClass stsPanelClass = (StsObjectPanelClass) stsClass;
				StsTreeNode classTreeNode = stsPanelClass.addDynamicNode(this);
                if(classTreeNode != null)
				    classTreeNode.checkAddChildren(stsPanelClass);

			}
			treeModel = new DefaultTreeModel(rootNode);
//			finalizeTreeModel();
//			refreshTree();
			return true;

		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "createObjectPanelNodes", e);
			return false;
		}
	}

	private Object[] getPanelObjects()
	{
		StsTreeNode treeNode;
		StsTreeObjectI treeObject;
		Class treeObjectClass;

		TreePath[] selectionPaths = tree.getSelectionPaths();
		if(selectionPaths == null) return null;
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return null;

		treeNode = (StsTreeNode) selectionPaths[0].getLastPathComponent();
		treeObject = treeNode.getTreeNodeObject();
		StsObjectPanel panel = treeObject.getObjectPanel();
		return panel.getPanelObjects();
	}

	private void setPanelObjects()
	{
		StsTreeNode treeNode;
		StsTreeObjectI treeObject;
		Class treeObjectClass;

		TreePath[] selectionPaths = tree.getSelectionPaths();
		if(selectionPaths == null) return;
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return;

		treeNode = (StsTreeNode) selectionPaths[0].getLastPathComponent();
		treeNode.displayDetailPane(detailPane);
		if(treeNode.isStatic) return;

		treeObject = treeNode.getTreeNodeObject();
		if(treeObject instanceof StsObject)
			setCurrentObject((StsObject) treeObject);

		StsObjectPanel panel = treeObject.getObjectPanel();
		if(panel == null) return;

		if(nNodes > 1)
		{
			treeObjectClass = treeObject.getClass();
			StsTreeObjectI[] treeObjects = new StsTreeObjectI[nNodes];
			treeObjects[0] = treeObject;
			for(int n = 1; n < nNodes; n++)
			{
				treeNode = (StsTreeNode) selectionPaths[n].getLastPathComponent();
				treeObjects[n] = treeNode.getTreeNodeObject();
				if(treeObjects[n].getClass() != treeObjectClass)
				{
//                    new StsMessage(null, StsMessage.ERROR, "Selected items must all be of the same class.");
					return;
				}
			}
			panel.setObjects(treeObjects);
		}
		//panel.repaint();
	}

	private void setCurrentObject(StsObject stsObject)
	{
		if(stsObject.getIndex() < 0) return; // not persistent
		StsClass stsClass = model.getCreateStsClass(stsObject);
//        boolean changed = model.win3d.glPanel3d.cursor3d.setObject(stsObject);
		if(!stsClass.setCurrentObject(stsObject)) return;
//        if(changed) model.win3d.glPanel3d.repaint();
	}

	/*
	 private void setCurrentObject(StsObject stsObject)
	 {
		 if(stsObject.getIndex() < 0) return; // not persistent
		 StsClass stsClass = model.getCreateStsClass(stsObject);
		 boolean changed = model.win3d.glPanel3d.cursor3d.setObject(stsObject);
		 if(!stsClass.setCurrentObject(stsObject)) return;
		 if(changed) model.win3d.glPanel3d.repaint();
		 StsComboBoxToolbar toolbar = (StsComboBoxToolbar)model.win3d.getToolbarNamed("Object Selection Toolbar");
		 if(toolbar != null) toolbar.setSelectedObject(stsObject);
	 }
 */
	public void selected(StsObject object)
	{
		if(object == null) return;
		StsObject[] selectedObjects = getSelectedObjects();
		if(selectedObjects.length == 0) return;
		if(object == selectedObjects[0]) return;
		Enumeration enumeration = rootNode.depthFirstEnumeration();
		while (enumeration.hasMoreElements())
		{
			StsTreeNode treeNode = (StsTreeNode) enumeration.nextElement();
			if(treeNode.getUserObject() == object)
			{
				boolean selected = treeNode.setSelected(model, true, selectionModel, detailPane);
//                if(selected) repaint(); // doesn't seem to display selected node
			}
		}
	}

	public void classCursorState(StsObject object)
	{
		System.out.println("Toggle the cursor on/off...........");
	}

	private void displayDetailPane(StsTreeNode node)
	{
		node.displayDetailPane(detailPane);
	}

	private void selected(StsTreeNode node)
	{
		node.selected(model, detailPane);
	}

	private int[] getSelectedIndices()
	{
		int[] selectedIndices = null;
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if(selectionPaths != null)
		{
			StsTreeNode firstChild = (StsTreeNode) selectionPaths[0].getLastPathComponent();
			StsTreeNode parent = (StsTreeNode) firstChild.getParent();
			int[] selected = new int[selectionPaths.length];
			int numSelected = 0;
			for(int i = 0; i < selectionPaths.length; i++)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						(selectionPaths[i].getLastPathComponent());
				if(node instanceof StsTreeNode)
				{
					if(parent != null && node.getParent() == parent)
					{
						selected[numSelected] = parent.getIndex(node);
						numSelected++;
					}
				}
			}

			if(numSelected > 0)
			{
				selectedIndices = new int[numSelected];
				System.arraycopy(selected, 0, selectedIndices, 0, numSelected);
			}
		}
		return selectedIndices;
	}

	private void expanded(TreePath treePath, StsTreeNode node)
	{
		node.expanded(model, treePath, selectionModel, detailPane);
	}

	/*
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		TreePath[] selectionPaths = tree.getSelectionPaths();

		if(source == objectDeleteBtn)
		{
			for(int i=0; i<selectionPaths.length; i++)
			{
				TreeNode item = selectionPaths == null ? null : (TreeNode) selectionPaths[i].getLastPathComponent();
				if (item instanceof StsTreeNode)
				{
					StsTreeNode node = (StsTreeNode) item;
					node.delete( (StsTreeObjectI) node.getUserObject());
				}
			}
			refreshTree();
		}
		else if(source == objectExportBtn)
		{
			for(int i=0; i<selectionPaths.length; i++)
			{
				TreeNode item = selectionPaths == null ? null : (TreeNode) selectionPaths[i].getLastPathComponent();
				if (item instanceof StsTreeNode)
				{
					StsTreeNode node = (StsTreeNode) item;
					node.export( (StsTreeObjectI) node.getUserObject());
				}
			}
		}
	}
 */
	private void addObjectPopup(int x, int y)
	{
		JPopupMenu tp = new JPopupMenu("Object Popup");
		this.add(tp);
		StsObject[] selected = getSelectedObjects();
		if(selected.length != 0)
		{
			tp.add(objectDeleteBtn);
			if(selected[0].canExport())
				tp.add(objectExportBtn);
			tp.add(objectGoToBtn);
			if(selected[0].canLaunch())
				tp.add(objectLaunchBtn);
		}
		else
		{
			StsObjectPanelClass selectedObjectPanelClass = getSelectedClass();
			if(selectedObjectPanelClass != null && selectedObjectPanelClass.canExportView())
				tp.add(objectExportViewBtn);
		}
		tp.show(this, x, y);
	}

	public void stateChanged(ChangeEvent e)
	{
		refreshTree();
	}
/*
	public void run()
	{
		if(runTreeNode == null)
			doRefreshTree();
		else
			doRefreshTreeNode(runTreeNode);
	}
*/
/*
	public void refreshTreeNode(StsTreeObjectI userObject)
	{
		runUserObject = userObject;
		StsToolkit.runLaterOnEventThread(this);
	}
*/
	public void refreshTreeNode(StsTreeNode treeNode)
	{
		final StsTreeNode runTreeNode = treeNode;

		StsToolkit.runLaterOnEventThread
		(
			new Runnable()
			{
				public void run()
				{
					doRefreshTreeNode(runTreeNode);
				}
			}
		);
	}

	public void doRefreshTreeNode(StsTreeNode treeNode)
	{
		tree.setVisible(false);
		try
		{
			TreePath lastNodePath = null;
			if(rootNode == null) return;
			treeModel.nodeStructureChanged(treeNode);
			lastNodePath = new TreePath(treeNode.getPath());
			tree.scrollPathToVisible(lastNodePath);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "doRefreshTreeNode", e);
		}
		finally
		{
			tree.setVisible(true);
		}
	}

	public void refreshTree()
	{
		StsToolkit.runLaterOnEventThread
		(
			new Runnable()
			{
				public void run()
				{
					doRefreshTree();
				}
			}
		);
	}

	private void doRefreshTree()
	{
		tree.setVisible(false);
		try
		{
			TreePath lastNodePath = null;
			if(treeModel == null) return;
			Enumeration enumeration = rootNode.breadthFirstEnumeration();
			while (enumeration.hasMoreElements())
			{
				StsTreeNode treeNode = (StsTreeNode) enumeration.nextElement();
				StsTreeObjectI treeObject = treeNode.getTreeObjectI();
				//			if(treeNode.checkAddChildren(treeObject))
				if(!treeNode.isStatic && treeNode.checkAddChildren(treeObject))
				{
					treeModel.nodeStructureChanged(treeNode);
					lastNodePath = new TreePath(treeNode.getPath());
				}
			}
			if(lastNodePath != null) tree.scrollPathToVisible(lastNodePath);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "doRefreshTree()", e);
		}
		finally
		{
			tree.setVisible(true);
		}
	}

	public void addNodeToParent(StsTreeNode childNode, StsTreeNode parentNode)
	{
		final StsTreeNode child = childNode;
		final StsTreeNode parent = parentNode;
		StsToolkit.runLaterOnEventThread
		(
			new Runnable()
			{
				public void run()
				{
					treeModel.insertNodeInto(child, parent, parent.getChildCount());
//					doRefreshTreeNode(parent);
				}
			}
		);
	}

	/*
	 public void refreshTreeNode(StsClass stsClass)
	 {
		 if(!StsTreeObjectI.class.isAssignableFrom(stsClass.getClass())) return;
		 runUserObject = (StsTreeObjectI)stsClass;
		 StsToolkit.runLaterOnEventThread(this);
	 }
 */
/*
	private void doRefreshTreeNode(StsTreeObjectI userObject)
	{
		tree.setVisible(false);

		TreePath lastNodePath = null;
		if(rootNode == null) return;
		Enumeration enumeration = rootNode.breadthFirstEnumeration();
		while (enumeration.hasMoreElements())
		{
			StsTreeNode treeNode = (StsTreeNode) enumeration.nextElement();
			if(treeNode.getUserObject() == userObject)
//             if(!treeNode.isStatic && treeNode.getUserObject() == userObject)
			{
				treeNode.addChildren(userObject);
				treeModel.nodeStructureChanged(treeNode);
				lastNodePath = new TreePath(treeNode.getPath());
				break;
			}
		}
		if(lastNodePath != null) tree.scrollPathToVisible(lastNodePath);
		tree.setVisible(true);
	}
*/

	/*
	 public void addListeners(StsActionManager actionManager)
	 {
		 actionManager.addChangeListener(this);
	 }
 */
	public StsTreeNode createRootNode(StsTreeObjectI userObject, String label)
	{
		//       rootNode = StsTreeNode.staticNode(model.getProject(), "Project");
		rootNode = StsTreeNode.staticNode(userObject, label);
		return rootNode;
	}

	public void setRootNode(StsTreeNode node)
	{
		rootNode = node;
	}

	/*
	 public void initializeTreeModel(StsModel model)
	 {
		 rootNode = StsTreeNode.staticNode(model.getProject(), "Project");
		 rootNode.addStaticNode(model.getProject().getCropVolume(), "Crop PostStack3d");
		 dataNode = rootNode.addStaticNode(null, "Data");
		 modelNode = rootNode.addStaticNode(null, "Model");
		 rootNode.addStaticNode(null, "SubVolumes");
	 }
 */
/*
    public void initializeTreeModel(StsModel model)
    {
        rootNode = StsTreeNode.staticNode(model.getProject(), "Project");

        StsTreeNode subVolumeNode = StsTreeNode.staticNode(model.getProject().getCropVolume(), "Crop PostStack3d");
        rootNode.add(subVolumeNode);

        dataNode = StsTreeNode.staticNode(null, "Data");
        rootNode.add(dataNode);

        modelNode = StsTreeNode.staticNode(null, "Model");
        rootNode.add(modelNode);

        StsTreeNode dataSubVolumeNode = StsTreeNode.staticNode(null, "SubVolumes");
        rootNode.add(modelNode);
    }
*/
/*
    public StsTreeNode addStaticNode(StsTreeObjectI userObject, String label, StsTreeNode parentNode)
    {
        StsTreeNode treeNode = StsTreeNode.staticNode(userObject, label);
        if(parentNode != null) parentNode.add(treeNode);
        return treeNode;
    }

    public void addDataNode(StsTreeObjectI object, String label)
    {
        StsTreeNode node = StsTreeNode.constructDynamicNode(object, label);
        dataNode.add(node);
    }

    public void addModelNode(StsTreeObjectI object, String label)
    {
        StsTreeNode node = StsTreeNode.constructDynamicNode(object, label);
        modelNode.add(node);
    }
*/
	public void finalizeTreeModel()
	{
		treeModel = new DefaultTreeModel(rootNode);
		rootNode.explore();
		tree.setModel(treeModel);
		tree.setSelectionRow(0);
	}

	/*
	 public void createTreeModel(StsModel model)
	 {
		 rootNode = StsTreeNode.staticNode(model.getProject(), "Project");

		 StsTreeNode subVolumeNode = StsTreeNode.staticNode(model.getProject().getCropVolume(), "Crop PostStack3d");
		 rootNode.add(subVolumeNode);
		 StsTreeNode dataNode = StsTreeNode.staticNode(null, "Data");
		 rootNode.add(dataNode);
		 StsTreeNode modelNode = StsTreeNode.staticNode(null, "Model");
		 rootNode.add(modelNode);

		 // Actual Objects from files or databases
		 StsTreeNode seismicVolumesNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSeismicVolume.class), "Seismic Volumes");
		 dataNode.add(seismicVolumesNode);
		 StsTreeNode surfacesNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSurface.class), "Surfaces");
		 dataNode.add(surfacesNode);
		 StsTreeNode wellsNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsWell.class), "Wells");
		 dataNode.add(wellsNode);
		 StsTreeNode spectrumsNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSpectrum.class), "Spectrums");
		 dataNode.add(spectrumsNode);

		 // Constructed Objects from other objects
		 StsTreeNode crossplotNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsCrossplot.class), "Cross Plots");
		 dataNode.add(crossplotNode);
		 StsTreeNode virtualVolumesNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsVirtualSeismicVolume.class), "Virtual Volumes");
		 dataNode.add(virtualVolumesNode);

		 StsTreeNode dataSubVolumeNode = StsTreeNode.staticNode(null, "SubVolumes");
		 dataNode.add(dataSubVolumeNode);
		 StsTreeNode polyhedronNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSubVolume.class), "Polyhedron");
		 dataSubVolumeNode.add(polyhedronNode);
		 StsTreeNode singleSurfaceNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSubVolume.class), "Single Surface");
		 dataSubVolumeNode.add(singleSurfaceNode);
		 StsTreeNode dualSurfaceNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsSubVolume.class), "Dual Surface");
		 dataSubVolumeNode.add(dualSurfaceNode);

		 // Constructed Objects from the environment
		 StsTreeNode moviesNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsMovie.class), "Movies");
		 dataNode.add(moviesNode);
		 StsTreeNode weighPointsNode = StsTreeNode.constructDynamicNode(model.getCreateStsClass(StsWeighPoint.class), "Weigh Points");
		 dataNode.add(weighPointsNode);



		 treeModel = new DefaultTreeModel(rootNode);
		 rootNode.explore();
		 tree.setModel(treeModel);
		 tree.setSelectionRow(0);
	 }
 */
	public void delete()
	{
		StsObject[] selectedObjects = getSelectedObjects();
		int nObjects = selectedObjects.length;
		if(nObjects == 0) return;
		StsActionManager actionManager = model.mainWindowActionManager;
		actionManager.startAction(StsDeleteAction.class, new Object[]{selectedObjects});
	}

	public void goTo()
	{
		StsObject[] selectedObjects = getSelectedObjects();
		if(selectedObjects.length == 0) return;
		selectedObjects[0].goTo();
	}

//    public void activate()
//    {
//        StsObject[] selectedObjects = getSelectedObjects();
//        int nObjects = selectedObjects.length;
//        if(nObjects == 0) return;
//        selectedObjects[0].makeCurrent();
//    }

	public void exportView()
	{
		StsObjectPanelClass selectedObjectPanelClass = getSelectedClass();
		if(selectedObjectPanelClass == null) return;
		selectedObjectPanelClass.exportView();
	}

	public void export()
	{
		StsObject[] selectedObjects = getSelectedObjects();
		int nObjects = selectedObjects.length;
		if(nObjects == 0) return;
		for(int n = 0; n < nObjects; n++)
			selectedObjects[n].export();
	}

	public void launch()
	{
		StsObject[] selectedObjects = getSelectedObjects();
		int nObjects = selectedObjects.length;
		if(nObjects == 0) return;
		for(int n = 0; n < nObjects; n++)
			selectedObjects[n].launch();
	}

	public StsObjectPanelClass getSelectedClass()
	{
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if(selectionPaths == null) return null;
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return null;
		for(int i = 0; i < nNodes; i++)
		{
			StsTreeNode treeNode = (StsTreeNode) selectionPaths[i].getLastPathComponent();
			if(!treeNode.isStatic)
			{
				Object userObject = treeNode.getUserObject();
				if(userObject instanceof StsObjectPanelClass)
				{
					return (StsObjectPanelClass) userObject;
				}
			}
		}
		return null;
	}

	public StsObject[] getSelectedObjects()
	{
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if(selectionPaths == null) return new StsObject[0];
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return new StsObject[0];
		StsObject[] objects = new StsObject[nNodes];
		int nObjects = 0;
		for(int i = 0; i < nNodes; i++)
		{
			StsTreeNode treeNode = (StsTreeNode) selectionPaths[i].getLastPathComponent();
			if(!treeNode.isStatic)
			{
				Object userObject = treeNode.getUserObject();
				if(userObject instanceof StsObject)
				{
					boolean hasObject = false;
					for(int ii = 0; ii < i; ii++)
						if(objects[ii] == userObject) hasObject = true;
					if(!hasObject) objects[nObjects++] = (StsObject) userObject;
				}
			}
		}
		return (StsObject[]) StsMath.trimArray(objects, nObjects);
	}

	static public void main(String[] args)
	{
		try
		{
			StsModel model = StsModel.constructor("ObjectTreeTest");
			StsObjectTreePanel panel = new StsObjectTreePanel(model);
			model.objectTreePanel = panel;
			StsToolkit.createDialog(panel, false);
			// panel.createTreeModel(model);
			//StsWell well = new StsWell("WellOne", false);
			//well.addToModel();
			// panel.refreshTree();
		}
		catch(Exception e) { e.printStackTrace(); }
	}
}


