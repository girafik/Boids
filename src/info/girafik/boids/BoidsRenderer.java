package info.girafik.boids;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.WindowManager;

public class BoidsRenderer implements Renderer {

	private static final int NEIGHBOURS = 7;
	static final float RADIUS = 0.05f;
	private static float DISTANCE;
	Boid boids[];
	Boid newBoids[];
	private Context context;
	private int rotation = Surface.ROTATION_0;
	private int width;
	private int height;
	private static Vector tempVector = new Vector(0, 0, 0);
	float[][] distances;
	private int[][] neigbours;
	private float[] temp_dists = new float[NEIGHBOURS];
	private float ratio;

	public BoidsRenderer(Context context) {
		this.context = context;
	}

	@Override
	public void onDrawFrame(GL10 gl) {

		calculateScene();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL11.GL_COLOR_ARRAY);
		for (Boid boid : boids) {
			gl.glLoadIdentity();
			gl.glTranslatef(boid.location.x, boid.location.y, -DISTANCE
					+ boid.location.z);

			tempVector.copyFrom(boid.velocity).normalize();
			float theta = (float) Math.atan2(tempVector.y, tempVector.x) * 57.3f;
			float fi = (float) Math.acos(tempVector.z / tempVector.magnitude()) * 57.3f;

			gl.glRotatef(-90f, 0f, 0f, 1f);
			gl.glRotatef(theta, 0f, 0f, 1f);
			gl.glRotatef(fi, 0f, 1f, 0f);

			boid.draw(gl);
		}

	}

	private void calculateScene() {
		for (int i = 0; i < boids.length; i++) {
			Boid current = boids[i];
			for (int j = i + 1; j < boids.length; j++) {
				float distance = tempVector.copyFrom(current.location)
						.subtract(boids[j].location).magnitude();
				distances[i][j] = distance;
				distances[j][i] = distance;
			}
		}

		for (int i = 0; i < boids.length; i++) {
			System.arraycopy(distances[i], 0, temp_dists, 0, NEIGHBOURS);
			for (int j = 0; j < NEIGHBOURS; j++) {
				neigbours[i][j] = j;
			}

			for (int j = NEIGHBOURS; j < boids.length; j++) {
				for (int k = 0; k < temp_dists.length; k++) {
					if (temp_dists[k] > distances[i][j]) {
						temp_dists[k] = distances[i][j];
						neigbours[i][k] = j;
						break;
					}
				}
			}
		}

		for (int i = 0; i < boids.length; i++) {
			newBoids[i].copyFrom(boids[i]);
		}
		for (int i = 0; i < boids.length; i++) {
			boids[i].step(newBoids, neigbours[i]);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		ratio = (float) width / height;

		rotation = ((WindowManager) context.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			DISTANCE = 12f;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			DISTANCE = 12f / ratio;
			break;
		}

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL11.GL_PROJECTION);
		gl.glLoadIdentity();
		GLU.gluPerspective(gl, 45, ratio, DISTANCE - 5f, DISTANCE + 5f);
		gl.glMatrixMode(GL11.GL_MODELVIEW);
		gl.glLoadIdentity();

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		float size = Float.parseFloat(sp.getString("size", "0.04"));
		Boid.initModel(size);

		int count = Integer.parseInt(sp.getString("count", "150"));
		boids = new Boid[count];
		newBoids = new Boid[boids.length];
		for (int i = 0; i < boids.length; i++) {
			boids[i] = new Boid();
			newBoids[i] = new Boid();
		}
		distances = new float[boids.length][boids.length];
		neigbours = new int[boids.length][NEIGHBOURS];

		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glDisable(GL10.GL_DITHER);
	}

	public void touch(float x, float y) {

		float relx = (x - width / 2f) / width * (ratio * DISTANCE);
		float rely = (height / 2f - y) / height * DISTANCE;

		for (Boid boid : boids) {
			boid.velocity.x = relx - boid.location.x;
			boid.velocity.y = rely - boid.location.y;
			boid.velocity.z = 0 - boid.location.z;
			boid.velocity.copyFrom(tempVector.copyFrom(boid.velocity)
					.normalize());
		}

	}
}
