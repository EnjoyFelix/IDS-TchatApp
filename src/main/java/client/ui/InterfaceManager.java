package client.ui;

// State Pattern manager for the Interfaces
public class InterfaceManager {
    private static InterfaceManager instance;
    private Interface current;

    private InterfaceManager(){

    }

    public void update(Interface newInterface){
        this.current = newInterface;
    }

    /**
     * Singleton impl
     * @return The Instance
     */
    public static InterfaceManager getInstance() {
        if (instance == null){
            instance = new InterfaceManager();
        }

        return instance;
    }
}
