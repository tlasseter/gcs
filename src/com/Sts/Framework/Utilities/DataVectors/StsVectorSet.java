package com.Sts.Framework.Utilities.DataVectors;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.IO.FilenameFilters.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
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
public class StsVectorSet extends StsMainObject implements Cloneable, Serializable, Iterable<StsAbstractVector>
{
	protected StsVectorSetObject vectorSetObject;
	/** group this vectorSet belongs to; e.g., "well" */
	private String group;
	/** version number for this vectorSet; incremented from 0 for vector sets with group,name,format, and subname the same. */
	private int version;
	/** vectors of values or attributes; does not include x,y,z,t vectors */
	private StsAbstractFloatVector[] valueVectors;
	/** Number of value vectors */
	private int nValueVectors;

	/** indicates lineVectorSet has been fully initialized. */
    transient public boolean initializedVectorSet = false;
	/** Vectors which are not backed by files; they may be persisted, but values must be built from other vectors.
	 *  In real-time loading, the values in these vectors will be cleared.  If and when needed, the app constructs them.  */
	transient public ArrayList<StsFloatTransientValuesVector> transientValuesVectors;
	/** the relative path from the project directory to the ascii directory where the ascii header file is stored. */
	//transient String asciiDirPathname;
	/** the relative path from the project directory to the binary directory where the binary vectors of this vectorSet are stored. */
	transient String binaryDirPathname;
	/** an array of the vectorSet values */
	transient float[][] valueFloats;
/** names of valueVector properties */
    transient protected String[] propertyNames;
    /** number of properties */
    transient protected int nPropertyNames;
	/** the null float value used in defining null values in all the float vectors. */
    static public final float nullValue = StsParameters.nullValue;

	static public final int capacity = 1000;

	public StsVectorSet()
    {
    }

	public StsVectorSet(boolean persistent)
    {
		super(persistent);
    }
 /*
	static public StsVectorSet constructor(StsVectorSetLoader vectorSetLoader)
	{
		StsVectorSet vectorSet = new StsVectorSet(vectorSetLoader);
		//StsNameSet fileNameSet = vectorSetLoader.nameSet;
		//String name = vectorSetLoader.getStsMainObject().getName();
		if(!vectorSet.constructDataVectors(vectorSetLoader)) return null;
		return vectorSet;
	}
*/
/*
	public StsVectorSet(StsAbstractFile file, String group, String name)
	{
		this(file.getURIString(), file.lastModified(), null, group, name);
	}

	static public StsVectorSet constructor(StsProjectObjectFace projectObject, StsAbstractFile file, String group, String name, StsNameSet fileNameSet)
	{
		StsVectorSet vectorSet = new StsVectorSet(projectObject, file, group, name);
		if(!vectorSet.constructDataVectors(name, fileNameSet)) return null;
		return vectorSet;
	}

	public StsVectorSet(StsProjectObjectFace projectObject, StsAbstractFile file, String group, String name)
	{
		this(file.getURIString(), file.lastModified(), projectObject, group, name);
	}
*/
	public StsVectorSet(String sourceURI, long sourceCreateTime, String group, String name)
	{
		super(false);
		initializeDataSource();
		dataSource.setSourceURIString(sourceURI);
		dataSource.setSourceCreationTime(sourceCreateTime);
		this.group = group;
		this.name = name;
		setVersion();
		initialize();
	}

	public StsVectorSet(StsVectorSetLoader vectorSetLoader)
	{
		super(false);
		this.group = vectorSetLoader.group;
		this.name = vectorSetLoader.name;
		this.version = vectorSetLoader.version;
		constructDataVectors(vectorSetLoader);
	}

	public boolean initialize(StsModel model)
	{
		if(!initialize()) return false;
		return checkLoadBinaryFiles(true);
	}

	public boolean initialize()
	{
		return getVectorSetObject() != null;
	}

	public boolean checkLoadBinaryFiles(boolean loadValues)
	{
		if(!readBinaryFiles(loadValues)) return false;
		return checkVectors();
	}

	public boolean processSourceVectors(StsVectorSetLoader loader, StsAbstractFile sourceFile, boolean loadValues)
	{
		initializeVectors(loader, true);
		if(!readVectors(sourceFile)) return false;
		setVectorUnits(loader);
		// if(!assignVectors()) return false;
		return true;
	}

	public boolean writeBinaryFiles(StsVectorSetLoader loader, StsAbstractFile sourceFile, boolean loadValues)
	{
		if(!checkWriteBinaryFiles())
		{
			String message = "Failed to write binary file or files from file " + sourceFile.filename;
			if(loader.progressPanel != null) loader.progressPanel.appendLine(message);
			StsMessageFiles.errorMessage(message);
			return false;
		}
		if(!loadValues) clearValues();
		return true;
	}

	public void clearValues()
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
        for(StsDataVectorFace dataVector : dataVectors)
        	dataVector.clearValues();
	}

	/** if we can read the binaries ok, finish the vectors and return true. If we can't, then delete the binaries and return false. */
	public boolean processStsVectors(StsVectorSetLoader loader, StsAbstractFile stsHeaderFile, boolean loadValues)
	{
		initializeVectors(loader, false);
		if(readBinaryFiles(loadValues))
		{
			setVectorUnits(loader);
			return true;
		}
		else
		{
			deleteGroupBinaryFiles();
			String message = "Failed to write binary file or files from file " + stsHeaderFile.filename;
			if(loader.progressPanel != null) loader.progressPanel.appendLine(message);
			StsMessageFiles.errorMessage(message);
			return false;
		}
	}

	public boolean readVectors(StsAbstractFile file)
	{
		if(file.getReader() == null)
		{
			StsException.systemError(this, "readVectors", "Reader should still be open after reading header");
			return false;
		}

		try
		{
			return readVectorValues(file);
		}
		catch (Exception e)
		{
			StsMessageFiles.logMessage("Log curve read failed for well " + file.name);
			return false;
		}
		finally
		{
			file.closeReader();
			file.unlockReader();
		}
	}
/*
	protected boolean assignVectors()
	{
		setValueVectors(new StsAbstractFloatVector[this.getValueVectors().length]);
		int nValueVectors = 0;
		for(StsAbstractVector vector : this.getValueVectors())
		{
			if(vector == null) continue;
			if(!assignCoorVector(vector))
				getValueVectors()[nValueVectors++] = (StsAbstractFloatVector)vector;
		}
		setValueVectors((StsAbstractFloatVector[])StsMath.trimArray(getValueVectors(), nValueVectors));
		return true;
	}
*/
/*
	public void initializeValueVectors()
	{
		ArrayList<StsAbstractFloatVector> vectorArrayList = new ArrayList<StsAbstractFloatVector>();
		for(StsAbstractVector vector : vectors)
		{
			if(vector != null && !isFixedVector(vector.getType()))
				vectorArrayList.add((StsAbstractFloatVector)vector);
		}
		valueVectors =  vectorArrayList.toArray(new StsAbstractFloatVector[0]);
	}
*/
	public void addToModel()
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			if(!vector.isPersistent()) vector.addToModel();
		super.addToModel();
	}

	public boolean matchesParsedFilename(StsFilenameFilter parser)
	{
		if(!parser.getGroup().equals(group)) return false;
		if(!parser.getName().equals(name)) return false;
		return parser.getVersion() == version;
	}

	public String getBinaryFilename(String subname)
	{
		return StsLoader.constructBinaryFilename(group, name, subname, version);
	}

	public String getBinaryFilePathname(StsDataVectorFace dataVector)
	{
		return getBinaryDirectoryPathname() + (dataVector.getBinaryFilename());
	}

	public String getBinaryDirectoryPathname()
	{
		return getVectorSetObject().getBinaryDirectoryPathname();
	}

	public boolean hasBinaryFile(StsDataVectorFace dataVector)
	{
		String binaryFilePathname = getBinaryDirectoryPathname() + (dataVector.getBinaryFilename());
		File file = new File(binaryFilePathname);
		return file.exists();
	}

	public void deleteGroupBinaryFiles()
	{
		String binaryDirectory = getBinaryDirectoryPathname();
        String[] filenames = getGroupBinaryFiles(binaryDirectory);
		for(String filename : filenames)
		{
			File file = new File(binaryDirectory, filename);
			file.delete();
		}
	}

	public String[] getGroupBinaryFiles(String binaryDirectory)
	{
		StsFilenameFilter filenameFilter = new StsFilenameFilter(group, StsLoader.FORMAT_BIN, name);
		File directoryFile = new File(binaryDirectory);
		if(!directoryFile.exists()) return new String[0];
        return directoryFile.list(filenameFilter);
	}

	public String[] getGroupAsciiFiles(String asciiDirectory)
	{
		StsFilenameFilter filenameFilter = new StsFilenameFilter(group, StsLoader.FORMAT_TXT, name);
		File directoryFile = new File(asciiDirectory);
		if(!directoryFile.exists()) return new String[0];
        return directoryFile.list(filenameFilter);
	}

	private void setVersion()
	{
		String binaryDirectory = getBinaryDirectoryPathname();
        String[] filenames = getGroupBinaryFiles(binaryDirectory);
		int versionMax = -1;
		StsFilenameFilter parser = new StsFilenameFilter(group, StsLoader.FORMAT_BIN, name);
		for(String filename : filenames)
		{
			parser.parseCheckFilename(filename);
			versionMax = Math.max(versionMax, parser.version);
		}
		version = versionMax + 1;
	}

	public boolean checkAddVectors(boolean loadValues)
	{
		return true;
	}

	static public void copyPoint(float[][] points0, float[][] points1, int i0, int i1, int nVectors)
	{
		for(int n = 0; n < nVectors; n++)
			points1[n][i1] = points0[n][i0];
	}

	static public void getPointCoors(float[][] allVectorFloats, int index, int length, float[] coordinates)
	{
		for(int n = 0; n < length; n++)
			coordinates[n] = allVectorFloats[n][index];
	}

	static public void multByConstantAddPoint(float[] v1, float f, float[][] points, int index, int len)
	{
		float[] v = new float[len];
		for(int i = 0; i < len; i++)
			v[i] = points[i][index] * f + v1[i];
	}


    public void initializeDbVectors(int nVectors, int capacity)
    {
        StsFloatDbVector[] valueVectors = new StsFloatDbVector[nVectors];
        for(int n = 0; n < nVectors; n++)
            valueVectors[n] = new StsFloatDbVector(capacity);
		setValueVectors(valueVectors);
    }

    public boolean checkLoadVectors()
    {
		ArrayList<StsDataVectorFace> dataVectors = this.getDataVectorArrayList();
        for(StsDataVectorFace dataVector : dataVectors)
            if(!dataVector.checkLoadVector()) return false;
		return true;
    }

    public void addValueVector(StsFloatDataVector vector)
    {
        setValueVectors((StsAbstractFloatVector[]) StsMath.arrayAddElement(getValueVectors(), vector));
    }

    public void addValueVectorList(ArrayList<StsAbstractFloatVector> vectors)
    {
        setValueVectors((StsAbstractFloatVector[]) StsMath.arrayAddArray(getValueVectors(), vectors));
    }

	public boolean initializeVectors(StsVectorSetLoader loader, boolean initializeValues)
	{
		return true;
	}
/*
	protected boolean initializeVector(StsColumnName columnName, int columnIndexFlag)
	{
		StsException.systemError(this, "initializeVector", "Unable to handle columnIndexFlag " + columnIndexFlag + " for column " + columnName.name);
		return false;
	}
*/
	public void trimToSize()
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			vector.trimToSize();
	}

    public StsPoint getFirstValuePoint()
    {
        return getValuePoint(0);
    }

    public StsPoint getLastValuePoint()
    {
        return getValuePoint(getVectorsSize() - 1);
    }

    public StsPoint getValuePoint(int i)
    {
        float[] v = getValueFloats(i);
        return new StsPoint(v);
    }

    public float[] getValueFloats(int i)
    {
        float[] v = new float[nValueVectors];
        for(int n = 0; n < nValueVectors; n++)
		{
			if(valueVectors[n] != null)
            	v[n] = valueVectors[n].getValue(i);
			else
				v[n] = StsParameters.nullValue;
		}
        return v;
    }

    public StsPoint[] getValuesAsPoints()
    {
		int nPoints = getVectorsSize();
        StsPoint[] points = new StsPoint[nPoints];
        for(int i = 0; i < nPoints; i++)
            points[i] = getValuePoint(i);
        return points;
    }

    public void deleteValuePoint(int index)
    {
        for(int n = 0; n < nValueVectors; n++)
            valueVectors[n].deletePoint(index);
    }

    public int insertValuePointBefore(StsPoint point, int index)
    {
        return insertValuePointBefore(point.v, index);
    }

    public int insertValuePointBefore(float[] point, int index)
    {
        for(int n = 0; n < nValueVectors; n++)
            valueVectors[n].insertBefore(point[n], index);
        return index;
    }

    public int insertValuePointAfter(StsPoint point, int index)
    {
        return insertValuePointAfter(point.v, index);
    }

    public int insertValuePointAfter(float[] point, int index)
    {
        for(int n = 0; n < nValueVectors; n++)
            valueVectors[n].insertAfter(point[n], index);
        return index + 1;

    }

    public float[][] getValueFloats()
    {
		if(valueVectors == null) return null;
		int length = valueVectors[0].size;
		if(valueFloats != null && valueFloats[0].length == length) return valueFloats;
        valueFloats = new float[nValueVectors][];
        for(int n = 0; n < nValueVectors; n++)
            valueFloats[n] = getVectorFloats(n);
        return valueFloats;
    }

	private boolean checkValueFloats()
	{
		if(valueFloats == null) return false;
		int length = valueVectors[0].size;
		for(int n = 1; n < nValueVectors; n++)
			if(valueVectors[n].size < length) return false;
		return true;
	}

    public void getValueFloats(float[] coordinates, int index, int length)
    {
        for(int n = 0; n < length; n++)
            coordinates[n] = valueVectors[n].getValue(index);
    }

    public boolean checkSortBy(int type)
    {
        StsAbstractVector floatVector = this.getValueVectors()[type];
        if(floatVector.isMonotonicIncreasing()) return true;
        float[] floats = this.getValueVectors()[type].getValues();
        int[] sortIndex = StsMath.getSortedFloatsIndexList(floats);
        for(StsAbstractFloatVector vector : this.getValueVectors())
            vector.resortWithIndex(sortIndex);
        floatVector.setMonotonic(StsAbstractVector.MONOTONIC_INCR);
        return true;
    }

    public StsPoint computeInterpolatedValuesPoint(float indexF)
    {
        float[] v = computeInterpolatedFloatValues(indexF);
        return new StsPoint(v);
    }

	public StsPoint computeInterpolatedValuesPoint(StsAbstractFloatVector.IndexF indexF)
	{
		float[] v = computeInterpolatedValueFloats(indexF);
		if(v == null) return null;
		return new StsPoint(v);
	}

	static public StsPoint computeInterpolatedPoint(StsAbstractFloatVector[] vectors, StsAbstractFloatVector.IndexF indexF)
    {
        float[] v = computeInterpolatedVectorFloats(vectors, indexF);
		if(v == null) return null;
        return new StsPoint(v);
    }

    static public StsPoint computeInterpolatedPoint(StsAbstractFloatVector[] vectors, int index)
    {
        float[] v = computeInterpolatedVectorFloats(vectors, index);
		if(v == null) return null;
        return new StsPoint(v);
    }

    public float[] computeInterpolatedFloatValues(float indexF)
    {
        float[][] floats = getValueFloats();
        return computeInterpolatedFloatsStatic(floats, indexF);
    }

	public float[] computeInterpolatedValueFloats(StsAbstractFloatVector.IndexF indexF)
	{
		return computeInterpolatedVectorFloats(getValueVectors(), indexF);
	}

	public StsPoint computeInterpolatedValuePoint(StsAbstractFloatVector.IndexF indexF)
	{
		return new StsPoint(computeInterpolatedVectorFloats(getValueVectors(), indexF));
	}

    static public float[] computeInterpolatedVectorFloats(StsAbstractFloatVector[] vectors, StsAbstractFloatVector.IndexF indexF)
    {
		if(indexF == null || indexF.f == nullValue) return null;
		int nVectors = vectors.length;

        float[][] floats = new float[nVectors][];
		for(int n = 0; n < nVectors; n++)
		{
			StsAbstractFloatVector vector = vectors[n];
			if(vector == null) continue;
			floats[n] = vector.getValues();
		}
        return computeInterpolatedFloats(floats, indexF);
    }

    static public float[] computeInterpolatedVectorFloats(StsAbstractFloatVector[] vectors, int index)
    {
		int nVectors = vectors.length;

        float[] floats = new float[nVectors];
		for(int n = 0; n < nVectors; n++)
		{
			if(vectors[n] != null)
				floats[n] = vectors[n].getValue(index);
			else
				floats[n] = StsParameters.nullValue;
		}
		return floats;
    }

    static public float[] computeInterpolatedVectors(StsAbstractFloatVector[] vectors, int inputIndex, float value, boolean extrapolate)
    {
		StsAbstractFloatVector inputVector = vectors[inputIndex];
		if(inputVector == null) return null;
		StsAbstractFloatVector.IndexF indexF = inputVector.getIndexF(value, extrapolate);
		if(indexF == null || indexF.f == nullValue) return null;
		int nVectors = vectors.length;

        float[][] floats = new float[nVectors][];
		for(int n = 0; n < nVectors; n++)
            if(vectors[n] != null)
			    floats[n] = vectors[n].getValues();

        return computeInterpolatedFloats(floats, indexF);
    }

    static public float[] computeInterpolatedFloatsStatic(float[][] floats, float indexF)
    {
        int nVectors = floats.length;
        float[] v = new float[nVectors];
        int index = StsMath.floor(indexF);
        float f = indexF - index;
        if(f == 0.0f)
        {
            for(int n = 0; n < nVectors; n++)
                v[n] = floats[n][index];
            return v;
        }
        else
        {
            for(int n = 0; n < nVectors; n++)
            {
                float v0 = floats[n][index];
                float v1 = floats[n][index + 1];
                v[n] = v0 + f * (v1 - v0);
            }
        }
        return v;
    }

    static public float[] computeInterpolatedFloats(float[][] floats, StsAbstractFloatVector.IndexF indexF)
    {
        int nVectors = floats.length;
        float[] v = new float[nVectors];
        int index = indexF.index;
        float f = indexF.f;
		if(f == 0.0f)
		{
			for(int n = 0; n < nVectors; n++)
			{
				float[] values = floats[n];
				if(values == null)
					v[n] = nullValue;
				else
				{
					int length = values.length;
					if(index >= length)
						return null;
					if(values == null) continue;
					v[n] = values[index];
				}
			}
		}
		else
		{
			for(int n = 0; n < nVectors; n++)
			{
				float[] values = floats[n];
				if(values == null)
					v[n] = nullValue;
				else
				{
					int length = values.length;
					if(index >= length-1)
						return null;
					float v0 = values[index];
					float v1 = values[index + 1];
					v[n] = v0 + f * (v1 - v0);
				}
			}
		}
        return v;
    }

    static public float[] computeInterpolatedFloatsStatic(float[][] floats, int nValues, int index, float f)
    {
        int nVectors = floats.length;
        float[] v = new float[nVectors];
        if(f == 0.0f)
        {
            for(int n = 0; n < nVectors; n++)
                v[n] = floats[n][index];
            return v;
        }
        else if(f == 1.0f && index < nValues - 1)
        {
            index++;
            for(int n = 0; n < nVectors; n++)
                v[n] = floats[n][index];
            return v;
        }
        else
        {
            for(int n = 0; n < nVectors; n++)
            {
                float v0 = floats[n][index];
                float v1 = floats[n][index + 1];
                v[n] = v0 + f * (v1 - v0);
            }
        }
        return v;
    }

    static public float[] subPoints(StsAbstractFloatVector[] vectors, int index1, int index0)
    {
		int nVectors = vectors.length;
        float[] point = new float[nVectors];
        for(int n = 0; n < nVectors; n++)
            point[n] = vectors[n].getValue(index1) - vectors[n].getValue(index0);
        return point;
    }

    static public float[] subPoints(int index1, int index0, float[][] allVectorFloats, int nVectors)
    {
        float[] point = new float[nVectors];
        for(int n = 0; n < nVectors; n++)
            point[n] = allVectorFloats[n][index1] - allVectorFloats[n][index0];
        return point;
    }

    public int getVectorsSize()
    {
		if(valueVectors == null) return 0;
        if(valueVectors[0] == null) return 0;
        return valueVectors[0].size;
    }

	//TODO rewrite this to use mapped byte buffers
    public boolean checkWriteBinaryFiles()
    {
		String binaryDirPathname = getBinaryDirectoryPathname();
		StsFile.checkDirectory(binaryDirPathname);
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
        boolean ok = true;
        for(StsDataVectorFace dataVector : dataVectors)
        	if(!dataVector.checkWriteBinaryFile()) ok = false;
        return ok;
	}

	public void checkClearTransientVectors()
	{
		if(transientValuesVectors == null) return;
		for(StsFloatTransientValuesVector vector : transientValuesVectors)
			vector.clearValues();
	}

	public float getValueAtValue(float value, int inputType, int outputType, boolean extrapolate)
	{
		StsAbstractFloatVector inputVector = this.getValueVectors()[inputType];
		StsAbstractFloatVector outputVector = this.getValueVectors()[outputType];
		StsAbstractFloatVector.IndexF indexF = inputVector.getIndexF(value, extrapolate);
		return outputVector.getValue(indexF);
	}

	public float getValueAtValue(float value, StsAbstractFloatVector inputVector, StsAbstractFloatVector outputVector, boolean extrapolate)
	{
		StsAbstractFloatVector.IndexF indexF = inputVector.getIndexF(value, extrapolate);
		return outputVector.getValue(indexF);
	}

	public float[] multByConstantAddPoint(int index, float[] v, float f, StsAbstractFloatVector[] vectors)
	{
		float[] vsum = new float[nValueVectors];
		for(int i = 0; i < nValueVectors; i++)
			vsum[i] =  getValue(i, index) + f*v[i];
		return vsum;
	}

	public Iterator<StsAbstractVector> iterator()
	{
		ArrayList<StsAbstractVector> abstractVectors = new ArrayList<StsAbstractVector>();
		abstractVectors.addAll(Arrays.asList(this.getValueVectors()));
		return abstractVectors.iterator();
	}
/*
	public boolean appendTokens(String[] tokens)
	{
		for(StsAbstractFloatVector vector : this.getValueVectors())
			if(vector != null && vector instanceof StsFloatDataVector)
				vector.appendTokens(tokens);
		return true;
	}
*/
	public StsDataVectorFace[] getDataVectors()
	{
		ArrayList<StsDataVectorFace> dataVectorArrayList = getDataVectorArrayList();
		return dataVectorArrayList.toArray(new StsDataVectorFace[0]);
	}

	public ArrayList<StsDataVectorFace> getDataVectorArrayList()
	{
		ArrayList<StsDataVectorFace> dataVectors = new ArrayList<StsDataVectorFace>();
		for(StsAbstractFloatVector vector : valueVectors)
			if(vector != null && vector instanceof StsFloatDataVector)
				dataVectors.add((StsDataVectorFace)vector);
		return dataVectors;
	}

	public boolean binaryFilesOk()
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			if(!vector.hasBinaryFile() || !vector.binaryFileDateOK())
				return false;
		return true;
	}

	public boolean readBinaryFiles(boolean loadValues)
	{
		ArrayList<StsDataVectorFace> dataVectors = getDataVectorArrayList();
		for(StsDataVectorFace vector : dataVectors)
			if(!vector.checkReadBinaryFile(loadValues)) return false;
		return true;
	}

	public boolean checkReadAdditionalVectors(boolean loadValues)
	{
		return true;
	}

	public StsAbstractFloatVector getVectorOfType(int type)
	{
		if(this.getValueVectors() == null) return null;
		if(type < 0 || type >= this.getValueVectors().length) return null;
		return this.getValueVectors()[type];
	}

	public boolean readVectorValues(StsAbstractFile file)
	{
		try
		{
			// delete these binaries (if they exist), read the source files and rewrite the binaries
			deleteGroupBinaryFiles();
			return readAsciiCurveValues(file);
		}
		catch (Exception e)
		{
			StsException.outputException("StsWellKeywordIO.readVectorValues() failed.", e, StsException.WARNING);
			return false;
		}
	}
/*
	private boolean checkSetBinaryFileVectors()
	{                         && checkReadBinaryFiles(loadValues)
		for(Sts)
			if(!vector.checkReadBinaryFile(loadValues)) return false;
		return true;
	}
*/
    public boolean readAsciiCurveValues(StsAbstractFile file)
	{
		try
		{
			//if(!file.openReader()) return false;
		    String line;
			int nLines = 0;
			int badLineCount = 0;
			StsDataVectorFace[] dataVectors = getDataVectors();
			int nValuesRequired = dataVectors.length;
			// skip the header
			//if(!skipHeader(file)) return false;
            while ((line = file.readLine()) != null)
            {
                //line = line.trim();
                if (line.equals("")) continue;  // blank line
                // line = StsStringUtils.deTabString(line);
				String[] tokens = StsStringUtils.getTokens(line, StsLoader.tokenDelimiters);
                int nTokens = tokens.length;
				if(nTokens < nValuesRequired)
				{
					badLineCount++;
					if(badLineCount < 10)
					{
						StsMessageFiles.errorMessage("Skipping bad line. Needed " + nValuesRequired + " values but found only " + nTokens + " for line " + line);
						continue;
					}
					else
					{
						StsMessageFiles.errorMessage("Too may bad lines.  Giving up.");
						return false;
					}
				}
				for(StsDataVectorFace dataVector : dataVectors)
					dataVector.appendTokens(tokens);
				if(++nLines%1000 == 0) StsMessageFiles.logMessage("File: " + file.filename + ": " + nLines + " lines read.");
            }

            // complete load of vectors: null out any valueVectors whose values are all null and trim to size
			// xyztVectors are handled separately

			for(int n = 0; n < nValueVectors; n++)
			{
				StsAbstractFloatVector vector = valueVectors[n];
				if(vector == null) continue;
				if(vector.isNull()) valueVectors[n] = null;
            }

			for(StsDataVectorFace dataVector : dataVectors)
				dataVector.trimToSize();

            return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsVectorSet.class, "readAsciiCurveValues", e);
            return false;
        }
    }

	protected boolean skipHeader(StsAbstractFile file)
	{
		return true;
	}

	public boolean constructDataVectors(StsVectorSetLoader vectorSetLoader)
	{
		StsNameSet fileNameSet = vectorSetLoader.nameSet;
		String name = vectorSetLoader.name;
		return initializeDataVectors(name, fileNameSet);
	}

	public boolean initializeDataVectors(String name, StsNameSet fileNameSet)
	{
		try
		{
			ArrayList<StsAbstractFloatVector> vectorList = new ArrayList<StsAbstractFloatVector>();

			for(StsColumnName columnName : fileNameSet)
			{
				if(StsLoader.isTimeOrDateColumn(columnName))
				{
					StsException.systemError(this, "initializeDataVectors", "Vectors include a clockTimeColumn; should be built with StsTimeVectorSet.");
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
			setValueVectors(vectorList.toArray(new StsAbstractFloatVector[0]));
			return true;
		}
		catch(Exception e)
		{
			StsException.outputWarningException(this, "initializeDataVectors", e);
			return false;
		}
	}

	/** unique version number which separates this vectorSet from others with the same group, name, subname. */ /** version number for this vectorSet; incremented from 0 for vector sets with group,name,format, and subname the same. */ /** version number for this group; used differentiate separate vectorSets with the same group.name.subname. */
	public int getVersion()
	{
		return version;
	}
/*
	public void setValueVectors(StsAbstractFloatVector[] valueVectors)
	{
		this.valueVectors = valueVectors;
		nVectors = valueVectors.length;
	}
*/
	/** all vectors (other than clock-time are floatVectors with double origin */ /** a set of float vectors all of which must be the same length.  a point[i] is defined by the set of values in all vectors at index i.
	 *  Concrete subclasses define the type. StsFloatVector: transient; StsFloatDbVector: saved in DB;
	 *  StsFloatDataVector: saved to disk binary file. */

/* 	public StsAbstractFloatVector[] getValueVectors()
	{
		return this.valueVectors;
	}
*/
    public String toString()
    {
        return group + ".bin." + name + "." + version;
    }

	public float[] getVectorFloats(int type)
	{
		if(this.getValueVectors()[type] == null) return null;
		return this.getValueVectors()[type].getValues();
	}

	public float getValue(int type, int index)
	{
		if(this.getValueVectors()[type] == null) return nullValue;
		return this.getValueVectors()[type].getValue(index);
	}

	public boolean isDbVectorSet()
    {
        if(this.getValueVectors() == null) return false;
        return this.getValueVectors()[0] instanceof StsFloatDbVector;
    }

	/** group name associated with these vectors, such as "well_dev". */
	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public boolean isNameAndVersion(String name, int version)
	{
		return this.name.equals(name) && this.version == version;
	}

	public void processVectorSetChange(int firstIndex)
	{
	}

	public void addFloatTransientValuesVector(StsFloatTransientValuesVector vector)
	{
		if(transientValuesVectors == null)
			transientValuesVectors = new ArrayList<StsFloatTransientValuesVector>();
		transientValuesVectors.add(vector);
	}

	StsVectorSetObject getVectorSetObject() { return vectorSetObject; }

	public boolean setVectorUnits(StsVectorSetLoader loader)
	{
		return true;
	}

	public void setProjectObject(StsVectorSetObject vectorSetObject)
	{
		this.setVectorSetObject(vectorSetObject);
	}

	public int getNValues()
	{
		if(valueVectors == null || valueVectors.length == 0) return 0;
		return valueVectors[0].getSize();
	}

	protected boolean assignCoorVector(StsAbstractVector vector)
	{
		return true;
	}

	public boolean checkVectors()
	{
		return true;
	}

	public int getNValueVectors()
	{
		return nValueVectors;
	}


    public String[] getPropertyNames()
    {
		if(propertyNames != null) return propertyNames;
        nPropertyNames = getValueVectors().length;
        propertyNames = new String[nPropertyNames];
        for(int n = 0; n < nPropertyNames; n++)
            propertyNames[n] = getValueVectors()[n].getName();
		return propertyNames;
    }

	public StsAbstractFloatVector getValueVector(int index)
	{
		if(index < 0 || index >= nValueVectors) return null;
		return getValueVectors()[index];
	}

	public StsAbstractFloatVector getValueVector(String name)
    {
		for(StsAbstractFloatVector vector : valueVectors)
			if(vector.name.equals(name)) return vector;
		return null;
    }

	public StsAbstractFloatVector[] getValueVectors()
	{
		return valueVectors;
	}

	public void setValueVectors(StsAbstractFloatVector[] valueVectors)
	{
		this.valueVectors = valueVectors;
		nValueVectors = valueVectors.length;
	}

	public boolean hasValueVectorNamed(String name)
	{
		for(Object valueVector : valueVectors)
			if(((StsAbstractVector)valueVector).getName().equals(name)) return true;
		return false;
	}

	public String[] removeNullColumnNames(String[] columnNames)
	{
		if(valueVectors != null)
		{
			int nNonNull = 0;
			int nValueVectors = valueVectors.length;
			// some of the coorVectors might be null
			String[] nonNullVectorNames = new String[nValueVectors];
			for(int n = 0; n < nValueVectors; n++)
				if(valueVectors[n] != null) nonNullVectorNames[nNonNull++] = valueVectors[n].name;
			columnNames = (String[])StsMath.arrayAddArray(columnNames, nonNullVectorNames, nNonNull);
		}
		return columnNames;
	}

    public void setVectorSetObject(StsVectorSetObject vectorSetObject) {
        this.vectorSetObject = vectorSetObject;
    }
}
