package com.library.application.controller;

import com.library.application.payload.dto.WishlistDTO;
import com.library.application.payload.response.PageResponse;
import com.library.application.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService  wishlistService;

    @PostMapping("/add/{bookId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<?> addWishlist(@PathVariable Long bookId, @RequestParam(required = false) String notes){
        return new ResponseEntity<>(wishlistService.addToWishlist(bookId, notes), HttpStatus.CREATED);
    }


    @DeleteMapping("/remove-wishlist/{bookId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('USER')")
    public ResponseEntity<?> deleteWishlist(@PathVariable Long bookId){
        wishlistService.removeFromWishlist(bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update-notes/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateWishlistNotes(@PathVariable Long bookId, @RequestParam String notes){
        return ResponseEntity.ok(wishlistService.updateWishlist(bookId, notes));
    }


    @GetMapping("/my-wishlist")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PageResponse<WishlistDTO>> getMyWishlist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<WishlistDTO> wishlist = wishlistService.getMyWishlist(page, size);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponse<WishlistDTO>> getUserWishlist(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<WishlistDTO> wishlist = wishlistService.getUserWishlist(userId, page, size);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/check/{bookId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<IsInWishlistResponse> checkIfInWishlist(@PathVariable Long bookId){
        boolean isInWishlist = wishlistService.isBookInWishlist(bookId);
        return ResponseEntity.ok(new IsInWishlistResponse(isInWishlist));
    }

    @GetMapping("/my-count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CountResponse> getMyWishlistCount(){
        Long count = wishlistService.getMyWishlistCount();
        return ResponseEntity.ok(new CountResponse(count));
    }

    @GetMapping("/book/{bookId}/count")
    public ResponseEntity<CountResponse> getBookWishlistCount(@PathVariable Long bookId) {
        Long count = wishlistService.getBookWishlistCount(bookId);
        return ResponseEntity.ok(new CountResponse(count));
    }
    public static class IsInWishlistResponse {
        public boolean isInWishlist;

        public IsInWishlistResponse(boolean isInWishlist) {
            this.isInWishlist = isInWishlist;
        }
    }

    /**
     * Response DTO for count endpoints
     */
    public static class CountResponse {
        public Long count;

        public CountResponse(Long count) {
            this.count = count;
        }
    }
}
