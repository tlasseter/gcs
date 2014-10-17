package com.Sts.Framework.Interfaces;

/**
 * © tom 10/2/2014
 * All Rights Reserved
 * No part of this website or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of the author, unless otherwise indicated for stand-alone materials.
 */
public interface StsRandomDistribFace
{
    static public byte TYPE_LINEAR = 0;
    static public byte TYPE_GAUSS = 1;
    static public byte TYPE_LOGNORM = 2;
    static public byte TYPE_POISSON = 3;
    static public String LINEAR_STRING = "Linear";
    static public String GAUSS_STRING = "Gauss";
    static public String LOGNORM_STRING = "Log Normal";
    static public String POISSON_STRING = "Poisson";
    static public String[] typeStrings = new String[] { LINEAR_STRING, GAUSS_STRING, LOGNORM_STRING, POISSON_STRING };

    public double getAvg();
    public double getDev();
    public int getCount();
    public String getType();
}
