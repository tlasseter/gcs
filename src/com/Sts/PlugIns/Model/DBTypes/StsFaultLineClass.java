package com.Sts.PlugIns.Model.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

public class StsFaultLineClass extends StsLineClass implements StsSerializable, StsUnrotatedClass
{
    public StsFaultLineClass()
    {
    }

    public void initializeDisplayFields()
    {
//        initColors(StsFaultLine.getStaticDisplayFields());

        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "displayNames", "Names"),
            new StsBooleanFieldBean(this, "displayLines", "Enable")
        };
    }

	public void displayClass2d(StsGLPanel3d glPanel3d, int dirNo, float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed)
    {
        Iterator iter = getVisibleObjectIterator();
        while(iter.hasNext())
        {
            StsFaultLine faultLine = (StsFaultLine)iter.next();
            if(displayLines)
                faultLine.display2d(glPanel3d, displayNames, dirNo, dirCoordinate, axesFlipped, xAxisReversed, yAxisReversed);
        }
    }
}
