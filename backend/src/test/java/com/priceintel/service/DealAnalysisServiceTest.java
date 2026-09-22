package com.priceintel.service;

import com.priceintel.api.ProductDtos.DealVerdict;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DealAnalysisServiceTest {
    private final DealAnalysisService service = new DealAnalysisService();

    @Test
    void recognizesHistoricalLowUsingMedianBaseline() {
        var result = service.analyze(new BigDecimal("80.00"), prices("100", "95", "90", "85"));

        assertThat(result.verdict()).isEqualTo(DealVerdict.HISTORICAL_LOW);
        assertThat(result.medianPrice()).isEqualByComparingTo("92.50");
        assertThat(result.discountFromMedianPercentage()).isGreaterThan(13);
    }

    @Test
    void avoidsDealClaimsWhenHistoryIsTooSmall() {
        var result = service.analyze(new BigDecimal("80.00"), prices("100", "90"));

        assertThat(result.verdict()).isEqualTo(DealVerdict.INSUFFICIENT_HISTORY);
        assertThat(result.sampleSize()).isEqualTo(2);
    }

    @Test
    void flagsPricesWellAboveTheMedian() {
        var result = service.analyze(new BigDecimal("125.00"), prices("100", "100", "105", "95"));

        assertThat(result.verdict()).isEqualTo(DealVerdict.ABOVE_TYPICAL);
    }

    private List<BigDecimal> prices(String... values) {
        return java.util.Arrays.stream(values).map(BigDecimal::new).toList();
    }
}
