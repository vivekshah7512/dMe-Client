package com.decideme.client.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decideme.client.R;
import com.decideme.client.activities.HomeActivity;
import com.decideme.client.activities.OTPActivity;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek_shah on 9/11/17.
 */
public class Fragment_Profile extends Fragment implements View.OnClickListener {

    public static String base64Image;
    public static String path;
    private static int RESULT_LOAD_IMAGE = 1021;
    View view;
    Uri imageUri;
    File file;
    private ImageView img_profile;
    private EditText et_email, et_mobile, et_address;
    private RatingBar ratingBar;
    private TextView tv_total_rating, tv_name;
    private RelativeLayout rl_profile_change;
    private Button btn_submit, btn_edit;
    private ImageView img_edit;
    private String filePath = "";
    private int PERMISSION_REQUEST_CODE = 1;
    private String type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = inflater.inflate(R.layout.fragment_profile,
                container, false);

            init();

        return view;
    }

    public void init() {

        img_profile = (ImageView) view.findViewById(R.id.img_profile);
        et_email = (EditText) view.findViewById(R.id.et_profile_email);
        et_mobile = (EditText) view.findViewById(R.id.et_profile_mobile);
        et_address = (EditText) view.findViewById(R.id.et_profile_address);
        ratingBar = (RatingBar) view.findViewById(R.id.rating_profile);
        tv_total_rating = (TextView) view.findViewById(R.id.tv_profile_rating_total);
        tv_name = (TextView) view.findViewById(R.id.tv_profile_name);
        rl_profile_change = (RelativeLayout) view.findViewById(R.id.rl_profile_edit);
        rl_profile_change.setOnClickListener(this);

        btn_submit = (Button) view.findViewById(R.id.btn_profile_update);
        btn_submit.setOnClickListener(this);
        btn_edit = (Button) view.findViewById(R.id.btn_profile_edit);
        btn_edit.setOnClickListener(this);
        img_edit = (ImageView) view.findViewById(R.id.img_address_edit);
        img_edit.setOnClickListener(this);

        if (Utility.isNetworkAvaliable(getActivity())) {
            try {
                getData getTask = new getData(getActivity());
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_profile_edit:
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
                break;
            case R.id.btn_profile_update:
                if (et_address.getText().toString().equalsIgnoreCase("")) {
                    Toast.makeText(getActivity(), "Please enter your address", Toast.LENGTH_SHORT).show();
                } else {
                    if (Utility.isNetworkAvaliable(getActivity())) {
                        try {
                            saveData getTask = new saveData(getActivity());
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.btn_profile_edit:
                et_address.setEnabled(true);
                et_mobile.setEnabled(true);
                et_email.setEnabled(true);
                rl_profile_change.setVisibility(View.VISIBLE);
                btn_submit.setVisibility(View.VISIBLE);
                btn_edit.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMAGE) {
                Bitmap thumbnail = getPath(data.getData());
                base64Image = encodeTobase64(Bitmap.createScaledBitmap(
                        thumbnail, thumbnail.getWidth(), thumbnail.getHeight(), false));
                Uri yourUri = Uri.fromFile(file);
                Glide.with(this).load(yourUri)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                        .thumbnail(0.5f)
                        .into(img_profile);
            }
        }
    }

    private Bitmap getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        file = new File(filePath);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        return bitmap;
    }

    public String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        immagex.setDensity(100);

        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public class getData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, username,
                userprofile,
                email,
                mobile,
                address,
                rating,
                total_review;

        public getData(final Context mContext) {
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
                    tv_name.setText(username);
                    tv_total_rating.setText("(" + total_review + ")");
                    ratingBar.setRating(Float.parseFloat(rating));

                    if (mobile.equalsIgnoreCase("null") || mobile.equalsIgnoreCase("")) {
                        et_mobile.setText("");
                    } else {
                        et_mobile.setText(mobile);
                    }
                    if (email.equalsIgnoreCase("null") || email.equalsIgnoreCase("")) {
                        et_email.setText("");
                    } else {
                        et_email.setText(email);
                    }
                    if (address.equalsIgnoreCase("null") || address.equalsIgnoreCase("")) {
                        et_address.setText("");
                    } else {
                        et_address.setText(address);
                    }

                    if (!userprofile.equalsIgnoreCase("")) {
                        Glide.with(mContext).load(userprofile)
                                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                .thumbnail(0.5f)
                                .into(img_profile);
                    }

                    // Side Menu
                    Utility.writeSharedPreferences(mContext, Constant.USER_NAME, username);
                    Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, userprofile);
                    Utility.writeSharedPreferences(mContext, Constant.USER_EMAIL, email);

                    HomeActivity.tv_name.setText(username);
                    HomeActivity.tv_city.setText(email);

                    if (!userprofile.equalsIgnoreCase("")) {
                        Glide.with(getActivity()).load(userprofile)
                                .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                .thumbnail(0.5f)
                                .into(HomeActivity.img_profile);
                    }

                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_PROFILE;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    username = jObject.getString("user_name");
                    userprofile = jObject.getString("user_image");
                    email = jObject.getString("user_email");
                    mobile = jObject.getString("user_mobile");
                    address = jObject.getString("user_address");
                    rating = jObject.getString("user_rating");
                    total_review = jObject.getString("user_total_review");
                    type = jObject.getString("type");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class saveData extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        private final DME_ProgressDilog mProgressDialog;
        String response;
        String message, otp, profile_url;

        public saveData(final Context mContext) {
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

                    HomeActivity.tv_name.setText(tv_name.getText().toString());
                    HomeActivity.tv_city.setText(et_email.getText().toString());
                    HomeActivity.img_profile.setImageURI(Uri.parse(profile_url));

                    Utility.writeSharedPreferences(mContext, Constant.USER_IMAGE, profile_url);

                    try {
                        if (profile_url != null || profile_url.equals("")) {
                            Glide.with(getActivity()).load(profile_url)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(HomeActivity.img_profile);
                            Glide.with(getActivity()).load(profile_url)
                                    .apply(RequestOptions.circleCropTransform().placeholder(R.mipmap.user).error(R.mipmap.user))
                                    .thumbnail(0.5f)
                                    .into(img_profile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (otp.equalsIgnoreCase("false")) {
                        et_address.setEnabled(false);
                        et_mobile.setEnabled(false);
                        et_email.setEnabled(false);
                        rl_profile_change.setVisibility(View.GONE);
                        btn_submit.setVisibility(View.GONE);
                        btn_edit.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        et_address.setEnabled(false);
                        et_mobile.setEnabled(false);
                        et_email.setEnabled(false);
                        rl_profile_change.setVisibility(View.GONE);
                        btn_submit.setVisibility(View.GONE);
                        btn_edit.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(getActivity(), OTPActivity.class);
                        intent.putExtra("activity_from", "edit");
                        intent.putExtra("otp_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID));
                        intent.putExtra("otp_email", et_email.getText().toString().trim());
                        intent.putExtra("otp_login_type", type);
                        intent.putExtra("otp_user_name", tv_name.getText().toString().trim());
                        intent.putExtra("otp_mobile", et_mobile.getText().toString().trim());
                        intent.putExtra("back_otp", "profile");

                        intent.putExtra("otp_address", et_address.getText().toString().trim());
                        intent.putExtra("otp_image", base64Image);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_SAVE_PROFILE;
            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(getActivity(), Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("user_image", base64Image));
                nameValuePairs.add(new BasicNameValuePair("user_address", et_address.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("user_name", tv_name.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("user_email", et_email.getText().toString().trim()));
                nameValuePairs.add(new BasicNameValuePair("user_mobile", et_mobile.getText().toString().trim()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);
                JSONObject jObject = new JSONObject(response1);
                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");
                if (response.equalsIgnoreCase("true")) {
                    otp = jObject.getString("otp");
                    profile_url = jObject.getString("profile_url");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
