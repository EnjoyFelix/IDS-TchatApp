package client;

import server.TchatServer;

public class TchatClient {

    private final String host;
    private final int port;
    TchatClient(final String host, final int port){
        this.port = port;
        this.host = host;
    }

    public void run(){
        // connect to the server
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
