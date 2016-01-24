package com.simplytapp.demo.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CredentialsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_creds_activity);

        final Button okButton = (Button) findViewById(R.id.ok_button);
        final EditText credentialsEditText = (EditText) findViewById(R.id.credentials_editText);
        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String credentials = defaultSharedPreferences.getString(Constants.KEY_CREDENTIALS, null);
        if (!TextUtils.isEmpty(credentials)) {
            credentialsEditText.setText(credentials);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCredentials(credentialsEditText.getText().toString())) {
                    final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString(Constants.KEY_CREDENTIALS, credentialsEditText.getText().toString()).apply();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(CredentialsActivity.this, getText(R.string.credentials_format_error_message), Toast.LENGTH_LONG).show();
                }
            }
        });
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
                return true;
            }
        }
        return false;
    }
}