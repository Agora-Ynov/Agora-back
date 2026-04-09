package com.agora.dto.response.auth;

import com.agora.enums.group.DiscountType;

public final class GroupDiscountLabelFormatter {

    private GroupDiscountLabelFormatter() {}

    public static String format(DiscountType discountType, int discountValue) {
        if (discountType == null || discountType == DiscountType.NONE) {
            return "Plein tarif";
        }
        return switch (discountType) {
            case FULL_EXEMPT -> "Exonération totale";
            case PERCENTAGE -> "Réduction " + discountValue + "%";
            case FIXED_AMOUNT -> "Réduction " + discountValue + " €";
            case NONE -> "Plein tarif";
        };
    }
}
