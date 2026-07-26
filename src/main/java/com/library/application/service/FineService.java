package com.library.application.service;

import com.library.application.domain.FineStatus;
import com.library.application.payload.dto.FineDTO;
import com.library.application.payload.request.CreateFineRequest;
import com.library.application.payload.response.PaymentInitiateResponse;

public interface FineService {

    FineDTO createFine(CreateFineRequest request);

    PaymentInitiateResponse payFineFully(Long fineId, String transactionId);


    void markAsPaid(Long fineId, Double amount, String transactionId);
}
