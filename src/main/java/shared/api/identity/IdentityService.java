package shared.api.identity;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IdentityService extends Remote {
    // The identifier used to register the instance
    static final String REGISTRATION_NAME = "IdentityService";

    Identity login(final String username, final String password) throws RemoteException;;
}
