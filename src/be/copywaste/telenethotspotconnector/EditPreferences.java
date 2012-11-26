package be.copywaste.telenethotspotconnector;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EditPreferences extends SherlockPreferenceActivity 
{
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		addPreferencesFromResource(R.xml.preferences);
		
	}
}
