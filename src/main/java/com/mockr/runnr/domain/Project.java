package com.mockr.runnr.domain;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "project")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "context_path", unique = true, nullable = false)
    private String contextPath;
}
