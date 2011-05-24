package org.ramadda.client.android;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.CheckBox;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.media.MediaRecorder;
import android.os.Environment;


import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class NewNote extends RamaddaActivity implements View.OnClickListener { 
	private boolean recording = false;
	private MediaRecorder recorder;
	private String recordingFile;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.newnote); 
        addClickListener(new int[]{R.id.newnote_create, R.id.newnote_cancel,R.id.newnote_record});
    }
    
    public void onClick(View v) {
    	if(v.getId() == R.id.newnote_record) {
    		try {	
    			if(!recording) {
    				beginRecording();
    			} else {
      				endRecording();
      			}
    		} catch(Exception exc) {
    			throw new RuntimeException(exc);
      		}
      		return;
      	}

    	if(v.getId() == R.id.newnote_create) {
    		CheckBox locationButton = (CheckBox)findViewById(R.id.newnote_location);
    		String name = getTextFromWidget(R.id.newnote_name);
    		String description = getTextFromWidget(R.id.newnote_description);
    		boolean includeLocation = locationButton.isChecked();
    		System.err.println(name +" " + description +" " + includeLocation);
    	}
    	finish();

    }
    
    private void beginRecording() throws IOException {
     	if(recording) return;
        recordingFile = Environment.getExternalStorageDirectory().getAbsolutePath();
        recordingFile += "/voicenote.3gp";
        Log.d("","path:" + recordingFile);

    	 ((Button)findViewById(R.id.newnote_record)).setText("Stop Recording");
    	 recording = true;
    	 recorder = new MediaRecorder();
    	 recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	 recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	 recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    	 recorder.setOutputFile(recordingFile);
    	 recorder.prepare();
    	 recorder.start();   // Recording is now started

    }
    
    private void endRecording() {
    	if(!recording || recorder==null) return;
    	((Button)findViewById(R.id.newnote_record)).setText("Start Recording");
    	recorder.stop();
    	recorder.reset();  
    	recorder.release(); 
    	recorder=null;
    	recording = false;
    }
 
}