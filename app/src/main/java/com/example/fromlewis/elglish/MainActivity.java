package com.example.fromlewis.elglish;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Intent;
import android.widget.TextView;
import android.widget.AdapterView;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "imagesearchexample";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "https://secure.flickr.com/services/rest/?method=flickr.photos.search";
    private String API_KEY = "&api_key=ede05baa95280ac90e0c31b127f092f9";
    private String PER_PAGE = "&per_page=50";
    private String SORT = "&sort=interestingness-desc";
    private String FORMAT = "&format=json";
    private String CONTECT_TYPE = "&content_type=1";
    private String SEARCH_TEXT = "&text=";
    private String REQUEST_URL = SEARCH_URL + API_KEY + PER_PAGE + SORT + FORMAT + CONTECT_TYPE + SEARCH_TEXT;

    private ProgressDialog progressDialog = null;
    private SimpleAdapter adapter = null;
    private List<HashMap<String,String>> photoinfoList = null;
    private EditText searchKeyword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonRequestJSON = (Button)findViewById(R.id.button_main_requestjson);
        ListView listviewPhtoList = (ListView)findViewById(R.id.listview_main_list);
        searchKeyword = (EditText)findViewById(R.id.edittext_main_searchkeyword);

        photoinfoList = new ArrayList<HashMap<String,String>>();

        String[] from = new String[]{"id", "title","secret","server","farm"};
        int[] to = new int[] {R.id.textview_main_listviewdata1, R.id.textview_main_listviewdata2,
                R.id.textview_main_listviewdata3, R.id.textview_main_listviewdata4, R.id.textview_main_listviewdata5};
        adapter = new SimpleAdapter(this, photoinfoList, R.layout.listview_items, from, to);
        listviewPhtoList.setAdapter(adapter);


        buttonRequestJSON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                progressDialog = new ProgressDialog( MainActivity.this );
                progressDialog.setMessage("Please wait.....");
                progressDialog.show();

                String keyword = searchKeyword.getText().toString();
                getJSON(keyword);
            }
        });

        listviewPhtoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                TextView textviewId = (TextView)view.findViewById(R.id.textview_main_listviewdata1);
                TextView textviewTitle = (TextView)view.findViewById(R.id.textview_main_listviewdata2);
                TextView textviewSecret = (TextView)view.findViewById(R.id.textview_main_listviewdata3);
                TextView textviewServer = (TextView)view.findViewById(R.id.textview_main_listviewdata4);
                TextView textviewFarm = (TextView)view.findViewById(R.id.textview_main_listviewdata5);

                Bundle bundle = new Bundle();
                bundle.putString( "id", textviewId.getText().toString());
                bundle.putString( "title", textviewTitle.getText().toString());
                bundle.putString( "secret", textviewSecret.getText().toString());
                bundle.putString( "server", textviewServer.getText().toString());
                bundle.putString( "farm", textviewFarm.getText().toString());

                Intent intent = new Intent(MainActivity.this, ShowImage.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler
    {
        private final WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainactivity)
        {
            weakReference = new WeakReference<MainActivity>(mainactivity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            MainActivity mainactivity = weakReference.get();

            if (mainactivity != null) {
                switch (msg.what) {
                    case LOAD_SUCCESS:
                        mainactivity.progressDialog.dismiss();
                        mainactivity.adapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public void  getJSON(final String keyword)
    {
        if ( keyword == null) return;

        Thread thread = new Thread(new Runnable() {

            public void run()
            {
                String result;

                try {
                    Log.d(TAG, REQUEST_URL+keyword);
                    URL url = new URL(REQUEST_URL+keyword);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();
                } catch (Exception e) {
                    result = e.toString();
                }

                if (jsonParser(result)){
                   Message message = mHandler.obtainMessage(LOAD_SUCCESS);
                    mHandler.sendMessage(message);
                }
            }
        });

        thread.start();
    }

    public boolean jsonParser(String jsonString)
    {
        if (jsonString == null ) return false;

        jsonString = jsonString.replace("jsonFlickrApi(", "");
        jsonString = jsonString.replace(")", "");

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject photos = jsonObject.getJSONObject("photos");
            JSONArray photo = photos.getJSONArray("photo");

            photoinfoList.clear();

            for (int i = 0; i < photo.length(); i++) {
                JSONObject photoInfo = photo.getJSONObject(i);

                String id = photoInfo.getString("id");
                String secret = photoInfo.getString("secret");
                String server = photoInfo.getString("server");
                String farm = photoInfo.getString("farm");
                String title = photoInfo.getString("title");

                HashMap<String, String> photoinfoMap = new HashMap<String, String>();
                photoinfoMap.put("id", id);
                photoinfoMap.put("secret", secret);
                photoinfoMap.put("server", server);
                photoinfoMap.put("farm", farm);
                photoinfoMap.put("title", title);

                photoinfoList.add(photoinfoMap);
            }

            return true;
        } catch (JSONException e) {
            Log.d(TAG, e.toString() );
        }

        return false;
    }
}
