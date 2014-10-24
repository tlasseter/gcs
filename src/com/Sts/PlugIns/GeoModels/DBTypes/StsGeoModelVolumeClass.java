package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.StsModelObjectPanelClass;
import com.Sts.Framework.DBTypes.StsObjectPanelClass;
import com.Sts.Framework.DBTypes.StsSpectrumClass;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.MVC.Views.StsCursor3d;
import com.Sts.Framework.MVC.Views.StsCursor3dTexture;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.PlugIns.GeoModels.Views.StsGeoModelCursorSection;
import com.Sts.PlugIns.Seismic.DBTypes.StsSeismicVolume;
import com.Sts.PlugIns.Seismic.Views.StsSeismicCursorSection;

public class StsGeoModelVolumeClass extends StsModelObjectPanelClass implements StsSerializable, StsTreeObjectI, StsRotatedClass,
        StsClassObjectSelectable, StsClassCursor3dTextureDisplayable
{
    private boolean fillPlaneNulls = false;
    private boolean isPixelMode = true; // Blended Pixels or Nearest
    protected boolean contourColors = false; // shader

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
    public StsCursor3dTexture constructDisplayableSection(StsModel model, StsCursor3d cursor3d, int dir)
    {
        return new StsGeoModelCursorSection(model, (StsGeoModelVolume)currentObject, cursor3d, dir);
    }

    public void displayClass(StsGLPanel3d glPanel3d, long time)
    {
        for (int n = 0; n < getSize(); n++)
        {
            StsGeoModelVolume geoModelVolume = (StsGeoModelVolume)getElement(n);
            geoModelVolume.display(glPanel3d);
        }
    }

    public String[] getSelectableButtonIconNames()
    {
        return new String[]{"geomodel", "nogeomodel"};
    }

    public boolean isPixelMode()
    {
        return isPixelMode;
    }

    public void setPixelMode(boolean isPixelMode)
    {
        this.isPixelMode = isPixelMode;
    }

    public void setContourColors(boolean contourColors)
    {
        if (this.contourColors == contourColors)
            return;
        this.contourColors = contourColors;
        currentModel.win3dDisplayAll();
    }

    public boolean getContourColors()
    {
        return contourColors;
    }
}
