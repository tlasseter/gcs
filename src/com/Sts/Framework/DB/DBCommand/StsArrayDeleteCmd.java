package com.Sts.Framework.DB.DBCommand;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.lang.reflect.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

public class StsArrayDeleteCmd extends StsDBCommand
{
     /** parent object containing the array */
    private StsObject obj = null;
    /** field name of array in parent object */
    private String fieldName;
    /** index at which element is to be inserted */
    private int arrayIndex;
    /** reinitialize parent object after operation: obj.initialize(StsModel) */
    private boolean reinitialize = false;

	// These fields are only used in the case of the third constructor. Use with care! The change command object assumes
	// responsibility for setting and aborting the field change.
	private boolean allowRollback = false;

	public StsArrayDeleteCmd()
	{
	}

	public StsArrayDeleteCmd(StsObject parentObj, String fieldName, int arrayIndex, boolean reinitialize)
	{
		super();
		this.obj = parentObj;
		this.fieldName = fieldName;
        checkFieldName(parentObj, fieldName);
        this.arrayIndex = arrayIndex;
		this.reinitialize = reinitialize;
    }
/*
	public StsArrayDeleteCmd(StsObject parentObj, Object element, String fieldName)
	{
		super();
		this.obj = parentObj;
        this.arrayIndex = StsMath.arrayGetIndex(parentObj, element);
        this.fieldName = fieldName;
		reinitialize = false;
	}
*/
	public void abort() throws StsException
    {
//		if (allowRollback)
//			setField(oldElement);
	}

	public void write(StsDBOutputStream dbOutputStream) throws IOException
    {
        if (debug) debugMessageWrite();
        if(!obj.isPersistent()) return; // don't write if parent obj already deleted
        writeCmdClassIndex(dbOutputStream);
        StsDBTypeClass objDBType = (StsDBTypeClass)dbOutputStream.getOutputDBType(obj);
        dbOutputStream.writeInt(objDBType.getIndex());
		dbOutputStream.writeObject(obj, objDBType);
		int fieldNumber = objDBType.getIndexOfField(fieldName);
		dbOutputStream.writeInt(fieldNumber);
		dbOutputStream.writeInt(arrayIndex);
		dbOutputStream.writeBoolean(reinitialize);
	}

	// TODO if element is null, we have presumably added an element on the end of the array, so need to reduce its length by 1
	private void setField(Object element)
	{
		try
		{
			StsDBTypeStsClass dbClass = (StsDBTypeStsClass)StsObject.getCurrentModel().getDatabase().getCurrentDBType(obj);
			//Field field = obj.getClass().getField(fieldName);
			Field field = dbClass.getField(fieldName);
			if (field == null) return;
			field.setAccessible(true);
			Object fieldObj = field.get(obj);
			if (fieldObj == null) return;
			Object[] array = (Object[])fieldObj;
			array[arrayIndex] = element;
		}
		catch (Exception e)
		{
			StsException.outputException("StsChangeCmd::abort() failed. FieldName: " + fieldName,
												  e, StsException.WARNING);
		}
	}

	public void read(StsDBInputStream dbInputStream) throws IOException
	{
		try
		{
            int parentObjectTypeIndex = dbInputStream.readInt();
			StsDBTypeClass objDBType = (StsDBTypeClass)dbInputStream.getInputDBType(parentObjectTypeIndex);
			if (objDBType == null)
			{
				throw new RuntimeException("StsArrayDeleteCmd::read(StsDBInputStream) StsDBTypeObject is null, parentObjectTypeIndex = " + parentObjectTypeIndex);
			}
			obj = (StsObject)dbInputStream.readObject(objDBType);

			int fieldNumber = dbInputStream.readInt();
		    arrayIndex = dbInputStream.readInt();
			reinitialize = dbInputStream.readBoolean();
            
            Field field = objDBType.getField(fieldNumber);
			if (field == null) return;
			field.setAccessible(true);
			Object fieldObj = field.get(obj);
			if (fieldObj == null) return;
			fieldObj = StsMath.arrayDeleteElement(fieldObj, arrayIndex);
			field.set(obj, fieldObj);
            if(debug) debugMessageRead();
//			if (obj != null && reinitialize)
//				dbInputStream.addToObjects(obj);
		}
		catch (Exception e)
		{
			StsException.outputException("StsChangeCmd::read() failed. FieldName: " + fieldName, e, StsException.WARNING);
		}
	}

	public String toDebugString()
	{
		return toDebugString(obj) + "." + fieldName + "[" + arrayIndex + "]";
	}

	public byte getDBCommandClassIndex()
	{
		return StsDBCommand.ARRAY_DELETE_CMD_INDEX;
	}
}
