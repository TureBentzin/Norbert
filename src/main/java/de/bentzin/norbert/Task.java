package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;

public record Task(@NotNull String name, boolean done) {
    public Task {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
    }

    @Override
    public String toString() {
        return name + (done ? " (done)" : "");
    }
}
