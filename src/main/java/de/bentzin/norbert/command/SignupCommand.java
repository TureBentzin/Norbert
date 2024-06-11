package de.bentzin.norbert.command;

import de.bentzin.norbert.Account;
import de.bentzin.norbert.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Ture Bentzin
 * @since 11-06-2024
 */
public class SignupCommand extends GCommand {
    public SignupCommand() {
        super("signup", "Sign up to the testat service.");
    }

    @Override
    protected void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        int matrNr = event.getOption("matr").getAsInt();
        String name = event.getOption("name").getAsString();
        event.deferReply(true).queue();
        boolean b = Objects.requireNonNull(Bot.getDatabaseManager())
                .addAccount(new Account(name, event.getUser().getIdLong(), matrNr));
        if (b) {
            event.getHook().editOriginal("Hello " + name + "! My name is Norbert. Welcome to the testat service." +
                    " I will listen for your testat updates under matriculation number \"" + matrNr + "\"!").queue();
            logger.info("User {} signed up ({}) with id: {}!", name, matrNr, event.getUser().getIdLong());
        } else {
            logger.info("User {} tried to sign up, but failed!", event.getUser().getName());
            event.getHook().editOriginal("You are already signed up!").queue();
        }
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return super.getCommandData()
                .addOption(OptionType.INTEGER, "matr", "Your matriculation number", true)
                .addOption(OptionType.STRING, "name", "Your name", true);
    }
}
