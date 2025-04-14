package com.estonnaicker.tasks.task;

import java.time.LocalDate;
import javax.persistence.*;

import com.estonnaicker.tasks.utils.enums.TaskStatus;

import lombok.*;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Task {
    
    @Id
    @SequenceGenerator(
        name = "task_sequence",
        sequenceName = "task_sequence",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_sequence")
    private Long id;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private String title;
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
}
