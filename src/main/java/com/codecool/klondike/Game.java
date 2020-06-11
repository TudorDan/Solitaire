package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck;

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;
    private static int victoryCounter = 0;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile currentPile = card.getContainingPile();

//      If click event occurs on the Stock Pile, just move the top card onto discard pile
        if (currentPile.getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
//       else detect double click on other piles; if click, do nothing; if double click find a suitable pile for the double clicked card
        } else {
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                autoMoveCard(card, currentPile);
            }
        }
    };

    private void autoMoveCard(Card card, Pile currentPile) {
        if (currentPile.getPileType() == Pile.PileType.TABLEAU || currentPile.getPileType() == Pile.PileType.DISCARD) {
            Pile validPile = findValidAutoMovePile(card, currentPile);
            if (validPile != null) {
                card.moveToPile(validPile);
            }
        }
    }

    private Pile findValidAutoMovePile(Card card, Pile currentPile) {
        for (Pile fPile : foundationPiles) {
            if (isMoveValid(card, fPile) && card == currentPile.getTopCard()) {
                return fPile;
            }
        }

        for (Pile tPile : tableauPiles) {
            if (isMoveValid(card, tPile) && card == currentPile.getTopCard()) {
                return tPile;
            }
        }

        return null;
    }

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();

        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.FOUNDATION)
            return;
        if (card.isFaceDown())
            return;

        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        //create list of dragged cards
        draggedCards = FXCollections.observableArrayList();
        int indexOfCard = activePile.getIndexOfCard(card);
        for (int i = indexOfCard; i < activePile.numOfCards(); i++) {
            draggedCards.add(activePile.getCard(i));
        }

        //move on screen all dragged cards
        for (Card cd : draggedCards) {
            cd.getDropShadow().setRadius(20);
            cd.getDropShadow().setOffsetX(10);
            cd.getDropShadow().setOffsetY(10);

            cd.toFront();
            cd.setTranslateX(offsetX);
            cd.setTranslateY(offsetY);
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards == null)
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);

        //TODO = handle foundation destination
        if (pile != null) {
            handleValidMove(card, pile);
        } else if (pile == null) {
            pile = getValidIntersectingPile(card, foundationPiles);
            if (pile == null) {
                if (draggedCards != null) {
                    draggedCards.forEach(MouseUtil::slideBack);
                }
                draggedCards = null;
            } else {
                handleValidMove(card, pile);
                victoryCounter++;
                if (isGameWon()) {
//                    SHOW POPUP
                }
            }
        } else {
            if (draggedCards != null) {
                draggedCards.forEach(MouseUtil::slideBack);
            }
            draggedCards = null;
        }
    };

    public boolean isGameWon() {
        return victoryCounter == 52;
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);

    }

    public void refillStockFromDiscard() {
        //If the Stock becomes empty, turn the entire discard pile over and make it the new Stock.
        while (!discardPile.isEmpty()) {
            Card card = discardPile.getTopCard();
            card.flip();
            card.moveToPile(stockPile);
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType() == Pile.PileType.TABLEAU) {
            if (destPile.isEmpty())
                return card.getRank() == Rank.KING;
            else {
                Card topcard = destPile.getTopCard();
                return !card.getSuit().getColor().equals(topcard.getSuit().getColor()) &&
                        card.getRank().ordinal() + 1 == topcard.getRank().ordinal();
            }
        }
        if (destPile.getPileType() == Pile.PileType.FOUNDATION) {
            if (destPile.isEmpty())
                return card.getRank() == Rank.ACE;
            else {
                Card topcard = destPile.getTopCard();
                return card.getSuit().getName().equals(topcard.getSuit().getName()) &&
                        card.getRank().ordinal() - 1 == topcard.getRank().ordinal();
            }
        }
        return false;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }

        System.out.println(msg);

        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards = null;
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();

        //deal cards  to initial tableauPiles
        int numberOfCards = 0;
        for (Pile pile : tableauPiles) {
            numberOfCards++;
            for (int i = 1; i <= numberOfCards; i++) {
                Card card = deckIterator.next();
                pile.addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
            }
            pile.getTopCard().flip();
        }

        //deal remaining cards to stockPile
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
