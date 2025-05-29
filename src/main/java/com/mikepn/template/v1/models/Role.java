package com.mikepn.template.v1.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mikepn.template.v1.enums.ERole;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ERole name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;
}
