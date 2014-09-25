package com.Sts.Framework.Actions;

import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

public class StsCursor3dReadout extends StsAction
{
    StsView currentView = null;

    public StsCursor3dReadout(StsActionManager actionManager)
    {
        super(actionManager, true);
    }

	public boolean start()
    {
        return true;
    }

   	public boolean performMouseAction(StsMouse mouse, StsGLPanel glPanel)
	{
        StsGLPanel3d glPanel3d =  (StsGLPanel3d)glPanel;
        StsView view = glPanel3d.getView();

        if(currentView != view) currentView = view;
        if(currentView instanceof StsView3d || currentView instanceof StsViewCursor)
            glPanel3d.window.getCursor3d().setDisplay3dCursor(true);

 //       if(!mouse.isButtonStateReleased(StsMouse.LEFT)) return true;
        currentView.logReadout(glPanel3d, mouse);

        return true;
    }

  	public boolean end()
    {
        if(currentView == null) return false;
        currentView.cursorButtonState = StsMouse.CLEARED;
        statusArea.textOnly();
    	logMessage(" ");
        return true;
    }
}
