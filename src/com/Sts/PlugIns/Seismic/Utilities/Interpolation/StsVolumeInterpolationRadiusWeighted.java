package com.Sts.PlugIns.Seismic.Utilities.Interpolation;

/**
 * <p>Title: S2S Development</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: S2S Systems LLC</p>
 * @author TJLasseter
 * @version beta 1.0
 */

import com.Sts.Framework.Utilities.*;
import com.Sts.PlugIns.Seismic.DBTypes.*;

/** Given dZdX and dZdY at each interpolatingPoint, compute 1/R weighted
 *  estimate of Z at point.
 */
public class StsVolumeInterpolationRadiusWeighted
{
    private StsSeismicVolume volume;
    private int rowMin, rowMax, colMin, colMax, sliceMin, sliceMax;
    private int iCenter, jCenter;
    private int nSpirals = 0;
    private double maxDistance, minDistance;
    private double weightSum, weightZ;
    private Point[] points = new Point[maxNPoints];
    private int nPoints = 0;
    private int nSlice;
    private float z;
    static final int minNPoints = 3;
    static final int maxNPoints = 6;

    private StsVolumeInterpolationRadiusWeighted(StsSeismicVolume volume)
    {
        this.volume = volume;
        rowMin = 0;
        rowMax = volume.nRows-1;
        colMin = 0;
        colMax = volume.nCols-1;
        sliceMin = 0;
        sliceMax = volume.nSlices-1;
    }

    static public StsVolumeInterpolationRadiusWeighted constructor(StsSeismicVolume volume)
    {
        return new StsVolumeInterpolationRadiusWeighted(volume);
    }

    public float interpolate(int row, int col, int slice)
    {
        iCenter = row;
        jCenter = col;
        this.nSlice = nSlice;
        doInterpolate();
        return z;
    }

    public boolean doInterpolate()
    {
        boolean doBottom, doTop, doLeft, doRight;
        int i, j;

        try
        {
            int imin = iCenter;
            int imax = iCenter;
            int jmin = jCenter;
            int jmax = jCenter;

//            points = new ArrayList(100);

            nSpirals = 0;
            nPoints = 0;
//            foundMaxNPoints = false;

            while(true)
            {
                boolean terminate = true;

                doBottom = imin > rowMin;
                if(doBottom) { imin--; terminate = false; }
                doTop = imax < rowMax;
                if(doTop) { imax++; terminate = false; }
                doLeft = jmin > colMin;
                if(doLeft) { jmin--;  terminate = false; }
                doRight = jmax < colMax;
                if(doRight) { jmax++;  terminate = false; }

                if(terminate) break;

                if(doBottom)
                    for(j = jmin; j < jmax; j++)
                        addPoint(imin, j);
                /*
                {
                    for(j = jCenter; j >= jmin; j--)
                        addPoint(imin, j);
                    for(j = jCenter+1; j < jmax; j++)
                        addPoint(imin, j);
                }
                */
                if(doRight)
                    for(i = imin; i < imax; i++)
                        addPoint(i, jmax);

                /*
                {
                    for(i = iCenter; i >= imin; i--)
                        addPoint(i, jmax);
                    for(i = iCenter+1; i < imax; i++)
                        addPoint(i, jmax);
                }
                */
                if(doTop)
                    for(j = jmin+1; j <= jmax; j++)
                        addPoint(imax, j);
                /*
                {
                    for(j = jCenter; j > jmin; j--)
                        addPoint(imax, j);
                    for(j = jCenter+1; j <= jmax; j++)
                        addPoint(imax, j);
                }
                */

                if(doLeft)
                    for(i = imin+1; i <= imax; i++)
                        addPoint(i, jmin);
                /*
                {
                    for(i = iCenter; i > imin; i--)
                        addPoint(i, jmin);
                    for(i = iCenter+1; i <= imax; i++)
                        addPoint(i, jmin);
                }
                */

                nSpirals++;
                if(isConverged()) break;
            }
            computeZ();
            return true;
        }
        catch(Exception e)
        {
            StsException.outputWarningException(this, "doInterpolate", " for row: " + iCenter + " of " + rowMin + "-" + rowMax +
					" rows and " + jCenter + " of " + colMin + "-" + colMax + " cols.", e);
            return false;
        }
    }

    public void addPoint(int i, int j)
    {

        if(nPoints >= maxNPoints) return;
        int intValue = volume.getIntValue(i, j, nSlice);
        if(intValue == 255) return;
//        float floatValue = Math.
//        points.add(new Point(i, j, pointsZ[i][j]));
//        points[nPoints++] = new Point(i, j, pointsZ[i][j]);
    }

    private boolean isConverged()
    {
        return nPoints >= maxNPoints;
    }
/*
    private boolean isConverged()
    {
        int nPoints = points.size();
        if(!foundMaxNPoints && nPoints >= maxNPoints)
        {
            foundMaxNPoints = true;
            maxSpiral = StsMath.ceiling(nSpirals*1.41421356);
            return false;
        }
        else if(foundMaxNPoints && nSpirals == maxSpiral)
            return true;
        else
            return false;
    }
*/
    private void computeZ()
    {
        try
        {
            weightSum = 0.0;
            weightZ = 0.0f;

//            int nPoints = points.size();
            if(nPoints == 0) return;

            nPoints = Math.min(maxNPoints, nPoints);
//            Collections.sort(points);
//            maxDistance = ((Point)points.get(nPoints-1)).distance;
//            minDistance = ((Point)points.get(0)).distance;
            maxDistance = points[nPoints-1].distance;
            minDistance = points[0].distance;
            if(nPoints < minNPoints || minDistance == maxDistance)
            {
                for(int n = 0; n < nPoints; n++)
                    points[n].sum();
//                    ((Point)points.get(n)).sum();
            }
            else
            {
                for(int n = 0; n < nPoints; n++)
                {
                    double distance = points[n].distance;
//                    double distance = ((Point)points.get(n)).distance;
                    double dwt = (distance - maxDistance);
                    dwt = dwt*dwt/(distance*maxDistance);

                    weightZ += points[n].z*dwt;
//                    weightZ += ((Point)points.get(n)).z*dwt;
                    weightSum += dwt;
                }
            }

            float z = (float)(weightZ/weightSum);
//            pointsZ[iCenter][jCenter] = z;
//            pointsNull[iCenter][jCenter] = interpolatedNullType;
        }
        catch(Exception e)
        {
            StsException.outputException ("StsSurfaceInterpolationRadiusWeighted.computeZ() failed.",
                e, StsException.WARNING);
        }
    }

    class Point implements Comparable
    {
        int i, j;
        float z;
        int distSq;
        double distance;
        double weight;

        private Point(int i, int j, float z)
        {
            this.i = i;
            this.j = j;
            this.z = z;
            int dx = j - jCenter;
            int dy = i - iCenter;
            distSq = dx*dx + dy*dy;
            distance = Math.sqrt(distSq);
            weight = 1.0/distance;
        }

        private void sum()
        {
//            float z = pointsZ[i][j];
            weightSum += weight;
            weightZ += weight*z;
        }

        public int compareTo(Object other)
        {
            double otherWeight = ((Point)other).weight;
            if(weight > otherWeight) return -1;
            else if(weight < otherWeight) return 1;
            else return 0;
        }
    }
}