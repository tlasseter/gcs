package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.DBTypes.*;
import com.Sts.PlugIns.Model.Types.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 1/3/12
 */

public class StsLineVectorSet extends StsCoorTimeVectorSet
{
	static public int M = 4;

	public StsLineVectorSet() { }

    public StsLineVectorSet(boolean persistent) { super(persistent); }

	public StsLineVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(vectorSetLoader);
	}

	public StsLineVectorSet(String dataSource, long sourceCreateTime, String group, String name)
	{
		super(dataSource, sourceCreateTime, group, name);
	}

    public StsLineVectorSet(String group, String name)
    {
        super(group, name);
    }

    static public StsLineVectorSet constructor()
    {
        return new StsLineVectorSet();
    }

	static public StsLineVectorSet constructor(StsTimeVectorSetLoader timeVectorSetLoader)
	{
		StsLineVectorSet vectorSet = new StsLineVectorSet(timeVectorSetLoader);
		if(!vectorSet.constructDataVectors(timeVectorSetLoader)) return null;
		return vectorSet;
	}

    static public StsLineVectorSet dbConstructor()
    {
        StsLineVectorSet lineVectorSet = new StsLineVectorSet();
        lineVectorSet.initializeCoorDbVectors(StsLoader.xyztmVectorNames, capacity);
        return lineVectorSet;
    }

    static public StsLineVectorSet constructor(String group, String name)
    {
        StsLineVectorSet lineVectorSet = new StsLineVectorSet(group, name);
        lineVectorSet.initializeCoorDataVectors(StsLoader.xyztmVectorNames, capacity);
        return lineVectorSet;
    }

    static public StsLineVectorSet construct(StsSurfaceVertex[] surfaceVertices)
    {
        int nValues = surfaceVertices.length;
        StsLineVectorSet lineVectorSet = StsLineVectorSet.dbConstructor();
        for(int n = 0; n < nValues; n++)
            lineVectorSet.addXyztPoint(surfaceVertices[n].getPoint());
        return lineVectorSet;
    }

	public boolean initializeDataVectors(String name, StsNameSet fileNameSet)
	{
		try
		{
			nCoorVectors = 5;
			ArrayList<StsAbstractFloatVector> valueVectorList = new ArrayList<StsAbstractFloatVector>();
			boolean hasClockTime = false;
			coorVectors = new StsAbstractFloatVector[nCoorVectors];

			for(StsColumnName columnName : fileNameSet)
			{
				if(StsLoader.isTimeOrDateColumn(columnName))
				{
					hasClockTime = true;
					continue;
				}
				StsFloatDataVector vector = new StsFloatDataVector(this, columnName);
				int vectorSetColumnIndex = columnName.columnIndexFlag;
				if(vectorSetColumnIndex >= 0)
					coorVectors[vectorSetColumnIndex] = vector;
				else
					valueVectorList.add(vector);
			}
			if(hasClockTime)
				clockTimeVector = StsLoader.checkConstructClockTimeVector(this, fileNameSet);
			setValueVectors(valueVectorList.toArray(new StsAbstractFloatVector[0]));
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "constructDataVectors", e);
			return false;
		}
	}

	/** Object containing this xyztmVectorSet */
	public StsLineVectorSetObject getLineVectorSetObject()
	{
		return (StsLineVectorSetObject)vectorSetObject;
	}

	public StsLineVectorSet getLineVectorSet()
	{
		return getLineVectorSetObject().getLineVectorSet();
	}

	public StsAbstractFloatVector[] getLineVectors()
	{
		StsAbstractFloatVector[] lineVectors = new StsAbstractFloatVector[nCoorVectors];
		System.arraycopy(coorVectors, 0, lineVectors, 0, 4);
		return lineVectors;
	}

	public float[] getMFloats()
    {
		StsAbstractFloatVector mVector = getMVector();
		if(mVector == null) return null;
        return mVector.getValues();
    }

	public boolean checkComputeMDepths(int firstIndex)
	{
		if(getMVector() != null) return true;
		float[] mdepths = computeMDepths(firstIndex);
		setMVectorValues(mdepths);
		return true;
	}

	public boolean checkComputeWellMDepthVector()
	{
		StsAbstractFloatVector mVector = getMVector();
		if(mVector != null) return true;
		float[] mdepths = computeMDepths();
		if(mdepths == null) return false;
		mVector = new StsFloatMdWellVector(this);
		mVector.setValues(mdepths);
		setMVector(mVector);
		return mVector.hasValues();
	}

	public StsAbstractFloatVector checkGetWellMDepthVector()
	{
		if(!checkComputeWellMDepthVector()) return null;
		return getMVector();
	}

	public void processVectorSetChange(int firstIndex)
	{
		StsProject project = getCurrentProject();
		StsLineVectorSetObject lineVectorSetObject = getLineVectorSetObject();
		if(!project.checkSetOrigin(getXOrigin(), getYOrigin()))
			adjustRotatedPoints(firstIndex);
		if(getMVector() instanceof StsFloatMdWellVector)
			lineVectorSetObject.checkComputeMDepths(firstIndex);
		lineVectorSetObject.adjustBoundingBox();
		project.addToProjectUnrotatedBoundingBox(lineVectorSetObject);
	}

	public boolean setVectorUnits(StsTimeVectorSetLoader loader)
	{
		super.setVectorUnits(loader);
		StsAbstractFloatVector mVector = getMVector();
		if(mVector != null) mVector.setUnits(loader.vUnits);
		return true;
	}

	public StsAbstractFloatVector getMVector()
	{
		return coorVectors[M];
	}

	public void setMVector(StsAbstractFloatVector mVector)
    {
        coorVectors[M] = mVector;
		dbFieldChanged("coorVectors", coorVectors);
    }

	public boolean setMVectorValues(float[] mdepths)
    {
		StsAbstractFloatVector mVector = getMVector();
		if(mVector == null)
		{
			StsException.systemError(this, "setMVectorValues", "mVector is null");
			return false;
		}
		mVector.checkSetValues(mdepths);
		return true;
    }

	public float getMValue(int index)
    {
		return getMVector().getValue(index);
    }

	public void setMD(float value, int index)
    {
        getMVector().setValue(value, index);
    }

	public float getDepthFromMDepth(float m, boolean extrapolate)
    {
        return getValueAtValue(m, getMVector(), getZVector(), extrapolate);
    }

	public float getMDepthFromDepth(float depth, boolean extrapolate)
    {
        return getValueAtValue(depth, getZVector(), getMVector(), extrapolate);
    }

	public float getMDepthFromTime(float time, boolean extrapolate)
    {
        return getValueAtValue(time, getTVector(), getMVector(), extrapolate);
    }

	public float[] getCoordinatesAtMDepth(float m, boolean extrapolate)
    {
        return getFloatsAtCoordinate(M, m, extrapolate);
    }

	public float[] getSlopeAtMDepth(float mdepth)
	{
		StsAbstractVector.IndexF indexF = getMVector().getIndexF(mdepth, false, false);
		if(indexF == null) return null;
		int index = indexF.index;
		if(index > getVectorsSize() -1) index = getVectorsSize() -2;
		StsAbstractFloatVector[] vectors = getLineVectors();
		float[] slope = subPoints(vectors, index, index + 1);
		float dmdepth = slope[M];
		for(int n = 0; n < slope.length; n++)
			slope[n] /= dmdepth;
		return slope;
	}

	public float[] computeDepthsFromMDepths(float[] mdepths)
	{
		initializeVectorSet();
		if(!checkComputeWellMDepthVector()) return null;
		float[] wellDepths = getZFloats();
		float[] wellMdepths = getMFloats();
		return StsMath.interpolateValues(mdepths, wellMdepths, wellDepths);
	}

	public float[] computeDepthsFromLogMDepths(StsLogVectorSet logVectorSet)
	{
		float[] mdepths = logVectorSet.getMFloats();
		return computeDepthsFromMDepths(mdepths);
	}

	public float[] computeMDepths()
	{
		return computeMDepths(0);
	}

	public float[] computeMDepths(int firstIndex)
	{
		int nPoints = 0;
		try
		{
			nPoints = getVectorsSize();
			float[] xFloats = getXFloats();
			float[] yFloats = getYFloats();
			float[] zFloats = getZFloats();
			float[] mdepths = new float[nPoints];
			float mdepth, x1, y1, z1;
			if(firstIndex == 0)
			{
				mdepth = 0.0f;
				x1 = xFloats[0];
				y1 = yFloats[0];
				z1 = zFloats[0];
			}
			else
			{
				float[] currentMDepths = getMFloats();
				if(currentMDepths == null)
				{
					StsException.systemError(this, "computeMDepths", "mdepths are null. firstIndex: " + firstIndex);
					return null;
				}
				else
				{
					if(!StsMath.copyFloats(currentMDepths, mdepths)) return null;
					mdepth = mdepths[firstIndex-1];
					x1 = xFloats[firstIndex-1];
					y1 = yFloats[firstIndex-1];
					z1 = zFloats[firstIndex-1];
				}
			}

			for (int n = firstIndex; n < nPoints; n++)
			{
				float x0 = x1;
				float y0 = y1;
				float z0 = z1;
				x1 = xFloats[n];
				y1 = yFloats[n];
				z1 = zFloats[n];
				float dx = x1 - x0;
				float dy = y1 - y0;
				float dz = z1 - z0;
				mdepth += (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
				mdepths[n] = mdepth;
			}
			return mdepths;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "computeMDepths", "Failed to construct mdepth from lineVectorSet " + name + " with nPoints: " + nPoints);
			return null;
		}
	}

	/** Given a depthVector, for example from logs, return corresponding mdepth values using the depth and mdepth vectors of the well.
	 *  The assumption here is that the well's depth vector is monotonically increasing; if not.....
	 */
	public float[] computeMDepthsFromDepths(float[] depths)
	{
		initializeVectorSet();
		if(!checkComputeWellMDepthVector()) return null;
		float[] wellDepths = getZFloats();
		float[] wellMdepths = getMFloats();
		return StsMath.interpolateValues(depths, wellDepths, wellMdepths);
	}

	public float[] computeMDepthsFromLogDepths(StsLogVectorSet logVectorSet)
	{
		float[] depths = logVectorSet.getZFloats();
		return computeMDepthsFromDepths(depths);
	}
	public boolean checkComputeXYLogVectors(StsLogVectorSet logVectorSet)
	{
		StsAbstractFloatVector logMVector = logVectorSet.getMVector();
		if(logMVector == null) return false;
		StsAbstractFloatVector logXVector = logVectorSet.getXVector();
		if(logXVector != null && logXVector.size == logMVector.size)
			return true;

		initializeVectorSet();

		float[] logMdepths = logMVector.getValues();
		if(logMdepths == null) return false;
		int logMVectorSize = logMdepths.length;

		float[] wellXFloats = getXFloats();
		float[] wellYFloats = getYFloats();
		StsAbstractFloatVector wellMVector = getMVector();

		float[] currentLogXFloats = logVectorSet.getXFloats();
		float[] logXFloats;
		float[] logYFloats;
		int currentSize = 0;
		if(currentLogXFloats == null)
		{
			logXFloats = new float[logMVectorSize];
			logYFloats = new float[logMVectorSize];
		}
		else
		{
			currentSize = logVectorSet.getXVector().size;
			logXFloats = StsMath.copyFloats(currentLogXFloats, logMVectorSize);
			float[] currentLogYFloats = logVectorSet.getYFloats();
			logYFloats = StsMath.copyFloats(currentLogYFloats, logMVectorSize);
		}
		StsAbstractVector.IndexF indexF = wellMVector.getIndexF(logMdepths[currentSize]);
		if(indexF == null) return false;
		float[] wellMdepths = wellMVector.getValues();
		int nWellValues = wellMdepths.length;
		if(nWellValues < 2) return false;
		int start = indexF.index;
		if(start == nWellValues-1) start--;
		float a0 = wellMdepths[start];
		float a1= wellMdepths[start+1];
		int n = currentSize;
		for(; n < logMVectorSize; n++)
		{
			float a = logMdepths[n];
			if(a0 > a)
			{
				while(a0 > a)
				{
					if(start == 0) break;
					a1 = a0;
					start--;
					a0 = wellMdepths[start];
				}
			}
			if(a1 <= a)
			{
				while(a1 <= a)
				{
					if(start == nWellValues-2) break;
					a0 = a1;
					start++;
					a1 = wellMdepths[start+1];
				}
			}
			float f = (a - a0)/(a1 - a0);
			float x0 = wellXFloats[start];
			float x1 = wellXFloats[start+1];
			logXFloats[n] =  x0 + f*(x1 - x0);
			float y0 = wellYFloats[start];
			float y1 = wellYFloats[start+1];
			logYFloats[n] =  y0 + f*(y1 - y0);
		}
		if(n < logMVectorSize)
		{
			logXFloats = StsMath.trimArray(logXFloats, n);
			logYFloats = StsMath.trimArray(logYFloats, n);
		}
		logVectorSet.setXVector(new StsFloatTransientVector(StsLoader.X, logXFloats));
		logVectorSet.setYVector(new StsFloatTransientVector(StsLoader.Y, logYFloats));
		return true;
	}
	/** A tdVectorSet is available, so build a timeVector and add to lineVectorSet, unless a velocityModel exists. */
	public boolean checkBuildTimeVectorFromTdCurve(StsTdVectorSet tdVectorSet)
	{
		//if(checkBuildTimeVectorFromVelocityModel()) return true;
		if(!checkComputeWellMDepthVector()) return false;
		float[] tdMDepths = tdVectorSet.getMFloats();
		if(tdMDepths == null) return false;
		float[] tdTimes = tdVectorSet.getTFloats();
		if(tdTimes == null) return false;
		float[] wellMDepths = getMFloats();
		if(wellMDepths == null) return false;

		int nValues = getVectorsSize();

		float[] tFloats = new float[nValues];
		for (int n = 0; n < nValues; n++)
			tFloats[n] = StsLogCurve.getInterpolatedValue(tdMDepths, tdTimes, wellMDepths[n]);
		StsAbstractFloatVector tVector = getTVector();
		if(tVector == null)
		{
			tVector = new StsFloatTransientVector(StsLoader.T, tFloats);
//			setTVector(tVector);
//			tVector.addToModel();
//			dbFieldChanged("coorVectors", coorVectors);
		}
//        else
            setTVector(tVector);
		tVector.setMinValue(tFloats[0]);
		tVector.setMaxValue(tFloats[tFloats.length-1]);
		return true;
    }

    public boolean projectToSection(StsSection onSection)
    {
        StsPoint point = new StsPoint();
        StsSectionPoint sectionPoint = new StsSectionPoint();
        for(int n = 0; n < getNValues(); n++)
        {
            sectionPoint.point = getXYZorTPoint(n, point);
            if(!onSection.computeNearestPoint(sectionPoint)) continue;
            setXYZorT(sectionPoint.nearestPoint, n);
        }
        //TODO need to add a methodCmd here to change the vectorLineSet point or a dbFieldChanged for the complete set
        //TODO also possible that we project to section when the corresponding line is initialized and projected onto section
        return true;
    }

	public StsPoint getSlopeAtMDepthPoint(float mdepth)
	{
		float[] slope = getSlopeAtMDepth(mdepth);
		return new StsPoint(slope);
	}

    public boolean computeExtendedPoints(float zMin, float zMax)
    {
        try
        {
            float zTop = getZorT(0);
            if(zMin != nullValue && zMin < zTop)
            {
                float[] interpolatedPoint = getExtrapolatedPoint(0, zMin);
                if(interpolatedPoint != null) insertValuePointBefore(interpolatedPoint, 0);
            }
            int nValues = getVectorsSize();
            float zBot = getZorT(nValues-1);
            if(zMax != nullValue && zMax > zBot)
            {
                float[] interpolatedPoint = getExtrapolatedPoint(nValues-2, zMax);
                if(interpolatedPoint != null) addCoorPoint(interpolatedPoint);
            }
            return true;
        }
        catch(Exception e)
        {
            StsException.outputException("StsLine.computeProjectPoints() failed.", e, StsException.WARNING);
            return false;
        }
    }

	public float[] getExtrapolatedPoint(int index, float z)
    {
        int zOrT = isDepth ? COL_Z : COL_T;
        float zt0 = coorVectors[zOrT].getValue(index);
        float zt1 = coorVectors[zOrT].getValue(index+1);
        float f = (z - zt0)/(zt1 - zt0);
        if(index == 0 && f < 0.0f)
        {
            float[] slope = subPoints(coorVectors, 1, 0);
            return multByConstantAddPoint(0, slope, f, coorVectors);
        }
        int nValues = getVectorsSize();
        if(index == nValues-2 && f > 1.0f)
        {
            float[] slope = subPoints(coorVectors, nValues-1, nValues-2);
            return multByConstantAddPoint(nValues-2, slope, f, coorVectors);
        }
        else
            return null;
    }

	public StsPoint[] getCoorsAsPoints()
	{
		int nPoints = getVectorsSize();
		StsPoint[] points = new StsPoint[nPoints];
		for(int i = 0; i < nPoints; i++)
			points[i] = getCoorPoint(i);
		return points;
	}

}
