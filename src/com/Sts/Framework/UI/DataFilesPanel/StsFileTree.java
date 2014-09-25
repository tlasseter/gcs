package com.Sts.Framework.UI.DataFilesPanel;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 4/25/12
 */
public class StsFileTree extends JTree
{
	StsModel model;
	private StsMenuItem objectLoadBtn = new StsMenuItem();
	private StsMenuItem fileUploadBtn = new StsMenuItem();
	JPopupMenu popup;
	StsMouse mouse = StsMouse.getInstance();
	StsFileUploaderFace uploader = null;

	private StsFileTree(StsModel model, StsFileTreeModel fileTreeModel)
	{
		super(fileTreeModel);
		this.model = model;
		setEditable(true);
		setShowsRootHandles(true);
		setDragEnabled(true);
		setScrollsOnExpand(true);

		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		// define the popup
		objectLoadBtn.setMenuActionListener("Load", this, "load", null);
		fileUploadBtn.setMenuActionListener("Upload", this, "upload", null);
		popup = new JPopupMenu("Object Popup");
		popup.add(objectLoadBtn);
		popup.add(fileUploadBtn);
		popup.setOpaque(true);
		popup.setLightWeightPopupEnabled(true);
		addMouseListener(new ShowPopupMouseListener());
//		addListeners();

		//Set the magic property which makes the first click outside the popup
		//capable of selecting tree nodes, as well as dismissing the popup.
		UIManager.put("PopupMenu.consumeEventOnClose", Boolean.FALSE);
	}

	static public StsFileTree constructor(StsModel model)
	{
		String rootPath = model.getProject().getStsDataDirString();
		StsFileTreeNode rootNode = new StsFileTreeNode(rootPath);
		StsFileTreeModel fileTreeModel = new StsFileTreeModel(rootNode);
		return new StsFileTree(model, fileTreeModel);
		//.setEditable(true);
		//fileTree.setShowsRootHandles(true);
		//fileTree.setDragEnabled(true);
		//fileTree.setScrollsOnExpand(true);
	}

	public StsFileTreeNode addChild(StsFileTreeNode parent, StsFileTreeNode child, boolean shouldBeVisible)
	{
		//It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
		((StsFileTreeModel)treeModel).insertNodeInto(child, parent, parent.getChildCount());

		//Make sure the user can see the lovely new node.
//		if(shouldBeVisible)
//		{
//			scrollPathToVisible(new TreePath(child.getPath()));
//		}
		return child;
	}

	private class ShowPopupMouseListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			showMenuIfPopupTrigger(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			showMenuIfPopupTrigger(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			showMenuIfPopupTrigger(e);
		}

		private void showMenuIfPopupTrigger(final MouseEvent e)
		{
			mouse.setState(e);
			if(mouse.currentButton == StsMouse.POPUP)
			{
				//set the new selections before showing the popup
				setSelectedItemsOnPopupTrigger(e);
				//show the menu, offsetting from the mouse click slightly
				popup.show((Component) e.getSource(), e.getX() + 3, e.getY() + 3);
			}
		}

		/**
		 * Fix for right click not selecting tree nodes -
		 * We want to implement the following behaviour which matches windows explorer:
		 * If the item under the click is not already selected, clear the current selections and select the
		 * item, prior to showing the popup.
		 * If the item under the click is already selected, keep the current selection(s)
		 */
		private void setSelectedItemsOnPopupTrigger(MouseEvent e)
		{
			TreePath p = getPathForLocation(e.getX(), e.getY());
			if(!getSelectionModel().isPathSelected(p))
			{
				getSelectionModel().setSelectionPath(p);
			}
		}
	}
/*
	private void addListeners()
	{
		addTreeSelectionListener(new TreeSelectionListener()
		{

			public void valueChanged(TreeSelectionEvent e)
			{
				if(filesTreePanel != null) filesTreePanel.setDetailPanelFileNode();
			}
		});

		addTreeExpansionListener(new TreeExpansionListener()
		{
			public void treeCollapsed(TreeExpansionEvent e)
			{
			}

			public void treeExpanded(TreeExpansionEvent e)
			{
				TreePath path = e.getPath();
				StsFileTreeNode node = (StsFileTreeNode) path.getLastPathComponent();

				if(!node.isExplored())
				{
					node.explore();
					((StsFileTreeModel) getModel()).nodeStructureChanged(node);
				}
				expanded(path, node);
//                node.expanded(path, selectionModel);
			}
		});
	}

	private void expanded(TreePath treePath, StsFileTreeNode node)
	{
		// node.expanded(model, treePath, selectionModel, detailPane);
	}
*/
	public void load()
	{
		ArrayList<StsFileTreeNode> selectedNodes = new ArrayList<StsFileTreeNode>();
		for(TreePath p : getSelectionPaths())
		{
			StsFileTreeNode treeNode = (StsFileTreeNode)p.getLastPathComponent();
			System.out.println("load: " + treeNode.toString());
			treeNode.selected = true;
			selectedNodes.add(treeNode);
		}
		ArrayList<StsAbstractFile> loadableFiles = new ArrayList<StsAbstractFile>();
		for(StsFileTreeNode treeNode : selectedNodes)
		{
			if(treeNode.ancestorSelected()) continue;
			StsAbstractFile file = treeNode.getFile();
			String group = file.getGroup();
			String format = StsLoader.FORMAT_TXT;
			getLoadableFiles(file, group, format, loadableFiles);
		}
		for(StsAbstractFile loadableFile : loadableFiles)
			StsLoader.loadStsFile(model, loadableFile, true);

		model.getProject().completeLoading();
	}

	public void getLoadableFiles(StsAbstractFile file, String group, String format, ArrayList<StsAbstractFile> loadableFiles)
	{
		if(file.isAFile())
		{
			if(file.isGroupAndFormat(group, format))
				loadableFiles.add(file);
		}
		else
		{
			StsAbstractFile[] childFiles = file.getDirectoryFiles();
			for(StsAbstractFile childFile : childFiles)
            {
                if(group.equals(StsProject.DATA_FOLDER)) // this is data root directory; get groups from subdirectories
                    getLoadableFiles(childFile, childFile.getGroup(), format, loadableFiles);
                else
                    getLoadableFiles(childFile, group, format, loadableFiles);
            }
		}
	}

	public void upload()
	{
		if(uploader == null)
		{
			new StsMessage(null, StsMessage.ERROR, "No uploader available.");
			return;
		}
		ArrayList<StsFileTreeNode> selectedNodes = new ArrayList<StsFileTreeNode>();
		for(TreePath p : getSelectionPaths())
		{
			StsFileTreeNode treeNode = (StsFileTreeNode)p.getLastPathComponent();
			System.out.println("load: " + treeNode.toString());
			treeNode.selected = true;
			selectedNodes.add(treeNode);
		}
		ArrayList<StsAbstractFile> loadableFiles = new ArrayList<StsAbstractFile>();
		for(StsFileTreeNode treeNode : selectedNodes)
		{
			if(treeNode.ancestorSelected()) continue;
			StsAbstractFile file = treeNode.getFile();
			String group = file.getGroup();
			String format = StsLoader.FORMAT_TXT;
			getLoadableFiles(file, group, format, loadableFiles);
		}
		for(StsAbstractFile loadableFile : loadableFiles)
		{
			try
			{
				uploader.doUpload(loadableFile.getFile(), loadableFile.getPathname());
			}
			catch(Exception e)
			{
				StsException.outputWarningException(this, "upload", "Failed to upload: " + loadableFile.getPathname(), e);
			}
		}

		model.getProject().completeLoading();
	}

	public void setUploader(StsFileUploaderFace uploader)
	{
		this.uploader = uploader;
	}

	public static void main(String[] args)
	{
		StsModel model = StsModel.constructor("g:\\Qtest");
		StsFileTree fileTree = StsFileTree.constructor(model);
		StsDataFilesTreePanel filesTreePanel = new StsDataFilesTreePanel(model);
		filesTreePanel.treePane.add(fileTree);
		filesTreePanel.treePane.getViewport().setView(fileTree);
		JScrollPane scrollPane = new JScrollPane(filesTreePanel);
		StsToolkit.createDialog(scrollPane, true, 400, 600);
	}
}
