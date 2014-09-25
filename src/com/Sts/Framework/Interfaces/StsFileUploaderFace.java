package com.Sts.Framework.Interfaces;

import java.io.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 7/14/12
 */
public interface StsFileUploaderFace
{
	public void doUpload(File file, String comment) throws FileNotFoundException;
}
