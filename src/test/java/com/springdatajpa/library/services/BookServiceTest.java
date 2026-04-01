package com.springdatajpa.library.services;

import com.springdatajpa.library.dto.BookForm;
import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Genre;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.repositories.BookRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
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
        bookService.findAll(true, false, false, false, false, false);
        verify(bookRepository).findAll(Sort.by("yearPublished"));
    }

    @Test
    void findAll_sortsByGenreWhenRequested() {
        bookService.findAll(false, true, false, false, false, false);
        verify(bookRepository).findAll(Sort.by("genre"));
    }

    @Test
    void findAll_sortsByTitleWhenRequested() {
        bookService.findAll(false, false, true, false, false, false);
        verify(bookRepository).findAll(Sort.by("title"));
    }

    @Test
    void findAll_sortsByAuthorWhenRequested() {
        bookService.findAll(false, false, false, true, false, false);
        verify(bookRepository).findAll(Sort.by("author"));
    }

    @Test
    void findAll_titleThenAuthorWhenBothRequested() {
        bookService.findAll(false, false, true, true, false, false);
        verify(bookRepository).findAll(Sort.by("title").and(Sort.by("author")));
    }

    @Test
    void findAll_filterFreeOnlyWithYearSort() {
        bookService.findAll(true, false, false, false, true, false);
        verify(bookRepository).findByOwnerIsNull(Sort.by("yearPublished"));
        verify(bookRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void findAll_filterFreeOnlyUnsorted() {
        bookService.findAll(false, false, false, false, true, false);
        verify(bookRepository).findByOwnerIsNull(Sort.unsorted());
    }

    @Test
    void findAll_filterIssuedOnlyPrependsTakenAt() {
        Sort expected = Sort.by("takenAt").and(Sort.by("genre"));
        bookService.findAll(false, true, false, false, true, true);
        verify(bookRepository).findByOwnerIsNotNull(expected);
    }

    @Test
    void findAll_withoutSortUsesFindAll() {
        bookService.findAll(false, false, false, false, false, false);
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
    void createBook_savesMappedEntity() {
        BookForm form = new BookForm();
        form.setTitle("T");
        form.setAuthor("A");
        form.setYearPublished(2000);
        form.setGenre(Genre.POETRY);
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        bookService.createBook(form);
        verify(bookRepository).save(captor.capture());
        assertEquals("T", captor.getValue().getTitle());
        assertEquals("A", captor.getValue().getAuthor());
        assertEquals(2000, captor.getValue().getYearPublished());
        assertEquals(Genre.POETRY, captor.getValue().getGenre());
    }

    @Test
    void update_patchesManagedEntityWithoutSave() {
        Person owner = new Person();
        owner.setPersonId(5);
        Book existing = new Book("Old", "Auth", 1999);
        existing.setOwner(owner);
        when(bookRepository.findById(3)).thenReturn(Optional.of(existing));

        BookForm updated = new BookForm();
        updated.setTitle("New");
        updated.setAuthor("Auth2");
        updated.setYearPublished(2001);
        updated.setGenre(Genre.HISTORY);
        bookService.update(3, updated);

        assertEquals("New", existing.getTitle());
        assertEquals("Auth2", existing.getAuthor());
        assertEquals(2001, existing.getYearPublished());
        assertEquals(Genre.HISTORY, existing.getGenre());
        assertSame(owner, existing.getOwner());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_throwsWhenBookMissing() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookService.update(1, new BookForm()));
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
    void searchBooks_delegatesToRepository() {
        List<Book> list = List.of(new Book("T", "A", 2000));
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("ab", "ab")).thenReturn(list);
        assertEquals(list, bookService.searchBooks("ab"));
    }

    @Test
    void searchBooks_blankDoesNotHitRepository() {
        assertTrue(bookService.searchBooks("").isEmpty());
        assertTrue(bookService.searchBooks("   ").isEmpty());
        verify(bookRepository, never()).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void searchBooks_trimsAndClampsLength() {
        bookService.searchBooks("  x  ");
        verify(bookRepository).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("x", "x");
        String longQ = "a".repeat(250);
        bookService.searchBooks(longQ);
        verify(bookRepository).findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("a".repeat(200), "a".repeat(200));
    }

    @Test
    void findForIndexPage_noParamsUsesFindAll() {
        when(bookRepository.findAll()).thenReturn(List.of());
        Page<Book> page = bookService.findForIndexPage(null, null, false, false, false, false, false, false);
        verify(bookRepository).findAll();
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void findForIndexPage_noParams_sortByGenre() {
        when(bookRepository.findAll(Sort.by("genre"))).thenReturn(List.of());
        bookService.findForIndexPage(null, null, false, true, false, false, false, false);
        verify(bookRepository).findAll(Sort.by("genre"));
    }

    @Test
    void findForIndexPage_onlyPageFillsDefaultPageSize() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(repoPage);
        Page<Book> result = bookService.findForIndexPage(1, null, false, false, false, false, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findForIndexPage_clampsOversizedPageSize() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 100))).thenReturn(repoPage);
        Page<Book> result = bookService.findForIndexPage(1, 500, false, false, false, false, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_appliesSortWhenRequested() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(1, 5, Sort.by("yearPublished")))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(1, 5, true, false, false, false, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_sortByGenre() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10, Sort.by("genre")))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false, true, false, false, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_sortByTitleAndAuthor() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        Sort sort = Sort.by("title").and(Sort.by("author"));
        when(bookRepository.findAll(PageRequest.of(0, 10, sort))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false, false, true, true, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_withoutSort() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false, false, false, false, false, false);
        assertEquals(List.of(b), result.getContent());
    }

    @Test
    void findWithPagination_filterFreeOnly() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findByOwnerIsNull(PageRequest.of(0, 10))).thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false, false, false, false, true, false);
        assertEquals(List.of(b), result.getContent());
        verify(bookRepository).findByOwnerIsNull(PageRequest.of(0, 10));
    }

    @Test
    void findWithPagination_filterIssuedOnly_sortByTakenAt() {
        Book b = new Book("T", "A", 2000);
        Page<Book> repoPage = new PageImpl<>(List.of(b));
        when(bookRepository.findByOwnerIsNotNull(PageRequest.of(0, 10, Sort.by("takenAt"))))
                .thenReturn(repoPage);
        Page<Book> result = bookService.findWithPagination(0, 10, false, false, false, false, true, true);
        assertEquals(List.of(b), result.getContent());
    }
}
