package com.simplytapp.demo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.simplytapp.demo.BuildConfig;
import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;
import com.simplytapp.demo.VirtualCardMessenger;
import com.simplytapp.demo.common.logger.Log;
import com.simplytapp.demo.json.Card;
import com.simplytapp.virtualcard.ApduService;
import com.simplytapp.virtualcard.ApprovalData;
import com.simplytapp.virtualcard.DefaultCredentials;
import com.simplytapp.virtualcard.VirtualCard;
import com.simplytapp.virtualcard.VirtualCardBuilder;
import com.simplytapp.virtualcard.VirtualCardMessaging;

import java.io.IOException;

public abstract class CardFragment extends Fragment implements VirtualCardMessenger.OnCardMessageListener {

    private static final String TAG = CardFragment.class.getSimpleName();

    protected static final String NEW_LINE = "\n";
    private static final String EXCEPTION_MESSAGE_BAD_CREDENTIAL = "BAD_CREDENTIAL";
    private static final String EXCEPTION_MESSAGE_BAD_DEVICE = "BAD_DEVICE";

    protected String consumerKey;
    protected String consumerSecret;
    protected String accessToken;
    protected String tokenSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            final Bundle arguments = getArguments();
            consumerKey = arguments.getString(Constants.KEY_CONSUMER_KEY);
            consumerSecret = arguments.getString(Constants.KEY_CONSUMER_SECRET);
            accessToken = arguments.getString(Constants.KEY_ACCESS_TOKEN);
            tokenSecret = arguments.getString(Constants.KEY_TOKEN_SECRET);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Activity activity = getActivity();
            if (activity != null) {
                final Context applicationContext = activity.getApplicationContext();
                final CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(applicationContext));
                if (cardEmulation.categoryAllowsForegroundPreference(CardEmulation.CATEGORY_PAYMENT)) {
                    final ComponentName paymentServiceComponent = new ComponentName(applicationContext, ApduService.class);
                    cardEmulation.setPreferredService(activity, paymentServiceComponent);
                }
                }
            }
        }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Activity activity = getActivity();
            if (activity != null) {
                final CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(activity.getApplicationContext()));
                cardEmulation.unsetPreferredService(activity);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.KEY_CONSUMER_KEY, consumerKey);
        outState.putString(Constants.KEY_CONSUMER_SECRET, consumerSecret);
        outState.putString(Constants.KEY_ACCESS_TOKEN, accessToken);
        outState.putString(Constants.KEY_TOKEN_SECRET, tokenSecret);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void showPinDialog(final ApprovalData approvalData, final VirtualCard virtualCard, final String dialogMessage) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    new PinDialog(dialogMessage, null, activity).show(
                            new PinDialog.ResultHandler() {
                                @Override
                                public void pinSet(String pin) {
                                    try {
                                        ((ApprovalData.StringData) approvalData.getApprovalData()).setAnswer(pin);
                                        virtualCard.messageApproval(true, approvalData);
                                    } catch (IOException e) {
                                        Log.e(TAG, "Unable to approve PIN. - " + e.getMessage());
                                    }
                                }

                                @Override
                                public void pinCancelled() {
                                    virtualCard.messageApproval(false, null);
                                }
                            }
                    );
                }
            });
        }
    }

    @Override
    public void onCardTransactionEnded() {
        final Activity activity = getActivity();
        if (activity != null) {
            // Get instance of Vibrator from current Context
            final Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 400 milliseconds
            vibrator.vibrate(400);
        }
    }

    protected void setupVirtualCard(Card cardData) {
        final VirtualCardMessaging virtualCardMessenger = new VirtualCardMessenger(this);
        // set the default messenger
        VirtualCard.setDefaultVirtualCardMessaging(virtualCardMessenger);
        VirtualCard virtualCard = ApduService.getVirtualCard();

        final String message = getArguments().getString(com.simplytapp.virtualcard.Constants.INTENT_MESSAGE_KEY, "");
        if (com.simplytapp.virtualcard.Constants.CARD_LOAD_FAILED_VALUE.equals(message)) {
            handleCardLoadFailureFromTapError();
        }

        if (virtualCard == null || !virtualCard.getVirtualCardId().equals(cardData.getId())) {
            android.util.Log.d(TAG, "setupVirtualCard() - card.load()");
            // Create a card with data from AdminApi console
            // See wiki for help:
            // http://wiki.simplytapp.com/home/using-admin-api-console
            //
            // Values are found within your AdminApi Console. Click Get APP
            // Credentials.
            //
            // card agent code hash is found inside the Issuer Entity portion of the
            // web portal
            //
            try {
                DefaultCredentials.getSetter().walletAccessToken(accessToken)
                        .walletTokenSecret(tokenSecret)
                        .mobileAppConsumerKey(consumerKey)
                        .mobileAppConsumerSecret(consumerSecret)
                        .store(getActivity().getApplicationContext());
            } catch (IOException e) {
            }
            final VirtualCard card = VirtualCardBuilder
                    .cardId(cardData.getId())
                    .cardAgentCodeHash(cardData.getHash())
                    .context(getActivity().getApplicationContext())
                    .virtualCardMessaging(virtualCardMessenger)
                    .build();

            // now load the card...
            // this requires network connectivity and is blocking
            // so we must run this in the background (use a thread, AsyncTask, etc)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        card.load();
                        final StringBuilder cardLoadLogMessage = new StringBuilder();
                        cardLoadLogMessage.append("========================");
                        cardLoadLogMessage.append(NEW_LINE);
                        cardLoadLogMessage.append("==Loading Card From Server==");
                        cardLoadLogMessage.append(NEW_LINE);
                        cardLoadLogMessage.append("========================");
                        Log.i(TAG, cardLoadLogMessage.toString());
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load", e);
                        final String message = e.getMessage();
                        if (EXCEPTION_MESSAGE_BAD_CREDENTIAL.equals(message)) {
                            startCredentialsActivity();
                        }
                        if (EXCEPTION_MESSAGE_BAD_DEVICE.equals(message)) {
                            postToast(getText(R.string.bad_device_error_message));
                        }
                    }
                }
            }).start();
        } else {
            android.util.Log.d(TAG, "setupVirtualCard() - card already loaded, updated virtualcard attributes");
            updateVirtualCardAttributesAndCardUI(accessToken, tokenSecret, virtualCard, virtualCardMessenger);
        }
    }

    private void updateVirtualCardAttributesAndCardUI(final String accessToken, final String tokenSecret, final VirtualCard virtualCard, final VirtualCardMessaging virtualCardMessenger) {
        virtualCard.getCredentialSetter().mobileAppConsumerKey(consumerKey)
                .mobileAppConsumerSecret(consumerSecret)
                .walletAccessToken(accessToken)
                .walletTokenSecret(tokenSecret)
                .virtualCardMessaging(virtualCardMessenger)
                .set();

        final String message = getArguments().getString(com.simplytapp.virtualcard.Constants.INTENT_MESSAGE_KEY, "");
        if (com.simplytapp.virtualcard.Constants.CARD_TRANSACTION_ENDED_VALUE.equals(message)) {
            getArguments().remove(com.simplytapp.virtualcard.Constants.INTENT_MESSAGE_KEY);
            onCardTransactionEnded();
            if (BuildConfig.DEBUG) {
                // log the message
                android.util.Log.d(TAG, "updateVirtualCardAttributesAndCardUI() - message = " + message);
            }
        }
        onCardUpdated(virtualCard);
    }

    protected void postToast(final CharSequence text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected abstract void updateContent(VirtualCard virtualCard);

    protected abstract void handleCardLoadFailureFromTapError();

    protected void startCredentialsActivity() {
        final Activity activity = getActivity();
        if (activity != null) {
            //credentials are incorrect.  let's get new ones now
            activity.startActivityForResult(new Intent(activity, CredentialsActivity.class), Constants.REQUEST_CODE_GET_CREDENTIALS);
        }
    }
}