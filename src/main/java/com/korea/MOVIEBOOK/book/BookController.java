package com.korea.MOVIEBOOK.book;

import com.korea.MOVIEBOOK.ContentsService;
import com.korea.MOVIEBOOK.ContentsDTO;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.heart.HeartRepository;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.payment.PaymentRepository;
import com.korea.MOVIEBOOK.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/book")
public class BookController {

    private final BookService bookService;
    private final ContentsService contentsService;
    private final MemberService memberService;
    private final PaymentRepository paymentRepository;
    private final HeartRepository heartRepository;
    @GetMapping("")
    public String mainPage(Model model) {
        List<Book> bestSellerList = bookService.getBestSellerList();
        bestSellerList.sort(Comparator.comparing(Book::getBestRank));
        List<List<Book>> bestSellerListList = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 5;
        for (int i = 1; i <= bestSellerList.size()/5; i++) {
            bestSellerListList.add(bestSellerList.subList(startIndex, Math.min(endIndex, bestSellerList.size())));
            startIndex+=5;
            endIndex+=5;
        }

        List<Book> newSpecialBookList = bookService.getNewSpecialBookList();
        List<List<Book>> newSpecialBookListList = new ArrayList<>();
        startIndex = 0;
        endIndex = 5;
        for (int i = 1; i <= newSpecialBookList.size()/5; i++) {
            newSpecialBookListList.add(newSpecialBookList.subList(startIndex, Math.min(endIndex, newSpecialBookList.size())));
            startIndex+=5;
            endIndex+=5;
        }

        List<Book> recommendList = bookService.getRecommendationList();
        List<List<Book>> recommendListList = new ArrayList<>();
        startIndex = 0;
        endIndex = 5;
        for (int i = 1; i <= recommendList.size()/5; i++) {
            recommendListList.add(recommendList.subList(startIndex, Math.min(endIndex, recommendList.size())));
            startIndex+=5;
            endIndex+=5;
        }
        List<List<List<Book>>> allList = new ArrayList<>();
        allList.add(bestSellerListList);
        allList.add(newSpecialBookListList);
        allList.add(recommendListList);
        model.addAttribute("allList", allList);
        return "book/book_list";
    }

    @GetMapping("/detail")
    public String bookDetail(String isbn, Model model, Principal principal) {
        Book book = bookService.findByIsbn(isbn);
        ContentsDTO contentsDTOS = this.contentsService.setBookContentsDTO(book);
        List<List<String>> authorListList = bookService.getAuthorListList(book);
        List<Review> reviews = book.getReviewList().stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = book.getReviewList();
        String paid = "false";


        double avgRating = reviews.stream() // reviews에서 stream 생성
                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
                .average() // 평점의 평균값 계산
                .orElse(0); // 리뷰가 없을 경우 0.0출력

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());


        model.addAttribute("category", "book");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", authorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        String providerID = "";
        if(principal != null ) {
            providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }
            Heart heart = this.heartRepository.findByMemberAndBook(member, book);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "book", isbn));
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
            model.addAttribute("heart",heart);
        } else {
            model.addAttribute("paid",paid);
            model.addAttribute("login","false");
            model.addAttribute("member","");
            model.addAttribute("sum","");
            model.addAttribute("heart","");
        }

        return "contents/contents_detail";
    }

    @GetMapping("/detail/{isbn}")
    public String bookDetail1(@PathVariable("isbn") String isbn, Model model, Principal principal) {
        Book book = bookService.findByIsbn(isbn);
        ContentsDTO contentsDTOS = this.contentsService.setBookContentsDTO(book);
        List<List<String>> authorListList = bookService.getAuthorListList(book);
        List<Review> reviews = book.getReviewList().stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = book.getReviewList();
        String paid = "false";

        double avgRating = reviews.stream() // reviews에서 stream 생성
                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
                .average() // 평점의 평균값 계산
                .orElse(0); // 리뷰가 없을 경우 0.0출력

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());


        model.addAttribute("category", "book");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("authorListList", authorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        if(principal != null){
            String providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }
            Heart heart = this.heartRepository.findByMemberAndBook(member, book);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "book", isbn));
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
            model.addAttribute("heart",heart);
        } else {
            model.addAttribute("paid",paid);
            model.addAttribute("login","false");
            model.addAttribute("member","");
            model.addAttribute("sum","");
            model.addAttribute("heart","");
        }

        return "contents/contents_detail";
    }
}