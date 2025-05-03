package ru.ledvanov.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown=true)
public class PlaceDto {
    private String title;
    private String address;
    private String subway;
    private LocationDto coords;
}
