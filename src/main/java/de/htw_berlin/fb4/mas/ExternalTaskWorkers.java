package de.htw_berlin.fb4.mas;

import de.htw_berlin.fb4.mas.worker.*;
import org.camunda.bpm.client.ExternalTaskClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Main class for registering and starting External Task Workers in a Camunda environment.
 *
 * <p>This class creates an {@link ExternalTaskClient} to connect to a Camunda engine via its REST API.
 * It subscribes to specific topics and assigns handlers to process tasks from those topics.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Configures the Camunda External Task Client with a base URL, timeouts, and task lock durations.</li>
 *     <li>Subscribes to various topics and assigns specific handlers for task processing.</li>
 *     <li>Logs the startup of the workers for monitoring purposes.</li>
 * </ul>
 *
 * <h2>Topics and Handlers:</h2>
 * The following topics and corresponding handlers are registered:
 * <ul>
 *     <li><b>print-variables</b>: Handled by {@link PrintVariables}</li>
 *     <li><b>send-mail</b>: Handled by {@link SendMail}</li>
 *     <li><b>RPA-CheckOrderNumber</b>: Handled by {@link RunUiPathRobot}</li>
 *     <li><b>post-SlackMessage</b>: Handled by {@link PostSlackMessage}</li>
 *     <li><b>write-to-txt-file</b>: Handled by {@link WriteToTxtFile}</li>
 *     <li><b>form-received-topic</b>: Handled by {@link FormReceived}</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * Run this class as a standalone Java application to start all workers and connect to the Camunda engine.
 */
public class ExternalTaskWorkers {

    /**
     * Logger instance for logging task-related information.
     */
    private static final Logger log = LoggerFactory.getLogger(ExternalTaskWorkers.class);

    /**
     * Entry point of the application. Configures and starts the External Task Client, subscribes to topics,
     * and assigns task handlers.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create and configure the External Task Client
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .asyncResponseTimeout(20000)
                .lockDuration(10000)
                .maxTasks(1)
                .build();

        // Subscribe to topics and assign handlers
        client.subscribe("print-variables").handler(new PrintVariables()).open();
        client.subscribe("send-mail").handler(new SendMail()).open();
        client.subscribe("RPA-CheckOrderNumber")
                        .handler(new RunUiPathRobot(Path.of("src/main/resources/uipath/ValidateOrderNumber.1.0.2.nupkg")))
                        .open();
        client.subscribe("post-SlackMessage").handler(new PostSlackMessage()).open();
        client.subscribe("write-to-txt-file").handler(new WriteToTxtFile()).open();
        client.subscribe("form-received-topic").handler(new FormReceived()).open();

        // Log application startup
        log.info("ExternalTaskWorkers started");
    }
}
