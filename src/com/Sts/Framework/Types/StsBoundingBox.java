//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Types;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Utilities.*;

import javax.media.opengl.*;
import java.io.*;

public class StsBoundingBox extends StsMainObject implements Cloneable, Serializable
{
    public float xMin = largeFloat;
    public float yMin = largeFloat;
    public float zMin = largeFloat;
    public float tMin = largeFloat;
    public float xMax = -largeFloat;
    public float yMax = -largeFloat;
    public float zMax = -largeFloat;
    public float tMax = -largeFloat;
    public byte zDomainByte = TD_NONE;

    public double xOrigin, yOrigin;
    public boolean originSet = false;

    // transient public boolean initialized = false;
    // transient public boolean initializedXY = false;
    //transient public boolean initializedZ = false;
    //transient public boolean initializedT = false;

    static public final float nullValue = StsParameters.nullValue;
    static public final float largeFloat = StsParameters.largeFloat;

    static final long serialVersionUID = -5962170170363197056L;

    static public final int XMIN = 0;
    static public final int XMAX = 1;
    static public final int YMIN = 2;
    static public final int YMAX = 3;
    static public final int ZMIN = 4;
    static public final int ZMAX = 5;
    static public final int CENTER = -1;
    static public final int NONE = -99;

    static public final byte TD_NONE = StsParameters.TD_NONE;
    static public final byte TD_DEPTH = StsParameters.TD_DEPTH;
    static public final byte TD_TIME = StsParameters.TD_TIME;

    public StsBoundingBox()
    {
    }

    /** Rotated Bounding Box constructTraceAnalyzer allowing non-persistent construction */
    public StsBoundingBox(boolean persistent)
    {
        super(persistent);
    }

    public StsBoundingBox(boolean persistent, String name)
    {
        super(persistent, name);
    }

    public StsBoundingBox(float xMin, float xMax, float yMin, float yMax)
    {
        xOrigin = 0.0;
        yOrigin = 0.0;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public StsBoundingBox(StsXYSurfaceGridable grid)
    {
        xOrigin = grid.getXOrigin();
        yOrigin = grid.getYOrigin();
        xMin = 0.0f;
        yMin = 0.0f;
        xMax = grid.getXSize(); // only used if unrotated
        yMax = grid.getYSize(); // only used if unrotated
        zMin = grid.getZMin();
        zMax = grid.getZMax();
    }

    public StsBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
    {
        initialize(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    public StsBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, boolean persistent)
    {
        super(persistent);
        initialize(xMin, xMax, yMin, yMax, zMin, zMax);
    }

    public StsBoundingBox(double xOrigin, double yOrigin, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, float tMin, float tMax)
    {
        setOrigin(xOrigin, yOrigin);
        initialize(xMin, xMax, yMin, yMax, zMin, zMax, tMin, tMax);
    }

    public StsBoundingBox(double xOrigin, double yOrigin)
    {
        super(false);
        setOrigin(xOrigin, yOrigin);
    }

    public StsBoundingBox(StsPoint[] points, double xOrigin, double yOrigin)
    {
        addPoints(points, xOrigin, yOrigin);
    }

    public StsBoundingBox(StsPoint[] points)
    {
        addPoints(points);
    }

    public StsBoundingBox getClone()
    {
        try
        {
            return (StsBoundingBox) this.clone();
        }
        catch(Exception e)
        {
            StsException.outputException("StsBoundingBox.getClone(grid) failed.", e, StsException.WARNING);
            return null;
        }
    }

    public void initialize(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
    {
        initialize(xMin, xMax, yMin, yMax, zMin, zMax, true);
    }

    public void initialize(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, boolean initialized)
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }

    public void initialize(StsBoundingBox box, boolean initialized)
    {
		this.xOrigin = box.xOrigin;
		this.yOrigin = box.yOrigin;
        this.xMin = box.xMin;
        this.xMax = box.xMax;
        this.yMin = box.yMin;
        this.yMax = box.yMax;
        // this.initialized = initialized;
        if(isDepth)
        {
            this.zMin = box.zMin;
            this.zMax = box.zMax;
        }
        else
        {
            this.tMin = this.getZMin();
            this.tMax = box.zMax;
        }
    }

    public void initialize(float[] xyzMinMax, boolean initialized)
    {
		xOrigin = 0.0;
		yOrigin = 0.0;
        this.xMin = xyzMinMax[0];
        this.xMax = xyzMinMax[1];
        this.yMin = xyzMinMax[2];
        this.yMax = xyzMinMax[3];
        if(isDepth)
        {
            this.zMin = xyzMinMax[4];
            this.zMax = xyzMinMax[5];
            this.zDomainByte = TD_DEPTH;
        }
        else
        {
            this.tMin = xyzMinMax[4];
            this.tMax = xyzMinMax[5];
            this.zDomainByte = TD_TIME;
        }
    }

    public void initialize(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, float tMin, float tMax)
    {
        initialize(xMin, xMax, yMin, yMax, zMin, zMax, true);
        this.tMin = tMin;
        this.tMax = tMax;
        setDomain();
    }

    public void setDomain()
    {
        if(isDepth)
            zDomainByte = TD_DEPTH;
        else
            zDomainByte = TD_TIME;
    }

    public boolean initialize(StsModel model)
    {
        return true;
    }

   //  public boolean initialized() { return initialized; }
    public boolean initializedXY() { return xMin != largeFloat; }
    public boolean initializedZ() { return zMin != largeFloat; }
    public boolean initializedT() { return tMin != largeFloat; }

    /** Initialize the origin from this boundingBox if it has been set. */
    public void checkSetOrigin(StsBoundingBox boundingBox)
    {
        if(originSet || !boundingBox.originSet) return;
        xOrigin = boundingBox.xOrigin;
        yOrigin = boundingBox.yOrigin;
        originSet = true;
        // this.reinitializeBoundingBox();
    }

    /**
     * classInitialize the boundingBox with a new origin and reset the limits.
     * This occurs when the first object generated boundingBox is added to the project.
     */
    public void initializeOriginRange(double xOrigin, double yOrigin)
    {
        initializeOrigin(xOrigin, yOrigin);
        resetRanges();
    }

    public void initializeOrigin(double xOrigin, double yOrigin)
    {
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        originSet = true;
    }

    public void reinitializeBoundingBox()
    {
        originSet = false;
        resetRanges();
    }

	public void resetRanges()
	{
		resetXYRange();
        resetZRange();
        resetTRange();
	}

    public void resetXYRange()
    {
        xMin = largeFloat;
        yMin = largeFloat;
        xMax = -largeFloat;
        yMax = -largeFloat;
    }

    public void resetZRange()
    {
        zMin = largeFloat;
        zMax = -largeFloat;
    }

    public void resetTRange()
    {
        tMin = largeFloat;
        tMax = -largeFloat;
    }

    public boolean isInsideXY(float[] xy)
    {
        return isInsideXY(xy[0], xy[1]);
    }

    public boolean isInsideXY(float x, float y)
    {
        return x >= xMin && x <= xMax && y >= yMin && y <= yMax;
    }

    public boolean isInsideXY(StsPoint p)
    {
        return isInsideXY(p.v[0], p.v[1]);
    }

    public boolean isInsideXYZ(StsPoint p)
    {
        return isInsideXYZ(p.v[0], p.v[1], p.v[2]);
    }

    public boolean isInsideXYZ(float x, float y, float z)
    {
        if (x < xMin) return false;
        if (x > xMax) return false;
        if (y < yMin) return false;
        if (y > yMax) return false;
        if (z < zMin) return false;
        if (z > zMax) return false;
        return true;
    }
   public boolean isInsideXYZ(float[] xyz)
   {
	   return isInsideXYZ(xyz[0], xyz[1], xyz[2]);
   }

	public boolean isXYZSameSize(StsBoundingBox boundingBox, int dir)
	{
		switch(dir)
		{
			case 0: // X
				if (yMin != boundingBox.yMin) return false;
				if (yMax != boundingBox.yMax) return false;
				if (this.getZMin() != this.getZMin()) return false;
				if (zMax != boundingBox.zMax) return false;
				return true;
			case 1: // Y
				if (xMin != boundingBox.xMin) return false;
				if (xMax != boundingBox.xMax) return false;
				if (this.getZMin() != this.getZMin()) return false;
				if (zMax != boundingBox.zMax) return false;
				return true;
			case 2: // Z
				if (xMin != boundingBox.xMin) return false;
				if (xMax != boundingBox.xMax) return false;
				if (yMin != boundingBox.yMin) return false;
				if (yMax != boundingBox.yMax) return false;
				return true;
			default:
				StsException.systemError(this, "isXYZGridSameSize", "called with wrong argument:  " + dir);
				return false;
		}
	}

    public void makeCongruent(StsRotatedBoundingBox box)
    {
		if(initializedXY() && box.initializedXY())
		{
			xMin = Math.min(xMin, box.xMin);
			xMax = Math.max(xMax, box.xMax);

			yMin = Math.min(yMin, box.yMin);
			yMax = Math.max(yMax, box.yMax);
		}
		if(initializedZ() && box.initializedZ())
		{
			zMin = Math.min(zMin, box.getZMin());
			zMax = Math.max(zMax, box.getZMax());
		}
    }

	public boolean adjustZTRange(StsBoundingBox box)
	{
		boolean changed = false;
		if(box.initializedZ())
		{
			if(initializedZ())
			//if(initialized && initializedZ())
			{
				changed = changed | adjustZMin(box.zMin);
				changed = changed | adjustZMax(box.zMax);
			}
			else
			{
				setZMin(box.zMin);
				setZMax(box.zMax);
				changed = true;
			}
		}
		if(box.initializedT())
		{
			if(initializedT())
			//if(initialized && initializedT())
			{
				changed = changed | adjustTMin(box.tMin);
				changed = changed | adjustTMax(box.tMax);
			}
			else
			{
				setTMin(box.tMin);
				setTMax(box.tMax);
				changed = true;
			}
		}
		return changed;
	}

    public void adjustZRange(float newZMin, float newZMax)
    {
        zMin = Math.min(zMin, newZMin);
        zMax = Math.max(zMax, newZMax);
    }

    public float getXSize()
    {
        return xMax - xMin;
    }

    public float getYSize()
    {
        return yMax - yMin;
    }

	public float getZTSize()
	{
		if(isDepth) return zMax - this.getZMin();
		else		return tMax - tMin;
	}

    public float[] getRelativeXY(double x, double y)
    {
        return new float[]
                {(float) (x - xOrigin), (float) (y - yOrigin)};
    }

    public double[] getAbsoluteXRange()
    {
        return new double[]{xOrigin + xMin, xOrigin + xMax};
    }


    public double[] getAbsoluteYRange()
    {
        return new double[]{yOrigin + yMin, yOrigin + yMax};
    }

    public void adjustXYZPosition(StsPoint dPoint)
    {
        float[] dxyz = dPoint.getXYZorT();
        xMin += dxyz[0];
        xMax += dxyz[0];
        yMin += dxyz[1];
        yMax += dxyz[1];
        zMin += dxyz[2];
        zMax += dxyz[2];
    }

    public void addPoint(float[] xyz)
    {
        if(xyz == null) return;

        if(xyz.length > 2)
        {
            float z = xyz[2];
            if(z == nullValue)
            {
                return;
            }
            zMin = Math.min(z, zMin);
            zMax = Math.max(z, zMax);
        }
        float x = xyz[0];
        float y = xyz[1];

        xMin = Math.min(x, xMin);
        xMax = Math.max(x, xMax);
        yMin = Math.min(y, yMin);
        yMax = Math.max(y, yMax);
    }

    public void addPoint(float[] xyz, float dXOrigin, float dYOrigin)
    {
        float x = xyz[0];
        float y = xyz[1];
        float z = xyz[2];
        addPoint(x, y, z, dXOrigin, dYOrigin);
    }

    public void addPoint(float x, float y, float z, float dXOrigin, float dYOrigin)
    {
        if(z == nullValue) return;

        x += dXOrigin;
        y += dYOrigin;

        xMin = Math.min(x, xMin);
        xMax = Math.max(x, xMax);
        yMin = Math.min(y, yMin);
        yMax = Math.max(y, yMax);
        zMin = Math.min(z, zMin);
        zMax = Math.max(z, zMax);
    }

    public void addPoint(float x, float y)
    {
        xMin = Math.min(x, xMin);
        xMax = Math.max(x, xMax);
        yMin = Math.min(y, yMin);
        yMax = Math.max(y, yMax);
	}
/*
    public void addPoint(StsGridSectionPoint point)
    {
        addPoint(point.getXYZorT());
    }
*/
    public void addPoints(StsPoint[] points)
    {
        if(points == null)
        {
            return;
        }
        for(int n = 0; n < points.length; n++)
        {
            StsPoint point = points[n];
            if(point == null)
            {
                return;
            }
            addPoint(point.v);
        }
    }

    public void addPoints(double[][] points)
    {
        int nPoints = points.length;
        if(nPoints == 0)
        {
            return;
        }
        int nCoors = Math.min(points[0].length, 3);
        if(nCoors == 0)
        {
            return;
        }

        for(int n = 0; n < nCoors; n++)
        {
            float min = StsParameters.largeFloat;
            float max = -StsParameters.largeFloat;

            for(int p = 0; p < nPoints; p++)
            {
                float value = (float) points[p][n];
                min = Math.min(min, value);
                max = Math.max(max, value);
            }

            switch(n)
            {
                case 0:
                    xMin = min;
                    xMax = max;
                    break;
                case 1:
                    yMin = min;
                    yMax = max;
                    break;
                case 2:
                    zMin = min;
                    zMax = max;
            }
        }
        // if(nCoors > 2) initializedZ = true;
    }

    /**
     * This point is offset from its own origin.
     * Add it to this boundingBox which has its own origin
     */
    public void addPoint(StsPoint point, double xOrigin, double yOrigin)
    {
        if(point == null)
        {
            return;
        }
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        addPoint(point.v, dXOrigin, dYOrigin);
    }

    public void addPoint(float[] xyz, double xOrigin, double yOrigin)
    {
        if (xyz == null) return;
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        addPoint(xyz, dXOrigin, dYOrigin);
    }

    public void addPoint(double x, double y, double z)
    {
        float dx = (float) (x - this.xOrigin);
        float dy = (float) (y - this.yOrigin);
        addPoint(dx, dy, (float)z);
    }

    public void addPoint(float dx, float dy, float z)
    {
        xMin = Math.min(dx, xMin);
        xMax = Math.max(dx, xMax);
        yMin = Math.min(dy, yMin);
        yMax = Math.max(dy, yMax);
        zMin = Math.min(z, zMin);
        zMax = Math.max(z, zMax);
    }

    public void addXY(StsPoint point, double xOrigin, double yOrigin)
    {
        if(point == null) return;
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        float x = point.v[0] + dXOrigin;
        float y = point.v[1] + dYOrigin;

        xMin = Math.min(x, xMin);
        xMax = Math.max(x, xMax);
        yMin = Math.min(y, yMin);
        yMax = Math.max(y, yMax);
    }

    public void addXY(double x, double y, double xOrigin, double yOrigin)
    {
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        x += dXOrigin;
        y += dYOrigin;

        xMin = (float) Math.min(x, xMin);
        xMax = (float) Math.max(x, xMax);
        yMin = (float) Math.min(y, yMin);
        yMax = (float) Math.max(y, yMax);
    }

    private boolean isXYOutsideBoxLimit(double x, double y, double limit)
    {
        if(xMin == StsParameters.largeFloat)     // First point in project always passes sanity check
        { return true; }
        // check point against current bounding box
        if(xMin - x > limit) return true;
        if(x - xMax > limit) return true;
        if(yMin - y > limit) return true;
        if(y - yMax > limit) return true;
        return false;
    }

    public boolean isXYOutsideBoxLimit(float[] xy)
    {
        if(xMin == StsParameters.largeFloat)     // First point in project always passes sanity check
        { return true; }
        // check point against current bounding box
		float limit = 2*getXSize();
		if(xMin - xy[0] > limit) return true;
        if(xy[0] - xMax > limit) return true;
        if(yMin - xy[1] > limit) return true;
        if(xy[1] - yMax > limit) return true;
        return false;
    }

    public boolean isXYOutsideBoxLimit(double x, double y, double xOrigin, double yOrigin, double limit)
    {
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        x += dXOrigin;
        y += dYOrigin;

        // check point against current bounding box
        return isXYOutsideBoxLimit(x, y, limit);
    }

    public boolean isXYOutsideBoxLimit(StsPoint point, double xOrigin, double yOrigin, double limit)
    {
        if(point == null)
        { return false; }
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        float x = point.v[0] + dXOrigin;
        float y = point.v[1] + dYOrigin;

        // check point against current bounding box
        return isXYOutsideBoxLimit(x, y, limit);
    }

    public boolean isBoxOriginOutsideBoxXYLimit(StsBoundingBox box, double limit)
    {
		if(!originSet) return false;
        float dx = (float) (box.xOrigin - xOrigin);
        float dy = (float) (box.yOrigin - yOrigin);
        double x = dx + box.xMin;
        double y = dy + box.yMin;

        // check point against current bounding box
        return isXYOutsideBoxLimit(x, y, limit);
    }

    public boolean isBoxOriginOutsideBoxXYLimit(StsBoundingBox box)
    {
		double limit = Math.max(getXSize(), getYSize());
		return isBoxOriginOutsideBoxXYLimit(box, limit);
    }

    public void addPoint(float x, float y, float z, double xOrigin, double yOrigin)
    {
        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        addPoint(x, y, z, dXOrigin, dYOrigin);
    }

    public void addPoints(StsPoint[] points, double xOrigin, double yOrigin)
    {
        if(points == null)
        {
            return;
        }

        float dXOrigin = (float) (xOrigin - this.xOrigin);
        float dYOrigin = (float) (yOrigin - this.yOrigin);
        for(int n = 0; n < points.length; n++)
        {
            StsPoint point = points[n];
            if(point == null)
            {
                return;
            }
            addPoint(point.v, dXOrigin, dYOrigin);
        }
    }

    /** Adjust this boundingBox to include box. This method should not be used for a rotated bounding box.
     * Returns true if this box has been changed by the addition. */
    public boolean addBoundingBox(StsBoundingBox box)
    {
        boolean changed = false;
        // if(!initialized) resetRange();
        if(!originSet)
        {
            initializeOriginRange(box.xOrigin, box.yOrigin);
            if(initializedXY())
            {
                xMin = Math.min(xMin, box.xMin);
                xMax = Math.max(xMax, box.xMax);
                yMin = Math.min(yMin, box.yMin);
                yMax = Math.max(yMax, box.yMax);
            }
            else
            {
                xMin = box.xMin;
                xMax = box.xMax;
                yMin = box.yMin;
                yMax = box.yMax;
            }
        }
        else
        {
            float dx = (float) (box.xOrigin - xOrigin);
            float dy = (float) (box.yOrigin - yOrigin);
            if(initializedXY())
            {
                changed = changed | adjustXMin(box.xMin + dx);
                changed = changed | adjustXMax(box.xMax + dx);
                changed = changed | adjustYMin(box.yMin + dy);
                changed = changed | adjustYMax(box.yMax + dy);
            }
            else
            {
                xMin = box.xMin + dx;
                xMax = box.xMax + dx;
                yMin = box.yMin + dy;
                yMax = box.yMax + dy;
                changed = true;
            }
        }
        changed = changed | adjustZTRanges(box);
        // initialized = true;
        return changed;
    }

    public void intersectBoundingBox(StsBoundingBox box)
    {
        if(!originSet)
        {
            // Don't want to reset the ranges as well or else zmin and zmax will always be 1E-10 and -1E-10
            // because of these statements below; zMin = Math.max(zMin, box.zMin); zMax = Math.min(zMax, box.zMax);
            // initializeOriginRangeAngle(box.xOrigin, box.yOrigin);
            originSet = true;
            if(initializedXY())
            {
                xMin = Math.max(xMin, box.xMin);
                xMax = Math.min(xMax, box.xMax);
                yMin = Math.max(yMin, box.yMin);
                yMax = Math.min(yMax, box.yMax);
            }
            else
            {
                xMin = box.xMin;
                xMax = box.xMax;
                yMin = box.yMin;
                yMax = box.yMax;
            }
        }
        else
        {
            float dx = (float) (box.xOrigin - xOrigin);
            float dy = (float) (box.yOrigin - yOrigin);
            box.xOrigin = xOrigin;
            box.yOrigin = yOrigin;
            box.xMin += dx;
            box.xMax += dx;
            box.yMin += dy;
            box.yMax += dy;
            if(initializedXY())
            {
                xMin = Math.max(xMin, box.xMin);
                xMax = Math.min(xMax, box.xMax);
                yMin = Math.max(yMin, box.yMin);
                yMax = Math.min(yMax, box.yMax);
            }
            else
            {
                xMin = box.xMin;
                xMax = box.xMax;
                yMin = box.yMin;
                yMax = box.yMax;
            }
        }
        if(isDepth)
        {
            if(initializedZ())
            {
                zMin = Math.max(zMin, box.zMin);
                zMax = Math.min(zMax, box.zMax);
            }
            else
            {
                zMin = box.zMin;
                zMax = box.zMax;
            }
        }
        else
        {
            if(initializedT())
            {
                tMin = Math.max(tMin, box.tMin);
                tMax = Math.min(tMax, box.tMax);
            }
            else
            {
                tMin = box.tMin;
                tMax = box.tMax;
            }
        }
    }

    public boolean intersectsBoundingBox(StsBoundingBox box)
    {
		double xMinI = Math.max(box.getAbsXMin(), getAbsXMin());
		double xMaxI = Math.min(box.getAbsXMax(), getAbsXMax());
		if(xMaxI <= xMinI) return false;

		double yMinI = Math.max(box.getAbsYMin(), getAbsYMin());
		double yMaxI = Math.min(box.getAbsYMax(), getAbsYMax());
		if(yMaxI <= yMinI) return false;

		float zMinI = Math.max(box.zMin, zMin);
		float zMaxI = Math.min(box.zMax, zMax);
		return zMaxI > zMinI;
    }

    public void initialize(StsBoundingBox box)
    {
        if(box.originSet) setOrigin(box.xOrigin,  box.yOrigin);
        if(box.initializedXY())
        {
            xMin = box.xMin;
            xMax = box.xMax;
            yMin = box.yMin;
            yMax = box.yMax;
        }
        if(box.initializedZ())
        {
            zMin = box.zMin;
            zMax = box.zMax;
        }
        if(box.initializedT())
        {
            tMin = box.tMin;
            tMax = box.tMax;
        }
        // initialized = box.initialized;
    }

    public boolean clipLine(StsPoint p0, StsPoint p1)
    {
        double[] xyz0 = new double[]{p0.v[0], p0.v[1], p0.v[2]};
        double[] xyz1 = new double[]{p1.v[0], p1.v[1], p1.v[2]};
        return clipLine(xyz0, xyz1);
    }

    public boolean clipLine(double[] xyz0, double[] xyz1)
    {
        double max, min;

        /* clip ends between xMin and xMax 				*/

        if(xyz0[0] < xyz1[0])
        {
            clipXLine(xyz0, xyz1);
        }
        else
        {
            clipXLine(xyz1, xyz0);

            /* check if clipped line is below yMin or above yMax	*/

        }
        max = Math.max(xyz0[1], xyz1[1]);
        min = Math.min(xyz0[1], xyz1[1]);

        if(max <= yMin || min >= yMax)
        {
            return false;
        }

        /* clip ends between yMin and yMax 					*/

        if(xyz0[1] < xyz1[1])
        {
            clipYLine(xyz0, xyz1);
        }
        else
        {
            clipYLine(xyz1, xyz0);

            /* check if clipped line is below xMin or above xMax	*/

        }
        max = Math.max(xyz0[0], xyz1[0]);
        min = Math.min(xyz0[0], xyz1[0]);

        if(max <= xMin || min >= xMax)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    // Line is increasing in X-index from start to end and extends beyond
    // an X-direction boundary: clip it.

    private void clipXLine(StsPoint start, StsPoint end)
    {
        float f;

        if(start.v[0] < xMin && end.v[0] > xMin)
        {
            f = (xMin - start.v[0]) / (end.v[0] - start.v[0]);
            start.v[1] = start.v[1] + f * (end.v[1] - start.v[1]);
            start.v[0] = xMin;
        }

        if(end.v[0] > xMax && start.v[0] < xMax)
        {
            f = (xMax - start.v[0]) / (end.v[0] - start.v[0]);
            end.v[1] = start.v[1] + f * (end.v[1] - start.v[1]);
            end.v[0] = xMax;
        }
    }

    private void clipXLine(double[] start, double[] end)
    {
        double f;

        if(start[0] < xMin && end[0] > xMin)
        {
            f = (xMin - start[0]) / (end[0] - start[0]);
            start[0] = xMin;
            start[1] = start[1] + f * (end[1] - start[1]);
            start[2] = start[2] + f * (end[2] - start[2]);
        }

        if(end[0] > xMax && start[0] < xMax)
        {
            f = (xMax - start[0]) / (end[0] - start[0]);
            end[0] = xMax;
            end[1] = start[1] + f * (end[1] - start[1]);
            end[2] = start[2] + f * (end[2] - start[2]);
        }
    }

    // Line is increasing in Y-index from start to end and extends beyond
    // a Y-direction boundary: clip it.

    private void clipYLine(StsPoint start, StsPoint end)
    {
        float f;

        if(start.v[1] < yMin && end.v[1] > yMin)
        {
            f = (yMin - start.v[1]) / (end.v[1] - start.v[1]);
            start.v[0] = start.v[0] + f * (end.v[0] - start.v[0]);
            start.v[1] = yMin;
        }

        if(end.v[1] > yMax && start.v[1] < yMax)
        {
            f = (yMax - start.v[1]) / (end.v[1] - start.v[1]);
            end.v[0] = start.v[0] + f * (end.v[0] - start.v[0]);
            end.v[1] = yMax;
        }
    }

    private void clipYLine(double[] start, double[] end)
    {
        double f;

        if(start[1] < yMin && end[1] > yMin)
        {
            f = (yMin - start[1]) / (end[1] - start[1]);
            start[1] = yMin;
            start[0] = start[0] + f * (end[0] - start[0]);
            start[2] = start[2] + f * (end[2] - start[2]);
        }

        if(end[1] > yMax && start[1] < yMax)
        {
            f = (yMax - start[1]) / (end[1] - start[1]);
            end[1] = yMax;
            end[0] = start[0] + f * (end[0] - start[0]);
            end[2] = start[2] + f * (end[2] - start[2]);
        }
    }

    public boolean addRotatedBoundingBox(StsRotatedBoundingBox box)
    {
        boolean changed = false;
        // if(!initialized) resetRange();
        if(!originSet) initializeOriginRange(box.xOrigin, box.yOrigin);
            
        double[][] corners = new double[4][];
        corners[0] = box.getAbsoluteXY(box.xMin, box.yMin);
        corners[1] = box.getAbsoluteXY(box.xMax, box.yMin);
        corners[2] = box.getAbsoluteXY(box.xMax, box.yMax);
        corners[3] = box.getAbsoluteXY(box.xMin, box.yMax);

        for(int n = 0; n < 4; n++)
        {
            float x = (float) (corners[n][0] - xOrigin);
            float y = (float) (corners[n][1] - yOrigin);
            changed = changed | adjustXMin(x);
            changed = changed | adjustXMax(x);
            changed = changed | adjustYMin(y);
            changed = changed | adjustYMax(y);
        }
        // initialized = true;
        return changed | adjustZTRanges(box);
    }

    public boolean adjustZTRanges(StsBoundingBox box)
    {
        boolean changed = false;

        if(box.initializedZ())
        {
            if(initializedZ())
            {
                changed = changed | adjustZMin(this.getZMin());
                changed = changed | adjustZMax(box.zMax);
            }
            else
            {
                zMin = box.zMin;
                zMax = box.zMax;
                changed = true;
            }
        }
        if(box.initializedT())
        {
            if(initializedT())
            {
                changed = changed | adjustTMin(box.tMin);
                changed = changed | adjustTMax(box.tMax);
            }
            else
            {
                tMin = box.tMin;
                tMax = box.tMax;
                changed = true;
            }
        }
        return changed;
    }

    public float[] getXYZCenter() { return new float[] { (xMax + xMin)/2, (yMax + yMin)/2, (zMax + zMin)/2 }; }

    public String getLabel()
    {
        return toDetailString();
    }

    public String toDetailString()
    {
        return super.toString() + " xMin: " + xMin + " xMax: " + xMax + " yMin: " + yMin + " yMax: " + yMax + " zMin: " + this.getZMin() + " zMax: " + zMax;
    }

    public static void main(String[] args)
    {
		StsBoundingBox box1 = new StsBoundingBox(false);
		box1.xOrigin = 1000000;
		box1.yOrigin = -500000;
		box1.xMin = -10000.0f;
		box1.xMax = -1000.0f;
		box1.yMin = -1000.0f;
		box1.yMax = 5000.0f;
		box1.zMin = -100.0f;
		box1.zMax = 500.0f;

		StsBoundingBox box2 = new StsBoundingBox(false);
		box2.xOrigin = 1001000;
		box2.yOrigin = -501000;
		box2.xMin = -2500.0f;
		box2.xMax = 1000.0f;
		box2.yMin = 1000.0f;
		box2.yMax = 2000.0f;
		box2.zMin = -500.0f;
		box2.zMax = 100.0f;

		boolean intersects = box1.intersectsBoundingBox(box2);
		intersects = box2.intersectsBoundingBox(box1);
		System.exit(0);
	/*
        float xMin = -1.0f;
        float xMax = 1.0f;
        float yMin = -1.0f;
        float yMax = 1.0f;

        StsBoundingBox box = new StsBoundingBox(-1.0f, 1.0f, -1.0f, 1.0f);
        System.out.println("xMin: " + xMin + " xMax: " + xMax +
                " yMin: " + yMin + " yMax: " + yMax);

        StsPoint p0 = new StsPoint(-2.0f, -2.0f, -2.0f);
        StsPoint p1 = new StsPoint(2.0f, 2.0f, 2.0f);
        System.out.println("input points: " + p0.toString() + " " + p1.toString());

        box.clipLine(p0, p1);
        System.out.println("output points: " + p0.toString() + " " + p1.toString());
     */
    }

    public void setXOrigin(double xOrigin)
    {
        this.xOrigin = xOrigin;
    }

    public void setYOrigin(double yOrigin)
    {
        this.yOrigin = yOrigin;
    }

    public boolean setOrigin(double xOrigin, double yOrigin)
    {
        if(originSet) return false;
        initializeOriginRange(xOrigin, yOrigin);
        return true;
    }

    public void setZRange(float zMin, float zMax)
    {
        if(this.getZMin() > zMin)
        {
            this.zMin = zMin;
            dbFieldChanged("zMin", zMin);
        }
        if(this.zMax < zMax)
        {
            this.zMax = zMax;
            dbFieldChanged("zMax", zMax);
        }
    }

    public void setTRange(float tMin, float tMax)
    {
        if(this.tMin > tMin)
        {
            this.tMin = tMin;
            dbFieldChanged("tMin", tMin);
        }
        if(this.tMax < tMax)
        {
            this.tMax = tMax;
            dbFieldChanged("tMax", tMax);
        }
    }

    public void setZTRange(float ztMin, float ztMax, byte zDomain)
    {
        if(zDomain == StsParameters.TD_DEPTH)
            setZRange(ztMin, ztMax);
        else
            setTRange(ztMin, ztMax);
    }

    public boolean sameAs(StsBoundingBox otherBox)
    {
        return sameAs(otherBox, true);
    }

    public boolean sameAs(StsBoundingBox otherBox, boolean checkZ)
    {
        if(!StsMath.sameAs(xOrigin, otherBox.xOrigin))
        {
            return false;
        }
        if(!StsMath.sameAs(yOrigin, otherBox.yOrigin))
        {
            return false;
        }
        if(!StsMath.sameAs(xMin, otherBox.xMin))
            return false;
        if(!StsMath.sameAs(xMax, otherBox.xMax))
        {
            return false;
        }
        if(!StsMath.sameAs(yMin, otherBox.yMin))
        {
            return false;
        }
        if(!StsMath.sameAs(yMax, otherBox.yMax))
        {
            return false;
        }
        if(checkZ && !StsMath.sameAs(this.getZMin(), this.getZMin()))
        {
            return false;
        }
        if(checkZ && !StsMath.sameAs(zMax, otherBox.zMax))
        {
            return false;
        }
        return true;
    }

    /** Get the x center of the box */
    public float getXCenter()
    {
        return (xMin + xMax) / 2;
    }

    /** Get the y center of the box */
    public float getYCenter()
    {
        return (yMin + yMax) / 2;
    }

    /**
     * Get the maximum Project dimensions
     *
     * @returns maximum projection distance in X, Y or Z
     */
    public float getDimensions()
    {
        return StsMath.max3((xMax - xMin), (yMax - yMin), (getZTMax() - getZTMin()));
    }

    public double getXOrigin()
    {
        return xOrigin;
    }

    public double getYOrigin()
    {
        return yOrigin;
    }

	public double[] getOrigin() { return new double[] { xOrigin, yOrigin }; }

	/** we wish to get an origin which is shifted to xMin, yMin for this box. */
	public double[] getAdjustedOrigin()
	{
		return new double[] { xOrigin + xMin, yOrigin + yMin };
	}

	/** move origin to a new location and adjust x and y ranges */
	public void resetOrigin(double[] origin)
	{
		double dx = origin[0] - xOrigin;
		double dy = origin[1] - yOrigin;
		xOrigin = origin[0];
		yOrigin = origin[1];
		xMin -= dx;
		xMax -= dx;
		yMin -= dy;
		yMax -= dy;
	}

	public boolean originSame(double[] otherOrigin)
	{
		return xOrigin == otherOrigin[0] && yOrigin == otherOrigin[1];
	}

    public void setZMin(float zMin)
    {
        if(zMin >= this.getZMin()) return;
        this.zMin = zMin;
        if(isPersistent()) dbFieldChanged("zMin", zMin);
    }

    public void setZMax(float zMax)
    {
        if(zMax <= this.zMax) return;
        this.zMax = zMax;
        if(isPersistent()) dbFieldChanged("zMax", zMax);
    }

    public float getZMin()
    {
        return zMin;
    }

    public float getZMin(int row)
    {
        return this.getZMin();
    }

    public float getZMax()
    {
        return zMax;
    }

    public float getZTMin()
    {
        if(isDepth) return this.getZMin();
        else return tMin;
    }

    public void setZTMin(float ztMin)
    {
        if(isDepth) setZMin(ztMin);
        else setTMin(ztMin);
    }

    public float getZTMax()
    {
        if(isDepth) return zMax;
        else return tMax;
    }

    public void setZTMax(float ztMax)
    {
        if(isDepth) setZMax(ztMax);
        else setTMax(ztMax);
    }

    public float getYMin()
    {
        return yMin;
    }

    public float getYMax()
    {
        return yMax;
    }

	public void setYMin(float yMin)
	{
		if(yMin >= this.yMin) return;
		this.yMin = yMin;
		if(isPersistent()) dbFieldChanged("yMin", yMin);
	}

	public void setYMax(float yMax)
	{
		if(yMax <= this.yMax) return;
		this.yMax = yMax;
		if(isPersistent()) dbFieldChanged("yMax", yMax);
	}

	public void setXMin(float xMin)
	{
		if(xMin >= this.xMin) return;
		this.xMin = xMin;
		if(isPersistent()) dbFieldChanged("xMin", xMin);
	}

	public void setXMax(float xMax)
	{
		if(xMax <= this.xMax) return;
		this.xMax = xMax;
		if(isPersistent()) dbFieldChanged("xMax", xMax);
	}

    public float getXMin()
    {
        return xMin;
    }

    public float getXMax()
    {
        return xMax;
    }

    public void setTMin(float tMin)
    {
        if(tMin >= this.tMin) return;
        this.tMin = tMin;
        if(isPersistent()) dbFieldChanged("tMin", tMin);
    }

    public void setTMax(float tMax)
    {
        if(tMax <= this.tMax) return;
        this.tMax = tMax;
        if(isPersistent()) dbFieldChanged("tMax", tMax);
    }

    public float getTMin() { return tMin; }
    public float getTMax() { return tMax; }

    public boolean adjustXMin(float xMin)
    {
        if(this.xMin <= xMin) return false;
        this.xMin = xMin;
        return true;
    }

    public boolean adjustXMax(float xMax)
    {
        if(this.xMax >= xMax) return false;
        this.xMax = xMax;
        return true;
    }

    public boolean adjustYMin(float yMin)
    {
        if(this.yMin <= yMin) return false;
        this.yMin = yMin;
        return true;
    }

    public boolean adjustYMax(float yMax)
    {
        if(this.yMax >= yMax) return false;
        this.yMax = yMax;
        return true;
    }

    public boolean adjustZMin(float zMin)
    {
        if(this.getZMin() <= zMin) return false;
        setZMin(zMin);
        return true;
    }

    public boolean adjustZMax(float zMax)
    {
        if(this.zMax >= zMax) return false;
        setZMax(zMax);
        return true;
    }

    public boolean adjustTMin(float tMin)
    {
        if(this.tMin <= tMin) return false;
        setTMin(tMin);
        return true;
    }

    public boolean adjustTMax(float tMax)
    {
        if(this.tMax >= tMax) return false;
        setTMax(tMax);
        return true;
    }

	public double getAbsXMin() { return xOrigin + xMin; }
	public double getAbsXMax() { return xOrigin + xMax; }
	public double getAbsYMin() { return yOrigin + yMin; }
	public double getAbsYMax() { return yOrigin + yMax; }

	public void setXMin(Float xMin) { setXMin(xMin.floatValue()); }
	public void setXMax(Float xMax) { setXMax(xMax.floatValue()); }
	public void setYMin(Float yMin) { setYMin(yMin.floatValue()); }
	public void setYMax(Float yMax) { setYMax(yMax.floatValue()); }
	public void setZMin(Float zMin) { setZMin(zMin.floatValue()); }
	public void setZMax(Float zMax) { setZMax(zMax.floatValue()); }

    public void displayBoundingBox(GL gl, StsColor stsColor, float lineWidth)
    {
        stsColor.setGLColor(gl);
        gl.glLineWidth(lineWidth);

        float ztMin = getZTMin();
        float ztMax = getZTMax();
        // Draw the bottom outer boundaries
        drawZRectangle(gl, ztMax);
        // Draw the top outer boundaries
        drawZRectangle(gl, ztMin);

        // Draw verticals
        gl.glBegin(GL.GL_LINES);
        {
            gl.glVertex3f(xMin, yMin, ztMin);
            gl.glVertex3f(xMin, yMin, ztMax);

            gl.glVertex3f(xMin, yMax, ztMin);
            gl.glVertex3f(xMin, yMax, ztMax);

            gl.glVertex3f(xMax, yMax, ztMin);
            gl.glVertex3f(xMax, yMax, ztMax);

            gl.glVertex3f(xMax, yMin, ztMin);
            gl.glVertex3f(xMax, yMin, ztMax);
        }
        gl.glEnd();
    }

    protected void drawZRectangle(GL gl, float z)
    {
        gl.glBegin(GL.GL_LINE_LOOP);
        {
            gl.glVertex3f(xMin,  yMin, z);
            gl.glVertex3f(xMin,  yMax, z);
            gl.glVertex3f(xMax,  yMax, z);
            gl.glVertex3f(xMax,  yMin, z);
        }
        gl.glEnd();
    }

    public void drawCornerPoints(GL gl, StsColor color, boolean isPicking)
    {
        float[] xyz = new float[3];

        xyz[0] = xMin;
        xyz[1] = yMin;
        xyz[2] = getZTMin();

        if(isPicking) gl.glPushName(0);
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[0] = xMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(1);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[1] = yMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(2);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[0] = xMin;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(3);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[2] = getZTMax();
        xyz[1] = yMin;

        if(isPicking) gl.glPushName(4);
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[0] = xMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(5);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[1] = yMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(6);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        xyz[0] = xMin;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(7);
        }
        StsGLDraw.drawPoint(gl, xyz, color, 4);

        if(isPicking) gl.glPopName();
    }

    public void drawFacePoints(GL gl, boolean isPicking)
    {
        float[] xyz = new float[3];
        xyz[2] = (getZTMax() + getZTMax()) / 2;

        xyz[0] = xMin;
        xyz[1] = (yMin + yMax) / 2;
        if(isPicking) gl.glPushName(0);
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        xyz[0] = xMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(1);
        }
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        xyz[0] = (xMin + xMax) / 2;
        xyz[1] = yMin;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(2);
        }
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        xyz[1] = yMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(3);
        }
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        xyz[1] = (yMin + yMax) / 2;
        xyz[2] = this.getZMin();
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(4);
        }
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        xyz[2] = zMax;
        if(isPicking)
        {
            gl.glPopName();
            gl.glPushName(5);
        }
        StsGLDraw.drawPoint(gl, xyz, StsColor.WHITE, 4);

        if(isPicking) gl.glPopName();
    }

    public float[] getFaceCenter(int faceIndex)
    {
        switch(faceIndex)
        {
            case XMIN:
                return new float[]{xMin, (yMin + yMax) / 2, (this.getZMin() + zMax) / 2};
            case XMAX:
                return new float[]{xMax, (yMin + yMax) / 2, (this.getZMin() + zMax) / 2};
            case YMIN:
                return new float[]{(xMin + xMax) / 2, yMin, (this.getZMin() + zMax) / 2};
            case YMAX:
                return new float[]{(xMin + xMax) / 2, yMax, (this.getZMin() + zMax) / 2};
            case ZMIN:
                return new float[]{(xMin + xMax) / 2, (yMin + yMax) / 2, this.getZMin()};
            case ZMAX:
                return new float[]{(xMin + xMax) / 2, (yMin + yMax) / 2, zMax};
            default:
                return null;
        }
    }

    public void adjustRange(int faceIndex, float adjustment)
    {
        switch(faceIndex)
        {
            case XMIN:
                if((xMin + adjustment) < getXMax())
                { setXMin(xMin += adjustment); }
                break;
            case XMAX:
                if((xMax + adjustment) > getXMin())
                { setXMax(xMax += adjustment); }
                break;
            case YMIN:
                if((yMin + adjustment) < getYMax())
                { setYMin(yMin += adjustment); }
                break;
            case YMAX:
                if((yMax + adjustment) > getYMin())
                { setYMax(yMax += adjustment); }
                break;
            case ZMIN:
                if((zMin + adjustment) < zMax)
                	zMin += adjustment;
                break;
            case ZMAX:
                if((zMax + adjustment) > zMin)
                	zMax += adjustment;
                break;
        }
    }

    public byte getZDomainSupported()
    {
        StsException.notImplemented(this, "getZDomainSupported");
        return 0;
    }

    public byte getZDomainOriginal()
    {
        StsException.notImplemented(this, "getZDomainSupported");
        return 0;
    }

    public boolean isBorderBox(StsBoundingBox otherBox)
    {
		if(xMin <= otherBox.xMin) return true;
        if(xMax >= otherBox.xMax) return true;
        if(yMin <= otherBox.yMin) return true;
        if(yMax >= otherBox.yMax) return true;
		if(initializedZ() && otherBox.initializedZ())
		{
			if(zMin <= otherBox.zMin) return true;
			if(zMax >= otherBox.zMax) return true;
		}
	    if(initializedT() && otherBox.initializedT())
		{
			if(tMin <= otherBox.tMin) return true;
			if(tMax >= otherBox.tMax) return true;
		}
        return false;
    }

	public float[] getLocalOrigin() { return new float[] { xMin, yMin };  }
}
