package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelClass;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelSet;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

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

    public void display(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, byte channelsState, byte drawType, StsColor stsColor)
    {
        GL gl = glPanel3d.getGL();

        if (gl == null || centerLinePoints == null) return;

        StsGLDraw.drawLine(gl, stsColor, true, centerLinePoints);
        if (displayCenterLinePoints)
        {
            glPanel3d.setViewShift(gl, 1.0f);
            StsGLDraw.drawPoint(gl, centerLinePoints[0].v, StsColor.GREEN, 6);
            glPanel3d.resetViewShift(gl);
        }
        if(drawType == StsChannelClass.DRAW_FILLED && channelsState >= StsChannelSet.CHANNELS_ARCS)
            StsGLDraw.drawTwoLineStrip(gl, innerPoints, outerPoints, innerPoints.length, stsColor);
        else if(drawType == StsChannelClass.DRAW_GRID && channelsState == StsChannelSet.CHANNELS_GRIDS)
        {
            channelCellGrid.display(gl, stsColor, false);
        }
    }

    public void fillData(byte[] byteData, int nChannel)
    {
        this.channelCellGrid.fillData(byteData, nChannel);
    }


    public void fillData(byte[] byteData, int dir, int nPlane, StsChannel channel)
    {
        channelCellGrid.fillData(byteData, dir, nPlane, channel);
    }

    public void fillSerializableArrays(int index, byte[] segmentTypes, StsPoint[] startPoints, float[] startDirections, float[] sizes, float[] arcs)
    {
        segmentTypes[index] = LINE;
        startPoints[index] = startPoint;
        startDirections[index] = startDirection;
        sizes[index] = length;
        arcs[index] = 0;
    }

    public void buildGrids(StsGeoModelVolume geoModelVolume)
    {
        channelCellGrid = new StsSegmentCellGrid(outerPoints, innerPoints, geoModelVolume, startPoint.getZ());
    }

}
