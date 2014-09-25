package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/10/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsLogVectorSet extends StsLineVectorSet
{
	/** operation to perform when real-time data is loaded */
	transient byte computeOperation = COMPUTE_NONE;

	static final byte COMPUTE_NONE = 01;
	static final byte COMPUTE_MDEPTH = 1;
	static final byte COMPUTE_DEPTH = 2;

	public StsLogVectorSet()
    {
    }

    public StsLogVectorSet(boolean persistent)
    {
		super(persistent);
    }

	public StsLogVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(vectorSetLoader);
	}

	public StsLogVectorSet(String dataSource, long sourceCreateTime, String group, String name)
	{
		super(dataSource, sourceCreateTime, group, name);
	}
/*
	static public StsLogVectorSet constructor(StsLogCurvesLoader vectorSetLoader)
	{
		StsLogVectorSet vectorSet = new StsLogVectorSet(vectorSetLoader);
		//StsNameSet fileNameSet = vectorSetLoader.nameSet;
		//String name = vectorSetLoader.name;
		if(!vectorSet.constructDataVectors(vectorSetLoader)) return null;
		return vectorSet;
	}
*/
    static public StsLogVectorSet dbConstructor(int capacity)
    {
        StsLogVectorSet logVectorSet = new StsLogVectorSet();
        logVectorSet.initializeCoorDbVectors(StsLoader.xyztmVectorNames, capacity);
        return logVectorSet;
    }
/*
    static public StsLogVectorSet construct(StsSurfaceVertex[] surfaceVertices)
    {
        int nValues = surfaceVertices.length;
        StsLogVectorSet logVectorSet = StsLogVectorSet.dbConstructor(nValues);
        for(int n = 0; n < nValues; n++)
            logVectorSet.addXyztPoint(surfaceVertices[n].getPoint());
        return logVectorSet;
    }
*/

	static public StsLogVectorSet copy(StsCoorTimeVectorSet coorTimeVectorSet)
	{
		StsLogVectorSet logVectorSet = new StsLogVectorSet(false);
		StsToolkit.copy(coorTimeVectorSet, logVectorSet);
		return logVectorSet;
	}

	public boolean initialize(StsModel model)
	{
		coorVectorSetObject = getLineVectorSetObject();
		if(!initialize()) return false;
		return checkLoadBinaryFiles(false);
	}

	public boolean checkVectors()
	{
		try
		{
			//setProjectObject(well);
			StsAbstractFloatVector mVector = getMVector();
			StsAbstractFloatVector zVector = getZVector();
			if (mVector == null && zVector == null)
                return false;

			if (mVector == null && vectorSetObject != null)
			{
				computeOperation = COMPUTE_MDEPTH;
				StsFloatTransientValuesVector mVectorTransient = new StsFloatMdLogVector();
				setMVector(mVectorTransient);
				addFloatTransientValuesVector(mVectorTransient);
			}
			else if (zVector == null && vectorSetObject != null)
			{
				computeOperation = COMPUTE_DEPTH;
				StsLineVectorSet lineVectorSet = getLineVectorSetObject().getLineVectorSet();
				float[] depths = lineVectorSet.computeDepthsFromMDepths(getMVector().getValues());
				StsFloatTransientVector zVectorTransient =  new StsFloatTransientVector(StsLoader.Z, depths);
				coorVectors[COL_Z] = zVectorTransient;
				zVectorTransient.checkMonotonic();
				zVectorTransient.setMinValue(depths[0]);
				zVectorTransient.setMaxValue(depths[depths.length-1]);
				addFloatTransientValuesVector(zVectorTransient);
			}
			return true;
		}
		catch (Exception e)
		{
			StsException.outputException("StsWellKeywordIO.checkVectors() failed.", e, StsException.WARNING);
			return false;
		}
	}

	public void processVectorSetChange(int firstIndex)
	{
		// if mdepth data exists in the loaded depthVectorSet, then the mVector will be a persisted type (StsFloatDataVector)
		// If it doesn't, then the mdepth is beimg computed from other vectors and is saved in memory and the project as a
		// a vector with non-peristed propertys (StsFloatVector) and must be generated again from depth when the project is reloaded
		if(computeOperation == COMPUTE_NONE) return;
		if(computeOperation == COMPUTE_MDEPTH)
		{
			StsWell well = (StsWell)vectorSetObject;
			StsLineVectorSet wellLineVectorSet = well.getLineVectorSet();
			float[] wellDepths = wellLineVectorSet.getZFloats();
			float[] wellMdepths = wellLineVectorSet.getMFloats();
			int nValues = getVectorsSize();
			StsAbstractFloatVector logMDepthVector = getMVector();
			float[] logDepths = getZFloats();
			float[] logMDepths = getMFloats();
			float[] newLogMDepths = new float[nValues];
			System.arraycopy(logMDepths, 0, newLogMDepths, 0, firstIndex-1);
			for(int n = firstIndex; firstIndex < nValues; n++)
				newLogMDepths[n] = StsMath.interpolateValue(logDepths[n], wellDepths, wellMdepths);
			logMDepthVector.checkSetValues(newLogMDepths);
		}
		else if(computeOperation == COMPUTE_DEPTH)
		{
			StsWell well = (StsWell)vectorSetObject;
			StsLineVectorSet wellLineVectorSet = well.getLineVectorSet();
			float[] wellDepths = wellLineVectorSet.getZFloats();
			float[] wellMdepths = wellLineVectorSet.getMFloats();
			int nValues = getVectorsSize();
			StsAbstractFloatVector logDepthVector = getZVector();
			float[] logDepths = getZFloats();
			float[] logMDepths = getMFloats();
			float[] newLogDepths = new float[nValues];
			System.arraycopy(logDepths, 0, newLogDepths, 0, firstIndex-1);
			for(int n = firstIndex; firstIndex < nValues; n++)
				newLogDepths[n] = StsMath.interpolateValue(logMDepths[n], wellMdepths, wellDepths);
			logDepthVector.checkSetValues(newLogDepths);
		}
		// well.checkComputeXYLogVectors(this);
	}
/*
	public boolean constructDataVectors(StsVectorSetLoader vectorSetLoader)
	{
		StsNameSet fileNameSet = vectorSetLoader.nameSet;
		String name = vectorSetLoader.getStsMainObject().getName();
		return constructDataVectors(name, fileNameSet);
	}
*/
	public boolean constructDataVectors(String name, StsNameSet fileNameSet)
	{
		if(!initializeDataVectors(name, fileNameSet)) return false;

		StsAbstractFloatVector 	zVector = getVectorOfType(COL_Z);
		StsAbstractFloatVector 	mVector = getVectorOfType(M);
		if(zVector == null && mVector == null)
		{
			StsMessageFiles.errorMessage(this, "constructDataVectors", " Didn't find  depth or mdepth vector in file: " + name);
			return false;
		}
		return true;
	}

	public boolean initializeVectors(StsVectorSetLoader loader, boolean initializeValues)
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			vector.initialize(loader, initializeValues);
		return true;
	}

	/* Create log curves from value vectors.  Constructor initializes logCurves to non-persistent,  so they subsequently must be added to the model. */
	public boolean createLogCurves(StsWell well)
	{
		ArrayList<StsLogCurve> logCurves = new ArrayList<StsLogCurve>();

		for (StsAbstractFloatVector valueVector : getValueVectors())
		{
			//valueVector.setMinMaxAndNulls(StsParameters.nullValue);
			StsLogCurve logCurve = StsLogCurve.constructLogCurve(well, this, valueVector, getVersion());
			if(logCurve != null) logCurves.add(logCurve);
		}
		well.addLogCurves(logCurves.toArray(new StsLogCurve[0]));
		StsWellClass wellClass = (StsWellClass)currentModel.getStsClass(well);
		wellClass.checkInitializeLogCurve3dDisplays(well);
		return true;
	}

	public boolean checkAddVectors(boolean loadValues)
	{
		// If mdepth vector is null, we need to compute it from the depthVector (which cannot also be null).
		// This is a bogus operation if the depth vector is not monotonically increasing (we should warn user if it isn't).
		// So pass the depthVector to the well and get a mDepthVector back
		if(getMVector() == null)
		{
			StsFloatMdLogVector mVector = new StsFloatMdLogVector(this);
			setMVector(mVector);
			addFloatTransientValuesVector(mVector);
		}
		StsAbstractFloatVector zVector = getZVector();
		if(zVector == null)
		{
			zVector = new StsFloatDepthLogVector(this);
			setZVector(zVector);
		}
		return true;
	}

	protected boolean skipHeader(StsAbstractFile file)
	{
		String line;
		try
		{
			while ((line = file.readLine()) != null)
			{
				line = line.trim();
				if (line.equals("")) continue;  // blank line
				//line = StsStringUtils.deTabString(line);
				if(line.endsWith(StsLoader.VALUE)) return true;
			}
			StsException.systemError(this, "skipHeader", "Failed to find keyword VALUE in file " + file.getPathname());
			return false;
		}
		catch(Exception e)
		{
			StsException.systemError(this, "skipHeader", "Failed to skip header for file " + file.getPathname());
			return false;
		}
	}

	/** Check if a seismic time vector can be built from either a velocityModel or from the well.lineVectorSet. */
	public boolean checkBuildTimeVector(StsLineVectorSet wellLineVectorSet)
	{
		StsAbstractFloatVector tVector = getTVector();
		if(tVector != null) return true;
		//if(checkBuildTimeVectorFromVelocityModel()) return true;
		if(!wellLineVectorSet.checkComputeWellMDepthVector()) return false;
		float[] wellMDepths = wellLineVectorSet.getMFloats();
		if(wellMDepths == null) return false;
		float[] wellTimes = wellLineVectorSet.getTFloats();
		if(wellTimes == null) return false;
		// if(!checkComputeWellMDepths()) return false;
		float[] logMDepths = getMFloats();
		int nValues = getVectorsSize();

		float[] tFloats = new float[nValues];
		for (int n = 0; n < nValues; n++)
			tFloats[n] = StsLogCurve.getInterpolatedValue(wellMDepths, wellTimes, logMDepths[n]);
		tVector = new StsFloatTransientVector(StsLoader.T, tFloats);
		setTVector(tVector);
		tVector.addToModel();
		tVector.setMinValue(tFloats[0]);
		tVector.setMaxValue(tFloats[tFloats.length-1]);
		dbFieldChanged("coorVectors", coorVectors);
		return true;
    }

	public int getVectorsSize()
	{
		StsAbstractFloatVector mVector = getMVector();
		if(mVector == null) return 0;
		return mVector.getSize();
	}
}
