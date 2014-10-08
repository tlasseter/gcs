package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DBTypes.StsMainObject;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.Interfaces.StsXYGridable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * © tom 9/30/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsChannelSet extends StsMainObject implements Serializable, Cloneable
{
    ArrayList<StsChannelLine> channels = new ArrayList<>(1000);

    public StsChannelSet() { }

    public StsChannelSet(boolean persistent) { super(persistent); }

    public void addChannel(StsChannelLine channelLine)
    {
        channelLine.addToModel();
        channels.add(channelLine);
    }
}
