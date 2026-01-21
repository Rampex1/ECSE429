Feature: Delete Todos
  As a user, I want to delete todos so that I can remove tasks I no longer need.

  Background:
    Given the todo service is running
    And the following todos exist in the system:
      | title       |
      | Temp Task   |

  # Normal Flow: Delete existing
  Scenario Outline: Delete a todo (Normal Flow)
    When I delete the todo with title "<title>"
    Then the response status code should be 200
    And the todo "<title>" should no longer exist

    Examples:
      | title     |
      | Temp Task |

  # Alternate Flow: Delete a todo that was already deleted (Idempotency check)
  Scenario Outline: Delete the same todo twice (Alternate Flow)
    Given I delete the todo with title "<title>"
    When I delete the todo with title "<title>" again
    Then the response status code should be 404

    Examples:
      | title     |
      | Temp Task |

  # Error Flow: Delete using an invalid ID format
  Scenario Outline: Delete with invalid ID (Error Flow)
    When I delete the todo with ID "<id>"
    Then the response status code should be 404

    Examples:
      | id      |
      | invalid |
