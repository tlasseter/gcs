package com.Sts.Framework.MVC.Views;

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;
import com.sun.opengl.util.*;

import javax.media.opengl.*;
import java.nio.*;

/**
 * Textures consists of n by n square texels.  The center of the first texel
 * is 1/(2*n) and the center of the last is 1-1/(2*n).  Each tile butts against
 * the edge of the adjoining tile, i.e., they share texels along that common edge.
 */

public class StsTextureTile
{
    int nTotalRows, nTotalCols;
    int nTotalPoints;
    public int rowMin, rowMax, colMin, colMax;
    public int croppedRowMin, croppedRowMax, croppedColMin, croppedColMax;
    public int nRows, nCols;
    StsTextureTiles textureTiles;
    int nBackgroundRows, nBackgroundCols;
    public double minRowTexCoor, minColTexCoor;
    public double maxRowTexCoor, maxColTexCoor;
    public double dRowTexCoor, dColTexCoor;
    boolean cellCenteredTexture = false;
    public double[][] xyzPlane = new double[4][];
    int texture = 0;
    ByteBuffer tileData;
    int displayListNum = 0;
    int displayListNum2d = 0;
    boolean axesFlipped = false;
    public int nTile = -1;

    static private final int XDIR = StsCursor3d.XDIR;
    static private final int YDIR = StsCursor3d.YDIR;
    static private final int ZDIR = StsCursor3d.ZDIR;

    static private boolean runTimer = false;
    static private StsTimer timer = null;
    static public boolean debug = false;

    public StsTextureTile(int nTotalRows, int nTotalCols, int row, int col, StsTextureTiles textureTiles, int maxTextureSize, boolean cellCenteredTexture, int nTile)
    {
        this.nTotalRows = nTotalRows;
        this.nTotalCols = nTotalCols;
        this.nTotalPoints = nTotalRows * nTotalCols;
        this.rowMin = row;
        this.colMin = col;
        this.nTile = nTile;
        this.textureTiles = textureTiles;
        this.cellCenteredTexture = cellCenteredTexture;

        if(runTimer && timer == null) timer = new StsTimer();

        rowMax = Math.min(row + maxTextureSize - 1, nTotalRows - 1);
        colMax = Math.min(col + maxTextureSize - 1, nTotalCols - 1);

        initializeCroppedRange();

        nRows = rowMax - rowMin + 1;
        nCols = colMax - colMin + 1;

        nBackgroundRows = StsMath.nextBaseTwoInt(nRows);
        nBackgroundCols = StsMath.nextBaseTwoInt(nCols);

        double rowBorder = 0;
        double colBorder = 0;
        if(!cellCenteredTexture)
        {
            rowBorder = 1.0 / (2.0 * nBackgroundRows);
            colBorder = 1.0 / (2.0 * nBackgroundCols);
        }

        minRowTexCoor = rowBorder;
        minColTexCoor = colBorder;
        maxRowTexCoor = (double) nRows / nBackgroundRows - rowBorder;
        maxColTexCoor = (double) nCols / nBackgroundCols - colBorder;
        dRowTexCoor = (double) (maxRowTexCoor - minRowTexCoor) / (nRows - 1);
        dColTexCoor = (double) (maxColTexCoor - minColTexCoor) / (nCols - 1);

		setTileCoordinates(rowMin, rowMax, colMin, colMax);
    }

	private void setTileCoordinates(int rowMin, int rowMax, int colMin, int colMax)
	{
		setTileCoordinates(rowMin, rowMax, colMin, colMax, 0.0f);
	}

	private void setTileCoordinates(int rowMin, int rowMax, int colMin, int colMax, float dirCoordinate)
	{
		int dir = textureTiles.dir;
		if(dir < 0) return;
        StsRotatedGridBoundingBox boundingBox = textureTiles.boundingBox;

        float minNormRowF = (float) rowMin / (nTotalRows - 1);
        float minNormColF = (float) colMin / (nTotalCols - 1);
        float maxNormRowF = (float) rowMax / (nTotalRows - 1);
        float maxNormColF = (float) colMax / (nTotalCols - 1);

        float xTileMin, xTileMax, yTileMin, yTileMax, zTileMin, zTileMax;

        switch(dir)
        {
            case StsCursor3d.XDIR:
				xTileMin = 0.0f;
				xTileMax = 0.0f;
                yTileMin = boundingBox.getYCoorFromNormF(minNormRowF);
                yTileMax = boundingBox.getYCoorFromNormF(maxNormRowF);
                zTileMin = boundingBox.getZTCoorFromNormF(minNormColF);
                zTileMax = boundingBox.getZTCoorFromNormF(maxNormColF);
                xyzPlane[0] = new double[] {dirCoordinate, yTileMin, zTileMin};
                xyzPlane[1] = new double[] {dirCoordinate, yTileMin, zTileMax};
                xyzPlane[2] = new double[] {dirCoordinate, yTileMax, zTileMax};
                xyzPlane[3] = new double[] {dirCoordinate, yTileMax, zTileMin};
                break;
            case StsCursor3d.YDIR:
                xTileMin = boundingBox.getXCoorFromNormF(minNormRowF);
                xTileMax = boundingBox.getXCoorFromNormF(maxNormRowF);
				yTileMin = 0.0f;
				yTileMax = 0.0f;
                zTileMin = boundingBox.getZTCoorFromNormF(minNormColF);
                zTileMax = boundingBox.getZTCoorFromNormF(maxNormColF);
                xyzPlane[0] = new double[] {xTileMin, dirCoordinate, zTileMin};
                xyzPlane[1] = new double[] {xTileMin, dirCoordinate, zTileMax};
                xyzPlane[2] = new double[] {xTileMax, dirCoordinate, zTileMax};
                xyzPlane[3] = new double[] {xTileMax, dirCoordinate, zTileMin};
                break;
            case StsCursor3d.ZDIR:
                xTileMin = boundingBox.getXCoorFromNormF(minNormColF);
                xTileMax = boundingBox.getXCoorFromNormF(maxNormColF);
                yTileMin = boundingBox.getYCoorFromNormF(minNormRowF);
                yTileMax = boundingBox.getYCoorFromNormF(maxNormRowF);
				zTileMin = 0.0f;
				zTileMax = 0.0f;
                xyzPlane[0] = new double[] {xTileMin, yTileMin, dirCoordinate};
                xyzPlane[1] = new double[] {xTileMax, yTileMin, dirCoordinate};
                xyzPlane[2] = new double[] {xTileMax, yTileMax, dirCoordinate};
                xyzPlane[3] = new double[] {xTileMin, yTileMax, dirCoordinate};
                break;
			default:
				StsException.systemError(this, "setTileCoordinates", "dir must be 0, 1, or 2 ");
				xTileMin = 0.0f; xTileMax = 0.0f; yTileMin = 0.0f; yTileMax = 0.0f; zTileMin = 0.0f; zTileMax = 0.0f;
        }

        if(debug)
        {
            System.out.println("StsTextureTile.constructor() called.");
            System.out.println("    Geometry for tile in direction: " + dir);
            System.out.println("        xTileMin: " + xTileMin + " xTileMax: " + xTileMax);
            System.out.println("        yTileMin: " + yTileMin + " yTileMax: " + yTileMax);
            System.out.println("        zTileMin: " + zTileMin + " zTileMax: " + zTileMax);
            System.out.println("        maxRowTexCoor: " + maxRowTexCoor + " maxColTexCoor: " + maxColTexCoor);
        }
	}

    public StsTextureTile(int nTotalRows, int nTotalCols, int row, int col, StsTextureTiles textureTiles, int maxTextureSize, float[][] axisRanges, int nTile)
    {
        this.nTotalRows = nTotalRows;
        this.nTotalCols = nTotalCols;
        this.nTotalPoints = nTotalRows * nTotalCols;
        this.rowMin = row;
        this.colMin = col;
        this.textureTiles = textureTiles;
        this.nTile = nTile;

        if(runTimer && timer == null) timer = new StsTimer();

        rowMax = Math.min(row + maxTextureSize - 1, nTotalRows - 1);
        colMax = Math.min(col + maxTextureSize - 1, nTotalCols - 1);

        initializeCroppedRange();

        nRows = rowMax - rowMin + 1;
        nCols = colMax - colMin + 1;

        nBackgroundRows = StsMath.nextBaseTwoInt(nRows);
        nBackgroundCols = StsMath.nextBaseTwoInt(nCols);

        double rowBorder = 0;
        double colBorder = 0;
        if(cellCenteredTexture)
        {
            rowBorder = 1.0 / (2.0 * nBackgroundRows);
            colBorder = 1.0 / (2.0 * nBackgroundCols);
        }

        minRowTexCoor = rowBorder;
        minColTexCoor = colBorder;
        maxRowTexCoor = (double) nRows / nBackgroundRows - rowBorder;
        maxColTexCoor = (double) nCols / nBackgroundCols - colBorder;
        dRowTexCoor = (double) (maxRowTexCoor - minRowTexCoor) / (nRows - 1);
        dColTexCoor = (double) (maxColTexCoor - minColTexCoor) / (nCols - 1);

        int dirNo = textureTiles.dir;
        if(dirNo < 0) return;

        double minRowF = (double) rowMin / (nTotalRows - 1);
        double minColF = (double) colMin / (nTotalCols - 1);
        double maxRowF = (double) rowMax / (nTotalRows - 1);
        double maxColF = (double) colMax / (nTotalCols - 1);

        double xTileMin = 0.0f, xTileMax = 0.0f;
        double yTileMin = 0.0f, yTileMax = 0.0f;
        double zTileMin = 0.0f, zTileMax = 0.0f;

        float zMin = axisRanges[0][0];
        float zMax = axisRanges[0][1];
        float xMin = axisRanges[1][0];
        float xMax = axisRanges[1][1];

        xTileMin = xMin + minRowF * (xMax - xMin);
        xTileMax = xMin + maxRowF * (xMax - xMin);
        zTileMin = zMin + minColF * (zMax - zMin);
        zTileMax = zMin + maxColF * (zMax - zMin);
        xyzPlane[0] = new double[] {xTileMin, 0.0f, zTileMin};
        xyzPlane[1] = new double[] {xTileMin, 0.0f, zTileMax};
        xyzPlane[2] = new double[] {xTileMax, 0.0f, zTileMax};
        xyzPlane[3] = new double[] {xTileMax, 0.0f, zTileMin};

        if(debug)
        {
            System.out.println("StsTextureTile.constructor() called.");
            System.out.println("    Geometry for tile in direction: " + dirNo);
            System.out.println("        xTileMin: " + xTileMin + " xTileMax: " + xTileMax);
            System.out.println("        yTileMin: " + yTileMin + " yTileMax: " + yTileMax);
            System.out.println("        zTileMin: " + zTileMin + " zTileMax: " + zTileMax);
            System.out.println("        maxRowTexCoor: " + maxRowTexCoor + " maxColTexCoor: " + maxColTexCoor);
        }
    }

    private void initializeCroppedRange()
    {
        croppedRowMin = rowMin;
        croppedColMin = colMin;
        croppedRowMax = rowMax;
        croppedColMax = colMax;
    }

    public void setAxesFlipped(boolean axesFlipped)
    {
        this.axesFlipped = axesFlipped;
    }

    public void display(StsTextureSurfaceFace textureSurface, float[] planeData, boolean isPixelMode, GL gl, boolean reversePolarity, int n, byte nullByte)
    {
        // convert back to unsigned bytes
        if(planeData == null)
            display(textureSurface, (byte[]) null, isPixelMode, gl, n, nullByte);
        else
        {
            byte[] bData = new byte[planeData.length];
            float val;
            if(reversePolarity)
                for(int i = 0; i < planeData.length; i++)
                {
                    val = planeData[i];
                    val = val > 1.0f ? 1.0f : val;
                    val = val < -1.0f ? -1.0f : val;
                    bData[i] = (byte) ((int) (127 * (-val)) + 127); // signed to unsigned byte, reverse polarity
                }

            else
                for(int i = 0; i < planeData.length; i++)
                {
                    val = planeData[i];
                    val = val > 1.0f ? 1.0f : val;
                    val = val < -1.0f ? -1.0f : val;
                    bData[i] = (byte) ((int) (127 * val) + 127); // signed to unsigned byte
                }
            display(textureSurface, bData, isPixelMode, gl, n, nullByte);
            bData = null;
        }
    }

    public void display(StsTextureSurfaceFace textureSurface, byte[] planeData, boolean isPixelMode, GL gl, int nTile, byte nullByte)
    {
        if(textureSurface == null) return;

        if(texture == 0)
        {
            if(planeData == null) return;
            createTexture(gl, nTile);
            if(!bindTexture(gl)) return;
            createTextureBackground(isPixelMode, gl);
        }
        else if(!bindTexture(gl)) return;

        if(planeData != null) addData(gl, planeData);

		if(debug)
		{
			if(planeData != null)
            {
                int midIndex = planeData.length/2;
                printDebug("mmmm", "aaaa", nTile, " for planeData[" + midIndex + "] = " + planeData[midIndex]);
            }
			else
				printDebug("mmmm", "aaaa", nTile, " for planeData[0] = null");
		}
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        //gl.glDisable(GL.GL_LIGHTING); nog good -- lighted surfaces // jbw
        if(displayListNum != 0)
            gl.glCallList(displayListNum);
        else
            textureSurface.drawTextureTileSurface(this, gl, true, nTile);
        gl.glFlush();
    }

    private void printDebug(String method, String action, int n, String message)
    {
        String threadName = " " + Thread.currentThread().getName() + " ";
        String description = "texture " + texture + " nRows " + nRows + " nCols " + nCols + " maxTileCoordinates " + maxRowTexCoor + " " + maxColTexCoor;
        textureTileDebug(method, action, n, description + threadName + message);
    }

    private void printDebug(int n, String message)
    {
        String threadName = " " + Thread.currentThread().getName() + " ";
        String description = "texture " + texture + " nRows " + nRows + " nCols " + nCols + " maxTileCoordinates " + maxRowTexCoor + " " + maxColTexCoor;
        textureTileDebug(n, description + threadName + message);
    }

    public void display(StsTextureSurfaceFace textureSurface, ByteBuffer planeData, boolean isPixelMode, GL gl, int nTile, byte nullByte)
	{
		if(textureSurface == null)return;

        if(texture == 0)
        {
            if(planeData == null) return;
            createTexture(gl, nTile);
            if(!bindTexture(gl)) return;
            createTextureBackground(isPixelMode, gl);
        }
        else if(!bindTexture(gl)) return;

		if(planeData != null) addData(gl, planeData, nullByte, nTile);
		if(debug)
		{
            printDebug("display", "", nTile, "display data");
		}
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        //gl.glDisable(GL.GL_LIGHTING); nog good -- lighted surfaces // jbw
        if(displayListNum != 0)
            gl.glCallList(displayListNum);
        else
            textureSurface.drawTextureTileSurface(this, gl, true, nTile);
        gl.glFlush();
    }

    public void display2d(StsTextureSurfaceFace textureSurface, boolean isPixelMode, byte[] planeData, GL gl, boolean axesFlipped, int nTile, byte nullByte)
    {
        if(textureSurface == null) return;

        if(texture == 0)
        {
            createTexture(gl, nTile);
            if(!bindTexture(gl)) return;
            createTextureBackground(isPixelMode, gl);
        }
        else if(!bindTexture(gl)) return;

        if(planeData != null) addData(gl, planeData);

		if(debug)
		{
			if(planeData != null)
            {
                printDebug("mmmm", "aaaa", nTile, " for planeData[" + planeData.length/2 + "] = " + planeData[planeData.length/2]);
            }
            else
				printDebug("mmmm", "aaaa", nTile, " for planeData = null");
		}

        this.axesFlipped=axesFlipped;

        gl.glColor4f(1.0f,1.0f,1.0f,1.0f);

    //		System.out.println("TextureTile lighting: " + gl.glIsEnabled(GL.GL_LIGHTING));
    //		gl.glDisable(GL.GL_LIGHTING); // jbw

        if(displayListNum2d!=0)
            gl.glCallList(displayListNum2d);
        else
            textureSurface.drawTextureTileSurface(this, gl, false, nTile);
        gl.glFlush();
    }

    public void display2d(StsTextureSurfaceFace textureSurface, boolean isPixelMode, ByteBuffer planeData, GL gl, boolean axesFlipped, int nTile, byte nullByte)
    {
        if(textureSurface == null) return;

        if(texture == 0)
        {
            if(planeData == null) return;
            createTexture(gl, nTile);
            if(!bindTexture(gl)) return;
            createTextureBackground(isPixelMode, gl);
        }
        else if(!bindTexture(gl)) return;

        if(planeData != null) addData(gl, planeData, nullByte, nTile);

        // if(debug) printDebug("display2d", "", nullByte);

        this.axesFlipped = axesFlipped;

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        gl.glDisable(GL.GL_LIGHTING); // jbw

		if(displayListNum2d != 0)
			gl.glCallList(displayListNum2d);
		else
			textureSurface.drawTextureTileSurface(this, gl, false, nTile);
		gl.glFlush();
	}

	/** A default method which draw this texture in 3d on a rectangle. */
	public void drawQuadSurface3d(GL gl)
	{
		gl.glBegin(GL.GL_QUADS);
		gl.glTexCoord2d(minColTexCoor, minRowTexCoor);
		gl.glVertex3dv(xyzPlane[0], 0);
		gl.glTexCoord2d(maxColTexCoor, minRowTexCoor);
		gl.glVertex3dv(xyzPlane[1], 0);
		gl.glTexCoord2d(maxColTexCoor, maxRowTexCoor);
		gl.glVertex3dv(xyzPlane[2], 0);
		gl.glTexCoord2d(minColTexCoor, maxRowTexCoor);
		gl.glVertex3dv(xyzPlane[3], 0);
		gl.glEnd();
	}

    /** A default method which draw this texture in 2d on a rectangle. */
    public void drawQuadSurface2d(GL gl)
    {
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glDisable(GL.GL_LIGHTING); // jbw
 		int dir = textureTiles.dir;
        if(dir == StsCursor3d.XDIR)
        {
            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2d(minColTexCoor, minRowTexCoor);
            gl.glVertex2d(xyzPlane[0][1], xyzPlane[0][2]);
            gl.glTexCoord2d(maxColTexCoor, minRowTexCoor);
            gl.glVertex2d(xyzPlane[1][1], xyzPlane[1][2]);
            gl.glTexCoord2d(maxColTexCoor, maxRowTexCoor);
            gl.glVertex2d(xyzPlane[2][1], xyzPlane[2][2]);
            gl.glTexCoord2d(minColTexCoor, maxRowTexCoor);
            gl.glVertex2d(xyzPlane[3][1], xyzPlane[3][2]);
            gl.glEnd();
        }
        else if(dir == StsCursor3d.YDIR)
        {
            gl.glBegin(GL.GL_QUADS);
            gl.glTexCoord2d(minColTexCoor, minRowTexCoor);
            gl.glVertex2d(xyzPlane[0][0], xyzPlane[0][2]);
            gl.glTexCoord2d(maxColTexCoor, minRowTexCoor);
            gl.glVertex2d(xyzPlane[1][0], xyzPlane[1][2]);
            gl.glTexCoord2d(maxColTexCoor, maxRowTexCoor);
            gl.glVertex2d(xyzPlane[2][0], xyzPlane[2][2]);
            gl.glTexCoord2d(minColTexCoor, maxRowTexCoor);
            gl.glVertex2d(xyzPlane[3][0], xyzPlane[3][2]);
            gl.glEnd();
        }
        else if(dir == StsCursor3d.ZDIR)
        {
            if(!axesFlipped)
            {
                gl.glBegin(GL.GL_QUADS);
                gl.glTexCoord2d(minColTexCoor, minRowTexCoor);
                gl.glVertex2dv(xyzPlane[0], 0);
                gl.glTexCoord2d(maxColTexCoor, minRowTexCoor);
                gl.glVertex2dv(xyzPlane[1], 0);
                gl.glTexCoord2d(maxColTexCoor, maxRowTexCoor);
                gl.glVertex2dv(xyzPlane[2], 0);
                gl.glTexCoord2d(minColTexCoor, maxRowTexCoor);
                gl.glVertex2dv(xyzPlane[3], 0);
                gl.glEnd();
            }
            else
            {
                gl.glBegin(GL.GL_QUADS);
                gl.glTexCoord2d(minColTexCoor, minRowTexCoor);
                gl.glVertex2d(xyzPlane[0][1], xyzPlane[0][0]);
                gl.glTexCoord2d(maxColTexCoor, minRowTexCoor);
                gl.glVertex2d(xyzPlane[1][1], xyzPlane[1][0]);
                gl.glTexCoord2d(maxColTexCoor, maxRowTexCoor);
                gl.glVertex2d(xyzPlane[2][1], xyzPlane[2][0]);
                gl.glTexCoord2d(minColTexCoor, maxRowTexCoor);
                gl.glVertex2d(xyzPlane[3][1], xyzPlane[3][0]);
                gl.glEnd();
            }
        }
    }

    /** A default method which draw this texture in 2d on a series of rectangular coordinates. */
    public void drawQuadStripCursorSurface2d(GL gl, double[] zCoordinates)
    {
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glDisable(GL.GL_LIGHTING); // jbw
        int dir = textureTiles.dir;
        if(dir == StsCursor3d.XDIR)
            drawQuadStripSurface2d(gl, xyzPlane[0][1], xyzPlane[3][1], zCoordinates);
        else if(dir == StsCursor3d.YDIR)
            drawQuadStripSurface2d(gl, xyzPlane[0][0], xyzPlane[3][0], zCoordinates);
    }

    public void drawQuadStripSurface2d(GL gl, double xMin, double xMax, double[] yCoordinates)
    {
        if(debug) StsException.systemDebug(this, "drawQuadStripSurface2d(gl, xMin, xMax, yCoordinates)", "draw called.");
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glBegin(GL.GL_QUAD_STRIP);
        double colTexCoor = minColTexCoor;
        for(int col = croppedColMin; col <= croppedColMax; col++, colTexCoor += dColTexCoor)
        {
            gl.glTexCoord2d(colTexCoor, minRowTexCoor);
            gl.glVertex2d(xMin, yCoordinates[col]);
            gl.glTexCoord2d(colTexCoor, maxRowTexCoor);
            gl.glVertex2d(xMax, yCoordinates[col]);
        }
        gl.glEnd();
    }

    public void drawQuadStripSurface2d(GL gl, double xMin, double xInc, double[][] yCoordinates)
    {
        if(yCoordinates == null) return;
        if(debug) StsException.systemDebug(this, "drawQuadStripSurface2d(gl, xCoordinates, yCoordinates)", "draw called.");
         gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glDisable(GL.GL_LIGHTING);
        double rowTexCoor = minRowTexCoor;
        double x2 = xMin + rowMin * xInc;
        for(int row = croppedRowMin; row < croppedRowMax; row++, rowTexCoor += dRowTexCoor)
        {
            gl.glBegin(GL.GL_QUAD_STRIP);
            double colTexCoor = minColTexCoor;
            double x1 = x2;
            x2 += xInc;
            for(int col = croppedColMin; col <= croppedColMax; col++, colTexCoor += dColTexCoor)
            {
                gl.glTexCoord2d(colTexCoor, rowTexCoor);
                gl.glVertex2d(x1, yCoordinates[row][col]);
                gl.glTexCoord2d(colTexCoor, rowTexCoor + dRowTexCoor);
                gl.glVertex2d(x2, yCoordinates[row][col]);
            }
            gl.glEnd();
        }
    }

    public void drawQuadStripSurface2d(GL gl, double[] xCoordinates, double[] yCoordinates)
    {
        if(yCoordinates == null) return;
        if(debug) System.out.println("drawQuadStripSurface2d(gl, xCoordinates, yCoordinates) called.");
         gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glDisable(GL.GL_LIGHTING);
        double rowTexCoor = minRowTexCoor;
        double x2 = xCoordinates[croppedRowMin];
        for(int row = croppedRowMin; row < croppedRowMax; row++, rowTexCoor += dRowTexCoor)
        {
            gl.glBegin(GL.GL_QUAD_STRIP);
            double colTexCoor = minColTexCoor;
            double x1 = x2;
            x2 = xCoordinates[row + 1];
            for(int col = croppedColMin; col <= croppedColMax; col++, colTexCoor += dColTexCoor)
            {
                double y = yCoordinates[col];
                gl.glTexCoord2d(colTexCoor, rowTexCoor);
                gl.glVertex2d(x1, y);
                gl.glTexCoord2d(colTexCoor, rowTexCoor + dRowTexCoor);
                gl.glVertex2d(x2, y);
            }
            gl.glEnd();
        }
    }

    private void createTexture(GL gl, int nTile)
	{
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
        texture = textures[0];
        if(StsTextureTiles.debug) textureTileDebug("createTexture", "create", nTile, texture);
	}

    public void textureTileDebug(String method, String action, int nTile, int texture)
    {
        StsException.systemDebug(this, method, action + " textureTiles[" + nTile + "] " + texture + " for: " + textureTiles.textureSurface.getName());
    }

    public void textureTileDebug(String method, String action, int nTile, String message)
    {
        StsException.systemDebug(this, method, action + " textureTiles[" + nTile + "] " + texture + " for: " + textureTiles.textureSurface.getName() + " " + message);
    }

    public void textureTileDebug(int nTile, String message)
    {
        StsException.systemDebug(this, "", " textureTiles[" + nTile + "] " + texture + " for: " + textureTiles.textureSurface.getName() + " " + message);
    }

    public void textureTileDebug(String method)
    {
        StsException.systemDebug(this, method, " textureTiles[" + nTile + "] " + texture + " for: " + textureTiles.textureSurface.getName());
    }

    private boolean bindTexture(GL gl)
    {

        if(texture == 0)
        {
            StsException.systemError("Attempt to bind a 0 texture");
            return false;
        }

        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        return true;
    }

    private void createTextureBackground(boolean isPixelMode, GL gl)
    {
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        // ByteBuffer background = BufferUtil.newByteBuffer(nBackgroundRows*nBackgroundCols);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        if(!isPixelMode)
        {
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        }
        else
        {
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        }
        /*
            int errorCode = glu.gluBuild2DMipmaps( GL.GL_TEXTURE_2D, GL.GL_RGBA8, nBackgroundCols, nBackgroundRows, GL.GL_COLOR_INDEX, GL.GL_UNSIGNED_BYTE, background);
            if(errorCode != 0)
            {
             StsException.systemError("StsTextureTile.createTextureBackground() failed. GLU error code: " + glu.errorString(errorCode));
            }
           */
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1); // jbw essential to set june 27
        if((textureTiles.shader != StsJOGLShader.NONE) && StsJOGLShader.canDoARBShader(gl))
        {
            gl.glPixelTransferf(GL.GL_MAP_COLOR, 0);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_INTENSITY8 /*GL.GL_COMPRESSED_INTENSITY*/, nBackgroundCols, nBackgroundRows, 0, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, null); //background
            //System.out.println("bg image2D "+gl.glGetError());
            //StsJOGLShader.reloadTLUT(gl);
            //StsJOGLShader.enableARBShader(gl, textureTiles.shader);
        }
        else
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, nBackgroundCols, nBackgroundRows, 0, GL.GL_COLOR_INDEX, GL.GL_UNSIGNED_BYTE, null); //background

        //background = null;
    }

    protected void addData(GL gl, byte[] planeData)
    {
        int nData, nTile;
        if(planeData != null)
            try
            {
                if(planeData != null)
                {
                    if(runTimer) timer.start();

                    if(nTotalCols == nCols)
                    {
                        nData = rowMin * nTotalCols + colMin;
                        nTile = 0;
                        tileData = BufferUtil.newByteBuffer(nRows * nCols);
                        tileData.put(planeData, nData, nCols * nRows);
                        //                  System.arrayCastCopy(planeData, nData, tileData, nTile, nTextureCols*nTextureRows);
                    }
                    else
                    {
                        nData = rowMin * nTotalCols + colMin;
                        nTile = 0;
                        tileData = BufferUtil.newByteBuffer(nRows * nCols);
                        int nDataRows = planeData.length / nTotalCols;
                        int nReadRows = Math.min(nDataRows, nRows);
                        for(int row = 0; row < nReadRows; row++)
                        {
                            tileData.put(planeData, nData, nCols);
                            nData += nTotalCols;
                            nTile += nCols;
                        }
                    }
                    if(runTimer) timer.stopPrint("        add data for tile " + texture);
                }
                tileData.rewind();
                if(runTimer) timer.start();
                gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1); // jbw essential to set june 27
                if((textureTiles.shader != StsJOGLShader.NONE) && StsJOGLShader.canDoARBShader(gl))
                {
                    gl.glPixelTransferf(GL.GL_MAP_COLOR, 0);
                    gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, nCols, nRows, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, tileData);
                }
                else
                    gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, nCols, nRows, GL.GL_COLOR_INDEX, GL.GL_UNSIGNED_BYTE, tileData);
                if(!debug) tileData = null;
                if(runTimer) timer.stopPrint("        add subImage to texture");
            }
            catch(Exception e)
            {
                StsException.outputException("StsTextureTile.addData() failed.", e, StsException.WARNING);
            }
    }

    protected void addData(GL gl, ByteBuffer planeData, byte nullByte, int nTile)
    {
        int nData;
        if(planeData != null)
            try
            {
                if(planeData != null)
                {
                    if(runTimer) timer.start();

                    if(nTotalCols == nCols)
                    {
                        nData = rowMin * nTotalCols + colMin;
                        tileData = planeData;
                        tileData.position(nData);
                    }
                    else
                    {
                        nData = rowMin * nTotalCols + colMin;
                        tileData = ByteBuffer.allocateDirect(nRows * nCols);
                        int nReadRows = Math.min(nTotalRows, nRows);
                        byte[] rowData = new byte[nCols];
                        for(int row = 0; row < nReadRows; row++)
                        {
                            planeData.position(nData);
                            planeData.get(rowData);
                            tileData.put(rowData);
                            nData += nTotalCols;
                        }
                        tileData.rewind();
                    }
                    if(debug)
                    {
                        byte firstByte = tileData.get();
                        tileData.rewind();
                        printDebug("addData", "adding new data", nTile, "tile first byte: " + firstByte);
                    }
                    if(runTimer) timer.stopPrint("        add data for tile " + texture);
                }
                if(runTimer) timer.start();
                gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1); // jbw essential to set june 27
                if((textureTiles.shader == StsJOGLShader.ARB_TLUT_NO_SPECULAR_LIGHTS) && StsJOGLShader.canUseShader)
                {
                    gl.glPixelTransferf(GL.GL_MAP_COLOR, 0);
                    gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, nCols, nRows, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, tileData);
                }
                else
                    gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, nCols, nRows, GL.GL_COLOR_INDEX, GL.GL_UNSIGNED_BYTE, tileData);

                if(!debug) tileData = null;
                if(runTimer) timer.stopPrint("        add subImage to texture");
            }
            catch(Exception e)
            {
                StsException.outputException("StsTextureTile.addData() failed.", e, StsException.WARNING);
            }
    }

    public boolean deleteTexture(GL gl, int n)
    {
        if(texture == 0) return false;
        if(StsTextureTiles.debug) textureTileDebug("deleteTexture", "deleteTileTexture", n, texture);
        gl.glDeleteTextures(1, new int[] {texture}, 0);
		texture = 0;
        return true;
    }

    public boolean deleteDisplayList(GL gl, int n)
    {
        boolean deleted = false;
        if(displayListNum != 0)
        {
            if(StsTextureTiles.debug) textureTileDebug("deleteDisplayList", "delete tile 3d displayList", n, displayListNum);
            gl.glDeleteLists(displayListNum, 1);
            displayListNum = 0;
            deleted = true;
        }
        if(displayListNum2d != 0)
        {
            if(StsTextureTiles.debug) textureTileDebug("deleteDisplayList", "delete tile 2d displayList", n, displayListNum2d);
            gl.glDeleteLists(displayListNum2d, 1);
            displayListNum2d = 0;
            deleted = true;
        }
        return deleted;
    }

    public void setDirCoordinate(float dirCoordinate)
    {
        int dir = textureTiles.dir;
        for(int n = 0; n < 4; n++)
            xyzPlane[n][dir] = dirCoordinate;
		if(debug)
		{
			switch(dir)
        	{
            	case StsCursor3d.XDIR:
					StsException.systemDebug(this, "setDirCoordinate", "Dir: " + "new X: " + dirCoordinate + " Y range: " + planeYRange() + " Z range: " + planeZRange());
               		break;
				case StsCursor3d.YDIR:
					StsException.systemDebug(this, "setDirCoordinate", "Dir: " + "new Y: " + dirCoordinate + " X range: " + planeXRange() + " Z range: " + planeZRange());
               		break;
                case StsCursor3d.ZDIR:
					StsException.systemDebug(this, "setDirCoordinate", "Dir: " + "new Z: " + dirCoordinate + "X range: " + planeXRange() + "Y range: " + planeYRange());
               		break;
			}
		}
    }

	private String planeXRange() { return xyzPlane[0][0] + "-" + xyzPlane[2][0]; }
	private String planeYRange() { return xyzPlane[0][1] + "-" + xyzPlane[2][1]; }
	private String planeZRange() { return xyzPlane[0][2] + "-" + xyzPlane[1][2]; }

    public void adjust(int cropRowMin, int cropRowMax, int cropColMin, int cropColMax)
    {
        double rowBorder = 0;
        double colBorder = 0;
        if(!cellCenteredTexture)
        {
            rowBorder = 1.0 / (2.0 * nBackgroundRows);
            colBorder = 1.0 / (2.0 * nBackgroundCols);
        }

        croppedRowMin = StsMath.minMax(cropRowMin, rowMin, rowMax);
        croppedRowMax = StsMath.minMax(cropRowMax, rowMin, rowMax);
        croppedColMin = StsMath.minMax(cropColMin, colMin, colMax);
        croppedColMax = StsMath.minMax(cropColMax, colMin, colMax);

        minRowTexCoor = ((double) croppedRowMin - (double) rowMin) / nBackgroundRows + rowBorder;
        minColTexCoor = ((double) croppedColMin - (double) colMin) / nBackgroundCols + colBorder;
        maxRowTexCoor = ((double) croppedRowMax - (double) rowMin) / nBackgroundRows + rowBorder;
        maxColTexCoor = ((double) croppedColMax - (double) colMin) / nBackgroundCols + colBorder;

		float dirCoordinate = textureTiles.dirCoordinate;
		setTileCoordinates(croppedRowMin, croppedRowMax, croppedColMin, croppedColMax, dirCoordinate);
    }

	public void constructSurface(StsTextureSurfaceFace surface, GL gl, boolean useDisplayLists, boolean is3d, int n)
	{
		deleteDisplayList(gl, n);
		if(!useDisplayLists)return;
		if(is3d)
		{
            displayListNum = gl.glGenLists(1);
			gl.glNewList(displayListNum, GL.GL_COMPILE);
            if(StsTextureTiles.debug) textureTileDebug("constructSurface", "construct 3d displayList", n, displayListNum);
 		}
		else
		{
			displayListNum2d = gl.glGenLists(1);
			gl.glNewList(displayListNum2d, GL.GL_COMPILE);
            if(StsTextureTiles.debug) textureTileDebug("constructSurface", "construct 2d displayList", n, displayListNum2d);
		}
		surface.drawTextureTileSurface(this, gl, is3d, n);
		gl.glEndList();
	}
	/*
	 public void draw(GL gl, boolean useDisplayLists)
	 {
	  if(displayListNum != 0) gl.deleteLists(displayListNum, 1);
	  displayListNum = 0;
	  if(!useDisplayLists) return;
	  displayListNum = gl.genLists(1);
	  gl.glNewList(displayListNum, GL.GL_COMPILE);
	  surface.drawTextureTileSurface(this, gl);
	  gl.glEndList();
	 }
	 */
}
