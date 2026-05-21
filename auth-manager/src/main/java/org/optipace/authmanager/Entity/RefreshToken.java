package org.optipace.authmanager.Entity;


import com.sun.tools.attach.AgentInitializationException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Data
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "refresh_token")
    public String refreshToken;

    @Column(name= "created_datetime")
    public LocalDateTime createdDatetime;

    @Column(name= "expire_datetime")
    public LocalDateTime expireDatetime;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    public User user;



}
