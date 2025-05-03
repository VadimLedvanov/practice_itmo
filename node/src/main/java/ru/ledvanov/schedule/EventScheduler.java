package ru.ledvanov.schedule;

import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.ledvanov.dao.EventDAO;
import ru.ledvanov.dto.EventDateDto;
import ru.ledvanov.dto.EventItemDto;
import ru.ledvanov.dto.EventResponseDto;
import ru.ledvanov.entity.Event;
import ru.ledvanov.entity.enums.EventCategory;
import ru.ledvanov.service.TextCleanerService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;

@Log4j
@Component
public class EventScheduler {

    private final RestTemplate restTemplate;
    private final EventDAO eventDAO;

    public EventScheduler(RestTemplate restTemplate, EventDAO eventDAO) {
        this.restTemplate = restTemplate;
        this.eventDAO = eventDAO;
    }

    @Scheduled(cron = "${cron.expression}")
    public void fetchEvents() {
        getEvents();
    }

    public String getEvents() {
        return getEventsByCategory("exhibition") + "\n\n" +
                getEventsByCategory("cinema") + "\n\n" +
                getEventsByCategory("concert") + "\n\n" +
                getEventsByCategory("theater") + "\n\n" +
                getEventsByCategory("business-events") + "\n\n" +
                getEventsByCategory("tour") + "\n\n";
    }

    private String getEventsByCategory(String category) {
        String url = String.format("https://kudago.com/public-api/v1.2/events/?fields=title,description,dates,place,site_url&expand=title,description,dates,place,site_url&location=spb&categories=%s&order_by=-publication_date", category);
        String result = "";
        int count = 0;
        int total = 0;
        try {
            EventResponseDto response = restTemplate.getForObject(url, EventResponseDto.class);

            if (response != null && response.results != null) {
                log.info(String.format("Получено %d событий по категории %s.", response.results.size(), category));
                total = response.results.size();
                for (EventItemDto item : response.results) {
                    String title = TextCleanerService.clean(item.getTitle());
                    String description = TextCleanerService.clean(item.getDescription());

                    String subway;
                    String address;
                    String nameAddress;
                    String location = "";
                    String lat = "";
                    String lon = "";
                    if (item.getPlace() != null) {
                        subway = TextCleanerService.clean(item.getPlace().getSubway());
                        address = TextCleanerService.clean(item.getPlace().getAddress());
                        nameAddress = TextCleanerService.clean(item.getPlace().getTitle());

                        location = subway.isEmpty()
                                ? String.format("%s, %s", address, nameAddress)
                                : String.format("метро: %s %s, %s", subway, address, nameAddress);

                        lat = item.getPlace().getCoords().getLat();
                        lon = item.getPlace().getCoords().getLon();
                    }

                    String siteUrl = item.getSite_url();

                    EventDateDto latestDate = item.getDates().stream()
                            .filter(date -> date.getStart_date() != null)
                            .filter(date -> date.getStart_time() != null)
                            .max(Comparator.comparing(EventDateDto::getStart_date)
                                    .thenComparing(EventDateDto::getStart_time))
                            .orElse(null);

                    LocalDate startDate = null;
                    LocalTime startTime = null;

                    if (latestDate != null) {
                        startDate = latestDate.getStart_date();
                        startTime = latestDate.getStart_time();
                    }

                    EventCategory eventCategory = null;
                    switch (category) {
                        case "exhibition":
                            eventCategory = EventCategory.EXHIBITION;
                            break;
                        case "cinema":
                            eventCategory = EventCategory.CINEMA;
                            break;
                        case "concert":
                            eventCategory = EventCategory.CONCERT;
                            break;
                        case "theater":
                            eventCategory = EventCategory.THEATER;
                            break;
                        case "business-events":
                            eventCategory = EventCategory.BUSINESS;
                            break;
                        case "tour":
                            eventCategory = EventCategory.TOUR;
                            break;
                    }

                    eventDAO.save(Event.builder()
                            .name(title)
                            .location(location)
                            .startDate(startDate)
                            .startTime(startTime)
                            .description(description)
                            .category(eventCategory)
                            .lat(lat)
                            .lon(lon)
                            .siteUrl(siteUrl)
                            .build());

                    count++;
                }
            }
            if (response != null && response.results != null) {
                log.info(String.format("✅ В базу данных успешно сохранено %d событий по категории %s", response.results.size(), category));
                result = String.format("✅ В базу данных успешно сохранено %d событий по категории %s", response.results.size(), category);
            }
        } catch (Exception e) {
            log.error("⚠ Ошибка при получении данных: " + e);
            result = String.format("⚠️ Ошибка при получении данных с API: %s\n\nВ базу было добавлено %d мероприятий из %d выборки по категории %s", e, count, total, category);
        }

        return result;
    }
}
