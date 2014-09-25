package com.Sts.Framework.DBTypes;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import com.Sts.Framework.DB.*;
import com.Sts.Framework.UI.Beans.*;

public class StsBoxSetSubVolumeClass extends StsSubVolumeClass implements StsSerializable
{
	public StsBoxSetSubVolumeClass()
	{
	}

	public void initializeDisplayFields()
	{
//		initColors(StsBoxSetSubVolume.displayFields);

		displayFields = new StsFieldBean[]
			{new StsBooleanFieldBean(this, "isVisible", "Visible"), new StsBooleanFieldBean(this, "isApplied", "Applied")
		};
	}

}
