package ru.ledvanov.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ru.ledvanov.entity.enums.EventCategory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(generator = "uuid-hibernate-generator")
    @GenericGenerator(name = "uuid-hibernate-generator", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    @Column(name = "location", length = 100, nullable = false)
    private String location;
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    @Column(name = "description", length = 500)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private EventCategory category;
}
