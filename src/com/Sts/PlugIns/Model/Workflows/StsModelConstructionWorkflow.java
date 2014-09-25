package com.Sts.PlugIns.Model.Workflows;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version 1.1
 */

import com.Sts.Framework.Workflow.*;

public class StsModelConstructionWorkflow extends StsWorkflow
{
	protected StsWorkflowTreeNode horPick = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Horpick.StsHorpickWizard", "Pick Surfaces",
		"Auto-track an event through a seismic volume using the S2S 3D spiral tracker.",
		"A minimum of one seismic volume is required to pick a surface. Run the Load->3D Seismic Volume first.",
		"horizonPicker20x20.gif");
	protected StsWorkflowTreeNode defineHorizons = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Horizons.StsHorizonsWizard", "Horizons",
		"Horizon definition is used to ensure that all surfaces are translated to the same grid and optionally related to markers. It is required for fault definition and reservoir model construction.",
		"Must have loaded or auto-picked at least one surface. Either run the Load->Surfaces or Define->Pick Surfaces workflow step(s) prior to defining horizons.",
		"defineHorizons20x20.gif");
	protected StsWorkflowTreeNode workflowInitialSeisVel = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Velocity.StsSeisVelWizard", "Initial Seismic Velocities",
		"Construct a post-stack velocity model using seismic and well velocity data.",
		"The minimum reqiurement to construct an initial seismic velocity is either well Time/Depth curve or imported velocity functions.",
		"loadSeismic20x20.gif");
	protected StsWorkflowTreeNode workflowVelocityModel = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Velocity.StsVelocityWizard", "Velocity Model",
		"Construct a post-stack velocity model using any combination of user input, and imported well markers, horizons and velocity data.",
		"The minimum reqiurement to construct a velocity model is horizon data or an imported velocity cube. Run the Define->Horizons and/or Load->3D PostStack Volume first. If well ties are desired, import well markers with wells via the Load->Wells & Logs workflow step.",
		"loadSeismic20x20.gif");
	protected StsWorkflowTreeNode surfacesFromMarkers = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.SurfacesFromMarkers.StsSurfacesFromMarkersWizard", "Surfaces From Markers",
		 "Construct a surface from a set of markers in existing wells.", "Must have one or more well marker sets in order to construct.",
		 "importSurfaces20x20.gif");

	protected StsWorkflowTreeNode buildModelNode = new StsWorkflowTreeNode("Build Model");

	protected StsWorkflowTreeNode defineZones = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Zones.StsZonesWizard", "Define Zones",
		"Select horizon pairs which define zones across the reservoir.", "Run the Define->Horizon workflow step first.",
		"defineHorizons20x20.gif");
	protected StsWorkflowTreeNode buildBoundary = new StsWorkflowTreeNode("com.Sts.Actions.Boundary.StsBuildBoundary", "Define Boundary",
		"Define a boundary within the project to limit the fault and reservoir model construction. Boundaries can be removed and re-defined by running this workflow step.",
		"Must convert surfaces to horizons prior to defining a boundary. Run the Define->Horizon workflow step first.", "boundary20x20.gif");
	protected StsWorkflowTreeNode buildFrame = new StsWorkflowTreeNode("com.Sts.Actions.Build.StsBuildFrame", "Fault Framework",
		"Define a fault framework within a pre-defined boundary. Faults are digitized on horizons and/or cursor slices. Dying faults are supportted.",
		"Must have defined a boundary prior to defining a fault framework. Run the Define->Boundary workflow step first.", "buildFrame20x20.gif");
	protected StsWorkflowTreeNode completeModel = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Model.StsModelWizard", "Construct Model",
		"Construct the reservoir model from the fault framework and horizon data and initialize the stratigraphic layering.",
		"Define boundary, horizons and fault framework prior to constructing the reservoir model. Run Define->Boundary, Define->Horizons and Define->Fault Framework steps in this order prior to constructing the model.",
		"buildModel20x20.gif");
	protected StsWorkflowTreeNode exportEclipseModel = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.SimulationFile.StsSimulationFileWizard", "Export Model in Eclipse Format. ",
		"Export the reservoir model grid and properties in Eclipse format.",
		"Define boundary, horizons and fault framework prior to constructing the reservoir model. Run Define->Boundary, Define->Horizons and Define->Fault Framework steps in this order prior to constructing the model.",
		"buildModel20x20.gif");
	protected StsWorkflowTreeNode translateEclipse = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.SimulationFile.StsTranslateEclipseWizard", "Translate Eclipse indexes & oordinates. ",
		"Translate between S2S IJKB and Eclipse IJK indexes and XYZ coordinates.",
		"Construct the Eclipse Model first.  Required for index/coordinate translation.", "buildModel20x20.gif");
   protected StsWorkflowTreeNode loadEclipse = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.EclipseLoad.StsEclipseLoadWizard", "Load Eclipse restart file. ",
		"Reads Eclipse output files and translates to S2S block data structure files, one for each property and restart time (if dynamic).",
		"Construct the Eclipse Model first.", "buildModel20x20.gif");

    public StsModelConstructionWorkflow()
    {
        name = "StsModelConstructionWorkflow";
        workflowName = "Model Construction";
        description = new String("Builds faulted model grid from horizons, faults and wells.");
    }

    public void createWorkflowNodes(StsTreeModel treeModel, StsWorkflowTreeNode workflowRoot)
    {
		this.treeModel = treeModel;
        treeModel.addMenuNode(newProject);
        treeModel.addMenuNode(openProject);

		workflowRoot.addChild(processNode);
			addGroupNode(processNode, processWells);
			addGroupNode(processNode, processPostStack3d);
		    addGroupNode(processNode, processPostStack2d);
			addGroupNode(processNode, processVsp2d);

		workflowRoot.addChild(loadNode);
			addGroupNode(loadNode, loadPostStack3d);
		    addGroupNode(loadNode, loadPostStack2d);
		    addGroupNode(loadNode, loadWells);
			addGroupNode(loadNode, loadGraphicWells);
			addGroupNode(loadNode, loadVsp2d);
			addGroupNode(loadNode, loadSurfaces);
			addGroupNode(loadNode, loadMicroseismic);
			addGroupNode(loadNode, loadStaticSensors);
			addGroupNode(loadNode, loadFaultSticks);
			addGroupNode(loadNode, loadPallettes);
			addGroupNode(loadNode, loadCulture);
			// addGroupNode(loadNode, loadAncillaryData);
			addGroupNode(loadNode, loadVsp2d);

		workflowRoot.addChild(defineNode);
			addGroupNode(defineNode, horPick);
		    addGroupNode(defineNode, defineHorizons);
			addGroupNode(defineNode, workflowInitialSeisVel);
			addGroupNode(defineNode, workflowVelocityModel);
			addGroupNode(defineNode, surfacesFromMarkers);

		workflowRoot.addChild(buildModelNode);
			addGroupNode(buildModelNode, defineZones);
			addGroupNode(buildModelNode, buildBoundary);
			addGroupNode(buildModelNode, buildFrame);
			addGroupNode(buildModelNode, completeModel);
			addGroupNode(buildModelNode, exportEclipseModel);
			addGroupNode(buildModelNode, translateEclipse);
			addGroupNode(buildModelNode, loadEclipse);

		StsNodeBundle nodeBundle;

        nodeBundle = StsNodeBundle.oneRequiredConstructor(loadPostStack3d, processPostStack3d);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, horPick));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(loadSurfaces, horPick, surfacesFromMarkers);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, defineHorizons));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(defineHorizons);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, defineZones));
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, buildBoundary));

		nodeBundle = StsNodeBundle.oneRequiredConstructor(loadPostStack3d, processPostStack3d);
		treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, workflowInitialSeisVel));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(loadPostStack3d, loadSurfaces);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, workflowVelocityModel));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(loadWells, loadGraphicWells);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, surfacesFromMarkers));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(buildBoundary);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, buildFrame));
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, completeModel));

        nodeBundle = StsNodeBundle.oneRequiredConstructor(completeModel);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, exportEclipseModel));

        nodeBundle = new StsNodeBundle(exportEclipseModel);
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, translateEclipse));
        treeModel.addNodeConnection(new StsNodeConnection(nodeBundle, loadEclipse));
        //logUsageChange();
    }
}
