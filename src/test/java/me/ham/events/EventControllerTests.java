package me.ham.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

//    @MockBean
//    EventRepository eventRepository;

    @Test
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .closeEnrollmentDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .endEventDateTime(LocalDateTime.of(2019, 9, 28, 13,4))
                .basePrice(100)
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
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        ;
    }

    @Test
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
    public void createEven_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }
}
