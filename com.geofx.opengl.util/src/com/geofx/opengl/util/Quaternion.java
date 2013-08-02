package com.geofx.opengl.util;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.geofx.opengl.util.Quaternion;

public class Quaternion
{
	private static final double EPSILON = 0.00001;
	private static final double EPSILONSQ = 0.000000001;

	// A quaternion.
	// This quaternion class is generic and may be non-unit, however most
	// anticipated uses of quaternions are typically unit cases representing
	// a rotation 2*acos(w) about the axis (x,y,z).

	protected double 	w; 		// w component of quaternion
	protected double 	x; 		// x component of quaternion
	protected double 	y; 		// y component of quaternion
	protected double 	z; 		// z component of quaternion

	public Quaternion()
	{
	}

	/**
	 * Copy constructor
	 * 
	 * @param quaternion
	 */
	public Quaternion(Quaternion quaternion)
	{
		this.w = quaternion.w;
		this.x = quaternion.x;
		this.y = quaternion.y;
		this.z = quaternion.z;
	}

	// construct quaternion from real component w and imaginary x,y,z.
	public Quaternion(double w, double x, double y, double z)
	{
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// construct quaternion from angle-axis
	public Quaternion(double angle, Vector3d axis)
	{
		double a = angle * 0.5f;
		double s = (double) Math.sin(a);
		double c = (double) Math.cos(a);
		w = c;
		x = axis.x * s;
		y = axis.y * s;
		z = axis.z * s;
	}

	// construct quaternion from rotation matrix.
	public Quaternion(Matrix4d matrix)
	{
		// Algorithm in Ken Shoemaker's article in 1987 SIGGRAPH course notes
		// article "Quaternion Calculus and Fast Animation".

		double trace = matrix.m11 + matrix.m22 + matrix.m33;

		if (trace > 0)
		{
			// |w| > 1/2, may as well choose w > 1/2

			double root = Math.sqrt(trace + 1.0f); // 2w
			w = 0.5f * root;
			root = 0.5f / root; // 1/(4w)
			x = (matrix.m32 - matrix.m23) * root;
			y = (matrix.m13 - matrix.m31) * root;
			z = (matrix.m21 - matrix.m12) * root;
		}
		else
		{
			// |w| <= 1/2

			final int[] next = { 2, 3, 1 };

			int i = 1;
			if (matrix.m22 > matrix.m11)
				i = 2;
			if (matrix.m33 > matrix.getElement(i, i))
				i = 3;
			int j = next[i];
			int k = next[j];

			double root = Math.sqrt(matrix.getElement(i, i) - matrix.getElement(j, j) - matrix.getElement(k, k) + 1.0f);
			double[] quaternion = { x, y, z };
			quaternion[i] = 0.5f * root;
			root = 0.5f / root;
			w = (matrix.getElement(k, j) - matrix.getElement(j, k)) * root;
			quaternion[j] = (matrix.getElement(j, i) + matrix.getElement(i, j)) * root;
			quaternion[k] = (matrix.getElement(k, i) + matrix.getElement(i, k)) * root;
		}
	}

	// convert quaternion to matrix.
	public Matrix4d matrix()
	{
		// from david eberly's sources used with permission.

		double fTx = 2.0f * x;
		double fTy = 2.0f * y;
		double fTz = 2.0f * z;
		double fTwx = fTx * w;
		double fTwy = fTy * w;
		double fTwz = fTz * w;
		double fTxx = fTx * x;
		double fTxy = fTy * x;
		double fTxz = fTz * x;
		double fTyy = fTy * y;
		double fTyz = fTz * y;
		double fTzz = fTz * z;

		return new Matrix4d(1.0f - (fTyy + fTzz), fTxy - fTwz, fTxz + fTwy, 0.0, fTxy + fTwz, 1.0f - (fTxx + fTzz), fTyz - fTwx,
				0.0, fTxz - fTwy, fTyz + fTwx, 1.0f - (fTxx + fTyy), 0.0, 0.0, 0.0, 0.0, 1.0);
	}

	// convert quaternion to angle-axis.
	public double angleAxis(Vector3d axis)
	{
		double squareLength = x * x + y * y + z * z;
		double angle = 0;

		if (squareLength > EPSILONSQ)
		{
			angle = 2.0f * (double) Math.acos(w);
			double inverseLength = 1.0f / (double) Math.pow(squareLength, 0.5f);
			axis.x = x * inverseLength;
			axis.y = y * inverseLength;
			axis.z = z * inverseLength;
		}
		else
		{
			angle = 0.0f;
			axis.x = 1.0f;
			axis.y = 0.0f;
			axis.z = 0.0f;
		}

		return angle;
	}

	// set quaternion to zero.
	public void zero()
	{
		w = 0;
		x = 0;
		y = 0;
		z = 0;
	}

	// set quaternion to identity.

	public void identity()
	{
		w = 1;
		x = 0;
		y = 0;
		z = 0;
	}

	// add another quaternion to this quaternion.
	public void set(double w, double x, double y, double z)
	{
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// add another quaternion to this quaternion.
	public void set(Quaternion quat)
	{
		this.w = quat.w;
		this.x = quat.x;
		this.y = quat.y;
		this.z = quat.z;
	}

	// add another quaternion to this quaternion.
	public void add(Quaternion q)
	{
		w += q.w;
		x += q.x;
		y += q.y;
		z += q.z;
	}

	// subtract another quaternion from this quaternion.
	public void subtract(Quaternion q)
	{
		w -= q.w;
		x -= q.x;
		y -= q.y;
		z -= q.z;
	}

	// multiply this quaternion by a scalar.
	public void multiply(double s)
	{
		w *= s;
		x *= s;
		y *= s;
		z *= s;
	}

	// divide this quaternion by a scalar.
	public void divide(double s)
	{
		assert (s != 0);

		double inv = 1.0f / s;
		w *= inv;
		x *= inv;
		y *= inv;
		z *= inv;
	}

	// multiply this quaternion with another quaternion.
	public void multiply(Quaternion q)
	{
		double rw = w * q.w - x * q.x - y * q.y - z * q.z;
		double rx = w * q.x + x * q.w + y * q.z - z * q.y;
		double ry = w * q.y - x * q.z + y * q.w + z * q.x;
		double rz = w * q.z + x * q.y - y * q.x + z * q.w;
		w = rw;
		x = rx;
		y = ry;
		z = rz;
	}

	// multiply this quaternion with another quaternion and store result in parameter.
	void multiply(Quaternion q, Quaternion result)
	{
		result.w = w * q.w - x * q.x - y * q.y - z * q.z;
		result.x = w * q.x + x * q.w + y * q.z - z * q.y;
		result.y = w * q.y - x * q.z + y * q.w + z * q.x;
		result.z = w * q.z + x * q.y - y * q.x + z * q.w;
	}

	// dot product of two quaternions.
	public Quaternion dot(Quaternion q)
	{
		return new Quaternion(w * q.w + x * q.x + y * q.y + z * q.z, 0.0, 0.0, 0.0);
	}

	// dot product of two quaternions writing result to parameter.
	void dot(Quaternion q, Quaternion result)
	{
		result = new Quaternion(w * q.w + x * q.x + y * q.y + z * q.z, 0, 0, 0);
	}

	// calculate conjugate of quaternion.
	public Quaternion conjugate()
	{
		return new Quaternion(w, -x, -y, -z);
	}

	// calculate conjugate of quaternion and store result in parameter.
	public void conjugate(Quaternion result)
	{
		result = new Quaternion(w, -x, -y, -z);
	}

	// calculate length of quaternion
	public double length()
	{
		return Math.sqrt(w * w + x * x + y * y + z * z);
	}

	// calculate norm of quaternion.

	public double norm()
	{
		return w * w + x * x + y * y + z * z;
	}

	// normalize quaternion.
	public void normalize()
	{
		double length = this.length();

		if (length == 0)
		{
			w = 1;
			x = 0;
			y = 0;
			z = 0;
		}
		else
		{
			double inv = 1.0f / length;
			x = x * inv;
			y = y * inv;
			z = z * inv;
			w = w * inv;
		}
	}

	// check if quaternion is normalized
	public boolean normalized()
	{
		return equal(norm(), 1);
	}

	// calculate inverse of quaternion
	public Quaternion inverse()
	{
		double n = norm();
		assert (n != 0);
		return new Quaternion(w / n, -x / n, -y / n, -z / n);
	}

	// calculate inverse of quaternion and store result in parameter.
	public void inverse(Quaternion result)
	{
		double n = norm();
		result = new Quaternion(w / n, -x / n, -y / n, -z / n);
	}

	public boolean equal(double a, double b)
	{
		double d = a - b;
		if (d < EPSILON && d > -EPSILON)
			return true;
		else
			return false;
	}

	// equals operator
	public boolean equals(Quaternion other)
	{
		if (equal(w, other.w) & equal(x, other.x) & equal(y, other.y) & equal(z, other.z))
			return true;
		else
			return false;
	}

	// element access
	public double getElement(int i)
	{
		if (i == 1)
			return x;
		else if (i == 2)
			return y;
		else if (i == 3)
			return z;
		else
			return w;
	}

	public Quaternion negate(Quaternion a)
	{
		return new Quaternion(-a.w, -a.x, -a.y, -a.z);
	}

	Quaternion add(Quaternion a, Quaternion b)
	{
		return new Quaternion(a.w + b.w, a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public Quaternion sub(Quaternion a, Quaternion b)
	{
		return new Quaternion(a.w - b.w, a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public Quaternion mul(Quaternion a, Quaternion b)
	{
		return new Quaternion(a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z, a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y, a.w
				* b.y - a.x * b.z + a.y * b.w + a.z * b.x, a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w);
	}

	public Quaternion incr(Quaternion a, Quaternion b)
	{
		a.w += b.w;
		a.x += b.x;
		a.y += b.y;
		a.z += b.z;
		return a;
	}

	public Quaternion decr(Quaternion a, Quaternion b)
	{
		a.w -= b.w;
		a.x -= b.x;
		a.y -= b.y;
		a.z -= b.z;
		return a;
	}

	public boolean equals(Quaternion q, double scalar)
	{
		return equal(q.w, scalar) && equal(q.x, 0) && equal(q.y, 0) && equal(q.z, 0);
	}

	public Quaternion mul(Quaternion a, double s)
	{
		return new Quaternion(a.w * s, a.x * s, a.y * s, a.z * s);
	}

	public Quaternion scale(double s)
	{
		return new Quaternion(w * s, x * s, y * s, z * s);
	}

	public Quaternion div(Quaternion a, double s)
	{
		return new Quaternion(a.w / s, a.x / s, a.y / s, a.z / s);
	}

	public Quaternion mulRef(Quaternion a, double s)
	{
		a.multiply(s);
		return a;
	}

	public Quaternion divRef(Quaternion a, double s)
	{
		a.divide(s);
		return a;
	}

	/*
	 * Quaternion operator*(double s, Quaternion a) { return Quaternion(a.w*s, a.x*s, a.y*s, a.z*s);
	 * }
	 * 
	 * Quaternion& operator*=(double s, Quaternion a) { a.multiply(s); return a; }
	 */

	public Quaternion slerp(Quaternion a, Quaternion b, double t)
	{
		// assert(t >= 0);
		// assert(t <= 1);
		if (t < 0 || t > 1)
			throw new RuntimeException("slerp: t not between 0..1 !");

		double flip = 1;

		double cosine = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;

		if (cosine < 0)
		{
			cosine = -cosine;
			flip = -1;
		}

		if ((1 - cosine) < EPSILON)
		{
			Quaternion quatA = mul(a, (1 - t));
			Quaternion quatB = mul(b, t * flip);
			return mul(quatA, quatB);
		}

		double theta = Math.acos(cosine);
		double sine = Math.sin(theta);
		double beta = Math.sin((1 - t) * theta) / sine;
		double alpha = Math.sin(t * theta) / sine * flip;

		Quaternion quatA = mul(a, beta);
		Quaternion quatB = mul(b, alpha);
		return mul(quatA, quatB);
	}

	public void rkIntegrateAdd(Quaternion a, Quaternion b, Quaternion c, Quaternion d, double dt)
	{
		Quaternion tmp = add(b, c);
		tmp.multiply(2.0);
		tmp.add(a);
		tmp.add(d);
		tmp.multiply(dt / 6.0);
		add(tmp);
	}
}
