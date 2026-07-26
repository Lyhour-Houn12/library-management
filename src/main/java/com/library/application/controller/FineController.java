package com.library.application.controller;

import com.library.application.payload.request.CreateFineRequest;
import com.library.application.repository.FineRepository;
import com.library.application.service.FineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;


    @PostMapping("/create-fine")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createFine(@Valid @RequestBody CreateFineRequest request){
        return new ResponseEntity<>(fineService.createFine(request), HttpStatus.CREATED);
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<?> payFineFully(@PathVariable Long fineId, @RequestParam(required = false) String transactionId){
        return ResponseEntity.ok(fineService.payFineFully(fineId, transactionId));
    }



}
