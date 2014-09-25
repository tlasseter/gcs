package com.Sts.Framework.DBTypes.VectorSetObjects;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;

import java.util.*;

/**
 * Copyright:  Copyright (c) 2011
 * Author: Tom Lasseter
 * Date: 12/20/11
 */
abstract public class StsVectorSetObject extends StsBoundingBoxObject
{
	/** VectorSet containing valueVectors and clockTimeVector */
	protected StsVectorSet vectorSet;
	/** Array of values for a new attribute */
    transient protected float[] attribute = null;
	/** Name of the new attribute to add to sensor */
	transient protected String attributeName = null;

	static public final byte LINEAR = 0;
	static public final byte LOG10 = 1;
	static public final byte LOGE = 2;
	static public final String[] AXIS_STRINGS = new String[] { "Linear", "Log10", "LogE"};
	static public final byte[] AXIS_TYPES = new byte[] {LINEAR, LOG10, LOGE};

	/** Event symbols   */
	static public final byte SQUARE = 0;
	static public final byte CUBE = 1;
	static public final byte SPHERE = 2;
	static public final byte CYLINDER = 3;
	static public final byte DISK = 4;
	static public final byte TRIANGLE = 5;
	static public final byte STAR = 6;
	static public final byte DIAMOND = 7;
	static public final byte CIRCLE = 8;
	static public final byte NBSQUARE = 9;
	static public final byte BEACHBALL = 10;
	static public final String[] SYMBOL_TYPE_STRINGS = new String[] { "Square", "Cube", "Sphere", "Cylinder", "Disk", "Triangle", "Star", "Diamond", "Circle", "NoBorderSquare", "Beachballs"};
	static public final byte[] SYMBOL_TYPES = new byte[] {SQUARE, CUBE, SPHERE, CYLINDER, DISK, TRIANGLE, STAR, DIAMOND, CIRCLE, NBSQUARE, BEACHBALL};
	/** symbol type for sensor drawing */
    protected byte pointType = CUBE;

	abstract public boolean addVectorSetToObject(StsVectorSet vectorSet);

	protected StsVectorSetObject()
	{
	}

	protected StsVectorSetObject(boolean persistent, String name)
	{
		super(persistent, name);
	}

	StsVectorSetObject(boolean persistent)
	{
		super(persistent);
	}

	/** vector set containing valueVectors and clockTimeVector */
	public StsVectorSet getVectorSet()
	{
		return vectorSet;
	}

	public void setVectorSet(StsVectorSet vectorSet)
	{
		this.vectorSet = vectorSet;
	}

	public StsAbstractFloatVector[] getValueVectors()
	{
		return vectorSet.getValueVectors();
	}

	public int getNValues()
	{
		return vectorSet.getNValues();
	}

	public boolean hasValueVectorNamed(String name)
	{
		return vectorSet.hasValueVectorNamed(name);
	}

	public boolean addValueVector(StsFloatDataVector valueVector)
	{
		vectorSet.addValueVector(valueVector);
		return true;
	}

	public boolean addValueVectorList(ArrayList<StsAbstractFloatVector> valueVectors)
	{
		vectorSet.addValueVectorList(valueVectors);
		return true;
	}

	/**
	 * Re-write the currently set attribute. This will overwrite the current binary file with new data. It uses
	 * the AttVals and attributeName set in the setAttribute method
	 * @return success
	 */
	public boolean saveAttribute(String binaryDir, String name)
	{
		if(name == null)
			name = attributeName;
		if((attribute == null) || (attribute.length != getNValues()))
		{
			new StsMessage(currentModel.win3d, StsMessage.WARNING, "Unable to save attribute (" + name + "): Must have " + getNValues() + " for sensor " +  getName());
			return false;
		}
		String binaryFilename = "sensor.bin." + getName() + "." + name + ".0";
		StsFloatDataVector valueVector = new StsFloatDataVector(vectorSet, name);
		valueVector.checkSetValues(attribute);
		valueVector.checkWriteBinaryFile();
		addValueVector(valueVector);
		return true;
	}

	public String getSymbolString()
	{
		return SYMBOL_TYPE_STRINGS[pointType];
	}
}
