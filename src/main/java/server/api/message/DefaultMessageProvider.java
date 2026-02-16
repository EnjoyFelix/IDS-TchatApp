package server.api.message;

import server.TchatServer;
import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Logger;


public class DefaultMessageProvider implements MessageService {
    private final ConcurrentMap<String, SpaceSubscriber> subscriberMap = new ConcurrentHashMap<>();

    private final Object mutex = new Object();

    @Override
    public void send(Message message, Identity identity) throws RemoteException {
        TchatServer.getLogger().log(Level.INFO, "Received a message from %s !".formatted(identity.username()));
        // notify the subscribers
        saveMessageInHistory(message);

        // iterator to prevent a ConcurrentModificationError (happens when you remove an item inside a foreach)
        // var used to mask the iterator's long type
        // synchronized loop because iterators can't guarantee thread safety
        var iterator = subscriberMap.entrySet().iterator();
        synchronized (subscriberMap) {
            while (iterator.hasNext()) {
                final Map.Entry<String, SpaceSubscriber> value = iterator.next();
                // filter out the sender
                final String username = value.getKey();
                if (username.equals(identity.username())) continue;

                // notify this user
                sendMessage(message, value.getValue(), value.getKey());
            }
        }
    }

    @Override
    public void subscribe(final Identity identity, final SpaceSubscriber subscriber) {
        final Logger logger = TchatServer.getLogger();
        logger.log(Level.INFO, "New subscriber : %s !".formatted(identity.username()));

        // add the user
        this.subscriberMap.put(identity.username(), subscriber);

        //Send history message to subscriber
        try(BufferedReader br = new BufferedReader(new FileReader(PATH_HISTORY_FILE))){
            String line;

            while ((line = br.readLine()) != null) {
                //Parse to create Message
                String[] parse = line.split("~", 3);

                if(parse.length == 3){
                    subscriber.onMessage(new Message(parse[0], parse[2], Instant.parse(parse[1])));
                }

            }
        }catch(IOException e){
            logger.log(Level.WARNING, "[ERR] Error read history : %s".formatted(e));
        }

        // notify the other users
        this.subscriberMap.forEach((k, v) -> sendConnectionNotification(identity.username(), v, k));
    }

    @Override
    public void unSubscribe(final Identity identity) {
        removeSubscriber(identity.username());

        // notify the other users
        this.subscriberMap.forEach((k, v) -> sendDisconnectNotification(identity.username(), v, k));
    }

    private void removeSubscriber(final String key){
        subscriberMap.remove(key);
    }

    private void saveMessageInHistory(Message message){
        // to avoid competition problems
        //FIXME : Write to the end of the file and do not overwrite the content
        synchronized (mutex){
            try(FileWriter writer = new FileWriter(PATH_HISTORY_FILE);){
                //Formated message
                String m = "%s~%s~%s\n".formatted(message.username(), message.date(), message.message());

                writer.write(m);

            }catch (IOException e){
                System.err.println("[ERR] Error saving message : "+e);
            }
        }

    }

    /**
     * Called to notify a user that a message was sent
     * @param msg The message
     * @param subscriber The subscriber to notify
     * @param key The username of the currentSubscriber
     */
    private void sendMessage(final Message msg, final SpaceSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onMessage(msg);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client %s".formatted(key));
            this.removeSubscriber(key);
        }
    }

    private void sendConnectionNotification(final String newUser, final SpaceSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onConnect(newUser);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client");
            this.removeSubscriber(key);
        }
    }

    private void sendDisconnectNotification(final String oldUser, final SpaceSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onDisconnect(oldUser);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client %s".formatted(key));
            this.removeSubscriber(key);
        }
    }
}
