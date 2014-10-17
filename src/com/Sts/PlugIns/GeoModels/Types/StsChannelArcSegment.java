package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;

import javax.media.opengl.GL;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelArcSegment extends StsChannelSegment
{
    float radius;
    float arc;

    static final float dArc = 10;

    public StsChannelArcSegment() { }

    public StsChannelArcSegment(float startDirection, float radius, float arc, StsPoint startPoint)
    {
        super(startDirection, startPoint);
        this.radius = radius;
        this.arc = arc;
        computePoints();
    }

    public boolean computePoints()
    {
        try
        {
            int nSegments = Math.max(5, StsMath.ceiling(Math.abs(arc) / dArc));

            points = new StsPoint[nSegments + 1];
            points[0] = startPoint;

            float centerAngle, angle;
            float ddArc = arc / nSegments;
            if (arc < 0)
            {
                centerAngle = startDirection;
                angle = startDirection + 180;
            }
            else
            {
                centerAngle = startDirection + 180;
                angle = startDirection;
            }

            float x0 = startPoint.getX();
            float y0 = startPoint.getY();
            float xc = (float) (x0 + radius * StsMath.cosd(centerAngle));
            float yc = (float) (y0 + radius * StsMath.sind(centerAngle));
            float z = startPoint.getZ();
            for (int n = 1; n < nSegments+1; n++)
            {
                angle += ddArc;
                float x = (float) (xc + radius * StsMath.cosd(angle));
                float y = (float) (yc + radius * StsMath.sind(angle));
                points[n] = new StsPoint(x, y, z);
            }
            return true;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "computePoints", e);
            return false;
        }
    }

    static public float addAngles(float a1, float a2)
    {
        float a = a1 + a2;
        return adjustAngle(a);
    }

    static public float subtractAngles(float a1, float a2)
    {
        float a = a1 - a2;
        return adjustAngle(a);
    }

    static private float adjustAngle(float a)
    {
        if(a > 180)
        {
            while (a > 180)
                a -= 360;
        }
        if(a < -180)
        {
            while (a < -180)
                a += 360;
        }

        assert(a >= -180 && a <= 180);
        return a;
    }

    /** Angles are normally between -180 and +180 inclusive,
     *  where 180 is CCW from 0 and -180 is clockwise from 0,
     *  but angle may have rotated around past these limits.
     *  If so, we need to rotate in the opposite direction until within these limits.
     *  We assume that the refAngle is between -180 and +180 inclusive.
     *  dAngle is the angle to rotate from refAngle to given angle: dAngle = angle - refAngle.
     *  So if dAngle is > 180 then rotate CW to unwind and if < -180 rotate CCW to unwind.
     *  dAngle < -180      : CCW rotate to unwind
     *  -180 <= dAngle < 0 : CCW to unwind
     *  0 <= dAngle <= 180 : CW to unwind
     *  dAngle > 180       : CW to unwind
     * @param angle angle being provided
     * @param refAngle reference angle
     * @return  desired rotation to unwind back towards refAngle.
     */
    public static boolean rotateCW(float angle, float refAngle)
    {
        return angle >= refAngle;
    }

    public void display(GL gl, boolean displayCenterLinePoints)
    {
        if (gl == null || points == null) return;
        StsGLDraw.drawLine(gl, StsColor.RED, true, points);
        if(displayCenterLinePoints)
            StsGLDraw.drawPoint(gl, points[0].v, StsColor.RED, 6);
    }

    public void fillSerializableArrays(int index, byte[] segmentTypes, StsPoint[] startPoints, float[] startDirections, float[] sizes, float[] arcs)
    {
        segmentTypes[index] = ARC;
        startPoints[index] = startPoint;
        startDirections[index] = startDirection;
        sizes[index] = radius;
        arcs[index] = arc;
    }

    public StsPoint getLastPoint()
    {
        return StsPoint.getLastPoint(points);
    }
}
