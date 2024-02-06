package com.korea.MOVIEBOOK.review;

import com.korea.MOVIEBOOK.drama.Drama;
import com.korea.MOVIEBOOK.drama.DramaService;
import com.korea.MOVIEBOOK.movie.movie.Movie;
import com.korea.MOVIEBOOK.movie.movie.MovieService;
import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.book.BookRepository;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.webtoon.webtoonList.WebtoonService;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final MovieService movieService;
    private final BookRepository bookRepository;
    private final WebtoonService webtoonService;
    private final DramaService dramaService;

    public void saveMovieReview(String movieCD, String comment, Double rating, Member member){

        Movie movie =  this.movieService.findMovieByCD(movieCD);
        Review review = new Review();
        review.setMovie(movie);
        review.setComment(comment);
        review.setCategory("movie");
        review.setRating(rating);
        review.setMember(member);
        review.setDateTime(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public List<Review> findReviews(Long id){
        return this.reviewRepository.findReviewsByMovieId(id);
    }

    public void modifyMovieReview(String movieCD,String comment, Double rating){
        Movie movie =  this.movieService.findMovieByCD(movieCD);
        Review review = new Review();
        review.setMovie(movie);
        review.setComment(comment);
        review.setCategory("movie");
        review.setRating(rating);
        review.setModifyDate(LocalDateTime.now());
        this.reviewRepository.save(review);
    }



    public void saveDramaReview(Long dramaId, String comment, Double rating, Member member){
        Drama drama = this.dramaService.getDramaById(dramaId);
        Review review = new Review();
        review.setDrama(drama);
        review.setComment(comment);
        review.setCategory("drama");
        review.setRating(rating);
        review.setMember(member);
        review.setDateTime(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public void saveBookReview(String isbn, String comment, Double rating, Member member){
        Book book = bookRepository.findByIsbn(isbn);
        Review review = new Review();
        review.setBook(book);
        review.setComment(comment);
        review.setCategory("book");
        review.setRating(rating);
        review.setMember(member);
        review.setDateTime(LocalDateTime.now());
        this.reviewRepository.save(review);
    }



    public void saveWebtoonReview(Long webtoonId, String comment, Double rating, Member member) {
        Webtoon webtoon = this.webtoonService.findWebtoonByWebtoonId(webtoonId);
        Review review = new Review();
        review.setWebtoon(webtoon);
        review.setComment(comment);
        review.setCategory("webtoon");
        review.setRating(rating);
        review.setMember(member);
        review.setDateTime(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public List<Review> findWebtoonReview(Long webtoonId){
        Webtoon webtoon = webtoonService.findWebtoonByWebtoonId(webtoonId);
//        WebtoonService.findById();
        List<Review> reviews = this.reviewRepository.findReviewsByWebtoonId(webtoon.getId());
        return reviews;
    }

    public Long getReivewCount(Member member) {
        return reviewRepository.countByMember(member);
    }


    public List<Review> getReviewByBookId(Long BookId) {
        return reviewRepository.findReviewsByBookId(BookId);
    }
    public List<Review> getReviewByDramaId(Long dramaId) {
        return reviewRepository.findByDramaId(dramaId);
    }
    public Review findReviewById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public List<Review> getAllReview() {
        return reviewRepository.findAll();
    }



    public Page<Review> getPaymentsByMovie(Movie movie, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10,Sort.by(sorts));
        return this.reviewRepository.findByMovie(movie, pageable);
    }
    public Page<Review> getPaymentsByBook(Book book, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10,Sort.by(sorts));
        return this.reviewRepository.findByBook(book, pageable);
    }

    public Page<Review> getPaymentsByDrama(Drama drama, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10,Sort.by(sorts));
        return this.reviewRepository.findByDrama(drama, pageable);
    }

    public Page<Review> getPaymentsByWebtoon(Webtoon webtoon, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("dateTime"));
        Pageable pageable = PageRequest.of(page, 10,Sort.by(sorts));
        return this.reviewRepository.findByWebtoon(webtoon, pageable);
    }





    public Long getDramaIdByReviewId(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다.")); // 해당 ID의 리뷰를 찾습니다.
        return review.getDrama().getId(); // 리뷰가 속한 드라마의 ID를 반환합니다.
    }

    public void updateReview(Review updateReview) {
        Review existingReview = reviewRepository.findById(updateReview.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid review Id:" + updateReview.getId()));
        existingReview.setComment(updateReview.getComment());
        existingReview.setRating(updateReview.getRating());
        reviewRepository.save(existingReview);
    }
    public void updateReview(String comment, Long reviewId){
        Review review = findReviewById(reviewId);
        review.setComment(comment);
        review.setDateTime(LocalDateTime.now());
        this.reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId){
        Review review = this.reviewRepository.findById(reviewId).get();
        this.reviewRepository.delete(review);
    }
}
