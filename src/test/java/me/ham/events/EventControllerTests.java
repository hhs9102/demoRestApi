package me.ham.events;

import me.ham.accounts.Account;
import me.ham.accounts.AccountRepository;
import me.ham.accounts.AccountRole;
import me.ham.accounts.AccountService;
import me.ham.common.AppProperties;
import me.ham.common.BaseControllerTest;
import me.ham.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTests  extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    @Before
    public void setUp(){
        this.eventRepository.deleteAll();;
        this.accountRepository.deleteAll();;
    }

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .endEventDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .basePrice(20)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("신촌역 윙스터디")
                .build();

        mockMvc.perform(post("/api/events/")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)    //Hypertext Application Language
                    .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Content-Type", MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))   //가격이 있으니까
                .andExpect(jsonPath("offline").value(true)) //location이 있으니까
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
        ;
    }

    private String getBaererToken() throws Exception {
        String username = appProperties.getUserUsername();
        String password = appProperties.getUserPassword();
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build()
                ;

        accountService.saveAccount(account);

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", username)
                .param("password", password)
                .param("grant_type", "password"));

        var responseBody = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser jackson2JsonParser = new Jackson2JsonParser();
        return jackson2JsonParser.parseMap(responseBody).get("access_token").toString();
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .endEventDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("신촌역 윙스터디")
                .free(true)
                .offline(false)
                .build();


        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)    //Hypertext Application Language
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력 값이 잘못되어 있을 때")
    public void createEvent_Bad_Request_individual() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .endEventDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .basePrice(100)     //max(50)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();


        mockMvc.perform(post("/api/events/")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)    //Hypertext Application Language
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있을경우")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("hsham")
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 잘못된 경우에 발생하는 테스트")
    public void createEvent_BadRequest_Wrong_Input() throws Exception {
        //등록종료일이 등록시작일보다 작은경우
        EventDto eventDto = EventDto.builder()
                .name("hsham")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019,10,5,10,5,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2019,10,6,10,5,0))
                .endEventDateTime(LocalDateTime.of(2019,10,3,15,0,0))
                .build();

        this.mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("content[0].objectName").exists())
                .andExpect(jsonPath("content[0].defaultMessage").exists())
                .andExpect(jsonPath("content[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
                .andDo(print())
        ;
    }

    @Test
    @TestDescription("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception{
        IntStream.range(0, 30).forEach(this::generateEvent);

        this.mockMvc.perform(get("/api/events/")
                            .param("page","1")
                            .param("size", "10")
                            .param("sort", "name,DESC")
                        ) // 0부터 시작
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("page").exists())
                        .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                        .andExpect(jsonPath("_links.self").exists())
                        .andExpect(jsonPath("_links.profile").exists())
//                        .andDo(document("query-events"))
                ;
    }

    @Test
    @TestDescription("기존의 이벤트를 하나 조회하기")
    public void getEvent() throws Exception{
        //given
        Event event = this.generateEvent(100);

        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    @TestDescription("없는 이벤트 request시 404 에러")
    public void getEvent404() throws Exception{
        this.mockMvc.perform(get("/api/events/1298371893"))
                .andExpect(status().isNotFound())
        ;
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정하기.")
    public void updateEvent() throws Exception {
        Event event = generateEvent(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(eventName))
                .andExpect(jsonPath("_links.self").exists())
//                .andExpect(jsonPath("_links.profile").exists())
//                .andExpect(jsonPath("_links.update").exists())
        ;
    }

    @Test
    @TestDescription("입력값이 비어 있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        Event event = generateEvent(100);
        EventDto eventDto = new EventDto();

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        Event event = generateEvent(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("존재하지 않는 이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        Event event = generateEvent(100);
        EventDto eventDto = this.modelMapper.map(event, EventDto.class);

        this.mockMvc.perform(put("/api/events/12312983")
                .header(HttpHeaders.AUTHORIZATION, "Bearer" + getBaererToken())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }

    private Event generateEvent(int index){
        Event event = Event.builder()
                        .name("Spring")
                        .description("REST API Development with Spring")
                        .beginEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                        .closeEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                        .endEventDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                        .basePrice(20)
                        .maxPrice(200)
                        .limitOfEnrollment(100)
                        .location("신촌역 윙스터디")
                        .free(false)
                        .offline(true)
                        .eventStatus(EventStatus.DRAFT)
                        .build();
        this.eventRepository.save(event);

        return event;
    }
}
