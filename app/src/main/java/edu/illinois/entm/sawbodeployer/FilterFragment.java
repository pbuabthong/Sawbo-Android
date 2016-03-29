package edu.illinois.entm.sawbodeployer;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import edu.illinois.entm.sawbodeployer.R;

//import edu.uiuc.sawbo.adapter.BrowseFilterArrayAdapter;
//import edu.uiuc.sawbo.adapter.BrowseListArrayAdapter;

/**
 * Created by Pakpoomb.
 */
public class FilterFragment extends Fragment {
    public String header_str;
    public View rootView;
    public TextView headertxt;
    public ListView l;
    public String[] dispFilter;
    private HashMap<String, String> topicArray = new HashMap<String, String>();
    public ArrayList<String> selectedFilter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_filter, container, false);


        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        l = (ListView) rootView.findViewById(R.id.filter_list);
        headertxt = (TextView) rootView.findViewById(R.id.header_id);
        headertxt.setText(header_str);

        /*Button reset_btn = (Button) rootView.findViewById(R.id.reset_btn_id);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilter();
            }
        });
*/
        Button ok_btn = (Button) rootView.findViewById(R.id.ok_btn_id);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                BrowseFragment bf = new BrowseFragment();
                bf.filterArray.put(header_str, selectedFilter);
                /*fragmentManager.beginTransaction().replace(R.id.frame_container, new BrowseFragment()).commit();*/
                fragmentManager.popBackStack();
            }
        });

        prepareFilterView();
        /*String[] values = new String[dispFilter.length];
        for (int i=0;i<dispFilter.length;i++) {
            String checked = selectedFilter.contains(dispFilter[i])?"1":"0";
            values[i] =  dispFilter[i] + "::" + checked;
        }

        BrowseFilterArrayAdapter adapter = new BrowseFilterArrayAdapter(getActivity(), values);
        l.setAdapter(adapter);*/
        /*l.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {
                        Log.v("int", dispArray.get(position).get("filename"));

                        VideoDetailFragment fragment = null;
                        fragment = new VideoDetailFragment();
                        fragment.videoFilename = dispArray.get(position).get("filename");
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                    }
                }
        );*/

        return rootView;
    }

    private void prepareFilterView(){
        int dl = 0;
        if (header_str.equals("Topic")) {
            PrepareTitle pt = new PrepareTitle();
            topicArray = pt.retrieveTopic(getActivity());
            Log.v("topic", "topic");
        }
        if(dispFilter.length > 0) dl = dispFilter.length;
        String[] values = new String[dl];
        for (int i=0;i<dispFilter.length;i++) {
            String checked = selectedFilter.contains(dispFilter[i])?"1":"0";
            String dispString = dispFilter[i];
            if (header_str.equals("Topic")) {
                String fullTopic = topicArray.get(dispString);
                if (fullTopic == null) fullTopic = dispString;
                dispString = fullTopic;
            }
            values[i] =  dispString + "::" + checked;
        }

        BrowseFilterArrayAdapter adapter = new BrowseFilterArrayAdapter(getActivity(), values);
        l.setAdapter(adapter);
    }

    /*
    private void resetFilter(){
        selectedFilter.clear();
        prepareFilterView();
    }
    */

    public boolean setFilter(String name, boolean setCheck){
        if(setCheck){
            selectedFilter.add(name);
        }else{
            selectedFilter.remove(name);
        }
        return true;
    }

    public class BrowseFilterArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String [] values;

        public BrowseFilterArrayAdapter(Context context, String[] values) {
            super(context, R.layout.filter_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.filter_row, parent, false);
            final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.filter_checkBox);

            StringTokenizer tokens = new StringTokenizer(values[position], "::");
            checkBox.setText(tokens.nextToken());
            String checkedst = tokens.nextToken();
            boolean checked = checkedst.equals("1");
            checkBox.setChecked(checked);
            //set listener
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // This gets the correct item to work with.
                    //Log.v("ischecked", String.valueOf(checkBox.isChecked()));
                    String filtertochk = String.valueOf(checkBox.getText());
                    String realtopicabbrev = "";
                    if (header_str.equals("Topic")) {
                        realtopicabbrev = getKeyByValue(topicArray, filtertochk);
                        if (realtopicabbrev != null) filtertochk = realtopicabbrev;
                    }
                    if (!checkBox.isChecked()) {
                        setFilter(filtertochk, false);
                    } else {
                        setFilter(filtertochk, true);
                    }
                }
            });
            return rowView;
        }
    }

    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}

