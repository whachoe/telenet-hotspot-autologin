package be.copywaste.telenethotspotconnector.fragments;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import be.copywaste.telenethotspotconnector.R;

import com.actionbarsherlock.app.SherlockListFragment;
import com.commonsware.cwac.tlv.TouchListView;

public class FavWiFiFragment extends SherlockListFragment {
	private IconicAdapter adapter=null;	
	WifiManager wm;
	View root;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.favwifi, container, false);
		return root;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the current WIFI
		wm = (WifiManager) getActivity().getSystemService(Activity.WIFI_SERVICE);
		List<WifiConfiguration> wifilist = wm.getConfiguredNetworks();
		
		if (wifilist != null) {
			Collections.sort(wifilist, new Comparator<WifiConfiguration>() {
				@Override
				public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
					// Reversed sorting: we compare rhs instead of lhs
					Integer l = new Integer(rhs.priority);
					return l.compareTo(lhs.priority);
				}
			});
		}
		
		TouchListView tlv=(TouchListView) getListView();
		adapter=new IconicAdapter(wifilist);
		setListAdapter(adapter);
		
		tlv.setDropListener(onDrop);
		tlv.setRemoveListener(onRemove);
	}

	private TouchListView.DropListener onDrop=new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
				WifiConfiguration item=adapter.getItem(from);
				
				adapter.remove(item);
				adapter.insert(item, to);
		}
	};
	
	private TouchListView.RemoveListener onRemove=new TouchListView.RemoveListener() {
		@Override
		public void remove(int which) {
				adapter.remove(adapter.getItem(which));
		}
	};
	
	class IconicAdapter extends ArrayAdapter<WifiConfiguration> {
		List<WifiConfiguration> array;
		
		IconicAdapter(List<WifiConfiguration> list) {
			super(getActivity(), R.layout.favwifirow, list);
			array = list;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View row=convertView;
			
			if (row==null) {	
				LayoutInflater lf = getActivity().getLayoutInflater();   
				row=lf.inflate(R.layout.favwifirow, parent, false);
			}
			
			TextView label=(TextView)row.findViewById(R.id.label);
			label.setText(array.get(position).SSID.replace("\"","")+" ("+String.valueOf(array.get(position).priority)+")");
			
			ImageView status = (ImageView)row.findViewById(R.id.status);
			if (array.get(position).status == WifiConfiguration.Status.CURRENT)
				status.setImageResource(android.R.drawable.presence_online);
			if (array.get(position).status == WifiConfiguration.Status.ENABLED)
				status.setImageResource(android.R.drawable.presence_invisible);
			if (array.get(position).status == WifiConfiguration.Status.DISABLED)
				status.setImageResource(android.R.drawable.presence_offline);
			
			return(row);
		}
	}	
	
	@Override
	public void onPause() {
		for (int i=0; i <adapter.getCount(); i++) {
			WifiConfiguration wi = adapter.getItem(i);
			wi.priority = (adapter.getCount()-i)*10;
			wm.updateNetwork(wi);
		}
		
		adapter.notifyDataSetChanged();
		Toast.makeText(getActivity(), getString(R.string.save_succesful), Toast.LENGTH_LONG).show();
		super.onPause();
	}
}
