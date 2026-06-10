package com.library.application.mapper;

import com.library.application.entity.Book;
import com.library.application.entity.Genre;
import com.library.application.exception.BookException;
import com.library.application.payload.dto.BookDTO;
import com.library.application.repository.BookRepository;
import com.library.application.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookMapper {
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    public Book toEntity(BookDTO bookDTO) {
        if(bookDTO == null) return null;

        Book book = new Book();
        book.setIsbn(bookDTO.getIsbn());
        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());

        // Map genre information
        if(bookDTO.getGenreId() != null){
            Genre  genre = genreRepository.findById(bookDTO.getGenreId())
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
            book.setGenre(genre);
        }
        book.setPublisher(bookDTO.getPublisher());
        book.setPublicationDate(bookDTO.getPublicationDate());
        book.setLanguage(bookDTO.getLanguage());
        book.setDescription(bookDTO.getDescription());
        book.setTotalCopies(bookDTO.getTotalCopies());
        book.setAvailableCopies(bookDTO.getAvailableCopies());
        book.setPrice(bookDTO.getPrice());
        book.setCoverImage(bookDTO.getCoverImageUrl());
        book.setActive(true);
        return book;
    }

    public BookDTO toDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setIsbn(book.getIsbn());
        bookDTO.setAuthor(book.getAuthor());
        bookDTO.setTitle(book.getTitle());

        // Map genre information
        if(book.getGenre() != null){
            bookDTO.setGenreId(book.getGenre().getId());
            bookDTO.setGenreName(book.getGenre().getName());
            bookDTO.setGenreCode(book.getGenre().getCode());
        }
        bookDTO.setPublisher(book.getPublisher());
        bookDTO.setPublicationDate(book.getPublicationDate());
        bookDTO.setLanguage(book.getLanguage());
        bookDTO.setDescription(book.getDescription());
        bookDTO.setTotalCopies(book.getTotalCopies());
        bookDTO.setAvailableCopies(book.getAvailableCopies());
        bookDTO.setPrice(book.getPrice());
        bookDTO.setCoverImageUrl(book.getCoverImage());
        bookDTO.setActive(book.getActive());

        bookDTO.setCreatedAt(book.getCreatedAt());
        bookDTO.setUpdatedAt(book.getUpdatedAt());

        return bookDTO;
    }

    public void updateEntityFromDTO(BookDTO bookDTO, Book book){
        if(bookDTO == null || book == null) return;
         // ISBN could not be updated

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());

        // Update genre if provided
        if (bookDTO.getGenreId() != null) {
            Genre genre = genreRepository.findById(bookDTO.getGenreId())
                    .orElseThrow(() -> new BookException("Genre with ID " + bookDTO.getGenreId() + " not found"));
            book.setGenre(genre);
        }
        book.setPublisher(bookDTO.getPublisher());
        book.setPublicationDate(bookDTO.getPublicationDate());
        book.setLanguage(bookDTO.getLanguage());
        book.setDescription(bookDTO.getDescription());
        book.setTotalCopies(bookDTO.getTotalCopies());
        book.setAvailableCopies(bookDTO.getAvailableCopies());
        book.setPrice(bookDTO.getPrice());
        book.setCoverImage(bookDTO.getCoverImageUrl());

        if(bookDTO.getActive() != null){
            book.setActive(bookDTO.getActive());
        }

    }
}
