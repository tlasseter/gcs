
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Surfaces.Actions.Wizards;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.Framework.MVC.*;
import com.Sts.PlugIns.Surfaces.DBTypes.*;
import com.Sts.Framework.IO.*;

import java.util.*;

public class StsImportSurfaces
{
    public static boolean debug = false;
    public static byte horizontalUnits = StsParameters.DIST_NONE;
    public static byte verticalUnits = StsParameters.DIST_NONE;
    public static float datumShift = 0.0f;
    
    /* separate out the type/class string from the surface name */
    static public String parseFilename(String filename, StringBuffer name)
    {
        StringTokenizer stok = new StringTokenizer(filename, ".");
        String fileType, fileClass;
        try
        {
            fileType = stok.nextToken();
            fileClass = stok.nextToken();
            if (name != null) // return the name in a string buffer
            {
                name.setLength(0);
                name.append(stok.nextToken());
            }
            return fileType + "." + fileClass;
        }
        catch (Exception e)
        {
            StsException.outputException("StsImportSurfaces.parseFilename: ",
                                e, StsException.WARNING);
            return null;
        }
    }

    static public void writeBinaryFile(StsModel model, StsSurface surface, String group)
    {
        StsProject proj = model.getProject();
        String dirname = proj.getBinaryDirString();
        String name = surface.getName();
        String binaryFilename = new String(group + "." + StsAbstractFile.binaryFormat  + "." + name);
        surface.writeBinaryFile(dirname, binaryFilename);
    }
}
