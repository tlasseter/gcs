package com.Sts.PlugIns.Wells.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.Beans.*;

import java.util.*;

public class StsLineClass extends StsObjectPanelClass implements StsUnrotatedClass
{
	protected boolean displayNames = false;
    protected boolean displayLines = true;

    public StsLineClass()
    {
    }

    public void initializeDisplayFields()
    {
        // StsLine.initColors();

        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "displayNames", "Names"),
            new StsBooleanFieldBean(this, "displayLines", "Enable")
        };
    }

    public boolean getDisplayNames() { return displayNames; }
    public boolean getDisplayLines() { return displayLines; }

    public void setDisplayNames(boolean display)
    {
        displayNames = display;
        currentModel.win3dDisplayAll();
    }

    public void setDisplayLines(boolean display)
    {
        displayLines = display;
        currentModel.win3dDisplayAll();
    }

	public void displayClass2d(StsGLPanel3d glPanel3d, int dirNo, float dirCoordinate, boolean axesFlipped, boolean xAxisReversed, boolean yAxisReversed)
    {
        Iterator iter = getVisibleObjectIterator();
        while(iter.hasNext())
        {
            StsLine line = (StsLine)iter.next();
            if(displayLines)
                line.display2d(glPanel3d, displayNames, dirNo, dirCoordinate, axesFlipped, xAxisReversed, yAxisReversed);
        }
    }

    public void projectRotationAngleChanged()
    {
        forEach("projectRotationAngleChanged");
    }

	/** classInitialize after db is loaded */
    public boolean postDbLoadInitialize()
    {
		computeRelativePoints();
		return true;
    }

	protected void computeRelativePoints()
	{
		for(StsObject object : getElements())
			((StsLine)object).checkComputeRelativePoints();
	}

    /** This is the first step in the wells-and-sections initialization process:
     *  classInitialize wells which are not on sections */
/*
     public void initLinesNotOnSections()
    {
        int nLines = getSize();

        for(int n = 0; n < nLines; n++)
        {
            StsLine line = (StsLine)getElement(n);
            if (line != null && line.getOnSection() == null)
                line.initialize();
        }
    }
*/
    /** This is the third step in the wells-and-sections initialization process after
     *  initializing sections not on other sections: classInitialize wells on sections
     *  whether section is classInitialize or not.  We iterate until both wells and sections
     *  are properly initialized.
     */
/*
    public boolean initLinesOnSections()
    {
        boolean allInitialized = true;

        int nLines = getSize();

        for(int n = 0; n < nLines; n++)
        {
            StsLine line = (StsLine)getElement(n);
            if (line != null && line.getOnSection() != null)
                if(!line.initialize()) allInitialized = false;
        }

        return allInitialized;
    }
*/
}
