package com.hmetsallik.corebanking.account.exception;

import com.hmetsallik.corebanking.common.Currency;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateCurrencyException extends RuntimeException {
    public DuplicateCurrencyException(List<Currency> currencies) {
        super("Duplicate currency in request: " + findDuplicates(currencies));
    }

    private static Set<Currency> findDuplicates(List<Currency> currencies) {
        Set<Currency> seen = new HashSet<>();
        Set<Currency> duplicates = new HashSet<>();
        for (Currency currency : currencies) {
            if (!seen.add(currency)) {
                duplicates.add(currency);
            }
        }
        return duplicates;
    }
}
