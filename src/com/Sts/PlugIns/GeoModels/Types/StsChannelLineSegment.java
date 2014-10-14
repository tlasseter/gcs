package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.StsSerialize;
import com.Sts.Framework.MVC.Views.StsGLPanel;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsGLDraw;

import javax.media.opengl.GL;
import java.io.Serializable;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelLineSegment extends StsChannelSegment implements Cloneable, StsSerializable, Serializable
{
    public StsChannelLineSegment() { }

    public StsChannelLineSegment(StsPoint firstPoint, StsPoint lastPoint)
    {
        points = new StsPoint[] { firstPoint, lastPoint};
    }

    public StsChannelLineSegment(StsPoint firstPoint, float direction, float length)
    {
        StsPoint lastPoint = firstPoint.addXYVector(direction + 90, length);
        points = new StsPoint[] { firstPoint, lastPoint};
    }

    public void display(StsGLPanel3d glPanel3d)
    {
        GL gl = glPanel3d.getGL();
        if (gl == null) return;
        StsGLDraw.drawLine(gl, StsColor.RED, true, points);
    }
}
