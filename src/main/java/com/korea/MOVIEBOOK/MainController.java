package com.korea.MOVIEBOOK;
import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.book.BookDTO;
import com.korea.MOVIEBOOK.book.BookService;
import com.korea.MOVIEBOOK.drama.Drama;
import com.korea.MOVIEBOOK.drama.DramaService;
import com.korea.MOVIEBOOK.movie.MovieDTO;
import com.korea.MOVIEBOOK.movie.daily.MovieDailyAPI;
import com.korea.MOVIEBOOK.movie.movie.Movie;
import com.korea.MOVIEBOOK.movie.movie.MovieService;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.webtoon.webtoonList.WebtoonService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final MovieDailyAPI movieDailyAPI;
    private final BookService bookService;
    private final DramaService dramaService;
    private final MovieService movieService;
    private final WebtoonService webtoonService;
    private final ContentsService contentsService;

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    String date = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    @GetMapping(value = "/test", produces = "application/json")
    @ResponseBody
    public Movie aaaa(Model model, @RequestParam("input") Long movieid)  {

       Movie movie = this.movieService.getMovieUseId(movieid);

       return movie;
    }

    @GetMapping("/search")
    public String searchList(Model model,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "kw", defaultValue = "") String kw){


        Page<Movie> pagingMovie = movieService.getMovieList(page, kw);
        Page<Drama> pagingDrama = dramaService.getDramaList(page, kw);
        Page<Book> pagingBook = bookService.getBookList(page, kw);
        Page<Webtoon> pagingWebtoon = webtoonService.getWebtoonList(page, kw);

        model.addAttribute("pagingWebtoon", pagingWebtoon);
        model.addAttribute("pagingmovie", pagingMovie);
        model.addAttribute("pagingDrama", pagingDrama);
        model.addAttribute("pagingBook", pagingBook);
        model.addAttribute("kw", kw);

        return "search_list";
    }

    @GetMapping("/search/webtoon")
    public String searchWebtoonList(Model model,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "kw", defaultValue = "") String kw){

        Page<Webtoon> pagingWebtoon = webtoonService.getWebtoonList(page, kw);

        model.addAttribute("pagingWebtoon", pagingWebtoon);
        model.addAttribute("kw", kw);
        return "webtoon_search_list";
    }

    @GetMapping("/")
    public String mainPage(Model model) {

        List<ContentsDTO> contentsDTOList = new ArrayList<>();
        List<Book> bestSellerList = bookService.getBestSellerList();
        List<MovieDTO> boxofficeList = movieService.listOfMovieDailyDTO();
        if (boxofficeList.isEmpty()) {
            List<Map> failedMovieList = movieDailyAPI.movieDaily(date);
            movieDailySize(failedMovieList);
            movieService.listOfMovieDailyDTO();
        }
        boxofficeList = movieService.listOfMovieDailyDTO();

        List<Book> bookList = bestSellerList.subList(0, Math.min(5, bestSellerList.size()));
        List<MovieDTO> movieList = boxofficeList.subList(0, Math.min(5, boxofficeList.size()));
        model.addAttribute("bookList", bookList);
        model.addAttribute("movieList", movieList);

        return "mainPage";
    }

    public void movieDailySize(List<Map> failedMovieList) {
        if (failedMovieList != null && !failedMovieList.isEmpty()) {
            List<Map> failedMoiveList = movieDailyAPI.saveDailyMovieDataByAPI(failedMovieList);
            movieDailySize(failedMoiveList);
        }
    }



}

