package com.simplytapp.demo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.simplytapp.demo.BuildConfig;
import com.simplytapp.demo.Constants;
import com.simplytapp.demo.R;
import com.simplytapp.demo.api.CardService;
import com.simplytapp.demo.api.oauth.okhttp.OAuthInterceptor;
import com.simplytapp.demo.api.okhttp.LoggingInterceptor;
import com.simplytapp.demo.common.logger.Log;
import com.simplytapp.demo.json.Card;
import com.simplytapp.demo.json.CardListRequest;
import com.simplytapp.demo.json.CardListResponse;
import com.simplytapp.demo.json.RequestData;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

public class ManageCardsFragment extends Fragment {

    private static final String TAG = ManageCardsFragment.class.getSimpleName();

    private static final String KEY_ACTIVE_CARDS = "com.simplytapp.demo.ACTIVE_CARDS";
    private static final String NEW_LINE = "\n";

    private static final CardListRequest CARD_LIST_REQUEST = new CardListRequest(new RequestData(CardListRequest.COMMAND_GET_CARD_LIST));

    private CardAdapter mCardAdapter;
    private CardService mCardService;

    private View mEmptyTextView;
    private View mProgressBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Card> mActiveCards;

    public static ManageCardsFragment newInstance(String consumerKey, String consumerSecret, String accessToken, String tokenSecret, String cardId) {
        final ManageCardsFragment fragment = new ManageCardsFragment();
        final Bundle arguments = new Bundle();
        arguments.putString(Constants.KEY_CONSUMER_KEY, consumerKey);
        arguments.putString(Constants.KEY_CONSUMER_SECRET, consumerSecret);
        arguments.putString(Constants.KEY_ACCESS_TOKEN, accessToken);
        arguments.putString(Constants.KEY_TOKEN_SECRET, tokenSecret);
        arguments.putString(Constants.KEY_CARD_ID, cardId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        final Bundle arguments = getArguments();

        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(10, TimeUnit.SECONDS);
        okHttpClient.setWriteTimeout(10, TimeUnit.SECONDS);
        okHttpClient.networkInterceptors().add(new OAuthInterceptor(arguments.getString(Constants.KEY_CONSUMER_KEY),
                arguments.getString(Constants.KEY_CONSUMER_SECRET),
                arguments.getString(Constants.KEY_ACCESS_TOKEN),
                arguments.getString(Constants.KEY_TOKEN_SECRET)));

        if (BuildConfig.DEBUG) {
            okHttpClient.interceptors().add(new LoggingInterceptor());
        }

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(getString(R.string.api_url) + CardService.API_BASE_PATH)
                .setClient(new OkClient(okHttpClient))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        mCardService = restAdapter.create(CardService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_manage_cards, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mEmptyTextView = rootView.findViewById(R.id.emptyText);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        int scrollPosition = 0;
        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            mActiveCards = new ArrayList<>();
            mCardService.getCardList(CARD_LIST_REQUEST, new Callback());
            mRecyclerView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mActiveCards = savedInstanceState.getParcelableArrayList(KEY_ACTIVE_CARDS);
            if (mActiveCards == null || mActiveCards.isEmpty()) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mEmptyTextView.setVisibility(View.GONE);
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

        mCardAdapter = new CardAdapter(getActivity(), mActiveCards);
        mRecyclerView.setAdapter(mCardAdapter);
        updateSelectedCard();

        mRecyclerView.addOnItemTouchListener(
                new CardAdapter.RecyclerItemClickListener(getActivity(), new CardAdapter.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mCardAdapter.setSelectedItem(position);
                        final Card activeCard = mActiveCards.get(position);

                        final FragmentActivity activity = getActivity();
                        if (activity != null) {
                            final Context applicationContext = activity.getApplicationContext();
                            final Intent intent = new Intent(applicationContext, MainActivity.class);
                            intent.putExtra(Constants.KEY_ACTIVE_CARD, activeCard);
                            startActivity(intent);

                            final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                            final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                            editor.putString(Constants.KEY_ACTIVE_CARD_JSON, new Gson().toJson(activeCard));
                            editor.apply();
                        }
                    }
                })
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_ACTIVE_CARDS, mActiveCards);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.manage_cards, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_refresh == item.getItemId()) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            refreshCards();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshCards() {
        mCardService.getCardList(CARD_LIST_REQUEST, new Callback());
    }

    private void updateSelectedCard() {
        final String cardId = getArguments().getString(Constants.KEY_CARD_ID, null);
        if (!TextUtils.isEmpty(cardId)) {
            for (int i = 0; i < mActiveCards.size(); i++) {
                final Card card = mActiveCards.get(i);
                if (cardId.equals(card.getId())) {
                    mCardAdapter.setSelectedItem(i);
                    mLayoutManager.scrollToPosition(i);
                    break;
                }
            }
        }
    }

    class Callback implements retrofit.Callback<CardListResponse> {
        @Override
        public void success(CardListResponse cardListResponse, Response response) {

            if (cardListResponse.getStatus() != 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                final List<Card> cards = cardListResponse.getData().getCards();

                if (cards.isEmpty()) {
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.GONE);

                    final StringBuilder cardsLoadLogMessage = new StringBuilder();
                    cardsLoadLogMessage.append("========================");
                    cardsLoadLogMessage.append(NEW_LINE);
                    cardsLoadLogMessage.append("=Loading Cards From Server=");
                    cardsLoadLogMessage.append(NEW_LINE);
                    cardsLoadLogMessage.append("========================");
                    cardsLoadLogMessage.append(NEW_LINE);

                    mActiveCards = new ArrayList<>(cards.size());
                    for (Card card : cards) {
                        if (!card.isDisabled()) {
                            mActiveCards.add(card);
                            cardsLoadLogMessage.append("===");
                            cardsLoadLogMessage.append(card.getPan());
                            cardsLoadLogMessage.append("===");
                            cardsLoadLogMessage.append(NEW_LINE);
                        }
                    }

                    mCardAdapter = new CardAdapter(getActivity(), mActiveCards);
                    mRecyclerView.setAdapter(mCardAdapter);
                    mRecyclerView.setVisibility(View.VISIBLE);

                    updateSelectedCard();

                    cardsLoadLogMessage.append("========================");
                    cardsLoadLogMessage.append(NEW_LINE);
                    cardsLoadLogMessage.append("======Cards Loaded======");
                    Log.i(TAG, cardsLoadLogMessage.toString());
                }

            }
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG, "Failed to load", error);
        }
    }
}