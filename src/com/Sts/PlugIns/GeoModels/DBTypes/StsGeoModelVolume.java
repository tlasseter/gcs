package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.StsVolumeDisplayable;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.DataCube.StsBlocksMemoryManager;
import com.Sts.Framework.Utilities.DataCube.StsCubeFileBlocks;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.Framework.Utilities.StsParameters;

import javax.media.opengl.GL;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * © tom 9/27/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsGeoModelVolume extends StsRotatedGridBoundingBox implements StsTreeObjectI, StsXYGridable, StsVolumeDisplayable, Serializable, Cloneable
{
    protected StsObjectRefList channelSets;
    /** The current colorscale viewed on the object panel */
    protected StsColorscale colorscale;
    /** a grid on cell centers reduced by half a grid cell on each side.  Used in grid construction. */
    transient public StsRotatedGridBoundingBox centeredGrid;

    transient StsBlocksMemoryManager blocksMemory = null;
    transient public StsCubeFileBlocks[] filesMapBlocks;
    transient public StsCubeFileBlocks fileMapRowFloatBlocks;

    transient public StsColorList geoVolumeColorList = null;

    private boolean readoutEnabled = false;

    /** Indicates whether cube is time or depth */
    public String zDomain = StsParameters.TD_DEPTH_STRING;

    static private boolean smallVolume = false;

    static protected StsObjectPanel objectPanel = null;

    static public final StsFieldBean[] geoModelDisplayFields =
    {
        new StsBooleanFieldBean(StsGeoModelVolume.class, "isVisible", "Enable"),
        new StsBooleanFieldBean(StsGeoModelVolume.class, "readoutEnabled", "Mouse Readout")
    };

    static public final StsFieldBean[] geoModelPropertyFields = new StsFieldBean[]
    {
        new StsStringFieldBean(StsGeoModelVolume.class, "name", true, "Name"),
        new StsStringFieldBean(StsGeoModelVolume.class, "zDomainString", false, "Z Domain"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nRows", false, "Number of Lines"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nCols", false, "Number of Crosslines"),
        new StsIntFieldBean(StsGeoModelVolume.class, "nSlices", false, "Number of Samples"),
        new StsDoubleFieldBean(StsGeoModelVolume.class, "xOrigin", false, "X Origin"),
        new StsDoubleFieldBean(StsGeoModelVolume.class, "yOrigin", false, "Y Origin"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "xInc", false, "X Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "yInc", false, "Y Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTInc", false, "Z or T Inc"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "xMin", false, "X Loc Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "yMin", false, "Y Loc Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTMin", false, "Z or T Min"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "zTMax", false, "Z or T Max"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "angle", false, "Angle to Line Direction"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "rowNumMin", false, "Min Line"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "rowNumMax", false, "Max Line"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "colNumMin", false, "Min Crossline"),
        new StsFloatFieldBean(StsGeoModelVolume.class, "colNumMax", false, "Max Crossline")
    };

    public StsGeoModelVolume() { }

    public StsGeoModelVolume(boolean persistent)
    {
        super(persistent);
        initializeVolume();
    }

    public boolean initialize(StsModel model)
    {
        centeredGrid = createCenteredGrid();
        initializeColorscale();
        return true;
    }

    public void initializeVolume()
    {
        xMin = 0;
        yMin = 0;
        zMin = 0;
        angle = 0;

        xOrigin = 0;
        yOrigin = 0;
        rowNumMin = 0;
        colNumMin = 0;
        rowNumInc = 1;
        colNumInc = 1;

        if(smallVolume)
        {
            xInc = 200;
            yInc = 100;
            zInc = 4;

            nRows = 101;
            nCols = 51;
            nSlices = 26;
        }
        else
        {
            xInc = 10;
            yInc = 10;
            zInc = 0.2f;

            nRows = 1001;
            nCols = 1001;
            nSlices = 501;
        }
        centeredGrid = createCenteredGrid();
        initializeColorscale();
    }

    public byte getZDomainSupported()
    {
        return StsParameters.getZDomainFromString(zDomain);
    }

    public byte getZDomain()
    {
        return StsParameters.getZDomainFromString(zDomain);
    }

    public boolean canDisplayZDomain()
    {
        StsProject project = currentModel.getProject();
        return project.canDisplayZDomain(getZDomain()) || project.hasVelocityModel();
    }

    public void addChannelSet(StsChannelSet channelSet)
    {
        if(channelSets == null) channelSets = StsObjectRefList.constructor(1, 1, "channelSets", this, true);
        channelSets.add(channelSet);
    }

    public boolean checkGrid()
    {
        boolean gridOK = true;

        if (nCols <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number columns incorrect: " + nCols +
                            " for volume " + getName());
            gridOK = false;
        }
        if (nRows <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number rows incorrect: " + nRows +
                            " for volume " + getName());
            gridOK = false;
        }
        if (nSlices <= 0)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Number slices incorrect: " + nSlices +
                            " for volume " + getName());
            gridOK = false;
        }
        if (xInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "X increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (yInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Y increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (zInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "Z increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (rowNumInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "row number increment is zero" + " for volume " + getName());
            gridOK = false;
        }
        if (colNumInc == 0.0f)
        {
            new StsMessage(currentModel.win3d, StsMessage.WARNING,
                    "col number increment is zero" + " for volume " + getName());
            gridOK = false;
        }

        if(gridOK)
        {
            xMax = xMin + (nCols - 1) * xInc;
            yMax = yMin + (nRows - 1) * yInc;
            zMax = zMin + (nSlices - 1) * zInc;
            rowNumMax = rowNumMin + (nRows - 1);
            colNumMax = colNumMin + (nCols - 1);
            setAngle();
        }
        return gridOK;
    }

    public StsFieldBean[] getDisplayFields() { return geoModelDisplayFields; }
    public StsFieldBean[] getPropertyFields() { return geoModelPropertyFields; }
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

    public boolean getReadoutEnabled()
    {
        return readoutEnabled;
    }

    public void setReadoutEnabled(boolean readoutEnabled)
    {
        this.readoutEnabled = readoutEnabled;
    }

    // to be implemented
    public int getPlaneValue(int dir, float[] xyz)
    {
        int row = -1, col = -1, slice = -1;
        byte signedByteValue;
        byte[] planeData;
        try
        {
            row = getNearestBoundedRowCoor(xyz[1]);
            col = getNearestBoundedColCoor(xyz[0]);
            slice = getNearestBoundedSliceCoor(xyz[2]);
            signedByteValue = -1;
//            signedByteValue = filesMapBlocks[YDIR].readByteValue(row, col, slice);
            return StsMath.signedByteToUnsignedInt(signedByteValue);
        }
        catch (Exception e)
        {
            StsException.systemError(
                    "StsSeismicVolume.getPlaneValue() failed for row " + row +
                            " col " + col +
                            " slice " +
                            slice);
            return 0;
        }
    }
    public String getUnits()
    {
        return "";
    }

    public StsGeoModelVolumeClass getGeoModelVolumeClass()
    {
        return (StsGeoModelVolumeClass) currentModel.getCreateStsClass(getClass());
    }

    public ByteBuffer readByteBufferPlane(int dir, float dirCoordinate)
    {
        try
        {
            if(channelSets == null) return null;

            int nPlane = this.getCursorPlaneIndex(dir, dirCoordinate);
            if (nPlane == -1) return null;
            int nBytes = getNSamplesInPlane(dir);
            byte[] byteData = new byte[nBytes];
            Arrays.fill(byteData, (byte)-1);
            StsChannelSet[] channelSetList = (StsChannelSet[])channelSets.getCastList(StsChannelSet.class);
            for(StsChannelSet channelSet : channelSetList)
                channelSet.fillData(byteData, dir, nPlane);
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteData);
            return checkInterpolateUserNull(dir, nPlane, byteBuffer);
        }
        catch (Exception e)
        {
            StsException.outputException("StsSeismicVolume.readPlaneData() failed.", e, StsException.WARNING);
            return null;
        }
    }

    private ByteBuffer checkInterpolateUserNull(int dir, int nPlane, ByteBuffer byteBuffer)
    {
        boolean fillPlaneNulls = getGeoModelVolumeClass().getFillPlaneNulls();
        if (!fillPlaneNulls) return byteBuffer;
        //StsVolumeInterpolation volumeInterpolator = StsVolumeInterpolation.getInstance(this);
        //return volumeInterpolator.interpolatePlane(dir, nPlane, byteBuffer);
        return null;
    }

    public boolean setGLColorList(GL gl, boolean nullsFilled, int dir, int shader)
    {
        /*
        if (dir == ZDIR && currentAttribute != null && currentAttribute != nullAttribute)
            return currentAttribute.setGLColorList(gl, nullsFilled, shader);
        else if
        */
        if (geoVolumeColorList != null)
            return geoVolumeColorList.setGLColorList(gl, nullsFilled, shader);
        else
            return false;
    }

    public void initializeColorscale()
    {
        try
        {
            if (colorscale == null)
            {
                StsSpectrumClass spectrumClass = currentModel.getSpectrumClass();
                colorscale = new StsColorscale("Test", spectrumClass.getSpectrum(spectrumClass.SPECTRUM_AUTOCAD), 0, 255);
                colorscale.setEditRange(0, 255);
            }
            geoVolumeColorList = new StsColorList(colorscale);
            colorscale.addActionListener(this);
        }
        catch (Exception e)
        {
            StsException.outputException("StsSeismicVolume.initializeColorscale() failed.", e, StsException.WARNING);
        }
    }

    public byte[] readBytePlaneData(int dir, float dirCoordinate)
    {
        try
        {
            int nPlane = this.getCursorPlaneIndex(dir, dirCoordinate);
            if (nPlane == -1) return null;
            int nBytes = getNSamplesInPlane(dir);
            byte[] byteData = new byte[nBytes];
            for(int n = 0; n < nBytes; n++)
                byteData[n] = (byte)n;
            return byteData;
            //return filesMapBlocks[dir].readBytePlane(nPlane);
        }
        catch (Exception e)
        {
            StsException.outputException("StsSeismicVolume.readPlaneData() failed.", e, StsException.WARNING);
            return null;
        }
    }

    public final boolean isByteValueNull(byte byteValue)
    {
        return byteValue == -1;
    }
}
