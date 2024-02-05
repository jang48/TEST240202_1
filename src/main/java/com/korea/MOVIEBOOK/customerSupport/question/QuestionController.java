package com.korea.MOVIEBOOK.customerSupport.question;

import com.korea.MOVIEBOOK.customerSupport.Category;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberCreateForm;
import com.korea.MOVIEBOOK.member.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("customerSupport/question")
public class QuestionController {

    private final QuestionService questionService;
    private final MemberService memberService;

    @GetMapping("/questionForm")
    public String questionForm(Model model, QuestionCreateForm questionCreateForm) {
        model.addAttribute("category", Category.QUESTION);
        return "/customerSupport/question/questionForm";
    }

    @PostMapping("/createQuestion")
    public String createQuestion(Model model, Principal principal, @Valid @NotNull QuestionCreateForm questionCreateForm, BindingResult bindingResult) {
        Member member = memberService.getMember(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", Category.QUESTION);
            return "/customerSupport/question/questionForm";
        }
        questionService.create(member, questionCreateForm.getTitle(), questionCreateForm.getContent(), Category.QUESTION, questionCreateForm.isCheckBox());
        return "redirect:/customerSupport/question";
    }

    @GetMapping("/noticeForm")
    public String noticeForm(Model model, QuestionCreateForm questionCreateForm) {
        model.addAttribute("category", Category.NOTICE);
        return "/customerSupport/question/questionForm";
    }

    @PostMapping("/createNotice")
    public String createNotice(Model model, Principal principal, @Valid @NotNull QuestionCreateForm questionCreateForm, BindingResult bindingResult) {
        Member member = memberService.getMember(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", Category.NOTICE);
            return "/customerSupport/question/questionForm";
        }
        questionService.create(member, questionCreateForm.getTitle(), questionCreateForm.getContent(), Category.NOTICE, questionCreateForm.isCheckBox());
        return "redirect:/customerSupport/notice";
    }

    @GetMapping("/FAQForm")
    public String FAQForm(Model model, QuestionCreateForm questionCreateForm) {
        model.addAttribute("category", Category.FAQ);
        return "/customerSupport/question/questionForm";
    }

    @PostMapping("/createFAQ")
    public String createFAQ(Model model, Principal principal, @Valid @NotNull QuestionCreateForm questionCreateForm, BindingResult bindingResult) {
        Member member = memberService.getMember(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", Category.FAQ);
            return "/customerSupport/question/questionForm";
        }
        questionService.create(member, questionCreateForm.getTitle(), questionCreateForm.getContent(), Category.FAQ, questionCreateForm.isCheckBox());
        return "redirect:/customerSupport/FAQ";
    }

    @GetMapping("/detail")
    public String questionDetail(Long questionId, Model model, Principal principal, HttpSession session) {
        Question question = questionService.findByQuestionId(questionId);
        String errorMsg = "해당 글을 읽을 권한이 없습니다.";
        Member member = memberService.getMember(principal.getName());
        if (principal == null) {    //  로그인안했을때
            if (question.isPrivate()) { //  로그인 안했는데 비밀글 일때
                return "redirect:/customerSupport/question?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
            } else {    //  비회원인데 비밀글이 아닐때
                model.addAttribute("question", question);
                model.addAttribute("category", question.getCategory());
                return "/customerSupport/question/questionDetail";
            }
        } else {    //  로그인했을때
            if (question.isPrivate()) {
                if (question.getMember() == member || member.getUsername().equals("admin")) {   //  로그인했고 비밀글이지만 작성자 맞을때
                    model.addAttribute("user", member);
                    model.addAttribute("question", question);
                    model.addAttribute("category", question.getCategory());
                    return "/customerSupport/question/questionDetail";
                } else {    //  로그인했고 비밀글이지만 작성자 아닐때
                    System.out.println("====================>작성자 아닐때");
                    System.out.println("====================>" + errorMsg);
                    return "redirect:/customerSupport/question?error=" + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8);
                }
            } else {    //  로그인했고 비밀글이 아닐때
                model.addAttribute("user", member);
                model.addAttribute("question", question);
                model.addAttribute("category", question.getCategory());
                return "/customerSupport/question/questionDetail";
            }
        }
    }

    @PostMapping("/delete/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId, Principal principal, String category) {
        Member member = memberService.getMember(principal.getName());
        Question question = questionService.findByQuestionId(questionId);
        System.out.println(category);
        if (member.getUsername().equals(question.getMember().getUsername())) {
            questionService.delete(question);
            if (category.equals(Category.QUESTION.getValue())) {
                return "redirect:/customerSupport/question";
            } else if (category.equals(Category.NOTICE.getValue())) {
                return "redirect:/customerSupport/notice";
            } else if (category.equals(Category.FAQ.getValue())) {
                return "redirect:/customerSupport/FAQ";
            } else {
                return "redirect:/customerSupport/myQuestion";
            }
        } else {
            if (category.equals(Category.QUESTION.getValue())) {
                return "redirect:/customerSupport/question";
            } else if (category.equals(Category.NOTICE.getValue())) {
                return "redirect:/customerSupport/notice";
            } else if (category.equals(Category.FAQ.getValue())) {
                return "redirect:/customerSupport/FAQ";
            } else {
                return "redirect:/customerSupport/myQuestion";
            }
        }
    }

    @GetMapping("update/{questionId}")
    public String updateQuestion(@PathVariable Long questionId, Model model, Principal principal, String category) {
        Question question = questionService.findByQuestionId(questionId);
        Member member = memberService.getMember(principal.getName());
        if (member.getUsername().equals(question.getMember().getUsername())) {
            model.addAttribute("category", category);
            model.addAttribute("question", question);
            return "customerSupport/question/questionForm";
        } else {
            return "redirect:/";
        }
    }
}
