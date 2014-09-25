
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Interfaces;

import com.Sts.Framework.Types.*;
import com.Sts.PlugIns.Model.DBTypes.*;

public interface StsEdgeLinkable
{
    StsEdgeLinkable getNextEdge();
    StsEdgeLinkable getPrevEdge();
    StsSurfaceVertex getNextVertex();
    StsSurfaceVertex getPrevVertex();
    String getLabel();
    StsList getEdgePointsList();
    StsList getGridEdgePointsList();
    float[][] getXYZPoints();
    int getRowOrCol();
    int getRowCol();
    boolean delete();
    StsGridSectionPoint getLastGridPoint();
    StsGridSectionPoint getFirstGridPoint();
}
