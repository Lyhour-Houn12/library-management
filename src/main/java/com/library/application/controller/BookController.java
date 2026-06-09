package com.library.application.controller;


import com.library.application.payload.dto.BookDTO;
import com.library.application.payload.request.BookSearchRequest;
import com.library.application.payload.response.ApiResponse;
import com.library.application.payload.response.BookStatResponse;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody BookDTO bookDTO){
        try{
            return ResponseEntity.ok(bookService.createBook(bookDTO));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiResponse("Error creating book", false));
        }
    }

    @PostMapping("/bulks")
    public ResponseEntity<?> createBookBulk(@Valid @RequestBody List<BookDTO> bookDTOs){
        try{
            return ResponseEntity.ok(bookService.createBooksBulk(bookDTOs));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiResponse("Error creating book", false));
        }
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<?> getBookByIsbn(@PathVariable String isbn){
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }
    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable Long bookId, @Valid @RequestBody BookDTO bookDTO){
        try{
            return ResponseEntity.ok(bookService.updateBook(bookId, bookDTO));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiResponse("Error updating book", false));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookDTO>> searchBooks(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long genreId,
            @RequestParam(defaultValue = "true") Boolean availableOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ){
        BookSearchRequest bookSearchRequest = new BookSearchRequest();
        bookSearchRequest.setSearchTerm(searchTerm);
        bookSearchRequest.setGenreId(genreId);
        bookSearchRequest.setAvailableOnly(availableOnly);
        bookSearchRequest.setPage(page);
        bookSearchRequest.setPageSize(pageSize);
        bookSearchRequest.setSortBy(sortBy);
        bookSearchRequest.setSortDirection(sortDirection);

        PageResponse<BookDTO> book = bookService.searchBookWithFilter(bookSearchRequest);

        return ResponseEntity.ok(book);
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<BookDTO>> advancedSearch(@RequestBody BookSearchRequest bookSearchRequest){
        PageResponse<BookDTO> books =bookService.searchBookWithFilter(bookSearchRequest);
        return ResponseEntity.ok(books);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId){
        bookService.deleteBookById(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{bookId}/permanent")
    public ResponseEntity<?> hardDeleteBook(@PathVariable Long bookId){
        bookService.hardDeleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/stat")
    public ResponseEntity<BookStatResponse> bookStats(){
        BookStatResponse bookStatResponse = new BookStatResponse(bookService.getTotalActiveBooks(),  bookService.getTotalAvailableBooks(), bookService.getTotalUnAvailableBooks(),  bookService.getTotalInActiveBooks());
        return ResponseEntity.ok(bookStatResponse);
    }




}
