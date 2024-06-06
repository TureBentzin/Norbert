package de.bentzin.norbert.command;

import de.bentzin.norbert.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public abstract class GCommand {
    @NotNull
    public final Logger logger;
    @NotNull
    private final String description;
    public boolean admin;
    @NotNull
    private String name;

    public GCommand(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
        admin = false;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public GCommand(@NotNull String name, @NotNull String description, boolean admin) {
        this.name = name;
        this.admin = admin;
        this.description = description;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void callCommand(@NotNull SlashCommandInteractionEvent event) {
        if (admin && !(Bot.isAdmin(event.getInteraction().getUser().getIdLong()))) {
            event.reply("This command is not available!").setEphemeral(true).queue();
        } else {
            if (checkAuthorized(event))
                onSlashCommandInteraction(event);
            else {
                event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
            }
        }

    }

    protected boolean checkAuthorized(@NotNull SlashCommandInteractionEvent event) {
        return true;
    }

    protected abstract void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event);

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public SlashCommandData getCommandData() {
        return Commands.slash(name, description);
    }
}
