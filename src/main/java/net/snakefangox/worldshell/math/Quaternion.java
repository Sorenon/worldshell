/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.snakefangox.worldshell.math;

/**
 * <code>Quaternion</code> defines a single example of a more general class of
 * hypercomplex numbers. Quaternions extends a rotation in three dimensions to a
 * rotation in four dimensions. This avoids "gimbal lock" and allows for smooth
 * continuous rotation.
 *
 * <code>Quaternion</code> is defined by four floating point numbers: {x y z w}.
 *
 * @author Mark Powell
 * @author Joshua Slack
 */
public final class Quaternion implements Cloneable, java.io.Serializable {

	static final long serialVersionUID = 1;

	/**
	 * Represents the identity quaternion rotation (0, 0, 0, 1).
	 */
	public static final Quaternion IDENTITY = new Quaternion();
	/**
	 * another instance of the identity Quaternion (0, 0, 0, 1)
	 */
	public static final Quaternion DIRECTION_Z = new Quaternion();
	/**
	 * The zero quaternion (0, 0, 0, 0) doesn't represent any rotation.
	 */
	public static final Quaternion ZERO = new Quaternion(0, 0, 0, 0);

	static {
		DIRECTION_Z.fromAxes(Vector3d.UNIT_X, Vector3d.UNIT_Y, Vector3d.UNIT_Z);
	}

	/**
	 * the X component (not an angle!)
	 */
	protected double x;
	/**
	 * the Y component (not an angle!)
	 */
	protected double y;
	/**
	 * the Z component (not an angle!)
	 */
	protected double z;
	/**
	 * the W (real) component (not an angle!)
	 */
	protected double w;

	/**
	 * Constructor instantiates a new <code>Quaternion</code> object
	 * initializing all values to zero, except w which is initialized to 1.
	 */
	public Quaternion() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
	}

	/**
	 * Constructor instantiates a new <code>Quaternion</code> object from the
	 * given list of parameters.
	 *
	 * @param x the x value of the quaternion.
	 * @param y the y value of the quaternion.
	 * @param z the z value of the quaternion.
	 * @param w the w value of the quaternion.
	 */
	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	/**
	 * Determine the X component.
	 *
	 * @return x
	 */
	public double getX() {
		return x;
	}

	/**
	 * Determine the Y component.
	 *
	 * @return y
	 */
	public double getY() {
		return y;
	}

	/**
	 * Determine the Z component.
	 *
	 * @return z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Determine the W (real) component.
	 *
	 * @return w
	 */
	public double getW() {
		return w;
	}

	/**
	 * sets the data in a <code>Quaternion</code> object from the given list of
	 * parameters.
	 *
	 * @param x the x value of the quaternion.
	 * @param y the y value of the quaternion.
	 * @param z the z value of the quaternion.
	 * @param w the w value of the quaternion.
	 * @return this
	 */
	public Quaternion set(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	/**
	 * Sets the data in this <code>Quaternion</code> object to be equal to the
	 * passed <code>Quaternion</code> object. The values are copied producing
	 * a new object.
	 *
	 * @param q The Quaternion to copy values from.
	 * @return this
	 */
	public Quaternion set(Quaternion q) {
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
		this.w = q.w;
		return this;
	}

	/**
	 * Sets this Quaternion to {0, 0, 0, 1}. Same as calling set(0,0,0,1).
	 */
	public void loadIdentity() {
		x = y = z = 0;
		w = 1;
	}

	/**
	 * Reconfigure this Quaternion based on Tait-Bryan rotations, applying the
	 * rotations in x-z-y extrinsic order or y-z'-x" intrinsic order.
	 *
	 * @param xAngle the X angle (in radians)
	 * @param yAngle the Y angle (in radians)
	 * @param zAngle the Z angle (in radians)
	 * @return this
	 * @see <a href="http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm">http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm</a>
	 */
	public Quaternion fromAngles(double xAngle, double yAngle, double zAngle) {
		double angle;
		double sinY, sinZ, sinX, cosY, cosZ, cosX;
		angle = zAngle * 0.5f;
		sinZ = Math.sin(angle);
		cosZ = Math.cos(angle);
		angle = yAngle * 0.5f;
		sinY = Math.sin(angle);
		cosY = Math.cos(angle);
		angle = xAngle * 0.5f;
		sinX = Math.sin(angle);
		cosX = Math.cos(angle);

		// variables used to reduce multiplication calls.
		double cosYXcosZ = cosY * cosZ;
		double sinYXsinZ = sinY * sinZ;
		double cosYXsinZ = cosY * sinZ;
		double sinYXcosZ = sinY * cosZ;

		w = (cosYXcosZ * cosX - sinYXsinZ * sinX);
		x = (cosYXcosZ * sinX + sinYXsinZ * cosX);
		y = (sinYXcosZ * cosX + cosYXsinZ * sinX);
		z = (cosYXsinZ * cosX - sinYXcosZ * sinX);

		normalizeLocal();
		return this;
	}

	/**
	 * <code>fromRotationMatrix</code> generates a quaternion from a supplied
	 * matrix. This matrix is assumed to be a rotational matrix.
	 *
	 * @param matrix the matrix that defines the rotation.
	 * @return this
	 */
	public Quaternion fromRotationMatrix(Matrix3d matrix) {
		return fromRotationMatrix(matrix.m00, matrix.m01, matrix.m02, matrix.m10,
				matrix.m11, matrix.m12, matrix.m20, matrix.m21, matrix.m22);
	}

	/**
	 * Set this quaternion based on a rotation matrix with the specified
	 * elements.
	 *
	 * @param m00 the matrix element in row 0, column 0
	 * @param m01 the matrix element in row 0, column 1
	 * @param m02 the matrix element in row 0, column 2
	 * @param m10 the matrix element in row 1, column 0
	 * @param m11 the matrix element in row 1, column 1
	 * @param m12 the matrix element in row 1, column 2
	 * @param m20 the matrix element in row 2, column 0
	 * @param m21 the matrix element in row 2, column 1
	 * @param m22 the matrix element in row 2, column 2
	 * @return this Quaternion
	 */
	public Quaternion fromRotationMatrix(double m00, double m01, double m02,
										 double m10, double m11, double m12, double m20, double m21, double m22) {
		// first normalize the forward (F), up (U) and side (S) vectors of the rotation matrix
		// so that the scale does not affect the rotation
		double lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;
		if (lengthSquared != 1f && lengthSquared != 0f) {
			lengthSquared = 1.0f / Math.sqrt(lengthSquared);
			m00 *= lengthSquared;
			m10 *= lengthSquared;
			m20 *= lengthSquared;
		}
		lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;
		if (lengthSquared != 1f && lengthSquared != 0f) {
			lengthSquared = 1.0f / Math.sqrt(lengthSquared);
			m01 *= lengthSquared;
			m11 *= lengthSquared;
			m21 *= lengthSquared;
		}
		lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;
		if (lengthSquared != 1f && lengthSquared != 0f) {
			lengthSquared = 1.0f / Math.sqrt(lengthSquared);
			m02 *= lengthSquared;
			m12 *= lengthSquared;
			m22 *= lengthSquared;
		}

		// Use the Graphics Gems code, from
		// ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z
		// *NOT* the "Matrix and Quaternions FAQ", which has errors!

		// the trace is the sum of the diagonal elements; see
		// http://mathworld.wolfram.com/MatrixTrace.html
		double t = m00 + m11 + m22;

		// we protect the division by s by ensuring that s>=1
		if (t >= 0) { // |w| >= .5
			double s = Math.sqrt(t + 1); // |s|>=1 ...
			w = 0.5f * s;
			s = 0.5f / s;                 // so this division isn't bad
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else if ((m00 > m11) && (m00 > m22)) {
			double s = Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
			x = s * 0.5f; // |x| >= .5
			s = 0.5f / s;
			y = (m10 + m01) * s;
			z = (m02 + m20) * s;
			w = (m21 - m12) * s;
		} else if (m11 > m22) {
			double s = Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
			y = s * 0.5f; // |y| >= .5
			s = 0.5f / s;
			x = (m10 + m01) * s;
			z = (m21 + m12) * s;
			w = (m02 - m20) * s;
		} else {
			double s = Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
			z = s * 0.5f; // |z| >= .5
			s = 0.5f / s;
			x = (m02 + m20) * s;
			y = (m21 + m12) * s;
			w = (m10 - m01) * s;
		}

		return this;
	}

	/**
	 * <code>toRotationMatrix</code> converts this quaternion to a rotational
	 * matrix. Note: the result is created from a normalized version of this quat.
	 *
	 * @return the rotation matrix representation of this quaternion.
	 */
	public Matrix3d toRotationMatrix() {
		Matrix3d matrix = new Matrix3d();
		return toRotationMatrix(matrix);
	}

	/**
	 * <code>toRotationMatrix</code> converts this quaternion to a rotational
	 * matrix. The result is stored in result.
	 *
	 * @param result The Matrix3d to store the result in.
	 * @return the rotation matrix representation of this quaternion.
	 */
	public Matrix3d toRotationMatrix(Matrix3d result) {

		double norm = norm();
		// we explicitly test norm against one here, saving a division
		// at the cost of a test and branch.  Is it worth it?
		double s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

		// compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
		// will be used 2-4 times each.
		double xs = x * s;
		double ys = y * s;
		double zs = z * s;
		double xx = x * xs;
		double xy = x * ys;
		double xz = x * zs;
		double xw = w * xs;
		double yy = y * ys;
		double yz = y * zs;
		double yw = w * ys;
		double zz = z * zs;
		double zw = w * zs;

		// using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
		result.m00 = 1 - (yy + zz);
		result.m01 = (xy - zw);
		result.m02 = (xz + yw);
		result.m10 = (xy + zw);
		result.m11 = 1 - (xx + zz);
		result.m12 = (yz - xw);
		result.m20 = (xz - yw);
		result.m21 = (yz + xw);
		result.m22 = 1 - (xx + yy);

		return result;
	}

	/**
	 * <code>toTransformMatrix</code> converts this quaternion to a transform
	 * matrix. The result is stored in result.
	 * Note this method won't preserve the scale of the given matrix.
	 *
	 * @param store The Matrix3d to store the result in.
	 * @return the transform matrix with the rotation representation of this quaternion.
	 */
	public Matrix3d toTransformMatrix(Matrix3d store) {

		double norm = norm();
		// we explicitly test norm against one here, saving a division
		// at the cost of a test and branch.  Is it worth it?
		double s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

		// compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
		// will be used 2-4 times each.
		double xs = x * s;
		double ys = y * s;
		double zs = z * s;
		double xx = x * xs;
		double xy = x * ys;
		double xz = x * zs;
		double xw = w * xs;
		double yy = y * ys;
		double yz = y * zs;
		double yw = w * ys;
		double zz = z * zs;
		double zw = w * zs;

		// using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
		store.m00 = 1 - (yy + zz);
		store.m01 = (xy - zw);
		store.m02 = (xz + yw);
		store.m10 = (xy + zw);
		store.m11 = 1 - (xx + zz);
		store.m12 = (yz - xw);
		store.m20 = (xz - yw);
		store.m21 = (yz + xw);
		store.m22 = 1 - (xx + yy);

		return store;
	}

	/**
	 * <code>fromAngleNormalAxis</code> sets this quaternion to the values
	 * specified by an angle and a normalized axis of rotation.
	 *
	 * @param angle the angle to rotate (in radians).
	 * @param axis  the axis of rotation (already normalized).
	 * @return this
	 */
	public Quaternion fromAngleNormalAxis(double angle, Vector3d axis) {
		if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
			loadIdentity();
		} else {
			double halfAngle = 0.5f * angle;
			double sin = Math.sin(halfAngle);
			w = Math.cos(halfAngle);
			x = sin * axis.x;
			y = sin * axis.y;
			z = sin * axis.z;
		}
		return this;
	}

	/**
	 * <code>add</code> adds the values of this quaternion to those of the
	 * parameter quaternion. The result is stored in this Quaternion.
	 *
	 * @param q the quaternion to add to this.
	 * @return This Quaternion after addition.
	 */
	public Quaternion addLocal(Quaternion q) {
		this.x += q.x;
		this.y += q.y;
		this.z += q.z;
		this.w += q.w;
		return this;
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter quaternion.
	 * The result is returned as a new quaternion. It should be noted that
	 * quaternion multiplication is not commutative so q * p != p * q.
	 *
	 * @param q the quaternion to multiply this quaternion by.
	 * @return the new quaternion.
	 */
	public Quaternion mult(Quaternion q) {
		return mult(q, null);
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter quaternion.
	 * The result is returned as a new quaternion. It should be noted that
	 * quaternion multiplication is not commutative so q * p != p * q.
	 * <p>
	 * It IS safe for q and res to be the same object.
	 * It IS NOT safe for this and res to be the same object.
	 *
	 * @param q   the quaternion to multiply this quaternion by.
	 * @param res the quaternion to store the result in.
	 * @return the new quaternion.
	 */
	public Quaternion mult(Quaternion q, Quaternion res) {
		if (res == null) {
			res = new Quaternion();
		}
		double qw = q.w, qx = q.x, qy = q.y, qz = q.z;
		res.x = x * qw + y * qz - z * qy + w * qx;
		res.y = -x * qz + y * qw + z * qx + w * qy;
		res.z = x * qy - y * qx + z * qw + w * qz;
		res.w = -x * qx - y * qy - z * qz + w * qw;
		return res;
	}

	/**
	 * <code>fromAxes</code> creates a <code>Quaternion</code> that
	 * represents the coordinate system defined by three axes. These axes are
	 * assumed to be orthogonal and no error checking is applied. Thus, the user
	 * must insure that the three axes being provided indeed represents a proper
	 * right handed coordinate system.
	 *
	 * @param xAxis vector representing the x-axis of the coordinate system.
	 * @param yAxis vector representing the y-axis of the coordinate system.
	 * @param zAxis vector representing the z-axis of the coordinate system.
	 * @return this
	 */
	public Quaternion fromAxes(Vector3d xAxis, Vector3d yAxis, Vector3d zAxis) {
		return fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y,
				zAxis.y, xAxis.z, yAxis.z, zAxis.z);
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter vector. The
	 * result is stored in the supplied vector
	 *
	 * @param v the vector to multiply this quaternion by.
	 * @return v
	 */
	public Vector3d multLocal(Vector3d v) {
		double tempX, tempY;
		tempX = w * w * v.x + 2 * y * w * v.z - 2 * z * w * v.y + x * x * v.x
				+ 2 * y * x * v.y + 2 * z * x * v.z - z * z * v.x - y * y * v.x;
		tempY = 2 * x * y * v.x + y * y * v.y + 2 * z * y * v.z + 2 * w * z
				* v.x - z * z * v.y + w * w * v.y - 2 * x * w * v.z - x * x
				* v.y;
		v.z = 2 * x * z * v.x + 2 * y * z * v.y + z * z * v.z - 2 * w * y * v.x
				- y * y * v.z + 2 * w * x * v.y - x * x * v.z + w * w * v.z;
		v.x = tempX;
		v.y = tempY;
		return v;
	}

	/**
	 * Multiplies this Quaternion by the supplied quaternion. The result is
	 * stored in this Quaternion, which is also returned for chaining. Similar
	 * to this *= q.
	 *
	 * @param q The Quaternion to multiply this one by.
	 * @return This Quaternion, after multiplication.
	 */
	public Quaternion multLocal(Quaternion q) {
		double x1 = x * q.w + y * q.z - z * q.y + w * q.x;
		double y1 = -x * q.z + y * q.w + z * q.x + w * q.y;
		double z1 = x * q.y - y * q.x + z * q.w + w * q.z;
		w = -x * q.x - y * q.y - z * q.z + w * q.w;
		x = x1;
		y = y1;
		z = z1;
		return this;
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter vector. The
	 * result is returned as a new vector.
	 *
	 * @param v     the vector to multiply this quaternion by.
	 * @param store the vector to store the result in. It IS safe for v and store
	 *              to be the same object.
	 * @return the result vector.
	 */
	public Vector3d mult(Vector3d v, Vector3d store) {
		if (store == null) {
			store = new Vector3d();
		}
		if (v.x == 0 && v.y == 0 && v.z == 0) {
			store.set(0, 0, 0);
		} else {
			double vx = v.x, vy = v.y, vz = v.z;
			store.x = w * w * vx + 2 * y * w * vz - 2 * z * w * vy + x * x
					* vx + 2 * y * x * vy + 2 * z * x * vz - z * z * vx - y
					* y * vx;
			store.y = 2 * x * y * vx + y * y * vy + 2 * z * y * vz + 2 * w
					* z * vx - z * z * vy + w * w * vy - 2 * x * w * vz - x
					* x * vy;
			store.z = 2 * x * z * vx + 2 * y * z * vy + z * z * vz - 2 * w
					* y * vx - y * y * vz + 2 * w * x * vy - x * x * vz + w
					* w * vz;
		}
		return store;
	}

	/**
	 * <code>mult</code> multiplies this quaternion by a parameter scalar. The
	 * result is stored locally.
	 *
	 * @param scalar the quaternion to multiply this quaternion by.
	 * @return this.
	 */
	public Quaternion multLocal(double scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * Interpolates this quaternion towards another quaternion.
	 *
	 * @param target the quaternion to slerp towards. Mutated
	 * @param alpha  a fraction that indicates how far to move toward the desired value
	 * @return this
	 */
	public Quaternion slerp(Quaternion target, double alpha) {
		if (equals(target)) return this;

		double dot = dot(target);

		if (dot < 0) {
			target.multLocal(-1);
			dot = -dot;
		}

		double scale = alpha;
		double invScale = 1 - alpha;

		if ((1.0 - dot) > 0.1) {
			double theta = Math.acos(dot);
			double sinTheta = 1d / Math.sin(theta);

			invScale = Math.sin((1d - alpha) * theta) * sinTheta;
			scale = Math.sin(alpha * theta) * sinTheta;
		}

		x = (invScale * x) + (scale * target.x);
		y = (invScale * y) + (scale * target.y);
		z = (invScale * z) + (scale * target.z);
		w = (invScale * w) + (scale * target.w);

		return this;
	}

	/**
	 * Gets the dot product of this quaternion with another.
	 *
	 * @param other the quaternion to take the dot of
	 * @return the dot product
	 */
	public double dot(Quaternion other) {
		return x * other.x + y * other.y + z * other.z + w * other.w;
	}

	public double getRoll() {
		return Math.atan2(2d * (w * x + y * z), 1d - 2d * ((x * x) + (y * y)));
	}

	public double getYaw() {
		return Math.atan2(2d * (w * z + x * y), 1d - 2d * ((y * y) + (z * z)));
	}

	public double getPitch() {
		return Math.asin(2d * (w * y - z * x));
	}

	/**
	 * <code>norm</code> returns the norm of this quaternion. This is the dot
	 * product of this quaternion with itself.
	 *
	 * @return the norm of the quaternion.
	 */
	public double norm() {
		return w * w + x * x + y * y + z * z;
	}

	/**
	 * <code>normalize</code> normalizes the current <code>Quaternion</code>.
	 * The result is stored internally.
	 *
	 * @return this
	 */
	public Quaternion normalizeLocal() {
		double n = 1f / Math.sqrt(norm());
		x *= n;
		y *= n;
		z *= n;
		w *= n;
		return this;
	}

	/**
	 * <code>inverse</code> returns the inverse of this quaternion as a new
	 * quaternion. If this quaternion does not have an inverse (if its normal is
	 * 0 or less), then null is returned.
	 *
	 * @return the inverse of this quaternion or null if the inverse does not
	 * exist.
	 */
	public Quaternion inverse() {
		double norm = norm();
		if (norm > 0.0) {
			double invNorm = 1.0f / norm;
			return new Quaternion(-x * invNorm, -y * invNorm, -z * invNorm, w
					* invNorm);
		}
		// return an invalid result to flag the error
		return null;
	}

	/**
	 * <code>toString</code> returns a string representation of this
	 * <code>Quaternion</code>. The format is:
	 * <p>
	 * (X.XXXX, Y.YYYY, Z.ZZZZ, W.WWWW)
	 *
	 * @return the string representation of this object.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}

	/**
	 * <code>equals</code> determines if two quaternions are logically equal,
	 * that is, if the values of (x, y, z, w) are the same for both quaternions.
	 *
	 * @param o the object to compare for equality
	 * @return true if they are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Quaternion)) {
			return false;
		}

		if (this == o) {
			return true;
		}

		Quaternion comp = (Quaternion) o;
		if (Double.compare(x, comp.x) != 0) {
			return false;
		}
		if (Double.compare(y, comp.y) != 0) {
			return false;
		}
		if (Double.compare(z, comp.z) != 0) {
			return false;
		}
		if (Double.compare(w, comp.w) != 0) {
			return false;
		}
		return true;
	}

	/**
	 * <code>hashCode</code> returns the hash code value as an integer and is
	 * supported for the benefit of hashing based collection classes such as
	 * Hashtable, HashMap, HashSet etc.
	 *
	 * @return the hashcode for this instance of Quaternion.
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 37;
		hash = (int) (37 * hash + Double.doubleToLongBits(x));
		hash = (int) (37 * hash + Double.doubleToLongBits(y));
		hash = (int) (37 * hash + Double.doubleToLongBits(z));
		hash = (int) (37 * hash + Double.doubleToLongBits(w));
		return hash;

	}

	/**
	 * Create a copy of this quaternion.
	 *
	 * @return a new instance, equivalent to this one
	 */
	@Override
	public Quaternion clone() {
		try {
			return (Quaternion) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // can not happen
		}
	}
}
