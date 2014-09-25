package com.Sts.PlugIns.Wells.DBTypes;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Jul 14, 2007
 * Time: 1:46:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsLogCurveTypeClass extends StsObjectPanelClass
{
    int nextColorIndex = 5;


    static private String[] curveNames = { "ILD", "RHOB", "DT", "NPHI", "GR", "SP", "Seismic", "Synthetic", "T/D", "Interval Vel", "Average Vel", "RMS Vel" };
    static StsColor[] logCurveTypeColors = StsColor.colors16;

    static public final long serialVersionUID = 1L;

    public StsLogCurveTypeClass()
    {
    }

    public int getNextColorIndex(String name)
    {
        for(int n = 0; n < curveNames.length; n++)
        {
            if(name.equals(curveNames[n]))
                return n;
        }
        nextColorIndex++;
        if(nextColorIndex == 16)
            nextColorIndex = 6;
        return nextColorIndex;
    }
}
