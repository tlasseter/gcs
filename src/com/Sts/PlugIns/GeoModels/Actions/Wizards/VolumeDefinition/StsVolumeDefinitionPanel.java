package com.Sts.PlugIns.GeoModels.Actions.Wizards.VolumeDefinition;

import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

import java.awt.*;

/**
 * Created by tom on 9/23/2014.
 */

public class StsVolumeDefinitionPanel extends StsJPanel
{
    private StsWizard wizard;
    private StsVolumeDefinitionStep wizardStep;
    private StsGeoModelVolume geoModelVolume;

    private StsGroupBox surveyDefinitionBox;
    private StsJPanel parameterPanel;

    private StsStringFieldBean nameBean;
    private StsDoubleFieldBean xMinBean;
    private StsDoubleFieldBean yMinBean;
    private StsFloatFieldBean zMinBean;
    private StsFloatFieldBean xIncBean;
    private StsFloatFieldBean yIncBean;
    private StsFloatFieldBean zIncBean;
    private StsIntFieldBean nColsBean;
    private StsIntFieldBean nColsIncBean;
    private StsIntFieldBean nRowsBean;
    private StsIntFieldBean nRowsIncBean;
    private StsIntFieldBean nSlicesBean;
    private StsFloatFieldBean angleBean;

    public StsVolumeDefinitionPanel(StsWizard wizard, StsVolumeDefinitionStep wizardStep, StsGeoModelVolume geoModelVolume)
    {
        super(true); // true adds insets
        this.wizard = wizard;
        this.wizardStep = wizardStep;
        this.geoModelVolume = geoModelVolume;
        try
        {
            constructBeans();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void constructBeans()
    {
        nameBean = new StsStringFieldBean(geoModelVolume, "name", "Vol name", true, "Vol Name:");
        xMinBean = new StsFloatFieldBean(geoModelVolume, "xMin", true, "X Minimum:", true);
        xMinBean.setToolTipText("The minimum x coordinate inside the project bounds.");

        nColsBean = new StsIntFieldBean(geoModelVolume, "nCols", true, "Number Cols (XLines):", true);
        // nColsIncBean = new StsIntFieldBean(geoModelVolume, "nColsInc", true, "Number Cols Inc (XLines):", true);

        yMinBean = new StsFloatFieldBean(geoModelVolume, "yMin", true, "Y Minimum:", true);
        yMinBean.setToolTipText("The minimum y coordinate inside the project bounds.");

        nRowsBean = new StsIntFieldBean(geoModelVolume, "nRows", true, "Number Rows (InLines):", true);
        // nRowsIncBean = new StsIntFieldBean(geoModelVolume, "nRowsInc", true, "Number Rows Inc (InLines):", true);

        zMinBean = new StsFloatFieldBean(geoModelVolume, "zMin", true, "Z Minimum:", true);
        zMinBean.setToolTipText("The minimum z coordinate inside the project bounds.");

        nSlicesBean = new StsIntFieldBean(geoModelVolume, "nSlices", true, "Number Slices:", true);

        xIncBean = new StsFloatFieldBean(geoModelVolume, "xInc", true, "X Interval:", true);
        xIncBean.setToolTipText("Grid increment in the X direction.");

        yIncBean = new StsFloatFieldBean(geoModelVolume, "yInc", true, "Y Interval:", true);
        yIncBean.setToolTipText("Grid increment in the Y direction.");

        zIncBean = new StsFloatFieldBean(geoModelVolume, "zInc", true, "Z Interval:", true);
        zIncBean.setToolTipText("Grid increment in the Z direction.");

        angleBean = new StsFloatFieldBean(geoModelVolume, "angle", 0.0f, 360.0f, "Angle:", true);
    }

    public void initialize()
    {
        buildPanel();
    }

    private void buildPanel()
    {
        removeAll();

        surveyDefinitionBox = new StsGroupBox("Survey Definition");
        parameterPanel = StsJPanel.addInsets();

        gbc.fill = GridBagConstraints.HORIZONTAL;

        parameterPanel.gbc.fill = GridBagConstraints.HORIZONTAL;

        parameterPanel.add(nameBean);

        parameterPanel.addToRow(xMinBean);
        parameterPanel.addEndRow(xIncBean);

        parameterPanel.addToRow(yMinBean);
        parameterPanel.addEndRow(yIncBean);

        parameterPanel.addToRow(zMinBean);
        parameterPanel.addEndRow(zIncBean);

        parameterPanel.addToRow(nColsBean);
        parameterPanel.addEndRow(nRowsBean);

        parameterPanel.addToRow(nSlicesBean);
        parameterPanel.addEndRow(angleBean);

        surveyDefinitionBox.gbc.fill = GridBagConstraints.HORIZONTAL;
        surveyDefinitionBox.add(parameterPanel);

        this.add(surveyDefinitionBox);

        wizard.rebuild();
    }

    public void setXMin(float value)
    {
        geoModelVolume.setXMin(value);
        geoModelVolume.setXOrigin(value);
    }
    public void setYMin(float value)
    {
        geoModelVolume.setYMin(value);
        geoModelVolume.setYOrigin(value);
    }
    public void setNRows(int value)
    {
        geoModelVolume.setNRows(value);
    }
    public void setNCols(int value)
    {
        geoModelVolume.setNCols(value);
    }
    public void setNSlices(int value)
    {
        geoModelVolume.setNSlices(value);
    }

    public StsGeoModelVolume getGeoModelVolume()
    {
        return geoModelVolume;
    }

    public void accept()
    {
        // new StsMessage(null, StsMessage.INFO, "All fields must be specified. Fill in missing fields or cancel.");
        return;
    }

    public void cancel()
    {
        return;
    }
}