package com.estonnaicker.tasks.integration.task;


import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.estonnaicker.tasks.exception.InvalidTaskFieldException;
import com.estonnaicker.tasks.task.Task;
import com.estonnaicker.tasks.task.TaskMapper;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

@SpringBootTest
public class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void canMapTaskToDto() {
        Task task = new Task(
            1L,
            false,
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS
        );

        TaskDto taskDto = taskMapper.toDto(task);
        assertEquals(taskDto.getId(), task.getId());
        assertEquals(taskDto.getTitle(), task.getTitle());
        assertEquals(taskDto.getDescription(), task.getDescription());
        assertEquals(taskDto.getDueDate().toString(), task.getDueDate().toString());
        assertEquals(taskDto.getStatus(), task.getStatus().toString());
    }

    @Test
    void canMapDtoToTask() {
        TaskDto taskDto = new TaskDto(
            1L, 
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );

        Task task = taskMapper.toEntity(taskDto);
        assertEquals(taskDto.getId(), task.getId());
        assertEquals(taskDto.getTitle(), task.getTitle());
        assertEquals(taskDto.getDescription(), task.getDescription());
        assertEquals(taskDto.getDueDate().toString(), task.getDueDate().toString());
        assertEquals(taskDto.getStatus(), task.getStatus().toString());
    }

    @Test
    void canMapCreateDtoToTask() {
        TaskCreateDto createDto = new TaskCreateDto(
            "Test",
            "Test Desc",
            LocalDate.now(),
            TaskStatus.IN_PROGRESS.toString()
        );

        Task task = taskMapper.toEntity(createDto);
        assertEquals(createDto.getTitle(), task.getTitle());
        assertEquals(createDto.getDescription(), task.getDescription());
        assertEquals(createDto.getDueDate().toString(), task.getDueDate().toString());
        assertEquals(createDto.getStatus(), task.getStatus().toString());
    }

    @Test
    void errorsWithInvalidStatus() {
        TaskDto taskDto = new TaskDto(
            1L, 
            "Test",
            "Test Desc",
            LocalDate.now(),
            "Invalid"
        );

        InvalidTaskFieldException exception = assertThrows(InvalidTaskFieldException.class, () -> taskMapper.toEntity(taskDto));
        assertEquals(exception.getMessage(), "Expected values " + Arrays.toString(TaskStatus.values()));
    }

    @Test
    void canMapPageTaskToPageTaskDto() {
        Task task1 = new Task(1L, false, "Task 1", "Desc 1", LocalDate.now(), TaskStatus.TODO);
        Task task2 = new Task(2L, false, "Task 2", "Desc 2", LocalDate.now(), TaskStatus.IN_PROGRESS);
        Page<Task> taskPage = new PageImpl<>(List.of(task1, task2));

        Page<TaskDto> dtoPage = taskMapper.toDtoPaged(taskPage);
        
        assertEquals(2, dtoPage.getTotalElements());
        assertEquals(task1.getId(), dtoPage.getContent().get(0).getId());
        assertEquals(task2.getId(), dtoPage.getContent().get(1).getId());
        assertEquals(task1.getTitle(), dtoPage.getContent().get(0).getTitle());
    }
}
