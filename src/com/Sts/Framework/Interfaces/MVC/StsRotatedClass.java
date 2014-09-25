package com.Sts.Framework.Interfaces.MVC;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 3/29/11
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */

import com.Sts.Framework.DBTypes.*;

/** This flag interface indicates that this is a rotated bounding box class whose instances are added/removed to/from the project bounding boxes */
public interface StsRotatedClass
{
	abstract public StsObject[] getElements();
}
