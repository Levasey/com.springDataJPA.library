package com.springdatajpa.library.services;

import com.springdatajpa.library.dto.BookForm;
import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.repositories.BookRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final BookRepository bookRepository;
    private final PeopleRepository peopleRepository;

    public BookService(BookRepository bookRepository, PeopleRepository peopleRepository) {
        this.bookRepository = bookRepository;
        this.peopleRepository = peopleRepository;
    }

    public List<Book> findAll(boolean sortByYear, boolean sortByGenre) {
        Sort sort = indexSort(sortByYear, sortByGenre);
        if (sort.isUnsorted()) {
            return bookRepository.findAll();
        }
        return bookRepository.findAll(sort);
    }

    public Book findOne(int id) {
        return bookRepository.findWithOwnerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
    }

    @Transactional
    public void createBook(BookForm form) {
        bookRepository.save(form.toNewBook());
    }

    @Transactional
    public void update(int id, BookForm form) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
        form.applyTo(book);
    }

    @Transactional
    public void delete(int id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found: id=" + id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public void release(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + id));
        book.setOwner(null);
        book.setTakenAt(null);
    }

    @Transactional
    public void assign(int bookId, int personId) {
        if (personId <= 0) {
            throw new BadRequestException("Choose a reader to assign the book.");
        }
        Person person = peopleRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: id=" + personId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: id=" + bookId));
        book.setOwner(person);
        book.setTakenAt(LocalDateTime.now());
    }

    public List<Book> searchBooks(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        String q = query.trim();
        if (q.length() > 200) {
            q = q.substring(0, 200);
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(q, q);
    }

    /**
     * Без параметров пагинации — одна «страница» со всеми книгами; иначе — страница Spring Data.
     */
    public Page<Book> findForIndexPage(Integer page, Integer booksPerPage,
                                       boolean sortByYear, boolean sortByGenre) {
        if (page == null && booksPerPage == null) {
            Sort sort = indexSort(sortByYear, sortByGenre);
            List<Book> content = sort.isUnsorted()
                    ? bookRepository.findAll()
                    : bookRepository.findAll(sort);
            return new PageImpl<>(content);
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
        return findWithPagination(pageOneBased - 1, size, sortByYear, sortByGenre);
    }

    public Page<Book> findWithPagination(Integer page, Integer booksPerPage,
                                         boolean sortByYear, boolean sortByGenre) {
        Sort sort = indexSort(sortByYear, sortByGenre);
        Pageable pageable = sort.isUnsorted()
                ? PageRequest.of(page, booksPerPage)
                : PageRequest.of(page, booksPerPage, sort);
        return bookRepository.findAll(pageable);
    }

    private static Sort indexSort(boolean sortByYear, boolean sortByGenre) {
        if (sortByYear && sortByGenre) {
            return Sort.by("genre").and(Sort.by("yearPublished"));
        }
        if (sortByYear) {
            return Sort.by("yearPublished");
        }
        if (sortByGenre) {
            return Sort.by("genre");
        }
        return Sort.unsorted();
    }
}
