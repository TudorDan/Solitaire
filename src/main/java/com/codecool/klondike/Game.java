package com.codecool.klondike;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game extends Pane {

    private List<Card> deck;

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 2;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;
    private static int victoryCounter = 0;

    private static Stage stage = null;


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
                if (validPile.getPileType() == Pile.PileType.FOUNDATION) {
                    victoryCounter++;
                    if (isGameWon()) {
                        showGameWonDialog();
                    }
                }
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
                    showGameWonDialog();
                }
            }
        } else {
            if (draggedCards != null) {
                draggedCards.forEach(MouseUtil::slideBack);
            }
            draggedCards = null;
        }
    };

    private void showGameWonDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);

        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("CONGRATULATIONS!"));

        Button restartButton = new Button("PLAY AGAIN");
        restartButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                restartGame();
                dialog.close();
            }
        });

        Button closeGameButton = new Button("EXIT");
        closeGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                exitGame();
            }
        });

        StackPane layout = new StackPane();
        layout.getChildren().add(dialogVbox);
        layout.getChildren().add(restartButton);
        layout.getChildren().add(closeGameButton);

        layout.setAlignment(dialogVbox, Pos.TOP_CENTER);
        layout.setAlignment(restartButton, Pos.CENTER_LEFT);
        layout.setAlignment(closeGameButton, Pos.CENTER_RIGHT);

        Scene dialogScene = new Scene(layout, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    void restartGame(){
        getChildren().remove(discardPile);
        List<Card> allCards = getAllCards();
        List<Pile> allPiles = getAllPiles();
        for (Card card : allCards) {
            getChildren().remove(card);
        }
        for (Pile pile : allPiles) {
            getChildren().remove(pile);
        }


        Klondike main = new Klondike();
        main.start(stage);
    }

    public List<Pile> getAllPiles() {
        List<Pile> allPiles = Stream.concat(foundationPiles.stream(), tableauPiles.stream()).collect(Collectors.toList());
        allPiles.add(stockPile);
        allPiles.add(discardPile);
        return allPiles;
    }

    public List<Card> getAllCards() {
        List<Card> allCards = new ArrayList<>();
        List<Pile> allPiles = getAllPiles();

        for (Pile pile : allPiles) {
            ObservableList<Card> cards = pile.getCards();
            for (Card card : cards) {
                allCards.add(card);
            }
        }

        return allCards;
    }

    void exitGame() {
        Platform.exit();
    }

    

    public boolean isGameWon() {
        return victoryCounter == 52;
    }

    public Game(Stage primaryStage) {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
        stage = primaryStage;

//      Automatic game completion TEST
//        startGameAutocompletion();
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
        stockPile.setLayoutY(35);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(290);
        discardPile.setLayoutY(35);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(35);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(305);
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

    public void setCardBacks(String imageURL) {
        List<Card> allCards = getAllCards();
        for (Card card : allCards) {
            card.setCardBack(imageURL);
        }
    }

//  Testing
    private void startGameAutocompletion() throws InterruptedException {
        boolean deadend = false;
        while (!isGameWon() && !deadend) {
            System.out.println(victoryCounter);
            for (Pile tableauPile : tableauPiles) {
                ObservableList<Card> pileCards = tableauPile.getCards();
                List<Card> faceUpPileCards = new ArrayList<>();

                for (Card card : pileCards) {
                    if (!card.isFaceDown()) {
                        faceUpPileCards.add(card);
                    }
                }

                for (Card faceUpCard : faceUpPileCards) {
                    for (Pile tPile : tableauPiles) {
                        if (isMoveValid(faceUpCard, tPile) && tPile != tableauPile) {
                            draggedCards = FXCollections.observableArrayList();
                            int indexOfCard = tableauPile.getIndexOfCard(faceUpCard);
                            for (int i = indexOfCard; i < tableauPile.numOfCards(); i++) {
                                draggedCards.add(tableauPile.getCard(i));
                            }

                            for (Card draggedCard : draggedCards) {
                                if (draggedCard != null) {
                                    draggedCard.moveToPile(tPile);
                                    Thread.sleep(500);
                                }
                            }
                        }
                    }
                }

                Card topCard = tableauPile.getTopCard();
                if (topCard != null) {
                    for (Pile fPile : foundationPiles) {
                        if (isMoveValid(topCard, fPile)) {
                            topCard.moveToPile(fPile);
                            victoryCounter++;
                            if (isGameWon()) {
                                return;
                            }
                            Thread.sleep(500);
                        }
                    }
                }
            }

            Card stockPileTopCard = stockPile.getTopCard();
            try {
                stockPileTopCard.moveToPile(discardPile);
                stockPileTopCard.flip();
            } catch (NullPointerException e) {
                ObservableList<Card> discardPileCards = discardPile.getCards();
                System.out.println("Stock Pile Empty");
                try {
                    for (Card card : discardPileCards) {
                        card.flip();
                        card.moveToPile(stockPile);
                        Thread.sleep(500);
                    }
                } catch (ConcurrentModificationException exception) {
                    System.out.println("Error in refilling Stock Pile from Discard Pile");
                }
            }
            boolean movedStockPileTopCard = false;
            try {
                for (Pile fPile : foundationPiles) {
                    if (isMoveValid(stockPileTopCard, fPile)) {
                        stockPileTopCard.moveToPile(fPile);
                        movedStockPileTopCard = true;
                        Thread.sleep(500);
                        victoryCounter++;
                        if (isGameWon()) {
                            return;
                        }
                    }
                }
            } catch (NullPointerException exception) {
                System.out.println("Stock Pile top card is null");
            }

            if (!movedStockPileTopCard) {
                for (Pile tPile : tableauPiles) {
                    try {
                        if (isMoveValid(stockPileTopCard, tPile)) {
                            stockPileTopCard.moveToPile(tPile);
                            Thread.sleep(500);

                        }
                    } catch (NullPointerException exception) {
                        System.out.println("Stock Pile top card is null");
                    }
                }
            }


//            deadend = true;
        }
    }
}
