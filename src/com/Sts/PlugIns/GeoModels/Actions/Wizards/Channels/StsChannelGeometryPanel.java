package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;

import com.Sts.Framework.Actions.WizardComponents.StsRandomDistribGroupBox;
import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.DBTypes.StsObjectRefList;
import com.Sts.Framework.DBTypes.StsProject;
import com.Sts.Framework.Interfaces.StsRandomDistribFace;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.UI.Beans.StsButtonFieldBean;
import com.Sts.Framework.UI.Beans.StsJPanel;
import com.Sts.Framework.Utilities.StsMath;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannel;
import com.Sts.PlugIns.GeoModels.DBTypes.StsChannelSet;
import com.Sts.PlugIns.GeoModels.DBTypes.StsGeoModelVolume;
import com.Sts.PlugIns.GeoModels.Types.StsChannelArcSegment;
import com.Sts.PlugIns.GeoModels.Types.StsChannelLineSegment;
import com.Sts.PlugIns.GeoModels.Types.StsChannelSegment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

;

/**
 * Created by tom on 9/23/2014.
 */

public class StsChannelGeometryPanel extends StsJPanel
{
    private StsWizard wizard;
    private StsChannelGeometryStep wizardStep;
    private StsGeoModelVolume geoModelVolume;

    private StsRandomDistribGroupBox channelArcRadiusDistribBox, channelArcAngleDistribBox;
    private StsRandomDistribGroupBox channelLineLengthDistribBox;
    private StsButtonFieldBean buildButton;

    static float channelArcRadiusWidthAvg = 10;
    static float channelArcRadiusWidthDev = 4;
    static float channelArcAngleAvg = 135;
    static float channelArcAngleDev = 45;
    static float channelLineLengthWidthAvg = 5;
    static float channelLineLengthWidthDev = 2;

    static byte LINE = 1;
    static byte ARC = 2;
    static byte[] typeList = new byte[] { LINE, ARC };

    private StsChannelSet channelSet;

    public StsChannelGeometryPanel(StsWizard wizard, StsChannelGeometryStep wizardStep)
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
        channelArcRadiusDistribBox = new StsRandomDistribGroupBox(channelArcRadiusWidthAvg, channelArcRadiusWidthDev, StsRandomDistribFace.TYPE_GAUSS, "Channel arc radius/width");
        channelArcAngleDistribBox = new StsRandomDistribGroupBox(channelArcAngleAvg, channelArcAngleDev, StsRandomDistribFace.TYPE_GAUSS, "Channel arc angle");
        channelLineLengthDistribBox = new StsRandomDistribGroupBox(channelLineLengthWidthAvg, channelLineLengthWidthDev, StsRandomDistribFace.TYPE_GAUSS, "Channel line length");
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
        addEndRow(channelArcRadiusDistribBox);
        addEndRow(channelArcAngleDistribBox);
        addEndRow(channelLineLengthDistribBox);
        addEndRow(buildButton);
        wizard.rebuild();
    }

    public void build()
    {
        StsChannelSet channelSet = ((StsCreateChannelsWizard)wizard).getChannelSet();
        StsObjectRefList channels = channelSet.getChannels();
        Iterator<StsChannel> iter = channels.getIterator();
        Random random = new Random();
        while(iter.hasNext())
        {
            StsChannel channel = iter.next();
            StsPoint startPoint = channel.getStartPoint();
            float channelDirection = channel.getDirection();
            float segmentDirection = channelDirection;
            boolean isArc = random.nextBoolean();
            StsPoint lastPoint = startPoint;
            StsChannelSegment segment;
            ArrayList<StsChannelSegment> segments = new ArrayList<>();
            while(insideVolume(geoModelVolume, lastPoint))
            {
                if(isArc)
                {
                    float radius = (float)channelArcRadiusDistribBox.getSample();
                    float arcAngle = (float)channelArcAngleDistribBox.getSample();
                    boolean rotateCW = StsChannelArcSegment.rotateCW(segmentDirection, channelDirection);
                    // if we need to rotate CCW back towards the channelDirection, then make our next rotation CCW, otherwise CW
                    // a clockwise rotation is negative
                    if(rotateCW) arcAngle = -arcAngle;
                    segment = new StsChannelArcSegment(segmentDirection, radius, arcAngle, lastPoint);
                    segmentDirection += arcAngle;
                    // segmentDirection = StsChannelArcSegment.addAngles(segmentDirection, arcAngle);
                }
                else
                {
                    float length = (float)channelLineLengthDistribBox.getSample();
                    segment = new StsChannelLineSegment(lastPoint, segmentDirection, length);

                }
                isArc = !isArc;
                lastPoint = segment.getLastPoint();
                segments.add(segment);
            }
            StsChannelSegment[] trimmedSegments = segments.toArray(new StsChannelSegment[0]);
            channel.fieldChanged("channelSegments", trimmedSegments);
        }
    }

    private boolean insideVolume(StsGeoModelVolume geoModelVolume, StsPoint lastPoint)
    {
        float x = lastPoint.getX();
        float y = lastPoint.getY();
        return x >= geoModelVolume.xMin && x <= geoModelVolume.xMax && y <= geoModelVolume.yMax;
    }

    public void cancel()
    {
        return;
    }
}