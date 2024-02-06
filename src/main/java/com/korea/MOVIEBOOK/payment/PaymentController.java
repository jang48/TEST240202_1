package com.korea.MOVIEBOOK.payment;

import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Controller
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;

    @GetMapping("/payment")
    public String kakao(Model model, Principal principal, @RequestParam(value = "page", defaultValue = "0") int page, HttpServletRequest request) {


        String url = request.getRequestURI();
        String referer = request.getHeader("Referer");

        System.out.println(url);
        System.out.println(referer);

        String providerID = "";
        if(principal != null ) {
            providerID = principal.getName();
        }

        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }
        List<Payment> payments = this.paymentService.findPaymentListByMember(member);
        long sum = 0;

        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getContent().contains("충전")) {
                sum += Long.valueOf(payments.get(i).getPaidAmount());
            } else {
                sum -= Long.valueOf(payments.get(i).getPaidAmount());
            }
        }

        Page<Payment> paging = this.paymentService.getPaymentsByMember(member, page);


        model.addAttribute("member", member);
        model.addAttribute("paging", paging);
        model.addAttribute("sum", sum);
        return "payment/payment";
    }

    @PostMapping("/kakaoPayCheck")
    public String kakaoPayCheck(@RequestBody PaymentDTO paymentDTO) {
        String content = paymentDTO.getO_paidAmount() + "Coin 충전";
        this.paymentService.savePayment(paymentDTO.getM_id(), "kakao", paymentDTO.getO_paidAmount(), paymentDTO.getO_shipno(), paymentDTO.getO_paytype(), paymentDTO.getS_phone(), content, "", "", Pageable.unpaged());
        return "redirect:/payment";
    }

    @PostMapping("/tossPayCheck")
    public String tossPayCheck(@RequestBody PaymentDTO paymentDTO) {
        String content = paymentDTO.getO_paidAmount() + "Coin 충전";
        this.paymentService.savePayment(paymentDTO.getM_id(), "toss", paymentDTO.getO_paidAmount(), paymentDTO.getO_shipno(), paymentDTO.getO_paytype(), paymentDTO.getS_phone(), content, "", "", Pageable.unpaged());
        return "redirect:/payment";
    }
//    @PostMapping("/kakaoPayCheck")
//    public String saveKakaoPaymentData(@RequestParam("m_id") Long id,
//                                       @RequestParam("m_email") String mEmail,
//                                       @RequestParam("s_name") String sName,
//                                       @RequestParam("s_phone") String sPhone,
//                                       @RequestParam("o_shipno") String oPaidNo,
//                                       @RequestParam("o_paidAmount") String oPaidAmount,
//                                       @RequestParam("o_paytype") String oPayType) {
//        System.out.println("Received Data:");
//        System.out.println("m_email: " + mEmail);
//        System.out.println("s_name: " + sName);
//        System.out.println("s_phone: " + sPhone);
//        System.out.println("o_paidAmount: " + oPaidAmount);
//        System.out.println("o_paytype: " + oPayType);
//
//        this.paymentService.SavePayment(id, "kakao", oPaidAmount, oPaidNo, oPayType, Long.valueOf(sPhone));
//       return "redirect:/kakaoPay";
//    }

    @PostMapping("/payment/category/{category}/{contentsID}/{usedCoins}")
    public String paymentCatgory(Principal principal, @PathVariable("category") String category, @PathVariable("contentsID") String contentsID, @PathVariable("usedCoins") String paidAmount) {
        String providerID = principal.getName();
        String content = paidAmount + "Coin 사용";

        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }
        this.paymentService.savePayment(member.getId(), "coin", paidAmount, "00000", "point", null, content, category, contentsID, Pageable.unpaged());

        if (Objects.equals(category, "movie")) {
            return "redirect:/movie/detail/" + contentsID;
        } else if (Objects.equals(category, "book")) {
            return "redirect:/book/detail/" + contentsID;
        } else if (Objects.equals(category, "drama")) {
            return "redirect:/drama/detail/" + contentsID;
        }
        return "redirect:/webtoon/detail/" + contentsID;
    }
}
