package com.linnap.routereplay;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.linnap.routereplay.replay.Loader;
import com.linnap.routereplay.replay.Loader.LoaderException;

public class ChooseReplayActivity extends ListActivity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        String[] replayNames = loadReplayNames();
        
        if (replayNames != null && replayNames.length > 0) {        
        	setListAdapter(new ArrayAdapter<String>(this, R.layout.choose_replay_listitem, replayNames));
        
        	ListView lv = getListView();
        	lv.setTextFilterEnabled(true);

        	lv.setOnItemClickListener(new OnItemClickListener() {
        		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        			try {
        				ApplicationGlobals app = (ApplicationGlobals)getApplicationContext();
        				app.loadedReplay = Loader.load((String)((TextView)view).getText());
        				Intent intent = new Intent(ChooseReplayActivity.this, ReplayInfoActivity.class);
            			startActivity(intent);
        			} catch (LoaderException e) {
        				Toast.makeText(ChooseReplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        			}
        		}
        	});
        } else {
        	Toast.makeText(this, "There are no replays.", Toast.LENGTH_LONG).show();
        }
    }
	
	public void onResume() {
		super.onResume();
		((ApplicationGlobals)getApplicationContext()).killCapture();
		Log.d(Utils.TAG, "ChooseReplay resuming");
	}
	
	public void onPause() {
		super.onPause();
		Log.d(Utils.TAG, "ChooseReplay pausing");
	}

	private String[] loadReplayNames() {
		return Utils.DATA_DIR.list();
	}
}
