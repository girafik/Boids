package info.girafik.boids;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.WindowManager;

import java.util.BitSet;

public class BoidsRenderer implements Renderer {

	private static final int NEIGHBOURS = 7;
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
    private BitSet[][] grid;
	private float[] temp_dists = new float[NEIGHBOURS];
	private float ratio;
	private float r;
	private float g;
	private float b;

    long startTime = System.currentTimeMillis();
    long dt;


    public BoidsRenderer(Context context) {
		this.context = context;
	}

	@Override
	public void onDrawFrame(GL10 gl) {

        dt = System.currentTimeMillis() - startTime;
        if (dt < 30){
            try {
                Thread.sleep(30 - dt);
            } catch (InterruptedException e) {
            }
        }
        startTime = System.currentTimeMillis();

        calculateScene();

		gl.glClearColor(r, g, b, 1.0f);
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


    private int index(float x){
        if(Math.abs(x)>4f){
            x = (x>0?1:1)*4f;
        }
        x += 4f;
        x *= 3;
        if (x>=24){
            x=23;
        }
        return (int) Math.floor(x);
    }

    private void calculateScene(){
        for(int i=0; i<grid.length; i++){
            for(int j=0; j<grid.length; j++){
                grid[i][j].clear();
                grid[j][i].clear();
            }
        }

        for(int b = 0; b<boids.length; b++){
            int i = index(boids[b].location.x);
            int j = index(boids[b].location.y);
            grid[i][j].set(b);
        }

        for(int b=0; b<boids.length; b++){
            int count = 0;
            int x = index(boids[b].location.x);
            int y = index(boids[b].location.y);
            int r = 0;
            while(count < neigbours[b].length){
                for(int i=Math.max(0, x-r); i<=x+r && i<grid.length; i++){
                    for(int j=Math.max(0, y-r); j<=y+r && j<grid.length; j++){
                        if(Math.abs(x-i) == r || Math.abs(y-j) == r){
                            int bit = 0;
                            while(grid[i][j].nextSetBit(bit) >= 0 && count < neigbours[b].length && !grid[i][j].isEmpty()){
                                bit = grid[i][j].nextSetBit(bit);
                                neigbours[b][count] = bit;
                                count++;
                                bit++;
                            }
                        }
                    }
                }
                r++;
            }
            for(int n=0; n<neigbours[b].length; n++){
                float distance = tempVector.copyFrom(boids[b].location)
                        .subtract(boids[neigbours[b][n]].location).magnitude2();
                distances[b][neigbours[b][n]] = distance;
            }
        }

        for (int b = 0; b < boids.length; b++) {
            newBoids[b].copyFrom(boids[b]);
        }

        for (int b = 0; b < boids.length; b++) {
            boids[b].step(newBoids, neigbours[b], distances[b]);
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
		float size = sp.getInt("boidsSize", 4) / 100f;
		int model = sp.getInt("model", 0xff0099CC);

		Boid.initModel(size, model);

		int backgroud = sp.getInt("background", 0xff000000);
		r = Color.red(backgroud) / 255f;
		g = Color.green(backgroud) / 255f;
		b = Color.blue(backgroud) / 255f;

		int count = sp.getInt("boidsCount", 100);
		boids = new Boid[count];
		newBoids = new Boid[boids.length];
		for (int i = 0; i < boids.length; i++) {
			boids[i] = new Boid();
			newBoids[i] = new Boid();
		}
		distances = new float[boids.length][boids.length];
		neigbours = new int[boids.length][NEIGHBOURS];
        grid = new BitSet[64][64];
        for(int i=0; i<grid.length; i++){
            for(int j=0; j<grid.length; j++){
                grid[i][j] = new BitSet();
                grid[j][i] = new BitSet();
            }
        }

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
