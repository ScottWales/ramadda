package org.ramadda.client.android;
import android.app.Activity;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.content.Context;

import java.util.List;

public class GuiUtils {

	public static Button makeButton(Context context,String label, View.OnClickListener listener) {
		Button button = new Button(context);
		button.setText(label);
		button.setOnClickListener(listener);
		return button;
	
	}
	
	public static LinearLayout vbox(Context context, List<View> views) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.FILL_HORIZONTAL);
		for(View view: views) {
			layout.addView(view);
		}
		return layout;
	}	
	
	
}
