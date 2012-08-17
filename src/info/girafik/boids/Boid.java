package info.girafik.boids;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class Boid {

	private static float side = 0.04f;
	private static final double MAX_VELOCITY = 0.005;
	private static final double DESIRED_SEPARATION = 0.05;
	private static final double SEPARATION_WEIGHT = 0.01;
	private static final double ALIGNMENT_WEIGHT = 0.3;
	private static final double COHESION_WEIGHT = 0.1;
	private static final double MAX_FORCE = 0.001;
	private static final double INERTION = 0.00001;

	Vector location;
	Vector velocity;

	private FloatBuffer mFVertexBuffer;
	private FloatBuffer mColorBuffer;
	private ByteBuffer mIndexBuffer;

	public Boid() {
		Random r = new Random();
		location = new Vector((r.nextBoolean() ? 1f : -1f) * r.nextFloat(),
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat());
		velocity = new Vector((r.nextBoolean() ? 1f : -1f) * r.nextFloat()
				/ 100f, (r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f);

		byte indices[] = { 0, 3, 1, 0, 2, 3 };
		float vertices[] = { -side, -side, side, -side, -side, side, side, side };
		float colors[] = { 1.0f, 0.0f, 0.0f, 0.8f, 
				0.0f, 1.0f, 0.0f, 0.8f,
				0.0f, 0.0f, 1.0f, 0.8f, 
				1.0f, 1.0f, 1.0f, 0.8f };

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

	public void draw(GL10 gl) {
		gl.glFrontFace(GL11.GL_CW);
		gl.glVertexPointer(2, GL11.GL_FLOAT, 0, mFVertexBuffer);
		gl.glColorPointer(4, GL11.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE,
				mIndexBuffer);
		gl.glFrontFace(GL11.GL_CCW);
	}

	public void step(Boid[] neighbours, Vector bounds) {
		Vector acceleration = flock(neighbours, bounds);
		velocity.add(acceleration).limit(MAX_VELOCITY);
		location.add(velocity);
		rotate(bounds);
	}

	
	private void rotate(Vector bounds) {
		double delta = 0.05;
		if (location.x >= bounds.x) {
			location.x = -bounds.x + delta;
		} else if (location.x <= -bounds.x) {
			location.x = bounds.x - delta;
		}

		if (location.y >= bounds.y) {
			location.y = -bounds.y + delta;
		} else if (location.y <= -bounds.y) {
			location.y = bounds.y - delta;
		}

	}

	private Vector flock(Boid[] neighbors, Vector bounds) {
		Vector separation = separate(neighbors, bounds).multiply(
				SEPARATION_WEIGHT);
		Vector alignment = align(neighbors).multiply(ALIGNMENT_WEIGHT);
		Vector cohesion = cohere(neighbors, bounds).multiply(COHESION_WEIGHT);
		return separation.add(alignment).add(cohesion);
	}

	/*
	 * Move to center of the neighbors
	 */
	private Vector cohere(Boid[] neighbors, Vector bounds) {
		Vector sum = new Vector(0, 0);
		for (Boid boid : neighbors) {
			sum.add(boid.location);
		}

		if (neighbors.length == 0)
			return sum;

		return steerTo(sum.divide(neighbors.length));
	}

	private Vector steerTo(Vector target) {
		Vector desired = target.subtract(location);
		double d = desired.magnitude();
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

	private Vector align(Boid[] neighbors) {
		Vector mean = new Vector(0, 0);
		for (Boid boid : neighbors) {
			mean.add(boid.velocity);
		}
		if (neighbors.length != 0) {
			mean.divide(neighbors.length);
		}
		return mean.limit(MAX_FORCE);
	}

	private Vector separate(Boid[] neighbors, Vector bounds) {
		Vector mean = new Vector(0, 0);
		int count = 0;
		for (Boid boid : neighbors) {
			double d = location.distanceTo(boid.location);
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
