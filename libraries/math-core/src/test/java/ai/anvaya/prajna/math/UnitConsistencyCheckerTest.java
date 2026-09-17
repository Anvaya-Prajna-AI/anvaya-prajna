package ai.anvaya.prajna.math;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitConsistencyCheckerTest {

    private final UnitConsistencyChecker checker = new UnitConsistencyChecker();

    @Test
    void shouldCheckUnitCompatibility() {
        assertThat(checker.areUnitsCompatible("km", "m")).isTrue();
        assertThat(checker.areUnitsCompatible("hour", "min")).isTrue();
        assertThat(checker.areUnitsCompatible("kg", "g")).isTrue();
        assertThat(checker.areUnitsCompatible("km/h", "m/s")).isTrue();
        assertThat(checker.areUnitsCompatible("km", "hour")).isFalse();
        assertThat(checker.areUnitsCompatible(null, "m")).isTrue();
        assertThat(checker.areUnitsCompatible("sec", null)).isTrue();
        assertThat(checker.areUnitsCompatible("Joules", "Joules")).isTrue();
    }
}
