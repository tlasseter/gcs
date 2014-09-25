package com.Sts.Framework.DBTypes;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.IO.*;
import com.Sts.Framework.UI.Beans.*;
import com.Sts.Framework.Utilities.*;

import java.nio.file.attribute.*;

/**
 * <p>Title: Workflow development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: 4D Systems LLC</p>
 * @author unascribed
 * @version 1.0
 */

public class StsDataSource extends StsSerialize implements StsSerializable
{
	private String sourceURIString;
	private long stsFileCreationTime = -1L;
	private long sourceCreationTime = -1L;
	private long sourceSize;

	private long bornDate = 0L;
	private long completeDate = 0L;
	private long deathDate = Long.MAX_VALUE;

	transient private long stsFileLastModifiedTime = -1L;
	transient private long sourceLastModifiedTime = -1L;

	static final boolean debug = true;

	public StsDataSource()
    {
    }

	public StsDataSource(StsAbstractFile sourceFile)
    {
		setDataSource(sourceFile);
    }

	public void setDataSource(StsAbstractFile sourceFile)
    {
		BasicFileAttributes fileAttributes = sourceFile.getFileAttributes();
		if(fileAttributes != null)
		{
			sourceCreationTime = fileAttributes.creationTime().toMillis();
			sourceLastModifiedTime = fileAttributes.lastModifiedTime().toMillis();
		}
		sourceURIString = sourceFile.getURIString();
    }

	public void reinitialize(long stsFileLastModifiedTime, long sourceLastModifiedTime)
	{
		this.stsFileLastModifiedTime = stsFileLastModifiedTime;
		this.sourceLastModifiedTime = sourceLastModifiedTime;
	}

	public boolean isAlive(long time)
	{
		if(time < 0 || bornDate < 0) return true;
		// Time check
		return bornDate < time && (deathDate > time || deathDate < 0);
	}

	public void setBornDate(long born) { if(born > 0l) bornDate = born; }
	public long getBornDate() { return bornDate; }

	public void setDeathDate(long death) { if(death > 0l) deathDate = death;  }
	public long getDeathDate() { return deathDate; }

	public void setBornDate(String born)
	{
		long newDate = StsDateFieldBean.convertToLong(born);
		setBornDate(newDate);
		return;
	}

	public String getBornDateString()
	{
		if(bornDate < 0) return "Undefined";
		return StsDateFieldBean.convertToString(bornDate);
	}

	public void setDeathDate(String death)
	{
		setDeathDate(getLongFromDateTimeString(death));
	}

	public String getDeathDateString()
	{
		if(deathDate < 0) return "Undefined";
		return getDateTimeStringFromLong(deathDate);
	}

	private String getDateTimeStringFromLong(long time)
	{
		return getCurrentProject().getDateTimeStringFromLong(deathDate);
	}


	private long getLongFromDateTimeString(String dateTimeString)
	{
		return getCurrentProject().getLongFromDateTimeString(dateTimeString);
	}

	public long getStsFileCreationTime()
	{
		return stsFileCreationTime;
	}

	public void setStsFileCreationTime(long stsFileCreationTime)
	{
		this.stsFileCreationTime = stsFileCreationTime;
	}

	public long getStsFileLastModifiedTime()
	{
		return stsFileLastModifiedTime;
	}

	public void setStsFileLastModifiedTime(long stsFileLastModifiedTime)
	{
		this.stsFileLastModifiedTime = stsFileLastModifiedTime;
	}

	public String getSourceURIString()
	{
		return sourceURIString;
	}

	public void setSourceURIString(String sourceURIString)
	{
		this.sourceURIString = sourceURIString;
	}

	public long getSourceCreationTime()
	{
		return sourceCreationTime;
	}

	public void setSourceCreationTime(long sourceCreationTime)
	{
		this.sourceCreationTime = sourceCreationTime;
	}

	public long getSourceLastModifiedTime()
	{
		return sourceLastModifiedTime;
	}

	public void setSourceLastModifiedTime(long sourceLastModifiedTime)
	{
		this.sourceLastModifiedTime = sourceLastModifiedTime;
	}

	public long getSourceSize()
	{
		return sourceSize;
	}

	public void setSourceSize(long sourceSize)
	{
		if(debug) StsException.systemDebug(this, "setSourceSize", "source: " + sourceURIString + " size: " + sourceSize);
		this.sourceSize = sourceSize;
	}

	public long getCompleteDate()
	{
		return completeDate;
	}

	public void setCompleteDate(long completeDate)
	{
		this.completeDate = completeDate;
	}
}
