package com.Sts.PlugIns.Wells.Views;

/**
 * Title:        Workflow development
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      4D Systems LLC
 * @author
 * @version 1.0
 */

import com.Sts.Framework.Actions.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;

public class StsWellPanelReadout extends StsAction
{

    public StsWellPanelReadout()
    {
//        super(actionManager);
    }

    public boolean performMouseAction(StsMouse mouse, StsGLPanel glPanel)
    {
        glPanel.performMouseAction();
        return true;
    }
}