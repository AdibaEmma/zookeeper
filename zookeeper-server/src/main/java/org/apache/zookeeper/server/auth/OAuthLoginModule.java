package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.common.security.auth.AuthenticateCallbackHandler;
import org.apache.zookeeper.common.security.auth.SaslExtensions;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
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

    OAuthBearerToken[] tokens = new OAuthBearerToken[] {oAuthBearerTokens(), oAuthBearerTokens(), oAuthBearerTokens()};

    SaslExtensions[] extensions = new SaslExtensions[] {saslExtensions(),
            saslExtensions(), saslExtensions()};
    private AuthenticateCallbackHandler callbackHandler = new OAuthCallbackHandler(tokens, extensions);
    private OAuthBearerToken tokenRequiringCommit = null;
    private OAuthBearerToken myCommittedToken = null;
    private SaslExtensions extensionsRequiringCommit = null;
    private SaslExtensions myCommittedExtensions = null;
    private LoginState loginState;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        
        if(options.containsKey("oauth.client-id")){
            LOG.info("Options in jaas config contains the key, {}", options.get("oauth.client-id"));
            this.subject = subject;
            String username = (String) options.get("oauth.client-id");
            this.subject.getPublicCredentials().add(username);
            String password = (String) options.get("oauth.client-secret");
            this.subject.getPrivateCredentials().add(password);
        }
    }

    @Override
    public boolean login() throws LoginException {
        //TODO Http call with clieth auth data to authorization endpoing of the provider
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
