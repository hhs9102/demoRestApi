package me.ham.events;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of= "id")
@Getter
@Setter
@Entity
public class Event {
    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;

    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;

    public void update() {
        if(this.basePrice == 0 && this.maxPrice ==0){
            this.free = true;
        }else{
            this.free = false;
        }

        if(location != null && !"".equals(this.location)){
            this.offline = true;
        }else{
            this.offline = false;
        }
    }
}
