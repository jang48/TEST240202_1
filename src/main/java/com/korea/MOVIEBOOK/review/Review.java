package com.korea.MOVIEBOOK.review;

import com.korea.MOVIEBOOK.comment.Comment;
import com.korea.MOVIEBOOK.drama.Drama;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.movie.movie.Movie;
import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String Category;         // movie, tv, book, webtoon

    @Column(length = 1000)            // 1000 char로 제한
    private String comment;           // 리뷰 내용

    private Double rating;            // 리뷰 평점

    private LocalDateTime dateTime;   // 리뷰 작성 일자

    private LocalDateTime modifyDate; // 리뷰 수정 일자

    @ManyToOne
    private Member member;

    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Book book;

    @ManyToOne
    private Drama drama;

    @ManyToOne
    private Webtoon webtoon;

    @OneToMany( mappedBy = "review", cascade = CascadeType.ALL)
    private List<Heart> hearts;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    private List<Comment> commentList;
}
