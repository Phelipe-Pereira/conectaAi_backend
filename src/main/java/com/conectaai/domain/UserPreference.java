package com.conectaai.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_preferences_user", columnList = "user_id"),
        @Index(name = "idx_user_preferences_key", columnList = "pref_key"),
        @Index(name = "idx_user_preferences_created_at", columnList = "createdAt")
})
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pref_key", nullable = false)
    private String key;

    @Column(name = "pref_value", nullable = false)
    private String value;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
