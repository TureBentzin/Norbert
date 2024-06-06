package de.bentzin.norbert.command;
import de.bentzin.norbert.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Ture Bentzin
 * @since 28-03-2024
 */
public class UpdateCommand extends GCommand {

    public UpdateCommand() {
        super("update", "Execute an update", true);
    }

    @Override
    protected void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply(false).queue();
        Objects.requireNonNull(Bot.getDataManager()).update();
        event.getHook().editOriginal("Update executed!").queue();
    }
}
