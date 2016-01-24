package com.simplytapp.demo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;
import com.simplytapp.demo.json.Card;
import com.simplytapp.virtualcard.VirtualCard;

import java.text.ParseException;
import java.util.Date;

public class TapAndPayFragment extends CardFragment {

    private static final String TAG = TapAndPayFragment.class.getSimpleName();

    private Card activeCard;

    private TextView mCardBrandField;
    private TextView mCardExpirationDateField;
    private TextView mCardNumberField;
    private TextView mTapStatusField;

    private ImageView mContactlessImageView;
    private View mCardContainer;
    private View mProgressBar;

    public static CardFragment newInstance(Bundle extras) {
        final CardFragment fragment = new TapAndPayFragment();
        final Bundle arguments = new Bundle(extras);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            activeCard = getArguments().getParcelable(Constants.KEY_ACTIVE_CARD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tap_and_pay, container, false);
        if (view != null) {
            mTapStatusField = (TextView) view.findViewById(R.id.tap_status_field);
            mCardBrandField = (TextView) view.findViewById(R.id.card_brand_field);
            mCardExpirationDateField = (TextView) view.findViewById(R.id.card_expiration_date_field);
            mCardNumberField = (TextView) view.findViewById(R.id.card_number_field);
            mContactlessImageView = (ImageView) view.findViewById(R.id.contactless_image);
            mCardContainer = view.findViewById(R.id.card_container);
            mProgressBar = view.findViewById(R.id.progressBar);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (activeCard == null) {
            mCardContainer.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mContactlessImageView.setImageResource(R.drawable.contactless_grey);
            mTapStatusField.setText(getText(R.string.tap_and_pay_status_unavailable));
        } else {
            updateContent(activeCard);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret) || TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(tokenSecret)) {
            startCredentialsActivity();
        } else {
            if (activeCard == null) {
                final FragmentActivity activity = getActivity();
                SnackbarManager.show(
                        Snackbar.with(activity.getApplicationContext()) // context
                                .text(R.string.tap_and_pay_status_reason_default_card_not_set) // text to be displayed
                                .actionLabel(R.string.set_default_button) // action button label
                                .actionColorResource(R.color.accent) // action button label color
                                .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE)
                                .swipeToDismiss(false)
                                .actionListener(new ActionClickListener() {
                                    @Override
                                    public void onActionClicked(Snackbar snackbar) {
                                        final Intent intent = new Intent(activity.getApplicationContext(), ManageCardsActivity.class);
                                        intent.putExtra(Constants.KEY_ACCESS_TOKEN, accessToken);
                                        intent.putExtra(Constants.KEY_TOKEN_SECRET, tokenSecret);
                                        intent.putExtra(Constants.KEY_CONSUMER_KEY, consumerKey);
                                        intent.putExtra(Constants.KEY_CONSUMER_SECRET, consumerSecret);
                                        startActivity(intent);
                                    }
                                }) // action button's ActionClickListener
                        , activity); // activity where it is displayed
            } else {
                SnackbarManager.dismiss();
                mTapStatusField.setText(getText(R.string.card_status_activating));
                mProgressBar.setVisibility(View.VISIBLE);
                setupVirtualCard(activeCard);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Constants.KEY_ACTIVE_CARD, activeCard);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void updateContent(VirtualCard virtualCard) {
        mTapStatusField.setText(getText(R.string.tap_and_pay_status_ready_to_tap));
        mContactlessImageView.setImageResource(R.drawable.contactless_blue);
        updateCardDetails(virtualCard.getVirtualCardType(), virtualCard.getVirtualCardExpDate(), virtualCard.getVirtualCardNumber());
        mCardContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void updateContent(Card card) {
        mTapStatusField.setText(getText(R.string.tap_and_pay_status_unavailable));
        mContactlessImageView.setImageResource(R.drawable.contactless_grey);
        updateCardDetails(card.getBrand(), card.getExpiration(), card.getPan());
        mCardContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void updateCardDetails(final String brand, final String expirationDate, final String number) {
        mCardBrandField.setText(brand);

        if (!TextUtils.isEmpty(number)) {
            final int length = number.length();
            mCardNumberField.setText(getString(R.string.account_number_mask, number.substring(length - 4, length)));
        }

        if (!TextUtils.isEmpty(expirationDate)) {
            try {
                final Date date = Constants.DATE_FORMAT_UTC.parse(expirationDate);
                mCardExpirationDateField.setText(Constants.DATE_FORMAT_MM_YY.format(date));
            } catch (ParseException ignored) {
            }
        }
    }

    @Override
    public void onCardUpdated(final VirtualCard virtualCard) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateContent(virtualCard);
                }
            });
        }
    }

    @Override
    public void postMessage(String message) {
        postToast(message);
    }

    @Override
    public void onCardTransactionEnded() {
        super.onCardTransactionEnded();

        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SnackbarManager.show(
                            Snackbar.with(activity.getApplicationContext()) // context
                                    .text(R.string.card_transaction_end) // text to be displayed
                                    .actionLabel(R.string.ok_button_text) // action button label
                                    .actionColorResource(R.color.accent) // action button label color
                                    .dismissOnActionClicked(true) // action button's dismisses snackbar
                            , activity); // activity where it is displayed
                }
            });
        }
    }

    @Override
    protected void handleCardLoadFailureFromTapError() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SnackbarManager.show(
                            Snackbar.with(activity.getApplicationContext()) // context
                                    .text(R.string.error_message_card_load_failure_on_tap) // text to be displayed
                                    .actionLabel(R.string.ok_button_text) // action button label
                                    .actionColorResource(R.color.accent) // action button label color
                                    .dismissOnActionClicked(true) // action button's dismisses snackbar
                            , activity); // activity where it is displayed
                }
            });
        }
    }
}