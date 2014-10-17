package com.Sts.PlugIns.GeoModels.Actions.Wizards.Channels;

import com.Sts.Framework.Actions.WizardComponents.StsRandomDistribGroupBox;
import com.Sts.Framework.Actions.Wizards.StsWizard;
import com.Sts.Framework.DBTypes.StsObjectRefList;
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

    static float channelArcRadiusAvg = 100;
    static float channelArcRadiusDev = 40;
    static float channelArcAngleAvg = 135;
    static float channelArcAngleDev = 45;
    static float channelLineLengthAvg = 50;
    static float channelLineLengthDev = 20;

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
        channelArcRadiusDistribBox = new StsRandomDistribGroupBox(channelArcRadiusAvg, channelArcRadiusDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel arc radius/width");
        channelArcAngleDistribBox = new StsRandomDistribGroupBox(channelArcAngleAvg, channelArcAngleDev, StsRandomDistribFace.TYPE_GAUSS, "Channel arc angle");
        channelLineLengthDistribBox = new StsRandomDistribGroupBox(channelLineLengthAvg, channelLineLengthDev, StsRandomDistribFace.TYPE_LOGNORM, "Channel line length");
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
        Random random = new Random();
        boolean lastRotateCW = true;
        while(iter.hasNext())
        {
            StsChannel channel = iter.next();
            StsPoint startPoint = channel.getStartPoint();
            float channelDirection = channel.getDirection();
            float segmentDirection = channelDirection;
            boolean isArc = false; // first segment will be line
            StsPoint lastPoint = startPoint;
            StsChannelSegment segment;
            ArrayList<StsChannelSegment> segments = new ArrayList<>();
            while(insideVolume(geoModelVolume, lastPoint))
            {
                if(isArc)
                {
                    float radius = (float)channelArcRadiusDistribBox.getSample();
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

                    segment = new StsChannelArcSegment(segmentDirection, radius, arcAngle, lastPoint);
                    segments.add(segment);
                    lastPoint = segment.getLastPoint();

                    segmentDirection += arcAngle;
                    lastRotateCW = rotateCW;
                }
                else
                {
                    if(StsMath.betweenInclusive(segmentDirection, -limitAddLineAngle, limitAddLineAngle))
                    {
                        float length = (float) channelLineLengthDistribBox.getSample();
                        segment = new StsChannelLineSegment(lastPoint, segmentDirection, length);
                        segments.add(segment);
                        lastPoint = segment.getLastPoint();
                    }
                }
                isArc = !isArc; // alternate arcs and lines; but draw lines only if segment direction is close to channel direction
            }
            channel.channelSegments = segments.toArray(new StsChannelSegment[0]);
            channelSet.setChannelsState(StsChannelSet.CHANNELS_ARCS);
        }
    }
    public void buildx()
    {
        StsChannelSet channelSet = ((StsCreateChannelsWizard)wizard).getChannelSet();
        StsObjectRefList channels = channelSet.getChannels();
        Iterator<StsChannel> iter = channels.getIterator();
        Random random = new Random();
        boolean lastRotateCW = true;
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
                    double rotationAngle = StsChannelArcSegment.subtractAngles(segmentDirection, channelDirection);
                    // if segmentDirection is more than 30 degrees away from channel direction, rotate the opposite way;
                    // otherwise rotate the opposite of the previous rotation
                    boolean rotateCW;
                    if(rotationAngle > 30)
                        rotateCW = true;
                    else if(rotationAngle < -30)
                        rotateCW = false;
                    else
                        rotateCW = !lastRotateCW;

                    // if we need to rotate CCW back towards the channelDirection, then make our next rotation CCW, otherwise CW
                    // a clockwise rotation is negative
                    if(rotateCW) arcAngle = -arcAngle;
                    // if we are reversing rotation or the current segment direction is more than 90 degrees from channel direction,
                    // don't insert a line segment (isArc == true indicates next segment is an arc).
                    isArc = Math.abs(rotationAngle) > limitAddLineAngle || lastRotateCW != rotateCW;
                    // In this case, don't let the reverse rotation to go more than 120 degrees the other side of the channel direction.
                    if(isArc)
                    {
                        nextSegmentDirection = segmentDirection + arcAngle; // wrap past 180 to keep track of winding
                        nextRotationAngle = nextSegmentDirection - channelDirection; // wrap past 180 to keep track of winding
                        if (nextRotationAngle < -limitRotateAngle)
                            arcAngle += -limitRotateAngle - nextRotationAngle;
                        else if (nextRotationAngle > limitRotateAngle)
                            arcAngle -= nextRotationAngle - limitRotateAngle;
                    }
                    segment = new StsChannelArcSegment(segmentDirection, radius, arcAngle, lastPoint);
                    segmentDirection += arcAngle;

                    lastRotateCW = rotateCW;
                    // segmentDirection = StsChannelArcSegment.addAngles(segmentDirection, arcAngle);
                }
                else
                {
                    float length = (float)channelLineLengthDistribBox.getSample();
                    segment = new StsChannelLineSegment(lastPoint, segmentDirection, length);
                    isArc = true;
                }
                lastPoint = segment.getLastPoint();
                segments.add(segment);
            }
            channel.channelSegments = segments.toArray(new StsChannelSegment[0]);
            channelSet.setChannelsState(StsChannelSet.CHANNELS_ARCS);
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