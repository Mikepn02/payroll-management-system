package com.mikepn.template.v1.models;

import com.mikepn.template.v1.common.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
public class Person extends AbstractEntity {


    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String phoneNumber;
    private LocalDate dateOfBirth;


    public Person(String firstName, String lastName,String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

}
