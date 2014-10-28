package com.Sts.PlugIns.GeoModels.Views;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DBTypes.StsCropVolume;
import com.Sts.Framework.DBTypes.StsMainObject;
import com.Sts.Framework.DBTypes.StsObject;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsPoint2D;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsJOGLShader;
import com.Sts.Framework.Utilities.StsParameters;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelClass;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolumeClass;

import javax.media.opengl.GL;
import java.nio.ByteBuffer;

public class StsGeoModelCursorSection extends StsCursor3dVolumeTexture
{
    public StsGeoModelVolume geoModelVolume;
    transient public ByteBuffer dataBuffer = null;
    transient public ByteBuffer subVolumeDataBuffer = null;
    transient public boolean cropChanged = false;
    transient boolean isVisible = true;
    transient byte[] transparentData = null;
    transient private StsGeoModelVolumeClass geoModelVolumeClass;
    transient private StsChannelClass channelClass;

    /** minimum size in pixels of a crossplot point */
    static private int minPointSize = 4;

    static private final boolean debug = false;

    public StsGeoModelCursorSection()
    {
    }

    public StsGeoModelCursorSection(StsModel model, StsGeoModelVolume geoModelVolume, StsCursor3d cursor3d, int dir)
    {
        this.geoModelVolume = geoModelVolume;
        initialize(model, cursor3d, dir);
    }

    public boolean initialize(StsModel model, StsCursor3d cursor3d, int dir)
    {
        super.initialize(model, cursor3d, dir);
        if(geoModelVolume == null) return false;
        geoModelVolumeClass = geoModelVolume.getGeoModelVolumeClass();
        channelClass = (StsChannelClass)model.getCreateStsClass(StsChannel.class);

        isPixelMode = getIsPixelMode();
        return true;
    }

    protected boolean getIsPixelMode()
    {
        return geoModelVolumeClass.isPixelMode();
    }

    public int getDefaultShader() { return StsJOGLShader.ARB_TLUT_NO_SPECULAR_LIGHTS; }
    public boolean getUseShader() { return geoModelVolume.getGeoModelVolumeClass().getContourColors(); }

    /**
     * This results in texture being deleted on next displayTexture call.
     * Delete the texture unconditionally unless plane is ZDIR.
     * If is ZDIR, delete texture if not displaying basemap or if basemap has changed.
     */
    public boolean textureChanged()
    {
        if (geoModelVolume == null)
        {
            textureChanged = false;
            return false;
        }
        textureChanged = true;

        if(debug) StsException.systemDebug(this, "textureChanged");
        return textureChanged;
    }

    public boolean dataChanged()
    {
        return textureChanged();
    }

    public void cropChanged()
    {
        /*
                 if (textureTiles == null)
           {
            if(!checkTextureTiles()) return;
           }
                 else
           */
        if (textureTiles == null) return;
//		addTextureToDeleteList();
        textureTiles.cropChanged();
    }

    public void subVolumeChanged()
    {
    }

    public Class getDisplayableClass() { return StsGeoModelVolume.class; }

    public boolean canDisplayClass(Class c) { return StsGeoModelVolume.class.isAssignableFrom(c); }

    public boolean isDisplayableObject(Object object)
    {
        return (object instanceof StsGeoModelVolume);
    }

    public boolean isDisplayingObject(Object object)
    {
        if (geoModelVolume == object) return true;
        if (this.textureTiles != null && object == textureTiles.cropVolume) return true;
        return false;
    }

    public boolean setDirCoordinate(float dirCoordinate)
    {
        if (this.dirCoordinate == dirCoordinate) return false;
        this.dirCoordinate = dirCoordinate;
        textureChanged();
        // StsXPolygonAction.clearSeismicPoints();
        return true;
    }

    public boolean setObject(Object object)
    {
        if (object == geoModelVolume) return false;

        if (debug) StsException.systemDebug(this, "setObject", "Object changed from " +
            StsMainObject.getObjectName(geoModelVolume) + " to: " +
            StsMainObject.getObjectName((StsMainObject) object));

        if (isDisplayableObject(object))
        {
            geoModelVolume = (StsGeoModelVolume) object;
            isVisible = true;
        }
        else if (object != null)
            return false;
        else // object == null
        {
            isVisible = false;
            geoModelVolume = null;
        }
        textureChanged();
        return true;
    }

    public Object getObject()
    {
        return geoModelVolume;
    }

    public boolean isVisible()
    {
        if (!isVisible) return false;
        if (geoModelVolume == null) return false;
        return true;
    }

    protected void checkTextureAndGeometryChanges(StsGLPanel3d glPanel3d, boolean is3d)
    {
        if (isPixelMode != geoModelVolume.getGeoModelVolumeClass().isPixelMode())
        {
            textureChanged = true;
            isPixelMode = !isPixelMode;
        }

        if (geoModelVolumeChanged())
        {
            textureChanged = true;
        }

        byte projectZDomain = StsObject.getCurrentModel().getProject().getZDomain();
        if (projectZDomain != zDomain)
        {
            geometryChanged = true;
            zDomain = projectZDomain;
        }
    }

    protected boolean initializeTextureTiles(StsGLPanel3d glPanel3d)
    {
        // if (!glPanel3d.initialized) return false;
        if (geoModelVolume == null) return false;

        if(channelClass.getDrawTypeByte() != StsChannelClass.DRAW_GRID) return false;

        if (textureTiles == null)
        {
            StsCropVolume subVolume = glPanel3d.model.getProject().getCropVolume();
            textureTiles = StsTextureTiles.constructor(model, this, dirNo, geoModelVolume, isPixelMode, subVolume, true);
            if (textureTiles == null) return false;
            nTextureRows = textureTiles.nTotalRows;
            nTextureCols = textureTiles.nTotalCols;
            textureChanged = true;
//            geometryChanged = true;
        }
        else if (!textureTiles.isSameSize(geoModelVolume))
        {
            textureTiles.constructTiles(geoModelVolume, false);
            nTextureRows = textureTiles.nTotalRows;
            nTextureCols = textureTiles.nTotalCols;
            textureChanged = true;
//            geometryChanged = true;
        }

        if (textureTiles.shaderChanged())
        {
            textureChanged = true;
        }
        textureTiles.setTilesDirCoordinate(dirCoordinate);

        return true;
    }

    protected void computeTextureData()
    {
        if (debug)
        {
            int nPlane = geoModelVolume.getCursorPlaneIndex(dirNo, dirCoordinate);
            System.out.println("Texture changed, reading new seismic texture." + "dir: " + dirNo + " " + " coor: " +
                dirCoordinate + " plane: " + nPlane);
        }
        dataBuffer = geoModelVolume.readByteBufferPlane(dirNo, dirCoordinate);
        if (dataBuffer == null)
        {
            textureChanged = false;
            return;
        }
        // byte[] subVolumePlane = cursor3d.getSubVolumePlane(dirNo, dirCoordinate, geoModelVolume, geoModelVolume.getZDomain());
        subVolumeDataBuffer = dataBuffer;
        if(subVolumeDataBuffer == null)
        {
            StsException.systemError(this, "computeTextureData", "Failed to compute subVolumeDataBuffer");
            subVolumeDataBuffer = dataBuffer;
        }
        if (debug)
        {
            String message = "read dataBuffer for volume: " + geoModelVolume.getName() + " dirNo: " + dirNo;
            String methodName = "displayTexture";
            textureDebug(subVolumeDataBuffer, nTextureRows, nTextureCols, this, methodName, message, nullByte);
            StsException.systemDebug(this, "displayTexture", "Displaying changed seismic texture 3d in dirNo " + dirNo + ".");
            {
                subVolumeDataBuffer.rewind();

                byte sample = -1;
                int row = -1, col = -1;
                for(row = 0; row < nTextureRows; row++)
                {
                    for(col = 0; col < nTextureCols; col++)
                    {
                        byte value = dataBuffer.get();
                        if(value > 0)
                        {
                            sample = value;
                            break;
                        }
                    }
                }
                subVolumeDataBuffer.rewind();
                if(sample != -1)
                    StsException.systemDebug(this, "displayTexture", "read dataBuffer for volume: " + geoModelVolume.getName() + " dirNo: " + dirNo +
                        " First byte value > 0: dataBuffer[" + row + "][" + col + "] = " + sample);
                else
                    StsException.systemDebug(this, "displayTexture", "read dataBuffer for volume: " + geoModelVolume.getName() + " dirNo: " + dirNo +
                        " NO positive bytes");
            }
        }
    }

    protected boolean enableGLState(StsGLPanel3d glPanel3d, GL gl, boolean is3d)
    {
        super.enableGLState(glPanel3d, gl, is3d);
        if (debug) StsException.systemDebug(this, "displayTexture", "setGLColorList called with shader: " + textureTiles.shader);
        return geoModelVolume.setGLColorList(gl, false, dirNo, textureTiles.shader);
    }

    protected void displayTiles3d(StsGLPanel3d glPanel3d, GL gl)
    {
        textureTiles.displayTiles(this, gl, isPixelMode, subVolumeDataBuffer, nullByte);
        subVolumeDataBuffer = null;
    }

    protected void displayTiles2d(StsGLPanel3d glPanel3d, GL gl)
    {
        StsViewCursor viewCursor = (StsViewCursor)glPanel3d.getView();
        textureTiles.displayTiles2d(this, gl, viewCursor.axesFlipped, isPixelMode, subVolumeDataBuffer, nullByte);
        subVolumeDataBuffer = null;
    }

    private boolean geoModelVolumeChanged()
    {
        /*
        StsGeoModelVolumeClass geoModelVolumeClass = (StsGeoModelVolumeClass) model.getStsClass(geoModelVolume.getClass());
        StsGeoModelVolume currentSeismicVolume = geoModelVolumeClass.getCurrentGeoModelVolume();
        if (geoModelVolume == currentSeismicVolume)
            return geoModelVolume.hasAttributeChanged();

        if (currentSeismicVolume == null) return true;

        return true;
        */
        return false;
    }

    private byte[] transparentData()
    {
        if (transparentData != null) return transparentData;
        transparentData = new byte[nTextureRows * nTextureCols];
        return transparentData;
    }

    /**
     * The subVolumePlane has the dimensions of the cursorBoundingBox it cuts through;
     * The dataBuffer has the dimensions of the corresponding seismic/virtual volume.
     * If they are the same size, apply the dataBuffer directly to the subVolumePlane;
     * if not get the grid coordinates of the dataBuffer relative to the subVolume plane
     * and apply.
     */
    protected ByteBuffer applySubVolume(byte[] subVolumePlane)
    {
        int nCursorCols;
        int cursorIndex, seismicIndex;

        if (dataBuffer == null) return null;
        if (subVolumePlane == null) return dataBuffer;
        return null;
    /*
        if (!geoModelVolume.getGeoModelVolumeClass().getDisplayOnSubVolumes())
            return dataBuffer;

        int nPlanePoints = nTextureRows * nTextureCols;
        ByteBuffer subVolumeData = ByteBuffer.allocateDirect(nPlanePoints);

        if (nPlanePoints == subVolumePlane.length)
        {
            for (int n = 0; n < nPlanePoints; n++)
            {
                if (subVolumePlane[n] == 0)
                    subVolumeData.put((byte) -1);
                else
                    subVolumeData.put(dataBuffer.get(n));
            }
            return subVolumeData;
        }
        else
        {
            nCursorCols = cursorBoundingBox.getNCursorCols(dirNo);

            int seismicRowStart = cursorBoundingBox.getSubVolumeCursorRowMin(geoModelVolume, dirNo);
            int seismicRowEnd = cursorBoundingBox.getSubVolumeCursorRowMax(geoModelVolume, dirNo);
            int seismicColStart = cursorBoundingBox.getSubVolumeCursorColMin(geoModelVolume, dirNo);
            int seismicColEnd = cursorBoundingBox.getSubVolumeCursorColMax(geoModelVolume, dirNo);

            cursorIndex = 0;
            seismicIndex = 0;

            try
            {
                for (int row = seismicRowStart; row <= seismicRowEnd; row++)
                {
                    cursorIndex = row * nCursorCols + seismicColStart;
                    for (int col = seismicColStart; col <= seismicColEnd; col++, cursorIndex++, seismicIndex++)
                    {
                        byte planeByte = dataBuffer.get();
                        if (subVolumePlane[cursorIndex] != 0)
                            subVolumeData.put(planeByte);
                        else
                            subVolumeData.put(StsParameters.nullByte);
                    }
                }
                return subVolumeData;
            }
            catch (Exception e)
            {
                StsException.outputException("StsSeismicCursorSection.applySubVolume() failed.", e, StsException.WARNING);
                return null;
            }
        }
    */
    }

    public void drawTextureTileSurface(StsTextureTile tile, GL gl, boolean is3d, int nTile)
    {
        drawTextureTileSurface(geoModelVolume, tile, gl, is3d, geoModelVolume.getZDomain());
    }

    /*
      public void drawTextureTileSurface2d(StsTextureTile tile, GL gl)
      {
       line2d.drawTextureTileSurface(tile, gl, dirNo, false);
      }
      */
    public void display(StsGLPanel3d glPanel3d, boolean is3d)
    {
        if (geoModelVolume == null)
            return;

        if (!geoModelVolume.canDisplayZDomain()) return;

        GL gl = glPanel3d.getGL();
        if (is3d)
        {
            //displayCrossplotPoints(gl, glPanel3d);
        }
        else
        {
            StsViewCursor viewCursor = (StsViewCursor) glPanel3d.getView();
            //displayCrossplotPoints2d(gl, glPanel3d, viewCursor);
            //displaySeismicPolygons2d(gl, glPanel3d, viewCursor.axesFlipped);
        }
    }

    public String propertyReadout(StsPoint point)
    {
        StringBuffer stringBuffer = null;
        if (geoModelVolume == null)
            return new String("Nothing is currently displayed on the cursor plane.");

        StsObject[] volumes = model.getObjectList(geoModelVolume.getClass());
        for (int i = 0; i < volumes.length; i++)
        {
            StsGeoModelVolume volume = (StsGeoModelVolume) volumes[i];
            if (!volume.getReadoutEnabled() && volume != geoModelVolume)
            {
                continue;
            }
            if (stringBuffer == null)
            {
                stringBuffer = new StringBuffer();
            }
            byte byteValue = (byte) volume.getPlaneValue(dirNo, point.v);
            if (byteValue == StsParameters.nullByte)
                stringBuffer.append(" " + volume.getName() + ": null");
            else
                stringBuffer.append(" " + volume.getName() + ": " + volume.getScaledValue(byteValue)+ " "+volume.getUnits());
        }
        if (stringBuffer == null)
        {
            return null;
        }
        return stringBuffer.toString();
    }


    public int[] getDataRowCol(StsPoint2D point)
    {
        int row, col;

        if (dirNo == StsCursor3d.XDIR) // first row is vertically down
        {
            row = (int) geoModelVolume.getRowCoor(point.x);
            col = (int) geoModelVolume.getSliceCoor(point.y);
        }
        else if (dirNo == StsCursor3d.YDIR) // first row is vertically down
        {
            row = (int) geoModelVolume.getColCoor(point.x);
            col = (int) geoModelVolume.getSliceCoor(point.y);
        }
        else // dirNo == ZDIR  first row is horizontally across
        {
            row = (int) geoModelVolume.getRowCoor(point.y);
            col = (int) geoModelVolume.getColCoor(point.x);
        }
        if (isInRange(row, col))
        {
            return new int[]
                {row, col};
        }
        else
        {
            return null;
        }
    }

    /** given 2D coordinates on this 2D cursorSection, return the row col coordinates */
    public float[] getDataRowColF(float x, float y)
    {
        float rowF, colF;

        if (dirNo == StsCursor3d.XDIR) // first row is vertically down
        {
            rowF = geoModelVolume.getRowCoor(x);
            colF = geoModelVolume.getSliceCoor(y);
        }
        else if (dirNo == StsCursor3d.YDIR) // first row is vertically down
        {
            rowF = geoModelVolume.getColCoor(x);
            colF = geoModelVolume.getSliceCoor(y);
        }
        else // dirNo == ZDIR  first row is horizontally across
        {
            rowF = geoModelVolume.getRowCoor(y);
            colF = geoModelVolume.getColCoor(x);
        }
        if (isInRange(rowF, colF))
        {
            return new float[]
                {rowF, colF};
        }
        else
        {
            return null;
        }
    }

    /** given 3D coordinates on this 3d cursorSection, return the row col coordinates */
    public float[] getDataRowColF(StsPoint point)
    {
        float rowF, colF;

        if (dirNo == StsCursor3d.XDIR) // first row is vertically down
        {
            rowF = geoModelVolume.getRowCoor(point.getY());
            colF = geoModelVolume.getSliceCoor(point.getZ());
        }
        else if (dirNo == StsCursor3d.YDIR) // first row is vertically down
        {
            rowF = geoModelVolume.getColCoor(point.getX());
            colF = geoModelVolume.getSliceCoor(point.getZ());
        }
        else // dirNo == ZDIR  first row is horizontally across
        {
            rowF = geoModelVolume.getRowCoor(point.getY());
            colF = geoModelVolume.getColCoor(point.getX());
        }
        if (isInRange(rowF, colF))
        {
            return new float[]
                {rowF, colF};
        }
        else
        {
            return null;
        }
    }

    public String getName()
    {
        return "Cursor view[" + dirNo + "] of: " + geoModelVolume.getName();
    }
}
