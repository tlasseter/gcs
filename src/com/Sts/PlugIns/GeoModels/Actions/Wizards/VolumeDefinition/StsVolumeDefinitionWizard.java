package com.Sts.PlugIns.GeoModels.Actions.Wizards.VolumeDefinition;

import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.Actions.Wizards.StsWizardStep;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.MVC.StsActionManager;
import com.Sts.Framework.UI.Progress.StsProgressPanel;
import com.Sts.Framework.UI.StsMessage;
import com.Sts.Framework.Utilities.StsParameters;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

/**
 * Created by tom on 9/23/2014.
 */

public class StsVolumeDefinitionWizard extends StsWizard
{
    String volName = "geoVolumeName";
    private StsGeoModelVolume geoModelVolume = new StsGeoModelVolume(false);
    public StsVolumeDefinitionStep defineVolume = new StsVolumeDefinitionStep(this, geoModelVolume);

    public StsVolumeDefinitionWizard(StsActionManager actionManager)
    {
        super(actionManager);
        addSteps();
        dialog.setPreferredSize(800, 600);
    }

    public void addSteps()
    {
        addSteps(new StsWizardStep[]{defineVolume});
    }

    public StsVolumeDefinitionWizard(StsActionManager actionManager, int width, int height)
    {
        super(actionManager, width, height);
        addSteps();
    }

    public boolean start()
    {
        // addSteps();
        if (!super.start()) return false;
        dialog.setTitle("Construct Volume for GeoModel");
        this.enableFinish();
        return true;
    }

    public boolean end()
    {
        success = checkGrid();
        return (success == true) ? super.end() : false ;

    }

    public void next()
    {
        if (currentStep == defineVolume) end();
    }

    public void previous() {
        gotoPreviousStep();
    }
    public void setVolumeName(String name) {
        this.volName = name;
    }
    public String getVolumeName() {
        return volName;
    }
    public boolean buildVolume(StsProgressPanel ppanel) {
        return true;
    }

    public boolean checkGrid()
    {
        boolean gridOK = geoModelVolume.checkGrid();
        if(!gridOK) return false;
        return addToProjectAndModel(geoModelVolume);
    }

    public boolean addToProjectAndModel(StsGeoModelVolume geoModelVolume)
    {
        StsProject project = model.getProject();
        if(!project.addToProject(geoModelVolume, StsParameters.TD_DEPTH))
        {
            new StsMessage(null, StsMessage.WARNING, "Failed to add bounding box to project.");
            return false;
        }
        geoModelVolume.addToModel();
        project.initializeViews();
        return true;
    }
}