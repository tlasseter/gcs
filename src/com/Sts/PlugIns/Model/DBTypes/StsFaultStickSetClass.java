package com.Sts.PlugIns.Model.DBTypes;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.MVC.*;
import com.Sts.Framework.Types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Nov 9, 2007
 * Time: 5:51:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsFaultStickSetClass extends StsObjectPanelClass implements StsUnrotatedClass
{
    int nextColorIndex = 0;

    public StsFaultStickSetClass()
    {
    }

    public StsColor getNextColor()
    {
        StsSpectrum spectrum = currentModel.getSpectrum("Basic");
        return new StsColor(spectrum.getColor(nextColorIndex++));
    }

	//TODO Need to implement this!
	public void projectRotationAngleChanged()
	{
	}

}
