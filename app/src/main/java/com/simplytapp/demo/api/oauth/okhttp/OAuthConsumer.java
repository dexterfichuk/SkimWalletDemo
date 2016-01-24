package com.simplytapp.demo.api.oauth.okhttp;

import com.squareup.okhttp.Request;

import oauth.signpost.AbstractOAuthConsumer;
import oauth.signpost.http.HttpRequest;

public class OAuthConsumer extends AbstractOAuthConsumer {

    public OAuthConsumer(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret);
    }

    @Override
    protected HttpRequest wrap(Object o) {
        if (!(o instanceof Request)) {
            throw new IllegalArgumentException(
                    "This consumer expects requests of type "
                            + HttpRequest.class.getCanonicalName());
        } else {
            return new RequestAdapter((Request) o);
        }
    }
}