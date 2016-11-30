package com.example.dianaalgma.smartdisplay;

/**
 * Created by Diana Algma on 02-Oct-16.
 */


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment for the room schedule
 */
public class ScheduleFragment extends Fragment {
    private static ScheduleFragment instance = null;

    private ArrayList<String> monEvents, tueEvents, wedEvents, thuEvents, friEvents;
    private ArrayAdapter monAdapter, tueAdapter, wedAdapter, thuAdapter, friAdapter;

    private Button nextWeek, prevWeek;

    private LinearLayout progressLayout;

    private TextView currentWeek, MonDate, TueDate, WedDate, ThuDate, FriDate;

    private View.OnClickListener nextWeekListener, prevWeekListener;

    private static String ACCOUNT_NAME = "Room name";
    private static String CALENDAR_NAME = "Schedule";

    public ScheduleFragment() {
    }

    /**
     * Returns a new instance of this fragment.
     */
    public static ScheduleFragment newInstance() {
        if(instance == null){
            instance = new ScheduleFragment();
        }
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
