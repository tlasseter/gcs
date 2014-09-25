package com.Sts.PlugIns.Model.DBTypes;

import com.Sts.Framework.Utilities.DataVectors.*;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Aug 11, 2010
 * Time: 3:32:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsDynamicPropertyVolume extends StsPropertyVolume
{
    String time;

    public StsDynamicPropertyVolume()
    {
    }

    public StsDynamicPropertyVolume(StsDynamicPropertyType propertyType, String outputPathname, StsBlock block, String time)
    {
        super(propertyType, block);
        this.time = time;
        setFilename(propertyType, block, outputPathname);
    }

    public StsDynamicPropertyVolume(StsDynamicPropertyType propertyType, String outputPathname, StsBlock block, byte distributionType, String time)
    {
        super(propertyType, block);
        this.time = time;
        setFilename(propertyType, block, outputPathname);
        setDistributionType(distributionType);
    }
    /** constructor when reloading a propertyVolume file; parameters will be gotten from file. */
    public StsDynamicPropertyVolume(StsPropertyType propertyType, String time)
    {
        super(propertyType);
        this.time = time;
    }

    protected void setFilename(StsPropertyType propertyType, StsBlock block, String outputPathname)
    {
        filename = outputPathname + propertyType.eclipseName + File.separator + "time." + time + ".block." + block.getIndex();
    }

    public StsFloatTransientVector computeDynamicFloatVector(float[] eclipseValues, float[] indexMap)
    {
        int nEclipseValues = eclipseValues.length;
        StsFloatTransientVector floatValues = new StsFloatTransientVector(values);
        Arrays.fill(values, nullValue);
        int nValues = values.length;
        for(int n = 0; n < nValues; n++)
        {
            if(indexMap[n] == -1.0f) continue;
            int index = (int)indexMap[n];
            if(index < nEclipseValues)
                values[n] = eclipseValues[index];
        }
        setRange();
        floatValues.setMinValue(valueMin);
        floatValues.setMaxValue(valueMax);
        values = null;
        return floatValues;
    }

    public boolean matches(StsBlock block, StsPropertyType propertyType, String time)
    {
        return super.matches(block, propertyType) && this.time == time;
    }
}
