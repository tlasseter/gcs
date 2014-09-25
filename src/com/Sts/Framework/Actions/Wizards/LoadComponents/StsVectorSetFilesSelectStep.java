
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Actions.Wizards.LoadComponents;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.*;
import com.Sts.Framework.UI.DataTransfer.*;

public class StsVectorSetFilesSelectStep extends StsWizardStep
{
	public StsAbstractFilesSelectPanel panel;
	public StsHeaderPanel header;

    public StsVectorSetFilesSelectStep(StsLoadWizard wizard, StsAbstractFilesSelectPanel panel)
    {
        super(wizard);
		this.panel = panel;
        this.header = new StsHeaderPanel();
        setPanels(panel, header);
    }

    public boolean start()
    {
        return true;
    }

    public boolean end()
    {
        return true;
    }
}

