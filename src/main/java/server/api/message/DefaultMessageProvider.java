package server.api.message;

import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultMessageProvider implements MessageService {
    private final AtomicInteger messageIdentifiers = new AtomicInteger(0);
    private final Map<String, SpaceSubscriber> subscriberMap = new HashMap<>();

    @Override
    public int send(Message message, Identity identity) throws RemoteException {
        // verify the permissions

        // notify the subscribers

        return messageIdentifiers.getAndIncrement();
    }

    @Override
    public void subscribe(Identity identity, SpaceSubscriber subscriber) {
        this.subscriberMap.put(identity.username(), subscriber);
    }

    @Override
    public void unSubscribe(Identity identity) {
        subscriberMap.remove(identity.username());
    }

    public void notify(final Message msg, final SpaceSubscriber subscriber){
        try {
            // notify the receiver
            subscriber.onMessage(msg);
        } catch (RemoteException e) {
            //

        }
    }
}
