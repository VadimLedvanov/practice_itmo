package ru.ledvanov.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventItemDto {
    private String title;
    private String description;
    private List<EventDateDto> dates;
    private PlaceDto place;
    private String site_url;

}
