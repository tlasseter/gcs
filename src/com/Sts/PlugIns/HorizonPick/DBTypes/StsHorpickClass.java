package com.Sts.PlugIns.HorizonPick.DBTypes;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.StsObjectPanelClass;
import com.Sts.Framework.Interfaces.MVC.StsClassCursorDisplayable;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.PlugIns.Seismic.Types.StsSeismicCurtain;
import com.Sts.PlugIns.Surfaces.DBTypes.StsSurface;

import javax.media.opengl.GL;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StsHorpickClass extends StsObjectPanelClass implements StsSerializable, StsClassCursorDisplayable
{
    public StsHorpickClass()
    {
    }

    public void initializeDisplayFields()
    {
//        initColors(StsHorpick.displayFields);

        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "isVisible", "Enable")
        };
        super.setIsVisible(false);
    }

    public void drawOnCursor2d(StsGLPanel3d glPanel3d, int dirNo, float dirCoordinate,
                               boolean axesFlipped, boolean xAxisReversed, boolean axisReversed)
    {
        if(currentObject == null) return;
        if(!isVisible) return;
		GL gl = glPanel3d.getGL();
//		glPanel3d.setViewShift(gl, StsGraphicParameters.vertexOnEdgeShift);
        ((StsHorpick)currentObject).display2d(gl, dirNo, dirCoordinate, axesFlipped);
//		glPanel3d.resetViewShift(gl);
        /*
        int nElements = this.getSize();
        GL gl = glPanel3d.getGL();
        for(int n = 0; n < nElements; n++)
        {
            StsHorpick horpick = (StsHorpick)getElement(n);
            horpick.display2d(gl, dirNo, dirCoordinate, axesFlipped);
        }
        */
    }

    public void drawOnCursor3d(StsGLPanel3d glPanel3d, int dirNo, float dirCoordinate, StsPoint[] planePoints, boolean isDragging)
    {
    }

    public void drawOn3dCurtain(StsGLPanel3d glPanel3d, StsGridPoint[] gridCrossingPoints)
    {
    }

	public StsHorpick getHorpickWithSurface(StsSurface surface)
    {
        for(int n = 0; n < getSize(); n++)
        {
            StsHorpick horpick = (StsHorpick) getElement(n);
            if (horpick.getSurface() == surface)return horpick;
        }
        return null;
    }



    public void setIsVisible(boolean isVisible)
    {
        super.setIsVisible(isVisible);
        currentModel.win3dDisplay();
    }
}
