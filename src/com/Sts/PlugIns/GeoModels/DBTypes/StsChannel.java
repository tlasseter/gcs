package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsCustomSerializable;
import com.Sts.Framework.DB.StsDBInputStream;
import com.Sts.Framework.DB.StsDBOutputStream;
import com.Sts.Framework.DB.StsObjectDBFileIO;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsRotatedGridBoundingSubBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.UI.StsMessageFiles;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.PlugIns.GeoModels.Types.StsChannelArcSegment;
import com.Sts.PlugIns.GeoModels.Types.StsChannelLineSegment;
import com.Sts.PlugIns.GeoModels.Types.StsChannelSegment;

import javax.media.opengl.GL;
import java.io.IOException;
import java.io.Serializable;

/**
 * © tom 9/27/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannel extends StsRotatedGridBoundingSubBox implements StsTreeObjectI, StsXYGridable, Serializable, Cloneable, StsCustomSerializable
{
    private StsChannelSet channelSet;
    private float channelWidth;
    private float channelThickness;
    private StsPoint startPoint;
    private StsPoint endPoint;
    private float direction;
    /** persisted index of color in spectrum */
    protected int nColor = 0;

    transient public StsChannelSegment[] channelSegments;
    /** color for this channel */
    transient StsColor stsColor;
    /** display list number for surface fill */
    transient private int displayListNum = 0;
    /** Display lists currently being used for surface geometry */
    transient boolean usingDisplayLists = false;

    private boolean readoutEnabled = false;
    static protected StsObjectPanel objectPanel = null;

    /** these are colors used in drawing channels; color selected is defined by nColor.  -1 is null */
    static public StsColor[] colorList = StsColor.colors32NoGrey;

    static public final StsFieldBean[] displayFields =
    {
        new StsBooleanFieldBean(StsChannel.class, "isVisible", "Enable"),
        new StsBooleanFieldBean(StsChannel.class, "readoutEnabled", "Mouse Readout")
    };

    static public final StsFieldBean[] propertyFields = new StsFieldBean[]
    {
        new StsStringFieldBean(StsChannel.class, "name", true, "Name"),
        new StsStringFieldBean(StsChannel.class, "zDomainString", false, "Z Domain"),
        new StsIntFieldBean(StsChannel.class, "nRows", false, "Number of Lines"),
        new StsIntFieldBean(StsChannel.class, "nCols", false, "Number of Crosslines"),
        new StsIntFieldBean(StsChannel.class, "nSlices", false, "Number of Samples"),
        new StsFloatFieldBean(StsChannel.class, "rowNumMin", false, "Min Line"),
        new StsFloatFieldBean(StsChannel.class, "rowNumMax", false, "Max Line"),
        new StsFloatFieldBean(StsChannel.class, "colNumMin", false, "Min Crossline"),
        new StsFloatFieldBean(StsChannel.class, "colNumMax", false, "Max Crossline")
    };

    public StsChannel() { }

    public StsChannel(StsChannelSet channelSet, float channelWidth, float channelThickness, StsPoint firstPoint, StsPoint lastPoint, float direction)
    {
        this.channelSet = channelSet;
        this.channelWidth = channelWidth;
        this.channelThickness = channelThickness;
        this.startPoint = firstPoint;
        this.endPoint = lastPoint;
        this.direction = direction;
        nColor = (getIndex()% 31) + 1;  // color 0 is reserved for background (grey), loop over other 31 colors
        stsColor = colorList[nColor];

        // initialize the boundingBox container to the geoModelVolume
        // set the slice range in this subBox based on top and thickness
        initialize(channelSet.getGeoModelVolume());
        float zTop = startPoint.getZ();
        sliceMin = Math.round(getSliceCoor(zTop));
        sliceMax = Math.round(getSliceCoor(zTop + channelThickness));
    }

    public boolean initialize(StsModel model)
    {
        if(channelSegments == null) return true;
        stsColor = colorList[nColor];
        for(StsChannelSegment channelSegment : channelSegments)
        {
            if (!channelSegment.computePoints()) return false;
            channelSegment.buildGrids(channelSet.getGeoModelVolume());
        }
        return true;
    }

    public void display(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, boolean displayAxes, byte drawType, int zPlane)
    {
        if(subBoxContainsSlice(zPlane))
            display(glPanel3d, displayCenterLinePoints, displayAxes, drawType);
    }

    public void display(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, boolean displayAxes, byte drawType)
    {
        GL gl = glPanel3d.getGL();

        byte channelsState = channelSet.getChannelsState();
        if(channelsState == StsChannelSet.CHANNELS_AXES || displayAxes)
        {
            StsGLDraw.drawLine(gl, stsColor, true, new StsPoint[] { startPoint, endPoint});
        }
        else if(channelsState == StsChannelSet.CHANNELS_ARCS) // || channelsState == StsChannelSet.CHANNELS_GRIDS)
        {
            if (!currentModel.useDisplayLists && usingDisplayLists)
            {
                deleteDisplayLists(gl);
                usingDisplayLists = false;
            }

            if (currentModel.useDisplayLists)
            {
                if (displayListNum == 0) // build display list
                {
                    displayListNum = gl.glGenLists(1);
                    if (displayListNum == 0)
                    {
                        StsMessageFiles.logMessage("System Error in StsChannel.display(): " + "Failed to allocate a display list");
                        return;
                    }
                    gl.glNewList(displayListNum, GL.GL_COMPILE_AND_EXECUTE);
                    drawChannelSegments(glPanel3d, displayCenterLinePoints, channelsState, drawType);
                    gl.glEndList();

                    //timer.stop("display list surface setup: ");
                }
                gl.glCallList(displayListNum);
            }
            else
                drawChannelSegments(glPanel3d, displayCenterLinePoints, channelsState, drawType);
        }
    }

    private void drawChannelSegments(StsGLPanel3d glPanel3d, boolean displayCenterLinePoints, byte channelsState, byte drawType)
    {
        if(channelSegments == null) return;
        for (StsChannelSegment channelSegment : channelSegments)
            channelSegment.display(glPanel3d, displayCenterLinePoints, channelsState, drawType, stsColor);
    }

    public void fillData(byte[] byteData)
    {
        if(channelSegments == null) return;
        int channelIndex = getIndex();
        for(StsChannelSegment channelSegment : channelSegments)
            channelSegment.fillData(byteData, channelIndex);
    }

    public void fillData(byte[] byteData, int dir, int nPlane)
    {
        if(channelSegments == null) return;
        for(StsChannelSegment channelSegment : channelSegments)
            channelSegment.fillData(byteData, dir, nPlane, this);
    }


    public void deleteDisplayLists(GL gl)
    {
        if (displayListNum > 0)
        {
            gl.glDeleteLists(displayListNum, 1);
            displayListNum = 0;
        }
    }

    public StsFieldBean[] getDisplayFields() { return displayFields; }
    public StsFieldBean[] getPropertyFields() { return propertyFields; }
    public StsFieldBean[] getDefaultFields() { return null; }

    public Object[] getChildren()
    {
        return new Object[0];
    }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null)
        {
            objectPanel = StsObjectPanel.constructor(this, true);
        }
        return objectPanel;
    }

    public boolean anyDependencies() { return false; }
    public boolean canExport() { return false; }
    public boolean export() { return false; }
    public boolean canLaunch() { return false; }
    public boolean launch() { return false; }

    public void treeObjectSelected()
    {
        currentModel.setCurrentObject(this);
        currentModel.getGlPanel3d().checkAddView(StsView3d.class);
        currentModel.win3dDisplayAll();
    }

    public boolean isReadoutEnabled()
    {
        return readoutEnabled;
    }

    public void setReadoutEnabled(boolean readoutEnabled)
    {
        this.readoutEnabled = readoutEnabled;
    }

    public float getChannelWidth()
    {
        return channelWidth;
    }

    public float getChannelThickness()
    {
        return channelThickness;
    }

    public StsPoint getStartPoint()
    {
        return startPoint;
    }

    public StsPoint getEndPoint()
    {
        return endPoint;
    }

    public float getDirection()
    {
        return direction;
    }

    public byte getZDomainSupported()
    {
        return channelSet.getZDomainSupported();
    }
    // type of segment: 1 is line and 2 is arc
    byte[] segmentTypes;
    // Starting points for lines and arcs.
    StsPoint[] startPoints;
    // Starting direction for lines and arcs.  0 is in the +Y direction, +90 is -X, -90 is +X, -180 & +180 are -Y.
    float[] startDirections;
    // The length of lineSegments and the radius of arcSegments.
    float[] sizes;
    // Arc length of arcSegments.  Plus arcs are CW and minus arcs are CCW.
    float[] arcs;

    public void writeObject(StsDBOutputStream out) throws IllegalAccessException, IOException
    {
        if(channelSegments == null)
        {
            out.writeInt(0);
            return;
        }
        int nSegments = channelSegments.length;
        out.writeInt(nSegments);

        segmentTypes = new byte[nSegments];
        startPoints = new StsPoint[nSegments];
        startDirections = new float[nSegments];
        sizes = new float[nSegments];
        arcs = new float[nSegments];

        for(int n = 0; n < nSegments; n++)
            channelSegments[n].fillSerializableArrays(n, segmentTypes, startPoints, startDirections, sizes, arcs);

        out.write(segmentTypes);
        out.write(startPoints);
        out.write(startDirections);
        out.write(sizes);
        out.write(arcs);
        out.flush();

    }

    public void readObject(StsDBInputStream in) throws IllegalAccessException, IOException
    {
        int nSegments = in.readInt();
        if(nSegments == 0) return;


        segmentTypes = new byte[nSegments];
        in.readBytes(segmentTypes);
        startPoints = new StsPoint[nSegments];
        in.read(startPoints);
        startDirections = new float[nSegments];
        in.read(startDirections);
        sizes = new float[nSegments];
        in.read(sizes);
        arcs = new float[nSegments];
        in.read(arcs);

        channelSegments = new StsChannelSegment[nSegments];
        for(int n = 0; n < nSegments; n++)
        {
            if(segmentTypes[n] == StsChannelSegment.ARC)
                channelSegments[n] = new StsChannelArcSegment(this, startDirections[n], sizes[n], arcs[n], startPoints[n]);
            else
                channelSegments[n] = new StsChannelLineSegment(this, startPoints[n], startDirections[n], sizes[n]);
        }
    }

    public void exportObject(StsObjectDBFileIO objectIO) { }
}
