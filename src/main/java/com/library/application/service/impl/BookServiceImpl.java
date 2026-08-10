package com.library.application.service.impl;

import com.library.application.domain.BookLoanStatus;
import com.library.application.entity.Book;
import com.library.application.entity.User;
import com.library.application.exception.BookException;
import com.library.application.mapper.BookMapper;
import com.library.application.payload.dto.BookDTO;
import com.library.application.payload.request.BookSearchRequest;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.BookLoanRepository;
import com.library.application.repository.BookRepository;
import com.library.application.repository.ReservationRepository;
import com.library.application.service.BookService;
import com.library.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final BookLoanRepository bookLoanRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public BookDTO createBook(BookDTO bookDTO) {
        if(bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            throw new BookException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }

        Book book = bookMapper.toEntity(bookDTO);
        if(!book.isAvailableCopiesValid()){
            throw new BookException("Available copies can not exceed total copies");
        }

        Book bookSaved = bookRepository.save(book);

        return bookMapper.toDTO(bookSaved);
    }

    @Override
    public BookDTO getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookException("Book not found with ISBN: " + isbn));
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO updateBook(Long bookId, BookDTO bookDTO) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(String.format("Book with id = %s not found", bookId)));

        if(bookDTO.getTotalCopies() < bookDTO.getAvailableCopies()){
            throw new BookException("Total copies can not exceed total copies");
        }

        // Check if trying to update ISBN to a value that already exists for another book
        if(!existingBook.getIsbn().equals(bookDTO.getIsbn())){
            throw new BookException("ISBN can not be changed after creation");
        }

        bookMapper.updateEntityFromDTO(bookDTO, existingBook);

        Book updateBook = bookRepository.save(existingBook);

        return bookMapper.toDTO(updateBook);
    }

    @Override
    public void deleteBookById(Long bookId) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(String.format("Book with id = %s not found", bookId)));

        existingBook.setActive(false);
        bookRepository.save(existingBook);
    }

    @Override
    public void hardDeleteBook(Long bookId) {
        Book existingBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(String.format("Book with id = %s not found", bookId)));
        bookRepository.delete(existingBook);
    }

    @Override
    public List<BookDTO> createBooksBulk(List<BookDTO> bookDTOList) {
        if(bookDTOList.isEmpty()){
            throw new  BookException("BookDTOList can not be empty");
        }
        for(BookDTO bookDTO : bookDTOList){
            long duplicateIsbn = bookDTOList.stream()
                    .filter(b -> b.getIsbn().equals(bookDTO.getIsbn()))
                    .count();
            if(duplicateIsbn > 1){
                throw new BookException("Duplicate ISBN");
            }
            if(bookRepository.existsByIsbn(bookDTO.getIsbn())){
                throw new  BookException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
            }
            if(bookDTO.getTotalCopies() < bookDTO.getAvailableCopies()){
                throw new BookException("Total copies can not exceed total copies");
            }
            if(bookDTO.getGenreId() == null){
                throw new   BookException("Genre id is required for ISBN: " + bookDTO.getIsbn());
            }
        }
        List<Book> bookToSave = new ArrayList<>();
        for(BookDTO bookDTO : bookDTOList){
            Book book = bookMapper.toEntity(bookDTO);
            book.setActive(true);
            bookToSave.add(book);
        }

        List<Book> savedBook = bookRepository.saveAll(bookToSave);

        return savedBook.stream()
                .map(bookMapper::toDTO)
                .toList();
    }

    @Override
    public BookDTO getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookException(String.format("Book with id = %s not found", bookId)));
        BookDTO bookDTO = bookMapper.toDTO(book);

        User currentUser = userService.getCurrentUser();
        boolean alreadyHasLoan = bookLoanRepository.existsByUserIdAndBookIdAndStatus(currentUser.getId(), bookId, BookLoanStatus.CHECKOUT);
        boolean alreadyHaveReservation = reservationRepository.findActiveReservationByUserAndBook(currentUser.getId(), bookId).isPresent();

        bookDTO.setAlreadyHaveLoan(alreadyHasLoan);
        bookDTO.setAlreadyHaveReservation(alreadyHaveReservation);

        return bookDTO;
    }

    @Override
    public PageResponse<BookDTO> searchBookWithFilter(BookSearchRequest bookSearchRequest) {
        Pageable pageable = createPageable(bookSearchRequest.getPage(),
                bookSearchRequest.getPageSize(),
                bookSearchRequest.getSortBy(),
                bookSearchRequest.getSortDirection());
        Page<Book> bookPage = bookRepository.searchBookWithFilter(
                bookSearchRequest.getSearchTerm() == null ||
                        bookSearchRequest.getSearchTerm().trim().isEmpty()
                        ? null
                        : bookSearchRequest.getSearchTerm().trim(),
                bookSearchRequest.getGenreId(),
                bookSearchRequest.getAvailableOnly() != null ?  bookSearchRequest.getAvailableOnly() : false,
                pageable
        );
        return convertToPageResponse(bookPage);
    }

    @Override
    public Long getTotalActiveBooks() {
        return bookRepository.countByActiveTrue();
    }

    @Override
    public Long getTotalAvailableBooks() {
        return bookRepository.countAvailableBooks();
    }

    @Override
    public Long getTotalInActiveBooks() {
        return bookRepository.countByActiveFalse();
    }

    @Override
    public Long getTotalUnAvailableBooks() {
        return bookRepository.countUnAvailableBooks();
    }


    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        size = Math.min(size, 10);
        size = Math.max(size, 1);
        Sort sort = sortDirection.equalsIgnoreCase("ASC")? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<BookDTO> convertToPageResponse(Page<Book> book){
        List<BookDTO> bookDTOS = book.getContent()
                .stream()
                .map(bookMapper::toDTO)
                .toList();
        return new PageResponse<>(
                bookDTOS,
                book.getNumber(),
                book.getSize(),
                book.getTotalElements(),
                book.getTotalPages(),
                book.isLast(),
                book.isFirst(),
                book.isEmpty()
        );
    }
}

