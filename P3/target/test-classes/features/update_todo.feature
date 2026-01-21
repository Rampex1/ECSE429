Feature: Update Todos
  As a user, I want to update a todo so that I can mark progress or fix information.

  Background:
    Given the todo service is running
    And the following todos exist in the system:
      | title       | doneStatus |
      | Office Work | false      |

  # Normal Flow: Change completion status
  Scenario Outline: Mark todo as done (Normal Flow)
    When I update the todo "<title>" to have doneStatus "<status>"
    Then the response status code should be 200
    And the todo "<title>" should have status "<status>"

    Examples:
      | title       | status |
      | Office Work | true   |

  # Alternate Flow: Partial update (Title change)
  Scenario Outline: Rename an existing todo (Alternate Flow)
    When I change the title of "<oldTitle>" to "<newTitle>"
    Then the response status code should be 200
    And the response should contain title "<newTitle>"

    Examples:
      | oldTitle    | newTitle     |
      | Office Work | Home Work    |

  # Error Flow: Update a non-existent todo
  Scenario Outline: Update invalid todo (Error Flow)
    When I try to update todo with ID "<id>" to title "<title>"
    Then the response status code should be 404

    Examples:
      | id    | title   |
      | 12345 | Ghost   |
