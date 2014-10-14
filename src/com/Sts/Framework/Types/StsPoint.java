//Title:        S2S: Seismic-to-simulation
//Version:
//Copyright:    Copyright (c) 2001
//Author:       Tom Lasseter
//Company:      4D Systems LLC
//Description:  Web-enabled Integrated Interpretation System

package com.Sts.Framework.Types;

import com.Sts.Framework.DB.*;
import com.Sts.Framework.DBTypes.*;
import com.Sts.Framework.Utilities.*;

import java.io.*;
import java.util.*;

public class StsPoint extends StsSerialize implements Cloneable, StsSerializable, Serializable, Comparable
{
    public float[] v = null;

	/** bit type combinations used in calling distanceSquaredType method */
	static public final int DIST_X = 4;
	static public final int DIST_Y = 2;
	static public final int DIST_Z = 1;
	static public final int DIST_XY = 6;
	static public final int DIST_XZ = 5;
	static public final int DIST_YZ = 3;
	static public final int DIST_XYZ = 7;

	/** indices of parameter locations in v[] vector */
	static public final int X = 0;
	static public final int Y = 1;
	static public final int Z = 2; //depth
	static public final int T = 3; // time
	static public final int M = 4; // measured depth
	static public final int F = 5; // parametric coefficient

    static final float nullValue = StsParameters.nullValue;

	static public int compareIndex = 2;
	static PointComparator comparator = null;

	public StsPoint()
	{
	}

	public StsPoint(int length)
	{
		createVector(length);
	}

	public StsPoint(int length, StsPoint oldPoint)
	{
		int oldLength = oldPoint.getLength();
		int newLength = Math.max(length, oldLength);
		createVector(newLength);
		System.arraycopy(oldPoint.v, 0, v, 0, oldLength);
	}

	public StsPoint(float x, float y, float z)
	{
		createVector(3);
		v[X] = x;
		v[Y] = y;
		v[Z] = z;
	}

	public StsPoint(float x, float y)
	{
		createVector(3);
		v[X] = x;
		v[Y] = y;
		v[Z] = 0.0f;
	}

	public StsPoint(double x, double y, double z)
	{
		createVector(3);
		v[X] = (float) x;
		v[Y] = (float) y;
		v[Z] = (float) z;
	}

	public StsPoint(float[] vv)
	{
		if(vv == null) return;
		createVector(vv.length);
		System.arraycopy(vv, 0, v, 0, getLength());
	}

	public StsPoint(double[] vv)
	{
		if(vv == null) return;
		createVector(vv.length);
		for(int n = 0; n < getLength(); n++)
			v[n] = (float) vv[n];
	}

	/** construct a 4D point from a 3D point plus F value. */

	public StsPoint(StsPoint point, float vf)
	{
		this(point.v, vf);
	}

	public StsPoint(float[] vv, float vf)
	{
		createVector(4);
		for(int n = 0; n < 3; n++)
			v[n] = vv[n];
		v[T] = vf;
	}

	public StsPoint(StsPoint point)
	{
		this(point.v);
	}

	public StsPoint(double x, double y, double z, double f)
	{
		createVector(4);
		v[X] = (float) x;
		v[Y] = (float) y;
		v[Z] = (float) z;
		v[F] = (float) f;
	}

	public StsPoint(double x, double y, double z, double m, double t)
	{
		createVector(5);
		v[X] = (float) x;
		v[Y] = (float) y;
		v[Z] = (float) z;
		v[M] = (float) m;
		v[T] = (float) t;
	}

	public boolean checkExtendVector(int newLength)
	{
		if(getLength() >= newLength) return false;
		float[] newV = new float[newLength];
		System.arraycopy(v, 0, newV, 0, getLength());
		v = newV;
		return true;
	}

	static public StsPoint nullPoint(int length)
	{
		float[] v = new float[length];
		Arrays.fill(v, StsParameters.nullValue);
		return new StsPoint(v);
	}

	private void createVector(int length)
	{
		v = new float[length];
	}

	// Methods for StsPoint input
	public float[] getPointValues()
	{
		return v;
	}

	public boolean subPoints(StsPoint point0, StsPoint point1)
	{
		if(point0 == null || point1 == null) return false;

		int minLength = StsMath.min3(getLength(), point0.getLength(), point1.getLength());

		for(int n = 0; n < minLength; n++)
			v[n] = point0.v[n] - point1.v[n];

		return true;
	}

	static public StsPoint subPointsStatic(StsPoint point0, StsPoint point1)
	{
		if(point0 == null || point1 == null) return null;

		int minLength = Math.min(point0.getLength(), point1.getLength());
		StsPoint point = new StsPoint(minLength);

		for(int n = 0; n < minLength; n++)
			point.v[n] = point0.v[n] - point1.v[n];

		return point;
	}

	static public StsPoint subPointsStatic(StsPoint point0, StsPoint point1, int minLength)
	{
		if(point0 == null || point1 == null) return null;
		StsPoint point = new StsPoint(minLength);

		for(int n = 0; n < minLength; n++)
			point.v[n] = point0.v[n] - point1.v[n];

		return point;
	}

	static public StsPoint addPointsStatic(StsPoint point0, StsPoint point1)
	{
		if(point0 == null || point1 == null) return null;

		int minLength = Math.min(point0.getLength(), point1.getLength());
		StsPoint point = new StsPoint(minLength);

		for(int n = 0; n < minLength; n++)
			point.v[n] = point0.v[n] + point1.v[n];

		return point;
	}

	public void addPoints(StsPoint point0, StsPoint point1)
	{
		if(point0 == null || point1 == null) return;

		int minLength = Math.min(point0.getLength(), point1.getLength());

		for(int n = 0; n < minLength; n++)
			v[n] = point0.v[n] + point1.v[n];
	}

	public boolean crossProduct(StsPoint vA, StsPoint vB)
	{
		if(vA == null || vB == null) return false;

		int minLength = StsMath.min3(getLength(), vA.getLength(), vB.getLength());

		if(minLength >= 3)
		{
			v[X] = vA.v[Y] * vB.v[Z] - vA.v[Z] * vB.v[Y];
			v[Y] = vA.v[Z] * vB.v[X] - vA.v[X] * vB.v[Z];
			v[Z] = vA.v[X] * vB.v[Y] - vA.v[Y] * vB.v[X];
		}

		return true;
	}

	static public StsPoint leftCrossProductStatic(StsPoint vA, StsPoint vB)
	{
		if(vA == null || vB == null) return null;
		int minLength = Math.min(vA.getLength(), vB.getLength());
		if(minLength < 3) return null;
		StsPoint point = new StsPoint(minLength);
		float[] v = point.v;
		v[X] = -vA.v[Y] * vB.v[Z] + vA.v[Z] * vB.v[Y];
		v[Y] = -vA.v[Z] * vB.v[X] + vA.v[X] * vB.v[Z];
		v[Z] = -vA.v[X] * vB.v[Y] + vA.v[Y] * vB.v[X];
		return point;
	}

	public boolean leftCrossProduct(StsPoint vA, StsPoint vB)
	{
		if(vA == null || vB == null) return false;

		int minLength = StsMath.min3(getLength(), vA.getLength(), vB.getLength());

		if(minLength >= 3)
		{
			v[X] = -vA.v[Y] * vB.v[Z] + vA.v[Z] * vB.v[Y];
			v[Y] = -vA.v[Z] * vB.v[X] + vA.v[X] * vB.v[Z];
			v[Z] = -vA.v[X] * vB.v[Y] + vA.v[Y] * vB.v[X];
		}

		return true;
	}

// Methods for float[] input


	public boolean subPoints(float[] vA, float[] vB)
	{
		if(vA == null || vB == null) return false;

		int minLength = StsMath.min3(getLength(), vA.length, vB.length);

		for(int n = 0; n < minLength; n++)
			v[n] = vA[n] - vB[n];

		return true;
	}

	public void crossProduct(float[] vA, float[] vB)
	{
		int minLength = StsMath.min3(getLength(), vA.length, vB.length);

		if(minLength >= 3)
		{
			v[X] = vA[1] * vB[2] - vA[2] * vB[1];
			v[Y] = vA[2] * vB[0] - vA[0] * vB[2];
			v[Z] = vA[0] * vB[1] - vA[1] * vB[0];
		}
	}

	static public float crossProduct2D(StsPoint a1, StsPoint a0, StsPoint b1, StsPoint b0)
	{
		float ax = a1.v[X] - a0.v[X];
		float ay = a1.v[Y] - a0.v[Y];

		float bx = b1.v[X] - b0.v[X];
		float by = b1.v[Y] - b0.v[Y];

		return ax * by - bx * ay;
	}

	static public float crossProduct2D(StsPoint a, StsPoint b)
	{
		return a.v[X] * b.v[Y] - b.v[X] * a.v[Y];
	}

	public void interpolatePoints(float[] vA, float[] vB, float f)
	{
		int minLength = StsMath.min3(getLength(), vA.length, vB.length);

		if(f == 0.0)
		{
			for(int n = 0; n < minLength; n++)
				v[n] = vA[n];
		}
		else if(f == 1.0)
		{
			for(int n = 0; n < minLength; n++)
				v[n] = vB[n];
		}
		else
		{
			for(int n = 0; n < minLength; n++)
				v[n] = vA[n] + f * (vB[n] - vA[n]);
		}
	}

	public void interpolatePoints(StsPoint A, StsPoint B, double f)
	{
		interpolatePoints(A.v, B.v, (float) f);
	}

	public void interpolatePoints(StsPoint A, StsPoint B, float f)
	{
		interpolatePoints(A.v, B.v, f);
	}

	static public StsPoint staticInterpolatePoints(StsPoint A, StsPoint B, float f)
	{
		try
		{
			int length = Math.min(A.v.length, B.v.length);
			StsPoint point = new StsPoint(length);
			point.interpolatePoints(A.v, B.v, f);
			return point;
		}
		catch(Exception e)
		{
			StsException.systemError("StsPoint.staticInterpolatePoints() failed.");
			return null;
		}
	}

	static public StsPoint staticInterpolatePoints(int length, StsPoint A, StsPoint B, float f)
	{
		try
		{
			StsPoint point = new StsPoint(length);
			point.interpolatePoints(A.v, B.v, f);
			return point;
		}
		catch(Exception e)
		{
			StsException.systemError("StsPoint.staticInterpolatePoints() failed.");
			return null;
		}
	}

	public void interpolatePoints(int length, float[] vA, float[] vB, float f)
	{
		int minLength = StsMath.min4(length, this.getLength(), vA.length, vB.length);

		for(int n = 0; n < minLength; n++)
			v[n] = vA[n] + f * (vB[n] - vA[n]);
	}

	static public StsPoint staticInterpolatePoints(float[] xyzA, float[] xyzB, float f)
	{
		try
		{
			int length = Math.min(xyzA.length, xyzB.length);
			StsPoint point = new StsPoint(length);
			point.interpolatePoints(xyzA, xyzB, f);
			return point;
		}
		catch(Exception e)
		{
			StsException.systemError("StsPoint.staticInterpolatePoints() failed.");
			return null;
		}
	}

	public void interpolatePoints(int length, double[] vA, double[] vB, double f)
	{
		int minLength = StsMath.min4(length, this.getLength(), vA.length, vB.length);

		for(int n = 0; n < minLength; n++)
			v[n] = (float) (vA[n] + f * (vB[n] - vA[n]));
	}

	public void interpolatePoints(int length, StsPoint A, StsPoint B, float f)
	{
		interpolatePoints(length, A.v, B.v, f);
	}

	/**
	 * Assume that we wish to normalize vector components 0, 1, and 2
	 * other components are not included as they are parametric coordinates
	 */
	public float normalizeReturnLength()
	{
		int n;

		int length = Math.min(this.getLength(), 3);

		double sumSq = 0.0;

		for(n = 0; n < length; n++)
			sumSq += v[n] * v[n];

		float vLength = (float) Math.sqrt(sumSq);

		if(vLength > 0.0)
		{
			for(n = 0; n < length; n++)
				v[n] /= vLength;
		}

		return vLength;
	}

	/**
	 * Assume that we wish to normalize xyz vector components,
	 * i.e., we want the length of the xyz vector to be 1.0.
	 * divide other components by this amount so we interpolate.
	 */
	public float normalizeXYZReturnLength()
	{
		int n;

		int length = Math.min(this.getLength(), 3);

		double sumSq = 0.0;

		for(n = 0; n < length; n++)
			sumSq += v[n] * v[n];

		float vLength = (float) Math.sqrt(sumSq);

		if(vLength > 0.0)
		{
			for(n = 0; n < this.getLength(); n++)
				v[n] /= vLength;
		}
		return vLength;
	}

	/**
	 * Assume that we wish to normalize vector components 0, 1, and 2
	 * other components are not included as they are parametric coordinates
	 */
	public boolean normalize()
	{
		int n;

		int length = Math.min(this.getLength(), 3);

		double sumSq = 0.0;

		for(n = 0; n < length; n++)
			sumSq += v[n] * v[n];

		float vLength = (float) Math.sqrt(sumSq);

		if(vLength <= 0.0) return false;

		for(n = 0; n < length; n++)
			v[n] /= vLength;

		return true;
	}

	/** normalizes XYZ and applies normalization factor to other components */
	public boolean normalizeXYZ()
	{
		int n;
		int length = Math.min(this.getLength(), 3);
		double sumSq = 0.0;

		for(n = 0; n < length; n++)
			sumSq += v[n] * v[n];

		float vLength = (float) Math.sqrt(sumSq);
		if(vLength <= 0.0f) return false;

		for(n = 0; n < this.getLength(); n++)
			v[n] /= vLength;
		return true;
	}

	/** vertical distance from this point to an otherPoint */
	public float verticalDistance(StsPoint otherPoint)
	{
		return (float) Math.abs(getZ() - otherPoint.getZ());
	}

	/** horizontal distance from this point to an otherPoint */
	public float horizontalDistance(StsPoint otherPoint)
	{
		float dv, sum = 0.0f;

		for(int n = 0; n < 2; n++)
		{
			dv = otherPoint.v[n] - this.v[n];
			sum += dv * dv;
		}
		return (float) Math.sqrt(sum);
	}

	/** distance from this point to an otherPoint */
	public float distance(StsPoint otherPoint)
	{
		return (float) Math.sqrt(distanceSquared(otherPoint));
	}

	/** angle from this point to an otherPoint */
	public double fromAngle(StsPoint otherPoint)
	{
		double dx = getX() - otherPoint.getX();
		double dy = v[Y] - otherPoint.v[Y];
		return StsMath.atan2(dx, dy);
	}

	/** dip from this point to an otherPoint */
	public float fromDip(StsPoint otherPoint)
	{
		float dip = (float) Math.atan((getZ() - otherPoint.getZ()) / distance(2, otherPoint));
		return dip;
	}

	/** distance-squared from this point to an otherPoint */
	public float distanceSquared(StsPoint otherPoint)
	{
		int length = StsMath.min3(3, this.v.length, otherPoint.v.length);
		float dv, sum = 0.0f;

		for(int n = 0; n < length; n++)
		{
			dv = otherPoint.v[n] - this.v[n];
			sum += dv * dv;
		}

		return sum;
	}

	/**
	 * distance from this point to an otherPoint
	 * @param length the number of dimensions to use
	 */
	public float distance(int length, StsPoint otherPoint)
	{
		return (float) Math.sqrt(distanceSquared(otherPoint, length));
	}

	/**
	 * Return a distance from thisPoint to otherPoint defined by the type
	 * XYZ: distance in 3D, XY: distance in XY, etc.
	 */
	public float distanceType(int type, StsPoint otherPoint)
	{
		return (float) Math.sqrt(distanceSquaredType(type, otherPoint));
	}

	public float distanceType(int type, float[] otherPointV)
	{
		return (float) Math.sqrt(distanceSquaredType(type, otherPointV));
	}

	/**
	 * distance-squared from this point to an otherPoint
	 * @param length the number of dimensions to use
	 */
	public float distanceSquared(StsPoint otherPoint, int length)
	{
		int minLength = StsMath.min3(length, this.v.length, otherPoint.v.length);
		float dv, sum = 0.0f;

		for(int n = 0; n < minLength; n++)
		{
			dv = otherPoint.v[n] - this.v[n];
			sum += dv * dv;
		}

		return sum;
	}

	public float distanceSquaredType(int type, StsPoint otherPoint)
	{
		return distanceSquaredType(type, otherPoint.v);
	}

	public float distanceSquaredType(int type, float[] otherPointV)
	{
		boolean x, y, z;
		int minLength;

		/** The type defines which coordinates to use: x is the 4-bit,
		 *  y is the 2-bit, and Z is the 1-bit. Default (0) is x & y.
		 */

		if(type == 0)
		{
			x = true;
			y = true;
			z = false;
		}
		else
		{
			x = (type & 4) >> 2 == 1;
			y = (type & 2) >> 1 == 1;
			z = (type & 1) == 1;
		}

		if(z) minLength = 3;
		else if(y) minLength = 2;
		else if(x) minLength = 1;
		else return StsParameters.largeFloat;

		int pointsLength = Math.min(this.v.length, otherPointV.length);
		if(pointsLength < minLength) return StsParameters.largeFloat;

		float dv, sum = 0.0f;

		if(x)
		{
			dv = otherPointV[0] - this.v[X];
			sum += dv * dv;
		}
		if(y)
		{
			dv = otherPointV[1] - this.v[Y];
			sum += dv * dv;
		}
		if(z)
		{
			dv = otherPointV[2] - this.v[Z];
			sum += dv * dv;
		}

		return sum;
	}

	public float projectedInterpolationFactor(int length, StsPoint point1, StsPoint point2)
	{
		int minLength = StsMath.min4(length, this.getLength(), point1.v.length, point2.v.length);

		float distSq1 = distanceSquared(point1, minLength);
		float distSq2 = distanceSquared(point2, minLength);
		float distSq12 = point1.distanceSquared(point2, minLength);

		if(distSq12 <= 0.0f)
			return 0.5f;
		else
			return 0.5f + 0.5f * (distSq1 - distSq2) / distSq12;
	}

	/* length of a vector defined by a point (distance from origin ) */
	public float length()
	{
		float sum = 0.0f;

		for(int n = 0; n < getLength(); n++)
			sum += v[n] * v[n];

		return (float) Math.sqrt(sum);
	}

	public float lengthSq()
	{
		float sum = 0.0f;
		for(int n = 0; n < getLength(); n++)
			sum += v[n] * v[n];
		return sum;
	}

	/* length of a XY vector defined by a point (distance from origin ) */
	public float lengthXY()
	{
		return (float) Math.sqrt(v[X] * v[X] + v[Y] * v[Y]);
	}

	public float lengthXYZ()
	{
		return (float) Math.sqrt(v[X] * v[X] + v[Y] * v[Y] + v[Z] * v[Z]);
	}

	/** dot product length is assumed to be a max of 3 xyz, or xy */
	public float dot(StsPoint otherPoint)
	{
		int length = StsMath.min3(3, this.v.length, otherPoint.v.length);
		return dot(otherPoint, length);
	}

	public float dot(StsPoint otherPoint, int length)
	{
		float product, sum = 0.0f;

		for(int n = 0; n < length; n++)
			sum += otherPoint.v[n] * this.v[n];

		return sum;
	}

	static public StsPoint multByConstantAddPointStatic(StsPoint p0, float f, StsPoint p1)
	{
		float[] v0 = p0.v;
		float[] v1 = p1.v;
		int len = Math.min(v0.length, v1.length);
		StsPoint p = new StsPoint(len);
		for(int i = 0; i < len; i++)
			p.v[i] = p0.v[i] * f + p1.v[i];
		return p;
	}

	public void multByConstantAddPoint(StsPoint p0, float f, StsPoint p1)
	{
		float[] v0 = p0.v;
		float[] v1 = p1.v;
		int len = StsMath.min3(getLength(), v0.length, v1.length);
		for(int i = 0; i < len; i++)
			this.v[i] = p0.v[i] * f + p1.v[i];
	}

	public void addScaledVector(StsPoint p0, float f)
	{
		float[] v0 = p0.v;
		int len = Math.min(getLength(), v0.length);
		for(int i = 0; i < len; i++)
			v[i] += v0[i] * f;
	}

    public StsPoint addXYVector(float angle, float length)
    {
        StsPoint point = this.copy();
        point.v[0] += length*(float)StsMath.cosd(angle);
        point.v[1] += length*(float)StsMath.sind(angle);
        return point;
    }

	public void multiply(float f)
	{
		for(int i = 0; i < v.length; i++)
			v[i] *= f;
	}

	public void multiply(double d)
	{
		for(int i = 0; i < v.length; i++)
			v[i] *= d;
	}

	public void divide(float f)
	{
		f = 1 / f;
		for(int i = 0; i < v.length; i++)
			v[i] *= f;
	}

	public void divide(double d)
	{
		d = 1 / d;
		for(int i = 0; i < v.length; i++)
			v[i] *= d;
	}

	public void average(StsPoint p0, StsPoint p1)
	{
		for(int i = 0; i < 3; i++)
		{
			v[i] = (p0.v[i] + p1.v[i]) / 2.0f;
		}
	}

	public void reverse()
	{
		int length = this.v.length;

		for(int n = 0; n < length; n++)
			v[n] = -v[n];
	}

	static public StsPoint reverseStatic(StsPoint point)
	{
		int length = point.v.length;
		StsPoint reverse = new StsPoint(length);
		for(int n = 0; n < length; n++)
			reverse.v[n] = -point.v[n];
		return reverse;
	}

	public StsPoint rotationVectorLeft(StsPoint otherVector)
	{
		float thisLength = this.length();
		if(thisLength <= 0.0f) return null;
		float otherLength = otherVector.length();
		if(otherLength <= 0.0f) return null;
		if(thisLength != 1.0f) normalize();
		if(otherLength != 1.0f) otherVector.normalize();
		StsPoint cross = new StsPoint(3);
		if(!cross.leftCrossProduct(this, otherVector)) return null;
		return cross;
	}

	public String toString()
	{
		if(v == null) return "null";
		switch(v.length)
		{
			case 0:
				return "empty";
			case 1:
				return " X: " + this.v[X];
			case 2:
				return " XY: " + this.v[X] + " " + this.v[Y];
			case 3:
				return " XYZ: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z];
			case 4:
				return " XYZT: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z] + " " + this.v[T];
			case 5:
				return " XYZTM: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z] + " " + this.v[T] + " " + this.v[M];
			default:
				return " XYZTMF: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z] + " " + this.v[T] + " " + this.v[M] + " " + this.v[F];
		}
	}

	public void print()
	{
		System.out.println(" XYZ: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z]);
	}

	public void print(String string, int index)
	{
		System.out.println(string + " " + index + " XYZ: " + this.v[X] + " " + this.v[Y] + " " + this.v[Z]);
	}

	// Accessors
	public void setX(float x)
	{
		v[X] = x;
	}

	public float getX()
	{
        if(v.length > X)
		    return v[X];
        else
            return nullValue;
	}

	public void setY(float y)
	{
		v[Y] = y;
	}

	public float getY()
	{
        if(v.length > Y)
            return v[Y];
        else
            return nullValue;
	}

	public void setZ(float z)
	{
		v[Z] = z;
	}

	public float getZ()
	{
        if(v.length > Z)
            return v[Z];
        else
            return nullValue;
	}

	public void shiftY(float shift)
	{
		v[Y] += shift;
	}

	static public void shiftY(StsPoint[] points, float shift)
	{
		int nPoints = points.length;
		for(int i = 0; i < nPoints; i++)
			points[i].shiftY(shift);
	}

	public float getZorT()
	{
		if(isDepth || getLength() < 5)
			return v[Z];
		else
			return v[T];
	}

	public float getZorT(boolean isDepth)
	{
		if(isDepth || getLength() < 5)
			return v[Z];
		else
			return v[T];
	}

	/*
	 public float getZ(boolean isDepth)
	 {
		 if (isDepth || length < 5)return v[Z];
		 else return v[T];
	 }
 */
	public void setZorT(float zt)
	{
		setZorT(zt, isDepth);
	}

	public void setZorT(float zt, boolean isDepth)
	{
		if(isDepth)
			v[Z] = zt;
		else
		{
			if(getLength() >= 5)
			{
				v[T] = zt;
			}
			else
			{
				float[] vOld = v;
				int lengthOld = getLength();
				createVector(5);
				System.arraycopy(vOld, 0, v, 0, lengthOld);
				v[T] = zt;
			}
		}
	}

	public void setF(float f)
	{
		int oldLength = v.length;
		if(oldLength < 4)
		{
			float[] oldV = v;
			createVector(4);
			System.arraycopy(oldV, 0, v, 0, oldLength);
			v[T] = f;
		}
		else if(oldLength == 4)
			v[T] = f;
		else if(oldLength < 6)
		{
			float[] oldV = v;
			createVector(6);
			System.arraycopy(oldV, 0, v, 0, oldLength);
			v[F] = f;
		}
		else if(oldLength == 6)
			v[F] = f;
		else
			StsException.systemError("Error in length of point array: " + oldLength);
	}

	public float getF()
	{
		if(v.length == 6) return v[F];
		else if(v.length == 5) return v[M];
		else return StsParameters.nullValue;
	}

	public void setM(float m)
	{
		int oldLength = v.length;
		if(oldLength < 5)
		{
			float[] oldV = v;
			createVector(5);
			System.arraycopy(oldV, 0, v, 0, 3);
		}
		v[M] = m;
	}

	public float getM()
	{
		if(getLength() < 5)
		{
			StsException.systemError("StsPoint.getM() failed: point doesn't have m value.");
			return StsParameters.nullValue;
		}
		return v[M];
	}

	public void setT(float t)
	{
		int oldLength = v.length;
		if(oldLength < 5)
		{
			float[] oldV = v;
			createVector(5);
			System.arraycopy(oldV, 0, v, 0, oldLength);
		}
		v[T] = t;
	}

	public boolean hasT() { return v.length > 4; }

	public float getT()
	{
		if(v.length > 3) return v[T];
		else return v[Z];
	}

	public StsPoint getXYZorTPoint()
	{
		if(isDepth)
			return new StsPoint(this);
		else // is time
			return new StsPoint(v[X], v[Y], getZorT());
	}

	public StsPoint getXYZorTPoint(boolean isDepth)
	{
		if(isDepth || getLength() < 5)
			return new StsPoint(this);
		else
			return new StsPoint(v[X], v[Y], getT());
	}

	public void convertToXYZorT()
	{
		if(getLength() <= 3) return;
		float zt;
		if(isDepth)
			zt = getZ();
		else
			zt = getT();
		v = new float[]{v[X], v[Y], zt};
	}

	/*
	 public float[] getXYZorXYT(boolean isDepth)
	 {
		 return new float[]
			 {v[X], v[Y], getZ(isDepth)};
	 }
 */
	public float[] getXYZorT()
	{
		if(isDepth) return v;
		else return new float[]
				{v[X], v[Y], getZorT()};
	}

	public float[] getXYZorT(boolean isDepth)
	{
		if(isDepth || getLength() < 5)
			return v;
		else
			return new float[]{v[X], v[Y], getT()};
	}

	/** static utility method which has nothing to do with StsPoint; just seemed to be convenient here. */
	static public float[] getXYZorT(float[] xyztm)
	{
		if(xyztm == null) return null;
		if(isDepth) return xyztm;
		if(xyztm.length < 5) return null;
		else return new float[]{xyztm[0], xyztm[1], xyztm[3]};
	}

	public float[] getXYZ()
	{
		return v;
	}

	public void setXYZ(float x, float y, float z)
	{
		v[X] = x;
		v[Y] = y;
		v[Z] = z;
	}

	public void setValues(float[] v)
	{
		int minLength = Math.min(v.length, getLength());

		for(int n = 0; n < minLength; n++)
			this.v[n] = v[n];
	}


	public void setValues(double[] v)
	{
		int minLength = Math.min(v.length, getLength());

		for(int n = 0; n < minLength; n++)
			this.v[n] = (float) v[n];
	}

	public void setValues(StsPoint point)
	{
		setValues(point.v);
	}

	public void copyTo(StsPoint point)
	{
		int minLength = Math.min(point.v.length, getLength());

		for(int n = 0; n < minLength; n++)
			point.v[n] = this.v[n];
	}

	/** Only copy length dimensions to point */
	public void copyTo(int length, StsPoint point)
	{
		int minLength = StsMath.min3(length, this.getLength(), point.v.length);

		for(int n = 0; n < minLength; n++)
			point.v[n] = this.v[n];
	}

	public void copyFrom(StsPoint point)
	{
		int minLength = Math.min(point.v.length, getLength());

		for(int n = 0; n < minLength; n++)
			this.v[n] = point.v[n];
	}

	/** Only copy length dimensions to point */
	public void copyFrom(int length, StsPoint point)
	{
		int minLength = StsMath.min3(length, this.getLength(), point.v.length);

		for(int n = 0; n < minLength; n++)
			this.v[n] = point.v[n];
	}

	public void copyFrom(float[] v)
	{
		int minLength = Math.min(this.v.length, v.length);

		for(int n = 0; n < minLength; n++)
			this.v[n] = v[n];
	}

	public StsPoint copy()
	{
		return new StsPoint(this);
	}

	static public StsPoint[] copy(StsPoint[] points)
	{
		if(points == null) return new StsPoint[0];
		int nPoints = points.length;
		StsPoint[] newPoints = new StsPoint[nPoints];
		for(int n = 0; n < nPoints; n++)
			newPoints[n] = points[n].copy();
		return newPoints;
	}

	/** points have same vector */
	public boolean equals(StsPoint otherPoint)
	{
		if(otherPoint == null) return false;
		if(otherPoint.v.length != v.length) return false;
		for(int i = 0; i < getLength(); i++)
		{
			if(v[i] != otherPoint.v[i]) return false;
		}
		return true;
	}

	public boolean sameAs(StsPoint otherPoint)
	{
		if(otherPoint == null) return false;
		int length = Math.min(otherPoint.v.length, v.length);
		for(int i = 0; i < length; i++)
		{
			if(!StsMath.sameAs(v[i], otherPoint.v[i])) return false;
		}
		return true;
	}

	public boolean equals(double[] v)
	{
		int length = Math.min(v.length, this.v.length);
		for(int i = 0; i < length; i++)
			if((float) v[i] != this.v[i]) return false;
		return true;
	}

	public boolean sameXY(StsPoint otherPoint)
	{
		if(otherPoint == null) return false;
		if(v.length < 2 || otherPoint.v.length < 2) return false;
		if(!StsMath.sameAs(v[X], otherPoint.v[X])) return false;
		if(!StsMath.sameAs(v[Y], otherPoint.v[Y])) return false;
		return true;
	}

	/** add a point to the point */
	public boolean add(StsPoint otherPoint)
	{
		if(otherPoint == null) return false;
		if(otherPoint.v.length != v.length) return false;
		for(int i = 0; i < getLength(); i++) v[i] += otherPoint.v[i];
		return true;
	}

	/** subtract a point from the point */
	public boolean subtract(StsPoint otherPoint)
	{
		if(otherPoint == null) return false;
		if(otherPoint.v.length != v.length) return false;
		for(int i = 0; i < getLength(); i++) v[i] -= otherPoint.v[i];
		return true;
	}

    static public StsPoint getLastPoint(StsPoint[] points)
    {
        if(points == null) return null;
        return points[points.length-1];
    }

	public StsPoint getNearestPointOnSegmentedLine(int distanceType, StsPoint[] linePoints, float indexF)
	{
		int i;
		int minIndex = -1;
		float minDistSq = StsParameters.largeFloat;
		int nPoints = linePoints.length;

		for(i = nPoints - 1; i >= 0; i--)
		{
			if(linePoints[i] != null) break;
			nPoints = i;
		}

		if(indexF < 0.0f || indexF > nPoints - 1) /** startF not used: search whole line */
		{
			for(i = 0; i < nPoints; i++)
			{
				float distSq = distanceSquaredType(distanceType, linePoints[i]);
				if(distSq < minDistSq)
				{
					minDistSq = distSq;
					minIndex = i;
				}
			}
		}
		else /* Use startF to compute a starting index */
		{
			int startI = (int) indexF;
			startI = StsMath.minMax(startI, 0, nPoints - 1);

			for(i = startI; i < nPoints; i++)
			{
				float distSq = distanceSquaredType(distanceType, linePoints[i]);

				if(distSq < minDistSq)
				{
					minDistSq = distSq;
					minIndex = i;
				}
				else if(distSq > 2.0f * minDistSq) break;
			}

			for(i = startI - 1; i >= 0; i--)
			{
				float distSq = distanceSquaredType(distanceType, linePoints[i]);

				if(distSq < minDistSq)
				{
					minDistSq = distSq;
					minIndex = i;
				}
				else if(distSq > 2.0f * minDistSq) break;
			}
		}

		if(minIndex == -1)
		{
			StsException.outputException(new StsException(StsException.WARNING,
					"StsPoint.getNearestPointOnSegmentedLine(...) failed."));
			return null;
		}

		float distSq1, distSq2, distSq12, f;

		if(minIndex == nPoints - 1) minIndex--;
		if(minIndex > 0)
		{
			/** If point to left of this point is closer than one to the right:
			 *  move the interval left 1
			 */
			distSq1 = distanceSquaredType(distanceType, linePoints[minIndex - 1]);
			distSq2 = distanceSquaredType(distanceType, linePoints[minIndex + 1]);
			if(distSq1 < distSq2)
			{
				minIndex--;
				distSq2 = distanceSquaredType(distanceType, linePoints[minIndex + 1]);
			}
			else
				distSq1 = distanceSquaredType(distanceType, linePoints[minIndex]);
		}
		else
		{
			distSq1 = distanceSquaredType(distanceType, linePoints[minIndex]);
			distSq2 = distanceSquaredType(distanceType, linePoints[minIndex + 1]);
		}

		StsPoint linePoint1 = linePoints[minIndex];
		StsPoint linePoint2 = linePoints[minIndex + 1];
		distSq12 = linePoint1.distanceSquaredType(distanceType, linePoint2);

		if(distSq12 <= 0.0f)
			f = 0.5f;
		else
		{
			f = StsMath.minMax(0.5f + 0.5f * (distSq1 - distSq2) / distSq12, 0.0f, 1.0f);

			// if f is off interval 0 to 1 and interval is not an end interval, set f to 0 if < 0
			// or 1 if > 1: nearest point is the end point of the interval.
			// if fi is off interval, but this is an end interval, the f value is subsequently used
			// to indicate we are off end of line and nearest point will be recomputed to ben the
			// end point of the line

			if(f < 0.0f && minIndex > 0)
				f = 0.0f;
			else if(f > 1.0f && minIndex < nPoints - 1)
				f = 1.0f;
		}

		indexF = minIndex + f;

		if(indexF < 0.0f)
			return linePoints[0];
		else if(indexF > (float) (nPoints - 1))
			return linePoints[nPoints - 1];
		else
		{
			StsPoint iPoint = new StsPoint(4);
			iPoint.interpolatePoints(linePoints[minIndex], linePoints[minIndex + 1], f);
			iPoint.setF(indexF);
			return iPoint;
		}
	}

	static public boolean computeNormalizedArcLengths(int distanceType, StsPoint[] points)
	{
		int nPoints = points.length;
		float length = 0;
		points[0].setF(0.0f);

		StsPoint point, nextPoint;

		nextPoint = points[0];
		for(int n = 1; n < nPoints; n++)
		{
			point = nextPoint;
			nextPoint = points[n];
			length += point.distanceType(distanceType, nextPoint);
			nextPoint.setF(length);
		}
		// normalize
		for(int n = 1; n < nPoints - 1; n++)
		{
			float normLength = points[n].getF() / length;
			points[n].setF(normLength);
		}
		points[nPoints - 1].setF(1.0f);
		return true;
	}

	static public void setCompareIndex(int index) { compareIndex = index; }

	public int compareTo(Object otherObj)
	{
		StsPoint otherPoint = (StsPoint) otherObj;
		if(v[compareIndex] < otherPoint.v[compareIndex]) return -1;
		else if(v[compareIndex] == otherPoint.v[compareIndex]) return 0;
		else return 1;
	}

	static public int getVerticalIndex()
	{
		if(isDepth) return Z;
		else return T;
	}

	public float[] getPointXYZ()
	{
		if(isDepth) return v;
		else if(v.length <= T) return v;
		else return new float[]{v[X], v[Y], v[T]};
	}

	public double[] getPointXYZDoubles()
	{
		if(isDepth || v.length <= T) return new double[]{v[X], v[Y], v[Z]};
		else return new double[]{v[X], v[Y], v[T]};
	}

	static public StsPoint leftCrossProductTime(StsPoint vA, StsPoint vB)
	{
		if(vA == null || vB == null) return null;

		int minLength = Math.min(vA.getLength(), vB.getLength());
		StsPoint cross = new StsPoint(5);
		float[] v = cross.v;
		if(minLength < 5) return null;
		{
			v[X] = -vA.v[Y] * vB.v[T] + vA.v[T] * vB.v[Y];
			v[Y] = -vA.v[T] * vB.v[X] + vA.v[X] * vB.v[T];
			v[T] = -vA.v[X] * vB.v[Y] + vA.v[Y] * vB.v[X];
		}
		return cross;
	}

	static public boolean normalizeTime(StsPoint point)
	{
		float[] v = point.v;
		double sumSq = v[X] * v[X] + v[Y] * v[Y] + v[T] * v[T];
		float vLength = (float) Math.sqrt(sumSq);
		if(vLength <= 0.0) return false;
		v[X] /= vLength;
		v[Y] /= vLength;
		v[T] /= vLength;
		return true;
	}

	static public PointComparator getComparator(int index)
	{
		compareIndex = index;
		if(comparator == null) comparator = new PointComparator();
		return comparator;
//		if(comparator == null) comparator = new Comparator(index);
//		return comparator;
	}

	public int getLength()
	{
		if(v == null) return 0;
		return v.length;
	}

	static class PointComparator implements java.util.Comparator
	{
		public int compare(Object p1, Object p2)
		{
			return ((StsPoint) p1).compareTo(p2);
		}

		public boolean equals(Object p1, Object p2)
		{
			return compare(p1, p2) == 0;
		}
	}

/*
	@param o1 the first object to be compared.
		 * @param o2 the second object to be compared.
		 * @return a negative integer, zero, or a positive integer as the
		 * 	       first argument is less than, equal to, or greater than the
     *	       second.
*/

	/** field access methods for DBSerialization interface */
/*
    public void setBoolean(Field field, boolean i) throws java.lang.IllegalAccessException
    {
        field.setBoolean(this, i);
    }

    public boolean getBoolean(Field field) throws java.lang.IllegalAccessException
    {
        return field.getBoolean(this);
    }

    public void setByte(Field field, byte i) throws java.lang.IllegalAccessException
    {
        field.setByte(this, i);
    }

    public byte getByte(Field field) throws java.lang.IllegalAccessException
    {
        return field.getByte(this);
    }

    public void setChar(Field field, char c) throws java.lang.IllegalAccessException
    {
        field.setChar(this, c);
    }

    public char getChar(Field field) throws java.lang.IllegalAccessException
    {
        return field.getChar(this);
    }

    public void setShort(Field field, short i) throws java.lang.IllegalAccessException
    {
        field.setShort(this, i);
    }

    public short getShort(Field field) throws java.lang.IllegalAccessException
    {
        return field.getShort(this);
    }

    public void setInt(Field field, int i) throws java.lang.IllegalAccessException
    {
        field.setInt(this, i);
    }
    public int getInt(Field field) throws java.lang.IllegalAccessException
    {
        return field.getInt(this);
    }

    public void setLong(Field field, long i) throws java.lang.IllegalAccessException
    {
        field.setLong(this, i);
    }
    public long getLong(Field field) throws java.lang.IllegalAccessException
    {
        return field.getLong(this);
    }

    public void setFloat(Field field, float f) throws java.lang.IllegalAccessException
    {
        field.setFloat(this, f);
    }
    public float getFloat(Field field) throws java.lang.IllegalAccessException
    {
        return field.getFloat(this);
    }

    public void setDouble(Field field, double d) throws java.lang.IllegalAccessException
    {
        field.setDouble(this, d);
    }

    public double getDouble(Field field) throws java.lang.IllegalAccessException
    {
        return field.getDouble(this);
    }

    public void set(Field field, Object obj) throws java.lang.IllegalAccessException
    {
        field.set(this, obj);
    }

    public Object get(Field field) throws java.lang.IllegalAccessException
    {
        return field.get(this);
    }
*/
}





