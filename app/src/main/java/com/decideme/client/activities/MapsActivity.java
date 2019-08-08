package com.decideme.client.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.adapter.WorkerItemAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.DataParser;
import com.decideme.client.attributes.Utility;
import com.decideme.client.model.LatLngClass;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skyfishjy.library.RippleBackground;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener,
        WorkerItemAdapter.MarkerClick, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int Date_id = 0, Time_id = 1;
    private static final long INTERVAL = 1000 * 10;
    private static final float SMALLEST_DISPLACEMENT = 0.25F;
    public static String reschedule_date, reschedule_time;
    public static String reschedule_date1, reschedule_time1;
    public static boolean active_map = false;
    private static LatLng POINT_A, POINT_B;
    View mapView;
    HashMap<Marker, LatLngClass> hashMapMarker = new HashMap<Marker, LatLngClass>();
    double latitude_pin = 0.0;
    double longitude_pin = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    DatePickerDialog datePickerDialog;
    CountDownTimer count;
    Thread backgroundData;
    ArrayList<LatLng> MarkerPoints;
    Handler h = new Handler();
    int delay = 5000; //15 seconds
    Runnable runnable;
    NotificationManager notificationManager;
    // Time picker dialog
    TimePickerDialog.OnTimeSetListener time_listener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {

            Calendar calNow = Calendar.getInstance();
            calSetTime = (Calendar) calNow.clone();

            calSetTime.set(Calendar.HOUR_OF_DAY, hour);
            calSetTime.set(Calendar.MINUTE, minute);
            calSetTime.set(Calendar.SECOND, 0);
            calSetTime.set(Calendar.MILLISECOND, 0);

            String am_pm = "";

            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.HOUR_OF_DAY, hour);
            datetime.set(Calendar.MINUTE, minute);

            if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                am_pm = "am";
            else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                am_pm = "pm";

            String time1 = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime
                    .get(Calendar.HOUR) + "";
            String time = datetime.get(Calendar.HOUR_OF_DAY) + "";

            int m = datetime.get(Calendar.MINUTE);
            String min = "", hours = "";

            if (m >= 10) {
                min = String.valueOf(m);
            } else {
                min = "0" + String.valueOf(m);
            }

            if (Integer.parseInt(time1) >= 10) {
                hours = time1;
            } else {
                hours = "0" + time1;
            }

            Calendar c = Calendar.getInstance();

            if (calSetTime.getTimeInMillis() < c.getTimeInMillis()) {
                Toast.makeText(MapsActivity.this, "You have not set past time to schedule please select valid time", Toast.LENGTH_SHORT).show();
            } else {
                String display = hours + ":" + min + " " + am_pm;
                et_time.setText(display);
            }
        }
    };
    private GoogleMap googleMap;
    private ImageView img_back;
    private LinearLayout ll_profile, ll_dateTime, ll_worker_dialog, ll_search, ll_timer, ll_profile_btn,
            ll_accept_req_dialog, ll_time_consumed;
    private View view_date;
    private RelativeLayout rl_date, rl_worker_data, rl_next, rl_pre, rl_animation, rl_call, rl_chat, rl_my_location;
    private Button btn_schedule, btn_request, btn_next, btn_cancel;
    private EditText et_date, et_time;
    private ImageView confirm_address_map_custom_marker, img_w_profile, img_cancel_req;
    private TextView tv_time, tv_w_name, tv_w_job_type, tv_w_total, tv_date_time, tv_job_type, tv_rate, tv_payment_type,
            tv_payment_method;
    private RatingBar ratingBar_w;
    private RecyclerView rv_ivr;
    private Marker locationMarker;
    private Calendar calSetDate, calSetTime;
    // Date picker dialog
    DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar calNow = Calendar.getInstance();
            calSetDate = (Calendar) calNow.clone();

            calSetDate.set(Calendar.DAY_OF_MONTH, day);
            calSetDate.set(Calendar.MONTH, month);
            calSetDate.set(Calendar.YEAR, year);

            month = month + 1;
            String month_name = null, day_name = null;
            if (month < 10) {
                month_name = "0" + String.valueOf(month);
            } else {
                month_name = "" + String.valueOf(month);
            }
            if (day < 10) {
                day_name = "0" + String.valueOf(day);
            } else {
                day_name = "" + String.valueOf(day);
            }
            reschedule_date = month_name + "/" + day_name
                    + "/" + String.valueOf(year);
            reschedule_date1 = day_name + "/" + month_name
                    + "/" + String.valueOf(year);

            et_date.setText(reschedule_date);
        }
    };
    private SupportMapFragment mapFragment;
    private PlaceAutocompleteFragment autocompleteFragment;
    private boolean flag = false;
    private Chronometer cm_timer;
    private LinearLayoutManager layoutManager;
    private String[] worker_id, worker_name, worker_image, worker_price, worker_lat, worker_long;
    private WorkerItemAdapter workerItemAdapter;
    private ArrayList<LatLngClass> arraylist1 = new ArrayList<LatLngClass>();
    private String joined_worker_id = "", btn_type = "", payment_method = "", notification_id = "",
            service_id = "", worker_latitude = "", worker_longitude = "", worker_chat_id = "",
            user_latitude = "", user_longitude = "", w_name, w_image, worker_rating, worker_total_review,
            worker_job_type, worker_mobile;
    private LocationRequest mLocationRequest;
    private boolean worker_flag = false;
    private int p_flag = 0;
    private Polyline polylinePath;
    private DatePickerDialog fromDatePickerDialog;
    private TimePickerDialog fromTimePickerDialog;
    private SimpleDateFormat dateFormatter;
    private Calendar selectedDate;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("finish")) {
                Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
                Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
                Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
                Utility.writeSharedPreferences(MapsActivity.this, "timer", "");
                Toast.makeText(getApplicationContext(), "Job Finished", Toast.LENGTH_SHORT).show();
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                finish();
                Intent intent_finish = new Intent(MapsActivity.this, InvoiceActivity.class);
                Utility.writeSharedPreferences(MapsActivity.this, "invoice_s_id", service_id);
                Utility.writeSharedPreferences(MapsActivity.this, "sc_flag", "0");
                Utility.writeSharedPreferences(MapsActivity.this, "coupon_flag", "0");
                Utility.writeSharedPreferences(MapsActivity.this, "back_flag", "1");
                startActivity(intent_finish);
            } else if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("cancel")) {
                Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
                Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
                Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
                Toast.makeText(getApplicationContext(), "Job Cancel", Toast.LENGTH_SHORT).show();
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                Intent intent_cancel = new Intent(MapsActivity.this, HomeActivity.class);
                startActivity(intent_cancel);
                finish();
            } else if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("savetime")) {
                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                img_cancel_req.setVisibility(View.GONE);
                ll_profile.setVisibility(View.GONE);
                view_date.setVisibility(View.GONE);
                rl_date.setVisibility(View.GONE);
                rl_worker_data.setVisibility(View.GONE);
                rl_animation.setVisibility(View.GONE);
                confirm_address_map_custom_marker.setVisibility(View.GONE);
                ll_search.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                ll_timer.setVisibility(View.GONE);
                ll_profile_btn.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.VISIBLE);
                try {
                    if (Utility.getAppPrefString(MapsActivity.this, "noti_save_time_type").equalsIgnoreCase("pause")) {
                        cm_timer.stop();
                    } else {
                        cm_timer.setText(Utility.getAppPrefString(MapsActivity.this, "timer"));
                        cm_timer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(MapsActivity.this, "timer")));
                        cm_timer.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("start")) {
                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                img_cancel_req.setVisibility(View.GONE);
                ll_profile.setVisibility(View.GONE);
                view_date.setVisibility(View.GONE);
                rl_date.setVisibility(View.GONE);
                rl_worker_data.setVisibility(View.GONE);
                rl_animation.setVisibility(View.GONE);
                confirm_address_map_custom_marker.setVisibility(View.GONE);
                ll_search.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                ll_timer.setVisibility(View.GONE);
                ll_profile_btn.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.VISIBLE);
                backgroundData.interrupt();
                if (googleMap != null) {
                    googleMap.clear();
                }
                try {
                    h.removeCallbacks(runnable);
                    cm_timer.setBase(SystemClock.elapsedRealtime());
                    cm_timer.setText("00:00:00");
                    cm_timer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("interview")) {
                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                img_cancel_req.setVisibility(View.VISIBLE);
                ll_profile.setVisibility(View.GONE);
                view_date.setVisibility(View.GONE);
                rl_date.setVisibility(View.GONE);
                rl_worker_data.setVisibility(View.GONE);
                rl_animation.setVisibility(View.GONE);
                confirm_address_map_custom_marker.setVisibility(View.GONE);
                ll_search.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                ll_timer.setVisibility(View.GONE);
                ll_profile_btn.setVisibility(View.GONE);
                ll_time_consumed.setVisibility(View.GONE);

                h.postDelayed(new Runnable() {
                    public void run() {
                        getTracking();
                        runnable = this;
                        h.postDelayed(runnable, delay);
                    }
                }, delay);

            } else {
                try {
                    count.cancel();
                    worker_chat_id = intent.getStringExtra("worker_id");
                    worker_latitude = intent.getStringExtra("worker_latitude");
                    worker_longitude = intent.getStringExtra("worker_longitude");
                    user_latitude = intent.getStringExtra("user_latitude");
                    user_longitude = intent.getStringExtra("user_longitude");
                    w_name = intent.getStringExtra("worker_name");
                    w_image = intent.getStringExtra("worker_image");
                    worker_rating = intent.getStringExtra("worker_rating");
                    worker_total_review = intent.getStringExtra("worker_total_review");
                    worker_job_type = intent.getStringExtra("worker_job_type");
                    worker_mobile = intent.getStringExtra("worker_mobile");
                    notification_id = intent.getStringExtra("notification_id");
                    service_id = intent.getStringExtra("service_id");

                    POINT_A = new LatLng(Double.parseDouble(user_latitude),
                            Double.parseDouble(user_longitude));
                    POINT_B = new LatLng(Double.parseDouble(worker_latitude),
                            Double.parseDouble(worker_longitude));
                    MarkerPoints = new ArrayList();
                    MarkerPoints.add(POINT_A);
                    MarkerPoints.add(POINT_B);

                    if (googleMap != null) {
                        if (MarkerPoints.size() >= 2) {
                            POINT_A = (LatLng) MarkerPoints.get(0);
                            POINT_B = (LatLng) MarkerPoints.get(1);
                            String url = getUrl(POINT_A, POINT_B);
                            new FetchUrl().execute(new String[]{url});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                img_cancel_req.setVisibility(View.VISIBLE);
                tv_w_name.setText(w_name);
                tv_w_job_type.setText(worker_job_type);
                tv_w_total.setText("(" + worker_total_review + ")");
                if (!w_image.equalsIgnoreCase("")) {
                    Glide.with(MapsActivity.this).load(w_image)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_w_profile);
                }
                ratingBar_w.setRating(Float.parseFloat(worker_rating));

                ll_profile.setVisibility(View.GONE);
                view_date.setVisibility(View.GONE);
                rl_date.setVisibility(View.GONE);
                rl_worker_data.setVisibility(View.GONE);
                rl_animation.setVisibility(View.GONE);
                confirm_address_map_custom_marker.setVisibility(View.GONE);
                ll_search.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.GONE);
                ll_timer.setVisibility(View.GONE);
                ll_profile_btn.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

            initUI();

            dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            TimeZone gmtTime = TimeZone.getTimeZone("GMT");
            dateFormatter.setTimeZone(gmtTime);

            Calendar newCalendar = Calendar.getInstance();
            Calendar newTimeCalendar = Calendar.getInstance();
            newTimeCalendar.setTimeInMillis(newCalendar.getTimeInMillis() + 180000);

            fromDatePickerDialog = new DatePickerDialog(MapsActivity.this,/* AlertDialog.THEME_HOLO_LIGHT,*/
                    new DatePickerDialog.OnDateSetListener() {
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            selectedDate = Calendar.getInstance();
                            selectedDate.set(year, monthOfYear, dayOfMonth);
//                        et_date.setText(dateFormatter.format(selectedDate.getTime()));

                            monthOfYear = monthOfYear + 1;
                            String month_name = null, day_name = null;
                            if (monthOfYear < 10) {
                                month_name = "0" + String.valueOf(monthOfYear);
                            } else {
                                month_name = "" + String.valueOf(monthOfYear);
                            }
                            if (dayOfMonth < 10) {
                                day_name = "0" + String.valueOf(dayOfMonth);
                            } else {
                                day_name = "" + String.valueOf(dayOfMonth);
                            }

                            reschedule_date = month_name + "/" + day_name
                                    + "/" + String.valueOf(year);
                            reschedule_date1 = day_name + "/" + month_name
                                    + "/" + String.valueOf(year);

                            et_date.setText(reschedule_date);

                        }
                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH),
                    newCalendar.get(Calendar.DAY_OF_MONTH));
            fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);


            fromTimePickerDialog = new TimePickerDialog(MapsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    Calendar datetime = Calendar.getInstance();
                    Calendar c = Calendar.getInstance();
                    datetime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                    datetime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                    datetime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                    datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    datetime.set(Calendar.MINUTE, minute);

                    String am_pm = "";

                    if (datetime.get(Calendar.AM_PM) == Calendar.AM)
                        am_pm = "am";
                    else if (datetime.get(Calendar.AM_PM) == Calendar.PM)
                        am_pm = "pm";

                    String time1 = (datetime.get(Calendar.HOUR) == 0) ? "12" : datetime
                            .get(Calendar.HOUR) + "";

                    int m = datetime.get(Calendar.MINUTE);
                    String min = "", hours = "";

                    if (m >= 10) {
                        min = String.valueOf(m);
                    } else {
                        min = "0" + String.valueOf(m);
                    }
                    if (Integer.parseInt(time1) >= 10) {
                        hours = time1;
                    } else {
                        hours = "0" + time1;
                    }

                    if (datetime.getTimeInMillis() > (c.getTimeInMillis() + 7200000)) {
                        //it's after current
                        et_time.setText(hours + ":" + min + " " + am_pm);
                    } else {
                        //it's before current'
                        Toast.makeText(getApplicationContext(),
                                "You have not set past time to schedule please select valid time", Toast.LENGTH_LONG).show();
                    }
                }
            }, newTimeCalendar.get(Calendar.HOUR_OF_DAY), (newTimeCalendar.get(Calendar.MINUTE)), false);


            if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("notification")) {
                try {
                    Intent intent = getIntent();
                    worker_chat_id = intent.getStringExtra("worker_id");
                    worker_latitude = intent.getStringExtra("worker_latitude");
                    worker_longitude = intent.getStringExtra("worker_longitude");
                    notification_id = intent.getStringExtra("notification_id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                autocompleteFragment = (PlaceAutocompleteFragment)
                        getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14.0f);
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextColor(ColorStateList.valueOf(Color.parseColor("#545454")));
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Search Address");
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setHintTextColor(ColorStateList.valueOf(Color.parseColor("#545454")));
                ((ImageButton) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_button)).setColorFilter(Color.parseColor("#545454"), PorterDuff.Mode.SRC_IN);

                Typeface tf = Typeface.createFromAsset(getAssets(), Constant.font_regular);
                ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTypeface(tf);

                autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        // TODO: Get info about the selected place.
                        Log.i(Constant.TAG, "Place: " + place.getName());
                        LatLng latLng = place.getLatLng();
                        Log.i(Constant.TAG, "Place Lat: " + latLng.latitude);
                        Log.i(Constant.TAG, "Place Long: " + latLng.longitude);

                        latitude = latLng.latitude;
                        longitude = latLng.longitude;
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
                        if (googleMap != null) {
                            googleMap.animateCamera(cameraUpdate);
                        }
                        getNearestData();
                    }

                    @Override
                    public void onError(Status status) {
                        // TODO: Handle the error.
                        Log.i(Constant.TAG, "An error occurred: " + status);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void initUI() {

        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);

        et_date = (EditText) findViewById(R.id.et_select_date);
        et_date.setOnClickListener(this);
        et_time = (EditText) findViewById(R.id.et_select_time);
        et_time.setOnClickListener(this);
        tv_date_time = (TextView) findViewById(R.id.tv_date_time);
        tv_job_type = (TextView) findViewById(R.id.tv_job_type);
        tv_job_type.setText(Utility.getAppPrefString(MapsActivity.this, "cat"));

        tv_rate = (TextView) findViewById(R.id.tv_rate);
        tv_rate.setText("P " + Utility.getAppPrefString(MapsActivity.this, "rate"));

        ll_profile = (LinearLayout) findViewById(R.id.ll_worker_profile);
        ll_worker_dialog = (LinearLayout) findViewById(R.id.ll_worker_dialog);
        ll_dateTime = (LinearLayout) findViewById(R.id.ll_date_time_dialog);
        view_date = (View) findViewById(R.id.view_date_time);
        rl_date = (RelativeLayout) findViewById(R.id.rl_date_time);
        btn_schedule = (Button) findViewById(R.id.btn_schedule);
        btn_schedule.setOnClickListener(this);
        btn_request = (Button) findViewById(R.id.btn_request);
        btn_request.setOnClickListener(this);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
        rl_worker_data = (RelativeLayout) findViewById(R.id.rl_worker_data);
        rl_next = (RelativeLayout) findViewById(R.id.rl_next);
        rl_next.setOnClickListener(this);
        rl_pre = (RelativeLayout) findViewById(R.id.rl_back);
        rl_pre.setOnClickListener(this);
        rl_animation = (RelativeLayout) findViewById(R.id.rl_animation);
        confirm_address_map_custom_marker = (ImageView) findViewById(R.id.confirm_address_map_custom_marker);
        layoutManager = new LinearLayoutManager(MapsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rv_ivr = (RecyclerView) findViewById(R.id.rv_worker);
        rv_ivr.setLayoutManager(layoutManager);
        ll_search = (LinearLayout) findViewById(R.id.ll_search);
        ll_timer = (LinearLayout) findViewById(R.id.rl_timer);
        tv_time = (TextView) findViewById(R.id.tv_timer);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        ll_profile_btn = (LinearLayout) findViewById(R.id.ll_profile_btn);
        tv_payment_type = (TextView) findViewById(R.id.tv_payment_type);
        tv_payment_type.setOnClickListener(this);
        tv_payment_method = (TextView) findViewById(R.id.tv_payment_method);

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

        img_cancel_req = (ImageView) findViewById(R.id.img_cancel_req);
        img_cancel_req.setOnClickListener(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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

        if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("notification")) {
            try {
                mapFragment.getMapAsync(MapsActivity.this);
                Intent intent = getIntent();
                worker_chat_id = intent.getStringExtra("worker_id");
                worker_latitude = intent.getStringExtra("worker_latitude");
                worker_longitude = intent.getStringExtra("worker_longitude");
                user_latitude = intent.getStringExtra("user_latitude");
                user_longitude = intent.getStringExtra("user_longitude");
                w_name = intent.getStringExtra("worker_name");
                w_image = intent.getStringExtra("worker_image");
                worker_rating = intent.getStringExtra("worker_rating");
                worker_total_review = intent.getStringExtra("worker_total_review");
                worker_job_type = intent.getStringExtra("worker_job_type");
                worker_mobile = intent.getStringExtra("worker_mobile");
                notification_id = intent.getStringExtra("notification_id");
                service_id = intent.getStringExtra("service_id");

                Log.v("LatLang: ", user_latitude + "," + user_longitude
                        + "\n" + worker_latitude + "," + worker_longitude);

                if (!user_latitude.equalsIgnoreCase("")) {
                    POINT_A = new LatLng(Double.parseDouble(user_latitude),
                            Double.parseDouble(user_longitude));
                    POINT_B = new LatLng(Double.parseDouble(worker_latitude),
                            Double.parseDouble(worker_longitude));
                    MarkerPoints = new ArrayList();
                    MarkerPoints.add(POINT_A);
                    MarkerPoints.add(POINT_B);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.VISIBLE);

            ll_profile.setVisibility(View.GONE);
            view_date.setVisibility(View.GONE);
            rl_date.setVisibility(View.GONE);
            rl_worker_data.setVisibility(View.GONE);
            rl_animation.setVisibility(View.GONE);
            confirm_address_map_custom_marker.setVisibility(View.GONE);
            ll_search.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
            ll_timer.setVisibility(View.GONE);
            ll_profile_btn.setVisibility(View.GONE);

            tv_w_name.setText(w_name);
            tv_w_job_type.setText(worker_job_type);
            tv_w_total.setText("(" + worker_total_review + ")");
            if (!w_image.equalsIgnoreCase("")) {
                Glide.with(MapsActivity.this).load(w_image)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_w_profile);
            }
            ratingBar_w.setRating(Float.parseFloat(worker_rating));

        } else if (Utility.getAppPrefString(MapsActivity.this, "data_flag").equalsIgnoreCase("true")) {
            if (Utility.isNetworkAvaliable(MapsActivity.this)) {
                try {
                    getCurrentData getTask = new getCurrentData(MapsActivity.this);
                    getTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            mapFragment.getMapAsync(MapsActivity.this);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_profile.getLayoutParams();
            ll_profile.setWeightSum(3.0f);
            view_date.setVisibility(View.GONE);
            rl_date.setVisibility(View.GONE);
            ll_profile.setLayoutParams(params);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_payment_type:
                String type = tv_payment_method.getText().toString();
                if (type.equalsIgnoreCase("Cash"))
                    p_flag = 0;
                else
                    p_flag = 1;
                final Dialog dialog = new Dialog(MapsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_change_payment);

                RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.rg_payment);
                RadioButton rb_cash = (RadioButton) dialog.findViewById(R.id.rb_cash);
                RadioButton rb_card = (RadioButton) dialog.findViewById(R.id.rb_card);
                Button btn_save = (Button) dialog.findViewById(R.id.btn_pay_method_save);
                Button btn_cancel = (Button) dialog.findViewById(R.id.btn_pay_method_cancel);

                if (p_flag == 0)
                    rb_cash.setChecked(true);
                else
                    rb_card.setChecked(true);

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.rb_cash:
                                p_flag = 0;
                                break;
                            case R.id.rb_card:
                                p_flag = 1;
                                break;
                        }
                    }
                });

                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (p_flag == 0)
                            tv_payment_method.setText("Cash");
                        else
                            tv_payment_method.setText("Card");
                    }
                });

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Window window = dialog.getWindow();
                WindowManager.LayoutParams wlp = window.getAttributes();

                wlp.gravity = Gravity.CENTER;
                wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setAttributes(wlp);

                dialog.show();
                break;
            case R.id.rl_my_location:
                getMyLocation();
                break;
            case R.id.rl_chat:
                Utility.writeSharedPreferences(this, "chat_from", "activity");
                Intent intent_chat = new Intent(MapsActivity.this, ChatActivity.class);
                intent_chat.putExtra("worker_id", worker_chat_id);
                intent_chat.putExtra("notification_id", notification_id);
                intent_chat.putExtra("service_id", service_id);
                intent_chat.putExtra("user_name", w_name);
                intent_chat.putExtra("user_image", w_image);
                intent_chat.putExtra("user_category", worker_job_type);
                intent_chat.putExtra("user_mobile", worker_mobile);
                intent_chat.putExtra("chatFrom","activity");
                startActivity(intent_chat);
                break;
            case R.id.rl_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+63" + worker_mobile));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                startActivity(callIntent);
                break;
            case R.id.btn_cancel:
                if (Utility.isNetworkAvaliable(MapsActivity.this)) {
                    try {
                        cancelRequest getTask = new cancelRequest(MapsActivity.this);
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.img_back:
                if (Utility.getAppPrefString(MapsActivity.this, "schedule").equalsIgnoreCase("true")) {
                    finish();
                } else {
                    Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    Utility.writeSharedPreferences(MapsActivity.this, "back_flag", "1");
                }
                break;
            case R.id.rl_back:
                rv_ivr.getLayoutManager().scrollToPosition(layoutManager.findLastVisibleItemPosition() - 1);
                break;
            case R.id.rl_next:
                rv_ivr.getLayoutManager().scrollToPosition(layoutManager.findFirstVisibleItemPosition() + 1);
                break;
            case R.id.btn_schedule:
                if (worker_flag) {
                    if (!flag) {
                        ll_worker_dialog.setVisibility(View.GONE);
                        ll_dateTime.setVisibility(View.VISIBLE);
                    } else {
                        if (WorkerItemAdapter.CheckId.size() > 0) {
                            btn_type = "schedule";
                            payment_method = "cash";

                            joined_worker_id = TextUtils.join(",", WorkerItemAdapter.CheckId);
                            if (Utility.isNetworkAvaliable(MapsActivity.this)) {
                                try {
                                    requestWorker getTask = new requestWorker(MapsActivity.this);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(MapsActivity.this, "Please select atleast one provider.", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "No Provider Available", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_request:
                if (!flag) {
                    if (worker_flag) {
                        btn_type = "normal";
                        payment_method = "";
                        if (Utility.isNetworkAvaliable(MapsActivity.this)) {
                            try {
                                requestWorker getTask = new requestWorker(MapsActivity.this);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "No Provider Available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Utility.getAppPrefString(MapsActivity.this, "schedule").equalsIgnoreCase("true")) {
                        finish();
                    } else {
                        Utility.writeSharedPreferences(MapsActivity.this, "map_flag", "0");
                        Intent intent1 = getIntent();
                        finish();
                        startActivity(intent1);
                    }

                }
                break;
            case R.id.btn_next:
                if (et_date.getText().toString().equalsIgnoreCase("") ||
                        et_time.getText().toString().equalsIgnoreCase("")) {
                    if (et_date.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please select Date", Toast.LENGTH_SHORT).show();
                    } else if (et_time.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please select Time", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ll_worker_dialog.setVisibility(View.VISIBLE);
                    ll_dateTime.setVisibility(View.GONE);
                    btn_schedule.setText("Confirm");
                    btn_request.setText("Cancel");
                    flag = true;
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_profile.getLayoutParams();
                    ll_profile.setWeightSum(4.0f);
                    ll_profile.setLayoutParams(params);

                    view_date.setVisibility(View.VISIBLE);
                    rl_date.setVisibility(View.VISIBLE);
                    tv_date_time.setText(et_date.getText().toString() + " at " + et_time.getText().toString());

                    rl_worker_data.setVisibility(View.VISIBLE);

                }
                break;
            case R.id.img_cancel_req:
                if (Utility.isNetworkAvaliable(MapsActivity.this)) {
                    try {
                        cancelRequest1 getTask = new cancelRequest1(MapsActivity.this);
                        getTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.et_select_date:
                fromDatePickerDialog.show();
//                onCreateDialog(Date_id);
                break;
            case R.id.et_select_time:
                if (et_date.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(MapsActivity.this, "Please select date", Toast.LENGTH_LONG).show();
                } else {
                    fromTimePickerDialog.show();
                }
//                onCreateDialog(Time_id);
                break;
            default:
                break;
        }
    }

    protected Dialog onCreateDialog(int id) {

        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        switch (id) {
            case Date_id:
                datePickerDialog = new DatePickerDialog(
                        MapsActivity.this, DatePickerDialog.THEME_HOLO_LIGHT, date_listener,
                        year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
                break;
            case Time_id:
                new TimePickerDialog(MapsActivity.this, TimePickerDialog.THEME_HOLO_LIGHT, time_listener,
                        hour, minute, false).show();
                break;
        }
        return null;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.map_style);
        this.googleMap.setMapStyle(style);

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else {
            if (Utility.getAppPrefString(MapsActivity.this, "activity_from").equalsIgnoreCase("notification")) {
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
            } else if (Utility.getAppPrefString(MapsActivity.this, "data_flag").equalsIgnoreCase("true")) {
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

                } else {
                    try {
                        LatLng POINT_CURRENT = new LatLng(Double.parseDouble(worker_latitude),
                                Double.parseDouble(worker_longitude));

                        MarkerOptions options = new MarkerOptions();
                        options.position(POINT_CURRENT);
                        options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.orange_pin));
                        googleMap.addMarker(options);
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(POINT_CURRENT).zoom(14).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (googleMap != null) {
                    mapView = mapFragment.getView().findViewById(Integer.parseInt("1"));
                    // Get the button view
                    View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                    rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                    googleMap.setPadding(0, 0, 12, 0);

                    if (Utility.getAppPrefString(MapsActivity.this, "map_flag").equalsIgnoreCase("0")) {
                        try {
                            latitude = Double.parseDouble(Utility.getAppPrefString(MapsActivity.this, "latitude"));
                            longitude = Double.parseDouble(Utility.getAppPrefString(MapsActivity.this, "longitude"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (latitude != 0.0) {
                            try {
                                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                                List<Address> addresses;
                                String address = null, area = "", cityName = "";
                                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (addresses != null || addresses.size() != 0) {
                                    address = addresses.get(0).getAddressLine(0);
                                    ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                                } else {
                                    ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("No location found");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(14).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            Utility.writeSharedPreferences(MapsActivity.this, "map_flag", "1");
                        }
                    }

                    googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {

                            Log.i("centerLat", cameraPosition.target.latitude + "");
                            Log.i("centerLong", cameraPosition.target.longitude + "");

                            try {

                                if (latitude != cameraPosition.target.latitude) {
                                    if (ll_accept_req_dialog.getVisibility() != View.VISIBLE) {
                                        getNearestData();
                                    }
                                }
                                latitude_pin = cameraPosition.target.latitude;
                                longitude_pin = cameraPosition.target.longitude;

                                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                                List<Address> addresses;
                                String address = null, area = "", cityName = "";
                                addresses = geocoder.getFromLocation(latitude_pin, longitude_pin, 1);
                                if (addresses != null || addresses.size() != 0) {
                                    try {
                                        address = addresses.get(0).getAddressLine(0);
                                        ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    ((EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setText("No location found");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

        }
    }

    @Override
    public void markerClick(int pos) {
        LatLng coordinate = new LatLng(Double.parseDouble(worker_lat[pos]), Double.parseDouble(worker_long[pos])); //Store these lat lng values somewhere. These should be constant.
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                coordinate, 15);
        googleMap.animateCamera(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        active_map = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active_map = false;
        Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
        Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
        Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
        Utility.writeSharedPreferences(MapsActivity.this, "timer", "");
        if (count != null)
            count.cancel();
        finish();
        Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
    }

    // Request/Schedule Worker API

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active_map = false;
        Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
        Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
        Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
        Utility.writeSharedPreferences(MapsActivity.this, "timer", "");
        if (count != null)
            count.cancel();
        finish();
        Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
    }

    // Cancel Ongoing Request API

    public void getNearestData() {
        backgroundData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String webUrl = Constant.URL_GET_WORKER_DATA;
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(MapsActivity.this, Constant.USER_ID)));
                    nameValuePairs.add(new BasicNameValuePair("cat_id", Utility.getAppPrefString(MapsActivity.this, "cat_id")));
                    nameValuePairs.add(new BasicNameValuePair("latitude", latitude + ""));
                    nameValuePairs.add(new BasicNameValuePair("longitude", longitude + ""));
                    String response1 = Utility.postRequest(MapsActivity.this, webUrl, nameValuePairs);

                    JSONObject jObject = new JSONObject(response1);
                    Log.v("Response:", jObject.toString());

                    String response = "", message = "";
                    JSONArray jsonArray;
                    JSONObject jsonObjectMessage;

                    response = jObject.getString("response");
                    message = jObject.getString("message");

                    if (response.equalsIgnoreCase("true")) {
                        if (arraylist1.size() > 0) {
                            arraylist1.clear();
                        }
                        worker_flag = true;
                        jsonArray = jObject.getJSONArray("map_data");
                        int lenth = jsonArray.length();

                        worker_id = new String[lenth];
                        worker_name = new String[lenth];
                        worker_image = new String[lenth];
                        worker_price = new String[lenth];
                        worker_lat = new String[lenth];
                        worker_long = new String[lenth];

                        for (int a = 0; a < lenth; a++) {
                            jsonObjectMessage = jsonArray.getJSONObject(a);

                            try {
                                worker_id[a] = jsonObjectMessage.getString("worker_id");
                                worker_name[a] = jsonObjectMessage.getString("worker_name");
                                worker_image[a] = jsonObjectMessage.getString("worker_image");
                                worker_price[a] = jsonObjectMessage.getString("worker_price");
                                worker_lat[a] = jsonObjectMessage.getString("worker_lat");
                                worker_long[a] = jsonObjectMessage.getString("worker_long");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0; i < lenth; i++) {
                            LatLngClass wp = new LatLngClass(worker_id[i], worker_name[i],
                                    worker_image[i], worker_price[i], worker_lat[i], worker_long[i]);
                            arraylist1.add(wp);
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    workerItemAdapter = new WorkerItemAdapter(MapsActivity.this, worker_id,
                                            worker_name,
                                            worker_image,
                                            worker_price,
                                            worker_lat,
                                            worker_long);
                                    rv_ivr.setAdapter(workerItemAdapter);
                                    layoutManager.scrollToPositionWithOffset(0, 0);
                                    rv_ivr.setNestedScrollingEnabled(false);

                                    if (googleMap != null) {
                                        googleMap.clear();
                                    }

                                    for (int i = 0; i < arraylist1.size(); i++) {
                                        final LatLngClass bean = arraylist1.get(i);
                                        locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(arraylist1.get(i).getWorker_lat()), Double.parseDouble(arraylist1.get(i).getWorker_long()))).icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_tip)));

                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(locationMarker.getPosition());
                                        hashMapMarker.put(locationMarker, bean);

                                        /*googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                            // Use default InfoWindow frame
                                            @Override
                                            public View getInfoWindow(final Marker marker) {
                                                View v = getLayoutInflater().inflate(R.layout.custom_map_snippet, null);
                                                LatLngClass bean = hashMapMarker.get(marker);

                                                TextView tv_name = (TextView) v.findViewById(R.id.tv_map_name);
                                                tv_name.setText(bean.getWorker_name());
                                                ImageView img_profile = (ImageView) v.findViewById(R.id.img_map_profile);
                                                ImageView img_close = (ImageView) v.findViewById(R.id.img_map_close);

                                                Glide.with(MapsActivity.this).load(bean.getWorker_image())
                                                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                                        .thumbnail(0.5f)
                                                        .into(img_profile);

                                                img_close.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        marker.hideInfoWindow();
                                                    }
                                                });
                                                return v;
                                            }

                                            @Override
                                            public View getInfoContents(Marker marker) {
                                                return null;
                                            }
                                        });*/
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    worker_flag = false;
                                    if (googleMap != null) {
                                        googleMap.clear();
                                    }
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

    // get Nearest Data API

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

    public void getTracking() {
        backgroundData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray;
                    JSONObject jsonObjectMessage;
                    String[] worker_id, worker_latitude, worker_longitude;
                    String webUrl = Constant.URL_GET_TRACKING;
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(MapsActivity.this, Constant.USER_ID)));
                    nameValuePairs.add(new BasicNameValuePair("worker_id", worker_chat_id));
                    nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                    String response1 = Utility.postRequest(MapsActivity.this, webUrl, nameValuePairs);

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

    @Override
    public void onBackPressed() {
        if (Utility.getAppPrefString(MapsActivity.this, "schedule").equalsIgnoreCase("true")) {
            finish();
        } else {
            Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
            Utility.writeSharedPreferences(MapsActivity.this, "back_flag", "1");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        active_map = true;
        registerReceiver(mMessageReceiver, new IntentFilter("unique_name"));

        if (Utility.getAppPrefString(MapsActivity.this, "noti_flag").equalsIgnoreCase("accept")) {
            if (count != null)
                count.cancel();

            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.VISIBLE);
            ll_profile.setVisibility(View.GONE);
            view_date.setVisibility(View.GONE);
            rl_date.setVisibility(View.GONE);
            rl_worker_data.setVisibility(View.GONE);
            rl_animation.setVisibility(View.GONE);
            confirm_address_map_custom_marker.setVisibility(View.GONE);
            ll_search.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
            ll_timer.setVisibility(View.GONE);
            ll_profile_btn.setVisibility(View.GONE);

            try {
                Intent intent = getIntent();

                worker_chat_id = intent.getStringExtra("worker_id");
                if (worker_chat_id != null) {
                    worker_latitude = intent.getStringExtra("worker_latitude");
                    worker_longitude = intent.getStringExtra("worker_longitude");
                    user_latitude = intent.getStringExtra("user_latitude");
                    user_longitude = intent.getStringExtra("user_longitude");
                    w_name = intent.getStringExtra("worker_name");
                    w_image = intent.getStringExtra("worker_image");
                    worker_rating = intent.getStringExtra("worker_rating");
                    worker_total_review = intent.getStringExtra("worker_total_review");
                    worker_job_type = intent.getStringExtra("worker_job_type");
                    worker_mobile = intent.getStringExtra("worker_mobile");
                    notification_id = intent.getStringExtra("notification_id");
                    service_id = intent.getStringExtra("service_id");
                } else {
                    worker_chat_id = Utility.getAppPrefString(MapsActivity.this, "worker_id");
                    worker_latitude = Utility.getAppPrefString(MapsActivity.this, "worker_latitude");
                    worker_longitude = Utility.getAppPrefString(MapsActivity.this, "worker_longitude");
                    user_latitude = Utility.getAppPrefString(MapsActivity.this, "user_latitude");
                    user_longitude = Utility.getAppPrefString(MapsActivity.this, "user_longitude");
                    w_name = Utility.getAppPrefString(MapsActivity.this, "worker_name");
                    w_image = Utility.getAppPrefString(MapsActivity.this, "worker_image");
                    worker_rating = Utility.getAppPrefString(MapsActivity.this, "worker_rating");
                    worker_total_review = Utility.getAppPrefString(MapsActivity.this, "worker_total_review");
                    worker_job_type = Utility.getAppPrefString(MapsActivity.this, "worker_job_type");
                    worker_mobile = Utility.getAppPrefString(MapsActivity.this, "worker_mobile");
                    notification_id = Utility.getAppPrefString(MapsActivity.this, "notification_id");
                    service_id = Utility.getAppPrefString(MapsActivity.this, "service_id");
                }

                POINT_A = new LatLng(Double.parseDouble(user_latitude),
                        Double.parseDouble(user_longitude));
                POINT_B = new LatLng(Double.parseDouble(worker_latitude),
                        Double.parseDouble(worker_longitude));
                MarkerPoints = new ArrayList();
                MarkerPoints.add(POINT_A);
                MarkerPoints.add(POINT_B);

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

                tv_w_name.setText(w_name);
                tv_w_job_type.setText(worker_job_type);
                tv_w_total.setText("(" + worker_total_review + ")");
                if (!w_image.equalsIgnoreCase("")) {
                    Glide.with(MapsActivity.this).load(w_image)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                            .thumbnail(0.5f)
                            .into(img_w_profile);
                }
                ratingBar_w.setRating(Float.parseFloat(worker_rating));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Utility.getAppPrefString(MapsActivity.this, "noti_flag").equalsIgnoreCase("finish")) {
            Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
            Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
            Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
            Utility.writeSharedPreferences(MapsActivity.this, "timer", "");
            finish();
            Intent intent_finish = new Intent(MapsActivity.this, InvoiceActivity.class);
            Utility.writeSharedPreferences(MapsActivity.this, "invoice_s_id", service_id);
            Utility.writeSharedPreferences(MapsActivity.this, "sc_flag", "0");
            Utility.writeSharedPreferences(MapsActivity.this, "coupon_flag", "0");
            Utility.writeSharedPreferences(MapsActivity.this, "back_flag", "1");
            startActivity(intent_finish);
        } else if (Utility.getAppPrefString(MapsActivity.this, "noti_flag").equalsIgnoreCase("time")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.GONE);
            ll_profile.setVisibility(View.GONE);
            view_date.setVisibility(View.GONE);
            rl_date.setVisibility(View.GONE);
            rl_worker_data.setVisibility(View.GONE);
            rl_animation.setVisibility(View.GONE);
            confirm_address_map_custom_marker.setVisibility(View.GONE);
            ll_search.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
            ll_timer.setVisibility(View.GONE);
            ll_profile_btn.setVisibility(View.GONE);
        } else if (Utility.getAppPrefString(MapsActivity.this, "noti_flag").equalsIgnoreCase("start")) {
            ll_accept_req_dialog.setVisibility(View.VISIBLE);
            img_cancel_req.setVisibility(View.GONE);
            ll_profile.setVisibility(View.GONE);
            view_date.setVisibility(View.GONE);
            rl_date.setVisibility(View.GONE);
            rl_worker_data.setVisibility(View.GONE);
            rl_animation.setVisibility(View.GONE);
            confirm_address_map_custom_marker.setVisibility(View.GONE);
            ll_search.setVisibility(View.GONE);
            btn_cancel.setVisibility(View.GONE);
            ll_timer.setVisibility(View.GONE);
            ll_profile_btn.setVisibility(View.GONE);
            if (googleMap != null) {
                googleMap.clear();
            }
            h.removeCallbacks(runnable);
        } else if (Utility.getAppPrefString(MapsActivity.this, "noti_flag").equalsIgnoreCase("cancel")) {
            Intent intent_cancel = new Intent(MapsActivity.this, HomeActivity.class);
            startActivity(intent_cancel);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        active_map = false;
        unregisterReceiver(mMessageReceiver);
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

    private void getMyLocation() {
        try {
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
            googleMap.animateCamera(cameraUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String updateTime(int hours, int mins) {
        String timeSet = "";

        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hours).append(':')
                .append(minutes).append(" ").append(timeSet).toString();

        return aTime;
    }

    public class requestWorker extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public requestWorker(final Context mContext) {
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

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    if (btn_type.equalsIgnoreCase("schedule")) {
                        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        if (googleMap != null) {
                            googleMap.clear();
                        }
                        ll_profile.setVisibility(View.GONE);
                        view_date.setVisibility(View.GONE);
                        rl_date.setVisibility(View.GONE);
                        rl_worker_data.setVisibility(View.GONE);
                        rl_animation.setVisibility(View.VISIBLE);
                        confirm_address_map_custom_marker.setVisibility(View.GONE);
                        ll_search.setVisibility(View.GONE);
                        btn_cancel.setVisibility(View.VISIBLE);
                        ll_timer.setVisibility(View.VISIBLE);
                        ll_profile_btn.setVisibility(View.GONE);
                        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
                        rippleBackground.startRippleAnimation();
                        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                        count = new CountDownTimer(61000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                tv_time.setText(millisUntilFinished / 1000 + "");
                            }

                            public void onFinish() {
                                try {
                                    finish();
                                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                                    Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_REQUEST_WORKER;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("cat_id", Utility.getAppPrefString(mContext, "cat_id")));
                nameValuePairs.add(new BasicNameValuePair("latitude", latitude_pin + ""));
                nameValuePairs.add(new BasicNameValuePair("longitude", longitude_pin + ""));
                nameValuePairs.add(new BasicNameValuePair("time", et_time.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("date", reschedule_date1));
                nameValuePairs.add(new BasicNameValuePair("payment_type", tv_payment_method.getText().toString().toLowerCase()));
                nameValuePairs.add(new BasicNameValuePair("worker_id", joined_worker_id));

                if (Utility.getAppPrefString(MapsActivity.this, "schedule").equalsIgnoreCase("true")) {
                    nameValuePairs.add(new BasicNameValuePair("type", "schedule"));
                    nameValuePairs.add(new BasicNameValuePair("notification_id", Utility.getAppPrefString(mContext, "schedule_notification_id")));
                } else {
                    nameValuePairs.add(new BasicNameValuePair("type", btn_type));
                }

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                service_id = jObject.getString("service_id");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Cancel Interviewed API

    public class cancelRequest extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public cancelRequest(final Context mContext) {
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

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                    Utility.writeSharedPreferences(MapsActivity.this, "data_flag", "false");
                    Utility.writeSharedPreferences(MapsActivity.this, "activity_from", "activity");
                    Utility.writeSharedPreferences(MapsActivity.this, "noti_flag", "false");
                    Utility.writeSharedPreferences(MapsActivity.this, "timer", "");
                    if (count != null)
                        count.cancel();
                    finish();
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_CANCEL_REQUEST;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));
                nameValuePairs.add(new BasicNameValuePair("type", "reject"));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        private FetchUrl() {
        }

        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = MapsActivity.this.downloadUrl(url[0]);
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
                polylinePath = googleMap.addPolyline(lineOptions);

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

                    Utility.writeSharedPreferences(mContext, "schedule", "false");
                    Utility.writeSharedPreferences(mContext, "map_flag", "0");
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Utility.writeSharedPreferences(mContext, "activity_from", "activity");
                    Utility.writeSharedPreferences(mContext, "noti_flag", "false");
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//                    recreate();

                    finish();
                    Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
                    startActivity(intent);
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
                nameValuePairs.add(new BasicNameValuePair("service_id", service_id));

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
        String response, message;
        String date, time, request_type, worker_latitude,
                worker_longitude, worker_mobile, current_flag, type;

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
                        Utility.writeSharedPreferences(mContext, "data_flag", "true");
                        Utility.writeSharedPreferences(mContext, "saved_worker_id", worker_chat_id);
                        Utility.writeSharedPreferences(mContext, "saved_notification_id", notification_id);
                        Utility.writeSharedPreferences(mContext, "saved_service_id", service_id);
                        Utility.writeSharedPreferences(mContext, "saved_worker_name", w_name);
                        Utility.writeSharedPreferences(mContext, "saved_worker_image", w_image);
                        Utility.writeSharedPreferences(mContext, "saved_rating", worker_rating);
                        Utility.writeSharedPreferences(mContext, "saved_total_review", worker_total_review);
                        Utility.writeSharedPreferences(mContext, "saved_date", date);
                        if (time.equalsIgnoreCase("0")) {
                            Utility.writeSharedPreferences(mContext, "saved_time", "00:00:00");
                        } else {
                            Utility.writeSharedPreferences(mContext, "saved_time", time);
                        }
                        Utility.writeSharedPreferences(mContext, "saved_request_type", request_type);
                        Utility.writeSharedPreferences(mContext, "saved_worker_cat", worker_job_type);
                        Utility.writeSharedPreferences(mContext, "saved_worker_latitude", worker_latitude);
                        Utility.writeSharedPreferences(mContext, "saved_worker_longitude", worker_longitude);
                        Utility.writeSharedPreferences(mContext, "saved_worker_mobile", worker_mobile);
                        Utility.writeSharedPreferences(mContext, "saved_current_flag", current_flag);

                        POINT_A = new LatLng(Double.parseDouble(Utility.getAppPrefString(MapsActivity.this, "latitude")),
                                Double.parseDouble(Utility.getAppPrefString(MapsActivity.this, "longitude")));
                        POINT_B = new LatLng(Double.parseDouble(worker_latitude),
                                Double.parseDouble(worker_longitude));
                        MarkerPoints = new ArrayList();
                        MarkerPoints.add(POINT_A);
                        MarkerPoints.add(POINT_B);

                        mapFragment.getMapAsync(MapsActivity.this);

                        if (Utility.getAppPrefString(MapsActivity.this, "data_flag").equalsIgnoreCase("true")) {

                            worker_chat_id = Utility.getAppPrefString(MapsActivity.this, "saved_worker_id");
                            worker_latitude = Utility.getAppPrefString(MapsActivity.this, "saved_worker_latitude");
                            worker_longitude = Utility.getAppPrefString(MapsActivity.this, "saved_worker_longitude");
                            notification_id = Utility.getAppPrefString(MapsActivity.this, "saved_notification_id");
                            service_id = Utility.getAppPrefString(MapsActivity.this, "saved_service_id");
                            w_name = Utility.getAppPrefString(MapsActivity.this, "saved_worker_name");
                            w_image = Utility.getAppPrefString(MapsActivity.this, "saved_worker_image");
                            worker_job_type = Utility.getAppPrefString(MapsActivity.this, "saved_worker_cat");
                            worker_mobile = Utility.getAppPrefString(MapsActivity.this, "saved_worker_mobile");

                            if (Utility.getAppPrefString(MapsActivity.this, "saved_current_flag").equalsIgnoreCase("pause")) {
                                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                                img_cancel_req.setVisibility(View.GONE);
                                ll_profile.setVisibility(View.GONE);
                                view_date.setVisibility(View.GONE);
                                rl_date.setVisibility(View.GONE);
                                rl_worker_data.setVisibility(View.GONE);
                                rl_animation.setVisibility(View.GONE);
                                confirm_address_map_custom_marker.setVisibility(View.GONE);
                                ll_search.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.GONE);
                                ll_timer.setVisibility(View.GONE);
                                ll_profile_btn.setVisibility(View.GONE);
                                ll_time_consumed.setVisibility(View.VISIBLE);
                                cm_timer.setText(Utility.getAppPrefString(MapsActivity.this, "saved_time"));

                            } else if (Utility.getAppPrefString(MapsActivity.this, "saved_current_flag").equalsIgnoreCase("start")) {
                                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                                img_cancel_req.setVisibility(View.GONE);
                                ll_profile.setVisibility(View.GONE);
                                view_date.setVisibility(View.GONE);
                                rl_date.setVisibility(View.GONE);
                                rl_worker_data.setVisibility(View.GONE);
                                rl_animation.setVisibility(View.GONE);
                                confirm_address_map_custom_marker.setVisibility(View.GONE);
                                ll_search.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.GONE);
                                ll_timer.setVisibility(View.GONE);
                                ll_profile_btn.setVisibility(View.GONE);
                                ll_time_consumed.setVisibility(View.VISIBLE);
                                if (googleMap != null) {
                                    googleMap.clear();
                                }
                                try {
                                    h.removeCallbacks(runnable);
                                    cm_timer.setText(Utility.getAppPrefString(MapsActivity.this, "saved_time"));
                                    cm_timer.setBase(SystemClock.elapsedRealtime() - convert(Utility.getAppPrefString(MapsActivity.this, "saved_time")));
                                    cm_timer.start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (Utility.getAppPrefString(MapsActivity.this, "saved_current_flag").equalsIgnoreCase("interview")) {
                                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                                img_cancel_req.setVisibility(View.VISIBLE);
                                ll_profile.setVisibility(View.GONE);
                                view_date.setVisibility(View.GONE);
                                rl_date.setVisibility(View.GONE);
                                rl_worker_data.setVisibility(View.GONE);
                                rl_animation.setVisibility(View.GONE);
                                confirm_address_map_custom_marker.setVisibility(View.GONE);
                                ll_search.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.GONE);
                                ll_timer.setVisibility(View.GONE);
                                ll_profile_btn.setVisibility(View.GONE);
                            } else if (Utility.getAppPrefString(MapsActivity.this, "saved_current_flag").equalsIgnoreCase("finish")) {
                                finish();
                                Intent intent = new Intent(MapsActivity.this, InvoiceActivity.class);
                                Utility.writeSharedPreferences(mContext, "invoice_s_id", service_id);
                                Utility.writeSharedPreferences(mContext, "sc_flag", "0");
                                Utility.writeSharedPreferences(mContext, "coupon_flag", "0");
                                startActivity(intent);
                            } else {
                                ll_accept_req_dialog.setVisibility(View.VISIBLE);
                                img_cancel_req.setVisibility(View.VISIBLE);
                                ll_profile.setVisibility(View.GONE);
                                view_date.setVisibility(View.GONE);
                                rl_date.setVisibility(View.GONE);
                                rl_worker_data.setVisibility(View.GONE);
                                rl_animation.setVisibility(View.GONE);
                                confirm_address_map_custom_marker.setVisibility(View.GONE);
                                ll_search.setVisibility(View.GONE);
                                btn_cancel.setVisibility(View.GONE);
                                ll_timer.setVisibility(View.GONE);
                                ll_profile_btn.setVisibility(View.GONE);
                            }
                            tv_w_name.setText(Utility.getAppPrefString(MapsActivity.this, "saved_worker_name"));
                            tv_w_job_type.setText(Utility.getAppPrefString(MapsActivity.this, "saved_worker_cat"));
                            tv_w_total.setText(Utility.getAppPrefString(MapsActivity.this, "saved_total_review"));
                            if (!Utility.getAppPrefString(MapsActivity.this, "saved_worker_image").equalsIgnoreCase("")) {
                                Glide.with(MapsActivity.this).load(Utility.getAppPrefString(MapsActivity.this, "saved_worker_image"))
                                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                        .thumbnail(0.5f)
                                        .into(img_w_profile);
                            }
                            ratingBar_w.setRating(Float.parseFloat(Utility.getAppPrefString(MapsActivity.this, "saved_rating")));
                        }
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        Utility.writeSharedPreferences(mContext, "activity_from", "activity");
                        Utility.writeSharedPreferences(mContext, "map_flag", "0");
                        mapFragment.getMapAsync(MapsActivity.this);
                        MarkerPoints = new ArrayList();
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_profile.getLayoutParams();
                        ll_profile.setWeightSum(3.0f);
                        view_date.setVisibility(View.GONE);
                        rl_date.setVisibility(View.GONE);
                        rl_worker_data.setVisibility(View.GONE);
                        ll_profile.setLayoutParams(params);
                        ll_accept_req_dialog.setVisibility(View.GONE);
                        img_cancel_req.setVisibility(View.GONE);
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "login", "false");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                    alertDialog.setTitle("Authentication Error");
                    alertDialog.setMessage("Authentication error occur. Please login again.");
                    alertDialog.setCancelable(false);

                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
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

                    /*jsonArray = jObject.getJSONArray("req_data");
                    int lenth = jsonArray.length();

                    worker_id = new String[lenth];*/

                    worker_chat_id = jObject.getString("worker_id");
                    notification_id = jObject.getString("notification_id");
                    service_id = jObject.getString("service_id");
                    w_name = jObject.getString("worker_name");
                    w_image = jObject.getString("worker_image");
                    worker_rating = jObject.getString("rating");
                    worker_total_review = jObject.getString("total_review");
                    request_type = jObject.getString("request_type");
                    worker_job_type = jObject.getString("worker_cat");
                    worker_latitude = jObject.getString("worker_latitude");
                    worker_longitude = jObject.getString("worker_longitude");
                    worker_mobile = jObject.getString("mobile");
                    current_flag = jObject.getString("current_flag");
                    time = jObject.getString("timer");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}