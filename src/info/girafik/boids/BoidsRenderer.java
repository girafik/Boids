package info.girafik.boids;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.view.Surface;
import android.view.WindowManager;

public class BoidsRenderer implements Renderer {

	static final double RADIUS = 0.5;
	private static float DISTANCE;
	Boid boids[];
	Vector bounds;
	float ratio;
	List<Boid> closeBoids = new ArrayList<Boid>();
	private Context context;
	private int rotation = Surface.ROTATION_0;

	public BoidsRenderer(Context context) {
		this.context = context;
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		for (Boid boid : boids) {
			boid.step(boids, bounds);
		}

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		for (Boid boid : boids) {
			gl.glLoadIdentity();
			// gl.glTranslatef(boid.location.x, boid.location.y, -DISTANCE +
			// boid.location.z);
			gl.glTranslatef(boid.location.x, boid.location.y, -DISTANCE - 2 + boid.location.z);
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

		rotation = ((WindowManager) context.getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			DISTANCE = 2f;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			DISTANCE = 2f / ratio;
			break;
		}

		bounds = new Vector(ratio * DISTANCE, 1 * DISTANCE, 2f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		boids = new Boid[100];
		for (int i = 0; i < boids.length; i++) {
			boids[i] = new Boid();
		}
		gl.glDisable(GL11.GL_DITHER);
		gl.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_FASTEST);

		gl.glEnable(GL11.GL_CULL_FACE);
		gl.glShadeModel(GL11.GL_SMOOTH);
		gl.glDisable(GL11.GL_DEPTH_TEST);
		gl.glEnable(GL11.GL_BLEND);
	}
}
