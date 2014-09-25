
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Wells.DBTypes;

import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.magician.fonts.*;

import javax.media.opengl.*;

public class StsPerforationMarker extends StsWellMarker implements StsSelectable
{
    // instance fields
    protected float length = 1.0f;
    protected boolean highlighted = false;
    protected int numShots = 1;

    transient boolean firstAlive = false;
    transient boolean accentPerf = false;
    transient boolean initialized = false;
	transient StsPoint topPoint;
	transient StsPoint botPoint;
	transient int topIndex;
	transient int botIndex;
	transient StsPoint[] shotPoints;

    /** DB constructor */
    public StsPerforationMarker()
    {
    }

    private StsPerforationMarker(String name, StsWell well, StsPoint location, float length, int nShots, long time)
    {
		super(name, well, StsMarker.PERFORATION, location);
	    setLength(length);
        setNumShots(nShots);
        setBornDate(time);
    }
    
    static public StsPerforationMarker constructor(String name, StsWell well, StsPoint location, float length)
    {
  		return constructor(name, well, location, length, 1, 0l);
    }

    static public StsPerforationMarker constructor(String name, StsWell well, StsPoint location, float length, int nShots, long time)
    {
		if(well == null) return null;
            return new StsPerforationMarker(name, well, location, length, nShots, time);
    }

    static public StsPerforationMarker constructor(String name, StsWell well, float mdepth, float length)
    {
		try
		{
			StsPoint location = well.getPointAtMDepth(mdepth, false);
			return new StsPerforationMarker(name, well, location, length, 1, 0L);
       	}
        catch(Exception e)
        {
            StsMessageFiles.errorMessage(e.getMessage());
            return null;
        }
	}

	private void initializeColor()
	{
		StsColor color = StsColor.colors32[colorIndex++];
        setStsColor(color);
	}
	
    public float getLength() { return length; }
    public void setLength(float len)
    {
    	length = len;
    }

    public int getNumShots() { return numShots; }
    public void setNumShots(int nshots)
    {
    	numShots = nshots;
    }

    public void setHighlighted(boolean highlight)
    {
    	highlighted = highlight;
        dbFieldChanged("highlighted", highlighted);        
        currentModel.viewObjectRepaint(well, well);
    }

    public boolean delete()
    {
        well.getMarkers().delete(this);
        marker.getWellMarkers().delete(this);
        return super.delete();
    }
    public void display2d(StsGLPanel3d glPanel3d, int dirNo, boolean displayName)
    {
            display2d(glPanel3d, dirNo, displayName, getStsColor(), 1.0);
    }     
    public void display2d(StsGLPanel3d glPanel3d, int dirNo, boolean displayName, StsColor color, double viewshift)
    {
        boolean isAlive = isAlive(currentModel.getProject().getProjectTime());
        if(!well.isVisible() || !well.getDrawPerfMarkers() || !well.getWellClass().getDisplayPerfMarkers())
            return;

        accentPerf = false;
        if(well.getWellClass().getTimeEnabled())
        {
            if(!isAlive)
            {
                firstAlive = false;
                return;
            }

            if((firstAlive == false) && isAlive)
            {
                firstAlive = true;
                accentPerf = true;
            }
        }

        float[] xyz = location.getXYZorT();
        float[] xy = new float[2];
        float[][] points = new float[2][];
        int xidx = 0, yidx = 1;
        switch(dirNo)
        {
        	case StsCursor3d.ZDIR:
        		xy[0] = xyz[0];
        		xy[1] = xyz[1];
        		xidx = 0;
        		yidx = 1;
        		break;
        	case StsCursor3d.YDIR:
        		xy[0] = xyz[0];
        		xy[1] = xyz[2];
        		xidx = 0;
        		yidx = 2;
        		break;
        	case StsCursor3d.XDIR:
        		xy[0] = xyz[1];
        		xy[1] = xyz[2];
        		xidx = 1;
        		yidx = 2;
        		break;         		
        }
        final StsColor currentColor = StsWell.getWellClass().getDefaultColor(getIndex());
    	StsColor.setGLJavaColor(glPanel3d.getGL(), currentColor.getColor());
	    float mdepth = location.getM();
	    
	    //System.out.println("Mdepth=" + mdepth + " Length=" + length);
	    points[0] = well.getPointAtMDepth(mdepth-length/2, true).getXYZorT();
	    points[1] = well.getPointAtMDepth(mdepth+length/2, true).getXYZorT();


        GL gl = glPanel3d.getGL();
        //StsPoint[] stsPts = new StsPoint[2];
        //stsPts[0] = new StsPoint(points[0]);
        //stsPts[1] = new StsPoint(points[1]);
        StsColor aColor = new StsColor(currentColor, 0.5f);
        //StsGLDraw.drawLineStrip2d(gl, aColor, stsPts, 20);
        int scale = (int)well.getWellClass().getPerfScale();
        if(highlighted)
        {
            StsPoint[] stsPoints = new StsPoint[2];
            stsPoints[0] = new StsPoint(points[0]);
            stsPoints[1] = new StsPoint(points[1]);
            StsGLDraw.drawLine2d(gl, currentColor, scale*2, stsPoints, 2);
        }
        else
        {
            StsGLDraw.drawDottedLine2d(gl, currentColor, StsColor.BLACK, scale, points[0][xidx], points[0][yidx], points[1][xidx], points[1][yidx]);
        }
        accent2DPerforation(currentColor, location.getXYZorT());

        if(!displayName) return;
        
    	StsColor.setGLJavaColor(glPanel3d.getGL(), currentColor.getColor());
        // Display the name
        gl.glDisable(GL.GL_LIGHTING);
        StsGLDraw.fontOutput(gl, xy[0]+5, xy[1]+5, marker.getName(), GLHelvetica12BitmapFont.getInstance(gl));         
        gl.glEnable(GL.GL_LIGHTING);    	 
    }

   public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean isDrawingCurtain, int width, int height, StsColor color, double viewshift)
    {
		if(!checkComputeMarkerLocation()) return;
        if (!currentModel.getProject().canDisplayZDomain(well.getZDomainSupported()))
            return;

        boolean isAlive = isAlive(currentModel.getProject().getProjectTime());
        if(!well.isVisible() || !well.getDrawPerfMarkers() || !well.getWellClass().getDisplayPerfMarkers())
            return;

        accentPerf = false;
        if(well.getWellClass().getTimeEnabled())
        {
            if(!isAlive)
            {
                firstAlive = false;
                return;
            }

            if((firstAlive == false) && isAlive)
            {
                firstAlive = true;
                accentPerf = true;
            }
        }

        if(isDrawingCurtain) viewshift += 2.0;

	    //float[] xyztm = checkGetMarkerCoordinates();
		//if(xyztm == null) return;
	    //float mdepth = location.getM();
		drawMarker(glPanel3d, displayName, width, height, color, viewshift);
	}

	protected void drawMarker(StsGLPanel3d glPanel3d, boolean displayName, int width, int height, StsColor color, double viewshift)
	{
		if(!checkSetDrawPoints()) return;
	    //System.out.println("Mdepth=" + mdepth + " Length=" + length);
        int lineWidth = (int)well.getWellClass().getPerfScale();
        GL gl = glPanel3d.getGL();
        StsColor aColor = new StsColor(color, 0.5f);
		float mdepth = location.getM();
        float[] topXyz = topPoint.getXYZorT();
		float[] botXyz = botPoint.getXYZorT();
        double[] screenPointTop = glPanel3d.getScreenCoordinates(topXyz);
        double[] screenPointBot = glPanel3d.getScreenCoordinates(botXyz);
        double pixelDistance = StsMath.distance(screenPointTop, screenPointBot, 3);

		if(pixelDistance < 2)
		{
			double stretchFactor = 2/pixelDistance;
			screenPointBot = StsMath.interpolate(screenPointTop, screenPointBot, stretchFactor);
			botXyz = glPanel3d.getWorldCoordinateFloats(screenPointBot);
			if(highlighted)
				StsGLDraw.drawLineSegment(gl, aColor, topXyz, botXyz, lineWidth);
			else
				StsGLDraw.drawDottedLineSegment(gl, aColor, topXyz, botXyz, lineWidth);
		}
		else
		{
			float[][] xyzVectors = well.getXYZorTFloatVectors();
			if(highlighted)
				StsGLDraw.drawLineStrip(gl, aColor, xyzVectors, topXyz, botXyz, topIndex, botIndex);
			else
				StsGLDraw.drawDottedLineStrip(gl, aColor, xyzVectors, topXyz, botXyz, topIndex, botIndex);
		}

		// Perforation Shots
		for(int n = 0; n < numShots; n++)
			StsGLDraw.drawSphere(glPanel3d, shotPoints[n].getXYZorT(), StsColor.GREY, 5);

        // accent3DPerforation(glPanel3d, aColor, mdepth-length/2, lineWidth);
        if(!displayName) return;

        color.setGLColor(gl);
        glPanel3d.setViewShift(gl, viewshift + 2.0);
        gl.glDisable(GL.GL_LIGHTING);
		float[] xyz = location.getXYZorT();
        StsGLDraw.fontHelvetica12(gl, xyz, marker.getName());
        gl.glEnable(GL.GL_LIGHTING);
        glPanel3d.resetViewShift(gl);
    }

	private boolean checkSetDrawPoints()
	{
		StsAbstractFloatVector.IndexF indexTopF, indexBotF;
		if(topPoint != null && botPoint != null) return true;
		float mdepthTop = mdepth - length / 2;
		float mdepthBot = mdepth + length / 2;
		indexBotF = well.getIndexAtMDepth(mdepth + length / 2, true);
		if(indexBotF == null) return false;
		indexTopF = well.getIndexAtMDepth(mdepth - length / 2, true);
		if(indexTopF == null) return false;
		botPoint = well.getPointAtIndexF(indexBotF);
		if(botPoint == null) return false;
		topPoint = well.getPointAtIndexF(indexTopF);
		if(topPoint == null) return false;
		topIndex = indexTopF.index;
		botIndex = indexBotF.index;
		shotPoints = new StsPoint[numShots];
		float mdInterval = length/numShots;
		float mdCenter = mdepthTop + mdInterval/2;
		for(int n = 0; n < numShots; n++)
			shotPoints[n] = well.getPointAtMDepth(mdCenter, false);
		return true;
	}

    public void accent3DPerforation(StsGLPanel3d glPanel3d, StsColor color, float start, int scale)
    {
        float[] point = null;
        if(accentPerf)
        {
//            if(well.getWellClass().getEnableSound())
//                StsSound.play(well.getWellClass().getDefaultPerfSound());
        }
    }
    public void accent2DPerforation(StsColor color, float[] xyz)
    {
        if(accentPerf)
        {
            ;
        }
    }

    public StsColor getStsColor()
    {
        return StsWell.getWellClass().getDefaultColor(getIndex());
    }
}













