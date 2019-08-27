package com.decideme.client.attributes;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.decideme.client.R;
import com.decideme.client.activities.HomeActivity;
import com.decideme.client.activities.LoginActivity;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utility {

    // private Activity activity = null;
    private Context context;

    public Utility(Context context) {
        this.context = context;
    }

    public static void toast(String toastMessage, Context context) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public static void snackBarShow(View container, String msg) {
        Snackbar.make(container, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void writeSharedPreferences(Context mContext, String key,
                                              String value) {
        try {
            SharedPreferences settings = mContext.getSharedPreferences(
                    Constant.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(key, value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAppPrefString(Context mContext, String key) {
        try {
            SharedPreferences settings = mContext.getSharedPreferences(
                    Constant.PREFS_NAME, 0);
            String value = settings.getString(key, "");
            return value;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static Bitmap decodeFile(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 250;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    /*public static void internetErrorDialogWithTitle(final Activity context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_internet);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(wlp);

        dialog.show();

    }*/

    /*public static void errorDialogWithTitle(String title, String message,
                                            final Activity context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alert_dialog);
        dialog.setCanceledOnTouchOutside(false);
        Button btnOkay = (Button) dialog.findViewById(R.id.dialog_ok);
        TextView tv_message = (TextView) dialog
                .findViewById(R.id.dialog_description);
        TextView tv_title = (TextView) dialog.findViewById(R.id.dialog_title);
        tv_title.setText(title);
        tv_message.setText(message);
        btnOkay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        if (dialog.isShowing()) {
        } else {
            dialog.show();
        }
    }*/


    public static String getCurrentDateTime() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy/dd/mm");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String getCurrentDateTime1() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static boolean isNetworkAvaliable(final Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            final Dialog dialog = new Dialog(ctx);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.layout_no_internet);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            Button btn_reload = (Button) dialog.findViewById(R.id.btn_try_again);
            btn_reload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (Utility.getAppPrefString(ctx, "login")
                            .equalsIgnoreCase("true")) {
                        Intent intent = new Intent(ctx, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ctx.startActivity(intent);
                    } else {
                        Intent intent = new Intent(ctx, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ctx.startActivity(intent);
                    }
                }
            });

            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.width = ActionBar.LayoutParams.MATCH_PARENT;
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setAttributes(wlp);
            if (!dialog.isShowing())
                dialog.show();
            return false;
        }
    }

    public static boolean isValidEmail1(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target)
                    .matches();
        }
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]"
                + "+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/mm/yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void showDialogOK(Context c, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(c)
                .setTitle("dMe Client")
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public static void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("dme.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("dme.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            e.printStackTrace();
//            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


    public static void shareToGMail(Context context, String[] email, String subject) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/plain");
        final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith("com.android.email"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        context.startActivity(emailIntent);
    }

    public static boolean isApplicationSentToBackground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    public static String postRequest(final Context c, String url, List<NameValuePair> data) {

        String result = "";
        System.out.println("Url " + url + " Data is " + data);

        try {
            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 25000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);

            int timeoutSocket = 25000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient
                    .execute(httpPost);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                result = result.trim();
            }
        } catch (Exception e) {
            // //v("ASYNC EXCEP", e+"");
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText((Activity) c, "unable to reach server, please try again later", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();

        }
        return result;
    }

    public static String getRequest(final Context c, String url, List<NameValuePair> data) {

        String result = "";
        System.out.println("Url " + url + " Data is " + data);

        try {
            HttpGet httpGet = new HttpGet(url);

            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 25000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);

            int timeoutSocket = 25000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient
                    .execute(httpGet);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                result = result.trim();
            }
        } catch (Exception e) {
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText((Activity) c, "unable to reach server, please try again later", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();

        }
        return result;
    }

    public static String postRequest1(final Context c, String url, List<NameValuePair> data) {

        String result = "";
        System.out.println("Url " + url + " Data is " + data);

        try {
            HttpPost httpPost = new HttpPost(url);

            httpPost.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.setHeader("Authorization", "Basic " + Constant.PAYMAYA_PUBLIC_KEY + ",");
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 25000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);

            int timeoutSocket = 25000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpClient
                    .execute(httpPost);

            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
                result = result.trim();
            }
        } catch (Exception e) {
            // //v("ASYNC EXCEP", e+"");
            ((Activity) c).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText((Activity) c, "unable to reach server, please try again later", Toast.LENGTH_LONG).show();
                }
            });
            e.printStackTrace();

        }
        return result;
    }

    public static String getStringFile(File f) {
        InputStream inputStream = null;
        String encodedFile = "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile = output.toString();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        Log.d("lastv", lastVal);
        return lastVal;
    }

    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getFileExt(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

}