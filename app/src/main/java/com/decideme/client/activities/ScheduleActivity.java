package com.decideme.client.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.adapter.ScheduleListAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.ExpandableHeightListView;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends Activity implements View.OnClickListener, ScheduleListAdapter.RefreshActivity {

    public static boolean active_schedule = false;
    private Context mContext;
    private ExpandableHeightListView expandableHeightListView;
    private TextView tv_job_type, tv_date, tv_no_of_recruiters;
    private Button btn_add;
    private String[] worker_id, worker_name, worker_image, worker_rating, worker_status, worker_timer, service_id,
            worker_lat, worker_long, current_flag, notification_id, timer, mobile;
    private String job_type, rate, schedule_date, no_of_req, cat_id;
    private ImageView img_back;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utility.isNetworkAvaliable(mContext)) {
                try {
                    getCurrentData getTask = new getCurrentData(mContext);
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
        setContentView(R.layout.activity_schedule);

            initUI();

    }

    private void initUI() {

        mContext = ScheduleActivity.this;

        expandableHeightListView = (ExpandableHeightListView) findViewById(R.id.list_schedule);
        tv_job_type = (TextView) findViewById(R.id.tv_sc_cat);
        tv_date = (TextView) findViewById(R.id.tv_sc_date);
        tv_no_of_recruiters = (TextView) findViewById(R.id.tv_sc_no_of_recruiters);
        btn_add = (Button) findViewById(R.id.btn_add_recruit);
        btn_add.setOnClickListener(this);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);

        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                getCurrentData getTask = new getCurrentData(mContext);
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ScheduleActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        active_schedule = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active_schedule = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        active_schedule = true;
        registerReceiver(mMessageReceiver, new IntentFilter("unique_name1"));
        initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active_schedule = false;
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Intent intent_back = new Intent(ScheduleActivity.this, HomeActivity.class);
                startActivity(intent_back);
                finish();
                break;
            case R.id.btn_add_recruit:
                if (worker_id.length < 3) {

                    Utility.writeSharedPreferences(mContext, "activity_from", "activity");
                    Utility.writeSharedPreferences(mContext, "schedule", "true");
                    Utility.writeSharedPreferences(mContext, "schedule_notification_id", notification_id[0]);
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    Utility.writeSharedPreferences(mContext, "map_flag", "0");

                    Intent intent = new Intent(mContext, MapsActivity.class);
                    Utility.writeSharedPreferences(mContext, "rate", rate);
                    Utility.writeSharedPreferences(mContext, "cat", job_type);
                    Utility.writeSharedPreferences(mContext, "cat_id", cat_id);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "You can not add more than 3 providers at a time",
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void refresh() {
        if (worker_id.length == 1) {
            Intent intent = new Intent(ScheduleActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            recreate();
        }
    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response, message, type;
        JSONArray jsonArray;
        JSONObject jsonObjectMessage;

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
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (type.equalsIgnoreCase("authorized")) {
                    if (response.equalsIgnoreCase("true")) {
                        Utility.writeSharedPreferences(mContext, "sc_job_type", job_type);
                        tv_date.setText(schedule_date);
                        tv_job_type.setText(job_type);
                        tv_no_of_recruiters.setText(no_of_req);
                        expandableHeightListView.setAdapter(new ScheduleListAdapter(ScheduleActivity.this, worker_id, worker_name,
                                worker_image, worker_rating, worker_status, worker_timer, service_id,
                                worker_lat, worker_long, current_flag, notification_id, timer, mobile));
                    } else {
                        Intent intent1 = new Intent(ScheduleActivity.this, HomeActivity.class);
                        startActivity(intent1);
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
                    job_type = jObject.getString("job_type");
                    rate = jObject.getString("rate");
                    schedule_date = jObject.getString("schedule_date");
                    no_of_req = jObject.getString("no_of_req");
                    cat_id = jObject.getString("cat_id");

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
