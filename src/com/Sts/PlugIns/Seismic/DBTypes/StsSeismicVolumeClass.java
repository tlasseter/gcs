package com.Sts.PlugIns.Seismic.DBTypes;

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
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;
import com.Sts.PlugIns.Seismic.UI.*;
import com.Sts.PlugIns.Seismic.Views.*;

public class StsSeismicVolumeClass extends StsSeismicClass implements StsSerializable, StsClassSurfaceDisplayable,
		StsClassCursor3dTextureDisplayable,StsClassDisplayable, StsClassObjectSelectable // , StsClassDisplayable
{
	protected boolean displayGridLines = false;
	protected boolean displayVoxels = false;
	protected boolean displayAxis = true;
	protected boolean applySurfacesSubVolume = false;
	protected boolean enableTime = true;

	protected boolean isVisibleOnCursor = true;
	protected boolean isVisibleOnSection = false;
    protected boolean isVisibleOnCurtain = true;
	protected boolean isVisibleOnSurface = false;
	protected boolean displayOnSubVolumes = true;
    /** display properties for wiggle trace displays for all StsSeismicVolume(s) */
    public StsWiggleDisplayProperties wiggleDisplayProperties = null;

    /** default display properties for wiggle trace displays for all StsSeismicVolume(s) */
    transient public StsWiggleDisplayProperties defaultWiggleDisplayProperties = null;

    public StsSeismicVolumeClass()
	{
	}

	public void initializeDisplayFields()
	{
		displayFields = new StsFieldBean[]
							 {
							 new StsBooleanFieldBean(this, "isVisibleOnCursor", "On 3D Cursors"),
							 new StsBooleanFieldBean(this, "enableTime", "Enable Time"),
							 new StsBooleanFieldBean(this, "isVisibleOnSection", "On Sections"),
                             new StsBooleanFieldBean(this, "isVisibleOnCurtain", "On Curtains"),
							 new StsBooleanFieldBean(this, "isVisibleOnSurface", "On Surfaces"),
							 new StsBooleanFieldBean(this, "displayVoxels", "Display Voxel bodies"),
							 new StsBooleanFieldBean(this, "displayOnSubVolumes", "Filter by SubVolumes"),
							 new StsBooleanFieldBean(this, "displayAxis", "Plot Axis"),
							 new StsBooleanFieldBean(this, "isPixelMode", "Pixel Display Mode"),
							 new StsBooleanFieldBean(this, "displayWiggles", "2D Wiggle Traces"),
							 new StsBooleanFieldBean(this, "contourColors", "Contoured Seismic Colors"),
							 new StsIntFieldBean(this, "wiggleToPixelRatio", 1, 100, "2D Wiggle to Pixel Ratio:"),
                             new StsBooleanFieldBean(this, "fillPlaneNulls", "Fill cursor plane nulls"),
                             new StsButtonFieldBean("Wiggle Display Properties", "Edit 2D wiggle display properties.", this, "displayWiggleProperties"),
        };
	}

	public void initializeDefaultFields()
	{
		defaultFields = new StsFieldBean[]
							 {
							 new StsComboBoxFieldBean(this, "seismicSpectrumName", "Spectrum:", StsSpectrumClass.cannedSpectrums),
		};
	}

	public void setIsVisibleOnCursor(boolean isVisibleOnCursor)
	{
		if (this.isVisibleOnCursor == isVisibleOnCursor)
			return;
		this.isVisibleOnCursor = isVisibleOnCursor;
//      setDisplayField("isVisibleOnCursor", isVisibleOnCursor);
		currentModel.win3dDisplayAll();
	}

	public boolean getIsVisibleOnCursor()
	{
		return isVisibleOnCursor;
	}

	public void toggleVisibleOnCursor()
	{
		isVisibleOnCursor = !isVisibleOnCursor;
		currentModel.win3dDisplayAll();
	}

	public void setIsVisibleOnSection(boolean isVisibleOnSection)
	{
		if (this.isVisibleOnSection == isVisibleOnSection)
			return;
		this.isVisibleOnSection = isVisibleOnSection;
//      setDisplayField("isVisibleOnSection", isVisibleOnSection);
		currentModel.win3dDisplayAll();
	}

	public boolean getIsVisibleOnSection()
	{
		return isVisibleOnSection;
	}

	public void setIsVisibleOnCurtain(boolean isVisibleOnCurtain)
	{
		if (this.isVisibleOnCurtain == isVisibleOnCurtain)
			return;
		this.isVisibleOnCurtain = isVisibleOnCurtain;
//      setDisplayField("isVisibleOnCurtain", isVisibleOnCurtain);
		currentModel.win3dDisplayAll();
	}

	public boolean getIsVisibleOnCurtain()
	{
		return isVisibleOnCurtain;
	}

	public void setIsVisibleOnSurface(boolean isVisibleOnSurface)
	{
		if (this.isVisibleOnSurface == isVisibleOnSurface)
			return;
		this.isVisibleOnSurface = isVisibleOnSurface;
//      setDisplayField("isVisibleOnSurface", isVisibleOnSurface);
		currentModel.win3dDisplayAll();
	}

	public boolean getIsVisibleOnSurface()
	{
		return isVisibleOnSurface;
	}

	public void setDisplayOnSubVolumes(boolean displayOnSubVolumes)
	{
		if (this.displayOnSubVolumes == displayOnSubVolumes) return;
		this.displayOnSubVolumes = displayOnSubVolumes;
		// currentModel.subVolumeChanged();
		currentModel.win3dDisplayAll();
	}

	public boolean getDisplayOnSubVolumes()
	{
		return displayOnSubVolumes;
	}

	public StsSeismicVolume getSeismicVolumeDisplayableOnSection()
	{
		if (currentObject == null)
			return null;
		if (!getIsVisibleOnSection() || !getIsVisible())
			return null;
		return (StsSeismicVolume)currentObject;
	}

	public StsCursor3dTexture constructDisplayableSection(StsModel model, StsCursor3d cursor3d, int dir)
	{
		return new StsSeismicCursorSection(model, (StsSeismicVolume)currentObject, cursor3d, dir);
	}

    public void readoutOnCursor(StsCursorPoint cursorPoint)
	{
	}
/*
	public boolean setCurrentObject(StsObject object)
	{
		if (currentObject == object)
			return false;

		StsSeismicVolume currentSeismicVolume = (StsSeismicVolume)currentObject;
		if (currentSeismicVolume != null)
		{
			currentSeismicVolume.deleteTransients();
			// TODO: Following isn't working correctly; need to mainDebug and fix! TJL 2006-06-03
			// If volume requires a change of zDomain when selected, it should be done and depth/time bean reset
		//	 byte volumeZDomain = currentSeismicVolume.getZDomain();
		//	 if(volumeZDomain != currentModel.project.getZDomain())
		//	  currentModel.project.toggleZDomain(volumeZDomain);
		}
		return super.setCurrentObject(object);
	}
*/
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

	public void setEnableTime(boolean enable)
	{
		if (this.enableTime == enable)
			return;
		this.enableTime = enable;
		currentModel.win3dDisplayAll();
	}

	public boolean getEnableTime()
	{
		return enableTime;
	}


/*
	  public void setDisplayGridLines(boolean displayGridLines)
	  {
		if(this.displayGridLines == displayGridLines) return;
		this.displayGridLines = displayGridLines;
		setDisplayField("displayGridLines", displayGridLines);
		currentModel.win3dDisplayAll();
	  }
	  public boolean getDisplayGridLines() { return displayGridLines; }
*/
	public void setDisplayVoxels(boolean displayVoxels)
	{
		if (this.displayVoxels == displayVoxels)
			return;
		this.displayVoxels = displayVoxels;
//      setDisplayField("displayVoxels", displayVoxels);
		currentModel.win3dDisplayAll();
	}

	public boolean getDisplayVoxels()
	{
		return displayVoxels;
	}

	public void setDisplayAxis(boolean displayAxis)
	{
		if (this.displayAxis == displayAxis)
			return;
		this.displayAxis = displayAxis;
//      setDisplayField("displayAxis", displayAxis);
		currentModel.win3dDisplayAll();
	}

	public boolean getDisplayAxis()
	{
		return displayAxis;
	}

	public void setApplySurfacesSubVolume(boolean apply)
	{
		if (this.applySurfacesSubVolume == apply)
			return;
		this.applySurfacesSubVolume = apply;
//      setDisplayField("applySurfacesSubVolume", apply);
		currentModel.win3dDisplayAll();
	}

	public boolean getApplySurfacesSubVolume()
	{
		return applySurfacesSubVolume;
	}

	public void displayClass(StsGLPanel3d glPanel3d, long time)
	{
		if (!getDisplayVoxels())
			return;

		for (int n = 0; n < getSize(); n++)
		{
			StsSeismicVolume seismicVolume = (StsSeismicVolume)getElement(n);
			if ((enableTime && seismicVolume.isAlive(time)) || (!enableTime))
				seismicVolume.display(glPanel3d);
		}
	}

	// if currentSeismicVolume has been deleted, reset currentSeismicVolume to first available
	public StsSeismicVolume getCurrentSeismicVolume()
	{
		return (StsSeismicVolume)currentObject;
	}

    public StsWiggleDisplayProperties getWiggleDisplayProperties()
    {
        if(wiggleDisplayProperties == null)
        {
            if(defaultWiggleDisplayProperties == null)
                defaultWiggleDisplayProperties = new StsWiggleDisplayProperties(this, "defaultWiggleDisplayProperties");
            wiggleDisplayProperties = new StsWiggleDisplayProperties(this, defaultWiggleDisplayProperties, "wiggleDisplayProperties");
        }
        wiggleDisplayProperties.setParentObject(this);
        return wiggleDisplayProperties;
    }

	public void displayWiggleProperties()
	{
		new StsOkApplyCancelDialog(currentModel.win3d, getWiggleDisplayProperties(), "Wiggle Display Properties", false);
	}

	public String getObjectPanelLabel() { return "3D Seismic Volumes"; }

	public String[] getSelectableButtonIconNames() { return new String[] { "seismic", "noseismic"}; }

	public Class getStsClassObjectSelectableParent()
	{
		return StsSeismicVolume.class;
	}
}
