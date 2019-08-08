package com.decideme.client.attributes;

public class Constant {

    public static String font_bold = "fonts/Roboto-Bold.ttf";
    public static String font_light = "fonts/Roboto-Light.ttf";
    public static String font_regular = "fonts/Roboto-Regular.ttf";

    public static String PREFS_NAME = "dme_pref";
    public static String TAG = "DME";

    public static String USER_ID = "user_id";
    public static String USER_NAME = "user_name";
    public static String USER_IMAGE = "user_image";
    public static String USER_EMAIL = "user_email";
    public static String USER_MOBILE = "user_mobile";

    public static String FACEBOOK_USER_ID = "facebook_user_id";

    public static String GOOGLE_USER_NAME = "google_user_name";
    public static String GOOGLE_USER_IMAGE = "google_user_image";

    // For Services Parent URL
//    public static String URL_PARENT = "http://dme.twinkas.club/api/client/";
//    public static String URL_PARENT = "http://www.decidemejob.com/API/api/client/";
//    public static String URL_PARENT1 = "http://www.decidemejob.com/API/api/recruit/";

    public static String URL_PARENT = "https://www.decidemejob.com/API/api/client/";
    public static String URL_PARENT1 = "https://www.decidemejob.com/API/api/recruit/";

    // For All Services URL
    public static String URL_LOGIN = URL_PARENT + "login";
    public static String URL_REGISTRATION = URL_PARENT + "create";
    public static String URL_FORGOT_PASSWORD = URL_PARENT + "forgot";
    public static String URL_GET_CATEGORY = URL_PARENT + "getcat";
    public static String URL_GET_WORKER_DATA = URL_PARENT + "getMapData";
    public static String URL_REQUEST_WORKER = URL_PARENT + "reqservice";
    public static String URL_CANCEL_REQUEST = URL_PARENT + "rejectrequest";

    public static String URL_CHAT_HISTORY = URL_PARENT + "getchatlist";
    public static String URL_SEND_MESSAGE = URL_PARENT + "sendmessage";

    public static String URL_GET_TRACKING = URL_PARENT + "usertracking";
    public static String URL_GET_HISTORY_LIST = URL_PARENT + "getServiceHistoryList";
    public static String URL_GET_CURRENT_DATA = URL_PARENT + "getcurrdata";
    public static String URL_LOGOUT_USER = URL_PARENT + "userLogout";
    public static String URL_CANCEL_INTERVIEWED = URL_PARENT + "cancelinterview";

    public static String URL_GET_PROFILE = URL_PARENT + "getProfileClient";
    public static String URL_SAVE_PROFILE = URL_PARENT + "saveProfileClient";
    public static String URL_SAVE_EDIT_PROFILE = URL_PARENT + "saveEditProfileClient";
    public static String URL_ADD_CARD = URL_PARENT + "addCard";
    public static String URL_GET_CARD_AND_WALLET = URL_PARENT + "getWalletAmount";
    public static String URL_GET_CARD = URL_PARENT + "getCard";
    public static String URL_REMOVE_CARD = URL_PARENT + "removeCard";
    public static String URL_GET_INVOICE = URL_PARENT + "getInvoiceData";
    public static String URL_APPLY_COUPON = URL_PARENT + "applyCopounCode";
    public static String URL_ADD_MONEY = URL_PARENT + "addMoneyInWallet";
    public static String URL_GET_COUPON_LIST = URL_PARENT + "getCouponList";
    public static String URL_PAYMENT = URL_PARENT + "payPayment";
    public static String URL_PAYMENT1 = URL_PARENT + "paypalPayment";
    public static String URL_ADD_RATING = URL_PARENT + "addRating";
    public static String URL_SEND_NONCE = URL_PARENT + "brainTreePayment";

    public static String URL_GET_OTP = URL_PARENT + "getOTP";
    public static String URL_VERIFY_OTP = URL_PARENT + "verifyOTP";
    public static String URL_RESEND_OTP = URL_PARENT1 + "resendOTPEditProfile";


    //    public static String PAYMAYA_SECRET_KEY = "sk-DiZJiYisTICvq87nA1Whp6yflS0tcmajCokh3tErNMX"; // Sandbox
    public static String PAYMAYA_SECRET_KEY = "sk-IUPCxncassgLByEWINh6hmuozBKm60sBtkNkyHIqAmC"; // Production
    //    public static String PAYMAYA_PUBLIC_KEY = "pk-ag4C50lxTcfXBc4XC7Pe6LHJGWzN2JXkgTnzG6xsKf1"; // Sandbox
    public static String PAYMAYA_PUBLIC_KEY = "pk-QBtF42MPyTeM4wT83JyadOStko9MIyNYTt3EwTYnhyV"; // Production

}
