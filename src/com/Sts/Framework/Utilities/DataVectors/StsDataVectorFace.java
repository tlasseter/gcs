package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 8/5/11
 * Time: 10:35 AM
 * To change this template use File | Settings | File Templates.
 */
public interface StsDataVectorFace
{
	public String getBinaryFilename();
	public String getBinaryPathname();
	public boolean hasBinaryFile();
	public boolean binaryFileDateOK();
	public boolean checkReadBinaryFile(boolean loadValues);
	public boolean checkWriteBinaryFile();
	public void appendTokens(String[] tokens);
	public void appendTokenTable(String[][] tokenTable, int nLines);
	public void trimToSize();
    public boolean checkSetMinMax();
	public void initialize(StsVectorSetLoader vectorSetLoader, boolean initializeValues);
	public boolean checkLoadVector();
	public byte[] getFileHeaderBytes();
	public int getNValueBytes();
	public boolean isPersistent();
	public void addToModel();
	public boolean isNull();
	public void deleteValues();
	public void clearValues();
}
