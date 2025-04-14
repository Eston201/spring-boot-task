package com.estonnaicker.tasks.task.dto;

import java.time.LocalDate;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TaskDto {
    @ApiModelProperty(
        value = "Unique identifier of a task",
        example = "1",
        required = true
    )
    private Long id;

    @ApiModelProperty(
        value = "Title of a task",
        example = "Spring-boot: create books DTO",
        required = true
    )
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;

    @ApiModelProperty(
        value = "Description of a task",
        example = "Create books DTO to avoid entity interacting with api layer",
        required = false
    )
    private String description;

    @ApiModelProperty(
        value = "When task should be done by",
        example = "2026-01-12",
        required = true
    )
    @NotNull(message = "Due date cannot be null")
    @Future(message = "Due date must be greater than present date")
    private LocalDate dueDate;

    @ApiModelProperty(
        value = "The status of the task",
        example = "TODO",
        required = true,
        allowableValues = "TODO, IN_PROGRESS, DONE"
    )
    @NotBlank(message = "status cannot be empty")
    private String status;
}
