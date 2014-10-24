//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Types;

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/** A boundingSubBox has a larger boundingBox and a congruent subBox defined within it. */
public class StsRotatedGridBoundingSubBox extends StsRotatedGridBoundingBox implements StsXYGridable, Cloneable
{
    public int rowMin = largeInt; // row at yMin
    public int rowMax = -largeInt; // row at yMax
    public int colMin = largeInt; // col at xMin
    public int colMax = -largeInt; // col at xMax
    public int sliceMin = largeInt; // col at xMin
    public int sliceMax = -largeInt; // col at xMax

    public boolean rowRangeOK = false;
    public boolean colRangeOK = false;

    static final int ROW = StsParameters.ROW;
    static final int COL = StsParameters.COL;
    static final int ROWCOL = StsParameters.ROWCOL;

    static final int PLUS = StsParameters.PLUS;
    static final int MINUS = StsParameters.MINUS;

    static public final int XDIR = StsCursor3d.XDIR;
    static public final int YDIR = StsCursor3d.YDIR;
    static public final int ZDIR = StsCursor3d.ZDIR;

    static final int largeInt = StsParameters.largeInt;

    public StsRotatedGridBoundingSubBox()
    {
    }

    public StsRotatedGridBoundingSubBox(boolean persistent)
    {
        super(persistent);
    }

    public StsRotatedGridBoundingSubBox(StsRotatedGridBoundingBox boundingBox)
    {
		super();
        initializeBoundingBoxAndSubBox(boundingBox);
    }

    public StsRotatedGridBoundingSubBox(StsRotatedGridBoundingSubBox box)
    {
		super();
        initialize(box);
    }

    public StsRotatedGridBoundingSubBox(int rowMin, int rowMax, int colMin, int colMax, boolean persistent)
    {
        super(persistent);
        this.rowMin = rowMin;
        this.rowMax = rowMax;
        this.colMin = colMin;
        this.colMax = colMax;
        rowRangeOK = true;
        colRangeOK = true;
    }

    public StsRotatedGridBoundingSubBox(int rowMin, int rowMax, int colMin, int colMax)
    {
        this.rowMin = rowMin;
        this.rowMax = rowMax;
        this.colMin = colMin;
        this.colMax = colMax;
        rowRangeOK = true;
        colRangeOK = true;
    }

	public StsRotatedGridBoundingSubBox(boolean persistent, String name)
    {
        super(persistent, name);
    }

    /** Initialize boundingBox to passed in boundingBox and initialize subBox to the same box.
     *  If we call initialize(rotatedBoundingBox), it will be executed by the superclass boundingBox and
     *  this subBox will be left uninitialized.
     * @param rotatedBoundingBox  the larger boundingBox containing this subBox.
     */
    public void initializeBoundingBoxAndSubBox(StsRotatedGridBoundingBox rotatedBoundingBox)
    {
		super.initialize(rotatedBoundingBox);
        rowMin = 0;
        rowMax = nRows - 1;
        colMin = 0;
        colMax = nCols - 1;
        sliceMin = 0;
        sliceMax = rotatedBoundingBox.nSlices - 1;
        rowRangeOK = true;
        colRangeOK = true;
    }

    /** initialize the larger boundingBox only; leave subBox uninitialized. */
    public void initialize(StsRotatedGridBoundingBox gridBoundingBox)
    {
        super.initialize(gridBoundingBox);
    }

    /** initialize this subBox to an existing subBox. */
    public void initialize(StsRotatedGridBoundingSubBox gridBoundingBox)
    {
        super.initialize(gridBoundingBox);
        rowMin = gridBoundingBox.rowMin;
        rowMax = gridBoundingBox.rowMax;
        colMin = gridBoundingBox.colMin;
        colMax = gridBoundingBox.colMax;
        sliceMin = gridBoundingBox.sliceMin;
        sliceMax = gridBoundingBox.sliceMax;
        rowRangeOK = true;
        colRangeOK = true;
    }

	public void reinitializeBoundingBox()
    {
        super.reinitializeBoundingBox();
		rowMin = largeInt;
		rowMax = -largeInt;
		colMin = largeInt;
		colMax = -largeInt;
		sliceMin = largeInt;
		sliceMax = -largeInt;
		rowRangeOK = false;
   		colRangeOK = false;
	}

    /** initialize this boundingBox with cell limits which are one less than grid limits in each direction. */
    public void initializeCellGridVolume(StsRotatedGridBoundingSubBox gridBoundingBox)
    {
        initialize(gridBoundingBox);
        rowMax--;
        colMax--;
        sliceMax--;
    }

    /** classInitialize this instance of StsRotatedGridBoundingSubBox so that it is the size of the subVolumeBoundingBox.  Its row/col/slice
     *  range is determiend from the loopBoundingBox.
     */

    public void initialize(StsRotatedGridBoundingBox gridBoundingBox, StsRotatedGridBoundingBox subVolumeBoundingBox)
    {
		super.initialize(subVolumeBoundingBox);
        setXYZIncs(gridBoundingBox);
        rowMin = gridBoundingBox.getNearestBoundedRowCoor(yMin);
        rowMax = gridBoundingBox.getNearestBoundedRowCoor(yMax);
        colMin = gridBoundingBox.getNearestBoundedColCoor(xMin);
        colMax = gridBoundingBox.getNearestBoundedColCoor(xMax);
        sliceMin = gridBoundingBox.getNearestBoundedSliceCoor(getZMin());
        sliceMax = gridBoundingBox.getNearestBoundedSliceCoor(zMax);

        xOrigin = subVolumeBoundingBox.xOrigin;
        yOrigin = subVolumeBoundingBox.yOrigin;

        originSet = true;
    }

    public StsRotatedGridBoundingSubBox getGridBoundingBox()
    {
        return getClone();
    }

    public StsRotatedGridBoundingSubBox getClone()
    {
        try
        {
            return (StsRotatedGridBoundingSubBox)this.clone();
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "getClone", e);
            return null;
        }
    }

    public int getNSubRows()
    {
        if (rowRangeOK)
        {
            return rowMax - rowMin + 1;
        }
        else
        {
            return 0;
        }
    }

    public int getNSubCols()
    {
        if (colRangeOK)
        {
            return colMax - colMin + 1;
        }
        else
        {
            return 0;
        }
    }

    public int getNSlices()
    {
        return sliceMax - sliceMin + 1;
    }

    public int getNCellRows()
    {
        return getNSubRows() - 1;
    }

    public int getNCellCols()
    {
        return getNSubCols() - 1;
    }

    public int getNCellSlices()
    {
        return getNSlices() - 1;
    }

    public int getNLayers()
    {
        return getNCellSlices();
    }

    public boolean isInsideRowCol(float rowF, float colF)
    {
        return rowF >= rowMin && rowF <= rowMax && colF >= colMin && colF <= colMax;
    }

    public boolean isInsideRowCol(int row, int col)
    {
        return row >= rowMin && row <= rowMax && col >= colMin && col <= colMax;
    }

    public boolean isInsideCellRowCol(int row, int col)
    {
        return row >= rowMin && row < rowMax && col >= colMin && col < colMax;
    }

    public boolean isInsideCellRowColF(float row, float col)
    {
        return row >= rowMin && row < rowMax && col >= colMin && col < colMax;
    }

    public boolean isInsideBlockCellRowColF(float blockRow, float blockCol)
    {
        return blockRow >= 0 && blockRow < getNSubRows()-1 && blockCol >= 0 && blockCol < getNSubCols()-1;
    }

    public boolean isInsideBlockRowCol(int blockRow, int blockCol)
    {
        return blockRow >= 0 && blockRow < getNSubRows() && blockCol >= 0 && blockCol < getNSubCols();
    }

    public boolean isInsideBlockRowColF(float blockRow, float blockCol)
    {
        return blockRow >= 0 && blockRow < getNSubRows() && blockCol >= 0 && blockCol < getNSubCols();
    }

    public boolean isInsideBlockCellRowCol(int blockRow, int blockCol)
    {
        return blockRow >= 0 && blockRow < getNSubRows()-1 && blockCol >= 0 && blockCol < getNSubCols()-1;
    }

    public void setGridMinMax(StsRotatedGridBoundingBox boundingBox)
    {

        rowMin = boundingBox.getNearestBoundedRowCoor(boundingBox.yMin);
        rowMax = boundingBox.getNearestBoundedRowCoor(boundingBox.yMax);
        colMin = boundingBox.getNearestBoundedRowCoor(boundingBox.xMin);
        colMax = boundingBox.getNearestBoundedRowCoor(boundingBox.xMax);
        rowMin = boundingBox.getNearestBoundedRowCoor(boundingBox.yMin);
        rowMax = boundingBox.getNearestBoundedRowCoor(boundingBox.yMax);
		if(boundingBox.initializedZ())
		{
			sliceMin = boundingBox.getNearestBoundedZSliceCoor(boundingBox.getZMin());
			sliceMax = boundingBox.getNearestBoundedZSliceCoor(boundingBox.getZMax());
		}
		if(boundingBox.initializedT())
		{
			sliceMin = boundingBox.getNearestBoundedTSliceCoor(boundingBox.getTMin());
			sliceMax = boundingBox.getNearestBoundedTSliceCoor(boundingBox.getTMax());
		}
    }

    public int getCursorRowMin(int dir)
    {
        if (dir == XDIR)
        {
            return rowMin;
        }
        else if (dir == YDIR)
        {
            return colMin;
        }
        else if (dir == ZDIR)
        {
            return rowMin;
        }
        else
        {
            return 0;
        }
    }

    public int getCursorColMin(int dir)
    {
        if (dir == XDIR)
        {
            return sliceMin;
        }
        else if (dir == YDIR)
        {
            return sliceMin;
        }
        else if (dir == ZDIR)
        {
            return colMin;
        }
        else
        {
            return 0;
        }
    }

    public int getCursorRowMax(int dir)
    {
        if (dir == XDIR)
        {
            return rowMax;
        }
        else if (dir == YDIR)
        {
            return colMax;
        }
        else if (dir == ZDIR)
        {
            return rowMax;
        }
        else
        {
            return 0;
        }
    }

    public int getCursorColMax(int dir)
    {
        if (dir == XDIR)
        {
            return sliceMax;
        }
        else if (dir == YDIR)
        {
            return sliceMax;
        }
        else if (dir == ZDIR)
        {
            return colMax;
        }
        else
        {
            return 0;
        }
    }

	public float getCursorRowMinCoor(int dir)
	{
		if (dir == XDIR) return getRowMinCoor();
		else if (dir == YDIR) return getColMinCoor();
		else if (dir == ZDIR) return getRowMinCoor();
		else return 0;
	}

	public float getCursorRowMaxCoor(int dir)
	{
		if (dir == XDIR) return getRowMaxCoor();
		else if (dir == YDIR) return getColMaxCoor();
		else if (dir == ZDIR) return getRowMaxCoor();
		else return 0;
	}

	public float getCursorColMinCoor(int dir)
	{
		if (dir == XDIR) return getSliceMinCoor();
		else if (dir == YDIR) return getSliceMinCoor();
		else if (dir == ZDIR) return getColMinCoor();
		else return 0;
	}

	public float getCursorColMaxCoor(int dir)
	{
		if (dir == XDIR) return getSliceMaxCoor();
		else if (dir == YDIR) return getSliceMaxCoor();
		else if (dir == ZDIR) return getColMaxCoor();
		else return 0;
	}

    public int getCursorIndexFromRowColSlice(int dir, int row, int col, int slice)
    {
        if (dir == XDIR)
            return row*nSlices + slice;
        else if (dir == YDIR)
            return col*nSlices + slice;
        else if (dir == ZDIR)
            return row*nCols + col;
        else
            return 0;
    }
	public float getRowMinCoor()
	{
		return getYCoor(rowMin);
	}

	public float getRowMaxCoor()
	{
		return getYCoor(rowMax);
	}

	public float getColMinCoor()
	{
		return getXCoor(colMin);
	}

	public float getColMaxCoor()
	{
		return getXCoor(colMax);
	}

	public float getSliceMinCoor()
	{
		return getZTCoor(sliceMin);
	}

	public float getSliceMaxCoor()
	{
		return getZTCoor(sliceMax);
	}

    public void computeRowColRanges(StsXYGridable grid)
    {
        float[] lowerLeftCorner = new float[] {xMin, yMin};
        float[] upperRightCorner = new float[] {xMax, yMax};
        rowMin = StsMath.floor(grid.getRowCoor(lowerLeftCorner));
        rowMax = StsMath.ceiling(grid.getRowCoor(upperRightCorner));
        colMin = StsMath.floor(grid.getColCoor(lowerLeftCorner));
        colMax = StsMath.ceiling(grid.getColCoor(upperRightCorner));
        xInc = grid.getXInc();
        yInc = grid.getYInc();
        rowRangeOK = true;
        colRangeOK = true;
    }

    public void computeSliceRange()
    {
        sliceMin = 0;
        sliceMax = Math.round((zMax - getZMin())/zInc);
    }

    public int getPosition(int row, int col, int slice)
    {
        int nRows = rowMax - rowMin + 1;
        int nCols = colMax - colMin + 1;
        return ( (slice - sliceMin) * nRows + (row - rowMin)) * nCols + (col - colMin);
    }

    public void addPoint(int row, int col)
    {
        rowMin = Math.min(rowMin, row);
        rowMax = Math.max(rowMax, row);
        colMin = Math.min(colMin, col);
        colMax = Math.max(colMax, col);
        rowRangeOK = true;
        colRangeOK = true;
    }

    public void addPoint(float rowF, float colF)
    {
        rowMin = Math.min(rowMin, StsMath.floor(rowF));
        rowMax = Math.max(rowMax, StsMath.ceiling(rowF));
        colMin = Math.min(colMin, StsMath.floor(colF));
        colMax = Math.max(colMax, StsMath.ceiling(colF));
        rowRangeOK = true;
        colRangeOK = true;
    }

    public void addPoint(StsGridCrossingPoint gridCrossingPoint)
    {
        if(gridCrossingPoint.isRow())
        {
            int row = Math.round(gridCrossingPoint.iF);
            rowMin = Math.min(rowMin, row);
            rowMax = Math.max(rowMax, row);
            rowRangeOK = true;
        }
        if(gridCrossingPoint.isCol())
        {
            int col = Math.round(gridCrossingPoint.jF);
            colMin = Math.min(colMin, col);
            colMax = Math.max(colMax, col);
            colRangeOK = true;
        }
    }

    public void addPoint(StsRowCol rowCol)
    {
        int rowOrCol = rowCol.getRowOrCol();
        if (rowOrCol == ROW || rowOrCol == ROWCOL)
        {
            int row = rowCol.getRow();
            rowMin = Math.min(rowMin, row);
            rowMax = Math.max(rowMax, row);
            rowRangeOK = true;
        }
        if (rowOrCol == COL || rowOrCol == ROWCOL)
        {
            int col = rowCol.getCol();
            colMin = Math.min(colMin, col);
            colMax = Math.max(colMax, col);
            colRangeOK = true;
        }
    }

    public void addBoundingBox(StsRotatedGridBoundingSubBox box)
    {
        addUnrotatedBoundingBox(box);
        rowMin = Math.min(rowMin, box.rowMin);
        rowMax = Math.max(rowMax, box.rowMax);
        colMin = Math.min(colMin, box.colMin);
        colMax = Math.max(colMax, box.colMax);
        sliceMin = Math.min(sliceMin, box.sliceMin);
        sliceMax = Math.max(sliceMax, box.sliceMax);
        setXYZIncs(box);
        rowRangeOK = true;
        colRangeOK = true;
    }

    public boolean addRotatedBoundingBox(StsRotatedGridBoundingBox box)
    {
        boolean changed = addUnrotatedBoundingBox(box);
        rowMin = 0;
        rowMax = Math.max(rowMax, box.nRows-1);
        colMin = 0;
        colMax = Math.max(colMax, box.nCols-1);
        sliceMin = 0;
        sliceMax = Math.max(sliceMax, box.nSlices-1);
        setXYZIncs(box);
        rowRangeOK = true;
        colRangeOK = true;
		return changed;
    }

    private void setXYZIncs(StsRotatedGridBoundingSubBox box)
    {
        if (xInc == 0.0f)
            xInc = box.xInc;
        else if (box.xInc != 0.0f && Math.abs(box.xInc) < Math.abs(xInc))
            xInc = box.xInc;

        if (yInc == 0.0f)
            yInc = box.yInc;
        else if (box.yInc != 0.0f && Math.abs(box.yInc) < Math.abs(yInc))
            yInc = box.yInc;
        if (zInc == 0.0f)
            zInc = box.zInc;
        else if (box.zInc != 0.0f && Math.abs(box.zInc) < Math.abs(zInc))
            zInc = box.zInc;
    }

    private void setXYZIncs(StsRotatedGridBoundingBox box)
    {
        if (xInc == 0.0f)
            xInc = box.xInc;
        else if (box.xInc != 0.0f && Math.abs(box.xInc) < Math.abs(xInc))
            xInc = box.xInc;

        if (yInc == 0.0f)
            yInc = box.yInc;
        else if (box.yInc != 0.0f && Math.abs(box.yInc) < Math.abs(yInc))
            yInc = box.yInc;
        if (zInc == 0.0f)
            zInc = box.zInc;
        else if (box.zInc != 0.0f && Math.abs(box.zInc) < Math.abs(zInc))
            zInc = box.zInc;
    }

    public void intersectBoundingBox(StsRotatedGridBoundingSubBox box)
    {
        super.intersectBoundingBox(box);
        rowMin = Math.max(rowMin, box.rowMin);
        rowMax = Math.min(rowMax, box.rowMax);
        colMin = Math.max(colMin, box.colMin);
        colMax = Math.min(colMax, box.colMax);
        sliceMin = Math.max(sliceMin, box.sliceMin);
        sliceMax = Math.min(sliceMax, box.sliceMax);

        rowRangeOK = true;
        colRangeOK = true;
    }

    public void addBorder(int border, StsXYGridable limitGrid)
    {
        rowMin = Math.max(limitGrid.getRowMin(), rowMin - border);
        rowMax = Math.min(limitGrid.getRowMax(), rowMax + border);
        colMin = Math.max(limitGrid.getColMin(), colMin - border);
        colMax = Math.min(limitGrid.getColMax(), colMax + border);
        rowRangeOK = true;
        colRangeOK = true;
    }

    public float getGridZMin(StsXYSurfaceGridable grid)
    {
        if (!initializedXY())
        {
            return StsParameters.nullValue;
        }

        float zMin = nullValue;
        int rowStart = Math.max(rowMin, 0);
        int rowEnd = Math.min(rowMax, grid.getNRows());
        int colStart = Math.max(colMin, 0);
        int colEnd = Math.min(colMax, grid.getNCols());

        for (int i = rowStart; i <= rowEnd; i++)
        {
            for (int j = colStart; j <= colEnd; j++)
            {
                float[] xyz = grid.getXYZorT(i, j);
                if (xyz[2] != nullValue)
                {
                    zMin = Math.min(zMin, xyz[2]);
                }
            }
        }
        return zMin;
    }

    public void integerAdjustRange(StsRotatedGridBoundingBox gridBoundingBox)
    {
        zMin = StsMath.floor(zMin);
        zMax = StsMath.ceiling(zMax);
        xMin = StsMath.intervalRoundDown(xMin, gridBoundingBox.xMin,  gridBoundingBox.xInc);
        xMax = StsMath.intervalRoundUp(xMax, gridBoundingBox.xMin,  gridBoundingBox.xInc);
        yMin = StsMath.intervalRoundDown(yMin, gridBoundingBox.yMin,  gridBoundingBox.yInc);
        yMax = StsMath.intervalRoundUp(yMax, gridBoundingBox.yMin,  gridBoundingBox.yInc);
    }

    public int getNPoints()
    {
        return getNSubRows()* getNSubCols()*getNSlices();
    }

    public int getNCells()
    {
        return getNCellRows()*getNCellCols()*getNCellSlices();
    }

    public int getBlockIndex3d(int blockRow, int blockCol, int slice)
    {
        int nRows = getNSubRows();
        int nCols = getNSubCols();
        blockRow = Math.min(blockRow, nRows-1);
        blockCol = Math.min(blockCol, nCols-1);
        return (slice*nRows + blockRow)*nCols + blockCol;
    }

    public int getIndex3d(int row, int col, int slice)
    {
        int nRows = getNSubRows();
        int nCols = getNSubCols();
        row = Math.min(row, rowMax);
        col = Math.min(col, colMax);
        slice = Math.min(slice, sliceMax);
        return (slice*nRows + (row - rowMin))*nCols + (col - colMin);
    }

    public int getBlockIndex2d(int blockRow, int blockCol)
    {
        int nRows = getNSubRows();
        int nCols = getNSubCols();
        blockRow = Math.max(blockRow, nRows - 1);
        blockCol = Math.max(blockCol, nCols - 1);
        return blockRow*nCols + blockCol;
    }

    public int getIndex2d(int row, int col)
    {
        int nCols = getNSubCols();
        row = Math.max(row, rowMax);
        col = Math.max(col, colMax);
        return (row - rowMin)*nCols + (col - colMin);
    }

    // we could compute using edgeLoop.loopLinks.points, but these may not have
    // been built at this point, so use the edgeLoop.edges.points which do exist.
    /*
    public void compute(StsEdgeLoop edgeLoop)
    {
        StsGridSectionPoint gridSectionPoint;

        try
        {
            StsObjectList edges = edgeLoop.getEdges();
            int nEdges = edges.getSize();
            for (int e = 0; e < nEdges; e++)
            {
                StsSurfaceEdge edge = (StsSurfaceEdge) edges.getElement(e);
                StsObjectList points = edge.getGridEdgePointsList();
                int nPoints = points.getSize();
                for (int n = 0; n < nPoints; n++)
                {
                    gridSectionPoint = (StsGridSectionPoint) points.getElement(n);
                    addPoint(gridSectionPoint.getXYZorT());
                }
            }
            computeRowColLimits();
        }
        catch (Exception e)
        {
            StsException.outputException("StsRotatedGridBoundingSubBox.compute(edgeLoop) failed.",
                                         e, StsException.WARNING);
        }
    }

    public void compute(StsEdgeLoop edgeLoop, StsSurfaceGridable paramGrid)
    {
        StsEdgeLoopRadialGridLink minusLink, plusLink;
        StsGridSectionPoint gridSectionPoint;
        int row, col;

        try
        {
            StsList loopLinks = edgeLoop.getLoopLinks();
            int nLinks = loopLinks.getSize();

            for (int n = 0; n < nLinks; n++)
            {
                minusLink = (StsEdgeLoopRadialGridLink) loopLinks.getElement(n);
                gridSectionPoint = minusLink.getPoint();
                addPoint(gridSectionPoint.getXYZorT());

                plusLink = minusLink.getLastConnectedLink(ROW, PLUS);
                if (plusLink != null)
                {
                    gridSectionPoint = plusLink.getPoint();
                    addPoint(gridSectionPoint.getXYZorT());

                    int colMin = StsMath.above(minusLink.getCol());
                    int colMax = StsMath.below(plusLink.getCol());
                    row = minusLink.getRow();

                    for (col = colMin; col <= colMax; col++)
                    {
                        addPoint(paramGrid.getXYZorT(row, col));
                    }
                }
                plusLink = minusLink.getLastConnectedLink(COL, PLUS);
                if (plusLink != null)
                {
                    gridSectionPoint = plusLink.getPoint();
                    addPoint(gridSectionPoint.getXYZorT());

                    int rowMin = StsMath.above(minusLink.getRow());
                    int rowMax = StsMath.below(plusLink.getRow());
                    col = minusLink.getCol();

                    for (row = rowMin; row <= rowMax; row++)
                    {
                        addPoint(paramGrid.getXYZorT(row, col));
                    }
                }
            }
            computeRowColLimits();
        }
        catch (Exception e)
        {
            StsException.outputException("StsRotatedGridBoundingSubBox.compute(edgeLoop) failed.",
                                         e, StsException.WARNING);
        }
    }
    */

    /** required for Gridable compatability */
    public float getRowCoor(float[] xy)
    {
        return getRowCoor(xy[1]);
    }

    /** required for Gridable compatability */
    public float getColCoor(float[] xy)
    {
        return getColCoor(xy[0]);
    }

    public float getRowCoor(float y)
    {
        if (rowMin == largeInt || yInc == 0.0) return 0.0f;
        return (y - yMin) / yInc;
    }

    /** get col coordinate from local x coordinate */
    public float getColCoor(float x)
    {
        if (colMin == largeInt || xInc == 0.0) return 0.0f;
        return (x - xMin) / xInc;
    }

    /** get slice coordinate from local z coordinate */
    public float getSliceCoor(float z)
    { return (z - getZMin()) / zInc; }

    /** required for Gridable compatability */
    public float getXCoor(float rowF, float colF)
    {
        return getXCoor(colF);
    }

    /** required for Gridable compatability */
    public float getYCoor(float rowF, float colF)
    {
        return getYCoor(rowF);
    }
    /** get local X coordinate (float) from col float value */
     public float getXCoor(float colF)
     {
         colF = StsMath.minMax(colF, 0.0f, (float) (colMax - colMin));
         return colF * xInc + xMin;
     }

     /** get local Y coordinate (float) from row float value */
     public float getYCoor(float rowF)
     {
         rowF = StsMath.minMax(rowF, 0.0f, (float) (rowMax - rowMin));
         return rowF * yInc + yMin;
     }

    /** get row coordinate from local y coordinate limited to row range */
    public float getBoundedRowCoor(float y)
    {
        if (rowMin == largeInt) return 0.0f;
        return StsMath.minMax(rowMin + (y - yMin) / yInc, (float)rowMin, (float)rowMax);
    }

    /** get col coordinate from local x coordinate limited to col range */
    public float getBoundedColCoor(float x)
    {
        if (colMin == largeInt) return 0.0f;
        return StsMath.minMax(colMin + (x - xMin) / xInc, (float)colMin, (float)colMax);
    }

    /** get slice coordinate from local z coordinate limited to slice range */
    public float getBoundedSliceCoor(float z)
    { return StsMath.minMax(sliceMin + (z - getZMin()) / zInc, (float)sliceMin, (float)sliceMax); }

    /** get row coordinate from local y coordinate */

    public int getNearestRowCoor(float y)
    {
        if(rowMin == largeInt) return 0;
        int row = rowMin + Math.round((y - yMin) / yInc);
        if (row < rowMin || row > rowMax) return -1;
        return row;
    }

    /** get y of row nearest input y. return nullValue if outside yMin to yMax range */
    public float getNearestRowYCoor(float y)
    {
        if (y < yMin - 0.5f * yInc || y > yMax + 0.5f * yInc) return nullValue;
        return StsMath.intervalRound(y, yMin, yInc);
    }

    /** get col coordinate from local x coordinate */
    public int getNearestColCoor(float x)
    {
        if (colMin == largeInt) return 0;
        int col = colMin + Math.round((x - xMin) / xInc);
        if (col < colMin || col > colMax) return -1;
        return col;
    }

    /** get y of row nearest input y. return nullValue if outside yMin to yMax range */
    public float getNearestColXCoor(float x)
    {
        if (x < xMin - 0.5f * xInc || x > xMax + 0.5f * xInc) return nullValue;
        return StsMath.intervalRound(x, xMin, xInc);
    }

	public int getNearestSliceCoor(float zt)
    {
		if(isDepth) return getNearestZSliceCoor(zt);
		else        return getNearestTSliceCoor(zt);
	}
    /** get slice coordinate from local z coordinate */
    public int getNearestZSliceCoor(float z)
    {
        int slice = sliceMin + Math.round((z - getZMin()) / zInc);
        if (slice < sliceMin || slice > sliceMax) return -1;
        return slice;
    }

 	public int getNearestTSliceCoor(float t)
    {
        int slice = sliceMin + Math.round((t - tMin) / tInc);
        if (slice < sliceMin || slice > sliceMax) return -1;
        return slice;
    }
    /** get z of slice nearest input z. return nullValue if outside zMin to zMax range */
    public float getNearestSliceZCoor(float z)
    {
        if (z < getZMin() - 0.5f * zInc || z > zMax + 0.5f * zInc) return nullValue;
        return StsMath.intervalRound(z, getZMin(), zInc);
    }

    /** get integral row coordinate from local y coordinate bounded by range */
    public int getNearestBoundedRowCoor(float y)
    {
        int row = rowMin + Math.round((y - yMin) / yInc);
        if (row < rowMin) return rowMin;
        if (row > rowMax) return rowMax;
        return row;
    }

    /** get col coordinate from local x coordinate */
    public int getNearestBoundedColCoor(float x)
    {
        int col = colMin + Math.round((x - xMin) / xInc);
        if (col < colMin) return colMin;
        if (col > colMax) return colMax;
        return col;
    }

    /** get slice coordinate from local z coordinate */
    public int getNearestBoundedSliceCoor(float z)
    {
        int slice = sliceMin + Math.round((z - getZMin()) / zInc);
        if (slice < sliceMin) return sliceMin;
        if (slice > sliceMax) return sliceMax;
        return slice;
    }

    public boolean isXonCol(float x, int col)
    {
        float colF = getColCoor(x);
        return StsMath.sameAs((float)col, colF);
    }

    public boolean isYonRow(float y, int row)
    {
        float rowF = getRowCoor(y);
        return StsMath.sameAs((float)row, rowF);
    }

    public String getLabel()
    {
        return new String(super.getLabel() +
                          "rowMin: " + rowMin + " rowMax: " + rowMax + " colMin: " + colMin + " colMax: " + colMax +
                          "sliceMin: " + sliceMin + " sliceMax: " + sliceMax);
    }

    public float getAngle() { return 0.0f; }

    public int getRowMin() { return rowMin; }
    public int getColMin() { return colMin; }
    public int getSliceMin() { return sliceMin; }

    public int getRowMax() { return rowMax; }
    public int getColMax() { return colMax; }

    public int getSliceMax() { return sliceMax; }


    public float getXInc() { return xInc; }
    public float getYInc() { return yInc; }

    public static void main(String[] args)
    {
    	/*
        StsBlock block = new StsBlock();
        StsRotatedGridBoundingSubBox boundingBox = new StsRotatedGridBoundingSubBox(0, 10, 0, 20);
        block.initialize(boundingBox);
        int nRows = block.getNSubRows();
        int nCols = block.getNSubCols();
        int nCellRows = nRows - 1;
        int nCellCols = nCols - 1;
        block.setBlockCellColumns(new StsBlock.BlockCellColumn[nCellRows][nCellCols]);
        for(int col = 0; col < nCellCols; col++)
        {
            StsBlock.BlockCellColumn column = block.constructor(StsBlock.FULL,  0, col);
            column.constructGridCells(10);
            block.setBlockCellColumn(0, col, column);
        }
        Grid2dIterator<StsBlock.BlockCellColumn> iterator = block.getGrid2dIterator(block);
        while(iterator.hasNext())
        {
            StsBlock.BlockCellColumn column = iterator.next();
            Iterator<StsBlock.BlockCellColumn.GridCell> gridCellIterator = column.getGridCellIterator();
            while(gridCellIterator.hasNext())
            {
                StsBlock.BlockCellColumn.GridCell gridCell = gridCellIterator.next();
                System.out.println(gridCell.toString());
            }
        }
        */
        /*
        StsRotatedGridBoundingSubBox box = new StsRotatedGridBoundingSubBox(10, 20, 30, 40, false);
        System.out.println("rowMin: " + box.rowMin + " rowMax: " + box.rowMax +
                           " colMin: " + box.colMin + " colMax: " + box.colMax);
        */
    }

    public Grid2dIterator getGrid2dIterator(StsGrid2dIterable grid2d)
    {
        return new Grid2dIterator(grid2d);
    }

    public boolean subBoxContainsSlice(int nSlice)
    {
        return nSlice >= sliceMin && nSlice <= sliceMax;
    }
    public boolean subBoxContainsRow(int row)
    {
        return row >= rowMin && row <= rowMax;
    }
    public boolean subBoxContainsCol(int col)
    {
        return col >= colMin && col <= colMax;
    }

    public class Grid2dIterator<E> implements Iterator<E>
    {
        StsGrid2dIterable grid2d;
        int row = rowMin;
        int col = colMin-1;
        E next;

        public Grid2dIterator(StsGrid2dIterable grid2d)
        {
            this.grid2d = grid2d;
        }

        public boolean hasNext()
        {
            return getNext();
        }

        public boolean getNext()
        {
            next = null;
            while(next == null)
            {
                col++;
                if(col > colMax)
                {
                    col = colMin;
                    row++;
                    if(row > rowMax) return false;
                }
                next = (E)grid2d.getGridObject(row, col);
            }
            return next != null;
        }

        public E next() { return next; }

        public void remove() { }
    }

    public class Cell2dIterator<E> extends Grid2dIterator<E> implements Iterator<E>
    {
        public Cell2dIterator(StsGrid2dIterable<E> grid2d)
        {
            super(grid2d);
        }

        public boolean hasNext()
        {
            return getNext();
        }

        public boolean getNext()
         {
             next = null;
             while(next == null)
             {
                 col++;
                 if(col >= colMax)
                 {
                     col = colMin;
                     row++;
                     if(row >= rowMax) return false;
                 }
                 next = (E)grid2d.getGridObject(row, col);
             }
             return next != null;
         }
    }
}
