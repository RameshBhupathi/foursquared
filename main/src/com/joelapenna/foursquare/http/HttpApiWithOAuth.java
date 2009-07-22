/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquare.http;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquare.error.FoursquareCredentialsError;
import com.joelapenna.foursquare.error.FoursquareError;
import com.joelapenna.foursquare.error.FoursquareException;
import com.joelapenna.foursquare.error.FoursquareParseException;
import com.joelapenna.foursquare.parsers.AbstractParser;
import com.joelapenna.foursquare.parsers.Parser;
import com.joelapenna.foursquare.types.FoursquareType;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class HttpApiWithOAuth extends HttpApi {
    protected static final String TAG = "HttpApiWithOAuth";
    protected static final boolean DEBUG = Foursquare.DEBUG;

    private OAuthConsumer mConsumer;

    public HttpApiWithOAuth(DefaultHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public FoursquareType doHttpRequest(HttpRequestBase httpRequest,
            Parser<? extends FoursquareType> parser) throws FoursquareException, IOException {
        if (DEBUG) Log.d(TAG, "doHttpRequest: " + httpRequest.getURI());
        try {
            if (DEBUG) Log.d(TAG, "Signing request: " + httpRequest.getURI());
            if (DEBUG) Log.d(TAG, "Consumer: " + mConsumer.getConsumerKey() + ", "
                    + mConsumer.getConsumerSecret());
            if (DEBUG) Log.d(TAG, "Token: " + mConsumer.getToken() + ", "
                    + mConsumer.getTokenSecret());
            mConsumer.sign(httpRequest);
        } catch (OAuthMessageSignerException e) {
            if (DEBUG) Log.d(TAG, "OAuthMessageSignerException", e);
            throw new RuntimeException(e);
        } catch (OAuthExpectationFailedException e) {
            if (DEBUG) Log.d(TAG, "OAuthExpectationFailedException", e);
            throw new RuntimeException(e);
        }
        HttpResponse response = executeHttpRequest(httpRequest);
        if (DEBUG) Log.d(TAG, "executed HttpRequest for: " + httpRequest.getURI().toString());
        if (response == null) {
            if (DEBUG) Log.d(TAG, "execute() call for the httpRequest generated an exception;");
            return null;
        }

        switch (response.getStatusLine().getStatusCode()) {
            case 200:
                InputStream is = response.getEntity().getContent();
                try {
                    return parser.parse(AbstractParser.createXmlPullParser(is));
                } finally {
                    is.close();
                }
            case 401:
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
                bestEffortConsumeContent(response);
=======
                response.getEntity().consumeContent();
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
=======
                if (DEBUG) Log.d(TAG, EntityUtils.toString(response.getEntity()));
>>>>>>> be0d8a3... Add a debugging message when auth_exchange 401s:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
                throw new FoursquareCredentialsError(response.getStatusLine().toString());
            default:
                if (DEBUG) Log.d(TAG, "Default case for status code reached: "
                        + response.getStatusLine().toString());
<<<<<<< HEAD:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
                bestEffortConsumeContent(response);
=======
                response.getEntity().consumeContent();
>>>>>>> ce6538a... Improve content consumption (so we don't leak threads) in HttpApis.:src/com/joelapenna/foursquare/http/HttpApiWithOAuth.java
                return null;
        }
    }

    @Override
    public String doHttpPost(String url, NameValuePair... nameValuePairs) throws FoursquareError,
            FoursquareParseException, IOException, FoursquareCredentialsError {
        throw new RuntimeException("Haven't written this method yet.");
    }

    public void setOAuthConsumerCredentials(String key, String secret) {
        mConsumer = new CommonsHttpOAuthConsumer(key, secret, SignatureMethod.HMAC_SHA1);
    }

    public void setOAuthTokenWithSecret(String token, String tokenSecret) {
        verifyConsumer();
        if (token == null && tokenSecret == null) {
            if (DEBUG) Log.d(TAG, "Resetting consumer due to null token/secret.");
            String consumerKey = mConsumer.getConsumerKey();
            String consumerSecret = mConsumer.getConsumerSecret();
            mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret,
                    SignatureMethod.HMAC_SHA1);
        } else {
            mConsumer.setTokenWithSecret(token, tokenSecret);
        }
    }

    public boolean hasOAuthTokenWithSecret() {
        verifyConsumer();
        return (mConsumer.getToken() != null) && (mConsumer.getTokenSecret() != null);
    }

    private void verifyConsumer() {
        if (mConsumer == null) {
            throw new IllegalStateException(
                    "Cannot call method without setting consumer credentials.");
        }
    }
}