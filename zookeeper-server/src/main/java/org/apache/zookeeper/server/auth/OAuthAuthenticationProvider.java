package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.server.ServerCnxn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthAuthenticationProvider implements AuthenticationProvider{

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthenticationProvider.class.getName());
    private static final String OAUTH_PROVIDER = System.getProperty("zookeeper.oauth.Provider");
    

    @Override
    public String getScheme() {
        return "oauth";
    }

    @Override
    public Code handleAuthentication(ServerCnxn cnxn, byte[] authData) {
        // TODO Main authentication calls for this provider will happen here
        return null;
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
    
}
