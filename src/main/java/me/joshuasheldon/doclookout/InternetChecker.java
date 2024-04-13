package me.joshuasheldon.doclookout;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Future;

/**
 * Provides the ability to (effectively) check for Internet
 * access by attempting to fetch content from the
 * Discord URL.
 */
public class InternetChecker extends WebOperationExecutor {

    public Future<Boolean> isInternetAvailable() {
        return this.executor.submit(() -> {

            URLConnection conn = null;

            try {

                // Attempt to connect to Discord
                conn = new URL("https://discord.com/").openConnection();
                conn.connect();
                conn.getInputStream().close();
                return true;

            } catch(Exception e) {

                // If we can't, then the application shouldn't
                // proceed with web operations
                return false;

            } finally {

                // Clean up

                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }

            }
        });
    }

}
