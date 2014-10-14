package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;


import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.StsHeaderPanel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelSet;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

/**
 * Created by tom on 9/23/2014.
 */

public class StsChannelsAxesStep extends StsWizardStep
{
    public StsChannelAxesPanel panel;
    private StsHeaderPanel header;

    public StsChannelsAxesStep(StsWizard wizard)
    {
        super(wizard);
        panel = new StsChannelAxesPanel(wizard, this);
        header = new StsHeaderPanel();
        setPanels(panel, header);
        header.setTitle("Channel Axes Definition");
        header.setSubtitle("Define Centerlines for Channel Bodies");
        header.setInfoText(wizardDialog,"      **** Edit distribution functions *****\n" +
                "Channels are hardwired to run generally in the +Y direction with starting points at Y=0, distributed along the x-axis.\n" +
                "Channels are organized into clusters with distributions for the cluster centers.\n" +
                "Channels within a cluster are distributed about the cluster center in the general cluster direction.\n" +
                "Channel centerlines are distributed about the cluster center by the channel offset distribution.\n" +
                "Channel directions from this start point are sampled from the channel directions distribution.\n" +
                "The first channel's depth is the max depth of the model and is decremented by the channel thickness until" +
                "it is above the min depth of the model.");

    }
    public boolean start()
    {
        wizard.dialog.setTitle("Define Channel Axes");
//        initialize(wizard, panel, null, header);
        panel.initialize();
        return true;
    }


    public StsChannelSet getChannelSet()
    {
        return panel.getChannelSet();
    }

    public boolean end()
    {
        return true;
    }
}