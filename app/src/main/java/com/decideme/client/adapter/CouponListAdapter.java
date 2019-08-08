package com.decideme.client.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.attributes.Constant;
import com.decideme.client.attributes.DME_ProgressDilog;
import com.decideme.client.attributes.Utility;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vivek.shah on 16-Jan-16.
 */
public class CouponListAdapter extends BaseAdapter {

    private String[] code, description;
    Context context;
    private LayoutInflater inflater = null;

    public CouponListAdapter(Activity context, String[] code, String[] description) {
        this.context = context;
        this.code = code;
        this.description = description;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return code.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return code[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {

        TextView tv_code, tv_description;
        Button btn_apply;

    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        // TODO Auto-generated method stub

        final Holder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_coupon_list_item, null);
            holder = new Holder();

            holder.tv_code = (TextView) convertView.findViewById(R.id.tv_coupon_code);
            holder.tv_description = (TextView) convertView.findViewById(R.id.tv_coupon_description);
            holder.btn_apply = (Button) convertView.findViewById(R.id.btn_coupon_apply);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        holder.tv_code.setText(code[position]);
        holder.tv_description.setText(description[position]);

        holder.btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.isNetworkAvaliable(context)) {
                    try {
                        applyCouponCode getTask = new applyCouponCode(context);
                        getTask.execute(code[position]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return convertView;
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
                getAboutMeListItem(params[0]);
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
                    ((Activity)context).finish();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // ======================================================================================================

        private Object getAboutMeListItem(String code) {
            String webUrl = Constant.URL_APPLY_COUPON;

            try {

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                nameValuePairs.add(new BasicNameValuePair("user_id", Utility.getAppPrefString(mContext, Constant.USER_ID)));
                nameValuePairs.add(new BasicNameValuePair("service_id", Utility.getAppPrefString(mContext, "invoice_s_id")));
                nameValuePairs.add(new BasicNameValuePair("coupon_code", code));

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
}
