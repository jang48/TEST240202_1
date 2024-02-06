package com.korea.MOVIEBOOK.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korea.MOVIEBOOK.webtoon.webtoonList.Webtoon;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;

    private void getAPI(String url, String command, Boolean isNew, Boolean recommend) {
        //  기본url과 명령어를 넣으면 api를 사용해서 이미 db에있는지 체크하고 없다면 db에 저장해주는 메서드.
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url + command).build();

            ResponseEntity<Map> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Map.class);

            ArrayList<Map<String, Object>> bookList = (ArrayList<Map<String, Object>>) resultMap.getBody().get("item");
            for (Map<String, Object> bookData : bookList) {
                if (checkDuplicate((String) bookData.get("isbn"))) {
                    saveBook(bookData, isNew, recommend);
                    System.out.println("============================= 책 추가됨 =============================");
                } else if (!checkDuplicate((String) bookData.get("isbn")) &&
                        !bookRepository.findByIsbn((String) bookData.get("isbn")).getAddDate().equals(LocalDate.now())) {
                    updateBook(bookData, isNew, recommend);
                    System.out.println("============================= 책 업데이트 됨 =============================");
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // 예외 처리
            e.printStackTrace();
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
        }
    }

    public void getBook(String isbn) {
        String url = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx?ttbkey=ttbdlrjsrn81027001&itemIdType=ISBN&ItemId=";
        String command = isbn + "&output=JS&Cover=Big&Version=20131101";
        getAPI(url, command, false, true);
    }
    public void getBestSeller() {
        String url = "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=ttbdlrjsrn81027001";
        String command = "&QueryType=Bestseller&MaxResults=25&start=1&Cover=Big&SearchTarget=Book&output=JS&Version=20131101";
        getAPI(url, command, false, false);
        System.out.println("======================== 베스트셀러도서추가 ========================");
    }
    public void getNewSpecialBook() {
        String url = "http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=ttbdlrjsrn81027001";
        String command = "&QueryType=ItemNewSpecial&MaxResults=25&start=1&Cover=Big&SearchTarget=Book&output=JS&Version=20131101";
        getAPI(url, command, true, false);
        System.out.println("======================== 주목할만한신간도서추가 ========================");
    }

    public List<Book> getBestSellerList() {
        //  db에 있는 오늘 추가된 책들 중 bestSeller들만 추려서 List를 리턴하는 함수
        List<Book> bookList = bookRepository.findByAddDate(LocalDate.now());
        if (bookList.isEmpty()) {
            getBestSeller();
        }
        bookList = bookRepository.findByAddDate(LocalDate.now());
        List<Book> bestSellerList = new ArrayList<>();
        for (Book book : bookList) {
            if (book.getBestRank() != null) {
                bestSellerList.add(book);
            }
        }
        return bestSellerList;
    }


//    여기서부터 수정해야함 여기 bookList를 만드는데 AddDate로해서 신간도서를 추가안하게됨.
    public List<Book> getNewSpecialBookList() {
        //  db에 있는 책들중 NewSpecialBook 들만 추려서 List를 리턴하는 함수
        List<Book> newSpecialBookList = bookRepository.findByIsNewBookAndAddDate(true, LocalDate.now());
        if (newSpecialBookList.isEmpty()) {
            getNewSpecialBook();
        }
        newSpecialBookList = bookRepository.findByIsNewBookAndAddDate(true, LocalDate.now());
        return newSpecialBookList;
    }

    public List<Book> getRecommendationList() {
        List<Book> recommendationList = bookRepository.findByRecommended(true);
        if (recommendationList.isEmpty()) {
            getBook("9791197182204");
            getBook("9788954614344");
            getBook("9788960172586");
            getBook("9791186757093");
            getBook("9788954608640");
            getBook("9788998274931");
            getBook("9788956604992");
            getBook("9788990982575");
            getBook("9788925576459");
            getBook("9788932916378");
        }
        recommendationList = bookRepository.findByRecommended(true);
        return recommendationList;
    }


    private Boolean checkDuplicate(String isbn) {
        //  이미 DB에 저장되어있는 책인지 확인하는 함수
        List<Book> bookList = bookRepository.findAll();
        boolean answer = true;
        for (Book book : bookList) {
            if (book.getIsbn().equals(isbn)) {
                answer = false;
                break;
            }
        }
        return answer;
    }
    private void saveBook(Map<String, Object> bookData, Boolean isNew, Boolean recommend) {
        //  book정보를 db에 저장하는 함수
        String plot = (String) bookData.get("description");
        String replacedText = plot.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

        Book book = new Book();
        book.setPlot(replacedText);
        book.setTitle((String) bookData.get("title"));
        book.setAuthor((String) bookData.get("author"));
        book.setIsbn((String) bookData.get("isbn"));
        book.setIsbn13((String) bookData.get("isbn13"));
        book.setImageUrl((String) bookData.get("cover"));
        book.setPublisher((String) bookData.get("publisher"));
        book.setPricestandard((Integer) bookData.get("priceStandard"));
        book.setBestRank((Integer) bookData.get("bestRank"));
        book.setPubdate(getLocalDate(bookData.get("pubDate")));
        book.setAddDate(LocalDate.now());
        book.setIsNewBook(isNew);
        book.setRecommended(recommend);
        bookRepository.save(book);
    }

    private void updateBook(Map<String, Object> bookData, Boolean isNew, Boolean recommend) {
        //  book정보를 오늘자 정보로 db에 업데이트 하는 함수
        Book book = bookRepository.findByIsbn((String) bookData.get("isbn"));
        book.setTitle((String) bookData.get("title"));
        book.setAuthor((String) bookData.get("author"));
        book.setPlot((String) bookData.get("plot"));
        book.setIsbn((String) bookData.get("isbn"));
        book.setIsbn13((String) bookData.get("isbn13"));
        book.setImageUrl((String) bookData.get("cover"));
        book.setPublisher((String) bookData.get("publisher"));
        book.setPricestandard((Integer) bookData.get("priceStandard"));
        book.setBestRank((Integer) bookData.get("bestRank"));
        book.setPubdate(getLocalDate(bookData.get("pubDate")));
        book.setAddDate(LocalDate.now());
        book.setIsNewBook(isNew);
        book.setRecommended(recommend);
        bookRepository.save(book);
    }
    private BookDTO createBookDTO(Map<String, Object> bookData) {
        try {
            return BookDTO.builder()
                    .title((String) bookData.get("title"))
                    .author((String) bookData.get("author"))
                    .plot((String) bookData.get("plot"))
                    .isbn((String) bookData.get("isbn"))
                    .isbn13((String) bookData.get("isbn13"))
                    .imageUrl((String) bookData.get("cover"))
                    .publisher((String) bookData.get("publisher"))
                    .priceStandard((Integer) bookData.get("priceStandard"))
                    .bestRank((Integer) bookData.get("bestRank"))
                    .pubDate(getLocalDate(bookData.get("pubDate")))
                    .build();
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return null;
        }
    }

    private LocalDate getLocalDate(Object dateObj) {
        //  String으로 받은 날짜 데이터를 LocalDate타입으로 변경해주는 함수
        String dateString = (String) dateObj;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate pubDate = LocalDate.parse(dateString, formatter);
        return pubDate;
    }
    public List<List<String>> getAuthorListList(Book book) {
        String author = book.getAuthor();
        String[] authors = new String[]{};
        if(!author.isEmpty()) {
            authors = author.split(",");
        }

        List<String> authorsName = new ArrayList<>();
        List<String> authorsRole = new ArrayList<>();

        for (String actorList : authors) {
            int idx = actorList.indexOf('(');
            if (idx != -1) {
                authorsName.add(actorList.substring(0, idx).trim());
                authorsRole.add(actorList.substring(idx).trim());
            }
        }

        List<String> actorList = new ArrayList<>();
        for(int i = 0; i < authorsName.size(); i++){
            actorList.add(authorsName.get(i));
            actorList.add(authorsRole.get(i));
        }

        List<List<String>> authorListList = new ArrayList<>();
        int chunkSize = 16;
        int totalElements = actorList.size();

        for (int i = 0; i < (totalElements + chunkSize - 1) / chunkSize; i++) {
            int start = i * chunkSize;
            int end = Math.min((i + 1) * chunkSize, totalElements);
            authorListList.add(actorList.subList(start, end));
        }

        return authorListList;
    }
    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }


    public Page<Book> getBookList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("title"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));

        return bookRepository.findAllByBookKeyword(kw, pageable);
    }
}
