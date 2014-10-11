package com.Sts.Framework.DBTypes;

import com.Sts.Framework.UI.ObjectPanel.StsObjectTreePanel;
import com.Sts.Framework.UI.ObjectPanel.StsTreeNode;

/**
 * © tom 10/9/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public class StsModelObjectPanelClass extends StsObjectPanelClass
{
    public StsModelObjectPanelClass()
    {
        super();
    }

    public StsTreeNode getParentNode(StsObjectTreePanel objectTreePanel)
    {
        return objectTreePanel.modelNode;
    }
}
