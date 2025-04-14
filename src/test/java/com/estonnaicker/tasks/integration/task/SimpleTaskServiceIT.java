package com.estonnaicker.tasks.integration.task;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.estonnaicker.tasks.exception.ResourceNotFoundException;
import com.estonnaicker.tasks.task.Task;
import com.estonnaicker.tasks.task.TaskMapper;
import com.estonnaicker.tasks.task.TaskRepository;
import com.estonnaicker.tasks.task.TaskService;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

@SpringBootTest
public class SimpleTaskServiceIT {

    @Autowired
    TaskRepository taskRepository;
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    private TaskService taskService;

    @AfterEach
    public void cleanup() {
        taskRepository.deleteAll();
    }

    @Test
    public void testCreateTask() {
        TaskCreateDto taskDto = new TaskCreateDto(
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );

        TaskDto createdTask = this.taskService.createTask(taskDto);

        assertEquals(createdTask.getTitle(), taskDto.getTitle());
        assertEquals(createdTask.getDescription(), taskDto.getDescription());
        assertEquals(createdTask.getDueDate().toString(), taskDto.getDueDate().toString());
        assertEquals(createdTask.getStatus(), taskDto.getStatus().toString());
    }

    @Test
    public void testUpdateTask() {
        // Create a task
        TaskCreateDto taskDto = new TaskCreateDto(
            "Test Update",
            "Test update desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );
        TaskDto createdTask = this.taskService.createTask(taskDto);

        TaskUpdateDto taskUpdateDto = new TaskUpdateDto();
        taskUpdateDto.setTitle("Updated Title");
        taskUpdateDto.setDescription("Updated Description");

        TaskDto updatedTask = taskService.updateTask(createdTask.getId(), taskUpdateDto);

        assertEquals(updatedTask.getTitle(), taskUpdateDto.getTitle());
        assertEquals(updatedTask.getDescription(), taskUpdateDto.getDescription());
    }

    @Test
    public void testUpdateTaskErrorsWhenTaskNotFound() {
        taskRepository.deleteAll();
        Long id = 3L;
        TaskUpdateDto taskUpdateDto = new TaskUpdateDto();
        taskUpdateDto.setTitle("Updated Title");

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, 
            () -> taskService.updateTask(id, taskUpdateDto)
        );

        assertEquals(exception.getMessage(), "Task not found with id : " + id);
    }

    @Test
    public void testGetTaskById() {
        // Create a task
        TaskCreateDto taskDto = new TaskCreateDto(
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );

        TaskDto createdTask = this.taskService.createTask(taskDto);

        // Fetch it 
        TaskDto fetchedTask = this.taskService.getTaskById(createdTask.getId());
        assertEquals(fetchedTask.getId(), createdTask.getId());
        assertEquals(fetchedTask.getTitle(), createdTask.getTitle());
    }

    @Test
    public void testGetTaskByIdCanThrowNotFound() {
        // Fetch it 
        Long id = 11L;
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, 
            () -> this.taskService.getTaskById(id)
        );
        assertEquals(exception.getMessage(), "Task not found with id : " + id);
    }
    
    @Test
    public void testDeleteTask() {
        TaskCreateDto taskDto = new TaskCreateDto(
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );
        TaskDto createdTask = this.taskService.createTask(taskDto);

        this.taskService.deleteTask(createdTask.getId());
        // Check that it still exists as it's soft deleted
        Optional<Task> fetchedTask = this.taskRepository.findById(createdTask.getId());
        assertEquals(fetchedTask.get().getId(), createdTask.getId());
        assertEquals(fetchedTask.get().isArchived(), true);
    }

    @Test
    public void testDeleteTaskErrorsWhenNoTaskFound() {
        Long id = 1L;
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, 
            () -> taskService.deleteTask(id)
        );

        assertEquals(exception.getMessage(), "Task not found with id : " + id);
    }

    @Nested
    class getTasks {
        private List<Task> tasks;

        @BeforeEach
        public void setup() {
            tasks = taskRepository.saveAll(
                List.of(
                    new Task(1L, false, "Test 1", "Test Desc 1", LocalDate.now(), TaskStatus.IN_PROGRESS),
                    new Task(2L, false, "Test 2", "Test Desc 2", LocalDate.now().plusDays(1L), TaskStatus.TODO),
                    new Task(3L, false, "Test 3", "Test Desc 3", LocalDate.now().plusDays(2), TaskStatus.TODO),
                    new Task(4L, false, "Test 4", "Test Desc 4", LocalDate.now(), TaskStatus.TODO)
                )
            );
        }
        
        @Test
        public void testGetTasksCanReturnTasks() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(null, null, pageable);
            assertEquals(fetchedTasks.getTotalElements(), tasks.size());
            assertEquals(fetchedTasks.getTotalPages(), 1);
        }
    
        @Test
        public void testGetTasksCanPaginateTasks() {    
            Pageable pageable = PageRequest.of(0, 2);
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(null, null, pageable);

            assertEquals(fetchedTasks.getContent().size(), 2); // There should only be 2 Tasks in the returned list
            assertEquals(fetchedTasks.getTotalElements(), tasks.size());
            assertEquals(fetchedTasks.getTotalPages(), 2); // Size is 2 and total is 4 so 2 pages
        }
    
        @Test
        public void testGetTasksCanSortAsc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(null, null, pageable);
            assertEquals(fetchedTasks.getContent().size(), 2);
            assertEquals(fetchedTasks.getContent().get(0).getId(), tasks.get(0).getId());
            assertEquals(fetchedTasks.getContent().get(1).getId(), tasks.get(1).getId());
            assertEquals(fetchedTasks.getTotalElements(), tasks.size());
            assertEquals(fetchedTasks.getTotalPages(), 2);
        }

        @Test
        public void testGetTasksCanSortDesc() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(null, null, pageable);
            assertEquals(fetchedTasks.getContent().size(), 2);
            // Check the order and id
            assertEquals(fetchedTasks.getContent().get(0).getId(), tasks.get(3).getId());
            assertEquals(fetchedTasks.getContent().get(1).getId(), tasks.get(2).getId());
            assertEquals(fetchedTasks.getTotalElements(), tasks.size());
            assertEquals(fetchedTasks.getTotalPages(), 2);
        }

        @Test
        public void testGetTasksThrowsErrorWithInvalidSort() {
            Pageable pageable = PageRequest.of(0, 2, Sort.by("invalid").descending());

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getAllTasks(null, null, pageable)
            );
            assertEquals(exception.getMessage(), "Invalid sort property: 'invalid'. Valid properties are: [id, title, dueDate, status]");
        }

        @Test
        public void testGetTasksCanFilterByStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(TaskStatus.IN_PROGRESS, null, pageable);
            assertEquals(fetchedTasks.getContent().size(), 1); // Only 1 in progress task
            // Check the order and id
            assertEquals(fetchedTasks.getContent().get(0).getId(), tasks.get(0).getId());
            assertEquals(fetchedTasks.getTotalElements(), 1);
            assertEquals(fetchedTasks.getTotalPages(), 1);
        }

        @Test
        public void testGetTasksCanFilterByDueDate() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(null, LocalDate.now(), pageable);
            assertEquals(fetchedTasks.getContent().size(), 2); // 2 task in tasks has now for their dates
            // Check the order and id
            assertEquals(fetchedTasks.getContent().get(0).getId(), tasks.get(0).getId());
            assertEquals(fetchedTasks.getTotalElements(), 2);
            assertEquals(fetchedTasks.getTotalPages(), 1);
        }

        @Test
        public void testGetTasksCanFilterByDueDateAndStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<TaskDto> fetchedTasks = taskService.getAllTasks(TaskStatus.TODO, LocalDate.now().plusDays(1), pageable);
            assertEquals(fetchedTasks.getContent().size(), 1);
            // Check the order and id
            assertEquals(fetchedTasks.getContent().get(0).getId(), tasks.get(1).getId());
            assertEquals(fetchedTasks.getTotalElements(), 1);
            assertEquals(fetchedTasks.getTotalPages(), 1);
        }
    }
}
