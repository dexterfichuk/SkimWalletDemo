package com.simplytapp.demo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nispok.snackbar.SnackbarManager;
import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;
import com.simplytapp.demo.common.logger.Log;
import com.simplytapp.demo.common.logger.LogFragment;
import com.simplytapp.demo.common.ui.widget.SlidingTabLayout;
import com.simplytapp.demo.json.Card;
import com.simplytapp.virtualcard.ApduService;
import com.simplytapp.virtualcard.gcm.GoogleCloudMessagingUtil;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String tokenSecret;

    private Card activeCard;

    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;


    public void onClick(View view) {
        Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
        //SmsManager smsManager = SmsManager.getDefault();
        //smsManager.sendTextMessage("+17059772661", null, "<Hey beautiful>", null, null);
        //String number = "17059772661";  // The number on which you want to send SMS
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 62) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+12048179215", null, saltStr, null, null);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter(getSupportFragmentManager()));

        if (savedInstanceState == null) {
            loadApplicationKeys();
            if (!checkCredentials(defaultSharedPreferences.getString(Constants.KEY_CREDENTIALS, null))) {
                final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                editor.remove(Constants.KEY_CREDENTIALS).apply();
            }
            if (defaultSharedPreferences.contains(Constants.KEY_ACTIVE_CARD_JSON)) {
                activeCard = new Gson().fromJson(defaultSharedPreferences.getString(Constants.KEY_ACTIVE_CARD_JSON, null), Card.class);
            }
            checkDefaultServiceForPayment();
        } else {
            accessToken = savedInstanceState.getString(Constants.KEY_ACCESS_TOKEN);
            tokenSecret = savedInstanceState.getString(Constants.KEY_TOKEN_SECRET);
            consumerKey = savedInstanceState.getString(Constants.KEY_CONSUMER_KEY);
            consumerSecret = savedInstanceState.getString(Constants.KEY_CONSUMER_SECRET);
            activeCard = savedInstanceState.getParcelable(Constants.KEY_ACTIVE_CARD);
        }

        // On screen logging via a fragment with a TextView.
        final LogFragment logFragment = new LogFragment();
        final SamplePagerAdapter samplePagerAdapter = (SamplePagerAdapter) mViewPager.getAdapter();
        samplePagerAdapter.addTab(1, getText(R.string.log_tab_title), logFragment);

        updateContent(getIntent().getExtras());

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary_text_icons));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getText(R.string.app_name));
            setSupportActionBar(toolbar);
        }

        // Start GCM registration service
        GoogleCloudMessagingUtil.startGcmRegistration(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Override to capture subsequent requests, since the activity's launchMode is "singleTask"
        super.onNewIntent(intent);
        setIntent(intent);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.KEY_ACTIVE_CARD)) {
                activeCard = extras.getParcelable(Constants.KEY_ACTIVE_CARD);
                updateContent(extras);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE_GET_CREDENTIALS) {
            if (resultCode == Activity.RESULT_OK) {
                final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                final String credentials = defaultSharedPreferences.getString(Constants.KEY_CREDENTIALS, null);
                if (checkCredentials(credentials)) {
                    updateContent(new Bundle());
                }
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        accessToken = savedInstanceState.getString(Constants.KEY_ACCESS_TOKEN);
        tokenSecret = savedInstanceState.getString(Constants.KEY_TOKEN_SECRET);
        consumerKey = savedInstanceState.getString(Constants.KEY_CONSUMER_KEY);
        consumerSecret = savedInstanceState.getString(Constants.KEY_CONSUMER_SECRET);
        activeCard = savedInstanceState.getParcelable(Constants.KEY_ACTIVE_CARD);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        android.util.Log.d(TAG, "onSaveInstanceState()");
        outState.putString(Constants.KEY_ACCESS_TOKEN, accessToken);
        outState.putString(Constants.KEY_TOKEN_SECRET, tokenSecret);
        outState.putString(Constants.KEY_CONSUMER_KEY, consumerKey);
        outState.putString(Constants.KEY_CONSUMER_SECRET, consumerSecret);
        outState.putParcelable(Constants.KEY_ACTIVE_CARD, activeCard);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_manage_cards == item.getItemId()) {
            SnackbarManager.dismiss();

            final Intent intent = new Intent(getApplicationContext(), ManageCardsActivity.class);
            intent.putExtra(Constants.KEY_ACCESS_TOKEN, accessToken);
            intent.putExtra(Constants.KEY_TOKEN_SECRET, tokenSecret);
            intent.putExtra(Constants.KEY_CONSUMER_KEY, consumerKey);
            intent.putExtra(Constants.KEY_CONSUMER_SECRET, consumerSecret);
            if (activeCard != null) {
                intent.putExtra(Constants.KEY_CARD_ID, activeCard.getId());
            }
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }

    private boolean checkCredentials(String data) {
        if (!TextUtils.isEmpty(data)) {
            String consumerKey = null;
            String consumerSecret = null;
            String accessToken = null;
            String tokenSecret = null;

            Pattern pattern = Pattern.compile("^Consumer Key:\\s*(.*?)\\s*$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                consumerKey = matcher.group(1);
            }
            pattern = Pattern.compile("^Consumer Secret:\\s*(.*?)\\s*$", Pattern.MULTILINE);
            matcher = pattern.matcher(data);
            if (matcher.find()) {
                consumerSecret = matcher.group(1);
            }
            pattern = Pattern.compile("^Access Token:\\s*(.*?)\\s*$", Pattern.MULTILINE);
            matcher = pattern.matcher(data);
            if (matcher.find()) {
                accessToken = matcher.group(1);
            }
            pattern = Pattern.compile("^Token Secret:\\s*(.*?)\\s*$", Pattern.MULTILINE);
            matcher = pattern.matcher(data);
            if (matcher.find()) {
                tokenSecret = matcher.group(1);
            }

            if (consumerKey != null && consumerSecret != null && accessToken != null && tokenSecret != null) {
                this.consumerKey = consumerKey;
                this.consumerSecret = consumerSecret;
                this.accessToken = accessToken;
                this.tokenSecret = tokenSecret;
                return true;
            }
        }
        return false;
    }

    private void checkDefaultServiceForPayment() {
        // try to set this app as the default HCE application app
        final CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(getApplicationContext()));
        final ComponentName paymentServiceComponent = new ComponentName(getApplicationContext(), ApduService.class);

        if (!cardEmulation.isDefaultServiceForCategory(paymentServiceComponent, CardEmulation.CATEGORY_PAYMENT)) {
            final Intent intent = new Intent(CardEmulation.ACTION_CHANGE_DEFAULT);
            intent.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT);
            intent.putExtra(CardEmulation.EXTRA_SERVICE_COMPONENT, paymentServiceComponent);
            startActivityForResult(intent, 0);
        }
    }

    private void loadApplicationKeys() {
        try {
            final Bundle metadata = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA | PackageManager.GET_ACTIVITIES).metaData;
            final Resources resources = getResources();
            consumerKey = resources.getString(metadata.getInt(Constants.KEY_CONSUMER_KEY));
            consumerSecret = resources.getString(metadata.getInt(Constants.KEY_CONSUMER_SECRET));
            accessToken = resources.getString(metadata.getInt(Constants.KEY_ACCESS_TOKEN));
            tokenSecret = resources.getString(metadata.getInt(Constants.KEY_TOKEN_SECRET));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to find meta-data", e);
        }
    }

    private void updateContent(final Bundle bundle) {
        final SamplePagerAdapter samplePagerAdapter = (SamplePagerAdapter) mViewPager.getAdapter();
        final CharSequence title = getText(R.string.card_tab_title);

        final Bundle extras = bundle != null ? new Bundle(bundle) : new Bundle();
        extras.putString(Constants.KEY_ACCESS_TOKEN, accessToken);
        extras.putString(Constants.KEY_TOKEN_SECRET, tokenSecret);
        extras.putString(Constants.KEY_CONSUMER_KEY, consumerKey);
        extras.putString(Constants.KEY_CONSUMER_SECRET, consumerSecret);
        if (activeCard != null) {
            extras.putParcelable(Constants.KEY_ACTIVE_CARD, activeCard);
        }

        final CardFragment fragment = TapAndPayFragment.newInstance(extras);
        samplePagerAdapter.addTab(0, title, fragment);

        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.setViewPager(mViewPager);
        }
    }

    class SamplePagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<Fragment> tabs;
        private SparseArray<CharSequence> tabTitles;

        public SamplePagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = new SparseArray<>(2);
            tabTitles = new SparseArray<>(2);
        }

        @Override
        public Fragment getItem(int position) {
            return tabs.get(position);
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }

        public void addTab(int position, CharSequence title, Fragment tab) {
            tabs.put(position, tab);
            tabTitles.put(position, title);
            notifyDataSetChanged();
        }

        public int getItemPosition(Object object) {
            if (tabs.indexOfValue((Fragment) object) >= 0) {
                return POSITION_UNCHANGED;
            }
            return POSITION_NONE;
        }
    }
}