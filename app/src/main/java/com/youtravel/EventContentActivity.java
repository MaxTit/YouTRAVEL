package com.youtravel;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class EventContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_event_content);
        init_interface();
        final Event event = (Event)getIntent().getSerializableExtra("event");
        output(event.html);
        final tools.Contact contact = new tools.Contact(this,"event",event.id+"");
        (findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("lololo","lololol");
                AlertDialog dialog = contact.callType(event.name);
                dialog.show();
            }
        });
        /// Analytics
        Bundle params = new Bundle();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("my_data", 0);
        if (settings.getString("id_user", null) != null)
            params.putInt("user_id", Integer.parseInt(settings.getString("id_user", "-1")));
        params.putInt("source_id",event.id);
        HomeActivity.mFirebaseAnalytics.logEvent("Event",params);
        ////
    }

    private void output(String content){
        WebView webview = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.requestFocusFromTouch();
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
//                if(progress < 100 && pd.getVisibility() == ProgressBar.GONE){
//                    pd.setVisibility(ProgressBar.VISIBLE);
//                }
//                pd.setProgress(progress);
//                if(progress == 100) {
//                    pd.setVisibility(ProgressBar.GONE);
//                }
            }
        });
        webview.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });
        Log.d("html",content);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.loadData(content, "text/html; charset=utf-8", "utf-8"); //loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tour_content, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_filter);

        return true;
    }



    private void init_interface(){
        final MainMenu menu = new MainMenu(this, "Мероприятия");

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(EventContentActivity.this, EventFiltersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });
    }

}
