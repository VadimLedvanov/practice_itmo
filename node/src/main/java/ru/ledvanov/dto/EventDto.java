package ru.ledvanov.dto;

import lombok.Getter;
import lombok.Setter;
import ru.ledvanov.entity.enums.EventCategory;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventDto {
    public enum StepState {
        ASK_EVENT_NAME,
        ASK_EVENT_LOCATION,
        ASK_EVENT_DESCRIPTION,
        ASK_EVENT_DATE_MESSAGE,
        ASK_EVENT_CATEGORY,
        CONFIRM
    }

    private StepState currentStep;
    private String name;
    private String location;
    private String description;
    private LocalDateTime date;
    private EventCategory category;
}
