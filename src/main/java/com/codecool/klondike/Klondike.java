package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game(primaryStage);
        game.setTableBackground(new Image("/table/green.png"));

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        Menu menuGame = new Menu("Game");
        MenuItem restart = new MenuItem("Restart");
        restart.setOnAction(e -> {
            game.restartGame();
        });
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            game.exitGame();
        });
        menuGame.getItems().add(restart);
        menuGame.getItems().add(exit);

        Menu backgroundMenu = new Menu("Background");
        MenuItem dark = new MenuItem("Dark");
        dark.setOnAction(e -> {
            game.setTableBackground(new Image("/table/dark.jpeg"));
        });
        MenuItem light = new MenuItem("Light");
        light.setOnAction(e -> {
            game.setTableBackground(new Image("/table/light.jpg"));
        });
        MenuItem green = new MenuItem("Green");
        green.setOnAction(e -> {
            game.setTableBackground(new Image("/table/green.png"));
        });
        MenuItem blue = new MenuItem("Blue");
        blue.setOnAction(e -> {
            game.setTableBackground(new Image("/table/blue.jpg"));
        });
        MenuItem red = new MenuItem("Red");
        red.setOnAction(e -> {
            game.setTableBackground(new Image("/table/red.jpg"));
        });
        MenuItem wood = new MenuItem("Wood");
        wood.setOnAction(e -> {
            game.setTableBackground(new Image("/table/wood.jpg"));
        });
        backgroundMenu.getItems().add(dark);
        backgroundMenu.getItems().add(light);
        backgroundMenu.getItems().add(green);
        backgroundMenu.getItems().add(blue);
        backgroundMenu.getItems().add(red);
        backgroundMenu.getItems().add(wood);

        Menu cardBacks = new Menu("Card Backs");
        MenuItem classic = new MenuItem("Classic");
        classic.setOnAction(e -> {
            game.setCardBacks("/card_images/classic.jpg");
        });
        MenuItem redBack = new MenuItem("Red");
        redBack.setOnAction(e -> {
            game.setCardBacks("/card_images/card_back.png");
        });
        MenuItem blackWhite = new MenuItem("Black & White");
        blackWhite.setOnAction(e -> {
            game.setCardBacks("/card_images/black_white.png");
        });
        MenuItem fantasy = new MenuItem("Fantasy");
        fantasy.setOnAction(e -> {
            game.setCardBacks("/card_images/fantasy.jpg");
        });
        MenuItem gorilla = new MenuItem("Gorilla");
        gorilla.setOnAction(e -> {
            game.setCardBacks("/card_images/gorilla.png");
        });
        MenuItem nouveau = new MenuItem("Nouveau Bourgogne");
        nouveau.setOnAction(e -> {
            game.setCardBacks("/card_images/nouveau_bourgogne.png");
        });
        MenuItem prototype = new MenuItem("Prototype");
        prototype.setOnAction(e -> {
            game.setCardBacks("/card_images/prototype.jpg");
        });

        cardBacks.getItems().add(classic);
        cardBacks.getItems().add(redBack);
        cardBacks.getItems().add(blackWhite);
        cardBacks.getItems().add(fantasy);
        cardBacks.getItems().add(gorilla);
        cardBacks.getItems().add(nouveau);
        cardBacks.getItems().add(prototype);

        menuBar.getMenus().add(menuGame);
        menuBar.getMenus().add(backgroundMenu);
        menuBar.getMenus().add(cardBacks);

        game.getChildren().add(menuBar);
    }

}
