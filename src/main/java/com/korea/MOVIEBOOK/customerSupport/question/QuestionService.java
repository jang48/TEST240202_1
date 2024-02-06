package com.korea.MOVIEBOOK.customerSupport.question;

import com.korea.MOVIEBOOK.customerSupport.Category;
import com.korea.MOVIEBOOK.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public void create(Member member, String title, String content, Category category, boolean isPrivate) {
        Question question = new Question();
        question.setTitle(title);
        question.setContent(content);
        question.setCategory(category);
        question.setWriteDate(LocalDateTime.now());
        question.setMember(member);
        question.setPrivate(isPrivate);
        questionRepository.save(question);
    }

    public Page<Question> getQuestionList(Category category, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return questionRepository.findByCategory(category, pageable);
    }

    public Page<Question> getMyQuestionList(Member member, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return questionRepository.findByMember(member, pageable);
    }

    public Question findByQuestionId(Long questionId) {
        Question question = null;
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isPresent()) {
            question = optionalQuestion.get();
        }
        return question;
    }

    public void delete(Question question) {
        questionRepository.delete(question);
    }

    public Question updateQuestion(Long questionId, QuestionCreateForm questionCreateForm) {
        Question question = questionRepository.findById(questionId).get();
        question.setTitle(questionCreateForm.getTitle());
        question.setContent(questionCreateForm.getContent());
        question.setPrivate(questionCreateForm.isCheckBox());
        questionRepository.save(question);
        return question;
    }
}
