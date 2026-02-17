package shared.api.message;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface NotificationSubscriber extends Remote, Serializable {
    void onMessage(final Message message) throws RemoteException;
    void onConnect(final String username) throws RemoteException;
    void onDisconnect(final String username) throws RemoteException;
    void onHistoryResult(final Collection<Message> messages) throws RemoteException;
}
