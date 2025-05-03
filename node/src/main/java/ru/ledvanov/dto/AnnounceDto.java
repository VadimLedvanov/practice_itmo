package ru.ledvanov.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnounceDto {
    public enum AnnounceStep {
        ASK_ANNOUNCEMENT_TEXT,
    }

    private AnnounceStep step;
    private String text;
}
