package com.demod.dcba;

import java.util.Collection;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class AutoCompleteEvent extends ParamPayloadEvent {

	private final CommandAutoCompleteInteractionEvent event;

	//TODO refactor autocomplete to be its own event that is contextualized by DCBA per parameter, not per command
	private AutoCompleteQuery focusedOption;

	public AutoCompleteEvent(CommandAutoCompleteInteractionEvent event) {
		super(event);
		this.event = event;
		focusedOption = event.getFocusedOption();
	}

	public AutoCompleteQuery getFocusedOption() {
		return focusedOption;
	}

	public void reply(Collection<String> choices) {
		event.replyChoiceStrings(choices).complete();
	}

	public void replyDecimals(Collection<Double> choices) {
		event.replyChoiceDoubles(choices).complete();
	}

	public void replyIntegers(Collection<Long> choices) {
		event.replyChoiceLongs(choices).complete();
	}

	public void replyNamed(Collection<Choice> choices) {
		event.replyChoices(choices).complete();
	}
}
