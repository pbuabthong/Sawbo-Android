package edu.illinois.entm.sawbodeployer;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.sender.HttpSender;

import edu.illinois.entm.sawbodeployer.adapter.NavDrawerListAdapter;
import edu.illinois.entm.sawbodeployer.model.NavDrawerItem;
import edu.illinois.entm.sawbodeployer.R;

@ReportsCrashes(
        formUri = "https://sawbo.cloudant.com/acra-sawbo/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "itiontheringstainglentor",
        formUriBasicAuthPassword = "HeRkIN4BSEF5DcCY3wJ83INS",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST
)
@SuppressWarnings("ResourceType")
public class MainActivity extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList, langList;
	private ActionBarDrawerToggle mDrawerToggle;
    private boolean doubleBackToExitPressedOnce = false;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles, langstr;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

    private Locale myLocale;
    private WriteLog wl = new WriteLog();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ACRA.init(getApplication());
		setContentView(R.layout.activity_main);
        loadLocale();
        loadActivity();

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
	}

    public void loadActivity() {


        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Browse
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Downloads
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        // Share
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Info
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_language);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();


            langList = (ListView) dialog.findViewById(R.id.language_listview);

            langstr = getResources().getStringArray(R.array.lang_list);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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
                            lang = "es";
                            break;
                        case 2:
                            lang = "fr";
                            break;
                        case 3:
                            lang = "th";
                            break;
                        default:
                            break;
                    }
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.successchg_str)+itemValue, Toast.LENGTH_SHORT).show();
                    wl.writeNow(MainActivity.this, "locale", itemValue, "");
                    changeLang(lang);
                    loadLocale();
                    loadActivity();

                    dialog.dismiss();
                }

            });
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        menu.findItem(R.id.action_settings).setVisible(false);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
        String fragTag = "";
		switch (position) {
		case 0:
			fragment = new HomeFragment();
            fragTag = "Home";
			break;
		case 1:
			fragment = new BrowseFragment();
            fragTag = "Browse";
			break;
		case 2:
			fragment = new DownloadsFragment();
            fragTag = "Downloads";
			break;
		case 3:
			fragment = new ShareFragment();
            fragTag = "Share";
			break;
		case 4:
			fragment = new InfoFragment();
            fragTag = "Info";
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment, fragTag).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

    public void changeLang(String lang)
    {
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        Configuration config = new Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }


    public void saveLocale(String lang)
    {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }


    public void loadLocale()
    {
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref, "");
        changeLang(language);
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        Log.v("onResume", "onResume");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v("onTouch", "onTouch");
        return super.onTouchEvent(event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        getCurrentFrag();
        if(keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot()) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.quit)
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes_str, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Stop the activity
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.noquit, null)
                    .show();

            return true;
        }
        else {
            Log.v("key", "others");
            return super.onKeyDown(keyCode, event);
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        // .... other stuff in my onResume ....
        this.doubleBackToExitPressedOnce = false;
    }

    @Override
    public void onBackPressed() {
        String[] fmtagschk = {"Home", "Browse", "Downloads", "Share", "Info"};
        if (Arrays.asList(fmtagschk).contains(getCurrentFrag())) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            new Runnable() {
                // Spin up new runnable to reset the mIsBackEnabled var after 3 seconds
                @Override
                public void run() {
                    CountDownTimer cdt = new CountDownTimer(3000, 3000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            // I don't want to do anything onTick
                        }

                        @Override
                        public void onFinish() {
                            doubleBackToExitPressedOnce = false;
                        }
                    }.start();
                }
            }.run();

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.twice), Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private String getCurrentFrag() {
        String res = "";
        String[] fmtags = {"Home", "Browse", "Downloads", "Share", "Info", "Other"};
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fm = fragmentManager.findFragmentById(R.id.frame_container);
        if (fm instanceof HomeFragment) {
            Log.v("fragment", "home~");
            res = fmtags[0];
        } else if (fm instanceof BrowseFragment) {
            Log.v("fragment", "browse~");
            res = fmtags[1];
        } else if (fm instanceof DownloadsFragment) {
            Log.v("fragment", "downloads~");
            res = fmtags[2];
        } else if (fm instanceof ShareFragment) {
            Log.v("fragment", "share~");
            res = fmtags[3];
        } else if (fm instanceof InfoFragment) {
            Log.v("fragment", "info~");
            res = fmtags[4];
        } else {
            Log.v("fragment", "others");
            res = fmtags[5];
        }
        return res;
    }

}
