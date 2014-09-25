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
import com.Sts.Framework.Utilities.*;
import com.magician.fonts.*;

import javax.media.opengl.*;
import java.awt.*;
import java.text.*;

public class StsFMIMarker extends StsWellMarker implements StsSelectable
{
	// instance fields
	protected float height = 1.0f;
	protected float dip = 0.0f;
	protected float azimuth = 0.0f;
	protected float[] attributes = null;
	transient DecimalFormat format = new DecimalFormat("#");

	/** DB constructor */
	public StsFMIMarker()
	{
	}

	/** constructor */
	private StsFMIMarker(StsWell well, StsPoint location, float dip, float azimuth) throws StsException
	{
		super(well, StsMarker.FMI, location);
		this.azimuth = azimuth;
		this.dip = dip;
	}


	static public StsFMIMarker constructor(StsWell well, StsPoint location, float dip, float azimuth)
	{
		try
		{
			if(!createMarkerOk(well, StsMarker.FMI)) return null;
			return new StsFMIMarker(well, location, dip, azimuth);
		}
		catch(Exception e)
		{
			StsMessageFiles.errorMessage(e.getMessage());
			return null;
		}
	}

	public float getDip() { return dip; }

	public void setDip(float dip)
	{
		this.dip = dip;
	}

	public float getAzimuth() { return azimuth; }

	public void setAzimuth(float az)
	{
		azimuth = az;
	}

	public float getHeight() { return height; }

	public void setHeight(float hgt)
	{
		height = hgt;
	}

	public boolean delete()
	{
		well.getMarkers().delete(this);
		marker.getWellMarkers().delete(this);
		return super.delete();
	}

	public void display2d(StsGLPanel3d glPanel3d, int dirNo, boolean displayName)
	{
		display2d(glPanel3d, dirNo, displayName, 6, getStsColor(), 1.0);
	}

	public void display2d(StsGLPanel3d glPanel3d, int dirNo, boolean displayName, int width,
						  StsColor color, double viewshift)
	{
		StsColor.setGLJavaColor(glPanel3d.getGL(), Color.BLUE);
		float[] xyz = location.getXYZorT();
		float[] xy = new float[2];
		switch(dirNo)
		{
			case StsCursor3d.ZDIR:
				xy[0] = xyz[0];
				xy[1] = xyz[1];
				break;
			case StsCursor3d.YDIR:
				xy[0] = xyz[0];
				xy[1] = xyz[2];
				break;
			case StsCursor3d.XDIR:
				xy[0] = xyz[1];
				xy[1] = xyz[2];
				break;
		}
		StsGLDraw.drawPoint2d(xy, glPanel3d.getGL(), width);
		if(!displayName) return;

		GL gl = glPanel3d.getGL();
		if(!displayName)
			return;

		// Display the name
		gl.glDisable(GL.GL_LIGHTING);
		StsGLDraw.fontOutput(gl, xy[0] + width, xy[1] + width, marker.getName(), GLHelvetica12BitmapFont.getInstance(gl));
		gl.glEnable(GL.GL_LIGHTING);
	}

	public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean isDrawingCurtain, int width, int height, StsColor color, double viewshift)
	{
		if(!checkComputeMarkerLocation()) return;
		color = StsColor.BLUE;
		drawMarker(location, glPanel3d, displayName, width, height, color, viewshift);
	}

	protected void drawMarker(StsPoint location, StsGLPanel3d glPanel3d, boolean displayName, int width, int height, StsColor color, double viewshift)
	{
		float[] xyz = location.getXYZorT();
		StsGLDraw.drawSphere(glPanel3d, xyz, color, 4.0f);
		StsGLDraw.drawDisk3d(glPanel3d, xyz, StsColor.BLACK, well.getWellClass().getFmiScale() + 4, azimuth, dip, true);
		glPanel3d.setViewShift(glPanel3d.getGL(), 2.0);
		StsGLDraw.drawDisk3d(glPanel3d, xyz, color, well.getWellClass().getFmiScale(), azimuth, dip, true);
		glPanel3d.resetViewShift(glPanel3d.getGL());

		GL gl = glPanel3d.getGL();
		if(!displayName) return;

		// Display the MD
		if(displayName)
		{
			color.setGLColor(gl);
			glPanel3d.setViewShift(gl, viewshift + 2.0);
			gl.glDisable(GL.GL_LIGHTING);
			StsGLDraw.fontHelvetica12(gl, xyz, getName());
			gl.glEnable(GL.GL_LIGHTING);
			glPanel3d.resetViewShift(gl);
		}
	}

}













