package com.example.dianaalgma.smartdisplay;

/**
 * Created by Diana Algma on 02-Oct-16.
 */


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.*;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.*;
import android.provider.CalendarContract.Calendars;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.dianaalgma.smartdisplay.MainActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class ScheduleFragment extends Fragment {
    private static ScheduleFragment instance = null;

    private ArrayList<String> monEvents, tueEvents, wedEvents, thuEvents, friEvents;
    private ArrayAdapter monAdapter, tueAdapter, wedAdapter, thuAdapter, friAdapter;

    private Button nextWeek, prevWeek;

    private LinearLayout progressLayout;

    private TextView currentWeek, MonDate, TueDate, WedDate, ThuDate, FriDate;

    private View.OnClickListener nextWeekListener, prevWeekListener;

    private static String ACCOUNT_NAME = "Room 220";
    private static String CALENDAR_NAME = "Schedule";

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public ScheduleFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ScheduleFragment newInstance() {
        if(instance == null){
            instance = new ScheduleFragment();
        }

        //Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        //fragment.setArguments(args);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.schedule_fragment, container, false);
        if(monEvents == null) {
            monEvents = new ArrayList<String>();
            monAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem, R.id.textview, monEvents);
            tueEvents = new ArrayList<String>();
            tueAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem, R.id.textview, tueEvents);
            wedEvents = new ArrayList<String>();
            wedAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem, R.id.textview, wedEvents);
            thuEvents = new ArrayList<String>();
            thuAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem, R.id.textview, thuEvents);
            friEvents = new ArrayList<String>();
            friAdapter = new ArrayAdapter<String>(getContext(), R.layout.listitem, R.id.textview, friEvents);

            ListView monListView = (ListView) rootView.findViewById(R.id.MonList);
            ListView tueListView = (ListView) rootView.findViewById(R.id.TueList);
            ListView wedListView = (ListView) rootView.findViewById(R.id.WedList);
            ListView thuListView = (ListView) rootView.findViewById(R.id.ThuList);
            ListView friListView = (ListView) rootView.findViewById(R.id.FriList);

            monListView.setAdapter(monAdapter);
            tueListView.setAdapter(tueAdapter);
            wedListView.setAdapter(wedAdapter);
            thuListView.setAdapter(thuAdapter);
            friListView.setAdapter(friAdapter);
            /*for (int i = 0; i < 3; i++) {
                monAdapter.add("An event on Mon at 14.15 somewhere");
            }
            for (int i = 0; i < 5; i++) {
                tueAdapter.add("An event on Tue at 1" + i + ".15 somewhere else - very very far far away");
            }*/
            nextWeek = (Button) rootView.findViewById(R.id.next);
            nextWeek.setOnClickListener(nextWeekListener);
            prevWeek = (Button) rootView.findViewById(R.id.prev);
            prevWeek.setOnClickListener(prevWeekListener);

            progressLayout = (LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress);

            currentWeek = (TextView) rootView.findViewById(R.id.currentweek);
            MonDate = (TextView) rootView.findViewById(R.id.MonDate);
            TueDate = (TextView) rootView.findViewById(R.id.TueDate);
            WedDate = (TextView) rootView.findViewById(R.id.WedDate);
            ThuDate = (TextView) rootView.findViewById(R.id.ThuDate);
            FriDate = (TextView) rootView.findViewById(R.id.FriDate);
        }
        //Calendar stuff:
        createCalendar(getContext());
        addEvent(getContext(),"Test","Proov","Test",10l ,11l);
        getEventByID(getContext(),5);
        return rootView;
    }

    public void setNextWeekListener(View.OnClickListener listener){
        nextWeekListener = listener;
    }

    public void setPrevWeekListener(View.OnClickListener listener){
        prevWeekListener = listener;
    }

    public void disableButtons(){
        prevWeek.setEnabled(false);
        nextWeek.setEnabled(false);
    }

    public void showProgress(boolean inProgress){
        if(inProgress)
            progressLayout.setVisibility(View.VISIBLE);
        else
            progressLayout.setVisibility(View.GONE);
    }

    /**Creates the values the new calendar will have*/
    private static ContentValues buildNewCalContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put(Calendars.ACCOUNT_NAME, ACCOUNT_NAME);
        cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(Calendars.NAME, CALENDAR_NAME);
        cv.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME);
        cv.put(Calendars.CALENDAR_COLOR, 0xEA8561);
        //user can only read the calendar
        cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_READ);
        cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        cv.put(Calendars.VISIBLE, 1);
        cv.put(Calendars.SYNC_EVENTS, 1);
        return cv;
    }
    /**The main/basic URI for the android calendars table*/
    private static final Uri CAL_URI = Calendars.CONTENT_URI;

    /**Builds the Uri for your Calendar in android database (as a Sync Adapter)*/
    private static Uri buildCalUri() {
        return CAL_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }

    /**Create and insert new calendar into android database
     * @param ctx The context (e.g. activity)
     */
    private static Long CAL_ID;
    public static void createCalendar(Context ctx) {
        ContentResolver cr = ctx.getContentResolver();
        final ContentValues cv = buildNewCalContentValues();
        Uri calUri = buildCalUri();
        //insert the calendar into the database
        cr.insert(calUri, cv);
        Uri newUri = cr.insert(buildCalUri(), cv);
        CAL_ID = Long.parseLong(newUri.getLastPathSegment());
    }

    public static void addEvent(Context ctx, String title, String description, String location,
                                long dtstart, long dtend) {
        ContentResolver cr = ctx.getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.CALENDAR_ID, CAL_ID);
        cv.put(CalendarContract.Events.TITLE, title);
        cv.put(CalendarContract.Events.DTSTART, dtstart);
        cv.put(CalendarContract.Events.DTEND, dtend);
        cv.put(CalendarContract.Events.EVENT_LOCATION, location);
        cv.put(CalendarContract.Events.DESCRIPTION, description);
        cv.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
        cr.insert(buildEventUri(), cv);
    }
    private static final Uri EVENT_URI = CalendarContract.Events.CONTENT_URI;
    public static Uri buildEventUri() {
        return EVENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }
    public static void getEventByID(Context ctx, long id) {
        ContentResolver cr = ctx.getContentResolver();
        //Projection array for query (the values you want)
        final String[] PROJECTION = new String[] {
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
        };
        final int ID_INDEX = 0, TITLE_INDEX = 1, DESC_INDEX = 2, LOCATION_INDEX = 3,
                START_INDEX = 4, END_INDEX = 5;
        long start_millis=0, end_millis=0;
        String title=null, description=null, location=null;
        final String selection = "("+ CalendarContract.Events.OWNER_ACCOUNT+" = ? AND "+ CalendarContract.Events._ID+" = ?)";
        final String[] selectionArgs = new String[] {ACCOUNT_NAME, id+""};
        Cursor cursor = cr.query(buildEventUri(), PROJECTION, selection, selectionArgs, null);
        //at most one event will be returned because event ids are unique in the table
        if (cursor.moveToFirst()) {
            id = cursor.getLong(ID_INDEX);
            title = cursor.getString(TITLE_INDEX);
            description = cursor.getString(DESC_INDEX);
            location = cursor.getString(LOCATION_INDEX);
            start_millis = cursor.getLong(START_INDEX);
            end_millis = cursor.getLong(END_INDEX);

            Log.d("Test",description);
            //do something with the values...

        }
        cursor.close();
    }

    public void setWeekEvents(List<Event> monEs, List<Event> tueEs, List<Event> wedEs,
                              List<Event> thuEs, List<Event> friEs, DateTime weekStart){
        ClearCalendar();
        monAdapter.addAll(ListToStrings(monEs));
        tueAdapter.addAll(ListToStrings(tueEs));
        wedAdapter.addAll(ListToStrings(wedEs));
        thuAdapter.addAll(ListToStrings(thuEs));
        friAdapter.addAll(ListToStrings(friEs));
        monAdapter.notifyDataSetChanged();
        tueAdapter.notifyDataSetChanged();
        wedAdapter.notifyDataSetChanged();
        thuAdapter.notifyDataSetChanged();
        friAdapter.notifyDataSetChanged();
        //System.err.println(events);

        //Reset dates on display
        String mon = new SimpleDateFormat("dd.MM").format(new Date(weekStart.getValue()));
        MonDate.setText(new SimpleDateFormat("EEE, dd.MM").format(new Date(weekStart.getValue())));
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(weekStart.getValue());
        c.add(java.util.Calendar.DATE, 1);
        TueDate.setText(new SimpleDateFormat("EEE, dd.MM").format((new DateTime(c.getTimeInMillis())).getValue()));
        c.add(java.util.Calendar.DATE, 1);
        WedDate.setText(new SimpleDateFormat("EEE, dd.MM").format((new DateTime(c.getTimeInMillis())).getValue()));
        c.add(java.util.Calendar.DATE, 1);
        ThuDate.setText(new SimpleDateFormat("EEE, dd.MM").format((new DateTime(c.getTimeInMillis())).getValue()));
        c.add(java.util.Calendar.DATE, 1);
        String fri = new SimpleDateFormat("dd.MM").format((new DateTime(c.getTimeInMillis())).getValue());
        FriDate.setText(new SimpleDateFormat("EEE, dd.MM").format((new DateTime(c.getTimeInMillis())).getValue()));
        currentWeek.setText(mon + " - " + fri);
        showProgress(false);
        nextWeek.setEnabled(true);
        prevWeek.setEnabled(true);
    }

    public void ClearCalendar(){
        monAdapter.clear();
        tueAdapter.clear();
        wedAdapter.clear();
        thuAdapter.clear();
        friAdapter.clear();
    }
    public String EventToString(Event event){
        if(event.getSummary() == null){
            event.setSummary("(No title)");
        }
        return String.format("%s-%s %s",
                new SimpleDateFormat("HH:mm").format(new Date(event.getStart().getDateTime().getValue())),
                new SimpleDateFormat("HH:mm").format(new Date(event.getEnd().getDateTime().getValue())),
                event.getSummary());
    }

    public List<String> ListToStrings(List<Event> events){
        List<String> result = new ArrayList<>();
        for(Event e : events){
            result.add(EventToString(e));
        }
        return result;
    }

}
