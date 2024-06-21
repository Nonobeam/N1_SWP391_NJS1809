package com.example.DentistryManagement.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
public class LoginTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private AuthenticationController authenticateController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("Parameterized Test for Authenticate Controller")
    @ParameterizedTest(name = "{index} => mail={0}, password={1}, expectedStatus={2}")
    @CsvFileSource(resources = "/login.csv", numLinesToSkip = 1)
    void testAuthenticate(String mail, String password, int expectedStatus) throws Exception {
        String jsonRequest = String.format("{\"mail\":\"%s\",\"password\":\"%s\"}", mail, password);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().is(expectedStatus));
    }
}

