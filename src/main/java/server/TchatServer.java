package server;

import lombok.Getter;
import server.api.identity.DefaultIdentityService;
import server.api.message.DefaultMessageProvider;
import shared.api.identity.Identity;
import shared.api.identity.IdentityService;
import shared.api.message.MessageService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TchatServer {
    // Service Instances, should only be accessed using the functions
    @Getter
    private IdentityService identityService = new DefaultIdentityService();
    @Getter
    private MessageService messageService = new DefaultMessageProvider();

    private final int port;
    TchatServer(int port){
        this.port = port;
    }

    public void run() {
        // register the services
        if (!registerServices()) return;
        System.out.println("Server is ready !");
    }

    private boolean registerServices() {
        try {
            // create the sharde objects
            IdentityService identityService = (IdentityService) UnicastRemoteObject.exportObject(this.getIdentityService(), 0);
            MessageService messageService = (MessageService) UnicastRemoteObject.exportObject(this.getMessageService(), 0);

            // registed the services
            final Registry registry = getRegistry();
            registry.rebind(IdentityService.REGISTRATION_NAME, identityService);
            registry.rebind(MessageService.REGISTRATION_NAME, messageService);
        } catch (RemoteException rmE){
            getLogger().log(Level.SEVERE, "Unable to register the services : %s".formatted(rmE.getMessage()));
            return false;
        }
        return true;
    };

    public Registry getRegistry() throws RemoteException {
        return LocateRegistry.getRegistry(port);
    }

    public static Logger getLogger(){
        return Logger.getLogger("ServerLogger");
    }


    public static void main(String[] args) {
        // check the number of args
        if (args.length < 1){
            System.out.println("Usage : TchatServer <port>");
            return;
        }

        // get the args
        int port = Integer.parseInt(args[0]);

        // run the server
        TchatServer server = new TchatServer(port);
        server.run();
    }
}
