package com.linnap.routereplay;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {
	
	public static final String TAG = "RouteReplay";
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Record tab
	    intent = new Intent().setClass(this, RecordActivity.class);
	    spec = tabHost.newTabSpec("record").setIndicator("Record", res.getDrawable(R.drawable.tab_record)).setContent(intent);
	    tabHost.addTab(spec);

	    // Replay tab
	    intent = new Intent().setClass(this, ReplayActivity.class);
	    spec = tabHost.newTabSpec("replay").setIndicator("Replay", res.getDrawable(R.drawable.tab_record)).setContent(intent);
	    tabHost.addTab(spec);

	    
	    // Scheduled replay tab
	    // TODO

	    tabHost.setCurrentTab(0);
	}
}
