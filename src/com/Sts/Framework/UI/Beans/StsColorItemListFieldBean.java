//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.UI.Beans;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.awt.*;

/** This class is intended for a list of instances of StsColorListItem.
 *  Each instance has an associated object, name, and color.
 */
public class StsColorItemListFieldBean extends StsComboBoxFieldBean
{
//	private StsColorListItem[] items = null;

	public StsColorItemListFieldBean()
    {
    }

    public StsColorItemListFieldBean(String fieldLabel)
    {
        classInitialize(null, null, fieldLabel);
        comboBox.setRenderer(new ColorItemListRenderer());
//        comboBox.setLightWeightPopupEnabled(false);
    }

    public StsColorItemListFieldBean(String fieldName, String fieldLabel)
    {
        classInitialize(null, fieldName, fieldLabel);
        comboBox.setRenderer(new ColorItemListRenderer());
//        comboBox.setLightWeightPopupEnabled(false);
    }

	public StsColorItemListFieldBean(Class c, String fieldName, String fieldLabel, StsColorListItem[] items)
    {
//        this.items = items;
        initialize(c, fieldName, fieldLabel, items);
        comboBox.setRenderer(new ColorItemListRenderer());
//        comboBox.setLightWeightPopupEnabled(false);
   }

	public StsColorItemListFieldBean(Class c, String fieldName, String fieldLabel)
    {
        super.classInitialize(c, fieldName, fieldLabel, null);
        comboBox.setRenderer(new ColorItemListRenderer());
//        comboBox.setLightWeightPopupEnabled(false);
   }

	public StsColorItemListFieldBean(Object beanObject, String fieldName, String fieldLabel, StsColorListItem[] items)
    {
        initialize(beanObject, fieldName, fieldLabel, items);
        comboBox.setRenderer(new ColorItemListRenderer());
//        comboBox.setLightWeightPopupEnabled(false);
   }

    public void initialize(Class c, String fieldName, String fieldLabel, StsColorListItem[] items)
    {
        super.classInitialize(c, fieldName, fieldLabel, items);
        comboBox.setRenderer(new ColorItemListRenderer());
    }

    public void initialize(Object beanObject, String fieldName, String fieldLabel, StsColorListItem[] items)
    {
        if(beanObject == null)
        {
            StsException.systemError("StsFieldBean.classInitialize() failed. beanObject cannot be null.");
            return;
        }
        this.beanObject = beanObject;
        Class c = beanObject.getClass();
        initialize(c, fieldName, fieldLabel, items);
        setValueFromPanelObject(beanObject);
    }

    public void initialize(Object instance, String fieldName, String fieldLabel)
    {
        super.initialize(instance, fieldName, fieldLabel);
        comboBox.setRenderer(new ColorItemListRenderer());
    }
/*
	public StsColorItemListFieldBean copy(Object beanObject)
	{
		try
		{
			StsColorItemListFieldBean beanCopy = new StsColorItemListFieldBean();
			if(listFieldName != null)
				beanCopy.initialize(beanObject, fieldName, getLabelString(), listFieldName);
			else
				beanCopy.initialize(beanObject, fieldName, getLabelString(), items);
			return beanCopy;
		}
		catch(Exception e)
		{
			return null;
		}
	}
*/
    public void initializeColors(String fieldLabel, StsColorListItem[] items)
    {
        super.classInitialize(null, null, fieldLabel, items);
        comboBox.setRenderer(new ColorItemListRenderer());
    }

    public void setListItems(StsColorListItem[] items)
    {
    	super.setListItems(items);
    }

    public void addItem(StsColorListItem item)
    {
        super.addItem(item);
    }

	protected Object getNoneItem()
	{
		return new StsColorListItem(StsColor.GREY, "none");
	}

    public Object getValueObject() { return comboBox.getSelectedItem(); }
//	public Object getDefaultValueObject() { return comboBox.getItemAt(0); }

    public void setValueObject(StsColorListItem item)
    {
        super.setSelectedItem(item);
    }

    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
    }

	public String toString()
	{
		return comboBox.getSelectedItem().toString();
	}

	public Object fromString(String string)
	{
        if(getNItems() == 0) return null;
		return Color.decode(string);
	}

    public Object getSelectedItem() { return comboBox.getSelectedItem(); }
    public void setSelectedItem(Object object)
    {
        final Object obj = object;
        StsToolkit.runLaterOnEventThread ( new Runnable() { public void run() { comboBox.setSelectedItem(obj); } } );
    }

    public void removeItem(Object object)
    {
        final Object obj = object;
        StsToolkit.runLaterOnEventThread ( new Runnable() { public void run() { comboBox.removeItem(obj); } } );
    }

    public void removeItemAtIndex(int index_)
    {
       final int index = index_;
       StsToolkit.runLaterOnEventThread ( new Runnable() { public void run() { comboBox.removeItemAt(index); } } );
    }

    public void removeAllItems()
    {
        StsToolkit.runLaterOnEventThread ( new Runnable() { public void run() { comboBox.removeAllItems(); } } );
    }

    public int getListSize()
    {
        return comboBox.getModel().getSize();
    }

    public StsColorListItem getItemAt(int index)
    {
        return (StsColorListItem)comboBox.getModel().getElementAt(index);
    }

    public void deleteObject(Object object)
    {
        int nItems = getListSize();
        for(int n = 0; n < nItems; n++)
        {
            StsColorListItem item = getItemAt(n);
            if(item.getObject() == object)
            {
                removeItem(item);
                return;
            }
        }
    }

    static public StsColorListItem getNullListItem()
    {
        StsColor color = StsColor.GRAY;
        String name = "";
        return new StsColorListItem(null, color, name, 16, 16, 1);
    }

    static public void main(String[] args)
    {
        StsColorItemListTest test = new StsColorItemListTest();
        StsColorItemListFieldBean colorListBean = new StsColorItemListFieldBean();
        colorListBean.initialize(test, "colorListItem", "Colors", (StsColorListItem[])null);
        test.initialize(colorListBean);
		StsButton addColorButton = new StsButton("addColor", "Add a color.", test, "addColor", colorListBean);
        StsButton deleteAllButton = new StsButton("deleteAll", "delete all colors.", test, "deleteAllColors", colorListBean);
		StsJPanel panel = StsJPanel.addInsets();
		panel.add(colorListBean);
 		panel.add(addColorButton);
        panel.add(deleteAllButton);
        StsToolkit.createDialog(panel);
    }
}

class ColorItemListRenderer extends JLabel implements ListCellRenderer
{
    public Component getListCellRendererComponent(JList list, Object value, int index,
                        boolean isSelected, boolean hasFocus)
    {
        StsColorListItem item;

        if(value == null)
            item = StsColorItemListFieldBean.getNullListItem();
        else
            item = (StsColorListItem)value;

        setOpaque(true);
        setIcon(item.getIcon());
        setText(item.getName());
        if (isSelected)
        {
            setBackground(Color.black);
            setForeground(Color.white);
        }
        else // not selected
        {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        return this;
    }
}

class StsColorItemListTest
{
    public StsColorListItem colorListItem;
    public int index = 0;

    public static StsColor[] colors = StsColor.colors8;

    StsColorItemListTest()
    {
    }

//    public Color[] getColors() { return colors; }
//    public Color getColor() { return color; }
//    public void setBeachballColors(Color c) { color = c; }

    public StsColorListItem getColorListItem() { return colorListItem; }
    public void setColorListItem(StsColorListItem colorListItem) { this.colorListItem = colorListItem; }

    public void initialize(StsColorItemListFieldBean colorListBean)
    {
        StsColorListItem listItem = StsColorItemListFieldBean.getNullListItem();
        colorListBean.addItem(listItem);
        colorListBean.comboBox.setSelectedItem(listItem);
    }

    public void addColor(StsColorItemListFieldBean colorListBean)
    {

        StsColor color = colors[(index++)%10];
        String name = Integer.toString(index-1);

        // first item is a dummy and has a null object; if this is only item,
        // change it to a legitimate item
        StsColorListItem listItem = (StsColorListItem)colorListBean.getSelectedItem();
        if(listItem.getObject() == null)
        {
            listItem.setStsColor(color);
            listItem.setName(name);
            listItem.setObject(new String(""));
            colorListBean.repaint();
        }
        else
        {
            listItem = new StsColorListItem(new String(""), color, name, 16, 16, 1);
            colorListBean.addItem(listItem);
        }
        colorListBean.comboBox.setSelectedItem(listItem);
    }

    public void deleteAllColors(StsColorItemListFieldBean colorListBean)
    {
        colorListBean.removeAllItems();
        initialize(colorListBean);
        colorListBean.repaint();
    }
}

