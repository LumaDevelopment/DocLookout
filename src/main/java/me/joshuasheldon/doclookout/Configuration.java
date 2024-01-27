package me.joshuasheldon.doclookout;

/**
 * Configuration object for DocLookout.
 *
 * @param webhookURL        The URL of the webhook to publish to when
 *                          document content length modifications have
 *                          been detected.
 * @param checkIntervalInMs The interval in milliseconds at which to
 *                          check the document content lengths.
 * @param urlsToCheck       The URLs to check the content length of.
 * @param roleIDToPing      The ID of the role to ping when documents
 *                          are updated. If it is blank, no role will
 *                          be pinged.
 */
public record Configuration(String webhookURL, Long checkIntervalInMs, String[] urlsToCheck, String roleIDToPing) {

    /**
     * - <code>webhookURL</code> = <code>"https://discord.com/"</code><br>
     * - <code>checkIntervalInMs</code> = <code>60000</code><br>
     * - <code>urlsToCheck</code> = <code>["https://cs.fit.edu/~dmitra/ArtInt/Spr2024/AI-PlanSp2024.doc"]</code><br>
     * - <code>roleIDToPing</code> = <code>""</code>
     *
     * @return The default state of the configuration.
     */
    public static Configuration getDefault() {
        return new Configuration(
                "https://discord.com/",
                60_000L,
                new String[]{
                        "https://cs.fit.edu/~dmitra/ArtInt/Spr2024/AI-PlanSp2024.doc"
                },
                ""
        );
    }

}
