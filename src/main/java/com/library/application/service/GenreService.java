package com.library.application.service;

import com.library.application.exception.GenreException;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.payload.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreService {
    GenreDTO createGenre(GenreDTO genreDTO);

    List<GenreDTO> createGenresBulk(List<GenreDTO> genreDTOs) ;

    GenreDTO getGenreById(Long genreId) ;

    GenreDTO getGenreByCode(String code);

    GenreDTO updateGenre(Long genreId, GenreDTO genreDTO);

    void deleteGenre(Long genreId) ;

    void hardDeleteGenre(Long genreId) ;

    List<GenreDTO> getAllActiveGenres();

    List<GenreDTO> getAllActiveGenresWithSubGenres();

    List<GenreDTO> getTopLevelGenres();

    List<GenreDTO> getSubGenresByParentId(Long parentGenreId) ;

    PageResponse<GenreDTO> searchGenres(String searchTerm, Pageable pageable);

    long getTotalActiveGenres();

    long getBookCountByGenre(Long genreId);

    boolean isGenreInUse(Long genreId) ;

}

