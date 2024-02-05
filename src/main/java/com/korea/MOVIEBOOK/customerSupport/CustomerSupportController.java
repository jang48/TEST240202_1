package com.korea.MOVIEBOOK.customerSupport;

import com.korea.MOVIEBOOK.customerSupport.question.Question;
import com.korea.MOVIEBOOK.customerSupport.question.QuestionService;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customerSupport")
public class CustomerSupportController {

    private final QuestionService questionService;
    private final MemberService memberService;

    @GetMapping("notice")
    public String mainPage(Model model, Principal principal, @RequestParam(value="page", defaultValue="0") int page) {
        Page<Question> paging = questionService.getQuestionList(Category.NOTICE, page);
        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);
        model.addAttribute("paging", paging);
        model.addAttribute("category", Category.NOTICE);
        return "customerSupport/customerSupportQuestion";
    }

    @GetMapping("question")
    public String question(Model model, Principal principal, @RequestParam(value="page", defaultValue="0") int page) {
        Page<Question> paging = questionService.getQuestionList(Category.QUESTION, page);
        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);
        model.addAttribute("paging", paging);
        model.addAttribute("category", Category.QUESTION);
        return "customerSupport/customerSupportQuestion";
    }

    @GetMapping("FAQ")
    public String FAQ(Model model, Principal principal, @RequestParam(value="page", defaultValue="0") int page) {
        Page<Question> paging = questionService.getQuestionList(Category.FAQ, page);
        Member member = memberService.getMember(principal.getName());
        model.addAttribute("member", member);
        model.addAttribute("paging", paging);
        model.addAttribute("category", Category.FAQ);
        return "customerSupport/customerSupportQuestion";
    }

    @GetMapping("myQuestion")
    public String myQuestion(Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Member member = memberService.getMember(principal.getName());
        Page<Question> paging = questionService.getMyQuestionList(member, page);
        model.addAttribute("member", member);
        model.addAttribute("paging", paging);
        model.addAttribute("category", Category.MYQUESTION);
        return "customerSupport/customerSupportQuestion";
    }
}
