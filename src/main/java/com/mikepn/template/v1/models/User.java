package com.mikepn.template.v1.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @Column(unique = true)
    private String email;

    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    boolean isVerified;


    @JsonIgnore
    private String verificationCode;

    @JsonIgnore
    private LocalDateTime verificationCodeGeneratedAt;

    @JsonIgnore
    private String passwordResetCode;

    @JsonIgnore
    private LocalDateTime passwordResetCodeGeneratedAt;


    @Transient
    private String fullName;



    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnore
    private Set<Role> roles;



}
