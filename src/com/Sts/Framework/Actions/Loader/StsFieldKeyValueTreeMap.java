package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Utilities.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 8/18/11
 * Time: 5:29 AM
 *
 * For each possible keyName in the fieldKeyValue, put a keyName-fieldKeyValue pair in the TreeMap
 */
public class StsFieldKeyValueTreeMap extends TreeMap<String, StsFieldKeyValue>
{
	TreeMap<String, Field> objectFieldsTreeMap;
	public StsFieldKeyValueTreeMap(Class c)
	{
		objectFieldsTreeMap = StsToolkit.getFieldsTreeMap(c);
	}

	public void put(StsFieldKeyValue keyValue)
	{
		String fieldName = keyValue.fieldName;
		Field field = objectFieldsTreeMap.get(fieldName);
		if(field == null) return;
		keyValue.field = field;
		for(String keyName : keyValue.keyNames)
			put(keyName, keyValue);
	}
}
