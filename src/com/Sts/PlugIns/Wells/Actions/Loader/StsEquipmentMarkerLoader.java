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
public class StsEquipmentMarkerLoader extends StsMarkerLoader
{
	int subTypeIndex = -1;

	static public final StsColumnName subTypeName = new StsColumnName(StsLoader.SUB_TYPE, StsLoader.SUB_TYPE_KEYWORDS);

	public StsEquipmentMarkerLoader(StsModel model, String name, StsWell well, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		super(model, name, well, deleteStsData, isSourceData, progressPanel);
		acceptableNameSet.addAliases(subTypeName);
	}

	public void setGroup()
	{
		group = StsLoader.GROUP_WELL_EQUIP;
	}

	protected boolean processColumnName(StsColumnName columnName)
	{
		if(super.processColumnName(columnName))
			return true;
		if(!columnName.equalsColumnName(StsLoader.SUB_TYPE)) return false;
		subTypeIndex = columnName.fileColumnIndex;
		return true;
	}

	public boolean checkColumnNames()
	{
		if(!super.checkColumnNames()) return false;
		return subTypeIndex != -1;
	}

	public StsWellMarker constructMarker()
	{
		subTypeString = tokens[subTypeIndex];
		return StsEquipmentMarker.constructor(markerName, well, location, subTypeString);
	}
}
