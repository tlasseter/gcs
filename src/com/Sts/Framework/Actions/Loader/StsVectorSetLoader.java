package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;

import java.lang.reflect.*;

/**
 * Copyright:  Copyright (c) 2012
 * Author: Tom Lasseter
 * Date: 1/5/12
 */
abstract public class StsVectorSetLoader extends StsLoader
{
	transient protected StsVectorSet vectorSet = null;
	transient protected StsVectorSetObject vectorSetObject;
	transient public int capacity = 1000;
	transient public int growInc = 1000;

	abstract protected Class getVectorSetClass();

	public StsVectorSetLoader(StsModel model)
	{
		super(model);
	}

	public StsVectorSetLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		this(model, null, deleteStsData, progressPanel);
	}

	public StsVectorSetLoader(StsModel model, String name, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, name, deleteStsData, isSourceData, progressPanel);
	}

	public StsVectorSetLoader(StsModel model, String name, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, name, wizard, progressPanel);
	}

	public StsVectorSetLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, progressPanel);
	}

	protected boolean initializeVectorSet()
	{
		vectorSet = vectorSetConstructor(this);
		return vectorSet != null;
	}

	static public StsVectorSet vectorSetConstructor(StsVectorSetLoader vectorSetLoader)
	{
		try
		{
			Class classType = vectorSetLoader.getVectorSetClass();
			Constructor constructor = classType.getDeclaredConstructor(new Class[] { StsVectorSetLoader.class } );
			return (StsVectorSet)constructor.newInstance(new Object[] { vectorSetLoader } );
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsVectorSetLoader.class, "getConstructor", "Failed for loader " + vectorSetLoader.getLoaderDebugName(), e);
			return null;
		}
	}
	/** standard file loader for concrete subclasses.
	 *
	 * @param file file to be read (either a source file or stsHeaderFile)
	 * @param vectorSetObject object being read or added to in this read operation
	 * @param loadValues true if vector data is to be loaded into memory or completion of processing
	 * @param isSourceData
	 * @return true if successfully processed
	 */
	public boolean processVectorFile(StsAbstractFile file, StsVectorSetObject vectorSetObject, boolean loadValues, boolean isSourceData)
	{
		setVectorSetObject(vectorSetObject);
		boolean stsHeaderFileOk = stsHeaderFileOk(file);
		if(isSourceData)
		{
			// reading source data; if this fails, try to read the stsFiles if they are all ok
			if(deleteStsData || !stsHeaderFileOk)
				return processSourceVectorFile(file, loadValues);

			StsAbstractFile stsHeaderFile = getMatchingAsciiFile(file, false);
			if(stsHeaderFile == null) return false;
			return processStsFile(stsHeaderFile, loadValues);
		}
		else
		{
			// reading sts files data; if this fails, try to read the source file; if this fails, we are toast: return false
			if(stsHeaderFileOk && processStsFile(file, loadValues))
				return true;
			String sourceURIString = vectorSetObject.getSourceURI();
			StsFile sourceFile = StsFile.constructor(sourceURIString);
			sourceFile.parseFilename();
			return processSourceVectorFile(sourceFile, loadValues);
		}
	}

	public boolean processSourceVectorFile(StsAbstractFile sourceFile, boolean loadValues)
	{
		try
		{
			if(sourceFile == null) return false;
			if(!readSourceFileHeader(sourceFile)) return false;
			initializeVersion(sourceFile);
			if(!initializeVectorSet()) return false;
			if(!vectorSet.processSourceVectors(this, sourceFile, loadValues)) return false;
			// remove names of null columns
			removeNullColumnNames();
			// the loader for this object contains fields to be written to header: write them out
			writeAsciiFile(sourceFile);
			vectorSet.writeBinaryFiles(this, sourceFile, loadValues);
			if(debug) StsException.systemDebug(this, "processSourceVectorFile", sourceFile.filename + " size " + sourceFile.length());
			return addVectorSetToObject();
		}
		catch(Exception e)
		{
			appendLine("Failed to process data from file (" + sourceFile.filename + "), check file contents.");
			setDescriptionAndLevel("Failed to process" + sourceFile.filename + ".", StsProgressBar.ERROR);
			StsException.outputWarningException(this, "processVectorFile", e);
			return false;
		}
	}

	private void initializeVersion(StsAbstractFile sourceFile)
	{
		version = 0;
		int currentVersion = getCurrentAsciiFileVersion(sourceFile);
		if(currentVersion != -1)
			version = currentVersion;
	}

	private void removeNullColumnNames()
	{
		columnNames = vectorSet.removeNullColumnNames(new String[0]);
	}

	/** Try reading the ascii stsHeader file; if unsuccessful, return false.
	 *  If read ok, get the sourceFile for this object and check the time-stamp against the stsHeader file.
	 *  If source file is newer or we can't read the binary vector files, return false (source file will then be reprocessed).
	 *  otherwise, load the vectors into memory (only the headers if loadValues is false), and add this vectorSet to the object.
	 *
	 * @param stsHeaderFile
	 * @param loadValues if true, load the data into the binary vectors; otherwise just initialize the binary header info.
	 * @return
	 */
	public boolean processStsFile(StsAbstractFile stsHeaderFile, boolean loadValues)
	{
		try
		{
			if(stsHeaderFile == null) return false;
			if(!readStsHeader(stsHeaderFile)) return false;
			// if we've successfully read the stsFileHeader try to read the binary vectors and return false if this fails
			if(!initializeVectorSet()) return false;
			if(!vectorSet.binaryFilesOk()) return false;
			if(!vectorSet.processStsVectors(this, stsHeaderFile, loadValues)) return false;
			if(!addVectorSetToObject()) return false;
			//if(!addToModelAndProject(vectorSetObject, true)) return false;
			return true;
		}
		catch(Exception e)
		{
			appendLine("Failed to process data from file (" + stsHeaderFile.filename + "), check file contents.");
			setDescriptionAndLevel("Failed to process" + stsHeaderFile.filename + ".", StsProgressBar.ERROR);

			StsException.outputWarningException(this, "processVectorFile", e);
			return false;
		}
	}

	public boolean addVectorSetToObject()
	{
		vectorSetObject.addVectorSetToObject(vectorSet);
		return true;
	}

	public void setVectorSet(StsVectorSet vectorSet) { this.vectorSet = vectorSet; }
	public StsVectorSet getVectorSet() { return vectorSet; }

	public StsVectorSetObject getVectorSetObject() { return vectorSetObject; }
	public void setVectorSetObject(StsVectorSetObject vectorSetObject) { this.vectorSetObject = vectorSetObject; }
}
