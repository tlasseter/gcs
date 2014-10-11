package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.MVC.Views.StsGLPanel;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.Framework.Utilities.StsParameters;
import com.Sts.PlugIns.GeoModels.StsChannelLineSegment;

import java.io.Serializable;

/**
 * © tom 9/27/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannel extends StsRotatedGridBoundingBox implements StsTreeObjectI, StsXYGridable, Serializable, Cloneable
{
    private boolean readoutEnabled = false;
    private StsChannelLineSegment[] channelSegments = new StsChannelLineSegment[0];
    static protected StsObjectPanel objectPanel = null;

    static public final StsFieldBean[] displayFields =
    {
        new StsBooleanFieldBean(StsChannel.class, "isVisible", "Enable"),
        new StsBooleanFieldBean(StsChannel.class, "readoutEnabled", "Mouse Readout")
    };

    static public final StsFieldBean[] propertyFields = new StsFieldBean[]
    {
        new StsStringFieldBean(StsChannel.class, "name", true, "Name"),
        new StsStringFieldBean(StsChannel.class, "zDomainString", false, "Z Domain"),
        new StsIntFieldBean(StsChannel.class, "nRows", false, "Number of Lines"),
        new StsIntFieldBean(StsChannel.class, "nCols", false, "Number of Crosslines"),
        new StsIntFieldBean(StsChannel.class, "nSlices", false, "Number of Samples"),
        new StsFloatFieldBean(StsChannel.class, "rowNumMin", false, "Min Line"),
        new StsFloatFieldBean(StsChannel.class, "rowNumMax", false, "Max Line"),
        new StsFloatFieldBean(StsChannel.class, "colNumMin", false, "Min Crossline"),
        new StsFloatFieldBean(StsChannel.class, "colNumMax", false, "Max Crossline")
    };

    public StsChannel() { }

    public StsChannel(boolean persistent)
    {
        super(persistent);
    }

    static public StsChannel buildStraightLine(StsPoint firstPoint, StsPoint lastPoint)
    {
        StsChannel channel = new StsChannel(false);
        if(!channel.constructStraightLine(firstPoint, lastPoint)) return null;
        return channel;
    }
    private boolean constructStraightLine(StsPoint firstPoint, StsPoint lastPoint)
    {
        try
        {
            StsChannelLineSegment lineSegment = new StsChannelLineSegment(firstPoint, lastPoint);
            channelSegments = (StsChannelLineSegment[])StsMath.arrayAddElement(channelSegments, lineSegment);

            return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "constructStraightLine", e);
            return false;
        }
    }
    public void display(StsGLPanel3d glPanel3d)
    {
        for(StsChannelLineSegment channelSegment : channelSegments)
            channelSegment.display(glPanel3d);
    }

    public StsFieldBean[] getDisplayFields() { return displayFields; }
    public StsFieldBean[] getPropertyFields() { return propertyFields; }
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
