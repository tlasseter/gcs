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
import com.Sts.Framework.Utilities.StsToolkit;

import java.util.Iterator;

public class StsChannelClass extends StsModelObjectPanelClass implements StsSerializable, StsTreeObjectI, StsRotatedClass, StsClassDisplayable
{
    private boolean displayCenterLinePoints = false;
    private boolean displayChanged = false;
    private int numberInSelectedGroup = 5;
    private byte drawType = DRAW_LINES;
    private byte drawSequence = DRAW_ALL;

    /** drawType selections */
    static public final byte DRAW_AXES = 0;
    static public final byte DRAW_LINES = 1;
    static public final byte DRAW_FILLED = 2;
    static public final byte DRAW_GRID = 3;
    static public final String DRAW_AXES_STRING = "Draw axes";
    static public final String DRAW_LINES_STRING = "Draw lines";
    static public final String DRAW_FILLED_STRING = "Draw filled";
    static public final String DRAW_GRID_STRING = "Draw grid";
    static final String[] DRAW_TYPE_STRINGS = new String[] { DRAW_AXES_STRING, DRAW_LINES_STRING,  DRAW_FILLED_STRING, DRAW_GRID_STRING};

    /** drawSequence selections */
    static public final byte DRAW_ALL = 0;
    static public final byte DRAW_SEQUENCE = 1;
    static public final byte DRAW_ZPLANE = 2;
    static public final String DRAW_ALL_STRING = "Draw all";
    static public final String DRAW_SEQUENCE_STRING = "Draw sequence";
    static public final String DRAW_ZPLANE_STRING = "Draw Z cursor plane";
    static final String[] DRAW_SEQUENCE_STRINGS = new String[] { DRAW_ALL_STRING, DRAW_SEQUENCE_STRING, DRAW_ZPLANE_STRING};

    public StsChannelClass() { }

    public void initializeDisplayFields()
    {
        displayFields = new StsFieldBean[]
        {
            new StsComboBoxFieldBean(this, "drawType", "Display filled.", DRAW_TYPE_STRINGS),
            new StsComboBoxFieldBean(this, "drawSequence", "Display Selection.", DRAW_SEQUENCE_STRINGS),
            new StsIntFieldBean(this, "numberInSelectedGroup", 1, 10, "Number to display"),
            new StsBooleanFieldBean(this, "displayCenterLinePoints", "Display points")
        };
    }

    public boolean setCurrentObject(StsObject object)
    {
        if(!super.setCurrentObject(object)) return false;
        currentModel.repaintWin3d();
        return true;
    }

    private Iterator<StsChannel> getZPlaneChannelIterator(int nSlice)
    {
        return new ZPlaneChannelIterator(nSlice);
    }

    public class ZPlaneChannelIterator implements Iterator<StsChannel>
    {
        int nSlice;
        StsObjectList.ObjectIterator iterator;
        StsChannel next = null;

        ZPlaneChannelIterator(int nSlice)
        {
            this.nSlice = nSlice;
            iterator = list.getObjectIterator();
            next = initializeNext();
        }

        private StsChannel initializeNext()
        {
            while(iterator.hasNext())
            {
                StsChannel channel = (StsChannel) iterator.next();
                if (channel.subBoxContainsSlice(nSlice))
                    return channel;
            }
            return null;
        }

        private StsChannel getNext()
        {
            if(!iterator.hasNext()) return null;
            return (StsChannel)iterator.next();
        }

        public boolean hasNext()
        {
            return next != null;
        }
        public StsChannel next()
        {
            StsChannel currentNext = next;
            next = getNext();
            if(next != null && !next.subBoxContainsSlice(nSlice)) next = null;
            return currentNext;
        }

        public void remove()
        {
        }
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

        // draw grid is done with texture rather than object drawing, so skip out
        if(drawType == StsChannelClass.DRAW_GRID) return;

        if(drawSequence == DRAW_ZPLANE)
        {
            int nSlice = currentModel.getCursor3d().getCurrentGridCoordinate(StsCursor3d.ZDIR);
            Iterator<StsChannel> iter = getZPlaneChannelIterator(nSlice);
            while(iter.hasNext())
            {
                StsChannel channel = iter.next();
                channel.display(glPanel3d, displayCenterLinePoints, drawType);
            }
        }
        else if (drawSequence == DRAW_ALL)
        {
            Iterator iter = getVisibleObjectIterator();
            while (iter.hasNext())
            {
                StsChannel channel = (StsChannel) iter.next();
                channel.display(glPanel3d, displayCenterLinePoints, drawType);
            }
        }
        else if (drawSequence == DRAW_SEQUENCE)
        {
            int zPlane = currentModel.getCursor3d().getCurrentGridCoordinate(StsCursor3d.ZDIR);
            StsChannel channel = (StsChannel) getCurrentObject();
            if(channel == null) return;
            Iterator<StsChannel> iter = getChannelGroupIterator(channel);
            while (iter.hasNext())
                iter.next().display(glPanel3d, displayCenterLinePoints, drawType);
        }
    }


    private Iterator<StsChannel> getChannelGroupIterator(StsChannel channel)
    {
        int index = list.getIndex(channel);
        return list.getObjectSubsetIterator(index, numberInSelectedGroup);
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
        currentModel.repaintWin3dLaterOnEventThread();
    }

    public byte getDrawTypeByte() { return drawType; }

    public String getDrawSequence()
    {
        return DRAW_SEQUENCE_STRINGS[drawSequence];
    }

    public void setDrawSequence(String sequenceString)
    {
        byte drawSequence = StsParameters.getByteIndexFromString(sequenceString, DRAW_SEQUENCE_STRINGS);
        if(this.drawSequence == drawSequence) return;
        this.drawSequence = drawSequence;
        currentModel.getCursor3d().textureChanged();
        displayChanged = true;
        currentModel.repaintWin3d();
    }
}
