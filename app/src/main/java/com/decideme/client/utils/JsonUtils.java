package com.decideme.client.utils;

import com.decideme.client.model.Payments;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by jadeantolingaa on 2/23/16.
 */
public class JsonUtils {

    public static Payments fromJSONPayments(String json) throws JSONException {
        JSONObject root = new JSONObject(json);

        Payments payments = new Payments();
        payments.setId(root.getString("id"));
        payments.setEnvironment(root.getString("environment"));
        payments.setStatus(root.getString("status"));
        payments.setPaymentTokenId(root.getString("paymentTokenId"));
        payments.setDescription(root.getString("description"));

        return payments;
    }
}
