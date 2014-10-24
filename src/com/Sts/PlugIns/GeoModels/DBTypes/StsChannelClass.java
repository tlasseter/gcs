package com.Sts.PlugIns.GeoModels.DBTypes;

import com.Sts.Framework.DB.StsSerializable;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.StsClassDisplayable;
import com.Sts.Framework.Interfaces.MVC.StsRotatedClass;
import com.Sts.Framework.Interfaces.StsTreeObjectI;
import com.Sts.Framework.MVC.Views.StsCursor3d;
import com.Sts.Framework.MVC.Views.StsGLPanel3d;
import com.Sts.Framework.UI.Beans.StsBooleanFieldBean;
import com.Sts.Framework.UI.Beans.StsComboBoxFieldBean;
import com.Sts.Framework.UI.Beans.StsFieldBean;
import com.Sts.Framework.UI.Beans.StsIntFieldBean;
import com.Sts.Framework.Utilities.StsParameters;

import java.util.Iterator;

public class StsChannelClass extends StsModelObjectPanelClass implements StsSerializable, StsTreeObjectI, StsRotatedClass, StsClassDisplayable
{
    private boolean displayAxes = false;
    private boolean displayAll = false;
    private boolean displayCenterLinePoints = false;
    private boolean displayChanged = false;
    private int numberInSelectedGroup = 5;
    private byte drawType = DRAW_LINES;

    static public final byte DRAW_LINES = 0;
    static public final byte DRAW_FILLED = 1;
    static public final byte DRAW_GRID = 2;
    static public final byte DRAW_ZPLANE = 3;
    static public final String DRAW_LINES_STRING = "Draw lines";
    static public final String DRAW_FILLED_STRING = "Draw filled";
    static public final String DRAW_GRID_STRING = "Draw grid";
    static public final String DRAW_ZPLANE_STRING = "Draw Z cursor plane";
    static final String[] DRAW_TYPE_STRINGS = new String[] { DRAW_LINES_STRING,  DRAW_FILLED_STRING, DRAW_GRID_STRING, DRAW_ZPLANE_STRING};

    public StsChannelClass() { }

    public void initializeDisplayFields()
    {
        displayFields = new StsFieldBean[]
        {
            new StsBooleanFieldBean(this, "displayAxes", "Display channel axes only"),
            new StsBooleanFieldBean(this, "displayAll", "Display all channels."),
            new StsBooleanFieldBean(this, "displayCenterLinePoints", "Display points"),
            new StsIntFieldBean(this, "numberInSelectedGroup", 1, 10, "Number to display"),
            new StsComboBoxFieldBean(this, "drawType", "Display filled.", DRAW_TYPE_STRINGS)
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

        if(drawType != DRAW_ZPLANE)
        {
            if (displayAll)
            {
                Iterator iter = getVisibleObjectIterator();
                while (iter.hasNext())
                {
                    StsChannel channel = (StsChannel) iter.next();
                    channel.display(glPanel3d, displayCenterLinePoints, displayAxes, drawType);
                }
            } else // display only those intersected by Z cursor plane
            {
                int zPlane = currentModel.getCursor3d().getCurrentGridCoordinate(StsCursor3d.ZDIR);
                StsChannel channel = (StsChannel) getCurrentObject();
                if(channel == null) return;
                Iterator<StsChannel> iter = getChannelGroupIterator(channel);
                while (iter.hasNext())
                    iter.next().display(glPanel3d, displayCenterLinePoints, displayAxes, drawType, zPlane);
            }
        }
    }

    private Iterator<StsChannel> getChannelGroupIterator(StsChannel channel)
    {
        int index = list.getIndex(channel);
        return list.getObjectSubsetIterator(index, numberInSelectedGroup);

    }

    /** if true, display only the currently selected channel; otherwise display all channels. */
    public boolean isDisplayAll()
    {
        return displayAll;
    }

    public void setDisplayAll(boolean displayAll)
    {
        this.displayAll = displayAll;
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

    public String getDrawType()
    {
        return DRAW_TYPE_STRINGS[drawType];
    }

    public void setDrawType(String typeString)
    {
        byte drawType = StsParameters.getByteIndexFromString(typeString, DRAW_TYPE_STRINGS);
        if(this.drawType == drawType) return;
        this.drawType = drawType;
        currentModel.getCursor3d().textureChanged();
        displayChanged = true;
        currentModel.repaintWin3d();
    }

    public byte getDrawTypeByte() { return drawType; }
}
