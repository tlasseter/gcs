package com.Sts.PlugIns.GeoModels.Workflows;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version 1.1
 */

import com.Sts.Framework.Workflow.*;

public class StsGeoModelVolumeWorkflow extends StsWorkflow
{
    protected StsWorkflowTreeNode buildModelNode = new StsWorkflowTreeNode("Build Model");

	protected StsWorkflowTreeNode geoModel = new StsWorkflowTreeNode("com.Sts.PlugIns.GeoModels.Actions.Wizards.StsVolumeDefinitionWizard", "Build volume for geoModell",
		"Construct volume for geological model.",
		"Must have new project as a minimum.",
		"horizonPicker20x20.gif");

    public StsGeoModelVolumeWorkflow()
    {
        description = new String("Builds volume for geological model.");
    }

    public void createWorkflowNodes(StsTreeModel treeModel, StsWorkflowTreeNode workflowRoot)
    {
		this.treeModel = treeModel;
        treeModel.addMenuNode(newProject);
        treeModel.addMenuNode(openProject);

		workflowRoot.addChild(buildModelNode);
			addGroupNode(buildModelNode, geoModel);

    }
}
