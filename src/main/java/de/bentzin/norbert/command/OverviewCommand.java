package de.bentzin.norbert.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ture Bentzin
 * @since 11-06-2024
 */
public class OverviewCommand extends GCommand{
    public OverviewCommand() {
        super("overview", "See an overview of the testats!");
    }

    @Override
    protected void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        //TODO: Implement this command
        event.reply("This command is not implemented yet!").setEphemeral(true).queue();
    }

    @Override
    public @NotNull SlashCommandData getCommandData() {
        return super.getCommandData().addOption(OptionType.INTEGER, "matr", "matriculation number", false);
    }
}
