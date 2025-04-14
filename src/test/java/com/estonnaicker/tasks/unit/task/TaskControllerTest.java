package com.estonnaicker.tasks.unit.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


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
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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
                    .andExpect(jsonPath("$.errors.id").value("Expected a number"));
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

        private List<TaskDto> tasks;
        private TaskDto todoTask;
        private TaskDto inProgressTask;
        private TaskDto doneTask;

        @BeforeEach
        public void setup() {
            todoTask = new TaskDto(1L, "Test 1", "Test 1 Description", LocalDate.now(), TaskStatus.TODO.toString());
            inProgressTask = new TaskDto(2L, "Test 2", "Test 2 Description", LocalDate.now(), TaskStatus.IN_PROGRESS.toString());
            doneTask = new TaskDto(3L, "Test 3", "Test 3 Description", LocalDate.now(), TaskStatus.DONE.toString());
            tasks = List.of(todoTask, inProgressTask, doneTask);
        }
        
        @Test
        public void getTasks_Returns_Correctly() throws Exception {
            Page<TaskDto> page = new PageImpl<>(
                tasks,
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                tasks.size()
            );
            when(taskService.getAllTasks(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(tasks.size())))
                    .andExpect(jsonPath("$.data[0].id").value(1L))
                    .andExpect(jsonPath("$.metadata.totalElements").value(3))
                    .andExpect(jsonPath("$.metadata.totalPages").value(1));
        }

        @Test
        public void getTasks_Calls_With_Status_Correctly() throws Exception {
            Page<TaskDto> page = new PageImpl<>(
                List.of(inProgressTask),
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                1
            );
            when(taskService.getAllTasks(eq(TaskStatus.IN_PROGRESS), isNull(), any(Pageable.class))).thenReturn(page);
            mockMvc.perform(get("/api/v1/tasks")
                    .param("status", TaskStatus.IN_PROGRESS.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id").value(inProgressTask.getId()))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1))
                    .andExpect(jsonPath("$.metadata.totalPages").value(1));
                    
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(taskService, times(1)).getAllTasks(eq(TaskStatus.IN_PROGRESS), isNull(), pageableCaptor.capture());
        }

        @Test
        public void getTasks_Errors_With_Invalid_Status() throws Exception {
            mockMvc.perform(get("/api/v1/tasks")
                    .param("status", "notAValidStatus")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Type"))
                    .andExpect(jsonPath("$.errors.status").value("Expected values [TODO, IN_PROGRESS, DONE]"));
        }
        
        @Test
        public void getTasks_Calls_With_DueDate_Correctly() throws Exception {
            LocalDate now = LocalDate.now();
            TaskDto nowTask = new TaskDto(1L, "Date Test", "Date Test Desc", now, TaskStatus.IN_PROGRESS.toString());

            Page<TaskDto> page = new PageImpl<>(
                List.of(nowTask),
                PageRequest.of(0, 10, Sort.by("id").ascending()),
                tasks.size()
            );
            when(taskService.getAllTasks(isNull(), eq(now), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/tasks")
                    .param("dueDate", String.valueOf(now))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id").value(nowTask.getId()))
                    .andExpect(jsonPath("$.metadata.totalElements").value(1))
                    .andExpect(jsonPath("$.metadata.totalPages").value(1));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(taskService, times(1)).getAllTasks(isNull(), eq(now), pageableCaptor.capture());
        }

        @Test
        public void getTasks_Errors_With_Invalid_DueDate() throws Exception {
            mockMvc.perform(get("/api/v1/tasks")
                    .param("dueDate","invalidDate")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid Input Type"))
                    .andExpect(jsonPath("$.errors.dueDate").value("Invalid date format supplied, expected yyyy-MM-dd HH:mm:ss"));
        }
        
        @Test
        public void getTasks_Sets_Pagination_Values_Correctly() throws Exception {
            int pageNo = 0;
            int size = 2;

            Page<TaskDto> page = new PageImpl<>(
                tasks,
                PageRequest.of(pageNo, size, Sort.by("id").ascending()),
                tasks.size()
            );
            when(taskService.getAllTasks(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/tasks")
                    .param("page", String.valueOf(pageNo))
                    .param("size", String.valueOf(size))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(tasks.size())))
                    .andExpect(jsonPath("$.data[0].id").value(1L))
                    .andExpect(jsonPath("$.metadata.totalElements").value(tasks.size()))
                    .andExpect(jsonPath("$.metadata.totalPages").value(2));
            
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(taskService, times(1)).getAllTasks(isNull(), isNull(), pageableCaptor.capture());
        }

        @Test
        public void getTasks_Sets_Sort_Values_Correctly() throws Exception {
            String sortColumn = "id";
            String sortDir = Sort.Direction.ASC.toString();

            Page<TaskDto> page = new PageImpl<>(
                tasks,
                PageRequest.of(0, 10, Sort.by(sortColumn).ascending()),
                tasks.size()
            );
            when(taskService.getAllTasks(isNull(), isNull(), any(Pageable.class))).thenReturn(page);
            
            mockMvc.perform(get("/api/v1/tasks")
                    .param("sort", sortColumn + "," + sortDir)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(tasks.size())))
                    .andExpect(jsonPath("$.data[0].id").value(1L))
                    .andExpect(jsonPath("$.metadata.totalElements").value(tasks.size()))
                    .andExpect(jsonPath("$.metadata.totalPages").value(1));
            
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(taskService, times(1)).getAllTasks(isNull(), isNull(), pageableCaptor.capture());
        }
        
        @Test
        public void getTasks_Can_Handle_All_Params() throws Exception {
            TaskStatus status = TaskStatus.IN_PROGRESS;
            // dueDate Filter Prep
            LocalDate now = LocalDate.now();
            TaskDto nowTask = new TaskDto(4L, "Date Test", "Date Test Desc", now, TaskStatus.IN_PROGRESS.toString());
            List<TaskDto>  allParamsTasks = new ArrayList<>(tasks);
            allParamsTasks.add(nowTask);

            // Pagination
            int pageNo = 0;
            int size = 2;
            // Sort
            String sortColumn = "id";
            String sortDir = Sort.Direction.ASC.toString();

            Page<TaskDto> page = new PageImpl<>(
                allParamsTasks,
                PageRequest.of(pageNo, size, Sort.by(sortColumn).ascending()),
                allParamsTasks.size()
            );
            when(taskService.getAllTasks(eq(status), eq(now), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/v1/tasks")
                    .param("status", status.toString())
                    .param("dueDate", String.valueOf(now))
                    .param("page", String.valueOf(pageNo))
                    .param("size", String.valueOf(size))
                    .param("sort", sortColumn + "," + sortDir)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(allParamsTasks.size())))
                    .andExpect(jsonPath("$.metadata.totalElements").value(4))
                    .andExpect(jsonPath("$.metadata.totalPages").value(2)); // 2 because we said size is 2 and total elements is 4

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(taskService, times(1)).getAllTasks(eq(status), eq(now), pageableCaptor.capture());
        }
    }

    @Nested
    class CreateTask {
        private TaskCreateDto createTaskDto;
        private TaskDto taskDto;

        @BeforeEach
        public void setup() {
            // Create dummy DTO
            LocalDate now = LocalDate.now().plusDays(1);
            createTaskDto = new TaskCreateDto();
            createTaskDto.setTitle("Test Task");
            createTaskDto.setDescription("Test Task Desc");
            createTaskDto.setDueDate(now);
            createTaskDto.setStatus(TaskStatus.IN_PROGRESS.toString());
            // The returned dto
            taskDto = new TaskDto();
            taskDto.setId(1L);
            taskDto.setTitle(createTaskDto.getTitle());
            taskDto.setDescription(createTaskDto.getDescription());
            taskDto.setDueDate(now);
            taskDto.setStatus(createTaskDto.getStatus());
        }
        
        @Test
        public void Can_CreateTask() throws Exception {
            when(taskService.createTask(any(TaskCreateDto.class))).thenReturn(taskDto);
            
            mockMvc.perform(
                post("/api/v1/tasks/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskDto))
            ).andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.title").value(createTaskDto.getTitle()))
            .andExpect(jsonPath("$.data.description").value(createTaskDto.getDescription()))
            .andExpect(jsonPath("$.data.status").value(createTaskDto.getStatus()));
    
            verify(taskService, times(1)).createTask(any(TaskCreateDto.class));
        }

        @Test
        public void createTask_Errors_With_Invalid_Title_Field() throws Exception {
            // Invalid title
            createTaskDto.setTitle(null);

            mockMvc.perform(
                post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTaskDto))
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid arguments provided"))
            .andExpect(jsonPath("$.errors.title").value("Title cannot be empty"));
        }
    }

    @Nested
    class UpdateTask {
        private TaskUpdateDto updateTask;
        private TaskDto taskDto;

        @BeforeEach
        public void setup() {
            // Create dummy DTO
            LocalDate now = LocalDate.now().plusDays(1);
            updateTask = new TaskUpdateDto();
            updateTask.setTitle("Test Task");
            updateTask.setStatus(TaskStatus.IN_PROGRESS.toString());
            // The returned dto
            taskDto = new TaskDto();
            taskDto.setId(1L);
            taskDto.setTitle(updateTask.getTitle());
            taskDto.setDescription(updateTask.getDescription());
            taskDto.setDueDate(now);
            taskDto.setStatus(updateTask.getStatus());
        }
        
        @Test
        public void Can_UpdateTask() throws Exception {
            Long id = taskDto.getId();
            when(taskService.updateTask(eq(id), any(TaskUpdateDto.class))).thenReturn(taskDto);
            
            mockMvc.perform(
                patch("/api/v1/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTask))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value(updateTask.getTitle()))
            .andExpect(jsonPath("$.data.description").value(updateTask.getDescription()))
            .andExpect(jsonPath("$.data.status").value(updateTask.getStatus()));
    
            verify(taskService, times(1)).updateTask(eq(taskDto.getId()), any(TaskUpdateDto.class));
        }

        @Test
        public void updateTask_Errors_With_Invalid_Field() throws Exception {
            Long id = taskDto.getId();
            updateTask.setTitle("err");
            
            mockMvc.perform(
                patch("/api/v1/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTask))
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid arguments provided"))
            .andExpect(jsonPath("$.errors.title").value("Title must be at least 5 characters"));

            verify(taskService, never()).updateTask(eq(id), any(TaskUpdateDto.class));
        }
    }

    @Nested
    class DeleteTask {

        @Test
        public void deleteTask_Returns_Correctly() throws Exception {
            Long id = 1L;
            
            mockMvc.perform(
                delete("/api/v1/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(id));
        }

        @Test
        public void deleteTask_Errors_With_Invalid_ID() throws Exception {
            String id = "Not Valid";
            
            mockMvc.perform(
                delete("/api/v1/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.id").value("Expected a number"));
        }
    }
}
