package com.Sts.Framework.Actions.Loader;

import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.*;

public class StsSourceSelectStep extends StsWizardStep
{
	protected StsLoadWizard loadWizard;
	public StsSourceSelectPanel panel;
	public StsHeaderPanel header;

	public StsSourceSelectStep(StsLoadWizard wizard, String sourceKeyword, String[] sourceTypes, String[] dataTypes)
	{
		super(wizard);
		this.loadWizard = wizard;
		panel = new StsSourceSelectPanel(wizard, this, sourceKeyword, sourceTypes, dataTypes);
		header = new StsHeaderPanel();
		setPanels(panel, header);
		header.setTitle("Well Load");
		header.setSubtitle("File Type Definition");
		header.setInfoText(wizardDialog, "(1) Select the type of file that will be loaded.\n" +
				"       S2S formatted wells will be a series of files for each well.\n" +
				"           well-dev.txt.<name> for deviation\n" +
				"           well-logs.las.<name> for logs\n" +
				"           well-ref.txt.<name> for geologic references (tops)\n" +
				"           well-perfs.txt.<name> for perforations\n" +
				"           well-equiment.txt.,name. for phones, packers, etc...\n" +
				"           well-fmi.txt.<name> for formation scanner results\n" +
				"       Geographix formatted wells are wells output in the wls ASCII format.\n" +
				"(2) Press the Next>> Button.\n");
		header.setLink("http://www.GeoCloudRealTime.com/Protected/Marketing/AppLinks.html#Well");
	}

	public boolean start()
	{
	//	panel.initialize();
		return true;
	}

	public boolean end()
	{
		return true;
	}

	public byte getType()
	{
		return panel.getType();
	}

}
