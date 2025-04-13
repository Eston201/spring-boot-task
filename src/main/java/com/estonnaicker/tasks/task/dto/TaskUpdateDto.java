package com.estonnaicker.tasks.task.dto;

import java.time.LocalDate;

import lombok.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TaskUpdateDto {
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;
    private String description;
    @Future(message = "Due date must be greater than present date")
    private LocalDate dueDate;
    private String status;
}
