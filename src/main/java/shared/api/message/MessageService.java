package shared.api.message;

import shared.api.identity.Identity;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.PriorityQueue;

public interface MessageService extends Remote {
    // The identifier used to register the instance
    static final String REGISTRATION_NAME = "MessageService";

    /**
     * Allows a client to send a message
     * @param message the message to send
     * @param identity The user sending the message, to validate it
     * @return The identifier of the message
     * @throws RemoteException Something went wrong with the distant machine
     */
    int send(Message message, Identity identity) throws RemoteException;


    void subscribe(final Identity identity, final SpaceSubscriber subscriber);
    void unSubscribe(final Identity identity);
}
