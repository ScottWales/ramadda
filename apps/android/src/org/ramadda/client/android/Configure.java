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

public class Configure extends RamaddaActivity implements View.OnClickListener { 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configure); 
        addClickListener(new int[]{R.id.configure_save, R.id.configure_cancel});
        setText(R.id.configure_server,RamaddaClient.getServer(this));
        setText(R.id.configure_user,RamaddaClient.getUserId(this));
        setText(R.id.configure_password, RamaddaClient.getPassword(this));        
    }
    

    public void onClick(View v) {
    	if(v.getId() == R.id.configure_save) {
    		RamaddaClient.putServer(this, getTextFromWidget(R.id.configure_server));
    		RamaddaClient.putUserId(this, getTextFromWidget(R.id.configure_user));
    		RamaddaClient.putPassword(this, getTextFromWidget(R.id.configure_password));
    	}
        finish();
    	
    }


 
}