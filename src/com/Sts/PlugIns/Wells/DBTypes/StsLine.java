//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System


package com.Sts.PlugIns.Wells.DBTypes;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.DBTypes.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;

import javax.media.opengl.*;
import java.io.*;
import java.util.*;

public class StsLine extends StsLineVectorSetObject implements StsTreeObjectI, StsInstance3dDisplayable, StsViewSelectable
{
	/** Section this line is on or sections connected to this line.  StsObject to decouple.  Cast by calling classes to StsLineSections */
	private StsObject lineSections;
	
   /** persisted index of color in spectrum */
	protected int nColor = 0;
	
    /** Indicates lineVectorSet are in rotated coordinate system */
    private boolean isVerticesRotated = true;

    protected boolean drawZones = false;
    protected boolean drawLabels = false;

	transient StsColor stsColor;
    
    transient protected int iTop, iBot;
    transient protected boolean drawVertices = false;
    transient protected int selectedPointIndex = -1;

    /** In Bezier curve fitting, max distance between generated points */
    static float maxSplineDistance = 100.0f;

    static public StsObjectPanel objectPanel = null;

    static protected boolean checkExcursion = false;

    static final float nullValue = StsParameters.nullValue;

    protected boolean highlighted = false;
    /** draw the well highlighted */

    transient static public StsHighlightedList highlightedList = new StsHighlightedList(4, 2);
    transient static protected StsLine currentSectionLine = null;

    transient protected StsRotatedBoundingBox boundingBox = null;

    // display fields: renamed to pseudoDisplayFields because parent class has static displayFields also
    static public StsFieldBean[] pseudoDisplayFields = null;
	/** indicates line has been fully initialized. */
    public transient boolean initialized = false;
	/** indicates lineVectorSet has been fully initialized. */
    // public transient boolean initializedVectorSet = false;
    

    public static final int RIGHT = StsParameters.RIGHT;
    public static final int LEFT = StsParameters.LEFT;

    static public final int NONE = StsParameters.NONE;
    static public final int MINUS = StsParameters.MINUS;
    static public final int PLUS = StsParameters.PLUS;

    static public final byte TD_DEPTH = StsParameters.TD_DEPTH;
    static public final byte TD_TIME = StsParameters.TD_TIME;
    static public final byte TD_APPROX_DEPTH = StsParameters.TD_APPROX_DEPTH;
    static public final byte TD_TIME_DEPTH = StsParameters.TD_TIME_DEPTH;
    static public final byte TD_APPROX_DEPTH_AND_DEPTH = StsParameters.TD_APPROX_DEPTH_AND_DEPTH;
    static public final byte TD_NONE = StsParameters.TD_NONE;

    static public final long serialVersionUID = 1L;

	/** these are colors used in drawing line; actual color is defined by nColor */
	static public StsColor[] colorList = StsColor.basic32Colors;

    /** default constructor */
    public StsLine() // throws StsException
    {
        /*
            if(currentModel == null) return;
    //        setName(new String("PseudoLine-" + index));
            connectedSections = StsObjectRefList.constructor(2, 2, "connectedSections", this);
            lineVectorSet = StsObjectRefList.constructor(2, 2, "lineVectorSet", this);
            surfaceVertices = StsObjectRefList.constructor(2, 2, "surfaceVertices", this);
            StsProject project = currentModel.getProject();
            stsColor = new StsColor(Color.RED);
            xOrigin = project.getXOrigin();
            yOrigin = project.getYOrigin();
        */
    }

    public StsLine(String name, boolean persistent)
    {
        this(persistent);
        setName(name);
    }

    public StsLine(boolean persistent)
    {
        super(persistent);
    }

	public boolean initialize(StsModel model)
	{
		return initialize();
	}

    /**Initialize well even if on uninitialized section. Return true only if
     * not on section or section is initialized */
    public boolean initialize()
    {
        if (initialized) return true;
        if(!initializeLine()) return false;
        // checkBuildTimeVector();
        // checkAdjustFromVelocityModel();
        initialized = initializeSection();
        return initialized;
    }

    protected boolean initializeLine()
    {
        stsColor = colorList[nColor];
        StsLineVectorSet lineVectorSet = getLineVectorSet();
        if(lineVectorSet == null) return false;
        return lineVectorSet.initialize();
    }

	public StsBoundingBox getBoundingBox() { return this; }
/*
    static public void initColors()
    {
        if (pseudoDisplayFields == null) return;
        StsColorIndexFieldBean colorListBean = (StsColorIndexFieldBean) StsFieldBean.getBeanWithFieldName(pseudoDisplayFields, "stsColorIndex");
        colorListBean.setListItems(colorList);
    }
*/
    static public StsLine buildLine()
    {
        StsLine line = new StsLine(false);
        line.setZDomainOriginal(currentModel.getProject().getZDomain());
        return line;
    }

    public String getName()
    {
        if (name != null)
            return name;
        else
            return "Line-" + getIndex();
    }

    public void setDrawZones(boolean state)
    {
        drawZones = state;
    }

    public boolean getDrawZones()
    {
        return drawZones;
    }

    public void setDrawLabels(boolean state)
    {
        drawLabels = state;
    }

    public boolean getDrawLabels()
    {
        return drawLabels;
    }

    public void setIsVertical(boolean isVertical)
    {
        this.isVertical = isVertical;
    }

    public boolean getIsVertical()
    {
        return isVertical;
    }

    /* public void setRotatedPoints(StsPoint[] rotatedPoints) { this.rotatedPoints = rotatedPoints; } */

    public StsPoint[] getExportPoints() { return getLineVertexPoints(); }

    public boolean normalShift(float offset)
    {
        // compute scale factors in X & Y
        float[] xyzFirst = getFirstXYZorT();
        float[] xyzLast = getLastXYZorT();
        float dist = StsMath.distance(xyzFirst, xyzLast, 2);
        float[] normal = StsMath.horizontalNormal(xyzFirst, xyzLast, 1);
        float xScale = normal[0] / dist;
        float yScale = normal[1] / dist;
        // compute shifts normal to the azimuth

        this.xOrigin = xOrigin + xScale * offset;
        this.yOrigin = yOrigin + yScale * offset;

        checkComputeRelativePoints();
        return true;
    }

    public float[] getFirstXYZorT() { return getXYZorT(0); }
    public float[] getLastXYZorT()
    {
        int nValues = getNValues();
        return getXYZorT(nValues-1);
    }

    public int getNPoints()
    {
        if(getLineVectorSet() == null) return 0;
        return getLineVectorSet().getVectorsSize();
    }
    public float[] getXYZorT(int index)
    {
        return getLineVectorSet().getXYZorT(index);
    }

    public int getTopIndex()
    {
        return iTop;
    }

    public int getBotIndex()
    {
        return iBot;
    }



    /** returns the top line vertex point.  This is a unrotated coordinate point relative to the
     *  origin point.  Don't use as a rotated local coordinate, because it isn't.  Along with the origin,
     *  it allows you to recover the absolute global coordinates of this point.
     * @return unrotated coordinate offset from well origin (typically Kelly bushing).
     */
    public StsPoint getTopPoint()
    {
        return getLineVectorSet().getFirstCoorPoint();
    }


    /** returns the bot line vertex point.  This is a unrotated coordinate point relative to the
     *  origin point.  Don't use as a rotated local coordinate, because it isn't.  Along with the origin,
     *  it allows you to recover the absolute global coordinates of this point.
     * @return unrotated coordinate offset from well origin (typically Kelly bushing).
     */
    public StsPoint getBotPoint()
    {
        return getLineVectorSet().getLastCoorPoint();
    }

    public StsPoint getBotVectorPoint()
    {
        int nValues = getLineVectorSet().getVectorsSize();
		StsAbstractFloatVector[] vectors = getLineVectorSet().getLineVectors();
        float[] floats = getLineVectorSet().subPoints(vectors, nValues - 1, nValues - 2);
        return new StsPoint(floats);
    }

    public float getTopZ()
    {
        return getTopPoint().getZorT();
    }

    public float getBotZ()
    {
        return getBotPoint().getZorT();
    }
/*
    public byte getZDomainSupported()
    {
        return zDomainSupported;
    }
*/
    public StsPoint[] getLineVertexPoints()
    {
        if (getLineVectorSet() == null) return null;
        return getAsCoorPoints();
    }

    public void deleteVertexNearestPoint(StsPoint point)
    {
        if (getLineVectorSet() == null)
        {
            return;
        }
        int nLineVertices = getLineVectorSet().getVectorsSize();

        StsPoint[] lineVertexPoints = getAsCoorPoints();
        for (int n = 0; n < nLineVertices; n++)
        {
            StsPoint linePoint = lineVertexPoints[n];
            if (point.getZorT() < linePoint.getZorT())
            {
                if (n == 0)
                    getLineVectorSet().deleteCoorPoint(n);
                else
                {
                    if ((linePoint.getZorT() - point.getZorT()) > (point.getZorT() - lineVertexPoints[n - 1].getZorT()))
                        getLineVectorSet().deleteCoorPoint(n - 1);
                    else
                        getLineVectorSet().deleteCoorPoint(n);
                }
            }
        }
        return;
    }

    public void setHighlighted(boolean val)
    {
        highlighted = val;
        dbFieldChanged("highlighted", highlighted);
        currentModel.win3dDisplayAll();
    }

    public boolean getHighlighted() { return highlighted; }

    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    public boolean getInitialized()
    {
        return initialized;
    }

    public void setDrawVertices(boolean draw)
    {
        drawVertices = draw;
    }

    public boolean getDrawVertices()
    {
        return drawVertices;
    }

    public void setSelectedPointIndex(int index)
    {
        selectedPointIndex = index;
    }

    public int getSelectedPointIndex()
    {
        return selectedPointIndex;
    }

    public StsColor getStsColor()
    {
		return colorList[nColor];
        // return stsColor;
    }

    public int getStsColorIndex()
    {
		return nColor;
        // return stsColor;
    }

    public void setStsColorIndex(int nColor)
    {
		if(this.nColor == nColor) return;
		this.nColor = nColor;
		dbFieldChanged("nColor", nColor);
		stsColor = colorList[nColor];
		currentModel.viewObjectRepaint(this, this);
    }
    /*
       public StsSection[] getAssociatedSections()
       {
           StsSection[] associatedSections = new StsSection[0];
    if(section != null) associatedSections = (StsSection[])StsMath.arrayAddElement(associatedSections, section);
           if(connectedSections == null) return associatedSections;

           for(int n = 0; n < connectedSections.length; n++)
           {
               StsSection connectedSection = (StsSection)connectedSections.getElement(n);
               associatedSections = (StsSection[])StsMath.arrayAddElement(associatedSections, connectedSection);
           }
           return associatedSections;
       }
    */


    public String getLabel()
    {
        return getName() + " ";
    }

    public String lineOnSectionLabel()
    {
		if(lineSections == null)
			return getLabel();
		return StsLineSections.lineOnSectionLabel(this);
    }

    public boolean isFault()
    {
        return getType() == StsParameters.FAULT;
    }

    public StsPoint getXYZPointAtZorT(float z, boolean extrapolate)
    {
        return getXYZPointAtZorT(z, extrapolate, isDepth);
    }
    
    public StsPoint getXYZPointAtZorT(float z, boolean extrapolate, boolean isDepth)
    {
        StsPoint point;
        int i = 0;
        float z0 = 0.0f, z1 = 0.0f;
        return getLineVectorSet().getXYZorTPoint(z, extrapolate, isDepth);
    }
    public float[] getXyzOrXytAtDepth(float z, boolean extrapolate)
    {
        StsPoint point;
        int i = 0;
        float z0 = 0.0f, z1 = 0.0f;
        return getLineVectorSet().getXyzOrXytAtDepth(z, extrapolate);
    }
    public StsPoint getPointAtZorT(float z, boolean extrapolate)
    {
        return getLineVectorSet().getXYZorTPoint(z, extrapolate, isDepth);
    }
    public StsPoint getPointAtZ(float z, boolean extrapolate)
    {
        return getXYZPointAtZorT(z, extrapolate, true);
    }
    public StsPoint getPointAtMDepth(float m, boolean extrapolate)
    {
        StsAbstractFloatVector mdVector = getLineVectorSet().getMVector();
		if(mdVector == null)
		{
			StsException.systemError(this, "getPointAtMDepth", "Failed to get point at mdepth " + m + " for well " + name);
			return null;
		}
        StsAbstractFloatVector.IndexF indexF = mdVector.getIndexF(m, extrapolate);
        return getLineVectorSet().computeInterpolatedValuesPoint(indexF);
    }

    public float[] getFloatsAtMDepth(float m, boolean extrapolate)
    {
        StsAbstractFloatVector mdVector = getLineVectorSet().getMVector();
		if(mdVector == null)
		{
			StsException.systemError(this, "getPointAtMDepth", "Failed to get point at mdepth " + m + " for well " + name);
			return null;
		}
        StsAbstractFloatVector.IndexF indexF = mdVector.getIndexF(m, extrapolate);
        return getLineVectorSet().computeInterpolatedValueFloats(indexF);
    }
    public float getDepthFromMDepth(float m, boolean extrapolate)
    {
        return getLineVectorSet().getDepthFromMDepth(m, extrapolate);
    }
    public float[] getFloatsAtDepth(float z, boolean extrapolate)
    {
        return getLineVectorSet().getFloatsAtDepth(z, extrapolate);
    }
	public boolean checkComputeMDepths()
	{
		return checkComputeMDepths(0);
	}
	public boolean checkComputeMDepths(int firstIndex)
	{
		return getLineVectorSet().checkComputeMDepths(firstIndex);
	}
	public void checkSortLineVectorSet()
    {
        getLineVectorSet().checkSortBy(getLineVectorSet().COL_Z);
    }

    public void removeClosePoints()
    {
        int nPoints = getLineVectorSet().getVectorsSize();
        float[] zFloats = getLineVectorSet().getZFloats();
        float z = zFloats[nPoints-1];
        for(int n = nPoints-2; n >= 0; n--)
        {
            float prevZ = z;
            z = zFloats[n];
            if(Math.abs(z-prevZ) < 1.0f)
                getLineVectorSet().deleteCoorPoint(n);
        }
    }

    public boolean checkIsVertical()
    {
        isVertical = getLineVectorSet().isVertical();
        return isVertical;
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
				return (StsPoint[])StsMath.arraycopy(slopes, n-1);
		}
        return slopes;
    }
    public StsPoint getPointAtMDepth(float m, StsPoint[] points, boolean extrapolate)
    {
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
        if (isVertical || nPoints < 2)
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

    /** remove a well from the instance list and in the 3d window */
    public boolean delete()
    {
        // don't delete if connected to a section and on a section
        if (lineSections != null)
        {
            if(!lineSections.delete()) return false;
        }
        super.delete();
        return true;
    }

    
    public void deleteLinePoint(int pointIndex)
    {
        if (getLineVectorSet() == null) return;
        getLineVectorSet().deleteCoorPoint(pointIndex);
    }

    public boolean computePointsProjectToSection()
    {
        if (!checkComputeRelativePoints()) return false;
        return projectToSection();
    }

	/** If this line has lineSections, project it to section if it is on one.
	 *  This method is an example of lazy loading:  StsLineSections won't be loaded until needed.
	 * @return true if is on section and has been successfully projected to.
	 */
    public boolean projectToSection()
    {
        if(lineSections == null) return false;
		return StsLineSections.projectToSection(this);
    }

    public boolean projectRotationAngleChanged()
    {
        return checkComputeRelativePoints();
    }

	public boolean computeExtendedPoints(float zMin, float zMax)
    {
        try
        {
            if (getLineVectorSet() == null) return false;
            return getLineVectorSet().computeExtendedPoints(zMin, zMax);
        }
        catch (Exception e)
        {
            StsException.outputException("Exception in StsLine.computeExtendedPoints()", e, StsException.WARNING);
            return false;
        }
    }

    public boolean checkComputeRelativePoints()
    {
		if (getLineVectorSet() == null) return false;
		return getLineVectorSet().checkComputeRelativePoints(this);
    }

    public boolean adjustRotatedPoints(int nFirstIndex)
    {
		if (getLineVectorSet() == null) return false;
		return getLineVectorSet().adjustRotatedPoints(nFirstIndex);
	}

    private boolean checkIsVertical(StsPoint[] slopes)
    {
        for (int n = 0; n < slopes.length; n++)
        {
            if (!StsMath.sameAsTol(slopes[n].getX(), 0.0f, 1.0e-5f)) return false;
            if (!StsMath.sameAsTol(slopes[n].getY(), 0.0f, 1.0e-5f)) return false;
        }
        return true;
    }

    /**
     * Returns interval this z vector falls in.
     * Assume the interval above is -1 and interval below is nVertexPoints.
     * So if above or below, return these vector.
     */
    private int getVertexIndexBelow(StsPoint[] vertexPoints, float z)
    {
        int nVertexPoints = vertexPoints.length;
        int n = 0;
        for (n = 0; n < nVertexPoints; n++)
            if (vertexPoints[n].getZorT() >= z) return n;
        return nVertexPoints;
    }

    // has not been debugged
    private int getVertexIndexAbove(StsPoint[] vertexPoints, float z)
    {
        int nVertexPoints = vertexPoints.length;
        int n = 0;
        for (n = nVertexPoints - 1; n >= 0; n--)
            if (vertexPoints[n].getZorT() <= z) return n;
        return -1;
    }


    /**
     * Try an iterative top-down method to find well-grid intersection.  If this fails,
     * go to a methodical top-down search.
     */

    public StsGridPoint computeGridIntersect(StsXYSurfaceGridable grid)
    {
        if (isVertical)
        {
            return computeVerticalGridIntersect(grid);
        }
        else
        {
            return computeGridIntersect(grid, grid.getZMin());
        }
        //            return computeGridIntersect(grid, grid.getZMin(), grid.getZMax());
    }

    // from this startZ iterate to final intersection
    public StsGridPoint computeGridIntersect(StsXYSurfaceGridable grid, float startZ)
    {
        int indexAbove, indexBelow;
        boolean aboveOK, belowOK;
        StsGridPoint point;
        try
        {
            float[] ztFloats = getLineVectorSet().getZorTFloats();
            int nPoints = getLineVectorSet().getVectorsSize();

            if (startZ < ztFloats[0])
            {
                indexAbove = -1;
            }
            else if (startZ > ztFloats[nPoints - 1])
            {
                indexAbove = nPoints - 1;
            }
            else
                indexAbove = StsMath.binarySearch(ztFloats, nPoints, startZ);

            indexBelow = indexAbove + 1;
            aboveOK = indexAbove >= 0;
            belowOK = indexBelow < nPoints;

            while (aboveOK || belowOK)
            {
                if (aboveOK)
                {
                    point = computeGridIntervalIntersect(grid, indexAbove + 1);
                    if (point != null)
                    {
                        return point;
                    }
                    indexAbove--;
                    if (indexAbove < 0)
                    {
                        aboveOK = false;
                    }
                }
                if (belowOK)
                {
                    point = computeGridIntervalIntersect(grid, indexBelow);
                    if (point != null)
                    {
                        return point;
                    }
                    indexBelow++;
                    if (indexBelow >= nPoints)
                    {
                        belowOK = false;
                    }
                }
            }
            // didn't find intersection along wellLine: try extrapolations above and below

            point = computeGridIntervalIntersect(grid, -1);
            if (point != null) return point;
            point = computeGridIntervalIntersect(grid, nPoints);
            if (point != null) return point;
            StsException.systemError("StsLine.computeGridIntersect() failed." +
                " Fault: " + getLabel() + " Grid: " + grid.getLabel());
            return null;
        }
        catch (Exception e)
        {
            StsException.outputException("StsLine.computeGridIntersect() failed." +
                " Fault: " + getLabel() + " Grid: " + grid.getLabel(),
                e, StsException.WARNING);
            return null;
        }
    }

    private StsGridPoint computeGridIntervalIntersect(StsXYSurfaceGridable grid, int indexBelow)
    {
        StsPoint point0, point1;
        StsPoint[] points = getAsCoorPoints();
        int nPoints = points.length;
        if (indexBelow <= 0)
        {
            point0 = getLineVectorSet().getXYZorTPoint(grid.getZMin(), true, isDepth);
            point1 = points[0];
        }
        else if (indexBelow > nPoints - 1)
        {
            point0 = points[nPoints - 1];
            point1 = getLineVectorSet().getXYZorTPoint(grid.getZMax(), true, isDepth);
        }
        else
        {
            point0 = points[indexBelow - 1];
            point1 = points[indexBelow];
        }

        StsGridCrossings gridCrossings = new StsGridCrossings(point0, point1, grid);
        return gridCrossings.getGridIntersection(grid);
    }
/*
    public void adjustSurfaceVertex(StsXYSurfaceGridable surface)
    {
        StsGridPoint gridPoint = computeGridIntersect(surface);
        if (gridPoint == null) return;
        surface.interpolateBilinearZ(gridPoint, true, true);
        StsSurfaceVertex surfaceVertex = getSurfaceVertex(surface);
        surfaceVertex.setPoint(gridPoint.getPoint());
    }
*/
    // use this method in model construction as it has only x, y, and z
/*
    private StsSurfaceVertex computeModelSurfaceVertex(StsSurface surface, StsBlock block)
    {
        StsGridPoint gridPoint = computeGridIntersect(surface);
        if (gridPoint == null)
        {
            return null;
        }
        surface.interpolateBilinearZ(gridPoint, true, true);
        //        StsPoint point = new StsPoint(gridPoint);
        StsPoint point = new StsPoint(gridPoint.getXYZorT(isDepth));
        //        getUnrotatedRelativeXYFromRotatedXY(point);
        return new StsSurfaceVertex(point, this, surface, block, true);
    }
*/
    /*
       public StsSurfaceVertex addLinePoint(StsPoint point)
       {
           return addLinePoint(point, true);
       }
    */
    /** Add a vertex to the list of vertices in increasing order of z. */
    public int addLinePoint(StsPoint point, boolean computePath, boolean extend)
    {
        int insertIndex = -1;
        if (point == null)
        {
            StsMessageFiles.logMessage("Can't add a null point.");
            return insertIndex;
        }
        float z = point.getZorT();
        if (point.getLength() < 5)
        {
            if (isDepth)
                point = new StsPoint(point.getX(), point.getY(), z, 0.0f, 0.0f);
            else
                point = new StsPoint(point.getX(), point.getY(), 0.0f, 0.0f, z);
        }
        StsProject project = currentModel.getProject();
        if (z < project.getZorTMin())
        {
            StsMessageFiles.logMessage("Can't add z: " + z + ". Less than project min: " + project.getZorTMin());
            return insertIndex;
        }
        else if (z > project.getZorTMax())
        {
            StsMessageFiles.logMessage("Can't add z: " + z + ". Greater than project max: " + project.getZorTMax());
            return insertIndex;
        }

        /** Check if picked point seems like a large lateral pick (mispick or start
         *  of next well. Query the user if he wants it.
         */
        if (getLineVectorSet().getVectorsSize() > 0)
        {
            StsPoint lastPoint = getLineVectorSet().getLastCoorPoint();
            float dH = point.distanceSquaredType(StsPoint.DIST_XY, lastPoint);
            float dV = point.distanceSquaredType(StsPoint.DIST_Z, lastPoint);

            if (checkExcursion && dV > 0.0f && dH > 2.0f * dV)
            {
                if (!StsYesNoDialog.questionValue(currentModel.win3d, "Lateral excursion of this pick seems large.\n" +
                    "Do you wish to include this point?"))
                {
                    return insertIndex;
                }
            }
        }

        /** If we don't have a color yet, get it now */
        if (stsColor == null)
        {
            stsColor = new StsColor(currentModel.getSpectrumClass().getCurrentSpectrumColor("Basic"));
        }
        // measured depth is used as dimensionaless parameter in splining curve shape, so set Z or T as value
        point.setM(z);
        //        float dZMax = 0.0f;
        //        try { dZMax = 2.0f * project.getZInc(); }
        //        catch (Exception e) { return null; }
        return insertLinePoint(point, computePath, 1.0f, false); // use tolerance
    }

    public int insertLinePoint(StsPoint point, boolean computePath, float minZTolerance, boolean replaceWithinTolerance)
    {
        int insertIndex = -1;
        if (point == null) return insertIndex;
        StsProject project = currentModel.getProject();
        if (project == null)
            return insertIndex;

        float z = point.getZorT();
        if (z < project.getZorTMin() || z > project.getZorTMax())
            return insertIndex;

        float lastZ = -StsParameters.largeFloat;
        if (z == -1) return insertIndex;

        if (onlyMonotonic())
        {
            StsPoint[] linePoints = getAsCoorPoints();
            int nPoints = linePoints.length;
            for(int n = 0; n < nPoints; n++)
            {
                StsPoint linePoint = linePoints[n];
                float nextZ = linePoint.getZorT();
                if (z == lastZ)
                {
                    return insertIndex;
                }

                if (z >= lastZ && z <= nextZ)
                {
                    if (z - lastZ < minZTolerance || nextZ - z < minZTolerance)
                    {
                        if (replaceWithinTolerance)
                        //                    if (replaceWithinTolerance && !lineVertex.usedInLine(currentModel))
                        {
                            try
                            {
                                getLineVectorSet().deleteCoorPoint(n);
                                float pointZ = linePoint.getZorT();

                                for (int i = 0; i < nPoints; i++)
                                {
                                    if (linePoints[i].getZorT() > pointZ)
                                    {
                                        insertIndex = getLineVectorSet().insertValuePointBefore(point, i);
                                        if (computePath) computePointsProjectToSection();
                                        return insertIndex;
                                    }
                                }
                                insertIndex = getLineVectorSet().addXyztPoint(point);
                                if (computePath) computePointsProjectToSection();
                                return insertIndex;
                            }
                            catch (Exception e)
                            {
                                return insertIndex;
                            }
                        }
                        return insertIndex;
                    }
                    insertIndex = getLineVectorSet().insertValuePointBefore(point, n);
                    if (computePath) computePointsProjectToSection();
                    return insertIndex;
                }
                lastZ = nextZ;
            }
        }
        insertIndex = getLineVectorSet().addCoorPoint(point.v);
        if (computePath) computePointsProjectToSection();
        return insertIndex;
    }

    public boolean onlyMonotonic() { return true; }

    public StsGridPoint computeVerticalGridIntersect(StsXYSurfaceGridable grid)
    {
        StsPoint point = getTopPoint().copy();
        StsGridPoint gridPoint = new StsGridPoint(point, grid);
        float z = grid.interpolateBilinearZ(gridPoint, true, true);
        if (z == nullValue)
        {
            StsException.systemError("StsLine.computeVerticalGridIntersect() failed." +
                " For well: " + getLabel());
            return null;
        }
        return gridPoint;
    }

    static public void displayClass(StsGLPanel3d glPanel3d, StsClass instanceList)
    {
        int nInstances = instanceList.getSize();
        for (int n = 0; n < nInstances; n++)
        {
            StsLine line = (StsLine) instanceList.getElement(n);
            line.display(glPanel3d, false);
        }
    }

    public void display(StsGLPanel3d glPanel3d)
    {
        display(glPanel3d, false);
    }

    public void display(StsGLPanel3d glPanel3d, boolean displayName)
    {
        if (glPanel3d == null) return;
        if (!isVisible()) return;
        display(glPanel3d, highlighted, getName());
    }

    public void display(StsGLPanel3d glPanel3d, String name)
    {
        if (glPanel3d == null) return;
        if (isVisible()) display(glPanel3d, highlighted, name);
    }

    public void display(StsGLPanel3d glPanel3d, boolean highlighted, String name)
    {
        display(glPanel3d, highlighted, name, false);
    }

    public void display(StsGLPanel3d glPanel3d, boolean highlighted, String name, boolean drawDotted)
    {
        if (glPanel3d == null) return;
        if (!isVisible()) return;
        GL gl = glPanel3d.getGL();
        if (gl == null) return;

        // if(!currentModel.getProject().supportsZDomain(zDomainSupported)) return;
		// checkBuildTimeVector();
	 /*
        StsCropVolume cropVolume = currentModel.getProject().cropVolume;
        boolean isZCropped = cropVolume.isZCropped();
        if (isZCropped)
        {
            StsAbstractFloatVector zFloats = getLineVectorSet().getZVector();
            float cropMinZ = cropVolume.getCropZMin();
            StsAbstractFloatVector.IndexF topIndexF = zFloats.getIndexF(cropMinZ);
            float cropMaxZ = cropVolume.getCropZMax();
            StsAbstractFloatVector.IndexF botIndexF = zFloats.getIndexF(cropMaxZ);
            drawLine(gl, stsColor, highlighted, topIndexF.index+topIndexF.f, botIndexF.index+botIndexF.f, drawDotted);
        }
        else
     */
            drawLine(gl, stsColor, highlighted, drawDotted);

        if (name != null)
            displayName(glPanel3d, name);
        if (drawVertices)
            displayVertices(glPanel3d);
    }

    private void drawLine(GL gl, StsColor stsColor, boolean highlighted, boolean drawDotted)
    {
        //float[][] lineVectorFloats = getLineVectorSet().getXYZorTFloats();
        drawLine(gl, stsColor, highlighted, 0.0f, getLineVectorSet().getMaxIndex() -1.0f, drawDotted);
    }

    public void drawLine(GL gl, StsColor stsColor, boolean highlighted, StsCoorTimeVectorSet xyztmVectorSet, boolean drawDotted)
    {
        float[][] lineVectorFloats = getXYZorTFloatVectors();
        if (drawDotted)
            StsGLDraw.drawDottedLine(gl, stsColor, highlighted, lineVectorFloats, 0, xyztmVectorSet.getMaxIndex());
        else
            StsGLDraw.drawLine(gl, stsColor, highlighted, lineVectorFloats, 0, xyztmVectorSet.getMaxIndex());
    }

    public void drawLine(GL gl, StsColor stsColor, boolean highlighted, float topIndexF, float botIndexF, boolean drawDotted)
    {
		if(gl == null || stsColor == null) return;
        float[][] lineVectorFloats = getXYZorTFloatVectors();
		if(lineVectorFloats == null) return;
        if (drawDotted)
            StsGLDraw.drawDottedLine(gl, stsColor, highlighted, lineVectorFloats, topIndexF, botIndexF);
        else
            StsGLDraw.drawLine(gl, stsColor, highlighted, lineVectorFloats, topIndexF, botIndexF);
    }

    public void drawLine(GL gl, StsColor stsColor, boolean highlighted, int min, int max, boolean drawDotted)
    {
        float[][] lineVectorFloats = getXYZorTFloatVectors();
        if (drawDotted)
            StsGLDraw.drawDottedLine(gl, stsColor, highlighted, lineVectorFloats, min, max);
        else
            StsGLDraw.drawLine(gl, stsColor, highlighted, lineVectorFloats, min, max);
    }

	public float[][] getXYZorTFloatVectors()
	{
		return getLineVectorSet().getZorT_3FloatVectors();
	}

    public int getPointsZIndex()
    {
        return currentModel.getProject().getPointsZIndex();
    }

    public void displayName(StsGLPanel3d glPanel3d, String name)
    {
        displayName(glPanel3d, name, 0, getNValues()-1);
    }

    private void displayName(StsGLPanel3d glPanel3d, String name, int min, int max)
    {
        GL gl = glPanel3d.getGL();
        if (gl == null) return;
        gl.glDisable(GL.GL_LIGHTING);
        stsColor.setGLColor(gl);
        float[] xyzOrT = getLineVectorSet().getXYZorT(min);
        StsGLDraw.fontHelvetica12(gl, xyzOrT, name);
        xyzOrT = getLineVectorSet().getXYZorT(max);
        StsGLDraw.fontHelvetica12(gl, xyzOrT, name);
        gl.glEnable(GL.GL_LIGHTING);
    }

    public void display2d(StsGLPanel3d glPanel3d, boolean displayName, int dirNo,
                          float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed)
    {
        if (glPanel3d == null) return;
        if (!isVisible()) return;
        GL gl = glPanel3d.getGL();
        if (gl == null) return;

        stsColor.setGLColor(gl);
        if (displayName) displayName2d(gl, getName(), dirNo, axesFlipped);
        gl.glDisable(GL.GL_LIGHTING);
        glPanel3d.setViewShift(gl, StsGraphicParameters.edgeShift);
        displayLine2d(gl, stsColor, dirNo, dirCoordinate, axesFlipped, xAxisReversed, yAxisReversed);
        glPanel3d.resetViewShift(gl);
        gl.glEnable(GL.GL_LIGHTING);
    }

    private void displayLine2d(GL gl, StsColor color, int dirNo, float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed)
    {
        if (highlighted)
        {
            gl.glLineWidth(StsGraphicParameters.well3dLineWidthHighlighted);
        }
        else
        {
            gl.glLineWidth(StsGraphicParameters.well3dLineWidth);
        }

        gl.glLineStipple(1, StsGraphicParameters.dottedLine);

        int verticalIndex = StsPoint.getVerticalIndex();
        switch (dirNo)
        {
            case 0:
                displayLine2d(gl, color, 1, verticalIndex, 0, yAxisReversed, dirCoordinate);
                break;
            case 1:
                displayLine2d(gl, color, 0, verticalIndex, 1, xAxisReversed, dirCoordinate);
                break;
            case 2:
                if (!axesFlipped)
                    displayLine2d(gl, color, 0, 1, verticalIndex, false, dirCoordinate);
                else
                    displayLine2d(gl, color, 1, 0, verticalIndex, false, dirCoordinate);
        }
    }

    static final int FRONT = 1;
    static final int BACK = -1;
    static final int END = 0;

    private void displayLine2d(GL gl, StsColor color, int nXAxis2d, int nYAxis2d, int nDepthAxis, boolean axisReversed, float cursorDepth)
    {
        int[] range = new int[]{-1, -1};
        float[][] lineVectorFloats = getXYZorTFloatVectors();
        while (true)
        {
            int inFront = isPointsInFrontOrBack(lineVectorFloats, nXAxis2d, nYAxis2d, nDepthAxis, axisReversed, range, cursorDepth);
            switch (inFront)
            {
                case FRONT:
                    StsGLDraw.drawLine2d(gl, color, false, lineVectorFloats, range[0], range[1], nXAxis2d, nYAxis2d);
                    break;
                case BACK:
                    StsGLDraw.drawDottedLine2d(gl, color, false, lineVectorFloats, range[0], range[1], nXAxis2d, nYAxis2d);
                    break;
                default:
                    return;
            }

        }
    }

    /**
     * draw 2d line between last XY and XY. Line is solid if in front of cursor, dotted if behind.
     * Line is solid if depth is greater than cursorDepth unless axis is reversed; dotted otherwise.
     *
     * @param cursorDepth  depth of cursor
     * @param axisReversed depth direction is reversed
     */

    private int isPointsInFrontOrBack(float[][] xyzFloats, int nXAxis2d, int nYAxis2d, int nDepthAxis, boolean axisReversed, int[] range, float cursorDepth)
    {
        int nPoints = getNValues();
        int min = range[0] + 1;
        if (min >= nPoints) return END;

        int lastInFront = FRONT, inFront;
        float depth = xyzFloats[nDepthAxis][min];
        inFront = getInFront(axisReversed, depth, cursorDepth);
        int max = min + 1;
        for (; max < nPoints; max++)
        {
            lastInFront = inFront;
            inFront = getInFront(axisReversed, depth, cursorDepth);
            if (lastInFront != inFront)
            {
                range[0] = min;
                range[1] = max;
                return lastInFront;
            }
        }
        range[0] = min;
        range[1] = nPoints - 1;
        return lastInFront;
    }

    private int getInFront(boolean axisReversed, float depth, float cursorDepth)
    {
        boolean inFront = axisReversed ? depth >= cursorDepth : depth <= cursorDepth;
        if (inFront)
            return FRONT;
        else
            return BACK;
    }

    private void drawLinePoint2d(GL gl, float lastX, float lastY, float lastDepth, float x, float y, float depth,
                                 float cursorDepth, boolean axisReversed)
    {
        boolean lastInFront, inFront;

        if (axisReversed)
        {
            lastInFront = lastDepth <= cursorDepth;
            inFront = depth <= cursorDepth;
        }
        else
        {
            lastInFront = lastDepth >= cursorDepth;
            inFront = depth >= cursorDepth;
        }
        if (lastInFront == inFront || depth == lastDepth)
        {
            if (!inFront)
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
            }
            gl.glBegin(GL.GL_LINES);
            gl.glVertex2f(lastX, lastY);
            gl.glVertex2f(x, y);
            gl.glEnd();
            if (!inFront)
            {
                gl.glDisable(GL.GL_LINE_STIPPLE);
            }
        }
        else
        {
            float f = (cursorDepth - lastDepth) / (depth - lastDepth);
            float cursorX = lastX + f * (x - lastX);
            float cursorY = lastY + f * (y - lastY);
            if (inFront)
            {
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(lastX, lastY);
                gl.glVertex2f(cursorX, cursorY);
                gl.glEnd();

                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(cursorX, cursorY);
                gl.glVertex2f(x, y);
                gl.glEnd();
                gl.glDisable(GL.GL_LINE_STIPPLE);
            }
            else
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(lastX, lastY);
                gl.glVertex2f(cursorX, cursorY);
                gl.glEnd();
                gl.glDisable(GL.GL_LINE_STIPPLE);

                gl.glBegin(GL.GL_LINES);
                gl.glVertex2f(cursorX, cursorY);
                gl.glVertex2f(x, y);
                gl.glEnd();
            }
            StsGLDraw.drawPoint2d(x, y, gl, 4);
        }
    }

    private void displayName2d(GL gl, String name, int dirNo, boolean axesFlipped)
    {
        float[] xyzTop = getLineVectorSet().getXYZorT(0);
        float[] xyzBot = getLineVectorSet().getXYZorT(getNValues() - 1);

        switch (dirNo)
        {
            case 0:
                StsGLDraw.fontHelvetica12(gl, xyzTop[1], xyzTop[2], name);
                StsGLDraw.fontHelvetica12(gl, xyzBot[1], xyzBot[2], name);
                break;
            case 1:
                StsGLDraw.fontHelvetica12(gl, xyzTop[0], xyzTop[2], name);
                StsGLDraw.fontHelvetica12(gl, xyzBot[0], xyzBot[2], name);
                break;
            case 2:
                if (!axesFlipped)
                {
                    StsGLDraw.fontHelvetica12(gl, xyzTop[0], xyzTop[1], name);
                    StsGLDraw.fontHelvetica12(gl, xyzBot[0], xyzBot[1], name);
                }
                else
                {
                    StsGLDraw.fontHelvetica12(gl, xyzTop[1], xyzTop[0], name);
                    StsGLDraw.fontHelvetica12(gl, xyzBot[1], xyzBot[0], name);
                }
                break;
        }
    }

    public void displayLabel2d(GL gl, float[] xyz, String name, int dirNo, boolean axesFlipped)
    {
        switch (dirNo)
        {
            case 0:
                StsGLDraw.fontHelvetica12(gl, xyz[1], xyz[2], name);
                StsGLDraw.fontHelvetica12(gl, xyz[1], xyz[2], name);
                break;
            case 1:
                StsGLDraw.fontHelvetica12(gl, xyz[0], xyz[2], name);
                StsGLDraw.fontHelvetica12(gl, xyz[0], xyz[2], name);
                break;
            case 2:
                if (!axesFlipped)
                {
                    StsGLDraw.fontHelvetica12(gl, xyz[0], xyz[1], name);
                    StsGLDraw.fontHelvetica12(gl, xyz[0], xyz[1], name);
                }
                else
                {
                    StsGLDraw.fontHelvetica12(gl, xyz[1], xyz[0], name);
                    StsGLDraw.fontHelvetica12(gl, xyz[1], xyz[0], name);
                }
                break;
        }
    }
    public void displayVertices(StsGLPanel3d glPanel3d)
    {
        //if (win3d == null) return;
        int nVertices = (getLineVectorSet() == null) ? 0 : getLineVectorSet().getVectorsSize();
        float[][] xyzVectors = getXYZorTFloatVectors();
        StsGLDraw.drawPoint(xyzVectors, nVertices, selectedPointIndex, StsColor.WHITE, glPanel3d, 0.0);
    }

    /*
         public boolean intersectsCursor(StsGLPanel3d glPanel3d, int dirNo)
         {
             StsBoundingBox wellBoundingBox = getBoundingBox();
             return glPanel3d.getCursor3d().cursorIntersected(wellBoundingBox, dirNo);
         }
    */
    public void pick(GL gl, StsGLPanel glPanel)
    {
        if(!currentModel.getProject().canDisplayZDomain(getZDomainSupported())) return;
        int zIndex = getPointsZIndex();
        if(!currentModel.getProject().supportsZDomain(getZDomainSupported())) return;
        float[][] xyzFloats = getXYZorTFloatVectors();
        StsGLDraw.pickLineStrip(gl, stsColor, xyzFloats, highlighted);
    }

    public void mouseSelectedEdit(StsMouse mouse)
    {
        logMessage();
    }

    public void showPopupMenu(StsGLPanel glPanel, StsMouse mouse) { }

    public void pickVertices(StsGLPanel3d glPanel3d)
    {
        GL gl = glPanel3d.getGL();
        float[][] xyzVectors = getLineVectorSet().getZorT_3FloatVectors();
        int nValues = getLineVectorSet().getVectorsSize();
        gl.glPointSize(4.0f);
        for(int n = 0; n < nValues; n++)
        {
            gl.glInitNames();
            gl.glPushName(n);
            StsGLDraw.drawPoint(xyzVectors[0][n], xyzVectors[1][n], xyzVectors[2][n], gl);
            gl.glPopName();
        }
    }

    /**
     * Methods for handling well highlighting.
     *
     * @param state indicates true or false
     * @return return true if toggled.
     */
    public boolean setHighlight(boolean state)
    {
        if (highlighted != state)
        {
            highlighted = state;
            return true;
        }
        else
        {
            return false;
        }
    }

    /** Draw all wells currently in highlighted list */
    static public void drawHighlightedLines(StsGLPanel3d glPanel3d)
    {
        //    	if(highlightedList != null) highlightedList.display(glPanel3d);
    }

    /** Clear the highlighted list */
    static public void clearHighlightedLines()
    {
        if (highlightedList != null)
        {
            highlightedList.clear();
        }
    }

	public boolean addToProject()
	{
		getLineVectorSet().checkSetCurrentTime();
		return currentModel.getProject().addToProjectUnrotatedBoundingBox(this, StsProject.TD_DEPTH);
	}

	public boolean checkSetCurrentTime()
	{
		return getLineVectorSet().checkSetCurrentTime();
	}
	public void setTimeIndex(boolean timeEnabled)
	{
		getLineVectorSet().setTimeIndex(timeEnabled);
	}

    public boolean initializeSection()
    {
		if(lineSections == null) return true;
		return StsLineSections.initializeSection(this);
    }
	public boolean reinitialize()
	{
		if(lineSections == null) return true;
		return StsLineSections.reinitialize(this);
	}

    static public StsLine buildVertical(StsSurfaceVertex vertex, byte type)
    {
        StsLine line;

        line = vertex.getSectionLine();
        if (line != null) return line;
        return buildVertical(vertex.getPoint(), type);
    }

    static public StsLine buildVertical(StsPoint point, byte type)
    {
        StsGridPoint gridPoint = new StsGridPoint(point, null);
        return buildVertical(gridPoint, type);
    }

    /*
        static public StsLine buildVertical(StsPoint point, boolean persistent)
        {
            StsLine line = new StsLine(persistent);
            StsGridPoint gridPoint = new StsGridPoint(point, null);
            line.constructVertical(gridPoint);
            return line;
        }
    */
    static public StsLine buildVertical(StsGridPoint gridPoint, byte type)
    {
        try
        {
            StsLine line = new StsLine(false);
            line.setZDomainOriginal(currentModel.getProject().getZDomain());
            line.constructVertical(gridPoint.point);
            line.addToModel();
            return line;
        }
        catch (Exception e)
        {
            StsException.systemError("StsLine.buildVertical(gridPoint) failed.");
            return null;
        }
    }

    /*
       static public StsLine buildVertical(StsSurfaceVertex vertex, byte type)
       {
           StsLine line;

           line = vertex.getSectionLine();
           if(line != null) return line;
     StsGridPoint gridPoint = new StsGridPoint(vertex.getPoint(), null);
           line = buildVertical(gridPoint);
           vertex.setAssociation(line);
           return line;
       }

       static public StsLine buildVertical(StsPoint point)
       {
          return buildVertical(point, true);
       }

       static public StsLine buildVertical(StsPoint point, boolean persistent)
       {
           StsGridPoint gridPoint = new StsGridPoint(point, null);
           StsLine line = buildVertical(gridPoint, persistent);
           return line;
       }

       static public StsLine buildVertical(StsGridPoint gridPoint)
       {
           return buildVertical(gridPoint, true);
       }

       static public StsLine buildVertical(StsGridPoint gridPoint, boolean persistent)
       {
           try
           {
               StsLine line = new StsLine(persistent);
               line.constructVertical(gridPoint, persistent);
               return line;
           }
           catch(Exception e)
           {
               StsException.systemError("StsLine.buildVertical(gridPoint) failed.");
               return null;
           }
       }
    */

    /*
        private boolean constructVertical(StsGridPoint gridPoint, byte type, boolean persistent)
        {
            float z;
            if (getLineVectorSet() == null)
                getLineVectorSet() = StsObjectRefList.constructor(2, 1, "getLineVectorSet()", this, persistent);

            points = new StsPoint[2];
            StsProject project = currentModel.getProject();

            float x = gridPoint.getPoint().getX();
            float y = gridPoint.getPoint().getY();

            z = project.getZorTMin();
            points[0] = new StsPoint(x, y, z);
            addLinePoint(points[0], true, false);
            StsGridSectionPoint gridSectionPoint = new StsGridSectionPoint(points[0], row, col, null, null, persistent);
            vertex = new StsSurfaceVertex(gridSectionPoint, this, persistent); // point is cloned
            z = project.getZorTMax();
            points[1] = new StsPoint(x, y, z);
            return true;
        }
    */
    protected boolean constructVertical(StsPoint point)
    {
        if (getLineVectorSet() == null)
            setTimeVectorSet(StsLineVectorSet.dbConstructor());

        StsProject project = currentModel.getProject();

        setZDomainSupported(project.getZDomainSupported());

        float x = point.getX();
        float y = point.getY();
        float[] xyzmt = new float[5];
        xyzmt[0] = x;
        xyzmt[1] = y;

        float t, z;
	/*
        StsSeismicVelocityModel velocityModel = project.getSeismicVelocityModel();
        if (velocityModel != null)
        {
            t = velocityModel.gettMin();
            z = (float) velocityModel.getZ(x, y, t);
        }
        else
    */
        {
            t = project.getTimeMin();
            z = project.getDepthMin();
        }
        xyzmt[2] = z;
        xyzmt[4] = t;
        getLineVectorSet().addCoorPoint(xyzmt);
	/*
        if (velocityModel != null)
        {
            t = velocityModel.gettMax();
            z = (float) velocityModel.getZ(x, y, t);
        }
        else
    */
        {
            t = project.getTimeMax();
            z = project.getDepthMax();
        }
        xyzmt[2] = z;
        xyzmt[4] = t;
        getLineVectorSet().addCoorPoint(xyzmt);

        isVertical = true;
        if (stsColor == null)
            stsColor = currentModel.getSpectrumClass().getCurrentSpectrumColor("Basic");
        return true;
    }

    /*
        public StsPoint createStsPoint(float x, float y, float z)
        {
            if (isDepth)
            {
                return new StsPoint(x, y, z);
            }
            else
            {
                StsPoint point = new StsPoint(5);
                point.setX(x);
                point.setY(y);
                point.setZ(z); // hack: we need to use a velocity, td curve, or td model
                point.setT(z);
                return point;
            }
        }
    */
    public StsPoint createStsPoint(float x, float y, float t, float d)
    {
        /*
                if (isDepth)
                {
                    return new StsPoint(x, y, z);
                }
                else
                {
        */
        StsPoint point = new StsPoint(5);
        point.setX(x);
        point.setY(y);
        point.setZ(d);
        point.setT(t);
        return point;
        //         }
    }

    public StsPoint createStsPoint(StsPoint point)
    {
        return createStsPoint(point.v);
    }

    public StsPoint createStsPoint(float[] xyz)
    {
        if (isDepth)
        {
            return new StsPoint(xyz[0], xyz[1], xyz[2]);
        }
        else
        {
            StsPoint point = new StsPoint(5);
            point.setX(xyz[0]);
            point.setY(xyz[1]);
            point.setZ(xyz[2]);
            if (xyz.length < 5)
            {
                point.setT(xyz[2]);
            }
            else
            {
                point.setT(xyz[4]);
            }
            return point;
        }
    }

    public boolean construct(StsSurfaceVertex[] vertices)
    {
        if (vertices == null) return false;
        setTimeVectorSet(StsLineVectorSet.construct(vertices));
        setZDomainSupported(currentModel.getProject().getZDomainSupported());
        return true;
    }
/*
	public boolean construct(StsSurfaceVertex[] vertices)
	{
		StsPoint point;
		StsGridSectionPoint gridSectionPoint;
		StsSurfaceVertex vertex;

		if (vertices == null) return false;

		int nVertices = vertices.length;
		StsGridSectionPoint[] gridPoints = new StsGridSectionPoint[nVertices];
		for(int n = 0; n < nVertices; n++)
				gridPoints[n] = vertices[n].getSurfacePoint();

		int nPoints = gridPoints.length;
		if (nPoints == 0)
		{
			return false;
		}
		if (lineVectorSet == null)
		{
			lineVectorSet = StsObjectRefList.constructor(nPoints, 1, "lineVectorSet", this);
		}
		points = new StsPoint[nPoints];
		StsProject project = currentModel.getProject();
		xOrigin = project.getXOrigin();
		yOrigin = project.getYOrigin();
		for (int n = 0; n < nPoints; n++)
		{
			float rowF = gridPoints[n].getGridRowF();
			float colF = gridPoints[n].getGridColF();

			point = gridPoints[n].point.copy(); // make a copy of rotated point; will subsequently unrotate it
//            gridPoints[n].point = point;
			points[n] = point;
			float[] unrotatedXY = getUnrotatedRelativeXYFromRotatedXY(point.getX(), point.getY());
//            point = gridPoints[n].point.copy();
			point.setX(unrotatedXY[0]);
			point.setY(unrotatedXY[1]);
			gridSectionPoint = new StsGridSectionPoint(point, rowF, colF, null, null, true);
			vertex = new StsSurfaceVertex(gridSectionPoint, this); // point is cloned
			getLineVectorSet().add(vertex);
			if (stsColor == null)
			{
				stsColor = currentModel.getSpectrumClass().getCurrentSpectrumColor("Basic");
			}
		}
		return true;
	}
    */
    private float[] getUnrotatedRelativeXY(float x, float y)
    {
        return currentModel.getProject().getUnrotatedRelativeXYFromRotatedXY(x, y);
    }

    public float getDipAngle(float[] dipDirectionVector, StsPoint linePoint, int sectionEnd)
    {
        StsPoint topPoint, botPoint, lineVector;

		if(lineSections != null)
		{
			float[] sectionDipVector = StsLineSections.getDipDirectionVector(this, linePoint, sectionEnd);
			if(sectionDipVector != null) dipDirectionVector = sectionDipVector;
		}

        topPoint = getTopPoint();
        botPoint = getBotPoint();

        lineVector = new StsPoint(botPoint);
        lineVector.subtract(topPoint);
        lineVector.normalize();

        float horizontal = StsMath.dot(dipDirectionVector, lineVector.v);
        float vertical = lineVector.getZorT();
        float dipAngle = StsMath.atan2(vertical, horizontal);
        if (dipAngle > 180.0f)
        {
            dipAngle -= 360.0f; // reset dipAngle between -180 to +180
        }
        return dipAngle;
    }

    public void adjustDipAngle(float dipAngleChange, float[] axis, StsPoint linePoint)
    {
        StsRotationMatrix rotMatrix;

        rotMatrix = StsRotationMatrix.constructRotationMatrix(linePoint.v, axis, dipAngleChange);

        StsPoint[] points = getAsCoorPoints();
        int nPoints = points.length;
        for (int n = 0; n < nPoints; n++)
            rotMatrix.pointRotate(points[n].v);
        getLineVectorSet().setPoints(points);
        computePointsProjectToSection();
        setIsVertical(false);
    }

    public StsPoint getPointOnLineNearestMouse(int nSegment, StsMouse mouse, StsGLPanel3d glPanel3d)
    {
        int n = Math.min(getNValues() - 1, nSegment);
        StsPoint point0 = getLineVectorSet().getXYZorTPoint(n);
        StsPoint point1 = getLineVectorSet().getXYZorTPoint(n);
        return glPanel3d.getPointOnLineNearestMouse(mouse, point0, point1);
    }

    public boolean extendEnds()
    {
        float z;
        StsPoint point;
        float zTop = getLineVectorSet().getFirstCoorPoint().getZorT();
        float zBot = getLineVectorSet().getLastCoorPoint().getZorT();
        float zDif = zBot - zTop;
        zTop = zTop - zDif / 2;
        zBot = zBot + zDif / 2;
        StsProject project = currentModel.getProject();

        int sectionIndexMin = project.getIndexAbove(zTop);
        int sectionIndexMax = project.getIndexBelow(zBot);
        zTop = project.getZAtIndex(sectionIndexMin);
        zBot = project.getZAtIndex(sectionIndexMax);
        setZTMin(zTop);
        setZTMax(zBot);
        return computeExtendedPoints(zTop, zBot);
    }

    /*
// Extend ends to top and bottom of project
    public void extendEnds()
    {
     float z;
     StsPoint point;

           computeArcPoints();
     StsProject project = currentModel.getProject();
     z = project.getZMin();
     point = getPointAtZ(z, true);
           addLinePoint(point, true);
     z = project.getZMax();
     point = getPointAtZ(z, true);
           addLinePoint(point, true);
           computeArcPoints();
    }
    */
    public StsRotatedBoundingBox getRotatedBoundingBox()
    {
        if (boundingBox != null) return boundingBox;
        return getLineVectorSet().getRotatedBoundingBox();
    }

    public void addToUnrotatedBoundingBox(StsBoundingBox unrotatedBoundingBox)
    {
        StsBoundingBox lineBoundingBox = getLineVectorSet().getUnrotatedBoundingBox();
        unrotatedBoundingBox.addBoundingBox(lineBoundingBox);
    }

    public StsBoundingBox getUnrotatedBoundingBox() { return getLineVectorSet().getUnrotatedBoundingBox(); }

    public void addToRotatedBoundingBox(StsRotatedBoundingBox rotatedBoundingBox)
    {
        StsRotatedBoundingBox lineBoundingBox = getRotatedBoundingBox();
        rotatedBoundingBox.addBoundingBox(lineBoundingBox);
    }

    public void logMessage()
    {
        logMessage(lineOnSectionLabel());
    }
            /*
    public float[] getRelativeRange(StsVector xDataVector, StsVector yDataVector, StsVector zDataVector)
    {
        float xMin = xDataVector.getMinValue();
        float xMax = xDataVector.getMaxValue();
        float yMin = yDataVector.getMinValue();
        float yMax = yDataVector.getMaxValue();
        float zMin = zDataVector.getMinValue();
        float zMax = zDataVector.getMaxValue();

        return new float[] { xMin, xMax, yMin, yMax, zMin, zMax };
    }
*/
    public StsFieldBean[] getDisplayFields()
    {
        if (pseudoDisplayFields == null)
        {
            pseudoDisplayFields = new StsFieldBean[]
                {
                    new StsBooleanFieldBean(StsLine.class, "isVisible", "Enable"),
                    new StsBooleanFieldBean(StsLine.class, "drawZones", "Zones"),
                    new StsColorIndexFieldBean(StsLine.class, "stsColorIndex", "Color", colorList),
                    new StsFloatFieldBean(StsLine.class, "topZ", false, "Min Depth"),
                    new StsFloatFieldBean(StsLine.class, "botZ", false, "Max Depth"),
                    new StsDoubleFieldBean(StsLine.class, "xOrigin", false, "X Origin"),
                    new StsDoubleFieldBean(StsLine.class, "yOrigin", false, "Y Origin")
                };
        }
        return pseudoDisplayFields;
    }

    public StsFieldBean[] getPropertyFields()
    {
        return null;
    }

    public Object[] getChildren()
    {
        return new Object[0];
    }

    public boolean anyDependencies()
    {
        return false;
    }

    static public StsFieldBean[] getStaticDisplayFields()
    {
        return pseudoDisplayFields;
    }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null)
        {
            objectPanel = StsObjectPanel.constructor(this, true);
        }
        return objectPanel;
    }

    public void treeObjectSelected()
    {
        currentModel.getCreateStsClass(StsLine.class).selected(this);
        this.setStsColor(StsColor.BLACK);
    }

    public StsPoint[] getTimePoints(StsPoint[] depthPoints)
    {
        return null;
    }

    static public void addMDepthToPoints(StsPoint[] points)
    {
        if (points == null) return;
        StsPoint point1 = points[0];
        float mdepth = 0.0f;
        point1.setM(0.0f);
        for (int n = 1; n < points.length; n++)
        {
            StsPoint point0 = point1;
            point1 = points[n];
            mdepth += point0.distance(point1);
            point1.setM(mdepth);
        }
    }

    static public void addZorTasMDepthToPoints(StsPoint[] points)
    {
        if (points == null) return;
        for (int n = 0; n < points.length; n++)
            points[n].setM(points[n].getZorT());
    }

    static public void addMDepthToVertices(StsSurfaceVertex[] vertices)
    {
        if (vertices == null) return;
        StsPoint point1 = vertices[0].getPoint();
        float mdepth = 0.0f;
        point1.setM(0.0f);
        for (int n = 1; n < vertices.length; n++)
        {
            StsPoint point0 = point1;
            point1 = vertices[n].getPoint();
            mdepth += point0.distance(point1);
            point1.setM(mdepth);
        }
    }

    public ArrayList getGridCrossingPoints(StsXYSurfaceGridable grid)
    {
        StsPoint[] points = getAsCoorPoints();
        int nPoints = points.length;
        StsGridCrossingPoint[] gridPoints = new StsGridCrossingPoint[nPoints];
        for (int n = 0; n < nPoints; n++)
        {
            gridPoints[n] = new StsGridCrossingPoint(grid, points[n]);
        }
        return getGridCrossingPoints(gridPoints);
    }

    static public ArrayList getGridCrossingPoints(StsGridCrossingPoint[] gridPoints)
    {
        ArrayList list = new ArrayList();
        int nPoints = gridPoints.length;
        StsGridCrossingPoint gridPoint1 = gridPoints[0];
        list.add(gridPoint1);
        StsGridCrossingPoint lastGridPoint = gridPoint1;
        for (int n = 1; n < nPoints; n++)
        {
            StsGridCrossingPoint gridPoint0 = gridPoint1;
            gridPoint1 = gridPoints[n];
            StsGridCrossings gridCrossings = new StsGridCrossings(gridPoint0, gridPoint1, true);
            ArrayList gridCrossingPoints = gridCrossings.gridPoints;
            int nCrossings = gridCrossingPoints.size();
            for (int i = 0; i < nCrossings; i++)
            {
                StsGridCrossingPoint gridPoint = (StsGridCrossingPoint) gridCrossingPoints.get(i);
                checkAddGridPoint(gridPoint, lastGridPoint, list);
                lastGridPoint = gridPoint;
            }
        }
        if (gridPoint1.rowOrCol == StsParameters.NONE)
        {
            list.add(gridPoint1);
        }
        return list;
    }

    static private void checkAddGridPoint(StsGridCrossingPoint gridPoint, StsGridCrossingPoint lastGridPoint, ArrayList list)
    {
        if (gridPoint.rowOrCol == StsParameters.NONE)
        {
            return;
        }
        if (gridPoint.sameAs(lastGridPoint))
        {
            return;
        }
        list.add(gridPoint);
    }

    static public ArrayList getCellCrossingPoints(StsXYGridable grid, StsPoint[] points)
    {
        //        StsPoint[] points = getRotatedPoints();
        int nPoints = points.length;
        StsGridCrossingPoint[] gridPoints = new StsGridCrossingPoint[nPoints + 2];
        // extend the curtain laterally at the top and bottom so the viewer has good coverage
        // if the well is vertical, just make it 20 traces wide in inline direction
        for (int n = 1; n <= nPoints; n++)
        {
            gridPoints[n] = new StsGridCrossingPoint(grid, points[n - 1]);
            gridPoints[n].adjustToCellGridding();
        }
        int maxNRow = grid.getNRows() - 1;
        int maxNCol = grid.getNCols() - 1;
        float dRowF, dColF;
        float rowF1, rowF2, colF1, colF2;
        rowF1 = gridPoints[1].iF;
        rowF2 = gridPoints[nPoints].iF;
        dRowF = rowF2 - rowF1;
        colF1 = gridPoints[1].jF;
        colF2 = gridPoints[nPoints].jF;
        dColF = colF2 - colF1;
        // well is vertical: extend in inline direction
        if (Math.abs(dRowF) < 0.01f && Math.abs(dColF) < 0.01f)
        {
            rowF1 = Math.max(0, rowF1 - 10);
            gridPoints[0] = new StsGridCrossingPoint(rowF1, colF1, grid);
            rowF2 = Math.min(maxNRow, rowF1 + 20);
            gridPoints[nPoints + 1] = new StsGridCrossingPoint(rowF2, colF2, grid);
        }
        // well is almost vertical: extend in direction of plane thru top and bottom points
        else if (Math.abs(dRowF) < 1.0f && Math.abs(dColF) < 1.0f || nPoints <= 4)
        {
            float d = (float) Math.sqrt(dRowF * dRowF + dColF * dColF);
            float ratio = 10 / d;

            rowF1 -= dRowF * ratio;
            rowF1 = StsMath.minMax(rowF1, 0, maxNRow);
            colF1 -= dColF * ratio;
            colF1 = StsMath.minMax(colF1, 0, maxNCol);
            gridPoints[0] = new StsGridCrossingPoint(rowF1, colF1, grid);

            rowF2 += dRowF * ratio;
            rowF2 = StsMath.minMax(rowF2, 0, maxNRow);
            colF2 += dColF * ratio;
            colF2 = StsMath.minMax(colF2, 0, maxNCol);
            gridPoints[nPoints + 1] = new StsGridCrossingPoint(rowF2, colF2, grid);
        }
        else // search up from bottom and top until we have enough offset to locate an added point
        {
            rowF1 = gridPoints[1].iF;
            colF1 = gridPoints[1].jF;
            for (int n = 2; n < nPoints / 2; n++)
            {
                rowF2 = gridPoints[n].iF;
                dRowF = rowF2 - rowF1;
                colF2 = gridPoints[n].jF;
                dColF = colF2 - colF1;
                if (Math.abs(dRowF) > 1.0f || Math.abs(dColF) > 1.0f)
                {
                    break;
                }
            }
            float d = (float) Math.sqrt(dRowF * dRowF + dColF * dColF);
            float ratio = 10 / d;

            rowF1 -= dRowF * ratio;
            rowF1 = StsMath.minMax(rowF1, 0, maxNRow);
            colF1 -= dColF * ratio;
            colF1 = StsMath.minMax(colF1, 0, maxNCol);
            gridPoints[0] = new StsGridCrossingPoint(rowF1, colF1, grid);

            rowF1 = gridPoints[nPoints].iF;
            colF1 = gridPoints[nPoints].jF;
            for (int n = nPoints - 1; n >= nPoints / 2; n--)
            {
                rowF2 = gridPoints[n].iF;
                dRowF = rowF2 - rowF1;
                colF2 = gridPoints[n].jF;
                dColF = colF2 - colF1;
                if (Math.abs(dRowF) > 1.0f || Math.abs(dColF) > 1.0f)
                {
                    break;
                }
            }
            d = (float) Math.sqrt(dRowF * dRowF + dColF * dColF);
            ratio = 10 / d;

            rowF1 -= dRowF * ratio;
            rowF1 = StsMath.minMax(rowF1, 0, maxNRow);
            colF1 -= dColF * ratio;
            colF1 = StsMath.minMax(colF1, 0, maxNCol);
            gridPoints[nPoints + 1] = new StsGridCrossingPoint(rowF1, colF1, grid);
        }
        return getGridCrossingPoints(gridPoints);
    }

    public boolean adjustTimeOrDepth(StsSeismicVelocityModel velocityModel)
    {
        return adjustTimeOrDepth(velocityModel, isOriginalDepth());
    }

    public boolean isOriginalDepth()
    {
        return getZDomainOriginal() == StsProject.TD_DEPTH;
    }

    public boolean adjustTimeOrDepth(StsSeismicVelocityModel velocityModel, boolean isOriginalDepth)
    {
        int nVertices = getLineVectorSet().getVectorsSize();
        boolean success = true;
        StsPoint[] points = getAsCoorPoints();
        for (int n = 0; n < nVertices; n++)
        {
            StsPoint point = points[n];
            if (!velocityModel.adjustTimeOrDepthPoint(point, isOriginalDepth))
                success = false;
            else
                getLineVectorSet().setPoint(point, n);
        }
        float zMin = currentModel.getProject().getZorTMin();
        float zMax = currentModel.getProject().getZorTMax();
		if(lineSections != null) StsLineSections.computeExtendedPointsProjectToSection(this, zMin, zMax);
        return success;
    }

/*
    public void setVerticesRotated(boolean verticesRotated)
    {
        fieldChanged("isVerticesRotated", verticesRotated);
    }
*/
    public byte getZDomainOriginal()
    {
        return zDomainOriginal;
    }

    public void setZDomainOriginal(byte zDomainOriginal)
    {
        this.zDomainOriginal = zDomainOriginal;
        this.setZDomainSupported(zDomainOriginal);
    }

    public boolean checkAdjustFromVelocityModel()
    {
        StsSeismicVelocityModel velocityModel = currentModel.getProject().getSeismicVelocityModel();
        if (velocityModel != null)
            return adjustFromVelocityModel(velocityModel);
        else
            return true;
    }

    public boolean adjustFromVelocityModel(StsSeismicVelocityModel velocityModel)
    {
        if (getLineVectorSet() == null) return false;
        try
        {
            int nLineVertices = getLineVectorSet().getVectorsSize();

            float maxChange = 0.0f;
            float[][] xyztVectors = getLineVectorSet().getXYZorT_3FloatVectors(zDomainOriginal == StsProject.TD_DEPTH);
            float tEst = 0.0f;
            for (int n = 0; n < nLineVertices; n++)
            {
                if (zDomainOriginal == StsProject.TD_DEPTH)
                {
                    float[] times = velocityModel.getTVector(nLineVertices, xyztVectors);
                    getLineVectorSet().setCoorFloats(StsLineVectorSet.COL_T, times);
                }
                else // zDomainOriginal is TIME
                {
                    float[] depths = velocityModel.getZVector(nLineVertices, xyztVectors);
                    getLineVectorSet().setCoorFloats(StsLineVectorSet.COL_Z, depths);
                }
            }
            //             saveVertexTimesToDB(times);
            StsMessageFiles.logMessage("Well " + getName() + " max time adjustment " + maxChange);
            //             currentModel.addMethodCmd(this, "adjustTimePoints", new Object[] {velocityModel}, "adjustTimePoints for " + getName());
            return true;
        }
        catch (Exception e)
        {
            StsMessageFiles.errorMessage("Failed to adjust well " + getName() + " points probably not in time.");
            return false;
        }
    }

    public StsPoint getCurrentPoint()
    {


        if(selectedPointIndex == -1) return null;
        return getLineVectorSet().getCoorPoint(selectedPointIndex);
    }

    public int getNValues()
    {
        return getLineVectorSet().getNValues();
    }

    public StsPoint[] getAsCoorPoints() { return getLineVectorSet().getCoorsAsPoints(); }

	public void initializeBoundingBox(double xOrigin, double yOrigin)
    {
		setOrigin(xOrigin, yOrigin);
		adjustBoundingBox();
	}

	 public void adjustBoundingBox()
	 {
        StsProject project = getCurrentProject();
        boolean supportsTime = project.supportsTime(getZDomainSupported());
        boolean supportsDepth = project.supportsDepth(getZDomainSupported());
		if(supportsDepth)
		{
			 double[] zRange = getLineVectorSet().getZAbsoluteRange();
			 float[] scale = project.niceZTScale((float) zRange[0], (float) zRange[1]);
			 float depthMin = scale[0];
			 float depthMax = scale[1];
			 float depthInc = scale[2];
			 setZMin(depthMin);
			 setZMax(depthMax);
			 project.checkSetDepthInc(depthInc);
		}
        if(supportsTime)
        {
            double[] timeRange = getLineVectorSet().getTAbsoluteRange();
			if(timeRange != null)
			{
				setTMin((float)timeRange[0]);
				setTMax((float)timeRange[1]);
			}
        }

        float[] xRange = getLineVectorSet().getXRelativeRange();
        float[] yRange = getLineVectorSet().getYRelativeRange();
        setXMin(xRange[0]);
        setXMax(xRange[1]);
        setYMin(yRange[0]);
        setYMax(yRange[1]);
    }

	public float[] getLocalOrigin() { return getLineVectorSet().getLocalOrigin();  }

	public void addVectorSetToObject(StsLineVectorSet vectorSet)
	{
		vectorSet.checkAddVectors(true);
		if(getLineVectorSet().getTVector() != null)
			setZDomainSupported(StsProject.TD_TIME_DEPTH);
		vectorSet.addToModel();
	}
	public String getAsciiDirectoryPathname()
	{
		return getProject().getAsciiDirectoryPathname(getClassSubDirectoryString(), name);
	}
	public String getBinaryDirectoryPathname()
	{
		return getProject().getBinaryDirectoryPathname(getClassSubDirectoryString(), name);
	}
/*
	public void setZDomainSupported(byte zDomain)
	{
		if(this.zDomainSupported == zDomain) return;
		this.zDomainSupported = zDomain;
		dbFieldChanged("zDomainSupported", zDomainSupported);
	}
*/
	public float[] getMDepthFloats()
	{
		StsLineVectorSet lineVectorSet = getLineVectorSet();
		if(lineVectorSet == null) return null;
		if(lineVectorSet.getNValues() < 2) return null;
		return lineVectorSet.getMFloats();
	}

	public float[] getDepthFloats()
	{
		StsLineVectorSet lineVectorSet = getLineVectorSet();
		if(lineVectorSet == null) return null;
		if(lineVectorSet.getNValues() < 2) return null;
		return lineVectorSet.getZFloats();
	}

	public float[] computeMDepthsFromDepths(float[] depths)
	{
		return getLineVectorSet().computeMDepthsFromDepths(depths);
	}
	public float[] computeMDepthsFromLogDepths(StsLogVectorSet logVectorSet)
	{
		return getLineVectorSet().computeMDepthsFromLogDepths(logVectorSet);
	}

	public float[] computeDepthsFromLogMDepths(StsLogVectorSet logVectorSet)
	{
		return getLineVectorSet().computeMDepthsFromLogDepths(logVectorSet);
	}

	public float[] computeDepthsFromMDepths(float[] mdepths)
	{
		return getLineVectorSet().computeDepthsFromMDepths(mdepths);
	}
	public boolean checkComputeXYLogVectors(StsLogVectorSet logVectorSet)
	{
		return getLineVectorSet().checkComputeXYLogVectors(logVectorSet);
	}

	static public String getClassSubDirectoryString()
	{
	  	return "StsLines" + File.separator;
	}

	public float getZScalar()
	{
		return getLineVectorSet().getZScalar();
	}

	public float getXyScalar()
	{
		return getLineVectorSet().getXyScalar();
	}

	public StsVectorSet getVectorSet()
	{
		return getLineVectorSet();
	}

	public boolean checkSetProjectTime(long time)
	{
		if(getLineVectorSet() == null) return false;
			return getLineVectorSet().checkSetProjectTime(time);
	}

	public boolean disableTime()
	{
		if(getLineVectorSet() == null) return false;
		return getLineVectorSet().disableTime();
	}

	public StsObject getLineSections()
	{
		return lineSections;
	}

	public void setLineSections(StsObject lineSections)
	{
		this.lineSections = lineSections;
	}
}
