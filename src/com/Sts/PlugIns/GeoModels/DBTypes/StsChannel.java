package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.Types.StsChannelLineSegment;
import com.Sts.PlugIns.GeoModels.Types.StsChannelSegment;

import javax.media.opengl.GL;
import java.io.Serializable;

/**
 * © tom 9/27/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannel extends StsRotatedGridBoundingBox implements StsTreeObjectI, StsXYGridable, Serializable, Cloneable
{
    private float channelWidth;
    private float channelThickness;
    private StsPoint startPoint;
    private StsPoint endPoint;
    private float direction;
    private StsChannelSegment[] channelSegments;

    private boolean readoutEnabled = false;
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

    public StsChannel(float channelWidth, float channelThickness, StsPoint firstPoint, StsPoint lastPoint, float direction)
    {
        this.channelWidth = channelWidth;
        this.channelThickness = channelThickness;
        this.startPoint = firstPoint;
        this.endPoint = lastPoint;
        this.direction = direction;
    }

    public void display(StsGLPanel3d glPanel3d)
    {
        if(channelSegments == null || channelSegments.length == 0)
        {
            GL gl = glPanel3d.getGL();
            if (gl == null) return;
            StsGLDraw.drawLine(gl, StsColor.RED, true, new StsPoint[] { startPoint, endPoint});
        }
        else
        {
            for (StsChannelSegment channelSegment : channelSegments)
                channelSegment.display(glPanel3d);
        }
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

    public float getChannelWidth()
    {
        return channelWidth;
    }

    public float getChannelThickness()
    {
        return channelThickness;
    }

    public StsPoint getStartPoint()
    {
        return startPoint;
    }

    public StsPoint getEndPoint()
    {
        return endPoint;
    }

    public float getDirection()
    {
        return direction;
    }
}
