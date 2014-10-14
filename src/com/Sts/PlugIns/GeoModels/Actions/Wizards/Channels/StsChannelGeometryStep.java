package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;


import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.Actions.Wizards.StsWizardStep;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.StsHeaderPanel;

/**
 * Created by tom on 9/23/2014.
 */

public class StsChannelGeometryStep extends StsWizardStep
{
    public StsChannelGeometryPanel panel;
    private StsHeaderPanel header;

    public StsChannelGeometryStep(StsWizard wizard)
    {
        super(wizard);
        panel = new StsChannelGeometryPanel(wizard, this);
        header = new StsHeaderPanel();
        setPanels(panel, header);
        header.setTitle("Channel Axes Definition");
        header.setSubtitle("Define Geometries for Channel Bodies");
        header.setInfoText(wizardDialog,"      **** Edit distribution functions *****\n" +
                "Each channel consists of alternating circular arcs and straight sections.\n" +
                "The channel begins with a straight section. Circular arcs are generally alternating clockwise and counterclockwise,\n" +
                "but depends on which side of the primary direction it is pointing.  So if the direction is to the left, the next arc\n" +
                "will be clockwise (to the right).  If however this does not swing the path to the right, the next arc will also be clockwise.\n" +
                "The sequence of channel segments continues until it goes outside the geomodel bounding box.\n");

    }
    public boolean start()
    {
        wizard.dialog.setTitle("Define Channel Segment Distribution");
//        initialize(wizard, panel, null, header);
        panel.initialize();
        return true;
    }

    public boolean end()
    {
        return true;
    }
}