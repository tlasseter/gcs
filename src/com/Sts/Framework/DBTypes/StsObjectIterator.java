package com.Sts.Framework.DBTypes;

import java.util.*;

public class StsObjectIterator implements Iterator<StsObject>
{
	Collection classList;
	Iterator classIterator = null;
	Iterator<StsObject> objectIterator;

	public StsObjectIterator(Collection classList)
	{
		this.classList = classList;
		initialize();
	}

	private void initialize()
	{
		classIterator = classList.iterator();
		getNextClass();
	}

	private boolean getNextClass()
	{
		while (classIterator.hasNext())
		{
			StsClass stsClass = (StsClass) classIterator.next();
			if(stsClass.hasObjects())
			{
				objectIterator = stsClass.getStsObjectIterator();
				return true;
			}
		}
		objectIterator = null;
		return false;
	}

	public boolean hasNext()
	{
		if(objectIterator == null) return false;
		if(objectIterator.hasNext())
			return true;
		if(classIterator.hasNext())
		{
			if(!getNextClass()) return false;
			return objectIterator.hasNext();
		}
		return false;
	}

	public StsObject next() { return objectIterator.next(); }

	public void remove() { objectIterator.remove(); }

	public StsObject getFirst()
	{
		if(!hasNext()) return null;
		StsObject next = next();
		initialize();
		return next;
	}

    public StsObject getLast()
    {
        Object[] classes = classList.toArray();
        if(classes == null || classes.length == 0) return null;
        StsClass lastClass = (StsClass)classes[classes.length-1];
        return lastClass.getLast();
    }
}