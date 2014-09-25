package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsCoorTimeVectorSetLoader extends StsTimeVectorSetLoader
{
	public StsCoorTimeVectorSetLoader(StsModel model)
	{
		super(model);
	}

	public StsCoorTimeVectorSetLoader(StsModel model, StsLoadWizard wizard, String name, StsProgressPanel progressPanel)
	{
		super(model, wizard, name, progressPanel);
	}

	public StsCoorTimeVectorSetLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, progressPanel);
	}

	public StsCoorTimeVectorSetLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, deleteStsData, progressPanel);
	}

	protected void initializeNameSet()
	{
		super.initializeNameSet();
		acceptableNameSet.addAliases(xColumnName);
		acceptableNameSet.addAliases(yColumnName);
		acceptableNameSet.addAliases(depthColumnName);
		//acceptableNameSet.addAliases(StsLoader.seismicTimeColumnName);
	}

	public boolean loadFile(StsAbstractFile file, boolean loadValues, boolean addToProject, boolean isSourceData)
	{
		//setDynamicVectorSetObject(new StsMicroseismic(file.name));
		return super.loadFile(file, loadValues, addToProject, getDynamicVectorSetObject(), isSourceData);
	}

	public StsCoorTimeVectorSetObject getDynamicVectorSetObject()
	{
		return (StsCoorTimeVectorSetObject) getTimeVectorSetObject();
	}

	public void setDynamicVectorSetObject(StsCoorTimeVectorSetObject dynamicVectorSetObject)
	{
		this.setTimeVectorSetObject(dynamicVectorSetObject);
	}
}