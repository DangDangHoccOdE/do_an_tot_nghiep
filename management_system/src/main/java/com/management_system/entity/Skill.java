package com.management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "skills")
@Getter
@Setter
public class Skill extends BaseEntity {
    @Column(length = 100, unique = true)
    private String name; // Java, Python, PHP, React, etc.

    @Column(length = 500)
    private String description;

    @Column(name = "category")
    private String category; // Backend, Frontend, Mobile, DevOps, etc.
}
