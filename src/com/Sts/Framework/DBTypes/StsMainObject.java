package com.Sts.Framework.DBTypes;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 9/24/11
 */
public class StsMainObject extends StsObject
{
	public String name = null;
	protected boolean isVisible = true;
	private byte type = StsParameters.NONE;
	/** dataSource is initialized and used by data objects which have associated source and file attributes */
	public StsDataSource dataSource = null;

	public StsMainObject()
    {
    }

    public StsMainObject(boolean persistent)
    {
        super(persistent);
    }

	public StsMainObject(boolean persistent, String name)
	{
		super(persistent);
		setName(name);
    }

	public void initializeDataSource()
	{
		dataSource = new StsDataSource();
	}

    static public Comparator getNameComparator() { return new NameComparator(); }

	public boolean isVisible()
	{
		return isVisible;
	}

	public void setVisible(boolean visible)
	{
		isVisible = visible;
	}

	static public final class NameComparator implements Comparator
    {
        NameComparator()
        {
        }

        // order by versions and then order alphabetically
        public int compare(Object o1, Object o2)
        {
            StsMainObject so1 = (StsMainObject)o1;
            StsMainObject so2 = (StsMainObject)o2;
            String name1 = so1.getName();
            String name2 = so2.getName();
            return name1.compareTo(name2);
        }
    }

    static public String[] getNamesFromObjects(StsMainObject[] objects)
    {
        if(objects == null) return new String[0];
        int nObjects = objects.length;
        String[] names = new String[nObjects];
        for(int n = 0; n < nObjects; n++)
            names[n] = objects[n].getName();
        return names;
    }

	static public StsMainObject getListObjectWithName(StsMainObject[] objects, String name)
	{
		if(objects == null || name == null) return null;
		int nObjects = objects.length;
		for(int n = 0; n < objects.length; n++)
			if(objects[n].getName().equals(name)) return objects[n];
		return null;
	}

	public StsObject getStsClassObjectWithName(Class listClass, String name)
    {
        StsClass list = currentModel.getCreateStsClass(listClass);
        return getStsClassObjectWithName(list, name);
    }

    public StsObject getStsClassObjectWithName(String name)
    {
        StsClass stsClass = currentModel.getCreateStsClass(this.getClass());
        return getStsClassObjectWithName(stsClass, name);
    }

    public StsObject getStsClassObjectWithName(StsClass list, String name)
    {
        int nObjects = list.getSize();
        for (int n = 0; n < nObjects; n++)
        {
            StsObject stsObject = (StsObject) list.getElement(n);
			String objectName = stsObject.getName();
			if(objectName == null) continue; // shouldn't happen, but safeguards against bugs
            if (objectName.equals(name))
                return stsObject;
        }
        return null;
    }

	public void setName(String name)
	{
		this.name = name;
		if(isPersistent()) dbFieldChanged("name", name);
//        if(currentModel != null) currentModel.refreshObjectPanel(this);
	}

	public String getName()
	{
		if(name != null) return name;
		return super.getName();
	}

    static public String getObjectName(StsObject object) { return object.getName(); }

	public StsFieldBean[] getDefaultFields() { return null; }

	public byte getType() { return type; }
	public void setType(byte type) { this.type = type; }
	public boolean isType(byte type) { return getType() == type; }
	public String getTypeAsString() { return new String("Default-" + type); }

	public boolean getIsVisible() { return isVisible; }
	public void setIsVisible(boolean isVisible) { this.isVisible = isVisible; }

	public boolean setIsVisibleChanged(boolean isVisible)
	{
		if(this.isVisible == isVisible) return false;
		this.isVisible = isVisible;
		return true;
	}

	public StsDataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(StsDataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public void setDataSource(StsAbstractFile file)
	{
		this.dataSource = new StsDataSource(file);
	}
	
	public void changeDataSource(StsAbstractFile sourceFile)
	{
		dataSource.setDataSource(sourceFile);
	}
	
	public boolean isAlive(long time)
	{
		if(!sourceOk()) return false;
		return dataSource.isAlive(time);
	}

	private boolean sourceOk()
	{
		return dataSource != null;
	}

	private boolean debugSourceOk(String methodName)
	{
		if(dataSource != null) return true;
		StsException.systemError(this, methodName, "dataSource doesn't exist for " + toDebugString());
		return false;
	}

	public void setBornDate(long born)
	{
		if(!sourceOk()) return;
		 dataSource.setBornDate(born);
	}

	public long getBornDate()
	{
		if(!sourceOk()) return -1L;
		return dataSource.getBornDate();
	}

	public void setDeathDate(long death)
	{
		if(!sourceOk()) return;
		dataSource.setDeathDate(death);
	}
	public long getDeathDate()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getDeathDate();
	}

	public void setBornDate(String born)
	{
		if(!sourceOk()) return;
		dataSource.setBornDate(born);
	}

	public String getBornDateString()
	{
		if(!sourceOk()) return "undefined";
		return dataSource.getBornDateString();
	}

	public void setDeathDate(String death)
	{
		if(!sourceOk()) return;
		dataSource.setDeathDate(death);
	}

	public String getDeathDateString()
	{
		if(!sourceOk()) return "undefined";
		return dataSource.getDeathDateString();
	}

	private String getDateTimeStringFromLong(long time)
	{
		return getCurrentProject().getDateTimeStringFromLong(time);
	}

	private long getLongFromDateTimeString(String timeString)
	{
		return getCurrentProject().getLongFromDateTimeString(timeString);
	}

	public String getSourceURI()
	{
		if(!sourceOk()) return "undefined";
		return dataSource.getSourceURIString();
	}

	public void setSourceURI(String sourceURI)
	{
		if(!sourceOk()) return;
		dataSource.setSourceURIString(sourceURI);
	}
	
	public long getStsFileCreationTime()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getStsFileCreationTime();
	}

	public void setStsFileCreationTime(long stsFileCreationTime)
	{
		if(!sourceOk()) return;
		dataSource.setStsFileCreationTime(stsFileCreationTime);
	}

	public long getStsFileLastModifiedTime()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getStsFileLastModifiedTime();
	}

	public void setStsFileLastModifiedTime(long stsFileLastModifiedTime)
	{
		if(!sourceOk()) return;
		dataSource.setStsFileLastModifiedTime(stsFileLastModifiedTime);
	}

	public String getSourceURIString()
	{
		if(!sourceOk()) return "undefined";
		return dataSource.getSourceURIString();
	}

	public void setSourceURIString(String sourceURIString)
	{
		if(!sourceOk()) return;
		dataSource.setSourceURIString(sourceURIString);
	}

	public long getSourceCreationTime()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getSourceCreationTime();
	}

	public void setSourceCreationTime(long sourceCreationTime)
	{
		if(!sourceOk()) return;
		dataSource.setSourceCreationTime(sourceCreationTime);
	}

	public long getSourceLastModifiedTime()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getSourceLastModifiedTime();
	}

	public void setSourceLastModifiedTime(long sourceLastModifiedTime)
	{
		if(!sourceOk()) return;
		dataSource.setSourceLastModifiedTime(sourceLastModifiedTime);
	}

	public long getSourceSize()
	{
		if(!sourceOk()) return 0L;
		return dataSource.getSourceSize();
	}

	public void setSourceSize(long sourceSize)
	{
		if(!sourceOk()) return;
		dataSource.setSourceSize(sourceSize);
	}

	public long getCompleteDate()
	{
		if(!sourceOk()) return -0L;
		return dataSource.getCompleteDate();
	}

	public void setCompleteDate(long completeDate)
	{
		if(!sourceOk()) return;
		dataSource.setCompleteDate(completeDate);
	}

	public String getAsciiDirectoryPathname()
	{
		return getCurrentProject().getAsciiDirectoryPathname(getSimpleClassname(), name);
	}
}
