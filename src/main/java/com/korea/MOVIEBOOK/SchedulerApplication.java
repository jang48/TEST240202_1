package com.korea.MOVIEBOOK;

import com.korea.MOVIEBOOK.book.Book;
import com.korea.MOVIEBOOK.book.BookService;
import com.korea.MOVIEBOOK.drama.Drama;
import com.korea.MOVIEBOOK.drama.DramaRepository;
import com.korea.MOVIEBOOK.movie.MovieDTO;
import com.korea.MOVIEBOOK.movie.daily.MovieDailyAPI;
import com.korea.MOVIEBOOK.movie.movie.MovieService;
import com.korea.MOVIEBOOK.movie.weekly.MovieWeeklyAPI;
import com.korea.MOVIEBOOK.webtoon.days.Day;
import com.korea.MOVIEBOOK.webtoon.days.DayService;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayList;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayListService;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import com.korea.MOVIEBOOK.webtoon.webtoonList.WebtoonService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableScheduling // 추가
@SpringBootApplication
@RequiredArgsConstructor
public class SchedulerApplication {

    private final MovieDailyAPI movieDailyAPI;
    private final MovieService movieService;
    private final MovieWeeklyAPI movieWeeklyAPI;
    private final BookService bookService;
    private final WebtoonService webtoonService;
    private final DayService dayService;
    private final WebtoonDayListService webtoonDayListService;
    private final DramaRepository dramaRepository;
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    String date = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    LocalDateTime weeksago = LocalDateTime.now().minusDays(7);
    String weeks = weeksago.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    @Scheduled(cron = "0 09 02 * * *") // 매일 오후 18시에 실행
    public void run() throws ParseException {
        webtoonData();
        bookData();
        movieData();
        Optional<Drama> dramaOptional = dramaRepository.findById(1L);
        System.out.println(dramaOptional);
        if (dramaOptional.isEmpty()) {
            dramaData();
        }
    }

    public void bookData() {
        List<Book> bestSellerList = bookService.getBestSellerList();
        bestSellerList.sort(Comparator.comparing(Book::getBestRank));
    }

    public void movieData() throws ParseException {

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
    }

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

    public void webtoonData() {
        List<Day> days = this.dayService.findAll();
        List<List<List<Webtoon>>> allList = new ArrayList<>();//  월,화,수,목,금,토,일이라는 값을 가져오기 위함
        List<Webtoon> webtoonList = new ArrayList<>();

        for (Day day1 : days) {
            List<WebtoonDayList> webtoonDayLists = webtoonDayListService.findBywebtoonDay(day1);
            if (webtoonDayLists.isEmpty()) {
                List<Long> webtoon = webtoonService.getWebtoonAPI(day1.getUpdateDays());
                webtoonDayListService.SaveWebtoonDayList(day1.getId(), webtoon);
            }
        }
    }

    public void dramaData() {
        String url = "jdbc:mysql://localhost:3306/movieboovie2";
        String user = "root";
        String password = "";
        try {
            Connection myConn = DriverManager.getConnection(url, user, password);

            Statement mystmt = myConn.createStatement();

            String sql = "INSERT INTO drama"
                    + "(actor, company, director, genre, image_url, plot, rank_num, rating, release_date, title, viewing_rating)"
                    + " VALUES "
                    + "('박서준(), 한소희(), 수현(), 김해숙(), 조한철(), 위하준()', '글앤그림미디어, 카카오엔터테인먼트, 스튜디오드래곤', '정동윤', '공포/스릴러', '/1.jpg', '시대의 어둠이 가장 짙었던 1945년 봄, 생존이 전부였던 두 청춘이 탐욕 위에 탄생한 괴물과 맞서는 이야기', 1, 0.0, '2023', '경성크리처', '15세 이상 관람가'),"
                    + "('임시완(), 이선빈(), 이시우(), 강혜원()', '더스튜디오엠', '이명우', '청춘/코미디', '/2.jpg', '1989년 충청남도, 안 맞고 사는 게 일생 일대의 목표인 온양 찌질이 병태가 하루아침에 부여 짱으로 둔갑하면서 벌어지는 이야기', 2, 0.0, '2023', '소년시대', '청소년 관람불가'),"
                    + "('서인국(), 박소담()', 'SLL, 스튜디오N, 사람엔터테인먼트', '하병훈', '판타지/환생/액션', '/3.jpg', '지옥으로 떨어지기 직전의 이재가 12번의 죽음과 삶을 경험하게 되는 인생 환승 드라마', 3, 0.0, '2023', '이재, 곧 죽습니다', '청소년 관람불가'),"
                    + "('송강(), 이진욱(), 이시영(), 고민시(), 진영()', '스튜디오드래곤, 스튜디오N', '이응복, 박소현','공포/스릴러', '/4.jpg', '욕망이 괴물이 되는 세상. 그린홈을 떠나 새로운 터전에서 살아남기 위해 각자의 사투를 벌이는 현수와 그린홈의 생존자들, 그리고 또 다른 존재의 등장과 알 수 없는 미스터리한 현상들까지. 새로운 욕망과 사건, 사투를 그린 넷플릭스 시리즈', 4, 0.0, '2023', '스위트홈 시즌2', '청소년 관람불가'),"
                    + "('이재인(), 김우석(), 최예빈(), 차우민(), 안지호(), 정소리()', '이오콘텐츠그룹, 이오엔터테인먼트, STUDIO X+U','임대웅', '공포/미스터리', '/5.jpg', '종료가 불가능한 의문의 마피아 게임에 강제로 참여하게 된 유일고 2학년 3반의 하이틴 미스터리 스릴러', 5, 0.0, '2023', '밤이 되었습니다', '청소년 관람불가'),"
                    + "('이성민(), 유연석(), 이정은()', '스튜디오드래곤, 더그레이트쇼, 스튜디오N', '필감성', '스릴러/범죄', '/6.jpg', '평범한 택시기사 오택(이성민)이 고액을 제시하는 지방행 손님(유연석)을 태우고 가다 그가 연쇄살인마임을 깨닫게 되면서 공포의 주행을 시작하게 되는 이야기', 6, 0.0, '2023', '운수 오진 날', '청소년 관람불가'),"
                    + "('박보영(), 연우진(), 장동윤(), 이정은()', '필름몬스터', '이재규, 김남수', '드라마/일상', '/7.jpg', '정신건강의학과 근무를 처음 하게 된 간호사 다은이 정신병동 안에서 만나는 세상과 마음 시린 사람들의 다양한 이야기를 그린 넷플릭스 시리즈', 7, 0.0, '2023', '정신병동에도 아침이 와요', '15세 이상 관람가'),"
                    + "('지창욱(), 신혜선()', 'MI, SLL', '차영훈, 김형준', '로맨틱 코미디', '/8.jpg', '한라산 자락 어느 개천에서 난 용 같은 삼달이 어느 날 모든 걸 잃고 곤두박질치며 추락한 뒤, 개천을 소중히 지켜온 용필과 고향의 품으로 다시 돌아와 숨을 고르는 이야기, 그리고 다시 사랑을 찾는 이야기', 8, 0.0, '2023', '웰컴투 삼달리', '15세 이상 관람가'),"
                    + "('장동윤(), 이주명()', '에이스토리', '김진우', '로맨틱 코미디', '/9.jpg', '20년째 떡잎인 씨름 신동 김백두와 소싯적 골목대장 오유경이 다시 만나며 벌어지는 청춘 성장 로맨스', 9, 0.0, '2023', '모래에도 꽃이 핀다', '15세 이상 관람가'),"
                    + "('남주혁(), 유지태(), 이준혁(), 김소진()', '스튜디오N', '최정열', '다크 히어로/범죄', '/10.jpg', '낮에는 법을 수호하는 모범 경찰대생이지만, 밤이면 법망을 피한 범죄자들을 직접 심판하는 ‘비질란테’로 살아가는 김지용과 그를 둘러싸고 각기 다른 목적을 가진 사람들의 이야기를 그린 액션 스릴러', 10, 0.0, '2023', '비질란테', '청소년 관람불가'),"

                    + "('스티븐 연(), 앨리 웡(), 조지프 리()', 'A24', '이성진', '드라마, 블랙 코미디', '/11.jpg', '일이 잘 풀리지 않는 도급업자와 삶이 만족스럽지 않은 사업가. 두 사람 사이에서 난폭 운전 사건이 벌어지면서 내면의 어두운 분노를 자극하는 갈등이 촉발된다.', 11, 0.0, '2023', '성난사람들', '15세 이상 관람가'),"
                    + "('워커 스코벨(), 리아 제프리스(), 아리안 심하드리()', '20세기 텔레비전, 고담 그룹', '제임스 보빈', '어반 판타지, 액션, 어드벤처, 드라마', '/12.jpg', '괴물들과 상대하고 신들을 따돌리며 미국 곳곳을 탐험하는 것도 모자라 제우스의 번개 화살을 되찾고 전쟁을 저지해야하는 위험천만한 모험이 퍼시를 기다린다. 그는 모험을 함께하는 동료 아나베스, 그로버의 도움으로 평생을 짓누르던 의문, 이 세상에 어떻게 적응할 것이고, 그의 운명은 무엇인지에 대한 답을 찾아간다.', 12, 0.0, '2023', '퍼시 잭슨과 올림포스의 신들', '12세 이상 관람가'),"
                    + "('잭 퀘이드(), 칼 어번(), 안토니 스타(), 에린 모리아티()', '보우트 스튜디오', '에릭 크립키', '슈퍼히어로, 액션, 스릴러, 블랙 코미디', '/13.jpg', '어느 자경단원들이 능력을 남용하는 부패한 슈퍼히어로들을 잡으러 다니기로 한다.', 13, 0.0, '2019', '더 보이즈 시즌1', '청소년 관람불가'),"
                    + "('제레미 앨런 화이트(), 에번 모스배크랙(), 아요 에데비리(), 라이오넬 보이스(), 리자 콜론-자야스(), 애비 엘리엇()', 'FX 프로덕션', '크리스토퍼 스토러', '드라마, 코미디', '/14.jpg', '망해가는 식당을 살려라. 죽은 형이 남긴 싸구려 샌드위치 가게를 운영하러 시카고로 돌아온 파인다이닝계의 유명 셰프 카르멘. 어떻게 해서든 이곳을 바꿔보려 하지만, 주방은 엉망진창이고 직원들은 다들 제멋대로다. 그럼에도 불구하고 카르멘이 있어야 할 곳은 바로 이곳. 그는 다시 앞치마를 질끈 동여매고 혼돈의 주방으로 뛰어든다.', 14, 0.0, '2022', '더 베어 시즌1', '15세 이상 관람가'),"
                    + "('오스틴 버틀러(), 배리 키오건(), 칼럼 터너(), 앤서니 보일()', 'Apple Studio, 앰블린 텔레비전, 플레이톤, 팔리먼트 오브 올스', '애나 보든, 라이언 플렉, 팀 밴 패튼, 디 리스, 캐리 후쿠나가', '밀리터리, 전쟁', '/15.jpg', '밴드 오브 브라더스와 퍼시픽의 제작진, 스티븐 스필버그, 톰 행크스, 게리 고츠만 작품. 제2차 세계 대전 중, 용기, 죽음, 그리고 승리로 단단히 구축된 형제애를 바탕으로 공군 병사들이 제100폭격전대와 함께 목숨을 거는 대서사시.', 15, 0.0, '2024', '마스터스 오브 디 에어', '15세 이상 관람가'),"
                    + "('양자경() , 저스틴 첸(), 샘 송 리()', '레전더리 엔터테인먼트', '캐빈 탠캐런, 비엣 응우엔', '블래 코미디, 액션, 드라마', '/16.jpg', '정체 모를 적이 가족을 노린다. 타이베이 삼합회 일원인 남자는 로스앤젤레스로 향하는데. 그곳에 사는 완고한 어머니와 아무것도 모르는 남동생을 지켜야 하기 때문이다.', 16, 0.0, '2024', '선 브라더스' , '15세 이상 관람가'),"
                    + "('데버라 아요린드(), 애슐리 토머스(), 앨리슨 필()', '아마존 스튜디오', '넬슨 크래그', '드라마, 공포, 서스펜스', '/17.jpg', '그들은 미국을 배경으로 한 공포 시리즈다. 첫 번째 시즌은 1950년대, 소위 흑인 대이동 시기를 배경으로 노스캐롤라이나주에서 백인 동네인 로스앤젤레스로 이사한 흑인 가족이 주인공이다. 안락해야 할 집은 그들을 비웃으며 헐뜯고 파괴하려 하는 이웃과 알 수 없는 존재의 악의적인 표적이 된다.', 17, 0.0, '2021', '그들', '15세 이상 관람가'),"
                    + "('알라콰 콕스(), 빈센트 도노프리오()', '마블 스튜디오, 20세기 텔레비전', '케빈 파이기', '슈퍼히어로', '/18.jpg', '마블 스튜디오가 제공하는 에코는 윌슨 피스크의 범죄 제국에 쫓기는 마야 로페즈를 조명한다. 여정을 끝내고 집으로 돌아온 마야는 자신의 가족과 유산을 대면해야만 한다.', 18, 0.0, '2024', '에코' , '15세 이상 관람가'),"
                    + "('맨디 패틴킨(), 바이올렛 빈(), 린다 에몬드(), 제이인 앳킨슨(), 데이빗 마샬 그랜트(), 라울 콜리(), 애니 Q 리겔()', 'Hulu', '마크 웹', '범죄/미스터리/드라마', '/19.jpg', '글로벌 상류층의 화려함 속에서 펼쳐지는 <죽음을 둘러싼 작은 것들>은 뛰어난 재능과 끝없는 호기심을 가진 이모진 스콧(바이올렛 빈)을 중심으로 이야기를 풀어낸다. 이모진은 자신이 초래한 운명의 장난으로 밀실에서 벌어진 살인 사건의 주요 용의자로 지목된다.', 19, 0.0, '2024', '죽음을 둘러싼 작은것들', '청소년 관람불가'),"
                    + "('파블로 슈라이버(), 나타샤 매컬혼(), 샤바나 아즈미(), 하예린(), 젠 테일러(), 올리브 그레이, 대니 사파니', '앰블린 텔레비전, 343 인더스트리, 쇼타임, 원 빅 픽처스, 챕터 11', '스티븐 스필버그', '액션/모험/SF/스릴러/전쟁', '/20.jpg', '인류가 만들어낸 최강의 전사 ‘마스터 치프. 외계 종족으로부터 인류를 지켜내기 위해선 자신의 과거를 마주해야 한다. 이제껏 본적 없는 압도적인 스케일, 액션 블록버스터 헤일로', 20, 0.0, '2022', '헤일로 시즌 1', '12세 이상 관람가');";


            mystmt.executeUpdate(sql);
            System.out.println("Insert Complete");
            myConn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
