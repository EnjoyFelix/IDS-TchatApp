package server.api.message;

import server.TchatServer;
import server.utils.AppendObjectOutputStream;
import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.io.*;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;


public class DefaultMessageProvider implements MessageService {
    private final ConcurrentMap<String, SpaceSubscriber> subscriberMap = new ConcurrentHashMap<>();

    private final Object mutex = new Object();

    @Override
    public void send(Message message, Identity identity) throws RemoteException {
        TchatServer.getLogger().log(Level.INFO, "Received a message from %s !".formatted(identity.username()));
        // notify the subscribers
        saveMessageInHistory(message);
        subscriberMap.forEach((k, s) -> {
            this.notify(message, s, k);
        });
    }

    @Override
    public void subscribe(final Identity identity, final SpaceSubscriber subscriber) {
        this.subscriberMap.put(identity.username(), subscriber);

        //Load history
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PATH_HISTORY_FILE))) {

            while (true) {
                Message m = (Message) ois.readObject();
                //Print to all subscriber
                subscriber.onMessage(m);
            }

        } catch (EOFException e) {
            // End of the file
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unSubscribe(final Identity identity) {
        removeSubscriber(identity.username());
    }

    private void removeSubscriber(final String key){
        subscriberMap.remove(key);
    }

    private void saveMessageInHistory(Message message) {
        synchronized (mutex) {
            try {
                boolean fileExists = new File(PATH_HISTORY_FILE).exists();

                FileOutputStream fos = new FileOutputStream(PATH_HISTORY_FILE, true);

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
                System.err.println("[ERR] Error saving message: " + e);
            }
        }
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
