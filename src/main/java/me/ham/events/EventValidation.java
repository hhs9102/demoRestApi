package me.ham.events;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

@Component
public class EventValidation {

    public void validate(EventDto eventDto, Errors errors){
        if(eventDto.getBasePrice() > eventDto.getMaxPrice()
            && eventDto.getMaxPrice() > 0
        ){
            errors.rejectValue("basePrice", "wrongValue", "basePrice is wrong.");
            errors.rejectValue("maxPrice", "wrongValue", "maxPrice is wrong.");
        }else if(eventDto.getEndEventDateTime().isBefore(eventDto.getBeginEnrollmentDateTime())
        ||eventDto.getEndEventDateTime().isBefore(eventDto.getCloseEnrollmentDateTime())
        ||eventDto.getEndEventDateTime().isBefore(eventDto.getBeginEnrollmentDateTime())
        ){
            errors.rejectValue("endEventDateTime", "wrongValue", "endEventDateTime is wrong.");
        }
    }

    public static void main(String[] args) {

        @Getter
        class LocalDateTimeExample {
            LocalDateTime start = LocalDateTime.of(2019,1,1,12,0, 0);
            LocalDateTime end = LocalDateTime.of(2015,1,1,12,0, 0);
            LocalDateTime none;
        }
        LocalDateTimeExample localDateTimeExample = new LocalDateTimeExample();

        if(localDateTimeExample.getNone().isBefore(localDateTimeExample.start)){
            System.out.println("It is wrong.");
        }
    }
}
