package com.ecse429.steps;

import com.ecse429.api.ApiClient;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.junit.jupiter.api.Assertions;

public class Hooks {

    @BeforeAll
    public static void checkServerIsUp() {
        Assertions.assertTrue(ApiClient.isServerRunning(), "ERROR: The API service is not running on localhost:4567. Tests aborted.");
    }

    @Before
    public void setup() {
        ApiClient.clearAllTodos();
        ApiClient.clearAllCategories();
        ApiClient.clearAllProjects();
    }
}
