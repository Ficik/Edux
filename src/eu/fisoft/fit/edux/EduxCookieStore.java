package eu.fisoft.fit.edux;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;

public class EduxCookieStore extends BasicCookieStore {

	private final String FILENAME = "cookies-jar";
	private static EduxCookieStore instance;
	
	public static EduxCookieStore getInstance(Context context) {
		if (instance == null){
			instance = new EduxCookieStore();
			try {
				instance.read(context);
			} catch (IOException e) {
				Log.e("EDUX", "Someone ate my cookie!");
				e.printStackTrace();
			}
		}
		return instance;
		
	}
	
	private EduxCookieStore() {

	}
	
	public void write(Context context) throws IOException {
		FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
		for (Cookie cookie : getCookies()){
			fos.write((cookie.getName()+":"+cookie.getValue()+":"+cookie.isSecure()+":"+cookie.isPersistent()+"\n").getBytes());
		}
		fos.close();
	}
	
	public void read(Context context) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput(FILENAME)));
		String line;
		while ((line = reader.readLine()) != null){
			String[] data = line.split(":");
			BasicClientCookie cookie = new BasicClientCookie(data[0], data[1]);
			cookie.setSecure(data[2].equals("true")?true:false);
			cookie.setDomain("edux.fit.cvut.cz");
			cookie.setPath("/");
			cookie.setVersion(0);
			addCookie(cookie);
		}
	}
	
	public void updateCookieManager(CookieManager manager){
		for (Cookie cookie : getCookies())
			manager.setCookie("edux.fit.cvut.cz", cookie.getName()+"="+cookie.getValue()+";");
	}
}
