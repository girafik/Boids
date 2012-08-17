package info.girafik.boids;

public class Vector {

	double x;
	double y;

	public Vector(double x, double y) {
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

	public Vector limit(double limit) {
		if (Math.abs(x) > limit || Math.abs(y) > limit) {
			double scaleFactor = limit / Math.max(Math.abs(x), Math.abs(y));
			x *= scaleFactor;
			y *= scaleFactor;
		}
		return this;
	}

	public Vector multiply(double c) {
		x *= c;
		y *= c;
		return this;
	}

	public double distanceTo(Vector that) {
		return this.copy().subtract(that).magnitude();
	}

	public Vector divide(double c) {
		x /= c;
		y /= c;
		return this;
	}

	public double magnitude() {
		return Math.sqrt(this.copy().multiply(this));
	}

	public double multiply(Vector that) {
		return this.x * that.x + this.y * that.y;
	}

	public Vector normalize() {
		return this.divide(this.magnitude());
	}

	public Vector copy() {
		return new Vector(this.x, this.y);
	}
}
