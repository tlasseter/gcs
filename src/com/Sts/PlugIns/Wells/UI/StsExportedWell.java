package com.Sts.PlugIns.Wells.UI;

import com.Sts.Framework.Utilities.DataVectors.StsLineVectorSet;
import com.Sts.Framework.Utilities.DataVectors.StsVectorSet;
import com.Sts.Framework.Utilities.StsToolkit;
import com.Sts.PlugIns.Wells.DBTypes.StsWell;

/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 1/10/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsExportedWell extends StsWell
{
    public String name;
    public double xOrigin;
    public double yOrigin;
    public String[] columnNames;
    public boolean originInitialized = false;

    public StsExportedWell(StsWell well)
    {
        // StsToolkit.copy(well, this);
        name = well.getName();
        xOrigin = well.getXOrigin();
        yOrigin = well.getYOrigin();
        vectorSet = (StsLineVectorSet)well.getLineVectorSet().clone();
        vectorSet.setVectorSetObject(well);
        columnNames = ((StsLineVectorSet)vectorSet).getCoorVectorNames();
    }
}
