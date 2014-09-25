/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.Sts.Framework.UI;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class StsListModel implements ListModel
{
	int nObjects = 0;
	private Object[] listObjects;
	String longestString = "";

    protected EventListenerList listenerList = new EventListenerList();

	public StsListModel()
	{
	}

	public StsListModel(Object[] listObjects)
	{
		setListObjects(listObjects);
	}

	public int getSize()
	{
		return nObjects;
	}

	public Object getElementAt(int index)
	{
		return getListObjects()[index];
	}

	public boolean contains(Object elem)
	{
		for(Object object : getListObjects())
			if(object == elem) return true;
		return false;
	}

	public int indexOf(Object elem)
	{
		for(int n = 0; n < getListObjects().length; n++)
			if(getListObjects()[n] == elem) return n;
		return -1;
	}

	public int size()
	{
		return nObjects;
	}

	public String getLongestString() { return longestString; }

	public Object[] getListObjects()
	{
		return listObjects;
	}

	public Object getSelectedObject(int index)
	{
		if(index >= nObjects) return null;
		return listObjects[index];
	}

	public void setListObjects(Object[] listObjects)
	{
		fireIntervalRemoved(this, 0, nObjects-1);
		nObjects = (listObjects == null ? 0 : listObjects.length);
		this.listObjects = listObjects;
		if(listObjects == null) return;
		fireIntervalAdded(this, 0, nObjects-1);
	}

	protected void fireIntervalAdded(Object source, int index0, int index1)
	{
		if(index1 < index0) return;

		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
				{
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).intervalAdded(e);
			}
		}
	}

	protected void fireIntervalRemoved(Object source, int index0, int index1)
	{
		if(index1 < index0) return;
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
				{
					e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).intervalRemoved(e);
			}
		}
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType)
	{
		return listenerList.getListeners(listenerType);
	}

	protected void fireContentsChanged(Object source, int index0, int index1)
	{
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
				{
					e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
				}
				((ListDataListener) listeners[i + 1]).contentsChanged(e);
			}
		}
	}

	public void addListDataListener(ListDataListener l) {
	listenerList.add(ListDataListener.class, l);
	}

    public void removeListDataListener(ListDataListener l) {
	listenerList.remove(ListDataListener.class, l);
    }
}
