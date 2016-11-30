package com.example.dianaalgma.smartdisplay;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Information fragment containing the information about the room
 */
public class InformationFragment extends Fragment {

    private static final String ARG_LOCATION_ID = "location_id";
    private static final String ARG_SEATCOUNT = "seatcount";
    private static TextView currentevent_text;
    private static TextView nextevent_text;

    public InformationFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given location.
     */
    public static InformationFragment newInstance(String LOCATION_ID, int seatcount) {
        InformationFragment fragment = new InformationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION_ID, LOCATION_ID);
        args.putInt(ARG_SEATCOUNT, seatcount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView room_name = (TextView) rootView.findViewById(R.id.room_name);
        room_name.setText(getArguments().getString(ARG_LOCATION_ID));
        if(getArguments().getInt(ARG_SEATCOUNT) != -1){
            TextView room_info = (TextView) rootView.findViewById(R.id.roominfo);
            room_info.setText(getString(R.string.roominfo_format, getArguments().getInt(ARG_SEATCOUNT)));
        }
        currentevent_text = (TextView) rootView.findViewById(R.id.currenteventinfo);
        nextevent_text = (TextView) rootView.findViewById(R.id.nexteventinfo);
        return rootView;
    }

    public static void setCurrentEvents(Event current, Event next) {
        if(current != null){
            if(current.getSummary() == null) current.setSummary("(No title)");
            currentevent_text.setText(String.format("In progress right now:\n\t%s\n\tfrom %s to %s", current.getSummary(),
                    new SimpleDateFormat("HH:mm").format(new Date(current.getStart().getDateTime().getValue())),
                    new SimpleDateFormat("HH:mm").format(new Date(current.getEnd().getDateTime().getValue()))));
        }
        if(next != null){
            if(next.getSummary() == null) next.setSummary("(No title)");
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
