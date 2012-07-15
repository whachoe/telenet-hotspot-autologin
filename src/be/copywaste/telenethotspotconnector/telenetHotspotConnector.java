package be.copywaste.telenethotspotconnector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class telenetHotspotConnector extends Activity implements
		View.OnClickListener {

	// Constants
	private static final int ACTIVITY_ABOUT = 1;
	private static final int ACTIVITY_PREFERENCES = 2;
	
	// Properties
	TextView textConnected, textIp, textSsid, textBssid, textMac, textSpeed,
			textRssi;
	ToggleButton loginbutton;
	EditText useridview, userpwview;
	private static SharedPreferences prefs;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		textConnected = (TextView) findViewById(R.id.Connected);
		textIp = (TextView) findViewById(R.id.Ip);
		textSsid = (TextView) findViewById(R.id.Ssid);
		textBssid = (TextView) findViewById(R.id.Bssid);
		textMac = (TextView) findViewById(R.id.Mac);
		textSpeed = (TextView) findViewById(R.id.Speed);
		textRssi = (TextView) findViewById(R.id.Rssi);
		loginbutton = (ToggleButton) findViewById(R.id.loginbutton);
		loginbutton.setVisibility(View.INVISIBLE);

		// Setting the username and password from prefs
		useridview = (EditText) findViewById(R.id.userid);
		userpwview = (EditText) findViewById(R.id.password);
		useridview.setText(prefs.getString("userid", ""));
		userpwview.setText(prefs.getString("userpw", ""));

		// Initializing
		textConnected.setText("--- DISCONNECTED! ---");
		textIp.setText("---");
		textSsid.setText("---");
		textBssid.setText("---");
		textSpeed.setText("---");
		textRssi.setText("---");
		textMac.setText("---");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		new AsyncWifiInfo().execute();		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.savebutton:

			// Save those values
			Editor editor = prefs.edit();
			editor.putString("userid", useridview.getText().toString().trim());
			editor.putString("userpw", userpwview.getText().toString().trim());
			if (prefs.getString("userid_homespot", "") == "")
				editor.putString("userid_homespot", useridview.getText().toString().trim());
			if (prefs.getString("userpw_homespot", "") == "")
				editor.putString("userpw_homespot", userpwview.getText().toString().trim());
			
			if (editor.commit()) {
				Toast.makeText(this, "Login and password saved!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText( this,
								"There was a problem while saving your credentials. Please try again",
								Toast.LENGTH_LONG).show();
			}
			break;

		case R.id.loginbutton:
			ToggleButton button = (ToggleButton) v;
			if (button.isChecked()) {
				new AsyncLogin().execute();
			} else {
				// LOG OFF
				new AsyncLogout().execute();
			}

			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, ACTIVITY_PREFERENCES, 1, R.string.menu_preferences);
		menu.add(1, ACTIVITY_ABOUT, 2, R.string.menu_about);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		boolean bla = super.onMenuItemSelected(featureId, item);

		switch(item.getItemId()) {
		case ACTIVITY_ABOUT:
			startActivity(new Intent(this, telenetHotspotConnectorAbout.class));
			return(true);
			
		case ACTIVITY_PREFERENCES:
			startActivity(new Intent(this, EditPreferences.class));
			return(true);	
		}
		
		return bla;
	}
	
	class AsyncLogin extends AsyncTask<Void, String, Boolean> {

		protected Boolean doInBackground(Void... params) {
			SparseArray<String> creds = telenetHotspotConnectorApplication.getCredentialsForSSID(textSsid.getText().toString(), prefs);
			String userid=""; String userpw="";
			if (creds != null) {
				userid = creds.get(0); 
				userpw = creds.get(1);
			}

			return Boolean.valueOf(telenetHotspotConnectorApplication.doLogin(textSsid.getText().toString(), userid, userpw));
		}
		
		@Override
		protected void onPostExecute(Boolean didwelogin) {
			// Set status of button depending on our web-connectivity
			if (didwelogin && telenetHotspotConnectorApplication.webIsReachable()) {
				loginbutton.setChecked(true);
			} else {
				loginbutton.setChecked(false);
			}
		}
	}

	class AsyncLogout extends AsyncTask<Void, String, Void> {

		protected Void doInBackground(Void... params) {
			telenetHotspotConnectorApplication.doLogout(textSsid.getText().toString());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void unused) {
			// Set status of button depending on our web-connectivity
			if (telenetHotspotConnectorApplication.webIsReachable()) {
				loginbutton.setChecked(true);
			} else {
				loginbutton.setChecked(false);
			}
		}
	}
	
	class AsyncWifiInfoResult {
		public WifiInfo info;
		public boolean webIsReachable;
	}
	
	class AsyncWifiInfo extends AsyncTask<Void, String, AsyncWifiInfoResult> {

		protected AsyncWifiInfoResult doInBackground(Void... params) {
			WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			AsyncWifiInfoResult result = new AsyncWifiInfoResult();
			
			result.info = myWifiInfo;
			result.webIsReachable = telenetHotspotConnectorApplication.webIsReachable();
			return result;
		}
		
		@Override
		protected void onPostExecute(AsyncWifiInfoResult result) {
			WifiInfo myWifiInfo = result.info;
			if (myWifiInfo != null && myWifiInfo.getSSID() != null) {
				textConnected.setText("--- CONNECTED ---");
				
				int myIp = myWifiInfo.getIpAddress();
				int myIpSegment1 = myIp >> 24 & 0xFF;
				int myIpSegment2 = myIp >> 16 & 0xFF;
				int myIpSegment3 = myIp >> 8 & 0xFF;
				int myIpSegment4 = myIp >> 0 & 0xFF;
				
				textMac.setText(myWifiInfo.getMacAddress());
				textIp.setText(String.valueOf(myIpSegment4) + "."
						+ String.valueOf(myIpSegment3) + "." + String.valueOf(myIpSegment2)
						+ "." + String.valueOf(myIpSegment1));

				textSsid.setText(myWifiInfo.getSSID());
				textBssid.setText(myWifiInfo.getBSSID());

				textSpeed.setText(String.valueOf(myWifiInfo.getLinkSpeed()) + " "
						+ WifiInfo.LINK_SPEED_UNITS);
				textRssi.setText(String.valueOf(myWifiInfo.getRssi()));

				// When connected, check if we're logged in
				if (myWifiInfo.getSSID().equals(telenetHotspotConnectorApplication.hotspotssid) || myWifiInfo.getSSID().equals(telenetHotspotConnectorApplication.homespotssid)) {
					loginbutton.setVisibility(View.VISIBLE);
					
					// We are checking if we can reach the testurl:
					if (result.webIsReachable) {
						loginbutton.setChecked(true);
					} else {
						loginbutton.setChecked(false);
					}
				} else {
					// No loginbutton when on different SSID
					loginbutton.setVisibility(View.INVISIBLE);
				}
			} else {
				textConnected.setText("--- DISCONNECTED! ---");
				textIp.setText("---");
				textSsid.setText("---");
				textBssid.setText("---");
				textSpeed.setText("---");
				textRssi.setText("---");
				textMac.setText("---");
			}
		}
	}
}