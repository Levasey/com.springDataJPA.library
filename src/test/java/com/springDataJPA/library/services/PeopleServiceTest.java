package com.springDataJPA.library.services;

import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.repositories.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void findAll_delegatesToRepository() {
        List<Person> list = List.of(new Person());
        when(peopleRepository.findAll()).thenReturn(list);
        assertSame(list, peopleService.findAll());
    }

    @Test
    void findById_returnsPersonOrNull() {
        Person p = new Person();
        when(peopleRepository.findById(1)).thenReturn(Optional.of(p));
        assertSame(p, peopleService.findById(1));
        when(peopleRepository.findById(2)).thenReturn(Optional.empty());
        assertNull(peopleService.findById(2));
    }

    @Test
    void save_delegatesToRepository() {
        Person p = new Person();
        peopleService.save(p);
        verify(peopleRepository).save(p);
    }

    @Test
    void update_setsIdAndSaves() {
        Person p = new Person();
        peopleService.update(3, p);
        assertEquals(3, p.getPersonId());
        verify(peopleRepository).save(p);
    }

    @Test
    void delete_delegatesToRepository() {
        peopleService.delete(9);
        verify(peopleRepository).deleteById(9);
    }

    @Test
    void getBooksByPersonId_returnsEmptyWhenPersonMissing() {
        when(peopleRepository.findById(1)).thenReturn(Optional.empty());
        assertTrue(peopleService.getBooksByPersonId(1).isEmpty());
    }

    @Test
    void getBooksByPersonId_marksOverdueBooksExpired() {
        Person person = new Person();
        person.setPersonId(1);
        Book overdue = new Book("T", "A", 2000);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -11);
        overdue.setTakenAt(cal.getTime());
        Book fresh = new Book("T2", "A2", 2001);
        fresh.setTakenAt(new Date());
        person.setBooks(new ArrayList<>(List.of(overdue, fresh)));

        when(peopleRepository.findById(1)).thenReturn(Optional.of(person));

        List<Book> books = peopleService.getBooksByPersonId(1);

        assertEquals(2, books.size());
        assertTrue(overdue.isExpired());
        assertFalse(fresh.isExpired());
    }
}
