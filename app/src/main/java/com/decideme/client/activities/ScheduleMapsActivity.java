package com.decideme.client.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.DataParser;
import com.decideme.client.attributes.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ScheduleMapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final float SMALLEST_DISPLACEMENT = 0.25F;
    public static boolean active_schedule_map = false;
    private static LatLng POINT_A, POINT_B;
    View mapView;
    double latitude = 0.0;
    //    private String sc_notification_id = "", sc_worker_chat_id = "";
    double longitude = 0.0;
    Thread backgroundData;
    ArrayList<LatLng> MarkerPoints;
    Handler h = new Handler();
    int delay = 5000; //15 seconds
    Runnable runnable;
    NotificationManager notificationManager;
    private GoogleMap googleMap;
    private ImageView img_back;
    private SupportMapFragment mapFragment;
    private LinearLayout ll_accept_req_dialog;
    private RelativeLayout rl_call, rl_chat;
    private TextView tv_w_name, tv_w_job_type, tv_w_total;
    private ImageView img_w_profile;
    private RatingBar ratingBar_w;
    private LocationRequest mLocationRequest;
    private ImageView img_cancel_req;
    private RelativeLayout rl_my_location;
    private boolean isActive = true;
    private LinearLayout ll_time_consumed;
    private Chronometer cm_timer;

    private String[] worker_id, worker_name, worker_image, worker_rating, worker_status, worker_timer, service_id,
            worker_lat, worker_long, current_flag, notification_id, timer, mobile;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utility.isNetworkAvaliable(ScheduleMapsActivity.this)) {
                try {
                    getCurrentData getTask = new getCurrentData(ScheduleMapsActivity.this);
                    getTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_schedule_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

            initUI();

    }

    private void initUI() {

        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);

        rl_my_location = (RelativeLayout) findViewById(R.id.rl_my_location);
        rl_my_location.setOnClickListener(this);

        ll_accept_req_dialog = (LinearLayout) findViewById(R.id.ll_accept_request);
        rl_call = (RelativeLayout) findViewById(R.id.rl_call);
        rl_call.setOnClickListener(this);
        rl_chat = (RelativeLayout) findViewById(R.id.rl_chat);
        rl_chat.setOnClickListener(this);
        tv_w_name = (TextView) findViewById(R.id.tv_ar_name);
        tv_w_job_type = (TextView) findViewById(R.id.tv_ar_job_type);
        tv_w_total = (TextView) findViewById(R.id.tv_rating_ar_value);
        img_w_profile = (ImageView) findViewById(R.id.img_ar_profile);
        ratingBar_w = (RatingBar) findViewById(R.id.rating_ar);

        cm_timer = (Chronometer) findViewById(R.id.cm_timer);
        cm_timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
            }
        });
        cm_timer.setBase(SystemClock.elapsedRealtime());
        cm_timer.setText("00:00:00");
        ll_time_consumed = (LinearLayout) findViewById(R.id.ll_time_consumed);

        img_cancel_req = (ImageView) findViewById(R.id.img_cancel_req);
        img_cancel_req.setOnClickListener(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        tv_w_name.setText(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_name"));
        tv_w_job_type.setText(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_job_type"));
        tv_w_total.setText(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_total_review"));
        if (!Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_profile").equalsIgnoreCase("")) {
            Glide.with(ScheduleMapsActivity.this).load(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_profile"))
                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                    .thumbnail(0.5f)
                    .into(img_w_profile);
        }
        ratingBar_w.setRating(Float.parseFloat(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_rating")));

        POINT_A = new LatLng(Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "latitude")),
                Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "longitude")));
        POINT_B = new LatLng(Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_worker_lat")),
                Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_worker_long")));
        MarkerPoints = new ArrayList();
        MarkerPoints.add(POINT_A);
        MarkerPoints.add(POINT_B);

        mapFragment.getMapAsync(ScheduleMapsActivity.this);

        // From Schedule List
        if (Utility.getAppPrefString(ScheduleMapsActivity.this, "current_task").equalsIgnoreCase("accept")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.VISIBLE);
        } else if (Utility.getAppPrefString(ScheduleMapsActivity.this, "current_task").equalsIgnoreCase("interview")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.VISIBLE);
        } else if (Utility.getAppPrefString(ScheduleMapsActivity.this, "current_task").equalsIgnoreCase("start")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.GONE);
            ll_time_consumed.setVisibility(View.VISIBLE);
            try {
                h.removeCallbacks(runnable);
                cm_timer.setText(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_timer"));
                cm_timer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_timer")));
                cm_timer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Utility.getAppPrefString(ScheduleMapsActivity.this, "current_task").equalsIgnoreCase("pause")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.GONE);
            ll_time_consumed.setVisibility(View.VISIBLE);
            cm_timer.stop();
            cm_timer.setText(Utility.getAppPrefString(ScheduleMapsActivity.this, "sc_timer"));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_my_location:
                getMyLocation();
                break;
            case R.id.rl_chat:
                Utility.writeSharedPreferences(this, "chat_from", "activity");
                Intent intent_chat = new Intent(ScheduleMapsActivity.this, ChatActivity.class);
                intent_chat.putExtra("worker_id", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_worker_id"));
                intent_chat.putExtra("notification_id", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_notification_id"));
                intent_chat.putExtra("user_name", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_name"));
                intent_chat.putExtra("user_image", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_profile"));
                intent_chat.putExtra("user_category", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_job_type"));
                intent_chat.putExtra("user_mobile", Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_mobile"));
                intent_chat.putExtra("chatFrom", "activity");
                startActivity(intent_chat);
                break;
            case R.id.rl_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+63" + Utility.getAppPrefString(ScheduleMapsActivity.this,
                        "sc_mobile")));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.img_cancel_req:
                if (Utility.isNetworkAvaliable(ScheduleMapsActivity.this)) {
                    try {
                        cancelRequest1 getTask = new cancelRequest1(ScheduleMapsActivity.this);
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    // OnMap Ready
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(ScheduleMapsActivity.this, R.raw.map_style);
        this.googleMap.setMapStyle(style);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else {
            if (googleMap != null) {
                if (MarkerPoints.size() >= 2) {
                    POINT_A = (LatLng) MarkerPoints.get(0);
                    POINT_B = (LatLng) MarkerPoints.get(1);
                    String url = getUrl(POINT_A, POINT_B);
                    new FetchUrl().execute(new String[]{url});

                    h.postDelayed(new Runnable() {
                        public void run() {
                            getTracking();
                            runnable = this;
                            h.postDelayed(runnable, delay);
                        }
                    }, delay);
                }
            }
        }

        mapView = mapFragment.getView().findViewById(Integer.parseInt("1"));
        // Get the button view
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        googleMap.setPadding(0, 0, 12, 0);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(strUrl).openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String str = "";
            while (true) {
                str = br.readLine();
                if (str == null) {
                    break;
                }
                sb.append(str);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        return "https://maps.googleapis.com/maps/api/directions/json?" + ("origin=" + origin.latitude + "," + origin.longitude) + "&" + "destination=" + dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyC11_uyqNi5jOPnHL7c1PRJLPCKxrYWiTw";
//        return "https://maps.googleapis.com/maps/api/directions/json?" + ("origin=" + origin.latitude + "," + origin.longitude) + "&" + "destination=" + dest.latitude + "," + dest.longitude + "&sensor=false&units=metric&mode=driving&key=AIzaSyAUWhvhUNKcpe9BOIkmwtVYi9Rt2Ysyk1A";
    }

    @Override
    public void onStart() {
        super.onStart();
        active_schedule_map = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active_schedule_map = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActive = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        active_schedule_map = false;
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        active_schedule_map = true;
        registerReceiver(mMessageReceiver, new IntentFilter("unique_name2"));
    }

    // Worker Tracking
    public void getTracking() {
        if (isActive) {
            backgroundData = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray;
                        JSONObject jsonObjectMessage;
                        String[] worker_id, worker_latitude, worker_longitude;
                        String webUrl = Constant.URL_GET_TRACKING;
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                        nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(ScheduleMapsActivity.this, Constant.USER_ID)));
                        nameValuePairs.add(new BasicNameValuePair("worker_id", Utility.getAppPrefString(ScheduleMapsActivity.this,
                                "sc_worker_id")));
                        nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(ScheduleMapsActivity.this,
                                "sc_service_id")));
                        String response1 = Utility.postRequest(ScheduleMapsActivity.this, webUrl, nameValuePairs);

                        JSONObject jObject = new JSONObject(response1);

                        Log.v("Response:", jObject.toString());

                        String worker_id1 = "";
                        String worker_lat = "";
                        String worker_lng = "";

                        jsonArray = jObject.getJSONArray("worker_data");
                        int lenth = jsonArray.length();

                        worker_id = new String[lenth];
                        worker_latitude = new String[lenth];
                        worker_longitude = new String[lenth];

                        for (int a = 0; a < lenth; a++) {
                            jsonObjectMessage = jsonArray.getJSONObject(a);

                            try {
                                worker_id[a] = jsonObjectMessage.getString("worker_id");
                                worker_latitude[a] = jsonObjectMessage.getString("worker_latitude");
                                worker_longitude[a] = jsonObjectMessage.getString("worker_longitude");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        worker_id1 = worker_id[0];
                        worker_lat = worker_latitude[0];
                        worker_lng = worker_longitude[0];

                        if (!worker_id1.equalsIgnoreCase("")) {
                            final String finalWorker_lng = worker_lng;
                            final String finalWorker_lat = worker_lat;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        POINT_B = new LatLng(Double.parseDouble(finalWorker_lat),
                                                Double.parseDouble(finalWorker_lng));
                                        String url = getUrl(POINT_A, POINT_B);
                                        new FetchUrl().execute(new String[]{url});
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            backgroundData.start();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private long convert(String time) throws ParseException {
        String myDate = time;
        long milliseconds;

        String[] arrayString = myDate.split(":");
        int hours = Integer.parseInt(arrayString[0]);
        int minutes = Integer.parseInt(arrayString[1]);

        char toCheck = ':';
        int count = 0;

        for (char ch : myDate.toCharArray()) {
            if (ch == toCheck) {
                count++;
            }
        }

        if (count > 1) {
            int second = Integer.parseInt(arrayString[2]);
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes) + TimeUnit.SECONDS.toSeconds(second));
        } else {
            milliseconds = TimeUnit.SECONDS.toMillis(TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes));
        }
        return milliseconds;
    }

    // get Current Location
    private void getMyLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
        googleMap.animateCamera(cameraUpdate);
    }

    // Draw Path
    private class FetchUrl extends AsyncTask<String, Void, String> {
        private FetchUrl() {
        }

        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = ScheduleMapsActivity.this.downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
                return data;
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
                return data;
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());
                return routes;
            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
                return routes;
            }
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                ArrayList<LatLng> points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = (List) result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = (HashMap) path.get(j);
                    points.add(new LatLng(Double.parseDouble((String) point.get("lat")), Double.parseDouble((String) point.get("lng"))));
                }
                lineOptions.addAll(points);
                lineOptions.width(6.0f);
                lineOptions.color(Color.parseColor("#10B4F1"));
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }
            if (lineOptions != null) {
                googleMap.clear();
                googleMap.addPolyline(lineOptions);

                MarkerOptions options = new MarkerOptions();
                options.position(POINT_B);
                options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.blue_pin));
                googleMap.addMarker(options);

                MarkerOptions options1 = new MarkerOptions();
                options1.position(POINT_A);
                options1.icon(BitmapDescriptorFactory.fromResource(R.mipmap.orange_pin));
                googleMap.addMarker(options1);

                // Camera movement
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(POINT_A);
                builder.include(POINT_B);
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.15); // offset from edges of the map 10% of screen
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                googleMap.animateCamera(cu);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    // Cancel Service API
    public class cancelRequest1 extends AsyncTask<String, Integer, Object> {
        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String message;
        String response;

        public cancelRequest1(Context mContext) {
            this.mContext = mContext;
            this.mProgressDialog = new DME_ProgressDilog(mContext);
            this.mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            if (this.mProgressDialog != null && !this.mProgressDialog.isShowing()) {
                this.mProgressDialog.show();
            }
        }

        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Integer.valueOf(0);
        }

        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {

                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();

                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Utility.writeSharedPreferences(mContext, "activity_from", "activity");
                    Utility.writeSharedPreferences(mContext, "noti_flag", "false");
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_CANCEL_INTERVIEWED;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "sc_service_id")));

                JSONObject jObject = new JSONObject(Utility.postRequest(this.mContext, webUrl, nameValuePairs));

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return null;
        }

    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, type;
        JSONArray jsonArray;
        JSONObject jsonObjectMessage;
        int index;

        public getCurrentData(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected Object doInBackground(String... params) {
            try {
                getAboutMeListItem();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
                this.mProgressDialog.dismiss();
            }
            try {
                if (type.equalsIgnoreCase("authorized")) {
                    if (response.equalsIgnoreCase("true")) {
                        for (int i = 0; i < worker_id.length; i++) {
                            if (worker_id[i].equals(Utility.getAppPrefString(getApplicationContext(), "sc_worker_id"))) {
                                index = i;
                                break;
                            }
                        }
                        if (timer[index].equalsIgnoreCase("0")) {
                            timer[index] = "00:00:00";
                        }

                        POINT_A = new LatLng(Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "latitude")),
                                Double.parseDouble(Utility.getAppPrefString(ScheduleMapsActivity.this, "longitude")));
                        POINT_B = new LatLng(Double.parseDouble(worker_lat[index]),
                                Double.parseDouble(worker_long[index]));
                        MarkerPoints = new ArrayList();
                        MarkerPoints.add(POINT_A);
                        MarkerPoints.add(POINT_B);

                        mapFragment.getMapAsync(ScheduleMapsActivity.this);

                        if (current_flag[index].equalsIgnoreCase("pause")) {
                            ll_accept_req_dialog.setVisibility(View.VISIBLE);
                            img_cancel_req.setVisibility(View.GONE);
                            ll_time_consumed.setVisibility(View.VISIBLE);
                            cm_timer.stop();
                            cm_timer.setText(timer[index]);

                        } else if (current_flag[index].equalsIgnoreCase("start")) {
                            ll_accept_req_dialog.setVisibility(View.VISIBLE);
                            img_cancel_req.setVisibility(View.GONE);
                            ll_time_consumed.setVisibility(View.VISIBLE);
                            if (googleMap != null) {
                                googleMap.clear();
                            }
                            try {
                                h.removeCallbacks(runnable);
                                cm_timer.setText(timer[index]);
                                cm_timer.setBase(SystemClock.elapsedRealtime() - convert(timer[index]));
                                cm_timer.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (current_flag[index].equalsIgnoreCase("finish")) {
                            Intent intent = new Intent(ScheduleMapsActivity.this, InvoiceActivity.class);
                            Utility.writeSharedPreferences(mContext, "invoice_s_id", service_id[index]);
                            Utility.writeSharedPreferences(mContext, "sc_flag", "1");
                            Utility.writeSharedPreferences(mContext, "coupon_flag", "0");
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        finish();
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "login", "false");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle("Authentication Error");
                    alertDialog.setMessage("Authentication error occur. Please login again.");
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(mContext,
                                    LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                    alertDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_CURRENT_DATA;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));
                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                type = jObject.getString("type");

                if (response.equalsIgnoreCase("true")) {

                    jsonArray = jObject.getJSONArray("req_data");
                    int lenth = jsonArray.length();

                    worker_id = new String[lenth];
                    worker_name = new String[lenth];
                    worker_image = new String[lenth];
                    worker_rating = new String[lenth];
                    worker_status = new String[lenth];
                    worker_timer = new String[lenth];
                    service_id = new String[lenth];
                    worker_lat = new String[lenth];
                    worker_long = new String[lenth];
                    current_flag = new String[lenth];
                    notification_id = new String[lenth];
                    timer = new String[lenth];
                    mobile = new String[lenth];

                    for (int a = 0; a < lenth; a++) {
                        jsonObjectMessage = jsonArray.getJSONObject(a);

                        try {
                            worker_id[a] = jsonObjectMessage.getString("worker_id");
                            worker_name[a] = jsonObjectMessage.getString("worker_name");
                            worker_image[a] = jsonObjectMessage.getString("worker_image");
                            worker_rating[a] = jsonObjectMessage.getString("worker_rating");
                            worker_status[a] = jsonObjectMessage.getString("worker_status");
                            worker_timer[a] = jsonObjectMessage.getString("worker_timer");
                            service_id[a] = jsonObjectMessage.getString("service_id");
                            worker_lat[a] = jsonObjectMessage.getString("worker_lat");
                            worker_long[a] = jsonObjectMessage.getString("worker_long");
                            current_flag[a] = jsonObjectMessage.getString("current_flag");
                            notification_id[a] = jsonObjectMessage.getString("notification_id");
                            timer[a] = jsonObjectMessage.getString("timer");
                            mobile[a] = jsonObjectMessage.getString("mobile");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
