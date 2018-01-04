package com.example.adalynn.pushit2.app;

/**
 * Created by Ravi Tamada on 28/09/16.
 * www.androidhive.info
 */

public class Config {

    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
    public static final String DB_SHARED_PREF = "dbid";

    public static final String APP_NAME = "TrackIt";
    public static final String APP_URL = "www.trackit.com";

    public static final String WAIT_STR_MSG = "Please wait";

    public static final String HTTP_API_URL = "http://10.0.2.2/ecomm/landit/landit.php";
    //public static final String HTTP_API_URL = "http://kotlinclasses.org/ecomm/landit/landit.php";
    //http://kotlinclasses.org/ecomm/landit/landit.php?action=addnewuser&mobile=9336515951


    /**
     * Id to identify a contacts READ_PHONE_STATE_PERMISSION request.
     */
    public static final int READ_PHONE_STATE_PERMISSION = 1;
    public static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;
    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;

    //public static final String READ_PHONE_STATE_PERMISSION = "1";
   // READ_PHONE_STATE_PERMISSION_INT

}
