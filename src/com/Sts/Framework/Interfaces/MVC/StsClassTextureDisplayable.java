package com.Sts.Framework.Interfaces.MVC;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.Views.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Apr 5, 2007
 * Time: 11:50:20 AM
 * To change this template use File | Settings | File Templates.
 */
public interface StsClassTextureDisplayable
{
    public void cropChanged();
    public boolean textureChanged(StsObject object);
	public void displayClassTexture(StsGLPanel3d glPanel3d, long time);
}
