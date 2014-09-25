package com.Sts.Framework.Interfaces.MVC;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.MVC.Views.*;

/** subClasses of StsClass which are timeDisplayable (i.e., they may or may not be displayed depending on project time),
 *  implement this interface.
 */
public interface StsClassDisplayable
{
    public void displayClass(StsGLPanel3d glPanel3d, long time);
}
