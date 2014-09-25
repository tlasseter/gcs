package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 5/7/11
 * Time: 2:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsFmiMarkerLoader extends StsMarkerLoader
{
	int dipIndex = -1, azimuthIndex = -1;
	{
		nameIndex = -1;
	}

	static public final String[] DIP_KEYWORDS = StsLoader.DIP_KEYWORDS;
	static public final String DIP = StsLoader.DIP;
	static public final StsColumnName dipColumnName = new StsColumnName(DIP, DIP_KEYWORDS);

	static public final String[] AZIMUTH_KEYWORDS = StsLoader.AZIMUTH_KEYWORDS;
	static public final String AZIMUTH = StsLoader.AZIMUTH;
	static public final StsColumnName azimuthColumnName = new StsColumnName(AZIMUTH, AZIMUTH_KEYWORDS);

	public StsFmiMarkerLoader(StsModel model, String name, StsWell well, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		super(model, name, well, deleteStsData, isSourceData, progressPanel);
		acceptableNameSet.addAliases(dipColumnName);
		acceptableNameSet.addAliases(azimuthColumnName);
	}

	public void setGroup()
	{
		group = StsLoader.GROUP_WELL_FMI;
	}

	protected boolean processColumnName(StsColumnName columnName)
	{
		if(super.processColumnName(columnName))
			return true;
		if(columnName.equalsColumnName(DIP))
		{
			dipIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(AZIMUTH))
		{
			azimuthIndex = columnName.fileColumnIndex;
			return true;
		}
		return false;
	}

	public boolean checkColumnNames()
	{
		if(!super.checkColumnNames()) return false;
		return dipIndex != -1 && azimuthIndex != -1;
	}

	public StsWellMarker constructMarker()
	{
		float dip = Float.parseFloat(tokens[dipIndex]);
		float azimuth = Float.parseFloat(tokens[azimuthIndex]);
		return StsFMIMarker.constructor(well, location, dip, azimuth);
	}
}
