Feature: View Todos
  As a user, I want to retrieve todos so that I can see what tasks exist.

  Background:
    Given the todo service is running
    And the following todos exist in the system:
      | title    | doneStatus |
      | Task 1   | false      |
      | Task 2   | true       |

  # Normal Flow: Get all todos
  Scenario Outline: Retrieve all todos (Normal Flow)
    When I request all todos
    Then the response status code should be 200
    And the response should contain "<count>" todos

    Examples:
      | count |
      | 2     |

  # Alternate Flow: Get a specific todo by ID
  Scenario Outline: Retrieve a specific todo (Alternate Flow)
    When I request the todo with title "<title>"
    Then the response status code should be 200
    And the response should contain title "<title>"

    Examples:
      | title  |
      | Task 1 |

  # Error Flow: Get a todo that does not exist
  Scenario Outline: Retrieve non-existent todo (Error Flow)
    When I request a todo with invalid ID "<id>"
    Then the response status code should be 404
    And the response should contain error message "<error>"

    Examples:
      | id    | error                                         |
      | 99999 | Could not find an instance with todos/99999   |
