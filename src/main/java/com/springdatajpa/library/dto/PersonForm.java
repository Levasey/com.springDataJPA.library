package com.springdatajpa.library.dto;

import com.springdatajpa.library.models.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PersonForm {

    @NotBlank(message = "Name shouldn't be empty")
    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 characters")
    private String name;

    @NotBlank(message = "Surname shouldn't be empty")
    @Size(min = 2, max = 30, message = "Surname should be between 2 and 30 characters")
    private String surname;

    @NotBlank(message = "Email shouldn't be empty")
    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "[A-Z]\\w+, [A-Z]\\w+, \\d{6}",
            message = "Your address should be in this format: Country, City, Postal Code (6 digits)")
    private String address;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    public static PersonForm from(Person person) {
        PersonForm form = new PersonForm();
        form.setName(person.getName());
        form.setSurname(person.getSurname());
        form.setEmail(person.getEmail());
        form.setAddress(person.getAddress());
        form.setDateOfBirth(person.getDateOfBirth());
        return form;
    }

    public Person toNewPerson() {
        return new Person(name, surname, email, address, dateOfBirth);
    }

    public void applyTo(Person person) {
        person.setName(name);
        person.setSurname(surname);
        person.setEmail(email);
        person.setAddress(address);
        person.setDateOfBirth(dateOfBirth);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
