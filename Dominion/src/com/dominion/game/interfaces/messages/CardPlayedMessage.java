package com.dominion.game.interfaces.messages;

import com.dominion.game.Player;
import com.dominion.game.cards.Card;
import com.dominion.game.interfaces.PlayerInterface;

public class CardPlayedMessage implements NotifyMessage {

	private Player player;
	private Card card;
	
	public CardPlayedMessage(Player player, Card card) {
		this.player = player;
		this.card = card;
	}
	
	@Override
	public void notify(PlayerInterface playerInterface) {		
		playerInterface.notifyCardPlayed(player, card);
	}
}
