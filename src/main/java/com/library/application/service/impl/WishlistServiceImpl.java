package com.library.application.service.impl;

import com.library.application.entity.Book;
import com.library.application.entity.User;
import com.library.application.entity.Wishlist;
import com.library.application.exception.BookException;
import com.library.application.exception.WishlistException;
import com.library.application.mapper.WishlistMapper;
import com.library.application.payload.dto.WishlistDTO;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.BookRepository;
import com.library.application.repository.WishlistRepository;
import com.library.application.service.UserService;
import com.library.application.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final WishlistMapper wishlistMapper;
    private final UserService userService;
    private final BookRepository bookRepository;


    @Override
    public WishlistDTO addToWishlist(Long bookId, String notes) {
        User user = userService.getCurrentUser();
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookException("Book not found with id " + bookId));

        if(!book.getActive()){
            throw new BookException("Cannot inactive book to wishlist");
        }
        if(wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)){
           throw new WishlistException("Book is already in your wishlist");
        }
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlist.setAddedAt(LocalDateTime.now());
        wishlist.setNotes(notes);

        Wishlist savedWishlist =  wishlistRepository.save(wishlist);
        return wishlistMapper.toDto(savedWishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long bookId) {
        User user =  userService.getCurrentUser();

        if(!wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)){
            throw new WishlistException("Book is not in your current wishlist");
        }

        wishlistRepository.deleteByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    public PageResponse<WishlistDTO> getMyWishlist(Integer page, Integer size) {
        User user = userService.getCurrentUser();
        return getUserWishlist(user.getId(), page, size);
    }

    @Override
    public PageResponse<WishlistDTO> getUserWishlist(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("addedAt").descending());
        Page<Wishlist> wishlists = wishlistRepository.findByUserId(userId, pageable);
        return PageResponse.from(
                wishlists.map(wishlistMapper::toDto)
        );
    }

    @Override
    public boolean isBookInWishlist(Long bookId) {
        User user = userService.getCurrentUser();
        return wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    public WishlistDTO updateWishlist(Long bookId, String notes) {
        User user = userService.getCurrentUser();

        // 1. Find wishlist items
        Wishlist wishlist = wishlistRepository.findByUserIdAndBookId(user.getId(), bookId);

        wishlist.setNotes(notes);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return wishlistMapper.toDto(savedWishlist);
    }

    @Override
    public Long getMyWishlistCount() {
        User user = userService.getCurrentUser();
        return wishlistRepository.countWishlistByUserId(user.getId());
    }

    @Override
    public Long getBookWishlistCount(Long bookId) {
        return wishlistRepository.countByBookId(bookId);
    }
}
