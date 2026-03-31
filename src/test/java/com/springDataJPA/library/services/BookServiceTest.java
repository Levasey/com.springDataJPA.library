package com.springDataJPA.library.services;

import com.springDataJPA.library.exception.BadRequestException;
import com.springDataJPA.library.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
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
        verify(bookRepository).findAll(Sort.by("yearPublished"));
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
        when(bookRepository.findWithOwnerById(1)).thenReturn(Optional.of(book));
        assertSame(book, bookService.findOne(1));
    }

    @Test
    void findOne_throwsWhenMissing() {
        when(bookRepository.findWithOwnerById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.findOne(99));
    }

    @Test
    void saveBook_delegatesToRepository() {
        Book book = new Book("T", "A", 2000);
        when(bookRepository.save(book)).thenReturn(book);
        assertSame(book, bookService.saveBook(book));
        verify(bookRepository).save(book);
    }

    @Test
    void update_patchesManagedEntityWithoutSave() {
        Person owner = new Person();
        owner.setPersonId(5);
        Book existing = new Book("Old", "Auth", 1999);
        existing.setOwner(owner);
        when(bookRepository.findById(3)).thenReturn(Optional.of(existing));

        Book updated = new Book("New", "Auth2", 2001);
        bookService.update(3, updated);

        assertEquals("New", existing.getTitle());
        assertEquals("Auth2", existing.getAuthor());
        assertEquals(2001, existing.getYearPublished());
        assertSame(owner, existing.getOwner());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_throwsWhenBookMissing() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookService.update(1, new Book("X", "Y", 2000)));
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_delegatesToRepository() {
        when(bookRepository.existsById(7)).thenReturn(true);
        bookService.delete(7);
        verify(bookRepository).deleteById(7);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(bookRepository.existsById(7)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bookService.delete(7));
        verify(bookRepository, never()).deleteById(anyInt());
    }

    @Test
    void release_clearsOwnerAndTakenAt() {
        Book book = new Book("T", "A", 2000);
        book.setOwner(new Person());
        book.setTakenAt(LocalDateTime.now());
        when(bookRepository.findById(4)).thenReturn(Optional.of(book));

        bookService.release(4);

        assertNull(book.getOwner());
        assertNull(book.getTakenAt());
    }

    @Test
    void release_throwsWhenBookMissing() {
        when(bookRepository.findById(4)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.release(4));
    }

    @Test
    void assign_throwsWhenPersonIdNotPositive() {
        assertThrows(BadRequestException.class, () -> bookService.assign(1, 0));
        verify(peopleRepository, never()).findById(anyInt());
    }

    @Test
    void assign_throwsWhenPersonMissing() {
        when(peopleRepository.findById(10)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.assign(8, 10));
        verify(bookRepository, never()).findById(anyInt());
    }

    @Test
    void assign_throwsWhenBookMissing() {
        when(peopleRepository.findById(10)).thenReturn(Optional.of(new Person()));
        when(bookRepository.findById(8)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> bookService.assign(8, 10));
    }

    @Test
    void assign_setsOwnerAndTakenAtWhenBothExist() {
        Person person = new Person();
        person.setPersonId(10);
        Book book = new Book("T", "A", 2000);
        when(peopleRepository.findById(10)).thenReturn(Optional.of(person));
        when(bookRepository.findById(8)).thenReturn(Optional.of(book));

        bookService.assign(8, 10);

        assertSame(person, book.getOwner());
        assertNotNull(book.getTakenAt());
    }

    @Test
    void searchByTitle_delegatesToRepository() {
        List<Book> list = List.of(new Book("T", "A", 2000));
        when(bookRepository.findByTitleContainingIgnoreCase("ab")).thenReturn(list);
        assertEquals(list, bookService.searchByTitle("ab"));
    }

    @Test
    void searchByTitle_blankDoesNotHitRepository() {
        assertTrue(bookService.searchByTitle("").isEmpty());
        assertTrue(bookService.searchByTitle("   ").isEmpty());
        verify(bookRepository, never()).findByTitleContainingIgnoreCase(anyString());
    }

    @Test
    void findForIndexPage_noParamsUsesFindAll() {
        when(bookRepository.findAll()).thenReturn(List.of());
        Page<Book> page = bookService.findForIndexPage(null, null, false);
        verify(bookRepository).findAll();
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findForIndexPage_onlyPageFillsDefaultPageSize() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(repoPage);
        Page<Book> result = bookService.findForIndexPage(1, null, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findForIndexPage_clampsOversizedPageSize() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 100))).thenReturn(repoPage);
        Page<Book> result = bookService.findForIndexPage(1, 500, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_appliesSortWhenRequested() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(1, 5, Sort.by("yearPublished")))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(1, 5, true);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_withoutSort() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false);
        assertEquals(List.of(b), result.getContent());
    }
}
