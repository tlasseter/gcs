package com.Sts.Framework.DBTypes.VectorSetObjects;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.DataVectors.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 12/20/11
 */
/** This object adds a set of coordinate vectors contained in the vertexSet;
 * i.e., the vectorSet must be an instance of StsCoorTimeVectorSet or a subclass.
 * The coorVectors can be any or all of X,Y,Z,T where T is seismic time.  */
abstract public class StsCoorTimeVectorSetObject extends StsTimeVectorSetObject
{
	/** bounding box around the sensor points in unrotated coordinate system */
	transient protected StsBoundingBox unrotatedBoundingBox;
	/** routed grid bounding box around the sensor points; grid increments are taken from project */
	transient protected StsRotatedGridBoundingBox rotatedBoundingBox;

	public StsCoorTimeVectorSetObject() { }

	public StsCoorTimeVectorSetObject(boolean persistent)
	{
		super(persistent);
	}

	public boolean addToProject()
	{
		getCoorVectorSet().checkSetCurrentTime();
		return currentModel.getProject().addToProjectUnrotatedBoundingBox(this, StsProject.TD_DEPTH);
	}

	public boolean initializeBoundingBox()
    {
		double[] xyOrigin = getCoorVectorSet().getOrigin();
		if(xyOrigin == null) return false;
		setOrigin(xyOrigin[0], xyOrigin[1]);
		adjustBoundingBox();
		return true;
	}

	public void adjustBoundingBox()
	{
	   StsProject project = getCurrentProject();
	   boolean supportsTime = project.supportsTime(getZDomainSupported());
	   boolean supportsDepth = project.supportsDepth(getZDomainSupported());
	   if(supportsTime)
	   {
		   double[] timeRange = getCoorVectorSet().getTAbsoluteRange();
		   setTMin((float)timeRange[0]);
		   setTMax((float)timeRange[1]);
	   }
	   if(supportsDepth)
	   {
		   double[] zRange = getCoorVectorSet().getZAbsoluteRange();
		   float[] scale = project.niceZTScale((float) zRange[0], (float) zRange[1]);
		   float depthMin = scale[0];
		   float depthMax = scale[1];
		   float depthInc = scale[2];
		   setZMin(depthMin);
		   setZMax(depthMax);
		   project.checkSetDepthInc(depthInc);
	   }
	   float[] xRange = getCoorVectorSet().getXRelativeRange();
	   float[] yRange = getCoorVectorSet().getYRelativeRange();
	   setXMin(xRange[0]);
	   setXMax(xRange[1]);
	   setYMin(yRange[0]);
	   setYMax(yRange[1]);
   }

   public boolean checkSetProjectOrigin()
   {
	   xOrigin = getXOrigin();
	   yOrigin = getYOrigin();
	   // check if project origin already set; if not, set it to this origin
	   // if origin already set (originSet==false), then adjust the xVector and yVector origins and values to conform to project
	   boolean originSet = getCurrentProject().checkSetOrigin(xOrigin, yOrigin);
	   if(!originSet) checkComputeRotatedPoints();
	   return originSet;
   }

	public boolean adjustBoundingBoxOther()
   {
	   unrotatedBoundingBox = new StsBoundingBox(false);
	   StsProject project = getProject();
	   if(project.isOriginSet())
	   {
		   double xOrigin = project.getXOrigin();
		   double yOrigin = project.getYOrigin();
		   unrotatedBoundingBox.xOrigin = xOrigin;
		   unrotatedBoundingBox.yOrigin = yOrigin;
		   StsAbstractFloatVector xVector = getXVector();
		   StsAbstractFloatVector yVector = getYVector();
		   float dXOrigin = (float) (xVector.getOrigin() - xOrigin);
		   float dYOrigin = (float) (yVector.getOrigin() - yOrigin);
		   unrotatedBoundingBox.xMin = xVector.getMinValue() + dXOrigin;
		   unrotatedBoundingBox.xMax = xVector.getMaxValue() + dXOrigin;
		   unrotatedBoundingBox.yMin = yVector.getMinValue() + dYOrigin;
		   unrotatedBoundingBox.yMax = yVector.getMaxValue() + dYOrigin;
	   }
	   else
	   {
		   StsAbstractFloatVector xVector = getXVector();
		   StsAbstractFloatVector yVector = getYVector();
		   unrotatedBoundingBox.setOrigin(xVector.getOrigin(), yVector.getOrigin());
		   unrotatedBoundingBox.xMin = xVector.getMinValue();
		   unrotatedBoundingBox.xMax = xVector.getMaxValue();
		   unrotatedBoundingBox.yMin = yVector.getMinValue();
		   unrotatedBoundingBox.yMax = yVector.getMaxValue();
	   }

	   StsAbstractFloatVector zVector = getZVector();
	   unrotatedBoundingBox.zMin = zVector.getMinValue();
	   unrotatedBoundingBox.zMax = zVector.getMaxValue();

	   rotatedBoundingBox = new StsRotatedGridBoundingBox(false);
	   if(project.isOriginSet())
	   {
		   StsRotatedGridBoundingBox projectBoundingBox = project.getRotatedBoundingBox();
		   rotatedBoundingBox.initialize(projectBoundingBox);
		   rotatedBoundingBox.addUnrotatedBoundingBox(unrotatedBoundingBox);
//            rotatedBoundingBox.checkMakeCongruent(projectBoundingBox);
	   }
	   else
		   rotatedBoundingBox.addUnrotatedBoundingBox(unrotatedBoundingBox);
	   return true;
   }

	public boolean checkComputeRotatedPoints()
   {
	   if (getCoorVectorSet() == null) return false;
	   return getCoorVectorSet().checkComputeRelativePoints(this);
   }

	/**
	 * Get the relative X origin
	 *
	 * @return X origin
	 */
	public double getXOrigin()
	{
		return getCoorVectorSet().getXOrigin();
	}

	/**
	 * Get the relative Y origin
	 *
	 * @return Y Origin
	 */
	public double getYOrigin()
	{
		return getCoorVectorSet().getYOrigin();
	}

	/** Get the minimum and maximum Z or T dependent on which domain is in view */
/*
	public float[] getZTRange()
	{
		StsAbstractFloatVector zVector = getZVector();
		if(zVector == null) return null;
		float zMin = zVector.getMinValue();
		float zMax = zVector.getMaxValue();
		if(zDomainOriginal != currentModel.getProject().getZDomain())
		{
			if(zDomainOriginal == StsProject.TD_DEPTH)
			{
				return new float[]{currentModel.getProject().getVelocityModel().getSliceCoor(zMin),
						currentModel.getProject().getVelocityModel().getSliceCoor(zMax)};
			}
			else
			{
				return new float[]{currentModel.getProject().getVelocityModel().getZCoor(zMin),
						currentModel.getProject().getVelocityModel().getZCoor(zMax)};
			}
		}
		else
			return new float[]{zMin, zMax};
	}
*/
	/** original sensor x values */
	public StsAbstractFloatVector getXVector()
	{
		return getCoorVectorSet().getXVector();
	}

	/** original sensor y values */
	public StsAbstractFloatVector getZVector()
	{
		return getCoorVectorSet().getZVector();
	}

	public StsAbstractFloatVector getTVector()
	{
		return getCoorVectorSet().getTVector();
	}

	/** original sensor y values */
	public StsAbstractFloatVector getYVector()
	{
		return getCoorVectorSet().getYVector();
	}

	public float[][] getXYZVectors()
    {
		return getCoorVectorSet().getXYZFloats();
    }

	public StsAbstractFloatVector[] getCoorVectors()
	{
		return getCoorVectorSet().getCoorVectors();
	}

	public float[] getXFloats() { return getXVector().getValues(); }
	public float[] getYFloats() { return getYVector().getValues(); }
	public float[] getZFloats() { return getZVector().getValues(); }
	public float[] getTFloats() { return getTVector().getValues(); }

	/** unrotated boundingBox for all points: range of points in world coordinates */
    public StsBoundingBox getUnrotatedBoundingBox()
    {
        return unrotatedBoundingBox;
    }

	/** rotated boundingBox for all points: range of points in project local coordinates */
    public StsRotatedBoundingBox getRotatedBoundingBox()
    {
        return rotatedBoundingBox;
    }

	public boolean checkComputeRelativePoints()
	{
		if (getCoorVectorSet() == null) return false;
		return getCoorVectorSet().checkComputeRelativePoints(this);
	}

	public boolean projectRotationAngleChanged()
	{
		return checkComputeRelativePoints();
	}

	public boolean adjustRotatedPoints(int nFirstIndex)
    {
		if (getCoorVectorSet() == null) return false;
		return getCoorVectorSet().adjustRotatedPoints(nFirstIndex);
	}

	/** A vector set containing x,y,z, and optionally t vectors. */
	public StsCoorTimeVectorSet getCoorVectorSet()
	{
		return (StsCoorTimeVectorSet)vectorSet;
	}

	public void setCoorVectorSet(StsCoorTimeVectorSet coorVectorSet)
	{
		this.vectorSet = coorVectorSet;
	}

	public float getZAtT(float time)
	{
		return StsAbstractFloatVector.getInterpolatedVectorFloat(getTVector(), time, getZVector());
	}

	/**
 * Set a user supplied time shift for all time values
 * @param time - time shift in minutes
 */
public void setTimeShift(long time)
{
	StsTimeVector timeVector = getClockTimeVector();
	if(timeVector == null) return;
	timeVector.setOriginOffset(time * 60000); // Convert user specified minutes to milli-seconds
	currentModel.viewObjectRepaint(this, this);
}
}
