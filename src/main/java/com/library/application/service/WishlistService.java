package com.library.application.service;

import com.library.application.payload.dto.WishlistDTO;
import com.library.application.payload.response.PageResponse;

public interface WishlistService {


    WishlistDTO addToWishlist(Long bookId, String notes);


    void removeFromWishlist(Long bookId);


    PageResponse<WishlistDTO> getMyWishlist(Integer page, Integer size);

    /**
     * Get all wishlist items for a specific user (admin or public view)
     */
    PageResponse<WishlistDTO> getUserWishlist(Long userId, int page, int size);

    boolean isBookInWishlist(Long bookId);

    WishlistDTO updateWishlist(Long bookId, String notes);

    Long getMyWishlistCount();

    /**
     * Get count of how many users have wishlisted a specific book
     */
    Long getBookWishlistCount(Long bookId);
}
