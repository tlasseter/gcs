package com.Sts.Framework.UI.DataFilesPanel;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.UI.*;

import javax.swing.tree.*;
import java.nio.file.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 4/25/12
 */
public class StsFileTreeNode extends DefaultMutableTreeNode
{
	public boolean selected = false;

	public StsFileTreeNode(String pathString)
	{
		Path path = Paths.get(pathString);
		StsFile file = StsFile.constructor(path);
		if(file == null) return;
		setUserObject(file);
		setAllowsChildren(!file.isAFile());
	}

	public StsFileTreeNode(StsAbstractFile file)
	{
		super(file);
		setAllowsChildren(!file.isAFile());
	}

	final public boolean isFile()
	{
		return getFile().isAFile();
	}

	final public StsAbstractFile getFile() { return (StsAbstractFile)userObject; }

	final public StsAbstractFile[] getDirectoryFiles()
	{
		return getFile().listFiles();
	}

	final boolean ancestorSelected()
	{
		StsFileTreeNode nextNode;
		while(true)
		{
			nextNode = (StsFileTreeNode)getParent();
			if(nextNode == null) return false;
			if(nextNode.selected) return true;
		}
	}
/*
	public boolean isExplored() { return explored; }

	public void explore()
	{
		if(isLeaf()) return;

		if(!isExplored())
		{
			explored = true;
			checkAddChildren();
		}
	}

	public boolean isLeaf()
	{
		return isFile();
	}
*/

	boolean checkAddChildren(StsFileTree fileTree)
	{
		int nChildren = 0;

		StsAbstractFile[] children = getDirectoryFiles();
		if(children != null)
			nChildren = children.length;

		int nCurrentChildren = getChildCount();
		if(nChildren == nCurrentChildren) return false;

		removeAllChildren();

		for(StsAbstractFile child : children)
		{
			try
			{
				StsFileTreeNode childNode = new StsFileTreeNode(child);
				fileTree.addChild(this, childNode, true);
				childNode.checkAddChildren(fileTree);
			}
			catch(Exception e)
			{
				StsMessageFiles.errorMessage("Failed to construct StsFile for: " + child);
			}
		}
		return true;
	}

	public String getFileURL()
	{
		return getFile().getURL().toString();
	}
}
