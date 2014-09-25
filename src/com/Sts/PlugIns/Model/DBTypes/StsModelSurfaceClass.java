package com.Sts.PlugIns.Model.DBTypes;

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
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Surfaces.DBTypes.*;

public class StsModelSurfaceClass extends StsSurfaceClass implements StsSerializable, StsClassCursorDisplayable
{
	protected boolean displayHorizons = true;
	protected boolean displayHorizonEdges = true;

    public StsModelSurfaceClass()
    {
    }

    public void initializeDisplayFields()
    {
 //       initColors(StsModelSurface.modelDisplayFields);

        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "displayHorizons", "Display horizons"),
            new StsBooleanFieldBean(this, "displayHorizonEdges", "Display edges"),
            new StsBooleanFieldBean(this, "lighting", "Lighting"),
			new StsBooleanFieldBean(this, "nullsFilled", "Nulls Filled"),
            new StsComboBoxFieldBean(this, "nullColorName", "Null Color", nullColorNames )        };
    }

    public boolean getDisplayHorizons() { return displayHorizons; }

    public void setDisplayHorizons(boolean b)
    {
        if (this.displayHorizons == b) return;
        displayHorizons = b;

        int nSurfaces = getSize();
        for (int n = 0; n < nSurfaces; n++)
        {
            StsSurface surface = (StsModelSurface) getElement(n);
            surface.setIsVisible(displayHorizons);
        }
        currentModel.win3dDisplayAll();
    }

    public boolean getDisplayHorizonEdges() { return displayHorizonEdges; }

    public void setDisplayHorizonEdges(boolean b)
    {
        if (this.displayHorizonEdges == b) return;
        displayHorizonEdges = b;
        currentModel.win3dDisplayAll();
    }

	public StsModelSurface[] getModelSurfaces()
    {
        Object surfaceList;

        StsModelSurface[] surfaces = new StsModelSurface[0];
        surfaceList = currentModel.getCastObjectList(StsModelSurface.class);
        surfaces = (StsModelSurface[]) StsMath.arrayAddArray(surfaces, surfaceList);
        return surfaces;
    }

    public StsModelSurface[] getTopDownModelSurfaces()
    {
        StsModelSurface surface, lastSurface;

        StsClass zones = currentModel.getCreateStsClass(StsZone.class);
        int nZones = zones.getSize();
        if (nZones == 0) return new StsModelSurface[0];

        StsModelSurface[] surfaces = new StsModelSurface[2 * nZones];

        surface = null;
        int nSurfaces = 0;
        for (int n = 0; n < nZones; n++)
        {
            StsZone zone = (StsZone)zones.getElement(n);
            lastSurface = surface;
            surface = zone.getTopModelSurface();
            if(surface != lastSurface) surfaces[nSurfaces++] = surface;
            surface = zone.getBaseModelSurface();
            surfaces[nSurfaces++] = surface;
        }

        if (nSurfaces < 2 * nZones)
        {
            StsModelSurface[] trimmedSurfaces = new StsModelSurface[nSurfaces];
            System.arraycopy(surfaces, 0, trimmedSurfaces, 0, nSurfaces);
            return trimmedSurfaces;
        }
        else
            return surfaces;
    }

	/** Given an original surface, find a model surface derived from it.
	 *  return null if none.
	 */
	public StsModelSurface getModelSurfaceForOriginal(StsSurface surface)
	{
		int nSurfaces = getSize();
		for(int n = 0; n < nSurfaces; n++)
		{
			StsModelSurface modelSurface = (StsModelSurface)getElement(n);
			if(modelSurface.getOriginalSurface() == surface) return modelSurface;
		}
		return null;
	}

    public boolean textureChanged(StsObject object)
    {
        return ((StsModelSurface)object).textureChanged();
    }
}
