package com.Sts.Framework.UI.Beans;

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Icons.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import javax.swing.*;
import java.awt.*;

/**
 * <p>Title: S2S development</p>
 *
 * <p>Description: Integrated seismic to simulation software</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: S2S Systems LLC</p>
 *
 * @author not attributable
 * @version c51c
 */
public class StsToggleButtonFieldBean extends StsFieldBean
{
	StsToggleButton button = new StsToggleButton();
	StsMethod method;
	String methodName;

	public StsToggleButtonFieldBean()
	{
	}

	/** constructor for bean on object panel tied to a class.  Instance is set later. */
	public StsToggleButtonFieldBean(String name, String tip, Class actionClass, String methodName)
	{
		initialize(name, tip, actionClass, methodName);
	}

	/** constructor for a bean tied to a particular instance */
	public StsToggleButtonFieldBean(String name, String tip, Object instance, String methodName)
	{
		initialize(name, tip, instance, methodName);
	}

	/** constructor for a button bean tied to a particular instance.
	 *  When pushed it calls method with arguement value
	 */
	public StsToggleButtonFieldBean(String name, String tip, Object instance, String methodName, Object value)
	{
		initialize(name, tip, instance, methodName, value);
	}

	public void initialize(String name, String tip, Class actionClass, String methodName)
	{
		initializeButton(name, tip);
		method = new StsMethod(actionClass, methodName);
		button.addActionListener(new StsActionListener(method));
	}

	public void initialize(String name, String tip, Object instance, String methodName)
	{
		initializeButton(name, tip);
		method = new StsMethod(instance, methodName);
		button.addActionListener(new StsActionListener(method));
	}

	public void initialize(String name, String tip, Object instance, String methodName, Object value)
	{
		initializeButton(name, tip);
		method = new StsMethod(instance, methodName, value);
		button.addActionListener(new StsActionListener(method));
	}

	protected void initializeButton(String name, String tip)
	{
		button.setText(name);
		setName(name);
		button.setToolTipText(tip);
		button.setMargin(new Insets(0, 0, 0, 0));
		this.add(button);
	}
    /** constructor for actionManger.newAction with no args */
    public StsToggleButtonFieldBean(String name, String tip, StsActionManager actionManager, Class actionClass)
    {
        super();
        initialize(name, tip, actionManager, actionClass);
    }

    /** constructor for actionManger.newAction with single arg */
    public StsToggleButtonFieldBean(String name, String tip, StsActionManager actionManager, Class actionClass, Object arg)
    {
        super();
        initialize(name, tip, actionManager, actionClass, (arg == null) ? null : new Object[] {arg});
    }

    /** constructor for actionManager.newAction with args list */
    public StsToggleButtonFieldBean(String name, String tip, StsActionManager actionManager, Class actionClass,
                           Object[] args)
    {
        super();
        initialize(name, tip, actionManager, actionClass, args);
    }

    public void initialize(String name, String tip, StsActionManager actionManager, Class actionClass, Object arg)
    {
        initialize(name, tip, actionManager, actionClass, (arg == null) ? null : new Object[] {arg});
    }

    public void initialize(String name, String tip, StsActionManager actionManager, Class actionClass)
    {
        initialize(name, tip, actionManager, actionClass, null);
    }

    public void initialize(String name, String tip, StsActionManager actionManager, Class actionClass, Object[] args)
    {
        try
        {
            if (actionManager == null)
            {
                StsException.systemError("StsToggleButtonFieldBean.constructor(actionManger..) failed." +
						" ActionManager cannot be null.");
                return;
            }
            labelButton(name);
            setName(name);
            setToolTipText(tip);
            button.setMargin(new Insets(0, 0, 0, 0));
			add(button);

            Object[] selectArguments = new Object[] {actionClass, (args == null) ? new Object[0] : args};
            StsMethod selectMethod = new StsMethod(actionManager, "startAction", selectArguments);

            Object[] deselectArguments = new Object[] {actionClass};
            StsMethod deselectMethod = new StsMethod(actionManager, "endAction", deselectArguments);

            button.addActionListener(new StsToggleActionListener(button, selectMethod, deselectMethod));

            actionManager.addActionWorkflow(actionClass);
        }
        catch (Exception e)
        {
            StsException.systemError("StsToggleButtonFieldBean.constructor(class.method(args)) failed.");
        }
    }

    protected void labelButton(String name)
    {
        Icon icon = StsIcon.createIcon(name + ".gif");
        if (icon != null)
        {
            button.setIcon(icon);
        }
        else
        {
            button.setText(name);
        }
        button.setSize();
    }

	public boolean isSelected() { return button.isSelected(); }

	public void toggleButton()  {  button.setSelected(!button.isSelected()); }

	/** No reason for a buttonFieldBean to be persisted as there is no persistent state,
	 *  so toString and fromString are do-nothings; included here to fulfill abstract requrements.
	 */
	public String toString() { return NONE_STRING; }
	public Object fromString(String string) { return null; }

	public Component[] getBeanComponents() { return new Component[] { button }; }
	public Object getValueObject() { return null; }
	public void doSetValueObject(Object valueObject) {  }
	public void setBeanObject(Object instance)
	{
//		button.selectMethod.setInstance(instance);
//		button.deselectMethod.setInstance(instance);
	}
	public void setValueFromPanelObject(Object panelObject)
	{
		setBeanObject(panelObject);
	}

	public void setEditable()
	{
		button.setEnabled(editable);
	}

	public void testButton() { System.out.println("Button pushed."); }

	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Test Panel");
		frame.setSize(300, 200);
		Container contentPane = frame.getContentPane();
		TestButtonModel buttonModel = new TestButtonModel();
		StsToggleButtonFieldBean button = new StsToggleButtonFieldBean("Test Button", "Push it.", buttonModel, "testButton");
		contentPane.add(button);
		// StsToolkit.setLocation(frame);
		frame.pack();
		frame.setVisible(true);
	}
}



