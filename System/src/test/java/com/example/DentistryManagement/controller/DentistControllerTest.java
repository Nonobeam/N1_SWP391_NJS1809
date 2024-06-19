package com.example.DentistryManagement.controller;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import com.example.DentistryManagement.auth.AuthenticationRequest;
import com.example.DentistryManagement.auth.AuthenticationResponse;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DentistControllerTest {

    private final Logger LOGGER = LogManager.getLogger(DentistControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @TestFactory
    Stream<DynamicTest> testReminderNotice() throws IOException, CsvValidationException {

        List<DynamicTest> dynamicTests = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(new ClassPathResource("test-cases.csv").getFile()));
        String[] line;
        reader.readNext(); // skip header

        while ((line = reader.readNext()) != null) {
            String email = line[0];
            String password = line[1];
            String message = line[2];
            int expectedStatus = Integer.parseInt(line[3]);

            dynamicTests.add(DynamicTest.dynamicTest(
                    "Test with email: " + email + " and password: " + password,
                    () -> runTest(email, password, message, expectedStatus)
            ));
        }
        return dynamicTests.stream();
    }

    private void runTest(String email, String password, String message, int expectedStatus) throws Exception {
        // Perform login
        ResponseEntity<AuthenticationResponse> authResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/authenticate",
                new AuthenticationRequest(email, password),
                AuthenticationResponse.class
        );

        String json = "{\"message\":\"" + message + "\"}";

        if (authResponse.getStatusCode() == HttpStatus.OK) {
            String authToken = authResponse.getBody().getToken();

            // Perform the request to /api/v1/dentist/reminder
            mockMvc.perform(post("http://localhost:" + port + "/api/v1/dentist/reminder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + authToken)
                            .content(json))
                    .andExpect(status().is(expectedStatus));

            // Only check the message if the expected status is 200
            if (expectedStatus == 200) {
                mockMvc.perform(post("http://localhost:" + port + "/api/v1/dentist/reminder")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + authToken)
                                .content(json))
                        .andExpect(jsonPath("$.message").value(message));
            }
        } else {
            mockMvc.perform(post("http://localhost:" + port + "/api/v1/dentist/reminder")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(expectedStatus));
        }
    }
}
