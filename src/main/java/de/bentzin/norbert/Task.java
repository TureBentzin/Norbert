package de.bentzin.norbert;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Task(@NotNull String name, boolean done) {
    public Task {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
    }

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        return name + (done ? " (done)" : "");
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        return obj == this|| obj instanceof Task other && name.equals(other.name);
    }
}
