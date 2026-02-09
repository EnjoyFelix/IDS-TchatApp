package server.api.identity;

import shared.api.identity.Identity;
import shared.api.identity.IdentityService;

public class DefaultIdentityService implements IdentityService {

    @Override
    public Identity login(final String username, final String password) {
        // TODO : check if the username and password match
        return null;
    }
}
