package com.example.dianaalgma.smartdisplay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.calendar.model.Event;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class InformationFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_LOCATION_ID = "location_id";
    private static final String ARG_SEATCOUNT = "seatcount";
    private static TextView test_text;
    private static TextView currentevent_text;
    private static TextView nextevent_text;

    public InformationFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static InformationFragment newInstance(String LOCATION_ID, int seatcount) {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_LOCATION_ID, LOCATION_ID);
        args.putInt(ARG_SEATCOUNT, seatcount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        TextView room_name = (TextView) rootView.findViewById(R.id.room_name);
        room_name.setText(getArguments().getString(ARG_LOCATION_ID));
        if(getArguments().getInt(ARG_SEATCOUNT) != -1){
            TextView room_info = (TextView) rootView.findViewById(R.id.roominfo);
            room_info.setText(getString(R.string.roominfo_format, getArguments().getInt(ARG_SEATCOUNT)));
        }
        //test_text = (TextView) rootView.findViewById(R.id.test_text);
        currentevent_text = (TextView) rootView.findViewById(R.id.currenteventinfo);
        nextevent_text = (TextView) rootView.findViewById(R.id.nexteventinfo);
        return rootView;
    }

    public static void setTestText(String text){
        test_text.setText(text);
    }

    public static void setCurrentEvents(Event current, Event next) {
        if(current != null){
            currentevent_text.setText(String.format("In progress right now:\n\t%s\n\tfrom %s to %s", current.getSummary(),
                    new SimpleDateFormat("HH:mm").format(new Date(current.getStart().getDateTime().getValue())),
                    new SimpleDateFormat("HH:mm").format(new Date(current.getEnd().getDateTime().getValue()))));
        }
        if(next != null){
            if(DateUtils.isToday(next.getStart().getDateTime().getValue())){
                nextevent_text.setText(String.format("Next event:\n\t%s\n\ttoday from %s to %s", next.getSummary(),
                        new SimpleDateFormat("HH:mm").format(new Date(next.getStart().getDateTime().getValue())),
                        new SimpleDateFormat("HH:mm").format(new Date(next.getEnd().getDateTime().getValue()))));
            }
            else {
                nextevent_text.setText(String.format("Next event:\n\t%s\n\ton %s from %s to %s", next.getSummary(),
                        new SimpleDateFormat("EEE, dd.MM,").format(new Date(next.getStart().getDateTime().getValue())),
                        new SimpleDateFormat("HH:mm").format(new Date(next.getStart().getDateTime().getValue())),
                        new SimpleDateFormat("HH:mm").format(new Date(next.getEnd().getDateTime().getValue()))));
            }
        }
    }
}
