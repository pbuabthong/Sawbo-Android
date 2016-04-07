package edu.illinois.entm.sawbodeployer;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.entm.sawbodeployer.adapter.BrowseListArrayAdapter;
import edu.illinois.entm.sawbodeployer.R;
//import edu.uiuc.sawbo.PrepareTitle;

public class BrowseFragment extends Fragment {

    public BrowseFragment(){}

    public View rootView;
    public ListView l;
    public HashMap<String, String> titleArray = new HashMap<String, String>();
    ArrayList<HashMap<String, String>> vidArray = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> dispArray = new ArrayList<HashMap<String, String>>();
    String[] topicArray;
    String[] countryArray;
    String[] languageArray;
    HashMap<String, ArrayList<String>> filterArray = new HashMap<String, ArrayList<String>>();
    Button topic_btn, alert_btn;
    Button language_btn;
    Button country_btn;
    Button fulllist_btn;
    boolean goneHashprepare;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("onCreate", "inOnCreate");
        goneHashprepare = false;

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.v("size", String.valueOf(titleArray.size()));
        PrepareTitle pt = new PrepareTitle();
        if(pt.downloadTitle(getActivity())){
            Log.v("result", "successful");
        }else{
            Log.v("result", ":(");
        }
        File dirFiles = getActivity().getFilesDir();
        Log.v("absolute path", dirFiles.getAbsolutePath());
        for (String strFile : dirFiles.list())
        {
            Log.v("filename", strFile);
        }

        titleArray = pt.retrieveTitle(getActivity());
        //Log.v("Triple2", titleArray.get("Triple"));
        filterArray.put("Topic", new ArrayList<String>());
        //filterArray.get("Topic").add("Cholera");
        filterArray.put("Language", new ArrayList<String>());
        filterArray.put("Country", new ArrayList<String>());
        DownloadWebPageTask task = new DownloadWebPageTask();
        task.execute(new String[]{getResources().getString(R.string.totallist_url)});

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_browse, container, false);


        topic_btn = (Button) rootView.findViewById(R.id.topic_btn_id);
        language_btn = (Button) rootView.findViewById(R.id.language_btn_id);
        country_btn = (Button) rootView.findViewById(R.id.country_btn_id);
        fulllist_btn = (Button) rootView.findViewById(R.id.fulllist_btn);
        alert_btn = (Button) rootView.findViewById(R.id.alert_btn);
        //goneHashprepare = false;

        topic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment fragment = null;
                fragment = new FilterFragment();
                fragment.header_str = getResources().getString(R.string.topic_str);
                fragment.dispFilter = topicArray;
                fragment.selectedFilter = filterArray.get("Topic");
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
            }
        });

        language_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment fragment = null;
                fragment = new FilterFragment();
                fragment.header_str = getResources().getString(R.string.language_str);
                fragment.dispFilter = languageArray;
                fragment.selectedFilter = filterArray.get("Language");
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
            }
        });

        country_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterFragment fragment = null;
                fragment = new FilterFragment();
                fragment.header_str = getResources().getString(R.string.country_str);
                fragment.dispFilter = countryArray;
                fragment.selectedFilter = filterArray.get("Country");
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
            }
        });

        fulllist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterArray.get("Country").clear();
                filterArray.get("Language").clear();
                filterArray.get("Topic").clear();
                prepareDispArray();
            }
        });

        alert_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getResources().getString(R.string.email_url), null));
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.volunteersubj_str));
                intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.volunteerbody_str));
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.emailintent_str)));
            }
        });

        if (isAdded()) {
            prepareDispArray();
        }

        return rootView;
    }

    public void prepareDispArray() {

        l = (ListView) rootView.findViewById(R.id.browse_list);
        dispArray = vidArray;

        if(getActivity()!=null) {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null) {
                alert_btn.setVisibility(View.VISIBLE);
                alert_btn.setText(R.string.nointernet);
                alert_btn.setEnabled(false);
            } else {
                alert_btn.setVisibility(View.VISIBLE);
                alert_btn.setText(R.string.loading);
                alert_btn.setEnabled(false);
            }

            if (!filterArray.get("Topic").isEmpty()) {
                dispArray = filterList(dispArray, filterArray.get("Topic"), "video");
            }

            if (!filterArray.get("Language").isEmpty()) {
                dispArray = filterList(dispArray, filterArray.get("Language"), "language");
            }

            if (!filterArray.get("Country").isEmpty()) {
                dispArray = filterList(dispArray, filterArray.get("Country"), "country");
            }

            Log.v("dispArray", String.valueOf(dispArray.size()));

            if (dispArray.size() == 0 && goneHashprepare) {
                cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                ni = cm.getActiveNetworkInfo();
                if (ni == null) {
                    alert_btn.setVisibility(View.VISIBLE);
                    alert_btn.setText(R.string.nointernet);
                    alert_btn.setEnabled(false);
                } else {
                    alert_btn.setVisibility(View.VISIBLE);
                    alert_btn.setText(R.string.nofilter);
                    alert_btn.setEnabled(true);
                    fulllist_btn.setEnabled(true);
                }
            } else {
                alert_btn.setVisibility(View.GONE);
                alert_btn.setEnabled(false);
            }

            if (dispArray.size() > 0) {
                String[] values = new String[dispArray.size()];
                int count = 0;
                Log.i("BrowseFragment", String.valueOf(dispArray.size()));
                String fullTitle;
                for (HashMap<String, String> hashMap : dispArray) {
                    fullTitle = titleArray.get(hashMap.get("video"));
                    String addlight = "";
                    if (hashMap.get("filename").contains("_Light")) {
                        addlight = " | Light";
                    }
                    if (fullTitle == null) fullTitle = hashMap.get("video");
                    values[count] = fullTitle + "^^" + hashMap.get("language")
                            + " | " + hashMap.get("country") + addlight;
                    //Log.i("Test", values[count]);
                    count++;
                }

                BrowseListArrayAdapter adapter = new BrowseListArrayAdapter(getActivity(), values);
                l.setAdapter(adapter);
                l.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> arg0, View view,
                                                    int position, long id) {
                                Log.v("int", dispArray.get(position).get("filename"));
                                WriteLog wl = new WriteLog();
                                wl.sendLog(getActivity());
                                Log.v("BrowseFragment", "come back to Browse");
                                VideoDetailFragment fragment = null;
                                fragment = new VideoDetailFragment();
                                fragment.videoFilename = dispArray.get(position).get("filename");
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
                            }
                        }
                );
                topic_btn.setEnabled(true);
                language_btn.setEnabled(true);
                country_btn.setEnabled(true);
                fulllist_btn.setEnabled(true);
            }
        }

    }

    public ArrayList<HashMap<String, String>>filterList(ArrayList<HashMap<String, String>> inputList, ArrayList<String> selectedItem, String type) {
        ArrayList<HashMap<String, String>> processedList = new ArrayList<HashMap<String, String>>();
        for (HashMap<String, String> hashMap : dispArray) {
            String checkString = hashMap.get(type);
            if(selectedItem.contains(checkString)){
                processedList.add(hashMap);
            }
        }
        return processedList;
    }

    public class MapComparator implements Comparator<Map<String, String>>
    {
        private final String key;

        public MapComparator(String key)
        {
            this.key = key;
        }

        public int compare(Map<String, String> first,
                           Map<String, String> second)
        {
            String firstValue = first.get(key);
            String secondValue = second.get(key);
            return firstValue.compareTo(secondValue);
        }
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        public boolean isJSONValid(String test) {
            try {
                new JSONObject(test);
            } catch (JSONException ex) {
                try {
                    new JSONArray(test);
                } catch (JSONException ex1) {
                    return false;
                }
                return false;
            }
            return true;
        }

        public String returnString = "";
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                    Log.v("go i", "in");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            returnString = result;
            //Log.i("Task", result);
            //Check if result is JSON. If so, then result is the list of video,
            try {
                JSONObject jObject = new JSONObject(result);
                JSONArray jArrayAll = jObject.getJSONArray("all");
                for (int i = 0; i < jArrayAll.length(); i++) {
                    try {
                        JSONObject unitJSON = jArrayAll.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("filename", unitJSON.getString("Filename"));
                        map.put("video", unitJSON.getString("Video"));
                        map.put("language", unitJSON.getString("Language"));
                        map.put("country", unitJSON.getString("Country"));
                        vidArray.add(map);
                        //Log.v("teg",map.get("filename"));
                    } catch (JSONException e) {
                        //Error
                    }
                }
                JSONArray jArrayTopic = jObject.getJSONArray("Topic");
                JSONArray jArrayLanguage = jObject.getJSONArray("Language");
                JSONArray jArrayCountry = jObject.getJSONArray("Country");
                topicArray = new String[jArrayTopic.length()];
                languageArray = new String[jArrayLanguage.length()];
                countryArray = new String[jArrayCountry.length()];
                for (int i=0; i < jArrayTopic.length(); i++) {
                    try {
                        topicArray[i]=jArrayTopic.getString(i);
                    } catch (JSONException e) {
                        //Error
                    }
                }

                for (int i=0; i < jArrayLanguage.length(); i++) {
                    try {
                        languageArray[i]=jArrayLanguage.getString(i);
                    } catch (JSONException e) {
                        //Error
                    }
                }

                for (int i=0; i < jArrayCountry.length(); i++) {
                    try {
                        countryArray[i]=jArrayCountry.getString(i);
                    } catch (JSONException e) {
                        //Error
                    }
                }
                Arrays.sort(topicArray);
                Arrays.sort(languageArray);
                Arrays.sort(countryArray);
            } catch (JSONException e) {

            }
            Collections.sort(vidArray, new MapComparator("video"));
            goneHashprepare = true;
            prepareDispArray();
        }
    }
}