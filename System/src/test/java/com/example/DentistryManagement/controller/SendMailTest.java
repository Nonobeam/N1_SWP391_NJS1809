package com.example.DentistryManagement.controller;

import com.example.DentistryManagement.auth.AuthenticationRequest;
import com.example.DentistryManagement.auth.AuthenticationResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SendMailTest {

    private final Logger LOGGER = LogManager.getLogger(SendMailTest.class);

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @TestFactory
    Stream<DynamicTest> testSendEmail() throws IOException, CsvValidationException {

        List<DynamicTest> dynamicTests = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(new ClassPathResource("send-mail-test-cases.csv").getFile()));
        String[] line;
        reader.readNext(); // skip header

        while ((line = reader.readNext()) != null) {
            String username = line[0];
            String password = line[1];
            String notificationID = line[2];
            String mail = line[3];
            String subject = line[4];
            String text = line[5];
            String expectedErrorMessage = line.length > 6 ? line[6] : null;
            int expectedStatus = Integer.parseInt(line[7]);

            dynamicTests.add(DynamicTest.dynamicTest(
                    "Test with username: " + username + " and password: " + password,
                    () -> runTest(username, password, notificationID, mail, subject, text, expectedErrorMessage, expectedStatus)
            ));
        }
        return dynamicTests.stream();
    }

    private void runTest(String username, String password, String notificationID, String mail, String subject, String text, String expectedErrorMessage, int expectedStatus) throws Exception {
        // Perform login
        ResponseEntity<AuthenticationResponse> authResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/authenticate",
                new AuthenticationRequest(username, password),
                AuthenticationResponse.class
        );

        if (authResponse.getStatusCode() == HttpStatus.OK) {
            String authToken = authResponse.getBody().getToken();

            // Perform the request to /api/v1/staff/{notificationID}/send-email with query parameters
            var requestBuilder = MockMvcRequestBuilders
                    .post("/api/v1/staff/{notificationID}/send-email", notificationID)
                    .header("Authorization", "Bearer " + authToken)
                    .param("mail", mail)
                    .param("subject", subject)
                    .param("text", text);

            if (expectedStatus == 200) {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(expectedStatus));
            } else if (expectedStatus == 403) {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(expectedStatus));
            } else {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().isBadRequest())
                        .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                        .andExpect(result -> assertEquals("Notification not found with ID: " + notificationID, result.getResolvedException().getMessage()));
            }
        } else {
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/v1/staff/{notificationID}/send-email", notificationID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(expectedStatus));
        }
    }

}