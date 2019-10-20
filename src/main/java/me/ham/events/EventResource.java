package me.ham.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Getter
//public class EventResource extends ResourceSupport {
//
//    @JsonUnwrapped
//    private Event event;
//
//    public EventResource(Event event) {
//        this.event = event;
//    }
//}
public class EventResource extends Resource<Event> {

    public EventResource(Event event, Link... links) {
        super(event, links);

//        TypeSafe하지 않음. EventController의 mapping 혹은 port가 바뀐다면? 그에 맞게 다시 변경이 필요해짐.
//        add(new Link("http://localhost:8080/api/events/"+event.getId());
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}


