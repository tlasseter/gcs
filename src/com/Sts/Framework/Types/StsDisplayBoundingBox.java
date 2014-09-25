package com.Sts.Framework.Types;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Utilities.*;
import com.magician.fonts.*;

import javax.media.opengl.*;
import java.io.*;
import java.text.*;

/** The displayed bounding box for the project.  Constructed by taking the boundingBox
 *  around the project and expanding it to "rounded" values of xMin, xMax, yMin, and yMax
 */

public class StsDisplayBoundingBox extends StsBoundingBox implements Cloneable, Serializable
{
	/** displayed grid spacing in x */
    public float gridDX;
	/** displayed grid spacing in y */
    public float gridDY;
	/** set according to StsProject.gridLocation and zMin and zMax values */
    transient private float gridZ = StsParameters.nullValue;
    /** set according to StsProject.gridLocation and tMin and tMax values */
    transient private float gridT = StsParameters.nullValue;

	/** Approx number of XY Grid increments */
	static public int approxNumXYGridIncrements = 20;

//    public StsVerticalFont verticalFont = new StsVerticalFont(new GLHelvetica12BitmapFont());
//    public DecimalFormat labelFormat = new DecimalFormat("#,##0.0#");

    public StsDisplayBoundingBox()
    {
    }

	public StsDisplayBoundingBox(boolean persistent)
	{
		super(persistent);
	}

	public StsDisplayBoundingBox(boolean persistent, String name)
	{
		super(persistent, name);
	}

    public void initialize(StsBoundingBox box, boolean initialized)
    {
        super.initialize(box, initialized);
        initializeGridZTLocation();
    }

    public boolean initialize(StsModel model)
    {
        return true;
    }

	public void setZRange(float zMin, float zMax)
	{
		this.zMin = zMin;
		this.zMax = zMax;
		initializeGridZTLocation();
	}

	/* Starting with current boundingBox, adjust x, y, and z ranges so they are "nice".
	 * @parameters model the current StsModel object
	 * @see setValuesFromBoundingBox
	 * @see StsMath.niceScale2
	 *  */
	public void adjustToNiceSize()
	{
		float dSize = 0.0f;
		// if bounding box is tall and skinny, extend size of box laterally
		float xSize = xMax - xMin;
		float ySize = yMax - yMin;
		float zSize = zMax - getZMin();
		if (zSize > 2.0f * xSize || zSize > 2.0f * ySize) dSize = 0.5f*zSize;
		xMin -= dSize;
		xMax += dSize;
		yMin -= dSize;
		yMax += dSize;

		// find nice integral range for x and y
		double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, approxNumXYGridIncrements, true);
		xMin = (float) (xScale[0] - xOrigin);
		xMax = (float) (xScale[1] - xOrigin);
		gridDX = (float) xScale[2];
		double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, approxNumXYGridIncrements, true);
		yMin = (float) (yScale[0] - yOrigin);
		yMax = (float) (yScale[1] - yOrigin);
		gridDY = (float) yScale[2];
	}

    /** Copy the unrotatedBoundingBox to create the initial displayBoundingBox.  Extend in X and Y if too tall and skinny.
     *  Then round the dimensions up and down to a nice absolute scale.
     * @param boundingBox the input unrotatedBoundingBox which tightly contains all current objects
     */
	public void adjustBoundingBox(StsBoundingBox boundingBox)
	{
        initialize(boundingBox);
		// if display bounding box is tall and skinny, extend size of box laterally
		float xSize = xMax - xMin;
		float ySize = yMax - yMin;
		float zSize = zMax - getZMin();
		if (zSize > 2.0f * xSize || zSize > 2.0f * ySize)
        {
            float dSize = 0.5f*zSize;
            xMin -= dSize;
            xMax += dSize;
            yMin -= dSize;
            yMax += dSize;
        }
		// find nice integral range for x and y
		double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, approxNumXYGridIncrements, true);
		xMin = (float) (xScale[0] - xOrigin);
		xMax = (float) (xScale[1] - xOrigin);
		gridDX = (float) xScale[2];
		double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, approxNumXYGridIncrements, true);
		yMin = (float) (yScale[0] - yOrigin);
		yMax = (float) (yScale[1] - yOrigin);
		gridDY = (float) yScale[2];
    }

	public boolean niceAdjustBoundingBoxXY()
	{
		// if display bounding box is tall and skinny, extend size of box laterally
		float xSize = xMax - xMin;
		float ySize = yMax - yMin;
		float zSize = zMax - getZMin();
        boolean changed = false;
		if (zSize > 2.0f * xSize || zSize > 2.0f * ySize)
        {
            float dSize = 0.5f*zSize;
            xMin -= dSize;
            xMax += dSize;
            yMin -= dSize;
            yMax += dSize;
            changed = true;
        }
		// find nice integral range for x and y
		double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, approxNumXYGridIncrements, true);
		xMin = (float) (xScale[0] - xOrigin);
		xMax = (float) (xScale[1] - xOrigin);
		gridDX = (float) xScale[2];
		double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, approxNumXYGridIncrements, true);
		yMin = (float) (yScale[0] - yOrigin);
		yMax = (float) (yScale[1] - yOrigin);
		gridDY = (float) yScale[2];
        return changed;
    }

    public boolean niceAdjustBoundingBoxZ(StsRotatedGridBoundingBox rotatedBoundingBox, int approxNumZGridIncs, byte zDomainSupported)
    {
        boolean supportsTime = StsProject.supportsTime(zDomainSupported);
        boolean supportsDepth = StsProject.supportsDepth(zDomainSupported);
        boolean changed = false;
        if(supportsTime)
        {
            float tMin = rotatedBoundingBox.tMin;
            float tMax = rotatedBoundingBox.tMax;
            float tDif = tMax - tMin;
            float[] scale = StsMath.niceScale(tMin - tDif / 2, tMax + tDif / 2, approxNumZGridIncs, true);
            changed = changed | adjustTMin(scale[0]);
            changed = changed | adjustTMax(scale[1]);
        }
        if(supportsDepth)
        {
            float zMin = rotatedBoundingBox.getZMin();
            float zMax = rotatedBoundingBox.zMax;
            float zDif = zMax - zMin;
            float[] scale = StsMath.niceScale(zMin - zDif / 2, zMax + zDif / 2, approxNumZGridIncs, true);
            changed = changed | adjustZMin(scale[0]);
            changed = changed | adjustZMax(scale[1]);
        }
        return changed;
    }

    public void initialize(StsBoundingBox boundingBox)
    {
        super.initialize(boundingBox);
        initializeGridZTLocation();
    }

	/* Starting with current boundingBox, adjust x, y, and z ranges so they are "nice".
	 * @parameters model the current StsModel object
	 * @see setValuesFromBoundingBox
	 * @see StsMath.niceScale2
	 *  */
	public void adjustBoundingBoxXYRange(double xOrigin, double yOrigin, float xMin, float xMax, float yMin, float yMax)
	{
        setOrigin(xOrigin, yOrigin);

		// if display bounding box is tall and skinny, extend size of box laterally
		float xSize = xMax - xMin;
		float ySize = yMax - yMin;
		float zSize = zMax - getZMin();
		if (zSize > 2.0f * xSize || zSize > 2.0f * ySize)
        {
            float dSize = 0.5f*zSize;
            xMin -= dSize;
            xMax += dSize;
            yMin -= dSize;
            yMax += dSize;
        }

		// find nice integral range for x and y
		double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, approxNumXYGridIncrements, true);
		this.xMin = (float) (xScale[0] - xOrigin);
		this.xMax = (float) (xScale[1] - xOrigin);
		gridDX = (float) xScale[2];
		double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, approxNumXYGridIncrements, true);
		this.yMin = (float) (yScale[0] - yOrigin);
		this.yMax = (float) (yScale[1] - yOrigin);
		gridDY = (float) yScale[2];
	}

	/* Starting with current boundingBox, adjust x, y, and z ranges so they are "nice".
	 * @parameters model the current StsModel object
	 * @see setValuesFromBoundingBox
	 * @see StsMath.niceScale2
	 *  */
/*
	public void adjustBoundingBoxToNiceSize(StsRotatedGridBoundingBox boundingBox)
	{
		xOrigin = boundingBox.xOrigin;
		yOrigin = boundingBox.yOrigin;
		computeUnrotatedBoundingBox(boundingBox);
		zMin = boundingBox.zMin;
		zMax = boundingBox.zMax;

		float dSize = 0.0f;

		// if bounding box is tall and skinny, extend size of box laterally
		float xSize = xMax - xMin;
		float ySize = yMax - yMin;
		float zSize = zMax - zMin;
		if (zSize > 2.0f * xSize || zSize > 2.0f * ySize) dSize = 0.5f*zSize;
		xMin -= dSize;
		xMax += dSize;
		yMin -= dSize;
		yMax += dSize;

		// find nice integral range for x and y
		double[] xScale = StsMath.niceScale(xMin + xOrigin, xMax + xOrigin, approxNumXYGridIncrements, true);
		xMin = (float) (xScale[0] - xOrigin);
		xMax = (float) (xScale[1] - xOrigin);
		gridDX = (float) xScale[2];
		double[] yScale = StsMath.niceScale(yMin + yOrigin, yMax + yOrigin, approxNumXYGridIncrements, true);
		yMin = (float) (yScale[0] - yOrigin);
		yMax = (float) (yScale[1] - yOrigin);
		gridDY = (float) yScale[2];

		double[] zScale = StsMath.niceScale(zMin, zMax, approxNumZGridIncrements, true);
		zMin = (float)(zScale[0]);
		zMax = (float)(zScale[1]);
		gridZ = zMax;
		boundingBox.zInc = (float)zScale[2];
	}
*/

    public boolean addRotatedBoundingBox(StsRotatedBoundingBox box)
    {
        boolean changed = super.addRotatedBoundingBox(box);
        if(initializedZ()) initializeGridZTLocation();
        if(initializedT()) setGridT(tMax);

        return changed;
    }

	public boolean addBoundingBox(StsBoundingBox boundingBox)
	{
		boolean changed = super.addBoundingBox(boundingBox);
		// setGridZT();
        return changed;
	}

	public void setGridZT()
	{
		if(initializedZ()) setGridZ(zMax);
		if(initializedT()) setGridT(tMax);
	}
	/** Starting with current boundingBox, adjust x, y, and z grid lines so they are "nice".
	 * @parameters model the current StsModel object
	 * @see setValuesFromBoundingBox
	 * @see StsMath.niceScale2
	 */
/*
	public void adjustBoundingBoxGridLines(StsRotatedGridBoundingBox boundingBox)
	{
		// find nice integral range for x and y
		double xOrigin = boundingBox.xOrigin;
		double[] xScale = StsMath.niceScale2(xMin+xOrigin, xMax+xOrigin, approxNumXYGridIncrements, true);
		gridDX = (float)xScale[2];
		double yOrigin = boundingBox.yOrigin;
		double[] yScale = StsMath.niceScale2(yMin+yOrigin, yMax+yOrigin, approxNumXYGridIncrements, true);
		gridDY = (float)yScale[2];
		double[] zScale = StsMath.niceScale2(zMin, zMax, approxNumZGridIncrements, true);
		zMin = (float)(zScale[0]);
		zMax = (float)(zScale[1]);
		gridZ = zMax;
		boundingBox.zInc = (float)zScale[2];
	}
*/
	/** Display method for Project */
	public void display(StsGLPanel3d glPanel3d, StsModel model, float angle)
	{
		GL gl = glPanel3d.getGL();
		if(gl == null)return;

		try
		{
			if(model == null)return;
			StsProject project = model.getProject();

			if(angle != 0.0f)
			{
                gl.glPushMatrix();
				gl.glRotatef(angle, 0.0f, 0.0f, -1.0f);
			}

			float x, y;

			/* All lines are at grid_z */
			if(project.getShowGrid())
			{
                gl.glDisable(GL.GL_LIGHTING);
			    gl.glLineWidth(StsGraphicParameters.gridLineWidth);
			    gl.glLineStipple(1, StsGraphicParameters.dottedLine);
			    gl.glEnable(GL.GL_LINE_STIPPLE);
				/** draw lines with gray color */
				project.getGridColor().setGLColor(gl);

				float gridZT = project.getGridZT();

				gl.glBegin(GL.GL_LINES);
				{
					/* Draw the grid lines in the x-direction */
					for(x = xMin + gridDX; x < xMax; x += gridDX)
					{
						gl.glVertex3f(x, yMin, gridZT);
                        gl.glVertex3f(x, yMax, gridZT);
					}

					/* Draw the grid lines in the y-direction */
					for(y = yMin + gridDY; y < yMax; y += gridDY)
					{
                        gl.glVertex3f(xMin, y, gridZT);
                        gl.glVertex3f(xMax, y, gridZT);
					}
				}
				gl.glEnd();
                gl.glDisable(GL.GL_LINE_STIPPLE);
                if(gridZT != getZTMax())
                drawZRectangle(gl, gridZT);
			}

			if(!project.getIsVisible())return;

            // if(StsGLPanel.debugProjectionMatrix) glPanel3d.debugPrintProjectionMatrix("StsDisplayBoundingBox.display(). proj matrix before displayBoundingBox call: ");
            displayBoundingBox(gl, project.getStsGridColor(), StsGraphicParameters.gridLineWidth);

			 if(project.getShowLabels())
			 {
				 String label1, label2, label3, label4;
				 GLBitmapFont horizontalFont = GLHelvetica12BitmapFont.getInstance(gl);
				 DecimalFormat labelFormat = new DecimalFormat("###0");

				 labelFormat = model.getProject().getLabelFormat();

				 // Draw Edge Labels
		//			if(!glPanel3d.getCursor3d().isGridCoordinates())  // Actual Coordinates
				 {
					 label1 = new String("Xmin=" + labelFormat.format(xMin + xOrigin) + " Ymin=" + labelFormat.format(yMin + yOrigin));
					 label2 = new String("Xmax=" + labelFormat.format(xMax + xOrigin));
					 label3 = new String("Ymax=" + labelFormat.format(yMax + yOrigin));
		//				label4 = new String("Z=" + labelFormat.format(zMax));
				 }

				 float ztMin = getZTMin();
				 StsGLDraw.fontOutput(gl, xMin, yMin, ztMin, label1, horizontalFont);
				 StsGLDraw.fontOutput(gl, xMax, yMin, ztMin, label2, horizontalFont);
				 StsGLDraw.fontOutput(gl, xMin, yMax, ztMin, label3, horizontalFont);
			 }
			 else
			 {
				 GLBitmapFont horizontalFont = GLHelvetica18BitmapFont.getInstance(gl);
				 float ztMin = getZTMin();
				 StsGLDraw.fontOutput(gl, xMax, yMin, ztMin, "E", horizontalFont);
				 StsGLDraw.fontOutput(gl, xMin, yMax, ztMin, "N", horizontalFont);
			 }
		}
		catch(Exception e)
		{
			StsException.outputException("StsDisplayBoundingBox.display() failed.", e, StsException.WARNING);
		}
		finally
		{
            if(angle != 0.0f) gl.glPopMatrix();
            gl.glEnable(GL.GL_LIGHTING);
		}
	}
/*
	private void drawCursorCubeEdges(StsGLPanel3d glPanel3d, StsModel model, boolean isPicking)
	{
		GL gl = glPanel3d.getGL();
		if(gl == null) return;

		StsProject project = model.getProject();

		float vec[] = new float[3];

		String label1, label2, label3, label4;
		GLBitmapFont horizontalFont = new GLHelvetica12BitmapFont();
		DecimalFormat labelFormat = new DecimalFormat("###0");

		gl.glDisable(GL.GL_LIGHTING);

		gl.glLineWidth(1.0f);

		// draw lines with gray color
		StsColor.setGLColor(gl, project.getStsGridColor());

		gl.glDisable(GL.GL_LINE_STIPPLE);
	//	set_line_smoothing(current_window, TRUE);

		// Draw the bottom outer boundaries

		vec[2] = zMin;

		float xExt = (xMax - xMin) * 0.05f;
		float yExt = (yMax - yMin) * 0.05f;
		float zExt = (zMax - zMin) * 0.05f;

		gl.glBegin(GL.GL_LINE_LOOP);
		{
			vec[0] = xMin; vec[1] = yMin;
			gl.glVertex3fv(vec);
			vec[0] = xMin; vec[1] = yMax;
			gl.glVertex3fv(vec);
			vec[0] = xMax; vec[1] = yMax;
			gl.glVertex3fv(vec);
			vec[0] = xMax; vec[1] = yMin;
			gl.glVertex3fv(vec);
		}
		gl.glEnd();

		// Draw the top outer boundaries

		vec[2] = zMax;

		gl.glBegin(GL.GL_LINE_LOOP);
		{
			vec[0] = xMin; vec[1] = yMin;
			gl.glVertex3fv(vec);
			vec[0] = xMin; vec[1] = yMax;
			gl.glVertex3fv(vec);
			vec[0] = xMax; vec[1] = yMax;
			gl.glVertex3fv(vec);
			vec[0] = xMax; vec[1] = yMin;
			gl.glVertex3fv(vec);
		}
		gl.glEnd();

		if(project.getShowLabels())
		{
			StsSeismicVolumeClass lineSetClass = (StsSeismicVolumeClass)model.getCreateStsClass(StsSeismicVolume.class);
			if(lineSetClass != null)
				labelFormat = new DecimalFormat(lineSetClass.getLabelFormat());
			else
				labelFormat = new DecimalFormat("###0");

			// Draw Edge Labels
			if(!glPanel3d.getCursor3d().isGridCoordinates())  // Actual Coordinates
			{
				label1 = new String("X=" + labelFormat.format(xMin) + " Y=" +
								   labelFormat.format(yMin) + " Z=" +
								   labelFormat.format(zMin));
				label2 = new String("X=" + labelFormat.format(xMax));
				label3 = new String("Y=" + labelFormat.format(yMax));
				label4 = new String("Z=" + labelFormat.format(zMax));
			}
			else  // Relative Coordinates
			{
				label1 = new String("XL=" + labelFormat.format(glPanel3d.cursor3d.currentSeismicVolume.getLabelFromCoor(StsCursor3d.XDIR,xMin)) + " IL=" + labelFormat.format(yMin) +
									" Z=" + labelFormat.format(glPanel3d.cursor3d.currentSeismicVolume.getLabelFromCoor(StsCursor3d.ZDIR,zMin)));
				label2 = new String("XL=" + labelFormat.format(glPanel3d.cursor3d.currentSeismicVolume.getLabelFromCoor(StsCursor3d.XDIR,xMax)));
				label3 = new String("YL=" + labelFormat.format(glPanel3d.cursor3d.currentSeismicVolume.getLabelFromCoor(StsCursor3d.YDIR,yMax)));
				label4 = new String("Z=" + labelFormat.format(glPanel3d.cursor3d.currentSeismicVolume.getLabelFromCoor(StsCursor3d.ZDIR,zMax)));
			}
			vec[0] = xMin;
			vec[1] = yMin;
			vec[2] = zMin - zExt;
			StsGLDraw.fontOutput(gl, vec, label1, horizontalFont);
			vec[0] = xMax + xExt;
			vec[2] = zMin;
			StsGLDraw.fontOutput(gl, vec, label2, horizontalFont);
			vec[0] = xMin;
			vec[1] = yMax + yExt;
			StsGLDraw.fontOutput(gl, vec, label3, horizontalFont);
			vec[1] = yMin;
			vec[2] = zMax;
			StsGLDraw.fontOutput(gl, vec, label4, horizontalFont);
		}

		// Draw verticals

		gl.glBegin(GL.GL_LINES);
		{
			vec[0] = xMin; vec[1] = yMin;
			vec[2] = zMin - zExt;
			gl.glVertex3fv(vec);
			vec[2] = zMax;
			gl.glVertex3fv(vec);

			vec[0] = xMin; vec[1] = yMax;
			vec[2] = zMin;
			gl.glVertex3fv(vec);
			vec[2] = zMax;
			gl.glVertex3fv(vec);

			vec[0] = xMax; vec[1] = yMax;
			vec[2] = zMin;
			gl.glVertex3fv(vec);
			vec[2] = zMax;
			gl.glVertex3fv(vec);

			vec[0] = xMax; vec[1] = yMin;
			vec[2] = zMin;
			gl.glVertex3fv(vec);
			vec[2] = zMax;
			gl.glVertex3fv(vec);

			// Draw Extensions at X and Y Origin
			vec[2] = zMin;
			vec[0] = xMin - xExt; vec[1] = yMin;
			gl.glVertex3fv(vec);
			vec[0] = xMin;
			gl.glVertex3fv(vec);

			vec[0] = xMin; vec[1] = yMin - yExt;
			gl.glVertex3fv(vec);
			vec[1] = yMin;
			gl.glVertex3fv(vec);
		}
		gl.glEnd();

		gl.glEnable(GL.GL_LIGHTING);

	//	set_line_smoothing(current_window, FALSE);
	}
*/

    /** z value at which grid is displayed */
    public float getGridZ()
    {
        //StsException.systemDebug(this, "getGridZ", "gridZ: " + gridZ);
        return gridZ;
    }

    public void setGridZ(float gridZ)
    {
        this.gridZ = gridZ;
    }

    private void initializeGridZTLocation()
    {
        byte gridLocation = getCurrentProject().getGridLocation();
        setGridZTLocation(gridLocation, isDepth);
    }

    public void setGridZTLocation(byte location, boolean isDepth)
    {
        if(isDepth)
            setGridZLocation(location);
        else
            setGridTLocation(location);
    }

    public void setGridZLocation(byte location)
    {
        if(location == StsProject.BOTTOM)
            gridZ = zMax;
        else if(location == StsProject.TOP)
            gridZ = getZMin();
        else // CENTER
            gridZ = (getZMin() + zMax)/2;
    }

    public void setGridTLocation(byte location)
    {
        if(location == StsProject.BOTTOM)
            gridT = tMax;
        else if(location == StsProject.TOP)
            gridT = tMin;
        else // CENTER
            gridT = (tMin + tMax)/2;
    }

    public void setGridZLocation()
    {
		StsProject project = getCurrentProject();
		if(project == null) return;
        setGridZLocation(project.getGridLocation());
    }

    public void setGridTLocation()
    {
        setGridTLocation(getCurrentProject().getGridLocation());
    }

    /** time value at which grid is displayed */
    public float getGridT()
    {
        //StsException.systemDebug(this, "getGridT", "gridT: " + gridT);
        return gridT;
    }

    public void setGridT(float gridT)
    {
        this.gridT = gridT;
    }

    public void setZMax(float zMax)
    {
        super.setZMax(zMax);
        setGridZLocation();
    }

    public void setZMin(float zMin)
    {
        super.setZMin(zMin);
        setGridZLocation();
    }

    public void setTMax(float tMax)
    {
        super.setTMax(tMax);
        setGridTLocation();
    }

    public void setTMin(float tMin)
    {
        super.setTMin(tMin);
        setGridTLocation();
    }
}
