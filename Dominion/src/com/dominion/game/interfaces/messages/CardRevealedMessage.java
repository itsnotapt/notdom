package com.dominion.game.interfaces.messages;

import com.dominion.game.Player;
import com.dominion.game.cards.Card;
import com.dominion.game.interfaces.PlayerInterface;

public class CardRevealedMessage implements PlayerInterfaceMessage {

	private Player player;
	private Card card;
	
	public CardRevealedMessage(Player player, Card card) {
		this.player = player;
		this.card = card;
	}
	
	@Override
	public void invoke(PlayerInterface playerInterface) {		
		playerInterface.notifyCardRevealed(player, card);
	}
}