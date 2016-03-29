package edu.illinois.entm.sawbodeployer;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import edu.illinois.entm.sawbodeployer.R;

public class AgreementFragment extends Fragment {

	public AgreementFragment(){}
	int time = 1;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
 
        View rootView = inflater.inflate(R.layout.fragment_agreements, container, false);

        final WebView mWebView=(WebView)rootView.findViewById(R.id.agwebview);

        mWebView.loadUrl("file:///android_asset/disclaimer.html");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new MyWebViewClient());

        Button ok_btn = (Button) rootView.findViewById(R.id.ok_agreement_btn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(time == 1){
                    mWebView.loadUrl("file:///android_asset/terms.html");
                    time++;
                }else {
                    FragmentManager fragmentManager = getFragmentManager();
                /*fragmentManager.beginTransaction().replace(R.id.frame_container, new BrowseFragment()).commit();*/
                    fragmentManager.popBackStackImmediate();
                }
            }
        });
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
