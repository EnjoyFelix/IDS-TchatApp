package shared;

import java.io.*;

/**
 * The StorageUtils class our group used for our 1 year's COO Project
 */
public final class StorageUtils {

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
}