package scrobblefilter.model;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

public class DatastoreProvider {

    private static Datastore instance;

    public static synchronized Datastore get() {
        if (instance == null) {
            instance = DatastoreOptions.getDefaultInstance().getService();
        }
        return instance;
    }
}
