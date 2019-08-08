package com.decideme.client.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.adapter.CouponListAdapter;
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

public class CouponActivity extends Activity implements View.OnClickListener {

    private Context mContext;
    private TextView tv_done, tv_coupon_message;
    private EditText et_coupon;
    private ExpandableHeightListView expandableHeightListView;
    private ImageView img_back, img_clear;
    private Button btn_apply;
    private String[] coupon_code, coupon_description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_coupon);

            initUI();

            et_coupon.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 0)
                        btn_apply.setVisibility(View.VISIBLE);
                    else
                        btn_apply.setVisibility(View.GONE);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
    }

    public void initUI() {
        mContext = CouponActivity.this;

        Utility.writeSharedPreferences(mContext, "discount_price", "");
        Utility.writeSharedPreferences(mContext, "hourly_fee", "");
        Utility.writeSharedPreferences(mContext, "wallet_deduction", "");
        Utility.writeSharedPreferences(mContext, "total_amount", "");
        Utility.writeSharedPreferences(mContext, "base_fee", "");
        Utility.writeSharedPreferences(mContext, "tax", "");

        tv_done = (TextView) findViewById(R.id.tv_done);
        tv_done.setOnClickListener(this);
        tv_coupon_message = (TextView) findViewById(R.id.tv_coupon_message);
        et_coupon = (EditText) findViewById(R.id.et_coupon);
        expandableHeightListView = (ExpandableHeightListView) findViewById(R.id.list_coupon);
        img_back = (ImageView) findViewById(R.id.img_back);
        img_back.setOnClickListener(this);
        img_clear = (ImageView) findViewById(R.id.img_clear);
        img_clear.setOnClickListener(this);
        btn_apply = (Button) findViewById(R.id.btn_coupon);
        btn_apply.setOnClickListener(this);

        if (Utility.isNetworkAvaliable(mContext)) {
            try {
                getCouponList getTask = new getCouponList(mContext);
                getTask.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_done:
                finish();
                break;
            case R.id.img_clear:
                if (!et_coupon.getText().toString().equalsIgnoreCase("")) {
                    et_coupon.setText("");
                    tv_coupon_message.setVisibility(View.GONE);
                    btn_apply.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_coupon:
                if (!et_coupon.getText().toString().equalsIgnoreCase("")) {
                    if (Utility.isNetworkAvaliable(mContext)) {
                        try {
                            applyCouponCode getTask = new applyCouponCode(mContext);
                            getTask.execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.img_back:
                finish();
                break;
            default:
                break;
        }
    }

    // Coupon Code API

    public class applyCouponCode extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;
        private final DME_ProgressDilog mProgressDialog;
        String discount_price;
        String hourly_fee;
        String wallet_deduction;
        String total_amount;
        String base_fee;
        String tax;

        public applyCouponCode(final Context mContext) {
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
                    Utility.writeSharedPreferences(mContext, "coupon_flag", "1");
                    Utility.writeSharedPreferences(mContext, "discount_price", discount_price);
                    Utility.writeSharedPreferences(mContext, "hourly_fee", hourly_fee);
                    Utility.writeSharedPreferences(mContext, "wallet_deduction", wallet_deduction);
                    Utility.writeSharedPreferences(mContext, "total_amount", total_amount);
                    Utility.writeSharedPreferences(mContext, "base_fee", base_fee);
                    Utility.writeSharedPreferences(mContext, "tax", tax);
                    Utility.writeSharedPreferences(mContext, "message", message);
                    finish();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_APPLY_COUPON;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "invoice_s_id")));
                nameValuePairs.add(new BasicNameValuePair("coupon_code", et_coupon.getText().toString().trim()));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {
                    discount_price = jObject.getString("discount_price");
                    hourly_fee = jObject.getString("hourly_fee");
                    wallet_deduction = jObject.getString("wallet_deduction");
                    total_amount = jObject.getString("total_amount");
                    base_fee = jObject.getString("base_fee");
                    tax = jObject.getString("tax");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class getCouponList extends AsyncTask<String, Integer, Object> {

        private final Context mContext;
        String response;
        String message;
        private final DME_ProgressDilog mProgressDialog;
        JSONArray jsonArray;
        JSONObject jsonObjectMessage;

        public getCouponList(final Context mContext) {
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
                    expandableHeightListView.setAdapter(new CouponListAdapter(CouponActivity.this, coupon_code,coupon_description));
                } else {
                    Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem() {
            String webUrl = Constant.URL_GET_COUPON_LIST;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));

                String response1 = Utility.postRequest(mContext, webUrl, nameValuePairs);

                JSONObject jObject = new JSONObject(response1);

                Log.v("response", jObject.toString() + "");

                response = jObject.getString("response");
                message = jObject.getString("message");

                if (response.equalsIgnoreCase("true")) {

                    jsonArray = jObject.getJSONArray("coupon_data");
                    int lenth = jsonArray.length();

                    coupon_code = new String[lenth];
                    coupon_description = new String[lenth];

                    for (int a = 0; a < lenth; a++) {
                        jsonObjectMessage = jsonArray.getJSONObject(a);

                        try {
                            coupon_code[a] = jsonObjectMessage.getString("coupon_code");
                            coupon_description[a] = jsonObjectMessage.getString("coupon_description");
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
