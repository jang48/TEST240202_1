package com.korea.MOVIEBOOK.member;

import com.korea.MOVIEBOOK.book.BookService;
import com.korea.MOVIEBOOK.drama.DramaService;
import com.korea.MOVIEBOOK.movie.movie.Movie;
import com.korea.MOVIEBOOK.movie.movie.MovieService;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.payment.PaymentService;
import com.korea.MOVIEBOOK.review.Review;
import com.korea.MOVIEBOOK.review.ReviewService;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.webtoon.webtoonList.WebtoonService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.member;

@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;
    private final PaymentService paymentService;
    private final MovieService movieService;
    private final BookService bookService;
    private final DramaService dramaService;
    private final WebtoonService webtoonService;

    @GetMapping("/signup")
    public String signup(MemberCreateForm memberCreateForm) {
        return "member/signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid @NotNull MemberCreateForm memberCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 검증에 실패한 경우
            return "member/signup_form";
        }
        // 닉네임 중복 확인
        if (!memberService.nicknameUnique(memberCreateForm.getNickname())) {
            bindingResult.rejectValue("nickname", "duplicate", "이미 사용 중인 닉네임입니다.");
            return "member/signup_form";
        }
        try {
            Member member = memberService.create(memberCreateForm.getUsername(),
                    memberCreateForm.getPassword1(), memberCreateForm.getNickname(), memberCreateForm.getEmail());
            // memberService의 create 메서드를 호출하여 memberCreateForm에서 받은 데이터를 기반으로 새 회원을 생성하고
            // 그 결과로 반환된 회원 객체를 member 변수에 할당
            emailService.sendEmail(member.getEmail(), "http://localhost:8888/member/verify?userId=" + member.getId());
            // 이메일 인증 링크 전송
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자");
            return "member/signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "member/signup_form";
        }
        return "redirect:/member/login";
    }


    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("userId") long userId) {
        memberService.verifyEmail(userId);  // userId를 사용하여 이메일 인증 처리
        return "redirect:/member/emailVerificationSuccess";
    }

    @GetMapping("/emailVerificationSuccess")
    public String emailVerificationSuccess() {
        return "member/login_form";
    }

    @GetMapping("/emailVerification")
    public String emailVerification() {
        return "member/email_verification_form";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {

//        String uri = request.getHeader("Referer");
//        if (uri != null && !uri.contains("/login")) {
//            request.getSession().setAttribute("prevPage", uri);
//        }

        HttpSession session = request.getSession();
        String referer = request.getHeader("referer");
        session.setAttribute("referer", referer);
        session.setAttribute("currentPageUrl", request.getRequestURI());

        return "member/login_form";
    }

    @GetMapping("/login/contents")
    public String loginFromContents() {
        return "member/login_form";
    }
    @GetMapping("/login/google")
    public String googleLogin() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/login/kakao")
    public String kakaoLogin() {
        return "redirect:/oauth2/authorization/kakao";
    }

    @GetMapping("/login/naver")
    public String naverLogin() {
        return "redirect:/oauth2/authorization/naver";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid Member member, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "입력한 정보가 올바르지 않습니다.");
            return "member/reset_password";
        }

        Member member1 = memberService.getMemberByEmail(member.getEmail());
        if (member1 == null || !member1.getUsername().equals(member.getUsername())) {
            model.addAttribute("errorMessage", "해당 이메일 또는 아이디와 일치하는 회원 정보를 찾을 수 없습니다.");
            return "member/reset_password";
        }

        try {
            memberService.resetPassword(member1);
            model.addAttribute("successMessage", "이메일로 임시 비밀번호가 발송되었습니다.");
        } catch (MessagingException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "이메일 전송 중 오류가 발생했습니다.");
            return "member/login_form";
        }

        return "member/login_form";
    }

    @PostMapping("/findUsername")
    public String findUsername(@Valid @ModelAttribute Member member, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "유효하지 않은 이메일 형식입니다.");
            return "member/find_username";
        }

        Member member1 = memberService.getMemberByEmail(member.getEmail());
        if (member1 != null) {
            try {
                emailService.sendTemporaryUsername(member1.getEmail(), member1.getUsername());
                model.addAttribute("successMessage", "이메일로 아이디가 발송되었습니다.");
                return "member/login_form";
            } catch (MessagingException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "이메일 전송 중 오류 발생");
                return "member/find_username";
            }
        } else {
            model.addAttribute("errorMessage", "해당 이메일로 등록된 아이디 없음");
            return "member/find_username";
        }
    }





    @GetMapping("/resetPassword")
    public String showResetPasswordPage() {
        return "member/reset_password"; // Thymeleaf 템플릿 이름 (reset_password.html)
    }

    @GetMapping("/findUsername")
    public String findUsernamePage() {
        return "member/find_username";
    }

    @GetMapping("/mypage")
    @PreAuthorize("isAuthenticated()")
    public String showmyPage(Model model, Principal principal) {
        Member member = memberService.findByusername(principal.getName());
        if (member == null) {
            member = memberService.findByproviderId(principal.getName());
        }

        System.out.println("====================" + principal.getName());

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다.");
        }
        model.addAttribute("member", member);



        List<Payment> payments = this.paymentService.findPaymentListByMember(member);
        long sum = 0;

        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getContent().contains("충전")) {
                sum += Long.valueOf(payments.get(i).getPaidAmount());
            } else {
                sum -= Long.valueOf(payments.get(i).getPaidAmount());
            }
        }
        Long reviewCount = reviewService.getReivewCount(member);

        model.addAttribute("sum", sum);
        model.addAttribute("reviewCount", reviewCount);

//        Page<Payment> paging = this.paymentService.getPaymentsByMember(member, page);


//        List<Review> reviewList = reviewService.getAnswerTop5LatestByUser(user);
//        model.addAttribute("answerList", answerList);
        return "member/my_page";
    }




    // 마이페이지

    @RequestMapping("/mypage")
    @PreAuthorize("isAuthenticated()")
    public String uploadProfileImg(MultipartHttpServletRequest mre, Principal principal) throws IOException {
        Member member = memberService.getMember(principal.getName());

        MultipartFile mf = mre.getFile("file");
        String uploadPath = "";

//        String path = "C:\\" + "Project2\\" + "profileimg\\";
        String path = System.getProperty("user.dir") + "/Project2/" + "/profileimg/";

        File Folder = new File(path);
        if (!Folder.exists()) {
            Folder.mkdirs();
        }

        Path directoryPath = Paths.get(path);
        Files.createDirectories(directoryPath);

        String origional = mf.getOriginalFilename();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueFileName = timestamp + "_" + origional;
        uploadPath = path + uniqueFileName;

        try {
            mf.transferTo(new File(uploadPath));
            this.memberService.saveImg(member, "/profileimg/" + uniqueFileName);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        return "redirect:/member/mypage";
    }


    @GetMapping("/changeInformation")
    public String updateNm(Model model, NicknameForm nicknameForm, Principal principal, @RequestParam(value="page", defaultValue="0") int page) {

        paymentMember(model, principal, page);
        return "member/changeinfor";
    }

    @PostMapping("/changeInformation")
    public String updateNickname(Model model, @Valid NicknameForm nicknameForm, BindingResult bindingResult,
                                 Principal principal) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (bindingResult.hasErrors()) {
            return "member/changeinfor";
        }

        if (nicknameForm.getNewNickname().length() >= 2 || nicknameForm.getNewNickname().length() > 8) {
            Member member = memberService.findByusername(principal.getName());
            if (member == null) {
                member = memberService.findByproviderId(principal.getName());
            }
            memberService.updateNickname(member, nicknameForm.getNewNickname());
        }
        model.addAttribute("member", member);

        return "redirect:/member/mypage";
    }

    @GetMapping("/changePw")
    public String changePw(Model model, PasswordChangeForm passwordChangeForm, Principal principal, @RequestParam(value="page", defaultValue="0") int page) {
        paymentMember(model, principal, page);

        return "member/changepw";
    }


    @PostMapping("/changePw")
    public String passwordChange(Model model, @Valid PasswordChangeForm passwordChangeForm, BindingResult bindingResult,
                                 Principal principal) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        GrantedAuthority authority = authentication.getAuthorities().iterator().next();

        if (bindingResult.hasErrors()) {
            return "member/changepw";
        }

        if (passwordChangeForm.getPassword().equals(passwordChangeForm.getPasswordConfirm())) {
            if (authority.getAuthority().equals("ROLE_MEMBER")) {
                Member member = memberService.findByusername(principal.getName());
                if (passwordEncoder.matches(passwordChangeForm.getOldPassword(), member.getPassword())) {
                    memberService.changePassword(member, passwordChangeForm.getPassword());
                } else {
                    bindingResultReject(bindingResult);
                    return "member/changepw";
                }
            }
        } else {
            bindingResultReject(bindingResult);
            return "member/changepw";
        }

        model.addAttribute("member", member);
        return "redirect:/member/logout";
    }




    @GetMapping("/purchasedetails")
    public String memberPurchaseDetails(PasswordResetForm passwordResetForm, Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {


        paymentMember(model, principal, page);
        return "member/contents_purchase_details/member_purchase_details";
    }


    @GetMapping("/purchasedetails/movie")
    public String moviepurchasedetails(Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Member member = memberService.getMember(principal.getName());
        Page<Payment> paging = paymentService.getPaidMovieList(member, page);
        model.addAttribute("paging", paging);
        return "member/contents_purchase_details/movie_purchase_details";
    }

    @GetMapping("/purchasedetails/drama")
    public String dramapurchasedetails(Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Member member = memberService.getMember(principal.getName());
        Page<Payment> paging = paymentService.getPaidDramaList(member, page);
        model.addAttribute("paging", paging);
        return "member/contents_purchase_details/drama_purchase_details";
    }

    @GetMapping("/purchasedetails/book")
    public String bookpurchasedetails(Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Member member = memberService.getMember(principal.getName());
        Page<Payment> paging = paymentService.getPaidBookList(member, page);
        model.addAttribute("paging", paging);
        return "member/contents_purchase_details/book_purchase_details";
    }

    @GetMapping("/purchasedetails/webtoon")
    public String webtoonpurchasedetails(Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Member member = memberService.getMember(principal.getName());
        Page<Payment> paging = paymentService.getPaidWebtoonList(member, page);
        model.addAttribute("paging", paging);
        return "member/contents_purchase_details/webtoon_purchase_details";
    }



    @GetMapping("/deleteForm")
    public String memberDeleteForm(PasswordResetForm passwordResetForm, Principal principal, Model model, @RequestParam(value="page", defaultValue="0") int page) {
        paymentMember(model, principal, page);
        return "member/delete_form";
    }


    @PostMapping("/delete")
    public String memberDelete(Principal principal, @Valid PasswordResetForm passwordResetForm,
                               BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "member/delete_form";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (passwordResetForm.getPassword().equals(passwordResetForm.getPasswordConfirm())) {
            Member member = memberService.findByusername(principal.getName());
            if (member == null) {
                member = memberService.findByproviderId(principal.getName());
            }
            if (passwordEncoder.matches(passwordResetForm.getPassword(), member.getPassword())) {
                memberService.deleteMember(member);
                return "redirect:/";
            } else {
                bindingResultReject(bindingResult);
                return "member/delete_form";
            }
        } else {
            bindingResultReject(bindingResult);
            return "member/delete_form";
        }
    }


    private void bindingResultReject(BindingResult bindingResult) {
        bindingResult.rejectValue("passwordConfirm", "passwordInCorrect",
                "패스워드가 일치하지 않습니다.");
    }


    public void paymentMember(Model model, Principal principal, @RequestParam(value="page", defaultValue="0") int page){
        Member member = memberService.getMember(principal.getName());
        List<Payment> payments = this.paymentService.findPaymentListByMember(member);
        long sum = 0;
        Collections.sort(payments, Comparator.comparing(Payment::getDateTime).reversed());

        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getContent().contains("충전")) {
                sum += Long.valueOf(payments.get(i).getPaidAmount());
            } else {
                sum -= Long.valueOf(payments.get(i).getPaidAmount());
            }
        }


        model.addAttribute("sum", sum);
        model.addAttribute("PaymentList", payments);
        model.addAttribute("member", member);
    }
}

