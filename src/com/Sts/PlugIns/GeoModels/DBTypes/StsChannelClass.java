package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.StsClassDisplayable;
import com.Sts.Framework.Interfaces.MVC.StsClassObjectSelectable;
import com.Sts.Framework.Interfaces.MVC.StsRotatedClass;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.UI.ObjectPanel.StsObjectTreePanel;
import com.Sts.Framework.UI.ObjectPanel.StsTreeNode;
import com.Sts.PlugIns.Wells.DBTypes.StsWell;

import java.util.Iterator;

public class StsChannelClass extends StsClass implements StsSerializable, StsRotatedClass, StsClassDisplayable
{
    public StsChannelClass()
    {
    }

    public void displayClass(StsGLPanel3d glPanel3d, long time)
    {
        Iterator iter = getVisibleObjectIterator();
        while(iter.hasNext())
        {
            StsChannel channel = (StsChannel)iter.next();
            channel.display(glPanel3d);
        }
    }
}
