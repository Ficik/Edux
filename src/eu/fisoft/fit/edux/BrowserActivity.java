package eu.fisoft.fit.edux;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import eu.fisoft.fit.edux.storage.AttendedSQLHelper;

public class BrowserActivity extends Activity {

	private WebView browser;
	private Context context;
	private DownloadManager dm;
	
	final private String JS_REGISTER_MENUS = "var menus = document.getElementById('sidebar').getElementsByClassName('indexmenu_idx_head');for(i=0;i<menus.length;i++){ app.registerMenu(menus[i].innerHTML, menus[i].href);}";
    final private String JS_HIDE_SIDEBAR_TOPBAR = "document.getElementsByClassName('stylehead')[0].style.display='none';document.getElementById('sidebar').style.display='none';document.getElementById('bar__bottom').style.display='none';document.getElementsByClassName('footerinc')[0].style.display='none';";
    //final private String JS_TOGGLE_SIDEBAR = "var s = document.getElementById('sidebar').style.display; document.getElementById('sidebar').style.display = (s=='none'?'block':'none');";
    final private String JS_GET_NAME = "var t=document.getElementById('dokuwiki__top').innerHTML;var d=t.search(' ');app.saveName(t.substr(0,d),t.substr(d+1));";
    
    private List<MenuLinks> pageMenu = new ArrayList<MenuLinks>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_browser);
        setTitle(getIntent().getStringExtra("NAME"));
        browser = (WebView)findViewById(R.id.browser);
        CookieManager.getInstance().setAcceptCookie(true);
        EduxCookieStore.getInstance(this).updateCookieManager(CookieManager.getInstance());

        browser.setWebViewClient(new EduxWebClient());
        browser.setWebChromeClient(new EduxChromeClient());
        browser.addJavascriptInterface(new EduxJSInterface(), "app");
        browser.getSettings().setJavaScriptEnabled(true);
        browser.getSettings().setAllowFileAccess(true);

        dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        
        browser.setDownloadListener(new DownloadListener() {
			
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype, long contentLength) {
				String[] urlSplit = url.split("/");
				String name = urlSplit[urlSplit.length-1];
				
		        Request request = new Request(
		                Uri.parse(url));
		        request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie("edux.fit.cvut.cz"))
		        	.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name)
		        	.setTitle(name);
		        dm.enqueue(request);
				
			}
		});
        
        registerReceiver(onComplete,
                         new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick,
                         new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        
        String url = getIntent().getStringExtra("URL");
        
        if (url.endsWith(".1") || url.endsWith(".2")) // Ugly edux quick fix
        	url = url.substring(0, url.length()-2);
        
        browser.loadUrl(url);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      
      unregisterReceiver(onComplete);
      unregisterReceiver(onNotificationClick);
    }
    
    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
        	long did = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        	Query query = new Query();
            query.setFilterById(did);
            Cursor c = dm.query(query);
            Toast.makeText(ctxt, "Download complete", Toast.LENGTH_LONG).show();
            if (c.moveToFirst()) {
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    File file = new File(uriString);
                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    Intent myIntent = new Intent();
                    myIntent.setDataAndType(Uri.fromFile(file),mimetype);
                    startActivity(myIntent);
                }
            }
       	
        	
        }
      };
      
      BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
          Toast.makeText(ctxt, "Downloading..", Toast.LENGTH_LONG).show();
        }
      };
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_browser, menu);
        MenuItem showMenu = menu.findItem(R.id.menu_showmenu);
        if (showMenu != null)
        	showMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				public boolean onMenuItemClick(MenuItem item) {
					AlertDialog.Builder builder = new Builder(context);
					builder.setTitle("Menu");
					String[] items = new String[pageMenu.size()];
					for(int i=0;i<pageMenu.size();i++)
						items[i] = pageMenu.get(i).name;
					builder.setItems(items, new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							browser.loadUrl(pageMenu.get(which).url);
						}
					});
					builder.show();
					return false;
				}
			});
        return true;
    }
    
    private class EduxChromeClient extends WebChromeClient {
    	
    	@Override
    	public boolean onJsAlert(WebView view, String url, String message,
    			JsResult result) {
    		Log.v("EDUX", "Alert: "+message);
    		return super.onJsAlert(view, url, message, result);
    	}
    }
    
    private class EduxWebClient extends WebViewClient {
    	

   	
    	@Override
    	public void onPageFinished(WebView view, String url) {
    		browser.loadUrl("javascript:"+JS_HIDE_SIDEBAR_TOPBAR);
    		browser.loadUrl("javascript:"+JS_GET_NAME);
    		browser.loadUrl("javascript:"+JS_REGISTER_MENUS);
    		pageMenu.clear();
    		super.onPageFinished(view, url);
    	}
    	
    	@Override
    	public boolean shouldOverrideUrlLoading(WebView view, String url) {
    		return false;
    	}
    	
    }
    

    private class EduxJSInterface{
    	
    	@SuppressWarnings("unused")
		public void saveName(String code, String name){
    		AttendedSQLHelper db = new AttendedSQLHelper(context);
    		db.updateName(code,name);
    	}
    	
    	@SuppressWarnings("unused")
    	public void registerMenu(String name, String url){
    		pageMenu.add(new MenuLinks(name, url));
    	}
    	
    }

    
    private class MenuLinks {
    	public final String url;
    	public final String name;
    	
    	public MenuLinks(String name, String url) {
    		this.url = url;
    		this.name = name;
		}
    	
    	@Override
    	public String toString() {
    		return name;
    	}
    }


    
}
