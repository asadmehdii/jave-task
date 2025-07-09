package com.biosteel.teams.auth.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import com.biosteel.teams.user.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The VerificationToken Entity. Stores Registration Verification Token data.
 */
@Data
@Entity
@Table(name = "user_verification_token", schema = "biosteel")
@AllArgsConstructor
@ToString
@Accessors(fluent = false, chain = true)
@JsonInclude(Include.NON_NULL)
public class VerificationToken implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The Constant EXPIRATION. */
    private static final int EXPIRATION_MINS = 30 * 1;

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_verification_token_id", updatable = false, nullable = false)
    private UUID userVerificationTokenId;

    /** The token. */
    private String token;

    /** The user. */
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", foreignKey = @ForeignKey(name = "FK_VERIFY_USER"))
    private User user;

    /** The expiry date. */
    private Date expiryDate;

    /**
     * Instantiates a new verification token.
     */
    public VerificationToken() {
        super();
    }

    /**
     * Instantiates a new verification token.
     *
     * @param token the token
     */
    public VerificationToken(final String token) {
        super();
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINS);
    }

    /**
     * Instantiates a new verification token.
     *
     * @param token the token
     * @param user  the user
     */
    public VerificationToken(final String token, final User user) {
        super();
        this.token = token;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINS);
    }

    /**
     * Calculate expiry date.
     *
     * @param expiryTimeInMinutes the expiry time in minutes
     * @return the date
     */
    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    /**
     * Update token.
     *
     * @param token the token
     */
    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION_MINS);
    }

}
