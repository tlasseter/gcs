package com.Sts.Framework.UI;

import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Types.*;

/**
 * Created by IntelliJ IDEA.
 * User: Tom Lasseter
 * Date: 4/21/11
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class StsColorIndexItem extends StsColorListItem
{
	public int index;

    public StsColorIndexItem(int iconWidth, int iconHeight, int borderWidth, int index)
    {
        super(StsColor.GRAY, "gray", iconWidth, iconHeight, borderWidth);
		this.index = index;
    }

    public StsColorIndexItem(StsColor color, String name, int index)
    {
        super(color, name, 16, 16, 1);
		this.index = index;
    }

    public StsColorIndexItem(StsColor color, String name, int iconWidth, int iconHeight, int borderWidth, int index)
    {
		super(color, name, iconWidth, iconHeight, borderWidth);
		this.index = index;
    }

    public StsColorIndexItem(Object object, StsColor color, String name, int iconWidth, int iconHeight, int borderWidth, int index)
    {
		super(object, color, name, iconWidth, iconHeight, borderWidth);
		this.index = index;
    }

    public StsColorIndexItem(StsMainObject object, int index)
    {
        this(object.getStsColor(), object.getName(), 16, 16, 1, index);
    }
}
