package com.dominion.game.actions;

import com.dominion.game.cards.Card;

/**
 * 
 * @author Vomit
 *
 * Trash a card from your hand. Gain a card costing up to 2 Coins more than the trashed card.
 */
public class RemodelAction extends CardAction {
	@Override
	public void execute() {
		Card trashCard = player.getCardToTrash();
		
		player.trashCard(trashCard);
		
		Card card = player.getCardToGain(trashCard.getCost() + 2);
		player.addCardToHand(card);
	}
}
