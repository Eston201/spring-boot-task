package com.estonnaicker.tasks.task;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;
import com.estonnaicker.tasks.utils.enums.TaskStatus;

public class TaskSpecification {
    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasDueDate(LocalDate dueDate) {
        return (root, query, cb) -> dueDate == null ? null : cb.equal(root.get("dueDate"), dueDate);
    }

    public static Specification<Task> isNotArchived() {
        return (root, query, builder) -> 
            builder.isFalse(root.get("isArchived"));
    }
}
