package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DBTypes.StsObjectRefList;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.MVC.Views.StsCursor3d;
import com.Sts.Framework.MVC.Views.StsView3d;
import com.Sts.Framework.Types.StsRotatedGridBoundingBox;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.StsObjectPanel;

import java.io.Serializable;

/**
 * © tom 9/30/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelSet extends StsRotatedGridBoundingBox implements StsTreeObjectI, Serializable, Cloneable
{
    private StsGeoModelVolume geoModelVolume;
    private StsObjectRefList channels = null;
    private byte channelsState = CHANNELS_NONE;

    static public byte CHANNELS_NONE = 0;
    static public byte CHANNELS_AXES = 1;
    static public byte CHANNELS_ARCS = 2;
    static public byte CHANNELS_GRIDS = 2;

    static public final StsFieldBean[] displayFields =
    {
        new StsBooleanFieldBean(StsChannelSet.class, "isVisible", "Enable")
    };

    static public final StsFieldBean[] propertyFields = new StsFieldBean[]
    {
        new StsStringFieldBean(StsChannelSet.class, "name", true, "Name")
    };

    static protected StsObjectPanel objectPanel = null;

    public StsChannelSet() { }

    public StsChannelSet(StsGeoModelVolume geoModelVolume, boolean persistent)
    {
        super(persistent);
        this.geoModelVolume = geoModelVolume;
    }

    public void addChannel(StsChannel channel)
    {
        if(channels == null) channels = StsObjectRefList.constructor(100, 100, "channels", this);
        channels.add(channel);
    }

    public byte getZDomainSupported()
    {
        return geoModelVolume.getZDomainSupported();
    }

    public StsFieldBean[] getDisplayFields() { return displayFields; }
    public StsFieldBean[] getPropertyFields() { return propertyFields; }
    public StsFieldBean[] getDefaultFields() { return null; }

    public Object[] getChildren()
    {
        return channels.getTrimmedList();
    }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null)
        {
            objectPanel = StsObjectPanel.constructor(this, true);
        }
        return objectPanel;
    }

    public boolean anyDependencies() { return false; }
    public boolean canExport() { return false; }
    public boolean export() { return false; }
    public boolean canLaunch() { return false; }
    public boolean launch() { return false; }

    public void treeObjectSelected()
    {
        currentModel.setCurrentObject(this);
        currentModel.getGlPanel3d().checkAddView(StsView3d.class);
        currentModel.win3dDisplayAll();
    }

    public StsObjectRefList getChannels()
    {
        return channels;
    }

    public byte getChannelsState()
    {
        return channelsState;
    }

    public void setChannelsState(byte channelsState)
    {
        this.channelsState = channelsState;
    }

    public StsGeoModelVolume getGeoModelVolume()
    {
        return geoModelVolume;
    }

    public void setGeoModelVolume(StsGeoModelVolume geoModelVolume)
    {
        this.geoModelVolume = geoModelVolume;
    }

    public void fillData(byte[] byteData, int dir, int nPlane)
    {
        StsChannel[] channelList = (StsChannel[]) channels.getCastList();
        StsChannel channel;

        if(dir == StsCursor3d.ZDIR)
        {
            for (int n = 0; n < channelList.length; n++)
            {
                channel = channelList[n];
                if (channel.subBoxContainsSlice(nPlane))
                {
                    channel.fillData(byteData);
                    n++;
                    for (; n < channelList.length; n++)
                    {
                        channel = channelList[n];
                        if (!channel.subBoxContainsSlice(nPlane)) return;
                        channel.fillData(byteData);
                    }
                }
            }
        }
        else // for XDIR and YDIR planes, let StsChannel handle the  vertical intersections with segments
        {
            for (int n = 0; n < channelList.length; n++)
                channelList[n].fillData(byteData, dir, nPlane);
        }

    }
}
