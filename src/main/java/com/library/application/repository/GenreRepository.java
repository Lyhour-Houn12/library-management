package com.library.application.repository;

import com.library.application.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    Boolean existsByCode(String code);

    Optional<Genre> findByCode(String code);

    List<Genre> findByActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIdAndActiveTrueOrderByDisplayOrderAsc(Long parentGenreId);

    long countByActiveTrue();

    @Query("""
        SELECT g FROM Genre g WHERE
        LOWER(g.name) LIKE (CONCAT('%', :searchTerm, '%')) OR
        LOWER(g.code) LIKE (CONCAT('%', :searchTerm, '%'))
    """)
    Page<Genre> searchGenres(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("select count(b) from Book b where b.genre.id=:genreId")
    long countBooksByGenre(@Param("genreId") Long genreId);

    // check if any books are using this genre
    @Query("SELECT count(b) > 0 from Book b WHERE b.genre.id=:genreId")
    boolean isGenreInUse(@Param("genreId") Long genreId);

}
