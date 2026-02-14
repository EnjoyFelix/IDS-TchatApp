package shared;

import java.io.*;

/**
 * The StorageUtils class our group used for our 1 year's COO Project
 */
public final class StorageUtils {
    public static final String DATAPATH = "./data";

    // This class is only for utils
    private StorageUtils() {}

    /**
     * Saves the data to the given file
     * @param data The data to save
     * @param path The path of the file
     * @throws IOException could not write the file
     */
    public static void save(final Serializable data, final String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(data);
        }
    }

    /**
     * Loads the data as an instance of the given type
     * @param path The path of the file
     * @param type The class object of the data
     * @return An instance of type T
     * @param <T> The generic type of the data
     * @throws IOException Could not read the file
     * @throws ClassNotFoundException Invalid class
     */
    public static <T> T load(final String path, final Class<T> type)
            throws IOException, ClassNotFoundException {

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = in.readObject();
            return type.cast(obj);
        }
    }

    /**
     * Adds the path of the datapth to the given path
     * @param path the path of the object inside the datapath
     * @return a global path
     */
    public static String makeDataPath(final String path) {
        return DATAPATH + "/" + path;
    };
}