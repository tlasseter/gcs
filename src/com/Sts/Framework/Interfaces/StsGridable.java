package com.Sts.Framework.Interfaces;

import com.Sts.Framework.Types.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface StsGridable
{
    public int getNRows();
    public int getNCols();
    public int getRowMin();
    public int getRowMax();
    public int getColMin();
    public int getColMax();
    public String getLabel();
    public StsRotatedGridBoundingSubBox getGridBoundingBox();
}
