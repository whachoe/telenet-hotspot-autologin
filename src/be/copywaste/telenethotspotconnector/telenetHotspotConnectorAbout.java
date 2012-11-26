package be.copywaste.telenethotspotconnector;

import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class telenetHotspotConnectorAbout extends SherlockActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        // setup action bar for tabs
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayShowTitleEnabled(true);
        
	    TextView footer = (TextView) findViewById(R.id.footer);
        Linkify.addLinks(footer, Linkify.ALL);
    }
}
