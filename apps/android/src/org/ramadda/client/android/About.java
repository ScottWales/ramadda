package org.ramadda.client.android;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class About extends RamaddaActivity { 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about); 
    }
 
}