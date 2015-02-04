package com.dominion.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.dominion.game.actions.CardAction;
import com.dominion.game.cards.ActionCard;
import com.dominion.game.cards.Card;
import com.dominion.game.cards.ReactionCard;
import com.dominion.game.cards.TreasureCard;
import com.dominion.game.cards.VictoryCard;
import com.dominion.game.cards.basic.CopperCard;
import com.dominion.game.cards.basic.CurseCard;
import com.dominion.game.interfaces.PlayerInterface;
import com.dominion.game.interfaces.messages.CardGainedMessage;
import com.dominion.game.interfaces.messages.CardPlayedMessage;
import com.dominion.game.visitors.VictoryPointCounterCardVisitor;

public class Player {	
	private final static int NUM_CARDS_IN_HAND = 5;
	private PlayerBroadcaster broadcast;
	private final CardDeck cardDeck = new CardDeck();
	private final CardHand cardHand = new CardHand();
	
	private final DiscardPile discardPile = new DiscardPile();
	private GameBoard gameBoard;	
	private boolean immune;
	private Collection<Player> otherPlayers;
	
	private final PlayArea playArea = new PlayArea();
	
	private final PlayerInterface playerInterface;
	private String playerName;
	
	private TurnState turnState;
	
	/**
	 * Constructor
	 * @param playerInterface
	 */
	public Player(PlayerInterface playerInterface) {
		this.playerInterface = playerInterface;
	}
	
	public void actionEndGameScore(int score) {
		System.out.println(cardDeck.getCards());
		playerInterface.updateScore(score);
	}

	/**
	 * Add multiple cards to the discard pile
	 * @param cards
	 */
	public void addCardsToDiscardPile(Collection<Card> cards) {
		discardPile.addCards(cards);
		
		notifyOfDiscard();
	}

	/**
	 * Move a card into the hand
	 * @param card
	 */
	public void addCardToHand(Card card) {
		cardHand.addCard(card);
		
		notifyOfHand();
	}
	
	public int countCurseCardsInDeck() {
		int count = 0;
		for (Card c: cardDeck.getCards())
		{
			if (c instanceof CurseCard) {
				count++;
			}
		}
		return count;
	}
		
	/** 
	 * Counts the total number of victory points in the card deck for the
	 * player.
	 * 
	 * @param player
	 * @return
	 */
	public int countVictoryPointsInCardDeck() {
		VictoryPointCounterCardVisitor victoryPointCounterCardVisitor = new VictoryPointCounterCardVisitor();
		
		// Necessary for gardens card
		int cardDeckSize = cardDeck.getCards().size();		
		victoryPointCounterCardVisitor.setNumberOfCards(cardDeckSize);
		
		for (Card card : cardDeck.getCards()) {
			card.accept(victoryPointCounterCardVisitor);
		}
		
		return victoryPointCounterCardVisitor.getVictoryPoints();
	}
	
	/**
	 * Move a card from hand to the discard pile
	 * @param card
	 */
	public void discardCardFromHand(Card card) {
		cardHand.removeCard(card);
		discardPile.addCard(card);
		
		notifyOfHand();
		notifyOfDiscard();
	}

	/**
	 * Discard current hand into the discard pile
	 */
	public void discardHand() {
		discardPile.addCards(cardHand.clearHand());

		notifyOfHand();
		notifyOfDiscard();
	}
	
	/**
	 * Move all cards from the play area into the discard deck
	 */
	public void discardPlayArea() {
		discardPile.addCards(playArea.clearPlayArea());
		
		notifyOfDiscard();
		notifyOfPlayArea();
	}

	public void displayCardDeck() {
		HashMap<String, Integer> cardTally = new HashMap<String, Integer>();
		
		for (Card c: cardDeck.getCards()) {
			if (!cardTally.containsKey(c.getName())) {
				cardTally.put(c.getName(), 0);
			}
			cardTally.put(c.getName(), cardTally.get(c.getName())+1);
		}
		System.out.println(cardTally);
	}

	/**
	 * Take a card from the deck. If there are not enough cards in their Deck, 
	 * they draws as many as they can, shuffles their Discard pile to form a 
	 * new face-down Deck, and then draws another card.
	 */
	public Card drawCard() {
		// Check if deck is empty and the discard pile needs to be reshuffled
		if (cardDeck.isEmpty()) {
			moveDiscardPileToCardDeck(); 
			
			// No more cards to draw
			if (cardDeck.isEmpty()) {
				return null;
			}
			
			cardDeck.shuffle();
		}
		
		Card card = cardDeck.drawCard();

		notifyOfCardDeck();
		
		return card;
	}

	public void drawCardToHand() {
		Card card = drawCard();
		if (card != null) {
			cardHand.addCard(card);
			notifyOfHand();
		}		
	}

	/**
	 * The player draws a new hand of 5 cards from his Deck.
	 */
	public void drawNewHand() {
		for (int i = 0; i < NUM_CARDS_IN_HAND; i++) {
			drawCardToHand();
		}
	}	
	
	public void gainCardFromSupply(String stack) {
		Card card = gameBoard.removeCardFromSupplyStack(stack);
		// Should only occur for forced cards like witch
		if (card == null) {
			return;
		}
		
		discardPile.addCard(card);
		
		broadcast.notify(new CardGainedMessage(this, card));
		notifyOfSupply();
		notifyOfDiscard();
	}
	
	public void gainCardFromSupplyToDeck(String stack) {
		Card card = gameBoard.removeCardFromSupplyStack(stack);
		cardDeck.addCard(card);
		
		broadcast.notify(new CardGainedMessage(this, card));
		notifyOfSupply();
		notifyOfCardDeck();
	}
	
	public ActionCard getActionCardToPlay() {
		List<Card> cards = getActionCardsFromHand();
		
		// If the player has no action cards, we can end this phase
		if (cards.isEmpty()) {
			return null;
		}
		
		return playerInterface.selectActionCardToPlay(cards);
	}

	public Card getCardToDiscard() {
		if (cardHand.getCards().isEmpty()) {
			return null;
		}
		return playerInterface.selectCardToDiscard(cardHand.getCards());
	}
	
	public Card getCardToGain(int gainCost) {
		List<Card> cards = gameBoard.listCardsToBuy(gainCost);
		Card card = playerInterface.selectCardToBuy(cards);

		return card;
	}
	
	public Card getCardToTrash() {
		if (cardHand.getCards().isEmpty()) {
			return null;
		}
		return playerInterface.selectCardToTrash(cardHand.getCards());
	}
	
	public Card getCopperCardToTrash() {
		List<Card> cards = new LinkedList<Card>();
		
		for (Card card : cardHand.getCards()) {
			if (card instanceof CopperCard) {
				cards.add(card);
			}
		}
		
		if (!cards.isEmpty()) {
			return playerInterface.selectCardToTrash(cards);
		}
		
		return null;
	}

	/**
	 * Returns the current size of the hand
	 * @return
	 */
	public int getHandSize() {
		return cardHand.getSize();
	}

	public Collection<Player> getOtherPlayers() {
		return otherPlayers;
	}
	
	public ReactionCard getReactionCardToPlay() {
		List<Card> cards = new LinkedList<Card>();
		
		for (Card card : cardHand.getCards()) {
			if (card instanceof ReactionCard) {
				cards.add(card);
			}
		}
		
		if (!cards.isEmpty()) {
			return playerInterface.selectReactionCard(cards);
		}
		
		return null;
	}

	public Card getTreasureCardToGain(int gainCost) {
		List<Card> cards = gameBoard.listCardsToBuy(gainCost);
		List<Card> treasureCards = new LinkedList<Card>();
		for (Card c: cards) {
			if (c instanceof TreasureCard) {
				treasureCards.add(c);
			}
		}
		Card card = playerInterface.selectCardToBuy(treasureCards);
		return card;
	}
	
	public Card getTreasureCardToTrash() {
		List<Card> cards = getTreasureCardsFromHand();
		if (cards.isEmpty()) {
			return null;
		}
		return playerInterface.selectCardToTrash(cards);
	}
		
	public Card getVictoryCardToReveal() {
		List<Card> cards = new LinkedList<Card>();
		
		for (Card card : cardHand.getCards()) {
			if (card instanceof VictoryCard) {
				cards.add(card);
			}
		}
		
		if (!cards.isEmpty()) {
			return playerInterface.selectVictoryCardToReveal(cards);
		}
		
		return null;
	}
	
	public void incrementActions(int amount) {
		turnState.incrementActions(amount);
		
		notifyOfTurnState();
	}		
	
	public void incrementBuys(int amount) {
		turnState.incrementBuys(amount);
		
		notifyOfTurnState();
	}
	
	public void incrementCoins(int amount) {
		turnState.incrementCoins(amount);
		
		notifyOfTurnState();
	}
	
	public boolean isImmune() {
		return this.immune;
	}

	/**
	 * Move the card deck to the discard pile
	 */
	public void moveCardDeckToDiscardPile() {
		discardPile.addCards(cardDeck.getCards());
		
		notifyOfDiscard();
		notifyOfCardDeck();
	}


	/**
	 * Move card from the hand to deck e.g. Bureaucrat
	 */
	public void moveCardFromHandToDeck(Card card) {
		cardHand.removeCard(card);
		cardDeck.addCard(card);
		
		notifyOfHand();
		notifyOfCardDeck();
	}
	
	/**
	 * Move the discard pile back into the card deck
	 */
	public void moveDiscardPileToCardDeck() { 
		cardDeck.addCards(discardPile.clearDiscardPile());
		
		notifyOfCardDeck();
		notifyOfDiscard();
	}

	public void notifyOfCardDeck() {
		playerInterface.updateDeck(cardDeck.count());
	}	
	
	public void notifyOfDiscard() {
		// Update with last card added (top of pile)
		Card card = discardPile.getTopCard();
		playerInterface.updateDiscard(card);
	}

	public void notifyOfHand() {
		playerInterface.updateHand(cardHand.getCards());
	}

	public void notifyOfPlayArea() {
		playerInterface.updatePlayArea(playArea.getCards());	
	}

	public void notifyOfSupply() {
		playerInterface.updateSupply(gameBoard.getSupplyStacks());	
	}
	
	public void notifyOfTrash() {
		playerInterface.updateTrashPile(gameBoard.getTrashPile());
	}

	public void notifyOfTurnState() {
		playerInterface.updateTurnState(
				turnState.getNumberOfActions(), 
				turnState.getNumberOfBuys(), 
				turnState.getTotalCoins());
	}

	public void playActionCard(ActionCard actionCard) {

		playCard(actionCard);
		
		// Iterate through actions for action card
		for (CardAction action : actionCard.buildActionList()) {
			action.setPlayer(this);
			action.execute();
		}
	}

	/**
	 * Move card from hand into the play area
	 * @param card
	 */	
	public void playCard(Card card) {
		cardHand.removeCard(card);
		playArea.addCard(card);
		
		broadcast.notify(new CardPlayedMessage(this, card));
		notifyOfHand();
		notifyOfPlayArea();
	}
	
	/**
	 * During buy phase you can play treasure cards
	 * @param card
	 */
	public void playTreasureCard(TreasureCard card) {
		playCard(card);
		
		turnState.incrementCoins(card.getCoinAmount());

		notifyOfTurnState();
	}

	public void playTurn() {
		turnState = new TurnState();
		setImmune(false);
		
		actionPhase();
		buyPhase();
		cleanUpPhase();		
		drawNewHand();	
	}

	public void setBroadcast(PlayerBroadcaster broadcast) {
		this.broadcast = broadcast;
		broadcast.registerInterface(playerInterface);
	}
	
	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}
	
	public void setImmune(boolean immune) {
		this.immune = immune;
	}
	
	public void setOtherPlayers(Collection<Player> otherPlayers) {
		this.otherPlayers = otherPlayers;
	}

	public void trashCard(Card card) {
		cardHand.removeCard(card);
		gameBoard.addToTrashPile(card);
		
		notifyOfHand();
		notifyOfTrash();
	}

	public void trashCardFromPlayArea(Card card) {
		playArea.removeCard(card);
		gameBoard.addToTrashPile(card);
		
		notifyOfPlayArea();
		notifyOfTrash();
	}

	public boolean wantsToPutDeckInDiscard() {
		return playerInterface.chooseIfPutDeckInDiscard();
	}

	public boolean wantsToSetAsideCard(Card card) {
		return playerInterface.chooseIfSetAsideCard(card);
	}

	private void actionPhase() {
		// Continue while the player has actions left
		while(turnState.getNumberOfActions() > 0) {
			notifyOfTurnState();
			
			ActionCard actionCard = getActionCardToPlay();

			// If null, player didn't select a card and wants to end the action phase
			if (actionCard == null) {
				break;
			}
			
			// Play the selected action card
			playActionCard(actionCard);
			
			// Consume on action for this turn
			turnState.decrementActions();
		}
	}
	
	private void buyPhase() {
		
		while(true) {
			
			List<Card> cards = getTreasureCardsFromHand();
			
			// If the player has no treasure cards, we can end this phase
			if (cards.isEmpty()) {
				break;
			}
			
			TreasureCard treasureCard = playerInterface.selectTreasureCardToPlay(cards);
			System.out.println(treasureCard);
			
			// If null, player didn't select any cards and wants to end phase
			if (treasureCard == null) {
				break;
			}

			System.out.println(treasureCard.getName());
			
			playTreasureCard(treasureCard);
		}
		
		while(turnState.getNumberOfBuys() > 0) {
			
			notifyOfTurnState();
			
			List<Card> cards = gameBoard.listCardsToBuy(turnState.getTotalCoins());
			
			Card card = playerInterface.selectCardToBuy(cards);
			
			// If null, player didn't select any cards and wants to end phase
			if (card == null) {
				break;
			} else {
				// Gain a card
				gainCardFromSupply(card.getName());
				
				turnState.decrementCoins(card.getCost());
				turnState.decrementBuys();				
			}
		}		
	}
	
	private void cleanUpPhase() {
		discardPlayArea();
		discardHand();
	}

	private List<Card> getActionCardsFromHand() {
		// Build a collection of action cards the player can play
		List<Card> cards = new LinkedList<Card>();
		for (Card card : cardHand.getCards()) {
			if (card instanceof ActionCard) {
				cards.add(card);
			}
		}
		return cards;
	}

	private List<Card> getTreasureCardsFromHand() {
		// Build a collection of cards the player can play
		List<Card> cards = new LinkedList<Card>();
		for (Card card : cardHand.getCards()) {
			if (card instanceof TreasureCard) {
				cards.add(card);
			}
		}
		return cards;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
