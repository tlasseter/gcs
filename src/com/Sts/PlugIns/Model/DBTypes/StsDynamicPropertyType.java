package com.Sts.PlugIns.Model.DBTypes;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.MVC.*;
import com.Sts.PlugIns.Model.Types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: Aug 11, 2010
 * Time: 4:07:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsDynamicPropertyType extends StsPropertyType implements StsSerializable
{
    public StsDynamicPropertyType()
    {
    }

    public StsDynamicPropertyType(StsPropertyTypeDefinition propertyTypeName)
    {
        super(propertyTypeName);
    }

    public StsDynamicPropertyType(StsPropertyTypeDefinition propertyTypeName, boolean persistent)
    {
        super(propertyTypeName, persistent);
    }

    public boolean initialize(StsModel model)
    {
        return true;
    }

    static public StsDynamicPropertyType constructor(StsPropertyTypeDefinition propertyTypeDefinition)
    {
        return new StsDynamicPropertyType(propertyTypeDefinition);
    }
}
