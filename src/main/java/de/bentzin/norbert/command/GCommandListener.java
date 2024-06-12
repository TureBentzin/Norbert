package de.bentzin.norbert.command;

import de.bentzin.norbert.Bot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class GCommandListener extends ListenerAdapter {

    @NotNull
    public static final Logger logger = LoggerFactory.getLogger(GCommandListener.class);

    @NotNull
    private final Set<GCommand> commandSet = new HashSet<>();

    public void register(@NotNull GCommand gCommand) {
        commandSet.add(gCommand);
        logger.info("Registered command {} from class: {}", gCommand.getName(), gCommand.getClass().getName());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        logger.info("Received command {} from {}", event.getCommandString(), event.getUser().getName());
        String name = event.getName();
        for (GCommand gCommand : commandSet) {
            if (gCommand.getName().equals(name)) {
                logger.info("{} executed command {} in {}", event.getUser().getName(), name, event.getChannel().getName());
                try {
                    gCommand.callCommand(event);
                } catch (Exception e) {
                    logger.error("Error while executing command {}", name);
                    logger.error("Error: ", e);
                    event.getInteraction().getHook().editOriginal("An error occurred while executing the command! I will restart. You can try to execute the command again after im online again!").queue();
                    Bot.shutdownAsync(Bot.RESTART_ERROR);
                }

                return;
            }
        }
    }

    public void updateJDA(@NotNull JDA jda) {
        jda.updateCommands().addCommands(commandSet.stream().map(GCommand::getCommandData).toList()).queue();
    }


}
