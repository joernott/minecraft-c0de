package de.joernott.c0de.core.line;

import java.util.ArrayList;
import java.util.List;

/** The lines of a sheet, starting at z=0. Lines that are not stored are empty. */
public record Sheet(List<Line> lines) {
    public Sheet {
        List<Line> byZ = new ArrayList<>();
        for (Line line : lines) {
            while (byZ.size() < line.z()) {
                byZ.add(new Line(byZ.size(), List.of()));
            }
            if (line.z() < byZ.size()) {
                throw new IllegalArgumentException("Two lines with z=" + line.z());
            }
            byZ.add(line);
        }
        lines = List.copyOf(byZ);
    }

    /** The line at z; an empty line if there is none. */
    public Line line(int z) {
        return z >= 0 && z < lines.size() ? lines.get(z) : new Line(z, List.of());
    }

    /** Number of stored lines (the highest z plus one). */
    public int size() {
        return lines.size();
    }
}
