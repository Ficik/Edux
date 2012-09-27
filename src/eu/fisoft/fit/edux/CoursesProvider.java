package eu.fisoft.fit.edux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import eu.fisoft.fit.edux.storage.AttendedSQLHelper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CoursesProvider extends BaseAdapter implements Iterable<Course> {

	private LayoutInflater layoutInflater;
	private List<Course> items = new ArrayList<Course>();
	private DefaultHttpClient client;
	private Context context;
	private static CoursesProvider instance;
	
	public static CoursesProvider getInstance(Context context) {
		if (instance == null)
			instance = new CoursesProvider(context);
		return instance;
	}

	private CoursesProvider(Context context) {
		this.context = context ;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		client = new DefaultHttpClient();
		client.setCookieStore(EduxCookieStore.getInstance(context));
		AttendedSQLHelper db = new AttendedSQLHelper(context);
		items = db.all();

	}

	private class CourseIterator implements Iterator<Course> {

		private int index = 0;

		public boolean hasNext() {
			return index < items.size();
		}

		public Course next() {
			return items.get(index++);
		}

		public void remove() {

		}

	}

	public Iterator<Course> iterator() {
		return new CourseIterator();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = layoutInflater.inflate(R.layout.course_list_item, null);
		Course course = (Course) getItem(position);
		((TextView) view.findViewById(R.id.code)).setText(course.getCode());
		((TextView) view.findViewById(R.id.descrition)).setText(course
				.getName());
		view.setOnClickListener(new CourseToBrowser(context, course.getCode(), course.getName()));
		
		return view;
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return items.get(position).hashCode();
	}
	
	public void fetchCourses(){
		new FetchCoursesTask(this).execute();
	}
	
	public Course getCourseByCode(String code){
		for(Course course : items)
			if (course.getCode().equals(code))
				return course;
		return null;
	}


	class FetchCoursesTask extends AsyncTask<String, String, String> {

		BaseAdapter adapter;

		public FetchCoursesTask(BaseAdapter adapter) {
			this.adapter = adapter;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpPost request = new HttpPost(
						"https://edux.fit.cvut.cz/lib/exe/ajax.php?dashboard_current_lang=cs");
				List<NameValuePair> data = new ArrayList<NameValuePair>();
				data.add(new BasicNameValuePair("call",
						"dashboard_widget_update"));
				data.add(new BasicNameValuePair("lazy", "1"));
				data.add(new BasicNameValuePair("widget_max", "0"));
				data.add(new BasicNameValuePair("widget_real_id",
						"w_actual_courses_fit"));

				request.setEntity(new UrlEncodedFormEntity(data));

				HttpResponse response = client.execute(request);

				Pattern sectokPatter = Pattern
						.compile("courses\\\\/([^\\\\]+)\\\\/");
				BufferedReader reader;
				reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				String line;
				while ((line = reader.readLine()) != null) {
					Matcher matcher = sectokPatter.matcher(line);
					Log.v("EDUX", "Scanning for courses: "+line);
					while (matcher.find()){
						Log.v("EDUX", "Course found: "+matcher.group(1));
						items.add(new Course(matcher.group(1), "", matcher
								.group(1)));
					}
				}
			} catch (IOException e) {
				Toast toast = Toast.makeText(context, "Connection problem",
						Toast.LENGTH_LONG);
				toast.show();
			}

		
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			adapter.notifyDataSetChanged();
			new AttendedSQLHelper(context).fill(items);
			super.onPostExecute(result);
		}

	}

}
