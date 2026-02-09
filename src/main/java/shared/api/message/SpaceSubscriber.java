package shared.api.message;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SpaceSubscriber extends Remote, Serializable {
    void onMessage(final Message message) throws RemoteException;
}
