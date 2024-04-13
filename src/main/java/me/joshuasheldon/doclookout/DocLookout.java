package me.joshuasheldon.doclookout;

import java.util.*;
import java.util.concurrent.Future;

public class DocLookout {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The configuration for the program.
     */
    private final Configuration config;

    /**
     * Class used to retrieve the size of a file
     * pointed to by a URL by reading the
     * <code>content-length</code> header.
     */
    private final FilesizeRetriever filesizeRetriever;

    /**
     * Class used to detect if the Internet is
     * available for checking files and
     * notifying webhooks.
     */
    private final InternetChecker internetChecker;

    /**
     * Runs the task that checks all given URLs,
     * determines if any files have been updated,
     * and notifies the webhook if any files have
     * been updated.
     */
    private final Timer timer;

    /**
     * Class used to notify the webhook if any
     * files have been changed.
     */
    private final WebhookNotifier webhookNotifier;

    /* ---------- CONSTRUCTORS ---------- */

    public DocLookout() {
        this.config = ConfigurationMgr.getInstance().getConfiguration();
        this.filesizeRetriever = new FilesizeRetriever();
        this.internetChecker = new InternetChecker();
        this.timer = new Timer();
        this.webhookNotifier = new WebhookNotifier();
    }

    /* ---------- MAIN METHOD ---------- */

    /**
     * The entry point for the program.
     *
     * @param args Program arguments, which we don't use.
     */
    public static void main(String[] args) {
        new DocLookout().start();
    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Starts the program. This function simply schedules
     * the task which checks the filesize of all the
     * files linked by the URLs in the config, then
     * updates the storage and notifies the webhook
     * if any files have been updated.
     */
    public void start() {

        if (this.config == null) {
            System.out.println("Please modify the configuration file and restart the program!");
            stop();
            return;
        }

        // When the program shuts down, call the stop() method
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        System.out.println("DocLookout started!");

        // Schedule the main logic loop
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                // Check if we can access the Internet,
                // do not proceed if there is any issue
                // with the process
                try {
                    boolean internetAvailable = internetChecker.isInternetAvailable().get();
                    if (!internetAvailable) {
                        System.err.println("The Internet is not available, not proceeding!");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to check if the Internet is available, not proceeding!");
                    e.printStackTrace();
                    return;
                }

                System.out.println("Checking for document changes...");

                Future[] futures = new Future[config.urlsToCheck().length];

                // Start retrieving all file sizes
                for (int i = 0; i < config.urlsToCheck().length; i++) {
                    futures[i] = filesizeRetriever.retrieveFilesize(config.urlsToCheck()[i]);
                }

                Map<String, Long> newURLContentLengths = new HashMap<>();

                // As they finish, add them to map
                for (int i = 0; i < config.urlsToCheck().length; i++) {
                    try {
                        newURLContentLengths.put(config.urlsToCheck()[i], (Long) futures[i].get());
                    } catch (Exception e) {
                        System.err.println("Failed to retrieve filesize of " + config.urlsToCheck()[i] + "!");
                        e.printStackTrace();
                    }
                }

                System.out.printf("Successfully retrieved the content length of %d/%d URLs!%n",
                        newURLContentLengths.size(), config.urlsToCheck().length);

                // Update the storage and see what URLS have changed
                List<String> updated = StorageMgr.getInstance().updateStorage(newURLContentLengths);

                // Notify the webhook if there are any changes
                if (!updated.isEmpty()) {
                    System.out.printf("%d changes detected, notifying the webhook!%n", updated.size());
                    webhookNotifier.notifyWebhook(config.webhookURL(), config.roleIDToPing(), updated);
                }

            }
        }, 0, this.config.checkIntervalInMs());

    }

    /**
     * Stops DocLookout by stopping the timer and
     * the web operation executors.
     */
    public void stop() {

        this.timer.cancel();
        this.filesizeRetriever.stop();
        this.webhookNotifier.stop();

        System.out.println("Goodbye!");

    }

}