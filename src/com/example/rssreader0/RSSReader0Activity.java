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

	@Override
	public void run(){
		View btn = findViewbyId(R.id.button1);
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
	@Override
	public void run(){
		content = readRss(false);
		runOnUiThread(inMainThread);
	}
};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssreader0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_rssreader0, menu);
        return true;
    }
}
