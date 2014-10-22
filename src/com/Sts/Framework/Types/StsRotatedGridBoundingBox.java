package com.Sts.Framework.Types;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 *
 * @author T.Lasseter
 * @version 1.1
 */

public class StsRotatedGridBoundingBox extends StsRotatedBoundingBox implements StsXYGridable, Cloneable, Serializable
{
    public float dataMin = StsParameters.largeFloat; // minimum data value
    public float dataMax = -StsParameters.largeFloat; // maximum data value
    public float dataAvg = 0.0f; // average data value

    public int nRows = 0;
    public int nCols = 0;
    public int nSlices = 0;

    public float xInc = 0.0f;
    public float yInc = 0.0f;
    public float zInc = 0.0f;
    public float tInc = 0.0f;

	/** Seismic and seismic-derived volumes, and virtual volumes require that depth or time increments are congruent.
	 *  So in StsSeismicBoundingBox (mother of these classses, ztIncCongruent is set to true.
	 *  for other rotatedGridBoundingBoxes such as surfaces, zInc and tInc are not required to be congruent
	 */
	public boolean ztIncCongruent = false;

    public float rowNumMin = nullValue; // row number label for first row
    public float rowNumMax = nullValue; // row number label for last row
    public float colNumMin = nullValue; // col number label for first col
    public float colNumMax = nullValue; // col number label for last col
    public float rowNumInc = 0.0f; // interval between row number labels
    public float colNumInc = 0.0f; // interval between col number labels

    /*
        transient public boolean rowRangeOK = false;
        transient public boolean colRangeOK = false;
        transient public boolean sliceRangeOK = false;
    */
    transient public StsXYGridable grid;

    transient static public final int ROW = StsParameters.ROW;
    transient static public final int COL = StsParameters.COL;
    transient static public final int ROWCOL = StsParameters.ROWCOL;

    transient static final int PLUS = StsParameters.PLUS;
    transient static final int MINUS = StsParameters.MINUS;

    transient static public final int XDIR = StsParameters.XDIR; // 0: XDIR is at a constant X or crossLine
    transient static public final int YDIR = StsParameters.YDIR; // 1: YDIR is at a constant Y or inLine
    transient static public final int ZDIR = StsParameters.ZDIR; // 2: ZDIR is at a constant Z or slice

    transient static public final float nullValue = StsParameters.nullValue;

    static final long serialVersionUID = 1l;

    public StsRotatedGridBoundingBox()
    {
    }

    public StsRotatedGridBoundingBox(boolean persistent)
    {
        super(persistent);
    }

    public StsRotatedGridBoundingBox(boolean persistent, String name)
    {
        super(persistent, name);
    }

    public StsRotatedGridBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
    {
        super(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    public StsRotatedGridBoundingBox(StsRotatedGridBoundingBox otherBox, boolean persistent)
    {
        super(persistent);
        initializeToBoundingBox(otherBox);
    }

    public StsRotatedGridBoundingBox(StsRotatedGridBoundingBox otherBox)
    {
        initializeToBoundingBox(otherBox);
    }

    public StsRotatedGridBoundingBox(int nRows, int nCols, double xOrigin, double yOrigin, float xMin, float yMin, float xInc, float yInc, boolean persistent)
    {
        super(persistent);
        this.nRows = nRows;
        this.nCols = nCols;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xInc = xInc;
        this.yInc = yInc;
    }

    public void initialize(float xMin, float xMax, float xInc, float yMin, float yMax, float yInc, float zMin, float zMax, float zInc, boolean initialized)
    {
        super.initialize(xMin, xMax, yMin, yMax, zMin, zMax, initialized);
        this.xInc = xInc;
        this.yInc = yInc;
        this.zInc = zInc;
    }

    public void initialize(StsBoundingBox box, float[] incs, boolean initialized)
    {
        super.initialize(box, initialized);
        this.xInc = incs[0];
        this.yInc = incs[1];
        this.zInc = incs[2];
    }

	public void initialize(StsRotatedGridBoundingBox rotatedBoundingBox)
	{
        super.initialize(rotatedBoundingBox);
        this.setXYZIncs(rotatedBoundingBox);

        rowNumMin = rotatedBoundingBox.rowNumMin;
        rowNumMax = rotatedBoundingBox.rowNumMax;
        colNumMin = rotatedBoundingBox.colNumMin;
        colNumMax = rotatedBoundingBox.colNumMax;
        rowNumInc = rotatedBoundingBox.rowNumInc;
        colNumInc = rotatedBoundingBox.colNumInc;
	}
    public void initializeToBoundingBox(StsRotatedGridBoundingBox boundingBox)
    {
        StsToolkit.copySubToSuperclass(boundingBox, this, StsRotatedGridBoundingBox.class, StsBoundingBox.class, true);
    }

    public StsRotatedGridBoundingBox getGridBoundingBoxClone()
    {
        try
        {
            return (StsRotatedGridBoundingBox) this.clone();
        }
        catch (Exception e)
        {
            StsException.outputException("StsRotatedGridBoundingBox.getClone(grid) failed.", e, StsException.WARNING);
            return null;
        }
    }

    public StsRotatedGridBoundingSubBox getGridBoundingBox()
    {
        return new StsRotatedGridBoundingSubBox(this);
    }

    public StsRotatedGridBoundingSubBox getGridBoundingBox(boolean persistent)
    {
        StsRotatedGridBoundingSubBox gridBox = new StsRotatedGridBoundingSubBox(persistent);
        gridBox.rowRangeOK = true;
        gridBox.colRangeOK = true;
        gridBox.rowMin = getRowMin();
        gridBox.rowMax = getRowMax();
        gridBox.colMin = getColMin();
        gridBox.colMax = getColMax();
        gridBox.sliceMin = getSliceMin();
        gridBox.sliceMax = getSliceMax();
        return gridBox;
    }

    public boolean initialize(StsModel model)
    {
        return super.initialize(model);
    }

    public void reinitializeBoundingBox()
    {
        super.reinitializeBoundingBox();
        dataMin = StsParameters.largeFloat;
        dataMax = -StsParameters.largeFloat;

        nRows = 0;
        nCols = 0;
        nSlices = 0;

        xInc = 0.0f;
        yInc = 0.0f;
        zInc = 0.0f;
		tInc = 0.0f;

        rowNumMin = StsParameters.nullValue;
        rowNumMax = StsParameters.nullValue;
        colNumMin = StsParameters.nullValue;
        colNumMax = StsParameters.nullValue;
        rowNumInc = 0.0f;
        colNumInc = 0.0f;
    }

    public boolean isInsideRowColRange(int row, int col)
    {
        return row >= 0 && row <= nRows - 1 && col >= 0 && col <= nCols - 1;
    }

    public boolean isInsideRowColRange(float rowF, float colF)
    {
        return rowF >= 0 && rowF <= nRows - 1 && colF >= 0 && colF <= nCols - 1;
    }

    public boolean isInsideRange(int row, int col, int slice)
    {
        return row >= 0 && row <= nRows - 1 && col >= 0 && col <= nCols - 1 && slice >= 0 && slice <= nSlices - 1;
    }

    public boolean isRowColNumInitialized()
    {
        return rowNumMin != nullValue && colNumMin != nullValue;
    }

    public float[] getBoundingBoxRangeData(int direction)
    {
        if(direction == XDIR)
            return getXorXlineRange();
        else if(direction == YDIR)
            return getYorInlineRange();
        else
            return getZTRange();
    }
    
    public float[] getYorInlineRange()
    {
        if(isRowColNumInitialized())
            return new float[] { rowNumMin, rowNumMax, rowNumInc };
        else
            return new float[] { yMin, yMax, yInc };
    }

    public float[] getXorXlineRange()
    {
        if(isRowColNumInitialized())
            return new float[] { colNumMin, colNumMax, colNumInc };
        else
            return new float[] { xMin, xMax, xInc };
    }

    public float[] getZTRange()
    {
        return new float[] { getZTMin(), getZTMax(), getZTInc() };
    }

    public void initializeNumsToRowColRange()
    {
        rowNumMin = 0;
        rowNumMax = nRows - 1;
        colNumMin = 0;
        colNumMax = nCols - 1;
        rowNumInc = 1;
        colNumInc = 1;
    }

    /*
        public void computeRowColLimits()
        {
            float[] lowerLeftCorner = new float[] { xMin, yMin };
            float[] upperRightCorner = new float[] { xMax, yMax };
            rowMin = StsMath.floor(grid.getRowCoor(lowerLeftCorner));
            rowMax = StsMath.ceiling(grid.getRowCoor(upperRightCorner));
            colMin = StsMath.floor(grid.getColCoor(lowerLeftCorner));
            colMax = StsMath.ceiling(grid.getColCoor(upperRightCorner));
            nRows = rowMax - rowMin + 1;
            nCols = colMax - colMin + 1;
            rowRangeOK = nRows >= 0;
            colRangeOK = nCols >= 0;
        }

        public void addPoint(int row, int col)
        {
            rowMin = Math.min(rowMin, row);
            rowMax = Math.max(rowMax, row);
            colMin = Math.min(colMin, col);
            colMax = Math.max(colMax, col);
            nRows = rowMax - rowMin + 1;
            nCols = colMax - colMin + 1;
            rowRangeOK = nRows >= 0;
            colRangeOK = nCols >= 0;
        }

        public void addPoint(float rowF, float colF)
        {
            rowMin = Math.min(rowMin, StsMath.floor(rowF));
            rowMax = Math.max(rowMax, StsMath.ceiling(rowF));
            colMin = Math.min(colMin, StsMath.floor(colF));
            colMax = Math.max(colMax, StsMath.ceiling(colF));
            nRows = rowMax - rowMin + 1;
            nCols = colMax - colMin + 1;
            rowRangeOK = nRows >= 0;
            colRangeOK = nCols >= 0;
        }

        public void addPoint(StsRowCol rowCol)
        {
            int rowOrCol = rowCol.getRowOrCol();
            if(rowOrCol == ROW || rowOrCol == ROWCOL)
            {
                int row = rowCol.getRow();
                rowMin = Math.min(rowMin, row);
                rowMax = Math.max(rowMax, row);
                nRows = rowMax - rowMin + 1;
                rowRangeOK = nRows >= 0;
            }
            if(rowOrCol == COL || rowOrCol == ROWCOL)
            {
                int col = rowCol.getCol();
                colMin = Math.min(colMin, col);
                colMax = Math.max(colMax, col);
                nCols = colMax - colMin + 1;
                colRangeOK = nCols >= 0;
            }
        }
    */

    public boolean addRotatedGridBoundingBox(StsRotatedGridBoundingBox box)
    {
        boolean changed = super.addRotatedBoundingBox(box);
        setXYZIncs(box);
        adjustRowColNumbering(box);
        setRowColIndexRanges();
        setSliceIndexRange();
		return changed;
    }

    public boolean addLineRotatedBoundingBox(StsRotatedGridBoundingBox box)
    {
		boolean changed = super.addRotatedBoundingBox(box);
        setXYZIncs(box);
        addRotatedBoundingBox(box);
        setRowColIndexRanges();
        setSliceIndexRange();
		return changed;
    }

    public boolean addUnrotatedBoundingBox(StsBoundingBox box)
    {
        return super.addBoundingBox(box);
    }

	/** it must have already been determined that the input box is congruent with this box; if not, the load would have been rejected */
    private void setXYZIncs(StsRotatedGridBoundingBox box)
    {
		int nZTIncsMax = StsProject.nZTIncsMax;
		int approxNumZTIncs = StsProject.approxNumZTIncs;

        if (initializedXY())
        {
            if (xInc == 0.0f)
                setXInc(box.xInc);
            else if (box.xInc != 0.0f && Math.abs(box.xInc) < Math.abs(xInc))
                setXInc(box.xInc);

            if (yInc == 0.0f)
                setYInc(box.yInc);
            else if (box.yInc != 0.0f && Math.abs(box.yInc) < Math.abs(yInc))
                setYInc(box.yInc);
        }
        else
        {
             setXInc(box.xInc);
             setYInc(box.yInc);
        }

		if(initializedZ())
		{
			if(box.zInc != 0.0f)
			{
				if(zInc == 0.0f)
					setZInc(box.zInc);
				else
				{
					if(Math.abs(box.zInc) < Math.abs(zInc))
					{
						float[] niceScale = StsMath.niceScale(getZMin(), getZMax(), nZTIncsMax, true);
						float minZInc = niceScale[2];
						if (box.zInc > minZInc)  setZInc(minZInc);
					}
				}
			}
			else if(zInc == 0.0f)
			{
				float[] niceScale = StsMath.niceScale(getZMin(), getZMax(), approxNumZTIncs, true);
				setZInc(niceScale[2]);
			}
		}
		if(initializedT())
		{
			if(box.tInc != 0.0f)
			{
				if(tInc == 0.0f)
					setTInc(box.tInc);
				else
				{
					if(Math.abs(box.tInc) < Math.abs(tInc))
					{
						float[] niceScale = StsMath.niceScale(tMin, tMax, nZTIncsMax, true);
						float minTInc = niceScale[2];
						if (box.tInc > minTInc) setTInc(minTInc);
					}
				}
			}
			else if(tInc == 0.0f)
			{
				float[] niceScale = StsMath.niceScale(tMin, tMax, approxNumZTIncs, true);
				setTInc(niceScale[2]);
			}
		}
		this.nRows = box.nRows;
		this.nCols = box.nCols;
    }
/*
    public void checkMakeCongruent(StsRotatedGridBoundingBox box)
    {
        boolean isXIncCongruent = true, isYIncCongruent = true;

		int nZTIncsMax = StsProject.nZTIncsMax;
		int approxNumZTIncs = StsProject.approxNumZTIncs;

        if (initializedXY())
        {
            if (xInc == 0.0f)
                setXInc(box.xInc);
			// current xInc and box.xInc are both non-zero
            else if (box.xInc != 0.0f && Math.abs(box.xInc) < Math.abs(xInc))
                setXInc(box.xInc);

            if (yInc == 0.0f)
                setYInc(box.yInc);
            else if (box.yInc != 0.0f && Math.abs(box.yInc) < Math.abs(yInc))
                setYInc(box.yInc);
        }
        else
        {
            setXInc(box.xInc);
            setYInc(box.yInc);
        }

		if(initializedZ())
		{
			if(box.zInc != 0.0f)
			{
				if(zInc == 0.0f)
					setZInc(box.zInc);
				else
				{
					if(Math.abs(box.zInc) < Math.abs(zInc))
					{
						float[] niceScale = StsMath.niceScale(getZMin(), getZMax(), nZTIncsMax, true);
						float minZInc = niceScale[2];
						if (box.zInc > minZInc)  setZInc(minZInc);
					}
				}
			}
			else if(zInc == 0.0f)
			{
				float[] niceScale = StsMath.niceScale(getZMin(), getZMax(), approxNumZTIncs, true);
				setZInc(niceScale[2]);
			}
		}
		if(initializedT())
		{
			if(box.tInc != 0.0f)
			{
				if(tInc == 0.0f)
					setTInc(box.tInc);
				else
				{
					if(Math.abs(box.tInc) < Math.abs(tInc))
					{
						float[] niceScale = StsMath.niceScale(tMin, tMax, nZTIncsMax, true);
						float minTInc = niceScale[2];
						if (box.zInc > minTInc) setTInc(minTInc);
					}
				}
			}
			else if(tInc == 0.0f)
			{
				float[] niceScale = StsMath.niceScale(tMin, tMax, approxNumZTIncs, true);
				setTInc(niceScale[2]);
			}
		}

        if (xInc == 0.0f)
            xInc = box.xInc;
        else if (box.xInc != 0.0f)
        {
            isXIncCongruent = StsMath.isIntegralRatio(box.xInc, xInc, 0.001f);
//            if(isXIncCongruent && Math.abs(box.xInc) < Math.abs(xInc))
//               xInc = box.xInc;
        }
        if (yInc == 0.0f)
            yInc = box.yInc;
        else if (box.yInc != 0.0f)
        {
            isYIncCongruent = StsMath.isIntegralRatio(box.yInc, yInc, 0.001f);
//            if(isYIncCongruent && Math.abs(box.yInc) < Math.abs(yInc))
//                yInc = box.yInc;
        }

        if (zInc == 0.0f)
            zInc = box.zInc;
//        else if(box.zInc > 0.0f && Math.abs(box.zInc) < Math.abs(zInc))
//            zInc = box.zInc;


        if (originSet)
        {
            // adjust new box to the same origin as already set
            double dXOrigin = box.xOrigin - xOrigin;
            double dYOrigin = box.yOrigin - yOrigin;

            box.xOrigin = xOrigin;
            box.yOrigin = yOrigin;

            float dx = (float) (dXOrigin * cosXY + dYOrigin * sinXY);
            float dy = (float) (dYOrigin * cosXY - dXOrigin * sinXY);

            box.xMin += dx;
            box.xMax += dx;
            box.yMin += dy;
            box.yMax += dy;

            if (isXIncCongruent)
            {
                float ratio = box.xMin / xInc;
                int iRatio = Math.round(ratio);
                if (Math.abs(ratio - iRatio) <= 0.25f)
                {
                    dx = xInc * iRatio - box.xMin;
                    box.xMin += dx;
                    box.xMax += dx;
                }

            }
            if (isYIncCongruent)
            {
                float ratio = box.yMin / yInc;
                int iRatio = Math.round(ratio);
                if (Math.abs(ratio - iRatio) <= 0.25f)
                {
                    dy = yInc * iRatio - box.yMin;
                    box.yMin += dy;
                    box.yMax += dy;
                }
            }
        }

        adjustRowColNumbering(box);
		makeCongruent(box);

        setRowColIndexRanges();
        setSliceIndexRange();
    }
*/
	public void checkSetNiceIncrements(int approxNumXYIncs, int approxNumZTIncs)
	{
		float[] scale;
		if(initializedXY())
		{
			if(xInc == 0.0f)
			{
				scale = StsMath.niceScale(xMin, xMax, approxNumXYIncs, true);
				setXInc(scale[2]);
			}
			if(yInc == 0.0f)
			{
				scale = StsMath.niceScale(yMin, yMax, approxNumXYIncs, true);
				setYInc(scale[2]);
			}
		}
        if(initializedT())
        {
			if(tInc == 0.0f)
			{
            	scale = StsMath.niceScale(tMin, tMax, approxNumZTIncs, true);
            	setTInc(scale[2]);
			}
        }
        if(initializedZ())
        {
			if(zInc == 0.0f)
			{
            	scale = StsMath.niceScale(getZMin(), getZMax(), approxNumZTIncs, true);
            	setZInc(scale[2]);
			}
        }
	}

    // Row and col number labels must be the same for any bounding box displayed.
    // Initialize it to first one that is loaded.
    // As additional boxes are loaded, extend ranges as required.
    public void adjustRowColNumbering(StsRotatedGridBoundingBox box)
    {
        if (this == box)
        {
            StsException.systemError("Developer!  StsRotatedGridBoundingBox.adjustRowColNumbering called with this == box.\n" +
                " They need to be different, i.e., argument box is adjusting the current box.");
            return;
        }
        if (rowNumMin != nullValue)
        {
            double dXOrigin = box.xOrigin - xOrigin;
            double dYOrigin = box.yOrigin - yOrigin;

            float dx = (float) (dXOrigin * cosXY + dYOrigin * sinXY);
            float dy = (float) (dYOrigin * cosXY - dXOrigin * sinXY);

            int rowMax = Math.round(getRowCoor(box.yMax + dy));
            if (rowMax > nRows - 1) rowNumMax = getRowNumFromRow(rowMax);

            int rowMin = Math.round(getRowCoor(box.yMin + dy));
            if (rowMin < 0) rowNumMin = getRowNumFromRow(rowMin);

            int colMax = Math.round(getColCoor(box.xMax + dx));
            if (colMax > nCols - 1) colNumMax = getColNumFromCol(colMax);

            int colMin = Math.round(getColCoor(box.xMin + dx));
            if (colMin < 0) colNumMin = getColNumFromCol(colMin);
        }
        else if (box.rowNumMin != nullValue)
        {
            rowNumMin = box.rowNumMin;
            rowNumMax = box.rowNumMax;
            colNumMin = box.colNumMin;
            colNumMax = box.colNumMax;
            rowNumInc = box.rowNumInc;
            colNumInc = box.colNumInc;
        }
    }

    public void setRowColNumRange(float rowNumMin, float rowNumMax, float colNumMin, float colNumMax, float rowNumInc, float colNumInc)
    {
        this.rowNumMin = rowNumMin;
        this.rowNumMax = rowNumMax;
        this.colNumMin = colNumMin;
        this.colNumMax = colNumMax;
        this.rowNumInc = rowNumInc;
        this.colNumInc = colNumInc;
    }

    public void setIndexRanges()
    {
        if (rowNumInc != 0.0f)
            nRows = Math.round((rowNumMax - rowNumMin) / rowNumInc) + 1;
        else
            nRows = 1;
        if (colNumInc != 0.0f)
            nCols = Math.round((colNumMax - colNumMin) / colNumInc) + 1;
        else
            nCols = 1;
        if (zInc != 0.0f)
            nSlices = Math.round((zMax - getZMin()) / zInc) + 1;
        else
            nSlices = 1;
    }

    private void setRowColIndexRanges()
    {
        if (yInc != 0.0f)
            nRows = Math.round((yMax - yMin) / yInc) + 1;
        else
            nRows = 1;
        if (xInc != 0.0f)
            nCols = Math.round((xMax - xMin) / xInc) + 1;
        else
            nCols = 1;
    }

    private void setSliceIndexRange()
    {
		if(initializedZ())
		{
			if (zInc != 0.0f)
				nSlices = Math.round((zMax - getZMin()) / zInc) + 1;
			else
				nSlices = 1;
		}
		else if(initializedT())
		{
			if (tInc != 0.0f)
				nSlices = Math.round((tMax - tMin) / tInc) + 1;
			else
				nSlices = 1;
		}
    }

    public boolean addRotatedBoundingBox(StsRotatedGridBoundingBox box)
    {
        boolean changed = super.addRotatedBoundingBox(box);
        setXYZIncs(box);
        adjustRowColNumbering(box);
        // checkSetOriginAndAngle(box);
        setIndexRanges();
		return changed;
    }

    static public boolean allAreCongruent(StsRotatedGridBoundingBox[] boxes)
    {
        if (boxes == null || boxes.length == 0) return true;
        int nBoxes = boxes.length;
        if (nBoxes == 1) return true;
        StsRotatedGridBoundingBox firstBox = boxes[0];
        for (int n = 1; n < nBoxes; n++)
            if (!firstBox.congruentWith(boxes[n])) return false;
        return true;
    }

    public boolean congruentWith(StsRotatedGridBoundingBox otherBox)
    {
        if (!StsMath.sameAsTol(xInc, otherBox.xInc, 0.001f * xInc)) return false;
        if (!StsMath.sameAsTol(yInc, otherBox.yInc, 0.001f * yInc)) return false;
        if (!StsMath.sameAsTol(zInc, otherBox.zInc, 0.001f * zInc)) return false;
        if (!StsMath.sameAsTol(angle, otherBox.angle, 0.01f)) return false;
        if (!StsMath.isIntegral(getColCoor(otherBox.xMin), 0.001f * xInc)) return false;
        if (!StsMath.isIntegral(getRowCoor(otherBox.yMin), 0.001f * yInc)) return false;
		if(initializedZ() && otherBox.initializedZ() && !StsMath.isIntegral(getSliceCoor(otherBox.getZMin()), 0.001f * zInc)) return false;
		if(initializedT() && otherBox.initializedT() && !StsMath.isIntegral(getSliceCoor(otherBox.tMin), 0.001f * tInc)) return false;
        return true;
    }

	public boolean isXYCongruentDebug(StsRotatedGridBoundingBox otherBox)
	{
		return false;
	}

	public boolean isXYCongruent(StsRotatedGridBoundingBox otherBox)
	{
		if(!initializedXY()) return true;
		if(!angleSet) return true;
		if(!StsMath.isIntegralRatio(xInc, otherBox.xInc)) return false;
		if(!StsMath.isIntegralRatio(yInc, otherBox.yInc)) return false;
		if(xInc < otherBox.xInc && !StsMath.isIntegral(getColCoor(otherBox.xMin))) return false;
		else if(xInc > otherBox.xInc && !StsMath.isIntegral(otherBox.getColCoor(xMin))) return false;
		if(yInc < otherBox.yInc && !StsMath.isIntegral(getRowCoor(otherBox.yMin))) return false;
		else if(yInc > otherBox.yInc && !StsMath.isIntegral(otherBox.getRowCoor(yMin))) return false;
		return true;
	}

	public boolean isXYZCongruent(StsRotatedGridBoundingBox otherBox)
	{
		if(!isXYCongruent(otherBox)) return false;
		if(initializedZ() && otherBox.initializedZ())
		{
			if(!StsMath.isIntegralRatio(zInc, otherBox.zInc)) return false;
			if(zInc < otherBox.zInc && !StsMath.isIntegral(getSliceCoor(otherBox.getZMin()))) return false;
			else if(zInc > otherBox.zInc && !StsMath.isIntegral(otherBox.getSliceCoor(xMin))) return false;
		}
		if(initializedT() && otherBox.initializedT())
		{
			if(!StsMath.isIntegralRatio(tInc, otherBox.tInc)) return false;
			if(tInc < otherBox.tInc && !StsMath.isIntegral(getSliceCoor(otherBox.tMin))) return false;
			else if(tInc > otherBox.tInc && !StsMath.isIntegral(otherBox.getSliceCoor(tMin))) return false;
		}
		return true;
	}
    public boolean sameAs(StsRotatedGridBoundingBox otherBox)
    {
        return sameAs(otherBox, true);
    }

    public boolean xyGridSameAs(StsRotatedGridBoundingBox otherBox)
    {
        if (nRows != otherBox.nRows) return false;
        if (nCols != otherBox.nCols) return false;
        return super.sameAs(otherBox, false);
    }

	public boolean isCursorGridSameSize(int nCursorlRows, int nCursorCols, int dir)
    {
        switch (dir)
        {
            case StsCursor3d.XDIR:
                if (nCursorlRows != nRows) return false;
                if (nCursorCols != nSlices) return false;
                break;
            case StsCursor3d.YDIR:
                if (nCursorlRows != nCols) return false;
                if (nCursorCols != nSlices) return false;
                break;
            case StsCursor3d.ZDIR:
                if (nCursorlRows != nRows) return false;
                if (nCursorCols != nCols) return false;
                break;
        }
		return true;
    }

    public boolean sameAs(StsRotatedGridBoundingBox otherBox, boolean checkZ)
    {
        if (nRows != otherBox.nRows) return false;
        if (nCols != otherBox.nCols) return false;
        return super.sameAs(otherBox, checkZ);
    }

    public int getNRows() { return nRows; }

    public int getNCols() { return nCols; }

    public int getNSlices() { return nSlices; }

    public int getRowMin() { return 0; }

    public int getRowMax() { return nRows - 1; }

    public int getColMin() { return 0; }

    public int getColMax() { return nCols - 1; }

    public int getSliceMin() { return 0; }

    public int getSliceMax() { return nSlices - 1; }

    public float getXInc() { return xInc; }

    public double getEndX() { return getXOrigin() + (xInc * (xMax - xMin)); }

	public void setXInc(float xInc)
	{
		if(this.xInc > 0.0f && this.xInc < xInc) return;
		this.xInc = xInc;
		if(isPersistent()) dbFieldChanged("xInc", xInc);
	}

	public void setYInc(float yInc)
	{
		if(this.yInc > 0.0f && this.yInc < yInc) return;
		this.yInc = yInc;
		if(isPersistent()) dbFieldChanged("yInc", yInc);
	}

    public float getYInc() { return yInc; }

    public double getEndY() { return getYOrigin() + (yInc * (yMax - yMin)); }

    public float getZInc() { return zInc; }

     public float getZTInc()
     {
         if(isDepth)
            return zInc;
         else
            return tInc;
     }

    public float getZInc(int row) { return zInc; }

    public float getGridCellVolume() { return xInc*yInc*zInc; }

    public void setZInc(float zInc)
	{
		if(this.zInc > 0.0f && this.zInc < zInc) return;
		this.zInc = zInc;
		dbFieldChanged("zInc", zInc);
	}

    public float getDataMin() { return dataMin; }

    public void setDataMin(float dataMin) { this.dataMin = dataMin; }

    public float getDataMax() { return dataMax; }

    public void setDataMax(float dataMax) { this.dataMax = dataMax; }

    public float getDataAvg() { return dataAvg; }

    public void setDataAvg(float dataAvg) { this.dataAvg = dataAvg; }

    public float[] getDataRange() { return new float[]{dataMin, dataMax}; }

    public float getRowNumInc() { return rowNumInc; }

    public void setRowNumInc(float rowNumInc) { this.rowNumInc = rowNumInc; }

    public float getRowNumMin() { return rowNumMin; }

    public float getRowNumMax() { return rowNumMax; }

    public float getColNumInc() { return colNumInc; }

    public void setColNumInc(float colNumInc) { this.colNumInc = colNumInc; }

    public float getColNumMin() { return colNumMin; }

    public float getColNumMax() { return colNumMax; }

    public boolean rowColLabelsSet() { return rowNumMin != StsParameters.nullValue; }

    public void adjustRange()
    {
        nRows = 1 + Math.round((yMax - yMin) / yInc);
        yMax = yMin + (nRows - 1) * yInc;
        nCols = 1 + Math.round((xMax - xMin) / xInc);
        xMax = xMin + (nCols - 1) * xInc;
    }

    public void moveOriginToLL()
    {
        float temp = yMin;
        yMin = -yMax;
        yMax = -temp;
        temp = rowNumMin;
        rowNumMin = rowNumMax;
        rowNumMax = temp;
        rowNumInc = -rowNumInc;
    }

    /*
        public void setRowMax(int rowMax)
        {
            this.rowMax = rowMax;
            nRows = rowMax - rowMin + 1;
            rowRangeOK = nRows >= 0;
        }

        public void setRowMin(int rowMin)
        {
            this.rowMin = rowMin;
            nRows = rowMax - rowMin + 1;
            rowRangeOK = nRows >= 0;
        }

        public void setColMax(int colMax)
        {
            this.colMax = colMax;
            nCols = colMax - colMin + 1;
            colRangeOK = nCols >= 0;
        }

        public void setColMin(int colMin)
        {
            this.colMin = colMin;
            nCols = colMax - colMin + 1;
            colRangeOK = nCols >= 0;
        }

        public void setSliceMax(int sliceMax)
        {
            this.sliceMax = sliceMax;
            nCroppedSlices = sliceMax - sliceMin + 1;
            sliceRangeOK = nCroppedSlices >= 0;
        }

        public void setSliceMin(int sliceMin)
        {
            this.sliceMin = sliceMin;
            nCroppedSlices = sliceMax - sliceMin + 1;
            sliceRangeOK = nCroppedSlices >= 0;
        }
    */
    public void setNCols(int nCols)
    { this.nCols = nCols; }

    public void setNRows(int nRows) { this.nRows = nRows; }

    public void setNSlices(int nSlices) { this.nSlices = nSlices; }

    public void setRowNumMin(float rowNumMin)
    {
		if(this.rowNumMin == rowNumMin) return;
		this.rowNumMin = rowNumMin;
        dbFieldChanged("rowNumMin", rowNumMin);
    }

    public void setRowNumMax(float rowNumMax)
    {
		if(this.rowNumMax == rowNumMax) return;
		this.rowNumMax = rowNumMax;
        dbFieldChanged("rowNumMax", rowNumMax);
    }

    public void setColNumMin(float colNumMin)
    {
		if(this.colNumMin == colNumMin) return;
		this.colNumMin = colNumMin;
        dbFieldChanged("colNumMin", colNumMin);
    }

    public void setColNumMax(float colNumMax)
    {
		if(this.colNumMax == colNumMax) return;
		this.colNumMax = colNumMax;
        dbFieldChanged("colNumMax", colNumMax);
    }

    /** get row coordinate from local y coordinate */
    public float getRowCoor(float y)
    {
        if (nRows == 0 || yInc == 0.0) return 0.0f;
        return (y - yMin) / yInc;
    }

    /** get col coordinate from local x coordinate */
    public float getColCoor(float x)
    {
        if (nCols == 0 || xInc == 0.0) return 0.0f;
        return (x - xMin) / xInc;
    }

    /** get slice coordinate from local z coordinate */
    public float getSliceCoor(float zt)
    {
		if(isDepth) return (zt - getZMin()) / zInc;
		else		return (zt - tMin) / tInc;
	}

    /** get row coordinate from local y coordinate limited to row range */
    public float getBoundedRowCoor(float y)
    {
        if (nRows == 0) return 0.0f;
        return StsMath.minMax((y - yMin) / yInc, 0.0f, (float) (nRows - 1));
    }

    /** get col coordinate from local x coordinate limited to col range */
    public float getBoundedColCoor(float x)
    {
        if (nCols == 0) return 0.0f;
        return StsMath.minMax((x - xMin) / xInc, 0.0f, (float) (nCols - 1));
    }

    /** get slice coordinate from local z coordinate limited to slice range */
    public float getBoundedSliceCoor(float zt)
    {
		if(isDepth) return StsMath.minMax((zt - getZMin()) / zInc, 0.0f, (float) (nSlices - 1));
		else 		return StsMath.minMax((zt - tMin) / tInc, 0.0f, (float) (nSlices - 1));
	}

    /** get row coordinate from local y coordinate */
    public int getNearestRowCoor(float y)
    {
        if (nRows == 0) return 0;
        int row = Math.round((y - yMin) / yInc);
        if (row < 0 || row >= nRows) return -1;
        return row;
    }

   /** get row coordinate from local y coordinate */
    public float getNearestRowCoorF(float y)
    {
        if (nRows == 0) return 0.0f;
        return (y - yMin) / yInc;
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
        if (nCols == 0) return 0;
        int col = Math.round((x - xMin) / xInc);
        if (col < 0 || col >= nCols) return -1;
        return col;
    }

    /** get col coordinate from local x coordinate */
    public float getNearestColCoorF(float x)
    {
        if (nCols == 0) return 0.0f;
        return (x - xMin) / xInc;
    }

    /** get y of row nearest input y. return nullValue if outside yMin to yMax range */
    public float getNearestColXCoor(float x)
    {
        if (x < xMin - 0.5f * xInc || x > xMax + 0.5f * xInc) return nullValue;
        return StsMath.intervalRound(x, xMin, xInc);
    }

    /** get slice coordinate from local z coordinate */
    public int getNearestSliceCoor(float zt)
    {
		if(isDepth)
			return getNearestZSliceCoor(zt);
		else
			return getNearestTSliceCoor(zt);
    }
    public int getNearestZSliceCoor(float z)
    {
        int slice = Math.round((z - getZMin()) / zInc);
        if (slice < 0 || slice >= nSlices) return -1;
        return slice;
    }
    public int getNearestTSliceCoor(float t)
    {
        int slice = Math.round((t - tMin) / tInc);
        if (slice < 0 || slice >= nSlices) return -1;
        return slice;
    }

	public float getNearestSliceCoorF(float zt)
    {
		if(isDepth) return getNearestZSliceCoorF(zt);
		else		return getNearestTSliceCoorF(zt);
	}
    /** get slice coordinate from local z coordinate */
    public float getNearestZSliceCoorF(float z)
    {
        return (z - getZMin()) / zInc;
    }
    public float getNearestTSliceCoorF(float t)
    {
        return (t - tMin) / tInc;
    }
    /** get z of slice nearest input z. return nullValue if outside zMin to zMax range */
    public float getNearestSliceZCoor(float z)
    {
        if (z < getZMin() - 0.5f * zInc || z > zMax + 0.5f * zInc) return nullValue;
        return StsMath.intervalRound(z, getZMin(), zInc);
    }

	public float getNearestSliceTCoor(float t)
	{
		if (t < tMin - 0.5f * tInc || t > tMax + 0.5f * tInc) return nullValue;
		return StsMath.intervalRound(t, tMin, tInc);
	}

    /** get integral row coordinate from local y coordinate bounded by range */
    public int getNearestBoundedRowCoor(float y)
    {
        int row = Math.round((y - yMin) / yInc);
        if (row < 0) return 0;
        if (row >= nRows) return nRows - 1;
        return row;
    }

    /** get col coordinate from local x coordinate */
    public int getNearestBoundedColCoor(float x)
    {
        int col = Math.round((x - xMin) / xInc);
        if (col < 0) return 0;
        if (col >= nCols) return nCols - 1;
        return col;
    }

	public int getNearestBoundedSliceCoor(float zt)
    {
		if(isDepth) return getNearestBoundedZSliceCoor(zt);
		else	    return getNearestBoundedTSliceCoor(zt);
	}
    /** get slice coordinate from local z coordinate */
    public int getNearestBoundedZSliceCoor(float z)
    {
        int slice = Math.round((z - getZMin()) / zInc);
        if (slice < 0) return 0;
        if (slice >= nSlices) return nSlices - 1;
        return slice;
    }

	public int getNearestBoundedTSliceCoor(float t)
	{
		int slice = Math.round((t - tMin) / tInc);
		if (slice < 0) return 0;
		if (slice >= nSlices) return nSlices - 1;
		return slice;
	 }

   /** get integral row coordinate below from local y coordinate bounded by range */
    public int getFloorBoundedRowCoor(float y)
    {
        int row = StsMath.floor((y - yMin) / yInc);
        if (row < 0) return -1;
        if (row >= nRows) return nRows - 1;
        return row;
    }

   /** get integral row coordinate below from local y coordinate bounded by range */
    public int getFloorRowCoor(float y)
    {
        int row = StsMath.floor((y - yMin) / yInc);
        if (row < 0 || row >= nRows) return -1;
        return row;
    }

    /** get integral row coordinate below from local x coordinate bounded by range */
    public int getFloorBoundedColCoor(float x)
    {
        int col = StsMath.floor((x - xMin) / xInc);
        if (col < 0) return -1;
        if (col >= nCols) return nCols - 1;
        return col;
    }

    /** get integral row coordinate below from local x coordinate bounded by range */
    public int getFloorColCoor(float x)
    {
        int col = StsMath.floor((x - xMin) / xInc);
        if (col < 0 || col >= nCols) return - 1;
        return col;
    }

	public int getFloorBoundedSliceCoor(float zt)
    {
		if(isDepth) return getFloorBoundedZSliceCoor(zt);
		else 		return getFloorBoundedTSliceCoor(zt);
	}
    /** get integral row coordinate below from local z coordinate bounded by range */
    public int getFloorBoundedZSliceCoor(float z)
    {
        int slice = StsMath.floor((z - getZMin()) / zInc);
        if (slice < 0) return -1;
        if (slice >= nSlices) return nSlices - 1;
        return slice;
    }

    public int getFloorBoundedTSliceCoor(float t)
    {
        int slice = StsMath.floor((t - tMin) / tInc);
        if (slice < 0) return -1;
        if (slice >= nSlices) return nSlices - 1;
        return slice;
    }

	public int getBelowBoundedSliceCoor(float zt)
    {
		if(isDepth) return getBelowBoundedZSliceCoor(zt);
		else		return getBelowBoundedTSliceCoor(zt);
	}

    /** get integral row coordinate below from local z coordinate bounded by range */
    public int getBelowBoundedZSliceCoor(float z)
    {
        int slice = StsMath.below((z - getZMin()) / zInc);
        if (slice < 0) return -1;
        if (slice >= nSlices) return nSlices - 1;
        return slice;
    }

    public int getBelowBoundedTSliceCoor(float t)
    {
        int slice = StsMath.below((t - tMin) / tInc);
        if (slice < 0) return -1;
        if (slice >= nSlices) return nSlices - 1;
        return slice;
    }

	public int getFloorSliceCoor(float zt)
    {
		if(isDepth) return getFloorZSliceCoor(zt);
		else 		return getFloorTSliceCoor(zt);
	}

    /** get integral row coordinate below from local z coordinate bounded by range */
    public int getFloorZSliceCoor(float z)
    {
        int slice = StsMath.floor((z - getZMin()) / zInc);
        if (slice < 0 || slice >= nSlices) return - 1;
        return slice;
    }

	public int getFloorTSliceCoor(float t)
    {
        int slice = StsMath.floor((t - tMin) / tInc);
        if (slice < 0 || slice >= nSlices) return - 1;
        return slice;
    }
    /** get integral row coordinate below from local y coordinate bounded by range */
     public int getCeilingBoundedRowCoor(float y)
     {
         int row = StsMath.ceiling((y - yMin) / yInc);
         if (row < 0) return 0;
         if (row >= nRows) return -1;
         return row;
     }
    /** get integral row coordinate below from local y coordinate bounded by range */
     public int getCeilingRowCoor(float y)
     {
         int row = StsMath.ceiling((y - yMin) / yInc);
         if (row < 0 || row >= nRows) return -1;
         return row;
     }

    /** get integral row coordinate below from local x coordinate bounded by range */
    public int getCeilingBoundedColCoor(float x)
    {
        int col = StsMath.ceiling((x - xMin) / xInc);
        if (col < 0) return 0;
        if (col >= nCols) return -1;
        return col;
    }

    /** get integral row coordinate below from local x coordinate bounded by range */
    public int getCeilingColCoor(float x)
    {
        int col = StsMath.ceiling((x - xMin) / xInc);
        if (col < 0 || col >= nCols) return -1;
        return col;
    }

	public int getCeilingBoundedSliceCoor(float zt)
    {
		if(isDepth) return getCeilingBoundedZSliceCoor(zt);
		else  		return getCeilingBoundedTSliceCoor(zt);
	}
    /** get integral row coordinate below from local z coordinate bounded by range */
    public int getCeilingBoundedZSliceCoor(float z)
    {
        int slice = StsMath.ceiling((z - getZMin()) / zInc);
        if (slice < 0) return 0;
        if (slice >= nSlices) return -1;
        return slice;
    }

    public int getCeilingBoundedTSliceCoor(float t)
    {
        int slice = StsMath.ceiling((t - tMin) / tInc);
        if (slice < 0) return 0;
        if (slice >= nSlices) return -1;
        return slice;
    }

    public int getCeilingSliceCoor(float zt)
    {
		if(isDepth) return getCeilingZSliceCoor(zt);
		else        return getCeilingTSliceCoor(zt);
	}

    /** get integral row coordinate below from local z coordinate bounded by range */
    public int getCeilingZSliceCoor(float z)
    {
        int slice = StsMath.ceiling((z - getZMin()) / zInc);
        if (slice < 0 || slice >= nSlices) return -1;
        return slice;
    }

	public int getCeilingTSliceCoor(float t)
	{
		int slice = StsMath.ceiling((t - tMin) / tInc);
		if (slice < 0 || slice >= nSlices) return -1;
		return slice;
	}

    public int getNearestSliceCoor(boolean isDepth, float zt)
    {
        int slice;
        if(isDepth)
            slice = Math.round((zt - getZMin()) / zInc);
        else
            slice = Math.round((zt - tMin) / tInc);
        if(slice < 0 || slice >= nSlices) return -1;
        return slice;
    }
    /** get local X coordinate (float) from col int value */
    public float getXCoor(int col)
    {
        col = StsMath.minMax(col, 0, nCols - 1);
        return xMin + col * xInc;
    }

    /** get local Y coordinate (float) from row int value */
    public float getYCoor(int row)
    {
        row = StsMath.minMax(row, 0, nRows - 1);
        return yMin + row * yInc;
    }

    public float[] getXYCoors(int row, int col)
    {
        return new float[]{getXCoor(col), getYCoor(row)};
    }

    public void getXYCoors(int row, int col, float[] coors)
    {
        coors[0] = getXCoor(col);
        coors[1] = getYCoor(row);
    }

    /** get local Z coordinate (float) from slice int value */
    public float getZCoor(int slice)
    {
//        sliceF = StsMath.minMax(sliceF, 0.0f, (float)(nCroppedSlices-1));
        float z = (float) (getZMin() + slice * zInc);
        return StsMath.minMax(z, getZMin(), zMax);
    }

    /** get local X coordinate (float) from col float value */
    public float getXCoor(float colF)
    {
        colF = StsMath.minMax(colF, 0.0f, (float) (nCols - 1));
        return colF * xInc + xMin;
    }

	public float getXCoor(double colF)
    {
		return getXCoor((float)colF);
	}
    /** get local Y coordinate (float) from row float value */
    public float getYCoor(float rowF)
    {
        rowF = StsMath.minMax(rowF, 0.0f, (float) (nRows - 1));
        return rowF * yInc + yMin;
    }

 	public float getYCoor(double rowF)
    {
        return getYCoor((float)rowF);
    }
    /** get local Z coordinate (float) from slice float value */
    public float getZCoor(float sliceF)
    {
//        sliceF = StsMath.minMax(sliceF, 0.0f, (float)(nCroppedSlices-1));
        float z = (float) (getZMin() + sliceF * zInc);
        return StsMath.minMax(z, getZMin(), zMax);
    }

	public float getTCoor(float sliceF)
	{
//        sliceF = StsMath.minMax(sliceF, 0.0f, (float)(nCroppedSlices-1));
		float t = (float) (tMin + sliceF * tInc);
		return StsMath.minMax(t, tMin, tMax);
	}

	public float getZTCoor(float sliceF)
	{
		if(isDepth) return getZCoor(sliceF);
		else 		return getTCoor(sliceF);
	}

	public float getZTCoor(int slice)
	{
		if(isDepth) return getZCoor(slice);
		else 		return getTCoor(slice);
	}

	public float getZTCoor(double sliceF)
	{
		return getZTCoor((float)sliceF);
	}

    /** get local Y coordinate (float) from row number */
    public float getYFromRowNum(float rowNum)
    {
        return yMin + yInc * (rowNum - rowNumMin) / rowNumInc;
    }

    /** round this rowNum to the nearest one */
    public float getNearestColNum(float colNum)
    {
        int col = Math.round((colNum - colNumMin) / colNumInc);
        return colNumMin + colNumInc * col;
    }

    /** round this rowNum to the nearest one */
    public float getNearestRowNum(float rowNum)
    {
        int row = Math.round((rowNum - rowNumMin) / rowNumInc);
        return rowNumMin + rowNumInc * row;
    }

    /** get local X coordinate (float) from col number */
    public float getXFromColNum(float colNum)
    {
        return xMin + xInc * (colNum - colNumMin) / colNumInc;
    }

    /** required for Gridable compatability */
    public float getXCoor(float rowF, float colF)
    {
        return getXCoor(colF);
    }

	public final float getXCoorFromNormF(float normF)
	{
		return xMin + normF*(xMax - xMin);
	}

	public final float getYCoorFromNormF(float normF)
	{
		return yMin + normF*(yMax - yMin);
	}

	public final float getZCoorFromNormF(float normF)
	{
		return zMin + normF*(zMax - zMin);
	}

	public final float getTCoorFromNormF(float normF)
	{
		return tMin + normF*(tMax - tMin);
	}

	public final float getZTCoorFromNormF(float normF)
	{
		if(isDepth) return getZCoorFromNormF(normF);
		else		return getTCoorFromNormF(normF);
	}

    /** required for Gridable compatability */
    public float getYCoor(float rowF, float colF)
    {
        return getYCoor(rowF);
    }

    public float getXCoor(int row, int col)
    {
        return getXCoor(col);
    }

    /** required for Gridable compatability */
    public float getYCoor(int row, int col)
    {
        return getYCoor(row);
    }

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

    /** get row, col, or slice numbering given local coordinate on that axis */
    public float getNumFromCoor(int dir, float coor)
    {
        float number = 0.0f;

        switch (dir)
        {
            case StsCursor3d.XDIR:
                number = colNumMin + getColCoor(coor) * colNumInc;
//                number = colNumMin + getNearestColCoor(coor)*colNumInc;
                break;
            case StsCursor3d.YDIR:
                number = rowNumMin + getRowCoor(coor) * rowNumInc;
//                number = rowNumMin + getNearestRowCoor(coor)*rowNumInc;
                break;
            case StsCursor3d.ZDIR:
                getSliceCoor(coor);
//                number = zMin + getNearestSliceCoor(coor)*zInc;
                break;
        }
        return number;
    }

    public float getNearestNumFromCoor(int dir, float coor)
    {
        switch (dir)
        {
            case StsCursor3d.XDIR:
                return getNearestColNumFromX(coor);
            case StsCursor3d.YDIR:
                return getNearestRowNumFromY(coor);
            case StsCursor3d.ZDIR:
                return coor;
//                number = getNearestSliceCoor(coor);
            default:
                return 0.0f;
        }
    }

    public float getNearestBoundedColNumFromX(float x)
    {
        return colNumMin + getNearestBoundedColCoor(x) * colNumInc;
    }

    public float getNearestBoundedRowNumFromY(float y)
    {
        return rowNumMin + getNearestBoundedRowCoor(y) * rowNumInc;
    }

    public float getColNumFromX(float x)
    {
        return colNumMin + getColCoor(x) * colNumInc;
    }

    public float getRowNumFromY(float y)
    {
        return rowNumMin + getRowCoor(y) * rowNumInc;
    }

    public float getRowNumFromRow(int row)
    {
        return rowNumMin + row * rowNumInc;
    }

    public float getColNumFromCol(int col)
    {
        return colNumMin + col * colNumInc;
    }

    public float getNearestRowNumFromY(float y)
    {
        int minRow = Math.round(getRowCoor(y));
        return getRowNumFromRow(minRow);
    }

    public float getNearestColNumFromX(float x)
    {
        int minCol = Math.round(getColCoor(x));
        return getColNumFromCol(minCol);
    }

    /** get row, col, or slice index given number on that axis */
    public float getIndexFromNum(int dir, float number)
    {
        float index = 0.0f;

        switch (dir)
        {
            case StsCursor3d.XDIR:
                index = (number - colNumMin) / colNumInc;
                break;
            case StsCursor3d.YDIR:
                index = (number - rowNumMin) / rowNumInc;
                break;
            case StsCursor3d.ZDIR:
                index = number;
                break;
        }
        return index;
    }


    /** get row, col, or slice numbering given index on that axis */
    public float getNumFromIndex(int dir, float index)
    {
        float number = 0.0f;

        switch (dir)
        {
            case StsCursor3d.XDIR:
                number = colNumMin + index * colNumInc;
                break;
            case StsCursor3d.YDIR:
                number = rowNumMin + index * rowNumInc;
                break;
            case StsCursor3d.ZDIR:
                number = index;
                break;
        }
        return number;
    }

    public int getRowFromRowNum(float rowNum)
    {
        return Math.round((rowNum - rowNumMin) / rowNumInc);
    }

    public int getColFromColNum(float colNum)
    {
        return Math.round((colNum - colNumMin) / colNumInc);
    }

    public int getRowIncFromRowNumInc(float rowNumInc)
    {
        return Math.round(rowNumInc / this.rowNumInc);
    }

    public int getColIncFromColNumInc(float colNumInc)
    {
        return Math.round(colNumInc / this.colNumInc);
    }

    public int getSliceIncFromZInc(float zInc)
    {
        return Math.round(zInc / this.zInc);
    }

    public float getAvgInc()
    {
        return (float) Math.sqrt(xInc * yInc);
    }

    public void setZRange(float zMin, float zMax)
    {
        this.zMin = zMin;
        this.zMax = zMax;
    }

    public void resetZRange()
    {
		super.resetZRange();
        zInc = 0.0f;
    }

    public void resetTRange()
    {
		super.resetTRange();
        tInc = 0.0f;
    }

    public int getCursorRowMin(int dir)
    {
        if (dir == XDIR) return getRowMin();
        else if (dir == YDIR) return getColMin();
        else if (dir == ZDIR) return getRowMin();
        else return 0;
    }

    public int getCursorColMin(int dir)
    {
        if (dir == XDIR) return getSliceMin();
        else if (dir == YDIR) return getSliceMin();
        else if (dir == ZDIR) return getColMin();
        else return 0;
    }

    public int getCursorRowMax(int dir)
    {
        if (dir == XDIR) return getRowMax();
        else if (dir == YDIR) return getColMax();
        else if (dir == ZDIR) return getRowMax();
        else return 0;
    }

    public int getCursorColMax(int dir)
    {
        if (dir == XDIR) return getSliceMax();
        else if (dir == YDIR) return getSliceMax();
        else if (dir == ZDIR) return getColMax();
        else return 0;
    }

    public int getCursorRow(int dir, float coor)
    {
        if (dir == XDIR) return getNearestRowCoor(coor);
        else if (dir == YDIR) return getNearestColCoor(coor);
        else if (dir == ZDIR) return getNearestRowCoor(coor);
        else return 0;
    }

    public int getCursorCol(int dir, float coor)
    {
        if (dir == XDIR) return getNearestSliceCoor(coor);
        else if (dir == YDIR) return getNearestSliceCoor(coor);
        else if (dir == ZDIR) return getNearestColCoor(coor);
        else return 0;
    }

    public int getNCursorRows(int dir)
    {
        if (dir == XDIR) return nRows;
        else if (dir == YDIR) return nCols;
        else if (dir == ZDIR) return nRows;
        else return 0;
    }

    public float getCursorRowLength(int dir)
    {
        if (dir == XDIR || dir == ZDIR) return getBoxSize(YDIR);
        else if (dir == YDIR) return getBoxSize(XDIR);
        else return 0;
    }

    public int getNCursorCols(int dir)
    {
        if (dir == XDIR) return nSlices;
        else if (dir == YDIR) return nSlices;
        else if (dir == ZDIR) return nCols;
        else return 0;
    }

    public float getCursorColLength(int dir)
    {
        if (dir == XDIR || dir == YDIR) return getBoxSize(ZDIR);
        else if (dir == ZDIR) return getBoxSize(XDIR);
        else return 0;
    }

    public float getCursorRowMinCoor(int dir)
    {
        if (dir == XDIR) return yMin;
        else if (dir == YDIR) return xMin;
        else if (dir == ZDIR) return yMin;
        else return 0;
    }

    public float getCursorRowMaxCoor(int dir)
    {
        if (dir == XDIR) return yMax;
        else if (dir == YDIR) return xMax;
        else if (dir == ZDIR) return yMax;
        else return 0;
    }

    public float getCursorColMinCoor(int dir)
    {
        if (dir == XDIR) return getZTMin();
        else if (dir == YDIR) return getZTMin();
        else if (dir == ZDIR) return xMin;
        else return 0;
    }

    public float getCursorColMaxCoor(int dir)
    {
        if (dir == XDIR) return getZTMax();
        else if (dir == YDIR) return getZTMax();
        else if (dir == ZDIR) return xMax;
        else return 0;
    }

    public float getCursorNormalizedRowCoor(int dir, float[] xyz)
    {
        if (dir == XDIR) return xyz[1]/yMax;
        else if (dir == YDIR) return xyz[0]/xMax;
        else if (dir == ZDIR) return xyz[1]/yMax;
        else return 0.0f;
    }

    public float getCursorNormalizedColCoor(int dir, float[] xyz)
    {
        if (dir == XDIR) return xyz[2]/getZTMax();
        else if (dir == YDIR) return xyz[2]/getZTMax();
        else if (dir == ZDIR) return xyz[0]/xMax;
        else return 0.0f;
    }
    public int getBoxDimension(int dir)
    {
        switch (dir)
        {
            case XDIR:
                return nCols;
            case YDIR:
                return nRows;
            case ZDIR:
                return nSlices;
            default:
                return 0;
        }
    }

    public float getBoxSize(int dir)
    {
        switch (dir)
        {
            case XDIR:
                return xMax - xMin;
            case YDIR:
                return yMax - yMin;
            case ZDIR:
                return getZTSize();
            default:
                return 0;
        }
    }

    public int getSubVolumeCursorRowMin(StsRotatedGridBoundingBox subVolume, int dir)
    {
        int rowMin;

        switch (dir)
        {
            case XDIR:
            case ZDIR:
                float yMin = subVolume.getYMin();
                rowMin = getNearestRowCoor(yMin);
                break;
            case YDIR:
                float xMin = subVolume.getXMin();
                rowMin = getNearestColCoor(xMin);
                break;
            default:
                return 0;
        }
        if (rowMin == -1) return 0;
        else return rowMin;
    }

    public int getSubVolumeCursorRowMax(StsRotatedGridBoundingBox subVolume, int dir)
    {
        int rowMax;
        switch (dir)
        {
            case XDIR:
            case ZDIR:
                float yMax = subVolume.getYMax();
                rowMax = getNearestRowCoor(yMax);
                break;
            case YDIR:
                float xMax = subVolume.getXMax();
                rowMax = getNearestColCoor(xMax);
                break;
            default:
                return 0;
        }
        if (rowMax == -1) return nRows - 1;
        else return rowMax;
    }

    public int getSubVolumeCursorColMin(StsRotatedGridBoundingBox subVolume, int dir)
    {
        int colMin;
        switch (dir)
        {
            case XDIR:
            case YDIR:
                float zMin = subVolume.getZMin();
                colMin = getNearestSliceCoor(zMin);
                break;
            case ZDIR:
                float xMin = subVolume.getXMin();
                colMin = getNearestColCoor(xMin);
                break;
            default:
                return 0;
        }
        if (colMin == -1) return 0;
        else return colMin;
    }

    public int getSubVolumeCursorColMax(StsRotatedGridBoundingBox subVolume, int dir)
    {
        int colMax;
        switch (dir)
        {
            case XDIR:
            case YDIR:
                float zMax = subVolume.getZMax();
                colMax = getNearestSliceCoor(zMax);
                break;
            case ZDIR:
                float xMax = subVolume.getXMax();
                colMax = getNearestColCoor(xMax);
                break;
            default:
                return 0;
        }
        if (colMax == -1) return nCols - 1;
        else return colMax;
    }


    public int[] getRowColRange(int dir, float dirCoordinate, StsRotatedGridBoundingBox otherBox)
    {
        int[] rowColRange = new int[4];
        switch (dir)
        {
            case StsCursor3d.XDIR:

                if (dirCoordinate < otherBox.xMin || dirCoordinate > otherBox.xMax) return null;
                rowColRange[0] = StsMath.ceiling(getBoundedRowCoor(otherBox.yMin));
                rowColRange[1] = StsMath.floor(getBoundedRowCoor(otherBox.yMax));
                rowColRange[2] = StsMath.ceiling(getBoundedSliceCoor(otherBox.getZTMin()));
                rowColRange[3] = StsMath.floor(getBoundedSliceCoor(otherBox.getZTMax()));
                break;
            case StsCursor3d.YDIR:
                if (dirCoordinate < otherBox.yMin || dirCoordinate > otherBox.yMax) return null;
                rowColRange[0] = StsMath.ceiling(getBoundedColCoor(otherBox.xMin));
                rowColRange[1] = StsMath.floor(getBoundedColCoor(otherBox.xMax));
                rowColRange[2] = StsMath.ceiling(getBoundedSliceCoor(otherBox.getZTMin()));
                rowColRange[3] = StsMath.floor(getBoundedSliceCoor(otherBox.getZTMax()));
                break;
            case StsCursor3d.ZDIR:
                if (dirCoordinate < otherBox.getZTMin() || dirCoordinate > otherBox.getZTMax()) return null;
                rowColRange[0] = StsMath.ceiling(getBoundedRowCoor(otherBox.yMin));
                rowColRange[1] = StsMath.floor(getBoundedRowCoor(otherBox.yMax));
                rowColRange[2] = StsMath.ceiling(getBoundedColCoor(otherBox.xMin));
                rowColRange[3] = StsMath.floor(getBoundedColCoor(otherBox.xMax));
        }
        return rowColRange;
    }


    public int getCursorPlaneIndex(int dirNo, float dirCoordinate)
    {
        switch (dirNo)
        {
            case StsCursor3d.XDIR: // crossline direction
                float x = dirCoordinate;
//                if(x < xMin || x > xMax) return -1;
                int col = getNearestColCoor(x);
                if (col < 0 || col >= nCols) return -1;
                return col;
            case StsCursor3d.YDIR: // inline direction
                float y = dirCoordinate;
//                if(y < yMin || y > yMax) return -1;
                int row = getNearestRowCoor(y);
                if (row < 0 || row >= nRows) return -1;
                return row;
            case StsCursor3d.ZDIR:
                float z = dirCoordinate;
//                if(z < zMin || z > zMax) return -1;
                int slice = getNearestSliceCoor(z);
                if (slice < 0 || slice >= this.nSlices) return -1;
                return slice;
            default:
                return -1;
        }
    }

    /**
     * For the 3 cursor directions, get the coordinates on each cursor plane
     * translated to equivalent grid coordinates.  The XDIR plane is in directions y+,z+
     * The YDIR plane is in directions x+,z+ and the ZDIR plane is in directions x+,y+
     */
    public float[][] getGridCoordinateRanges(float[][] axisRanges, int currentDirNo, boolean axesFlipped)
    {
        float[][] coordinateRanges = new float[2][2];

        if (currentDirNo == StsCursor3d.XDIR)
        {
            coordinateRanges[0][0] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[0][0]);
            coordinateRanges[0][1] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[0][1]);
            coordinateRanges[1][0] = axisRanges[1][0]; // zMax
            coordinateRanges[1][1] = axisRanges[1][1]; // zMin
        }
        else if (currentDirNo == StsCursor3d.YDIR)
        {
            coordinateRanges[0][0] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[0][0]);
            coordinateRanges[0][1] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[0][1]);
            coordinateRanges[1][0] = axisRanges[1][0]; // zMax
            coordinateRanges[1][1] = axisRanges[1][1]; // zMin
        }
        else if (currentDirNo == StsCursor3d.ZDIR)
        {
            if (!axesFlipped)
            {
                coordinateRanges[1][0] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[1][0]);
                coordinateRanges[1][1] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[1][1]);
                coordinateRanges[0][0] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[0][0]);
                coordinateRanges[0][1] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[0][1]);
            }
            else
            {
                coordinateRanges[1][0] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[1][0]);
                coordinateRanges[1][1] = getNumFromCoor(StsCursor3d.XDIR, axisRanges[1][1]);
                coordinateRanges[0][0] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[0][0]);
                coordinateRanges[0][1] = getNumFromCoor(StsCursor3d.YDIR, axisRanges[0][1]);
            }
        }
        return coordinateRanges;
    }

    public int getPosition(int row, int col, int slice)
    {
        return (slice * nRows + row) * nCols + col;
    }

    public float[] getCursorPointFromScaledPoint(float[] cursorScaledPoint, int dirNo, float dirCoordinate)
    {
        float[] xyz = new float[3];

        switch (dirNo)
        {
            case XDIR:
                xyz[0] = dirCoordinate;
                xyz[1] = yMin + cursorScaledPoint[0] * (yMax - yMin);
                xyz[2] = getZTMin() + cursorScaledPoint[1] * getZTSize();
                break;
            case YDIR:
                xyz[1] = dirCoordinate;
                xyz[0] = xMin + cursorScaledPoint[0] * (xMax - xMin);
                xyz[2] = getZTMin() + cursorScaledPoint[1] * getZTSize();
                break;
            case ZDIR:
                xyz[2] = dirCoordinate;
                xyz[0] = xMin + cursorScaledPoint[0] * (xMax - xMin);
                xyz[1] = yMin + cursorScaledPoint[1] * (yMax - yMin);
        }
        return xyz;
    }

    public float[] getCursorPntNrmlFromScaledPoint(float[] cursorScaledPoint, int dirNo, float dirCoordinate)
    {
        float[] xyz = new float[6];

        switch (dirNo)
        {
            case XDIR:
                xyz[0] = dirCoordinate;
                xyz[1] = yMin + cursorScaledPoint[0] * (yMax - yMin);
                xyz[2] = getZTMin() + cursorScaledPoint[1] * getZTSize();
                xyz[3] = -1.0f;
                xyz[4] = 0.0f;
                xyz[5] = 0.0f;
                break;
            case YDIR:
                xyz[1] = dirCoordinate;
                xyz[0] = xMin + cursorScaledPoint[0] * (xMax - xMin);
                xyz[2] = getZTMin() + cursorScaledPoint[1] * getZTSize();
                xyz[3] = 0.0f;
                xyz[4] = -1.0f;
                xyz[5] = 0.0f;
                break;
            case ZDIR:
                xyz[2] = dirCoordinate;
                xyz[0] = xMin + cursorScaledPoint[0] * (xMax - xMin);
                xyz[1] = yMin + cursorScaledPoint[1] * (yMax - yMin);
                xyz[3] = 0.0f;
                xyz[4] = 0.0f;
                xyz[5] = -1.0f;
        }
        return xyz;
    }

    /**
     * Data on cursor is organized in rows and columns.  For an X-plane, data
     * increases down the Z axis and across the Y-axis so rows are from zMin to zMax
     * and columns are from yMin to yMax, for example.
     */
    public float[][] getCursorDataRange(int dir)
    {
        switch (dir)
        {
            case XDIR:
                return new float[][]{{yMin, yMax}, { getZTMax(), getZTMin() }};
            case YDIR:
                return new float[][]{{xMin, xMax}, { getZTMax(), getZTMin() }};
            case ZDIR:
                return new float[][]{{xMin, xMax}, {yMin, yMax}};
            default:
                return null;
        }
    }

    /**
     * Map 2d coordinates displayed on a cursor section to coordinates in which
     * data is actually organized.
     */
    static public int[] getCursor2dCoorDataIndexes(int dir, boolean axesFlipped)
    {
        switch (dir)
        {
            case XDIR:
                return new int[]{1, 0};
            case YDIR:
                return new int[]{1, 0};
            case ZDIR:
                if (!axesFlipped)
                {
                    return new int[]{0, 1};
                }
                else
                {
                    return new int[]{1, 0};
                }
            default:
                return null;
        }
    }

    /**
     * Given that point coordinates are X, Y, and Z, return Z,Y for X-plane,
     * Z,X for Y-plane, and X,Y for Z-plane.
     */
    public int[] getCursorCoorDisplayIndexes(int dir, boolean axesFlipped)
    {
        switch (dir)
        {
            case XDIR:
                return new int[]
                    {
                        YDIR, ZDIR};
            case YDIR:
                return new int[]
                    {
                        XDIR, ZDIR};
            case ZDIR:

                if (!axesFlipped)
                {
                    return new int[]
                        {
                            XDIR, YDIR};
                }
                else
                {
                    return new int[]
                        {
                            YDIR, XDIR};
                }
            default:
                return null;
        }
    }

    public float getScaledValue(byte byteValue)
    {
        if (byteValue == StsParameters.nullByte) return nullValue;
        float f = (float) StsMath.signedByteToUnsignedInt(byteValue);
        return dataMin + (f / 254) * (dataMax - dataMin);
    }

    public float getFloatFromSignedByte(byte byteValue)
    {
        int signedInt = (int) byteValue + 128;
        if (signedInt == 255) return nullValue;
        return dataMin + (signedInt / 254.0f) * (dataMax - dataMin);
    }

    public byte getByteValue(double value)
    {
        return StsMath.unsignedIntToUnsignedByte((int) (254 * (value - dataMin) / (dataMax - dataMin)));
    }

    public float getByteScale() { return dataMin + (1.0f / 254) * (dataMax - dataMin); }


    public boolean scaleFloatsToBytes(float[] floats, byte[] bytes)
    {
        return StsMath.floatsToUnsignedBytes254(floats, dataMin, dataMax, bytes);
    }

    public void resetDataMinMax()
    {
        dataMin = StsParameters.largeFloat;
        dataMax = -StsParameters.largeFloat;
    }
/*
    public void resetDataMinMaxByFormat(int sampleFormat)
    {
        switch (sampleFormat)
        {
            case StsSEGYFormat.BYTE: // 8 bit integer
                dataMin = Byte.MAX_VALUE;
                dataMax = Byte.MIN_VALUE;
                break;
            case StsSEGYFormat.INT2: // 2 byte integer
                dataMin = Short.MAX_VALUE;
                dataMax = Short.MIN_VALUE;
                break;
            case StsSEGYFormat.INT4: // 4 byte integer
                dataMin = Integer.MAX_VALUE;
                dataMax = Integer.MIN_VALUE;
                break;
            case StsSEGYFormat.IBMFLT: // 4 byte IBM floating point
            case StsSEGYFormat.IEEEFLT: // 4 byte IEEE Float
            case StsSEGYFormat.FLOAT8: // 1 byte Integer with Scalar to get Float
            case StsSEGYFormat.FLOAT16: // 2 byte integer with Scalar to get Float
                dataMin = Float.MAX_VALUE;
                dataMax = -dataMin;
                break;
        }
    }

    public void setDataMinByFormat(int sampleFormat)
    {
        switch (sampleFormat)
        {
            case StsSEGYFormat.BYTE: // 8 bit integer
                dataMin = Byte.MIN_VALUE;
                break;
            case StsSEGYFormat.INT2: // 2 byte integer
                dataMin = Short.MIN_VALUE;
                break;
            case StsSEGYFormat.INT4: // 4 byte integer
                dataMin = Integer.MIN_VALUE;
                break;
            case StsSEGYFormat.IBMFLT: // 4 byte IBM floating point
                // FALL_THROUGH
            case StsSEGYFormat.IEEEFLT: // 4 byte IEEE Float
            case StsSEGYFormat.FLOAT8: // 1 byte int with Scalar to get float
            case StsSEGYFormat.FLOAT16: // 2 byte integer with Scalar to get Float
                dataMin = -Float.MAX_VALUE;
                break;
        }
    }

    public void setDataMaxByFormat(int sampleFormat)
    {
        switch (sampleFormat)
        {
            case StsSEGYFormat.BYTE: // 8 bit integer
                dataMax = Byte.MAX_VALUE;
                break;
            case StsSEGYFormat.INT2: // 2 byte integer
                dataMax = Short.MAX_VALUE;
                break;
            case StsSEGYFormat.INT4: // 4 byte integer
                dataMax = Integer.MAX_VALUE;
                break;
            case StsSEGYFormat.IBMFLT: // 4 byte IBM floating point
            case StsSEGYFormat.IEEEFLT: // 4 byte IEEE Float
            case StsSEGYFormat.FLOAT8: // 1 byte Integer with Scalar to get Float
            case StsSEGYFormat.FLOAT16: // 2 byte integer with Scalar to get Float
                dataMax = Float.MAX_VALUE;
                break;
        }
    }
*/
    public void adjustBoundingBox(int maxIntervals)
    {
        float dx = xMax - xMin;
        float dy = yMax - yMin;
        int nXintervals, nYintervals;
        if (dx > dy)
        {
            nXintervals = maxIntervals;
            nYintervals = Math.round(dy * maxIntervals / dx);
        }
        else
        {
            nYintervals = maxIntervals;
            nXintervals = Math.round(dx * maxIntervals / dy);
        }
        double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, nXintervals, true);
        xMin = (float) (xScale[0] - xOrigin);
        xMax = (float) (xScale[1] - xOrigin);
        xInc = (float) xScale[2];
        nCols = 1 + Math.round((xMax - xMin) / xInc);
        xMax = xMin + (nCols - 1) * xInc;
        double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, nYintervals, true);
        yMin = (float) (yScale[0] - yOrigin);
        yMax = (float) (yScale[1] - yOrigin);
        yInc = (float) yScale[2];
        nRows = 1 + Math.round((yMax - yMin) / yInc);
        yMax = yMin + (nRows - 1) * yInc;
    }

	public void niceAdjustZTScale(int approxNumIncs)
	{
		if(initializedZ() && zInc == 0.0f)
		{
			float[] scale = StsMath.niceScale(zMin, zMax, approxNumIncs, true);
			zMin = scale[0];
			zMax = scale[1];
			zInc = scale[2];
		}
		if(initializedT() && tInc == 0.0f)
		{
			float[] scale = StsMath.niceScale(tMin, tMax, approxNumIncs, true);
			tMin = scale[0];
			tMax = scale[1];
			tInc = scale[2];
		}
	}
	/** adjust this origin to that of the boundingBox argument origin.
	 *  Adjust the x and y arranges as well if they have been initialized.
	 * @param boundingBox the boundingBox whose origin is to be used.
	 */
	public boolean adjustOrigin(StsRotatedBoundingBox boundingBox)
	{
		if(boundingBox == null || !boundingBox.originSet) return false;
		float[] xy = getRelativeXY(boundingBox.xOrigin, boundingBox.yOrigin);
		if(initializedXY())
		{
			xMin -= xy[0];
			xMax -= xy[0];
			yMin -= xy[1];
			yMax -= xy[1];
		}
		xOrigin = boundingBox.xOrigin;
		yOrigin = boundingBox.yOrigin;
		return true;
	}

    public int adjustLimitRow(int row)
    {
        return StsMath.minMax(row, 0, nRows - 1);
    }

    public int adjustLimitCol(int col)
    {
        return StsMath.minMax(col, 0, nCols - 1);
    }

    public void intersectBoundingBox(StsRotatedGridBoundingBox box)
    {
        super.intersectBoundingBox(box);
        nRows = (int) (getYSize() / xInc) + 1;
        nCols = (int) (getXSize() / xInc) + 1;

        rowNumMin = Math.max(rowNumMin, box.rowNumMin);
        rowNumMax = Math.min(rowNumMax, box.rowNumMax);
        colNumMin = Math.max(colNumMin, box.colNumMin);
        colNumMax = Math.min(colNumMax, box.colNumMax);
    }

    public Iterator getQuadCellIterator()
    {
        return new QuadCellIterator();

    }

    public void setTInc(float tInc)
    {
		if(this.tInc > 0.0f && this.tInc < tInc) return;
        this.tInc = tInc;
		dbFieldChanged("tInc", tInc);
    }

    /** Iterate a quadTree in which we divide row and cols by 2 and return the center of each cell as generated */
    class QuadCellIterator implements Iterator
    {
        int rowCellSize = 2 * (nRows - 1);
        int colCellSize = 2 * (nCols - 1);
        int row = nRows - 1;
        int col = nCols - 1;

        QuadCellIterator()
        {

        }

        public boolean hasNext()
        {
            col += colCellSize;
            if (col < nCols) return true;
            row += rowCellSize;
            if (row < nRows)
            {
                col = colCellSize / 2;
                return true;
            }
            if (rowCellSize <= 1 && colCellSize <= 1) return false;
            if (rowCellSize > 1) rowCellSize /= 2;
            if (colCellSize > 1) colCellSize /= 2;

            row = rowCellSize / 2;
            col = colCellSize / 2;

            return true;
        }

        public Object next()
        {
            return new int[]{row, col};
        }

        public void remove()
        {
        }
    }

    public StsRotatedGridBoundingBox createCenteredGrid()
    {
        float xMin = this.xMin + xInc/2;
        float yMin = this.yMin + yInc/2;
        int nRows = this.nRows - 1;
        int nCols = this.nCols - 1;
        return new StsRotatedGridBoundingBox(nRows, nCols, xOrigin, yOrigin, xMin, yMin, xInc, yInc, false);
    }

    public static void main(String[] args)
    {
        StsRotatedGridBoundingBox boundingBox = new StsRotatedGridBoundingBox(101, 51, 0.0, 0.0, 0.0f, 0.0f, 1.0f, 1.0f, false);
        Iterator iterator = boundingBox.getQuadCellIterator();
        while (iterator.hasNext())
        {
            int[] next = (int[]) iterator.next();
            System.out.println("row: " + next[0] + " col: " + next[1]);
        }
    }
}
