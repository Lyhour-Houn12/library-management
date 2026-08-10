package com.library.application.service;


import com.library.application.payload.dto.BookDTO;
import com.library.application.payload.request.BookSearchRequest;
import com.library.application.payload.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BookService {

    BookDTO createBook(BookDTO bookDTO);

    BookDTO getBookByIsbn(String isbn);

    List<BookDTO> createBooksBulk(List<BookDTO> bookDTOList);

    BookDTO getBookById(Long bookId);

    PageResponse<BookDTO> searchBookWithFilter(BookSearchRequest bookSearchRequest);

    BookDTO updateBook(Long bookId, BookDTO bookDTO);

    void deleteBookById(Long bookId);

    void hardDeleteBook(Long bookId);

    Long getTotalActiveBooks();

    Long getTotalAvailableBooks();

    Long  getTotalInActiveBooks();

    Long getTotalUnAvailableBooks();
}
