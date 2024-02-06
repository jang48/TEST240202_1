package com.korea.MOVIEBOOK.review;

import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.book.BookRepository;
import com.korea.MOVIEBOOK.drama.Drama;
import com.korea.MOVIEBOOK.drama.DramaRepository;
import com.korea.MOVIEBOOK.heart.HeartService;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import com.korea.MOVIEBOOK.movie.movie.Movie;
import com.korea.MOVIEBOOK.movie.movie.MovieRepository;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayList;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.webtoon.webtoonList.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
    private final ReviewService reviewService;
    private final MovieRepository movieRepository;
    private final BookRepository bookRepository;
    private final DramaRepository dramaRepository;
    private final WebtoonRepository webtoonRepository;
    private final MemberService memberService;
    private final HeartService heartService;

    @GetMapping("/create")
    public String createReview() {
        return "Review/review";
    }

    @GetMapping("/create/drama")
    public String createDramaReview(@RequestParam("dramaId") Long dramaId, @RequestParam("comment") String comment, @RequestParam("rating") Double rating, Principal principal) {

        String providerID = principal.getName();
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        this.reviewService.saveDramaReview(dramaId, comment, rating, member);
        return "redirect:/drama/detail/" + dramaId;
    }

    @GetMapping("/create/movie")
    public String createMovieReview(@RequestParam("movieCD") String movieCD, @RequestParam("comment") String comment, @RequestParam("rating") Double rating, Principal principal) {

        String providerID = principal.getName();
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        this.reviewService.saveMovieReview(movieCD, comment, rating, member);
        return "redirect:/movie/detail/" + movieCD;
    }


    @GetMapping("/modify/{id}")
    public String movieReviewModify(@RequestParam("movieCD") String movieCD, @RequestParam("comment") String comment, @RequestParam("rating") Double rating, Principal principal){
        String providerID = "";
        if(principal != null ) {
            providerID = principal.getName();
        }
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        this.reviewService.modifyMovieReview(movieCD, comment, rating);
        return "redirect:/movie/detail/"+ movieCD;
    }

    @GetMapping("/create/book")
    public String createBookReview(@RequestParam("isbn") String isbn, @RequestParam("comment") String comment, @RequestParam("rating") Double rating, Principal principal) {

        String providerID = principal.getName();
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        reviewService.saveBookReview(isbn, comment, rating, member);
        return "redirect:/book/detail/" + isbn;
    }

    @GetMapping("/create/webtoon")
    public String createWebtoonReview(@RequestParam("webtoonId") Long webtoonId, @RequestParam("comment") String comment, @RequestParam("rating") Double rating, Principal principal) {

        String providerID = principal.getName();
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        this.reviewService.saveWebtoonReview(webtoonId, comment, rating, member);
        return "redirect:/webtoon/detail/" + webtoonId;
    }

////    @GetMapping("/webtoon/review/create")
////    public String createWebtoonReview(@RequestParam("webtoonId") Long webtoonId, @RequestParam("comment") String
////            comment, @RequestParam("rating") Double rating) {
////        this.reviewService.saveWebtoonReview(webtoonId, comment, rating);
////        return "redirect:/webtoon/detail?webtoonId=" + webtoonId;
////    }
//
//    @GetMapping("/review/detail/{id}")
//    public String DramaReviewDetail(@PathVariable("id") Long reviewId, Model model) {
//        Review review = reviewService.findReviewById(reviewId);
//        if (review == null) {
//            return "redirect:/error";
//        }
//        model.addAttribute("review", review);
//        return "/drama_review/review_detail";
//    }

    @GetMapping("/list")
    public String reviewList(String category, String contentsID, Model model, @RequestParam(value = "page", defaultValue = "0") int page, Principal principal) {

        Page<Review> paging;
        String providerID = "";
        if(principal != null ) {
            providerID = principal.getName();
        }
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        if (Objects.equals(category, "movie")) {
            Movie movie = this.movieRepository.findBymovieCode(contentsID);
//            reviews = this.reviewService.findReviews(movie.getId());
            paging = this.reviewService.getPaymentsByMovie(movie, page);
        } else if (Objects.equals(category, "book")) {
            Book book = this.bookRepository.findByIsbn(contentsID);
//            reviews = this.reviewService.getReviewByBookId(book.getId());
            paging = this.reviewService.getPaymentsByBook(book, page);
        } else if (Objects.equals(category, "drama")) {
            Drama drama = this.dramaRepository.getReferenceById(Long.valueOf(contentsID));
//            reviews = this.reviewService.getReviewByDramaId(Long.valueOf(contentsID));
            paging = this.reviewService.getPaymentsByDrama(drama, page);
        } else {
            Webtoon webtoon = this.webtoonRepository.findByWebtoonId(Long.valueOf(contentsID));
//            reviews = this.reviewService.findWebtoonReview(Long.valueOf(contentsID));
            paging = this.reviewService.getPaymentsByWebtoon(webtoon, page);
        }

        model.addAttribute("memberInfo", member);
        model.addAttribute("category", category);
        model.addAttribute("contentsID", contentsID);
        model.addAttribute("paging", paging);
        return "review/review_list";
    }

    @PostMapping("/heart/{category}/{contentsID}/{reviewId}/{heartClick}/{gubun}")
    public String reviewList(@PathVariable("category") String category, @PathVariable("contentsID") String contentsID, @PathVariable("reviewId") String reviewId, @PathVariable("heartClick") String heartClick, @PathVariable("gubun") String gubun, Model model, Principal principal) {

        if (Objects.equals(heartClick, "true")) {
            this.heartService.plusReviewHeart(principal, category, contentsID, reviewId);
        } else {
            this.heartService.minusReviewHeart(principal, reviewId);
        }

        if(Objects.equals(gubun, "list")){
            return "redirect:/review/list?category="+category+"&contentsID=" + contentsID;
        }
        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsID + "&reviewId=" + reviewId;
    }



    @GetMapping("/detail")
    public String getreviewDetailList(String category, String contentsID, String reviewId, Model model,Principal principal) {
        Review review = this.reviewService.findReviewById(Long.valueOf(reviewId));
        String providerID = "";
        if(principal != null ) {
            providerID = principal.getName();
        }
        Member member = this.memberService.findByproviderId(providerID);
        if (member == null) {
            member = this.memberService.getMember(providerID);
        }

        if (Objects.equals(category, "movie")) {
            Movie movie = this.movieRepository.findBymovieCode(contentsID);
            model.addAttribute("info",movie);
        } else if (Objects.equals(category, "book")) {
            Book book = this.bookRepository.findByIsbn(contentsID);
            model.addAttribute("info",book);
        } else if (Objects.equals(category, "drama")) {
            Drama drama = this.dramaRepository.getReferenceById(Long.valueOf(contentsID));
            model.addAttribute("info",drama);
        } else  if (Objects.equals(category, "webtoon")) {
            Webtoon webtoon = this.webtoonRepository.findByWebtoonId(Long.valueOf(contentsID));
            StringBuilder updateDaysBuilder = new StringBuilder();

            for (WebtoonDayList updateDay : webtoon.getWebtoonDayLists()) {
                updateDaysBuilder.append(updateDay.getWebtoonDay().getUpdateDays());
            }

            String updateDays = updateDaysBuilder.toString();
            model.addAttribute("updateDays",updateDays);
            model.addAttribute("info",webtoon);
        }

        model.addAttribute("contentsID", contentsID);
        model.addAttribute("memberInfo", member);
        model.addAttribute("category",category);
        model.addAttribute("review", review);
        return "review/review_detail";
    }

    @PostMapping("/update")
    public String updateReview(@RequestParam String category, @RequestParam String contentsID, @RequestParam Long reviewId, @RequestParam String review){
        this.reviewService.updateReview(review, reviewId);
        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsID + "&reviewId=" + reviewId;
    }

    @PostMapping("/delete")
    public String deleteReview(@RequestParam String category, @RequestParam String contentsID, @RequestParam Long reviewId){
        this.reviewService.deleteReview(reviewId);

        return switch (category) {
            case "book" -> "redirect:/book/detail?isbn=" + contentsID;
            case "drama" -> "redirect:/drama/detail?dramaId=" + contentsID;
            case "movie" -> "redirect:/movie/detail?movieCD=" + contentsID;
            default -> "redirect:/webtoon/detail?webtoonId=" + contentsID;
        };
    }


//    @GetMapping("/drama/{id}/review_list")
//    public String showAllReviewForDrama(@PathVariable("id") Long id, @PathVariable("page") int page,
//                                        @PathVariable("size") int size, Model model) {
//        Page<Review> reviews = reviewService.getReviewByDramaIdWithPagination(id, page, size);
//        if (reviews.isEmpty()) {
//            model.addAttribute("errorMessage", "더 이상 리뷰가 없습니다.");
//            return "drama/drama_detail";
//        }
//        List<Review> review = reviewService.getReviewByDramaId(dramaId);
//        model.addAttribute("review", review);
//        return "review/review_list";
//    }
//
//    @GetMapping("/review/delete/{id}")
//    public String deleteDramaReview(@PathVariable("id") Long id) {
//        Long dramaId = reviewService.getDramaIdByReviewId(id);
//        reviewService.deleteReviewById(id);
//        return "redirect:/drama/" + dramaId;
//    }
//
//    @GetMapping("/review/edit/{id}")
//    public String showEditDramaReviewForm(@PathVariable Long id, Model model) {
//        Review review = reviewService.findReviewById(id);
//        model.addAttribute("review", review);
//        return "drama_review/review_edit";
//    }
//
//    @PostMapping("/review/update")
//    public String updateDramaReview(@ModelAttribute Review updateReview) {
//        reviewService.updateReview(updateReview);
//        return "redirect:/review/detail/" + updateReview.getId();
//    }
}
