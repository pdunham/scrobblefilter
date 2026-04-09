package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

public class DatastoreProvider {

    private static Datastore instance;

    public static synchronized void initialize() {
        if (instance == null) {
            instance = DatastoreOptions.getDefaultInstance().getService();
        }
    }

    public static Datastore get() {
        if (instance == null) {
            throw new IllegalStateException(
                "DatastoreProvider not initialized — call initialize() at startup");
        }
        return instance;
    }
}
