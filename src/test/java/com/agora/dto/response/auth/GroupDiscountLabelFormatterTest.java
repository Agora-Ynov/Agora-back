package com.agora.dto.response.auth;

import com.agora.enums.group.DiscountType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GroupDiscountLabelFormatterTest {

    @Test
    void format_nullOrNone_shouldReturnPleinTarif() {
        assertThat(GroupDiscountLabelFormatter.format(null, 0)).isEqualTo("Plein tarif");
        assertThat(GroupDiscountLabelFormatter.format(DiscountType.NONE, 10)).isEqualTo("Plein tarif");
    }

    @Test
    void format_percentage() {
        assertThat(GroupDiscountLabelFormatter.format(DiscountType.PERCENTAGE, 15)).isEqualTo("Réduction 15%");
    }

    @Test
    void format_fixedAmount() {
        assertThat(GroupDiscountLabelFormatter.format(DiscountType.FIXED_AMOUNT, 20)).isEqualTo("Réduction 20 €");
    }

    @Test
    void format_fullExempt() {
        assertThat(GroupDiscountLabelFormatter.format(DiscountType.FULL_EXEMPT, 0)).isEqualTo("Exonération totale");
    }
}
