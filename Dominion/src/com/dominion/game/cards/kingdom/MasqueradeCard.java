package com.dominion.game.cards.kingdom;

import java.util.List;

import com.dominion.game.actions.CardAction;
import com.dominion.game.actions.MasqueradeAction;
import com.dominion.game.actions.PlusCardAction;
import com.dominion.game.actions.TrashCardAction;
import com.dominion.game.cards.ActionCard;
import com.dominion.game.cards.Card;
import com.dominion.game.visitors.CardVisitor;

public class MasqueradeCard extends Card implements ActionCard {
	
	private static final int PLUS_CARDS = 1;

	public MasqueradeCard() {
		super("Masquerade", 3);
	}

	@Override
	public void accept(CardVisitor visitor) {
		visitor.visit(this);
	}
	
	@Override
	public List<CardAction> getActionList() {
		List<CardAction> cardActions = super.getActionList();
		
		cardActions.add(new PlusCardAction(PLUS_CARDS));
		cardActions.add(new MasqueradeAction());
		cardActions.add(new TrashCardAction(1, false));
		
		return cardActions;
	}
}
