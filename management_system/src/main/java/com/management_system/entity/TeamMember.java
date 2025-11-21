package com.management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team_members")
@Getter
@Setter
public class TeamMember extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "role_in_team", length = 100)
    private String roleInTeam;
}
