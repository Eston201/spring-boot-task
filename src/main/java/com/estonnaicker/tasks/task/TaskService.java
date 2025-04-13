package com.estonnaicker.tasks.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

public interface TaskService {
    TaskDto createTask(TaskCreateDto taskDto);

    TaskDto getTaskById(Long taskId);

    Page<TaskDto> getAllTasks(TaskStatus status, LocalDate dueDate, Pageable pageable);

    TaskDto updateTask(Long taskId, TaskUpdateDto taskDto);

    void deleteTask(Long taskId);
}
