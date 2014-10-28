package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.MVC.Views.StsCursor3d;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.StsMessageFiles;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

import javax.media.opengl.GL;
import java.util.ArrayList;

/**
 * © tom 10/20/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsSegmentCellGrid extends StsRotatedGridBoundingSubBox
{
    /**gridType: NONE, CHANNEL, POINT_BAR, OVERBANK, CREVASSE_SPLAY
    byte gridType = StsChannelSegment.NONE;
    /**object index of channel */
    int channelIndex;
    /**sequence index of segment */
    int segmentIndex;
    /**boolean 2d array indicating whether cell is filled or not */

    float z;
    boolean[][] filled;
    int nFilledRows;
    int nFilledCols;

    static final int ROW = StsRotatedGridBoundingBox.ROW;
    static final int COL = StsRotatedGridBoundingBox.COL;

    static final int INT_MAX = Integer.MAX_VALUE;

    StsSegmentCellGrid() {}

    StsSegmentCellGrid(StsPoint[] forwardPoints, StsPoint[] reversePoints, StsGeoModelVolume geoModelVolume, float z)
    {
        super(false);

        StsPoint[] loopPoints;
        ArrayList<StsGridCrossingPoint> gridCrossings;
        ArrayList<StsGridCrossingPoint>[] rowArrays;

        try
        {
            StsRotatedGridBoundingBox centeredGrid = geoModelVolume.centeredGrid;
            initialize(geoModelVolume);

            this.z = z;
            loopPoints = concatForwardReverseArraysLoop(forwardPoints, reversePoints);
            // compute all grid crossings of cell-centered grid
            gridCrossings = StsGridCrossings.computeGridCrossingPoints(loopPoints, centeredGrid);
            for (StsGridCrossingPoint gridCrossing : gridCrossings)
            {
                if (gridCrossing.isRowOrCol())
                    addPoint(gridCrossing);
            }

            // initially get the number of rows and cols crossing the loop and create
            // row and col entries with the crossing points
            int nSubRows = getNSubRows();
            int nSubCols = getNSubCols();
            rowArrays = new ArrayList[nSubRows];
            for (int n = 0; n < nSubRows; n++)
                rowArrays[n] = new ArrayList<>();

            for (StsGridCrossingPoint gridCrossing : gridCrossings)
                if (gridCrossing.isRow())
                    rowArrays[gridCrossing.getRow() - rowMin].add(gridCrossing);

            filled = new boolean[nSubRows][nSubCols];
            checkSortFillGridCrossingArrays(ROW, rowArrays, filled);
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "constructor", e);
        }
    }

    private boolean checkSortFillGridCrossingArrays(int rowOrCol, ArrayList<StsGridCrossingPoint>[] rowColArrays, boolean[][] filled)
    {
        for(ArrayList<StsGridCrossingPoint> rowColArray : rowColArrays)
        {
            StsGridCrossingPoint[] array  = rowColArray.toArray(new StsGridCrossingPoint[0]);
            if(array.length%2 != 0)
            {
                StsMessageFiles.errorMessage("Odd number of gridCrossingPoints (" + array.length + " ) for point " + array[0].toString() + " to " + array[array.length-1].toString());
                continue;
            }

            StsMath.qsort(array);

            for(int n = 0; n < array.length - 1; n += 2)
            {
                StsGridCrossingPoint point = array[n];
                StsGridCrossingPoint nextPoint = array[n+1];

                int row = point.getRow();
                if(!isInsideRowRange(row)) continue;
                row -= rowMin; // adjust to subBox local coors
                int firstCol = StsMath.ceiling(point.jF);
                if(firstCol < 0) firstCol = 0;
                // if(!isInsideColRange(firstCol)) continue;
                firstCol -= colMin;
                int lastCol = StsMath.floor(nextPoint.jF);
                if(lastCol > nCols - 1) lastCol = nCols - 1;
                // if(!isInsideColRange(lastCol)) continue;
                lastCol -= colMin;
                try
                {
                    for (int col = firstCol; col <= lastCol; col++)
                        filled[row][col] = true;
                }
                catch(Exception e)
                {
                    StsException.outputWarningException(this, "checkSortFillGridCrossingArrays", " row or col out of range of filled array", e);
                }
            }
        }
        return true;
    }

    /** we are given two StsPoint arrays both logically in the same direction.  Form a loop going forward on the first
     *  reversed on the second, with the first point added again to close the loop
     * @param forwardPoints  the StsPoint array in forward direction
     * @param reversePoints  the StsPoint array to be reversed
     * @return the concatenated list
     */
    static private StsPoint[] concatForwardReverseArraysLoop(StsPoint[] forwardPoints, StsPoint[] reversePoints)
    {
        int nForwardPoints = forwardPoints.length;
        int nReversePoints = reversePoints.length;
        int nTotalPoints = nForwardPoints + nReversePoints + 1;
        StsPoint[] loopPoints = new StsPoint[nTotalPoints];
        System.arraycopy(forwardPoints, 0, loopPoints, 0, nForwardPoints);
        for(int i = 0, n = nTotalPoints-1; i < nReversePoints; i++, n--)
            loopPoints[n-1] = reversePoints[i];
        loopPoints[nTotalPoints-1] = forwardPoints[0];
        return loopPoints;
    }

    /** for a slice plane, map the cellGrid boolean rectangle to the 2D byte array.
     *  if a cellGris value is true, insert the channel color index.
     *  Note that the byte array is nRows x nCols while we are filling only cell values
     *  (nRows-1)*(nCells-1)
     */
    public void fillData(byte[] byteData, int nChannel)
    {
        int row = 0, col = 0, r = 0, c = 0, n = 0;
        int rowStart, rowEnd, colStart, colEnd;
        try
        {
            rowStart = (rowMin < 0) ? 0 : rowMin;
            rowEnd = (rowMax > nRows-2) ? nRows-2 : rowMax;

            colStart = (colMin < 0) ? 0 : colMin;
            colEnd = (colMax > nCols-2) ? nCols-2 : colMax;

            for (row = rowStart, r = rowStart - rowMin; row <= rowEnd; row++, r++)
            {
                n = row * nCols + colStart;
                for (col = colStart, c = colStart - colMin; col <= colEnd; col++, c++, n++)
                    if (filled[r][c]) byteData[n] = (byte) nChannel;
            }
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "fillData", "row " + row + " col " + col + " r " + r + " c " + c + " n " + n, e);
        }
    }

    /** for a vertical row or col plane, map the cellGrid boolean rectangle to the 2D byte array.
     *  if a cellGris value is true, insert the channel color index.
     *  Note that the byte array is nCols x nSlices for a row plane (the array is vertically down from the first cell and
     *  down along each cell in the row) and nRows x nSlices for a col plane (the array is vertically down from the first cell and
     *  down along each cell in the col).  The cell arrays\ we are filling is one less n each coordinate direction, but the single-array indexing
     *  is over the grid array.
     */
    public void fillData(byte[] byteData, int dir, int nPlane, StsChannel channel)
    {
        int row = 0, col = 0, r = 0, c = 0, n = 0;

        try
        {
            byte channelIndex = (byte)channel.getIndex();
            int channelSliceMin = channel.getSliceMin();
            int channelSliceMax = channel.getSliceMax();
            int nChannelSlices = channelSliceMax - channelSliceMin + 1;

            // vertical plane is at a given xPlane (constant x) so is along a column thru the segment
            // so check filled 2d array along segment col
            if(dir == StsCursor3d.XDIR)
            {
                int planeCol = nPlane;
                if(!subBoxContainsCol(planeCol)) return;

                int segmentCol = planeCol - colMin;
                n = rowMin*nSlices + channelSliceMin;

                for (row = rowMin, r = 0; row <= rowMax; row++, r++, n += nSlices)
                {
                    boolean cellFilled = filled[r][segmentCol];
                    if(cellFilled)
                    {
                        for(int i = 0; i < nChannelSlices; i++)
                            byteData[n+i] = channelIndex;
                    }
                }
            }
            else if(dir == StsCursor3d.YDIR)
            {
                int planeRow = nPlane;
                if(!subBoxContainsRow(planeRow)) return;

                int segmentRow = planeRow - rowMin;
                n = colMin*nSlices + channelSliceMin;

                for (col = colMin, c = 0; col <= colMax; col++, c++, n += nSlices)
                {
                    boolean cellFilled = filled[segmentRow][c];
                    if(cellFilled)
                    {
                        for(int i = 0; i < nChannelSlices; i++)
                            byteData[n+i] = channelIndex;
                    }
                }
            }

        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "fillData", "row " + row + " col " + col + " r " + r + " c " + c + " n " + n, e);
        }
    }

    public void display(GL gl, StsColor stsColor, boolean stipple)
    {
        StsGLDraw.drawQuads(gl, this, filled, z, StsGLDraw.verticalNormal, stsColor, stipple);
    }
}


