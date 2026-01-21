Feature: Todo Relationships
  As a user, I want to view and manage a todo’s relationships so that I can organize tasks with categories and projects.

  Background:
    Given the todo service is running

  # --- CATEGORY RELATIONSHIPS ---

  # Normal Flow
  Scenario Outline: Add a category to an existing todo (Normal Flow)
    Given the following todos exist in the system:
      | title   |
      | <todo>  |
    When I add a new category with title "<category>" to todo "<todo>"
    Then the response status code should be 201
    And the response should contain title "<category>"

    Examples:
      | todo             | category         |
      | ECSE429_REL_TODO | ECSE429_REL_CAT  |

  # Alternate Flow
  Scenario Outline: View categories for a todo (Alternate Flow)
    Given the following todos exist in the system:
      | title  |
      | <todo> |
    And I add a new category with title "<category>" to todo "<todo>"
    When I request categories for todo "<todo>"
    Then the response status code should be 200
    And the response should contain category "<category>"

    Examples:
      | todo             | category         |
      | ECSE429_REL_TODO | ECSE429_REL_CAT  |

  # Error Flow
  Scenario Outline: Add a category with invalid data (Error Flow)
    Given the following todos exist in the system:
      | title  |
      | <todo> |
    When I attempt to add a category "<type>" to todo "<todo>"
    Then the response status code should be 400
    And the response should contain error message "<error>"

    Examples:
      | todo             | type             | error                         |
      | ECSE429_REL_TODO | without a title  | title : field is mandatory    |
      | ECSE429_REL_TODO | with an id       | Not allowed to create with id |

  # --- PROJECT RELATIONSHIPS ---

  # Normal Flow
  Scenario Outline: Create a task under a project (Normal Flow)
    Given the following projects exist in the system:
      | title     |
      | <project> |
    When I create a todo with title "<task>" under project "<project>"
    Then the response status code should be 201
    And the response should contain title "<task>"

    Examples:
      | project             | task             |
      | ECSE429_REL_PROJECT | ECSE429_REL_TASK |

  # Alternate Flow
  Scenario Outline: View all tasks for a project (Alternate Flow)
    Given the following projects exist in the system:
      | title     |
      | <project> |
    And I create a todo with title "<task>" under project "<project>"
    When I request tasks for project "<project>"
    Then the response status code should be 200
    And the response should contain title "<task>"

    Examples:
      | project             | task             |
      | ECSE429_REL_PROJECT | ECSE429_REL_TASK |

  # Error Flow
  Scenario Outline: Attempt to add project task with ID (Error Flow)
    Given the following projects exist in the system:
      | title     |
      | <project> |
    When I attempt to add an existing todo with id "<id>" as a task to project "<project>"
    Then the response status code should be 400
    And the response should contain error message "Not allowed to create with id"

    Examples:
      | project             | id |
      | ECSE429_REL_PROJECT | 1  |
