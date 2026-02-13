package server.api.message;

import server.TchatServer;
import shared.api.identity.Identity;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;


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
            System.err.println("[ERR] Error read history : "+e);
        }
    }

    @Override
    public void unSubscribe(final Identity identity) {
        removeSubscriber(identity.username());
    }

    private void removeSubscriber(final String key){
        subscriberMap.remove(key);
    }

    private void saveMessageInHistory(Message message){
        // to avoid competition problems
        synchronized (mutex){
            try(FileWriter writer = new FileWriter(PATH_HISTORY_FILE, true);){
                //Formated message
                String m = "%s~%s~%s\n".formatted(message.username(), message.date(), message.message());

                writer.write(m);

            }catch (IOException e){
                System.err.println("[ERR] Error saving message : "+e);
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
