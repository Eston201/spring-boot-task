package com.estonnaicker.tasks.task;

import java.util.Arrays;
import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import com.estonnaicker.tasks.exception.InvalidTaskFieldException;
import com.estonnaicker.tasks.task.dto.TaskCreateDto;
import com.estonnaicker.tasks.task.dto.TaskDto;
import com.estonnaicker.tasks.task.dto.TaskUpdateDto;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskMapper INSTANCE = Mappers.getMapper(TaskMapper.class);

    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    Task toEntity(TaskDto taskDto);
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "id", ignore = true)
    Task toEntity(TaskCreateDto taskCreateDto);

    TaskDto toDto(Task task);
    List<TaskDto> toDtoList(List<Task> tasks);

    default Page<TaskDto> toDtoPaged(Page<Task> tasks) {
        return tasks.map(this::toDto);
    }
    
    // Task Update mapping
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "id", ignore = true)
    void updateTaskFromDto(TaskUpdateDto dto, @MappingTarget Task entity);

    @Named("stringToStatus")
    default TaskStatus stringToStatus(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTaskFieldException("status", "Expected values " + Arrays.toString(TaskStatus.values()));
        }
    }
}
