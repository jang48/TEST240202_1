package com.korea.MOVIEBOOK.webtoon.webtoonList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WebtoonRepository extends JpaRepository<Webtoon, Long> {

//    List<Webtoon> findBywebtoonDay(String day);



    Webtoon findByWebtoonId(Long webtoonId);


    Page<Webtoon> findAll(Pageable pageable);


    @Query("SELECT w FROM Webtoon w " +
            "WHERE SUBSTRING_INDEX(w.author, '(', 1) LIKE %:kw% " +
            "   OR w.title LIKE %:kw% ")
    Page<Webtoon> findAllByWebtoonKeyword(@Param("kw") String kw, Pageable pageable);
}
