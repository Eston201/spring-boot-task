package com.estonnaicker.tasks.task.dto;

import java.time.LocalDate;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TaskCreateDto {
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;
    private String description;

    @NotNull(message = "Due date cannot be null")
    @Future(message = "Due date must be greater than present date")
    private LocalDate dueDate;
    @NotBlank(message = "status cannot be empty")
    private String status;
}
