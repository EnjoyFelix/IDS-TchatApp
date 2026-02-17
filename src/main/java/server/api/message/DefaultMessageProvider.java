package server.api.message;

import server.TchatServer;
import server.utils.AppendObjectOutputStream;
import shared.StorageUtils;
import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.NotificationSubscriber;

import java.io.*;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Logger;


public class DefaultMessageProvider implements MessageService {
    private static final String HISTORY_FILENAME = "history.ser";

    // Map of the Connected User's username and their "messenger"
    private final ConcurrentMap<String, NotificationSubscriber> subscriberMap = new ConcurrentHashMap<>();
    private final Object mutex = new Object();  //

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
                final Map.Entry<String, NotificationSubscriber> value = iterator.next();
                // filter out the sender
                final String username = value.getKey();
                if (username.equals(identity.username())) continue;

                // notify this user
                sendMessage(message, value.getValue(), value.getKey());
            }
        }
    }

    @Override
    public void subscribe(final Identity identity, final NotificationSubscriber subscriber) {
        final Logger logger = TchatServer.getLogger();
        logger.log(Level.INFO, "New subscriber : %s !".formatted(identity.username()));

        // add the user
        this.subscriberMap.put(identity.username(), subscriber);

        //Send history message to subscriber
        final String datapath = getFullDatapath();
        try(BufferedReader br = new BufferedReader(new FileReader(datapath))){
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

    private void saveMessageInHistory(Message message) {
        final Logger logger = TchatServer.getLogger();
        synchronized (mutex) {
            final String datapath = getFullDatapath();
            try {
                boolean fileExists = new File(datapath).exists();

                FileOutputStream fos = new FileOutputStream(datapath, true);

                ObjectOutputStream oos;

                if (fileExists) {
                    //Don't want header here
                    oos = new AppendObjectOutputStream(fos);
                } else {
                    //File doesn't exist can put a header
                    oos = new ObjectOutputStream(fos);
                }

                //Save message
                oos.writeObject(message);
                oos.close();

            } catch (IOException e) {
                logger.log(Level.WARNING, "[ERR] Error saving message: " + e);
            }
        }
    }

    /**
     * Called to notify a user that a message was sent
     * @param msg The message
     * @param subscriber The subscriber to notify
     * @param key The username of the currentSubscriber
     */
    private void sendMessage(final Message msg, final NotificationSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onMessage(msg);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client %s".formatted(key));
            this.removeSubscriber(key);
        }
    }

    private void sendConnectionNotification(final String newUser, final NotificationSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onConnect(newUser);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client");
            this.removeSubscriber(key);
        }
    }

    private void sendDisconnectNotification(final String oldUser, final NotificationSubscriber subscriber, final String key){
        try {
            // notify the receiver
            subscriber.onDisconnect(oldUser);
        } catch (RemoteException e) {
            // Log and unsubscribe them
            TchatServer.getLogger().log(Level.INFO, "Disconnected client %s".formatted(key));
            this.removeSubscriber(key);
        }
    }

    public void showHistory(int number, final Identity identity) throws RemoteException {
        // get the subscriber
        final NotificationSubscriber subscriber = subscriberMap.get(identity.username());
        if (subscriber == null) return;

        // init the list
        final List<Message> messages = new ArrayList<>();

        // read n messages
        final String datapath = getFullDatapath();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datapath))) {
            // read until null or n was reached
            Message m; int i = 0;
            while (i < number && (m = (Message) ois.readObject()) != null) {
                messages.add(m);
                i++;
            }
        } catch (EOFException e) {
            // End of the file
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        subscriber.onHistoryResult(messages);
    }

    private static String getFullDatapath() {
        return StorageUtils.makeDataPath(DefaultMessageProvider.HISTORY_FILENAME);
    }
}
