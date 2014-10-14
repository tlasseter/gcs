package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.StsSerialize;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;

import javax.media.opengl.GL;
import java.io.Serializable;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public abstract class StsChannelSegment extends StsSerialize implements Cloneable, StsSerializable, Serializable
{
    float startDirection;
    float z;
    StsPoint[] points = null;

    public StsChannelSegment() { }

    public StsChannelSegment(float startDirection, float z)
    {
        this.startDirection = startDirection;
        this.z = z;
    }

    public abstract void display(StsGLPanel3d glPanel3d);

    public StsPoint getLastPoint()
    {
        return StsPoint.getLastPoint(points);
    }
}
