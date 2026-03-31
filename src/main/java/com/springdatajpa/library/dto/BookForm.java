package com.springdatajpa.library.dto;

import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.validation.YearPublished;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookForm {

    @NotBlank(message = "Title should not be empty")
    @Size(min = 2, max = 100, message = "Title should be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Author should not be empty")
    @Size(min = 2, max = 100, message = "Author should be between 2 and 100 characters")
    private String author;

    @YearPublished
    private int yearPublished;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setYearPublished(book.getYearPublished());
        return form;
    }

    public Book toNewBook() {
        return new Book(title, author, yearPublished);
    }

    public void applyTo(Book book) {
        book.setTitle(title);
        book.setAuthor(author);
        book.setYearPublished(yearPublished);
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
}
