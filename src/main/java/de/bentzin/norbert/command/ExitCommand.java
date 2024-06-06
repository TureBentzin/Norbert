package de.bentzin.norbert.command;

import de.bentzin.norbert.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class ExitCommand extends GCommand {
    public ExitCommand() {
        super("exit", "Exit the bot to the launcher!", true);
    }

    @Override
    protected void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        int code = event.getOption("code").getAsInt();
        event.reply("Shutting down after a session that was established for " + Bot.getSessionDurationString()).setEphemeral(true).queue();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Bot.shutdownAsync(code);
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return super.getCommandData().addOption(OptionType.INTEGER, "code", "Exitcode to be sent", true);
    }
}
