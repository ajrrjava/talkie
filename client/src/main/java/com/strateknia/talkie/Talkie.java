package com.strateknia.talkie;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.BiConsumer;

public class Talkie extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Talkie.class);

    private Client<String> userClient;
    private Client<TalkieMessage> client;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Starting messaging client");

        String env = System.getProperty("APP_ENV", "conf/test/");
        userClient = ClientFactory.getKakfaClient(env + "user.properties");
        client = ClientFactory.getKakfaClient(env + "message.properties");

        logger.info("Loading Controller");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();

        TalkieController controller = loader.getController();

        controller.setOnUserAdded(this::onUserAdded);
        controller.setOnSendMessageHandler(this::onSendMessageHandler);

        logger.info("Configuring messaging client");

        Property<String> connectedUser = new SimpleStringProperty();
        controller.connectedUserProperty().bind(connectedUser);

        BiConsumer<Instant, String> addUserCallback = (i, u) -> {
            Platform.runLater(() -> connectedUser.setValue(u));
        };
        userClient.subscribe(CommonConstants.TOPIC_USERS, addUserCallback);

        Property<TalkieMessage> response = new SimpleObjectProperty<>();
        controller.responseProperty().bind(response);

        BiConsumer<Instant, TalkieMessage> messageCallback = (i, m) -> {
            logger.info("Receiving TalkieMessage {}", m);
            Platform.runLater(() ->  response.setValue(m));
        };
        client.subscribe(CommonConstants.TOPIC_MESSAGES, messageCallback);

        logger.info("Loading UI");

        Scene scene = new Scene(root);

        stage.setTitle("Talkie");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public void onUserAdded(String user) {
        logger.info("Publishing User {}", user);
        userClient.publish(CommonConstants.TOPIC_USERS, user);
    }

    public void onSendMessageHandler(TalkieMessage message) {
        logger.info("Publishing TalkieMessage {}", message);
        client.publish(CommonConstants.TOPIC_MESSAGES, message);
    }
}
