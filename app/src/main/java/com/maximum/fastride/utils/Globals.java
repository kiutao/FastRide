package com.maximum.fastride.utils;

import android.content.Context;
import android.os.Build;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.net.MalformedURLException;

/**
 * Created by Oleg Kleiman on 11-Apr-15.
 */
public class Globals {

    private static class DManClassFactory {

        static DrawMan drawMan;

        static DrawMan getDrawMan(){
            if( drawMan == null )
                return new DrawMan();
            else
                return drawMan;
        }
    }
    public static final DrawMan drawMan = DManClassFactory.getDrawMan();

    public static class WAMSClassFactory{

        static MobileServiceClient wamsClient;

        public static MobileServiceClient getClient (Context context) throws MalformedURLException {
            if( wamsClient == null ) {
                wamsClient = new MobileServiceClient(
                        Globals.WAMS_URL,
                        Globals.WAMS_API_KEY,
                        context);
            }

            return wamsClient;
        }
    }

    public static final String USERIDPREF = "userid";
    public static final String TOKENPREF = "accessToken";
    public static final String WAMSTOKENPREF = "wamsToken";

    public static final String FB_PROVIDER_FOR_STORE = "Facebook:";
    public static final String GOOGLE_PROVIDER_FOR_STORE = "Google:";
    public static final String MS_PROVIDER_FOR_STORE = "MS:";
    public static final String TWITTER_PROVIDER_FOR_STORE = "Twitter:";
    public static final String PLATFORM = "Android" + Build.VERSION.SDK_INT;

    // 'Project number' of project 'FastRide"
    // See Google Developer Console -> Billing & settings
    // https://console.developers.google.com/project/third-apex-91200/settings
    public static final String SENDER_ID = "1041824085053";

    public static final String WAMS_URL = "https://fastride.azure-mobile.net/";
    public static final String WAMS_API_KEY = "omCudOMCUJgIGbOklMKYckSiGKajJU91";

    public static final String FB_USERNAME_PREF = "username";
    public static final String FB_LASTNAME__PREF = "lastUsername";
    public static final String REG_PROVIDER_PREF = "registrationProvider";

    public static final String FIRST_NAME_PREF = "firstname";
    public static final String LAST_NAME_PREF = "lastname";
    public static final String REG_ID_PREF = "regid";
    public static final String PICTURE_URL_PREF = "pictureurl";
    public static final String EMAIL_PREF = "email";
    public static final String PHONE_PREF = "phone";
    public static final String USE_PHONE_PFER = "usephone";

}
