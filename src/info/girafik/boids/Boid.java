package info.girafik.boids;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Boid {

	static float side = 0.02f;
	private static final float MAX_VELOCITY = 0.01f;
	private static final float DESIRED_SEPARATION = 0.1f;
	private static final float SEPARATION_WEIGHT = 0.05f;
	private static final float ALIGNMENT_WEIGHT = 0.2f;
	private static final float COHESION_WEIGHT = 0.2f;
	private static final float MAX_FORCE = 0.001f;
	private static final float INERTION = 0.001f;

	Vector location;
	Vector velocity;

	private static FloatBuffer mFVertexBuffer;
	private static FloatBuffer mColorBuffer;
	private static ByteBuffer mIndexBuffer;
	private static Vector temp = new Vector(0, 0);
	private static Vector sum = new Vector(0, 0);
	private static Vector align = new Vector(0, 0);
	private static Vector separate = new Vector(0, 0);

	static {
		byte indices[] = { 0, 1, 2 };
		float[] vertices = { 0f, side, 0f, -side, -side, 0f, side, -side, 0f };
		float colors[] = { 0.8f, 0.8f, 0.2f, 1.0f, //
				0.8f, 0.8f, 0.2f, 1.0f,//
				0.8f, 0.8f, 0.2f, 1.0f,//
		};

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();
		mFVertexBuffer.put(vertices);
		mFVertexBuffer.position(0);

		vbb = ByteBuffer.allocateDirect(colors.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mColorBuffer = vbb.asFloatBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	public Boid() {
		Random r = new Random();
		location = new Vector(r.nextFloat() * 3, r.nextFloat() * 3);
		velocity = new Vector((r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f,
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f);
	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL11.GL_CW);
		gl.glVertexPointer(2, GL11.GL_FLOAT, 0, mFVertexBuffer);
		gl.glColorPointer(4, GL11.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL11.GL_TRIANGLES, 3, GL11.GL_UNSIGNED_BYTE, mIndexBuffer);
		gl.glFrontFace(GL11.GL_CCW);
	}

	public void step(Boid[] boids, Vector bounds) {
		Vector acceleration = flock(boids, bounds);
		velocity.add(acceleration).limit(MAX_VELOCITY);
		location.add(velocity);
		rotate(bounds);
	}

	private void rotate(Vector bounds) {
		if (location.x >= bounds.x) {
			location.x = -bounds.x;
		} else if (location.x <= -bounds.x) {
			location.x = bounds.x;
		}

		if (location.y >= bounds.y) {
			location.y = -bounds.y;
		} else if (location.y <= -bounds.y) {
			location.y = bounds.y;
		}

	}

	private Vector flock(Boid[] boids, Vector bounds) {
		Vector separation = separate(boids, bounds).multiply(SEPARATION_WEIGHT);
		Vector alignment = align(boids).multiply(ALIGNMENT_WEIGHT);
		Vector cohesion = cohere(boids, bounds).multiply(COHESION_WEIGHT);
		return separation.add(alignment).add(cohesion);
	}

	/*
	 * Move to center of the neighbors
	 */
	private Vector cohere(Boid[] boids, Vector bounds) {
		sum.init();
		int closeCount = 0;
		for (Boid boid : boids) {
			if (location.distanceTo(boid.location) < BoidsRenderer.RADIUS) {
				closeCount++;
				sum.add(boid.location);
			}
		}

		if (closeCount == 0)
			return sum;

		return steerTo(sum.divide(closeCount));
	}

	private Vector steerTo(Vector target) {
		Vector desired = target.subtract(location);
		float d = desired.magnitude();
		Vector steer = null;

		if (d > 0) {
			desired.normalize();

			if (d < INERTION) {
				desired.multiply(MAX_VELOCITY * d / INERTION);
			} else {
				desired.multiply(MAX_VELOCITY);
			}

			steer = desired.subtract(velocity).limit(MAX_FORCE);
		} else {
			steer = new Vector(0, 0);
		}
		return steer;
	}

	private Vector align(Boid[] boids) {
		align.init();
		int closeCount = 0;
		for (Boid boid : boids) {
			if (location.distanceTo(boid.location) < BoidsRenderer.RADIUS) {
				closeCount++;
				align.add(boid.velocity);
			}
		}
		if (closeCount != 0) {
			align.divide(closeCount);
		}
		return align.limit(MAX_FORCE);
	}

	private Vector separate(Boid[] boids, Vector bounds) {
		separate.init();
		int count = 0;
		for (Boid boid : boids) {
			float d = location.distanceTo(boid.location);
			if (d > 0 && d < DESIRED_SEPARATION) {
				separate.add(temp.copyFrom(location).subtract(boid.location).normalize().divide(d));
				count++;
			}
		}
		if (count != 0) {
			separate.divide(count);
		}
		return separate;
	}

}
