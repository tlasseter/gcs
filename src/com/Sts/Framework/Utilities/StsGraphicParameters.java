

//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Utilities;

public class StsGraphicParameters
{
    public static final float well3dLineWidth = 2.0f;
    public static final float well3dLineWidthHighlighted = 4.0f;
    public static final float edgeLineWidth = 2.0f;
    public static final float edgeLineWidthHighlighted = 4.0f;
    public static final float gridLineWidth = 1.0f;
    public static final int solidLineStyle = 1;
    public static final int dottedLineStyle = 2;
    public static final short altDottedLine = (short)0x3333;
    public static final short dottedLine = (short)0xCCCC;
    public static final int vertexDotWidth = 6;
    public static final int vertexDotWidthHighlighted = 8;

    /** These are view shift factors so objects are drawn on top
     *  of each other cleanly.  Negative shift is away from viewer.
     */
    public static final double sectionShift = -0.5;     /** section surfaces        */
    public static final double gridShift = 0.5;         /** grids on surfaces       */
    public static final double cursorGridShift = 1.0;   /** cursor edge on grid     */
    public static final double vertexShift = 1.0;       /** vertex on 0 shift edge  */
    public static final double edgeShift = 1.0;         /** section edges           */
    public static final double vertexOnEdgeShift = 2.0; /** vertex on section edge  */

    /** Debug flags used in GL classes */
    public static final int GL_NO_DEBUG = 1;    /** No GL mainDebug					  */
    public static final int GL_DRAW = 2;       /** Print GL calls			  */
    public static final int GL_TRACE = 3;       /** Print GL calls				  */
    public static final int GL_PROFILE = 4;     /** Print and time GL calls		  */
    public static final int GL_LOCK = 5;        /** Print context lock/unlocking  */
    public static final int GL_LOCK_TRACE = 6;  /** Print context lock/unlocking and stack trace  */

    public static final int GL_STATE = 7;  /** Print context lock/unlocking and stack trace  */

    static public final byte GL_STATE_NONE = 0; // gl not initialized
    static public final byte GL_STATE_INITIALIZED = 1; // gl initialized
    static public final byte GL_STATE_STARTED = 2; // glEventListener added; ready to display
    static public final byte GL_STATE_SUSPENDED = 3;

    static public final String[] GL_STATE_STRINGS = new String[] { "NONE", "STARTED", "INITIALIZE", "SUSPENDED" };
    public static int lastStipple = 0;
    public static byte[] bftone =
    {   (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF,
    	(byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF, (byte)0xBF
    };    
    public static byte[] dftone =
    {   (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF,
    	(byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF, (byte)0xDF
    };
    public static byte[] eftone =
    {   (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF,
    	(byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF, (byte)0xEF
    };    
    public static byte[] f7tone =
    {   (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7,
    	(byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7, (byte)0xF7
    };
    public static byte[] fbtone =
    {   (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB,
    	(byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB, (byte)0xFB
    };    

    public static byte[] abtone =
    {   (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA,
    	(byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA
    };
    public static byte[] batone =
    {   (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
    	(byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55
    };    
    public static byte[] abbtone =
    {   (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24,
    	(byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24, (byte)0x49, (byte)0x24
    };     
    public static byte[] bbatone =
    {   (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD,
    	(byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD, (byte)0xDD
    };
    public static byte[] bbbatone =
    {   (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE,
    	(byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE, (byte)0xEE
    }; 
    public static byte[] abbbtone =
    {   (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77,
    	(byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77, (byte)0x77
    };
    public static byte[] babbtone =
    {   (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB,
    	(byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB, (byte)0xBB
    };    

    public static byte[] halftone =
    {   (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
        (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55
    };
    public static byte[] baabtone =
    {   (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99,
    	(byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99, (byte)0x99
    };    
    public static byte[][] stipplePatterns = { halftone, abbtone, bbatone, abtone, batone, bbbatone, abbbtone, babbtone, baabtone };
    //public static byte[][] stipplePatterns = { bftone, dftone, eftone, f7tone, fbtone };
    
    public static byte[] getNextStipple() 
    { 
    	byte[] pattern = stipplePatterns[lastStipple%stipplePatterns.length];
    	lastStipple++;
    	return pattern;
    }
}



