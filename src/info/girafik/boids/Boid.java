package info.girafik.boids;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Boid {

	static float side = 0.025f;
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

	static {
		byte indices[] = { 0, 3, 1, 0, 2, 3 };
		float vertices[] = { -side, -side, side, -side, -side, side, side, side };

		float colors[] = { 0.8f, 0.8f, 0.8f, 1.0f, //
				0.85f, 0.6f, 0.0f, 1.0f,//
				0.85f, 0.6f, 0.0f, 1.0f,//
				0.4f, 0.2f, 0.0f, 1.0f //
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
		location = new Vector(0, 0);
		velocity = new Vector((r.nextBoolean() ? 1f : -1f) * r.nextFloat()
				/ 100f, (r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f);
	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL11.GL_CW);
		gl.glVertexPointer(2, GL11.GL_FLOAT, 0, mFVertexBuffer);
		gl.glColorPointer(4, GL11.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE,
				mIndexBuffer);
		gl.glFrontFace(GL11.GL_CCW);
	}

	public void step(List<Boid> closeBoids, Vector bounds) {
		Vector acceleration = flock(closeBoids, bounds);
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

	private Vector flock(List<Boid> closeBoids, Vector bounds) {
		Vector separation = separate(closeBoids, bounds).multiply(
				SEPARATION_WEIGHT);
		Vector alignment = align(closeBoids).multiply(ALIGNMENT_WEIGHT);
		Vector cohesion = cohere(closeBoids, bounds).multiply(COHESION_WEIGHT);
		return separation.add(alignment).add(cohesion);
	}

	/*
	 * Move to center of the neighbors
	 */
	private Vector cohere(List<Boid> closeBoids, Vector bounds) {
		Vector sum = new Vector(0, 0);
		for (Boid boid : closeBoids) {
			sum.add(boid.location);
		}

		if (closeBoids.size() == 0)
			return sum;

		return steerTo(sum.divide(closeBoids.size()));
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

	private Vector align(List<Boid> closeBoids) {
		Vector mean = new Vector(0, 0);
		for (Boid boid : closeBoids) {
			mean.add(boid.velocity);
		}
		if (closeBoids.size() != 0) {
			mean.divide(closeBoids.size());
		}
		return mean.limit(MAX_FORCE);
	}

	private Vector separate(List<Boid> closeBoids, Vector bounds) {
		Vector mean = new Vector(0, 0);
		int count = 0;
		for (Boid boid : closeBoids) {
			float d = location.distanceTo(boid.location);
			if (d > 0 && d < DESIRED_SEPARATION) {
				mean.add(location.copy().subtract(boid.location).normalize()
						.divide(d));
				count++;
			}
		}
		if (count != 0) {
			mean.divide(count);
		}
		return mean;
	}

}
