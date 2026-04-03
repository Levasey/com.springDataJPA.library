package com.springdatajpa.library.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "person")
public class Person {

    @Id
    @Column(name = "person_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int personId;

    @NotBlank(message = "{validation.person.name.notblank}")
    @Size(min = 2, max = 30, message = "{validation.person.name.size}")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "{validation.person.surname.notblank}")
    @Size(min = 2, max = 30, message = "{validation.person.surname.size}")
    @Column(name = "surname")
    private String surname;

    @Size(max = 30, message = "{validation.person.patronymic.size}")
    @Column(name = "patronymic", length = 30)
    private String patronymic;

    @NotBlank(message = "{validation.person.email.notblank}")
    @Email(message = "{validation.person.email.invalid}")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "{validation.person.readerCard.notblank}")
    @Size(max = 64, message = "{validation.person.readerCard.size}")
    @Column(name = "reader_card_number", nullable = false, unique = true, length = 64)
    private String readerCardNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "owner")
    private List<Book> books;

    @ManyToMany
    @JoinTable(name = "person_read_book",
            joinColumns = @JoinColumn(name = "person_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private Set<Book> readBooks = new HashSet<>();

    public Person() {
    }

    public Person(String name, String surname, String patronymic, String email, String readerCardNumber,
                  String address, LocalDate dateOfBirth) {
        this.name = name;
        this.surname = surname;
        this.patronymic = patronymic;
        this.email = email;
        this.readerCardNumber = readerCardNumber;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
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

    /**
     * ФИО в порядке «фамилия имя отчество» (отчество опускается, если не задано).
     */
    public String getFullName() {
        if (patronymic != null && !patronymic.isBlank()) {
            return surname + " " + name + " " + patronymic;
        }
        return surname + " " + name;
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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Set<Book> getReadBooks() {
        return readBooks;
    }

    public void setReadBooks(Set<Book> readBooks) {
        this.readBooks = readBooks;
    }

    @Override
    public String toString() {
        return "Person{personId=" + personId + "}";
    }
}
