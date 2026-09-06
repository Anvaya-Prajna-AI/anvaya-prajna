package ai.anvaya.prajna.math;

import java.util.Map;

public class UnitConsistencyChecker {

    private static final Map<String, String> BASE_UNITS = Map.ofEntries(
            Map.entry("km", "length"),
            Map.entry("m", "length"),
            Map.entry("cm", "length"),
            Map.entry("mm", "length"),
            Map.entry("hour", "time"),
            Map.entry("h", "time"),
            Map.entry("hr", "time"),
            Map.entry("min", "time"),
            Map.entry("s", "time"),
            Map.entry("sec", "time"),
            Map.entry("kg", "mass"),
            Map.entry("g", "mass"),
            Map.entry("km/h", "speed"),
            Map.entry("m/s", "speed")
    );

    /**
     * Verifies whether target unit is compatible with derived unit (e.g. length/time = speed).
     */
    public boolean areUnitsCompatible(String unit1, String unit2) {
        if (unit1 == null || unit2 == null) {
            return true;
        }
        String u1 = unit1.toLowerCase().trim();
        String u2 = unit2.toLowerCase().trim();
        if (u1.equals(u2)) {
            return true;
        }
        String dim1 = BASE_UNITS.get(u1);
        String dim2 = BASE_UNITS.get(u2);
        return dim1 != null && dim1.equals(dim2);
    }
}
