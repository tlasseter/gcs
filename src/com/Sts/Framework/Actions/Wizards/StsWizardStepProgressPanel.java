package com.Sts.Framework.Actions.Wizards;

import com.Sts.Framework.UI.Progress.*;

import javax.swing.*;
import java.awt.*;

public class StsWizardStepProgressPanel extends StsProgressPanel
{
    public StsWizard wizard;
    public StsWizardStep wizardStep;

    public StsWizardStepProgressPanel(StsWizard wizard, StsWizardStep wizardStep, int nRows, int nCols)
    {
		super(true, nRows, nCols, null);
        this.wizard = wizard;
        this.wizardStep = wizardStep;
    }

    static public void main(String[] args)
    {
        JDialog dialog = new JDialog((Frame) null, "Panel Test", true);
        StsWizardStepProgressPanel progressPanel = new StsWizardStepProgressPanel(null, null, 0, 0);
        dialog.getContentPane().add(progressPanel);
        dialog.pack();
        dialog.setVisible(true);
    }
}
