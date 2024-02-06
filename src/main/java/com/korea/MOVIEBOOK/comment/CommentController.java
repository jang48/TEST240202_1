package com.korea.MOVIEBOOK.comment;

import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final MemberService memberService;

    @GetMapping("/save")
    public String saveComment (@RequestParam String category, @RequestParam String contentsId, @RequestParam Long reviewId, @RequestParam Long memberId, @RequestParam String comment){

        this.commentService.saveComment(comment,memberId,reviewId);

        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsId + "&reviewId=" + reviewId;
    }

    @PostMapping("/update")
    public String updateComment(@RequestParam String category, @RequestParam String contentsID, @RequestParam Long reviewId, @RequestParam Long commentId, @RequestParam String comment){
        this.commentService.updateComment(comment, commentId);
        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsID + "&reviewId=" + reviewId;
    }

    @PostMapping("/delete")
    public String deleteComment(@RequestParam String category, @RequestParam String contentsID, @RequestParam Long reviewId, @RequestParam Long commentId){
        this.commentService.deleteComment(commentId);
        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsID + "&reviewId=" + reviewId;
    }

//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/delete/{id}")
//    public String commentDelete(Principal principal, Model model,  @RequestParam String category, @RequestParam String contentsId, @RequestParam Long reviewId, @PathVariable("id") Long commentid) {
//        Comment comment = this.commentService.getComment(commentid);
//        Member member = memberService.findByusername(principal.getName());
//        if (!comment.getMember().getUsername().equals(principal.getName())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
//        }
//        if (member == null) {
//            member = memberService.findByproviderId(principal.getName());
//        }
//
//        this.commentService.deleteComment(comment);
//        model.addAttribute("member", member);
//        return "redirect:/review/detail?category="+category+"&contentsID=" + contentsId + "&reviewId=" + reviewId;
//    }

}
