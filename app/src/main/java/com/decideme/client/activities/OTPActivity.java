package com.decideme.client.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private TextView tv_title, tv_username, tv_mobile_edit, tv_resend;
    private EditText et_email, et_mobile;
    private Button btn_submit;
    private PinEntryEditText et_otp;
    private TextView cm_timer;
    private LinearLayout ll_mobile, ll_otp, ll_resend;
    private ImageView img_edit, img_back;
    private String back_otp = "";

    CounterClass timer;

    Animation left_to_right_animation;
    Animation right_to_left_animation;
    private String email, id, login_type, user_name, type, mobile, user_address, user_image;

    Fragment fr = null;
    private static final String BACK_STACK_ROOT_TAG = "main_fragment";
    private int otp_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fb_email_mobile);

        initUI();

    }

    private void initUI() {

        tv_title = (TextView) findViewById(R.id.tv_mobile_title);
        tv_username = (TextView) findViewById(R.id.tv_mobile_username);
        tv_mobile_edit = (TextView) findViewById(R.id.tv_mobile_edit);
        tv_resend = (TextView) findViewById(R.id.tv_resend_otp);
        tv_resend.setOnClickListener(this);
        et_email = (EditText) findViewById(R.id.et_f_email);
        et_mobile = (EditText) findViewById(R.id.et_f_mobile);
        btn_submit = (Button) findViewById(R.id.btn_f_login);
        btn_submit.setOnClickListener(this);
        et_otp = (PinEntryEditText) findViewById(R.id.txt_pin_entry);
        cm_timer = (TextView) findViewById(R.id.cm_otp_timer);
        ll_mobile = (LinearLayout) findViewById(R.id.ll_mobile);
        ll_otp = (LinearLayout) findViewById(R.id.ll_otp);
        ll_resend = (LinearLayout) findViewById(R.id.ll_resend);
        img_edit = (ImageView) findViewById(R.id.img_edit);
        img_edit.setOnClickListener(this);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);

        left_to_right_animation = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        right_to_left_animation = AnimationUtils.loadAnimation(this, R.anim.right_to_left);
        ll_mobile.setVisibility(View.VISIBLE);

        try {
            Bundle extras = getIntent().getExtras();

            type = extras.getString("activity_from");
            back_otp = extras.getString("back_otp");
            if (type.equalsIgnoreCase("login")) {
                email = extras.getString("otp_email");
                id = extras.getString("otp_id");
                login_type = extras.getString("otp_login_type");
                user_name = extras.getString("otp_user_name");
                tv_username.setText("Hi, " + user_name);
                if (!email.equalsIgnoreCase("")) {
                    et_email.setText(email);
                    et_email.setEnabled(false);
                }
            } else if (type.equalsIgnoreCase("edit")) {
                email = extras.getString("otp_email");
                mobile = extras.getString("otp_mobile");
                id = extras.getString("otp_id");
                login_type = extras.getString("otp_login_type");
                user_name = extras.getString("otp_user_name");
                user_address = extras.getString("otp_address");
                user_image = extras.getString("otp_image");

                tv_username.setText("Hi, " + user_name);
                et_email.setText(email);
                et_email.setEnabled(false);
                et_mobile.setText(mobile);
                btn_submit.setText("VERIFY");
                tv_title.setText("OTP Verification");
                ll_mobile.setVisibility(View.GONE);
                ll_otp.setVisibility(View.VISIBLE);
                tv_mobile_edit.setText("+63 - " + mobile);
                cm_timer.setText("00:60");
                timer = new CounterClass(60000, 1000);
                timer.start();
            } else {
                email = extras.getString("otp_email");
                mobile = extras.getString("otp_mobile");
                id = extras.getString("otp_id");
                login_type = extras.getString("otp_login_type");
                user_name = extras.getString("otp_user_name");

                tv_username.setText("Hi, " + user_name);
                et_email.setText(email);
                et_email.setEnabled(false);
                et_mobile.setText(mobile);
                btn_submit.setText("VERIFY");
                tv_title.setText("OTP Verification");
                ll_mobile.setVisibility(View.GONE);
                ll_otp.setVisibility(View.VISIBLE);
                tv_mobile_edit.setText("+63 - " + mobile);
                cm_timer.setText("00:60");
                timer = new CounterClass(60000, 1000);
                timer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        if (back_otp.equalsIgnoreCase("profile")) {
            finish();
        } else if (back_otp.equalsIgnoreCase("login")) {
            Intent intent = new Intent(OTPActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(OTPActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;
            case R.id.tv_resend_otp:
                if (otp_count < 5) {
                    otp_count = otp_count + 1;
                    if (type.equalsIgnoreCase("edit")) {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                resendEditProfileOTP getTask = new resendEditProfileOTP(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                getOTP getTask = new getOTP(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    OTPActivity.this.finish();
                    Toast.makeText(mContext, "You have reached the maximum number of resend otp attempts, Please try after sometime", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_f_login:
                if (ll_mobile.getVisibility() == View.VISIBLE) {
                    if (!et_email.getText().toString().equalsIgnoreCase("") &&
                            !et_mobile.getText().toString().equalsIgnoreCase("")) {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                getOTP getTask = new getOTP(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "All fields are required", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!et_otp.getText().toString().equalsIgnoreCase("")) {
                        if (type.equalsIgnoreCase("edit")) {
                            if (Utility.isNetworkAvaliable(mContext)) {
                                try {
                                    verifyEditProfileOTP getTask = new verifyEditProfileOTP(mContext);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (Utility.isNetworkAvaliable(mContext)) {
                                try {
                                    verifyOTP getTask = new verifyOTP(mContext);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        Toast.makeText(mContext, "Please enter OTP", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.img_edit:
                recreate();
                break;
            default:
                break;
        }
    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            cm_timer.setVisibility(View.GONE);
            ll_resend.setVisibility(View.VISIBLE);
        }

        @SuppressLint("NewApi")
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            cm_timer.setText(hms);
        }
    }

    // getOTP

    public class getOTP extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;

        private final DME_ProgressDilog mProgressDialog;

        public getOTP(final Context mContext) {
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
                    btn_submit.setText("VERIFY");
                    tv_title.setText("OTP Verification");
                    ll_mobile.setVisibility(View.GONE);
                    ll_otp.setVisibility(View.VISIBLE);
                    tv_mobile_edit.setText("+63 - " + et_mobile.getText().toString());
                    cm_timer.setText("00:60");
                    timer = new CounterClass(60000, 1000);
                    timer.start();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_OTP;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("fguid", id));
                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("mobile", et_mobile.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("type", login_type));

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

    // Verify OTP

    public class verifyOTP extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;
        String userid,
                username,
                userprofile,
                email,
                mobile;

        private final DME_ProgressDilog mProgressDialog;

        public verifyOTP(final Context mContext) {
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
                    Utility.writeSharedPreferences(mContext, Constant.USER_ID, userid);
                    Utility.writeSharedPreferences(mContext, "login", "true");
                    Utility.writeSharedPreferences(mContext, Constant.USER_NAME, username);
                    Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, userprofile);
                    Utility.writeSharedPreferences(mContext, Constant.USER_EMAIL, email);
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    finish();
                    Intent intent = new Intent(OTPActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_VERIFY_OTP;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("mobile", et_mobile.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("otp", et_otp.getText().toString()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    userid = jObject.getString("userid");
                    username = jObject.getString("username");
                    userprofile = jObject.getString("userprofile");
                    email = jObject.getString("email");
                    mobile = jObject.getString("mobile");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Verify OTP (Edit Profile)

    public class verifyEditProfileOTP extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;

        private final DME_ProgressDilog mProgressDialog;

        public verifyEditProfileOTP(final Context mContext) {
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
                    OTPActivity.this.finish();
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_SAVE_EDIT_PROFILE;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("user_email", et_email.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("user_mobile", et_mobile.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("otp", et_otp.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("user_image", user_image));
                nameValuePairs.add(new BasicNameValuePair("user_address", user_address));

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

    // Resend OTP (Edit Profile)

    public class resendEditProfileOTP extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;

        private final DME_ProgressDilog mProgressDialog;

        public resendEditProfileOTP(final Context mContext) {
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
                    btn_submit.setText("VERIFY");
                    tv_title.setText("OTP Verification");
                    ll_mobile.setVisibility(View.GONE);
                    ll_otp.setVisibility(View.VISIBLE);
                    tv_mobile_edit.setText("+63 - " + et_mobile.getText().toString());
                    cm_timer.setText("00:60");
                    timer = new CounterClass(60000, 1000);
                    timer.start();
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_RESEND_OTP;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("worker_email", et_email.getText().toString()));
                nameValuePairs.add(new BasicNameValuePair("worker_mobile", et_mobile.getText().toString()));

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
