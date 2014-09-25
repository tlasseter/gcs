package com.Sts.PlugIns.Model.DBTypes;


import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

public class StsFaultLine extends StsLine implements StsTreeObjectI
{
    transient protected StsList faultZones = null;
    transient public StsFaultStickSet faultStickSet = null;
    // display fields
    static public StsFieldBean[] faultDisplayFields = null;

    public StsFaultLine()
    {
    }

    public StsFaultLine(boolean persistent)
    {
        super(persistent);
    }

    static public StsFaultLine buildFault()
    {
        StsFaultLine faultLine = new StsFaultLine();
        faultLine.setType(StsParameters.FAULT);
        faultLine.setZDomainOriginal(currentModel.getProject().getZDomain());
        return faultLine;
    }

    static public StsFaultLine buildImportedFault()
    {
        StsFaultLine faultLine = buildFault();
        // faultLine.setVerticesRotated(false);
        return faultLine;
    }

    static public StsFaultLine buildImportedFault(StsFaultStickSet faultStickSet)
    {
        StsFaultLine faultLine = buildImportedFault();
        faultLine.faultStickSet = faultStickSet;
        return faultLine;
    }

    static public StsFaultLine buildVerticalFault(StsGridPoint gridPoint)
    {
        try
        {
            StsFaultLine line = StsFaultLine.buildFault();
            line.constructVertical(gridPoint.point);
            return line;
        }
        catch (Exception e)
        {
            StsException.systemError(StsFaultLine.class, "buildVerticalFault");
            return null;
        }
    }

    static public StsFaultLine buildFault(StsSurfaceVertex[] vertices)
    {
        try
        {
            StsFaultLine line = StsFaultLine.buildFault();
            line.construct(vertices);
            addMDepthToVertices(vertices);
            return line;
        }
        catch (Exception e)
        {
            StsException.systemError("StsLine.buildFault(gridPoints) failed.");
            return null;
        }
    }

    public void deleteTransients()
    {
        StsLineSections.deleteTransientZones(this);
        faultZones = null;
    }

    public void addToSet(StsFaultStickSet stickSet)
    {
        faultStickSet = stickSet;
        faultStickSet.faultSticks.add(this);
    }

    public String getLabel()
    {
        return getName() + " ";
    }

    public String toString()
    {
        return getName();
    }

    public StsLineZone getLineZone(StsSurfaceVertex topVertex, StsSurfaceVertex botVertex, StsSection section, int side)
    {
        boolean isFault;
        StsLineZone zone;

        if (side == RIGHT)
            zone = getLineZone(topVertex, botVertex, section, isFault = false);
        else
            zone = getLineZone(topVertex, botVertex, section, isFault = true);

        if (zone == null)
        {
            StsException.outputException(new StsException(StsException.WARNING,
                "StsLine.getLineZone() failed.",
                "Tried to construct a zone for vertices: " + topVertex.getLabel() +
                    " and " + botVertex.getLabel() + " on: " + getLabel()));
        }
        return zone;
    }

    public boolean delete()
    {
        if (!super.delete()) return false;
        return true;
    }

    public StsLineZone getLineZone(StsSurfaceVertex topVertex, StsSurfaceVertex botVertex, StsSection section, boolean isFault)
    {
        StsList lineZones;
        StsLineZone zone;

        if (isFault)
        {
            if (faultZones == null) faultZones = new StsList(2, 2);
            lineZones = faultZones;
        }
        else
        {
			lineZones = StsLineSections.getZoneList(this);
            if (lineZones == null) lineZones = new StsList(2, 2);
        }

        int nZones = lineZones.getSize();
        for (int n = 0; n < nZones; n++)
        {
            zone = (StsLineZone) lineZones.getElement(n);
            if (zone.getTop() == topVertex) return zone;
        }

        /** Couldn't find zone: make one and store it in zones */
        zone = new StsLineZone(currentModel, topVertex, botVertex, this);
        lineZones.add(zone);
        return zone;
    }

    public StsFieldBean[] getDisplayFields()
    {
        if (faultDisplayFields == null)
        {
            faultDisplayFields = new StsFieldBean[]
                {
                    new StsBooleanFieldBean(StsFaultLine.class, "isVisible", "Enable"),
                    new StsBooleanFieldBean(StsFaultLine.class, "drawLabels", "Draw Labels"),
                    new StsColorIndexFieldBean(StsFaultLine.class, "stsColorIndex", "Color", colorList),
                    new StsFloatFieldBean(StsFaultLine.class, "topZ", false, "Min Depth"),
                    new StsFloatFieldBean(StsFaultLine.class, "botZ", false, "Max Depth"),
                    new StsDoubleFieldBean(StsFaultLine.class, "xOrigin", false, "X Origin"),
                    new StsDoubleFieldBean(StsFaultLine.class, "yOrigin", false, "Y Origin")
                };
        }
        return faultDisplayFields;
    }

    public StsFieldBean[] getPropertyFields() { return null; }

    public Object[] getChildren() { return new Object[0]; }

    public boolean anyDependencies() { return false; }

    static public StsFieldBean[] getStaticDisplayFields() { return faultDisplayFields; }

    public StsObjectPanel getObjectPanel()
    {
        if (objectPanel == null) objectPanel = StsObjectPanel.constructor(this, true);
        return objectPanel;
    }

    public void treeObjectSelected()
    {
        currentModel.getCreateStsClass(StsFaultLine.class).selected(this);
    }
}
