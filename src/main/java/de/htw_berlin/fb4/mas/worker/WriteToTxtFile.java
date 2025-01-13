package de.htw_berlin.fb4.mas.worker;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of {@link ExternalTaskHandler} that processes external tasks
 * by writing specific process variables to a text file.
 *
 * <p>This class is used in Camunda external task patterns to handle tasks by
 * extracting process variables and persisting them in a structured format to a
 * file for documentation purposes. If writing fails, the failure is
 * reported back to Camunda.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Logs task details and processing status using SLF4J.</li>
 *     <li>Writes extracted variables to a file in a structured format.</li>
 *     <li>Handles errors gracefully by reporting failures to Camunda.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>
 * ExternalTaskHandler handler = new WriteToTxtFile();
 * </pre>
 */
public class WriteToTxtFile implements ExternalTaskHandler {

    /**
     * Logger instance for logging task-related information.
     */
    private static final Logger log = LoggerFactory.getLogger(WriteToTxtFile.class);
    /**
     * The file path where task details will be written.
     */
    private static final Path FILE_PATH = Path.of("src/main/resources/docu/verluste.txt");

    /**
     * Executes the external task by retrieving variables, formatting them,
     * and writing to a specified text file.
     *
     * @param externalTask       the external task to be processed
     * @param externalTaskService the service used to signal task completion or handle failures
     */
    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("Handling external task (Task ID: {} - Process Instance ID: {})", externalTask.getId(), externalTask.getProcessInstanceId());

        // Safely retrieve variables and convert to strings
        String orderNumberClient = getStringVariable(externalTask, "ordernumber_client");
        String orderCategory = getStringVariable(externalTask, "order_category");
        String complaintDescription = getStringVariable(externalTask, "complaint_description");
        String emailClient = getStringVariable(externalTask, "email_client");
        String nameClient = getStringVariable(externalTask, "name_client");
        String surnameClient = getStringVariable(externalTask, "surname_client");
        String packageLost = getStringVariable(externalTask, "package_lost");
        String lastDestination = getStringVariable(externalTask, "last_destination");

        // Format the data into a single line
        String line = String.format("%s | %s | %s | %s | %s %s | %s | %s%n",
                orderNumberClient, orderCategory, complaintDescription, emailClient, nameClient, surnameClient, packageLost, lastDestination);

        // Write to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(FILE_PATH.toString()), true))) {
            writer.write(line);
            log.info("Successfully wrote to the file: {}", FILE_PATH);
            externalTaskService.complete(externalTask);
        } catch (IOException e) {
            String errorMessage = "Failed to write to the file.";
            log.error(errorMessage, e);
            externalTaskService.handleFailure(externalTask, errorMessage, e.getMessage(), 0, 0);
        }
    }

    /**
     * Retrieves a process variable as a string from the external task.
     *
     * @param externalTask the external task containing variables
     * @param variableName the name of the variable to retrieve
     * @return the variable value as a string, or an empty string if the variable is null
     */
    private String getStringVariable(ExternalTask externalTask, String variableName) {
        Object value = externalTask.getVariable(variableName);
        return value != null ? value.toString() : "";
    }

}