package com.Sts.Framework.DBTypes;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.Utilities.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 4/24/12
 */
public abstract class StsBoundingBoxObject extends StsBoundingBox
{
	/** Currently supported Z domains (time, depth) */
	private byte zDomainSupported = StsParameters.TD_DEPTH;
	/** The Z domain of the original boxObject data */
	protected byte zDomainOriginal = StsParameters.TD_DEPTH;
	/** The horizontal units of the original boxObject data */
	protected byte nativeHorizontalUnits = StsParameters.DIST_FEET;
	/** The vertical units of the original boxObject data */
	protected byte nativeVerticalUnits = StsParameters.DIST_FEET;
	/** bounding box around the points in unrotated coordinate system */
	transient protected StsBoundingBox unrotatedBoundingBox;
	/** routed grid bounding box around the points; grid increments are taken from project */
	transient protected StsRotatedGridBoundingBox rotatedBoundingBox;

	abstract public String getAsciiDirectoryPathname();
	abstract public String getBinaryDirectoryPathname();
	abstract public boolean addToProject();

	public StsBoundingBoxObject() { }

	public StsBoundingBoxObject(boolean persistent, String name)
	{
		super(persistent, name);
	}

	public StsBoundingBoxObject(boolean persistent)
	{
		super(persistent);
	}


	public void setZDomainSupported(byte zDomain)
	{
		if(this.zDomainSupported == zDomain) return;
		this.zDomainSupported = zDomain;
		dbFieldChanged("zDomainSupported", getZDomainSupported());
	}

	/** Currently supported Z domains (time, depth) */
	public byte getZDomainSupported()
	{
		return zDomainSupported;
	}
}
