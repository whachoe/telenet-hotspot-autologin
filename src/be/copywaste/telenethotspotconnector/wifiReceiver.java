package be.copywaste.telenethotspotconnector;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.SparseArray;

public class wifiReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (isConnectedIntent(intent)) {
			telenetHotspotConnectorApplication.logger("Received network change");
			
			WifiManager myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			
			// Getting SSID
			telenetHotspotConnectorApplication.logger("It is a WIFI change");
			String ssid = myWifiInfo.getSSID();
			ssid = ssid.replaceAll("^\"|\"$", "");
			if (ssid != null && !ssid.equals("")) {
				telenetHotspotConnectorApplication.logger("SSID: "+ssid);
			}
			
			if (telenetHotspotConnectorApplication.webIsReachable()) {
				telenetHotspotConnectorApplication.logger("We already have Internet access, no need to login");
				return;
			}
			
			// Checking SSID
			if (ssid != null && (ssid.equals(telenetHotspotConnectorApplication.hotspotssid) || ssid.equals(telenetHotspotConnectorApplication.homespotssid))) {
				// This string is just used for the notification message
				String hotspotname = (ssid.equals(telenetHotspotConnectorApplication.hotspotssid) ? "Hotspot" : "Homespot");

				// If the user asked not to log into hotspots: just leave now
				if (ssid.equals(telenetHotspotConnectorApplication.hotspotssid)
				&&	!prefs.getBoolean("login_hotspot", true)) {
					return;
				}
				
				// If the user asked not to log into homespots: just leave now
				if (ssid.equals(telenetHotspotConnectorApplication.homespotssid)
				&&	!prefs.getBoolean("login_homespot", true)) { 		
					return;
				}
				
				// Log the IP
				telenetHotspotConnectorApplication.logger("IP: "+String.valueOf(myWifiInfo.getIpAddress()));
				
				// Getting user-credentials
				SparseArray<String> creds = telenetHotspotConnectorApplication.getCredentialsForSSID(ssid, prefs);
				String userid=""; String userpw="";
				if (creds != null) {
					userid = creds.get(0); 
					userpw = creds.get(1);
				}
				
				// Log in
				if (!userid.trim().equals("") && !userpw.trim().equals("")) {
					telenetHotspotConnectorApplication.doLogin(ssid, userid, userpw);
					
					if (telenetHotspotConnectorApplication.webIsReachable()) {
						if (prefs.getBoolean("show_notifications", false)) {
							// Set up a notification so the user knows he's connected
							NotificationManager mgr=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
							Notification note=new Notification(R.drawable.icon, "Telenet "+hotspotname+" AutoLogin", System.currentTimeMillis()); 
							PendingIntent i=PendingIntent.getActivity(context, 0, new Intent(context, telenetHotspotConnector.class), 0);
							note.setLatestEventInfo(context, "Telenet "+hotspotname+" AutoLogin", context.getString(R.string.login_success), i);
							note.flags|=Notification.FLAG_AUTO_CANCEL;
							mgr.notify(9876, note);
						}
					} else {
						if (prefs.getBoolean("show_notifications", false)) {
							// Set up a notification so the user knows he's connected
							NotificationManager mgr=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
							Notification note=new Notification(R.drawable.icon, "Telenet "+hotspotname+" AutoLogin", System.currentTimeMillis()); 
							PendingIntent i=PendingIntent.getActivity(context, 0, new Intent(context, telenetHotspotConnector.class), 0);
							note.setLatestEventInfo(context, "Telenet "+hotspotname+" AutoLogin",context.getString(R.string.login_failed), i);
							note.flags|=Notification.FLAG_AUTO_CANCEL;
							mgr.notify(9876, note);
						}
					}
				} else {
					// Set up a notification so the user knows he has to configure his credentials
					NotificationManager mgr=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification note=new Notification(R.drawable.icon, "Telenet "+hotspotname+" AutoLogin", System.currentTimeMillis()); 
					PendingIntent i=PendingIntent.getActivity(context, 0, new Intent(context, telenetHotspotConnector.class), 0);
					note.setLatestEventInfo(context, "Telenet "+hotspotname+" AutoLogin", context.getString(R.string.login_failed_nopw), i);
					note.defaults |= Notification.DEFAULT_VIBRATE;
					note.defaults |= Notification.DEFAULT_SOUND;
					note.flags |=Notification.FLAG_AUTO_CANCEL;
					mgr.notify(9876, note);
				}
			}
		}
	}
	
	private boolean isConnectedIntent(Intent intent) {
        NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

        return (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}
}
