package org.apache.zookeeper.server.auth;

import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.server.ServerCnxn;

public class OAuthAuthenticationProvider implements AuthenticationProvider{

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
