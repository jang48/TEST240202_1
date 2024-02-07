package com.korea.MOVIEBOOK.webtoon.webtoonList;
import com.korea.MOVIEBOOK.ContentsService;
import com.korea.MOVIEBOOK.ContentsDTO;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.heart.HeartRepository;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.payment.PaymentRepository;
import com.korea.MOVIEBOOK.review.Review;
import com.korea.MOVIEBOOK.review.ReviewService;
import com.korea.MOVIEBOOK.webtoon.days.Day;
import com.korea.MOVIEBOOK.webtoon.days.DayService;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayList;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/webtoon")
public class WebtoonController {

    private final WebtoonService webtoonService;
    private final DayService dayService;
    private final WebtoonDayListService webtoonDayListService;
    private final ReviewService reviewService;
    private final ContentsService contentsService;
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final HeartRepository heartRepository;


    @GetMapping("")
    public String CreateDayList(Model model) {
        Day day = this.dayService.findByDay("tue");     // 그냥 DAY 테이블이 있는지 확인용
        if (day == null) {
            dayService.FindByWeek();
        }


        List<Day> days = this.dayService.findAll();
        List<List<List<Webtoon>>> allList = new ArrayList<>();//  월,화,수,목,금,토,일이라는 값을 가져오기 위함
        List<Webtoon> webtoonList = new ArrayList<>();

        for (Day day1 : days) {
            List<WebtoonDayList> webtoonDayLists2 = webtoonDayListService.findBywebtoonDay(day1);
            if(webtoonDayLists2.isEmpty()){
                List<Long> webtoon = webtoonService.getWebtoonAPI(day1.getUpdateDays());
                webtoonDayListService.SaveWebtoonDayList(day1.getId(), webtoon);
            }
            webtoonDayLists2 = webtoonDayListService.findBywebtoonDay(day1);
            for (WebtoonDayList webtoonDayList : webtoonDayLists2) {
                Webtoon webtoon2 = webtoonDayList.getWebtoonList();
                webtoonList.add(webtoon2);
            }
        }

        List<List<Webtoon>> dayListList = getListList(webtoonList);
        List<List<Webtoon>> mondayListList = getListListList(dayListList.get(0));
        List<List<Webtoon>> tuesdayListList = getListListList(dayListList.get(1));
        List<List<Webtoon>> wednesdayListList = getListListList(dayListList.get(2));
        List<List<Webtoon>> thursdayListList = getListListList(dayListList.get(3));
        List<List<Webtoon>> fridayListList = getListListList(dayListList.get(4));
        List<List<Webtoon>> saturdayListList = getListListList(dayListList.get(5));
        List<List<Webtoon>> sundayListList = getListListList(dayListList.get(6));

        allList.add(mondayListList);
        allList.add(tuesdayListList);
        allList.add(wednesdayListList);
        allList.add(thursdayListList);
        allList.add(fridayListList);
        allList.add(saturdayListList);
        allList.add(sundayListList);
        model.addAttribute("allList", allList);

        return "webtoon/webtoon_list";
    }


    public List<List<Webtoon>> getListListList(List<Webtoon> webtoonList){
        int startIndex = 0;
        int endIndex = 5;
        List<List<Webtoon>> result = new ArrayList<>();
        for (int i = 1; i <= webtoonList.size() / 5; i++) {
            result.add(webtoonList.subList(startIndex, Math.min(endIndex, webtoonList.size())));
            startIndex += 5;
            endIndex += 5;
        }
        return result;
    }


    public List<List<Webtoon>> getListList(List<Webtoon> webtoonList) {
        int startIndex = 0;
        int endIndex = 10;
        List<List<Webtoon>> webtoonListList = new ArrayList<>();
        for (int i = 1; i <= webtoonList.size() / 10; i++) {
            webtoonListList.add(webtoonList.subList(startIndex, Math.min(endIndex, webtoonList.size())));
            startIndex += 10;
            endIndex += 10;
        }
        return webtoonListList;
    }

    @GetMapping("/detail")
    public String WebtoonDetail1(Model model, Long webtoonId, Principal principal) {
        Webtoon webtoon = this.webtoonService.findWebtoonByWebtoonId(webtoonId);
        ContentsDTO contentsDTOS = this.contentsService.setWetoonContentsDTO(webtoon);
        List<List<String>> authorListList =  this.webtoonService.getAuthorListList(webtoon);
        List<Review> reviews = this.reviewService.findWebtoonReview(webtoon.getWebtoonId()).stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = this.reviewService.findWebtoonReview(webtoon.getWebtoonId());
        String paid = "false";

        double avgRating = reviews.stream() // reviews에서 stream 생성
                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
                .average() // 평점의 평균값 계산
                .orElse(0); // 리뷰가 없을 경우 0.0출력

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());


        model.addAttribute("category", "webtoon");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", authorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        if(principal != null){
            String providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }

            Heart heart = this.heartRepository.findByMemberAndWebtoon(member, webtoon);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "webtoon", String.valueOf(webtoonId)));
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
    @GetMapping("/detail/{webtoonId}")
    public String WebtoonDetail2(Model model, @PathVariable("webtoonId") Long webtoonId, Principal principal){
        Webtoon webtoon = this.webtoonService.findWebtoonByWebtoonId(webtoonId);
        ContentsDTO contentsDTOS = this.contentsService.setWetoonContentsDTO(webtoon);
        List<List<String>> authorListList =  this.webtoonService.getAuthorListList(webtoon);
        List<Review> reviews = this.reviewService.findWebtoonReview(webtoon.getWebtoonId()).stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = this.reviewService.findWebtoonReview(webtoon.getWebtoonId());
        String paid = "false";

        double avgRating = reviews.stream() // reviews에서 stream 생성
                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
                .average() // 평점의 평균값 계산
                .orElse(0); // 리뷰가 없을 경우 0.0출력

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());

        model.addAttribute("category", "webtoon");
        model.addAttribute("contents", webtoon);
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", authorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));

        if(principal != null){
            String providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }
            Heart heart = this.heartRepository.findByMemberAndWebtoon(member, webtoon);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "webtoon", String.valueOf(webtoonId)));
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


