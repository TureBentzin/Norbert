package de.bentzin.norbert.command;
import de.bentzin.norbert.Bot;

import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class ConnectTestCommand extends GCommand {

    public ConnectTestCommand() {
        super("connecttest", "Let the bot join a voice channel", false);
    }

    @Override
    protected void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ReplyCallbackAction replyCallbackAction = event.deferReply(false);
        replyCallbackAction.queue();
        Channel channel = event.getOption("channel").getAsChannel();
        if (channel.getType() == ChannelType.VOICE && channel instanceof AudioChannel) {
            Bot.getJda().getDirectAudioController().connect((AudioChannel) channel);
            event.getInteraction().getHook().editOriginal("Connected to " + channel.getName()).queue();
        } else {
            event.getInteraction().getHook().editOriginal("Channel is not a voice channel").queue();
        }
    }

    @NotNull
    @Override
    public SlashCommandData getCommandData() {
        return super.getCommandData().addOption(OptionType.CHANNEL, "channel", "The channel to join", true);
    }
}
