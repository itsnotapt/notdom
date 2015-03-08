package com.dominion.game.cards.kingdom;

import java.util.List;

import com.dominion.game.actions.CardAction;
import com.dominion.game.actions.TributeAction;
import com.dominion.game.cards.ActionCard;
import com.dominion.game.cards.Card;
import com.dominion.game.visitors.CardVisitor;

public class TributeCard extends Card implements ActionCard {
	public static final int COST = 5;
	public static final String NAME = "Tribute";

	@Override
	public void accept(CardVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public List<CardAction> getActionList() {
		List<CardAction> cardActions = super.getActionList();
		
		cardActions.add(new TributeAction());
		
		return cardActions;
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
