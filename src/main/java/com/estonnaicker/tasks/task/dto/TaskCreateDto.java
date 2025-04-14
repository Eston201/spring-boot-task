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
public class TaskCreateDto {

    @ApiModelProperty(
        value = "title",
        example = "Complete video game",
        required = true,
        dataType = "java.lang.String"
    )
    @NotBlank(message = "Title cannot be empty")
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;

    @ApiModelProperty(
        value = "description",
        example = "I need to complete this video game to play the next.",
        required = false,
        dataType = "java.lang.String"
    )
    private String description;

    @ApiModelProperty(
        value = "dueDate",
        example = "2026-01-01",
        required = true
    )
    @NotNull(message = "Due date cannot be null")
    @Future(message = "Due date must be greater than present date")
    private LocalDate dueDate;
    
    @ApiModelProperty(
        value = "Status of the task",
        example = "TODO",
        required = true,
        dataType = "java.lang.String",
        allowableValues = "TODO, IN_PROGRESS, DONE"
    )
    @NotBlank(message = "status cannot be empty")
    private String status;
}
