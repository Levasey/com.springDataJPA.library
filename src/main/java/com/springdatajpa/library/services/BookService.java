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

    public List<Book> findAll(boolean sortByYear, boolean sortByGenre,
                              boolean sortByTitle, boolean sortByAuthor,
                              boolean sortByAvailability, boolean availabilityIssuedFirst) {
        Sort secondary = secondaryIndexSort(sortByYear, sortByGenre, sortByTitle, sortByAuthor);
        if (sortByAvailability) {
            if (availabilityIssuedFirst) {
                return bookRepository.findByOwnerIsNotNull(issuedListSort(secondary));
            }
            if (secondary.isUnsorted()) {
                return bookRepository.findByOwnerIsNull(Sort.unsorted());
            }
            return bookRepository.findByOwnerIsNull(secondary);
        }
        if (secondary.isUnsorted()) {
            return bookRepository.findAll();
        }
        return bookRepository.findAll(secondary);
    }

    public Book findOne(int id) {
        return bookRepository.findWithOwnerById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена (id=" + id + ")."));
    }

    @Transactional
    public void createBook(BookForm form) {
        bookRepository.save(form.toNewBook());
    }

    @Transactional
    public void update(int id, BookForm form) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена (id=" + id + ")."));
        form.applyTo(book);
    }

    @Transactional
    public void delete(int id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Книга не найдена (id=" + id + ").");
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public void release(int id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена (id=" + id + ")."));
        book.setOwner(null);
        book.setTakenAt(null);
    }

    @Transactional
    public void assign(int bookId, int personId) {
        if (personId <= 0) {
            throw new BadRequestException("Выберите читателя для выдачи книги.");
        }
        Person person = peopleRepository.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден (id=" + personId + ")."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Книга не найдена (id=" + bookId + ")."));
        if (book.getOwner() != null) {
            throw new BadRequestException("Книга уже выдана. Сначала оформите возврат.");
        }
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
                                       boolean sortByYear, boolean sortByGenre,
                                       boolean sortByTitle, boolean sortByAuthor,
                                       boolean sortByAvailability, boolean availabilityIssuedFirst) {
        if (page == null && booksPerPage == null) {
            Sort secondary = secondaryIndexSort(sortByYear, sortByGenre, sortByTitle, sortByAuthor);
            List<Book> content;
            if (sortByAvailability) {
                if (availabilityIssuedFirst) {
                    content = bookRepository.findByOwnerIsNotNull(issuedListSort(secondary));
                } else if (secondary.isUnsorted()) {
                    content = bookRepository.findByOwnerIsNull(Sort.unsorted());
                } else {
                    content = bookRepository.findByOwnerIsNull(secondary);
                }
            } else if (secondary.isUnsorted()) {
                content = bookRepository.findAll();
            } else {
                content = bookRepository.findAll(secondary);
            }
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
        return findWithPagination(pageOneBased - 1, size, sortByYear, sortByGenre,
                sortByTitle, sortByAuthor, sortByAvailability, availabilityIssuedFirst);
    }

    public Page<Book> findWithPagination(Integer page, Integer booksPerPage,
                                         boolean sortByYear, boolean sortByGenre,
                                         boolean sortByTitle, boolean sortByAuthor,
                                         boolean sortByAvailability, boolean availabilityIssuedFirst) {
        Sort secondary = secondaryIndexSort(sortByYear, sortByGenre, sortByTitle, sortByAuthor);
        if (sortByAvailability) {
            if (availabilityIssuedFirst) {
                Sort sort = issuedListSort(secondary);
                return bookRepository.findByOwnerIsNotNull(PageRequest.of(page, booksPerPage, sort));
            }
            if (secondary.isUnsorted()) {
                return bookRepository.findByOwnerIsNull(PageRequest.of(page, booksPerPage));
            }
            return bookRepository.findByOwnerIsNull(PageRequest.of(page, booksPerPage, secondary));
        }
        Pageable pageable = secondary.isUnsorted()
                ? PageRequest.of(page, booksPerPage)
                : PageRequest.of(page, booksPerPage, secondary);
        return bookRepository.findAll(pageable);
    }

    /** Порядок полей год / жанр / название / автор (без фильтра по выдаче). */
    private static Sort secondaryIndexSort(boolean sortByYear, boolean sortByGenre,
                                          boolean sortByTitle, boolean sortByAuthor) {
        Sort sort = Sort.unsorted();
        if (sortByTitle) {
            sort = sort.and(Sort.by("title"));
        }
        if (sortByAuthor) {
            sort = sort.and(Sort.by("author"));
        }
        if (sortByGenre) {
            sort = sort.and(Sort.by("genre"));
        }
        if (sortByYear) {
            sort = sort.and(Sort.by("yearPublished"));
        }
        return sort;
    }

    private static Sort issuedListSort(Sort secondary) {
        Sort byTaken = Sort.by("takenAt");
        return secondary.isUnsorted() ? byTaken : byTaken.and(secondary);
    }
}
