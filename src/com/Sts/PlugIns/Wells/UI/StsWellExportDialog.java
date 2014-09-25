package com.Sts.PlugIns.Wells.UI;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.DataVectors.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Model.DBTypes.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;
import com.Sts.PlugIns.Surfaces.DBTypes.*;
import com.Sts.PlugIns.Wells.DBTypes.*;

import javax.swing.*;
import javax.xml.bind.*;
import java.awt.*;
import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class StsWellExportDialog extends JDialog
{
    StsModel model;
    StsExportedWell well = null;
    StsWell exportWell;
    boolean logsData = false;
    boolean deviationData = true;
    boolean seisAttsData = false;
    float minMDepth;
	float maxMDepth;
    float sampleRate;
    boolean resample = false;
	String exportName;

	private boolean cloneIt = true;
	private int numberRows = 25;
	private int numberCols = 40;
	private float xCloneOffset = 10000.0f;
	private float yCloneOffset = 10000.0f;

	StsJPanel panel = StsJPanel.addInsets();
	StsGroupBox selectBox;
	StsStringFieldBean nameBean;
	StsBooleanFieldBean logsDataBean;
	StsBooleanFieldBean deviationDataBean;
	StsBooleanFieldBean seisAttsDataBean;

    StsGroupBox rangeBox;
	StsFloatFieldBean minMDepthBean;
	StsFloatFieldBean maxMDepthBean;
    StsBooleanFieldBean resampleBean;
    StsFloatFieldBean sampleRateBean;

	StsGroupBox cloneBox;
	StsBooleanFieldBean cloneItBean;
	StsIntFieldBean numberRowsBean;
	StsIntFieldBean numberColsBean;
	StsFloatFieldBean xOffsetBean;
	StsFloatFieldBean yOffsetBean;

	StsJPanel buttonPanel = StsJPanel.addInsets();
	StsButton processButton = new StsButton("Process", "Export selected curves of selected well.", this, "process");
	StsButton cancelButton = new StsButton("Cancel", "Cancel this operation.", this, "cancel");

	public byte cloneGroup = CLONE_GROUP_ALL;

    public final static byte PROCESS = 0;
    public final static byte CANCELED = 1;
    byte mode = PROCESS;

	static public final byte CLONE_GROUP_BY_NUMBER = 0;
	static public final byte CLONE_GROUP_BY_WELL = 1;
	static public final byte CLONE_GROUP_ALL = 2;

    protected StsWellExportDialog(StsModel model, Frame frame, String title, boolean modal, StsWell well, String timeOrDepth)
    {
        super(frame, title, modal);
        this.model = model;
        this.well = new StsExportedWell(well);
        exportName = well.getName();
        constructBeans();
        constructPanel();
    }

    protected void constructBeans()
    {
        selectBox = new StsGroupBox("Export curves");
        nameBean = new StsStringFieldBean(this, "exportName", "Exported well name");
        logsDataBean = new StsBooleanFieldBean(this, "logsData", "Log Data");
        deviationDataBean = new StsBooleanFieldBean(this, "deviationData", "Dev Curve");
        seisAttsDataBean = new StsBooleanFieldBean(this, "seisAttsData", "Seismic Attributes");

        rangeBox = new StsGroupBox("Export mdepth range");
        minMDepthBean = new StsFloatFieldBean(this, "minMDepth", "Min MDepth");
        maxMDepthBean = new StsFloatFieldBean(this, "maxMDepth", "Max MDepth");

        resampleBean = new StsBooleanFieldBean(this, "resample", "Resample Output");
        sampleRateBean = new StsFloatFieldBean(this, "sampleRate", "Samples Per Foot");

		cloneBox = new StsGroupBox("Clone well");
		cloneItBean = new StsBooleanFieldBean(this, "cloneIt", "Clone Well");
		numberRowsBean = new StsIntFieldBean(this, "numberRows", true, "Num Rows");
		numberColsBean = new StsIntFieldBean(this, "numberCols", true, "Num Cols");
		xOffsetBean = new StsFloatFieldBean(this, "xCloneOffset", "X Clone Offset");
	 	yOffsetBean = new StsFloatFieldBean(this, "yCloneOffset", "T Clone Offset");
    }

    static public boolean exportWell(StsModel model, Frame frame, String title, boolean modal, StsWell well, String timeOrDepth)
    {
        try
        {
            StsWellExportDialog dialog = new StsWellExportDialog(model, frame, title, modal, well, timeOrDepth);
            dialog.setSize(200, 400);
            dialog.pack();
            dialog.setVisible(true);
            dialog.exportWell(timeOrDepth);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(StsWellExportDialog.class, "constructor", e);
            return false;
        }
    }

    protected void constructPanel()
    {

	    this.getContentPane().add(panel);
		this.setTitle("Well Export Parameters");
		panel.add(selectBox);
        panel.add(rangeBox);
		panel.add(cloneBox);
		panel.add(buttonPanel);

		selectBox.add(nameBean);
		selectBox.addToRow(logsDataBean);
		selectBox.addToRow(deviationDataBean);
		selectBox.addEndRow(seisAttsDataBean);

        rangeBox.addToRow(minMDepthBean);
		rangeBox.addEndRow(maxMDepthBean);

        rangeBox.addToRow(resampleBean);
        rangeBox.addEndRow(sampleRateBean);

		cloneBox.addEndRow(cloneItBean);
		cloneBox.addToRow(numberRowsBean);
		cloneBox.addEndRow(numberColsBean);
		cloneBox.addToRow(xOffsetBean);
	 	cloneBox.addEndRow(yOffsetBean);

	    buttonPanel.addToRow(processButton);
		buttonPanel.addEndRow(cancelButton);

	    minMDepthBean.setValue(0.0f);
        minMDepth = 0.0f;
		maxMDepth = well.getMaxMDepth();
		maxMDepthBean.setValue(maxMDepth);
        sampleRate = (maxMDepth - minMDepth)/well.getLineVectorSet().getSize();
        sampleRateBean.setValue(sampleRate);
        sampleRateBean.setRange(0.5, sampleRate);
        sampleRateBean.setEditable(false);
    }

	public void setExportName(String name)
    {
        exportName = StsStringUtils.cleanString(name);
    }
	public String getExportName() { return exportName; }

	public void setResample(boolean resample)
    {
        this.resample = resample;
        sampleRateBean.setEditable(resample);
    }
	public boolean getResample() { return resample; }

	public void setLogsData(boolean logsData) { this.logsData = logsData; }
	public boolean getLogsData() { return logsData; }

	public void setDeviationData(boolean deviationData) { this.deviationData = deviationData; }
	public boolean getDeviationData() { return deviationData; }

	public void setSeisAttsData(boolean seisAttsData) { this.seisAttsData = seisAttsData; }
	public boolean getSeisAttsData() { return seisAttsData; }

	public void setMinMDepth(float  minMDepth) { this.minMDepth = minMDepth; }
	public float getMinMDepth() { return this.minMDepth; }

	public void setMaxMDepth(float  maxMDepth) { this.maxMDepth = maxMDepth; }
	public float getMaxMDepth() { return this.maxMDepth; }

	public void setSampleRate(float sampleRate) { this.sampleRate = sampleRate; }
	public float getSampleRate() { return this.sampleRate; }

    public void process()
	{
		mode = PROCESS;
        setVisible(false);
	}

	public void cancel()
	{
		mode = CANCELED;
		setVisible(false);
 	}

    public byte getMode() { return mode; }

    public boolean exportWellXML(String timeOrDepth)
    {
        if (getMode() == CANCELED) return false;

        try
        {
            StsProject project = model.getProject();
            String pathname = project.getProjectDirString() + "well-dev.xml." + exportName;
            File file = new File(pathname);
            if (file.exists())
            {
                boolean overWrite = StsYesNoDialog.questionValue(model.win3d,
                    "File " + pathname + " already exists. Do you wish to overwrite it?");
                if (!overWrite) return false;
            }
            JAXBContext context = JAXBContext.newInstance(StsWell.class, StsObjectRefList.class, StsLogCurve.class,
                    StsObjectList.class, StsSection.class, StsColor.class, StsSurfaceVertex.class, StsGridSectionPoint.class, StsBlock.class, StsSurface.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, file);
            // StsToolkit.writeObjectXML(well, pathname);
            return true;
        }
        catch (Exception e)
        {
            StsException.outputException("StsWell.export() failed for well " + getName(), e, StsException.WARNING);
            return false;
        }
    }

	public boolean exportWell(String timeOrDepth)
    {
		if(!getCloneIt())
			return exportWell(timeOrDepth, 0, 0.0f, 0.0f);
		else
		{
			float yOffset = yCloneOffset;
			int n = 0;
			for(int row = 0; row < numberRows; row++, yOffset += yCloneOffset)
			{
				float xOffset = xCloneOffset;
				for(int col = 0; col < numberCols; col++, xOffset += xCloneOffset, n++)
					exportWell(timeOrDepth, n, xOffset, yOffset);
			}
			return true;
		}
	}

    public boolean exportWell(String timeOrDepth, int version, float xOffset, float yOffset)
    {
        if (getMode() == CANCELED) return false;
    /*
        StsAsciiFile asciiFile = null;
        boolean exportLogData = getLogsData();
        boolean exportDeviationData = getDeviationData();
        boolean exportSeisAttData = getSeisAttsData();
		float minMDepth = getMinMDepth();
		float maxMDepth = getMaxMDepth();


        boolean writeTime = false;
        boolean writeDepth = false;
        if (timeOrDepth == StsParameters.TD_TIME_DEPTH_STRING)
        {
            writeDepth = true;
            writeTime = true;
        }
        else if (timeOrDepth == StsParameters.TD_DEPTH_STRING)
            writeDepth = true;
        else
            writeTime = true;
    */
        try
        {
            StsProject project = model.getProject();
            String directory = project.getProjectDirString();
            String subdirectory = "";
			String fullExportName = exportName;
			if(cloneIt)
			{
				if(cloneGroup == CLONE_GROUP_BY_WELL)
					subdirectory = "ClonedWell-" + exportName + File.separator;
				else if(cloneGroup == CLONE_GROUP_BY_WELL)
					subdirectory = "ClonedWell-" + version + File.separator;
				else
				    subdirectory = "ClonedWell" + File.separator;

				File subDir = new File(directory + subdirectory);
				try
				{
					if(!subDir.exists()) subDir.mkdirs();
				}
				catch(Exception e)
				{
					StsException.systemError(this, "exportWells", "Failed to create directory " + subdirectory);
				}
				fullExportName = exportName + "-" + version;
			}
            well.setName(fullExportName);
			String pathname = well.getAsciiDirectoryPathname();

            File file = new File(pathname);
            if (file.exists() && !getCloneIt())
            {
                boolean overWrite = StsYesNoDialog.questionValue(model.win3d,
                    "File " + pathname + " already exists. Do you wish to overwrite it?");
                if (!overWrite)
                {
                    return false;
                }
            }
            StsFile stsFile = well.checkCreateStsAsciiFile();
            try
            {
                StsParameterFile.writeObjectFields(stsFile, well);
            }
            catch(Exception e)
            {
                StsException.outputWarningException(this, "exportWell", "Failed to write S2S Well Ascii file", e);
                return false;
            }
            StsLineVectorSet lineVectorSet = well.getLineVectorSet();
            return lineVectorSet.checkWriteBinaryFiles();
        /*
            StsFile stsFile = StsFile.constructor(pathname);
            asciiFile = new StsAsciiFile(stsFile);

            if(!asciiFile.openWrite())
			{
				new StsMessage(model.win3d, StsMessage.WARNING, "Failed to open file for writing: " + pathname);
				return false;
			}

			StsLineVectorSet lineVectorSet = well.getLineVectorSet();
			int nValues = lineVectorSet.getSize();
			lineVectorSet.computeUnrotatedPoints(well);
			float[] xVector = lineVectorSet.getUnrotatedXFloats();
			float[] yVector = lineVectorSet.getUnrotatedYFloats();
			float[] zVector = lineVectorSet.getZFloats();
			float[] mVector = lineVectorSet.getMFloats();
			float[] tVector = lineVectorSet.getTFloats();

            asciiFile.writeLine(StsLoader.WELLNAME);
            asciiFile.writeLine(fullExportName);
            asciiFile.writeLine("ORIGIN XY");

            asciiFile.writeLine((lineVectorSet.getUnrotatedXOrigin() + xOffset) + " " + (lineVectorSet.getUnrotatedXOrigin() + yOffset));


            asciiFile.writeLine("CURVE");
            asciiFile.writeLine("X");
            asciiFile.writeLine("Y");
            if (writeDepth)
            {
                asciiFile.writeLine("DEPTH");
                asciiFile.writeLine("MDEPTH");
            }
            if (writeTime)
            {
                asciiFile.writeLine("TIME");
            }
//            int nVertices = lineVectorSet.getSize();
            // Header for Log Data
            StsObjectRefList curves = null;
            StsLogCurve log = null;
            if(exportLogData)
            {
                curves = well.getLogCurves();
                if(curves.getSize() > 0)
                {
                    for(int i=0; i<curves.getSize(); i++)
                    {
                        asciiFile.writeLine(curves.getElement(i).getName());
//                        if(((StsLogCurve)curves.getElement(i)).getValuesFloatVector().getSize() > nVertices)
//                        {
//                            log = (StsLogCurve)curves.getElement(i);
//                            nVertices = log.getValuesFloatVector().getSize();
//                        }
                    }
                }
            }
            // Header for Seismic Data
            StsSeismicVolume[] vols = null;
            if(exportSeisAttData)
            {
                vols = (StsSeismicVolume[])model.getCastObjectList(StsSeismicVolume.class);
                for(int i=0; i<vols.length; i++)
                    asciiFile.writeLine(vols[i].getName());
            }
            asciiFile.writeLine("VALUE");

//            String valLine = null;
			StsPoint[] points = well.getExportPoints();
			int nPoints = points.length;

            float exportMinDepth = minMDepth;
        */
		/*
            if(well instanceof StsWellPlan)
            {
                StsWell drillingWell = ((StsWellPlan)well).getDrillingWell();
                if(drillingWell != null)
                    exportMinDepth = drillingWell.getMaxMDepth();
            }
        */
         /*
            StsPoint minPoint = well.getPointAtMDepth(exportMinDepth, points, true);
            outputPoint(minPoint, writeTime, writeDepth, exportLogData, curves, asciiFile);
            if(resample)
                points = resamplePoints(points);
            for(int n = 0; n < points.length; n++)
            {
                float mdepth = points[n].getM();
                if(mdepth <= exportMinDepth) continue;
                if(mdepth >= maxMDepth) break;
                outputPoint(points[n], writeTime, writeDepth, exportLogData, curves, asciiFile);
            }
            StsPoint maxPoint = well.getPointAtMDepth(maxMDepth, points, true);
            outputPoint(maxPoint, writeTime, writeDepth, exportLogData, curves, asciiFile);

			if(exportSeisAttData)
			{
				;
			}
            return true;
         */
        }
        catch (Exception e)
        {
            StsException.outputException("StsWell.export() failed for well " + getName(), e, StsException.WARNING);
            return false;
        }
    /*
        finally
        {
            if(asciiFile != null) asciiFile.close();
        }
    */
    }

    private StsPoint[] resamplePoints(StsPoint[] points)
    {
        float minMDepth = getMinMDepth();
		float maxMDepth = getMaxMDepth();
        int numberSamples = (int)((maxMDepth - minMDepth) * getSampleRate());
        float inc = (int)((maxMDepth - minMDepth)/numberSamples);
        StsPoint[] newPts = new StsPoint[numberSamples];
        for(int i=0; i<numberSamples; i++)
        {
            newPts[i] = well.getPointAtMDepth(minMDepth + ((i+1)*inc), points, true);
        }
        return newPts;
    }
	private void outputPoint(StsPoint point, boolean writeTime, boolean writeDepth, boolean exportLogData, StsObjectRefList curves, StsAsciiFile asciiFile)
	{
		String valLine = null;

		float m = point.getM();
		float x = point.getX();
		float y = point.getY();
		float[] unrotatedXY = model.getProject().getUnrotatedRelativeXYFromRotatedXY(x, y);
		x = unrotatedXY[0];
		y = unrotatedXY[1];
		float z = point.getZ();
		float t = point.getT();
		if (writeTime && writeDepth)
			 valLine = new String(x + " " + y + " " + z + " " + m + " " + t);
		 else if (writeDepth)
			 valLine = new String(x + " " + y + " " + z + " " + m);
		 else if (writeTime)
			 valLine = new String(x + " " + y + " " + t);

		if(exportLogData)
		{
			for(int i=0; i < well.getNLogCurves(); i++)
				valLine = valLine + " " + ((StsLogCurve)curves.getElement(i)).getInterpolatedValue(z);
		}
		try
		{
			asciiFile.writeLine(valLine);
		}
		catch(Exception e)
		{
		}
	}

	private void outputUnrotatedPoints(StsLineVectorSet lineVectorSet, boolean writeTime, boolean writeDepth, boolean exportLogData, StsObjectRefList curves, StsAsciiFile asciiFile)
	{
		float[] xFloats, yFloats, zFloats = null, mdFloats, tFloats = null;
		String valLine = null;

		int nValues = lineVectorSet.getSize();
		xFloats = lineVectorSet.getUnrotatedXFloats();
		yFloats = lineVectorSet.getUnrotatedYFloats();
		if(writeDepth)
			zFloats = lineVectorSet.getZFloats();
		mdFloats = lineVectorSet.getMFloats();
		if(writeTime)
			tFloats = lineVectorSet.getTFloats();

		if (writeTime && writeDepth)
		{
			for(int n = 0; n < nValues; n++)
			{
				valLine = new String(xFloats[n] + " " + yFloats[n] + " " + zFloats[n] + " " + mdFloats[n] + " " + tFloats[n]);
				writeLine(asciiFile, valLine, exportLogData, curves, zFloats[n]);
			}
		}
		else if(writeDepth)
		{
			for(int n = 0; n < nValues; n++)
			{
				valLine = new String(xFloats[n] + " " + yFloats[n] + " " + zFloats[n] + " " + mdFloats[n]);
				writeLine(asciiFile, valLine, exportLogData, curves, zFloats[n]);
			}
		}
		else if(writeTime)
		{
			for(int n = 0; n < nValues; n++)
				valLine = new String(xFloats[n] + " " + yFloats[n] + " " + zFloats[n] + " " + tFloats[n]);
		}
	}

	private void writeLine(StsAsciiFile asciiFile, String valLine, boolean exportLogData, StsObjectRefList curves, float z)
	{
		if(exportLogData)
		{
			for(int i=0; i < well.getNLogCurves(); i++)
				valLine = valLine + " " + ((StsLogCurve)curves.getElement(i)).getInterpolatedValue(z);
		}
		try { asciiFile.writeLine(valLine); }
		catch(Exception e) { }
	}

    static public void main(String[] args)
	{
        StsModel model = StsModel.constructor("test");
        StsWell well = new StsWell();
		StsWellExportDialog exportDialog = new StsWellExportDialog(model, null, "Well Export Utility", true, well, "");
		 exportDialog.setVisible(true);
		 boolean exportLogData = exportDialog.getLogsData();
		 boolean exportDeviationData = exportDialog.getDeviationData();
		 boolean exportSeisAttData = exportDialog.getSeisAttsData();
		 float minMDepth = exportDialog.getMinMDepth();
		 float maxMDepth = exportDialog.getMaxMDepth();
	}

	public boolean getCloneIt()
	{
		return cloneIt;
	}

	public void setCloneIt(boolean cloneIt)
	{
		this.cloneIt = cloneIt;
	}

	public int getNumberRows()
	{
		return numberRows;
	}

	public void setNumberRows(int numberRows)
	{
		this.numberRows = numberRows;
	}

	public int getNumberCols()
	{
		return numberCols;
	}

	public void setNumberCols(int numberCols)
	{
		this.numberCols = numberCols;
	}

	public float getXCloneOffset()
	{
		return xCloneOffset;
	}

	public void setXCloneOffset(float xCloneOffset)
	{
		this.xCloneOffset = xCloneOffset;
	}

	public float getYCloneOffset()
	{
		return yCloneOffset;
	}

	public void setYCloneOffset(float yCloneOffset)
	{
		this.yCloneOffset = yCloneOffset;
	}
}
