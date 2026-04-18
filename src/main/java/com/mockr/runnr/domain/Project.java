package com.mockr.runnr.domain;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // works with SQLite auto-increment
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;
}
