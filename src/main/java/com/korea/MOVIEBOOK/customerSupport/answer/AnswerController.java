package com.korea.MOVIEBOOK.customerSupport.answer;

import com.korea.MOVIEBOOK.customerSupport.Category;
import com.korea.MOVIEBOOK.customerSupport.question.Question;
import com.korea.MOVIEBOOK.customerSupport.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("customerSupport/answer")
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;

    @GetMapping("/answerForm/{questionId}")
    public String answerForm(@PathVariable Long questionId, Model model) {
        Question question = questionService.findByQuestionId(questionId);
        model.addAttribute("category", Category.QUESTION);
        model.addAttribute("question", question);
        return "/customerSupport/answer/answerForm";
    }

    @PostMapping("/createAnswer")
    public String createAnswer(Long questionId, String content) {
        Question question = questionService.findByQuestionId(questionId);
        answerService.create(question, content);
        return "redirect:/customerSupport/question/detail/" + questionId;
    }

    @GetMapping("/answerUpdateForm/{questionId}")
    public String answerUpdateForm(@PathVariable Long questionId, Model model) {
        Question question = questionService.findByQuestionId(questionId);
        model.addAttribute("category", Category.QUESTION);
        model.addAttribute("question", question);
        return "/customerSupport/answer/answerUpdateForm";
    }

    @PostMapping("/updateAnswer")
    public String updateAnswer(Long questionId, String content) {
        Question question = questionService.findByQuestionId(questionId);
        Answer answer = answerService.findByAnswerId(question.getAnswer().getId());
        answerService.update(answer, content);
        return "redirect:/customerSupport/question/detail/" + questionId;
    }
}
