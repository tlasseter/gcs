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
public class StsPerfMarkerLoader extends StsMarkerLoader
{
	int perfLengthIndex = -1, perfNShotsIndex = -1, longTimeIndex = -1;

	static public final String[] PERF_LENGTH_KEYWORDS = StsLoader.PERF_LENGTH_KEYWORDS;
	static public final String[] PERF_NSHOTS_KEYWORDS = StsLoader.PERF_NSHOTS_KEYWORDS;
	static public final String PERF_LENGTH = StsLoader.PERF_LENGTH;
	static public final String PERF_NSHOTS = StsLoader.PERF_NSHOTS;

	static public final StsColumnName perfLengthColumnName = new StsColumnName(PERF_LENGTH, PERF_LENGTH_KEYWORDS);
	static public final StsColumnName perfNShotsColumnName = new StsColumnName(PERF_NSHOTS, PERF_NSHOTS_KEYWORDS);
	static public final StsColumnName longTimeColumnName = new StsColumnName(LONG_TIME, LONG_TIME_KEYWORDS);

	public StsPerfMarkerLoader(StsModel model, String name, StsWell well, boolean deleteStsData, boolean isSourceData, StsProgressPanel progressPanel)
	{
		super(model, name, well, deleteStsData, isSourceData, progressPanel);
		acceptableNameSet.addAliases(perfLengthColumnName);
		acceptableNameSet.addAliases(perfNShotsColumnName);
		acceptableNameSet.addAliases(perfNShotsColumnName);
		acceptableNameSet.addAliases(longTimeColumnName);
	}

	public void setGroup()
	{
		group = GROUP_WELL_PERF;
	}

	protected boolean processColumnName(StsColumnName columnName)
	{
		if(super.processColumnName(columnName))
			return true;
		if(columnName.equalsColumnName(PERF_LENGTH))
		{
			perfLengthIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(PERF_NSHOTS))
		{
			perfNShotsIndex = columnName.fileColumnIndex;
			return true;
		}
		else if(columnName.equalsColumnName(LONG_TIME))
		{
			longTimeIndex = columnName.fileColumnIndex;
			return true;
		}
		return false;
	}

	public boolean checkColumnNames()
	{
		if(!super.checkColumnNames()) return false;
		return perfLengthIndex != -1;
	}

	public StsWellMarker constructMarker()
	{
		String name = tokens[nameIndex];

		float perfLength = Float.parseFloat(tokens[perfLengthIndex]);

		int perfNShots = 1;
		if(perfNShotsIndex != -1)
			perfNShots = Integer.parseInt(tokens[perfNShotsIndex]);

		long time = 0L;
		if(longTimeIndex != -1)
			time = Long.parseLong(tokens[longTimeIndex]);

		return StsPerforationMarker.constructor(name, well, location, perfLength, perfNShots, time);
	}
}
