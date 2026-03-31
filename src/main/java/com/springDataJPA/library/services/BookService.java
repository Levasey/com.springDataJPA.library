package com.springDataJPA.library.services;


import com.springDataJPA.library.exception.ResourceNotFoundException;
import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.repositories.BookRepository;
import com.springDataJPA.library.repositories.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly=true)
public class BookService {

    private final BookRepository bookRepository;
    private final PeopleRepository peopleRepository;

    @Autowired
    public BookService(BookRepository bookRepository, PeopleRepository peopleRepository) {
        this.bookRepository = bookRepository;
        this.peopleRepository = peopleRepository;
    }


    public List<Book> findAll(boolean sortByYear) {
        if (sortByYear)
            return bookRepository.findAll(Sort.by("yearPublished"));
        else
            return bookRepository.findAll();
    }

    public Book findOne(int id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
    }

    @Transactional
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void update(int id, Book updatedBook) {
        Book bookToBeUpdated = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
        updatedBook.setBookId(id);
        updatedBook.setOwner(bookToBeUpdated.getOwner());
        bookRepository.save(updatedBook);
    }

    @Transactional
    public void delete(int id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found: id=" + id);
        }
        bookRepository.deleteById(id);
    }

    public Person getBookOwner(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
        return book.getOwner();
    }

    @Transactional
    public void release(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
        book.setOwner(null);
        book.setTakenAt(null);
    }

    @Transactional
    public void assign(int bookId, Person selectedPerson) {
        if (selectedPerson == null) {
            throw new ResourceNotFoundException("Person is required to assign a book");
        }
        Person person = peopleRepository.findById(selectedPerson.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Person not found: id=" + selectedPerson.getPersonId()));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + bookId));
        book.setOwner(person);
        book.setTakenAt(new Date());
    }

    public List<Book> searchByTitle(String query) {
        return bookRepository.findByTitleStartingWithIgnoreCase(query);
    }

    public List<Book> findWithPagination(Integer page, Integer booksPerPage, boolean sortByYear) {
        if (sortByYear)
            return bookRepository.findAll(PageRequest.of(page, booksPerPage, Sort.by("yearPublished"))).getContent();
        else
            return bookRepository.findAll(PageRequest.of(page, booksPerPage)).getContent();
    }
}
