package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.IO.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.UI.Progress.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
abstract public class StsVectorSetTableLoader extends StsTimeVectorSetLoader
{
	public StsVectorSetTableLoader(StsModel model)
	{
		super(model);
	}

	public StsVectorSetTableLoader(StsModel model, StsLoadWizard wizard, StsProgressPanel progressPanel)
	{
		super(model, wizard, null, progressPanel);
	}

	public StsVectorSetTableLoader(StsModel model, boolean deleteStsData, StsProgressPanel progressPanel)
	{
		super(model, deleteStsData, progressPanel);
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
}