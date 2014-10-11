package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.StsModelObjectPanelClass;
import com.Sts.Framework.DBTypes.StsObjectPanelClass;
import com.Sts.Framework.DBTypes.StsSpectrumClass;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.PlugIns.Seismic.DBTypes.StsSeismicVolume;

public class StsGeoModelVolumeClass extends StsModelObjectPanelClass implements StsSerializable, StsTreeObjectI, StsRotatedClass,
        StsClassDisplayable, StsClassObjectSelectable
{
    private boolean fillPlaneNulls = false;

    public StsGeoModelVolumeClass()
    {
    }

    public void selected(StsGeoModelVolume geoModelVolume)
    {
        super.selected(geoModelVolume);
        setCurrentObject(geoModelVolume);
    }

    public void close()
    {
        list.forEach("close");
    }

    public boolean drawLast()
    {
        return false;
    }

    public boolean getFillPlaneNulls()
    {
        return fillPlaneNulls;
    }

    public void setFillPlaneNulls(boolean fillPlaneNulls)
    {
        this.fillPlaneNulls = fillPlaneNulls;
        currentModel.win3dDisplayAll();
    }

    public String[] getSelectableButtonIconNames()
    {
        return new String[]{"geomodel", "nogeomodel"};
    }
}
