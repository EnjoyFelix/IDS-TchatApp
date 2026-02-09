package client;

import client.api.message.DefaultSpaceSubscriber;
import lombok.Getter;
import lombok.Setter;
import server.TchatServer;
import shared.api.identity.Identity;
import shared.api.identity.IdentityService;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.SpaceSubscriber;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;

public class TchatClient {

    private final String host;
    private final int port;

    @Getter
    private final SpaceSubscriber spaceSubscriber;

    @Getter @Setter
    private Identity identity;
    TchatClient(final String host, final int port){
        this.port = port;
        this.host = host;
        this.spaceSubscriber = new DefaultSpaceSubscriber();
    }


    public void run() {
        // connect to the server
        // ROUGH POC
        try {
            // get the services
            final Registry registry = LocateRegistry.getRegistry(host, port);
            final IdentityService identityService = (IdentityService) registry.lookup(IdentityService.REGISTRATION_NAME);
            final MessageService messageService = (MessageService) registry.lookup(MessageService.REGISTRATION_NAME);

            // login
            final Identity identity = identityService.login("Test", "Test");
            if (identity == null) {
                System.out.println("Invalid credentials !");
                return;
            }
            // save the identity
            setIdentity(identity);
            System.out.printf("Successfully logged in with user %s !\n", identity.username());

            // register to the space
            messageService.subscribe(this.getIdentity(), this.getSpaceSubscriber());

            // send a default message
            messageService.send(new Message(this.getIdentity().username(), "Salut tout le monde !", Instant.now()), getIdentity());

            // unsubscribe
            messageService.unSubscribe(identity);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        // check the number of args
        if (args.length < 2){
            System.out.println("Usage : TchatClient <host> <port>");
            return;
        }

        // get the args
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        // run the server
        TchatClient client = new TchatClient(host, port);
        client.run();
    }
}
