package info.girafik.boids.wallpaper;

import info.girafik.boids.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;

public class WallpaperSettings extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
