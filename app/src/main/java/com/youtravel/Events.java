package com.youtravel;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tools.ContactMailChat;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.youtravel.StartActivityAlternative.*;

public class Events extends AppCompatActivity implements ScrollViewListener{

    static DBHelper dbHelper;
    public static ArrayList<String> country_filter;
    public static ArrayList<String> kind_filter;
    public static ArrayList<String> city_filter;
    public static ArrayList<Integer> price_filter;
    public static ArrayList<Date> date_filter;
    private static Date d1;
    private static Bundle freshTours;
    private static Bundle freshEvents;
    private static Bundle freshServices;
    private static Bundle freshExcursions;
    long diff0 = 0;
    static Boolean fl = true;
    static int limit = 0;
    static String search_qu = null;

    @Override
    public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        //  LinearLayout content =(LinearLayout)findViewById(R.id.linearLayout2);
        View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
        long diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        // if diff is zero, then the bottom has been reached
        if (diff == 0 && diff0!=0 ) {
            // do stuff
            if(fl)
            {
                fl = false;
                refreshInterface(3);

            }
        }
        else fl = true;
        diff0 =diff;
        Log.d("nololololo", "" + diff + "" + diff0);
    }

    public static Bundle getFreshTours() {
        return freshTours;
    }

    public static void setFreshTours(Bundle freshTours) {
        Events.freshTours = freshTours;
    }

    public static Bundle getFreshServices() {
        return freshServices;
    }

    public static void setFreshServices(Bundle freshServices) {
        Events.freshServices = freshServices;
    }

    public static Bundle getFreshEvents() {
        return freshEvents;
    }

    public static void setFreshEvents(Bundle freshEvents) {
        Events.freshEvents = freshEvents;
    }

    public static Bundle getFreshExcursions() {
        return freshExcursions;
    }

    public static void setFreshExcursions(Bundle freshExcursions) {
        Events.freshExcursions = freshExcursions;
    }
    private static boolean filled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        limit = 0;
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/SegoeBold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_events);
        ScrollViewExt scroll = (ScrollViewExt) findViewById(R.id.scroll_view);
        scroll.setScrollViewListener(this);
        init_interface();
        filled = true;
        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        View loading = getLayoutInflater().inflate(R.layout.loading, underframe, false);
        underframe.addView(loading);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus && filled){
            refreshInterface(0);
            //LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
            //underframe.removeViewAt(0);
            filled = false;
        }
    }


    private boolean outOfRange(ArrayList<Date> dateList, Date dateMin, Date dateMax) {
        boolean KNF = true;
        for (Date d : dateList) {
            KNF = KNF && (d.before(dateMin) || d.after(dateMax));
            if (!KNF) {
                d1 = d;
                break;
            }
        }
        return KNF;
    }



    public void notifyFresh() {
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                if (getFreshEvents() != null) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            ((getFreshEvents().getIntegerArrayList("new").size() > 0) ? "Новых мероприятий: " + getFreshEvents().getIntegerArrayList("new").size() : "") +
                                    ((getFreshEvents().getIntegerArrayList("updated").size() > 0) ? "Обновлено мероприятий: " + getFreshEvents().getIntegerArrayList("new").size() : ""),
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }, 0);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                refreshInterface(1);
                Bundle data = msg.getData();
                Log.i("HANDLED", "HANDLED");
                switch (data.getString("context", "")) {
                    case "туров":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshTours(data);
                        break;
                    case "услуг":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshServices(data);
                        break;
                    case "мероприятий":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshEvents(data);
                        break;
                    case "экскурсий":
                        if (data.getIntegerArrayList("new").size() + data.getIntegerArrayList("updated").size() > 0)
                            setFreshExcursions(data);
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_search, menu);
        menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_filter);

        // search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if(null!=searchManager ) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                if(query.length()>0)
                {
                    limit=0;
                    search_qu = query;
                    refreshInterface(1);
                }
                else
                {
                    if(search_qu!= null)
                    {
                        limit = 0;
                        search_qu = null;
                        refreshInterface(1);
                    }
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem,new MenuItemCompat.OnActionExpandListener()
        {

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {
                limit = 0;
                search_qu = null;
                Log.d("close search ", "999");
                refreshInterface(1);
                return true; // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                // TODO Auto-generated method stub
                return true;
            }
        });
        return true;
    }
    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }
    private void refreshInterface(int mode) {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        final boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        dbHelper = new DBHelper(this);

        LinearLayout underframe = (LinearLayout) findViewById(R.id.content_rel);
        Cursor c;
        SQLiteDatabase db;
        boolean isTablet = isTablet(this);

        if (mode != 3)
            try {
                underframe.removeAllViewsInLayout();
                limit = 0;
            } catch(NullPointerException ne) {}
/////////////////////////////////////////////////
        db = dbHelper.getWritableDatabase();
        String b = make_filtered_query(null, kind_filter, null, search_qu);
        c = db.rawQuery(b + " LIMIT 20 OFFSET " + limit, new String[]{});
        Log.i("ScrRef", "" + limit);
        limit+=10;

        Log.d("", "_____" + c.getCount() + "\n " + b);
        if (c.moveToFirst() && c.getCount()>0 ) {
            c.moveToFirst();
            LinearLayout pair = null;
            while (!c.isAfterLast()) {
                View v;
                final Event event = new Event(c, this, getApplicationContext().getSharedPreferences("my_data", 0));
                boolean is_up_to_date = false;
                boolean matches_city = false;
                boolean matches_country = false;
                boolean matches = false;
                int index = 0;

                for (int i = 0; i< event.location_and_time.size(); i = i + 2) {
                    if (date_filter != null && !date_filter.isEmpty() && StartActivity.make_date(event.location_and_time.get(i)) != "null" &&
                            !(event.location_and_time.get(i).before(date_filter.get(0)) && event.location_and_time.get(i+1).after(date_filter.get(1)))) {
                        is_up_to_date = true;
                    }
                    else is_up_to_date = !(date_filter != null && !date_filter.isEmpty() && StartActivity.make_date(event.location_and_time.get(i)) != "null");

                    if (city_filter != null && !city_filter.isEmpty()) {

                        if (Integer.parseInt(city_filter.get(0)) == event.id_city.get(index)) {
                            matches_city = true;
                        }
                        else matches_city = false;
                    } else
                        matches_city = true;

                    if (country_filter!=null && !country_filter.isEmpty()) {
                        if (Integer.parseInt(country_filter.get(0)) == event.id_country.get(index)) {
                            matches_country = true;
                        }
                        else
                            matches_country = false;
                    }
                    else
                        matches_country = true;

                    index++;
                    if (is_up_to_date && matches_city && matches_country) {
                        d1 = event.location_and_time.get(i);
                        matches = true;
                        break;
                    }
                }

                if ((!matches) && date_filter!=null && !date_filter.isEmpty()) {
                    c.moveToNext();
                    continue;
                }
                else if (price_filter!=null && !price_filter.isEmpty() &&
                        (event.price < price_filter.get(0) || event.price > price_filter.get(1))){
                    c.moveToNext();
                    continue;
                }
                if(isTablet)
                {
                    if((c.getPosition()+1)%2 !=0) {
                        pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                        underframe.addView(pair);
                        v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                        pair.addView(v);
                    }
                    else
                    {
                        v = getLayoutInflater().inflate(R.layout.catalogue_content2, pair, false);
                        pair.addView(v);
                    }
                }
                else {
                    pair = (LinearLayout) getLayoutInflater().inflate(R.layout.pair, underframe, false);
                    underframe.addView(pair);
                    v = getLayoutInflater().inflate(R.layout.catalogue_content, pair, false);
                    pair.addView(v);
                }

                v.findViewById(R.id.info_button).setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(Events.this, EventContentActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra("event", event);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                });

                v.findViewById(R.id.call_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+380504694030"));
                        startActivity(Intent.createChooser(intent, "Позвонить"));
                    }
                });

                final ContactMailChat contact_mail = new ContactMailChat(this, "tour", event.id + "");
                v.findViewById(R.id.mail_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog dialog = contact_mail.callType(event.name);
                        dialog.show();
                    }
                });

                // -------------------------------------------------------------------------------------------
                final String[] urls = event.img.split(",");
                if (!urls[0].isEmpty()) {
                    final ImageView imageView = (ImageView) v.findViewById(R.id.image);
                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                        imageView.setImageBitmap(bitmap_img);
                        Log.i("ImDow", "Subject = " + event.id + urls[0]);
                    } else {
                        final String subject = "event", id_subject = event.id+"";
                        if (isConnected)
                            imageView.post(new Runnable() {
                                public void run() {
                                    if ((new File(getCacheDir() + "/Images").exists())) {
                                        Log.i("ImDow", "true");
                                    }
                                    (new ImageDownloader(Events.this, subject, id_subject)).run();
                                    if ((new File(getCacheDir() + "/Images/" + urls[0])).exists()) {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        Bitmap bitmap_img = BitmapFactory.decodeFile(getCacheDir() + "/Images/" + urls[0], options);
                                        imageView.setImageBitmap(bitmap_img);
                                    }
                                }
                            });
                    }
                }
                // -------------------------------------------------------------------------------------------
                final TextView header = (TextView) v.findViewById(R.id.content);
                final TextView content = (TextView) v.findViewById(R.id.articula);
                final TextView contentDay = (TextView) v.findViewById(R.id.articula1);
                String content_header = event.name;
                header.setText(content_header);
                String city = "", country = "";

                if (date_filter == null || date_filter.isEmpty()) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(2900,11,11);
                    outOfRange(event.location_and_time, Calendar.getInstance().getTime(), cal.getTime());
                }
                content_header = "Вид мероприятия: "     + event.event_type
                        + "\n"   + "Дата: "    + StartActivity.make_date(d1)
                        + "\n" + "Город (Страна): "    +city + ", "  + country + (!country.isEmpty() ? (": " + country) : "");
                contentDay.setText(content_header);
                content.setText("от " +  String.valueOf(Math.round((double) Math.round(event.price * 100) / 100)) + " " + event.currency);
                c.moveToNext();
                d1 = null;
            }
        }
        if (underframe.getChildCount() == 0){
            View mes = getLayoutInflater().inflate(R.layout.dump_text, underframe, false);
            underframe.addView(mes);
            ImageView show_all = (ImageView) mes.findViewById(R.id.button);
            show_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventFiltersActivity.country_filter = null;
                    EventFiltersActivity.kind_filter = null;
                    EventFiltersActivity.city_filter = null;
                    EventFiltersActivity.price_filter = null;
                    EventFiltersActivity.date_filter = null;
                    Events.country_filter = null;
                    Events.kind_filter = null;
                    Events.city_filter = null;
                    Events.price_filter = null;
                    Events.date_filter = null;
                    EventFiltersActivity.country_last_pos = 0;
                    EventFiltersActivity.city_last_pos = 0;
                    EventFiltersActivity.first_time = true;
                    refreshInterface(1);
                }
            });
        }
        else if (mode == 0) {
            final int count = underframe.getChildCount();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Мероприятий найдено: " + count,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, 0);
        } else if (mode == 1){
            notifyFresh();
        }

        c.close();
        db.close();
    }

    private void init_interface(){
        final MainMenu menu = new MainMenu(this, "Мероприятия");

        menu.myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_favorite: {
                        Intent intent = new Intent(Events.this, EventFiltersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        return true;
                    }
                }
                return false;
            }
        });

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                AsyncTask<String, String, String> refresh = new AsyncTask<String, String, String>() {
                    @Override
                    protected String doInBackground(String... params) {

                        try {
                            news fresh;
                            Message msg = handler.obtainMessage();
                            Bundle context = new Bundle();
                            msg.what = 1;
                            fresh = update_events();
                            context.putString("context",   "мероприятий");
                            context.putIntegerArrayList("new", fresh.drained_new);
                            context.putIntegerArrayList("updated", fresh.drained_updated);
                            msg.setData(context);
                            handler.sendMessage(msg);
                            update_tours();
                            update_countries();
                            update_cities();
                            update_objects();
                            update_journey_kinds();
                            update_services();
                            update_excursions();
                            update_comments();
                            update_article();
                            if (settings.getString("email_user", null) != null) {
                                update_chat(settings.getString("id_user", null));
                                update_chat_mes(settings.getString("id_user", null));
                            }
                            update_currency();
                            update_currency_data();
                        } catch (Exception e) {
                            Log.e("Error: ", e.getMessage());
                        }

                        return null;
                    }
                };
                try {
                    refresh.execute();
                } catch (Exception ee) {
                }

                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        // stop refresh
                        swipeLayout.setRefreshing(false);
                    }
                }, 3000);
            }

        });


    }



    private String make_filtered_query(ArrayList<String> country_filter, ArrayList<String> kind_filter,
                                       ArrayList<String> city_filter, String search){
        final String name_country = " id_country = ";
        final String name_kind = " event_type = ";
        final String name_city = " id_city = ";
        String search_text = "";
        if(search!=null)
        {
            search_text = " AND (name like '%"+search+"%'" +
                    " or annotation like '%"+search+"%'" +
                    " or extra_info like '%"+search+"%'" +
                    " or location_and_time like '%"+search+"%'" +
                    " or description like '%"+search+"%')";
        }

        return "SELECT * FROM events WHERE ("
                + ((!transcript_filter(country_filter, name_country).isEmpty()) ?  (transcript_filter(country_filter, name_country))
                : "(id_country IS NOT NULL)")
                + ((!transcript_filter(kind_filter, name_kind).isEmpty()) ? (" AND " + transcript_filter(kind_filter, name_kind))
                : " ")
                + ((!transcript_filter(city_filter, name_city).isEmpty()) ? (" AND " + transcript_filter(city_filter, name_city))
                : " ") + ")" + search_text+" ORDER BY name ASC";
    }

    private String transcript_filter(ArrayList<String> filter, String name){
        String query = "";
        if (filter != null) {
            query += "(";
            for (int i = 0; i < filter.size(); i++) {
                query += name + filter.get(i) + ((i != (filter.size() - 1)) ? " OR" : ")");
            }
        }
        return query;
    }



}
