package me.joshuasheldon.doclookout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Manages the last found content length of all URLs.
 */
public class StorageMgr {

    /* ---------- CONSTANTS ---------- */

    /**
     * The name of the file where we store the last found content length of all URLs.
     */
    public static final String STORAGE_FILE_NAME = "storage.json";

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The object we use to read and write JSON.
     */
    private final ObjectMapper objectMapper;

    /**
     * The file where we store the last found content length of all URLs.
     */
    private final File storageFile;

    /**
     * The map where we store the last found content length of all URLs.
     */
    private final Map<String, Long> urlContentLengths;

    /* ---------- CONSTRUCTORS ---------- */

    private StorageMgr() {
        this.objectMapper = new ObjectMapper();
        this.storageFile = new File(StorageMgr.STORAGE_FILE_NAME);
        this.urlContentLengths = new HashMap<>();
        readFromStorage();
    }

    /* ---------- SINGLETON ---------- */

    private static final StorageMgr instance = new StorageMgr();

    public static StorageMgr getInstance() {
        return instance;
    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Attempts to insert all the entries from the parameter
     * into the internal map, and then starts a new thread
     * to writes the updated internal map to a file.
     *
     * @param newURLContentLengths The new URL-content length pairs.
     * @return A list of the URLs that:<br>
     * 1) were already in the map and<br>
     * 2) had their value changed.<br>
     * This effectively reports which files have changed.
     */
    public List<String> updateStorage(Map<String, Long> newURLContentLengths) {

        LinkedList<String> updatedURLs = new LinkedList<>();

        // Synchronize on the map to ensure that we're not updating
        // it and writing it to the file at the same time
        synchronized (this.urlContentLengths) {

            // Add all pairs from the parameter to the internal map
            for (Map.Entry<String, Long> pair : newURLContentLengths.entrySet()) {

                Long oldValue = this.urlContentLengths.put(pair.getKey(), pair.getValue());

                // If a URL has had its content length changed,
                // then add it to the list of updated URLs
                if (oldValue != null && !oldValue.equals(pair.getValue())) {
                    updatedURLs.push(pair.getKey());
                }

            }
        }

        // Now that we've updated our internal map, start a new thread
        // to write those changes to the storage file.
        new Thread(this::writeToStorage).start();

        return updatedURLs;

    }

    /* ---------- PRIVATE METHODS ---------- */

    /**
     * Attempts to read all existing URL-content length pairs
     * from the storage file into our in-memory map.
     */
    private void readFromStorage() {

        if (!this.storageFile.exists()) {
            System.out.println("No existing storage file found.");
            return;
        }

        // Attempt to read storage file into map object
        Map<?, ?> storageURLContentLengths;

        try {
            storageURLContentLengths =
                    this.objectMapper.readValue(this.storageFile, this.urlContentLengths.getClass());
        } catch (Exception e) {
            System.err.println("Failed to read storage file!");
            e.printStackTrace();
            return;
        }

        // Add all valid key-value pairs to the map
        for (Map.Entry<?, ?> entry : storageURLContentLengths.entrySet()) {

            // Make sure key is String and value is Long
            Object key = entry.getKey();
            Object value = entry.getValue();

            // Mathematical integer, not a programmatic integer
            boolean valueIsInteger = (value instanceof Long) || (value instanceof Integer);

            if (!(key instanceof String) || !valueIsInteger) {
                System.err.println("Invalid key-value pair in the storage file: <" + key + ", " + value + ">");
                continue;
            }

            // Fancy statement to either cast the value to Long
            // or convert it from an Integer to a Long
            Long valueAsLong = (value instanceof Long) ? (Long) value : ((Integer) value).longValue();

            // Should be a pair going from URL to content length
            this.urlContentLengths.put((String) key, valueAsLong);

        }

    }

    /**
     * Writes the internal map to the storage file. Synchronized
     * so that we're not doing concurrent writing.
     */
    private synchronized void writeToStorage() {

        // Make a copy of the map so that we can write it to the file
        // without worrying about it being modified while we're writing
        Map<String, Long> urlContentLengthsCopy;

        synchronized (this.urlContentLengths) {
            urlContentLengthsCopy = new HashMap<>(this.urlContentLengths);
        }

        try {
            this.objectMapper.writeValue(this.storageFile, urlContentLengthsCopy);
        } catch (Exception e) {
            System.err.println("Failed to write storage to file!");
            e.printStackTrace();
        }

    }

}
