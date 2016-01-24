package com.simplytapp.demo;

import android.content.Intent;

import com.simplytapp.demo.common.logger.Log;
import com.simplytapp.demo.ui.MainActivity;
import com.simplytapp.virtualcard.ApduService;
import com.simplytapp.virtualcard.ApprovalData;
import com.simplytapp.virtualcard.Constants;
import com.simplytapp.virtualcard.VirtualCard;
import com.simplytapp.virtualcard.VirtualCardMessaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualCardMessenger implements VirtualCardMessaging {

    private static final String TAG = VirtualCardMessenger.class.getSimpleName();

    private static final String NEW_LINE = "\n";

    private OnCardMessageListener onCardMessageListener;

    public VirtualCardMessenger(OnCardMessageListener onCardMessageListener) {
        this.onCardMessageListener = onCardMessageListener;
    }

    @Override
    public void virtualCardMessage(final VirtualCardMessaging.Message message) {
        final VirtualCard virtualCard = message.getVirtualCard();
        switch (message.getCode()) {
            case VirtualCardMessaging.CARD_CREATION_COMPLETED:
                // when card is created, then activate the card agent,
                // then connect it to the ApduService,
                // then ask the user if they want this app the default
                // app
                // for servicing the reader
                virtualCard.activate();
                ApduService.setVirtualCard(virtualCard);

                final StringBuilder cardCreationLogMessage = new StringBuilder();
                cardCreationLogMessage.append("========================");
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("=======Card Created======");
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("========================");
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Id: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardId());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Num: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardNumber());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Exp: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardExpDate());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Type: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardType());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Version: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardSpecVersion());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Agent Hash: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardAgentHash());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("==Card Logo: ");
                cardCreationLogMessage.append(virtualCard.getVirtualCardLogo());
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("========================");
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("======Card Activating======");
                cardCreationLogMessage.append(NEW_LINE);
                cardCreationLogMessage.append("========================");
                Log.i(TAG, cardCreationLogMessage.toString());
                break;
            case VirtualCardMessaging.CARD_ACTIVATE_COMPLETED:
                if (onCardMessageListener != null) {
                    onCardMessageListener.onCardUpdated(virtualCard);
                }

                final StringBuilder cardActivationCompletedLogMessage = new StringBuilder();
                cardActivationCompletedLogMessage.append("========================");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("========================");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("========================");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("======Card Activated======");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("======Ready To Tap=======");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("========================");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("========================");
                cardActivationCompletedLogMessage.append(NEW_LINE);
                cardActivationCompletedLogMessage.append("========================");
                Log.i(TAG, cardActivationCompletedLogMessage.toString());
                break;
            case VirtualCardMessaging.TRANSACTION_ENDED:
                if (onCardMessageListener != null) {
                    onCardMessageListener.onCardTransactionEnded();
                }

                virtualCard.deactivate();

                //bring this activity to the foreground
                final Intent intent = new Intent(virtualCard.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                intent.putExtra(Constants.INTENT_MESSAGE_KEY, Constants.CARD_TRANSACTION_ENDED_VALUE);
                virtualCard.getContext().startActivity(intent);

                final StringBuilder cardTransactionEndedLogMessage = new StringBuilder();
                cardTransactionEndedLogMessage.append("========================");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("========================");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("========================");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("===Ready To Tap Again====");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("========================");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("========================");
                cardTransactionEndedLogMessage.append(NEW_LINE);
                cardTransactionEndedLogMessage.append("========================");
                Log.i(TAG, cardTransactionEndedLogMessage.toString());
                break;
            case VirtualCardMessaging.CARD_DEACTIVATE_COMPLETED:
                virtualCard.activate();
                break;
            case VirtualCardMessaging.POST_MESSAGE:
                final String postMessage = "Card " + virtualCard.getVirtualCardNumber() + ":\n" + message.getMessage();
                if (onCardMessageListener != null) {
                    onCardMessageListener.postMessage(postMessage);
                }

                //MCBP cards sometimes need a pin initialized or used
                if (message.getApprovalData() != null) {
                    ApprovalData approvalData = message.getApprovalData();
                    Object data = approvalData.getApprovalData();
                    if (data.getClass().equals(ApprovalData.StringData.class)) {
                        ApprovalData.StringData stringData = (ApprovalData.StringData) data;
                        if (stringData.getType() == ApprovalData.StringData.NUMERIC && stringData.getMinLen() <= 4 && stringData.getMaxLen() >= 4) {
                            if (onCardMessageListener != null) {
                                //
                                //launch pin dialog
                                //
                                onCardMessageListener.showPinDialog(approvalData, virtualCard, message.getMessage());
                            } else {
                                virtualCard.messageApproval(false, null);
                            }
                        }
                    }
                }

                //reload on these events
                Pattern pattern = Pattern.compile("^(.*?)Personalized(.*?)$", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(message.getMessage());
                if (matcher.find()) {
                    if (onCardMessageListener != null) {
//                        onCardMessageListener.refreshCards();
                    }
                }
                pattern = Pattern.compile("^(.*?)Terminated(.*?)$", Pattern.MULTILINE);
                matcher = pattern.matcher(message.getMessage());
                if (matcher.find()) {
                    if (onCardMessageListener != null) {
//                        onCardMessageListener.refreshCards();
                    }
                }

                final StringBuilder cardPostMessageLogMessage = new StringBuilder();
                cardPostMessageLogMessage.append("========================");
                cardPostMessageLogMessage.append(NEW_LINE);
                cardPostMessageLogMessage.append("========================");
                cardPostMessageLogMessage.append(NEW_LINE);
                cardPostMessageLogMessage.append("=====Message From:======");
                cardPostMessageLogMessage.append(NEW_LINE);
                cardPostMessageLogMessage.append("========");
                cardPostMessageLogMessage.append(virtualCard.getVirtualCardNumber());
                cardPostMessageLogMessage.append("====");
                cardPostMessageLogMessage.append(NEW_LINE);
                cardPostMessageLogMessage.append("========================");
                cardPostMessageLogMessage.append(NEW_LINE);
                cardPostMessageLogMessage.append("========================");
                Log.i(TAG, cardPostMessageLogMessage.toString());
                Log.d(TAG, postMessage);
                break;
            default:
                break;
        }

        // log the message
        Log.d(TAG, message.getMessage() + " cardId = "
                + message.getVirtualCardId() + " code = "
                + message.getCode());
    }

    public interface OnCardMessageListener {
        void onCardUpdated(VirtualCard virtualCard);

        void onCardTransactionEnded();

        void showPinDialog(ApprovalData approvalData, VirtualCard virtualCard, String dialogMessage);

        void postMessage(String message);
    }
}