package shared.api.message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SpaceSubscriber extends Remote {
    void onMessage(Message message) throws RemoteException;
}
