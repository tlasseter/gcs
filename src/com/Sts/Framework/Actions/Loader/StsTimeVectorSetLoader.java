package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 8/27/11
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsTimeVectorSetLoader extends StsVectorSetLoader
{
	transient public StsTimeVector clockTimeVector;

	public StsTimeVectorSetLoader(StsModel model)
	{
		super(model);
		initializeNameSet();
	}

	public StsMainObject getStsMainObject() { return getTimeVectorSetObject(); }

	public StsTimeVectorSetLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, deleteStsData, progressPanel);
		initializeNameSet();
	}

	public StsTimeVectorSetLoader(StsModel model, String name, StsProgressPanel progressPanel)
	{
		super(model, name, false, progressPanel);
		initializeNameSet();
	}

	public StsTimeVectorSetLoader(StsModel model, StsLoadWizard wizard, String name, StsProgressPanel progressPanel)
	{
		super(model, name, wizard, progressPanel);
		initializeNameSet();
	}

	public StsTimeVectorSetLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, progressPanel);
		initializeNameSet();
	}

	protected void initializeNameSet()
	{
		addClockTimeNames();
		acceptableNameSet.addAliases(clockColumnNameTIME);
	}

	public boolean processColumnNames()
	{
		if(columnNames == null) return false;
		int nColumnIndex = 0;
		for(String columnName : columnNames)
		{
			StsColumnName matchColumnName = acceptableNameSet.get(columnName);
			if(matchColumnName != null) // add this as an acceptable (standard) column name
			{
				matchColumnName = new StsColumnName(matchColumnName, nColumnIndex);
				nameSet.add(matchColumnName);
			}
			else if(curveNameOk(columnName)) // foundVector == null: add this as an ok but non-standard column name
			{
				matchColumnName = new StsColumnName(columnName, nColumnIndex);
				nameSet.add(matchColumnName);
			}
			nColumnIndex++;
		}
		return true;
	}

	public String getAsciiFilePathname()
	{
		return getTimeVectorSetObject().getAsciiDirectoryPathname();
	}

	public boolean loadFile(StsAbstractFile file, boolean loadValues, boolean addToProject, StsTimeVectorSetObject vectorSetObject, boolean isSourceData)
	{
		try
		{
			vectorSetObject.setDataSource(file);
			if(!processVectorFile(file, vectorSetObject, loadValues, isSourceData)) return false;
			if(!addToModelAndProject(vectorSetObject, addToProject)) return false;
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "loadSourceFile", e);
			return false;
		}
	}


	public boolean readFileHeader(StsAbstractFile file)
    {
		return readFileColumnHeaders(file);
	}

	public boolean readFileColumnHeaders(StsAbstractFile file)
    {
		String line = "";

        try
        {
			while(true)
			{
				if(!file.openReader()) return false;
				line = file.readLine().trim();
                // line = StsStringUtils.deTabString(line);
				String[] tokens = StsStringUtils.getTokens(line, tokenDelimiters);
				if(tokens == null || StsStringUtils.isAnyNumeric(tokens)) continue;
				return readSingleLineColumnNames(tokens);
			}
        }
        catch(Exception e)
        {
			StsMessageFiles.errorMessage(this, "readFileHeader", "failed reading line: " + line + "in file: " + file.filename);
            StsException.outputWarningException(this, "read", e);
			return false;
        }
    }

	public boolean changeSourceFile(StsAbstractFile file, boolean loadValues)
	{
		try
		{
			if(getTimeVectorSet() == null) return false;
			int nValues = getTimeVectorSet().getVectorsSize();
			if(nValues == 0) return false;

			StsTimeVector timeVector = getTimeVectorSet().getClockTimeVector();
			if(timeVector == null) return false;

			long lastTime = timeVector.getLast();

			// StsDataSource dataSource = projectObject.getDataSource();
			long newSourceSize = file.length();
			if(debug) StsException.systemDebug(this, "changedSourceFile", file.getPathname() + " size was: " + sourceSize + " now is: " + newSourceSize);
			if(newSourceSize <= sourceSize) return false;
			if(!constructMappedBufferScanner(file.getPathname())) return false;
			//if(!getCreateMappedBufferScanner()) return false;
			//if(!initializeMappedBufferScanner()) return false;
			// get the lastTime that we have in the well

			int nOldLines = getTimeVectorSet().getVectorsSize();

			StsMappedByteBufferScanner.Line line, prevLine, nextLine = null;
			// scan the file for the line which bounds is just after or bounds the last position in the file
			// if it bounds it, then the first new line might be this line, or the one before, or after
			line = mappedByteBufferScanner.scanForLine((int)sourceSize);
			if(line == null) return false;
			long fileStartPosition = -1;
			long fileTime = timeVector.computeValue(line.lineString);
			// if the fileTime is > than the last time and this line does not exactly start at last file position
			// search backwards for lines until we find one whose time is <= lastTime so nextLine is > lastTime
			if(fileTime > lastTime)
			{
				if(line.startsNewLines)
					nextLine = line;
				else
				{
					long prevFileTime = fileTime;
					prevLine = line;
					long nextFileTime = fileTime;
					nextLine = line;
					while(prevFileTime > lastTime)
					{
						nextLine = prevLine;
						nextFileTime = prevFileTime;
						prevLine = mappedByteBufferScanner.scanForPrevLine(nextLine);
						if(prevLine == null) break;
						prevFileTime = timeVector.computeValue(prevLine.lineString);
					}
					if(nextFileTime <= lastTime) return false;
				}
			}
			else if(fileTime <= lastTime)
			{
				long prevFileTime = fileTime;
				prevLine = line;
				long nextFileTime = fileTime;
				nextLine = line;
				while(nextFileTime <= lastTime)
				{
					prevLine = nextLine;
					prevFileTime = nextFileTime;
					nextLine = mappedByteBufferScanner.scanForNextLine(prevLine);
					if(nextLine == null) break;
					nextFileTime = timeVector.computeValue(nextLine.lineString);
				}
				if(nextFileTime <= lastTime) return false;
			}
			fileStartPosition = nextLine.fileLineStart;

			// this iterator returns the nextLine and every line after that
			ArrayList<StsMappedByteBufferScanner.Line> newLines = mappedByteBufferScanner.getNewLines(nextLine);
			String lineString = nextLine.lineString;
			// lineString = StsStringUtils.deTabString(lineString);
			String[] tokens = StsStringUtils.getTokens(lineString);
			int nTokens = tokens.length;
			int nNewLines = newLines.size();
			String[][] tokenTable = new String[nNewLines][nTokens];
			for(int n = 0; n < nNewLines; n++)
			{
				line = newLines.get(n);
				lineString = line.lineString;
				//lineString = StsStringUtils.deTabString(lineString);
				tokenTable[n] = StsStringUtils.getTokens(lineString);
			}
			// these are the concrete vectors which need to be filled from the file
			ArrayList<StsDataVectorFace> vectorList = getTimeVectorSet().getDataVectorArrayList();
			for(StsDataVectorFace dataVector : vectorList)
				dataVector.appendTokenTable(tokenTable, nNewLines);
			// these vectors need to have values cleared as they will be rebuilt on demand by the app
			getTimeVectorSet().checkClearTransientVectors();
			if(simulatorPanel != null)
				simulatorPanel.appendLine(file.filename + " " + nNewLines + " lines processed by " + StsToolkit.getSimpleClassname(this));

			if(!getTimeVectorSet().checkWriteBinaryFiles())
			{
				String message = "Failed to write binary file or files from file " + file.filename;
				appendLine(message);
				if(simulatorPanel != null)
					simulatorPanel.appendErrorLine(message + " by loader " + StsToolkit.getSimpleClassname(this));
				StsMessageFiles.errorMessage(message);
				return false;
			}

			getTimeVectorSet().processVectorSetChange(nOldLines);
			// model.win3dDisplayAll();
			sourceSize = newSourceSize;
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "changedSourceFile", e);
			return false;
		}
		finally
		{
			if(mappedByteBufferScanner != null)
				mappedByteBufferScanner.close();
			mappedByteBufferScanner = null;
		}
	}

	protected void processVectorSetChange(int firstIndex)
	{
	}

	public StsTimeVectorSet getTimeVectorSet() { return (StsTimeVectorSet)vectorSet; }

	public StsTimeVectorSetObject getTimeVectorSetObject() { return (StsTimeVectorSetObject)vectorSetObject; }
	public void setTimeVectorSetObject(StsTimeVectorSetObject timeVectorSetObject)
	{
		this.vectorSetObject = timeVectorSetObject;
	}

	public boolean addVectorSetToObject()
	{
		if(clockTimeVector != null) clockTimeVector.initializeTimeIndex();
		getTimeVectorSetObject().addVectorSetToObject(getTimeVectorSet());
		return true;
	}
}
