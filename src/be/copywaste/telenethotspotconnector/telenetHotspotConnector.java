package be.copywaste.telenethotspotconnector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import be.copywaste.telenethotspotconnector.fragments.FavWiFiFragment;
import be.copywaste.telenethotspotconnector.fragments.MainFragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class telenetHotspotConnector extends SherlockFragmentActivity {

	// Constants
	private static final int ACTIVITY_ABOUT = 1;
	private static final int ACTIVITY_PREFERENCES = 2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setup action bar for tabs
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(true);

	    Tab tab = actionBar.newTab()
	            .setText(R.string.tab_main)
	            .setTabListener(new MyTabListener<MainFragment>(
	                    this, "main", MainFragment.class));
	    actionBar.addTab(tab);
	    
	    Tab tab2 = actionBar.newTab()
	            .setText(R.string.tab_hotspotlist)
	            .setTabListener(new MyTabListener<FavWiFiFragment>(
	                    this, "hotspotlist", FavWiFiFragment.class));
	    actionBar.addTab(tab2);
	    
	    // Select the previously selected tab when coming back (when changing orientation, fi)
	    if (savedInstanceState != null) {
	    	int selectedTab = savedInstanceState.getInt("selected_tab");
	    	actionBar.selectTab(actionBar.getTabAt(selectedTab));
	    }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("selected_tab", (int) getSupportActionBar().getSelectedTab().getPosition());
		super.onSaveInstanceState(outState);
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
	
	
	public final static class MyTabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public MyTabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }

	    /* The following are each of the ActionBar.TabListener callbacks */

	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.replace(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }

	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }

	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}
}
