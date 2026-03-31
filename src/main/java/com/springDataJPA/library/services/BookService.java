package com.springDataJPA.library.services;


import com.springDataJPA.library.exception.BadRequestException;
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

import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly=true)
public class BookService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

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
        if (selectedPerson == null || selectedPerson.getPersonId() <= 0) {
            throw new BadRequestException("Choose a reader to assign the book.");
        }
        Person person = peopleRepository.findById(selectedPerson.getPersonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Person not found: id=" + selectedPerson.getPersonId()));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + bookId));
        book.setOwner(person);
        book.setTakenAt(LocalDateTime.now());
    }

    public List<Book> searchByTitle(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        return bookRepository.findByTitleStartingWithIgnoreCase(query.trim());
    }

    /**
     * Список для главной страницы книг: без параметров — все книги; с любым из параметров пагинации —
     * страница с подстановкой значений по умолчанию и ограничением размера страницы.
     */
    public List<Book> findForIndexPage(Integer page, Integer booksPerPage, boolean sortByYear) {
        if (page == null && booksPerPage == null) {
            return findAll(sortByYear);
        }
        int pageOneBased = page == null ? 1 : page;
        int size = booksPerPage == null ? DEFAULT_PAGE_SIZE : booksPerPage;
        if (pageOneBased < 1) {
            pageOneBased = 1;
        }
        if (size < 1) {
            size = 1;
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        return findWithPagination(pageOneBased - 1, size, sortByYear);
    }

    public List<Book> findWithPagination(Integer page, Integer booksPerPage, boolean sortByYear) {
        if (sortByYear)
            return bookRepository.findAll(PageRequest.of(page, booksPerPage, Sort.by("yearPublished"))).getContent();
        else
            return bookRepository.findAll(PageRequest.of(page, booksPerPage)).getContent();
    }
}
