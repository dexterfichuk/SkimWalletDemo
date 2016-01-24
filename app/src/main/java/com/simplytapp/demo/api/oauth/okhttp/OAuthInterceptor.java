package com.simplytapp.demo.api.oauth.okhttp;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;

import java.io.IOException;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class OAuthInterceptor implements Interceptor {

    private static final String TAG = OAuthInterceptor.class.getSimpleName();

    private final String consumerKey;
    private final String consumerSecret;
    private final String token;
    private final String tokenSecret;

    public OAuthInterceptor(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    @Override
    public com.squareup.okhttp.Response intercept(Interceptor.Chain chain) throws IOException {
        final Request originalRequest = chain.request();

        final OAuthConsumer consumer = new OAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(token, tokenSecret);

        Request signedRequest = null;
        try {
            signedRequest = (Request) consumer.sign(originalRequest).unwrap();
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            Log.e(TAG, "Failed to sign request with OAuth credentials, " + e.getMessage());
        }

        return chain.proceed(signedRequest);
    }
}