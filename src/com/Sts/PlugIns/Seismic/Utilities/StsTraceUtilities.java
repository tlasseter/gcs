package com.Sts.PlugIns.Seismic.Utilities;

import com.Sts.Framework.Types.*;
import com.Sts.Framework.UI.*;
import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.UI.*;

import javax.media.opengl.*;
import java.nio.*;

/**
 * Created by IntelliJ IDEA.
  * User: Tom Lasseter
  * Date: May 15, 2009
  * Time: 6:42:54 AM
  * To change this template use File | Settings | File Templates.
  */
 public class StsTraceUtilities
 {
     /** fractional max error allowed in curve fitting wavelets */
     static double maxError = 0.01;

     public final static byte POINT_ORIGINAL = 0;
     public final static byte POINT_INTERPOLATED = 1;
     public final static byte POINT_FLAT_ZERO = 2;
     public final static byte POINT_MAXIMUM = 3;
     public final static byte POINT_MINIMUM = 4;
     public final static byte POINT_PLUS_ZERO_CROSSING = 5;
     public final static byte POINT_MINUS_ZERO_CROSSING = 6;
     public final static byte POINT_FALSE_MAXIMUM = 7;
     public final static byte POINT_FALSE_MINIMUM = 8;
     public final static String[] typeStrings = new String[]{"Original", "Interpolated", "Flat zero", "Max", "Min", "Zero Plus", "Zero Minus", "False Max", "False Min"};
     public final static StsColor[] typeColors = new StsColor[]{StsColor.GRAY, StsColor.MAGENTA, StsColor.CYAN, StsColor.RED, StsColor.BLUE, StsColor.GREEN, StsColor.GREEN, StsColor.PINK, StsColor.AQUAMARINE};
     public static double[][] tracePoints;
     public static byte[] tracePointTypes;
     public static final StsColor outsideMuteColor = StsColor.GRAY;

     public static final boolean debug = false;

     static public float[] computeCubicInterpolatedPoints(float[] inputPoints, int nIntervals)
     {
         int nInputPoints = inputPoints.length;
         int nPoints = (nInputPoints - 1) * nIntervals + 1;
         try
         {
             StsCubicForwardDifference cubicFd = new StsCubicForwardDifference(nIntervals);
             float[] points = new float[nPoints];
             double z0;
             double z1 = inputPoints[0];
             double z2 = inputPoints[1];
             double z3 = inputPoints[2];
             int out = 0;
             double slope1 = z2 - z1;
             double slope2 = 0.5 * (z3 - z1);
             cubicFd.hermiteInitialize(z1, z2, slope1, slope2);
             double[] interpolatedPoints = cubicFd.evaluate();
             for (int j = 0; j < nIntervals; j++)
                 points[out++] = (float) interpolatedPoints[j];
             for (int i = 3; i < nInputPoints; i++)
             {
                 z0 = z1;
                 z1 = z2;
                 z2 = z3;
                 z3 = inputPoints[i];
                 cubicFd.splineInitialize(z0, z1, z2, z3);
                 interpolatedPoints = cubicFd.evaluate();
                 for (int j = 0; j < nIntervals; j++)
                     points[out++] = (float) interpolatedPoints[j];
             }
             z0 = z1;
             z1 = z2;
             z2 = z3;
             slope1 = slope2;
             slope2 = z3 - z1;
             cubicFd.hermiteInitialize(z1, z2, slope1, slope2);
             interpolatedPoints = cubicFd.evaluate();
             for (int j = 0; j < nIntervals; j++)
                 points[out++] = (float) interpolatedPoints[j];
             points[out++] = (float) z2;
             return points;
         }
         catch (Exception e)
         {
             StsException.outputWarningException(StsTraceUtilities.class, "computeInterpolatedPoints", e);
             return null;
         }
     }

     static public float[] computeCubicInterpolatedPoints(double[] inputPoints, int nIntervals)
     {
         int nInputPoints = inputPoints.length;
         int nPoints = (nInputPoints - 1) * nIntervals + 1;
         try
         {
             StsCubicForwardDifference cubicFd = new StsCubicForwardDifference(nIntervals);
             float[] points = new float[nPoints];
             double z0;
             double z1 = inputPoints[0];
             double z2 = inputPoints[1];
             double z3 = inputPoints[2];
             int out = 0;
             double slope1 = z2 - z1;
             double slope2 = 0.5 * (z3 - z1);
             cubicFd.hermiteInitialize(z1, z2, slope1, slope2);
             double[] interpolatedPoints = cubicFd.evaluate();
             for (int j = 0; j < nIntervals; j++)
                 points[out++] = (float) interpolatedPoints[j];
             for (int i = 3; i < nInputPoints; i++)
             {
                 z0 = z1;
                 z1 = z2;
                 z2 = z3;
                 z3 = inputPoints[i];
                 cubicFd.splineInitialize(z0, z1, z2, z3);
                 interpolatedPoints = cubicFd.evaluate();
                 for (int j = 0; j < nIntervals; j++)
                     points[out++] = (float) interpolatedPoints[j];
             }
             z0 = z1;
             z1 = z2;
             z2 = z3;
             slope1 = slope2;
             slope2 = z3 - z1;
             cubicFd.hermiteInitialize(z1, z2, slope1, slope2);
             interpolatedPoints = cubicFd.evaluate();
             for (int j = 0; j < nIntervals; j++)
                 points[out++] = (float) interpolatedPoints[j];
             points[out++] = (float) z2;
             return points;
         }
         catch (Exception e)
         {
             StsException.outputWarningException(StsTraceUtilities.class, "computeInterpolatedPoints", e);
             return null;
         }
     }

     static public byte[] defineTracePointTypes(float[] pointValues)
     {
         int nPoints = pointValues.length;
         byte[] tracePointTypes = new byte[nPoints];
         // for first point, assign as Max or min, or flat-zero
         float v = pointValues[0];
         if (v > 0)
             tracePointTypes[0] = POINT_FALSE_MAXIMUM;
         else if (v < 0)
             tracePointTypes[0] = POINT_FALSE_MINIMUM;
         else // v == 0
             tracePointTypes[0] = POINT_FLAT_ZERO;
         // assign maxima and minima
         float vp = pointValues[1];
         for (int n = 1; n < nPoints - 1; n++)
         {
             float vm = v;
             v = vp;
             vp = pointValues[n + 1];

             if (v < vm && v <= vp && v < 0)
                 tracePointTypes[n] = POINT_MINIMUM;
             else if (v > vm && v >= vp && v > 0)
                 tracePointTypes[n] = POINT_MAXIMUM;
             else
             {
                 if (vm <= 0.0 && v > 0.0)
                 {
                     if (-vm < v)
                         tracePointTypes[n - 1] = POINT_PLUS_ZERO_CROSSING;
                     else
                         tracePointTypes[n] = POINT_PLUS_ZERO_CROSSING;
                 }
                 else if (vm >= 0.0 && v < 0.0)
                 {
                     if (vm < -v)
                         tracePointTypes[n - 1] = POINT_MINUS_ZERO_CROSSING;
                     else
                         tracePointTypes[n] = POINT_MINUS_ZERO_CROSSING;
                 }
             }
         }
         v = vp;
         if (v > 0)
             tracePointTypes[nPoints - 1] = POINT_FALSE_MAXIMUM;
         else if (v < 0)
             tracePointTypes[nPoints - 1] = POINT_FALSE_MINIMUM;
         else
             tracePointTypes[nPoints - 1] = POINT_FLAT_ZERO;

         return tracePointTypes;
     }

     static public boolean computeInstantAmpAndPhase(float[] gatherData, byte[] gatherPointTypes, float[] amplitudes, float[] phases)
     {
         int nPoints = gatherData.length;
         int[] eventIndexes = new int[nPoints];
         int nEvents = 0;
         for (int n = 0; n < nPoints; n++)
         {
             byte newPointType = gatherPointTypes[n];
             if (isPointTypeEvent(newPointType))
                 eventIndexes[nEvents++] = n;
         }
         for (int n = 0; n < nEvents; n++)
         {
             int i = eventIndexes[n];
             byte pointType = gatherPointTypes[i];
             switch (pointType)
             {
                 case POINT_MAXIMUM:
                     phases[i] = 0.0f;
                     break;
                 case POINT_MINIMUM:
                     phases[i] = 180;
                     break;
                 case POINT_PLUS_ZERO_CROSSING:
                     phases[i] = -90;
                     break;
                 case POINT_MINUS_ZERO_CROSSING:
                     phases[i] = 90;
                     break;
             }
         }
         int prevIndex = eventIndexes[0];
         for (int n = 1; n < nEvents; n++)
         {
             int i = eventIndexes[n];
             byte pointType = gatherPointTypes[i];
             switch (pointType)
             {
                 case POINT_MAXIMUM:
                     amplitudes[i] = gatherData[i];
                     interpolateAmplitudes(prevIndex, i, amplitudes);
                     prevIndex = i;
                     break;
                 case POINT_MINIMUM:
                     amplitudes[i] = -gatherData[i];
                     interpolateAmplitudes(prevIndex, i, amplitudes);
                     prevIndex = i;
                     break;
             }
         }
         int nextIndex = eventIndexes[0];
         for (int n = 1; n < nEvents; n++)
         {
             int index = nextIndex;
             nextIndex = eventIndexes[n];
             interpolatePhases(index, nextIndex, phases);
         }
         return true;
     }

     static public void interpolateAmplitudes(int index, int nextIndex, float[] amplitudes)
     {
         double df = 1.0 / (nextIndex - index);
         double dAmp = df * (amplitudes[nextIndex] - amplitudes[index]);
         for (int i = index + 1; i < nextIndex; i++)
             amplitudes[i] = (float) (amplitudes[i - 1] + dAmp);
     }

     static public void interpolatePhases(int index, int nextIndex, float[] phases)
     {
         double df = 1.0 / (nextIndex - index);
         double dPhase = phases[nextIndex] - phases[index];
         if (dPhase < 0.0)
             dPhase += 360;
         dPhase *= df;
         for (int i = index + 1; i < nextIndex; i++)
         {
             phases[i] = (float) (phases[i - 1] + dPhase);
             if (phases[i] > 180)
                 phases[i] -= 360;
         }
     }

     static public final boolean isPointTypeEvent(byte type)
     {
         return type >= POINT_MINIMUM && type <= POINT_MINUS_ZERO_CROSSING;
     }

     static public void drawWiggleTraces(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, StsWiggleDisplayProperties wiggleProperties, int displayInc)
     {
         try
         {
             if (values == null || values.length < 3) return;
             StsColor lineColor = wiggleProperties.getLineColor();
             gl.glDisable(GL.GL_LIGHTING);
             gl.glLineWidth(1.0f);
             lineColor.setGLColor(gl);
             gl.glBegin(GL.GL_LINE_STRIP);
             float z = displayZMin;
             for (int i = 0, n = nValueMin; i < nValues; i++, n += displayInc, z += zInc)
                 gl.glVertex2f(values[n] + x0, z);
         }
         catch (Exception e)
         {
             StsException.outputWarningException(StsTraceUtilities.class, "drawWiggleTraces", e);
         }
         finally
         {
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
     }

     static public void drawWigglePoints(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, int displayInc)
     {
         if (nValues < 3) return;
         gl.glDisable(GL.GL_LIGHTING);
         gl.glPointSize(4);
         gl.glBegin(GL.GL_POINTS);
         float z = displayZMin;
         for (int i = 0, n = nValueMin; i < nValues; i++, n += displayInc, z += zInc)
         {
             byte pointType = getPointType(values, n);
             if (pointType <= POINT_INTERPOLATED) continue;
             getPointTypeColor(pointType).setGLColor(gl);
             gl.glVertex2f(values[n] + x0, z);
         }
         gl.glEnd();
         gl.glEnable(GL.GL_LIGHTING);
     }

     static public byte getPointType(float[] values, int n)
     {
         if (n == 0)
             return getEndPointType(values[0]);
         int nValues = values.length;
         if (n == nValues - 1)
             return getEndPointType(values[nValues - 1]);
         else
             return getPointType(values[n - 1], values[n], values[n + 1]);
     }

     static private byte getEndPointType(float value)
     {
         if (value > 0)
             return POINT_FALSE_MAXIMUM;
         else if (value < 0)
             return POINT_FALSE_MINIMUM;
         else // value == 0
             return POINT_FLAT_ZERO;
     }

     static private byte getPointType(float vm, float v, float vp)
     {
         if (v < vm && v <= vp && v < 0)
             return POINT_MINIMUM;
         else if (v > vm && v >= vp && v > 0)
             return POINT_MAXIMUM;
         else
         {
             if (vm <= 0.0 && v > 0.0)
             {
                 if (-vm < v)
                     return POINT_PLUS_ZERO_CROSSING;
                 else
                     return POINT_PLUS_ZERO_CROSSING;
             }
             else if (vm >= 0.0 && v < 0.0)
             {
                 if (vm < -v)
                     return POINT_MINUS_ZERO_CROSSING;
                 else
                     return POINT_MINUS_ZERO_CROSSING;
             }
             else
                 return POINT_INTERPOLATED;
         }
     }

     static public void drawFilledWiggleTraces(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, StsWiggleDisplayProperties wiggleProperties, int displayInc)
     {
         if (values == null || values.length < 3) return;
         try
         {
             gl.glDisable(GL.GL_LIGHTING);
             StsColor plusColor = wiggleProperties.getWigglePlusColor();
             StsColor minusColor = wiggleProperties.getWiggleMinusColor();
             float amp1 = values[nValueMin];
             float t1 = displayZMin;
             boolean plus1 = amp1 >= 0;
             if (plus1)
                 plusColor.setGLColor(gl);
             else
                 minusColor.setGLColor(gl);

             gl.glBegin(GL.GL_QUAD_STRIP);
             gl.glVertex2d(x0, t1);
             gl.glVertex2d(x0 + amp1, t1);
             for (int i = 1, n = nValueMin + displayInc; i < nValues; i++, n += displayInc)
             {
                 float t0 = t1;
                 t1 += zInc;
                 float amp0 = amp1;
                 amp1 = values[n];
                 boolean plus0 = plus1;
                 plus1 = amp1 >= 0;

                 if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so continue drawing fill color
                 {
                     gl.glVertex2f(x0, t1);
                     gl.glVertex2f(x0 + amp1, t1);
                 }
                 else // line crosses at tz; draw old color to tz and then new color to t1
                 {
                     float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                     gl.glVertex2f(x0, tz);
                     gl.glVertex2f(x0, tz);
                     if (plus1)
                         plusColor.setGLColor(gl);
                     else
                         minusColor.setGLColor(gl);
                     gl.glVertex2f(x0, tz);
                     gl.glVertex2f(x0, tz);
                     gl.glVertex2f(x0, t1);
                     gl.glVertex2f(x0 + amp1, t1);
                 }
             }
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
         catch (Exception e)
         {
             StsException.outputException("StsPreStackLine.drawFilledWiggleTraces() failed.", e, StsException.WARNING);
         }
         finally
         {
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
     }

     static public void drawFilledWiggleTraces(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, StsWiggleDisplayProperties wiggleProperties, double[] muteRange, int displayInc)
     {
         if (values == null || values.length < 3) return;
         try
         {
             gl.glDisable(GL.GL_LIGHTING);
             StsColor plusColor = wiggleProperties.getWigglePlusColor();
             StsColor minusColor = wiggleProperties.getWiggleMinusColor();
             float amp1 = values[nValueMin];
             float t1 = displayZMin;
             boolean plus1 = amp1 >= 0;
             float topMute = (float) muteRange[0];
             float botMute = (float) muteRange[1];
             boolean muted = (t1 < topMute || t1 > botMute);
             if (muted) // start color is mute
                 outsideMuteColor.setGLColor(gl);
             else // start color is plus or minus color
             {
                 if (plus1)
                     plusColor.setGLColor(gl);
                 else
                     minusColor.setGLColor(gl);
             }
             gl.glBegin(GL.GL_QUAD_STRIP);
             gl.glVertex2d(x0, t1);
             gl.glVertex2d(x0 + amp1, t1);
             for (int i = 1, n = nValueMin + displayInc; i < nValues; i++, n += displayInc)
             {
                 float t0 = t1;
                 t1 += zInc;
                 float amp0 = amp1;
                 amp1 = values[n];
                 boolean plus0 = plus1;
                 plus1 = amp1 >= 0;

                 if (muted) // previous point is muted
                 {
                     muted = (t1 < topMute || t1 > botMute);
                     if (muted) // still muted
                     {
                         if (plus0 != plus1) // line does not cross zero, so continue drawing muted color to t1
                         {
                             float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                             if (plus1)
                                 outsideMuteColor.setGLColor(gl);
                             else
                                 minusColor.setGLColor(gl); //don't fill negative wiggles with mute color!! SWC 10/20/09
                             gl.glVertex2f(x0, tz);
                             gl.glVertex2f(x0, tz);
                         }
                         gl.glVertex2d(x0, t1);
                         gl.glVertex2d(x0 + amp1, t1);
                     }
                     else // have crossed into unmuted interval; only occurs if previous point is muted and current point is not
                     {
                         float ampMute = amp0 + (amp1 - amp0) * (topMute - t0) / (t1 - t0);
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so just draw unmuted color to t1
                         {
                             // complete muted color for first part of interval
                             gl.glVertex2d(x0, topMute);
                             gl.glVertex2d(x0 + ampMute, topMute);

                             // complete unmuted plus or minus color for rest of interval
                             if (plus1)
                                 plusColor.setGLColor(gl);
                             else
                                 minusColor.setGLColor(gl);

                             gl.glVertex2f(x0, topMute);
                             gl.glVertex2f(x0 + ampMute, topMute);
                             gl.glVertex2f(x0, t1);
                             gl.glVertex2f(x0 + amp1, t1);
                         }
                         else // line crosses at tz; so complete mute color to tz if tz > topMute
                         {
                             float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);

                             if (topMute < tz)
                             {
                                 // complete muted color for first part of interval
                                 gl.glVertex2f(x0, topMute);
                                 gl.glVertex2f(x0 + ampMute, topMute);
                                 // switch to old color and complete to tz
                                 if (plus0)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);
                                 gl.glVertex2f(x0, topMute);
                                 gl.glVertex2f(x0 + ampMute, topMute);
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, tz);
                                 // switch to new color and complete from tz to t1
                                 if (plus1)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, t1);
                                 gl.glVertex2f(x0 + amp1, t1);
                             }
                             else // topMute is > tz; so complete mute color to tz and then to topMute and new color beyond
                             {
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, topMute);
                                 gl.glVertex2f(x0 + ampMute, topMute);

                                 if (plus1)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);

                                 gl.glVertex2f(x0, topMute);
                                 gl.glVertex2f(x0 + ampMute, topMute);
                                 gl.glVertex2f(x0, t1);
                                 gl.glVertex2f(x0 + amp1, t1);
                             }
                         }
                     }
                 }
                 else // previous point not muted
                 {
                     muted = (t1 > botMute);
                     if (!muted) // still not muted
                     {
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so continue drawing fill color
                         {
                             gl.glVertex2f(x0, t1);
                             gl.glVertex2f(x0 + amp1, t1);
                         }
                         else // line crosses at tz; draw old color to tz and then new color to t1
                         {
                             float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                             gl.glVertex2f(x0, tz);
                             gl.glVertex2f(x0, tz);
                             if (plus1)
                                 plusColor.setGLColor(gl);
                             else
                                 minusColor.setGLColor(gl);
                             gl.glVertex2f(x0, tz);
                             gl.glVertex2f(x0, tz);
                             gl.glVertex2f(x0, t1);
                             gl.glVertex2f(x0 + amp1, t1);
                         }
                     }
                     else // have crossed into muted interval; occurs only as we cross the botMute time
                     {
                         float ampMute = amp0 + (amp1 - amp0) * (botMute - t0) / (t1 - t0);
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so just draw unmuted color to botMute and
                         {
                             // complete unmuted color for first part of interval
                             gl.glVertex2f(x0, botMute);
                             gl.glVertex2f(x0 + ampMute, botMute);

                             // switch to mute color for rest of interval
                             outsideMuteColor.setGLColor(gl);

                             gl.glVertex2f(x0, botMute);
                             gl.glVertex2f(x0 + ampMute, botMute);
                             gl.glVertex2f(x0, t1);
                             gl.glVertex2f(x0 + amp1, t1);
                         }
                         else // line crosses at tz; so complete color to tz if tz < botMute
                         {
                             float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);

                             if (tz < botMute)
                             {
                                 // complete color for first part of interval
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, tz);
                                 gl.glVertex2f(x0, botMute);
                                 gl.glVertex2f(x0 + ampMute, botMute);

                                 // switch to mute color for rest of interval
                                 outsideMuteColor.setGLColor(gl);

                                 gl.glVertex2f(x0, botMute);
                                 gl.glVertex2f(x0 + ampMute, botMute);
                                 gl.glVertex2f(x0, t1);
                                 gl.glVertex2f(x0 + amp1, t1);
                             }
                             else  // tz > botMute, so draw old color to botMute, mute color to tz and then to t1
                             {
                                 gl.glVertex2f(x0, botMute);
                                 gl.glVertex2f(x0 + ampMute, botMute);

                                 // switch to mute color for rest of interval
                                 outsideMuteColor.setGLColor(gl);

                                 gl.glVertex2f(x0, botMute);
                                 gl.glVertex2f(x0 + ampMute, botMute);
                                 gl.glVertex2f(x0, t1);
                                 gl.glVertex2f(x0 + amp1, t1);
                             }
                         }
                     }
                 }
             }
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
         catch (Exception e)
         {
             StsException.outputException("StsPreStackLine.drawFilledWiggleTraces() failed.", e, StsException.WARNING);
         }
         finally
         {
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
     }

     static public void drawFilledWiggleTracesLine(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, StsWiggleDisplayProperties wiggleProperties, int displayInc)
     {
         if (values == null || values.length < 3) return;

         try
         {
             if (!wiggleProperties.hasFill()) return;
             gl.glDisable(GL.GL_LIGHTING);
             StsColor plusColor = wiggleProperties.getWigglePlusColor();
             StsColor minusColor = wiggleProperties.getWiggleMinusColor();
             gl.glLineWidth(1.0f);
             float amp1 = values[nValueMin];
             float t1 = displayZMin;
             boolean plus1 = amp1 >= 0;
             if (plus1)
                 plusColor.setGLColor(gl);
             else
                 minusColor.setGLColor(gl);

             gl.glBegin(GL.GL_LINE_STRIP);
             gl.glVertex2d(x0, t1);
             gl.glVertex2d(x0 + amp1, t1);
             for (int i = 1, n = nValueMin + displayInc; i < nValues; i++, n += displayInc)
             {
                 float t0 = t1;
                 t1 += zInc;
                 float amp0 = amp1;
                 amp1 = values[n];
                 boolean plus0 = plus1;
                 plus1 = amp1 >= 0;

                 if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so continue drawing fill color
                 {
                     gl.glVertex2d(x0, t1);
                     //                            gl.glVertex2d(x0 + amp1, t1);
                 }
                 else // line crosses at tz; draw old color to tz and then new color to t1
                 {
                     double tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                     gl.glVertex2d(x0, tz);
                     //                            gl.glVertex2d(x0, tz);
                     if (plus1)
                         plusColor.setGLColor(gl);
                     else
                         minusColor.setGLColor(gl);
                     gl.glVertex2d(x0, tz);
                     //                            gl.glVertex2d(x0, tz);
                     gl.glVertex2d(x0, t1);
                     //                            gl.glVertex2d(x0 + amp1, t1);
                 }
             }
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
         catch (Exception e)
         {
             StsException.outputException("StsPreStackLine.drawFilledWiggleTraces() failed.", e, StsException.WARNING);
         }
         finally
         {
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
     }

     static public void drawFilledWiggleTracesLine(GL gl, float[] values, int nValueMin, int nValues, float x0, float displayZMin, float zInc, StsWiggleDisplayProperties wiggleProperties, double[] muteRange, int displayInc)
     {
         if (values == null || values.length < 3) return;

         try
         {
             if (!wiggleProperties.hasFill()) return;
             gl.glDisable(GL.GL_LIGHTING);
             StsColor plusColor = wiggleProperties.getWigglePlusColor();
             StsColor minusColor = wiggleProperties.getWiggleMinusColor();
             gl.glLineWidth(1.0f);
             float amp1 = values[nValueMin];
             float t1 = displayZMin;
             boolean plus1 = amp1 >= 0;
             double topMute = muteRange[0];
             double botMute = muteRange[1];
             boolean muted = (t1 < topMute || t1 > botMute);
             if (muted) // start color is mute
                 outsideMuteColor.setGLColor(gl);
             else // start color is plus or minus color
             {
                 if (plus1)
                     plusColor.setGLColor(gl);
                 else
                     minusColor.setGLColor(gl);
             }
             gl.glBegin(GL.GL_LINE_STRIP);
             gl.glVertex2d(x0, t1);
             gl.glVertex2d(x0 + amp1, t1);
             for (int i = 1, n = nValueMin + displayInc; i < nValues; i++, n += displayInc)
             {
                 float t0 = t1;
                 t1 += zInc;
                 float amp0 = amp1;
                 amp1 = values[n];
                 boolean plus0 = plus1;
                 plus1 = amp1 >= 0;

                 if (muted) // previous point is muted
                 {
                     muted = (t1 < topMute || t1 > botMute);
                     if (muted) // still muted
                     {
                         if (plus0 != plus1) // line does not cross zero, so continue drawing mmuted color to t1
                         {
                             float tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                             //                            gl.glVertex2d(x0, tz);
                             gl.glVertex2f(x0, tz);
                         }
                         gl.glVertex2d(x0, t1);
                         //                        gl.glVertex2d(x0 + amp1, t1);
                     }
                     else // have crossed into unmuted interval; only occurs if previous point is muted and current point is not
                     {
                         double ampMute = amp0 + (amp1 - amp0) * (topMute - t0) / (t1 - t0);
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so just draw unmmuted color to t1
                         {
                             // complete muted color for first part of interval
                             gl.glVertex2d(x0, topMute);
                             //                            gl.glVertex2d(x0 + ampMute, topMute);

                             // complete unmuted plus or minus color for rest of interval
                             if (plus1)
                                 plusColor.setGLColor(gl);
                             else
                                 minusColor.setGLColor(gl);

                             gl.glVertex2d(x0, topMute);
                             //                            gl.glVertex2d(x0 + ampMute, topMute);
                             gl.glVertex2d(x0, t1);
                             //                            gl.glVertex2d(x0 + amp1, t1);
                         }
                         else // line crosses at tz; so complete mute color to tz if tz > topMute
                         {
                             double tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);

                             if (topMute < tz)
                             {
                                 // complete muted color for first part of interval
                                 gl.glVertex2d(x0, topMute);
                                 //                                gl.glVertex2d(x0 + ampMute, topMute);
                                 // switch to old color and complete to tz
                                 if (plus0)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);
                                 gl.glVertex2d(x0, topMute);
                                 //                                gl.glVertex2d(x0 + ampMute, topMute);
                                 gl.glVertex2d(x0, tz);
                                 //                                gl.glVertex2d(x0, tz);
                                 // switch to new color and complete from tz to t1
                                 if (plus1)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);
                                 gl.glVertex2d(x0, tz);
                                 //                                gl.glVertex2d(x0, tz);
                                 gl.glVertex2d(x0, t1);
                                 //                                gl.glVertex2d(x0 + amp1, t1);
                             }
                             else // topMute is > tz; so complete mute color to tz and then to topMute and new color beyond
                             {
                                 gl.glVertex2d(x0, tz);
                                 //                                gl.glVertex2d(x0, tz);
                                 gl.glVertex2d(x0, topMute);
                                 //                                gl.glVertex2d(x0 + ampMute, topMute);

                                 if (plus1)
                                     plusColor.setGLColor(gl);
                                 else
                                     minusColor.setGLColor(gl);

                                 gl.glVertex2d(x0, topMute);
                                 //                                gl.glVertex2d(x0 + ampMute, topMute);
                                 gl.glVertex2d(x0, t1);
                                 //                                gl.glVertex2d(x0 + amp1, t1);
                             }
                         }
                     }
                 }
                 else // previous point not muted
                 {
                     muted = (t1 > botMute);
                     if (!muted) // still not muted
                     {
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so continue drawing fill color
                         {
                             gl.glVertex2d(x0, t1);
                             //                            gl.glVertex2d(x0 + amp1, t1);
                         }
                         else // line crosses at tz; draw old color to tz and then new color to t1
                         {
                             double tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);
                             gl.glVertex2d(x0, tz);
                             //                            gl.glVertex2d(x0, tz);
                             if (plus1)
                                 plusColor.setGLColor(gl);
                             else
                                 minusColor.setGLColor(gl);
                             gl.glVertex2d(x0, tz);
                             //                            gl.glVertex2d(x0, tz);
                             gl.glVertex2d(x0, t1);
                             //                            gl.glVertex2d(x0 + amp1, t1);
                         }
                     }
                     else // have crossed into muted interval; occurs only as we cross the botMute time
                     {
                         double ampMute = amp0 + (amp1 - amp0) * (botMute - t0) / (t1 - t0);
                         if (plus0 && plus1 || !plus0 && !plus1) // line does not cross zero, so just draw unmuted color to botMute and
                         {
                             // complete unmuted color for first part of interval
                             gl.glVertex2d(x0, botMute);
                             //                            gl.glVertex2d(x0 + ampMute, botMute);

                             // switch to mute color for rest of interval
                             outsideMuteColor.setGLColor(gl);

                             gl.glVertex2d(x0, botMute);
                             //                           gl.glVertex2d(x0 + ampMute, botMute);
                             gl.glVertex2d(x0, t1);
                             //                           gl.glVertex2d(x0 + amp1, t1);
                         }
                         else // line crosses at tz; so complete color to tz if tz < botMute
                         {
                             double tz = t0 + amp0 * (t1 - t0) / (amp0 - amp1);

                             if (tz < botMute)
                             {
                                 // complete color for first part of interval
                                 gl.glVertex2d(x0, tz);
                                 //                                gl.glVertex2d(x0, tz);
                                 gl.glVertex2d(x0, botMute);
                                 //                                gl.glVertex2d(x0 + ampMute, botMute);

                                 // switch to mute color for rest of interval
                                 outsideMuteColor.setGLColor(gl);

                                 gl.glVertex2d(x0, botMute);
                                 //                                gl.glVertex2d(x0 + ampMute, botMute);
                                 gl.glVertex2d(x0, t1);
                                 //                                gl.glVertex2d(x0 + amp1, t1);
                             }
                             else  // tz > botMute, so draw old color to botMute, mute color to tz and then to t1
                             {
                                 gl.glVertex2d(x0, botMute);
                                 //                                gl.glVertex2d(x0 + ampMute, botMute);

                                 // switch to mute color for rest of interval
                                 outsideMuteColor.setGLColor(gl);

                                 gl.glVertex2d(x0, botMute);
                                 //                                gl.glVertex2d(x0 + ampMute, botMute);
                                 gl.glVertex2d(x0, t1);
                                 //                                gl.glVertex2d(x0 + amp1, t1);
                             }
                         }
                     }
                 }
             }
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
         catch (Exception e)
         {
             StsException.outputException("StsPreStackLine.drawFilledWiggleTraces() failed.", e, StsException.WARNING);
         }
         finally
         {
             gl.glEnd();
             gl.glEnable(GL.GL_LIGHTING);
         }
     }

     static public int computeInterpolationInterval(float zInc, int approximateNIntervals)
     {
         double[] scale = StsMath.niceScale(0.0, zInc, approximateNIntervals, true);
         return (int) Math.round(zInc / scale[2]);
     }

     static public float[] getDisplayZRange(float[][] axisRanges, float zInc)
     {
         float displayZMin = axisRanges[1][1] - 3 * zInc;
         float displayZMax = axisRanges[1][0] + 3 * zInc;
         return new float[]{displayZMin, displayZMax};
     }

     static public int[] getDisplayRange(StsRotatedGridBoundingBox displayBoundingBox, float[] displayZRange)
     {

         int displayMin = displayBoundingBox.getNearestBoundedSliceCoor(displayZRange[0]);
         int displayMax = displayBoundingBox.getNearestBoundedSliceCoor(displayZRange[1]);
         return new int[]{displayMin, displayMax};
     }

     static public StsColor getPointTypeColor(byte pointType) { return typeColors[pointType]; }

     public static void stack(float[] traces, int nTraces, float[] stackTrace)
     {
         stack(traces, 0, nTraces - 1, stackTrace);
     }

     public static void stack(float[] inputTraces, int iTrace0, int iTraceN, float[] outputTrace)
     {
         if (inputTraces == null || outputTrace == null) return;
         if (inputTraces.length == 0 || inputTraces.length == 0 || outputTrace.length == 0) return;
         int nSamples = outputTrace.length;
         int nTraces = iTraceN - iTrace0 + 1;
         if (nTraces * nSamples > inputTraces.length)
         {
             StsMessage.printMessage("StsTraceUtilities.stack(): nTraces*nSamples >= traces.length");
             return;
         }
         double sum = 0;
         double val = 0;
         int nVals = 0;
         for (int i = 0; i < nSamples; i++)
         {
             sum = 0;
             nVals = 0;
             for (int j = iTrace0; j < iTraceN; j++)
             {
                 val = inputTraces[j * nSamples + i];
                 if (val == 0) continue;
                 sum += val;
                 nVals += 1;
             }
             if (nVals > 0)
                 outputTrace[i] = (float) (sum / nVals);
             else
                 outputTrace[i] = 0;
         }

     }

     public static void stack(float[][] inputTraces, int iTrace0, int iTraceN, float[] outputTrace)
     {
         if (inputTraces == null || outputTrace == null) return;
         if (inputTraces.length < iTraceN || outputTrace.length == 0) return;
         int nSamples = outputTrace.length;
         int nTraces = iTraceN - iTrace0 + 1;
         if (nTraces > inputTraces.length)
         {
             StsMessage.printMessage("StsTraceUtilities.stack(): nTraces > inputTraces.length");
             return;
         }
         if (inputTraces[iTrace0] != null && nSamples > inputTraces[iTrace0].length)
         {
             StsMessage.printMessage("StsTraceUtilities.stack(): nSamples > inputTraces[0].length");
             return;
         }
         double sum = 0;
         double val = 0;
         int nVals = 0;
         for (int i = 0; i < nSamples; i++)
         {
             sum = 0;
             nVals = 0;
             for (int j = iTrace0; j < iTraceN; j++)
             {
                 if (inputTraces[j] == null) continue;
                 val = inputTraces[j][i];
                 if (val == 0) continue;
                 sum += val;
                 nVals += 1;
             }
             if (nVals > 0)
                 outputTrace[i] = (float) (sum / nVals);
             else
                 outputTrace[i] = 0;
         }

     }
 }
