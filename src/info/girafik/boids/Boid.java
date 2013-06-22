package info.girafik.boids;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;

public class Boid {

	static float side = 0.015f;
	private static final float MAX_VELOCITY = 0.03f;
	private static final float DESIRED_SEPARATION = 0.01f;
	private static final float SEPARATION_WEIGHT = 0.05f;
	private static final float ALIGNMENT_WEIGHT = 0.3f;
	private static final float COHESION_WEIGHT = 0.3f;
	private static final float MAX_FORCE = 0.005f;
	private static final float INERTION = 0.001f;
	private static final float CENTRIC_POWER = 0.8f; // > 0 more power
	private static FloatBuffer mFVertexBuffer;
	private static FloatBuffer mColorBuffer;
	private static ByteBuffer mIndexBuffer;
	private static Vector tempVector = new Vector(0, 0, 0);
	private static Vector sum = new Vector(0, 0, 0);
	private static Vector align = new Vector(0, 0, 0);
	private static Vector separate = new Vector(0, 0, 0);

	public static void initModel(float size, int model) {
		Boid.side = size;

		byte indices[] = { // Vertex indices of the 4 Triangles
		2, 4, 3, // front face (CCW)
				1, 4, 2, // right face
				0, 4, 1, // back face
				4, 0, 3 // left face
		};
		float[] vertices = {//
		-side / 3f, -side, -side / 3f, // 0. left-bottom-back
				side / 3f, -side, -side / 3f, // 1. right-bottom-back
				side / 3f, -side, side / 3f, // 2. right-bottom-front
				-side / 3f, -side, side / 3f, // 3. left-bottom-front
				0.0f, side, 0.0f // 4. top
		};

		float colors[] = {//
				(Color.red(model) - 10)/255f, (Color.green(model) - 10)/255f, (Color.blue(model) - 10)/255f, 1.0f, //
				(Color.red(model) - 10)/255f, (Color.green(model) - 10)/255f, (Color.blue(model) - 10)/255f, 1.0f, //
				(Color.red(model) - 10)/255f, (Color.green(model) - 10)/255f, (Color.blue(model) - 10)/255f, 1.0f, //
				(Color.red(model) - 10)/255f, (Color.green(model) - 10)/255f, (Color.blue(model) - 10)/255f, 1.0f, //
				(Color.red(model) + 10)/255f, (Color.green(model) + 10)/255f, (Color.blue(model) + 10)/255f, 1.0f // nose
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

	Vector location;
	Vector velocity;

	public Boid() {
		Random r = new Random();
		location = new Vector(//
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() * 2, //
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() * 2, //
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() * 0.5f);
		velocity = new Vector(//
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f, //
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f, //
				(r.nextBoolean() ? 1f : -1f) * r.nextFloat() / 100f);
	}

	public void draw(GL10 gl) {

		gl.glFrontFace(GL10.GL_CCW);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, 12, GL10.GL_UNSIGNED_BYTE,
				mIndexBuffer);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glFrontFace(GL10.GL_CW);
	}

	public void step(Boid[] boids, int[] neigbours, float[] distances) {
		Vector acceleration = flock(boids, neigbours, distances);
		velocity.add(acceleration).limit(MAX_VELOCITY);
		location.add(velocity);
	}

	private Vector flock(Boid[] boids, int[] neigbours, float[] distances) {
		Vector separation = separate(boids, neigbours, distances).multiply(
				SEPARATION_WEIGHT);
		Vector alignment = align(boids, neigbours).multiply(ALIGNMENT_WEIGHT);
		Vector cohesion = cohere(boids, neigbours).multiply(COHESION_WEIGHT);
		return separation.add(alignment).add(cohesion);
	}

	private Vector cohere(Boid[] boids, int[] neigbours) {
		sum.init();
		for (int n : neigbours) {
			sum.add(boids[n].location);
		}
		sum.z /= 2;
		return steerTo(sum.divide(neigbours.length + CENTRIC_POWER));
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

	private Vector align(Boid[] boids, int[] neigbours) {
		align.init();
		for (int n : neigbours) {
			align.add(boids[n].velocity);
		}
		align.divide(neigbours.length);
		return align.limit(MAX_FORCE);
	}

	private Vector separate(Boid[] boids, int[] neigbours, float[] distances) {
		separate.init();
		int count = 0;
		for (int n : neigbours) {
			Boid boid = boids[n];
			float d = distances[n];
			tempVector.copyFrom(location).subtract(boid.location);
			if (d > 0 && d < DESIRED_SEPARATION) {
				separate.add(tempVector.divide((float) Math.sqrt(d)));
				count++;
			}
		}
		if (count != 0) {
			separate.divide(count);
		}
		return separate;
	}

	public void copyFrom(Boid boid) {
		this.location.copyFrom(boid.location);
		this.velocity.copyFrom(boid.velocity);
	}

}
