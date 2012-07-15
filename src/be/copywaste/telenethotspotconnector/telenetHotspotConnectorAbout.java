package be.copywaste.telenethotspotconnector;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class telenetHotspotConnectorAbout extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        TextView footer = (TextView) findViewById(R.id.footer);
        Linkify.addLinks(footer, Linkify.ALL);
    }
}
