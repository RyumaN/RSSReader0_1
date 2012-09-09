package com.example.rssreader0;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.xmlpull.v1.XmlPullParser;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class RSSReader0Activity extends Activity {

	static String strUrl,title;

	Future<?> waiting = null;
	ExecutorService executorService;
	static String content;

Runnable inMainThread = new Runnable(){


	public void run(){
		View btn = findViewById(R.id.button1);
		TextView tv = (TextView)findViewById(R.id.textView1);
		if(content == "")content = getResources().getString(R.string.message_error);
		tv.setText(Html.fromHtml(content));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setLinksClickable(true);
		btn.setEnabled(true);
		setTitle(title);
	}
};

Runnable inReadingThread = new Runnable(){

	public void run(){
		content = readRss(false);
		runOnUiThread(inMainThread);
	}
};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssreader0);

        SharedPreferences prefs = getSharedPreferences("RssReaderPrefs", MODE_PRIVATE);
        strUrl = prefs.getString("server", getResources().getTextArray(R.array.ServiceUrl)[0].toString());

        findViewById(R.id.button1).setOnClickListener(new OnClickListener(){

        	public void onClick(View v){
        		showRss();
        	}
        });
        showRss();
    }
    public void showRss(){
    	title = getResources().getString(R.string.app_name);
    	View btn = findViewById(R.id.button1);
    	btn.setEnabled(false);

    	executorService = Executors.newSingleThreadScheduledExecutor();
    	if(waiting != null)waiting.cancel(true);
    	waiting = executorService.submit(inReadingThread);
    }

    public static String readRss(boolean simple){

    	String str = "";
    	HttpURLConnection connection = null;

    	try{
    		URL url = new URL(strUrl);
    		connection = (HttpURLConnection) url.openConnection();
    		connection.setDoInput(true);
    		connection.connect();

    		XmlPullParser xmlPP = Xml.newPullParser();
    		xmlPP.setInput(new InputStreamReader(connection.getInputStream(), "UTF-8"));

    		int eventType = xmlPP.getEventType();

    		while(eventType != XmlPullParser.END_DOCUMENT){
    			if(eventType == XmlPullParser.START_TAG){
    				if(xmlPP.getName().equalsIgnoreCase("channel")){
    					do{
    						eventType = xmlPP.next();

    						if(xmlPP.getName() != null &&
    								xmlPP.getName().equalsIgnoreCase("title")){
    							title = xmlPP.nextText();
    							break;
    						}
    					}while(xmlPP.getName() != "item");
    				}

    				if(xmlPP.getName() != null && xmlPP.getName().equalsIgnoreCase("item")){
    					String itemtitle = "title";
    					String linkurl = "";
    					String putdate = "";

    					do{
    						eventType = xmlPP.next();
    						if(eventType == XmlPullParser.START_TAG){
    							String tagName = xmlPP.getName();
    							if(tagName.equalsIgnoreCase("title"))
    								itemtitle = xmlPP.nextText();
    							else
    								if(tagName.equalsIgnoreCase("link"))
    									linkurl = xmlPP.nextText();
    								else
    									if(tagName.equalsIgnoreCase("putdate"))
        									putdate = xmlPP.nextText();
    						}

    					}while(!((eventType == XmlPullParser.END_TAG) &&
    							(xmlPP.getName().equalsIgnoreCase("item"))));

    					if(simple){
    						str = str + Html.fromHtml(itemtitle).toString() + "\n";
    					}else{
    						str = str + "<a href = \"" + linkurl +"\">"
    								+ itemtitle + "</a><br>" + putdate
    								+ "<br>";
    					}
    				}
    			}
    			eventType = xmlPP.next();
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}

    	finally{
    		if(connection != null){
    			connection.disconnect();
    		}
    	}

    	return str;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String[] items = getResources().getStringArray(R.array.ServiceName);
        for(int i = 0; i < items.length; i++)
        	menu.add(0, Menu.FIRST + i, 0, items[i]);

        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		String[] items = getResources().getStringArray(R.array.ServiceUrl);
		strUrl = items[item.getItemId() - Menu.FIRST];
		SharedPreferences prefs = getSharedPreferences("RssReaderPrefs", MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("server", strUrl);
		editor.commit();
		showRss();

		return super.onOptionsItemSelected(item);
	}


}
