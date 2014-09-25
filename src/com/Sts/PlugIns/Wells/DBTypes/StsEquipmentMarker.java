
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

import javax.media.opengl.*;

public class StsEquipmentMarker extends StsWellMarker implements StsSelectable
{
	// Equipment Types
    static public byte PACKER = 0;
    static public byte CASING = 1;
    static public byte SENSOR = 2;
    static public byte[] subTypes = { PACKER, CASING, SENSOR };
    static public String[] equipmentTypeStrings = { "Packer", "Casing", "Sensor" };

    static public byte NONE = -1;
    
    // Packer Types
    static public byte CHEMICAL = 0;
    static public byte SWELL = 1;
    static public byte MECHANICAL = 3;
    
    // Wellbore Sensor Types
    static public byte PRESSURE = 0;
    static public byte TEMPERATURE = 1;
    
    protected byte subType = NONE;
    
    /** DB constructor */
    public StsEquipmentMarker()
    {
    }

	private StsEquipmentMarker(String name, StsWell well, StsPoint location, byte subType) throws StsException
	{
		super(name, well, StsMarker.EQUIPMENT, location);
		setSubType(subType);
    }

    private StsEquipmentMarker(String name, StsWell well, StsPoint location, String subTypeString) throws StsException
    {
		this(name, well, location, stringToType(subTypeString));
	}

    static public StsEquipmentMarker constructor(String name, StsWell well, StsPoint location, String subTypeString)
    {
        try
        {
            return new StsEquipmentMarker(name, well, location, subTypeString);
        }
        catch(Exception e)
        {
            StsMessageFiles.errorMessage(e.getMessage());
            return null;
        }
    }

    static public StsEquipmentMarker constructor(String name, StsWell well, float mdepth, byte subType)
    {
		try
		{
			StsPoint location = well.getPointAtMDepth(mdepth, false);
			return new StsEquipmentMarker(name, well, location, subType);
       	}
        catch(Exception e)
        {
            StsMessageFiles.errorMessage(e.getMessage());
            return null;
        }
	}

	static public String typeToString(byte type)
	{
        if(type < 0 || type >= 4) return null;
        return equipmentTypeStrings[type];
	}
	
    /** get a string equivalent for a type */
    static public byte stringToType(String typeString)
    {
        for(int n = 0; n < 3; n++)
            if(typeString.equalsIgnoreCase(equipmentTypeStrings[n])) return (byte)n;
        return (byte)-1;
    }
    
    public void setSubType(byte subType)
    {
    	if(subType >= subTypes.length || subType < 0)
    		this.subType = NONE;
    	this.subType = subType;
	    if(subType == NONE)
        	setStsColor(StsColor.GREY);
        else if(subType == SENSOR)
        	setStsColor(StsColor.DARK_YELLOW);
        else if(subType == PACKER)
        	setStsColor(StsColor.DARK_RED);
        else
        	setStsColor(StsColor.DARK_GREEN);    	
    }

    public byte getSubType() { return subType; }
	
    public boolean delete()
    {
        well.getMarkers().delete(this);
        marker.getWellMarkers().delete(this);
        return super.delete();
    }

   public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean isDrawingCurtain)
   {
          display(glPanel3d, displayName, isDrawingCurtain, false);
   }

   public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean isDrawingCurtain, boolean pick)
   {
          display(glPanel3d, displayName, isDrawingCurtain, getStsColor(), 1.0, pick);
   }
   
   public void display(StsGLPanel3d glPanel3d, boolean displayName, boolean isDrawingCurtain, StsColor color, double viewshift, boolean pick)
    {
		if(!checkComputeMarkerLocation()) return;
		if(isDrawingCurtain) viewshift += 2.0;
		drawMarker(glPanel3d, displayName, color, viewshift);
	}

	private void drawMarker(StsGLPanel3d glPanel3d, boolean displayName, StsColor color, double viewshift)
	{
	    float[] xyz = location.getXYZorT();
	    
        GL gl = glPanel3d.getGL();

        float equipmentSize = well.getWellClass().getEquipmentScale();
        if(subType == SENSOR)
        	StsGLDraw.drawSphere(glPanel3d, xyz, color, equipmentSize);
        else if(subType == PACKER)
        	StsGLDraw.drawCube(glPanel3d, xyz, color, equipmentSize);
        else
        	StsGLDraw.drawCube(glPanel3d, xyz, color, equipmentSize);

        if(!displayName) return;
        
        color.setGLColor(gl);
        glPanel3d.setViewShift(gl, viewshift + 40.0);
        gl.glDisable(GL.GL_LIGHTING);
        StsGLDraw.fontHelvetica12(gl, xyz, marker.getName());
        gl.glEnable(GL.GL_LIGHTING);
        glPanel3d.resetViewShift(gl);
    }

     /**
     * display equipment markers in 3d
     * @param gl - Graphics handle
     * @param glPanel - Graphics panel
     */
    public void pick(GL gl, StsGLPanel glPanel)
    {
        StsGLPanel3d glPanel3d = (StsGLPanel3d)glPanel;
        float[] xyz = location.getXYZorT();

        float equipmentSize = well.getWellClass().getEquipmentScale();
        if(subType == SENSOR)
        	StsGLDraw.drawSphere(glPanel3d, xyz, getStsColor(), equipmentSize);
        else if(subType == PACKER)
        	StsGLDraw.drawCube(glPanel3d, xyz, getStsColor(), equipmentSize);
        else
        	StsGLDraw.drawCube(glPanel3d, xyz, getStsColor(), equipmentSize);
    }
    /**
     * Convert the coordinates and attributes to a string
     * @return - string containing the point information
     */
    public String toString()
    {
		float[] xyz;
		if(location != null)
		{
			xyz = location.getXYZ();
			double[] xy = currentModel.getProject().getAbsoluteXYCoordinates(xyz);
        	return "Equipment marker (" + this.getName() + "): X = " + xy[0] + " Y = " + xy[1] + " Z = " + xyz[2] + " Measured Depth = " + mdepth;
    	}
		else
			return "Equipment marker (" + this.getName() + ")";
	}
}












