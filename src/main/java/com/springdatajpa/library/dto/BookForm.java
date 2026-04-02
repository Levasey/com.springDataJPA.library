package com.springdatajpa.library.dto;

import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Genre;
import com.springdatajpa.library.validation.YearPublished;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BookForm {

    @NotBlank(message = "{validation.book.title.notblank}")
    @Size(min = 2, max = 100, message = "{validation.book.title.size}")
    private String title;

    @NotBlank(message = "{validation.book.author.notblank}")
    @Size(min = 2, max = 100, message = "{validation.book.author.size}")
    private String author;

    @YearPublished
    private int yearPublished;

    @NotNull(message = "{validation.book.genre.notnull}")
    private Genre genre = Genre.OTHER;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setYearPublished(book.getYearPublished());
        form.setGenre(book.getGenre());
        return form;
    }

    public Book toNewBook() {
        return new Book(title, author, yearPublished, genre);
    }

    public void applyTo(Book book) {
        book.setTitle(title);
        book.setAuthor(author);
        book.setYearPublished(yearPublished);
        book.setGenre(genre);
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
