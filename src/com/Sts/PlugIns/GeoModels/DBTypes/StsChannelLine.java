//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System


package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DBTypes.StsClass;
import com.Sts.Framework.DBTypes.StsObject;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.DBTypes.VectorSetObjects.StsLineVectorSetObject;
import com.Sts.Framework.Interfaces.MVC.StsInstance3dDisplayable;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsViewSelectable;
import com.Sts.Framework.Interfaces.StsXYGridable;
import com.Sts.Framework.Interfaces.StsXYSurfaceGridable;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.MVC.Views.StsGLPanel;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;
import com.Sts.Framework.UI.StsMessageFiles;
import com.Sts.Framework.UI.StsYesNoDialog;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.DBTypes.StsLineSections;
import com.Sts.PlugIns.Model.DBTypes.StsSurfaceVertex;
import com.Sts.PlugIns.Seismic.DBTypes.StsSeismicVelocityModel;

import javax.media.opengl.GL;
import java.io.File;
import java.util.ArrayList;

public class StsChannelLine extends StsLineVectorSetObject implements StsTreeObjectI, StsInstance3dDisplayable, StsViewSelectable
{
   /** persisted index of color in spectrum */
	protected int nColor = 0;

	transient StsColor stsColor;

    static public StsObjectPanel objectPanel = null;

    /** draw the line highlighted */
    protected boolean highlighted = false;

    transient protected StsRotatedBoundingBox boundingBox = null;

    // display fields: renamed to pseudoDisplayFields because parent class has static displayFields also
    static public StsFieldBean[] pseudoDisplayFields = null;

	/** these are colors used in drawing line; actual color is defined by nColor */
	static public StsColor[] colorList = StsColor.basic32Colors;

    /** default constructor */
    public StsChannelLine()
    {
    }

    public StsChannelLine(String name, boolean persistent)
    {
        this(persistent);
        setName(name);
    }

    public StsChannelLine(boolean persistent)
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
        return initializeLine();
    }

    protected boolean initializeLine()
    {
        stsColor = colorList[nColor];
        StsLineVectorSet lineVectorSet = getLineVectorSet();
        if(lineVectorSet == null) return false;
        return lineVectorSet.initialize();
    }

    static public StsChannelLine buildStraightLine(StsPoint firstPoint, StsPoint lastPoint)
    {
        StsChannelLine line = new StsChannelLine(false);
        line.setZDomainOriginal(currentModel.getProject().getZDomain());
        if(!line.constructStraightLine(firstPoint, lastPoint)) return null;
        if(!line.checkWriteBinaryFiles()) return null;
        return line;
    }

    private boolean constructStraightLine(StsPoint firstPoint, StsPoint lastPoint)
    {
        try
        {
            setZDomainOriginal(currentModel.getProject().getZDomain());

            if (getLineVectorSet() == null)
                setTimeVectorSet(StsLineVectorSet.constructor());

            StsProject project = currentModel.getProject();

            setZDomainSupported(project.getZDomainSupported());

            getLineVectorSet().addXyztPoint(firstPoint);
            getLineVectorSet().addXyztPoint(lastPoint);

            if (stsColor == null)
                stsColor = currentModel.getSpectrumClass().getCurrentSpectrumColor("Basic");

            return true;
        }
        catch (Exception e)
        {
            StsException.systemError("StsLine.buildVertical(gridPoint) failed.");
            return false;
        }
    }

    public void addToModel()
    {
        if(currentModel == null) return;
        vectorSet.addToModel();
        currentModel.add(this);
//        refreshObjectPanel();
    }

    public void setZDomainOriginal(byte zDomainOriginal)
    {
        this.zDomainOriginal = zDomainOriginal;
        this.setZDomainSupported(zDomainOriginal);
    }

    public StsBoundingBox getBoundingBox() { return this; }

    public String getName()
    {
        if (name != null)
            return name;
        else
            return "Channel-" + getIndex();
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

    public boolean adjustRotatedPoints(int nFirstIndex)
    {
		if (getLineVectorSet() == null) return false;
		return getLineVectorSet().adjustRotatedPoints(nFirstIndex);
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

        drawLine(gl, stsColor, highlighted, drawDotted);
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

    public void display2d(StsGLPanel3d glPanel3d, boolean displayName, int dirNo,
                          float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed)
    {
        if (glPanel3d == null) return;
        if (!isVisible()) return;
        GL gl = glPanel3d.getGL();
        if (gl == null) return;

        stsColor.setGLColor(gl);
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
        StsGLDraw.drawLine2d(gl, color, false, lineVectorFloats, range[0], range[1], nXAxis2d, nYAxis2d);
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

	public boolean addToProject()
	{
		getLineVectorSet().checkSetCurrentTime();
		return currentModel.getProject().addToProjectUnrotatedBoundingBox(this, StsProject.TD_DEPTH);
	}

	public void setTimeIndex(boolean timeEnabled)
	{
		getLineVectorSet().setTimeIndex(timeEnabled);
	}

    public StsPoint createStsPoint(float x, float y, float t, float d)
    {
        StsPoint point = new StsPoint(5);
        point.setX(x);
        point.setY(y);
        point.setZ(d);
        point.setT(t);
        return point;
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

    public StsFieldBean[] getDisplayFields()
    {
        if (pseudoDisplayFields == null)
        {
            pseudoDisplayFields = new StsFieldBean[]
                {
                    new StsBooleanFieldBean(StsChannelLine.class, "isVisible", "Enable"),
                    new StsColorIndexFieldBean(StsChannelLine.class, "stsColorIndex", "Color", colorList),
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
        currentModel.getCreateStsClass(StsChannelLine.class).selected(this);
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

    public boolean isOriginalDepth()
    {
        return getZDomainOriginal() == StsProject.TD_DEPTH;
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

	public boolean checkComputeXYLogVectors(StsLogVectorSet logVectorSet)
	{
		return getLineVectorSet().checkComputeXYLogVectors(logVectorSet);
	}

	static public String getClassSubDirectoryString()
	{
	  	return "StsChannels" + File.separator;
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
}
