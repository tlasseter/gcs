package com.Sts.Framework.Interfaces.MVC;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Interfaces.*;

import java.util.*;

/** subclasses of StsClass which can be displayed on timeSeries plots implement this flag interface. */
public interface StsClassTimeSeriesDisplayable
{
	abstract public StsObject[] getElements();
	abstract public ArrayList<StsTimeSeriesDisplayable> getTimeSeriesDisplayableObjects();
}
