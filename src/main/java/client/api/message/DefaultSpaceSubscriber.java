package client.api.message;

import shared.api.message.Message;
import shared.api.message.SpaceSubscriber;

import java.rmi.RemoteException;

public class DefaultSpaceSubscriber implements SpaceSubscriber {


    @Override
    public void onMessage(final Message message) throws RemoteException {
        // format and print the message
        final String formatted = formatMessage(message);
        System.out.printf(formatted);
    }

    private String formatMessage(final Message message) {
        return "%s at %s : %s\n".formatted(message.username(), message.date(), message.message());
    }
}
