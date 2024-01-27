package me.joshuasheldon.doclookout;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Makes a HEAD request to a URL and attempts to retrieve
 * the <code>content-length</code> parameter from the
 * HTTP header of the response. This class is used to
 * determine if a file has changed by comparing the
 * filesize of the file on the server to the locally
 * saved filesize for the file.
 */
public class FilesizeRetriever extends WebOperationExecutor {

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Attempts to retrieve the <code>content-length</code>
     * parameter from the HTTP header of the response
     * given by the URL. This method does not attempt to
     * download any files, only trusts that if the URL
     * points to a file, that the server will return the
     * filesize in the header.
     *
     * @param url The URL to request to.
     * @return A Future. If the method executes successfully,
     * the Future will contain the value of the
     * <code>content-length</code> header, or <code>-1</code>
     * if the header is not present. If the method fails, the
     * Future will contain an exception.
     */
    public Future<Long> retrieveFilesize(String url) {

        // Instantiate first so we have an easy return value
        // if the retrieval fails before we can actually
        // retrieve the filesize
        CompletableFuture<Long> invalidURLResponse = new CompletableFuture<>();
        invalidURLResponse.completeExceptionally(new IllegalArgumentException("Invalid URL!"));

        // Primitive sanity checks
        if (url == null || url.isEmpty()) {
            return invalidURLResponse;
        }

        URL urlObj;

        try {
            urlObj = new URL(url);
        } catch (Exception e) {
            System.err.println("Attempted to retrieve filesize of malformed URL: " + url);
            return invalidURLResponse;
        }

        // Schedule filesize retrieval
        return this.executor.submit(() -> {

            // Principles of logic from StackOverflow
            // https://stackoverflow.com/a/12800801

            URLConnection conn = null;

            try {

                conn = urlObj.openConnection();
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).setRequestMethod("HEAD");
                }

                return conn.getContentLengthLong();

            } catch (Exception e) {

                System.err.println("Error while attempting to retrieve filesize of URL: " + url);
                throw e;

            } finally {

                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }

            }

        });

    }

}

