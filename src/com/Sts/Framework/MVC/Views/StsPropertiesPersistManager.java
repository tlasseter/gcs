package com.Sts.Framework.MVC.Views;

import com.Sts.Framework.DB.DBCommand.*;
import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

/**
 * <p>Title: S2S development</p>
 *
 * <p>Description: Integrated seismic to simulation software</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: S2S Systems LLC</p>
 *
 * @author not attributable
 * @version c51c
 */

/** This is the central class for the management of views and view properties persistence via custom serialization
 *
 */
public class StsPropertiesPersistManager implements StsSerializable //, Runnable
{
	private StsClass[] classes;
    private StsProperties modelProperties;
	transient private StsModel model;

    static final boolean debug = false;
	static final long serialVersionUID = 1l;

	public StsPropertiesPersistManager()
	{

	}

	public StsPropertiesPersistManager(StsModel m)
	{
		setModel(m);
	}

	public void setModel(StsModel m)
	{
		model = m;
	}

	/* from a save-as */
	public void setModelAs(StsModel m)
	{
		setModel(m);
	}

	public void restore()
	{
		try
		{
            model.setClasses(classes);
            if(modelProperties != null) model.properties = modelProperties;
		}
		catch (Exception e)
		{
			new StsMessage(model.win3d, StsMessage.WARNING, "Properties restoration failed.\nError: " + e.getMessage());
		}
	}

	public void save(StsDBFile db)
	{
		try
		{
            classes = model.classList.toArray(new StsClass[0]);
            modelProperties = model.properties;
            if(debug) System.out.println("Writing properties to DB.");
            // db.debugCheckWritePosition("before properties persist write");
            if(!db.commitCmd("save properties", new StsAddTransientModelObjectCmd(this, "propertiesPersistManager")))
            {
                StsException.systemError(this, "save", "failed to commit cmd to db. status: " + db.statusStrings[db.status] + " transaction:" + db.transactionTypeStrings[db.transactionType]);
                return;
            }
            StsMessageFiles.logMessage("Properties saved in db.");
        }
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}