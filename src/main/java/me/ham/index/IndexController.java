package me.ham.index;

import me.ham.events.EventController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class IndexController {

    private final String testUrl = "http://localhost:9000/resilience";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RestTemplate restTemplateLongReadTimeOut;

    @GetMapping("/api")
    public ResourceSupport index(){
        ResourceSupport index = new ResourceSupport();
        index.add(linkTo(EventController.class).withRel("events"));
        return index;
    }

    @GetMapping("/test/readtime")
    public String readTimeOutTest(){
        try{
            String readTimeOut3s = restTemplate.getForObject(testUrl+"/waitting/5000", String.class);
            System.out.println("####1");
            System.out.println(readTimeOut3s);
        }catch (ResourceAccessException e){
            System.out.println(e.getLocalizedMessage());
            System.out.println("리드 타임 아웃 발생");
        }
        String readTimeOut10s = restTemplateLongReadTimeOut.getForObject(testUrl+"/waitting/5000", String.class);

        System.out.println("####2");
        System.out.println(readTimeOut10s);
        System.out.println("####3");

        return "";
    }
}
