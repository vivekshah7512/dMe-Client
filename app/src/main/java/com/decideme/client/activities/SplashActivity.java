package com.decideme.client.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends Activity {

    private Context mContext;
    private TelephonyManager telephonyManager;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        mContext = this;

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkAndRequestPermissions()) {
                initUI();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Utility.getAppPrefString(mContext, "login").equalsIgnoreCase("true")) {
                            if (Utility.isNetworkAvaliable(mContext)) {
                                try {
                                    getCurrentData getTask = new getCurrentData(mContext);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SplashActivity.this.finish();
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                }, 1200);
            }
        } else {
            initUI();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Utility.getAppPrefString(mContext, "login").equalsIgnoreCase("true")) {
                        if (Utility.isNetworkAvaliable(mContext)) {
                            try {
                                getCurrentData getTask = new getCurrentData(mContext);
                                getTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }, 1200);
        }
    }

    @SuppressLint("MissingPermission")
    private void initUI() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Utility.writeSharedPreferences(mContext, "device_id", telephonyManager.getDeviceId());
        Utility.writeSharedPreferences(mContext, "activity_from", "activity");
        Utility.writeSharedPreferences(getApplicationContext(), "noti_flag", "false");

        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.decideme.client",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }*/

    }

    private boolean checkAndRequestPermissions() {
        int readPhoneState = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        int finePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int crossPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readPhoneState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (finePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (crossPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Permission", "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(android.Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        recreate();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                android.Manifest.permission.READ_PHONE_STATE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            Utility.showDialogOK(mContext, "Phone, Location, Storage Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }

    public class getCurrentData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message, type;
        private final DME_ProgressDilog mProgressDialog;
        String service_type, current_flag, service_id;

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
//                if (type.equalsIgnoreCase("authorized")) {
                Utility.writeSharedPreferences(mContext,"authentication_type",type);
                if (response.equalsIgnoreCase("true")) {
                    if (service_type.equalsIgnoreCase("schedual")) {
                        Utility.writeSharedPreferences(mContext, "schedule", "true");
                        Utility.writeSharedPreferences(mContext, "process_type", "schedule");
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, ScheduleActivity.class);
                        startActivity(intent);
                    } else if (current_flag.equalsIgnoreCase("finish")) {
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, InvoiceActivity.class);
                        Utility.writeSharedPreferences(mContext, "invoice_s_id", service_id);
                        Utility.writeSharedPreferences(mContext, "sc_flag", "0");
                        Utility.writeSharedPreferences(mContext, "coupon_flag", "0");
                        Utility.writeSharedPreferences(mContext, "process_type", "");
                        startActivity(intent);
                    } else {
                        Utility.writeSharedPreferences(mContext, "data_flag", "true");
                        Utility.writeSharedPreferences(mContext, "schedule", "false");
                        Utility.writeSharedPreferences(mContext, "process_type", "normal");
                        SplashActivity.this.finish();
                        Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                } else {
                    Utility.writeSharedPreferences(mContext, "data_flag", "false");
                    SplashActivity.this.finish();
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    Utility.writeSharedPreferences(mContext, "authentication_type", type);
                    startActivity(intent);
                }
                /*} else {
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
                }*/
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
