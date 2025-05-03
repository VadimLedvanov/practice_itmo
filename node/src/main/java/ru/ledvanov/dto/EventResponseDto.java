package ru.ledvanov.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponseDto {
    public List<EventItemDto> results;
}
