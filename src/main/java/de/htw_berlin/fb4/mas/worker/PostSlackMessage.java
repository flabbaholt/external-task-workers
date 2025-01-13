package de.htw_berlin.fb4.mas.worker;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * ExternalTaskHandler implementation for sending messages to a Slack channel via a Webhook API.
 *
 * <p>This class processes external tasks from a Camunda workflow by retrieving specific variables
 * and formatting them into a Slack message. The message is then sent to a configured Slack Webhook URL.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Loads the Slack Webhook URL from the slack.properties file.</li>
 *     <li>Retrieves task-related variables from the Camunda context.</li>
 *     <li>Formats the variables into a structured Slack message.</li>
 *     <li>Sends the message to Slack via HTTP POST request.</li>
 *     <li>Handles failures by reporting them back to Camunda.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * Ensure a valid `slack.properties` file exists in the project root with the key `slack.webhook.url`.
 * The value should be the Webhook URL provided by Slack.
 *
 * <h3>Example `slack.properties`:</h3>
 * <pre>
 * slack.webhook.url=https://hooks.slack.com/services/XXX/YYY/ZZZ
 * </pre>
 */
public class PostSlackMessage implements ExternalTaskHandler {

    /**
     * Logger instance for logging task-related information.
     */
    private static final Logger log = LoggerFactory.getLogger(PostSlackMessage.class);
    /**
     * The Slack Webhook URL loaded from the properties file.
     */
    private final String webhookUrl;

    /**
     * Constructs a new instance of {@code PostSlackMessage} and initializes the Webhook URL.
     *
     * @throws RuntimeException if the `slack.properties` file is missing or the Webhook URL is not configured.
     */
    public PostSlackMessage() {
        Properties slackProperties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Path.of("slack.properties").toAbsolutePath())) {
            slackProperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load Slack properties file.", e);
        }
        webhookUrl = slackProperties.getProperty("slack.webhook.url");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            throw new RuntimeException("Slack Webhook URL is not configured in slack.properties.");
        }
    }

    /**
     * Processes the external task by formatting and sending a Slack message.
     *
     * @param externalTask        the external task to process
     * @param externalTaskService the service used to complete or report failures for the task
     */
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("Handling external task (Task ID: {} - Process Instance ID: {})", externalTask.getId(), externalTask.getProcessInstanceId());

        String slackMessage = externalTask.getVariable("specified-slack-message");
        String nameClient = externalTask.getVariable("name_client");
        String surnameClient = externalTask.getVariable("surname_client");
        String emailClient = externalTask.getVariable("email_client");
        String orderNumberClient = externalTask.getVariable("ordernumber_client");
        String orderCategory = externalTask.getVariable("order_category");
        String complaintDescription = externalTask.getVariable("complaint_description");
        String phoneClient = externalTask.getVariable("phone_client");

        if (slackMessage == null || slackMessage.isEmpty()) {
            handleFailure(externalTask, externalTaskService, new IllegalArgumentException("Specified Slack message is missing or empty."));
            return;
        }

        String formattedMessage = String.format(
                "Message: %s\nName: %s %s\nEmail: %s\nOrder Number: %s\nOrder Category: %s\nComplaint: %s\nPhone: %s",
                slackMessage,
                nameClient,
                surnameClient,
                emailClient,
                orderNumberClient,
                orderCategory,
                complaintDescription,
                phoneClient
        );

        try {
            sendSlackMessage(formattedMessage);
            log.info("Successfully sent Slack message: {}", formattedMessage);
            externalTaskService.complete(externalTask);
        } catch (Exception e) {
            handleFailure(externalTask, externalTaskService, e);
        }
    }

    /**
     * Sends a message to the configured Slack Webhook URL.
     *
     * @param message the message to send
     * @throws IOException if an error occurs during the HTTP request
     */
    private void sendSlackMessage(String message) throws IOException {
        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoOutput(true);

        String payload = String.format("{\"text\": \"%s\"}", message.replace("\"", "\\\""));

        try (var outputStream = connection.getOutputStream()) {
            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to send Slack message. HTTP response code: " + responseCode);
        }
    }

    /**
     * Handles task failures by reporting them back to Camunda.
     *
     * @param externalTask        the external task that failed
     * @param externalTaskService the service used to handle task failures
     * @param exception           the exception that caused the failure
     */
    private static void handleFailure(ExternalTask externalTask, ExternalTaskService externalTaskService, Exception exception) {
        String errorMessage = "Failed to send Slack message.";
        log.error(errorMessage, exception);
        externalTaskService.handleFailure(externalTask, errorMessage, exception.getMessage(), 0, 0);
    }
}
