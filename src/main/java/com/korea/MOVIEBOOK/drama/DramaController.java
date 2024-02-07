package com.korea.MOVIEBOOK.drama;
import com.korea.MOVIEBOOK.ContentsService;
import com.korea.MOVIEBOOK.ContentsDTO;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.heart.HeartRepository;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.payment.PaymentRepository;
import com.korea.MOVIEBOOK.review.Review;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/drama")
public class DramaController {

    private final DramaService dramaService;
    private final ContentsService contentsService;
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final HeartRepository heartRepository;

    @GetMapping("")
    public String dramaLists(Model model) {
        List<Drama> dramaList1 = dramaService.findDramasInRange(1, 10);
        List<Drama> dramaList2 = dramaService.findDramasInRange(11, 20);

        List<List<Drama>> dramaListList1 = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 5;
        for (int i = 1; i <= dramaList1.size() / 5; i++) {
            dramaListList1.add(dramaList1.subList(startIndex, Math.min(endIndex, dramaList1.size())));
            startIndex += 5;
            endIndex += 5;
        }

        List<List<Drama>> dramaListList2 = new ArrayList<>();
        startIndex = 0;
        endIndex = 5;
        for (int i = 1; i <= dramaList2.size() / 5; i++) {
            dramaListList2.add(dramaList2.subList(startIndex, Math.min(endIndex, dramaList2.size())));
            startIndex += 5;
            endIndex += 5;
        }

        int rank = 1;
        for (Drama drama : dramaList1) {
            drama.setRankNum(rank++);
        }
        rank = 1;
        for (Drama drama : dramaList2) {
            drama.setRankNum(rank++);
        }

        model.addAttribute("dramaListList1", dramaListList1);
        model.addAttribute("dramaListList2", dramaListList2);
        return "drama/drama_list";
    }

    @GetMapping("/detail")
    public String dramaDetail(Long dramaId, Model model, Principal principal) {
        Drama drama = dramaService.getDramaById(dramaId);
        ContentsDTO contentsDTOS = this.contentsService.setDramaContentsDTO(drama);
        List<List<String>> actorListList = this.dramaService.getActorListList(drama);
        List<Review> reviews = dramaService.getReviewByDramaId(dramaId).stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = dramaService.getReviewByDramaId(dramaId);
        String paid = "false";

        double avgRating = reviews.stream()
                .filter(review -> review.getRating() != null)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0);

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());

        model.addAttribute("category", "drama");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", actorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        if (principal != null) {
            String providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }

            Heart heart = this.heartRepository.findByMemberAndDrama(member, drama);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "drama", String.valueOf(dramaId)));
            if (payment.isPresent()) {
                paid = "true";
            }
            List<Payment> payments = this.paymentRepository.findBymember(member);
            long sum = 0;

            for (int i = 0; i < payments.size(); i++) {
                if (payments.get(i).getContent().contains("충전")) {
                    sum += Long.valueOf(payments.get(i).getPaidAmount());
                } else {
                    sum -= Long.valueOf(payments.get(i).getPaidAmount());
                }
            }
            model.addAttribute("paid", paid);
            model.addAttribute("login", "true");
            model.addAttribute("member", member);
            model.addAttribute("sum", sum);
            model.addAttribute("heart", heart);
        } else {
            model.addAttribute("paid", paid);
            model.addAttribute("login", "false");
            model.addAttribute("member", "");
            model.addAttribute("sum", "");
            model.addAttribute("heart", "");
        }
        return "contents/contents_detail";
    }

    @GetMapping("/detail/{dramaId}")
    public String dramaDetail2(@PathVariable("dramaId") Long dramaId, Model model, Principal principal) {
        Drama drama = dramaService.getDramaById(dramaId);
        ContentsDTO contentsDTOS = this.contentsService.setDramaContentsDTO(drama);
        List<List<String>> actorListList =  this.dramaService.getActorListList(drama);
        List<Review> reviews = dramaService.getReviewByDramaId(dramaId).stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = dramaService.getReviewByDramaId(dramaId);
        String paid = "false";

        double avgRating = reviews.stream()
                .filter(review -> review.getRating() != null)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0);

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());

        model.addAttribute("category", "drama");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", actorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        if(principal != null){
            String providerID = principal.getName();Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "drama", String.valueOf(dramaId)));
            if(payment.isPresent()){
                paid ="true";
            }

            List<Payment> payments  = this.paymentRepository.findBymember(member);
            long sum = 0;

            for(int i  = 0 ; i < payments.size(); i++){
                if(payments.get(i).getContent().contains("충전")){
                    sum += Long.valueOf(payments.get(i).getPaidAmount());
                } else {
                    sum -= Long.valueOf(payments.get(i).getPaidAmount());
                }
            }
            model.addAttribute("paid",paid);
            model.addAttribute("login","true");
            model.addAttribute("member",member);
            model.addAttribute("sum",sum);
        } else {
            model.addAttribute("paid",paid);
            model.addAttribute("login","false");
            model.addAttribute("member","");
            model.addAttribute("sum","");
        }
        return "contents/contents_detail";
    }

}