
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.IO;

import com.Sts.Framework.Utilities.*;

public interface StsPackable
{
  	public String packFields() throws StsException;
  	public void unpackFields(String s) throws StsException;
}
