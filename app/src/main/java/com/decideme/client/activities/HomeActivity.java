package com.decideme.client.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.GPSTracker1;
import com.decideme.client.attributes.Utility;
import com.decideme.client.fragments.Fragment_Coupon;
import com.decideme.client.fragments.Fragment_Help;
import com.decideme.client.fragments.Fragment_Home;
import com.decideme.client.fragments.Fragment_Profile;
import com.decideme.client.fragments.Fragment_Service_History;
import com.decideme.client.fragments.Fragment_Wallet;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.plus.Plus;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String BACK_STACK_ROOT_TAG = "main_fragment";
    public static ImageView img_profile;
    public static TextView tv_name;
    public static TextView tv_city;
    GPSTracker1 gpsTracker1;
    double latitude;
    double longitude;
    Handler handler;
    Runnable runnable;
    FrameLayout fl_fragment;
    Fragment fr = null;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private NavigationView navigationView;
    private View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.post(new Runnable() {
            @Override
            public void run() {
                Drawable d = ResourcesCompat.getDrawable(getResources(), R.mipmap.menu, null);
                toolbar.setNavigationIcon(d);
            }
        });

        mContext = this;
        gpsTracker1 = new GPSTracker1(HomeActivity.this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

            initUI();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .enableAutoManage(HomeActivity.this  /* FragmentActivity */, HomeActivity.this  /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .addApi(Plus.API)
                    .build();
            if (!Utility.getAppPrefString(mContext, Constant.USER_IMAGE).equalsIgnoreCase("")) {
                Glide.with(mContext).load(Utility.getAppPrefString(mContext, Constant.USER_IMAGE))
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_profile);
            }
            navigationView.setNavigationItemSelectedListener(this);
    }

    private void initUI() {

        fl_fragment = (FrameLayout) findViewById(R.id.frame);
        fr = new Fragment_Home();
        if (fr != null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.frame, fr);
            fragmentTransaction.commit();
        }

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        img_profile = (ImageView) headerView.findViewById(R.id.img_menu_profile);
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fr = new Fragment_Profile();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                    fragmentTransaction.commit();
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        tv_name = (TextView) headerView.findViewById(R.id.tv_menu_user_name);
        tv_name.setText(Utility.getAppPrefString(mContext, Constant.USER_NAME));
        tv_city = (TextView) headerView.findViewById(R.id.tv_menu_user_city);
        tv_city.setText(Utility.getAppPrefString(mContext, Constant.USER_EMAIL));

        if (Utility.getAppPrefString(mContext, "authentication_type").equalsIgnoreCase("authorized")) {

            if (gpsTracker1.canGetLocation()) {
                latitude = gpsTracker1.getLatitude();
                longitude = gpsTracker1.getLongitude();
                if (!String.valueOf(latitude).matches("0.0")) {
                    try {
                        Log.v("Lat/Long: ", latitude + "\n" + longitude);
                        Utility.writeSharedPreferences(HomeActivity.this, "latitude", latitude + "");
                        Utility.writeSharedPreferences(HomeActivity.this, "longitude", longitude + "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        latitude = gpsTracker1.getLatitude();
                                        longitude = gpsTracker1.getLongitude();
                                        if (!String.valueOf(latitude).matches("0.0")) {
                                            handler.removeCallbacks(runnable);
                                            onResume();
                                        } else {
                                            handler.postDelayed(runnable, 1000);
                                            gpsTracker1 = new GPSTracker1(HomeActivity.this);
                                        }
                                    }
                                });
                            }
                        };
                        handler.postDelayed(runnable, 1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                settingAlert();
            }

            if (Utility.getAppPrefString(mContext, "sc_accept").equalsIgnoreCase("true")) {
                fr = new Fragment_Service_History();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                    fragmentTransaction.commit();
                }
            } else if (Utility.getAppPrefString(mContext, "coupon_flag").equalsIgnoreCase("true")) {
                fr = new Fragment_Coupon();
                if (fr != null) {
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    fragmentTransaction.replace(R.id.frame, fr);
                    fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                    fragmentTransaction.commit();
                }
            }
        } else {
            Utility.writeSharedPreferences(mContext, "login", "false");
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Authentication Error");
            alertDialog.setMessage("Authentication error occur. Please login again.");
            alertDialog.setCancelable(false);

            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    LoginManager.getInstance().logOut();
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int backStackCount = getSupportFragmentManager().getBackStackEntryCount();

            if (backStackCount >= 1) {
                getSupportFragmentManager().popBackStack();
                // Change to hamburger icon if at bottom of stack
                if (backStackCount == 1) {
                    finishAffinity();
                    System.exit(0);
                }
            } else {
                super.onBackPressed();
                navigationView.getMenu().getItem(0).setChecked(true);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        gpsTracker1.stopUsingGPS();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker1.stopUsingGPS();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to logout?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (Utility.isNetworkAvaliable(HomeActivity.this)) {
                                try {
                                    logoutUser getTask = new logoutUser(HomeActivity.this);
                                    getTask.execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (id == R.id.nav_payment) {
            fr = new Fragment_Wallet();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_history) {
            fr = new Fragment_Service_History();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out the App at: \n" +
                    "https://play.google.com/store/apps/details?id=com.decideme.client");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_coupon) {
            fr = new Fragment_Coupon();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_home) {
            getFragmentManager().popBackStack();
        } else if (id == R.id.nav_help) {
            fr = new Fragment_Help();
            if (fr != null) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack(BACK_STACK_ROOT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.frame, fr);
                fragmentTransaction.addToBackStack(BACK_STACK_ROOT_TAG);
                fragmentTransaction.commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gpsTracker1.canGetLocation()) {
            latitude = gpsTracker1.getLatitude();
            longitude = gpsTracker1.getLongitude();

            if (!String.valueOf(latitude).matches("0.0")) {

                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    latitude = gpsTracker1.getLatitude();
                                    longitude = gpsTracker1.getLongitude();
                                    Utility.writeSharedPreferences(HomeActivity.this, "latitude", latitude + "");
                                    Utility.writeSharedPreferences(HomeActivity.this, "longitude", longitude + "");
                                    Log.v("Lat/Long: ", latitude + "\n" + longitude);
                                    if (!String.valueOf(latitude).matches("0.0")) {
                                        handler.removeCallbacks(runnable);
                                        onResume();
                                    } else {
                                        handler.postDelayed(runnable, 1000);
                                        gpsTracker1 = new GPSTracker1(HomeActivity.this);
                                    }
                                }
                            });

                        }
                    };
                    handler.postDelayed(runnable, 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            settingAlert();
        }
    }

    public void settingAlert() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(HomeActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(1 * 1000);
            locationRequest.setFastestInterval(1 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            /*if (mProgressDialog1 != null) {
                                mProgressDialog1.dismiss();
                            }*/
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(HomeActivity.this, 1);
                            } catch (IntentSender.SendIntentException e) {
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    public class logoutUser extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;

        public logoutUser(final Context mContext) {
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
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                Utility.writeSharedPreferences(mContext, "login", "false");
                Utility.writeSharedPreferences(mContext, Constant.USER_ID, "");
                Utility.writeSharedPreferences(mContext, Constant.USER_NAME, "");
                Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, "");
                if (Utility.getAppPrefString(mContext, "login_type").equalsIgnoreCase("google")) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                            new ResultCallback<com.google.android.gms.common.api.Status>() {
                                @Override
                                public void onResult(com.google.android.gms.common.api.Status status) {
                                    HomeActivity.this.finish();
                                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });
                } else if (Utility.getAppPrefString(mContext, "login_type").equalsIgnoreCase("facebook")) {
                    LoginManager.getInstance().logOut();
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    HomeActivity.this.finish();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_LOGOUT_USER;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
