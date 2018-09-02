package com.example.fromlewis.elglish;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by webnautes on 2017-12-22.
 */

public class ShowImage extends AppCompatActivity
{
    private static final String TAG = "imagesearchexample";
    private ImageView imageviewThumbnailPhoto;
    private ImageView imageviewLargePhoto;

    @Override
    protected void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showimage);

        imageviewThumbnailPhoto = (ImageView)findViewById(R.id.imageview_showimage_small);
        imageviewLargePhoto = (ImageView)findViewById(R.id.imageview_showimage_large);
        Button buttonBack = (Button)findViewById(R.id.button_showimage_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String id = bundle.getString("id");
        String title = bundle.getString("title");
        String secret = bundle.getString("secret");
        String server = bundle.getString("server");
        String farm = bundle.getString("farm");

        String thumbnailPhotoURL = "http://farm"+farm+".staticflickr.com/"+server+"/"
                +id+"_"+secret+"_t.jpg";
        String largePhotoURL = "http://farm"+farm+".staticflickr.com/"+server+"/"
                +id+"_"+secret+"_b.jpg";

        showimageTask task = new showimageTask();
        task.execute(thumbnailPhotoURL, largePhotoURL);
    }

    private class showimageTask extends AsyncTask<String, Void, Bitmap[]>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = new ProgressDialog( ShowImage.this );
            progressDialog.setMessage("Please wait.....");
            progressDialog.show();
        }

        @Override
        protected Bitmap[] doInBackground(String... url)
        {
            String thumbnailPhotoURL = url[0].toString();
            String largePhotoURL = url[1].toString();

            Bitmap[] bitmap = new Bitmap[2];
            bitmap[0] = getImagefromURL(thumbnailPhotoURL);
            bitmap[1] = getImagefromURL(largePhotoURL);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmap)
        {
            super.onPostExecute(bitmap);

            imageviewThumbnailPhoto.setImageBitmap(bitmap[0]);
            imageviewLargePhoto.setImageBitmap(bitmap[1]);

            progressDialog.dismiss();
        }
    }

    public Bitmap getImagefromURL(final String photoURL)
    {
        if ( photoURL == null) return null;

        try {
            URL url = new URL(photoURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setReadTimeout(3000);
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.connect();

            int responseStatusCode = httpURLConnection.getResponseCode();

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            }
            else
                return null;

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

            bufferedInputStream.close();
            httpURLConnection.disconnect();

            return  bitmap;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        return null;
    }
}
