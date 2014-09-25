package com.Sts.PlugIns.Seismic.UI;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

/**
 * <p>Title: S2S development</p>
 *
 * <p>Description: Integrated seismic to simulation software</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: S2S Systems LLC</p>
 *
 * @author not attributable
 * @version c51c
 */
public abstract class StsSeismicPanelProperties extends StsPanelProperties implements StsSerializable
{
    public int wiggleOverlapPercent;


    public StsSeismicPanelProperties()
	{
	}

	public StsSeismicPanelProperties(String panelTitle, String fieldName)
	{
        super(panelTitle, fieldName);
	}

	public StsSeismicPanelProperties(Object parentObject, Object defaultProperties, String panelTitle, String fieldName)
	{
        super(parentObject, panelTitle, fieldName);
        initializeDefaultProperties(defaultProperties);
	}

    public void initializeDefaultProperties(Object defaultProperties)
    {
        if(defaultProperties != null)
            StsToolkit.copyObjectNonTransientFields(defaultProperties, this);
    }

    public int getWiggleOverlapPercent() { return wiggleOverlapPercent; }
    
    public void setWiggleOverlapPercent(int percent) 
    { 
        wiggleOverlapPercent = percent; 
    }

}
