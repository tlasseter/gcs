package com.Sts.Framework.Utilities.Geometry;/* com.Sts.Framework.Utilities.Geometry.StsPolygonIntersect.java
 *
 */

import com.Sts.Framework.Utilities.*;

import java.awt.geom.*;

/**
 * Area of Intersection of Polygons
 * <p/>
 * Algorithm based on http://cap-lore.com/MathPhys/IP/
 * <p/>
 * Adapted 9-May-2006 by Lagado
 */
public class StsPolygonIntersect extends Object
{
    /**
     * return the area of intersection of two polygons
     * <p/>
     * Note: the area result has little more accuracy than a float
     * This is true even if the polygon is specified with doubles.
     */
    public static double intersectionArea(Point2D[] a, Point2D[] b)
    {
        StsPolygonIntersect polygonIntersect = new StsPolygonIntersect();
        return polygonIntersect.inter(a, b);
    }

    //--------------------------------------------------------------------------

    static class Point
    {
        double x;
        double y;

        Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }
    }

    static class Box
    {
        Point min;
        Point max;

        Box(Point min, Point max)
        {
            this.min = min;
            this.max = max;
        }
    }

    static class Rng
    {
        int mn;
        int mx;

        Rng(int mn, int mx)
        {
            this.mn = mn;
            this.mx = mx;
        }
    }

    static class IPoint
    {
        int x;
        int y;
    }

    static class Vertex
    {
        IPoint ip;
        Rng rx;
        Rng ry;
        int in;
    }

    static final double gamut = 500000000.;
    static final double mid = gamut / 2.;

    //--------------------------------------------------------------------------

    private static void range(Point2D[] points, int c, Box bbox)
    {
        while (c-- > 0)
        {
            bbox.min.x = Math.min(bbox.min.x, points[c].getX());
            bbox.min.y = Math.min(bbox.min.y, points[c].getY());
            bbox.max.x = Math.max(bbox.max.x, points[c].getX());
            bbox.max.y = Math.max(bbox.max.y, points[c].getY());
        }
    }

    private static long area(IPoint a, IPoint p, IPoint q)
    {
        return (long) p.x * q.y - (long) p.y * q.x +
            (long) a.x * (p.y - q.y) + (long) a.y * (q.x - p.x);
    }

    private static boolean ovl(Rng p, Rng q)
    {
        return p.mn < q.mx && q.mn < p.mx;
    }

    //--------------------------------------------------------------------------

    private long ssss;
    private double sclx;
    private double scly;

    private void cntrib(int f_x, int f_y, int t_x, int t_y, int w)
    {
        ssss += (long) w * (t_x - f_x) * (t_y + f_y) / 2;
    }

    private void
    fit(Point2D[] x, int cx, Vertex[] ix, int fudge, Box B)
    {
        int c = cx;
        while (c-- > 0)
        {
            ix[c] = new Vertex();
            ix[c].ip = new IPoint();
            ix[c].ip.x = ((int) ((x[c].getX() - B.min.x) * sclx - mid) & ~7)
                | fudge | (c & 1);
            ix[c].ip.y = ((int) ((x[c].getY() - B.min.y) * scly - mid) & ~7)
                | fudge;
        }

        ix[0].ip.y += cx & 1;
        ix[cx] = ix[0];

        c = cx;
        while (c-- > 0)
        {
            ix[c].rx = ix[c].ip.x < ix[c + 1].ip.x ?
                new Rng(ix[c].ip.x, ix[c + 1].ip.x) :
                new Rng(ix[c + 1].ip.x, ix[c].ip.x);
            ix[c].ry = ix[c].ip.y < ix[c + 1].ip.y ?
                new Rng(ix[c].ip.y, ix[c + 1].ip.y) :
                new Rng(ix[c + 1].ip.y, ix[c].ip.y);
            ix[c].in = 0;
        }
    }

    private void
    cross(Vertex a, Vertex b, Vertex c, Vertex d,
          double a1, double a2, double a3, double a4)
    {
        double r1 = a1 / ((double) a1 + a2);
        double r2 = a3 / ((double) a3 + a4);

        cntrib((int) (a.ip.x + r1 * (b.ip.x - a.ip.x)),
            (int) (a.ip.y + r1 * (b.ip.y - a.ip.y)),
            b.ip.x, b.ip.y, 1);
        cntrib(d.ip.x, d.ip.y,
            (int) (c.ip.x + r2 * (d.ip.x - c.ip.x)),
            (int) (c.ip.y + r2 * (d.ip.y - c.ip.y)),
            1);
        ++a.in;
        --c.in;
    }

    private void inness(Vertex[] P, int cP, Vertex[] Q, int cQ)
    {
        int s = 0;
        int c = cQ;
        IPoint p = P[0].ip;

        while (c-- > 0)
        {
            if (Q[c].rx.mn < p.x && p.x < Q[c].rx.mx)
            {
                boolean sgn = 0 < area(p, Q[c].ip, Q[c + 1].ip);
                s += (sgn != Q[c].ip.x < Q[c + 1].ip.x) ? 0 : (sgn ? -1 : 1);
            }
        }
        for (int j = 0; j < cP; ++j)
        {
            if (s != 0)
                cntrib(P[j].ip.x, P[j].ip.y,
                    P[j + 1].ip.x, P[j + 1].ip.y, s);
            s += P[j].in;
        }
    }

    //-------------------------------------------------------------------------

    private double
    inter(Point2D[] a, Point2D[] b)
    {
        int na = a.length;
        int nb = b.length;
        Vertex[] ipa = new Vertex[na + 1];
        Vertex[] ipb = new Vertex[nb + 1];
        Box bbox = new Box(new Point(Double.MAX_VALUE, Double.MAX_VALUE),
            new Point(-Double.MAX_VALUE, -Double.MAX_VALUE));

        if (na < 3 || nb < 3)
            return 0;

        range(a, na, bbox);
        range(b, nb, bbox);

        double rngx = bbox.max.x - bbox.min.x;
        sclx = gamut / rngx;
        double rngy = bbox.max.y - bbox.min.y;
        scly = gamut / rngy;
        double ascale = sclx * scly;
        double minScaleValue = ascale* StsMath.roundOff;
        fit(a, na, ipa, 0, bbox);
        fit(b, nb, ipb, 2, bbox);

        for (int j = 0; j < na; ++j)
        {
            for (int k = 0; k < nb; ++k)
            {
                if (ovl(ipa[j].rx, ipb[k].rx) && ovl(ipa[j].ry, ipb[k].ry))
                {
                    long a1 = -area(ipa[j].ip, ipb[k].ip, ipb[k + 1].ip);
                    long a2 = area(ipa[j + 1].ip, ipb[k].ip, ipb[k + 1].ip);
                    boolean o = a1 < 0;
                    if (o == a2 < 0)
                    {
                        long a3 = area(ipb[k].ip, ipa[j].ip, ipa[j + 1].ip);
                        long a4 = -area(ipb[k + 1].ip, ipa[j].ip,
                            ipa[j + 1].ip);
                        if (a3 < 0 == a4 < 0)
                        {
                            if (o)
                                cross(ipa[j], ipa[j + 1], ipb[k], ipb[k + 1],
                                    a1, a2, a3, a4);
                            else
                                cross(ipb[k], ipb[k + 1], ipa[j], ipa[j + 1],
                                    a3, a4, a1, a2);
                        }
                    }
                }
            }
        }

        inness(ipa, na, ipb, nb);
        inness(ipb, nb, ipa, na);
        if(ssss < minScaleValue) return 0.0f;
        return ssss / ascale;
    }

    //-------------------------------------------------------------------------
    // test the code

    private static Point2D[] toPoints2DArray(double[][] a)
    {
        Point2D[] A = new Point2D[a.length];
        for (int i = 0; i < a.length; i++)
            A[i] = new Point2D.Double(a[i][0], a[i][1]);
        return A;
    }

    private static Point2D[] toPoints2DArray(float[][] a)
    {
        Point2D[] A = new Point2D[a.length];
        for (int i = 0; i < a.length; i++)
            A[i] = new Point2D.Double(a[i][0], a[i][1]);
        return A;
    }

    static public double intersectionArea(double[][] a, double[][] b)
    {
        Point2D[] A = toPoints2DArray(a);
        Point2D[] B = toPoints2DArray(b);

        return intersectionArea(A, B);
    }

    static public float intersectionArea(float[][] a, float[][] b)
    {
        Point2D[] A = toPoints2DArray(a);
        Point2D[] B = toPoints2DArray(b);
        return (float)intersectionArea(A, B);
    }

    private static void trial(double[][] a, double[][] b)
    {
        Point2D[] A = toPoints2DArray(a);
        Point2D[] B = toPoints2DArray(b);
        System.out.println(intersectionArea(A, B) + " " + intersectionArea(A, A));
    }

    private static void trial(float[][] a, float[][] b)
    {
        Point2D[] A = toPoints2DArray(a);
        Point2D[] B = toPoints2DArray(b);
        System.out.println(intersectionArea(A, B) + " " + intersectionArea(A, A));
    }

    static public double[] project2D(double[] H, double[] V, double[] p3)
    {
        double[] p2 = new double[2];
        p2[0] = StsMath.dot(H, p3);
        p2[1] = StsMath.dot(V, p3);
        return p2;
    }

    static public double[] project2D(double[] H, double[] V, float[] p3)
    {
        double[] p2 = new double[2];
        p2[0] = StsMath.dot(H, p3);
        p2[1] = StsMath.dot(V, p3);
        return p2;
    }

    static public float[] project2D(float[] H, float[] V, float[] p3)
    {
        float[] p2 = new float[2];
        p2[0] = StsMath.dot(H, p3);
        p2[1] = StsMath.dot(V, p3);
        return p2;
    }

    static public double[][] project2D(double[] H, double[] V, double[][] p3)
    {
        int nPoints = p3.length;
        double[][] p2 = new double[nPoints][2];
        for(int n = 0; n < nPoints; n++)
            p2[n] = project2D(H, V, p3[n]);
        return p2;
    }

    static public double[][] project2D(double[] H, double[] V, float[][] p3)
    {
        int nPoints = p3.length;
        double[][] p2 = new double[nPoints][2];
        for(int n = 0; n < nPoints; n++)
            p2[n] = project2D(H, V, p3[n]);
        return p2;
    }

    static public float[][] project2D(float[] H, float[] V, float[][] p3)
    {
        int nPoints = p3.length;
        float[][] p2 = new float[nPoints][2];
        for(int n = 0; n < nPoints; n++)
            p2[n] = project2D(H, V, p3[n]);
        return p2;
    }

    public static void main(String[] args)
    {
        double[][] pA = new double[][] { {1, 2, 0}, {1, 2.5, 1}, {1, 3, 0} };
        double[][] pB = new double[][] { {1, 2, 1}, {1, 3, 1}, {1, 2.5, 0} };
        double[] normal = new double[] { 1, 0, 0 };
        double[] Z = new double[] { 0, 0, 1 };
        double[] H = StsMath.cross(normal, Z);
        double[] V = StsMath.cross(normal, H);
        double[][] pA2 = project2D(H, V, pA);
        double[][] pB2 = project2D(H, V, pB);
        trial(pA2, pB2);

        /*
        double a1[][] = {{2, 3}, {2, 3}, {2, 3}, {2, 4}, {3, 3}, {2, 3}, {2, 3}};
        double b1[][] = {{1, 1}, {1, 4}, {4, 4}, {4, 1}, {1, 1}}; // 1/2, 1/2
        // The redundant vertices above are to provoke errors
        // as good test cases should.
        // It is not necessary to duplicate the first vertex at the end.

        double a2[][] = {{1, 7}, {4, 7}, {4, 6}, {2, 6}, {2, 3}, {4, 3}, {4, 2}, {1, 2}};
        double b2[][] = {{3, 1}, {5, 1}, {5, 4}, {3, 4}, {3, 5}, {6, 5}, {6, 0}, {3, 0}}; // 0, 9

        double a3[][] = {{1, 1}, {1, 2}, {2, 1}, {2, 2}};
        double b3[][] = {{0, 0}, {0, 4}, {4, 4}, {4, 0}}; // 0, 1/2

        double a4[][] = {{0, 0}, {3, 0}, {3, 2}, {1, 2}, {1, 1}, {2, 1}, {2, 3}, {0, 3}};
        double b4[][] = {{0, 0}, {0, 4}, {4, 4}, {4, 0}}; // -9, 11

        double a5[][] = {{0, 0}, {1, 0}, {0, 1}};
        double b5[][] = {{0, 0}, {0, 1}, {1, 1}, {1, 0}}; // -1/2, 1/2

        double a6[][] = {{1, 3}, {2, 3}, {2, 0}, {1, 0}};
        double b6[][] = {{0, 1}, {3, 1}, {3, 2}, {0, 2}}; // -1, 3

        double a7[][] = {{0, 0}, {0, 2}, {2, 2}, {2, 0}};
        double b7[][] = {{1, 1}, {3, 1}, {3, 3}, {1, 3}}; // -1, 4

        double a8[][] = {{0, 0}, {0, 4}, {4, 4}, {4, 0}};
        double b8[][] = {{1, 1}, {1, 2}, {2, 2}, {2, 1}}; // 1, 16

        trial(a1, b1);
        trial(a2, b2);
        trial(a3, b3);
        trial(a4, b4);
        trial(a5, b5);
        trial(a6, b6);
        trial(a7, b7);
        trial(a8, b8);
        */
    }
}
