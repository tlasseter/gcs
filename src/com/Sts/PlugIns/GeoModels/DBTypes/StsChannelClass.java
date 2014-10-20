package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.StsClassDisplayable;
import com.Sts.Framework.Interfaces.MVC.StsRotatedClass;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.UI.Beans.StsBooleanFieldBean;
import com.Sts.Framework.UI.Beans.StsFieldBean;
import com.Sts.Framework.UI.Beans.StsIntFieldBean;

import java.util.Iterator;

public class StsChannelClass extends StsModelObjectPanelClass implements StsSerializable, StsTreeObjectI, StsRotatedClass, StsClassDisplayable
{
    private boolean displayAxes = false;
    private boolean displaySelectedChannel = false;
    private boolean displayCenterLinePoints = false;
    private boolean displayChanged = false;
    private int numberInSelectedGroup = 5;
    private boolean drawFilled = false;
    public StsChannelClass()
    {
    }

    public void initializeDisplayFields()
    {
        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "displayAxes", "Display channel axes only"),
            new StsBooleanFieldBean(this, "displaySelectedChannel", "Display selected channel"),
            new StsBooleanFieldBean(this, "displayCenterLinePoints", "Display points"),
            new StsIntFieldBean(this, "numberInSelectedGroup", 1, 10, "Number to display"),
            new StsBooleanFieldBean(this, "drawFilled", "Display filled."),
        };
    }

    public boolean setCurrentObject(StsObject object)
    {
        if(!super.setCurrentObject(object)) return false;
        currentModel.repaintWin3d();
        return true;
    }

    public void displayClass(StsGLPanel3d glPanel3d, long time)
    {
        if(displayChanged)
        {
            Iterator iter = getObjectIterator();
            while (iter.hasNext())
            {
                StsChannel channel = (StsChannel) iter.next();
                channel.deleteDisplayLists(glPanel3d.getGL());
            }
            displayChanged = false;
        }
        if(displaySelectedChannel)
        {
            if(numberInSelectedGroup == 1)
            {
                StsChannel channel = (StsChannel)getCurrentObject();
                channel.display(glPanel3d, displayCenterLinePoints, displayAxes, drawFilled);
            }
            else
            {
                StsChannel channel = (StsChannel) getCurrentObject();
                Iterator<StsChannel> iter = getChannelGroupIterator(channel);
                while (iter.hasNext())
                    iter.next().display(glPanel3d, displayCenterLinePoints, displayAxes, drawFilled);
            }
        }
        else
        {
            Iterator iter = getVisibleObjectIterator();
            while (iter.hasNext())
            {
                StsChannel channel = (StsChannel) iter.next();
                channel.display(glPanel3d, displayCenterLinePoints, displayAxes, drawFilled);
            }
        }
    }

    private Iterator<StsChannel> getChannelGroupIterator(StsChannel channel)
    {
        int index = list.getIndex(channel);
        return list.getObjectSubsetIterator(index, numberInSelectedGroup);

    }

    /** if true, display only the currently selected channel; otherwise display all channels. */
    public boolean isDisplaySelectedChannel()
    {
        return displaySelectedChannel;
    }

    public void setDisplaySelectedChannel(boolean displaySelectedChannel)
    {
        this.displaySelectedChannel = displaySelectedChannel;
        currentModel.repaintWin3d();
    }

    public boolean isDisplayCenterLinePoints()
    {
        return displayCenterLinePoints;
    }

    public void setDisplayCenterLinePoints(boolean displayCenterLinePoints)
    {
        this.displayCenterLinePoints = displayCenterLinePoints;
        displayChanged = true;
        currentModel.repaintWin3d();
    }

    public int getNumberInSelectedGroup()
    {
        return numberInSelectedGroup;
    }

    public void setNumberInSelectedGroup(int numberInSelectedGroup)
    {
        this.numberInSelectedGroup = numberInSelectedGroup;
        displayChanged = true;
        currentModel.repaintWin3d();
    }

    public boolean isDisplayAxes()
    {
        return displayAxes;
    }

    public void setDisplayAxes(boolean displayAxes)
    {
        this.displayAxes = displayAxes;
        currentModel.repaintWin3d();
    }

    public boolean isDrawFilled()
    {
        return drawFilled;
    }

    public void setDrawFilled(boolean drawFilled)
    {
        this.drawFilled = drawFilled;
        displayChanged = true;
        currentModel.repaintWin3d();
    }
}
