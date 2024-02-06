package com.korea.MOVIEBOOK.webtoon.webtoonList;
import com.korea.MOVIEBOOK.member.Member;
import com.korea.MOVIEBOOK.payment.Payment;
import com.korea.MOVIEBOOK.webtoon.days.Day;
import com.korea.MOVIEBOOK.webtoon.days.DayService;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayList;
import com.korea.MOVIEBOOK.webtoon.webtoonDayList.WebtoonDayListRepository;
import lombok.RequiredArgsConstructor;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class WebtoonService {
    private final WebtoonRepository webtoonRepository;
    private final DayService dayService;
    private final WebtoonDayListRepository webtoonDayListRepository;


    public List<Long> getWebtoonAPI(String day) {
        List<WebtoonDTO> webtoonDTOList = new ArrayList<>();
        List<Long> webtoonID = new ArrayList<>();

        String date = day.toLowerCase();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl("https://korea-webtoon-api.herokuapp.com/?service=naver&updateDay=" + date).build();


            ResponseEntity<Map> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Map.class);

            ArrayList<Map> webtoonsList = (ArrayList<Map>) resultMap.getBody().get("webtoons");
//            List<Webtoon> webtoonList = new ArrayList<>();
            for (Map<String, Object> webtoonData : webtoonsList) {
                WebtoonDTO webtoonDTO = createWebtoonDTOFromMap(webtoonData);
                Webtoon webtoon = webtoonRepository.findByWebtoonId(webtoonDTO.getWebtoonId());
                if(webtoon == null){
                    webtoonID.add((Long) webtoonData.get("webtoonId"));
                    Webtoon webtoon1 = saveWebtoonFromDTO(webtoonDTO);
                }
                else{
                    Webtoon webtoon1 = findWebtoonByWebtoonId(webtoonDTO.getWebtoonId());
                    Day day1 = this.dayService.findIdByupdateDays(day);
                    WebtoonDayList webtoonDayList = new WebtoonDayList();
                    webtoonDayList.setWebtoonList(webtoon1);
                    webtoonDayList.setWebtoonDay(day1);
                    this.webtoonDayListRepository.save(webtoonDayList);
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println(e.toString());
            // 예외 처리 로직 추가
        } catch (Exception e) {
            System.out.println(e.toString());
            // 예외 처리 로직 추가
        }
        return webtoonID;
    }


    private WebtoonDTO createWebtoonDTOFromMap(Map<String, Object> webtoonData) {
        try {
            return WebtoonDTO.builder()
                    ._id((String) webtoonData.get("_id"))
                    .fanCount((Integer) webtoonData.get("fanCount"))
                    .webtoonId((Long) webtoonData.get("webtoonId"))
                    .title((String) webtoonData.get("title"))
                    .author((String) webtoonData.get("author"))
                    .imageUrl((String) webtoonData.get("img"))
                    .updateDays((List<String>) webtoonData.get("updateDays"))
                    .searchKeyword((String) webtoonData.get("searchKeyword"))
                    .detailUrl((String) webtoonData.get("detailUrl"))
                    .build();
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return null;
        }
    }

    public Webtoon saveWebtoonFromDTO(WebtoonDTO webtoonDTO) {

        Webtoon webtoon = new Webtoon();
//        webtoon.setid(webtoonDTO.getid());
        webtoon.setFanCount(webtoonDTO.getFanCount());
        webtoon.setWebtoonId(webtoonDTO.getWebtoonId());
        webtoon.setTitle(webtoonDTO.getTitle());
        webtoon.setAuthor(webtoonDTO.getAuthor());
        webtoon.setImageUrl(webtoonDTO.getImageUrl());
//        webtoon.setWebtoonDay(day);
//        System.out.println("UpdateDays: " + webtoon.getWebtoonDay()); // 디버깅용 로그
        webtoon.setDetailUrl(webtoonDTO.getDetailUrl());
        return webtoonRepository.save(webtoon);

    }



    public Webtoon findWebtoonByWebtoonId(Long webtoonId) {
        return this.webtoonRepository.findByWebtoonId(webtoonId);
    }

    public List<List<String>> getAuthorListList(Webtoon webtoon) {

        String author = webtoon.getAuthor();
        String[] authors = new String[]{};
        if(!author.isEmpty()) {
            authors = author.split(",");
        }

        List<String> authorName = new ArrayList<>();
        List<String> authorRole = new ArrayList<>();

        for (String authorList : authors) {
            authorName.add(authorList);
            authorRole.add("()");
        }

        List<String> authorList = new ArrayList<>();
        for(int i = 0; i < authorName.size(); i++){
            authorList.add(authorName.get(i));
            authorList.add(authorRole.get(i));
        }

        List<List<String>> actorListList = new ArrayList<>();
        int chunkSize = 16;
        int totalElements = authorList.size();

        for (int i = 0; i < (totalElements + chunkSize - 1) / chunkSize; i++) {
            int start = i * chunkSize;
            int end = Math.min((i + 1) * chunkSize, totalElements);
            actorListList.add(authorList.subList(start, end));
        }
        return actorListList;
    }


    public Page<Webtoon> getWebtoonList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("title"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));

        return webtoonRepository.findAllByWebtoonKeyword(kw, pageable);
    }


//    public Page<Webtoon> getWebtoonPurchase(Member member, int page) {
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("datetime"));
//        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
//
//        return webtoonRepository.findWebtoonPurchase(member, pageable);
//    }


//    public Page<Webtoon> getWebtoonsPurchase(Member member, int page) {
//        List<Sort.Order> sorts = new ArrayList<>();
//        sorts.add(Sort.Order.desc("dateTime"));
//        Pageable pageable = PageRequest.of(page, 8,Sort.by(sorts));
//        return this.webtoonRepository.webtoonPurchase(member, pageable);
//    }

}


