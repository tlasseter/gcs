package com.Sts.Framework.UI.Beans;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.*;

import java.awt.*;

public class StsSpectrumFieldBean extends StsFieldBean
{
    private StsSpectrum spectrum = null;
    private StsSpectrumPanel spectrumPanel = new StsSpectrumPanel();

    public StsSpectrumFieldBean()
    {
    }

    public StsSpectrumFieldBean(Class c, String fieldName)
    {
        super();
        initialize(c, fieldName);
    }

    public Component[] getBeanComponents() { return new Component[] { spectrumPanel }; }

    public Object getValueObject() { return getSpectrum(); }

    public StsSpectrum getSpectrum()
    {
        return (StsSpectrum)getPanelObject();
    }

    public void doSetValueObject(Object valueObject)
    {
        if(!(valueObject instanceof StsSpectrum)) return;
        spectrum = (StsSpectrum)valueObject;
        spectrumPanel.setSpectrum(spectrum);
    }

	public String toString() {return spectrum.getName(); }
	public Object fromString(String string)
	{
		StsModel currentModel = StsSerialize.getCurrentModel();
		if(currentModel == null) return null;
		return currentModel.getObjectWithName(StsSpectrum.class, string);
	}
}