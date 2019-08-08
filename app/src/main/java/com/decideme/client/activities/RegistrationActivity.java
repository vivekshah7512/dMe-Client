package com.decideme.client.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.PasswordValidator;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrationActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private EditText et_name, et_name1, et_email, et_mobile, et_pass, et_confirm_pass;
    private Button btn_sign_up;
    private TextView tv_login, tv_terms;
    private PasswordValidator passwordValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_registration);

        initUI();

    }

    private void initUI() {

        mContext = RegistrationActivity.this;
        passwordValidator = new PasswordValidator();

        et_name = (EditText) findViewById(R.id.et_reg_first_name);
        et_name1 = (EditText) findViewById(R.id.et_reg_last_name);
        et_email = (EditText) findViewById(R.id.et_reg_email);
        et_mobile = (EditText) findViewById(R.id.et_reg_mobile);
        et_pass = (EditText) findViewById(R.id.et_reg_pass);
        et_confirm_pass = (EditText) findViewById(R.id.et_reg_confirm_pass);
        btn_sign_up = (Button) findViewById(R.id.btn_register);
        btn_sign_up.setOnClickListener(this);
        tv_login = (TextView) findViewById(R.id.tv_register_login);
        tv_login.setOnClickListener(this);
        tv_terms = (TextView) findViewById(R.id.tv_terms);
        tv_terms.setOnClickListener(this);

        String styledText = "<u><font color='white'>Terms & Privacy Policy</font></u>.";
        tv_terms.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                if (et_name.getText().toString().equalsIgnoreCase("") ||
                        et_name1.getText().toString().equalsIgnoreCase("") ||
                        et_email.getText().toString().equalsIgnoreCase("") ||
                        et_mobile.getText().toString().equalsIgnoreCase("") ||
                        et_pass.getText().toString().equalsIgnoreCase("") ||
                        et_confirm_pass.getText().toString().equalsIgnoreCase("")) {
                    if (et_name.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your first name", Toast.LENGTH_SHORT).show();
                    } else if (et_name1.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your last name", Toast.LENGTH_SHORT).show();
                    } else if (et_email.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your email id", Toast.LENGTH_SHORT).show();
                    } else if (et_mobile.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter your mobile no.", Toast.LENGTH_SHORT).show();
                    } else if (et_pass.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_SHORT).show();
                    } else if (et_confirm_pass.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please enter confirm password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (Utility.isValidMobile(et_mobile.getText().toString()) == true &&
                            et_mobile.getText().toString().length() == 10) {
                        if (Utility.isValidMail(et_email.getText().toString()) == true) {
                            if (passwordValidator.validate(et_pass.getText().toString())) {
                                if (!et_pass.getText().toString().equals(et_confirm_pass.getText().toString())) {
                                    Toast.makeText(getApplicationContext(), "Your password does not match", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (Utility.isNetworkAvaliable(mContext)) {
                                        try {
                                            saveRegistrationDetails getTask = new saveRegistrationDetails(mContext);
                                            getTask.execute();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter password between 8 to 15 characters", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Email ID is not valid", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Mobile number is not valid", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case R.id.tv_register_login:
                RegistrationActivity.this.finish();
                Intent intent_login = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent_login);
                break;
            case R.id.tv_terms:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.decidemejob.com/index.php/terms-and-conditions"));
                startActivity(browserIntent);
                break;
            default:
                break;
        }
    }

    public class saveRegistrationDetails extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, user_id;

        public saveRegistrationDetails(final Context mContext) {
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
                    finish();
                    Utility.writeSharedPreferences(mContext, "authentication_type", "authorized");
                    Utility.writeSharedPreferences(mContext, "username", "");
                    Utility.writeSharedPreferences(mContext, "password", "");
                    Utility.writeSharedPreferences(mContext, "check_flag", "false");
                    Intent intent = new Intent(RegistrationActivity.this, OTPActivity.class);
                    intent.putExtra("activity_from", "reg");
                    intent.putExtra("otp_id", user_id);
                    intent.putExtra("otp_email", et_email.getText().toString().trim());
                    intent.putExtra("otp_login_type", "normal");
                    intent.putExtra("otp_user_name", et_name.getText().toString().trim());
                    intent.putExtra("otp_mobile", et_mobile.getText().toString().trim());
                    intent.putExtra("back_otp", "registration");
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_REGISTRATION;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("full_name", et_name.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("last_name", et_name1.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("mobile", et_mobile.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("lat", "0.0"));
                nameValuePairs.add(new BasicNameValuePair("longitude", "0.0"));
                nameValuePairs.add(new BasicNameValuePair("password", et_pass.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("confirm_password", et_confirm_pass.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));
                nameValuePairs.add(new BasicNameValuePair("deviceToken", Utility.getAppPrefString(mContext, "device_token")));
                nameValuePairs.add(new BasicNameValuePair("device", "android"));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    user_id = jObject.getString("user_id");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
