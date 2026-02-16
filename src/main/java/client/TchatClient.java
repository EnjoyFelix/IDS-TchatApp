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
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.util.Scanner;

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

            Scanner scanner = new Scanner(System.in);

            // login
            String username = "";
            String password = "";
            String input;

            while(username.isBlank()){
                System.out.println("Do you have an account ? y/n (/quit to leave)");
                input = scanner.nextLine();

                switch (input){
                    case "y":
                        System.out.println("Enter your username :");
                        username = scanner.nextLine();
                        System.out.println("Enter your password :");
                        password = scanner.nextLine();
                        break;

                    case "n":
                        System.out.println("===== Account creation : =====");
                        System.out.println("Enter your username :");
                        username = scanner.nextLine();
                        System.out.println("Enter your password :");
                        password = scanner.nextLine();

                        //add user to usermap
                        identityService.addUser(username, password);
                        break;

                    case "/quit":
                        //TODO : how to exit correctly ?
                        System.out.println("System exit...");
                        return;

                    default :
                        System.out.println("Unknow command...");

                }
            }

            final Identity identity = identityService.login(username, password);
            if (identity == null) {
                System.out.println("Invalid credentials !");
                return;
            }
            // save the identity
            setIdentity(identity);
            System.out.printf("Successfully logged in with user %s !\n", identity.username());

            // register to the space
            messageService.subscribe(this.getIdentity(), (SpaceSubscriber) UnicastRemoteObject.exportObject(this.getSpaceSubscriber(), 0));

            System.out.println("================= Tchat App =================");
            System.out.println("You can now enter your messages.");
            System.out.println("Available commands :");
            System.out.println("</quit>        Exit Tchat App");
            System.out.println("</history n>   Display the last n messages");
            System.out.println("---------------------------------------------");
            while(true){
                input = scanner.nextLine();

                //quit
                if(input.equalsIgnoreCase("/quit")){
                    System.out.println("System leaving...");
                    break;
                }

                if(input.startsWith("/history")){
                    String[] parts = input.split(" ", 1);
                    if(parts.length == 2){
                        try{
                            int n = Integer.parseInt(parts[1]);
                            System.out.printf("============= Display %s messages =============\n", n);
                            messageService.showHistory(n, (SpaceSubscriber) UnicastRemoteObject.exportObject(this.getSpaceSubscriber(), 0));
                            System.out.println("==============================================");
                        }catch(NumberFormatException e){
                            System.out.println("[ERR] number not valid");
                        }
                    } else {
                        System.out.println("Usage: /history <number>");
                    }
                    break;
                }

                // ignore empty message
                if(input.isBlank()){
                    continue;
                }

                //send message
                messageService.send(new Message(this.getIdentity().username(), input, Instant.now()), getIdentity());
            }

            // unsubscribe
            messageService.unSubscribe(identity);
            UnicastRemoteObject.unexportObject(this.getSpaceSubscriber(), false);
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
