
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Surfaces.Actions.Wizards;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.StsColor;
import com.Sts.Framework.UI.Progress.StsProgressPanel;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Surfaces.DBTypes.StsSurface;

import java.util.Arrays;
import java.util.Iterator;

public class StsImportAsciiSurfaces extends StsImportSurfaces
{
    // string constants
    static public final String ROW_ORDER = "ROW";
    static public final String COL_ORDER = "COL";
	static public final String XYZ_ORDER = "XYZ";

    static private String line;
    static private int nLines;
    static private boolean isTrueDepth;
    static private boolean isTvd;

    static public boolean scanSurface(StsModel model, StsAbstractFile file, String name, float datum,
                                      byte hUnits, byte vUnits, byte tUnits, boolean concatLines, boolean isTrueDepth_, boolean isTvd_)
    {
        double xOrigin, yOrigin;
        float xInc, yInc;
        int nCols, nRows, nPnts;
		String rowColOrder = ROW_ORDER;
        boolean rowOrder;
        float angle = 0.0f;
        float[][] zValues;
        float x, y, z = 0.0f;
        String[] tokens;
        
    	StsAsciiFile asciiFile = new StsAsciiFile(file);
        if(!asciiFile.openReadWithErrorMessage()) return false;
        
        String stringName = name.toString();
        datumShift = datum;
        isTrueDepth = isTrueDepth_;
        isTvd = isTvd_;
        try
        {
            // read the header line
            tokens = asciiFile.getTokens(new String[] {" ",","});

            if(tokens == null)
            {
        		new StsMessage(model.win3d, StsMessage.WARNING, "File header is null for " + asciiFile.getFilename());
            	return false;
            }
        }
        catch(Exception e)
       	{
        	new StsMessage(model.win3d, StsMessage.WARNING, "File header read error for " +
        			asciiFile.getFilename() + " during scan: " + e.getMessage());
        	return false;
        }
        try
        {
            boolean unitsChecked = false;
            // parse the header fields
            if(tokens.length < 7)
            {
                line = asciiFile.getLine();
        	    new StsMessage(model.win3d, StsMessage.WARNING, "Need 7 values in header line:\n" + line + "\n" + asciiFile.getFilename());
                return false;
            }

            xOrigin = Double.valueOf(tokens[0]).doubleValue();
            xInc =  Float.valueOf(tokens[1]).floatValue();
            yOrigin = Double.valueOf(tokens[2]).doubleValue();
            yInc = Float.valueOf(tokens[3]).floatValue();
            nCols = Integer.valueOf(tokens[4]).intValue();
            nRows = Integer.valueOf(tokens[5]).intValue();
			rowColOrder = tokens[6];

            if(tokens.length > 7) angle = Float.valueOf(tokens[7]).floatValue();

			if(rowColOrder == ROW_ORDER || rowColOrder == COL_ORDER)
			{
				Iterator tokenIterator = asciiFile.getTokenIterator();
				while(tokenIterator.hasNext())
				{
					String token = (String)tokenIterator.next();
					try
					{
						z = Float.parseFloat(token) + datumShift;
						break;
					}
					catch(Exception e)
					{
						StsException.systemError("Failed to parse float during scan of file: " + asciiFile.getFilename());
						return false;
					}
				}
			}
            // Validate the coordinates and grid against the project.
            if(StsImportAsciiSurfaces.isTrueDepth)
            {
            	;  // Not sure how to run an all inclusive check on ft versus meters.
            }
            else if(!unitsChecked)
            {
                if((verticalUnits == StsParameters.TIME_MSECOND) && (z < 20))
                {
                    if(StsYesNoDialog.questionValue(model.win3d, "Selected file " + asciiFile.getFilename() + " appears to have time in seconds, you selected milliseconds.\n\n Do you want to change it to seconds?\n"))
                        verticalUnits = StsParameters.TIME_SECOND;
                    unitsChecked = true;
                }
                if((verticalUnits == StsParameters.TIME_SECOND) && (z > 20))
                {
                    if(StsYesNoDialog.questionValue(model.win3d, "Selected file " + asciiFile.getFilename() + " appears to have time in milliseconds, you selected seconds.\n\n Do you want to change it to milliseconds?\n"))
                        verticalUnits = StsParameters.TIME_MSECOND;
                    unitsChecked = true;
                }
            }           
        }
        catch(Exception e)
       	{
        	new StsMessage(model.win3d, StsMessage.WARNING, "Problem parsing header line: " + line + "\n in file: " + asciiFile.getFilename());
        	return false;
        }
    	return true;
    }
    
    static public StsSurface createSurface(StsModel model, StsAbstractFile file, String name, boolean concatLines, StsProgressPanel panel)
    {
        StsSurface newSurface;
        StsAsciiFile asciiFile = null;
        double xOrigin, yOrigin;
        float xInc, yInc;
        int nCols, nRows, nPnts;
        boolean rowOrder;
		String rowColOrder;
        float angle = 0.0f;
        float[][] zValues;
        float x, y, z;
        boolean foundNull;

        asciiFile = new StsAsciiFile(file);
        if(!asciiFile.openReadWithErrorMessage()) return null;

        float nullZValue = model.getProject().getMapGenericNull();

        String fullPathname = file.getURLPathname();

        // see if we already have this surface read in
        StringBuffer surfaceName = new StringBuffer();  // object to receive surface name
        String filename = file.getFilename();
        parseFilename(filename, surfaceName);  // get surface name
        try
        {
            if (model.getCreateStsClass(StsSurface.class).getObjectWithName(surfaceName.toString()) != null)
            {
      	        StsMessageFiles.logMessage("Surface: " + surfaceName +
                        " already loaded into the model.  Ascii file read terminated.");
                return null;
            }
        }
        catch (Exception e)
        {
            StsMessageFiles.logMessage("Unable to read surface: " + surfaceName + ".");
            return null;
        }

      	StsMessageFiles.logMessage("Loading grid from: " + fullPathname + " ...");

        // String stringName = name.toString();

        byte zDomain = StsParameters.TD_TIME;
        if(isTrueDepth) zDomain = StsParameters.TD_DEPTH;

        // Get a color from the basic spectrum
        StsColor color = model.getCurrentSpectrumColor("Basic");

        // assume a z multiplier of -1 (elevation<->subsea)
//        float zMultiplier = -1.0f;

        String[] tokens;
        try
        {
            // read the header line
            tokens = asciiFile.getTokens(new String[] {" ",","});

            if(tokens == null)
            {
        		new StsMessage(model.win3d, StsMessage.WARNING, "File header is null for " +
                        fullPathname);
            	return null;
            }
        }
        catch(Exception e)
       	{
        	new StsMessage(model.win3d, StsMessage.WARNING, "File header read error for " +
                    fullPathname + ": " + e.getMessage());
//			StsStatusArea.getStatusArea().removeProgress();
//            cursor.restoreCursor();
        	return null;
        }
        try
        {
            // parse the header fields
            if(tokens.length < 7)
            {
                line = asciiFile.getLine();
        	    new StsMessage(model.win3d, StsMessage.WARNING, "Need 7 values in header line:\n" + line + "\n" +
                    fullPathname);
                return null;
            }

            xOrigin = Double.valueOf(tokens[0]).doubleValue();
            xInc =  Float.valueOf(tokens[1]).floatValue();
            yOrigin = Double.valueOf(tokens[2]).doubleValue();
            yInc = Float.valueOf(tokens[3]).floatValue();
            nCols = Integer.valueOf(tokens[4]).intValue();
            nRows = Integer.valueOf(tokens[5]).intValue();
			rowColOrder = tokens[6];
            rowOrder = rowColOrder.equals(ROW_ORDER);

            if(tokens.length > 7) angle = Float.valueOf(tokens[7]).floatValue();
        }
        catch(Exception e)
       	{
        	new StsMessage(model.win3d, StsMessage.WARNING, "Problem parsing header line: " +
                    line + "\n in file: " + fullPathname);
//			StsStatusArea.getStatusArea().removeProgress();
//            cursor.restoreCursor();
        	return null;
        }

        int nValuesFound = 0;
        int nValuesNeeded = nRows*nCols;

        int row, col;
        int rowStart, rowEnd, dRow;
        int colStart, colEnd, dCol;

        if(yInc > 0)
        {
            rowStart = 0; rowEnd = nRows-1; dRow = 1;
        }
        else
        {
            rowStart = nRows-1; rowEnd = 0; dRow = -1;
        }
        if(xInc > 0)
        {
            colStart = 0; colEnd = nCols-1; dCol = 1;
        }
        else
        {
            colStart = nCols-1; colEnd = 0; dCol = -1;
        }

        row = rowStart;
        col = colStart;

		try
        {
            zValues = new float[nRows][nCols];
            foundNull = false;

            if(rowColOrder == ROW_ORDER || rowColOrder == XYZ_ORDER)   // row ordered
	            panel.initialize(nRows);
            else if(rowColOrder == COL_ORDER)
	            panel.initialize(nCols);
            int progress = 0;

			if(rowColOrder == ROW_ORDER || rowColOrder == COL_ORDER)
			{
				Iterator tokenIterator = asciiFile.getTokenIterator();
				while(tokenIterator.hasNext())
				{
					String token = (String)tokenIterator.next();
					try { z = Float.parseFloat(token); }
					catch(Exception e)
					{
						line = asciiFile.getLine();
						nLines = asciiFile.getNLines();
						StsException.systemError("Failed to parse float from line number " + nLines + ":\n" + line);
						return null;
					}
					if (z == nullZValue)
					{
						zValues[row][col] = StsParameters.nullValue;
						foundNull = true;
					}
					else
					{
						if(isTvd)
							zValues[row][col] = z + datumShift;
						else
							zValues[row][col] = -z + datumShift;
					}
					nValuesFound++;
					if(rowOrder)
					{
						if(col == colEnd)
						{
							if(row == rowEnd) break;
							col = colStart;
							row += dRow;
						}
						else
							col += dCol;

					}
					else
					{
						if(row == rowEnd)
						{
							if(col == colEnd) break;
							row = rowStart;
							col += dCol;
						}
						else
							row += dRow;

					}
					panel.progressBar.incrementProgress();
				}
			}
			else
			{
				for(row = 0; row < nRows; row++)
					Arrays.fill(zValues[row], StsParameters.nullValue);

				int nLines = 0;
				while((tokens = asciiFile.getTokens(new String[] {" ",","})) != null)
				{
					try
					{
						x = Float.parseFloat(tokens[0]);
						y = Float.parseFloat(tokens[1]);
						z = Float.parseFloat(tokens[2]);
					}
					catch(Exception e)
					{
						line = asciiFile.getLine();
						nLines = asciiFile.getNLines();
						StsException.systemError("Failed to parse float from line number " + nLines + ":\n" + line);
						return null;
					}
					col = Math.round((float)(x-xOrigin)/xInc);
					row = Math.round((float)(y-yOrigin)/yInc);

					if(row < 0 || row > nRows-1)
					{
						StsMessageFiles.errorMessage("row " + row + " is out of range 0 to " +  nRows);
					}
					if(col < 0 || col > nCols-1)
					{
						StsMessageFiles.errorMessage("col " + col + " is out of range 0 to " +  nCols);
					}
					else
					{
						if(isTvd)
							zValues[row][col] = z + datumShift;
						else
							zValues[row][col] = -z + datumShift;
					}
					nLines++;
					if(nLines%nCols == 0)
						panel.progressBar.incrementProgress();
				}
			}
        }
       	catch (Exception e)
        {
        	new StsMessage(model.win3d, StsMessage.WARNING, "Error reading file at line: " + nLines +
            				 ".\n Last good line read was: " + line);
            return null;
        }

		if(rowColOrder == ROW_ORDER || rowColOrder == COL_ORDER)
		{
			int nMissing = nValuesNeeded - nValuesFound;
			if(nMissing > 0)
			{
				new StsMessage(model.win3d, StsMessage.WARNING, "Error reading file: "
						+ fullPathname + "\n" + nMissing + " values were missing");
				return null;
			}
		}

        // create the surface
        if (debug)
        {
            System.out.println("xOrigin = "  + xOrigin + ", xInc = " + xInc + ", nCols = " + nCols +
                               ", yOrigin = " + yOrigin + ", yInc = " + yInc + ", nRows = " + nRows +
                               ", angle = " + angle);
        }

        try
        {
            StsMessageFiles.logMessage("Adding surface to model ...");
/*
			String stringName = name.toString();

            boolean isDepth = StsMessage.questionValue(model.win3d, "Is " + stringName + " a depth surface (otherwise we will assume time)?");
            byte zDomain = StsParameters.TD_TIME;
            if(isDepth) zDomain = StsParameters.TD_DEPTH;
*/
            newSurface = StsSurface.constructSurface(name, color, StsSurface.IMPORTED,
				nCols, nRows, xOrigin, yOrigin, xInc, yInc, 0.0f, 0.0f, angle, zValues, foundNull,
                StsParameters.nullValue, zDomain, verticalUnits, horizontalUnits, panel);
            if(newSurface == null) return null;

//            if(newSurface.getZDomain() == StsParameters.TD_DEPTH)
//                newSurface.writeBinaryFile(dirname, binaryFilename, new byte[] {depthUnits, horizontalUnits});
//            else
//                newSurface.writeBinaryFile(dirname, binaryFilename, new byte[] {timeUnits, horizontalUnits});
            newSurface.setNativeUnits(verticalUnits, horizontalUnits);

            writeBinaryFile(model, newSurface, StsSurface.zmapGrp);

            model.incrementSpectrumColor("Basic");

             // Translate Surface to Model Units
            newSurface.toProjectUnits();

            if(!model.getProject().addToProjectRotatedBoundingBox(newSurface, newSurface.getZDomainOriginal()))
            {
                newSurface.delete();
                new StsMessage(model.win3d, StsMessage.WARNING, "Failed to load surface: " + name);
                return null;
            }
            newSurface.setRelativeRotationAngle();
        }
       	catch (Exception e)
        {
            StsException.outputException("StsGrid/Surface failed.", e, StsException.WARNING);
            return null;
        }

        asciiFile.close();

        System.out.println("Loaded map from file: " + fullPathname);
        StsMessageFiles.logMessage("Loaded map: " + name.toString());
        return newSurface;
    }
}
