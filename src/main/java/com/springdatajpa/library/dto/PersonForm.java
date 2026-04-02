package com.springdatajpa.library.dto;

import com.springdatajpa.library.models.Person;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PersonForm {

    @NotBlank(message = "{validation.person.name.notblank}")
    @Size(min = 2, max = 30, message = "{validation.person.name.size}")
    private String name;

    @NotBlank(message = "{validation.person.surname.notblank}")
    @Size(min = 2, max = 30, message = "{validation.person.surname.size}")
    private String surname;

    @Size(max = 30, message = "{validation.person.patronymic.size}")
    private String patronymic;

    @NotBlank(message = "{validation.person.email.notblank}")
    @Email(message = "{validation.person.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.person.readerCard.notblank}")
    @Size(max = 64, message = "{validation.person.readerCard.size}")
    private String readerCardNumber;

    private String address;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

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
}
