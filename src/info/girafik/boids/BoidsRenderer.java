package info.girafik.boids;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.FloatMath;
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
	private static Vector normalized = new Vector(0, 0, 0);

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
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		for (Boid boid : boids) {
			gl.glLoadIdentity();
			gl.glTranslatef(boid.location.x, boid.location.y, -3.f * DISTANCE + boid.location.z);

			normalized.copyFrom(boid.velocity).normalize();

			float theta = (float) Math.atan2(normalized.y, normalized.x) * 57.3f;

			float fi = (float) Math.acos(normalized.z
					/ FloatMath.sqrt(normalized.x * normalized.x + normalized.y * normalized.y
							+ normalized.z * normalized.z)) * 57.3f;

			gl.glRotatef(-90f, 0f, 0f, 1f);
			gl.glRotatef(theta, 0f, 0f, 1f);
			gl.glRotatef(fi, 0f, 1f, 0f);

			boid.draw(gl);
		}

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {

		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		ratio = (float) width / height;
		GLU.gluPerspective(gl, 45, ratio, .1f, 100.f);

		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();

		rotation = ((WindowManager) context.getApplicationContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			DISTANCE = 5f;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			DISTANCE = 5f / ratio;
			break;
		}
		bounds = new Vector(ratio * DISTANCE, 1 * DISTANCE, DISTANCE);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		boids = new Boid[100];
		for (int i = 0; i < boids.length; i++) {
			boids[i] = new Boid();
		}

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_DITHER);
	}
}
