package com.library.application.service;

import com.library.application.domain.BookLoanStatus;
import com.library.application.payload.dto.BookLoanDTO;
import com.library.application.payload.request.BookLoanSearchRequest;
import com.library.application.payload.request.CheckInRequest;
import com.library.application.payload.request.CheckoutRequest;
import com.library.application.payload.request.RenewalRequest;
import com.library.application.payload.response.PageResponse;

public interface BookLoanService {

    BookLoanDTO checkoutBookLoan(CheckoutRequest request);

    BookLoanDTO checkoutBookForUser(Long userId, CheckoutRequest request);

    BookLoanDTO checkInBookLoan(CheckInRequest request);


    BookLoanDTO renewalBookLoan(RenewalRequest request);

    PageResponse<BookLoanDTO> getBookLoans(BookLoanSearchRequest request);

    PageResponse<BookLoanDTO> getMyBookLoans(BookLoanStatus status,
                                             Integer page, Integer size);

    PageResponse<BookLoanDTO> getUserBookLoans(Long userId,
                                             BookLoanStatus status,
                                             Integer page, Integer size);

    Long updateOverdueBookLoans();
}
