package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;

import com.Sts.Framework.Actions.WizardComponents.StsRandomDistribGroupBox;
import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.Interfaces.StsRandomDistribFace;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelSet;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;

import java.awt.*;

/**
 * Created by tom on 9/23/2014.
 */

public class StsChannelAxesPanel extends StsJPanel
{
    private StsWizard wizard;
    private StsChannelsAxesStep wizardStep;
    private StsGeoModelVolume geoModelVolume;

    private StsRandomDistribGroupBox channelWidthDistribBox, widthThicknessRatioDistribBox;
    private StsRandomDistribGroupBox channelsPerClusterDistribBox, clusterDistribBox;
    private StsRandomDistribGroupBox lateralDistribBox, directionDistribBox;
    private StsButtonFieldBean buildButton;

    static float channelWidthAvg = 100;
    static float channelWidthDev = 30;
    static float widthThickRatioAvg = 50;
    static float widthThickRatioDev = 20;
    static int channelsPerClusterAvg = 20;
    static int channelsPerClusterDev = 5;

    static float channelZIncThicknessFraction = 0.25f;

    private StsChannelSet channelSet;

    public StsChannelAxesPanel(StsWizard wizard, StsChannelsAxesStep wizardStep)
    {
        super(true); // true adds insets
        this.wizard = wizard;
        this.wizardStep = wizardStep;
        this.geoModelVolume = (StsGeoModelVolume)wizard.getModel().getCurrentObject(StsGeoModelVolume.class);
        try
        {
            constructBeans();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void constructBeans()
    {
        channelWidthDistribBox = new StsRandomDistribGroupBox(channelWidthAvg, channelWidthDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel widths");
        widthThicknessRatioDistribBox = new StsRandomDistribGroupBox(widthThickRatioAvg, widthThickRatioDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel width/thicknesses");

        channelsPerClusterDistribBox = new StsRandomDistribGroupBox(channelsPerClusterAvg, channelsPerClusterDev, StsRandomDistribFace.TYPE_LOGNORM, "Channels per Cluster");

        float volumeXCenter = geoModelVolume.getXCenter();
        float volumeXSize = geoModelVolume.getXSize();
        clusterDistribBox = new StsRandomDistribGroupBox(volumeXCenter, volumeXSize/2, StsRandomDistribFace.TYPE_LINEAR, "Cluster centers");
        lateralDistribBox = new StsRandomDistribGroupBox(0, volumeXSize/50, StsRandomDistribFace.TYPE_GAUSS, "Channel centers");

        directionDistribBox = new StsRandomDistribGroupBox(0, 10, StsRandomDistribFace.TYPE_GAUSS, "Channel directions");

        buildButton = new StsButtonFieldBean("Build", "Execute build for this set.", this, "build");
    }

    public void initialize()
    {
        buildPanel();
    }

    private void buildPanel()
    {
        removeAll();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        addEndRow(channelWidthDistribBox);
        addEndRow(widthThicknessRatioDistribBox);

        addEndRow(channelsPerClusterDistribBox);
        addEndRow(clusterDistribBox);
        addEndRow(lateralDistribBox);
        addEndRow(directionDistribBox);
        addEndRow(buildButton);
        wizard.rebuild();
    }

    public void build()
    {
        channelSet = new StsChannelSet(geoModelVolume, true);
        geoModelVolume.addChannelSet(channelSet);
        float zMax = geoModelVolume.getZMax();
        float zMin = geoModelVolume.getZMin();
        float ySize = geoModelVolume.getYSize();
        StsChannel channel;

        float z = zMax;
        /* loop over clusters until filled */
        // don't draw 3d window until surfaces built
        wizard.model.disableDisplay();
        while(true)
        {
            double clusterCenter = clusterDistribBox.getSample();
            int nChannels = (int)Math.round(channelsPerClusterDistribBox.getSample());
            for (int n = 0; n < nChannels; n++)
            {
                double channelOffset = lateralDistribBox.getSample();
                float x0 = (float) (clusterCenter + channelOffset);
                float y0 = 0.0f;
                float direction = (float)directionDistribBox.getSample();
                float y1 = ySize;
                float x1 = (float) (x0 + y1 * StsMath.sind(direction));
                float channelWidthThicknessRatio = (float)widthThicknessRatioDistribBox.getSample();
                float channelWidth = (float)channelWidthDistribBox.getSample();
                float channelThickness = channelWidth/channelWidthThicknessRatio;

                // bottom of first channel is at bottom of model, so top is channelThickness above that
                // for each subsequent channel, the top is located a fraction of the thickness above the previous one
                if(n == 0)
                    z -= channelThickness;
                else
                    z -= channelThickness*channelZIncThicknessFraction;

                channel = new StsChannel(channelSet, channelWidth, channelThickness, new StsPoint(x0, y0, z), new StsPoint(x1, y1, z), direction);
                channelSet.addChannel(channel);

                if (z < zMin)
                {
                    addToProject(channelSet);
                    channelSet.setChannelsState(StsChannelSet.CHANNELS_AXES);
                    wizard.model.enableDisplay();
                    wizard.enableNext();
                    return;
                }
            }
        }
    }
/*
    public void buildStraightLines()
    {
        channelSet = new StsChannelSet(geoModelVolume, false);
        float zMax = geoModelVolume.getZMax();
        float zMin = geoModelVolume.getZMin();
        int nSlices = geoModelVolume.getNSlices();
        float zInc = geoModelVolume.getZInc();
        float ySize = geoModelVolume.getYSize();
        StsChannel channel;

        float z = zMax;
        // loop over clusters until filled
        while(true)
        {
            double clusterCenter = clusterDistribBox.getSample();
            int nChannels = (int)Math.round(channelsPerClusterDistribBox.getSample());
            for (int n = 0; n < nChannels; n++)
            {
                double channelOffset = lateralDistribBox.getSample();
                float x0 = (float) (clusterCenter + channelOffset);
                float y0 = 0.0f;
                float direction = (float)directionDistribBox.getSample();
                float y1 = ySize;
                float x1 = (float) (x0 + y1 * StsMath.sind(direction));
                float channelWidthThicknessRatio = (float)widthThicknessRatioDistribBox.getSample();
                float channelWidth = (float)channelWidthDistribBox.getSample();
                float channelThickness = channelWidth/channelWidthThicknessRatio;
                channel = new StsChannel(channelSet, channelWidth, channelThickness, new StsPoint(x0, y0, z), new StsPoint(x1, y1, z), direction);
                channelSet.addChannel(channel);
                z -= channelThickness;
                if (z < zMin)
                {
                    addToProject(channelSet);
                    return;
                }
            }
        }
    }
*/
    public boolean addToProject(StsChannelSet channelSet)
    {
        StsProject project = wizard.getProject();
        project.objectPanelChanged();
        project.initializeViews();
        return true;
    }

    public void cancel()
    {
        return;
    }

    public StsChannelSet getChannelSet()
    {
        return channelSet;
    }
}