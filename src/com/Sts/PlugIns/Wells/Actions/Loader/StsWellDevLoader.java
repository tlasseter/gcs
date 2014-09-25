package com.Sts.PlugIns.Wells.Actions.Loader;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsWellDevLoader extends StsTimeVectorSetLoader
{
	transient public StsWell well;

	/** A set of key-value pairs which define a set of alias names for a field name.
	 *  On reading a line in the file if one of the aliases is found in the first token,
	 *  the second token is set as the String value for the associated field.
	 *  On completion, the target object fields can then be set from these values.
	 */
	transient StsFieldKeyValueTreeMap fieldKeyValueMap = new StsFieldKeyValueTreeMap(StsWell.class);
	{
		fieldKeyValueMap.put(new StsFieldKeyValue("operator", new String[] { "operator" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("company", new String[] { "company" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("field", new String[] { "field" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("area", new String[] { "area" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("county", new String[] { "state" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("county", new String[] { "county" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("wellNumber", new String[] { "wellNumber" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("wellLabel", new String[] { "wellLabel" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("api", new String[] { "api" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("uwi", new String[] { "uwi" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("date", new String[] { "date" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("kbElev", new String[] { "kbElev" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("elev", new String[] { "elev" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("elevDatum", new String[] { "elevDatum" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("spudDate", new String[] { "spudDate" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("completionDate", new String[] { "completionDate" } ));
		fieldKeyValueMap.put(new StsFieldKeyValue("permitDate", new String[] { "permitDate" } ));
	}

	/** group the master file must belong to. */
	static private final String masterGroup = GROUP_WELL;
	/** subFiles must belong to one of these groups. */
	static private final String[] subFileGroups = new String[] { GROUP_WELL_LOGS, GROUP_WELL_TD, GROUP_WELL_REF, GROUP_WELL_PERF,GROUP_WELL_FMI, GROUP_WELL_EQUIP};

	static public StsFilenameFilter masterFileParser = new StsFilenameFilter(GROUP_WELL, FORMAT_TXT);
	static public StsFilenameFilter subFilesParser = new StsFilenameFilter(subFileGroups, new String[] { FORMAT_TXT } );
	static public StsFilenameFilter folderParser = new StsFilenameFilter(StsWell.getGroupSubFolderName(), FORMAT_TXT);

    public StsWellDevLoader(StsModel model)
	{
		super(model);
	}

	public StsWellDevLoader(StsModel model, String name)
    {
        super(model);
        this.name = name;
    }

    public StsWellDevLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
    {
        super(model, deleteStsData, progressPanel);
    }

	public StsWellDevLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, progressPanel);
	}

	public void initializeNameSet()
	{
		super.initializeNameSet();
       	acceptableNameSet.addAliases(xColumnName);
        acceptableNameSet.addAliases(yColumnName);
        acceptableNameSet.addAliases(depthColumnName);
        acceptableNameSet.addAliases(seismicTimeColumnName);
		acceptableNameSet.addAliases(mdepthColumnName);
	}

	protected Class getVectorSetClass()
	{
		return StsLineVectorSet.class;
	}

	public void setGroup()
	{
		group = GROUP_WELL;
	}

    public void setNullValue()
    {
        nullValue = project.getLogNull();
    }

	public boolean loadFile(StsAbstractFile file, boolean loadValues, boolean addToProject, boolean isSourceData)
	{
		try
		{
			well = new StsWell(file.name, false);
			setTimeVectorSetObject(well);

			if(!loadFile(file, loadValues, addToProject, well, isSourceData)) return false;
			// add subFiles for loading dependent objects
			addFolderSubFilesToMasterFile(file);
			// Process TD Curves --- Currently only supports S2S Format.
			checkLoadTdCurve(file, deleteStsData);
			// if clockTimeVector exists, set the vector index corresponding to current clock-time
			getTimeVectorSet().checkSetCurrentTime();
			// If origin  already set (checkSetOrigin returns true), then adjust the well.vectorSet X and Y vectors to local coordinates.
			// If origin not set, then we will set it, and will use vectorSet X and Y vectors as is.
			originInitialized = project.checkSetOrigin(xOrigin, yOrigin);
			if(!originInitialized) well.checkComputeRelativePoints();
			// Process Log Curves
			checkLoadLogCurves(file, deleteStsData);
			// process marker files
			checkLoadMarkers(file, deleteStsData, isSourceData);
			// return addToModel();
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "loadSourceFile", e);
			return false;
		}
	}

	private void checkLoadTdCurve(StsAbstractFile wellFile, boolean deleteStsData)
	{
		StsAbstractFile[] tdFiles = wellFile.getGroupSubFiles(GROUP_WELL_TD);
		if(tdFiles.length == 0) return;
		StsTdCurveLoader tdCurveLoader = new StsTdCurveLoader(model, well, deleteStsData, progressPanel);
		for(StsAbstractFile tdFile : tdFiles)
		{
			if(!well.hasLogCurveFileObject(tdFile))
				tdCurveLoader.processVectorFile(tdFile, well, false, isSourceData);
		}
	}

	private void checkLoadLogCurves(StsAbstractFile wellFile, boolean deleteStsData)
	{
		StsAbstractFile[] logFiles = wellFile.getGroupSubFiles(GROUP_WELL_LOGS);
		if(logFiles.length == 0) return;
		StsLogCurvesLoader logCurveLoader = new StsLogCurvesLoader(model, well, deleteStsData, progressPanel);
		logCurveLoader.processVectorFiles(logFiles, false);
	}

	private void checkLoadMarkers(StsAbstractFile wellFile, boolean deleteStsData, boolean isSourceData)
	{
		StsAbstractFile[] markerFiles;
		StsMarkerLoader markerLoader;

		markerFiles = wellFile.getGroupSubFiles(GROUP_WELL_REF);
		if(markerFiles.length > 0)
		{
			markerLoader = new StsMarkerLoader(model, wellFile.name, well, deleteStsData, isSourceData, progressPanel);
			markerLoader.processFiles(markerFiles);
		}
		markerFiles = wellFile.getGroupSubFiles(GROUP_WELL_PERF);
		if(markerFiles.length > 0)
		{
			markerLoader = new StsPerfMarkerLoader(model, wellFile.name, well, deleteStsData, isSourceData, progressPanel);
			markerLoader.processFiles(markerFiles);
		}

		markerFiles = wellFile.getGroupSubFiles(GROUP_WELL_FMI);
		if(markerFiles.length > 0)
		{
			markerLoader = new StsFmiMarkerLoader(model, wellFile.name, well, deleteStsData, isSourceData, progressPanel);
			markerLoader.processFiles(markerFiles);
		}

		markerFiles = wellFile.getGroupSubFiles(GROUP_WELL_EQUIP);
		if(markerFiles.length > 0)
		{
			markerLoader = new StsEquipmentMarkerLoader(model, wellFile.name, well, deleteStsData, isSourceData, progressPanel);
			markerLoader.processFiles(markerFiles);
		}
	}

	public boolean readFileHeader(StsAbstractFile file)
	{
		String line = "";
		nameSet.clear();
		try
		{
			while (true)
			{
				line = file.readLine();
				if(line == null) return false;
				line.trim();
				// line = StsStringUtils.deTabString(line);
				if(line.endsWith(WELLNAME))
				{
					String name = new String(file.readLine().trim());
				}
				else if(line.indexOf(ORIGIN) >= 0)  // is origin keyword there?
				{
					boolean xyOrder = true;
					if(line.indexOf(YX) >= 0) xyOrder = false;  // determine x-y order
					line = file.readLine().trim();  // get the next line

					// tokenize the x-y vector and convert to a point object
					String[] tokens = StsStringUtils.getTokens(line);
					if(tokens == null || tokens.length < 2) return false;
					xOrigin = Double.valueOf(tokens[0]).doubleValue();
					yOrigin = Double.valueOf(tokens[1]).doubleValue();

					if(!xyOrder)
					{
						double temp = xOrigin;
						xOrigin = yOrigin;
						yOrigin = temp;
					}
				}
				else if(line.endsWith(NULL_VALUE))
				{
					line = file.readLine().trim();  // get the next line
					StringTokenizer stok = new StringTokenizer(line);
					nullValue = Float.valueOf(stok.nextToken()).floatValue();
				}

				else if(line.endsWith(CURVE))
					return readMultiLineColumnNames(file, VALUE);
				else if(line.endsWith(VALUE)) // didn't find any curve names
				{
					StsMessageFiles.errorMessage(this, "readFileHeader", " Didn't find  any curve names in file: " + file.filename);
					return false;
				}
				else
				{
					String[] tokens = StsStringUtils.getTokens(line);
					if(tokens.length > 1)
					{
						StsFieldKeyValue fieldKeyValue = fieldKeyValueMap.get(tokens[0].toLowerCase());
						if(fieldKeyValue != null)
						{
							foundFieldKeyValues.add(fieldKeyValue);
							fieldKeyValue.fieldValue = tokens[1];
						}
						else
							progressPanel.appendErrorLine("File " + file.filename + " failed to process line: " + line);
					}
					else
						progressPanel.appendErrorLine("File " + file.filename + " failed to process line: " + line);

				}
			}
		}
		catch(Exception e)
		{
			StsMessageFiles.errorMessage(this, "readFileHeader", "failed reading line: " + line + "in file: " + file.filename);
			StsException.outputWarningException(this, "read", e);
			return false;
		}
	}

	static public StsAbstractFile constructMasterFileFromFolder(StsAbstractFile wellFolder)
	{
		StsAbstractFile masterFile = null;
		ArrayList<StsAbstractFile> subFiles = new ArrayList<StsAbstractFile>();
	 	File wellFolderFile	= wellFolder.getFile();
		File[] files = wellFolderFile.listFiles();
		if(files == null) return null;
		int nFiles = files.length;
		if(nFiles == 0) return null;
		StsFilenameFilter parser = StsFilenameFilter.FILENAME_FILTER;
		for(int n = 0; n < nFiles; n++)
		{
			StsAbstractFile abstractFile = StsFile.constructor(files[n].getPath());
			if(!parser.parseFile(abstractFile)) continue;
			String group = parser.group;
			if(group.equals(masterGroup))
			{
				if(masterFile != null)
				{
					StsMessageFiles.errorMessage(StsWellDevLoader.class, "constructMasterFileFromFolder", "More than one well file in folder: " + wellFolder.getPathname());
					return null;
				}
				masterFile = abstractFile;
			}
			else if(StsStringUtils.matchesStringInList(subFileGroups, group))
					subFiles.add(abstractFile);
		}
		if(masterFile == null) return null;
		masterFile.setSubFiles(subFiles.toArray(new StsAbstractFile[0]));
		return masterFile;
	}

	static public void addFolderSubFilesToMasterFile(StsAbstractFile masterFile)
	{
		if(masterFile == null) return;
		Path directoryPath = masterFile.getFile().toPath().getParent();
		StsAbstractFile wellFolder = StsFile.constructor(directoryPath);
	 	File wellFolderFile	= wellFolder.getFile();
		StsFilenameFilter subFileGroupsFilter = StsWell.getSubFileGroupsFilter();
		subFileGroupsFilter.setName(masterFile.name);
		String[] subFilenames = wellFolderFile.list(subFileGroupsFilter);
		if(subFilenames == null) return;
		int nSubFiles = subFilenames.length;
		if(nSubFiles == 0) return;
		ArrayList<StsAbstractFile> subFiles = new ArrayList<StsAbstractFile>();
		StsFilenameFilter parser = StsFilenameFilter.FILENAME_FILTER;
		Path wellFolderPath = wellFolderFile.toPath();
		for(String subFilename : subFilenames)
		{
			// resolve here combines the folder and filename paths
			Path subFilePath = wellFolderPath.resolve(Paths.get(subFilename));
			StsAbstractFile abstractFile = StsFile.constructor(subFilePath);
			if(!parser.parseFile(abstractFile)) continue;
			subFiles.add(abstractFile);
		}
		masterFile.setSubFiles(subFiles.toArray(new StsAbstractFile[0]));
	}

	static public void main(String[] args)
	{
		StsModel model = new StsModel();
		String name = "wellA";
		StsWellDevLoader wellLoader = new StsWellDevLoader(model, name);

		wellLoader.xOrigin = 1000;
		wellLoader.yOrigin = 2000;
		StsLineVectorSet lineVectorSet = new StsLineVectorSet();
		wellLoader.setVectorSet(lineVectorSet);
		lineVectorSet.addValueVector(new StsFloatDataVector(lineVectorSet, "GR"));
		lineVectorSet.addValueVector(new StsFloatDataVector(lineVectorSet, "NPHI"));
		String pathname = "C:/temp/WellHeader";
		try
		{
			StsParameterFile.writeObjectFields(pathname, wellLoader);
			name = "wellAcopy";
			StsWellDevLoader copyWellLoader = new StsWellDevLoader(model, name);
			StsFile file = StsFile.constructor(pathname);
			StsParameterFile.readObjectFields(file, copyWellLoader);
		}
		catch(Exception e)
		{
			StsException.outputWarningException(StsWellDevLoader.class, "main", e);
		}
	}
}