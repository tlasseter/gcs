//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Actions.Wizards.LoadComponents;

import com.Sts.Framework.Actions.Loader.*;
import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.DBTypes.VectorSetObjects.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

import java.awt.*;
import java.util.*;

public class StsVectorSetFilesLoadStep extends StsWizardStep implements Runnable
{
    public StsProgressPanel panel;
    public StsHeaderPanel header;
    protected StsLoadWizard loadWizard;
    protected boolean canceled = false;

    public StsVectorSetFilesLoadStep(StsLoadWizard loadWizard)
    {
        super(loadWizard);
        this.loadWizard = loadWizard;
        panel = StsProgressPanel.constructor(5, 50);
        header = new StsHeaderPanel();
        super.initialize(loadWizard, panel, null, header);
    }

    public boolean start()
    {
        run();
        return true;
    }

    public void run()
    {
		StsVectorSetImport importer = loadWizard.getImporter(model, panel);
		String messageName = importer.getGroupName();

        try
        {
            if (canceled)
            {
                success = false;
                return;
            }
            disablePrevious();

            // turn off redisplay
            model.disableDisplay();
            panel.appendLine("Starting " + messageName + " loading...");

            // turn on the wait cursor
            StsCursor cursor = new StsCursor(panel, Cursor.WAIT_CURSOR);
            ArrayList<StsVectorSetObject> vectorSetObjects = importer.constructObjects(loadWizard);

            int nSuccessfulObjects = vectorSetObjects.size();
            success = nSuccessfulObjects > 0;
			if(success)
				success = processProjectObjects(vectorSetObjects);

            panel.appendLine("Loading  " + messageName + " is complete. Press the Finish> button");

            if(nSuccessfulObjects == 0)
                 panel.setDescriptionAndLevel("All " + messageName + " data sets failed to load.", StsProgressBar.ERROR);
             else if(nSuccessfulObjects < vectorSetObjects.size())
                 panel.setDescriptionAndLevel("Some  " + messageName + " set(s) failed to load.", StsProgressBar.WARNING);
             else
                 panel.setDescriptionAndLevel("All " + messageName + " sets loaded successfully.", StsProgressBar.INFO);
            panel.finished();

            cursor.restoreCursor();
            if(nSuccessfulObjects > 0)
			{
				StsProject project = model.getProject();
				project.addToProjectUnrotatedBoundingBoxes(vectorSetObjects, StsProject.TD_DEPTH);
				project.runCompleteLoading();
			}
            model.enableDisplay();
            model.win3dDisplay();
			disableCancel();
			//loadWizard.addFilesToDataPanel();
            loadWizard.enableFinish();
        }
        catch (Exception e)
        {
        	panel.appendLine("Failed to load  " + messageName + ".");
            panel.appendLine("Error message: " + e.getMessage());
            StsException.outputWarningException(this, "run", e);
            success = false;
        }
    }

	public boolean processProjectObjects(ArrayList<StsVectorSetObject> vectorSetObjects)
	{
		return model.getProject().addToProjectUnrotatedBoundingBoxes(vectorSetObjects, StsProject.TD_DEPTH);
	}

    public boolean end()
    {
        return success;
    }
}
