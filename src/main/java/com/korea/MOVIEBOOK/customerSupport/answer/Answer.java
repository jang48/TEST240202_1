package com.korea.MOVIEBOOK.customerSupport.answer;

import com.korea.MOVIEBOOK.customerSupport.question.Question;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private LocalDateTime writeDate;

    @OneToOne
    private Question question;
}
