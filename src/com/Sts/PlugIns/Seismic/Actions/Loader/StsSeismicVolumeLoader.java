package com.Sts.PlugIns.Seismic.Actions.Loader;

import com.Sts.Framework.Actions.Loader.StsObjectLoader;
import com.Sts.Framework.IO.StsAbstractFile;
import com.Sts.Framework.MVC.StsModel;
import com.Sts.Framework.UI.Progress.StsProgressPanel;
import com.Sts.Framework.Utilities.StsException;
import com.Sts.Framework.Utilities.StsParameters;
import com.Sts.PlugIns.Seismic.DBTypes.StsSeismicVolume;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/30/11
 * Time: 8:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class StsSeismicVolumeLoader extends StsObjectLoader
{
	StsSeismicVolume volume;

    public StsSeismicVolumeLoader(StsModel model, String name)
    {
        super(model, name);
    }

    public boolean readProcessData(StsAbstractFile file, StsProgressPanel panel)
    {
        return true;
    }

    public boolean changeSourceFile(StsAbstractFile file, boolean change)
    {
        return true;
    }

	public void setVolume(StsSeismicVolume volume)
	{
		this.volume = volume;
		setStsMainObject(volume);
	}

	public String getAsciiFilePathname()
	{
		return volume.getAsciiDirectoryPathname();
	}

	public void setGroup()
	{
		group = GROUP_SEIS3D;
	}

    public void setNullValue()
    {
        nullValue = StsParameters.nullValue;
    }

    public boolean readFileHeader(StsAbstractFile file)
    {
        if(file == null) return false;

        try
        {
            StsSeismicVolume.checkLoadFromFilename(model, file, true);
            return true;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "readFileHeader", "Failed to read file " + file.filename, e);
			return false;
        }
    }
}