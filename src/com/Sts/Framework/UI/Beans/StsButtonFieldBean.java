package com.Sts.Framework.UI.Beans;

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
public class StsButtonFieldBean extends StsFieldBean
{
	JButton button = new JButton();
	StsMethod method;
	String methodName;

	public StsButtonFieldBean()
	{
	}

	/** constructor for bean on object panel tied to a class.  Instance is set later. */
	public StsButtonFieldBean(String name, String tip, Class actionClass, String methodName)
	{
		initialize(name, tip, actionClass, methodName);
	}

	/** constructor for a bean tied to a particular instance */
	public StsButtonFieldBean(String name, String tip, Object instance, String methodName)
	{
		initialize(name, tip, instance, methodName);
	}

	/** constructor for a button bean tied to a particular instance.
	 *  When pushed it calls method with arguement value
	 */
	public StsButtonFieldBean(String name, String tip, Object instance, String methodName, Object value)
	{
		initialize(name, tip, instance, methodName, value);
	}

	public StsButtonFieldBean copy(Object beanObject)
	{
		try
		{
			StsButtonFieldBean beanCopy = new StsButtonFieldBean();
			beanCopy.initialize(button.getText(), button.getToolTipText(), beanObject, method.getMethod().getName());
			return beanCopy;
		}
		catch(Exception e)
		{
			return null;
		}
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

	private void initializeButton(String name, String tip)
	{
		button.setText(name);
		setName(name);
		button.setToolTipText(tip);
		button.setMargin(new Insets(0, 0, 0, 0));
		this.add(button);
	}

	/** No reason for a buttonFieldBean to be persisted as there is no persistent state,
	 *  so toString and fromString are do-nothings; included here to fulfill abstract requrements.
	 */
	public String toString() { return NONE_STRING; }
	public Object fromString(String string) { return null; }

	public Component[] getBeanComponents() { return new Component[] { button }; }
	public Object getValueObject() { return null; }
	public void doSetValueObject(Object valueObject) {  }
	public void setBeanObject(Object instance) { method.setInstance(instance); }
	public void setValueFromPanelObject(Object panelObject)
	{
		setBeanObject(panelObject);
	}

	public void setEditable()
	{
		button.setEnabled(editable);
	}

    public void disable()
    {
        StsToolkit.runLaterOnEventThread
        (
            new Runnable()
            {
                public void run()
                {
                    button.setEnabled(false);
                }
            }
        );
    }

    public void enable()
    {
        StsToolkit.runLaterOnEventThread
                (
                        new Runnable()
                        {
                            public void run()
                            {
                                button.setEnabled(true);
                            }
                        }
                );
    }
	public void testButton() { System.out.println("Button pushed."); }

	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Test Panel");
		frame.setSize(300, 200);
		Container contentPane = frame.getContentPane();
		TestButtonModel buttonModel = new TestButtonModel();
		StsButtonFieldBean button = new StsButtonFieldBean("Test Button", "Push it.", buttonModel, "testButton");
		contentPane.add(button);
		StsToolkit.centerComponentOnScreen(frame);
		frame.pack();
		frame.setVisible(true);
	}
}

class TestButtonModel
{
	boolean state;

	public boolean testButtonModel() { state = !state; return state; }
}



