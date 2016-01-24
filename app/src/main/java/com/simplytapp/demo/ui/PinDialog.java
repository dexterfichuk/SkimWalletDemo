package com.simplytapp.demo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simplytapp.demo.R;

public class PinDialog implements TextWatcher {

    private final String TAG = "PINLIB";
    private final Context context;
    private final AlertDialog.Builder alertBuilder;
    private TextView[] digits;
    private AlertDialog dialog;
    private ResultHandler pinResultHandler;
    private EditText pinInput;

    public interface ResultHandler {
        void pinSet(String pin);

        void pinCancelled();
    }

    public void show(final ResultHandler handler) {
        dialog = alertBuilder.show();
        pinResultHandler = handler;

        LinearLayout pinContainer = (LinearLayout) dialog.findViewById(R.id.pin_container);

        pinInput = (EditText) dialog.findViewById(R.id.pin_entry_hidden_focus);

        digits = new TextView[pinContainer.getChildCount()];

        pinInput.requestFocus();
        pinInput.addTextChangedListener(this);


        for (int i = 0; i < digits.length; i++) {
            digits[i] = (TextView) pinContainer.getChildAt(i);
        }


        Button cancelButton = (Button) dialog.findViewById(R.id.pin_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.pinCancelled();
                dialog.dismiss();
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public PinDialog(String inputHint, String cancelButtonText, final Context context) {
        this.context = context;

        View view = LayoutInflater.from(context).inflate(R.layout.pin_input, null);

        if (inputHint != null) {
            ((TextView) view.findViewById(R.id.pin_input_hint)).setText(inputHint);
        }

        if (cancelButtonText != null) {
            ((Button) view.findViewById(R.id.pin_cancel_button)).setText(cancelButtonText);
        }

        alertBuilder = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false);
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d(TAG, "AFTER length=" + s.toString().length());

        int numEnteredDigits = s.length();

        if (numEnteredDigits >= digits.length) {
            String pin = s.toString();
            if (pin.length() != digits.length) {
                // This, in theory, can't happen
                Log.d(TAG, "The impossible happened");
                pin = pin.substring(0, digits.length);
            }
            pinResultHandler.pinSet(pin);
            dialog.dismiss();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // This method is called to notify you that, within s, the count characters beginning
        // at start are about to be replaced by new text with length after. It is an error to
        // attempt to make changes to s from this callback.

        Log.d(TAG, "BEFORE start=" + start + " count=" + count + " after=" + after + " sequence=" + s);

        //
        // Since you can't cut and paste into our tiny invisible box, this callback should get
        // called on every key press.  Knowing this, the count will be 0 if backspace removed
        // a digit and 1 if a new digit was entered.  In the worse case, if the impossible
        // happens, we just cancel the pin dialog.
        //
        if (after > 1 || after < 0 || start < 0 || start >= digits.length) {
            Log.d(TAG, "THIS, IN THEORY, NEVER HAPPENS");
            pinResultHandler.pinCancelled();
            dialog.dismiss();
        }

        digits[start].setText(after == 0 ? "" : "*");
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "ON start=" + start + " count=" + count + " sequence=" + s);
    }

}
