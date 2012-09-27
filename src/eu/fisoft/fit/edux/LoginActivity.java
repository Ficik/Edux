package eu.fisoft.fit.edux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class LoginActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
		Spinner auth_spinner = (Spinner) findViewById(R.id.auth_provider);
		auth_spinner.setAdapter(new ArrayAdapter<AuthMethod>(this,
				android.R.layout.simple_spinner_item, AuthMethod.values()));

	}

	public void login(View view) {
		Spinner auth_spinner = (Spinner) findViewById(R.id.auth_provider);
		EditText user_text = (EditText) findViewById(R.id.username);
		EditText pass_text = (EditText) findViewById(R.id.password);
		AuthMethod method = (AuthMethod) auth_spinner.getSelectedItem();
		String username = user_text.getText().toString();
		String password = pass_text.getText().toString();
		new LoginTask(username, password, method, this).execute();
	}

	class LoginTask extends AsyncTask<String, String, String> {
		private String username;
		private String password;
		private AuthMethod method;
		private Context context;
		
		
		public LoginTask(String username, String password, AuthMethod method,
				Context context) {
			super();
			this.username = username;
			this.password = password;
			this.method = method;
			this.context = context;
		}

		@Override
		protected String doInBackground(String... args) {
			try {
				tryLogin();
			} catch (IOException e) {
				Toast toast = Toast.makeText(context, "Connection problem",
						Toast.LENGTH_LONG);
				toast.show();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			Intent intent = new Intent(context, CourseSelectActivity.class);
	    	startActivity(intent);
		}

		private void tryLogin() throws IOException {
			DefaultHttpClient client = new DefaultHttpClient();
			client.setCookieStore(EduxCookieStore.getInstance(context));
			String sectok = getSectok(client);
			HttpPost request = new HttpPost(
					"https://edux.fit.cvut.cz/start?do=login");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("do", "login"));
			data.add(new BasicNameValuePair("u", username));
			data.add(new BasicNameValuePair("p", password));
			data.add(new BasicNameValuePair("authnProvider", ""
					+ method.getAuthID()));
			data.add(new BasicNameValuePair("r", "1"));
			data.add(new BasicNameValuePair("sectok", sectok));
			request.setEntity(new UrlEncodedFormEntity(data));
			client.execute(request);
			
			EduxCookieStore cookies = (EduxCookieStore)client.getCookieStore();
			cookies.write(context);
			CoursesProvider.getInstance(context).fetchCourses();
		}

		private String getSectok(HttpClient client) throws IOException {
			HttpPost request = new HttpPost(
					"https://edux.fit.cvut.cz/start?do=login");
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("do", "login"));
			request.setEntity(new UrlEncodedFormEntity(data));

			HttpResponse response = client.execute(request);
			Pattern sectokPatter = Pattern
					.compile("name=\"sectok\" value=\"([a-z0-9]+)\"");

			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
			String line;
			while ((line = reader.readLine()) != null) {
				Matcher matcher = sectokPatter.matcher(line);
				if (matcher.find())
					return matcher.group(1);
			}
			return null;
		}

	}
}
