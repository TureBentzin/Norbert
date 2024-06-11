package de.bentzin.norbert.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class SayCommand extends GCommand {
    public SayCommand() {
        super("say", "Hallo!", true);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.getChannel().sendMessage(Objects.requireNonNull(event.getInteraction().getOption("text")).getAsString()).queue();
        event.getInteraction().reply("Message sent!").setEphemeral(true).queue();
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return super.getCommandData().addOption(OptionType.STRING, "text", "message", true);
    }
}
