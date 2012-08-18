package info.girafik.boids.wallpaper;

import info.girafik.boids.BoidsRenderer;
import android.opengl.GLSurfaceView.Renderer;

public class BoidsWallpaperService extends OpenGLES2WallpaperService {

	@Override
	Renderer getNewRenderer() {
		return new BoidsRenderer();
	}

}
