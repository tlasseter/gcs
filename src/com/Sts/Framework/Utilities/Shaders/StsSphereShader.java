package com.Sts.Framework.Utilities.Shaders;

import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;

import javax.media.opengl.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Sep 14, 2009
 * Time: 10:54:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsSphereShader extends StsShader
{
    StsUniformVariableF materialColor = new StsUniformVariableF("materialColor", new float[] { 0.0f, 0.6f, 0.0f, 1.0f } );


    public StsSphereShader(GL gl) throws GLException
    {
        super(gl, "Sphere");
    }

    static public StsSphereShader getShader(StsGLPanel3d glPanel3d)
    {
        return (StsSphereShader)getEnableShader(glPanel3d, StsSphereShader.class);
    }

    public void display(StsGLPanel3d glPanel3d, float[] xyz, float[] dsr)
    {
        StsGLDraw.drawSphere(glPanel3d, xyz, StsColor.RED, 10.0f);
    }

    public void setColor(GL gl, StsColor color)
    {
        materialColor.setValues(gl, shaderProgram, color.getRGBA(), 4);
    }

    public void drawSphere(StsGLPanel3d glPanel3d, float[] xyz, StsColor color, float size)
    {
        if (xyz == null) return;
        GL gl = glPanel3d.getGL();
        if (gl == null) return;
        int displayListSphere = StsGLDraw.getDisplayListSphere(glPanel3d);
        if(displayListSphere == 0) return;
        // color.setGLColor(gl);
        try
        {
            gl.glPushMatrix();
            gl.glTranslatef(xyz[0], xyz[1], xyz[2]);
            float zscale = glPanel3d.getZScale();
            float sphereSize = StsGLDraw.sphereSize;
            float scaleFactor = size/sphereSize;
            gl.glScalef(scaleFactor, scaleFactor, scaleFactor / zscale);
            if(color != null) setColor(gl, new StsColor(color));
            gl.glCallList(displayListSphere);
        }
        finally
        {
            gl.glPopMatrix();
        }
    }
}
