package com.estonnaicker.tasks.task.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.estonnaicker.tasks.exception.ResourceNotFoundException;
import com.estonnaicker.tasks.task.Task;
import com.estonnaicker.tasks.task.TaskMapper;
import com.estonnaicker.tasks.task.TaskRepository;
import com.estonnaicker.tasks.task.TaskService;
import com.estonnaicker.tasks.task.TaskSpecification;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SimpleTaskService implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public TaskDto createTask(TaskCreateDto taskDto) {
        Task task = this.taskMapper.toEntity(taskDto);
        this.taskRepository.save(task);
        return this.taskMapper.toDto(task);
    }

    @Override
    public TaskDto getTaskById(Long taskId) {
        Optional<Task> task = this.taskRepository.findByIdAndIsArchivedFalse(taskId);

        if (!task.isPresent()) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }
        return this.taskMapper.toDto(task.get());
    }

    @Override
    public Page<TaskDto> getAllTasks(TaskStatus status, LocalDate dueDate, Pageable pageable) {
        this.validateSort(pageable);
        Specification<Task> spec = Specification.where(TaskSpecification.isNotArchived())
                                                .and(TaskSpecification.hasStatus(status))
                                                .and(TaskSpecification.hasDueDate(dueDate));

        Page<Task> tasks = this.taskRepository.findAll(spec, pageable);
        return this.taskMapper.toDtoPaged(tasks);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long taskId, TaskUpdateDto taskDto) {
        Task foundTask = this.taskMapper.toEntity(this.getTaskById(taskId));

        this.taskMapper.updateTaskFromDto(taskDto, foundTask);
        Task updatedTask = this.taskRepository.save(foundTask);
        return this.taskMapper.toDto(updatedTask);
    }

    @Override
    public void deleteTask(Long taskId) {
        Task foundTask = this.taskMapper.toEntity(this.getTaskById(taskId));
        foundTask.setArchived(true);
        this.taskRepository.save(foundTask);
    }

    private void validateSort(Pageable pageable) {
        List<String> allowedFields = List.of("id", "title", "dueDate", "status");
    
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            // Validate sort property name
            if (!allowedFields.contains(property)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Invalid sort property: '%s'. Valid properties are: %s",
                        property,
                        allowedFields
                    )
                );
            }
        }
    }
}
