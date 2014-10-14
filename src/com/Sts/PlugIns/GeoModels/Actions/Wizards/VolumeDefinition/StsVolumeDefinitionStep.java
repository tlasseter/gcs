package com.Sts.PlugIns.GeoModels.Actions.Wizards.VolumeDefinition;


import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.StsHeaderPanel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

/**
 * Created by tom on 9/23/2014.
 */

public class StsVolumeDefinitionStep extends StsWizardStep
{
    public StsVolumeDefinitionPanel panel;
    private StsHeaderPanel header;

    public StsVolumeDefinitionStep(StsWizard wizard, StsGeoModelVolume geoModelVolume)
    {
        super(wizard);
        panel = new StsVolumeDefinitionPanel(wizard, this, geoModelVolume);
        header = new StsHeaderPanel();
        setPanels(panel, header);
        header.setTitle("Volume Definition");
        header.setSubtitle("Define Volume Bounds");
        header.setInfoText(wizardDialog,"      **** At this point, all information in table at bottom of screen should be correct *****\n" +
                "      **** with the possible exception of coordinates which can be overriden on this screen. *****\n" +
                "(1) If required, select the override check, and viable override methods and fields will be activated.\n" +
                "(2) The user can either manually input values or use the current project definition.\n" +
                "      **** The software will decide if project override is possible. If not, option is not shown.\n" +
                "(3) If user specified, supply origin, interval and angle based on supplied starting inline, crossline.\n" +
                "(4) Verify ALL values in file table are correct before proceeding to processing\n" +
                "(5) Press the Next> Button to proceed to verification and processing screen.\n");

    }
    public boolean start()
    {
        wizard.dialog.setTitle("Define Volume Bounds");
//        initialize(wizard, panel, null, header);
        panel.initialize();
        return true;
    }

    public boolean end()
    {
        return true;
    }
}