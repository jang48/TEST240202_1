package com.korea.MOVIEBOOK.comment;

import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberRepository;
import com.korea.MOVIEBOOK.review.Review;
import com.korea.MOVIEBOOK.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    public void saveComment(String comment, Long memberId, Long reviewId){
        Member member = this.memberRepository.findById(memberId).get();
        Review review = this.reviewRepository.findById(reviewId).get();

        Comment comment1 = new Comment();
        comment1.setContent(comment);
        comment1.setMember(member);
        comment1.setReview(review);
        comment1.setDateTime(LocalDateTime.now());
        this.commentRepository.save(comment1);
    }

    public void updateComment(String comment, Long commentId){
        Comment comment1 = this.commentRepository.findById(commentId).get();
        comment1.setContent(comment);
        comment1.setDateTime(LocalDateTime.now());
        this.commentRepository.save(comment1);
    }

    public void deleteComment(Long commentId){
        Comment comment1 = this.commentRepository.findById(commentId).get();
        this.commentRepository.delete(comment1);
    }


    public Comment getComment(Long commentid) {
        Optional<Comment> commentOptional = this.commentRepository.findById(commentid);
        if(commentOptional.isEmpty()){
            return null;
        }
        return commentOptional.get();
    }

//    public void deleteComment(Comment comment) {
//        this.commentRepository.delete(comment);
//    }
}
