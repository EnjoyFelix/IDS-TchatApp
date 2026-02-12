package shared.api.identity;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IdentityService extends Remote {
    // The identifier used to register the instance
    static final String REGISTRATION_NAME = "IdentityService";

    /**
     * Allows the user to login
     * @param username The username of the user
     * @param password The password of the user
     * @return a new identity
     * @throws RemoteException an exception
     */
    Identity login(final String username, final String password) throws RemoteException;

    void addUser(String username, String password) throws RemoteException;
}
