package com.library.application.mapper;


import com.library.application.entity.Genre;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreMapper {
    private final GenreRepository genreRepository;

    public GenreDTO toDTO(Genre genre) {
        return toDTO(genre, false);
    }

    public void updateEntityFromDto(GenreDTO dto, Genre genre) {
        if(dto == null || genre == null) {
            return;
        }

        genre.setCode(dto.getCode());
        genre.setName(dto.getName());
        genre.setDescription(dto.getDescription());
        genre.setDisplayOrder(dto.getDisplayOrder() == null ? 0 : dto.getDisplayOrder());
        if(dto.getActive() != null) {
            genre.setActive(dto.getActive());
        }
        if(dto.getParentGenreId() != null){
            genreRepository.findById(dto.getParentGenreId())
                    .ifPresent(genre::setParentGenre);
        }
    }

    public GenreDTO toDTO(Genre genre, boolean includeSubGenres) {
        if (genre == null) {
            return null;
        }
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setCode(genre.getCode());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());
        dto.setDisplayOrder(genre.getDisplayOrder());
        dto.setActive(genre.getActive());
        dto.setCreatedAt(genre.getCreatedAt());
        dto.setUpdatedAt(genre.getUpdatedAt());

        if (genre.getParentGenre() != null) {
            dto.setParentGenreId(genre.getParentGenre().getId());
            dto.setParentGenreName(genre.getParentGenre().getName());
        }
        // Include sub-genres if requested (recursively)
        if (includeSubGenres && genre.getSubGenres() != null && !genre.getSubGenres().isEmpty()) {
            dto.setSubGenres(genre.getSubGenres().stream()
                    .filter(Genre::getActive)
                    .map(subGenre -> toDTO(subGenre, true))
                    .collect(Collectors.toList()));
        }


        return dto;
    }

    public Genre toEntity(GenreDTO dto) {
        if(dto == null) {
            return null;
        }
        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setCode(dto.getCode());
        genre.setName(dto.getName());
        genre.setDescription(dto.getDescription());
        genre.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        if(dto.getActive() != null) {
            genre.setActive(dto.getActive());
        }else{
            genre.setActive(true); // Default to active
        }
        // set parent genre if provided

        if(dto.getParentGenreId() != null){
            genreRepository.findById(dto.getParentGenreId())
                    .ifPresent(genre::setParentGenre);
        }
        return genre;
    }
    public List<GenreDTO> toDTOList(List<Genre> genres) {
        return toDTOList(genres, false);
    }

    public List<GenreDTO> toDTOList(List<Genre> genres, boolean includeSubGenres) {
        if (genres == null) {
            return null;
        }
        return genres.stream()
                .map(genre -> toDTO(genre, includeSubGenres))
                .collect(Collectors.toList());
    }
}
