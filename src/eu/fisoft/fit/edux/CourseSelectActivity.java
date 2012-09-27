package eu.fisoft.fit.edux;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class CourseSelectActivity extends Activity {

	public final String TAG = "CourseSelect";
	private CoursesProvider coursesProvider;
	private ListView list;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_select);
        coursesProvider = CoursesProvider.getInstance(this);
        list = (ListView)findViewById(R.id.coursesListView);
        list.setAdapter(coursesProvider);
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_course_select, menu);
        MenuItem login = menu.findItem(R.id.menu_login);
        login.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			public boolean onMenuItemClick(MenuItem item) {
				login();
				return false;
			}
		});
        return true;
    }
 
    
    public void login(){
    	Intent intent = new Intent(this, LoginActivity.class);
    	startActivity(intent);
    }
}
