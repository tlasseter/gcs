package com.Sts.Framework.Actions.Loader;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 1/10/12
 */
public class StsKeyList extends ArrayList<String>
{
	public String addKey(String key)
	{
		super.add(key);
		return key;
	}
}