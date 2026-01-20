Feature: Todo Relationships
  As a user, I want to view and manage a todo’s relationships so that I can organize tasks with categories and projects.

  Background:
    Given the todo service is running

  # Category relationships (Todo -> Categories)
  # Note: this TodoManager API creates a new category under a todo; it does NOT allow linking by id.

  Scenario: Add a category to an existing todo (Normal Flow)
    Given the following todos exist in the system:
      | title            |
      | ECSE429_REL_TODO |
    When I add a new category with title "ECSE429_REL_CAT" to todo "ECSE429_REL_TODO"
    Then the response status code should be 201
    When I request categories for todo "ECSE429_REL_TODO"
    Then the response status code should be 200
    And the response should contain category "ECSE429_REL_CAT"

  Scenario: Add a category without a title (Error Flow)
    Given the following todos exist in the system:
      | title            |
      | ECSE429_REL_TODO |
    When I attempt to add a category without a title to todo "ECSE429_REL_TODO"
    Then the response status code should be 400
    And the response should contain error message "title : field is mandatory"

  Scenario: Attempt to add a category by id (Error Flow)
    Given the following todos exist in the system:
      | title            |
      | ECSE429_REL_TODO |
    When I attempt to add an existing category with id "1" to todo "ECSE429_REL_TODO"
    Then the response status code should be 400
    And the response should contain error message "Not allowed to create with id"

  # Project relationships (Project -> Tasks (Todos))
  # Note: tasks are created under a project; linking an existing todo by id is not allowed.

  Scenario: Create a task under a project and view it in project tasks (Normal Flow)
    Given the following projects exist in the system:
      | title               |
      | ECSE429_REL_PROJECT |
    When I create a todo with title "ECSE429_REL_TASK" under project "ECSE429_REL_PROJECT"
    Then the response status code should be 201
    When I request tasks for project "ECSE429_REL_PROJECT"
    Then the response status code should be 200
    And the response should contain title "ECSE429_REL_TASK"

  Scenario: Attempt to add an existing todo by id as a project task (Error Flow)
    Given the following projects exist in the system:
      | title               |
      | ECSE429_REL_PROJECT |
    When I attempt to add an existing todo with id "1" as a task to project "ECSE429_REL_PROJECT"
    Then the response status code should be 400
    And the response should contain error message "Not allowed to create with id"
