package info.girafik.boids;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class Boid {

	static float side = 0.05f;
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
	private static Vector temp = new Vector(0, 0, 0);
	private static Vector sum = new Vector(0, 0, 0);
	private static Vector align = new Vector(0, 0, 0);
	private static Vector separate = new Vector(0, 0, 0);

	static {
		byte indices[] = { // Vertex indices of the 4 Triangles
		2, 4, 3, // front face (CCW)
				1, 4, 2, // right face
				0, 4, 1, // back face
				4, 0, 3 // left face
		};
		float[] vertices = { -side / 2f, -side, -side / 2f, // 0.
															// left-bottom-back
				side / 2f, -side, -side / 2f, // 1. right-bottom-back
				side / 2f, -side, side / 2f, // 2. right-bottom-front
				-side / 2f, -side, side / 2f, // 3. left-bottom-front
				0.0f, side, 0.0f // 4. top
		};

		float colors[] = { 0.0f, 0.0f, 1.0f, 1.0f, // blue
				0.0f, 0.0f, 1.0f, 1.0f, // blue
				0.0f, 0.0f, 1.0f, 1.0f, // blue
				0.0f, 0.0f, 1.0f, 1.0f, // blue
				1.0f, 1.0f, 0.0f, 1.0f}; // yellow

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
		location = new Vector(r.nextFloat() * 3, r.nextFloat() * 3, r.nextFloat() * 0.3f);
		velocity = new Vector((r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f,
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f, (r.nextBoolean() ? 1f : -1f)
						* r.nextFloat() / 100f);
	}

	public void draw(GL10 gl) {

		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, 12, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glFrontFace(GL10.GL_CW);
	}

	public void step(Boid[] boids, Vector bounds) {
		Vector acceleration = flock(boids, bounds);
		velocity.add(acceleration).limit(MAX_VELOCITY);
		rotate(bounds);
		location.add(velocity);
	}

	private void rotate(Vector bounds) {
		if (location.x >= bounds.x - 0.1f && velocity.x > 0) {
			float scale = (bounds.x - location.x);
			velocity.x *= scale;
		} else if (location.x <= 0.1f - bounds.x && velocity.x < 0) {
			float scale = (location.x - bounds.x);
			velocity.x *= scale;
		}

		if (location.y >= bounds.y - 0.1f && velocity.y > 0) {
			float scale = (bounds.y - location.y);
			velocity.y *= scale;
		} else if (location.y <= 0.1f - bounds.y && velocity.y < 0) {
			float scale = (location.y - bounds.y);
			velocity.y *= scale;
		}

		if (location.z >= bounds.z - 0.1f && velocity.z > 0) {
			float scale = (bounds.z - location.z);
			velocity.z *= scale;
		} else if (location.z <= 0.1f - bounds.z && velocity.z < 0) {
			float scale = (location.z - bounds.z);
			velocity.z *= scale;
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
			steer = new Vector(0, 0, 0);
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
