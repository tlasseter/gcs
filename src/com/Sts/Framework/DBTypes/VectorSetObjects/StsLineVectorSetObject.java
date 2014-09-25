package com.Sts.Framework.DBTypes.VectorSetObjects;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 1/3/12
 */
/** This class adds an measuredDepth vector to the coorVectors, i.e., it is an instance of StsLineVectorSet or a subclass. */
public class StsLineVectorSetObject extends StsCoorTimeVectorSetObject
{
	protected boolean isVertical = false;

	public StsLineVectorSetObject() { }

	public StsLineVectorSetObject(boolean persistent)
	{
		super(persistent);
	}

	public StsAbstractFloatVector getMVector()
	{
		return getLineVectorSet().getMVector();
	}

	public float[] getMFloats() { return getMVector().getValues(); }

	public boolean checkComputeMDepths()
	{
		return checkComputeMDepths(0);
	}

	public boolean checkComputeMDepths(int firstIndex)
	{
		return getLineVectorSet().checkComputeMDepths(firstIndex);
	}

	public StsPoint getPointAtMDepth(float m, boolean extrapolate)
	{
		StsLineVectorSet lineVectorSet = getLineVectorSet();
		if(!lineVectorSet.checkComputeWellMDepthVector()) return null;
		StsAbstractFloatVector mVector = lineVectorSet.getMVector();
		if(mVector == null)
		{
			StsException.systemError(this, "getPointAtMDepth", "Failed to get point at mdepth " + m + " for well " + name);
			return null;
		}
		StsAbstractFloatVector.IndexF indexF = mVector.getIndexF(m, extrapolate);
		return lineVectorSet.computeInterpolatedCoorPoint(indexF);
	}

	public StsAbstractFloatVector.IndexF getIndexAtMDepth(float m, boolean extrapolate)
	{
		StsAbstractFloatVector mVector = getMVector();
		if(mVector == null)
		{
			StsException.systemError(this, "getPointAtMDepth", "Failed to get point at mdepth " + m + " for well " + name);
			return null;
		}
		return mVector.getIndexF(m, extrapolate);
	}

	public StsAbstractFloatVector.IndexF getIndexFAtMDepth(float m, boolean extrapolate)
	{
		StsAbstractFloatVector mVector = getMVector();
		if(mVector == null)
		{
			StsException.systemError(this, "getPointAtMDepth", "Failed to get point at mdepth " + m + " for well " + name);
			return null;
		}
		return mVector.getIndexF(m, extrapolate);
	}

	public float getDepthFromMDepth(float m, boolean extrapolate)
	{
		return getLineVectorSet().getDepthFromMDepth(m, extrapolate);
	}

	public float[] getCoordinatesAtDepth(float z, boolean extrapolate)
	{
		return getLineVectorSet().getFloatsAtDepth(z, extrapolate);
	}

	public StsPoint getSlopeAtMDepthPoint(float m)
	{
		return getLineVectorSet().getSlopeAtMDepthPoint(m);
	}

	//TODO this could be made more efficient by taking advantage of fact that mdepths and rotatedPoints are monotonically increasing in depth
	public StsPoint[] getSlopesAtMDepthPoints(float[] mdepths)
	{
		int nMdepthPoints = mdepths.length;
		StsPoint[] slopes = new StsPoint[nMdepthPoints];
		for (int n = 0; n < nMdepthPoints; n++)
		{
			slopes[n] = getSlopeAtMDepthPoint(mdepths[n]);
			if(slopes[n] == null)
				return (StsPoint[]) StsMath.arraycopy(slopes, n - 1);
		}
		return slopes;
	}

	public StsPoint getPointAtMDepth(float m, StsPoint[] points, boolean extrapolate)
	{
		// 3 is measured depth index in point.v array
		return StsMath.interpolatePoint(m, points, 3, extrapolate);
	}

	public StsPoint getSlopeAtMDepth(float m, StsPoint[] points)
	{
		StsPoint slope;
		int i = 0;
		float f;

		if (points == null)
		{
			return null;
		}

		int nPoints = points.length;
		if (getIsVertical() || nPoints < 2)
		{
			slope = new StsPoint(5);
			slope.v[2] = 1.0f;
		}
		else
		{
			int index = StsMath.arrayIndexBelow(m, points, 3);
			index = StsMath.minMax(index, 0, points.length - 2);
			slope = StsPoint.subPointsStatic(points[index + 1], points[index]);
			slope.normalizeXYZ();
		}
		return slope;
	}

	/** returns a point at Z with all coordinates: x, y, z, mdepth, time */
	public StsPoint getLinePointAtZ(float z, boolean extrapolate)
	{
		return getLineVectorSet().getXYZorTPoint(z, extrapolate, true);
	}

	public float[] getMDepthFloats()
	{
		StsLineVectorSet lineVectorSet = getLineVectorSet();
		if(lineVectorSet == null) return null;
		if(lineVectorSet.getVectorsSize() < 2) return null;
		return lineVectorSet.getMFloats();
	}

	public float[] getDepthFloats()
	{
		StsLineVectorSet lineVectorSet = getLineVectorSet();
		if(lineVectorSet == null) return null;
		if(lineVectorSet.getVectorsSize() < 2) return null;
		return lineVectorSet.getZFloats();
	}

	public StsLineVectorSet getLineVectorSet() { return (StsLineVectorSet)vectorSet; }
	public void setLineVectorSet(StsLineVectorSet vectorSet) { this.vectorSet = vectorSet; }
	/** Given a depthVector, return corresponding mdepth values using the depth and mdepth vectors of the well.
	 *  The assumption here is that the well's depth vector is monotonically increasing; if not.....
	 */
	public float[] computeMDepthsFromDepths(float[] depths)
	{
		return getLineVectorSet().computeMDepthsFromDepths(depths);
	}

	public float[] computeDepthsFromMDepths(float[] mdepths)
	{
		return getLineVectorSet().computeDepthsFromMDepths(mdepths);
	}

	public boolean checkComputeXYLogVectors(StsLogVectorSet logVectorSet)
	{
		return getLineVectorSet().checkComputeXYLogVectors(logVectorSet);
	}

	public String getAsciiDirectoryPathname()
	{
		return getProject().getAsciiDirectoryPathname(StsLine.getClassSubDirectoryString(), name);
	}

	public String getBinaryDirectoryPathname()
	{
		return getProject().getBinaryDirectoryPathname(StsLine.getClassSubDirectoryString(), name);
	}

	public boolean addVectorSetToObject(StsVectorSet vectorSet)
	{
		StsLineVectorSet lineVectorSet = (StsLineVectorSet)vectorSet;
		setTimeVectorSet(lineVectorSet);
		if(!lineVectorSet.checkAddVectors(true)) return false;
		if(lineVectorSet.getTVector() != null)
			setZDomainSupported(StsProject.TD_TIME_DEPTH);
		initializeBoundingBox();
		lineVectorSet.addToModel();
		return true;
	}

	public void setIsVertical(boolean isVertical)
	{
		this.isVertical = isVertical;
	}

	public boolean getIsVertical()
	{
		return isVertical;
	}

	public StsPoint getXYZPointAtZorT(float z, boolean extrapolate)
	{
		return getXYZorTPoint(z, extrapolate, isDepth);
	}

	/**
	 * Line vertices have 5 coordinates (x, y, z, mDepth, time). Return a new Point
	 * at z with 3 coordinates. z is either z or time, depending on isDepth.
	 *
	 * @param z           float
	 * @param extrapolate boolean
	 * @return StsPoint
	 */
	public StsPoint getXYZorTPoint(float z, boolean extrapolate, boolean isDepth)
	{
		StsPoint point;
		int i = 0;
		float z0 = 0.0f, z1 = 0.0f;
		return getLineVectorSet().getXYZorTPoint(z, extrapolate, isDepth);
	}

	public float[] getXyZOrTAtDepth(float z, boolean extrapolate)
	{
		StsPoint point;
		int i = 0;
		float z0 = 0.0f, z1 = 0.0f;
		return getLineVectorSet().getXyzOrXytAtDepth(z, extrapolate);
	}

	public StsPoint getPointAtZorT(float zt, boolean extrapolate, boolean isDepth)
	{
		return getLineVectorSet().getXYZorTPoint(zt, extrapolate, isDepth);
	}

	public StsPoint getPointAtZ(float z, boolean extrapolate)
	{
		return getXYZorTPoint(z, extrapolate, true);
	}

	public StsPoint getPointAtIndexF(StsAbstractFloatVector.IndexF indexF)
	{
		return getLineVectorSet().computeInterpolatedCoorPoint(indexF);
	}

	public float[] getCoordinatesAtMDepth(float m, boolean extrapolate)
	{
		return getLineVectorSet().getCoordinatesAtMDepth(m, extrapolate);
	}
}
