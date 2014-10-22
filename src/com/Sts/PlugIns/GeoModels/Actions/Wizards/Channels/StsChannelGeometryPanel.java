package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;

import com.Sts.Framework.Actions.WizardComponents.StsRandomDistribGroupBox;
import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.DBTypes.StsObjectRefList;
import com.Sts.Framework.Interfaces.StsRandomDistribFace;
import com.Sts.Framework.Types.StsPoint;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
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

    private StsRandomDistribGroupBox channelArcRadiusDivWidthDistribBox, channelArcAngleDistribBox;
    private StsRandomDistribGroupBox channelLineLengthDivWidthDistribBox;
    private StsButtonFieldBean buildButton;
    private StsButtonFieldBean buildGridButton;

    static float channelArcRadiusDivWidthAvg = 5;
    static float channelArcRadiusDivWidthDev = 2;
    static float channelArcAngleAvg = 135;
    static float channelArcAngleDev = 45;
    static float channelLineLengthDivWidthAvg = 4;
    static float channelLineLengthDivWidthDev = 2;

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
        channelArcRadiusDivWidthDistribBox = new StsRandomDistribGroupBox(channelArcRadiusDivWidthAvg, channelArcRadiusDivWidthDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel arc radius/width");
        channelArcAngleDistribBox = new StsRandomDistribGroupBox(channelArcAngleAvg, channelArcAngleDev, StsRandomDistribFace.TYPE_GAUSS, "Channel arc angle");
        channelLineLengthDivWidthDistribBox = new StsRandomDistribGroupBox(channelLineLengthDivWidthAvg, channelLineLengthDivWidthDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel line/width length");
        buildButton = new StsButtonFieldBean("Build", "Execute build for this set.", this, "build");
        buildGridButton = new StsButtonFieldBean("Build Grid", "Build the 3D grid.", this, "buildGrid");
    }

    public void initialize()
    {
        buildPanel();
    }

    private void buildPanel()
    {
        removeAll();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        addEndRow(channelArcRadiusDivWidthDistribBox);
        addEndRow(channelArcAngleDistribBox);
        addEndRow(channelLineLengthDivWidthDistribBox);
        addToRow(buildButton);
        addEndRow(buildGridButton);
        wizard.rebuild();
    }

    /** If current direction is more than maxBackwardsAngle away from channelDirection, rotation must be back towards channelDirection *
     *  otherwise, rotation is opposite the previous rotation */
    static final float maxBackwardsAngle = 30;
    static final float limitRotateAngle = 120;
    static final float limitAddLineAngle = 30;

    float nextSegmentDirection;
    float nextRotationAngle;

    public void build()
    {
        StsChannelSet channelSet = ((StsCreateChannelsWizard)wizard).getChannelSet();
        StsObjectRefList channels = channelSet.getChannels();
        Iterator<StsChannel> iter = channels.getIterator();
        boolean lastRotateCW = true;
        // don't draw views until construction complete
        wizard.model.disableDisplay();
        wizard.disableNext();
        while(iter.hasNext())
        {
            StsChannel channel = iter.next();
            StsPoint startPoint = channel.getStartPoint();
            float channelDirection = channel.getDirection();
            float segmentDirection = channelDirection;
            boolean isArc = false; // first segment will be line
            StsPoint lastPoint = startPoint;
            float channelWidth = channel.getChannelWidth();
            StsChannelSegment segment;
            ArrayList<StsChannelSegment> segments = new ArrayList<>();
            while(insideVolume(geoModelVolume, lastPoint))
            {
                if(isArc)
                {
                    float radius = (float)channelArcRadiusDivWidthDistribBox.getSample()*channelWidth;
                    float arcAngle = (float)channelArcAngleDistribBox.getSample();
                    // rotation angle is angle from current segmentDirection back to channel direction
                    double rotationAngle = StsChannelArcSegment.subtractAngles(segmentDirection, channelDirection);
                    // if segmentDirection is more than 30 degrees away from channel direction, rotate the opposite way;
                    // otherwise rotate the opposite of the previous rotation
                    boolean rotateCW;
                    if(rotationAngle > maxBackwardsAngle)
                        rotateCW = true;
                    else if(rotationAngle < -maxBackwardsAngle)
                        rotateCW = false;
                    else
                        rotateCW = !lastRotateCW;
                    // arcAngle is negative for a CW rotation
                    if(rotateCW) arcAngle = -arcAngle;
                    // check to see if this next rotation is too far back; if so, limit the arcAngle
                    nextSegmentDirection = segmentDirection + arcAngle; // wrap past 180 to keep track of winding
                    nextRotationAngle = nextSegmentDirection - channelDirection; // wrap past 180 to keep track of winding
                    if (nextRotationAngle < -limitRotateAngle)
                        arcAngle += -limitRotateAngle - nextRotationAngle;
                    else if (nextRotationAngle > limitRotateAngle)
                        arcAngle -= nextRotationAngle - limitRotateAngle;

                    segment = new StsChannelArcSegment(channel, segmentDirection, radius, arcAngle, lastPoint);
                    segments.add(segment);
                    lastPoint = segment.getLastPoint();

                    segmentDirection += arcAngle;
                    lastRotateCW = rotateCW;
                }
                else
                {
                    if(StsMath.betweenInclusive(segmentDirection, -limitAddLineAngle, limitAddLineAngle))
                    {
                        float length = (float) channelLineLengthDivWidthDistribBox.getSample()*channelWidth;
                        segment = new StsChannelLineSegment(channel, lastPoint, segmentDirection, length);
                        segments.add(segment);
                        lastPoint = segment.getLastPoint();
                    }
                }
                isArc = !isArc; // alternate arcs and lines; but draw lines only if segment direction is close to channel direction
            }
            channel.channelSegments = segments.toArray(new StsChannelSegment[0]);
            channelSet.setChannelsState(StsChannelSet.CHANNELS_ARCS);
        }
        wizard.model.enableDisplay();
        wizard.enableFinish();
        wizard.model.win3dDisplay();
    }

    public void buildGrid()
    {
        StsChannelSet channelSet = ((StsCreateChannelsWizard)wizard).getChannelSet();
        StsObjectRefList channels = channelSet.getChannels();
        float nSlices = geoModelVolume.getNSlices();

        StsRotatedGridBoundingBox centeredGrid = geoModelVolume.createCenteredGrid();
        Iterator<StsChannel> iter = channels.getIterator();
        // don't draw views until construction complete
        wizard.model.disableDisplay();
        wizard.disableNext();
        while(iter.hasNext())
        {
            StsChannel channel = iter.next();
            for(StsChannelSegment channelSegment : channel.channelSegments)
                channelSegment.buildGrids(geoModelVolume);
            channelSet.setChannelsState(StsChannelSet.CHANNELS_GRIDS);
        }
        wizard.model.enableDisplay();
        wizard.enableFinish();
        wizard.model.win3dDisplay();
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