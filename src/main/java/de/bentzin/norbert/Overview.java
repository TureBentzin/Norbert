package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class Overview {


    @NotNull
    private final Account account;

    @NotNull
    private final String identifier;
    @NotNull
    private final Collection<Task> tasks;

    public Overview(@NotNull Account account, @NotNull String identifier, @NotNull Collection<Task> tasks) {
        this.account = account;
        this.identifier = identifier;
        this.tasks = tasks;
    }

    @NotNull
    public Collection<Task> getTasks() {
        return List.copyOf(tasks);
    }

    private boolean done(@NotNull String task) {
        return tasks.stream().filter(t -> t.name().equals(task)).findFirst().map(Task::done)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Task task : tasks) {
            builder.append(task.toString());
        }
        return builder.toString();
    }

    public @NotNull String getIdentifier() {
        return identifier;
    }

    public @NotNull Account getAccount() {
        return account;
    }
}
