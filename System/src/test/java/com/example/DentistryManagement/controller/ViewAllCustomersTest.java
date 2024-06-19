package com.example.DentistryManagement.controller;

import com.example.DentistryManagement.auth.AuthenticationRequest;
import com.example.DentistryManagement.auth.AuthenticationResponse;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
public class ViewAllCustomersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
    }

    @TestFactory
    Stream<DynamicTest> testGetAllCustomers() throws IOException, CsvValidationException {

        List<DynamicTest> dynamicTests = new ArrayList<>();
        CSVReader reader = new CSVReader(new FileReader(new ClassPathResource("test-cases-all-customer.csv").getFile()));
        String[] line;
        reader.readNext(); // skip header

        while ((line = reader.readNext()) != null) {
            String username = line[0];
            String password = line[1];
            int expectedStatus = Integer.parseInt(line[2]);

            dynamicTests.add(DynamicTest.dynamicTest(
                    "Test with username: " + username + " and password: " + password,
                    () -> runTest(username, password, expectedStatus)
            ));
        }
        return dynamicTests.stream();
    }

    private void runTest(String username, String password, int expectedStatus) throws Exception {
        // Perform login
        ResponseEntity<AuthenticationResponse> authResponse = testRestTemplate.postForEntity(
                "/api/v1/auth/authenticate",
                new AuthenticationRequest(username, password),
                AuthenticationResponse.class
        );

        if (authResponse.getStatusCode() == HttpStatus.OK) {
            String authToken = authResponse.getBody().getToken();

            // Perform the request to /api/v1/manager/all-customer
            mockMvc.perform(get("/api/v1/staff/all-customer")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().is(expectedStatus));
        } else {
            mockMvc.perform(get("/api/v1/staff/all-customer"))
                    .andExpect(status().is(expectedStatus));
        }
    }
}
