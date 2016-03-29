package edu.illinois.entm.sawbodeployer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import edu.illinois.entm.sawbodeployer.R;

public class HomeFragment extends Fragment {
	
	public HomeFragment(){}

    private ArrayList<HashMap<String, String>> newsArray = new ArrayList<HashMap<String, String>>();
    String newstext;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Button donate_btn = (Button) rootView.findViewById(R.id.donate_btn);
        donate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriUrl = Uri.parse(getResources().getString(R.string.donate_url));
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.showAgreement), false);
        if(!previouslyStarted){
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.showAgreement), Boolean.TRUE);
            edit.commit();
            AgreementFragment fragment = null;
            fragment = new AgreementFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
        }

        checkNews();

        return rootView;
    }

    private void checkNews() {
        // Creating new JSON Parser
        JSONparse jParser = new JSONparse();
        // Getting JSON from URL
        JSONObject json = jParser.getJSONFromUrl(getResources().getString(R.string.newsjson_url));
        if (json != null) {
            try {
                // Getting JSON Array
                JSONArray jArrayAll = json.getJSONArray("news");
                for (int i = 0; i < jArrayAll.length(); i++) {
                    try {
                        JSONObject unitJSON = jArrayAll.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id", unitJSON.getString("id"));
                        map.put("date", unitJSON.getString("date"));
                        map.put("text", unitJSON.getString("text"));
                        Log.d("online update", "" + unitJSON.getString("id"));
                        newsArray.add(map);
                    } catch (JSONException e) {
                        //Error
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        int latestnews = sharedPref.getInt("latestnews", 1);
        Log.d("local update", "" + latestnews);
        int onlinenews = 1;
        if (newsArray.size()>0) {
            onlinenews = Integer.parseInt(newsArray.get(newsArray.size()-1).get("id"));
        }
        Log.d("online update", "" + onlinenews);
        if (onlinenews > latestnews) {
            StringBuilder sb = new StringBuilder();
            for (int i = onlinenews; i > latestnews; i--) {
                int unixtime = Integer.parseInt(newsArray.get(i-1).get("date"));
                Date time=new Date((long)unixtime*1000);
                String content = newsArray.get(i-1).get("text");
                sb.append(time.toString() + " - " + content);
                sb.append("\n");
                Log.d("date", time.toString());
            }
            Log.d("news", sb.toString());
            newstext = sb.toString();

            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_news);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            TextView newsbody_txt = (TextView) dialog.findViewById(R.id.newsbody_txt);
            Button ok_btn = (Button) dialog.findViewById(R.id.ok_btn);

            newsbody_txt.setText(newstext);
            ok_btn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    }
            );

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("latestnews", onlinenews);
            editor.commit();
        } else if (latestnews > onlinenews) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("latestnews", onlinenews);
            editor.commit();
        }
    }
}
