package com.Sts.PlugIns.GeoModels;

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
public class StsChannelLineSegment extends StsSerialize implements Cloneable, StsSerializable, Serializable
{
    StsPoint[] points = new StsPoint[2];

    public StsChannelLineSegment() { }

    public StsChannelLineSegment(StsPoint firstPoint, StsPoint lastPoint)
    {
        points[0] = firstPoint;
        points[1] = lastPoint;
    }

    public void display(StsGLPanel3d glPanel3d)
    {
        GL gl = glPanel3d.getGL();
        if (gl == null) return;
        StsGLDraw.drawLine(gl, StsColor.RED, true, points);
    }
}
