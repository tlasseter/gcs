
//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2002
//Author:       Stuart A. Jackson
//Company:      S2S Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.PlugIns.Seismic.Actions.PostStack3dLoad;

import com.Sts.Framework.Actions.Wizards.*;
import com.Sts.Framework.Actions.Wizards.WizardHeaders.*;
import com.Sts.Framework.IO.*;

public class StsVolumeSelect extends StsWizardStep
{
    StsVolumeSelectPanel panel;
    StsHeaderPanel header;

    public StsVolumeSelect(StsWizard wizard)
    {
        super(wizard);
        panel = new StsVolumeSelectPanel(wizard, this);
        header = new StsHeaderPanel();
        setPanels(panel, header);
//        panel.setPreferredSize(new Dimension(400, 400));
        header.setTitle("PostStack3d Selection");
        header.setSubtitle("Selecting Available Volumes");
        header.setLink("http://www.s2ssystems.com/Protected/Marketing/AppLinks.html#PostStack3dLoad");                
        header.setInfoText(wizardDialog,"(1) Select the seismic volumes that will be loaded.\n" +
                           "     ***** Must pre-process SegY Files before loading them. See Process Seismic step. *****\n" +
                           "(2) If other volumes are desired, navigate to the directory containing the volumes using the Dir button.\n" +
                           "     ***** All volumes in the selected directory will be placed in the left list. *****\n" +
                           "     ***** The default location is the project directory but any directory can be selected ****\n" +
                           "(3) Select the desired volumes from the left list and place them in the right list using the provided controls between the lists.\n" +
                           "     ***** Information regarding the currently selected volume is isVisible in the panel at the bottom of the screen.*****\n" +
                           "(4) An initial palette can be selected using the Select Palette Button or left to the default Red-White-Blue palette.\n" +
                           "(5) Once all volume selections are complete, press the Next>> Button.");
    }

    public StsFile[] getSelectedFiles()
    {
        return panel.getSelectedFiles();
    }

    public String[] getFilenameEndings(StsAbstractFile[] files)
    {
        return panel.getFilenameEndings(files);
    }

    public boolean start()
    {
        return true;
    }

    public boolean end()
    {
        return true;
    }
}

