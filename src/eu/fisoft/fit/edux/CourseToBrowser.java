package eu.fisoft.fit.edux;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class CourseToBrowser implements OnClickListener {

	private Context context;
	private final String code;
	private final String name;
	
	public CourseToBrowser(Context context, String code, String name) {
		this.context = context;
		this.code = code;
		this.name = name;
	}
	
	public void onClick(View v) {
    	Intent intent = new Intent(context, BrowserActivity.class);
    	intent.putExtra("URL", "https://edux.fit.cvut.cz/courses/"+code);
    	intent.putExtra("NAME", name);
    	context.startActivity(intent);
	}

}
