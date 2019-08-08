package com.decideme.client.activities;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 007;
    public static GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private EditText et_username, et_password;
    private Button btn_login;
    private RelativeLayout btn_facebook, btn_google;
    private TextView tv_forgot_pass, tv_register;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private LoginButton loginButton;
    private SignInButton btnSignIn;
    private Dialog dialog;
    private EditText et_email;
    private CheckBox cb_remember;
    private String username, password;

    private String fg_id, fg_name, fg_profile_pic, fg_email, login_type;

    public static void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        initUI();
        initParameters();
        initViews();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this,
                new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        Log.e("newToken", newToken);
                        Utility.writeSharedPreferences(mContext, "device_token", newToken);
                    }
                });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null) {
                    Log.d("FB", "User logged out successfully");
                }
            }
        };
    }

    public void initParameters() {
        accessToken = AccessToken.getCurrentAccessToken();
        callbackManager = CallbackManager.Factory.create();

    }

    public void initViews() {

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(new String[]{"email", "user_birthday", "user_hometown"}));

        if (accessToken != null) {
            getProfileData();
        } else {
//            rlProfileArea.setVisibility(View.GONE);
        }

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FB", "User login successfully");
                getProfileData();
            }

            @Override
            public void onCancel() {
                // App code
                Log.d("FB", "User cancel login");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("FB", "Problem for login");
            }
        });
    }

    public void initUI() {
        mContext = LoginActivity.this;

        cb_remember = (CheckBox) findViewById(R.id.check_remember);
        et_username = (EditText) findViewById(R.id.et_login_username);
        et_password = (EditText) findViewById(R.id.et_login_password);
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_facebook = (RelativeLayout) findViewById(R.id.btn_login_facebook);
        btn_facebook.setOnClickListener(this);
        btn_google = (RelativeLayout) findViewById(R.id.btn_login_google);
        btn_google.setOnClickListener(this);
        tv_forgot_pass = (TextView) findViewById(R.id.tv_forgot_password);
        tv_forgot_pass.setOnClickListener(this);
        tv_register = (TextView) findViewById(R.id.tv_login_register);
        tv_register.setOnClickListener(this);

        tv_register.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        if (Utility.getAppPrefString(mContext, "check_flag").equalsIgnoreCase("true")) {
            et_username.setText(Utility.getAppPrefString(mContext, "username"));
            et_password.setText(Utility.getAppPrefString(mContext, "password"));
            cb_remember.setChecked(true);
        } else {
            cb_remember.setChecked(false);
        }

        btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Google", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.e("Google", "display name: " + acct.getDisplayName());
            String personName = null, personPhotoUrl = null, email = null, id = null;

            if (acct != null) {
                personName = acct.getDisplayName();
                if (acct.getPhotoUrl() != null) {
                    personPhotoUrl = acct.getPhotoUrl().toString();
                }
                email = acct.getEmail();
                id = acct.getId();
            }


            fg_id = id;
            fg_email = email;
            fg_name = personName;
            fg_profile_pic = personPhotoUrl;
            login_type = "google";

            Utility.writeSharedPreferences(mContext, "login_type", login_type);
            Utility.writeSharedPreferences(mContext, Constant.USER_NAME, personName);
            Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, personPhotoUrl);
            Log.e("Google", "Name: " + personName + ", email: " + email
                    + ", Image: " + personPhotoUrl);

            if (Utility.isNetworkAvaliable(mContext)) {
                try {
                    createLogin getTask = new createLogin(mContext);
                    getTask.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign_in:
                btnSignIn.performClick();
                break;
            case R.id.btn_login:
                fg_id = "";
                fg_email = "";
                fg_name = "";
                fg_profile_pic = "";
                login_type = "normal";

                username = et_username.getText().toString();
                password = et_password.getText().toString();

                if (et_username.getText().toString().equalsIgnoreCase("") || et_password.getText().toString().equalsIgnoreCase("")) {
                    if (et_username.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(mContext, "Please enter your email id", Toast.LENGTH_SHORT).show();
                    } else if (et_password.getText().toString().equalsIgnoreCase("")) {
                        Toast.makeText(mContext, "Please enter your password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (cb_remember.isChecked()) {
                        Utility.writeSharedPreferences(mContext, "username", username);
                        Utility.writeSharedPreferences(mContext, "password", password);
                        Utility.writeSharedPreferences(mContext, "check_flag", "true");
                    } else {
                        Utility.writeSharedPreferences(mContext, "username", "");
                        Utility.writeSharedPreferences(mContext, "password", "");
                        Utility.writeSharedPreferences(mContext, "check_flag", "false");
                    }
                    Utility.writeSharedPreferences(mContext, "login_type", login_type);
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            createLogin getTask = new createLogin(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.btn_login_facebook:
                loginButton.performClick();
                break;
            case R.id.btn_login_google:
                signIn();
                break;
            case R.id.tv_forgot_password:
                dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_forgot_password);

                Button btn_send = (Button) dialog.findViewById(R.id.btn_forgot_send);
                ImageView img_close = (ImageView) dialog.findViewById(R.id.img_forgot_close);
                et_email = (EditText) dialog.findViewById(R.id.et_forgot_email);

                img_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btn_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!et_email.getText().toString().equalsIgnoreCase("")) {
                            if (Utility.isValidMail(et_email.getText().toString()) == true) {

                                if (Utility.isNetworkAvaliable(mContext)) {
                                    try {
                                        sendForgotPasswordLink getTask = new sendForgotPasswordLink(mContext);
                                        getTask.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Email id is not valid", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Please enter email id", Toast.LENGTH_SHORT).show();
                        }
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
            case R.id.tv_login_register:
                Intent intent_register = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent_register);
                break;
            default:
                break;
        }
    }

    public void getProfileData() {
        try {
            accessToken = AccessToken.getCurrentAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            Log.d("FB", "Graph Object :" + object);
                            try {
                                fg_id = object.getString("id");
                                if (object.has("email")) {
                                    fg_email = object.getString("email");
                                } else {
                                    fg_email = "";
                                }

                                fg_name = object.getString("name");
                                fg_profile_pic = "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large";
                                login_type = "facebook";

                                Utility.writeSharedPreferences(mContext, "login_type", login_type);
                                Utility.writeSharedPreferences(mContext, Constant.USER_NAME, object.getString("name"));
                                Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, "https://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                                if (Utility.isNetworkAvaliable(mContext)) {
                                    try {
                                        createLogin getTask = new createLogin(mContext);
                                        getTask.execute();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,birthday,gender,email");
            request.setParameters(parameters);
            request.executeAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class sendForgotPasswordLink extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message;

        public sendForgotPasswordLink(final Context mContext) {
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
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_FORGOT_PASSWORD;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("email", et_email.getText().toString().trim()));

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

    public class createLogin extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response, message, userid, username, userprofile, email, mobile, type;

        public createLogin(final Context mContext) {
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

                    Utility.writeSharedPreferences(mContext, "login_type", login_type);
                    Utility.writeSharedPreferences(mContext, Constant.USER_ID, userid);
                    Utility.writeSharedPreferences(mContext, "login", "true");
                    Utility.writeSharedPreferences(mContext, Constant.USER_NAME, username);
                    Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, userprofile);
                    Utility.writeSharedPreferences(mContext, Constant.USER_EMAIL, email);
                    Utility.writeSharedPreferences(mContext, Constant.USER_MOBILE, mobile);

                    /*if(login_type.equalsIgnoreCase("facebook")||
                            login_type.equalsIgnoreCase("google")){
                        finish();
                        Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                        intent.putExtra("activity_from", "login");
                        intent.putExtra("otp_id", fg_id);
                        intent.putExtra("otp_email", fg_email);
                        intent.putExtra("otp_login_type", login_type);
                        intent.putExtra("otp_user_name", fg_name);
                        startActivity(intent);
                    } else {*/
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            getCurrentData getTask = new getCurrentData(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                    }

                } else {
                    if (type.equalsIgnoreCase("facebook") ||
                            type.equalsIgnoreCase("google")) {
                        finish();
                        Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
                        intent.putExtra("activity_from", "login");
                        intent.putExtra("otp_id", fg_id);
                        intent.putExtra("otp_email", fg_email);
                        intent.putExtra("otp_login_type", login_type);
                        intent.putExtra("otp_user_name", fg_name);
                        intent.putExtra("back_otp", "login");
                        startActivity(intent);
                    } else {
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_LOGIN;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("username", et_username.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("password", et_password.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("fguid", fg_id));
                nameValuePairs.add(new BasicNameValuePair("full_name", fg_name));
                nameValuePairs.add(new BasicNameValuePair("userprofile", fg_profile_pic));
                nameValuePairs.add(new BasicNameValuePair("email", fg_email));
                nameValuePairs.add(new BasicNameValuePair("type", login_type));
                nameValuePairs.add(new BasicNameValuePair("deviceID", Utility.getAppPrefString(mContext, "device_id")));
                nameValuePairs.add(new BasicNameValuePair("deviceToken", Utility.getAppPrefString(mContext, "device_token")));
                nameValuePairs.add(new BasicNameValuePair("device", "android"));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    userid = jObject.getString("user_id");
                    username = jObject.getString("username");
                    userprofile = jObject.getString("userprofile");
                    email = jObject.getString("email");
                    mobile = jObject.getString("mobile");
                } else {
                    type = jObject.getString("type");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response, message;
        String service_type, current_flag, service_id, type;

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

            try {
                Utility.writeSharedPreferences(mContext, "authentication_type", type);
                if (type.equalsIgnoreCase("authorized")) {
                    if (response.equalsIgnoreCase("true")) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        if (service_type.equalsIgnoreCase("schedual")) {
                            Utility.writeSharedPreferences(mContext, "schedule", "true");
                            finish();
                            Intent intent = new Intent(LoginActivity.this, ScheduleActivity.class);
                            startActivity(intent);
                        } else if (current_flag.equalsIgnoreCase("finish")) {
                            finish();
                            Intent intent = new Intent(LoginActivity.this, InvoiceActivity.class);
                            Utility.writeSharedPreferences(mContext, "invoice_s_id", service_id);
                            Utility.writeSharedPreferences(mContext, "sc_flag", "0");
                            Utility.writeSharedPreferences(mContext, "coupon_flag", "0");
                            startActivity(intent);
                        } else {
                            Utility.writeSharedPreferences(mContext, "data_flag", "true");
                            Utility.writeSharedPreferences(mContext, "schedule", "false");
                            finish();
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "false");
                        finish();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
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
                    service_type = jObject.getString("service_type");
                    current_flag = jObject.getString("current_flag");
                    service_id = jObject.getString("service_id");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
