package com.korea.MOVIEBOOK.movie.movie;
import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.movie.MovieDTO;
import com.korea.MOVIEBOOK.movie.daily.MovieDaily;
import com.korea.MOVIEBOOK.movie.daily.MovieDailyRepository;
import com.korea.MOVIEBOOK.movie.weekly.MovieWeekly;
import com.korea.MOVIEBOOK.movie.weekly.MovieWeeklyRepository;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieService {

    final private MovieRepository movieRepository;
    final private MovieDailyRepository movieDailyRepository;
    final private MovieWeeklyRepository movieWeeklyRepository;
    String dateString = "";
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    String date = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    public void findMovieList(String movieCD,String movieNm, String actorText, String runtime, String genre, String releaseDate, String viewingRating, String director, String nations){
        Movie movie = this.movieRepository.findByTitleAndNationsAndReleaseDate(movieNm, nations, releaseDate);
        if(movie == null){
            addDetail(movieCD, movieNm, actorText, runtime, genre, releaseDate, viewingRating, director, nations);
        }
    }
    public void addDetail(String movieCD, String movieNm, String actorText, String runtime, String genre, String releaseDate, String viewingRating, String director, String nations) {
        Movie movie = new Movie();
        movie.setActor(actorText);
        movie.setRuntime(runtime);
        movie.setGenre(genre);
        movie.setReleaseDate(releaseDate);
        movie.setViewingRating(viewingRating);
        movie.setDirector(director);
        movie.setTitle(movieNm);
        movie.setNations(nations);
        movie.setMovieCode(movieCD);
        this.movieRepository.save(movie);
    }

    public void addKmdb(String plot, String company, String imageUrl, String title) {
        String plotcontent = plot.replaceAll("!HS", "").replaceAll("!HE", "").replaceAll("\\s+", "");

        Movie movie = this.movieRepository.findByTitle(title);
        movie.setPlot(plotcontent);
        movie.setCompany(company);
        movie.setImageUrl(imageUrl);
        this.movieRepository.save(movie);
    }
    public void add(String title, Long audiAcc) {
        Movie movie = findMovie(title);
        movie.setAudiAcc(audiAcc);
        this.movieRepository.save(movie);
    }

    public Movie findMovieById(Long id){
        return this.movieRepository.findById(id).get();
    }

    public Movie findMovie(String title){
        return this.movieRepository.findByTitle(title);
    }


    public Movie findMovieByCD(String movieCode){
        return this.movieRepository.findBymovieCode(movieCode);
    }

    public String weeklydate(String date) throws ParseException {

        dateString = date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date dateparse = sdf.parse(dateString);

        // Calendar 객체를 사용하여 주차 계산
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateparse);

        String weekNumber = String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));

        return weekNumber;
    }

    public  List<MovieDTO> listOfMovieDailyDTO()
    {
        List<MovieDaily> movieDailyList = this.movieDailyRepository.findBydate(date);
        return setMovieDTO(movieDailyList);
    }
    public List<MovieDTO> setMovieDTO(List<MovieDaily> movieDaily){
        List<MovieDTO> movieDTOS = new ArrayList<>();
        for(MovieDaily movieDaily1 : movieDaily)
        {
            MovieDTO movieDTO= MovieDTO.builder()
                    .movieCode(movieDaily1.getMovie().getMovieCode())
                    .dailyRank(movieDaily1.getRankNum())
                    .date(movieDaily1.getDate())
                    .title(movieDaily1.getMovie().getTitle())
                    .director(movieDaily1.getMovie().getDirector())
                    .actor(movieDaily1.getMovie().getActor())
                    .runtime(movieDaily1.getMovie().getRuntime())
                    .plot(movieDaily1.getMovie().getPlot())
                    .genre(movieDaily1.getMovie().getGenre())
                    .releaseDate(movieDaily1.getMovie().getReleaseDate())
                    .company(movieDaily1.getMovie().getCompany())
                    .nations(movieDaily1.getMovie().getNations())
                    .audiAcc(movieDaily1.getMovie().getAudiAcc())
                    .viewingRating(movieDaily1.getMovie().getViewingRating())
                    .imageUrl(movieDaily1.getMovie().getImageUrl())
                    .build();
            movieDTOS.add(movieDTO);
        }
        return movieDTOS;
    }

    public  List<MovieDTO> listOfMovieWeeklyDTO(String weeks) throws ParseException {
        String week = weeklydate(weeks);
        String year = weeks.substring(0,4);
        List<MovieWeekly> movieWeeklyList = this.movieWeeklyRepository.findByYearAndWeek(year,week);
        return setMovieWeeklyDTO(movieWeeklyList);
    }
    public List<MovieDTO> setMovieWeeklyDTO(List<MovieWeekly> movieWeeklies){
        List<MovieDTO> movieDTOS2 = new ArrayList<>();
        for(MovieWeekly movieWeekly : movieWeeklies)
        {
            MovieDTO movieDTO= MovieDTO.builder()
                    .movieCode(movieWeekly.getMovie().getMovieCode())
                    .weeklyRank(movieWeekly.getRankNum())
                    .year(movieWeekly.getYear())
                    .week(movieWeekly.getWeek())
                    .title(movieWeekly.getMovie().getTitle())
                    .director(movieWeekly.getMovie().getDirector())
                    .actor(movieWeekly.getMovie().getActor())
                    .runtime(movieWeekly.getMovie().getRuntime())
                    .plot(movieWeekly.getMovie().getPlot())
                    .genre(movieWeekly.getMovie().getGenre())
                    .releaseDate(movieWeekly.getMovie().getReleaseDate())
                    .company(movieWeekly.getMovie().getCompany())
                    .nations(movieWeekly.getMovie().getNations())
                    .audiAcc(movieWeekly.getMovie().getAudiAcc())
                    .viewingRating(movieWeekly.getMovie().getViewingRating())
                    .imageUrl(movieWeekly.getMovie().getImageUrl())
                    .build();
            movieDTOS2.add(movieDTO);
        }
        return movieDTOS2;
    }

    public void test(MovieDaily movieDaily, String title){
        Movie movie = this.movieRepository.findByTitle(title);
        movie.setMoviedaily(movieDaily);
        this.movieRepository.save(movie);
    }

    public void setMovieWeeklyID(MovieWeekly movieWeekly, String movieCd){
        Movie movie = this.movieRepository.findBymovieCode(movieCd);
        movie.setMovieweekly(movieWeekly);
        this.movieRepository.save(movie);
    }
    public List<List<String>> getActorListList(Movie movie) {

        String director = "";
        if(!movie.getDirector().isEmpty()) {
            director = movie.getDirector();
        }
        String actor = movie.getActor();
        String[] actors = new String[]{};
        if(!actor.isEmpty()) {
            actors = actor.split(",");
        }

        List<String> actorName = new ArrayList<>();
        List<String> actorRole = new ArrayList<>();

        for (String actorList : actors) {
            int idx = actorList.indexOf('(');
            if (idx != -1) {
                actorName.add(actorList.substring(0, idx).trim());
                actorRole.add(actorList.substring(idx).trim());
            }
        }

        List<String> actorList = new ArrayList<>();
        for(int i = 0; i < actorName.size(); i++){
            actorList.add(actorName.get(i));
            actorList.add(actorRole.get(i));
        }

        actorList.add(director);
        actorList.add("(감독)");

        List<List<String>> actorListList = new ArrayList<>();
        int chunkSize = 16;
        int totalElements = actorList.size();

        for (int i = 0; i < (totalElements + chunkSize - 1) / chunkSize; i++) {
            int start = i * chunkSize;
            int end = Math.min((i + 1) * chunkSize, totalElements);
            actorListList.add(actorList.subList(start, end));
        }
        return actorListList;
    }


    public Page<Movie> getMovieList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("title"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));

        return movieRepository.findAllByMovieKeyword(kw, pageable);
    }


    public List<Movie> getRandomMovies() {
        List<Movie> allMovies = this.movieRepository.findAll();

        List<Movie> randomMovies = getRandomElements(allMovies, 10);

        return randomMovies;
    }

    private <T> List<T> getRandomElements(List<T> list, int count) {
        Collections.shuffle(list);
        return list.subList(0, Math.min(count, list.size()));
    }

//    public Page<Movie> moviePurchasePageForMember(Long paymentId, int page) {
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("dateTime")); // dateTime을 올바르게 사용하는지 확인해주세요.
//        Pageable pageable = PageRequest.of(page, 8, Sort.by(sorts));
//
//        return movieRepository.findByMemberId(paymentId, pageable);
//    }

    public Movie getMovieUseId(Long id) {

        Movie movie = this.movieRepository.getById(id);

        return movie;
    }
}


