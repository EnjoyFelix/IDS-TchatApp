package shared.api.message;

import shared.api.identity.Identity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.PriorityQueue;

public interface MessageService extends Remote {
    // The identifier used to register the instance
    static final String REGISTRATION_NAME = "MessageService";
    static final String PATH_HISTORY_FILE = "./history.txt";

    /**
     * Allows a client to send a message
     * @param message the message to send
     * @param identity The user sending the message, to validate it
     * @throws RemoteException Something went wrong with the distant machine
     */
    void send(Message message, Identity identity) throws RemoteException;

    void subscribe(final Identity identity, final SpaceSubscriber subscriber) throws RemoteException;
    void unSubscribe(final Identity identity) throws RemoteException;
}
