package com.korea.MOVIEBOOK.customerSupport.answer;

import com.korea.MOVIEBOOK.customerSupport.question.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;

    public void create(Question question, String content) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(question);
        answer.setWriteDate(LocalDateTime.now());
        answerRepository.save(answer);
    }

    public void update(Answer answer, String content) {
        answer.setContent(content);
        answerRepository.save(answer);
    }

    public Answer findByAnswerId(Long answerId) {
        return answerRepository.findById(answerId).get();
    }
}
