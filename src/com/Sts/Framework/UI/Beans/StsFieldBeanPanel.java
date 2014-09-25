package com.Sts.Framework.UI.Beans;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.awt.*;

public class StsFieldBeanPanel extends StsJPanel
{
	/** Object from which this panel get/set(s) values */
    protected Object beanObject = null;
	/** beans on this panel; these beans get their values from beanObject;  beanObject has a convenience copy of beanObject */
    protected StsFieldBean[] fieldBeans = null;
    private Component vertStrut5 = Box.createVerticalStrut(5);

	static final boolean debug = false;

    public StsFieldBeanPanel()
    {
    }

	public StsFieldBeanPanel(boolean addInsets)
    {
		super(addInsets);
    }

 	public static StsFieldBeanPanel addInsets() {return new StsFieldBeanPanel(true); }

    /** default layout required for UI designers like JBuilder */
    private void setLayout()
    {
//        setLayout(xYLayout1);
        setSize(400, 300);
    }
/*
	public void initializeLayout()
	{
        try
        {
            setLayout(gb);
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }
        catch(Exception e)
        {
            StsException.systemError("StsFieldBeanPanel.initializeLayout() failed.");
        }
    }
*/

    /** when beans are added to panel, they are first given this beanPanel */
/*
    public void add(StsFieldBean fieldBean)
    {
        fieldBean.setBeanPanel(this);
        addField(fieldBean);
    }
*/
    public Component add(Component component)
    {
		checkAddFieldBeans(component);
		return super.add(component);
    }
    public Component add(Component component, int ySpan, double weighty)
    {
        checkAddFieldBeans(component);
		return super.add(component, ySpan, weighty);
	}

	private void checkAddFieldBeans(Component component)
	{
		if(!(component instanceof StsFieldBeanPanel)) return;
		StsFieldBean[] newBeans = ((StsFieldBeanPanel)component).getFieldBeans();
		fieldBeans = (StsFieldBean[])StsMath.arrayAddArray(fieldBeans, newBeans, StsFieldBean.class);
	}

    public void add(Component component, GridBagConstraints gbc)
    {
        super.add(component, gbc);
        gbc.gridy += 1;
        super.add(vertStrut5, gbc);
        gbc.gridy += 1;
    }

	public Component addEndRow(Component component)
    {
        checkAddFieldBeans(component);
		return super.addEndRow(component);
	}
    public Component addEndRow(Component component, int xSpan, double weight)
    {
        checkAddFieldBeans(component);
		return super.addEndRow(component, xSpan, weight);
	}

    private void checkAddFieldBean(Component component)
    {
        if (!(component instanceof StsFieldBean)) return;
        StsFieldBean bean = (StsFieldBean) component;
        fieldBeans = (StsFieldBean[]) StsMath.arrayAddElement(fieldBeans, bean, StsFieldBean.class);
    }

    public StsFieldBean[] getFieldBeans()
    {
        return fieldBeans;
    }

    /** Use this routine to add a component to the current row */
    public Component addToRow(Component component)
    {
        checkAddFieldBeans(component);
		return super.addToRow(component);
    }

	public Component addBeanPanel(StsFieldBean fieldBean)
	{
		checkAddFieldBean(fieldBean);

		int savewidth = gbc.gridwidth;
		gbc.gridwidth = fieldBean.gridwidth;
		add(fieldBean, gbc);
		gbc.gridwidth = savewidth;

		gbc.gridy += 1;
		gbc.weightx = 0.;
		if (addVerticalSpacer)
		{
			super.add(vertStrut10, gbc);
			gbc.gridy += 1;
		}
		gbc.gridx = 0;
		return fieldBean;
	}

	public Component addBeanPanel(StsFieldBean fieldBean, GridBagConstraints gbc)
	{
		checkAddFieldBean(fieldBean);

		addBean(fieldBean, gbc);

		if (addVerticalSpacer)
		{
			super.add(vertStrut10, gbc);
			gbc.gridy += 1;
		}
		return fieldBean;
	}

    private void addBean(StsFieldBean fieldBean, GridBagConstraints gbc)
    {
        //gbc.gridwidth = fieldBean.gridwidth;
        add(fieldBean, gbc);
    }

    /** Adds individual components of bean to panel */
    private void addBeanComponents(StsFieldBean fieldBean)
    {
        fieldBean.addComponentsToPanel(this);
    }
    public Component add(StsFieldBean fieldBean)
    {
        gbc.gridx = 0;
        return addEndRow(fieldBean);
    }

    public void add(StsFieldBean[] beans)
    {
        if (beans == null) return;
        for (int n = 0; n < beans.length; n++)
            add(beans[n]);
    }

    public void addRowOfBeans(StsFieldBean[] beans)
    {
        if (beans == null) return;
        for (int n = 0; n < beans.length - 1; n++)
            addToRow(beans[n]);
        addEndRow(beans[beans.length - 1]);
    }

    /**
     * Add the bean (an StsJPanel) as a single component to to the container.
     * If the current gridwidth is > bean.gridwidth, use it instead.
     * Increment gridx by the bean width (usually 1).
     */
    public void addBeanToRow(StsFieldBean fieldBean)
    {
        checkAddFieldBean(fieldBean);
        int width = gbc.gridwidth;  // save width so we can restore it
        gbc.gridwidth = Math.max(fieldBean.gridwidth, gbc.gridwidth);
        gbc.gridwidth = fieldBean.gridwidth;
		super.add(fieldBean, gbc);
        gbc.gridx += fieldBean.gridwidth;
        gbc.gridwidth = width;
    }

    /**
     * Add the bean (an StsJPanel) as a single component to to the container.
     * If the current gridwidth is > bean.gridwidth, use it instead.
     * Increment gridx by the bean width (usually 1).
     */
    public void addBeanEndRow(StsFieldBean fieldBean)
    {
        int width = gbc.gridwidth;  // save width so we can restore it
        gbc.gridwidth = Math.max(fieldBean.gridwidth, gbc.gridwidth);
        checkAddFieldBean(fieldBean);
        add(fieldBean, gbc);
        gbc.gridwidth = width; // restore width
        gbc.gridx = 0;
        gbc.gridy += 1;
        if (addVerticalSpacer)
        {
            super.add(vertStrut10, gbc);
            gbc.gridy += 1;
        }
    }
    /** Use this routine to add a component to the end of the current row  and start a new row.  Use addToRow if there is no next row. */
    public Component addEndRow(StsFieldBean fieldBean)
    {
        addBeanEndRow(fieldBean);
        return fieldBean;
    }
    /** Use this routine to add a component to the current row */
    public Component addToRow(Component component, int xSpan, double weight)
    {
        checkAddFieldBeans(component);
		return super.addToRow(component, xSpan, weight);
	}

    public Component addToRow(StsFieldBean fieldBean)
    {
        addBeanToRow(fieldBean);
        return fieldBean;
    }
	public void setEditable(boolean editable)
	{
		if (fieldBeans == null) return;
		for (int n = 0; n < fieldBeans.length; n++)
			fieldBeans[n].setEditable(editable);
	}

	public void initializeBeans()
	{
		if (fieldBeans == null) return;
		for (int n = 0; n < fieldBeans.length; n++)
			fieldBeans[n].initializeBean();
	}

	public void updateBeans()
	{
		initializeBeans();
	}

/*
    public void add(StsFieldBean field, GridBagConstraints gbc)
    {
        if(field == null) return;

        Component[] components = field.getNonNullComponents();
//        components = removeNullComponents(components);

        int nComponents = components.length;
        if(nComponents > 0)
        {
            super.add(components[0], gbc);
            gbc.gridy += 1;
        }
        if(nComponents > 1)
        {
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx += 1;
            gbc.anchor = GridBagConstraints.EAST;
            super.add(components[1], gbc);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.gridx -= 1;
            gbc.gridwidth = 1;
       }
       gbc.gridy += 1;
	   super.add(vertStrut5, gbc);
	   gbc.gridy += 1;
	   fieldBeans = (StsFieldBean[])StsMath.arrayAddElement(fieldBeans, field, StsFieldBean.class);
    }

    public void remove(StsFieldBean field)
    {
        if(fieldBeans == null || fieldBeans.length == 0) return;
        fieldBeans = (StsFieldBean[])StsMath.arrayDeleteElement(fieldBeans, field);
        Component[] components = field.getBeanComponents();
        if(components == null) return;
        for(int n = 0; n < components.length; n++)
            if(components[n] != null) super.remove(components[n]);
    }

    private void addField(StsFieldBean field)
    {
        if(field == null) return;

        Component[] components = field.getNonNullComponents();
//        components = removeNullComponents(components);

        int nComponents = components.length;
        if(nComponents > 0)
        {
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            super.add(components[0], gbc);
        }
        if(nComponents > 1)
        {
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 1;
            super.add(components[1], gbc);
            gbc.gridwidth = 1;
        }
        gbc.gridy += 1;
        super.add(vertStrut5, gbc);
          gbc.gridy += 1;

        fieldBeans = (StsFieldBean[])StsMath.arrayAddElement(fieldBeans, field, StsFieldBean.class);
    }
*/
    public StsFieldBean getBeanNamed(String name)
    {
        if(fieldBeans == null) return null;
        for(int n = 0; n < fieldBeans.length; n++)
            if(fieldBeans[n].getName().equals(name)) return fieldBeans[n];
        return null;
    }

    public void setBeanObject(Object object)
    {
        beanObject = object;
		if(fieldBeans == null) return;
		for(StsFieldBean fieldBean : fieldBeans)
			fieldBean.setBeanObject(beanObject);
    }

    public Object getBeanObject()
    {
        if(beanObject == null) return null;
        return beanObject;
    }

	public Object[] getPanelObjects()
	{
		return new Object[] { beanObject };
	}

    private void checkAddFieldBean(StsFieldBean fieldBean)
    {
        fieldBeans = (StsFieldBean[])StsMath.arrayAddElement(fieldBeans, fieldBean, StsFieldBean.class);
    }
}
