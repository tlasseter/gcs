package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsGLDraw;

import javax.media.opengl.GL;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelLineSegment extends StsChannelSegment
{
    float length;

    public StsChannelLineSegment() { }

    public StsChannelLineSegment(StsPoint startPoint, float startDirection, float length)
    {
        this.startPoint = startPoint;
        this.startDirection = startDirection;
        this.length = length;
        computePoints();
    }

    public boolean initialize()
    {
        return computePoints();
    }

    public boolean computePoints()
    {
        StsPoint endPoint = startPoint.addXYVector(startDirection + 90, length);
        points = new StsPoint[] { startPoint, endPoint};
        return true;
    }

    public void display(GL gl, boolean displayCenterLinePoints)
    {
        if (gl == null) return;
        StsGLDraw.drawLine(gl, StsColor.GREEN, true, points);
        if(displayCenterLinePoints)
            StsGLDraw.drawPoint(gl, points[0].v, StsColor.GREEN, 6);
    }

    public void fillSerializableArrays(int index, byte[] segmentTypes, StsPoint[] startPoints, float[] startDirections, float[] sizes, float[] arcs)
    {
        segmentTypes[index] = LINE;
        startPoints[index] = startPoint;
        startDirections[index] = startDirection;
        sizes[index] = length;
        arcs[index] = 0;
    }
}
