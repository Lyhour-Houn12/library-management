package com.library.application.config;

import com.library.application.service.BookLoanService;
import com.library.application.service.ReservationService;
import com.library.application.service.impl.BookLoanServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    private final ReservationService reservationService;
    private final BookLoanServiceImpl bookLoanServiceImpl;

    public ServiceConfig(ReservationService reservationService, BookLoanServiceImpl bookLoanServiceImpl) {
        this.reservationService = reservationService;
        this.bookLoanServiceImpl = bookLoanServiceImpl;
    }

    @PostConstruct
    public void init() {
        // Set ReservationService in BookLoanService after construction
        bookLoanServiceImpl.setReservationService(reservationService);
    }
}
