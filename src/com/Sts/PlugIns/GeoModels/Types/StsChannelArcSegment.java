package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;

import javax.media.opengl.GL;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelArcSegment extends StsChannelSegment
{
    /** circular arc radius */
    float radius;
    /** circular arc sweep angle; positive is CCW */
    float arc;
    /** the circular arc center point */
    float[] centerPoint;
    /** uniformly sampled straight line between first and last arc lines: base of point bar.
     *  this will be gridded with innerRadius points to make point bar.  */
    StsPoint[] basePoints = null;

    static final float dArc = 10;

    public StsChannelArcSegment() { }

    public StsChannelArcSegment(StsChannel channel, float startDirection, float radius, float arc, StsPoint startPoint)
    {
        super(channel, startDirection, startPoint);
        this.radius = radius;
        this.arc = arc;
        computePoints();
    }

    public boolean computePoints()
    {
        try
        {
            // divide the arc into intervals and compute points on inner, outer, and center arcs
            int nIntervals = Math.max(5, StsMath.ceiling(Math.abs(arc) / dArc));

            centerLinePoints = new StsPoint[nIntervals + 1];
            innerPoints = new StsPoint[nIntervals + 1];
            outerPoints = new StsPoint[nIntervals + 1];
            basePoints =  new StsPoint[nIntervals + 1];

            float centerAngle, angle;
            float ddArc = arc / nIntervals;
            if (arc < 0)
            {
                centerAngle = startDirection;
                angle = startDirection + refDirection + 90;
            }
            else
            {
                centerAngle = startDirection + refDirection + 90;
                angle = startDirection;
            }

            float x0 = startPoint.getX();
            float y0 = startPoint.getY();
            float xc = (float) (x0 + radius * StsMath.cosd(centerAngle));
            float yc = (float) (y0 + radius * StsMath.sind(centerAngle));
            float z = startPoint.getZ();
            centerPoint = new float[] { xc, yc, z };

            float channelHalfWidth = channel.getChannelWidth()/2;
            float innerRadius = radius - channelHalfWidth;
            float outerRadius = radius + channelHalfWidth;

            for (int n = 0; n < nIntervals+1; n++, angle += ddArc)
            {
                float x, y;

                float cosa = (float)StsMath.cosd(angle);
                float sina = (float)StsMath.sind(angle);
                x = xc + radius * cosa;
                y = yc + radius * sina;
                centerLinePoints[n] = new StsPoint(x, y, z);
                x = xc + innerRadius * cosa;
                y = yc + innerRadius * sina;
                innerPoints[n] = new StsPoint(x, y, z);
                x = xc + outerRadius * cosa;
                y = yc + outerRadius * sina;
                outerPoints[n] = new StsPoint(x, y, z);
            }
            StsPoint endPoint = innerPoints[nIntervals];
            StsPoint dPoint = endPoint.subtract(startPoint);
            dPoint.divide(nIntervals);
            basePoints[0] = startPoint;
            for (int n = 0; n < nIntervals; n++)
                basePoints[n+1] = StsPoint.addPointsStatic(basePoints[n], dPoint);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "computeArcPoints", e);
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
        if(!drawFilled) return;
        // draw filled channels and point-bars
        StsGLDraw.drawTwoLineStrip(gl, innerPoints, outerPoints, innerPoints.length);
        StsGLDraw.drawTwoLineStippledStrip(gl, basePoints, innerPoints, innerPoints.length);
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
        return StsPoint.getLastPoint(centerLinePoints);
    }
}
