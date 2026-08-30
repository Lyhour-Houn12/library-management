package com.library.application.controller;

import com.library.application.payload.dto.BookDTO;
import com.library.application.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class BookAdminController {
    private final BookService bookService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBook(@Valid @RequestPart("book") BookDTO bookDTO,
                                        @RequestPart("coverImage") MultipartFile coverImage) {
        return ResponseEntity.ok(bookService.createBook(bookDTO, coverImage));
    }
}
