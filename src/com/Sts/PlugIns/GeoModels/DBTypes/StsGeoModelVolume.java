package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.Interfaces.MVC.StsVolumeDisplayable;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.StsParameters;

import java.io.Serializable;

/**
 * © tom 9/27/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsGeoModelVolume extends StsRotatedGridBoundingBox implements StsTreeObjectI, StsXYGridable, Serializable, Cloneable
{
    private boolean readoutEnabled = false;

    /** Indicates whether cube is time or depth */
    public String zDomain = StsParameters.TD_DEPTH_STRING;

    static protected StsObjectPanel objectPanel = null;

    static public final StsFieldBean[] geoModelDisplayFields =
    {
        new StsBooleanFieldBean(StsGeoModelVolume.class, "isVisible", "Enable"),
        new StsBooleanFieldBean(StsGeoModelVolume.class, "readoutEnabled", "Mouse Readout")
    };

    static public final StsFieldBean[] geoModelPropertyFields = new StsFieldBean[]
    {
        new StsStringFieldBean(StsGeoModelVolume.class, "name", true, "Name"),
        new StsStringFieldBean(StsGeoModelVolume.class, "zDomainString", false, "Z Domain"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nRows", false, "Number of Lines"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nCols", false, "Number of Crosslines"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nSlices", false, "Number of Samples"),
        new StsDoubleFieldBean(StsGeoModelVolume.class, "xOrigin", false, "X Origin"),
        new StsDoubleFieldBean(StsGeoModelVolume.class, "yOrigin", false, "Y Origin"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "xInc", false, "X Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "yInc", false, "Y Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTInc", false, "Z or T Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "xMin", false, "X Loc Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "yMin", false, "Y Loc Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTMin", false, "Z or T Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTMax", false, "Z or T Max"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "angle", false, "Angle to Line Direction"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "rowNumMin", false, "Min Line"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "rowNumMax", false, "Max Line"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "colNumMin", false, "Min Crossline"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "colNumMax", false, "Max Crossline")
    };

    public StsGeoModelVolume() { }
    public StsGeoModelVolume(boolean persistent)
    {
        super(persistent);
        initializeVolume();
    }

    public void initializeVolume()
    {
        xMin = 0;
        yMin = 0;
        zMin = 0;

        xInc = 10;
        yInc = 10;
        zInc = 10;

        nRows = 100;
        nCols = 100;
        nSlices = 10;

        angle = 0;

        xOrigin = 0;
        yOrigin = 0;
        rowNumMin = 0;
        colNumMin = 0;
        rowNumInc = 1;
        colNumInc = 1;
    }
    public byte getZDomainSupported()
    {
        return StsParameters.getZDomainFromString(zDomain);
    }

    public boolean checkGrid()
    {
        boolean gridOK = true;

        if (nCols <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number columns incorrect: " + nCols +
                            " for volume " + getName());
            gridOK = false;
        }
        if (nRows <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number rows incorrect: " + nRows +
                            " for volume " + getName());
            gridOK = false;
        }
        if (nSlices <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number slices incorrect: " + nSlices +
                            " for volume " + getName());
            gridOK = false;
        }
        if (xInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "X increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (yInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Y increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (zInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Z increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (rowNumInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "row number increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (colNumInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "col number increment is zero" + " for volume " + getName());
            gridOK = false;
        }

        if(gridOK)
        {
            xMax = xMin + (nCols - 1) * xInc;
            yMax = yMin + (nRows - 1) * yInc;
            zMax = zMin + (nSlices - 1) * zInc;
            rowNumMax = rowNumMin + (nRows - 1);
            colNumMax = colNumMin + (nCols - 1);
            setAngle();
        }
        return gridOK;
    }

    public StsFieldBean[] getDisplayFields() { return geoModelDisplayFields; }
    public StsFieldBean[] getPropertyFields() { return geoModelPropertyFields; }
    public StsFieldBean[] getDefaultFields() { return null; }

    public Object[] getChildren()
    {
        return new Object[0];
    }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null)
        {
            objectPanel = StsObjectPanel.constructor(this, true);
        }
        return objectPanel;
    }

    public boolean anyDependencies() { return false; }
    public boolean canExport() { return false; }
    public boolean export() { return false; }
    public boolean canLaunch() { return false; }
    public boolean launch() { return false; }

    public void treeObjectSelected()
    {
        currentModel.setCurrentObject(this);
        currentModel.getGlPanel3d().checkAddView(StsView3d.class);
        currentModel.win3dDisplayAll();
    }

    public boolean isReadoutEnabled()
    {
        return readoutEnabled;
    }

    public void setReadoutEnabled(boolean readoutEnabled)
    {
        this.readoutEnabled = readoutEnabled;
    }
}
