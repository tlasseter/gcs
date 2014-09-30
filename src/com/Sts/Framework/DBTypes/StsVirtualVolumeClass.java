package com.Sts.Framework.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;
import com.Sts.PlugIns.Seismic.Views.*;

public class StsVirtualVolumeClass extends StsSeismicVolumeClass implements StsSerializable, StsClassSurfaceDisplayable, StsClassCursor3dTextureDisplayable //, StsClassDisplayable
{
    static public StsVirtualSeismicVolume currentVirtualVolumeObject = null;
/*
    static final Class[] subClassClasses = new Class[] { StsMathVirtualVolume.class, StsCrossplotVirtualVolume.class,
        StsRGBAVirtualVolume.class, StsFilterVirtualVolume.class, StsBlendedVirtualVolume.class, StsSensorVirtualVolume.class};
	static StsVirtualVolumeClass[] subClasses = null;
*/
    public StsVirtualVolumeClass()
    {
    }

    public void initializeFields()
    {
        //initializeSubClasses();
    }
/*
    private void initializeSubClasses()
    {
        int nSubClasses = subClassClasses.length;
        subClasses = new StsVirtualVolumeClass[nSubClasses];
        int nActualInstances = 0;
        for(int n = 0; n < nSubClasses; n++)
        {
            StsVirtualVolumeClass subClassInstance = (StsVirtualVolumeClass) currentModel.getStsClass(subClassClasses[n]);
            if(subClassInstance != null) subClasses[nActualInstances++] = subClassInstance;
        }
        subClasses = (StsVirtualVolumeClass[])StsMath.trimArray(subClasses, nActualInstances);
    }
*/
    public StsVirtualSeismicVolume[] getVirtualVolumes()
    {
        Object virtualVolumeList;
        StsVirtualSeismicVolume[] virtualVolumes = new StsVirtualSeismicVolume[0];

        if(subClasses == null) return new StsVirtualSeismicVolume[0];
        for(StsClass subClass : subClasses)
        {
            virtualVolumeList = subClass.getCastObjectList();
            virtualVolumes = (StsVirtualSeismicVolume[]) StsMath.arrayAddArray(virtualVolumes, virtualVolumeList);
        }
        return virtualVolumes;
    }
    
    public StsVirtualSeismicVolume getVirtualVolumeWithName(String name)
    {
        StsVirtualSeismicVolume volume = null;
        if(subClasses == null) return volume;
        for(StsClass subClass : subClasses)
        {
            volume = (StsVirtualSeismicVolume) subClass.getObjectWithName(name);
            if(volume != null) break;
        }
        return volume;
    }
    public void setIsVisibleOnCursor(boolean isVisibleOnCursor)
    {
        if(subClasses == null) return;
        for(StsClass subClass : subClasses)
        {
            StsVirtualVolumeClass vvClass = (StsVirtualVolumeClass) subClass;
            vvClass.setIsVisibleOnCursor(isVisibleOnCursor);
        }
    }
	public boolean getContourColors() {	return contourColors; }
    public void setContourColors(boolean contour)
    {
        if(subClasses == null) return;
        for(StsClass subClass : subClasses)
        {
            StsVirtualVolumeClass vvClass = (StsVirtualVolumeClass) subClass;
            vvClass.setContourColors(contour);
        }
    }
    public void selected(StsVirtualSeismicVolume virtualVolume)
    {
        super.selected(virtualVolume);
        setCurrentObject(virtualVolume);
    }
    
 	public boolean getIsVisibleOnCursor()
	{
		return isVisibleOnCursor;
	}

    public boolean setCurrentObject(StsObject object)
    {
        currentVirtualVolumeObject = (StsVirtualSeismicVolume)object;
        return super.setCurrentObject(object);
    }

    public void toggleOn(StsWin3dBase win3d)
    {
        if(debug) System.out.println("toggleOn called.");
        win3d.getCursor3d().toggleOn(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public void toggleOff(StsWin3dBase win3d)
    {
        if(debug) System.out.println("toggleOff called.");
        win3d.getCursor3d().toggleOff(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public void toggleOn(StsWin3d win3d)
    {
        if(debug) System.out.println("toggleOn called.");
        win3d.getCursor3d().toggleOn(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public void toggleOff(StsWin3d win3d)
    {
        if(debug) System.out.println("toggleOff called.");
        win3d.getCursor3d().toggleOff(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public void toggleOn(StsWin3dFull win3d)
    {
        if(debug) System.out.println("toggleOn called.");
        win3d.getCursor3d().toggleOn(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public void toggleOff(StsWin3dFull win3d)
    {
        if(debug) System.out.println("toggleOff called.");
        win3d.getCursor3d().toggleOff(currentVirtualVolumeObject);
        win3d.repaint();
    }

    public StsCursor3dTexture constructDisplayableSection(StsModel model, StsCursor3d cursor3d, int dir)
    {
        return new StsSeismicCursorSection(model, (StsVirtualSeismicVolume)currentObject, cursor3d, dir);
    }

	public void setDisplayOnSubVolumes(boolean displayOnSubVolumes)
	{
		if(this.displayOnSubVolumes == displayOnSubVolumes) return;
        if(subClasses == null) return;
		boolean changed = false;
        for(StsClass subClass : subClasses)
        {
            StsVirtualVolumeClass vvClass = (StsVirtualVolumeClass) subClass;
            changed = changed | vvClass.setSubclassDisplayOnSubVolumes(displayOnSubVolumes);
        }
		this.displayOnSubVolumes = displayOnSubVolumes;
		if(!changed) return;
		currentModel.subVolumeChanged();
		currentModel.win3dDisplayAll();
	}

	public boolean setSubclassDisplayOnSubVolumes(boolean displayOnSubVolumes)
	{
		if(this.displayOnSubVolumes == displayOnSubVolumes) return false;
		this.displayOnSubVolumes = displayOnSubVolumes;
		return true;
	}
}
