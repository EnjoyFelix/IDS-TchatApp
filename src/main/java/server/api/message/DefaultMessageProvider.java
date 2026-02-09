package server.api.message;

import server.TchatServer;
import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class DefaultMessageProvider implements MessageService {
    private final ConcurrentMap<String, SpaceSubscriber> subscriberMap = new ConcurrentHashMap<>();

    @Override
    public void send(Message message, Identity identity) throws RemoteException {
        TchatServer.getLogger().log(Level.INFO, "Received a message from %s !", identity.username());
        // notify the subscribers
        subscriberMap.forEach((k, s) -> {
            this.notify(message, s, k);
        });
    }

    @Override
    public void subscribe(final Identity identity, final SpaceSubscriber subscriber) {
        this.subscriberMap.put(identity.username(), subscriber);
    }

    @Override
    public void unSubscribe(final Identity identity) {
        removeSubscriber(identity.username());
    }

    private void removeSubscriber(final String key){
        subscriberMap.remove(key);
    }

    public void notify(final Message msg, final SpaceSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onMessage(msg);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client");
            this.removeSubscriber(key);
        }
    }
}
