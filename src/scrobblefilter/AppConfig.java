package scrobblefilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private static final Properties props = new Properties();

    public static void load(InputStream in) throws IOException {
        props.load(in);
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
