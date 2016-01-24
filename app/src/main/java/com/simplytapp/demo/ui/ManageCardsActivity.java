package com.simplytapp.demo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;

public class ManageCardsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cards);

        if (savedInstanceState == null) {
            final Bundle extras = getIntent().getExtras();
            if (extras != null) {
                final String consumerKey = extras.getString(Constants.KEY_CONSUMER_KEY);
                final String consumerSecret = extras.getString(Constants.KEY_CONSUMER_SECRET);
                final String accessToken = extras.getString(Constants.KEY_ACCESS_TOKEN);
                final String tokenSecret = extras.getString(Constants.KEY_TOKEN_SECRET);
                final String cardId = extras.getString(Constants.KEY_CARD_ID);

                final Fragment fragment = ManageCardsFragment.newInstance(consumerKey, consumerSecret, accessToken, tokenSecret, cardId);
                final FragmentManager fragmentManager = getSupportFragmentManager();
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, fragment).commit();
            }
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}