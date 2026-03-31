package com.springDataJPA.library.services;

import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.repositories.BookRepository;
import com.springDataJPA.library.repositories.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void findAll_sortsByYearWhenRequested() {
        bookService.findAll(true);
        verify(bookRepository).findAll(Sort.by("year_published"));
    }

    @Test
    void findAll_withoutSortUsesFindAll() {
        bookService.findAll(false);
        verify(bookRepository).findAll();
        verify(bookRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void findOne_returnsBookWhenPresent() {
        Book book = new Book("T", "A", 2000);
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        assertSame(book, bookService.findOne(1));
    }

    @Test
    void findOne_returnsNullWhenMissing() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        assertNull(bookService.findOne(99));
    }

    @Test
    void saveBook_delegatesToRepository() {
        Book book = new Book("T", "A", 2000);
        when(bookRepository.save(book)).thenReturn(book);
        assertSame(book, bookService.saveBook(book));
        verify(bookRepository).save(book);
    }

    @Test
    void update_preservesOwnerAndSetsId() {
        Person owner = new Person();
        owner.setPersonId(5);
        Book existing = new Book("Old", "Auth", 1999);
        existing.setOwner(owner);
        when(bookRepository.findById(3)).thenReturn(Optional.of(existing));

        Book updated = new Book("New", "Auth2", 2001);
        bookService.update(3, updated);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();
        assertEquals(3, saved.getBookId());
        assertSame(owner, saved.getOwner());
    }

    @Test
    void update_doesNothingWhenBookMissing() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        bookService.update(1, new Book("X", "Y", 2000));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_delegatesToRepository() {
        bookService.delete(7);
        verify(bookRepository).deleteById(7);
    }

    @Test
    void getBookOwner_returnsOwnerOrNull() {
        Person owner = new Person();
        Book book = new Book("T", "A", 2000);
        book.setOwner(owner);
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        assertSame(owner, bookService.getBookOwner(2));

        when(bookRepository.findById(3)).thenReturn(Optional.empty());
        assertNull(bookService.getBookOwner(3));
    }

    @Test
    void release_clearsOwnerAndTakenAt() {
        Book book = new Book("T", "A", 2000);
        book.setOwner(new Person());
        book.setTakenAt(new Date());
        when(bookRepository.findById(4)).thenReturn(Optional.of(book));

        bookService.release(4);

        assertNull(book.getOwner());
        assertNull(book.getTakenAt());
    }

    @Test
    void assign_doesNothingWhenPersonNull() {
        bookService.assign(1, null);
        verify(peopleRepository, never()).findById(anyInt());
    }

    @Test
    void assign_setsOwnerAndTakenAtWhenBothExist() {
        Person person = new Person();
        person.setPersonId(10);
        Book book = new Book("T", "A", 2000);
        when(peopleRepository.findById(10)).thenReturn(Optional.of(person));
        when(bookRepository.findById(8)).thenReturn(Optional.of(book));

        bookService.assign(8, person);

        assertSame(person, book.getOwner());
        assertNotNull(book.getTakenAt());
    }

    @Test
    void searchByTitle_delegatesToRepository() {
        List<Book> list = List.of(new Book("T", "A", 2000));
        when(bookRepository.findByTitleStartingWith("ab")).thenReturn(list);
        assertEquals(list, bookService.searchByTitle("ab"));
    }

    @Test
    void findWithPagination_appliesSortWhenRequested() {
        Book b = new Book("T", "A", 2000);
        Page<Book> page = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(1, 5, Sort.by("year_published")))).thenReturn(page);
        assertEquals(List.of(b), bookService.findWithPagination(1, 5, true));
    }

    @Test
    void findWithPagination_withoutSort() {
        Book b = new Book("T", "A", 2000);
        Page<Book> page = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        assertEquals(List.of(b), bookService.findWithPagination(0, 10, false));
    }
}
