package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.common.KafkaException;
import org.apache.zookeeper.common.security.auth.AuthenticateCallbackHandler;
import org.apache.zookeeper.common.security.auth.SaslExtensions;
import org.apache.zookeeper.common.security.auth.SaslExtensionsCallback;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerToken;
import org.apache.zookeeper.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.apache.zookeeper.server.auth.OAuthModule.RAISE_UNSUPPORTED_CB_EXCEPTION_FLAG;

public class OAuthCallbackHandler implements AuthenticateCallbackHandler {
    private final OAuthBearerToken[] tokens;
    private int index = 0;
    private int extensionsIndex = 0;
    private final SaslExtensions[] extensions;

    public OAuthCallbackHandler(OAuthBearerToken[] tokens, SaslExtensions[] extensions) {
        this.tokens = tokens;
        this.extensions = extensions;
    }

    @Override
    public void configure(Map<String, ?> configs, String saslMechanism, List<AppConfigurationEntry> jaasConfigEntries) {

    }

    @Override
    public void close() {

    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
                if (callback instanceof OAuthBearerTokenCallback)
                    try {
                        handleCallback((OAuthBearerTokenCallback) callback);
                    } catch (KafkaException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                else if (callback instanceof SaslExtensionsCallback) {
                    try {
                        handleExtensionsCallback((SaslExtensionsCallback) callback);
                    } catch (KafkaException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                } else
                    throw new UnsupportedCallbackException(callback);
            }
    }

    private void handleCallback(OAuthBearerTokenCallback callback) throws IOException {
            if (callback.token() != null)
                throw new IllegalArgumentException("Callback had a token already");
            if (tokens.length > index)
                callback.token(tokens[index++]);
            else
                throw new IOException("no more tokens");
        }

        private void handleExtensionsCallback(SaslExtensionsCallback callback) throws IOException, UnsupportedCallbackException {
            if (extensions.length > extensionsIndex) {
                SaslExtensions extension = extensions[extensionsIndex++];

                if (extension == RAISE_UNSUPPORTED_CB_EXCEPTION_FLAG) {
                    throw new UnsupportedCallbackException(callback);
                }

                callback.extensions(extension);
            } else
                throw new IOException("no more extensions");
        }
}
