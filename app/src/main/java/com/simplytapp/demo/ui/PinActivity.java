package com.simplytapp.demo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.simplytapp.demo.R;


public class PinActivity extends Activity {

    private final static String TAG = "PINLIB_DEMO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_activity);
    }

    public void setPinDialog(View view) {
        new PinDialog("Set Pin Code", "Cancel", view.getContext()).show(
                new PinDialog.ResultHandler() {
                    @Override
                    public void pinSet(String pin) {
                        setCurrentPin(pin);
                        showToastMessage("Pin Changed");
                    }

                    @Override
                    public void pinCancelled() {
                        showToastMessage("Pin Set Cancelled");
                    }
                }
        );
    }

    public void verifyPinDialog(View view) {
        new PinDialog("Verify Pin Code", "Cancel", view.getContext()).show(
                new PinDialog.ResultHandler() {
                    @Override
                    public void pinSet(String pin) {
                        String message = pin.equals(getCurrentPin()) ? "Pin Verified" : "Pin Verification Failed";
                        showToastMessage(message);
                    }

                    @Override
                    public void pinCancelled() {
                        showToastMessage("Pin Verification Cancelled");
                    }
                }
        );
    }


    public void showToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void setCurrentPin(String pin) {
        ((TextView) findViewById(R.id.existing_pin)).setText(pin);
    }

    private String getCurrentPin() {
        return ((TextView) findViewById(R.id.existing_pin)).getText().toString();
    }


}
