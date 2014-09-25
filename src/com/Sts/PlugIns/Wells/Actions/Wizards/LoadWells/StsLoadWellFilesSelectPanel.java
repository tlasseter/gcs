package com.Sts.PlugIns.Wells.Actions.Wizards.LoadWells;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.DataTransfer.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class StsLoadWellFilesSelectPanel extends StsAbstractFilesSelectPanel
{
	float datumShift = 0.0f;

	StsJPanel unitFormatBox;
    StsJPanel datumShiftBox;
	StsComboBoxFieldBean hUnitsBean;
	StsComboBoxFieldBean vUnitsBean;
	StsComboBoxFieldBean tUnitsBean;
	StsFloatFieldBean datumShiftBean;

	StsCheckbox applyToTdBtn;
	StsCheckbox applyToRefBtn;
	StsCheckbox applyToLogBtn;
	StsCheckbox applyToDevBtn;
	StsCheckbox[] applyToBtns;

	public StsLoadWellFilesSelectPanel(StsLoadWizard loadWizard, int width, int height)
	{
		super(loadWizard, "Wells",  width, height);
	}

	public void addToPanel()
	{
		constructBeans();

		gbc.anchor = gbc.NORTH;
		gbc.fill = gbc.HORIZONTAL;
		unitFormatBox.addToRow(vUnitsBean);
        unitFormatBox.addEndRow(hUnitsBean);
		 unitFormatBox.addEndRow(tUnitsBean);
        addEndRow(unitFormatBox);

        datumShiftBox.addToRow(datumShiftBean);
        datumShiftBox.addToRow(applyToDevBtn);
        datumShiftBox.addToRow(applyToLogBtn);
        datumShiftBox.addToRow(applyToRefBtn);
        datumShiftBox.addEndRow(applyToTdBtn);
        addEndRow(datumShiftBox);
	}

    protected void constructBeans()
    {
		unitFormatBox = new StsGroupBox("Units and Format");
		datumShiftBox = new StsGroupBox("Depth Correction");
		hUnitsBean = new StsComboBoxFieldBean(wizard, "horzUnitsString", "Horizontal Units:  ", StsParameters.DIST_STRINGS);
		vUnitsBean = new StsComboBoxFieldBean(wizard, "vertUnitsString", "Vertical Units:  ", StsParameters.DIST_STRINGS);
		tUnitsBean = new StsComboBoxFieldBean(wizard, "timeUnitsString", "Time Units:  ", StsParameters.TIME_STRINGS);

		datumShiftBean = new StsFloatFieldBean(wizard, "datumShift", true, "Correction:  ");
        datumShiftBean.setToolTipText("Correction is added to the MDEPTH or DEPTH values");
		datumShiftBean.setColumns(6);

		applyToTdBtn = new StsCheckbox("Time-Depth", "Apply the correction to the time-depth data.");
		applyToRefBtn = new StsCheckbox("Markers", "Apply the correction to the marker data.");
		applyToLogBtn = new StsCheckbox("Logs", "Apply the correction to the log data.");
		applyToDevBtn = new StsCheckbox("Path", "Apply the correction to the well path data.");
		applyToBtns = new StsCheckbox[] { applyToDevBtn, applyToLogBtn,  applyToTdBtn, applyToRefBtn };

	}

 	public float getDatumShift() { return datumShift; }
    public void setDatumShift(float shift) { datumShift = shift; }
    public boolean[] getApplyDatumShift()
    {
        boolean[] apply = new boolean[applyToBtns.length];
        for(int i=0; i<applyToBtns.length; i++)
            apply[i] = applyToBtns[i].isSelected();
        return apply;
    }
}
