package info.girafik.boids;

import android.util.FloatMath;

public class Vector {

	float x;
	float y;

	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector add(Vector that) {
		this.x += that.x;
		this.y += that.y;
		return this;
	}

	public Vector subtract(Vector that) {
		this.x -= that.x;
		this.y -= that.y;
		return this;
	}

	public Vector limit(float limit) {
		if (Math.abs(x) > limit || Math.abs(y) > limit) {
			float scaleFactor = limit / Math.max(Math.abs(x), Math.abs(y));
			x *= scaleFactor;
			y *= scaleFactor;
		}
		return this;
	}

	public Vector multiply(float c) {
		x *= c;
		y *= c;
		return this;
	}

	public float distanceTo(Vector that) {
		return FloatMath.sqrt((that.x - this.x) * (that.x - this.x) + (that.y - this.y)
				* (that.y - this.y));
	}

	public Vector divide(float c) {
		x /= c;
		y /= c;
		return this;
	}

	public float magnitude() {
		return FloatMath.sqrt(x * x + y * y);
	}

	public float multiply(Vector that) {
		return this.x * that.x + this.y * that.y;
	}

	public Vector normalize() {
		return this.divide(this.magnitude());
	}

	public Vector copy() {
		return new Vector(x, y);
	}

	public Vector copyFrom(Vector that) {
		this.x = that.x;
		this.y = that.y;
		return this;
	}

	public void init() {
		this.x = 0;
		this.y = 0;
	}
}
