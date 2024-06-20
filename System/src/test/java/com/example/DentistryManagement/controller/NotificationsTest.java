package com.example.DentistryManagement.controller;

import com.example.DentistryManagement.auth.AuthenticationRequest;
import com.example.DentistryManagement.auth.AuthenticationResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationsTest {

    private final Logger LOGGER = LogManager.getLogger(NotificationsTest.class);

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
    }

    @TestFactory
    Stream<DynamicTest> testGetAllNotifications() throws IOException, CsvValidationException {

        List<DynamicTest> dynamicTests = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(new ClassPathResource("notification-test-cases.csv").getFile()));
        String[] line;
        reader.readNext(); // skip header

        while ((line = reader.readNext()) != null) {
            String username = line[0];
            String password = line[1];
            int expectedStatus = Integer.parseInt(line[2]);
            String expectations = line.length > 3 ? line[3] : "";

            dynamicTests.add(DynamicTest.dynamicTest(
                    "Test with username: " + username + " and password: " + password,
                    () -> runTest(username, password, expectedStatus, expectations)
            ));

            LOGGER.info(password);
        }
        return dynamicTests.stream();
    }

    private void runTest(String username, String password, int expectedStatus, String expectations) throws Exception {
        // Perform login
        ResponseEntity<AuthenticationResponse> authResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/authenticate",
                new AuthenticationRequest(username, password),
                AuthenticationResponse.class
        );

        if (authResponse.getStatusCode() == HttpStatus.OK) {
            String authToken = authResponse.getBody().getToken();

            // Perform the request to /api/v1/manager/all-notification
            var requestBuilder = get("http://localhost:" + port + "/api/v1/staff/all-notification")
                    .header("Authorization", "Bearer " + authToken);

            var resultActions = mockMvc.perform(requestBuilder)
                    .andExpect(status().is(expectedStatus));

            if (expectedStatus == HttpStatus.OK.value() && !expectations.isEmpty()) {
                String[] checks = expectations.split(",");
                for (String check : checks) {
                    String[] parts = check.split(":");
                    resultActions.andExpect(jsonPath(parts[0]).value(parts[1]));
                }
            }
        } else {
            mockMvc.perform(get("http://localhost:" + port + "/api/v1/staff/all-notification"))
                    .andExpect(status().is(expectedStatus));
        }
    }
}
