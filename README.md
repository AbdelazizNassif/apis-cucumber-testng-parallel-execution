# Api Automation

# Used tools
- Java, rest assured, cucumber, testNG

# Design patterns used:
- Object model design pattern
- Data-driven design pattern
  - Getting configuration variables from property files (like environment url & credentials)
  - Getting test data variables from json files (data to be used in testcases)
  - Behavior driver design patterns using cucumber as bdd tool.

# How to run:
- Download the project
- Open terminal in the project folder
- Run command mvn clean, mvn test

# The following features are implemented:
- parallel execution of api tests on method level using testng
- GitHub actions for running tests and artifact the test results
- cucumber-testng parallel execution on scenarios level
- cucumber-testng runner class
- Running with the help of cucumber tags

