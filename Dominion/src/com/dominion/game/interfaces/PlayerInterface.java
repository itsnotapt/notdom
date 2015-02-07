package com.dominion.game.interfaces;

import java.util.HashMap;
import java.util.List;

import com.dominion.game.Player;
import com.dominion.game.cards.ActionCard;
import com.dominion.game.cards.Card;
import com.dominion.game.cards.ReactionCard;
import com.dominion.game.cards.TreasureCard;
import com.dominion.game.interfaces.messages.NotifyMessage;

public interface PlayerInterface {
	public boolean chooseIfPutDeckInDiscard();
	public boolean chooseIfSetAsideCard(final Card card);
	public boolean chooseIfTrashCard(final Card card);
	public boolean chooseIfGainCard(final Card card);
	public boolean chooseIfDiscardCard(final Card card);
	public void notifyCardPlayed(Player player, Card card);
	public void notifyCardGained(Player player, Card card);
	public ActionCard selectActionCardToPlay(final List<Card> cards);
	public Card selectCardToBuy(final List<Card> cards);
	public Card selectCardToDiscard(final List<Card> cards);
	public Card selectCardToTrash(final List<Card> cards);
	public ReactionCard selectReactionCard(final List<Card> cards);
	public TreasureCard selectTreasureCardToPlay(final List<Card> cards);
	public Card selectVictoryCardToReveal(final List<Card> cards);
	public void updateDeck(final int numOfCards);
	public void updateDiscard(final Card card);
	public void updateHand(final List<Card> cards);
	public void updateOtherPlayer(final Player player);
	public void updatePlayArea(final List<Card> cards);
	public void updateScore(final int score);
	public void updateSupply(final HashMap<String, List<Card>> supplyStack);
	public void updateTrashPile(final List<Card> cards);
	public void updateTurnState(int numOfActions, int numOfBuys, int numOfCoins);
	public void notifyCardRevealed(Player player, Card card);
}
