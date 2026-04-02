package com.springdatajpa.library.dto;

import com.springdatajpa.library.models.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @Size(max = 30, message = "Patronymic should be at most 30 characters")
    private String patronymic;

    @NotBlank(message = "Email shouldn't be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Reader card number shouldn't be empty")
    @Size(max = 64, message = "Reader card number should be at most 64 characters")
    private String readerCardNumber;

    private String address;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    /** Только при создании читателя; в карточку Person не сохраняется. */
    private String password;

    private String confirmPassword;

    public static PersonForm from(Person person) {
        PersonForm form = new PersonForm();
        form.setName(person.getName());
        form.setSurname(person.getSurname());
        form.setPatronymic(person.getPatronymic());
        form.setEmail(person.getEmail());
        form.setReaderCardNumber(person.getReaderCardNumber());
        form.setAddress(person.getAddress());
        form.setDateOfBirth(person.getDateOfBirth());
        return form;
    }

    public Person toNewPerson() {
        return new Person(name, surname, patronymic, email, readerCardNumber, address, dateOfBirth);
    }

    public void applyTo(Person person) {
        person.setName(name);
        person.setSurname(surname);
        person.setPatronymic(patronymic);
        person.setEmail(email);
        person.setReaderCardNumber(readerCardNumber);
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

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReaderCardNumber() {
        return readerCardNumber;
    }

    public void setReaderCardNumber(String readerCardNumber) {
        this.readerCardNumber = readerCardNumber;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
