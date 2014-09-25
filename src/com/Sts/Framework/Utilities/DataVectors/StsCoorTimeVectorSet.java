package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/10/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsCoorTimeVectorSet extends StsTimeVectorSet
{
	/** xyzt vectors */
	protected StsAbstractFloatVector[] coorVectors;
	/** currently used only on export, but will be needed for property interpolation in global coordinates (perhaps) */
	transient StsFloatTransientVector unrotatedXFloatValues;
	/** currently used only on export, but will be needed for property interpolation in global coordinates (perhaps) */
	transient StsFloatTransientVector unrotatedYFloatValues;
	/** xyzt object associated with this vectorSet: well, microseismic.  This object may differ from the object owning this vector,
	    i.e., a logCurve has a vectorSet of curve values, but the xyzt vectors are defined by the well it belongs to. */
	transient StsCoorTimeVectorSetObject coorVectorSetObject;
	/** number of vectors in this coordinate vector set.  set in initializeDataVectors method in this and subclasses.  */
	transient public int nCoorVectors = 4;
	/** an array of the vectorSet values */
	transient float[][] coorFloats;

	/** standard column index of X. */
    static public int COL_X = 0;
	/** standard column index of Y. */
    static public int COL_Y = 1;
	/** standard column index of DEPTH. */
    static public int COL_Z = 2;
	/** standard column index of SEISMIC_TIME. */
    static public int COL_T = 3;

	public StsCoorTimeVectorSet()
    {
    }

	public StsCoorTimeVectorSet(boolean persistent)
    {
		super(persistent);
    }

	public StsCoorTimeVectorSet(String dataSource, long sourceCreateTime, String group, String name)
	{
		super(dataSource, sourceCreateTime, group, name);
	}

	public StsCoorTimeVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(vectorSetLoader);
		setProjectObject(vectorSetLoader.getVectorSetObject());
	}

	static public StsCoorTimeVectorSet constructor(StsCoorTimeVectorSetLoader vectorSetLoader)
	{
		StsCoorTimeVectorSet vectorSet = new StsCoorTimeVectorSet(vectorSetLoader);
		//StsNameSet fileNameSet = vectorSetLoader.nameSet;
		//String name = vectorSetLoader.name;
		if(!vectorSet.constructDataVectors(vectorSetLoader)) return null;
		return vectorSet;
	}

	public boolean initialize(StsModel model)
	{
		if(!initialize()) return false;
		return checkLoadBinaryFiles(true);
	}

	//TODO xyztmVectorSet.xyztmVectorSetObject is not necessarily a well; subclass for an StsWellVectorSet perhaps
/*
	public void setProjectObject(StsXyztVectorSetObject vectorSetObject)
	{
		if(!StsXyztVectorSetObject.class.isAssignableFrom(vectorSetObject.getClass()))
		{
			StsException.systemError(this, "setProjectObject", "VectorSetObject should be an StsxyztmVectorSetObject, but is an " + vectorSetObject.getSimpleClassname());
			return;
		}
		this.vectorSetObject = vectorSetObject;
		if(!(vectorSetObject instanceof StsWell))
			StsException.systemError(this, "setProjectObject", "Need to subclass StsxyztmVectorSet to StsWellVectorSet!");
		else
			xyztVectorSetObject = (StsWell) vectorSetObject;
	}
*/
	public boolean constructDataVectors(String name, StsNameSet fileNameSet)
	{
		initializeDataVectors(name, fileNameSet);

		StsAbstractFloatVector xVector = getXVector();
		if(xVector == null)
		{
			StsMessageFiles.errorMessage(this, "checktDeviationVector", " Didn't find  X vector in file: " + name);
			return false;
		}
		StsAbstractFloatVector yVector = getYVector();
		if(yVector == null)
		{
			StsMessageFiles.errorMessage(this, "checkSetDeviationVector", " Didn't find  Y vector in file: " + name);
			return false;
		}
		StsAbstractFloatVector zVector = getZVector();
		if(zVector == null)
		{
			StsMessageFiles.errorMessage(this, "checkSetDeviationVector", " Didn't find  depth vector in file: " + name);
			return false;
		}
		return true;
	}

	public boolean checkAddVectors(boolean loadValues)
	{
		return true;
	}

	public void processVectorSetChange(int firstIndex)
	{
		StsProject project = getCurrentProject();
		if(!project.checkSetOrigin(getXOrigin(), getYOrigin()))
			coorVectorSetObject.adjustRotatedPoints(firstIndex);
		coorVectorSetObject.adjustBoundingBox();
		project.addToProjectUnrotatedBoundingBox(coorVectorSetObject);
	}

	public boolean addVectorSetToObject()
	{
		if(clockTimeVector != null) clockTimeVector.initializeTimeIndex();
		coorVectorSetObject.addVectorSetToObject(this);
		return true;
	}

	public boolean initializeVectors(StsVectorSetLoader loader, boolean initializeValues)
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			vector.initialize(loader, initializeValues);
		initializeOriginAndOffsets(loader);
		return true;
	}
/*
	protected boolean assignVectors()
	{
		setValueVectors(new StsAbstractFloatVector[this.getValueVectors().length]);
		xyztVectors = new StsAbstractFloatVector[5];
		int nValueVectors = 0;
		int type= -1;
		for(StsAbstractVector vector : this.getValueVectors())
		{
			if(vector == null) continue;
			if(!assignCoorVector(vector))
				getValueVectors()[nValueVectors++] = (StsAbstractFloatVector)vector;
		}
		setValueVectors((StsAbstractFloatVector[])StsMath.trimArray(getValueVectors(), nValueVectors));
		return true;
	}
*/
	protected boolean assignCoorVector(StsAbstractVector vector)
	{
		String name = vector.name;
		if(name == StsLoader.X)
		{
			coorVectors[COL_X] = (StsAbstractFloatVector)vector;
			return true;
		}
		else if(name == StsLoader.Y)
		{
			coorVectors[COL_Y] = (StsAbstractFloatVector)vector;
			return true;
		}
		else if(name == StsLoader.DEPTH)
		{
			coorVectors[COL_Z] = (StsAbstractFloatVector)vector;
			return true;
		}
		else if(name == StsLoader.SEISMIC_TIME)
		{
			coorVectors[COL_T] = (StsAbstractFloatVector)vector;
			return true;
		}
		return false;
	}

	public boolean checkVectors()
	{
		return getXVector() != null && getYVector() != null && getZVector() != null;
	}

    public float[][] getCoorVectorFloats()
    {
		int length = coorVectors[0].getSize();
		if(valueFloats != null && valueFloats[0].length == length) return valueFloats;
        valueFloats = new float[nCoorVectors][];
        for(int n = 0; n < nCoorVectors; n++)
            valueFloats[n] = getVectorFloats(n);
        return valueFloats;
    }

	public boolean initializeVectorSet()
	{
		if(initializedVectorSet) return true;
        if(!checkLoadVectors()) return false;
		// if(!checkComputeWellMDepthVector()) return false;
		// if(!checkComputeRotatedPoints()) return false;
		initializedVectorSet = true;
		return true;
	}

	public boolean initializeDataVectors(String name, StsNameSet fileNameSet)
	{
		try
		{
			nCoorVectors = 4;
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

	public ArrayList<StsDataVectorFace> getDataVectorArrayList()
	{
		ArrayList<StsDataVectorFace> dataVectorArrayList = super.getDataVectorArrayList();
		for(StsAbstractFloatVector xyztVector : coorVectors)
			if(xyztVector != null && xyztVector instanceof StsFloatDataVector) dataVectorArrayList.add((StsFloatDataVector)xyztVector);
		return dataVectorArrayList;
	}
/*
	public boolean checkBuildTimeVectorFromVelocityModel()
	{
		if(currentModel.getProject().getSeismicVelocityModel() == null) return false;

		int nValues = getVectorsSize();

		StsSeismicVelocityModel velocityModel = currentModel.getProject().getSeismicVelocityModel();
		float[] tFloats = velocityModel.computeTFloats(nValues, getXFloats(), getYFloats(), getZFloats());
		StsAbstractFloatVector tVector = getTVector();
		if(tVector == null)
			tVector = new StsFloatTransientVector(StsLoader.T, tFloats);
		tVector.setMinValue(tFloats[0]);
		tVector.setMaxValue(tFloats[tFloats.length-1]);
		setTVector(tVector);
		return true;
	}
*/
	public float[] getUnrotatedXFloats()
	{
		if(unrotatedYFloatValues == null) return null;
		return unrotatedYFloatValues.getValues();
	}

    public float[] getUnrotatedYFloats()
    {
		if(unrotatedXFloatValues == null) return null;
        return unrotatedXFloatValues.getValues();
    }

	public double getUnrotatedXOrigin() { return unrotatedXFloatValues.getOrigin(); }
	public double getUnrotatedYOrigin() { return unrotatedYFloatValues.getOrigin(); }

    public double[] getXAbsoluteRange()
    {
        return getAbsoluteRange(COL_X);
    }

    public double[] getYAbsoluteRange()
    {
        return getAbsoluteRange(COL_Y);
    }

    public float[] getXRelativeRange()
    {
        return getRelativeRange(COL_X);
    }

    public float[] getYRelativeRange()
    {
        return getRelativeRange(COL_Y);
    }

	public float distance(int index0, int index1)
    {
        float[] xFloats = getXFloats();
        float[] yFloats = getYFloats();
        float[] zFloats = getZFloats();

        double dx = xFloats[index0] - xFloats[index1];
        double dy = yFloats[index0] - yFloats[index1];
        double dz = zFloats[index0] - zFloats[index1];
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public boolean isVertical()
    {
        return getXVector().isConstant() && getYVector().isConstant();
    }
/*
    public boolean projectToSection(StsSection onSection)
    {
        StsPoint point = new StsPoint();
        StsSectionPoint sectionPoint = new StsSectionPoint();
        for(int n = 0; n < getVectorsSize(); n++)
        {
            sectionPoint.point = getXYZorTPoint(n, point);
            if(!onSection.computeNearestPoint(sectionPoint)) continue;
            setXYZorT(sectionPoint.nearestPoint, n);
        }
        //TODO need to add a methodCmd here to change the vectorLineSet point or a dbFieldChanged for the complete set
        //TODO also possible that we project to section when the corresponding line is initialized and projected onto section
        return true;
    }
*/
    public StsPoint getNearestPointOnLine(StsPoint point)
    {
        float indexF = getIndexF(point);
        return computeInterpolatedValuesPoint(indexF);
    }

    private void setPointXYZorT(StsPoint point, int index)
    {
        float[] xyz = point.v;
        xyz[0] = getX(index);
        xyz[1] = getY(index);
        if(isDepth)
            xyz[2] = getZ(index);
        else
            xyz[2] = getT(index);
    }

    /** Point contains xyz or xyt.  Transfer values to point at index. */
    protected void setXYZorT(StsPoint point, int index)
    {
        float[] xyz = point.v;
        setX(xyz[0], index);
        setY(xyz[1], index);
        if(isDepth)
            setZ(xyz[2], index);
        else
            setT(xyz[2], index);
    }

	public boolean checkComputeRelativePoints(StsBoundingBox unrotatedLineBoundingBox)
	{
		StsRotatedBoundingBox projectRotatedBoundingBox = getCurrentProject().rotatedBoundingBox;
		if(projectRotatedBoundingBox == null)
		{
			StsException.systemError(this, "checkComputeRotatedPoints", "projectRotatedBoundingBox shouldn't be null!");
			return false;
		}
		initializedVectorSet = checkComputeRelativePoints(unrotatedLineBoundingBox, 0);
		return initializedVectorSet;
	}

    /** This lineVectorSet defines the path thru the boundingBox.
     *  If the vectors are dataVectors (loaded from binary files, then convert X and Y vectors to rotated coordinate system (if angle != 0).
	 *  The boundingBox for this line remains an unrotated boundingBox
     */
    public boolean checkComputeRelativePoints(StsBoundingBox unrotatedLineBoundingBox, int firstIndex)
    {
        if(!checkLoadVectors()) return false;
        if(isDbVectorSet()) return false;
		StsProject project = getCurrentProject();
		if(project.originAndAngleSame(unrotatedLineBoundingBox)) return true;

		double[] unrotatedLineOrigin = unrotatedLineBoundingBox.getOrigin();
        float[] xArray = getXFloats();
        float[] yArray = getYFloats();

		int nValues = getVectorsSize();
        for(int n = firstIndex; n < nValues; n++)
        {
            float[] xy = project.getRotatedRelativeXYFromUnrotatedAbsXY(unrotatedLineOrigin[0] + xArray[n], unrotatedLineOrigin[1] + yArray[n]);
            xArray[n] = xy[0];
            yArray[n] = xy[1];
        }
        // resetBoundingBox(boundingBox);
        return true;
    }
    /** This lineVectorSet defines the path thru the boundingBox.
     *  If the vectors are dataVectors (loaded from binary files, then convert X and Y vectors to rotated coordinate system (if angle != 0).
	 *  The boundingBox for this line remains an unrotated boundingBox
     */
    public boolean adjustRotatedPoints(int firstIndex)
    {
		StsBoundingBox unrotatedLineBoundingBox = getCoorVectorSetObject();

		StsProject project = getCurrentProject();
		if(project.originAndAngleSame(unrotatedLineBoundingBox)) return true;

		double[] unrotatedLineOrigin = unrotatedLineBoundingBox.getOrigin();
        float[] xArray = getXFloats();
        float[] yArray = getYFloats();
		int nValues = getVectorsSize();

        for(int n = firstIndex; n < nValues; n++)
        {
            float[] xy = project.getRotatedRelativeXYFromUnrotatedAbsXY(unrotatedLineOrigin[0] + xArray[n], unrotatedLineOrigin[1] + yArray[n]);
            xArray[n] = xy[0];
            yArray[n] = xy[1];
        }
        initializedVectorSet = true;
        return true;
    }

	public boolean computeUnrotatedPoints(StsBoundingBox boundingBox)
    {
		return computeUnrotatedPoints(getCurrentProject().rotatedBoundingBox, boundingBox);
	}

	/** Make copies of the current X and Y vectors and unrotated/untranslate them to their original position.
	 *  If these are DB vectors, there is no translation and they remain referenced to the project origin.
	 * @param rotatedBoundingBox the rotatedBoundingBox defining the rotated coordinate system
	 * @param boundingBox the new boundingBox defined for this unrotated object.
	 * @return
	 */
    public boolean computeUnrotatedPoints(StsRotatedBoundingBox rotatedBoundingBox, StsBoundingBox boundingBox)
    {
        if(!checkLoadVectors()) return false;
        if(isDbVectorSet()) return true;

        StsAbstractFloatVector xVector = getXVector();
		unrotatedXFloatValues = new StsFloatTransientVector(xVector.getValues());
        double xOrigin = xVector.getOrigin();

        StsAbstractFloatVector yVector = getYVector();
        unrotatedYFloatValues = new StsFloatTransientVector(yVector.getValues());
        double yOrigin = yVector.getOrigin();

        double projectXOrigin = rotatedBoundingBox.getXOrigin();
        double projectYOrigin = rotatedBoundingBox.getYOrigin();
        float dXOrigin = (float) (xOrigin - projectXOrigin);
        float dYOrigin = (float) (yOrigin - projectYOrigin);
        float angle = rotatedBoundingBox.getAngle();

        if(dXOrigin == 0.0 && dYOrigin == 0.0 && angle == 0.0f) return false;
        unrotatedXFloatValues.setOrigin(projectXOrigin);
        unrotatedYFloatValues.setOrigin(projectYOrigin);
        float[] xArray = unrotatedXFloatValues.getValues();
        float[] yArray = unrotatedYFloatValues.getValues();

        for(int n = 0; n < getVectorsSize(); n++)
        {
            float[] xy = rotatedBoundingBox.getUnrotatedRelativeXYFromRotatedXY(xArray[n], yArray[n]);
			unrotatedXFloatValues.setValue(n, xy[0] - dXOrigin);
			unrotatedYFloatValues.setValue(n, xy[1] - dYOrigin);
        }
        resetBoundingBox(boundingBox);
        return true;
    }

    public void resetBoundingBox(StsBoundingBox boundingBox)
    {
        StsAbstractFloatVector xVector = getXVector();
		boundingBox.xOrigin = xVector.getOrigin();
        boundingBox.xMin = xVector.getMinValue();
        boundingBox.xMax = xVector.getMaxValue();
        StsAbstractFloatVector yVector = getYVector();
		boundingBox.yOrigin = yVector.getOrigin();
        boundingBox.yMin = yVector.getMinValue();
        boundingBox.yMax = yVector.getMaxValue();
    }

    public boolean isDbVectorSet()
    {
        if(coorVectors == null) return false;
        return coorVectors[0] instanceof StsFloatDbVector;
    }

    public void setPoints(StsPoint[] points)
    {
        int pointSize = points[0].getLength();
        int nPoints = points.length;
        for(int n = 0; n < nPoints; n++)
        {
            float[] xyztm = points[n].v;
            for(int i = 0; i < pointSize; i++)
                coorVectors[i].setValue(xyztm[i], n);
        }
    }

    public void setPoint(StsPoint point, int index, int pointSize)
    {
        float[] xyztm = point.v;
        for(int n = 0; n < pointSize; n++)
            coorVectors[n].setValue(xyztm[n], index);
    }

    public void setPoint(StsPoint point, int index)
    {
        float[] xyztm = point.v;
        int pointSize = xyztm.length;
        for(int n = 0; n < pointSize; n++)
            coorVectors[n].setValue(xyztm[n], index);
    }

	/** The assumption is that the X and Y vectors have already been rotated into the rotated coordinate system with the project.angle.
	 *  The origin is not changed under rotation and the X and Y vector values are offsets from the origin in the rotated coordinate system.
	 * @return the rotatedBoundingBox around this lineVector.
	 */
    public StsRotatedBoundingBox getRotatedBoundingBox()
    {
        float[] xRange = getXVector().getRelativeRange();
        float[] yRange = getYVector().getRelativeRange();
        float[] zRange = getZVector().getRelativeRange();
        float[] tRange = getTVector().getRelativeRange();
        double xOrigin = getXOrigin();
        double yOrigin = getYOrigin();
        float angle = currentModel.getProject().getAngle();
        return new StsRotatedBoundingBox(xOrigin, yOrigin, angle, xRange[0], xRange[1], yRange[0], yRange[1], zRange[0], zRange[1], tRange[0], tRange[1]);
    }

    public StsBoundingBox getUnrotatedBoundingBox()
    {
        float[] xRange = getXVector().getRelativeRange();
        float[] yRange = getYVector().getRelativeRange();
        float[] zRange = getZVector().getRelativeRange();
        float[] tRange = getTVector().getRelativeRange();
        double xOrigin = getXOrigin();
        double yOrigin = getYOrigin();
        return new StsBoundingBox(xOrigin, yOrigin, xRange[0], xRange[1], yRange[0], yRange[1], zRange[0], zRange[1], tRange[0], tRange[1]);
    }

    public double getXOrigin()
	{
		StsAbstractFloatVector xVector = getXVector();
		if(xVector == null) return StsParameters.nullDoubleValue;
		else return xVector.getOrigin();
	}

    public double getYOrigin()
	{
		StsAbstractFloatVector yVector = getYVector();
		if(yVector == null) return StsParameters.nullDoubleValue;
		else return yVector.getOrigin();
	}

    public double[] getOrigin()
	{
		if(this.getNValues() == 0) return null;
		return new double[] { getXOrigin(), getYOrigin() };
	}

	public float[] getLocalOrigin() { return new float[] { getValue(COL_X, 0), getValue(COL_Y, 0) };  }

	public float getZScalar()
	{
		return currentModel.getProject().getDepthScalar(getZVector().getUnits());
	}

	public float getXyScalar()
	{
		return currentModel.getProject().getXyScalar(getXVector().getUnits());
	}

	public int getVectorsSize()
	{
		if(coorVectors[COL_X] == null) return 0;
		return coorVectors[COL_X].getSize();
	}

	public boolean setVectorUnits(StsTimeVectorSetLoader loader)
	{
		StsAbstractFloatVector xVector = getXVector();
		if(xVector != null) xVector.setUnits(loader.hUnits);
		StsAbstractFloatVector yVector = getYVector();
		if(yVector != null) yVector.setUnits(loader.hUnits);
		StsAbstractFloatVector zVector = getZVector();
		if(zVector != null) zVector.setUnits(loader.vUnits);
		StsAbstractFloatVector tVector = getTVector();
		if(tVector != null) tVector.setUnits(loader.tUnits);
		return true;
	}

	protected void initializeOriginAndOffsets(StsLoader loader)
	{
		StsAbstractFloatVector xVector = getXVector();
		if(xVector != null)
		{
			xVector.setOrigin(loader.xOrigin);
			xVector.setOffsetFromOrigin(true);
		}
		StsAbstractFloatVector yVector = getYVector();
		if(yVector != null)
		{
			yVector.setOrigin(loader.yOrigin);
			yVector.setOffsetFromOrigin(true);
		}
	}

	public StsAbstractFloatVector getZorTVector()
    {
        if(isDepth)
            return getCoorVector(COL_Z);
        else
            return getCoorVector(COL_T);
    }

	public double[] getZAbsoluteRange()
    {
        return getAbsoluteRange(COL_Z);
    }

	public double[] getTAbsoluteRange()
    {
        return getAbsoluteRange(COL_T);
    }

	public float[] getZRelativeRange()
    {
        return getRelativeRange(COL_Z);
    }

	public float[] getTRelativeRange()
    {
        return getRelativeRange(COL_T);
    }

	public double[] getAbsoluteRange(int type)
	{
		if(coorVectors[type] == null) return null;
		return coorVectors[type].getAbsoluteRange();
	}

	public float[] getRelativeRange(int type)
	{
		if(coorVectors[type] == null) return null;
		return coorVectors[type].getRelativeRange();
	}

	public float getDepthFromTime(float t, boolean extrapolate)
    {
        return getValueAtValue(t, COL_T, COL_Z, extrapolate);
    }

	/** Not reliable for horizontal well */
    public StsPoint getPointAtDepth(float depth, boolean extrapolate)
    {
        float[] floats = getFloatsAtCoordinate(COL_Z, depth, extrapolate);
        return new StsPoint(floats);
    }

	public float[] getFloatsAtDepth(float z, boolean extrapolate)
    {
        return getFloatsAtCoordinate(COL_Z, z, extrapolate);
    }

	public float[] getFloatsAtCoordinate(int inputType, float value, boolean extrapolate)
    {
		return computeInterpolatedVectors(coorVectors, inputType, value, extrapolate);
    }

	public float[] getZOrTFloatsAtIndexF(StsAbstractFloatVector.IndexF indexF)
	{
		float[][] floats = this.getXyZOrTFloats();
		return computeInterpolatedFloats(floats, indexF);
	}

	public StsPoint getPointAtIndexF(StsAbstractFloatVector.IndexF indexF)
	{
		return computeInterpolatedPoint(coorVectors, indexF);
	}

    public StsPoint computeInterpolatedCoorPoint(StsAbstractFloatVector.IndexF indexF)
    {
        return computeInterpolatedPoint(coorVectors, indexF);
    }

    public StsPoint computeInterpolatedCoorPoint(int index)
    {
        return computeInterpolatedPoint(coorVectors, index);
    }

	/** return XYZ or T floats as 3 vectors, i.e. floats[3][] */
	public float[][] getZorT_3FloatVectors()
	{
		return getXYZorT_3FloatVectors(isDepth);
	}

	/** return XYZ or T floats as 3 vectors, i.e. floats[3][] */
	public float[][] getXYZorT_3FloatVectors(boolean isDepth)
	{
		float[] zOrTFloats = getZorTFloats(isDepth);
		if(zOrTFloats == null)
			return null;
		else
			return new float[][]{getXFloats(), getYFloats(), getZorTFloats(isDepth)};
	}

	/** return XYZ or T floats as n points, i.e. floats[n][3] */
	public float[][] getZorT_N3FloatVectors()
	{
		return getZorT_N3FloatVectors(isDepth);
	}

	/** return XYZ or T floats as nx3 2D array, i.e. floats[n][3] */
	public float[][] getZorT_N3FloatVectors(boolean isDepth)
	{
		float[] xFloats = getXFloats();
		float[] yFloats = getYFloats();
		float[] zOrTFloats = getZorTFloats(isDepth);
		int nValues = xFloats.length;
		float[][] xyzFloatPoints = new float[nValues][];
		for(int n = 0; n < nValues; n++)
			xyzFloatPoints[n] = new float[] { xFloats[n], yFloats[n], zOrTFloats[n] };
		return xyzFloatPoints;
	}

	public float[] getXYZorT(int index)
	{
	   return getXYZorT(isDepth, index);
	}

	public float[] getXYZorT(boolean isDepth, int index)
	{
		int zOrT = isDepth ? COL_Z : COL_T;
		return new float[] { getValue(COL_X, index), getValue(COL_Y, index), getValue(zOrT, index) };
	}

	public float[] getXYZorT(float ztValue, boolean extrapolate, boolean isDepth)
	{
		int zOrT = isDepth ? COL_Z : COL_T;
		StsAbstractFloatVector.IndexF indexF = coorVectors[zOrT].getIndexF(ztValue, extrapolate);
		StsAbstractFloatVector[] xyzVectors = getXYZorTVectors(zOrT);
		return computeInterpolatedVectorFloats(xyzVectors, indexF);
	}

	public StsAbstractFloatVector[] getXYZorTVectors(int zOrT)
	{
		StsAbstractFloatVector[] xyzOrTVectors = new StsAbstractFloatVector[3];
		xyzOrTVectors[COL_X] = getXVector();
		xyzOrTVectors[COL_Y] = getYVector();
		if(zOrT == COL_Z)
			xyzOrTVectors[COL_Z] = getZVector();
		else
			xyzOrTVectors[COL_Z] = getTVector();
		return xyzOrTVectors;
	}

	public StsPoint getXYZorTPoint(int index)
	{
		return getXYZorTPoint(index, new StsPoint());
	}

	public StsPoint getXYZorTPoint(int index, StsPoint point)
	{
		point.v = getXYZorT(isDepth, index);
		return point;
	}

	public StsPoint getXYZorTPoint(float ztValue, boolean extrapolate, boolean isDepth)
	{
		float[] v = getXYZorT(ztValue, extrapolate, isDepth);
		if(v == null) return null;
		return new StsPoint(v);
	}

	public float[] getXyzOrXytAtDepth(float depth, boolean extrapolate)
	{
		float[] xyztm = getFloatsAtDepth(depth, extrapolate);
		if(xyztm == null) return null;
		return StsPoint.getXYZorT(xyztm);
	}

	public StsAbstractFloatVector getCoorVector(int col)
	{
		if(coorVectors == null) return null;
		if(col >= coorVectors.length)
		{
			StsException.systemError(this, "getXyztVector", "coorVectors index " + col + " exceeds max Index " + (coorVectors.length - 1));
			return null;
		}
		return coorVectors[col];
	}

	public StsAbstractFloatVector getCoorVector(String name)
	{
		if(coorVectors == null) return null;
		for(StsAbstractFloatVector vector : coorVectors)
			if(vector.name.equals(name)) return vector;
		return null;
	}

	public StsAbstractFloatVector getXVector()
	{
		return getCoorVector(COL_X);
	}

	public StsAbstractFloatVector getYVector()
	{
		return getCoorVector(COL_Y);
	}

	public StsAbstractFloatVector getZVector()
	{
		return getCoorVector(COL_Z);
	}

	public StsAbstractFloatVector getTVector()
	{
		return getCoorVector(COL_T);
	}

	public void setXVector(StsAbstractFloatVector xVector)
    {
        coorVectors[COL_X] = xVector;
		dbFieldChanged("coorVectors", coorVectors);
    }

	public void setYVector(StsAbstractFloatVector yVector)
    {
        coorVectors[COL_Y] = yVector;
		dbFieldChanged("coorVectors", coorVectors);
    }

	public void setZVector(StsAbstractFloatVector zVector)
    {
        coorVectors[COL_Z] = zVector;
		dbFieldChanged("coorVectors", coorVectors);
    }

	public void setTVector(StsAbstractFloatVector tVector)
    {
        coorVectors[COL_T] = tVector;
		dbFieldChanged("coorVectors", coorVectors);
    }

	public float[] getXyztVectorValues(int col)
	{
		StsAbstractFloatVector vector = getCoorVector(col);
		if(vector == null) return null;
        return vector.getValues();
	}


	public float getXyztVectorValue(int col, int index)
	{
		StsAbstractFloatVector vector = getCoorVector(col);
		if(vector == null) return nullValue;
        return vector.getValue(index);
	}

	public float[] getXFloats()
	{
		return getXyztVectorValues(COL_X);
	}

	public float[] getYFloats()
	{
		return getXyztVectorValues(COL_Y);
	}

	public float[] getZFloats()
	{
		return getXyztVectorValues(COL_Z);
	}

	public float[] getTFloats()
    {
        return getXyztVectorValues(COL_T);
    }

	public float[] getZorTFloats()
    {
		return getZorTFloats(isDepth);
    }

	public float[] getZorTFloats(boolean isDepth)
    {
        if(isDepth) return getZFloats();
        else return getTFloats();
    }

	public float getX(int index)
	{
		return getValue(COL_X, index);
	}

	public float getY(int index)
	{
		return getValue(COL_Y, index);
	}

	public float getZ(int index)
	{
		return getValue(COL_Z, index);
	}

	public float getT(int index)
    {
        return getValue(COL_T, index);
    }

	public float getZorT(int index)
    {
        if(isDepth)
            return getValue(COL_Z, index);
        else
            return getValue(COL_T, index);
    }


	public void setX(float value, int index)
	{
		setXyztValue(COL_X, value, index);
	}

	public void setY(float value, int index)
	{
		setXyztValue(COL_Y, value, index);
	}

	public void setZ(float value, int index)
    {
        setXyztValue(COL_Z, value, index);
    }

	public void setT(float value, int index)
    {
        setXyztValue(COL_T, value, index);
    }

	public float getValue(int col, int index)
	{
		return getXyztVectorValue(col, index);
	}

	public void setXyztValue(int col, float value, int index)
	{
		StsAbstractFloatVector vector = getCoorVector(col);
		if(vector == null) return;
		vector.setValue(value, index);
	}

	public double getZOrigin() { return getZVector().origin; }

	public float[][] getXYZFloats()
	{
		return new float[][] { getXFloats(), getYFloats(), getZFloats() };
	}

	public float[][] getXyZOrTFloats()
	{
		if(isDepth)
			return new float[][] { getXFloats(), getYFloats(), getZFloats() };
		else
			return new float[][] { getXFloats(), getYFloats(), getTFloats() };
	}

	public float getIndexF(StsPoint point)
    {
        StsAbstractFloatVector floatVector;
        float zt;
        if(isDepth)
        {
            floatVector = getZVector();
            zt = point.getZ();
        }
        else
        {
            floatVector = getTVector();
            zt = point.getT();
        }
        return floatVector.getIndexFactor(zt);
    }

	/** Object containing this xyztmVectorSet */
	public StsCoorTimeVectorSetObject getCoorVectorSetObject()
	{
		return (StsCoorTimeVectorSetObject)vectorSetObject;
	}

	public void setCoorVectorSetObject(StsCoorTimeVectorSetObject coorVectorSetObject)
	{
		this.coorVectorSetObject = coorVectorSetObject;
	}

	public void initializeCoorDbVectors(String[] names, int capacity)
	{
		coorVectors = new StsFloatDbVector[nCoorVectors];
		for(int n = 0; n < nCoorVectors; n++)
			coorVectors[n] = new StsFloatDbVector(names[n], capacity);
	}

	public int addXyztPoint(StsPoint point)
    {
		return addCoorPoint(point.v);
    }

	/** append this point to the end; return the index of this last point */
	public int addCoorPoint(float[] coors)
    {
		int length = coors.length;
		for(int n = 0; n < length; n++)
			coorVectors[n].append(coors[n]);
		return length;
    }

    public StsPoint computeInterpolatedXyztPoint(StsAbstractFloatVector.IndexF indexF)
    {
        float[] v = computeInterpolatedVectorFloats(coorVectors, indexF);
		if(v == null) return null;
        return new StsPoint(v);
    }

	public StsPoint getFirstCoorPoint()
	{
		return getCoorPoint(0);
	}

	public StsPoint getLastCoorPoint()
	{
		return getCoorPoint(getVectorsSize()-1);
	}

    public StsPoint getCoorPoint(int i)
    {
		return computeInterpolatedPoint(coorVectors, i);
    }
	
	public StsAbstractFloatVector[] getCoorVectors()
	{
		return coorVectors;
	}

    public String[] getCoorVectorNames()
    {
        ArrayList<String> vectorNames = new ArrayList<>();
        for(StsAbstractFloatVector coorVector : coorVectors)
            if(coorVector != null) vectorNames.add(coorVector.getName());
        return vectorNames.toArray(new String[0]);
    }

    public float[] getCoorFloats(int i)
    {
		return computeInterpolatedVectorFloats(coorVectors, i);
    }

	public void deleteCoorPoint(int index)
	{
		for(int n = 0; n < nCoorVectors; n++)
			coorVectors[n].deletePoint(index);
	}

	public int insertCoorPointBefore(StsPoint point, int index)
	{
		return insertCoorPointBefore(point.v, index);
	}

	public int insertCoorPointBefore(float[] point, int index)
	{
		for(int n = 0; n < nCoorVectors; n++)
			coorVectors[n].insertBefore(point[n], index);
		return index;
	}

	public int insertCoorPointAfter(StsPoint point, int index)
	{
		return insertCoorPointAfter(point.v, index);
	}

	public int insertCoorPointAfter(float[] point, int index)
	{
		for(int n = 0; n < nCoorVectors; n++)
			coorVectors[n].insertAfter(point[n], index);
		return index + 1;
	}

	public boolean setCoorFloats(int col, float[] values)
	{
		coorVectors[col].checkSetValues(values);
		return true;
	}

	public float[][] getCoorFloats()
	{
		int length = getCoorVector(0).size;
		if(coorFloats != null && coorFloats[0].length == length) return coorFloats;
		coorFloats = new float[nCoorVectors][];
		for(int n = 0; n < nCoorVectors; n++)
			coorFloats[n] = getVectorFloats(n);
		return coorFloats;
	}

	private boolean checkCoorFloats()
	{
		if(coorFloats == null) return false;
		int length = getCoorVector(0).size;
		for(int n = 1; n < nCoorVectors; n++)
			if(getCoorVector(n).size < length) return false;
		return true;
	}

	public void getValueFloats(float[] coordinates, int index, int length)
	{
		for(int n = 0; n < length; n++)
			coordinates[n] = coorVectors[n].getValue(index);
	}

	public int getNValues()
	{
		if(coorVectors == null  || coorVectors[0] == null || coorVectors.length == 0) return 0;
		return coorVectors[0].getSize();
	}

	public int getSize()
	{
		return getNValues();
	}

	public String[] removeNullColumnNames(String[] columnNames)
	{
		if(coorVectors != null)
		{
			int nNonNull = 0;
			int nCoorVectors = coorVectors.length;
			// some of the coorVectors might be null
			String[] nonNullVectorNames = new String[nCoorVectors];
			for(int n = 0; n < nCoorVectors; n++)
				if(coorVectors[n] != null) nonNullVectorNames[nNonNull++] = coorVectors[n].name;
			columnNames = (String[])StsMath.arrayAddArray(columnNames, nonNullVectorNames, nNonNull);
		}
		return super.removeNullColumnNames(columnNames);
	}
}
