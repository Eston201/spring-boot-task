package com.estonnaicker.tasks.task;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.estonnaicker.tasks.exception.ErrorResponse;
import com.estonnaicker.tasks.exception.InvalidTaskFieldException;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.ApiResponse;
import com.estonnaicker.tasks.utils.PagedApiResponse;
import com.estonnaicker.tasks.utils.enums.TaskStatus;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping(path = "/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("{id}")
    public ApiResponse<TaskDto> getTask(@PathVariable("id") Long taskId) {
        TaskDto taskDto = this.taskService.getTaskById(taskId);
        return new ApiResponse<TaskDto>(taskDto);
    }

    @GetMapping
    public PagedApiResponse<List<TaskDto>, TaskDto> getTasks(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<TaskDto> page = taskService.getAllTasks(status, dueDate, pageable);
        return new PagedApiResponse<List<TaskDto>, TaskDto>(page.getContent(), page);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@Valid @RequestBody TaskCreateDto taskdDto) {
        TaskDto createdTaskDto = taskService.createTask(taskdDto);

        return new ResponseEntity<ApiResponse<TaskDto>>(
            new ApiResponse<TaskDto>(createdTaskDto), 
            HttpStatus.CREATED
        );
    }

    @PatchMapping("{id}")
    public ApiResponse<TaskDto> updateTask(@PathVariable("id") Long taskId, @Valid @RequestBody TaskUpdateDto taskDto) {
        TaskDto task = taskService.updateTask(taskId, taskDto);
        return new ApiResponse<TaskDto>(task);
    }

    @DeleteMapping("{id}")
    public ApiResponse<Long> deleteTask(@PathVariable("id") Long taskId) {
        taskService.deleteTask(taskId);
        return new ApiResponse<Long>(taskId);
    }

    @ExceptionHandler(InvalidTaskFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTaskFieldException(InvalidTaskFieldException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Invalid field for task");
        errorResponse.setTimeStamp(System.currentTimeMillis());  
        errorResponse.addError(ex.getField(), ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Invalid Input Type");
        errorResponse.setTimeStamp(System.currentTimeMillis());

        if (ex.getRequiredType() == TaskStatus.class) {
            errorResponse.addError("status", "Expected values " + Arrays.toString(TaskStatus.values()));
        }
        
        else if (ex.getRequiredType() == LocalDate.class) {
            errorResponse.addError("dueDate", "Invalid date format supplied, expected yyyy-MM-dd HH:mm:ss");
        }

        else if(ex.getRequiredType() == Long.class) {
            errorResponse.addError(ex.getName(), "Expected a number");
        }

        else {
            errorResponse.addError(ex.getName(), "Expected type " + ex.getRequiredType().getSimpleName());
        }
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
