/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.Sts.Framework.UI;

import com.Sts.Framework.Utilities.*;

import java.util.*;


public class StsDualListModel extends StsListModel
{
	private Object[] listObjects;
	private boolean[] selected;
	int nAvailable;
	int nSelected;
	public StsListModel availableListModel = new StsListModel();
	public StsListModel selectedListModel = new StsListModel();

	static final int SELECT_ADD = 1;
	static final int SELECT_REMOVE = -1;

	public StsDualListModel()
	{
	}

	public void initialize(Object[] objects)
	{
		if(objects == null) return;
		this.listObjects = objects;
		nObjects = listObjects.length;
		nAvailable = nObjects;
		nSelected = 0;
		selected = new boolean[nObjects];
		availableListModel.setListObjects(listObjects);
		selectedListModel.setListObjects(null);
	}

	public boolean removeSelectedElement(Object element)
	{
		return changeSelectedElement(element, SELECT_REMOVE);
	}

	public boolean addSelectedElement(Object element)
	{
		return changeSelectedElement(element, SELECT_ADD);
	}

	public boolean changeSelectedElement(Object element, int addOrRemove)
	{
		int a = 0, s = 0;
		for(int n = 0; n < listObjects.length; n++)
		{
			Object listObject = listObjects[n];
			if(listObject == element)
			{
				selected[n] = false;
				nSelected -= addOrRemove;
				nAvailable += addOrRemove;
				return true;
			}
		}
		return false;
	}

	public Object[] addSelectedIndices(int[] selectedIndices)
	{
		int nItemsSelected = selectedIndices.length;
		if(nItemsSelected == 0) return new Object[0];
		nAvailable -= nItemsSelected;
		nSelected += nItemsSelected;
		int i = -1, n = -1, s = 0, a = 0;
		Object listObject;
		try
		{
			Object[] addedObjects = new Object[selectedIndices.length];
			i = 0;
			Object selectedObject = availableListModel.getSelectedObject(selectedIndices[i]);
			for(n = 0; n < listObjects.length; n++)
			{
				listObject = listObjects[n];
				if(listObject == selectedObject)
				{
					selected[n] = true;
					addedObjects[i] = selectedObject;
					i++;
					if(i >= nItemsSelected) break;
					selectedObject = availableListModel.getSelectedObject(selectedIndices[i]);
				}
			}
			Object[] availableObjects = new Object[nAvailable];
			Object[] selectedObjects = new Object[nSelected];
			for(n = 0; n < listObjects.length; n++)
			{
				listObject = listObjects[n];
				if(selected[n])
					selectedObjects[s++] = listObject;
				else
					availableObjects[a++] = listObject;
			}
			availableListModel.setListObjects(availableObjects);
			selectedListModel.setListObjects(selectedObjects);
			return selectedObjects;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "addSelectedIndices", e);
			return null;
		}
	}
	public Object[] removeSelectedIndices(int[] selectedIndices)
	{
		int nItemsSelected = selectedIndices.length;
		if(nItemsSelected == 0) return new Object[0];
		nAvailable += nItemsSelected;
		nSelected -= nItemsSelected;

        Object[] removedObjects = new Object[selectedIndices.length];
		int i = 0;
		Object selectedObject = selectedListModel.getListObjects()[selectedIndices[i]];
		for(int n = 0; n < listObjects.length; n++)
		{
			Object listObject = listObjects[n];
			if(listObject == selectedObject)
			{
				selected[n] = false;
				removedObjects[i] = selectedObject;
				i++;
				if(i >= nItemsSelected) break;
				selectedObject = selectedListModel.getListObjects()[selectedIndices[i]];
			}
		}
		Object[] availableObjects = new Object[nAvailable];
		Object[] selectedObjects = new Object[nSelected];
		int a = 0, s = 0;
		for(int n = 0; n < listObjects.length; n++)
		{
			Object listObject = listObjects[n];
			if(selected[n])
				selectedObjects[s++] = listObject;
			else
				availableObjects[a++] = listObject;
		}
		availableListModel.setListObjects(availableObjects);
		selectedListModel.setListObjects(selectedObjects);
		return selectedObjects;
	}

	public Object[] addAllObjects()
	{
		Arrays.fill(selected, true);
		int nObjects = listObjects.length;
		nAvailable = 0;
		nSelected = nObjects;
		selectedListModel.setListObjects(listObjects);
		availableListModel.setListObjects(null);
		return listObjects;
	}

	public Object[] removeAllObjects()
	{
		Arrays.fill(selected, false);
		int nObjects = listObjects.length;
		nAvailable = nObjects;
		nSelected = 0;
		availableListModel.setListObjects(listObjects);
		selectedListModel.setListObjects(null);
		return new Object[0];
	}
}
