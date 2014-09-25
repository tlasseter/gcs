package com.Sts.Framework.Workflow;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author unascribed
 * @version 1.1
 */

import com.Sts.Framework.Interfaces.*;
import com.Sts.Framework.MVC.*;
import com.Sts.Framework.MVC.Views.*;
import com.Sts.Framework.UI.ObjectPanel.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.Framework.Workflow.*;

/** This class describes a workflow.  A workflow has a name and description."
 */
public class StsWorkflow
{
    public String name = "com.Sts.MVC.StsMain";
    public String workflowName = "Default";
    public String description = "None";

    protected StsTreeModel treeModel;
    protected StsWorkflowTreeNode root;
    protected StsTreeNode rootNode, dataNode, modelNode;

	protected StsWorkflowTreeNode newProject = new StsWorkflowTreeNode("com.Sts.Framework.Actions.StsNewModel", "New Project", "newProj20x20.gif");
    protected StsWorkflowTreeNode openProject = new StsWorkflowTreeNode("com.Sts.Framework.Actions.StsOpenModel", "Open Project", null);
	protected StsNodeBundle projectNodeBundle = StsNodeBundle.oneRequiredConstructor(newProject, openProject);

	protected StsWorkflowTreeNode processNode = new StsWorkflowTreeNode("Process Data");
    protected StsWorkflowTreeNode loadNode = new StsWorkflowTreeNode("Load");
	protected StsWorkflowTreeNode defineNode = new StsWorkflowTreeNode("Define / Edit");
	protected StsWorkflowTreeNode analyzeNode = new StsWorkflowTreeNode("Analyze");
	protected StsWorkflowTreeNode outputNode = new StsWorkflowTreeNode("Output");

	protected StsWorkflowTreeNode processWells = new StsWorkflowTreeNode("com.Sts.PlugIns.Wells.Actions.Wizards.ProcessWells.StsProcessWellWizard", "Wells & Logs",
			"Process and optionally load well trajectory, logs, markers, perforations and time-depth functions. Various input formats are supported.",
			"A project must be created or opened prior to processing wells and logs.", "well20x20.gif");

	protected StsWorkflowTreeNode processPreStack2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PreStack2d.StsPreStack2dWizard", "PreStack 2D",
			"Pre-process pre-stack 2D datasets and optimize them for loading into S2S.",
			"A project must be created or opened prior to running this workflow step.",
			"2DSeismic20x20.gif");
	protected StsWorkflowTreeNode processPreStack3d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PreStack3d.StsPreStackWizard", "PreStack 3D",
			"Pre-process pre-stack 3D datasets and optimize them for loading into S2S.",
			"A project must be created or opened prior to running this workflow step.",
			"loadPreSeismic20x20.gif");
	protected StsWorkflowTreeNode processPostStack2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PostStack2d.StsPostStack2dWizard", "PostStack 2D",
			"Pre-process post-stack 2D datasets and optimize them for loading into S2S.",
			"A project must be created or opened prior to running this workflow step.",
			"2DSeismic20x20.gif");
	protected StsWorkflowTreeNode processPostStack3d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PostStack3d.StsPostStack3dWizard", "PostStack 3D",
			"Pre-process post-stack 3D datasets and optimize them for loading into S2S.",
			"A project must be created or opened prior to running this workflow step.",
			"loadSeismic20x20.gif");
	protected StsWorkflowTreeNode processVsp2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Vsp.StsSegyVspWizard", "Vertical Profiles",
			"Pre-process 2D VSP datasets and optimize them for loading into S2S.",
			"A project must be created or opened prior to running this workflow step.",
			"loadSeismic20x20.gif");

	protected StsWorkflowTreeNode loadPreStack2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PreStack2dLoad.StsPreStackLoad2dWizard", "PreStack 2D Seismic",
		"Load previously processed prestack 2d datasets into the project.",
		"Seismic data must be pre-processed prior to loading. Run the Process Seismic Data->PreStack 2D step first.",
		"2DSeismic20x20.gif");
	protected StsWorkflowTreeNode loadPreStack3d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PreStack3dLoad.StsPreStackLoadWizard", "PreStack 3D Seismic",
		"Load previously processed prestack 3d datasets into the project.",
		"Seismic data must be pre-processed prior to loading. Run the Process Seismic Data->PreStack 3D step first.",
		 "loadSeismic20x20.gif");
	protected StsWorkflowTreeNode loadPostStack2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.PostStack2dLoad.StsLine2dWizard", "PostStack 2D Seismic",
		"Load previously processed poststack 2d datasets into the project.",
		"Seismic data must be pre-processed prior to loading. Run the Process Seismic Data->PostStack 2D step first.",
		"2DSeismic20x20.gif");
	protected StsWorkflowTreeNode loadPostStack3d = new StsWorkflowTreeNode("com.Sts.PlugIns.Seismic.Actions.PostStack3dLoad.StsVolumeWizard", "PostStack 3D Seismic",
		"Load previously processed poststack 3d datasets into the project.",
		"Seismic data must be pre-processed prior to loading. Run the Process Seismic Data->PostStack 3D step first.",
		"loadSeismic20x20.gif");
	protected StsWorkflowTreeNode loadHandVels = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.HandVelocity.StsHandVelocityWizard", "HandVels",
		"Load handvel formatted velocity profiles into the project. These profiles can then be used to initialize a 2D or 3D pre-stack velocity model by running Define->Velocity Model on the PreStack Velocity Modeling Workflow",
		"A project must be created or opened prior to loading color palettes.",
		"importHandVels20x20.gif");
	protected StsWorkflowTreeNode loadVsp2d = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.VspLoad.StsVspLoadWizard", "VSP & Assign Wells",
		"Load previously processed 2D VSP datasets into the project and assign them to previously loaded wells.",
		"VSP data must be pre-processed and associated wells must have already been loaded prior to loading VSP data. Run the Process Seismic Data->Vertical Profiles and Load->Wells & Logs workflow steps first.",
		"loadSeismic20x20.gif");

	protected StsWorkflowTreeNode loadWells = new StsWorkflowTreeNode("com.Sts.PlugIns.Wells.Actions.Wizards.LoadWells.StsLoadWellWizard", "Wells & Logs",
		"Load well trajectory, logs, markers, perforations and time-depth functions. All data associated with a single well must be in properly named and formated file(s) to load properly.",
		"A project must be created or opened prior to loading wells and logs.", "well20x20.gif");

	protected StsWorkflowTreeNode loadGraphicWells = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.WellLoadGraphic.StsWellGraphicLoadWizard", "Graphical Well Selection",
		"Load well trajectory, logs, markers, perforations and time-depth functions. All data associated with a single well must be in properly named and formated file(s) to load properly.",
		"A project must be created or opened prior to loading wells and logs.", "well20x20.gif");

	protected StsWorkflowTreeNode loadSurfaces = new StsWorkflowTreeNode("com.Sts.PlugIns.Surfaces.Actions.Wizards.StsSurfaceWizard", "Surfaces",
		"Load gridded surface data into the project. Two grid formats are supported and are defined in the help.",
		"A project must be created or opened prior to loading surface data.",
		"importSurfaces20x20.gif");

	protected StsWorkflowTreeNode loadMicroseismic = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.LoadMicroseismic.StsLoadMicroseismicWizard", "Microseismic Data",
			"Load microseismic data. Data needs to be in a column ordered and space or tab delimited ASCII file with a header row. A minimum of four columns are required containing time,X,Y,Z and optional attributes.",
			"None", "timeSensor20x20.gif");

	protected StsWorkflowTreeNode loadStaticSensors = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.LoadSensors.StsLoadStaticSensorWizard", "Sensor Data",
			"Load sensor data. Data needs to be in a column ordered and space or tab delimited ASCII file with a header row. A minimum of two columns are required containing time and optional attribute(s).",
			"None", "timeSensor20x20.gif");

	protected StsWorkflowTreeNode loadFaultSticks = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.FaultSticks.StsFaultSticksWizard", "Fault Sticks",
		"Load ASCII file containing fault sticks", "Must have created an S2S Project.", "faultSticks20x20.gif");

	protected StsWorkflowTreeNode loadPallettes = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Color.StsPaletteWizard", "Color Palettes",
			"Load color palettes exported from other applications into the project. Once loaded palettes can be assigned to data objects via the object panel.",
			"A project must be created or opened prior to loading color palettes.", "importPalette20x20.gif");
	protected StsWorkflowTreeNode loadCulture = new StsWorkflowTreeNode("com.Sts.Framework.Actions.Wizards.Culture.StsCultureWizard", "Culture",
		"Load culture data in the form of lines, symbols and text. Loaded data is in XML ASCII format, examples are provided in the help.",
		"A project must be created or opened prior to loading culture data.", "loadCulture20x20.gif");

    StsWorkflowTreeNode analyzeData;
    StsWorkflowTreeNode buildStrat;
    StsWorkflowTreeNode buildFrame;
    StsWorkflowTreeNode buildModel;

    public StsWorkflow()
    {
        name = getClass().getName();
    }

    public String getDescription()
    {
        return description;
    }
/*
    public void logUsageChange()
    {
//        System.out.println("Workflow log - Module: " + Main.usageModule + " Message: " + Main.usageMessage);
//        Main.logUsage("com.Sts.WorkflowPlugIn.PlugIns." + name, "Changed Workflow");
        Main.setLogModule("com.Sts.WorkflowPlugIn.PlugIns." + name, "Changed Workflow");
//        System.out.println("Workflow set to - Module: " + Main.usageModule + " Message: " + Main.usageMessage);
    }
*/
    public void addGroupNode(StsWorkflowTreeNode groupNode, StsWorkflowTreeNode childNode)
    {
		if(childNode == null) return;
		groupNode.addChild(childNode);
    	treeModel.addNodeConnection(new StsNodeConnection(this.projectNodeBundle, childNode));
    }

    public void createWorkflowNodes(StsTreeModel treeModel, StsWorkflowTreeNode root)
    {

    }

    public boolean checkName()
    {
        String className = getClass().getName();
        if(className.indexOf(name) > 0) return true;

        StsException.systemError(className + " constructor incorrect. name: " + name +
            " must be a substring of className" + className);
        return false;
    }

    public void addAdditionalToolbars(StsWin3dBase win3d)
    {
        ; // Override in specific workflows as required.
    }

    public void addTimeActionToolbar(StsWin3d win3d)
    {
        if(win3d == null) return;
        win3d.checkAddTimeActionToolbar();
    }

    public StsTreeNode checkAddStaticNode(StsModel model, StsTreeNode node, StsTreeObjectI userObject, String label, boolean required)
    {
        return node.addStaticNode(userObject, label);
    }
}
