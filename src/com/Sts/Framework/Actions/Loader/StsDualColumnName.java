package com.Sts.Framework.Actions.Loader;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:04 AM
 *
 * Used for a value computed from two columns such as a long time value from time and date columns
 */
public class StsDualColumnName
{
	public String name;
	public String inputName;
	public StsColumnName columnName1;
	public StsColumnName columnName2;

	public StsDualColumnName(String inputName, String outputName, StsColumnName columnName1, StsColumnName columnName2)
	{
		this.inputName = inputName;
		this.name = outputName;
		this.columnName1 = columnName1;
		this.columnName2 = columnName2;
	}
}