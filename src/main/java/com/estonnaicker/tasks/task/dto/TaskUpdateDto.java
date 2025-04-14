package com.estonnaicker.tasks.task.dto;

import java.time.LocalDate;

import lombok.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TaskUpdateDto {
    @ApiModelProperty(
        value = "title",
        example = "Updated title",
        dataType = "java.lang.String"
    )
    @Size(min = 5, message = "Title must be at least 5 characters")
    private String title;

    @ApiModelProperty(
        value = "description",
        example = "An updated description here.",
        dataType = "java.lang.String"
    )
    private String description;

    @ApiModelProperty(
        value = "dueDate",
        example = "2026-01-01"
    )
    @Future(message = "Due date must be greater than present date")
    private LocalDate dueDate;

    @ApiModelProperty(
        value = "Status of the task",
        example = "TODO",
        dataType = "java.lang.String",
        allowableValues = "TODO, IN_PROGRESS, DONE"
    )
    private String status;
}
