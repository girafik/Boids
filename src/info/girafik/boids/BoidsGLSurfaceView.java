package info.girafik.boids;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class BoidsGLSurfaceView extends GLSurfaceView {

	private BoidsRenderer renderer;

	public BoidsGLSurfaceView(Context context) {
		super(context);
	}

	@Override
	public void setRenderer(Renderer renderer) {
		this.renderer = (BoidsRenderer) renderer;
		super.setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event != null) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (renderer != null) {
					queueEvent(new TouchEventRun(event.getX(), event.getY()));
				}
			}
		}
		return super.onTouchEvent(event);
	}

	private class TouchEventRun implements Runnable {

		private final float x;
		private final float y;

		public TouchEventRun(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void run() {
			renderer.touch(x, y);

		}

	}
}
