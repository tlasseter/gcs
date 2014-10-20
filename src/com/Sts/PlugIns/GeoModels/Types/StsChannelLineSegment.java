package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;

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

    public StsChannelLineSegment(StsChannel channel, StsPoint startPoint, float startDirection, float length)
    {
        super(channel, startDirection, startPoint);
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
        float halfWidth = channel.getChannelWidth()/2;
        StsPoint innerPoint0 = startPoint.addXYVector(startDirection, halfWidth);
        StsPoint outerPoint0 = startPoint.addXYVector(startDirection+180, halfWidth);
        StsPoint innerPoint1 = endPoint.addXYVector(startDirection, halfWidth);
        StsPoint outerPoint1 = endPoint.addXYVector(startDirection+180, halfWidth);
        centerLinePoints = new StsPoint[] { startPoint, endPoint };
        innerPoints = new StsPoint[] { innerPoint0, innerPoint1 };
        outerPoints = new StsPoint[] { outerPoint0, outerPoint1 };
        return true;
    }

    public void display(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, boolean drawFilled, StsColor stsColor)
    {
        GL gl = glPanel3d.getGL();

        if (gl == null || centerLinePoints == null) return;

        StsGLDraw.drawLine(gl, stsColor, true, centerLinePoints);
        if (displayCenterLinePoints)
        {
            glPanel3d.setViewShift(gl, 1.0f);
            StsGLDraw.drawPoint(gl, centerLinePoints[0].v, StsColor.WHITE, 6);
            glPanel3d.resetViewShift(gl);
        }
        if(drawFilled)
            StsGLDraw.drawTwoLineStrip(gl, innerPoints, outerPoints, innerPoints.length);
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
