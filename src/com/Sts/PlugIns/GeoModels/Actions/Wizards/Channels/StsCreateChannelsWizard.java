package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;

import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.Actions.Wizards.StsWizardStep;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.MVC.StsActionManager;
import com.Sts.Framework.UI.Progress.StsProgressPanel;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.StsParameters;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelSet;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

/**
 * Created by tom on 9/23/2014.
 */

public class StsCreateChannelsWizard extends StsWizard
{
    String volName = "geoVolumeName";
    private StsGeoModelVolume geoModelVolume = new StsGeoModelVolume(false);
    public StsChannelsAxesStep channelAxes = new StsChannelsAxesStep(this);
    public StsChannelGeometryStep channelGeometry = new StsChannelGeometryStep(this);

    public StsCreateChannelsWizard(StsActionManager actionManager)
    {
        super(actionManager);
        addSteps();
        dialog.setPreferredSize(800, 600);
    }

    public void addSteps()
    {
        addSteps(new StsWizardStep[]{channelAxes, channelGeometry});
    }

    public StsCreateChannelsWizard(StsActionManager actionManager, int width, int height)
    {
        super(actionManager, width, height);
        addSteps();
    }

    public boolean start()
    {
        // addSteps();
        if (!super.start()) return false;
        dialog.setTitle("Construct Volume for GeoModel");
        disableFinish();
        disableNext();
        return true;
    }

    public boolean end()
    {
        success = true;
        return (success == true) ? super.end() : false ;
    }

    public void next()
    {
        gotoNextStep();
    }

    public void previous() {
        gotoPreviousStep();
    }

    public StsChannelSet getChannelSet()
    {
        return channelAxes.getChannelSet();
    }
}