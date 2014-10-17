package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.DB.StsDBOutputStream;
import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.StsSerialize;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;

import javax.media.opengl.GL;
import java.io.IOException;
import java.io.Serializable;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public abstract class StsChannelSegment
{
    StsPoint startPoint;
    float startDirection;
    transient StsPoint[] points = null;

    static public byte LINE = 1;
    static public byte ARC = 2;

    public StsChannelSegment() { }

    public StsChannelSegment(float startDirection, StsPoint startPoint)
    {
        this.startDirection = startDirection;
        this.startPoint = startPoint;
    }

    public abstract boolean computePoints();

    public abstract void display(GL gl, boolean displayCenterLinePoints);

    public abstract void fillSerializableArrays(int index, byte[] segmentTypes, StsPoint[] startPoints, float[] startDirections, float[] sizes, float[] arcs);

    public void writeStsPointsArray(StsDBOutputStream out, int nPoints, StsPoint[] points) throws IllegalAccessException, IOException
    {
        for (int i = 0; i < nPoints; i++)
        {
            float[] v = points[i].v;
            out.writeInt(v.length);
            for (int j = 0; j < v.length; j++)
            {
                out.writeFloat(v[j]);
            }
        }
    }

    public void writeFloatArray(StsDBOutputStream out, int nFloats, float[] floats) throws IllegalAccessException, IOException
    {
        for (int i = 0; i < nFloats; i++)
                out.writeFloat(floats[i]);
    }

    public void writeByteArray(StsDBOutputStream out, int nBytes, byte[] bytes) throws IllegalAccessException, IOException
    {
        out.write(bytes);
    }

    public StsPoint getLastPoint()
    {
        return StsPoint.getLastPoint(points);
    }
}
