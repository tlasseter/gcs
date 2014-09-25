package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/10/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsTimeVectorSet extends StsVectorSet implements Cloneable, Serializable, Iterable<StsAbstractVector>
{
	/** An optional clock-time vector which defines the clock-time value at the corresponding "point" described by the float vectors.
	 *  Required by some subClasses (StsMicroseismic, StsPump, StsTank). */
	protected StsTimeVector clockTimeVector;

	public StsTimeVectorSet()
    {
    }

	public StsTimeVectorSet(boolean persistent)
    {
		super(persistent);
    }

	public StsTimeVectorSet(StsAbstractFile file, String group, String name)
	{
		this(file.getURIString(), file.lastModified(), group, name);
	}

	public StsTimeVectorSet(String dataSource, long sourceCreateTime, String group, String name)
	{
		super(dataSource, sourceCreateTime, group, name);
	}

	public StsTimeVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(vectorSetLoader);
		setProjectObject(vectorSetLoader.getVectorSetObject());
	}

	static public StsTimeVectorSet constructor(StsTimeVectorSetLoader vectorSetLoader)
	{
		StsTimeVectorSet vectorSet = new StsTimeVectorSet(vectorSetLoader);
		//StsNameSet fileNameSet = vectorSetLoader.nameSet;
		//String name = vectorSetLoader.getStsMainObject().getName();
		if(!vectorSet.constructDataVectors(vectorSetLoader)) return null;
		return vectorSet;
	}

	public void addToModel()
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			if(!vector.isPersistent()) vector.addToModel();
		super.addToModel();
	}

    public boolean checkSetProjectTime(long time)
    {
		if(clockTimeVector == null) return false;
		return clockTimeVector.checkSetProjectTime(time);
	}

	public boolean disableTime()
	{
		if(clockTimeVector == null) return false;
		clockTimeVector.initializeTimeIndex();
		return true;
	}

    public boolean checkSetCurrentTime()
    {
		if(clockTimeVector == null) return false;
		return clockTimeVector.checkSetProjectTime(currentModel.getProject().getProjectTime());
	}

	public void setTimeIndex(boolean timeEnabled)
	{
		if(timeEnabled)
			checkSetCurrentTime();
		else
			setTimeIndexMax();
	}

	public void setTimeIndexMax()
	{
		if(clockTimeVector == null) return;
		clockTimeVector.initializeTimeIndex();
	}

	public int getMaxIndex()
	{
		if(clockTimeVector == null || !getProject().getTimeEnabled()) return getVectorsSize();
		return clockTimeVector.currentTimeIndex;
	}
/*
	public boolean appendTokens(String[] tokens)
	{
		for(StsAbstractFloatVector vector : this.getValueVectors())
			if(vector != null && vector instanceof StsFloatDataVector)
				vector.appendTokens(tokens);
		if(clockTimeVector != null && clockTimeVector instanceof StsLongDataVector)
			clockTimeVector.appendTokens(tokens);
		return true;
	}
*/
	public ArrayList<StsDataVectorFace> getDataVectorArrayList()
	{
		ArrayList<StsDataVectorFace> dataVectorArrayList = super.getDataVectorArrayList();
		if(clockTimeVector != null)
		// if(clockTimeVector != null && StsLongDataVector.class.isAssignableFrom(clockTimeVector.getClass()))
			dataVectorArrayList.add(clockTimeVector);
		return dataVectorArrayList;
	}

	public StsTimeVector getClockTimeVector()
	{
		return clockTimeVector;
	}

	public void setClockTimeVector(StsTimeVector clockTimeVector)
	{
		this.clockTimeVector = clockTimeVector;
	}

	public boolean initializeDataVectors(String name, StsNameSet fileNameSet)
	{
		try
		{
			ArrayList<StsAbstractFloatVector> vectorList = new ArrayList<StsAbstractFloatVector>();
			boolean hasClockTime = false;

			for(StsColumnName columnName : fileNameSet)
			{
				if(StsLoader.isTimeOrDateColumn(columnName))
				{
					hasClockTime = true;
					continue;
				}
				StsFloatDataVector vector = new StsFloatDataVector(this, columnName);
				int vectorSetColumnIndex = columnName.columnIndexFlag;
				if(vectorSetColumnIndex >= 0)
				{
					StsException.systemError(this, "initializeDataVectors", "VectorSet contains a coordinate vector " + columnName.name + ". Should be an xyzt or xyztm vectorSet.");
					continue;
				}
				vectorList.add(vector);
			}
			if(hasClockTime)
				clockTimeVector = StsLoader.checkConstructClockTimeVector(this, fileNameSet);
			setValueVectors(vectorList.toArray(new StsAbstractFloatVector[0]));
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "constructDataVectors", e);
			return false;
		}
	}

	StsVectorSetObject getVectorSetObject() { return vectorSetObject; }

	public boolean setVectorUnits(StsTimeVectorSetLoader loader)
	{
		return true;
	}

	public String[] removeNullColumnNames(String[] columnNames)
	{
		if(clockTimeVector != null)
			columnNames = (String[])StsMath.arrayAddElement(columnNames, clockTimeVector.name);
		return super.removeNullColumnNames(columnNames);
	}

/*
	public void setProjectObject(StsVectorSetObject vectorSetObject)
	{
		this.vectorSetObject = vectorSetObject;
	}

	public boolean checkVectors()
	{
		return true;
	}
*/
}
