package info.girafik.boids.wallpaper;

import android.opengl.GLSurfaceView.Renderer;
import android.view.SurfaceHolder;

public abstract class OpenGLES2WallpaperService extends GLWallpaperService {
	@Override
	public Engine onCreateEngine() {
		return new OpenGLES2Engine();
	}

	class OpenGLES2Engine extends GLWallpaperService.GLEngine {

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			setRenderer(getNewRenderer());

		}
	}

	abstract Renderer getNewRenderer();
}
