package com.Sts.Framework.UI.DataFilesPanel;

import com.Sts.Framework.IO.*;

import javax.swing.tree.*;
import java.nio.file.*;

/**
 * The methods in this class allow the JTree component to traverse
 * the file system tree, and display the files and directories.
 */
class StsFileTreeModel extends DefaultTreeModel
{
	public StsFileTreeModel(StsFileTreeNode rootNode)
	{
		super(rootNode);
	}

	// Tell JTree whether an object in the tree is a leaf or not
	public boolean isLeaf(Object node) { return ((StsFileTreeNode)node).isFile(); }

	public StsFileTreeNode getRootNode() { return (StsFileTreeNode)getRoot(); }

	// Tell JTree how many children a node has
	public int getChildCount(Object parent)
	{
		StsAbstractFile file = ((StsFileTreeNode)parent).getFile();
		StsAbstractFile[] children = file.listFiles();
		if(children == null) return 0;
		return children.length;
	}

	// Fetch any numbered child of a node for the JTree.
	// Our model returns File objects for all nodes in the tree.  The
	// JTree displays these by calling the File.toString() method.
	public Object getChild(Object parentDirectory, int index)
	{
		StsAbstractFile[] children = ((StsFileTreeNode) parentDirectory).getDirectoryFiles();
		if((children == null) || (index >= children.length)) return null;
		return new StsFileTreeNode(children[index]);
	}

	// Figure out a child's position in its parent node.
	public int getIndexOfChild(Object parent, Object child)
	{
		StsAbstractFile[] children = ((StsFileTreeNode) parent).getDirectoryFiles();
		if(children == null) return -1;
		Path childPath = ((StsFileTreeNode)child).getFile().getPath();
		for(int i = 0; i < children.length; i++)
		{
			if(childPath.equals(children[i].getPath())) return i;
		}
		return -1;
	}
}
