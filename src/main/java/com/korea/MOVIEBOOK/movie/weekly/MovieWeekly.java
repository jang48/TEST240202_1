package com.korea.MOVIEBOOK.movie.weekly;

import com.korea.MOVIEBOOK.movie.movie.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MovieWeekly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String year;                // 조회 일자(년)     - LocalDate

    private String week;                // 조회 일자(주)     - LocalDate

    private Long rankNum;                  // 순위             - 영화 진흥원 API

    @OneToOne(mappedBy = "movieweekly")
    private Movie movie;
}
