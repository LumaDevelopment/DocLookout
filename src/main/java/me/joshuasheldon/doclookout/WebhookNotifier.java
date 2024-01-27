package me.joshuasheldon.doclookout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Notifies a Discord webhook of any updated documents.
 */
public class WebhookNotifier extends WebOperationExecutor {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The object mapper used to construct the JSON
     * to post to the webhook.
     */
    private final ObjectMapper objectMapper;

    /* ---------- CONSTRUCTORS ---------- */

    public WebhookNotifier() {
        super();
        this.objectMapper = new ObjectMapper();
    }

    /* ---------- PUBLIC METHODS ---------- */

    /**
     * Notifies the given webhook that the documents
     * at the given URLs have been updated.
     *
     * @param webhookURL   The URL of the webhook to notify.
     * @param roleIDToPing The ID of the role to ping when documents
     *                     are updated. If it is blank, no role will
     *                     be pinged.
     * @param updatedURLs  The URLs of the updated documents.
     */
    public void notifyWebhook(String webhookURL, String roleIDToPing, List<String> updatedURLs) {

        // This method shouldn't get called very often, but
        // submit it to an executor anyway
        this.executor.submit(() -> {

            // Include JSON construction in the task
            // so that the Timer task doesn't have to
            // wait for this method to return
            ObjectNode postContent = objectMapper.createObjectNode();

            // Put in base content
            String content;

            if (!roleIDToPing.isBlank()) {
                content = "<@&" + roleIDToPing + ">";
            } else {
                content = "";
            }

            postContent.put("content", content);
            postContent.put("tts", false);

            // Construct embed
            ArrayNode embeds = objectMapper.createArrayNode();
            ObjectNode embed = objectMapper.createObjectNode();

            embed.put("title", "Document(s) Updated!");
            embed.put("color", 0x99EEFF);

            // Construct description
            StringBuilder description = new StringBuilder();
            description.append("The following document(s) have been updated:");

            for (String url : updatedURLs) {
                description.append("\n - ").append(url);
            }

            embed.put("description", description.toString());

            // Collate all the embed data structures together
            // and add them to the post content
            embeds.add(embed);
            postContent.set("embeds", embeds);

            URL url;

            try {
                url = new URL(webhookURL);
            } catch (Exception e) {
                System.err.println("Invalid webhook URL: " + webhookURL);
                return;
            }

            try {

                // Set up connection
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                // Write JSON to connection
                OutputStream outputStream = connection.getOutputStream();
                objectMapper.writeValue(outputStream, postContent);
                outputStream.flush();
                outputStream.close();

                // Close connection
                connection.getInputStream().close();
                connection.disconnect();

            } catch (Exception e) {
                System.err.println("Failed notify webhook with URL: " + webhookURL);
                e.printStackTrace();
            }

        });

    }

}
