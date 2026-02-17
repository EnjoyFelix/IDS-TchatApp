package client.api.message;

import shared.api.message.Message;
import shared.api.message.NotificationSubscriber;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;

public class DefaultNotificationSubscriber implements NotificationSubscriber {
    @Override
    public void onMessage(final Message message) throws RemoteException {
        // format and print the message
        final String formatted = formatMessage(message);
        System.out.printf(formatted);
    }


    @Override
    public void onConnect(final String username) throws RemoteException {
        // format and print the message
        final String formatted = formatConnectionMessage(username);
        System.out.println(formatted);
    }

    @Override
    public void onDisconnect(final String username) throws RemoteException {
        // format and print the message
        final String formatted = formatDisconnectMessage(username);
        System.out.println(formatted);
    }

    @Override
    public void onHistoryResult(final Collection<Message> messages) throws RemoteException {
        // Format the text
        final StringBuilder builder = new StringBuilder("The first %d messages :\n".formatted(messages.size()));
        messages.forEach(m -> {
            builder.append("- ");
            builder.append(formatMessage(m));
        });

        // print it
        System.out.println(builder);
    }

    private static String formatMessage(final Message message) {
        return "%s at %s : %s\n".formatted(message.username(), message.date(), message.message());
    }

    private static String formatConnectionMessage(final String username){
        return "(+) %s".formatted(username);
    }

    private static String formatDisconnectMessage(final String username){
        return "(-) %s".formatted(username);
    }
}
