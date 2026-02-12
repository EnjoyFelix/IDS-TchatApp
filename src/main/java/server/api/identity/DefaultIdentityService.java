package server.api.identity;

import server.TchatServer;
import shared.StorageUtils;
import shared.api.identity.Identity;
import shared.api.identity.IdentityService;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultIdentityService implements IdentityService {
    // Temporary map to store account information
    private final AccountBank accountsMap;
    public DefaultIdentityService() {
        // Service can choose their own way of storing data, this is why this logic is here
        // get the exact path
        final String fullpath = AccountBank.getFullDatapath();
        final Logger logger = TchatServer.getLogger();

        // attempt to load the data
        AccountBank temp;
        try {
            temp = StorageUtils.load(fullpath, AccountBank.class);
            logger.log(Level.INFO, "Successfully loaded the AccountBank !");
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Could not load the AccountBank, going with the default Bank ! : %s".formatted(e.getMessage()));
            temp = new AccountBank();
        }
        this.accountsMap = temp;
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

    // synchronized for the save to be safe
    public synchronized void addUser(String username, String password){
        final Logger logger = TchatServer.getLogger();
        logger.log(Level.INFO, "Creating new user %s !".formatted(username));

        // add the user and save the account
        accountsMap.put(username, cryptPassword(password));
        try {
            StorageUtils.save(this.accountsMap, AccountBank.getFullDatapath());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "COULD NOT SAVE THE ACCOUNT BANK !!!\n%s".formatted(e.getCause()));
        }
    }

    private static String cryptPassword(final String clearPassword) {
        // FIXME : Passwords are in clear for test purposes
        return clearPassword;
    }

    // Alias class
    private static class AccountBank extends ConcurrentHashMap<String, String> implements Serializable {
        @Serial
        private static final long serialVersionUID = 42L;
        public static final String FILENAME = "AccountBank.dat";

        public static String getFullDatapath() {
            return StorageUtils.makeDataPath(AccountBank.FILENAME);
        }
    }
}
