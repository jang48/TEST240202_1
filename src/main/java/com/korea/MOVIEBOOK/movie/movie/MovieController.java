package com.korea.MOVIEBOOK.movie.movie;

import com.korea.MOVIEBOOK.ContentsService;
import com.korea.MOVIEBOOK.ContentsDTO;
import com.korea.MOVIEBOOK.heart.Heart;
import com.korea.MOVIEBOOK.heart.HeartRepository;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import com.korea.MOVIEBOOK.movie.daily.MovieDailyAPI;
import com.korea.MOVIEBOOK.movie.MovieDTO;
import com.korea.MOVIEBOOK.movie.weekly.MovieWeeklyAPI;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.payment.PaymentRepository;
import com.korea.MOVIEBOOK.review.Review;
import com.korea.MOVIEBOOK.review.ReviewRepository;
import com.korea.MOVIEBOOK.review.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/movie")
public class MovieController {

    private final MovieDailyAPI movieDailyAPI;
    private final MovieWeeklyAPI movieWeeklyAPI;
    private final MovieService movieService;
    private final ReviewService reviewService;
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;
    private final ContentsService contentsService;
    private final HeartRepository heartRepository;
    private final ReviewRepository reviewRepository;
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    String date = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    LocalDateTime weeksago = LocalDateTime.now().minusDays(7);
    String weeks = weeksago.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);

    // LocalDate를 Date로 변환
    Date oneWeekAgoDate = Date.from(oneWeekAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
    String weekInfo = getCurrentWeekOfMonth(oneWeekAgoDate);


    @GetMapping("")
    public String movie(Model model, Principal principal) throws ParseException {

        List<MovieDTO> movieDTOS = this.movieService.listOfMovieDailyDTO();
        List<MovieDTO> movieDTOS2 = this.movieService.listOfMovieWeeklyDTO(weeks);

        if (movieDTOS.isEmpty()) {
            List<Map> failedMovieList = this.movieDailyAPI.movieDaily(date);
            movieDailySize(failedMovieList);
            this.movieService.listOfMovieDailyDTO();
        }

        if (movieDTOS2.isEmpty()) {
            List<Map> failedMovieList2 = this.movieWeeklyAPI.movieWeekly(weeks);
            movieWeeklySize(failedMovieList2);
            this.movieService.listOfMovieWeeklyDTO(weeks);
        }

        movieDTOS = this.movieService.listOfMovieDailyDTO();
        List<List<MovieDTO>> movieListList = new ArrayList<>();

        Integer startIndex = 0;
        Integer endIndex = 5;

        for (int i = 0; i < movieDTOS.size() / 5; i++) {
            movieListList.add(movieDTOS.subList(startIndex, Math.min(endIndex, movieDTOS.size())));
            startIndex += 5;
            endIndex += 5;
        }

        for (List<MovieDTO> movieList : movieListList) {
            movieList.sort(new MovieDTOComparator());
        }

        movieDTOS2 = this.movieService.listOfMovieWeeklyDTO(weeks);
        List<List<MovieDTO>> movieWeeklyListList = new ArrayList<>();

        startIndex = 0;
        endIndex = 5;

        for (int i = 0; i < movieDTOS2.size() / 5; i++) {
            movieWeeklyListList.add(movieDTOS2.subList(startIndex, Math.min(endIndex, movieDTOS2.size())));
            startIndex += 5;
            endIndex += 5;
        }


        List<Object[]> movieRankings = null;
        movieRankings = reviewRepository.findMovieRankings();
        List<Movie> movies = new ArrayList<>();

        if (movieRankings.size() > 9) {
            for (Object[] result : movieRankings) {
                Long movieId = (Long) result[0];
                Movie movie = this.movieService.findMovieById(movieId);
                movies.add(movie);
            }
        }  else {
            movies = this.movieService.getRandomMovies();
        }


        List<List<Movie>> movieReviewListList = new ArrayList<>();

        startIndex = 0;
        endIndex = 5;

        for (int i = 0; i < movies.size() / 5; i++) {
            movieReviewListList.add(movies.subList(startIndex, Math.min(endIndex, movies.size())));
            startIndex += 5;
            endIndex += 5;
        }


        List<List<List<MovieDTO>>> allList = new ArrayList<>();
        allList.add(movieListList);
        allList.add(movieWeeklyListList);
        List<List<List<Movie>>> allList2 = new ArrayList<>();
        allList2.add(movieReviewListList);

        model.addAttribute("movieDailyDate", date);
        model.addAttribute("movieWeeklyDate", weekInfo);
        model.addAttribute("allList", allList);
        model.addAttribute("allList2", allList2);
        model.addAttribute("movieRankings", movieRankings);

        return "movie/movie_list";
    }


    @GetMapping("/detail")
    public String movieDetail(Model model, String movieCD, Principal principal) {

        Movie movie = this.movieService.findMovieByCD(movieCD);
        ContentsDTO contentsDTOS = this.contentsService.setMovieContentsDTO(movie);
        List<Review> reviews = this.reviewService.findReviews(movie.getId()).stream().limit(12).collect(Collectors.toList());
        List<Review> reviewList = this.reviewService.findReviews(movie.getId());
        String paid = "false";

        Integer runtime = Integer.valueOf(movie.getRuntime());
        Integer hour = (int) Math.floor((double) runtime / 60);
        Integer minutes = runtime % 60;
        String movieruntime = String.valueOf(hour) + "시간" + String.valueOf(minutes) + "분";

        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());

        List<List<String>> actorListList = this.movieService.getActorListList(movie);

        double avgRating = reviews.stream() // reviews에서 stream 생성
                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
                .average() // 평점의 평균값 계산
                .orElse(0); // 리뷰가 없을 경우 0.0출력


        model.addAttribute("category", "movie");
        model.addAttribute("contentsDTOS", contentsDTOS);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("author_actor_ListList", actorListList);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        model.addAttribute("movieruntime", movieruntime);


        if (principal != null) {
            String providerID = principal.getName();
            Member member = this.memberService.findByproviderId(providerID);
            if (member == null) {
                member = this.memberService.getMember(providerID);
            }
            Heart heart = this.heartRepository.findByMemberAndMovie(member, movie);

            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "movie", movieCD));
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

//    @GetMapping("/detail/{movieCD}")
//    public String movieDetail2(Model model, @PathVariable("movieCD") String movieCD, Principal principal) {
//        Movie movie = this.movieService.findMovieByCD(movieCD);
//        ContentsDTO contentsDTOS = this.contentsService.setMovieContentsDTO(movie);
//        List<Review> reviews = this.reviewService.findReviews(movie.getId()).stream().limit(12).collect(Collectors.toList());
//        List<Review> reviewList = this.reviewService.findReviews(movie.getId());
//        String paid = "false";
//
//        Integer runtime = Integer.valueOf(movie.getRuntime());
//        Integer hour = (int) Math.floor((double) runtime / 60);
//        Integer minutes = runtime % 60;
//        String movieruntime = String.valueOf(hour) + "시간" + String.valueOf(minutes) + "분";
//
//        List<List<String>> actorListList = this.movieService.getActorListList(movie);
//
//        double avgRating = reviews.stream() // reviews에서 stream 생성
//                .filter(review -> review.getRating() != null) // rating이 null인 review는 제외
//                .mapToDouble(Review::getRating) // 리뷰 객체에서 평점만 추출하여 정수 스트림 생성
//                .average() // 평점의 평균값 계산
//                .orElse(0); // 리뷰가 없을 경우 0.0출력
//
//        Collections.sort(reviews, Comparator.comparing(Review::getDateTime).reversed());
//
//        model.addAttribute("category", "movie");
//        model.addAttribute("contentsDTOS", contentsDTOS);
//        model.addAttribute("reviews", reviews);
//        model.addAttribute("reviewList", reviewList);
//        model.addAttribute("author_actor_ListList", actorListList);
//        model.addAttribute("avgRating", String.format("%.1f", avgRating));
//        model.addAttribute("movieruntime", movieruntime);
//
//
//        if (principal != null) {
//            String providerID = principal.getName();
//            Member member = this.memberService.findByproviderId(providerID);
//            if (member == null) {
//                member = this.memberService.getMember(providerID);
//            }
//
//            Optional<Payment> payment = Optional.ofNullable(this.paymentRepository.findByMemberAndContentsAndContentsID(member, "movie", movieCD));
//            if (payment.isPresent()) {
//                paid = "true";
//            }
//            List<Payment> payments = this.paymentRepository.findBymember(member);
//            long sum = 0;
//
//            for (int i = 0; i < payments.size(); i++) {
//                if (payments.get(i).getContent().contains("충전")) {
//                    sum += Long.valueOf(payments.get(i).getPaidAmount());
//                } else {
//                    sum -= Long.valueOf(payments.get(i).getPaidAmount());
//                }
//            }
//            model.addAttribute("paid", paid);
//            model.addAttribute("login", "true");
//            model.addAttribute("member", member);
//            model.addAttribute("sum", sum);
//        } else {
//            model.addAttribute("paid", paid);
//            model.addAttribute("login", "false");
//            model.addAttribute("member", "");
//            model.addAttribute("sum", "");
//        }
//        return "contents/contents_detail";
//    }

    //
    public void movieDailySize(List<Map> failedMovieList) {
        if (failedMovieList != null && !failedMovieList.isEmpty()) {
            List<Map> failedMoiveList = movieDailyAPI.saveDailyMovieDataByAPI(failedMovieList);
            movieDailySize(failedMoiveList);
        }
    }

    public void movieWeeklySize(List<Map> failedMovieList) throws ParseException {
        if (failedMovieList != null && !failedMovieList.isEmpty()) {
            List<Map> failedMoiveList = movieWeeklyAPI.saveWeeklyMovieDataByAPI(failedMovieList, weeks);
            movieDailySize(failedMoiveList);
        }
    }

    public static String getCurrentWeekOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1; // calendar에서의 월은 0부터 시작
        int day = calendar.get(Calendar.DATE);

        // 한 주의 시작은 월요일이고, 첫 주에 4일이 포함되어있어야 첫 주 취급 (목/금/토/일)
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setMinimalDaysInFirstWeek(4);

        int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);

        // 첫 주에 해당하지 않는 주의 경우 전 달 마지막 주차로 계산
        if (weekOfMonth == 0) {
            calendar.add(Calendar.DATE, -day); // 전 달의 마지막 날 기준
            return getCurrentWeekOfMonth(calendar.getTime());
        }

        // 마지막 주차의 경우
        if (weekOfMonth == calendar.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE)); // 이번 달의 마지막 날
            int lastDaysDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 이번 달 마지막 날의 요일

            // 마지막 날이 월~수 사이이면 다음달 1주차로 계산
            if (lastDaysDayOfWeek >= Calendar.MONDAY && lastDaysDayOfWeek <= Calendar.WEDNESDAY) {
                calendar.add(Calendar.DATE, 1); // 마지막 날 + 1일 => 다음달 1일
                return getCurrentWeekOfMonth(calendar.getTime());
            }
        }

        return month + "월 " + weekOfMonth + "주차";
    }

    class MovieDTOComparator implements Comparator<MovieDTO> {
        @Override
        public int compare(MovieDTO m1, MovieDTO m2) {
            // movieDailyRank 기준으로 정렬
            return m1.getDailyRank().compareTo(m2.getDailyRank());
        }
    }


//    class movieWeeklyRankComparator implements  Comparator<MovieWeekly>{
//        @Override
//        public int compare(MovieWeekly f1, MovieWeekly f2){
//            return f1.getRank().compareTo(f2.getRank());
//        }
//    }


}