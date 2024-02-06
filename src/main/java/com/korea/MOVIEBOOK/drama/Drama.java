package com.korea.MOVIEBOOK.drama;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.review.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Entity
@Getter
@Setter

public class Drama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; // 고유 값

    private String title; // 제목

    private String imageUrl; // 포스터

    private String genre; // 장르

    private String releaseDate; // 개봉일

    private String company; // 제작사

    private String director; // 감독

    private String viewingRating; // 연령등급

    @Column(columnDefinition = "text")
    private String actor; // 배우

    private int rankNum; // 순위

    private String plot; // 줄거리

    private double rating;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Review> reviewList;

    @OneToOne
    private Payment payment;

    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL)
    private List<Heart> heartList;
}
