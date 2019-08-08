package com.decideme.client.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.adapter.ChatListAdapter;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.ExpandableHeightListView;
import com.decideme.client.attributes.Utility;
import com.decideme.client.model.ChatList;
import com.decideme.client.model.ChatListModel;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    public static boolean active = false;
    Toolbar toolbarTop;
    private Context mContext;
    private TextView tv_name, tv_job_type;
    private ImageView img_profile, img_call, img_back, img_smiley;
    private EditText et_message;
    private RelativeLayout rl_send;
    private ExpandableHeightListView listView;
    private ChatListAdapter ChatListAdapter;
    private String worker_id = "", phone = "", from = "";
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ChatActivity.this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(ChatActivity.this, R.color.actionbar_color));
        }
        setContentView(R.layout.activity_chat);

        toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        if (toolbarTop != null) {
            setSupportActionBar(toolbarTop);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbarTop.setNavigationIcon(R.drawable.back_icon);
            toolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        initUI();

    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        active = true;
        if (Utility.getAppPrefString(mContext, "chat_from").equalsIgnoreCase("notification"))
            initUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    public void onBackPressed() {
        if (from.equalsIgnoreCase("activity")) {
            if (Utility.getAppPrefString(mContext, "process_type")
                    .equalsIgnoreCase("schedule")) {
                Utility.writeSharedPreferences(mContext, "schedule", "true");
                Intent intent = new Intent(mContext, ScheduleActivity.class);
                startActivity(intent);
                finish();
            } else {
                Utility.writeSharedPreferences(mContext, "data_flag", "true");
                Utility.writeSharedPreferences(mContext, "schedule", "false");
                Intent intent = new Intent(mContext, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            Utility.writeSharedPreferences(mContext, "data_flag", "false");
            finish();
            Intent intent = new Intent(mContext, HomeActivity.class);
            Utility.writeSharedPreferences(mContext, "authentication_type", "authorized");
            startActivity(intent);
        }
    }

    private void initUI() {

        tv_name = (TextView) findViewById(R.id.tv_chat_from_name);
        tv_job_type = (TextView) findViewById(R.id.tv_chat_from_job_type);
        img_profile = (ImageView) findViewById(R.id.img_chat_from_profile);
        img_call = (ImageView) findViewById(R.id.img_chat_from_call);
        img_call.setOnClickListener(this);
        img_back = (ImageView) findViewById(R.id.img_chat_back);
        img_back.setOnClickListener(this);
        img_smiley = (ImageView) findViewById(R.id.img_chat_smiley);
        img_smiley.setOnClickListener(this);
        et_message = (EditText) findViewById(R.id.et_chat_message);
        rl_send = (RelativeLayout) findViewById(R.id.rl_send);
        rl_send.setOnClickListener(this);
        listView = (ExpandableHeightListView) findViewById(R.id.list_chat);
        Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "false");

        try {
            Intent intent = getIntent();
            worker_id = intent.getStringExtra("worker_id");
            tv_name.setText(intent.getStringExtra("user_name"));
            tv_job_type.setText(intent.getStringExtra("user_category"));
            phone = intent.getStringExtra("user_mobile");
            from = intent.getStringExtra("chatFrom");
            if (!intent.getStringExtra("user_image").equalsIgnoreCase("")) {
                Glide.with(mContext).load(intent.getStringExtra("user_image"))
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_profile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // get Chat History List API
        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                getChatHistory getTask = new getChatHistory(mContext);
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_chat_from_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "+63" + phone));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                startActivity(callIntent);
                break;
            case R.id.img_chat_back:
                onBackPressed();
                break;
            case R.id.rl_send:
                if (!et_message.getText().toString().trim().equalsIgnoreCase("")) {
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            sendMessage getTask = new sendMessage(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public class getChatHistory extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;

        ChatListModel ChatListModel;

        public getChatHistory(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
            ChatListModel ChatListModel = (ChatListModel) getAboutMeListItem();
            if (ChatListModel != null)
                return ChatListModel.getChat_data();
            else
                return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (result != null) {
                    if (ChatListModel.getResponse().equalsIgnoreCase("true")) {
                        if (result != null && result instanceof List) {
                            ChatListAdapter = new ChatListAdapter(ChatActivity.this, (List<ChatList>) result);
                            listView.setAdapter(ChatListAdapter);
                            listView.setExpanded(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_CHAT_HISTORY;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("user_chat_id", worker_id));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                ChatListModel = (ChatListModel) new Gson().fromJson(jObject.toString(), ChatListModel.class);

                return ChatListModel;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Send Message API
    public class sendMessage extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        private String response, message;

        public sendMessage(final Context mContext) {
            this.mContext = mContext;
            mProgressDialog = new DME_ProgressDilog(mContext);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
            return Integer.valueOf(0);
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                if (response.equalsIgnoreCase("true")) {
                    et_message.setText("");
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            getChatHistory getTask = new getChatHistory(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_SEND_MESSAGE;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("user_chat_id", worker_id));
                nameValuePairs.add(new BasicNameValuePair("message", et_message.getText().toString()));

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
}