package com.library.application.mapper;

import com.library.application.entity.Wishlist;
import com.library.application.payload.dto.WishlistDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WishlistMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    WishlistDTO toDto(Wishlist wishlist);


    Wishlist toEntity(WishlistDTO wishlistDTO);
}
