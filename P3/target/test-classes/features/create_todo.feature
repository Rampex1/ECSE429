Feature: Create Todos
  As a user, I want to create new todos so that I can track tasks I need to complete.

  Background:
    Given the todo service is running

  # Normal Flow: Successful creation with JSON
  Scenario Outline: Create a valid todo (Normal Flow)
    When I create a todo with title "<title>" and description "<description>"
    Then the response status code should be 201
    And the response should contain title "<title>"
    And the todo should exist in the system

    Examples:
      | title      | description          |
      | Buy milk   | Need 2% fat          |
      | ECSE 429   | Finish assignment 1  |

  # Alternate Flow: Create todo using XML format
  Scenario Outline: Create a todo with XML (Alternate Flow)
    When I create a todo with title "<title>" using XML format
    Then the response status code should be 201
    And the response should contain title "<title>"

    Examples:
      | title         |
      | Submit Report |

  # Error Flow: Create todo with missing title (Bad Request)
  Scenario Outline: Create a todo with invalid data (Error Flow)
    When I attempt to create a todo with no title and only description "<description>"
    Then the response status code should be 400
    And the response should contain error message "<error>"

    Examples:
      | description   | error                                     |
      | Missing Title | title : field is mandatory                |
