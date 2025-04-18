package com.example.majorproject.BackgroundServices;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.majorproject.News;
import com.example.majorproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class NewsApiService extends Service {

    private static final int REQ_CODE = 1000;

    Handler handler = new Handler();
    Runnable apiRunnable =  new Runnable() {
        @Override
        public void run() {

            setNewsAppNotification();

            handler.postDelayed(this,60000);

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
      /*  // Remove any pending callbacks to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacks(apiRunnable);
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the API call loop
        handler.post(apiRunnable);

        // Return START_STICKY to restart the service if it gets terminated
        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setNewsAppNotification(){
        String api ="https://min-api.cryptocompare.com/data/v2/news/?lang=EN";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object=new JSONObject(response);
                            JSONArray array = object.getJSONArray("Data");


                                    JSONObject simpleobject = array.getJSONObject(0);
                                    String NEWS_ID = simpleobject.getString("id");
                                    SharedPreferences preferences = getSharedPreferences("MajorProject", MODE_PRIVATE);
                                    String newsId = preferences.getString("newsId", null);

                                    if(newsId!=null){

                                        Log.e("NewsNotifyTesting", "News Id : " + newsId);

                                        if(!newsId.equals(NEWS_ID)){
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("newsId", NEWS_ID);
                                            editor.apply();

                                            Log.e("NewsNotifyTesting", "Okay Working.");

                                            String imgUrl = simpleobject.getString("imageurl");
                                            String title = simpleobject.getString("title");
                                            String body = simpleobject.getString("body");

                                            setNotification(NewsApiService.this,title,body, imgUrl);
                                        }else{
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putString("newsId", NEWS_ID);
                                            editor.apply();
                                        }
                                    }else{
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.putString("newsId", NEWS_ID);
                                        editor.apply();

                                       /* String imgUrl = simpleobject.getString("imageurl");
                                        String title = simpleobject.getString("title");
                                        String body = simpleobject.getString("body");

                                        setNotification(NewsApiService.this,title,body, imgUrl);*/
                                    }

                                    JSONObject sourceInfo = simpleobject.getJSONObject("source_info");

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("api1", "onResponce:" + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api2","onError:" + error.getLocalizedMessage());
            }
        });
        queue.add(stringRequest);


    }




    public static void setNotification(Context context, String title, String body, String imgUrl) {
        // Download image asynchronously
        new DownloadImageTask(context, title, body).execute(imgUrl);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private Context context;
        private String title;
        private String body;
        private PendingIntent pendingIntent;

        public DownloadImageTask(Context context, String title, String body) {
            this.context = context;
            this.title = title;
            this.body = body;
        }


        @Override
        protected Bitmap doInBackground(String... strings) {
            String imgUrl = strings[0];
            try {
                InputStream in = new URL(imgUrl).openStream();
                return BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Create intent for launching News activity
            Intent intent = new Intent(context, News.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, REQ_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
            if (bitmap != null) {
                // If image is downloaded successfully, create notification with big picture style
                createNotification(context, title, body, bitmap, pendingIntent);
            } else {
                // If image download fails, create notification without the image
                createNotification(context, title, body, null, pendingIntent);
            }
        }
    }

    private static final String CHANNEL_ID = "NEWS";


    private static void createNotification(Context context, String title, String body, Bitmap imageBitmap, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create notification channel for Android Oreo and higher
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "News Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentIntent(pendingIntent);

        if (imageBitmap != null) {
            // Set big picture style if image was downloaded successfully
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitmap).bigLargeIcon(imageBitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(body));
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
