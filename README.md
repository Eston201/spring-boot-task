# Task Management API

A Spring Boot 2 application for managing tasks with CRUD operations, pagination, and soft deletion.

## Table of Contents
- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Running Tests](#running-tests)
- [API Endpoints](#api-endpoints)
- [Swagger UI](#swagger-ui)
- [Understanding Errors](#understanding-errors)

## Features
- Create tasks with title, description, due date, and status.
- Retrieve tasks by ID or fetch a paginated list with optional status and due date filters.
- Update existing tasks with partial updates.
- Soft-delete tasks by marking them as archived.
- Validate inputs (e.g., title length, future due date).
- Paginated responses with metadata (page number, size, total elements, etc.).
- Interactive API documentation via Swagger UI.
- Unit and integration tests.

## Technologies
- **Java**: 11
- **Spring Boot**: 2.3.4
- **Spring Data JPA**: database operations
- **Postgresql** Data storage
- **H2 Database**: In-memory database (used in integration test)
- **Springfox Swagger**: API documentation
- **MapStruct**: 1.5.5 for DTO-entity mapping
- **Lombok**: To reduce boilerplate code
- **Maven**: Build tool

## Prerequisites
- **Java**: JDK 11 installed
- **Maven**
- **Postgresql**
    - You have installed postgresql
    - You have created a task database and have given all privileges to your user
        * SQL
            ~~~sql
                CREATE DATABASE task;
                GRANT ALL PRIVILEGES ON DATABASE "task" TO username
            ~~~
    - A local postgresql server running

## Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/Eston201/spring-boot-task.git
   cd spring-boot-task 
   ```
2. **Running the application locally**:
    - `mvn clean install` - Install dependencies (once off)
    - `mvn spring-boot:run`
   
## Running Tests
- Component Test
    - `mvn test`
- Integration Test
    - `mvn failsafe:integration-test failsafe:verify`
- Both component and integration 
    - `mvn verify`

## API Endpoints
- The API can be reached at http://localhost:8080/api/v1/tasks

### GET /api/tasks/{id}
- Description: Retrieves a task by its ID.
- Path Parameter:
    - id: Task ID (e.g 1).
    
### GET /api/tasks
- Description: Retrieves a paginated list of tasks, with optional filtering by status and due date.
- Query Parameters:
    - status: Optional, one of TODO, IN_PROGRESS, COMPLETED (e.g., status=TODO).
    - dueDate: Optional, date in YYYY-MM-DD format (e.g., dueDate=2026-01-01).
    - page: Optional, page number (default: 0).
    - size: Optional, items per page (default: 10).
    - sort: Optional, field and direction (e.g., sort=id,asc). Allowed fields: id, title, dueDate, status
- Example query string
    -  http://localhost:8080/api/v1/tasks?status=IN_PROGRESS&dueDate=2025-04-14&page=0&size=5&sort=id,asc
- Example response: "data" contains list of tasks and "metadata" contains pagaination information
```javascript
{
    "status": "success",
    "data": [
        {
        "id": 1,
        "title": "Complete video game",
        "description": "Play the game",
        "dueDate": "2026-01-01",
        "status": "PENDING"
        }
    ],
    "metadata": {
        "pageNumber": 0,
        "pageSize": 10,
        "totalElements": 50,
        "totalPages": 5,
        "last": false
    }
}
```

### POST /api/tasks
- Description: Creates a task with title, description, due date, and status.
- Request Body - JSON:
    - title: Required, minimum 5 characters.
    - description: Optional.
    - dueDate: Required, must be a future date.
    - status: Required, one of TODO, IN_PROGRESS, COMPLETED.

### PATCH /api/tasks/{id}
- Description: Updates an existing task with partial updates
- Path Parameter: 
    - id: Task Id to update (e.g 1).
- Request Body - JSON:
    - title: Optional. If provided then minimum 5 characters.
    - description: Optional.
    - dueDate: Optional. If provided then must be a future date.
    - status: Optional. If provided then it must be one of TODO, IN_PROGRESS, COMPLETED.
    
### DELETE /api/tasks/{id}
- Description: Soft-deletes a task by marking it as archived.
- Path Parameter:
    - id: Task ID (e.g 1).
- Example Response: "data" is the ID of the task that was soft deleted
```javascript
{
    "status": "success",
    "data": 1
}
```

## Swagger UI
Interactive API documentation is available at: http://localhost:8080/swagger-ui

## Understanding Errors

### Error List Example
``` javascript
{
    "status": 400,
    "message": "Invalid Input Type",
    "timeStamp": 1744659113493,
    "errors": {
        "id": "Expected a number"
    }
}
```
This is an example error you might see when arguments are invalid. 
The keys in the errors object are the argument fields with the values being the error

### Error Object
``` javascript
{
    "status": 404,
    "message": "Task not found with id : 0",
    "timeStamp": 1744659272963
}
```
Sometimes error responses will contain a single message key with value as error. These errors might
occur when a single argument is invalid or when a more generic error message is needed.