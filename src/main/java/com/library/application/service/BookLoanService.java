package com.library.application.service;

import com.library.application.domain.BookLoanStatus;
import com.library.application.payload.CheckoutStatistics;
import com.library.application.payload.dto.BookLoanDTO;
import com.library.application.payload.request.*;
import com.library.application.payload.response.PageResponse;

public interface BookLoanService {

    BookLoanDTO checkoutBookLoan(CheckoutRequest request);

    BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest request);

    BookLoanDTO checkInBookLoan(CheckInRequest request);


    BookLoanDTO renewalBookLoan(RenewalRequest request);

    BookLoanDTO getBookLoanById(Long bookLoanId);

    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request);

    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status,
                                             Integer page, Integer size);

    PageResponse<BookLoanDTO> getUserBookLoans(Long userId,
                                             BookLoanStatus status,
                                             Integer page, Integer size);

    Long updateOverdueBookLoans();


    BookLoanDTO updateBookLoan(Long bookLoanId, UpdateBookLoanRequest updateRequest);

    CheckoutStatistics getCheckoutStatistics();
}
