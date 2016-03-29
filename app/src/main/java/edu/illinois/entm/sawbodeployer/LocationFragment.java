package edu.illinois.entm.sawbodeployer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.VideoView;

import edu.illinois.entm.sawbodeployer.R;

/**
 * Created by Pakpoomb on 9/6/14.
 */
public class LocationFragment extends Fragment {
    View rootView;
    String videoPath;
    VideoView videoView;
    FrameLayout wp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        rootView = inflater.inflate(R.layout.fragment_location, container, false);
        RadioGroup location_rd = (RadioGroup) rootView.findViewById(R.id.location_rd);

        final WebView mWebView=(WebView)rootView.findViewById(R.id.locationdisc_webview);

        mWebView.loadUrl("file:///android_asset/disclaimer.html");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new MyWebViewClient());

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean gpsOk = sharedPref.getBoolean("usegps", false);
        Log.v("location", ""+gpsOk);
        if (gpsOk) {
            location_rd.check(R.id.yes_rd);
        }else{
            location_rd.check(R.id.no_rd);
        }

        location_rd.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                // find the radiobutton by returned id
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(i);
                String svalue = (String) radioButton.getText();

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                if(svalue.equals("Yes")) {
                    editor.putBoolean("usegps", true);
                } else {
                    editor.putBoolean("usegps", false);
                }
                editor.commit();
                Log.v("location", svalue);
            }
        });

        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.showAgreement), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.showAgreement), Boolean.TRUE);
            edit.commit();
        }
        */
        return rootView;
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        //show the web page in webview but not in web browser
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl (url);
            return true;
        }
    }
}