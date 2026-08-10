package com.library.application.controller;

import com.library.application.domain.BookLoanStatus;
import com.library.application.payload.dto.BookLoanDTO;
import com.library.application.payload.request.*;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.BookLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book-loans")
@RequiredArgsConstructor
public class BookLoanController {
    private final BookLoanService bookLoanService;

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkoutBookLoan(@Valid @RequestBody CheckoutRequest checkoutRequest) {
        return new ResponseEntity<>(bookLoanService.checkoutBookLoan(checkoutRequest), HttpStatus.ACCEPTED);
    }

    @PostMapping("/checkout/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkoutBookForUser(
            @PathVariable Long userId,
            @Valid @RequestBody CheckoutRequest checkoutRequest) {
            return new ResponseEntity<>(bookLoanService.checkoutBookForUser(userId, checkoutRequest), HttpStatus.CREATED);
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkInBook(@Valid @RequestBody CheckInRequest checkInRequest) {
        return new ResponseEntity<>(bookLoanService.checkInBookLoan(checkInRequest), HttpStatus.OK);
    }

    @PostMapping("/renewal")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkInBook(@Valid @RequestBody RenewalRequest renewalRequest) {
        return new ResponseEntity<>(bookLoanService.renewalBookLoan(renewalRequest), HttpStatus.ACCEPTED);
    }

    @PutMapping("/update-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateOverdueBookLoan(){
        return new ResponseEntity<>(bookLoanService.updateOverdueBookLoans(), HttpStatus.OK);
    }


    @GetMapping("/mine")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> myBookLoans( @RequestParam(required = false) BookLoanStatus status,
                                          @RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size){
        return ResponseEntity.ok(bookLoanService.getMyBookLoans(status, page, size));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookLoans(@RequestBody BookLoanSearchRequest searchRequest) {
        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getBookLoans(searchRequest);
        return ResponseEntity.ok(bookLoans);
    }


    @GetMapping("/{bookLoanId}")
    public ResponseEntity<?> getBookLoanById(@PathVariable Long bookLoanId){
        return new ResponseEntity<>(bookLoanService.getBookLoanById(bookLoanId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserBookLoans(
            @PathVariable Long userId,
            @RequestParam(required = false) BookLoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookLoanDTO> bookLoans = bookLoanService.getUserBookLoans(userId, status, page, size);
        return ResponseEntity.ok(bookLoans);
    }

    @PutMapping("/update-bookloan/{bookLoanId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookLoan(@PathVariable Long bookLoanId, @Valid @RequestBody UpdateBookLoanRequest updateBookLoanRequest) {
        return new ResponseEntity<>(bookLoanService.updateBookLoan(bookLoanId, updateBookLoanRequest), HttpStatus.OK);
    }


    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookLoanStatistics() {
        return ResponseEntity.ok(bookLoanService.getCheckoutStatistics());
    }



}

