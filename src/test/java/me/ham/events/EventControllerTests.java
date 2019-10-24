package me.ham.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.ham.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EventRepository eventRepository;

//    @MockBean
//    EventRepository eventRepository;

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

        //eventRepository의 save가 호출되면, event를 리턴해라.
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events/")
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

        mockMvc.perform(post("/api/events")
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
                        ) //0부터 시작
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("page").exists())
                        .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                        .andExpect(jsonPath("_links.self").exists())
                        .andExpect(jsonPath("_links.profile").exists())
//                        .andDo(document("query-events"))
                ;
    }

    private void generateEvent(int index){
        Event event = Event.builder()
                .name("test"+index)
                .description("test Event" + index)
                .build();
        this.eventRepository.save(event);
    }
}
