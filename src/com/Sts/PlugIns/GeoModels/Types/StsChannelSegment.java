package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.DB.StsDBOutputStream;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

import java.io.IOException;
import java.util.Iterator;

/**
 * © tom 10/8/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public abstract class StsChannelSegment
{
    StsChannel channel;
    /** first point on centerLine for this segment */
    StsPoint startPoint;
    /** start direction of centerLine in degrees (0 is +Y) */
    float startDirection;

    /** uniform points along centerLine (2 for lineSegment and every dArc degrees for arcSegment */
    StsPoint[] centerLinePoints = null;
    /** uniform points along outer line/arc */
    StsPoint[] outerPoints = null;
    /** uniform points along inner line/arc */
    StsPoint[] innerPoints = null;

    StsSegmentCellGrid channelCellGrid;

    static final float refDirection = 90; // ref direction is North (+Y) which is a 90 deg positive ((CCW) rotation from global 0 degrees (+X)

    static public final byte LINE = 1;
    static public final byte ARC = 2;

    static public final byte GRID_NONE = 0;
    static public final byte GRID_CHANNEL = 1;
    static public final byte GRID_POINT_BAR = 2;
    static public final byte GRID_OVERBANK = 3;
    static public final byte GRID_CREVASSE_SPLAY = 4;

    public StsChannelSegment() { }

    public StsChannelSegment(StsChannel channel, float startDirection, StsPoint startPoint)
    {
        this.channel = channel;
        this.startDirection = startDirection;
        this.startPoint = startPoint;
    }

    public abstract boolean computePoints();
    public abstract void display(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, byte channelsState, byte drawType, StsColor stsColor);
    public abstract void fillSerializableArrays(int index, byte[] segmentTypes, StsPoint[] startPoints, float[] startDirections, float[] sizes, float[] arcs);
    public abstract void buildGrids(StsGeoModelVolume geoModelVolume);
    public abstract void fillData(byte[] byteData, int nChannel);
    public abstract void fillData(byte[] byteData, int dir, int nSlice, StsChannel channel);

    public StsPoint getLastInnerPoint() { return innerPoints[innerPoints.length-1]; }
    public StsPoint getLastOuterPoint() { return outerPoints[innerPoints.length-1]; }

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
        return StsPoint.getLastPoint(centerLinePoints);
    }
}
