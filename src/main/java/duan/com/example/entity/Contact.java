package duan.com.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Transient;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lienhe")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    public static final String STAFF_INBOX = "STAFF_INBOX";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String sender;

    private String receiver;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Transient
    private String senderName;

    @Transient
    private Boolean isGuest;
}