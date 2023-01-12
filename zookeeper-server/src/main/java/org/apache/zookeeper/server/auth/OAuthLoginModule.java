package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.common.security.auth.AuthenticateCallbackHandler;
import org.apache.zookeeper.common.security.auth.SaslExtensions;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerToken;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class OAuthLoginModule implements LoginModule {

        private enum LoginState {
        NOT_LOGGED_IN,
        LOGGED_IN_NOT_COMMITTED,
        COMMITTED
    }

    /**
     * The SASL Mechanism name for OAuth 2: {@code OAUTHBEARER}
     */
    public static final String OAUTHBEARER_MECHANISM = "OAUTHBEARER";

    public static final SaslExtensions RAISE_UNSUPPORTED_CB_EXCEPTION_FLAG = null;
    private static final Logger LOG = LoggerFactory.getLogger(OAuthLoginModule.class);
    private static final SaslExtensions EMPTY_EXTENSIONS = new SaslExtensions(Collections.emptyMap());
    private Subject subject = null;

    private JSONObject body = new JSONObject();

    OAuthBearerToken[] tokens = new OAuthBearerToken[] {oAuthBearerTokens(), oAuthBearerTokens(), oAuthBearerTokens()};

    SaslExtensions[] extensions = new SaslExtensions[] {saslExtensions(),
            saslExtensions(), saslExtensions()};
    private AuthenticateCallbackHandler callbackHandler = new OAuthCallbackHandler(tokens, extensions);
    private OAuthBearerToken tokenRequiringCommit = null;
    private OAuthBearerToken myCommittedToken = null;
    private SaslExtensions extensionsRequiringCommit = null;
    private SaslExtensions myCommittedExtensions = null;
    private LoginState loginState;
    private String authEndpoint,username, password = "";

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        
        if(options.containsKey("oauth.client-id")){
            LOG.info("Options in jaas config contains the key, {}", options.get("oauth.client-id"));
            this.subject = subject;
            username = (String) options.get("oauth.client-id");
            this.subject.getPublicCredentials().add(username);
            password = (String) options.get("oauth.client-secret");
            this.subject.getPrivateCredentials().add(password);
            authEndpoint = (String) options.get("oauth.auth-endpoint");
            body.put("auth-endpoint", authEndpoint);
            body.put("client-id", username);
            body.put("client-secret", password);
        }
    }
    
    @Override
    public boolean login() throws LoginException {
        LOG.info("Options in jaas config contains auth provider endpoint, {}", authEndpoint);
        try {
            URL url = new URL(authEndpoint);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(50000);
            con.setReadTimeout(100000);
            // Send post request
            if(body != null)
            {
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.write(body.toString().getBytes());
                wr.flush();
                wr.close();
            }
            int responseCode = con.getResponseCode();
            String respd = "";
            LOG.info("\n Sending {} request to URL {}: ", con.getRequestMethod(), url);
            LOG.info("Request body: {}" +  body);
            System.out.println("Response Code : " + responseCode);
        
            InputStream inputStream = null;
            try
            {
                inputStream = con.getInputStream();
            } catch (Exception e)
            {
                inputStream = con.getErrorStream();
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
            String inputLine = null;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
            {
                respd = response.append(inputLine).toString();
            }
            in.close();

            //Print contents of the response from call here

        } catch (Exception e) {
            e.printStackTrace();
        }

        
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        return false;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }


    private OAuthBearerToken oAuthBearerTokens() {
        return new OAuthBearerToken() {
            @Override
            public String value() {
                return null;
            }

            @Override
            public Long startTimeMs() {
                return null;
            }

            @Override
            public Set<String> scope() {
                return null;
            }

            @Override
            public String principalName() {
                return null;
            }

            @Override
            public long lifetimeMs() {
                return 10000000;
            }
        };
    }
     private SaslExtensions saslExtensions() {
        return SaslExtensions.empty();
    }
}
