package com.Sts.Framework.UI.DataFilesPanel;

//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

import com.Sts.Framework.IO.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;

public class StsDataFilesTreePanel extends JPanel implements ChangeListener // , ActionListener
{
	StsModel model = null;
	BorderLayout borderLayout1 = new BorderLayout();
	JSplitPane split = new JSplitPane();
	JScrollPane treePane = new JScrollPane();
	JScrollPane detailPane = new JScrollPane();
	StsFileTreeModel fileTreeModel;
	StsFileTreeNode rootNode;
	StsFileTree fileTree;

	static StsFieldBeanPanel detailPanel = StsFieldBeanPanel.addInsets();
	static StsStringFieldBean urlBean = new StsStringFieldBean(StsFileTreeNode.class, "fileURL", false, "URL: ");
	static
	{
		detailPanel.gbc.anchor = GridBagConstraints.WEST;
		// detailPanel.gbc.fill = GridBagConstraints.HORIZONTAL;
		detailPanel.add(urlBean);
		urlBean.setColumns(50);
		urlBean.setBeanPanel(detailPanel);
	}
	private StsMenuItem objectLoadBtn = new StsMenuItem();

	static double treePanePortion = 0.5; // 1.0 if treePane takes all the space

	public StsDataFilesTreePanel(StsModel model)
	{
		this.model = model;
		fileTree = StsFileTree.constructor(model);
		fileTreeModel = (StsFileTreeModel)fileTree.getModel();
		rootNode = fileTreeModel.getRootNode();
		split.setOrientation(0);
		split.setOneTouchExpandable(true);

		setLayout(borderLayout1);
		add(split, BorderLayout.CENTER);

		split.add(treePane, JSplitPane.TOP);
		split.add(detailPane, JSplitPane.BOTTOM);

		split.setResizeWeight(treePanePortion); // top component gets proportion of the extra space

		treePane.add(fileTree);
		treePane.getViewport().setView(fileTree);
	}

/*
	private void expanded(TreePath treePath, StsFileTreeNode node)
	{
		node.expanded(model, treePath, selectionModel, detailPane);
	}
*/
	private void addObjectPopup(int x, int y)
	{
		JPopupMenu tp = new JPopupMenu("Object Popup");
		this.add(tp);
		tp.add(objectLoadBtn);
		tp.show(this, x, y);
	}

	public StsAbstractFile[] getSelectedFiles()
	{
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		if(selectionPaths == null) return new StsAbstractFile[0];
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return new StsAbstractFile[0];
		StsAbstractFile[] abstractFiles = new StsAbstractFile[nNodes];
		int nFiles = 0;
		for(int i = 0; i < nNodes; i++)
		{
			StsFileTreeNode treeNode = (StsFileTreeNode)selectionPaths[i].getLastPathComponent();
			abstractFiles[i] = (StsAbstractFile)treeNode.getUserObject();
		}
		return abstractFiles;
	}

	public void setDetailPanelFileNode()
	{
		TreePath[] selectionPaths = fileTree.getSelectionPaths();
		if(selectionPaths == null) return;
		int nNodes = selectionPaths.length;
		if(nNodes == 0) return;

		StsFileTreeNode treeNode = (StsFileTreeNode) selectionPaths[0].getLastPathComponent();
		setDetailBeanObject(treeNode);
	}

	public void stateChanged(ChangeEvent e)
	{
		// refreshTree();
	}

	public void setDetailBeanObject(StsFileTreeNode treeNode)
	{
		detailPanel.setBeanObject(treeNode);
		detailPane.getViewport().setView(detailPanel);
	}

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
		fileTree.setVisible(false);
		try
		{
			TreePath lastNodePath = null;
			if(rootNode == null) return;
			fileTreeModel.nodeStructureChanged(treeNode);
			lastNodePath = new TreePath(treeNode.getPath());
			fileTree.scrollPathToVisible(lastNodePath);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "doRefreshTreeNode", e);
		}
		finally
		{
			fileTree.setVisible(true);
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

	//TODO needs rework as commented out lines below cause looping/slow enumeration
	private void doRefreshTree()
	{
		fileTree.setVisible(false);
		try
		{
			TreePath lastNodePath = null;
			if(fileTreeModel == null) return;
			Enumeration enumeration = rootNode.breadthFirstEnumeration();
			while (enumeration.hasMoreElements())
			{
				StsFileTreeNode treeNode = (StsFileTreeNode) enumeration.nextElement();
				if(treeNode.getAllowsChildren())
				//if(treeNode.checkAddChildren(fileTree))
				{
					//fileTreeModel.nodeStructureChanged(treeNode);
					lastNodePath = new TreePath(treeNode.getPath());
				}
			}
			if(lastNodePath != null) fileTree.scrollPathToVisible(lastNodePath);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "doRefreshTree()", e);
		}
		finally
		{
			fileTree.setVisible(true);
		}
	}

	public void setUploader(StsFileUploaderFace uploader)
	{
		fileTree.setUploader(uploader);
	}
/*
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
								fileTreeModel.insertNodeInto(child, parent, parent.getChildCount());
//					doRefreshTreeNode(parent);
							}
						}
				);
	}
*/
	static public void main(String[] args)
	{
		try
		{
			StsModel model = StsModel.constructor("g:\\Qtest\\");
			StsDataFilesTreePanel panel = new StsDataFilesTreePanel(model);
			StsToolkit.createDialog(panel, false, 100, 300);

			//StsWell well = new StsWell("WellOne", false);
			//well.addToModel();
			// panel.refreshTree();
		}
		catch(Exception e) { e.printStackTrace(); }
	}
}


