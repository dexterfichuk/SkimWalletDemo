package com.simplytapp.demo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.text.TextUtils;

import com.simplytapp.virtualcard.ApduService;
import com.simplytapp.virtualcard.BaseCardBroadcastReceiver;
import com.simplytapp.virtualcard.Constants;

import com.simplytapp.demo.ui.MainActivity;
import com.simplytapp.virtualcard.VirtualCard;

public class CardBroadcastReceiver extends BaseCardBroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;


    protected void handleAdpuServiceIntent(Context context, Intent intent) {
        final String message = intent.getExtras().getString(Constants.INTENT_MESSAGE_KEY, "");
        if (BuildConfig.DEBUG) {
            // log the message
            android.util.Log.d("CardBroadcastReceiver", "message = " + message);
        }
        switch (message) {
            case Constants.CARD_LOADED_VALUE:
                //TODO: ADD SPECIFIC PROCESSING FOR "CARD_LOADED"
                break;
            case Constants.CARD_STATE_ERROR_VALUE:
                //TODO: ADD SPECIFIC PROCESSING FOR "CARD_STATE_ERROR"
            case Constants.CARD_LOAD_FAILED_VALUE:
                //TODO: ADD SPECIFIC PROCESSING FOR "CARD_LOAD_FAILED"
            case Constants.CARD_TRANSACTION_ENDED_VALUE: {
                // TODO: INVESTIGATE VIRTUAL CARD BROADCASTS MESSAGES AND WHY/WHEN THEY ARE SENT
                final CardEmulation cardEmulation = CardEmulation.getInstance(NfcAdapter.getDefaultAdapter(context));
                final ComponentName paymentServiceComponent = new ComponentName(context, ApduService.class);

                if (cardEmulation.isDefaultServiceForCategory(paymentServiceComponent, CardEmulation.CATEGORY_PAYMENT)) {
                    final String cardId = intent.getExtras().getString(Constants.CARD_ID_KEY);
                    if (!TextUtils.isEmpty(cardId)) {
                        final VirtualCard card = VirtualCard.getLoadedVirtualCard(cardId);
                        if (card != null) {
                            card.deactivate();
                        }
                    }

                    //start activity
                    final Intent i = new Intent(context, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                    i.putExtras(intent.getExtras());
                    context.startActivity(i);
                }
                break;
            }
        }
    }

    protected void handleWakeupAlarmIntent(Context context, Intent intent){
        if(intent.getExtras() == null) return;
        final String message = intent.getExtras().getString(Constants.INTENT_MESSAGE_KEY);
        if(Constants.CARD_STATE_ERROR_VALUE.equals(message) || Constants.CARD_LOAD_FAILED_VALUE.equals(message)){
            Intent newIntent = new Intent(context, MainActivity.class);
            newIntent.putExtras(intent.getExtras());
            Notification.Builder builder = new Notification.Builder(context);
            final Resources resources = context.getResources();
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentInfo(resources.getString(R.string.notification_content_info_string));
            builder.setContentTitle(resources.getString(R.string.notification_content_title_string));
            builder.setContentText(resources.getString(R.string.notification_content_text_string));
            builder.setContentIntent(PendingIntent.getActivity(context, Activity.RESULT_OK, newIntent, PendingIntent.FLAG_ONE_SHOT));
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
        }
    }
}