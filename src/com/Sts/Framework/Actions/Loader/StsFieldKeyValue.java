package com.Sts.Framework.Actions.Loader;

import java.lang.reflect.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 8/18/11
 * Time: 5:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsFieldKeyValue
{
	public String fieldName;
	public String[] keyNames;
	public Field field;
	public String fieldValue;

	public StsFieldKeyValue(String fieldName, String[] keyNames)
	{
		this.fieldName = fieldName;
		int nKeyNames = keyNames.length;
		for(int n = 0; n < nKeyNames; n++)
			keyNames[n] = keyNames[n].toLowerCase();
		this.keyNames = keyNames;
	}

	public StsFieldKeyValue(String fieldName, String keyName)
	{
		this.fieldName = fieldName;
		this.keyNames = new String[] { keyName };
	}

	public boolean setKeyValue(String key, String value)
	{
		for(String keyName : keyNames)
		{
			if(keyName.equals(key))
			{
				fieldValue = value;
				return true;
			}
		}
		return false;
	}

	public String toString() { return "field: " + fieldName + " value: " + fieldValue; };
}
