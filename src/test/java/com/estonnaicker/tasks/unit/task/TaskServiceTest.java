package com.estonnaicker.tasks.unit.task;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.estonnaicker.tasks.exception.GlobalExceptionHandler;
import com.estonnaicker.tasks.exception.ResourceNotFoundException;
import com.estonnaicker.tasks.task.Task;
import com.estonnaicker.tasks.task.TaskMapper;
import com.estonnaicker.tasks.task.TaskRepository;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.task.impl.SimpleTaskService;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
public class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SimpleTaskService taskService;
    
    @Mock
    private TaskMapper taskMapper;

    private Task task;
    private TaskDto taskDto;
    private TaskCreateDto taskCreateDto;
    private TaskUpdateDto taskUpdateDto;
    
    @BeforeEach
    public void setup() {
        Long id = 1L;
        LocalDate now = LocalDate.now();
        TaskStatus status = TaskStatus.TODO;
        // Create DTO
        taskCreateDto = new TaskCreateDto();
        taskCreateDto.setTitle("Test Create Task");
        taskCreateDto.setDescription("Test Create Task Description");
        taskCreateDto.setDueDate(now);
        taskCreateDto.setStatus(status.toString());
        // Update DTO
        taskUpdateDto = new TaskUpdateDto();
        taskUpdateDto.setTitle(taskCreateDto.getTitle());
        taskUpdateDto.setDescription(taskCreateDto.getDescription());
        taskUpdateDto.setDueDate(now);
        taskUpdateDto.setStatus(status.toString());
        // Entity
        task = new Task();
        task.setId(id);
        task.setTitle(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getDescription());
        task.setDueDate(now);
        task.setStatus(status);
        task.setArchived(false);
        // DTO
        taskDto = new TaskDto();
        taskDto.setId(id);
        taskDto.setTitle(taskCreateDto.getTitle());
        taskDto.setDescription(taskCreateDto.getDescription());
        taskDto.setDueDate(now);
        taskDto.setStatus(status.toString());
    }

    @Test
    public void Can_CreateTask() {
        when(taskMapper.toEntity(taskCreateDto)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDto);
        when(taskRepository.save(task)).thenReturn(task);

        // Test create
        TaskDto returnedDto = taskService.createTask(taskCreateDto);
        
        assertNotNull(returnedDto);
        assertEquals(returnedDto.getId(), task.getId());
        assertEquals(returnedDto.getTitle(), taskCreateDto.getTitle());
        assertEquals(returnedDto.getStatus(), taskCreateDto.getStatus());
        assertEquals(returnedDto.getDescription(), taskCreateDto.getDescription());
    
        verify(taskRepository, times(1)).save(task);
        verify(taskMapper, times(1)).toEntity(taskCreateDto);
        verify(taskMapper, times(1)).toDto(task);
    }

    @Test
    public void getTaskById_Returns_Correctly() {
        Long id = 1L;
        when(taskRepository.findByIdAndIsArchivedFalse(id)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDto);

        TaskDto returnedDto = taskService.getTaskById(id);
        assertEquals(returnedDto.getId(), task.getId());
        assertEquals(returnedDto.getTitle(), task.getTitle());
        assertEquals(returnedDto.getStatus(), task.getStatus().toString());
        assertEquals(returnedDto.getDescription(), task.getDescription());

        verify(taskRepository, times(1)).findByIdAndIsArchivedFalse(id);
        verify(taskMapper, times(1)).toDto(task);
    }

    @Test
    public void getTaskById_Throws_Error_When_Not_Found() {
        Long id = 1L;
        when(taskRepository.findByIdAndIsArchivedFalse(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(id));
        verify(taskRepository, times(1)).findByIdAndIsArchivedFalse(1L);
        verify(taskMapper, never()).toDto(any());
    }

    @Test
    public void getAllTasks_Returns_Correctly() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Task> taskPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(taskPage);
        when(taskMapper.toDtoPaged(taskPage)).thenReturn(new PageImpl<>(List.of(taskDto)));

        Page<TaskDto> result = taskService.getAllTasks(TaskStatus.TODO, LocalDate.now(), pageable);
        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(taskDto.getId(), result.getContent().get(0).getId());

        verify(taskRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(taskMapper, times(1)).toDtoPaged(taskPage);
    }

    @Test
    public void getAllTasks_Can_Validate_SortValue() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("invalidSortColumn").ascending());
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> taskService.getAllTasks(TaskStatus.TODO, LocalDate.now(), pageable)
        );
        assertEquals("Invalid sort property: 'invalidSortColumn'. Valid properties are: [id, title, dueDate, status]", exception.getMessage());
        
        verify(taskRepository, never()).findAll();
        verify(taskMapper, never()).toDtoPaged(any());
    }
    
    @Test
    public void updateTask_Returns_Correctly() {
        Long id = 1L;
        when(taskRepository.findByIdAndIsArchivedFalse(id)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDto);
        when(taskMapper.toEntity(taskDto)).thenReturn(task);
        when(this.taskRepository.save(task)).thenReturn(task);

        TaskDto returnedDto = taskService.updateTask(id, taskUpdateDto);
        assertEquals(returnedDto.getId(), task.getId());
        assertEquals(returnedDto.getTitle(), taskUpdateDto.getTitle());
        assertEquals(returnedDto.getStatus(), taskUpdateDto.getStatus().toString());
        assertEquals(returnedDto.getDescription(), taskUpdateDto.getDescription());

        verify(taskRepository, times(1)).findByIdAndIsArchivedFalse(id);
        verify(taskRepository, times(1)).save(task);
        verify(taskMapper, times(2)).toDto(task); // 2 -> 1 from getTaskById and the return
        verify(taskMapper, times(1)).toEntity(taskDto);
    }

    @Test
    public void updateTask_Errors_When_Task_Not_Found() {
        Long id = 111L;
        when(taskRepository.findByIdAndIsArchivedFalse(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, 
            () -> taskService.updateTask(id, taskUpdateDto)
        );
        assertEquals("Task not found with id : " + id, exception.getMessage());

        verify(taskRepository, times(1)).findByIdAndIsArchivedFalse(id);
        verify(taskMapper, never()).toDto(any());
    }

    @Test
    public void deleteTask_Errors_When_Task_Not_Found() {
        Long id = 111L;
        when(taskRepository.findByIdAndIsArchivedFalse(id)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, 
            () -> taskService.deleteTask(id)
        );
        assertEquals("Task not found with id : " + id, exception.getMessage());

        verify(taskRepository, times(1)).findByIdAndIsArchivedFalse(id);
        verify(taskMapper, never()).toDto(any());
    }
}
