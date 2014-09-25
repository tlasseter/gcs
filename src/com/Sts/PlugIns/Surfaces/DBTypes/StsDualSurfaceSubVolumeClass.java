package com.Sts.PlugIns.Surfaces.DBTypes;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.UI.Beans.*;

public class StsDualSurfaceSubVolumeClass extends StsSubVolumeClass implements StsSerializable
{
    public StsDualSurfaceSubVolumeClass()
    {
    }

	public void initializeDisplayFields()
	{
		displayFields = new StsFieldBean[]
		{
		    new StsBooleanFieldBean(this, "isVisible", "Visible"),
		    new StsBooleanFieldBean(this, "isApplied", "Applied")
		};
	}
}
