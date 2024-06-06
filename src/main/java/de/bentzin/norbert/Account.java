package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Represents an account in the database
 *
 * @param displayName name to show
 * @param discordID discord id or -1 of unknown or undefined
 * @param matr_nr the matriculation number
 */
public record Account(@NotNull String displayName,
                      @Range(from = -1, to = Integer.MAX_VALUE) long discordID,
                      @Range(from = 0, to = Integer.MAX_VALUE) int matr_nr) {
    public static Account fromMatrNr(@Range(from = 0, to = Integer.MAX_VALUE) int matr_nr) {
        return new Account(Integer.toString(matr_nr),-1, matr_nr);
    }
}
