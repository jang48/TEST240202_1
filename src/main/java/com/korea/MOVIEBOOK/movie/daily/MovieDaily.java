package com.korea.MOVIEBOOK.movie.daily;

import com.korea.MOVIEBOOK.movie.movie.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class MovieDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date;               // 조회 일자        - LocalDate

    private Long rankNum;                 // 순위             - 영화 진흥원 API

    @OneToOne(mappedBy = "moviedaily")
    private Movie movie;

}
