package com.simplytapp.demo.ui;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;
import com.simplytapp.demo.json.Card;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Provide views to RecyclerView with data from cards.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private static final String TAG = CardAdapter.class.getSimpleName();

    private final Context context;

    private int selectedItem;
    private List<Card> cards;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView cardTitleTextView;
        private final TextView cardAccountNumberTextView;
        private final TextView cardExpirationDateTextView;

        public ViewHolder(View view) {
            super(view);
            cardTitleTextView = (TextView) view.findViewById(R.id.card_brand_field);
            cardAccountNumberTextView = (TextView) view.findViewById(R.id.card_number_field);
            cardExpirationDateTextView = (TextView) view.findViewById(R.id.card_expiration_date_field);
        }

        public TextView getCardTitleTextView() {
            return cardTitleTextView;
        }

        public TextView getCardAccountNumberTextView() {
            return cardAccountNumberTextView;
        }

        public TextView getCardExpirationDateTextView() {
            return cardExpirationDateTextView;
        }
    }

    public CardAdapter(Context context, List<Card> cards) {
        this.context = context.getApplicationContext();
        this.cards = cards;
        this.selectedItem = -1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final Card card = cards.get(position);
        viewHolder.getCardTitleTextView().setText(card.getBrand());

        final String cardNumber = card.getPan();
        if (!TextUtils.isEmpty(cardNumber)) {
            final int length = cardNumber.length();
            viewHolder.getCardAccountNumberTextView().setText(context.getString(R.string.account_number_mask, cardNumber.substring(length - 4, length)));
        }

        final String expirationDate = card.getExpiration();
        if (!TextUtils.isEmpty(expirationDate)) {
            try {
                final Date date = Constants.DATE_FORMAT_UTC.parse(expirationDate);
                viewHolder.getCardExpirationDateTextView().setText(Constants.DATE_FORMAT_MM_YY.format(date));
            } catch (ParseException ignored) {
            }
        }

        viewHolder.itemView.setActivated(selectedItem == position);
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItem = -1;
        notifyDataSetChanged();
    }

    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(final RecyclerView recyclerView, MotionEvent motionEvent) {
            final View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
                // Give some time to the ripple to finish the effect
                if (mListener != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
                        }
                    }, 200);
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}