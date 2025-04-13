package com.estonnaicker.tasks.unit.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.estonnaicker.tasks.exception.GlobalExceptionHandler;
import com.estonnaicker.tasks.exception.ResourceNotFoundException;
import com.estonnaicker.tasks.task.TaskController;
import com.estonnaicker.tasks.task.TaskService;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
public class TaskControllerTest {
    
    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                                 .setControllerAdvice(new GlobalExceptionHandler())
                                 .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                                 .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    class GetTaskById {
        @Test
        public void getTask() throws Exception {
            // Create dummy DTO
            TaskDto taskDto = new TaskDto();
            taskDto.setId(1L);
            taskDto.setTitle("Test Task");
            // Return it when service layer is invoked
            when(taskService.getTaskById(1L)).thenReturn(taskDto);
            
            mockMvc.perform(get("/api/v1/tasks/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(taskDto.getId()))
                    .andExpect(jsonPath("$.data.title").value(taskDto.getTitle()));
    
            verify(taskService, times(1)).getTaskById(taskDto.getId());
        }
    
        @Test
        public void getTask_Invalid_Task_Id_Throws_Error() throws Exception {
            mockMvc.perform(get("/api/v1/tasks/gbhjn")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Type"))
                    .andExpect(jsonPath("$.errors.id").value("Expected type Long"));
        }
    
        @Test
        public void getTask_Throws_NotFound() throws Exception {
            when(taskService.getTaskById(2L)).thenThrow(new ResourceNotFoundException("Task", "id", 2L));
            mockMvc.perform(get("/api/v1/tasks/2")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Task not found with id : 2"));
        }
    }

    @Nested
    class GetTasks {
        @BeforeEach
        public void setup() {
            List<TaskDto> tasks = List.of(
                new TaskDto(1L, "Test 1", "Test 1 Description", LocalDate.now(), TaskStatus.TODO.toString()),
                new TaskDto(2L, "Test 2", "Test 2 Description", LocalDate.now(), TaskStatus.IN_PROGRESS.toString())
            );
            Page<TaskDto> page = new PageImpl<>(
                tasks,
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                1
            );
            when(taskService.getAllTasks(isNull(), isNull(), any(Pageable.class))).thenReturn(page);
        }
        
        @Test
        public void getTasks_Returns_Correctly() throws Exception {
            mockMvc.perform(get("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id").value(1L))
                    .andExpect(jsonPath("$.metadata.totalElements").value(2))
                    .andExpect(jsonPath("$.metadata.totalPages").value(1));
        }
    }
}
