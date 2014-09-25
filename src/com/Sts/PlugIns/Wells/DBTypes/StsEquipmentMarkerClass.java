package com.Sts.PlugIns.Wells.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

public class StsEquipmentMarkerClass extends StsWellMarkerClass implements StsSerializable
{
    public StsEquipmentMarkerClass()
    {
//        initColors(StsWellMarker.displayFields);
    }

    public StsEquipmentMarker[] getEquipmentMarkerSet(StsMarker marker)
    {
        if (marker == null) return null;
        int nMarkers = getSize();
        ArrayList tempSet = new ArrayList();
        for (int i = 0; i < nMarkers; i++)
        {
        	StsEquipmentMarker wellMarker = (StsEquipmentMarker)getElement(i);
            if (wellMarker.getMarker() == marker) tempSet.add(tempSet);
        }
        int nInSet = tempSet.size();
        if (nInSet == 0) return null;
        StsEquipmentMarker[] wellMarkerSet = new StsEquipmentMarker[nInSet];
        for (int i = 0; i < nInSet; i++)
            wellMarkerSet[i] = (StsEquipmentMarker) tempSet.get(i);
        return wellMarkerSet;
    }

    /** set/get the orderedMarkerNames list */
    public void setOrderedMarkerNames(String[] names)
    {
        orderedMarkerNames = names;
    }

    public String[] getOrderedMarkerNames()
    {
        return getOrderedMarkerNames(true);
    }

    public String[] getOrderedMarkerNames(boolean ascending)
    {
        try
        {
            orderedMarkerNames = StsOrderedList.getOrderedNames(this, orderedMarkerNames, ascending);
            return orderedMarkerNames;
        }
        catch (Exception e)
        {
            StsException.outputException(e, StsException.WARNING);
            return null;
        }
    }
}
