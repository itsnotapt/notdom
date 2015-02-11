package com.dominion.game.cards.basic;

import com.dominion.game.cards.Card;
import com.dominion.game.cards.TreasureCard;
import com.dominion.game.visitors.CardVisitor;


public class CopperCard extends Card implements TreasureCard {
	public static final int COST = 0;
	public static final String NAME = "Copper";
	private final int COINS = 1;

	@Override
	public void accept(CardVisitor visitor) {
		visitor.visit(this);		
	}

	@Override
	public int getCoinAmount() {		
		return COINS;
	}
	
	@Override
	public int getCost() {
		return COST;
	}

	@Override
	public String getName() {
		return NAME;
	}	
}
