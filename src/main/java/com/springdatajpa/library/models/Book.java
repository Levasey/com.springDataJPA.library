package com.springdatajpa.library.models;

import com.springdatajpa.library.validation.YearPublished;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @Column(name = "book_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookId;

    @NotBlank(message = "Title should not be empty")
    @Size(min = 2, max = 100, message = "Title should be between 2 and 100 characters")
    @Column(name = "book_name")
    private String title;

    @NotBlank(message = "Author should not be empty")
    @Size(min = 2, max = 100, message = "Author should be between 2 and 100 characters")
    @Column(name = "author")
    private String author;

    @YearPublished
    @Column(name = "year_published")
    private int yearPublished;

    @NotNull(message = "Genre is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false, length = 32)
    private Genre genre = Genre.OTHER;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Transient
    private boolean expired;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "person_id")
    private Person owner;

    public Book() {
    }

    public Book(String title, String author, int yearPublished) {
        this(title, author, yearPublished, Genre.OTHER);
    }

    public Book(String title, String author, int yearPublished, Genre genre) {
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.genre = genre;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}
