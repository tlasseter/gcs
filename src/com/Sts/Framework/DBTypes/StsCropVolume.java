package com.Sts.Framework.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;

public class StsCropVolume extends StsRotatedGridBoundingSubBox implements StsTreeObjectI, Cloneable
{
    public boolean applyCrop = false;
    transient boolean valueChanged = false;

    /** List of display beans for StsProject */
    static public StsBooleanFieldBean applyCropBean;
    static public StsFieldBean[] displayFields = null;

    static public StsFloatFieldBean cropXMinBean;
    static public StsFloatFieldBean cropXMaxBean;
    static public StsFloatFieldBean cropYMinBean;
    static public StsFloatFieldBean cropYMaxBean;
    static public StsFloatFieldBean cropZMinBean;
    static public StsFloatFieldBean cropZMaxBean;
    static public StsFloatFieldBean cropRowNumMinBean;
    static public StsFloatFieldBean cropRowNumMaxBean;
    static public StsFloatFieldBean cropColNumMinBean;
    static public StsFloatFieldBean cropColNumMaxBean;
    static public StsFieldBean[] propertyFields;

    static StsObjectPanel objectPanel = null;

    static final long serialVersionUID = Main.getTime(103, 7, 3, 0, 0, 0);

	public StsCropVolume()
	{
	}

	public StsCropVolume(boolean persistent)
	{
		super(persistent);
        setMinMaxRanges();
    }

	public StsCropVolume(boolean persistent, String name)
    {
        super(persistent, name);
    }

	public StsCropVolume(StsRotatedGridBoundingBox rotatedBoundingBox)
	{
		super(rotatedBoundingBox);
	}

    public boolean initialize(StsModel model)
    {
        objectPanel = null;
        // setInitialToProject();
        StsFieldBean[] fieldBeans = getPropertyFields();
        StsFieldBean.setBeanObject(fieldBeans, this);
        return true;
    }

	public void reInitialize(StsRotatedGridBoundingBox rotatedBoundingBox)
    {
		super.initialize(rotatedBoundingBox);
		setMinMaxRanges();
	}

    public void initializeBeans()
    {
        setMinMaxRanges();
    }
/*
    public void setInitialToProject()
    {
        if(!StsToolkit.copySubToSuperclass(getProjectBoundingBox(), initialBoundingBox, StsBoundingBox.class)) return;
//        setCropToInitial();
    }
*/
    private StsRotatedGridBoundingBox getProjectBoundingBox()
    {
        if(currentModel == null || currentModel.getProject() == null) return null;
        return currentModel.getProject().getRotatedBoundingBox();
    }
/*
    private void setCropToInitial()
    {
        StsToolkit.copySubToSuperclass(initialBoundingBox, this, StsBoundingBox.class);
    }
*/
    public void setMinMaxRanges()
    {
		if(cropXMinBean == null) return;
        if(xMin != largeFloat) cropXMinBean.setValueAndRange(xMin, xMin, xMax);
        if(xMax != -largeFloat) cropXMaxBean.setValueAndRange(xMax, xMin, xMax);

        if(yMin != largeFloat) cropYMinBean.setValueAndRange(yMin, yMin, yMax);
        if(yMax != -largeFloat) cropYMaxBean.setValueAndRange(yMax, yMin, yMax);

		if(isDepth && initializedZ())
		{
			cropZMinBean.setValueAndRange(getZMin(), getZMin(), zMax);
			cropZMaxBean.setValueAndRange(zMax, getZMin(), zMax);
		}
		else if(!isDepth && initializedT())
		{
			cropZMinBean.setValueAndRange(tMin, tMin, tMax);
			cropZMaxBean.setValueAndRange(tMax, tMin, tMax);
		}
		if(rowNumMin != nullValue && rowNumMax != nullValue)
		{
			if (rowNumInc > 0.0f)
			{
				cropRowNumMinBean.setValueAndRange(rowNumMin, rowNumMin, rowNumMax);
				cropRowNumMaxBean.setValueAndRange(rowNumMax, rowNumMin, rowNumMax);
			}
			else
			{
				cropRowNumMinBean.setValueAndRange(rowNumMin, rowNumMax, rowNumMin);
				cropRowNumMaxBean.setValueAndRange(rowNumMax, rowNumMax, rowNumMin);
			}
		}
		if(colNumMin != nullValue && colNumMax != nullValue)
		{
			if (colNumInc > 0.0f)
			{
				cropColNumMinBean.setValueAndRange(colNumMin, colNumMin, colNumMax);
				cropColNumMaxBean.setValueAndRange(colNumMax, colNumMin, colNumMax);
			}
			else
			{
				cropColNumMinBean.setValueAndRange(colNumMin, colNumMax, colNumMin);
				cropColNumMaxBean.setValueAndRange(colNumMax, colNumMax, colNumMin);
			}
		}
    }

    public float getCropXMin() { return xMin; }
    public float getCropXMax() { return xMax; }
    public float getCropYMin() { return yMin; }
    public float getCropYMax() { return yMax; }
    public float getCropZMin() { return getZTCoor(sliceMin); }
    public float getCropZMax() { return getZTCoor(sliceMax); }

    public int getCropRowMin() { return getNearestBoundedRowCoor(yMin); }
    public int getCropRowMax() { return getNearestBoundedRowCoor(yMax); }
    public int getCropColMin() { return getNearestBoundedColCoor(xMin); }
    public int getCropColMax() { return getNearestBoundedColCoor(xMax); }
    public int getCropSliceMin() { return sliceMin; }
    public int getCropSliceMax() { return sliceMax; }

    public int getNCropRows() { return getCropRowMax() - getCropRowMin() + 1; }
    public int getNCropCols() { return getCropColMax() - getCropColMin() + 1; }
    public int getNCropSlices() { return getCropSliceMax() - getCropSliceMin() + 1; }

    public float getCropRowNumMin() { return (float)rowNumMin; }
    public float getCropRowNumMax() { return (float)rowNumMax; }
    public float getCropColNumMin() { return (float)colNumMin; }
    public float getCropColNumMax() { return (float)colNumMax; }

    public void setCropRowNumMin(float cropRowNumMin)
    {
		int rowMin = this.getRowFromRowNum(cropRowNumMin);
		if(this.rowMin == rowMin) return;
		this.rowMin = rowMin;
        float yMin = getYCoor(rowMin);
        cropYMinBean.setValue(yMin);
        valueChanged();
    }

    public void setCropRowNumMax(float cropRowNumMax)
    {
        int rowMax = this.getRowFromRowNum(cropRowNumMax);
		if(this.rowMax == rowMax) return;
		this.rowMax = rowMax;
        float yMax = getYCoor(rowMax);
        cropYMaxBean.setValue(yMax);
        valueChanged();
    }

    public void setCropColNumMin(float cropColNumMin)
    {
        int colMin = this.getColFromColNum(cropColNumMin);
		if(this.colMin == colMin) return;
		this.colMin = colMin;
        float xMin = getXCoor(colMin);
        cropXMinBean.setValue(xMin);
        valueChanged();
    }

    public void setCropColNumMax(float cropColNumMax)
    {
        int colMax = this.getColFromColNum(cropColNumMax);
		if(this.colMax == colMax) return;
		this.colMax = colMax;
        float xMax = getXCoor(colMax);
        cropXMaxBean.setValue(xMax);
        valueChanged();
    }

    public void setCropXMin(float cropXMin)
    {
		int colMin = this.getNearestColCoor(cropXMin);
		if(this.colMin == colMin) return;
		this.colMin = colMin;
		float xMin = this.getXCoor(colMin);
        cropXMinBean.setValue(xMin);
        valueChanged();
    }

    public void setCropXMax(float cropXMax)
    {
        int colMax = this.getNearestColCoor(cropXMax);
		if(this.colMax == colMax) return;
		this.colMax = colMax;
		float xMax= this.getXCoor(colMax);
        cropXMaxBean.setValue(colNumMax);
        valueChanged();
    }

    public void setCropYMin(float cropYMin)
    {
        int rowMin = this.getNearestColCoor(cropYMin);
		if(this.rowMin == rowMin) return;
		this.rowMin = rowMin;
		float yMin = this.getYCoor(rowMin);
        cropYMinBean.setValue(yMin);
        valueChanged();
    }

    public void setCropYMax(float cropYMax)
    {
        int rowMax = this.getNearestColCoor(cropYMax);
		if(this.rowMax == rowMax) return;
		this.rowMax = rowMax;
		float yMax = this.getYCoor(rowMax);
        cropYMinBean.setValue(yMin);
        valueChanged();
    }

    public void setCropZMin(float cropZMin)
    {
		int sliceMin = getNearestSliceCoor(isDepth, cropZMin);
		if(this.sliceMin == sliceMin) return;
		this.sliceMin = sliceMin;
		cropZMinBean.setValue(cropZMin);
        valueChanged();
    }

    public void setCropZMax(float cropZMax)
    {
		int sliceMax = getNearestSliceCoor(isDepth, cropZMax);
		if(this.sliceMax == sliceMax) return;
		this.sliceMax = sliceMax;
		cropZMaxBean.setValue(cropZMax);
        valueChanged();
    }

    public int getCursorRowMin(int dir)
    {
        if     (dir == XDIR) return rowMin;
        else if(dir == YDIR) return colMin;
        else if(dir == ZDIR) return rowMin;
        else                 return 0;
    }

    public int getCursorColMin(int dir)
    {
        if     (dir == XDIR) return sliceMin;
        else if(dir == YDIR) return sliceMin;
        else if(dir == ZDIR) return colMin;
        else                 return 0;
    }

    public int getCursorRowMax(int dir)
    {
        if     (dir == XDIR) return rowMax;
        else if(dir == YDIR) return colMax;
        else if(dir == ZDIR) return rowMax;
        else                 return 0;
    }

    public int getCursorColMax(int dir)
    {
        if     (dir == XDIR) return sliceMax;
        else if(dir == YDIR) return sliceMax;
        else if(dir == ZDIR) return rowMax;
        else                 return 0;
    }

    public StsFieldBean[] getDisplayFields()
    {
        if(displayFields == null)
        {
            applyCropBean = new StsBooleanFieldBean(StsCropVolume.class, "applyCrop", "Crop Volumes:");
            displayFields = new StsFieldBean[]
            {
                applyCropBean
            };
        }
        return displayFields;
    }

    public StsFieldBean[] getPropertyFields()
    {
        if(propertyFields == null)
        {
            cropXMinBean = new StsFloatFieldBean(this, "cropXMin", "Cropped Min X");
            cropXMaxBean = new StsFloatFieldBean(this, "cropXMax", "Cropped Max X");
            cropYMinBean = new StsFloatFieldBean(this, "cropYMin", "Cropped Min Y");
            cropYMaxBean = new StsFloatFieldBean(this, "cropYMax", "Cropped Max Y");
            cropZMinBean = new StsFloatFieldBean(this, "cropZMin", "Cropped Min Z or T");
            cropZMaxBean = new StsFloatFieldBean(this, "cropZMax", "Cropped Max Z or T");
            cropRowNumMinBean = new StsFloatFieldBean(this, "cropRowNumMin", "Min Cropped Row/Line");
            cropRowNumMaxBean = new StsFloatFieldBean(this, "cropRowNumMax", "Max Cropped Row/Line");
            cropColNumMinBean = new StsFloatFieldBean(this, "cropColNumMin", "Min Cropped Col/XLine");
            cropColNumMaxBean = new StsFloatFieldBean(this, "cropColNumMax", "Max Cropped Col/XLine");
            propertyFields = new StsFieldBean[] { cropXMinBean, cropXMaxBean, cropYMinBean, cropYMaxBean,
                                                  cropZMinBean, cropZMaxBean, cropRowNumMinBean, cropRowNumMaxBean,
                                                  cropColNumMinBean, cropColNumMaxBean };
            setMinMaxRanges();
        }
        return propertyFields;
    }

    public Object[] getChildren() { return new Object[0]; }
    public StsObjectPanel getObjectPanel()
    {
        if(objectPanel == null) objectPanel = StsObjectPanel.constructor(this, true);
        return objectPanel;
    }
    public void treeObjectSelected() { }

    public boolean anyDependencies()
    {
        return true;
    }

    public boolean isDirCoordinateCropped(int dir, float coor)
    {
        switch (dir)
        {
            case StsCursor3d.XDIR:
                return coor < xMin || coor > xMax;
            case StsCursor3d.YDIR:
                return coor < yMin || coor > yMax;
            case StsCursor3d.ZDIR:
                return coor < getZMin() || coor > zMax;
            default:
                return false;
        }
    }

    public void valueChanged()
    {
        valueChanged = true;
        checkApplyCrop();
        currentModel.cropChanged();
    }

    private void checkApplyCrop()
    {
       if(applyCrop) return;
       applyCropBean.setValue(true);
       setApplyCrop(true);
    }

    public boolean isCropped()
    {
        if(!applyCrop) return false;
        if(xMin > xMin) return true;
        if(xMax < xMax) return true;
        if(yMin > yMin) return true;
        if(yMax < yMax) return true;
        if(getZMin() > getZMin()) return true;
        if(zMax < zMax) return true;
        return false;
    }

	public boolean isZCropped()
	{
		if(!applyCrop) return false;
		if(getZMin() > getZMin()) return true;
		if(zMax < zMax) return true;
		return false;
	}

    public boolean getValueChanged() { return valueChanged; }


    /** @param crop indicates whether we want cropping isVisible */
    public void setApplyCrop(boolean crop)
    {
        if(crop == applyCrop) return;
        applyCrop = crop;
        dbFieldChanged("applyCrop", applyCrop);
        currentModel.cropChanged();
        currentModel.win3dDisplayAll();
    }

    public boolean getApplyCrop() { return applyCrop; }
}
