package org.apache.zookeeper.server.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.zookeeper.common.security.auth.AuthenticateCallbackHandler;
import org.apache.zookeeper.common.security.auth.SaslExtensions;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

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

    private final Map<String, String> body = new HashMap<>();

    OAuthBearerToken[] tokens = new OAuthBearerToken[] {oAuthBearerTokens(), oAuthBearerTokens(), oAuthBearerTokens()};

    SaslExtensions[] extensions = new SaslExtensions[] {saslExtensions(),
            saslExtensions(), saslExtensions()};
    private final AuthenticateCallbackHandler callbackHandler = new OAuthCallbackHandler(tokens, extensions);
    private final OAuthBearerToken tokenRequiringCommit = null;
    private final OAuthBearerToken myCommittedToken = null;
    private final SaslExtensions extensionsRequiringCommit = null;
    private final SaslExtensions myCommittedExtensions = null;
    private LoginState loginState;

    private Subject subject;
    private String authEndpoint;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        if(options.containsKey("oauth.client-id")){
            LOG.info("Options in jaas config contains the key, {}", options.get("oauth.client-id"));
            this.subject = subject;
            String username = (String) options.get("oauth.client-id");
            this.subject.getPublicCredentials().add(username);
            String password = (String) options.get("oauth.client-secret");
            this.subject.getPrivateCredentials().add(password);
            authEndpoint = (String) options.get("oauth.auth-endpoint");
            body.put("auth-endpoint", authEndpoint);
            body.put("client_id", username);
            body.put("client_secret", password);
            body.put("grant_type","client_credentials");
            body.put("scope", "email");
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
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(encodeParamValues(body).getBytes());
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            LOG.info("\n Sending {} request to URL {}: ", con.getRequestMethod(), url);
            LOG.info("Request body: {}", body);
            System.out.println("Response Code : " + responseCode);
        
            InputStream inputStream;
            String respd = "";
            try
            {
                inputStream = con.getInputStream();
            } catch (Exception e)
            {
                inputStream = con.getErrorStream();
            }

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
            {
                respd = response.append(inputLine).toString();

                //Print contents of the response from call here
                LOG.info("Response: {}", respd);
            }
            in.close();

            List<String> keys = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(respd);
            Iterator<String> iterator = jsonNode.fieldNames();
            iterator.forEachRemaining(e -> keys.add(e));

            boolean hasAccessToken = keys.stream().anyMatch(c -> c.equals("access_token"));
            LOG.info("Response has access_token key: {}", hasAccessToken);
            if (hasAccessToken) loginState = LoginState.COMMITTED;
            return hasAccessToken;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
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
        return getoAuthBearerToken();
    }

    public static OAuthBearerToken getoAuthBearerToken() {
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

        public static String encodeParamValues(Map<String, String> map) {
    StringBuilder encodedParam = new StringBuilder();
    try  {
        List<String> list = new LinkedList<>(map.keySet());

        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();)
        {
            String param = iterator.next();

            if(param == null || param.isEmpty() || param.trim().isEmpty())
            {
                param = "";
            }

            encodedParam.append(param).append("=").append(URLEncoder.encode(map.get(param), "UTF-8"));

            if (iterator.hasNext())
            {
                encodedParam.append("&");
            }

        }

    } catch (UnsupportedEncodingException ex)
    {
        ex.printStackTrace();
    }
        return encodedParam.toString();
    }
}
