package info.girafik.boids;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.opengl.GLSurfaceView.Renderer;

public class BoidsRenderer implements Renderer {

	private static final double RADIUS = 0.6;
	Boid boids[];
	Vector bounds;
	float ratio;
	List<Boid> closeBoids = new ArrayList<Boid>();

	@Override
	public void onDrawFrame(GL10 gl) {

		for (Boid boid : boids) {
			closeBoids.clear();
			for (Boid otherBoid : boids) {
				if (otherBoid != boid
						&& boid.location.distanceTo(otherBoid.location) < RADIUS) {
					closeBoids.add(otherBoid);
				}
			}
			boid.step(closeBoids, bounds);
		}

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		for (Boid boid : boids) {
			gl.glLoadIdentity();
			gl.glTranslatef((float) boid.location.x, (float) boid.location.y,
					-3.0f);
			boid.draw(gl);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		ratio = (float) width / height;
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

		bounds = new Vector(ratio * 3, 1 * 3);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		boids = new Boid[70];
		for (int i = 0; i < boids.length; i++) {
			boids[i] = new Boid();
		}
		gl.glDisable(GL11.GL_DITHER);
		gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);

		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glShadeModel(GL11.GL_SMOOTH);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(GL11.GL_DEPTH_TEST);
		gl.glEnable(GL11.GL_BLEND);

	}
}
