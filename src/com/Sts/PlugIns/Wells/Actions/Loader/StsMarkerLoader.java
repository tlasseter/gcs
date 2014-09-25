package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsMarkerLoader extends StsObjectLoader
{
	StsWell well;
	public int nLoaded = 0;
	protected String[] tokens;

	float vScalar, hScalar;
	byte coordinatesType = COOR_TYPE_NONE;
	/** name must be in first column (there is no NAME column header); override with -1 if a markerLoader subclass has no NAME column (such as FMIMarkers) */
	int nameIndex = 0;
	int xIndex = -1, yIndex = -1, zIndex = -1, mdIndex = -1;
	String markerName;
	StsPoint location;
	String subTypeString;
	StsWellMarker marker = null;

	static final byte COOR_TYPE_NONE = 0;
	static final byte COOR_TYPE_MDEPTH = 1;
	static final byte COOR_TYPE_DEPTH = 2;
	static final byte COOR_TYPE_XYZ = 3;

	static public final StsColumnName xColumnName = new StsColumnName(X, X_KEYWORDS);
	static public final StsColumnName yColumnName = new StsColumnName(Y, Y_KEYWORDS);
	static public final StsColumnName mdepthColumnName = new StsColumnName(MDEPTH, MD_KEYWORDS);
	static public final StsColumnName depthColumnName = new StsColumnName(DEPTH, DEPTH_KEYWORDS);

	/** subFiles must belong to one of these groups. */
	//static public final String[] markerGroups = new String[] { StsWell.groupWellRef, StsWell.groupWellPerf, StsWell.groupWellEquip, StsWell.groupWellFMI };

	public StsMarkerLoader(StsModel model, String name, StsWell well, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		super(model, name, deleteStsData, isSourceData, progressPanel);
		setWell(well);
		acceptableNameSet.addAliases(xColumnName);
		acceptableNameSet.addAliases(yColumnName);
		acceptableNameSet.addAliases(depthColumnName);
		acceptableNameSet.addAliases(mdepthColumnName);
		vScalar = well.getZScalar();
		hScalar = well.getXyScalar();
	}

	public void setWell(StsWell well)
	{
		this.well = well;
		setStsMainObject(well);
	}

	public String getAsciiFilePathname()
	{
		return well.getAsciiDirectoryPathname();
	}

	public void setGroup()
	{
		group = GROUP_WELL_REF;
	}

    public boolean readFileHeader(StsAbstractFile file)
    {
		if(file == null) return false;

        try
        {
			while(true)
			{
				String line = file.readLine().trim();
                // line = StsStringUtils.deTabString(line);
				if(line.endsWith(WELLNAME))
				{
					String name =  new String(file.readLine().trim());
				}
				else if(line.endsWith(NULL_VALUE))
				{
					line = file.readLine().trim();  // get the next line
					StringTokenizer stok = new StringTokenizer(line);
					nullValue = Float.valueOf(stok.nextToken()).floatValue();
				}
				else if(line.endsWith(CURVE))
				{
					line = file.readLine().trim();
					// first token in value lines must be the marker name
					int columnIndex = 0;
					if(nameIndex == 0)
						columnIndex = 1;

					while(!lineHasKeyword(line, VALUE))
					{
						// from all possible vectors (including aliases), find a matching nameVector and return a copy of it
						// with name and matching alias (if different), and current column index
						StsColumnName matchName = acceptableNameSet.getMatchName(line, columnIndex);
						if(matchName != null) // add this as an acceptable (standard) column name
						{
							matchName = new StsColumnName(matchName, columnIndex);
							nameSet.add(matchName);
						}
						else if(curveNameOk(line)) // foundVector == null: add this as an ok but non-standard column name
						{
							matchName = new StsColumnName(line, columnIndex);
							nameSet.add(matchName);
						}
						columnIndex++;
						line = file.readLine().trim();
					}
					nRequired = columnIndex;
					appendLine("Read Marker header file: " + file.filename);
					return checkVectors();
				}
				else if(line.endsWith(VALUE)) // didn't find any curve names
				{
					appendErrorLine(" Didn't find  any curve names in file: " + file.filename);
					StsMessageFiles.errorMessage("StsMarkerFileConstructor.read() failed." +
							" Didn't find  any curve names in file: " + file.filename);
					return false;
				}
				else
					progressPanel.appendErrorLine("File " + file.filename + " failed to process line: " + line);
			}

        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "read", e);
			return false;
        }
    }

	public boolean checkVectors()
	{
		for (StsColumnName columnName : nameSet)
			processColumnName(columnName);
		return checkColumnNames();
	}

	protected boolean processColumnName(StsColumnName columnName)
	{
		if(columnName.equalsColumnName(MDEPTH))
		{
			mdIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(DEPTH))
		{
			zIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(X))
		{
			xIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(Y))
		{
			yIndex = columnName.fileColumnIndex;
			return true;
		}
		return false;
	}

	protected boolean checkColumnNames()
	{
		if(mdIndex == -1 && zIndex == -1) return false;
		if(mdIndex != -1)
			coordinatesType = COOR_TYPE_MDEPTH;
		else if(xIndex != -1 && yIndex != -1)
			coordinatesType = COOR_TYPE_XYZ;
		else
			coordinatesType = COOR_TYPE_DEPTH;

		return true;
	}

    protected boolean readProcessData(StsAbstractFile file, StsProgressPanel progressPanel)
    {
		String line = null;
        nLoaded = 0;
		// ArrayList<StsWellMarker> markerList = new ArrayList<StsWellMarker>();

		if(file == null) return false;
        try
        {
			if(isSourceData)
			{
				String dataDirectory = well.getAsciiDirectoryPathname();
				StsFile dataFile = StsFile.constructor(dataDirectory, file.filename);
				file.copyTo(dataFile);
			}

            if(well == null || file == null) return false;

            while ((line = file.readLine()) != null)
            {
                line = line.trim();
                if (line.equals("")) continue;  // blank line
				tokens = StsStringUtils.getTokens(line);
				int nTokens = tokens.length;
				if(nTokens < nRequired) continue;
				if(nameIndex != -1)
                	markerName = tokens[nameIndex];
				if(well.getMarker(markerName) != null) continue;  // already have it
				location = computeMarkerLocation();
				if(location == null) continue;
				constructMarker();
				nLoaded++;
            }
			return nLoaded > 0;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(this, "consructMarker", "line: " + line, e);
            return false;
        }
        finally
        {
            file.closeReader();
        }
    }

	public StsPoint computeMarkerLocation()
	{
		marker = null;
		int nTokens = tokens.length;
		if(nTokens < nRequired) return null;
		if(coordinatesType == COOR_TYPE_MDEPTH)
		{
			float mdepth = Float.parseFloat(tokens[mdIndex])*vScalar;
			float[] xyztm = well.getCoordinatesAtMDepth(mdepth, false);
			if(xyztm == null)
			{
				location = StsPoint.nullPoint(5);
				location.setM(mdepth);
			}
			else
				location = new StsPoint(xyztm);
		}
		else
		{
			float depth = Float.parseFloat(tokens[zIndex])*vScalar;
			float[] xyztm = well.getCoordinatesAtDepth(depth, false);
			if(xyztm == null)
			{
				location = StsPoint.nullPoint(5);
				location.setZ(depth);
			}
			else
				location = new StsPoint(xyztm);
		}
		return location;
	}

	public StsWellMarker constructMarker()
	{
		return new StsWellMarker(markerName, well, (byte)type, location);
	}

	public boolean changeSourceFile(StsAbstractFile file, boolean loadValues)
	{
		return true;
	}

    public void setNullValue()
    {
        nullValue = project.getLogNull();
    }
}