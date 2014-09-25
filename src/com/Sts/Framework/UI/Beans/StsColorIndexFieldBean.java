package com.Sts.Framework.UI.Beans;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/21/11
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsColorIndexFieldBean extends StsColorListFieldBean
{
	public StsColorIndexFieldBean()
    {
    }

    public StsColorIndexFieldBean(String fieldLabel)
    {
        super.initialize(null, null, true, fieldLabel);
    }

    public StsColorIndexFieldBean(String fieldName, String fieldLabel)
    {
        super.initialize(null, fieldName, true, fieldLabel);
    }

	public StsColorIndexFieldBean(Class c, String fieldName, String fieldLabel, StsColor[] stsColors)
    {
        initializeColors(c, fieldName, fieldLabel, stsColors);
    }

	public StsColorIndexFieldBean(Class c, String fieldName, String fieldLabel, StsSpectrum spectrum)
    {
        initializeColors(c, fieldName, fieldLabel, spectrum.getStsColors());
    }

    public StsColorIndexFieldBean(Class c, String fieldName, String fieldLabel)
    {
        super.classInitialize(c, fieldName, fieldLabel, null);
    }

	public StsColorIndexFieldBean(Object beanObject, String fieldName, String fieldLabel, StsColor[] colors)
    {
        initializeColors(beanObject, fieldName, fieldLabel, colors);
    }

	private StsColorListItem[] createItems(StsColor[] colors)
	{
		if( colors == null ) return null;
		items = new StsColorIndexItem[colors.length];
		for( int i=0; i<items.length; i++ )
		{
			items[i] = new StsColorIndexItem(colors[i], null, 48, 16, 1, i);
		}
		return items;
	}

    public Object getValueObject() { return comboBox.getSelectedIndex(); }
//	public Object getDefaultValueObject() { return comboBox.getItemAt(0); }

    public boolean setValueObject(Object intObject)
    {
		super.setValueObject(intObject);
		return true;
	}

	public void doSetValueObject(Object intObject)
	{
        super.setSelectedIndex(((Integer)intObject).intValue());
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
/*
    public void setSelectedItem(Object object)
    {
        final Object obj = object;
        StsToolkit.runLaterOnEventThread(new Runnable()
		{
			public void run() { comboBox.setSelectedItem(obj); }
		});
    }
*/
}
