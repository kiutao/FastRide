package com.maximum.fastride;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.maximum.fastride.model.User;
import com.maximum.fastride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

public class RegisterActivity extends FragmentActivity
        implements ConfirmRegistrationFragment.RegistrationDialogListener{

    private static final String LOG_TAG = "FR.Register";

	private final String PENDING_ACTION_BUNDLE_KEY = "com.maximum.fastride:PendingAction";
    private final String fbProvider = "fb";

	private UiLifecycleHelper uiHelper;
	private LoginButton mFBLoginButton;

    private GraphUser fbUser;

    String mAccessToken;

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, final User user) {

        final String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPostExecute(Void result) {
                saveFBUser(fbUser);
                showRegistrationForm();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                user.setDeviceId(android_id);
                user.setPlatform(Globals.PLATFORM);

                try {
                    usersTable.delete(user).get();
                } catch (InterruptedException | ExecutionException ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }

                return null;
            }
        }.execute();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
	
	private PendingAction pendingAction = PendingAction.NONE;

    // 'Users' table is defined with 'Anybody with the Application Key'
    // permissions for READ and INSERT operations, so no authentication is
    // required for adding new user to it
    MobileServiceTable<User> usersTable;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.fastride_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle(getString(R.string.title_activity_register));

        final EditText txtPhoneNumber = (EditText)findViewById(R.id.phone);
        final String hint = getString(R.string.phone_hint);
        txtPhoneNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if( !hasFocus )
                    txtPhoneNumber.setHint(hint);

            }
        });
        txtPhoneNumber.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                txtPhoneNumber.setHint("");
                return false;
            }

        });

        mFBLoginButton = (LoginButton) findViewById(R.id.loginButton);
        mFBLoginButton.setReadPermissions("email");

        mFBLoginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(final GraphUser user) {
                if( user != null ) {
                    RegisterActivity.this.fbUser = user;

                    new AsyncTask<Void, Void, Void>() {

                        Exception mEx;
                        ProgressDialog progress;

                        @Override
                        protected void onPreExecute(){

                            LinearLayout loginLayout = (LinearLayout)findViewById(R.id.fb_login_form);
                            if( loginLayout != null )
                                loginLayout.setVisibility(View.GONE);

                            progress = ProgressDialog.show(RegisterActivity.this,
                                    "Almost there",
                                    "Making things ready");
                        }

                        @Override
                        protected void onPostExecute(Void result){
                            progress.dismiss();

                            if( mEx == null )
                                showRegistrationForm();

                        }

                        @Override
                        protected Void doInBackground(Void... params) {

                            String regID = Globals.FB_PROVIDER_FOR_STORE + user.getId();
                            try{
                                MobileServiceList<User> _users =
                                        usersTable.where().field("registration_id").eq(regID)
                                                .execute().get();

                                if( _users.getTotalCount() >= 1 ) {
                                    User registeredUser = _users.get(0);

//                                    try {
//                                        new AlertDialogWrapper.Builder(RegisterActivity.this)
//                                                .setTitle(R.string.dialog_confirm_registration)
//                                                .setMessage(R.string.registration_already_performed)
//                                                .autoDismiss(true)
//                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        //dialog.dismiss();
//                                                    }
//                                                })
//                                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        //dialog.dismiss();
//                                                    }
//                                                }).show();
//                                    } catch (Exception e) {
//                                        Log.e("FR", e.getMessage());
//                                    }

                                    //ConfirmRegistrationFragment dialog =
                                     new ConfirmRegistrationFragment()
                                    .setUser(registeredUser)
                                    .show(getFragmentManager(), "RegistrationDialogFragment");

                                    // Just prevent body execution with onPostExecute().
                                    // Normal flow continues from positive button handler.
                                    mEx = new Exception();
                                }
                                else {

                                    saveFBUser(user);
                                }

                            } catch (InterruptedException | ExecutionException ex) {
                                mEx = ex;
                                Log.e(LOG_TAG, ex.getMessage());
                            }

                            return null;
                        }
                    }.execute();

                }
            }
        });

        mFBLoginButton.setOnErrorListener(new LoginButton.OnErrorListener() {

            @Override
            public void onError(FacebookException error) {
                String msg = getResources().getString(R.string.fb_error_msg)
                        + error.getMessage().trim();

                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle(getResources().getString(R.string.fb_error))
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                        .show();

            }
        });

        try{
            usersTable = new MobileServiceClient(
                                Globals.WAMS_URL,
                                Globals.WAMS_API_KEY,
                                this)
                             .getTable("users", User.class);

        } catch(MalformedURLException ex ) {
            Log.e(LOG_TAG, ex.getMessage() + " Cause: " + ex.getCause());
        }
	}

    private void showRegistrationForm() {
        LinearLayout form = (LinearLayout)findViewById(R.id.register_form);
        form.setVisibility(View.VISIBLE);
    }

    private void hideRegistrationForm() {
        LinearLayout form = (LinearLayout)findViewById(R.id.register_form);
        form.setVisibility(View.GONE);
    }



    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void saveFBUser(GraphUser fbUser) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(Globals.FB_USERNAME_PREF, fbUser.getFirstName());
        editor.putString(Globals.REG_PROVIDER_PREF, fbProvider);
        editor.putString(Globals.FB_LASTNAME__PREF, fbUser.getLastName());
        editor.putString(Globals.TOKENPREF, mAccessToken);

        editor.apply();
    }

    private void handlePendingAction() {
        pendingAction = PendingAction.NONE;
    }
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
//                new AlertDialog.Builder(RegisterActivity.this)
//                    .setTitle(R.string.cancelled)
//                    .setMessage(R.string.permission_not_granted)
//                    .setPositiveButton(R.string.ok, null)
//                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        } else if( state == SessionState.OPENED ) {
            mAccessToken = session.getAccessToken();
        }

    }

    boolean bCardsFragmentDisplayed = false;

    public void onRegisterNext(View v){

        if( !bCardsFragmentDisplayed ) {

            EditText txtUser = (EditText) findViewById(R.id.phone);
            if (txtUser.getText().toString().isEmpty()) {

                String noPhoneNumber = getResources().getString(R.string.no_phone_number);
                txtUser.setError(noPhoneNumber);
                return;
            }

            final ProgressDialog progress = ProgressDialog.show(this, "Processing", "Adding your data");
            try {

                User newUser = new User();

                newUser.setRegistrationId(Globals.FB_PROVIDER_FOR_STORE + fbUser.getId());
                newUser.setFirstName( fbUser.getFirstName() );
                newUser.setLastName( fbUser.getLastName() );
                String pictureURL = "http://graph.facebook.com/" + fbUser.getId() + "/picture?type=large";
                newUser.setPictureURL(pictureURL);
                newUser.setEmail((String)fbUser.getProperty("email"));
                newUser.setPhone(txtUser.getText().toString());
                CheckBox cbUsePhone = (CheckBox)findViewById(R.id.cbUsePhone);
                newUser.setUsePhone(cbUsePhone.isChecked());

                String android_id = Settings.Secure.getString(this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                newUser.setDeviceId(android_id);

                newUser.setPlatform(Globals.PLATFORM);

                newUser.save(this);

                // 'Users' table is defined with 'Anybody with the Application Key'
                // permissions for READ and INSERT operations, so no authentication is
                // required for adding new user to it
                usersTable.insert(newUser, new TableOperationCallback<User>() {
                    @Override
                    public void onCompleted(User user, Exception e, ServiceFilterResponse serviceFilterResponse) {
                        progress.dismiss();

                        if( e != null ) {
                            Toast.makeText(RegisterActivity.this,
                                    e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {

                            hideRegistrationForm();

                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                            RegisterCarsFragment fragment = new RegisterCarsFragment();
                            fragmentTransaction.add(R.id.register_cars_form, fragment);
                            fragmentTransaction.commit();

                            bCardsFragmentDisplayed = true;
                            Button btnNext = (Button)findViewById(R.id.btnRegistrationNext);
                            btnNext.setText("Finish");
                        }
                    }
                });

            } catch(Exception ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                progress.dismiss();
            }

        } else {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(Globals.TOKENPREF, mAccessToken);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
	
}
