package server.api.identity;

import server.TchatServer;
import shared.api.identity.Identity;
import shared.api.identity.IdentityService;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultIdentityService implements IdentityService {

    // Temporary map to store account information
    private final ConcurrentMap<String, String> accountsMap;
    public DefaultIdentityService() {
        this.accountsMap = new ConcurrentHashMap<>();
        accountsMap.put("Test", cryptPassword("Test"));
    }

    @Override
    public Identity login(final String username, final String password) throws RemoteException {
        final Logger logger = TchatServer.getLogger();
        final String hashedPassword = accountsMap.get(username);

        // did we find a user ?
        if (hashedPassword == null) {
            logger.log(Level.INFO, "Received a logging attempt from %s, they're not in DB !".formatted(username));
            return null;
        }

        // is the password correct ?
        final String cryptedPassword = cryptPassword(password);
        if (!hashedPassword.equals(cryptedPassword)){
            logger.log(Level.INFO, "Received a logging attempt from %s, but password didn't match !".formatted(username));
            return null;
        }

        // FIXME : Token are used for validation, theyre empty for now
        logger.log(Level.INFO, "User %s successfully logged in !".formatted(username));
        return new Identity(username, "");
    }

    public String cryptPassword(final String clearPassword) {
        // FIXME : Passwords are in clear for test purposes
        return clearPassword;
    }
}
