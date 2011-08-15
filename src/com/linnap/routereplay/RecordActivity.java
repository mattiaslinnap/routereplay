package com.linnap.routereplay;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RecordActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView textview = new TextView(this);
        textview.setText("This is the Record tab");
        setContentView(textview);
    }
}
