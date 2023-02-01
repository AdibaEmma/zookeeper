package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.ServerCnxn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OAuthAuthenticationProvider implements AuthenticationProvider{

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthenticationProvider.class.getName());
    private static final String OAUTH_PROVIDER = System.getProperty("zookeeper.OAuthAuthenticationProvider.oauth");
    

    @Override
    public String getScheme() {
        return "oauth";
    }

    @Override
    public Code handleAuthentication(ServerCnxn cnxn, byte[] authData) {
        try {
            // Parse the authData to get the OAuth token
            String token = new String(authData, StandardCharsets.UTF_8);

            // Validate the OAuth token
            if (!validateToken(token)) {
            // If the token is not valid, close the connection
                cnxn.sendCloseSession();
                return Code.AUTHFAILED;
            }

            // If the token is valid, set the appropriate authentication credentials for the connection
            cnxn.addAuthInfo(new Id("oauth", token));
        }catch (Exception e) {
            LOGGER.error("Invalid toke", e);
        }
        return Code.OK;
        }

    @Override
    public boolean matches(String id, String aclExpr) {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public boolean isValid(String id) {
        return false;
    }

    private boolean validateToken(String token) {
    try {

        // todo: how to validate token in keycloak
        // Make a call to the OAuth server's API to validate the token
        URL url = new URL("http://localhost:8800/realms/zookeeper-oauth/protocol/openid-connect/token" + token);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        return responseCode == HttpURLConnection.HTTP_OK;
    } catch (Exception e) {
        // Log the exception and return false if there was an error during the API call
        LOGGER.error("Error while validating the token: ", e);
        return false;
    }
}
    
}
