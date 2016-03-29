package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

import edu.illinois.entm.sawbodeployer.R;

public class InfoFragment extends Fragment {

    public InfoFragment(){}
    View rootView;
    Spinner spinner;
    String[] items;
    WriteLog wl = new WriteLog();
    private Locale myLocale;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        loadLocale();

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        rootView = inflater.inflate(R.layout.fragment_info, container, false);
        Button email_btn = (Button)rootView.findViewById(R.id.email_btn);
        Button help_btn = (Button)rootView.findViewById(R.id.help_btn);
        Button web_btn = (Button)rootView.findViewById(R.id.web_btn);
        Button tel_btn = (Button)rootView.findViewById(R.id.tel_btn);
        Button location_btn = (Button)rootView.findViewById(R.id.location_btn);
        Button lang_btn = (Button)rootView.findViewById(R.id.language_btn);
        email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getResources().getString(R.string.email_url), null));
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.inquirysubj_str));
                intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.inquirybody_str));
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.emailintent_str)));
            }
        });

        web_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriUrl = Uri.parse(getResources().getString(R.string.sawbo_url));
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        tel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(getResources().getString(R.string.call_url)));
                startActivity(callIntent);
            }
        });

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationFragment fragment = null;
                fragment = new LocationFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
            }
        });

        help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sawbovidtut;

                VideoPlaybackFragment fragment = null;
                fragment = new VideoPlaybackFragment();
                fragment.videoPath = url;
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).addToBackStack(null).commit();
            }
        });

        lang_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_language);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                final ListView mDrawerList, langList;
                String[] langstr;

                langList = (ListView) dialog.findViewById(R.id.language_listview);

                langstr = getResources().getStringArray(R.array.lang_list);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, langstr);

                langList.setAdapter(adapter);

                langList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        // ListView Clicked item value
                        String itemValue = (String) langList.getItemAtPosition(position);

                        // Show Alert

                        String lang = "en";
                        switch (itemPosition) {
                            case 0:
                                lang = "en";
                                break;
                            case 1:
                                lang = "th";
                                break;
                            default:
                                break;
                        }
                        Toast.makeText(getActivity(), getResources().getString(R.string.successchg_str) + itemValue, Toast.LENGTH_SHORT).show();
                        wl.writeNow(getActivity(), "locale", itemValue, "");

                        changeLang(lang);

                        dialog.dismiss();
                    }

                });
            }
        });




        /*spinner = (Spinner) rootView.findViewById(R.id.spinner_accuracy);
        items = new String[]{"Fine", "Coarse"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        String acc = wl.getAcc(getActivity());
        Log.v("acc", acc);
        int sel = acc.equals("Fine") ? 0 : 1;
        spinner.setSelection(sel);
         */
        return rootView;
    }

    public void changeLang(String lang)
    {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

        //MainActivity ma = super.;
    }


    public void saveLocale(String lang)
    {
        String langPref = "Language";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }


    public void loadLocale()
    {
        String langPref = "Language";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }
}
