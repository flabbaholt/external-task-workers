package de.htw_berlin.fb4.mas.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ExternalTaskHandler} for processing external tasks related to form submissions.
 *
 * <p>This class handles tasks from Camunda by retrieving process variables, formatting them into a message payload,
 * and sending the payload to the Camunda REST API for message correlation. The message name used for correlation is
 * "FormReceived".</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Fetches task variables from the Camunda context.</li>
 *     <li>Prepares and logs the payload for the REST API call.</li>
 *     <li>Sends a message correlation request to the Camunda REST API.</li>
 *     <li>Completes the external task after successful message correlation.</li>
 *     <li>Logs errors during payload preparation or message correlation.</li>
 * </ul>
 *
 *
 * <h2>Dependencies:</h2>
 * Ensure the following dependencies are included in your project:
 * <ul>
 *     <li>Spring Web (for RestTemplate)</li>
 *     <li>Jackson Databind (for JSON serialization)</li>
 *     <li>SLF4J (for logging)</li>
 * </ul>
 */
public class FormReceived implements ExternalTaskHandler {

    /**
     * Logger instance for logging task-related information.
     */
    private static final Logger log = LoggerFactory.getLogger(FormReceived.class);
    /**
     * Base URL for the Camunda REST API.
     */
    private static final String CAMUNDA_ENGINE_REST_URL = "http://localhost:8080/engine-rest"; // Adjust as needed

    /**
     * Processes the external task by retrieving variables, preparing a message payload,
     * and sending a message correlation request to the Camunda REST API.
     *
     * @param externalTask        the external task to process
     * @param externalTaskService the service used to complete or report failures for the task
     */
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("Handling external task (Task ID: {} - Process Instance ID: {})",
                externalTask.getId(), externalTask.getProcessInstanceId());

        // Fetch variables
        String nameClient = externalTask.getVariable("name_client");
        String surnameClient = externalTask.getVariable("surname_client");
        String emailClient = externalTask.getVariable("email_client");
        String orderNumberClient = externalTask.getVariable("ordernumber_client");
        String phoneClient = externalTask.getVariable("phone_client");
        String orderCategory = externalTask.getVariable("order_category");
        String complaintDescription = externalTask.getVariable("complaint_description");

        // Prepare REST API payload
        Map<String, Object> variables = new HashMap<>();
        variables.put("name_client", Map.of("value", nameClient, "type", "String"));
        variables.put("surname_client", Map.of("value", surnameClient, "type", "String"));
        variables.put("email_client", Map.of("value", emailClient, "type", "String"));
        variables.put("ordernumber_client", Map.of("value", orderNumberClient, "type", "String"));
        variables.put("phone_client", Map.of("value", phoneClient, "type", "String"));
        variables.put("order_category", Map.of("value", orderCategory, "type", "String"));
        variables.put("complaint_description", Map.of("value", complaintDescription, "type", "String"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("messageName", "FormReceived");
        payload.put("processVariables", variables);

        // Log the payload for debugging
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info("Payload: {}", objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.error("Error serializing payload:", e);
        }

        // Send message correlation request
        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(CAMUNDA_ENGINE_REST_URL + "/message", payload, Void.class);
            log.info("Message 'FormReceived' correlated successfully.");
        } catch (Exception e) {
            log.error("Error correlating message 'FormReceived':", e);
        }

        // Complete the external task
        externalTaskService.complete(externalTask);
        log.info("External task (Task ID: {}) completed.", externalTask.getId());
    }
}