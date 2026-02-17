package client;

import client.api.message.DefaultNotificationSubscriber;
import lombok.Getter;
import lombok.Setter;
import shared.api.identity.Identity;
import shared.api.identity.IdentityService;
import shared.api.message.Message;
import shared.api.message.MessageService;
import shared.api.message.NotificationSubscriber;

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
    private final NotificationSubscriber notificationSubscriber;

    @Getter @Setter
    private Identity identity;
    TchatClient(final String host, final int port){
        this.port = port;
        this.host = host;
        this.notificationSubscriber = new DefaultNotificationSubscriber();
    }

    public void run() {
        // connect to the server
        // ROUGH POC
        try {
            // get the services
            final Registry registry = LocateRegistry.getRegistry(host, port);
            final IdentityService identityService = (IdentityService) registry.lookup(IdentityService.REGISTRATION_NAME);
            final MessageService messageService = (MessageService) registry.lookup(MessageService.REGISTRATION_NAME);


            // try to log in
            final Identity identity = doLogin(messageService, identityService);
            if (identity == null) {return;}

            // save the identity
            setIdentity(identity);
            System.out.printf("Successfully logged in with user %s !\n", identity.username());

            // register to the space
            messageService.subscribe(this.getIdentity(), (NotificationSubscriber) UnicastRemoteObject.exportObject(this.getNotificationSubscriber(), 0));

            System.out.println("================= Tchat App =================");
            System.out.println("You can now enter your messages.");
            System.out.println("Available commands :");
            System.out.println("</quit>        Exit Tchat App");
            System.out.println("</history n>   Display the last n messages");
            System.out.println("---------------------------------------------");


            // read and send the messages
            mainLoop(messageService);

            // unsubscribe
            messageService.unSubscribe(identity);
            UnicastRemoteObject.unexportObject(this.getNotificationSubscriber(), false);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main application logic
     * @param messageService The message service retreived from the registry
     * @throws RemoteException Something went wrong with the RMC
     */
    private void mainLoop(final MessageService messageService) throws RemoteException {
        final Scanner scanner = new Scanner(System.in);
        String input;

        // Read and send messages
        System.out.println("You can start to write (/quit to leave)");
        while(true){
            System.out.print("> ");
            input = scanner.nextLine();

            // (basic command) quit
            if(input.equalsIgnoreCase("/quit")){
                System.out.println("System leaving...");
                break;
            }

            if(input.startsWith("/history")){
                doHistoryCommand(input, messageService);
                continue;
            }

            // ignore empty message
            if(input.isBlank()){
                continue;
            }

            //send message
            messageService.send(new Message(this.getIdentity().username(), input, Instant.now()), getIdentity());
        }
    }

    /**
     * Login logic
     * @param messageService The message service queried from the Registry
     * @param identityService The Identity Service queried from the Registry
     * @return The identity, or Null if something went wrong
     * @throws RemoteException Something remote went wrong
     */
    private Identity doLogin(final MessageService messageService, final IdentityService identityService) throws RemoteException {
        Scanner scanner = new Scanner(System.in);

        // login
        String username = "";
        String password = "";
        String input;

        while(username.isBlank() || password.isBlank()) {
            System.out.println("Do you have an account ? y/n (/quit to leave)");
            input = scanner.nextLine();

            // does the user have an account ?
            switch (input){
                // yes, connect him
                case "y", "Y":
                    System.out.println("Enter your username :");
                    username = scanner.nextLine();
                    System.out.println("Enter your password :");
                    password = scanner.nextLine();
                    break;

                // No, Create an account
                case "n", "N":
                    System.out.println("===== Account creation : =====");
                    System.out.println("Enter your username :");
                    username = scanner.nextLine();
                    System.out.println("Enter your password :");
                    password = scanner.nextLine();

                    //add user to usermap
                    identityService.addUser(username, password);
                    break;

                case "/quit":
                    System.out.println("System exit...");
                    return null;

                default :
                    System.out.println("Unknow command...");

            }
        }

        // try to log in
        final Identity identity = identityService.login(username, password);
        if (identity == null) {
            System.out.println("Invalid credentials !");
        }

        return identity;
    }

    private void doHistoryCommand(final String input, final MessageService messageService) throws RemoteException{
        // split the command into it's args
        final String[] parts = input.split(" ");
        if(parts.length != 2){
            System.out.println("Usage: /history <number>");
            return;
        }

        // parse n and check for errors
        int n;
        try{
            n = Integer.parseInt(parts[1]);
            if (n <= 0) throw new NumberFormatException();
        }catch(NumberFormatException e){
            System.out.println("Error > n must be a positive integer !");
            return;
        }

        // request the messages
        messageService.showHistory(n, this.getIdentity());
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
