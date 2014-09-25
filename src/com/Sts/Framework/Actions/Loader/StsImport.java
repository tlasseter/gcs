//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

abstract public class StsImport
{
    public StsModel model;
	public StsLoader loader;
    public int nLoaded = 0;

	public StsProgressPanel progressPanel;

	static protected boolean showDetailedProgress = true;
	static public final String FORMAT_TXT = StsLoader.FORMAT_TXT;

	static public final boolean debug = false;

	public StsImport() { }

	protected void initialize(StsModel model)
	{
        this.model = model;
        StsProject project = model.getProject();
        nLoaded = 0;
    }

	public StsImport(StsModel model, StsProgressPanel progressPanel)
	{
		initialize(model);
		this.progressPanel = progressPanel;
	}

    public boolean constructObjects(StsAbstractFile[] files, boolean loadValues)
    {
        try
        {
            int nFiles = files.length;
			showDetailedProgress = nFiles < 100;
            progressPanel.initialize(nFiles);
			int n = 0;

            for (StsAbstractFile file : files)
            {
                String objectName = file.name;
				loader = StsLoader.getLoader(file, model, true);

				if (loader.processStsFile(file, loadValues))
				{
					if (showDetailedProgress)
						loader.appendLine("   Successfully processed " + file.format + " formatted " + file.group + ": " + file.name + "...");
					nLoaded++;
				}
				else if(showDetailedProgress)
					loader.appendLine("  Failed to process " + file.format + " formatted " + file.group + ": " + file.name + "...");

                progressPanel.setValue(++n);
                if(showDetailedProgress) loader.setDescription("Loaded " + file.group + " #" + nLoaded + " of " + nFiles);
            }
            return true;
        }
        catch (Exception e)
        {
            StsException.outputWarningException(StsImport.class, "constructWells", e);
            progressPanel.setDescriptionAndLevel("StsMicroseismicImport.constructMicroseismicSets() failed.\n", StsProgressBar.WARNING);
            return false;
        }
    }

	/** Override in concrete subclasses if subFiles need to be added to each file.  */
	protected void addSubFiles(StsAbstractFile[] files)
	{
	}

    public boolean constructObject(StsAbstractFile file, boolean loadValues, StsProgressPanel progressPanel)
    {
        try
        {
            if (showDetailedProgress) loader.appendLine("Processing S2S formatted " + file.group + ": " + file.name + "...");
			return loader.processStsFile(file, loadValues);
        }
        catch (Exception e)
        {
            StsException.outputException(StsToolkit.getSimpleClassname(this) + " failed.", e, StsException.WARNING);
            StsMessageFiles.logMessage("Unable to process file  " + file.filename);
            if (progressPanel != null) progressPanel.setDescriptionAndLevel("Unable to read " + file.group + " file(s).\n", StsProgressBar.ERROR);
            return false;
        }
    }
}
