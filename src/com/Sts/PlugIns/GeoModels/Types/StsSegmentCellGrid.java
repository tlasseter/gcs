package com.Sts.PlugIns.GeoModels.Types;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.StsMessageFiles;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsGLDraw;
import com.Sts.Framework.Utilities.StsMath;
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
        ArrayList<StsGridCrossingPoint>[] colArrays;


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

                int firstCol = StsMath.ceiling(point.jF)- colMin;
                int lastCol = StsMath.floor(nextPoint.jF)- colMin;
                int row = point.getRow() - rowMin;
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

    public void display(GL gl, StsColor stsColor, boolean stipple)
    {
        StsGLDraw.drawQuads(gl, this, filled, z, StsGLDraw.verticalNormal, stsColor, stipple);
    }
}


