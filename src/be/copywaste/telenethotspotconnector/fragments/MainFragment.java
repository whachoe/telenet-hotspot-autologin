package be.copywaste.telenethotspotconnector.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import be.copywaste.telenethotspotconnector.R;
import be.copywaste.telenethotspotconnector.telenetHotspotConnectorApplication;

import com.actionbarsherlock.app.SherlockFragment;

public class MainFragment extends SherlockFragment {
	// Properties
	TextView textConnected, textIp, textSsid, textBssid, textMac, textSpeed,
			textRssi;
	ToggleButton loginbutton;
	EditText useridview, userpwview;
	private static SharedPreferences prefs;
	private View root;
	private AsyncWifiInfo WifiInfoLoader;
	
	BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	WifiInfoLoader = new AsyncWifiInfo();
			WifiInfoLoader.execute();
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.main, container, false);
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		textConnected = (TextView) root.findViewById(R.id.Connected);
		textIp = (TextView) root.findViewById(R.id.Ip);
		textSsid = (TextView) root.findViewById(R.id.Ssid);
		textBssid = (TextView) root.findViewById(R.id.Bssid);
		textMac = (TextView) root.findViewById(R.id.Mac);
		textSpeed = (TextView) root.findViewById(R.id.Speed);
		textRssi = (TextView) root.findViewById(R.id.Rssi);
		loginbutton = (ToggleButton) root.findViewById(R.id.loginbutton);
		loginbutton.setVisibility(View.INVISIBLE);

		// Setting the username and password from prefs
		useridview = (EditText) root.findViewById(R.id.userid);
		userpwview = (EditText) root.findViewById(R.id.password);
		useridview.setText(prefs.getString("userid", ""));
		userpwview.setText(prefs.getString("userpw", ""));

		// Initializing
		textConnected.setText(getString(R.string.disconnected));
		textIp.setText("---");
		textSsid.setText("---");
		textBssid.setText("---");
		textSpeed.setText("---");
		textRssi.setText("---");
		textMac.setText("---");
		
		// What to do when we clicked the 'save' button:
		root.findViewById(R.id.savebutton).setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Save those values
				Editor editor = prefs.edit();
				editor.putString("userid", useridview.getText().toString().trim());
				editor.putString("userpw", userpwview.getText().toString().trim());
				if (prefs.getString("userid_homespot", "").length() == 0)
					editor.putString("userid_homespot", useridview.getText().toString().trim());
				if (prefs.getString("userpw_homespot", "").length() == 0)
					editor.putString("userpw_homespot", userpwview.getText().toString().trim());
				
				if (editor.commit()) {
					Toast.makeText(getActivity(), getString(R.string.login_saved), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText( getActivity(),getString(R.string.prefs_save_error),Toast.LENGTH_LONG).show();
				}
			}
		});
		
		// What to do when we click the 'login' button:
		loginbutton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToggleButton button = (ToggleButton) v;
				if (button.isChecked()) {
					new AsyncLogin().execute();
				} else {
					// LOG OFF
					new AsyncLogout().execute();
				}
			}
		});
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		getActivity().registerReceiver(networkStateReceiver, filter);
		
		WifiInfoLoader = new AsyncWifiInfo();
		WifiInfoLoader.execute();
	}
	
	@Override
	public void onPause() {
		getActivity().unregisterReceiver(networkStateReceiver);
		WifiInfoLoader.cancel(true);
		
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
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
//			if (didwelogin) {
				WifiInfoLoader = new AsyncWifiInfo();
				WifiInfoLoader.execute();
//			} else {
//				loginbutton.setChecked(false);
//			}
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
			WifiInfoLoader = new AsyncWifiInfo();
			WifiInfoLoader.execute();
		}
	}
	
	class AsyncWifiInfoResult {
		public WifiInfo info;
		public boolean webIsReachable;
	}
	
	class AsyncWifiInfo extends AsyncTask<Void, String, AsyncWifiInfoResult> {

		protected AsyncWifiInfoResult doInBackground(Void... params) {
			Context context = (Context) getActivity();
			AsyncWifiInfoResult result = null;
			
			if (context != null) {
				WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
				result = new AsyncWifiInfoResult();
			
				result.info = myWifiInfo;
				try {
					result.webIsReachable = telenetHotspotConnectorApplication.webIsReachable();
				} catch (Exception e) {
					result.webIsReachable = false;
				}
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(AsyncWifiInfoResult result) {
			if (result == null)
				return;
			
			WifiInfo myWifiInfo = result.info;
			if (myWifiInfo != null && myWifiInfo.getSSID() != null) {
				textConnected.setText(getString(R.string.connected));
				
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
				textConnected.setText(getString(R.string.disconnected));
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
