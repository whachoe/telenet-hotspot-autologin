package be.copywaste.telenethotspotconnector;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity 
{
	@Override
	public void onCreate(Bundle b)
	{
		super.onCreate(b);
		addPreferencesFromResource(R.xml.preferences);
	}
}
