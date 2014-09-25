//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Wells.DBTypes;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.Types.*;

import javax.media.opengl.*;
import java.io.*;
import java.util.*;

public class StsLogCurve extends StsMainObject implements StsSelectable
{
//    protected StsWell well;
	/** Name of the well this logCurve belongs to */
	protected StsWell well;
	/** A specific name for this logCurve, different from its logCurveType name (curvename); not currently used */
	protected String logname;
	/** this is the logCurveType name; it could be the same as or an alias of an existing logCurveType name */
	protected String curvename;
	/** If edited, this log curve will created a new instance with the same name, different values, and an incremented version number */
	protected int version = 0;
	/** The type or family this log curve belongs to (GR, NPHI, etc). */
	protected StsLogCurveType logCurveType = null;
	/** logVectorSet from which this logCurve was constructed */
	protected StsLogVectorSet logVectorSet;
	/** The value vector for this log. */
	protected StsAbstractFloatVector valueVector = null;

	protected transient StsAbstractFloatVector mdepthVector = null;
	protected transient StsAbstractFloatVector depthVector = null;
	protected transient StsAbstractFloatVector timeVector = null;
	protected transient StsTimeVector clockTimeVector;

	transient protected String binaryDirectory;

	transient StsWellClass wellClass;

	static public final double doubleNullValue = StsParameters.nullDoubleValue;
	static public final String X = StsLoader.X;
	static public final String Y = StsLoader.Y;
	static public final String DEPTH = StsLoader.DEPTH;
	static public final String MDEPTH = StsLoader.MDEPTH;

	public StsLogCurve()
	{

	}

	public StsLogCurve(boolean persistent)
	{
		super(persistent);
	}

	public StsLogCurve(String name, boolean persistent)
	{
		super(persistent);
		setName(name);
	}

	public StsLogCurve(StsWell well, StsLogVectorSet logVectorSet, StsAbstractFloatVector valueVector, int version)
	{
		this.well = well;
		this.logVectorSet = logVectorSet;
		mdepthVector = logVectorSet.getMVector();
		depthVector = logVectorSet.getZVector();
		timeVector = logVectorSet.getTVector();
		this.valueVector = valueVector;
		clockTimeVector = logVectorSet.getClockTimeVector();
		this.version = version;
		curvename = valueVector.name;
		logCurveType = getLogCurveTypeFromName(curvename);
		logCurveType.addLogCurve(this);
		initialize();
	}

	static public StsLogCurve constructLogCurve(StsWell well, StsLogVectorSet logVectorSet, StsAbstractFloatVector valueVector, int version)
	{
		if(valueVector == null) return null;
		if(logVectorSet.getMVector() == null && logVectorSet.getZVector() == null) return null;
		return new StsLogCurve(well, logVectorSet, valueVector, version);
	}

	public StsLogCurve(String name)
	{

		super(false);
		curvename = name;
		logCurveType = getLogCurveTypeFromName(curvename);
		addToModel();
		logCurveType.addLogCurve(this);
		wellClass = (StsWellClass) currentModel.getStsClass(StsWell.class);
		StsLogCurve[] logCurves = new StsLogCurve[0];
		logCurves = (StsLogCurve[]) StsMath.arrayAddElement(logCurves, this);
	}

	static public StsLogCurve nullLogCurveConstructor(String name)
	{
		return new StsLogCurve(name, false);
	}

	public boolean initialize(StsModel model)
	{
		return initialize();
	}

	public boolean initialize()
	{
		mdepthVector = logVectorSet.getMVector();
		depthVector = logVectorSet.getZVector();
		timeVector = logVectorSet.getTVector();
		clockTimeVector = logVectorSet.getClockTimeVector();
		wellClass = (StsWellClass) currentModel.getStsClass(StsWell.class);
		return true;
	}

	public void setWell(StsWell well)
	{
		this.well = well;
		this.dbFieldChanged("well", well);
	}

	public StsLogCurveType getLogCurveType() { return logCurveType; }

	public StsColor getStsColor() { return logCurveType.getStsColor(); }

	public StsAbstractFloatVector getMDepthVector() { return getMdepthVector(); }

	/** The depth vector: if not directly supplied, it is computed from the deviation survey. */ /** The depth vector: if not directly supplied, it is computed from the deviation survey. */
	public StsAbstractFloatVector getDepthVector() { return depthVector; }

	/** An optional seismic one-way time vector used to display the well in seismic time (or seismic depth). */ /** An optional seismic one-way time vector used to display the well in seismic time (or seismic depth). */
	public StsAbstractFloatVector getTimeVector() { return timeVector; }

	public StsAbstractFloatVector getValueVector() { return valueVector; }

	public float[] getMDepthValues() { return getMdepthVector().getValues(); }

	public float[] getTimeValues() { return getTimeVector().getValues(); }

	public StsAbstractFloatVector getMDepthFloatVector()
	{
		if(getMdepthVector() == null) return null;
		getMdepthVector().checkLoadVector();
		return getMdepthVector();
	}

	public StsAbstractFloatVector getDepthFloatVector()
	{
		if(getDepthVector() == null) return null;
		getDepthVector().checkLoadVector();
		return getDepthVector();
	}

	public StsAbstractFloatVector getTimeFloatVector()
	{
		if(getTimeVector() == null) return null;
		getTimeVector().checkLoadVector();
		return getTimeVector();
	}

	public StsAbstractFloatVector getValueFloatVector()
	{
		if(valueVector == null) return null;
		valueVector.checkLoadVector();
		return valueVector;
	}

	public float[] getMDepthVectorFloats()
	{
		if(getMdepthVector() == null) return null;
		getMdepthVector().checkLoadVector();
		return getMdepthVector().getValues();
	}

	public float[] getDepthVectorFloats()
	{
		if(getDepthVector() == null) return null;
		if(getDepthVector() == null) return null;
		return getDepthVector().getValues();
	}

	public float[] getTimeVectorFloats()
	{
		return getVectorFloats(getTimeVector());

	}

	public float[] getVectorFloats(StsAbstractFloatVector dataVector)
	{
		if(dataVector == null) return null;
		return dataVector.getValues();
	}

	public float[] getValuesVectorFloats()
	{
		if(valueVector == null) return null;
		valueVector.checkLoadVector();
		return valueVector.getValues();
	}

	public boolean checkLoadVectors()
	{
		if(getDepthVector() == null || !getDepthVector().checkLoadVector()) return false;
		if(valueVector == null || !valueVector.checkLoadVector()) return false;
		if(getMdepthVector() != null)
			getMdepthVector().checkLoadVector();
		return true;
	}

	public void convertDepthUnits(float scalar)
	{
		if(getDepthVector() != null)
			getDepthVector().applyScalar(scalar);
		if(getMdepthVector() != null)
			getMdepthVector().applyScalar(scalar);
		return;
	}

	public void convertTimeUnits(float scalar)
	{
		if(getTimeVector() != null)
			getTimeVector().applyScalar(scalar);
		return;
	}

	public String getName() { return curvename; }

	public String getLogname() { return logname; }

	public String getCurvename() { return curvename; }

	public String toString() { return curvename; }

	/** clear floatArrays to reduce memory requirements */
	public void clearFloatArrays()
	{
		if(valueVector != null) valueVector.clearArray();
		if(getMdepthVector() != null) getMdepthVector().clearArray();
		if(getDepthVector() != null) getDepthVector().clearArray();
		if(getTimeVector() != null) getTimeVector().clearArray();
	}

	/** set/get logCurveType */
	public StsLogCurveType getLogCurveTypeFromName(String name)
	{
		StsLogCurveType logCurveType;
		logCurveType = (StsLogCurveType) getStsClassObjectWithName(StsLogCurveType.class, name);
		if(logCurveType == null) logCurveType = new StsLogCurveType(this);
		return logCurveType;
	}

	public String getLogCurveTypeName()
	{
		return logCurveType.getName();
	}

	public boolean logCurveTypeNameMatches(String name)
	{
		return logCurveType.getName().equals(name);
	}

	/** get interpolated value for one array given another */
	public float getInterpolatedValue(float depth)
	{
		float[] depths = getDepthVectorFloats();
		if(depths == null) return StsParameters.nullValue;
		float[] values = getValuesVectorFloats();
		if(values == null) return StsParameters.nullValue;
		return getInterpolatedValue(depths, values, depth);
	}

	static public float getInterpolatedValue(float[] depths, float[] values, float depth)
	{
		int indexBelow = StsMath.arrayIndexBelow(depth, depths);
		int nValues = depths.length;
		indexBelow = StsMath.minMax(indexBelow, 0, nValues - 2);
		float f = (depth - depths[indexBelow]) / (depths[indexBelow + 1] - depths[indexBelow]);
		return values[indexBelow] + f * (values[indexBelow + 1] - values[indexBelow]);
	}

	public boolean matchesName(String name)
	{
		if(name.equals(curvename)) return true;
		if(!logCurveType.hasAlias()) return false;
		return name.equals(logCurveType.aliasToType.toString());
	}

	/** return average of values over a depth range */
	public float getAverageOverZRange(float zTop, float zBase)
	{
		boolean debug = false;
		StsAbstractFloatVector values = getValueFloatVector();
		int[] indexRange = getDepthFloatVector().getIndicesInValueRange(zTop, zBase);
		if(indexRange == null)
		{
			if(debug) System.out.println("No index range returned for "
					+ "curve: " + getName() + " with zTop = " + zTop
					+ " & zBase = " + zBase);
			return StsParameters.nullValue;
		}
		float total = 0.0f;
		int nValues = indexRange[1] - indexRange[0] + 1;
		if(debug)
		{
			System.out.println("\nStsLogCurves.getAverageOverZRange:");
			System.out.println("curve:  " + getName());
			System.out.println("Z range:  " + zTop + " - " + zBase);
			System.out.print("Values: ");
		}
		for(int i = indexRange[0]; i <= indexRange[1]; i++)
		{
			float value = values.getElement(i);
			if(debug) System.out.print(value + " ");
			if(value == StsParameters.nullValue) nValues--;
			else total += value;
		}
		if(debug) System.out.println(" ");
		float average = total / (float) nValues;
		if(debug) System.out.println("Average: " + average);
		return average;
	}

	/** return most common categorical facies over a depth range */

	public float getCategoricalValueOverZRange(StsCategoricalFacies categoricalFacies,
											   float zTop, float zBase)
	{
		try
		{
			StsAbstractFloatVector valueValues = getValueFloatVector();
			float[] allValues = valueValues.getValues();
			int[] indexRange = getDepthFloatVector().getIndicesInValueRange(zTop, zBase);

			int nValues = indexRange[1] - indexRange[0] + 1;
			float[] values = new float[nValues];
			System.arraycopy(allValues, indexRange[0], values, 0, nValues);
			return (float) categoricalFacies.getMostCommonFaciesCategory(values);
		}
		catch(NullPointerException e) { return StsParameters.nullValue; }
	}

	/**
	 * print out log curve values & their average over a depth range
	 * Note: this method doesn't do any tvd-measured depth conversion.
	 */
	public void displayRange(float zMin, float zMax)
	{
		try
		{
			StringBuffer buffer = new StringBuffer(" \n");

			String depthName = getDepthVector().getName();
			String curveName = getName();
			int nValues = valueVector.getSize();
			buffer.append("Curve: " + curveName +
					"\nNumber of values: " + nValues + "\n");
			buffer.append(" \n");
			buffer.append("index\t" + depthName + "\tValue\n");

			float[] depths = getDepthVector().getValues();
			float[] curveValues = valueVector.getValues();
			for(int j = 0; j < nValues; j++)
			{
				float depth = depths[j];
				if(depth >= zMin && depth <= zMax)
				{
					buffer.append(j + "\t" + getDepthVector().getElement(j) +
							"\t" + curveValues[j] + "\n");
					if(depth > zMax) break;
				}
			}
			buffer.append(" \n");

			// print average value
			float average = getAverageOverZRange(zMin, zMax);
			buffer.append(" \nAverage over range: " + average);

			// print out depth and curve names
			PrintWriter out = new PrintWriter(System.out, true); // needed for correct formatting
			out.println(buffer.toString());

			// display dialog box
			StsTextAreaDialog dialog = new StsTextAreaDialog(null, "Log Curve Range Listing for "
					+ curveName, buffer.toString(), 30, 40);
			dialog.setVisible(true);
		}
		catch(Exception e)
		{
			StsException.outputException("StsLogCurve.displayRange() failed.",
					e, StsException.WARNING);
		}
	}

	/*
	 public double getMinDepth()
	 {
		 if(depths == null) return nullValue;
		 checkLoadVector(depths);
		 return (double)depths.getMinValue();
	 }

	 public double getMaxDepth()
	 {
		 if(depths == null) return nullValue;
		 checkLoadVector(depths);
		 return (double)depths.getMaxValue();
	 }
 */
	public float getMinValue()
	{
		if(valueVector == null) return StsParameters.largeFloat;
		return valueVector.getMinValue();
	}

	public float getMaxValue()
	{
		if(valueVector == null) return -StsParameters.largeFloat;
		return valueVector.getMaxValue();
	}

	public int getDepthIndexGE(double depth)
	{
		float depthF = (float) depth;

		float[] depthArray = getDepthVector().getValues();
		return StsMath.arrayIndexAbove(depthF, depthArray);
	}

	public int getDepthIndexLE(double depth)
	{
		float depthF = (float) depth;
		float[] depthArray = getDepthVector().getValues();
		return StsMath.arrayIndexBelow(depthF, depthArray);
	}

	public double getValueFromPanelXFraction(double fraction)
	{
		if(logCurveType == null) return StsParameters.nullValue;
		if(logCurveType.name.equals("TIME/DEPTH")) return StsParameters.nullValue;

		if(logCurveType.name.equals("Interval Velocity"))
			return StsParameters.nullValue;
		float[] scale = logCurveType.getScale();
		float gridMin = scale[0];
		float gridMax = scale[1];
		if(logCurveType.isLinear())
			return gridMin + fraction * (gridMax - gridMin);
		else
		{
			double logGridMin = StsMath.log10(gridMin);
			double logGridMax = StsMath.log10(gridMax);
			return Math.pow(10, logGridMin + fraction * (logGridMax - logGridMin));
		}
	}

	/**
	 * draws two logs next to well, one on left, one on right.  From the viewer's perspective,
	 * logs are drawn from min to the left side to max at the right side.
	 * @param glPanel3d
	 * @param well
	 * @param origin	zero origin of log axis: -1 for left and 0 for right.
	 */
	public void display3d(StsGLPanel3d glPanel3d, StsWell well, float origin)
	{
		display3d(glPanel3d, well, origin, getMdepthVector().getMaxValue());
	}

	public void display3d(StsGLPanel3d glPanel3d, StsWell well, float origin, float mdepth)
	{
		displayLog3d(glPanel3d, well, origin, mdepth);
	}

	protected void displayLog3d(StsGLPanel3d glPanel3d, StsWell well, float origin)
	{
		displayLog3d(glPanel3d, well, origin, well.getMaxMDepth());
	}

	protected void displayLog3d(StsGLPanel3d glPanel3d, StsWell well, float origin, float mdLimit)
	{
		StsAbstractFloatVector valueVector = getValueVector();
		if(valueVector == null) return;
		float values[] = valueVector.getValues();
		if(values == null) return;

		if(!well.checkComputeXYLogVectors(logVectorSet)) return;
		float[][] xyzFloatVectors = logVectorSet.getZorT_3FloatVectors();
		if(xyzFloatVectors == null) return;

		if(getMdepthVector() == null) return;
		float mdepths[] = getMdepthVector().getValues();
		if(mdepths == null) return;

		float curveMin = valueVector.getMinValue();
		float curveMax = valueVector.getMaxValue();
		if(curveMin == curveMax) return;

		int logCurveWidth = wellClass.getLogCurveDisplayWidth();
		int logLineWidth = wellClass.getLogCurveLineWidth();

		int nValues = values.length;
		if(currentModel.getProject().getTimeEnabled() && getClockTimeVector() != null)
			nValues = StsMath.minMax(getClockTimeVector().getCurrentTimeIndex(), 0, nValues);

		float displayCurveMin = logCurveType.getDisplayCurveMin();
		float displayCurveMax = logCurveType.getDisplayCurveMax();

		int scaleType = logCurveType.getScaleType();
		if(scaleType == StsLogCurveType.LOG)
		{
			displayCurveMin = (float) Math.log10(displayCurveMin);
			displayCurveMax = (float) Math.log10(displayCurveMax);
			curveMin = (float) Math.log10(curveMin);
			curveMax = (float) Math.log10(curveMax);
		}
		float scale = 1.0f / (displayCurveMax - displayCurveMin);
		float offset = -displayCurveMin * scale;
		boolean clip = curveMin < displayCurveMin || curveMax > displayCurveMax;
		GL gl = glPanel3d.getGL();
		logCurveType.getStsColor().setGLColor(gl);
		gl.glLineWidth((float) logLineWidth);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glBegin(GL.GL_LINE_STRIP);
		boolean draw = true;
		float[] xyzm;
		float[] xyz = getXYZPointFromVectors(xyzFloatVectors, 0);
		float[] xyzp = getXYZPointFromVectors(xyzFloatVectors, 1);
		float mdm;
		float md = mdepths[0];
		float mdp = mdepths[1];
		for(int n = 0; n < nValues; n++)
		{
			float value = values[n];
			if(value == StsParameters.nullValue)
			{
				if(draw)
				{
					gl.glEnd();
					draw = false;
				}
				continue;
			}
			else if(!draw)
			{
				gl.glBegin(GL.GL_LINE_STRIP);
				draw = true;
			}
			float mdepth = mdepths[n];
			if(mdepth > mdLimit) break;

			if(n < nValues-1)
			{
				xyzm = xyz;
				xyz = xyzp;
				xyzp = getXYZPointFromVectors(xyzFloatVectors, n+1);
				mdm = md;
				md = mdp;
				mdp = mdepths[n+1];
			}
			else
			{
				xyzm = xyz;
				mdm = md;
			}

			double[] screenPoint = glPanel3d.getScreenCoordinates(xyz);
			float[] slope = StsMath.subtractDivide(xyzp, xyzm, mdp-mdm);
			slope = StsMath.multByConstantAddPointStatic(slope, 1000.0f, xyz);
			double[] screenSlopePoint = glPanel3d.getScreenCoordinates(slope);

			// screen normal is negative reciprocal of screen slope
			double dsx = -(screenSlopePoint[1] - screenPoint[1]);
			double dsy = (screenSlopePoint[0] - screenPoint[0]);

			double s = Math.sqrt(dsx * dsx + dsy * dsy);

			if(s == 0.0)
			{
				dsx = 1.0;
				dsy = 0.0;
			}
			else
			{
				dsx /= s;
				dsy /= s;
			}
			/*
						else if(dsx < 0.0)
						{
							dsx = -dsx/s;
							dsy = -dsy/s;
						}
						else if(dsx > 0.0)
						{
							dsx = dsx/s;
							dsy = dsy/s;
						}
						else
						{
							dsx = 0.0;
							dsy = 1.0;
						}
					*/
			if(scaleType == StsLogCurveType.LOG)
				value = (float) Math.log10(value);
			float scaledValue = value * scale + offset;
			if(clip) scaledValue = StsMath.minMax(scaledValue, 0.0f, 1.0f);
			screenPoint[0] += logCurveWidth * dsx * (origin + scaledValue);
			screenPoint[1] += logCurveWidth * dsy * (origin + scaledValue);
			double[] logPoint = glPanel3d.getWorldCoordinates(screenPoint);
			gl.glVertex3dv(logPoint, 0);
		}
		if(draw) gl.glEnd();
		gl.glEnable(GL.GL_LIGHTING);
	}

	private float[] getXYZPointFromVectors(float[][] xyzVectors, int index)
	{
		float[] xyz = new float[3];
		for(int n = 0; n < 3; n++)
			xyz[n] = xyzVectors[n][index];
		return xyz;
	}

	public boolean hasColorscale() { return false; }

	public void resetVectors()
	{
		getValueVector().resetVector();
		getTimeVector().resetVector();
		getDepthVector().resetVector();
		getMDepthVector().resetVector();
	}

	public void applyPoints(ArrayList<StsPoint> points, StsWell well)
	{
		getMdepthVector().setValuesFromPoints(StsLoader.MDEPTH, points, 1);
		float[] depths = well.getDepthsFromMDepths(getMdepthVector().getValues());
		getDepthVector().checkSetValues(depths);
		valueVector.setValuesFromPoints(name, points, 0);
	}
/*
    public boolean addValuesToCurve(double[] mdepths, double[] values)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getValueVector().writeAppend(values, binaryDataDir);
        getMDepthVector().writeAppend(mdepths, binaryDataDir);
    	return true;
    }

    public boolean addValuesToMDepthCurveWrite(double[] values)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getMDepthVector().writeAppend(values, binaryDataDir);
    	return true;
    }
    public boolean addValuesToMDepthCurve(double[] values)
    {
		getMDepthVector().appendValues(StsMath.convertDoubleToFloatArray(values));
    	return true;
    }

    public boolean addValuesToCurveWrite(double[] values)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getValueVector().writeAppend(values, binaryDataDir);
    	return true;
    }
    public boolean addValuesToCurve(double[] values)
    {
    	getValueVector().appendValues(values);
    	return true;
    }
    public boolean replaceValueInCurve(double mdepth, double value, int index)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getValueVector().writeReplaceAt(value, index, binaryDataDir);
        getMDepthVector().writeReplaceAt(mdepth, index, binaryDataDir);
    	return true;
    }
    public boolean replaceValueInCurve(double value, int index)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getValueVector().writeReplaceAt(value, index, binaryDataDir);
    	return true;
    }
    public boolean replaceValueInMDepthCurve(double value, int index)
    {
    	String binaryDataDir = currentModel.getProject().getBinaryDirString();
    	getMDepthVector().writeReplaceAt(value, index, binaryDataDir);
    	return true;
    }
*/
	/** draw a log curve */
/*
    public void display(StsLogTrack track) throws StsException
    {
        StsTrace.methodIn(this, "display");

        if (getValuesVector()==null || getDepthValues()==null) return;

        StsWellWinContainer wellWinCntr = track.getWellDisplay().getWellWinContainer();
        GL gl = wellWinCntr.getGL();
        GLComponent glc = wellWinCntr.getGLComponent();
        GLU glu = wellWinCntr.getGLU();

        // set bounds
        Rectangle bounds = track.getBounds();
        gl.glPushMatrix();
        System.out.println("Viewport:  origin = " + bounds.x + ", " + bounds.y +
                            ", size = " + bounds.getSize().width + ", " +
                           bounds.getSize().height);
		gl.glViewport(glc, bounds.x, bounds.y, bounds.getSize().width,
                       bounds.getSize().height);
        gl.glScissor(bounds.x, bounds.y, bounds.getSize().width,
                     bounds.getSize().height);
        int mm[] = new int[1];
	    gl.glGetIntegerv(GL.GL_MATRIX_MODE, mm);
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glLoadIdentity();
        System.out.println("Ortho:  xMin = " + values.getMinValue() +
                       ", xMax = " + values.getMaxValue() +
                       ", yMin = " + depths.getMaxValue() +
                       ", yMax = " + depths.getMinValue());
	    glu.gluOrtho2D((double)values.getMinValue(), (double)values.getMaxValue(),
                   (double)depths.getMaxValue(), (double)depths.getMinValue());
	    gl.glMatrixMode(mm[0]);

        // draw curve
        int nValues = values.getValuesVector().getSize();
        System.out.println("\nlog = " + values.getName());
        System.out.println("number of values = " + nValues);
        if (nValues>0)
        {
            float[] zSamples = depths.getValuesVector().getValuesVector();
            float[] samples = values.getValuesVector().getValuesVector();
            StsColor color = this.getStsColor();
            gl.glColor3f(color.red, color.green, color.blue);
            float nullValue = values.getNullValue();
            gl.glBegin(GL.GL_LINE_STRIP);
                for (int i=0; i<nValues; i++)
                {
                    if (i%50==0) System.out.println(i + ": x = " + samples[i] + ", y = "
                                        + zSamples[i] + " ");
                    if (samples[i] != nullValue) gl.glVertex2f(samples[i], zSamples[i]);
                }
            gl.glEnd();
            zSamples=null;  // ok to garbage collect
            samples=null;   // ok to garbage collect
        }
        gl.glPopMatrix();
        Rectangle winBounds = wellWinCntr.getBounds();
		gl.glViewport(glc, winBounds.x, winBounds.y, winBounds.getSize().width,
                       winBounds.getSize().height);
        gl.glScissor(winBounds.x, winBounds.y, winBounds.getSize().width,
                     winBounds.getSize().height);

        StsTrace.methodOut(this, "display");
    }
*/

	/** the independent measured depth vector. A log curve is set of vectors as a function of measured depth */ /** the independent measured depth vector. A log curve is set of vectors as a function of measured depth */
	public StsAbstractFloatVector getMdepthVector()
	{
		if(mdepthVector == null)
			mdepthVector = logVectorSet.getMVector();
		return mdepthVector;
	}

	/** An optional clock-time vector for time-dependent logs */ /** An optional clock-time vector for time-dependent logs */
	public StsTimeVector getClockTimeVector()
	{
		return clockTimeVector;
	}
}












