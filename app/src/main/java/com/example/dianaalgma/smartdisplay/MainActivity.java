package com.example.dianaalgma.smartdisplay;

//google api:

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;
import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.example.dianaalgma.smartdisplay.ScheduleFragment;
import com.example.dianaalgma.smartdisplay.InformationFragment;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    /*
        To get your Calendar_ID: go to your google calendar -> calendar settings -> Calendar Address
        When adding a new room, add its seat count in the method onCreate
     */
    //public static final String CALENDAR_ID = "m8f26uhisq9sljn3ng4jtj8tbnt81pbc@import.calendar.google.com";
    public static final String CALENDAR_ID = "ptv7ut1ovff009640ltdig14uc@group.calendar.google.com";
    //public static final String LOCATION_ID = "J. Liivi 2 - 403";
    //public static final String LOCATION_ID = "Ülikooli 17 - 220";
    public static final String LOCATION_ID = "J. Liivi 2 - 401";
    private static HashMap<String, Integer> seatcount = new HashMap<>();

    //calendar stuff:

    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static Calendar cal;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            System.err.println("this was open before");
        } else {
            // Probably initialize members with default values for a new instance
            System.err.println("this is a new one");
        }*/
        setContentView(R.layout.activity_main);
        /*
            This is where the seat counts are added:
         */
        seatcount.put("Ülikooli 17 - 220", 18);
        seatcount.put("J. Liivi 2 - 403", 72);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 1);

        //Breaks app, don't know why
        //ScheduleFragment.newInstance().showProgress(false);

        //Automatic update every 20 sec, later every 30 min
        Timer timer = new Timer ();
        TimerTask automaticUpdate = new TimerTask () {
            @Override
            public void run () {
                getResultsFromApi();
            }
        };

        // schedule the task to run starting now and then every hour...
        timer.schedule (automaticUpdate, 0l, 1000*2*10);   // TODO: make it appropriate 1000*30*60

        ScheduleFragment.newInstance().setNextWeekListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleFragment.newInstance().showProgress(true);
                new MakeRequestTask(mCredential,1).execute();
            }
        });

        ScheduleFragment.newInstance().setPrevWeekListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleFragment.newInstance().showProgress(true);
                new MakeRequestTask(mCredential,-1).execute();
            }
        });
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            System.err.println("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        //savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
        //savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
        //savedInstanceState.put

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    System.err.println(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }


    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private int weeknr;

        public MakeRequestTask(GoogleAccountCredential credential){
            this(credential,0);
        }

        public MakeRequestTask(GoogleAccountCredential credential, int weekn) {
            weeknr = weekn;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Smart Display")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi(weeknr);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi(int change) throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());

            //Getting the start of the needed week
            cal.add(Calendar.WEEK_OF_YEAR, change);

            //cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Monday
            final DateTime weekStart = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Monday
            DateTime monEnd = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Monday
            DateTime tueEnd = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Monday
            DateTime wedEnd = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Monday
            DateTime thuEnd = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 1);   //start of Saturday
            DateTime weekEnd = new DateTime(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_WEEK, 2); //move time to next week
            cal.add(Calendar.WEEK_OF_YEAR, -1); //reset the cal to the start of the week

            final List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(weekStart)
                    .setTimeMax(weekEnd).setOrderBy("startTime").setSingleEvents(true).execute();
            final Events monEvents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(weekStart)
                    .setTimeMax(monEnd).setOrderBy("startTime").setSingleEvents(true).execute();
            final Events tueEvents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(monEnd)
                    .setTimeMax(tueEnd).setOrderBy("startTime").setSingleEvents(true).execute();
            final Events wedEvents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(tueEnd)
                    .setTimeMax(wedEnd).setOrderBy("startTime").setSingleEvents(true).execute();
            final Events thuEvents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(wedEnd)
                    .setTimeMax(thuEnd).setOrderBy("startTime").setSingleEvents(true).execute();
            final Events friEvents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(30)
                    .setTimeMin(thuEnd)
                    .setTimeMax(weekEnd).setOrderBy("startTime").setSingleEvents(true).execute();

            final Events currentevents = mService.events().list(CALENDAR_ID)
                    .setMaxResults(2)
                    .setTimeMin(now).setOrderBy("startTime").setSingleEvents(true).execute();

            List<Event> items = LocationFilter(currentevents.getItems(), LOCATION_ID);
            final List<Event> monItems = LocationFilter(monEvents.getItems(), LOCATION_ID);
            final List<Event> tueItems = LocationFilter(tueEvents.getItems(), LOCATION_ID);
            final List<Event> wedItems = LocationFilter(wedEvents.getItems(), LOCATION_ID);
            final List<Event> thuItems = LocationFilter(thuEvents.getItems(), LOCATION_ID);
            final List<Event> friItems = LocationFilter(friEvents.getItems(), LOCATION_ID);
            //eventStrings.add(String.format("Events from %s to %s:", weekStart, weekEnd));
            eventStrings.add("CurrentEvents: ");
            final Event current;// = items.remove(0);
            final Event next;// = items.remove(0);
            Event item;
            if(!items.isEmpty()){
                Event first = items.remove(0);
                DateTime start = first.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = first.getStart().getDate();
                }
                if(start.getValue() > now.getValue()){
                    next = first;
                    current = null;
                }
                else{
                    current = first;
                    if(items.isEmpty()){
                        next = null;
                    }
                    else{
                        next = items.remove(0);
                    }
                }
            }else{
                current = null;
                next = null;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //InformationFragment.setTestText(eventStrings.toString());
                    InformationFragment.setCurrentEvents(current, next);
                    ScheduleFragment.newInstance().setWeekEvents(monItems, tueItems, wedItems, thuItems, friItems, weekStart);
                }
            });
            return eventStrings;
        }

        protected List<Event> LocationFilter(List<Event> events, String location){
            List<Event> items = new ArrayList<>();
            for(Event e: events){
                if(e.getLocation().equals(location)){
                    items.add(e);
                }
            }
            return items;
        }

        @Override
        protected void onPreExecute() {
            System.err.println("Starting progress");
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            //mProgress.hide();
            if (output == null || output.size() == 0) {
                //mOutputText.setText("No results returned.");
                System.err.println("No results returned");
            } else {
                output.add(0, "Data retrieved using the Google Calendar API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                System.out.println(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    //mOutputText.setText("The following error occurred:\n"
                    System.err.println("The following error occurred: \n"
                            + mLastError.getMessage());
                }
            } else {
                //mOutputText.setText("Request cancelled.");
                System.out.println("Request cancelled.");
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch(position) {
                case 0:
                    if(seatcount.get(LOCATION_ID) == null)
                        return InformationFragment.newInstance(LOCATION_ID, -1);
                    return InformationFragment.newInstance(LOCATION_ID, seatcount.get(LOCATION_ID));
                case 1:
                    return ScheduleFragment.newInstance();
                default:
                    return ScheduleFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
