package me.ham.events;


import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class EventTest {

    @Test
    public void builder(){
        Event event = Event.builder()
                .name("Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean(){
        Event event = new Event();
        String name = "Event";
        event.setName(name);
        String description = "Spring";
        event.setDescription(description);
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

    @Test
    @Parameters({
            "0, 0, true"
            ,"100, 0 , false"
            ,"0, 100, false"
    })
    public void testFree(int basePrice, int maxPrice, boolean isFree){
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    @Test
    @Parameters
    public void testOffline(String location, boolean isOffline){
        Event event = Event.builder()
                .location(location)
                .build();
        //when
        event.update();
        //then
        assertThat(event.isOffline()).isEqualTo(isOffline);
    }
    private Object[] parametersForTestOffline(){
        return new Object[]{
          new Object[]{"스터디룸", true}
          ,new Object[]{null, false}
        };
    }
}