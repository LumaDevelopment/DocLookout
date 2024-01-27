package me.joshuasheldon.doclookout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple parent class to be extended by all classes
 * of DocLookout that need to execute web operations.
 * Implements an executor service to ensure that only
 * one web operation is done per executor at a time.
 */
public class WebOperationExecutor {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The executor service that executes all web operations
     * in a single thread/queue to avoid resource consumption spikes
     * or resource contention.
     */
    protected final ExecutorService executor;

    /* ---------- CONSTRUCTORS ---------- */

    public WebOperationExecutor() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Stops this web operation executor by shutting down
     * the executor service behind it.
     */
    public void stop() {
        this.executor.shutdown();
    }

}
