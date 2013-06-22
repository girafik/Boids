package info.girafik.boids;

import android.util.FloatMath;

public class Vector {

	float x;
	float y;
	float z;

	public Vector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector add(Vector that) {
		this.x += that.x;
		this.y += that.y;
		this.z += that.z;
		return this;
	}

	public Vector subtract(Vector that) {
		this.x -= that.x;
		this.y -= that.y;
		this.z -= that.z;
		return this;
	}

	public Vector limit(float limit) {
		float max = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
		if (max > limit) {
			float scaleFactor = limit / max;
			x *= scaleFactor;
			y *= scaleFactor;
			z *= scaleFactor;
		}
		return this;
	}

	public Vector multiply(float c) {
		x *= c;
		y *= c;
		z *= c;
		return this;
	}

	public Vector divide(float c) {
		x /= c;
		y /= c;
		z /= c;
		return this;
	}

	public float magnitude() {
		return FloatMath.sqrt(magnitude2());
	}

	public float magnitude2() {
		return x * x + y * y + z * z;
	}

	public float multiply(Vector that) {
		return this.x * that.x + this.y * that.y + this.z * that.z;
	}

	public Vector normalize() {
		return this.divide(this.magnitude());
	}

	public Vector copyFrom(Vector that) {
		this.x = that.x;
		this.y = that.y;
		this.z = that.z;
		return this;
	}

	public Vector init() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
        return this;
	}
}
