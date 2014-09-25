package com.Sts.Framework.Types;
/**
 * <p>Title: S2S Development</p>
 * <p>Description: Class defining a rotated bounding box.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author T.Lasseter
 * @version 1.1
 */

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;

public class StsRotatedBoundingBox extends StsBoundingBox implements Cloneable, Serializable
{
    /** angle from global +X axis counter-clockwise to the local +X axis */
	public float angle = 0.0f;
	/** Indicates the rotation angle has been defined and can't be changed.
	 *  Size of box can be extended, however.
	 */
	public boolean angleSet = false;
    /** sine of angle in XY plane */
	transient public double sinXY = 0.0;
    /** cosine of angle in XY plane */
	transient public double cosXY = 1.0;

    /** Default Rotated Bounding Box constructTraceAnalyzer */
	public StsRotatedBoundingBox()
	{
	}

    /** Rotated Bounding Box constructTraceAnalyzer allowing non-persistent construction */
	public StsRotatedBoundingBox(boolean persistent)
	{
        super(persistent);
	}

	public StsRotatedBoundingBox(boolean persistent, String name)
	{
		super(persistent, name);
	}

    public StsRotatedBoundingBox(float angle)
    {
        setAngle(angle);
    }

    /**
	 * Rotated bounding box constructTraceAnalyzer using a rotated grid
	 * @param grid the grid used to define the bounding box
	 */
	public StsRotatedBoundingBox(StsXYSurfaceGridable grid)
	{
		super(grid);
		setAngle(grid.getAngle());
	}

    /**
	 * Rotated bounding box constructTraceAnalyzer using an origin and angle
	 * @param xOrigin the x value of the origin of the bounding box
	 * @param yOrigin the y value of the origin of the bounding box
	 * @param angle the angle of rotation of the bounding box
	 */
	public StsRotatedBoundingBox(double xOrigin, double yOrigin, float angle)
	{
		super(xOrigin, yOrigin);
		setAngle(angle);
	}

	public StsRotatedBoundingBox(double xOrigin, double yOrigin, float angle, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, float tMin, float tMax)
	{
        this(xOrigin, yOrigin, angle);
		initialize(xMin, xMax, yMin, yMax, zMin, zMax, tMin, tMax);
	}

	public StsRotatedBoundingBox(float xMin, float xMax, float yMin, float yMax, float zMin, float zMax)
	{
		initialize(xMin, xMax, yMin, yMax, zMin, zMax);
	}

	public boolean initialize(StsModel model)
	{
		super.initialize(model);
		setAngle();
		return true;
	}

	public void initializeAngle()
	{
		angle = 0.0f;
		setAngleSet(false);
		sinXY = 0.0;
		cosXY = 1.0;
	}

	public boolean isInitialized() { return angleSet; }
    public boolean getAngleSet() { return angleSet; }

    /** Initialize the angle from this boundingBox if it has been set. */
    public void checkSetAngle(StsRotatedBoundingBox boundingBox)
    {
        if(angleSet || !boundingBox.angleSet) return;
        angleSet = true;
        this.angle = boundingBox.angle;
    }

    public void setAngleSet(boolean set)
    {
		if(angleSet == set) return;
        angleSet = set;
        if(getIndex() >= 0 && angleSet) dbFieldChanged("angleSet", set);
    }

	public void initialize(StsBoundingBox box)
	{
		super.initialize(box);
		initializeAngle();
	}

    public void reinitializeBoundingBox()
    {
       super.reinitializeBoundingBox();
       initializeAngle();
    }

    public void checkSetOriginAndAngle(StsRotatedBoundingBox rotatedBoundingBox)
	{
		checkSetOrigin(rotatedBoundingBox);
		checkSetAngle(rotatedBoundingBox);
	}

    public void checkSetOriginAndAngle(double xOrigin, double yOrigin, float angle)
	{
		if (!originSet)
		{
			originSet = true;
			this.xOrigin = xOrigin;
			this.yOrigin = yOrigin;
			setAngle(angle);
		}
	}

    /**
     * Get the angle of rotation of the bounding box
     * @return the rotation angle
     */
    public float getAngle() { return angle; }

    /**
     * Set the rotation angle for the bounding box
     * @param angle the angle of rotation of the bounding box
     */
    public void setAngle(float angle)
    {
        angle = angle % 360.0f;
		if(angle < 0.0f) angle += 360.0f;
        this.angle = angle;
        setAngle();
        setAngleSet(true);
	}

    public boolean checkAngle(float angle)
    {
        angle = angle % 360.0f;
		if(angle < 0.0f) angle += 360.0f;
        return StsMath.sameAs(angle, this.angle, 0.01f);
    }

	public void setAngle()
	{
		sinXY = StsMath.sind(angle);
		cosXY = StsMath.cosd(angle);
        angleSet = true;
	}

    /** Compute the relative XY from the absolute XY of the rotated bounding box
     * @return float[] x, y
     * DO NOT call this method directly from a boundingBox object if you want to use project origin;
     * instead call project.getRotatedXY(double x, double y)
     * */
    public float[] getRotatedRelativeXYFromUnrotatedAbsXY(double x, double y)
    {
        return this.getRotatedRelativeXYFromUnrotatedRelativeXY((float)(x - xOrigin), (float)(y - yOrigin));
    }

    public double[] getRotatedAbsXYFromUnrotatedAbsXY(double[] xy)
    {
        float[] rotRelXY = getRotatedRelativeXYFromUnrotatedRelativeXY((float)(xy[0] - xOrigin), (float)(xy[1] - yOrigin));
		return new double[] { rotRelXY[0] + xOrigin, rotRelXY[1] + yOrigin };
    }

	/** Compute the absolute XY from local XY for the rotated bounding box
	 * @return float[] x, y
	 * */
	public double[] getAbsoluteXY(float x, float y)
	{
		if(angle == 0.0f) return new double[] { x + xOrigin, y + yOrigin };
		double absX = (double)(x*cosXY - y*sinXY + xOrigin);
		double absY = (double)(y*cosXY + x*sinXY + yOrigin);
		return new double[] { absX, absY };
	}

	/** we wish to get an origin which is shifted to xMin, yMin for this box. */
	public double[] getAdjustedOrigin()
	{
		return  getAbsoluteXY(xMin, yMin);
	}

	/** move origin to a new location and adjust x and y ranges */
	public void resetOrigin(double[] origin)
	{
		float[] xy = getRelativeXY(origin[0], origin[1]);
		float dx = xy[0] - xMin;
		xMin = xy[0];
		xMax += dx;
		float dy = xy[1] - yMin;
		yMin = xy[1];
		yMax += dy;
	}

    /** Compute the relative origin of the rotated bounding box
     * @return float[] xOrigin, yOrigin - 0.0, 0.0 if not explicitly set
     * */
    public float[] computeRelativeOrigin(double xOrigin, double yOrigin)
    {
        if(originSet) return getRotatedRelativeXYFromUnrotatedAbsXY(xOrigin, yOrigin);
        else          return new float[] { 0.0f, 0.0f };
    }

    /** relative to the project origin, return the unrotated xy given rotated xy */
	public float[] getUnrotatedRelativeXYFromRotatedXY(float x, float y)
	{
		if(angle == 0.0f) return new float[] { x, y };
		return new float[] { (float)(x*cosXY - y*sinXY), (float)(y*cosXY + x*sinXY) };
	}

	public float[] getUnrotatedRelativeXYFromAbsXY(double x, double y)
	{
        return new float[] { (float)(x - xOrigin), (float)(y - yOrigin) };
	}

    /** relative to the project origin, return the rotated xy given unrotated xy */
	public float[] getRotatedRelativeXYFromUnrotatedRelativeXY(float x, float y)
	{
		if(angle == 0.0f) return new float[] { x, y };
		return new float[] { (float)(x*cosXY + y*sinXY), (float)(y*cosXY - x*sinXY) };
	}

    /**
     * Adds an additional bounding box to the current one. It assumes that both boxes
     * have the same rotation angle
     * @param box the bounding box to be added
     */
    public boolean addRotatedBoundingBox(StsRotatedBoundingBox box)
    {
        if(!angleSet) reinitializeBoundingBox();
        if(box.angleSet) setAngle(box.angle);
        // if(!angleSet || !initialized) resetRange();
        boolean changed = false;
        float xMinBox = box.xMin;
        float xMaxBox = box.xMax;
        float yMinBox = box.yMin;
        float yMaxBox = box.yMax;
        if(originSet)
        {
            double dXOrigin = box.xOrigin - xOrigin;
            double dYOrigin = box.yOrigin - yOrigin;

            box.xOrigin = xOrigin;
            box.yOrigin = yOrigin;

            float dx = (float)(dXOrigin*cosXY + dYOrigin*sinXY);
            float dy = (float)(dYOrigin*cosXY - dXOrigin*sinXY);

            xMinBox += dx;
            xMaxBox += dx;
            yMinBox += dy;
            yMaxBox += dy;
        }
        else
            setOrigin(box.xOrigin, box.yOrigin);

        if(initializedXY() && box.initializedXY())
        // if(initialized && initializedXY() && box.initializedXY())
        {
            changed = changed | adjustXMin(xMinBox);
            changed = changed | adjustXMax(xMaxBox);
            changed = changed | adjustYMin(yMinBox);
            changed = changed | adjustYMax(yMaxBox);
        }
        else
        {
            setXMin(xMinBox);
            setXMax(xMaxBox);
            setYMin(yMinBox);
            setYMax(yMaxBox);
            changed = true;
        }
        changed = changed | adjustZTRanges(box);
        //initialized = true;
        return changed;
    }

    /**
     * Compares a bounding box to the current bounding box to verify that they have the
     * same rotation angle and can therefore be merged.
     * @param otherBox - the other bounding box to check
     * @return true if compatible with current boundng box
     */
	public boolean sameAs(StsRotatedBoundingBox otherBox)
	{
		return sameAs(otherBox, true);
	}

   public boolean sameAs(StsRotatedBoundingBox otherBox, boolean checkZ)
    {
        if(!StsMath.sameAsTol(angle, otherBox.angle, 0.5f)) return false;
        return super.sameAs(otherBox, checkZ);
    }
}
