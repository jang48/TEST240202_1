package com.korea.MOVIEBOOK.customerSupport.question;

import com.korea.MOVIEBOOK.customerSupport.Category;
import com.korea.MOVIEBOOK.customerSupport.answer.Answer;
import com.korea.MOVIEBOOK.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    private Category category;

    private LocalDateTime writeDate;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL)
    private Answer answer;

    @ManyToOne
    private Member member;

    private boolean isPrivate;
}
