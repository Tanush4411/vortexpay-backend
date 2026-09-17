package com.chrizlove.vortexpay.common_lib.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Money {
    private int amountUnits;
    private String currency;

    public static Money of(int amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money inr(int amount, String currency) {
        return new Money(amount, "INR");
    }

    public Money add(Money other) {
    if(!this.currency.equals(other.currency)) throw new IllegalArgumentException("Currency has wrong value");
    return new Money(this.amountUnits+ other.amountUnits, this.currency);
    }

    public Money subtract(Money other) {
        if(!this.currency.equals(other.currency)) throw new IllegalArgumentException("Currency has wrong value");
        return new Money(this.amountUnits- other.amountUnits, this.currency);
    }
}
